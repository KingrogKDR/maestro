/**
 * Visual review harness: serves the Wasm build of Maestro and drives it with
 * Playwright across phone and desktop sizes, light and dark, and reduced motion.
 *
 *   ./gradlew :shared:wasmJsBrowserDistribution
 *   cd design && bun install && bunx playwright install chromium && bun run shots
 *
 * Screenshots land in design/shots/ (git-ignored). Compose renders to a canvas,
 * so steps tap by coordinates; they're expressed in CSS px for each viewport.
 */
import { chromium, type Page } from "playwright";
import { mkdirSync } from "node:fs";
import { join, resolve } from "node:path";

const dist = resolve(import.meta.dir, "../shared/build/dist/wasmJs/productionExecutable");
const out = resolve(import.meta.dir, process.env.SHOT_DIR ?? "shots");
mkdirSync(out, { recursive: true });

const server = Bun.serve({
  port: 0,
  async fetch(req) {
    const path = new URL(req.url).pathname;
    const file = Bun.file(join(dist, path === "/" ? "index.html" : path));
    if (!(await file.exists())) return new Response("Not found", { status: 404 });
    const type = path.endsWith(".wasm") ? "application/wasm" : file.type;
    return new Response(file, { headers: { "Content-Type": type } });
  },
});
const url = `http://localhost:${server.port}/`;

type Step = { name: string; run: (page: Page) => Promise<void> };
type Scenario = {
  name: string;
  viewport: { width: number; height: number };
  scale: number;
  mobile: boolean;
  steps: Step[];
};

const settle = (page: Page, ms = 900) => page.waitForTimeout(ms);
const tap = async (page: Page, x: number, y: number, ms?: number) => {
  await page.mouse.click(x, y);
  await settle(page, ms);
};
/** A drag with intermediate moves so the app sees real velocity. */
const drag = async (page: Page, x: number, from: number, to: number, release = true) => {
  await page.mouse.move(x, from);
  await page.mouse.down();
  const steps = 12;
  for (let i = 1; i <= steps; i++) {
    await page.mouse.move(x, from + ((to - from) * i) / steps);
    await page.waitForTimeout(16);
  }
  if (release) await page.mouse.up();
};

const phone: Scenario = {
  name: "phone",
  viewport: { width: 390, height: 844 },
  scale: 3,
  mobile: true,
  steps: [
    { name: "01-albums", run: (p) => settle(p, 0) },
    { name: "02-album", run: (p) => tap(p, 100, 160) },
    { name: "03-playing", run: (p) => tap(p, 100, 418) },
    { name: "04-now-playing", run: (p) => tap(p, 195, 748, 1200) },
    {
      name: "05-sheet-mid-drag",
      run: async (p) => {
        await drag(p, 195, 200, 520, false);
        await settle(p, 100);
      },
    },
    {
      name: "06-sheet-released",
      run: async (p) => {
        await p.mouse.up();
        await settle(p, 1200);
      },
    },
    { name: "07-closed", run: (p) => p.keyboard.press("Escape").then(() => settle(p)) },
    { name: "08-songs", run: (p) => tap(p, 325, 816) },
    { name: "09-artists", run: (p) => tap(p, 195, 816) },
  ],
};

const desktop: Scenario = {
  name: "desktop",
  viewport: { width: 1280, height: 800 },
  scale: 2,
  mobile: false,
  steps: [
    { name: "01-albums", run: (p) => settle(p, 0) },
    { name: "02-album", run: (p) => tap(p, 340, 230) },
    { name: "03-playing", run: (p) => tap(p, 615, 293) },
    { name: "04-now-playing", run: (p) => tap(p, 760, 760, 1200) },
    { name: "05-closed", run: (p) => p.keyboard.press("Escape").then(() => settle(p)) },
    { name: "06-songs", run: (p) => tap(p, 110, 160) },
    {
      // Keyboard focus must show a ring; the mouse clicks above must not.
      name: "07-keyboard-focus",
      run: async (p) => {
        for (let i = 0; i < 4; i++) await p.keyboard.press("Tab");
        await settle(p, 400);
      },
    },
  ],
};

const only = process.argv.slice(2);
const runs = [
  { scenario: phone, scheme: "dark" as const, motion: "no-preference" as const },
  { scenario: phone, scheme: "light" as const, motion: "no-preference" as const },
  { scenario: phone, scheme: "dark" as const, motion: "reduce" as const },
  { scenario: desktop, scheme: "dark" as const, motion: "no-preference" as const },
  { scenario: desktop, scheme: "light" as const, motion: "no-preference" as const },
].filter((r) => only.length === 0 || only.some((o) => `${r.scenario.name}-${r.scheme}-${r.motion}`.includes(o)));

const browser = await chromium.launch();
try {
  for (const { scenario, scheme, motion } of runs) {
    const tag = `${scenario.name}-${scheme}${motion === "reduce" ? "-reduced" : ""}`;
    const context = await browser.newContext({
      viewport: scenario.viewport,
      deviceScaleFactor: scenario.scale,
      isMobile: scenario.mobile,
      hasTouch: scenario.mobile,
      colorScheme: scheme,
      reducedMotion: motion,
    });
    const page = await context.newPage();
    const errors: string[] = [];
    page.on("pageerror", (e) => errors.push(e.message));
    page.on("console", (m) => m.type() === "error" && errors.push(m.text()));
    await page.goto(url);
    await page.waitForSelector("canvas", { timeout: 30_000 });
    await settle(page, 2500);
    for (const step of scenario.steps) {
      await step.run(page);
      await page.screenshot({ path: join(out, `${tag}-${step.name}.png`) });
    }
    console.log(`${tag}: ${scenario.steps.length} shots${errors.length ? `, errors: ${errors.join(" | ")}` : ""}`);
    await context.close();
  }
} finally {
  await browser.close();
  server.stop();
}
