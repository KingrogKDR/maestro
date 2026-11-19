---
name: awesome-design
description: Library of 84 DESIGN.md analyses of real products (VoltAgent/awesome-design-md): palettes, type scales, spacing, radii, elevation and component rules. Use when deciding a visual direction, comparing how strong products solve a design problem (dark media UIs, players, sidebars, type scales), or calibrating tokens for Maestro.
---

# Awesome DESIGN.md reference library

`design-md/<product>/DESIGN.md` holds one design-system analysis per product, each with the same
sections: visual theme, color roles, typography, components, layout, depth, do's and don'ts,
responsive behavior, and an agent prompt guide.

## How to use it for Maestro

1. **Reference, never replicate.** Maestro is its own app. Study how a product solves a problem,
   then derive Maestro's answer from Maestro's own tokens. Never copy a brand's signature
   color, font, logo, wordmark or recognizable layout (no Spotify green, no Apple Music red).
2. **Pick by problem, not by brand.** Read only the 2 to 4 files relevant to the question.
   Useful starting points for a music player:
   - Dark, content-first media UI: `spotify`, `apple`, `playstation`
   - Calm, precise app chrome and sidebars: `linear.app`, `raycast`, `superhuman`, `notion`
   - Warm, editorial type and imagery: `airbnb`, `wired`, `theverge`
   - Hardware-like tactility: `dji`, `oura`, `tesla`
3. **Extract principles with numbers.** Note the ratios (surface steps, text contrast tiers,
   radius scale, spacing unit, type scale ratio) rather than the hex values.
4. **Check against Maestro's rules.** Every borrowed idea must still pass the `apple-design`
   accessibility floor (4.5:1 text, 44dp targets, reduce motion) and Impeccable's craft floor.
5. **Record decisions** in the project's `DESIGN.md` so later work stays consistent.

To list products: `ls design-md`. To search a topic across all of them:
`grep -ril "now playing\|player\|scrubber" design-md`.

Source: https://github.com/VoltAgent/awesome-design-md (MIT, see LICENSE).
