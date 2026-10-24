# LoafCraft AI Overhaul: Design Doc

Part 1 of the AI overhaul. Characters are Carpet fake players: every move below is done with real player inputs
(keys, mouse look, hotbar slots, right/left click), never by setting health, position or velocity directly.

Facts marked **[verified]** were checked against the decompiled Minecraft 1.21.11 jar this mod builds against
(`javap` on `MaceItem`, `Player`, `ThrownEnderpearl`, `Entity`, `AbstractWindCharge`). Facts marked **[check]**
still need an in-game check; part 5's `/loafcraft technique` command is the tool for that.

---

## 1. How skilled players fight in 1.21.x

### The attack cooldown is everything
- Each weapon has an attack speed. Sword 1.6/s means **12.5 ticks** to full charge, an axe 1.0/s means 20 ticks, a mace
  0.6/s means about 33 ticks (`MaceItem`: +5 damage, -3.4 speed **[verified]**).
- Damage is scaled by the charge: `0.2 + charge² × 0.8`. A hit at half charge does about 40%. Good players never
  spam-click; they hit at full charge, about 0.6 s apart with a sword.
- **Switching hotbar slots resets the charge.** Good players switch early (axe before the shield hit, mace before
  the fall), not in the same tick they attack.

### Critical hits
- A crit needs: charge above 0.9, falling (`fallDistance > 0`), not on the ground, not sprinting, not in water or on
  a ladder **[verified: Player.attack checks 0.9 and isSprinting]**. It does 1.5× damage.
- The rhythm is: jump, wait for the fall, hit on the way down. The jump takes about 10 ticks, so it lines up with a
  sword's 12.5-tick cooldown.

### Knockback control (W-tap, S-tap, sprint resets)
- A sprinting hit deals extra knockback, then **stops your sprint**. To get the extra knockback on the next hit you
  have to start sprinting again.
- **W-tap:** let go of W for 1–3 ticks right after a hit and press it again. That restarts the sprint, so the next hit
  knocks back far too, and the opponent can't hit back as easily.
- **S-tap:** tap backwards for 2–4 ticks after a hit. It opens a gap, so the opponent walks into your next hit
  while you stay out of their reach.
- **Strafing:** circling the target (A/D) while facing them makes you harder to hit, and makes their aim work harder.
  Good players switch direction every 0.5–2 s at unpredictable moments.
- **Jump reset:** jumping on the exact tick you're hit, while on the ground, adds the jump's forward boost against the
  knockback, so you get pushed back less. Timing window: about 1 tick, so even good players land it maybe 50–70%
  of the time.

### Shields and axes
- A raised shield takes **5 ticks (0.25 s)** before it blocks **[check: shield `blocks_attacks` block delay]**.
  It blocks melee and projectiles from the front.
- An axe hit on a raised shield **disables it for 5 seconds** **[check: axe `disable_blocking_for_seconds`]**.
  Good players hold the axe, hit the shield, then switch back to the sword for the opening.
- Good shield use is short: raise it when the opponent's charge is nearly full or they are drawing a bow, drop it
  to hit. A shield held up all the time gets axed.
- A raised shield blocks a mace smash from the front too **[check]**.

### Healing and potions
- Golden apple: 1.6 s to eat, Regeneration II 5 s and Absorption I 2 min. Enchanted: Regen II 20 s, Absorption IV,
  Resistance and Fire Resistance 5 min.
- Drinking a potion also takes 1.6 s, while a splash potion acts instantly. Strength I adds +3 damage, Strength II +6,
  and Speed I is +20% speed.
- Good players eat behind cover or after knocking the opponent away, never in their face. A totem in the offhand
  saves you from one lethal hit.

### Positioning
- Fight on flat ground, keep the high ground, and don't get hit off edges.
- Pillar up or place a block to break line of sight when healing.

---

## 2. Mace movement, checked against 1.21.11

### The smash attack **[verified: MaceItem]**
- A smash needs **fall distance above 1.5 blocks** and **not gliding** (`canSmashAttack`).
- Bonus damage by fall distance `d`:
  - `d ≤ 3`: +4 per block
  - `3 < d ≤ 8`: 12 + 2 per block after 3
  - `d > 8`: 22 + 1 per block after 8
  - No upper limit.
- Density adds 0.5 per block per level. Breach makes armor 15% less effective per level.
- **The smash bonus is added after the cooldown scaling** (`Player.attack`: base × charge factor, then
  `+ getAttackDamageBonus`). So a smash with a freshly switched-to mace still gets its full fall bonus. Only the
  base 6 damage is reduced. People often assume you have to wait for the mace to charge; you don't.
- A smash hit **resets the attacker's fall distance** and sets their vertical speed to 0.01, so they take no fall
  damage from that fall.
- Everyone within 3.5 blocks of the target gets knocked away.

### Wind charge launches and Wind Burst chains
- A wind charge thrown at your own feet bursts with power 1.2 and launches you up about 6–8 blocks when combined with
  a jump **[check: exact height]**.
- The launch also tells the game to ignore fall damage from this "impulse", but **fall distance still builds on the
  way down**. That's why wind-charge jump → smash works.
- Wind Burst launches you up after a successful smash, about 7 blocks per level **[check]**, so you can hit again on
  the way down: a chain. Each new fall starts from 0 (the smash reset it), so later hits in a chain only get the
  bonus from the new fall.
- Wind charges can't hit other wind charges (`AbstractWindCharge.canHitEntity`) **[verified]**.

### Pearl catching
- **Ender pearl teleport facts [verified: ThrownEnderpearl.onHit]:**
  - It moves you to where the pearl hit, **keeps your current velocity** (the teleport uses relative velocity with a
    zero change), **resets your fall distance to 0**, and clears the wind-charge "ignore fall damage" flag.
  - It also deals **5 damage** (2.5 hearts) to you.
  - A pearl can hit its owner once it has left the owner's hitbox (`Projectile.leftOwner`).
- **What people assume:** "catching your pearl teleports you higher." On its own it doesn't. A pearl that hits you
  puts you where you already are, and your fall distance goes back to 0. The height gain comes from the pearl
  landing somewhere **higher than your current arc** while your upward speed is kept.
- **Correction (part 5):** this section first described a "timed" catch: throw the pearl up, wind-jump into it as it
  comes back down. Working through the physics, that gains almost nothing. The pearl meets you where you already are,
  and the teleport only keeps the speed you already had.
- **What the catch really is (instant pearl catch):** a wind charge and a pearl thrown down the same line together.
  - **[verified]** A pearl can hit a wind charge, because wind charges are hittable projectiles.
  - **[verified]** Being hit doesn't burst the wind charge: `hurtServer` returns false, and it only gets deflected.
  - So the height comes from the wind charge then bursting on **you** after you've teleported to it. That's a
    launch from mid-air with no ground under you, a second jump.
  - Whether it bursts on you depends on whether it counts as having "left" you (`Projectile.leftOwner`). That's why
    the community says vanilla makes it inconsistent.
- **What the bots do:** a wind jump, then at the top a wind charge and, one tick later, a pearl down the same line.
  If the burst kicks them up, they ride the second launch. If not, they simply fall and smash from the first jump.
  Either way the move ends in a smash. **[check]** in game with `/loafcraft technique <name> pearl_catch`.
- **Cost:** 5 pearl damage every time. A bot at 10 health (5 hearts) or less won't use pearls.

### Diagonal pearl catching
- From 8–18 blocks away, the pearl is thrown in an arc to land 2–3 blocks to one side of the target (leading a
  moving target). When the teleport brings them over, they wind-jump from right there and come down on the target
  from the side.
- A failed throw lands about 6 blocks wide and they recover from there.
- This covers the distance reliably, using only verified mechanics (pearl teleport, wind jump, smash).

### Elytra rocket mace
- In 1.21.11 the elytra and chestplate are both `equippable` and **swappable with a right-click from the hotbar**
  **[verified in current code: Master already swaps this way]**.
- **[verified: Entity.checkFallDistanceAccumulation]:** while your downward speed is slower than 0.5 blocks/tick,
  your fall distance is capped at 1. A normal glide **doesn't build smash power**, and **you can't smash while
  gliding** (`canSmashAttack` needs not gliding).
- **The working version:**
  1. Swap to the elytra, jump, glide, and climb with rockets to about 15–20 blocks above the target.
  2. Glide over them, then swap the chestplate back on. That ends the glide.
  3. Free-fall. Fall distance now builds normally from about 1.
  4. Switch to the mace (the charge doesn't matter, see above) and smash.
- Alternative: dive steeply (straight down is faster than 0.5 blocks/tick, so fall distance builds), then swap the
  chestplate back on just before reaching the target. It's faster but harder to time.
- The chestplate swap is also what keeps your armor on for the landing.

### Picking the mace
- **Breach** against heavy armor (armor value 15+).
- **Density** for big falls (6+ blocks).
- **Wind Burst** to chain hits.

---

## 3. Making bots look human, not robotic

Robots give themselves away by being instant, perfect and repetitive. The fix is lag, error and variety:

1. **Reaction delay:** every new threat is noticed 150–300 ms (3–6 ticks) later, a different amount each time.
   Better fighters react faster. The bot can't attack, block or switch until the delay is up.
2. **Mouse-like aim:** the view turns toward the target with a speed limit and a spring. It overshoots a little,
   corrects back, and lags behind a strafing target. Each fighter has an aim error (degrees of wobble) and an aim
   speed. Snapping only happens for genuine flicks, and even those land with error.
3. **Hotbar switching takes time:** 2–5 ticks per slot change (number keys are faster than scrolling), and
   switching resets the attack charge, as it does for players.
4. **Imperfection on purpose:** a skill-based chance to swing early, miss a crit jump, throw a pearl short, panic
   when very low, or fail a jump reset.
5. **No perfect loops:** strafe direction, W-tap length and jump timing are randomized inside a range, and two hits
   are never timed the same.
6. **Attention:** they look at what's around them between fights, glance at players and what they're holding, and
   don't stare at one point.
7. **Mood and memory** change decisions (section 5), so the same situation doesn't always get the same answer.

---

## 4. Play styles (each character fights differently)

Every character has a **skill profile** (numbers) and a **play style** (how they like to fight). Both are in the
config with Easy, Normal and Hard presets.

| Character | Play style | How it shows |
|---|---|---|
| **ItzBreadLoaf** | **Brawler** | Sprints straight in, spams W-taps and jump crits, rarely blocks, chases too far, sometimes walks off a ledge mid-fight. His types lean on it: **Knight** = shield and sword "turtle", **Pirate** = axe-heavy shield breaker, **Archer** = kites with a bow, **Sweaty** = combo player (W-tap, strafe, jump resets), **Bandit** = hit-and-run, **Captain** = steady front-liner. |
| **FelixSyn** | **Aerialist** | Fights from the air: wind-charge jumps, pearl catches, diagonal pearls, Wind Burst chains. Trash-talks first. Weaker on flat ground with no wind charges left. |
| **Harley** | **Duelist** | Clean and consistent: perfect cooldown timing, strafes, shield at the right moments, axe for shields, jump resets. Keeps her current gear and difficulty presets. She's never overpowered, just tidy. |
| **ImLilyLol** | **Kiter / support** | Keeps her distance, bow first (Archer Lily), potions (Witchy Lily), runs when scared. When angry she focuses on one target and stops running. |
| **Madison** | **Defender** | Shield-heavy. Plants herself between danger and her animals or friends, counter-hits when the attacker's swing misses, and places blocks to stop a chase. Angry Madison pushes forward with knockback. |
| **Master BreadLoaf** | **Showman boss** | Sword pressure on the ground between big, loudly warned mace moves. Knows every technique but is limited by the nerf. |

Skill profile values (per character, per preset):

- Reaction delay range (ms)
- Aim error (°) and aim speed (°/tick)
- Hotbar switch time (ticks)
- Crit chance, W-tap chance, S-tap chance, jump-reset chance
- Shield skill (how well-timed the block is)
- Mistake chance
- Technique fail chance
- Panic health (below this they can panic)
- Aggression (how far they chase, how soon they engage)

---

## 5. AI framework plan (parts 3–7)

### Part 3: framework
- `SkillProfile` and `PlayStyle`, read from the config presets.
- `Mood`: happy, bored, tired, scared, angry or curious. It has a strength value that slowly fades, and events push
  it around: getting hurt, being fed, nightfall, a friend being attacked, nothing happening.
  - Mood changes speed, chat, how soon they flee, and what they do when idle.
- `Memory` per player, saved with the character: fed count, hurt count, last seen, and a trust score.
  - Example: a player who hurt Lily before makes her run sooner, while a player who feeds her often gets crouch
    greetings.
- `Routine` by time of day: wake, eat, work (bake, train, read, garden, farm, practice mace jumps, spar), visit
  friends, and bed at night.
  - Built on the existing home, pantry and bed systems. Existing taming, trust, friendship and missions stay as they
    are.

### Part 4: human-like combat
- `HumanAim` (spring aim with error and overshoot), a reaction delay queue, hotbar switch timing, W-tap and S-tap,
  strafe direction changes, and jump resets when hurt.
- Smarter shields (5-tick raise, only when threatened), axe swap for shields, and backing off to eat or drink behind
  cover.
- Blocks for cover when healing.

### Part 5: mace techniques
One technique machine shared by FelixSyn, Harley and Master BreadLoaf:

- `WIND_JUMP`
- `WIND_CHAIN`
- `PEARL_CATCH`
- `DIAGONAL_PEARL`
- `ELYTRA_DIVE`

Each one checks height room, distance, health (pearls cost 5) and the target's armor, and each has a skill-based
fail chance.

### Part 6: personality
- Idle actions: looking around, crouch greetings, happy jumps, looking at a held item, sorting items, sitting at
  tables, fishing, small builds, a little mining, and fiddling with the hotbar when bored.
- Social: greeting each other, hanging out, defending friends, and family-friendly rivalry trash-talk.
- More chat lines per mood. The existing `actionBarMessages` option turns them off.

### Part 7: optimization and debugging
- **Staggered thinking:** each character thinks on its own tick offset (hash of its UUID), so 30 characters don't all
  scan on the same tick.
- **Detail levels:**
  - Within 32 blocks of a real player: full AI.
  - 32–64 blocks: simple routines, with slower thinking (every 4 ticks).
  - Beyond that: paused, then despawned as now.
- **Pathfinding:** running it off the main thread isn't safe in vanilla, because reading chunks from another thread
  can crash or corrupt. Instead it gets the same benefit safely:
  - Searches are **time-sliced** across ticks, with a node budget per tick.
  - Paths are cached and reused while the target has moved less than 2 blocks.
  - This is a deliberate change from the request, flagged here.
- **Throttled checks:** target scans every 10–20 ticks, line of sight every 4, structure lookups every 40.
- `/loafcraft debug ai` shows each character's goal, mood, detail level and AI time per tick (measured with
  `System.nanoTime`).
- Test commands:
  - `/loafcraft duel <a> <b>`
  - `/loafcraft technique <character> pearl_catch|diagonal_pearl|elytra_mace|wind_chain`
  - `/loafcraft mood <character> <mood>`

---

## 6. Master BreadLoaf nerf (part 2)

| | Before | After (Normal) |
|---|---|---|
| Health | 200 | **100** |
| Armor | Netherite, Protection IV | **Diamond, Protection II** |
| Sword | Netherite, Sharpness V | **Plain diamond sword, and his hits do 55% damage** (tuned with the fight simulation below) |
| Maces | Wind Burst II, Density V, Breach IV | **One mace, Wind Burst I** |
| Biggest hit | Up to 6 blocks of fall counted | **Max 16 damage (8 hearts) per hit** from any of his attacks, and only 3 blocks of fall counted (a smash does about 12.7 before armor, about 4.5 through plain diamond) |
| Smash cooldown | 4.5–13 s | **12 s** after each smash ends (never under 8 s on any preset), Wind Burst chains capped at **2 hits** |
| Warnings | Sparks at the top of a fall | **1-second wind-up before every smash and every dive**: he stops, loud charge-up sounds, sparks and portal particles. A chain's second hit gets the warning again at the top of the bounce |
| Potions | Strength II, Swiftness II, Fire Resistance, 2 Healing II | **One Speed I**, no Strength, no healing potions |
| Golden apples | 3 | **1 per fight** (given back after 30 quiet seconds) |
| Advanced moves | Every 4.5–13 s | Elytra dives (and part 5's pearl catches) at most **once every 20 s**, and **25%** fail on purpose: he comes down 4–5 blocks away from you |
| Reactions and aim | Instant | **300–400 ms** before he reacts to a new target, **12%** of his sword swings miss, he jump-crits less often (25%), and his pearls are thrown a little off |
| Phases | 3 phases, phase 3 summons 2 ItzBreadLoafs | **2 phases.** At half health and below he's 8% faster, nothing more. No summons. |
| Ender Dragon | Appears at half dragon health | Appears **after the dragon dies** |
| Drops | Trophy guaranteed. Wind Burst mace 0.8% (from the "maces under 1%" rule) | **Unchanged** (see the note in the summary) |

Presets:

| | Easy | Normal (default) | Hard |
|---|---|---|---|
| Health | 70 | 100 | 120 |
| Armor | Diamond Prot I | Diamond Prot II | Diamond Prot III |
| His damage | 40% | 55% | 50%, plus a Strength I potion |
| Smash cooldown | 15 s | 12 s | 12 s |
| Fall counted for a smash | 3 blocks | 3 blocks | 4 blocks |
| Technique cooldown | 30 s | 20 s | 20 s |
| Technique fail chance | 35% | 25% | 15% |
| Reaction | 350–450 ms | 300–400 ms | 250–350 ms |
| Swing miss chance | 18% | 12% | 8% |
| Crit chance | 15% | 25% | 30% |
| Phase 2 speed bonus | +5% | +8% | +12% |

Every preset respects the 16-damage cap and all warnings.

### Fight simulation (instead of an in-game test)
`tools/boss_fight_sim.py` runs 3,000 fights per case using 1.21.11's damage rules: armor and toughness, Protection,
crits, the sword cooldown, the mace fall bonus and the 16 cap. It assumes realistic play: 75% of the player's swings
land, 25% crits, a shield blocks 30% of his sword hits, half of his warned smashes get dodged, and both fighters are
in reach 65% of the time. It also assumes he stops regenerating after about 40 s (he doesn't eat mid-fight) while the
player eats between hits.

| Player | Easy | Normal | Hard |
|---|---|---|---|
| Average, plain full diamond, sword, shield, 8 golden apples | 100% | **70%** (about 3.5 min) | 0% |
| Average, Protection II armor and Sharpness II sword, 8 golden apples | 100% | 100% | 69% |
| Weak (misses more, dodges less), plain diamond, 4 golden apples | 81% | 0% | 0% |

These are model numbers, not an in-game test. Movement, knockback and terrain are simplified.

Old worlds: a Master BreadLoaf saved before the nerf gets the new kit the next time he loads, and his health is
clamped to the new maximum.

---

## 7. Part 3: what was built

New package `brain/ai`, plus `brain/Activities.java`. Everything hooks into the existing brains; taming, trust (Lily's
5 gifts), friends, missions, homes, beds and structures are unchanged.

### Skill profiles and play styles
- `SkillProfile`: reaction ms, aim error and speed, hotbar switch ticks, crit, W-tap, S-tap, strafe and jump-reset
  chances, shield skill, mistake chance, technique fail chance, panic health and aggression.
- Every character has Easy, Normal and Hard profiles in `config/itzbreadloaf.json` under `skillProfiles`. They are
  written out with the defaults on first launch, so you can edit the numbers there.
- Which preset each character uses:
  - `aiDifficulty`, or a per-character entry in `aiDifficultyOverrides`
  - Harley follows `harleyDifficulty`, Master BreadLoaf follows `bossDifficulty`
- Used now: crit chance and the deliberate-miss chance (every character's swings miss a little now, by skill).
  Harley's crit and all of Master BreadLoaf's numbers stay on their own presets. Part 4 uses the rest.
- `PlayStyle`:
  - brawler, turtle, shield breaker, kiter, combo, hit and run, front-liner, aerialist, duelist, support, defender,
    showman
  - Picked from who they are, or set in `playStyleOverrides`. Part 4's combat reads it.

### Moods
`Mood` and `MoodState`:
- **Events push moods:** getting hurt (fighters get angry, Lily and low-health characters get scared), a friend nearby
  getting hurt, being fed or given something (happy), being tamed (happy), seeing a new face (curious), seeing someone
  who hurt them (scared, or angry for fighters and Madison), seeing a liked player (happy), and fighting (angry, or
  scared below panic health).
- **Fading:** moods fade over about 2 minutes back to the everyday mood: tired at night, bored after 3 quiet minutes,
  happy otherwise.
- **Effects now:**
  - Speed: happy +3%, bored -3%, tired -10%, scared +10%, angry +5%. Master BreadLoaf is excluded.
  - Backing off from a fight: scared characters back off sooner, angry ones later.
  - Routines change with mood (below).
  - A short line when the mood changes, at most every 30 seconds.

### Memory
`PlayerMemory` keeps, per real player:
- times fed, times hurt, damage taken, times helped
- last hurt, last seen
- a trust score from -100 to 100

It's saved with the character and keeps the 32 most recently seen players.
- A player who hurt Lily or Madison in the last 20 minutes (or whose trust is very low) is avoided from twice as
  far away.
- In a fight with that player, they back off 15% sooner.

### Routines
`Routine`:

| Time | Activity |
|---|---|
| Sunrise | Wake up |
| Morning | Work |
| Noon | Friends |
| Afternoon | Work |
| Evening | Free time, then head home |
| Night | Sleep |

- Tired characters go home and to bed an hour early and get up an hour late. Bored ones skip work for friends or
  free time. Happy and curious ones visit friends more.
- Each hour's choice is fixed per character and day, so they don't flip back and forth.

| Character | Work |
|---|---|
| ItzBreadLoaf | Trains on a training dummy (armor stand) at full charge, or shadow-boxes. Village and plain ones bake at a furnace, smoker or crafting table. Archers, miners and fishermen keep their own jobs. |
| ImLilyLol | Bookworm reads, Gardener plants and tends flowers, Witchy works at her brewing stand or cauldron, Archer Lily patrols. |
| Madison | Her job (farm, honey, fish). The baker works at her oven. Otherwise she tends the garden and organizes her pantry. Merchants stay at the stall. |
| FelixSyn | Mace practice: wind-charge jumps out in the open, shadow-boxing in between. |
| Harley | Spars with FelixSyn (swings land 35° to the side, so no hit ever connects), or trains alone. |

- **Friends time:** Lily visits Madison and Madison visits Lily (their existing visits). Everyone else walks over to a
  friend character, greets them with two quick crouches, and hangs out for about 15 seconds. FelixSyn goes to his
  usual hangout, and Harley comes along.
- **Safety:**
  - Practice swings never go off with someone alive right in front.
  - FelixSyn never practices wind-charge jumps near people, doors, buttons, levers, gates or bells (Harley is
    fine).
  - Work comes in 30–60 second stints with short breaks.

### Test command
- `/loafcraft mood <name>` shows mood, routine activity, skill preset and play style.
- `/loafcraft mood <name> <mood>` holds a mood for 2 minutes.

---

## 8. Part 4: human-like combat, what was built

Mostly in `Combat.java` and `Controls.java`, driven by each character's skill profile and play style. Every
character that fights uses it, because they all go through `Combat`.

### Aim
- **Mouse-like aim:** `Controls.humanAim` moves the view on a slightly loose spring. It speeds toward the target (capped
  at the skill's aim speed in degrees per tick), overshoots about 15%, and settles in about 6 ticks. It lags behind a
  strafing target and keeps a small drifting wobble the size of the aim error.
  - Harley: about 2° of error at 40°/tick.
  - Master BreadLoaf: about 5° at 28°/tick.
- **Clicking:** a swing only goes in when the crosshair is actually on the target's hitbox, within reach. Off target
  they wait for the aim to land, except that once per swing there's a skill-based chance (the mistake chance) they
  click anyway and miss.
  - This replaces part 3's forced misses for everyone except Master BreadLoaf, whose tuned miss chance stays.
- **Bows, crossbows and tridents:** aimed the same way, and only let go once the aim has settled. Pearls and other
  scripted throws still aim exactly.

### Reaction and hotbar
- **Reaction time:** a new target is noticed `reactionMinMs`–`reactionMaxMs` later (a new roll each time). Until then
  they just turn toward it.
  - Master BreadLoaf uses his own reaction delay from part 2, so it isn't doubled.
- **Hotbar:** `Controls.switchSlot` takes the skill's switch time (Harley 2 ticks, Lily 5). Asking for the same slot
  again doesn't restart the wait. No swings go in while a switch is on its way.
  - Scripted moves (drinking, mace jumps, Master's dives) still switch instantly, so their timings don't break.

### Cooldown and crits
- **Cooldown:** hits go in at 93–100% charge, rolled per swing.
- **Crits:** whether to go for a jump crit is decided once per swing at 80% charge, with the skill's crit chance. A
  mistimed crit (landing before the hit) just ends.
  - The old code rolled every tick, so crits were more frequent than the setting said.

### Knockback control
- **W-tap:** after a sprinting hit, let go of forward for 1–3 ticks.
- **S-tap:** step back for 2–4 ticks.
- **Hit-and-run:** the Bandit style backs off for 10–17 ticks after most hits.
- **Strafing:** circling while facing the target, with direction changes at random moments (more often for better
  fighters). Whether they strafe at all is rolled by `strafeChance`.
- **Distance:** they keep to their play style's distance (2.2–3.4 blocks).
- **Jump reset:** when hit while fighting and on the ground, a jump goes in on the next tick, rolled by
  `jumpResetChance`. **[check]** A server-side fake player jumps on its own next tick, not the exact tick of the hit
  the way a client can, so the knockback reduction may be smaller than a real player's.

### Shields
- **When to raise:** when a hit is coming. A player or character facing them whose attack charge will be full within
  5 ticks (the shield's raise time), a bow or crossbow being drawn, or a mob close by while their own swing isn't
  ready.
- **Timing by skill:** whether to block each incoming hit is decided once, based on shield skill and the play style's
  shield bias.
- **Limits:** lowered to attack, and never held more than 1.5 seconds against melee.
- **Axes:** skilled fighters (shield skill 0.5 and up) don't block axe users.
- **Shield breaking:** against a blocking target, if they carry an axe, they switch to it (with the hotbar delay), hit
  the shield to disable it, then switch back to their main weapon. **[check]** This relies on 1.21.11 axes disabling
  shields on any blocked hit (the `disable_blocking_for_seconds` component).

### Backing off and panic
- **Backing off:** low on health they back away (shield up, or sprinting off). If they carry building blocks and
  griefing is allowed, they place up to 2 blocks between themselves and the attacker. With a little room (4.5+ blocks)
  they eat a golden apple, drink a healing or regeneration potion, or eat food if hungry.
- **Before a fight:** at 6+ blocks they drink a Strength, Swiftness or Fire Resistance potion if they have one (not
  Harley or Master BreadLoaf, who have their own).
- **Panic:** below their panic health, less skilled fighters now and then do something erratic: a random jump, a
  sudden change of direction, a half-charged click, or backing off early.

### Focus
- Lily and Madison fight unfocused until they're angry: half the crits, no taps, half the strafing, more blocking and
  sloppier aim. Fighters are always focused.

### Effect on the Master BreadLoaf balance
His sword hits now also need the crosshair on you, so he lands a little less than in the part 2 simulation. The
Normal win rate there (about 70% for an average player in plain diamond) is now a floor, not an estimate.

---

## 9. Part 5: mace techniques, what was built

`brain/MaceTechniques.java` replaces the old single mace jump (`MaceJump.java`, removed). FelixSyn, Harley and
Master BreadLoaf all use it.

| Move | When | What happens |
|---|---|---|
| Wind jump | 1.5–5.5 blocks | Look down, jump, wind charge at the feet, mace out, drift on, smash on the way down. |
| Wind chain | 1.5–5.5 blocks, more often with a Wind Burst mace or against 15+ armor | After each smash, a wind charge straight down at the target bursts on them and launches the bot again. A Wind Burst mace bounces by itself. |
| Pearl catch | 3–8 blocks, more often against armor | Wind jump, then at the top a wind charge and a pearl down the same line (see section 2). |
| Diagonal pearl | 8–18 blocks | Arcing pearl to land beside the target, then a wind jump from there. |
| Elytra mace | 12–40 blocks, open sky over the target | Chestplate swapped for the elytra, rockets up to ~16 blocks above them, glide over, chestplate back on, free-fall smash. |

- **Picking a move:** weighted by the situation. FelixSyn's aerialist style favors the pearl and elytra moves 1.6×.
  Each move checks its items, room above, being on the ground, and health for pearls.
- **Picking a mace:** Breach against 15+ armor, Density for drops of 6+ blocks, Wind Burst for chains.
- **Smash timing:** a smash only goes in with the crosshair on the target (aimed with the bot's skill). A fall that
  misses is a real miss.
- **Failing on purpose:** by skill (`techniqueFailChance`; FelixSyn 15% on Normal). The pearl catch is thrown off
  line, the diagonal pearl lands wide, and a dive or wind jump comes down 4 blocks away.
- **Limits per user:**

| | Fall counted per smash | Chain | Moves | Cooldown |
|---|---|---|---|---|
| FelixSyn | 6 blocks (as before) | 3 hits | all five | 5–8 s |
| Harley | 6 blocks | 2 hits | wind jump and wind chain only, so her gear and balance stay as they were | 3 s against armor, 6 s otherwise |
| Master BreadLoaf | his preset's cap | 2 hits | adds pearl catches; his own elytra dive and wind jump code stays | at most once every 20 s, shared with his dives |

  - Master's pearl catches start with the same 1-second warning, warn again before a chain's second hit, fail at his
    preset's rate, and still obey the 16-damage cap.
- **New kit items for FelixSyn:** 8 ender pearls, 16 firework rockets and an elytra. While wild he restocks pearls and
  rockets, but not once tamed, so nobody can farm them. Wild FelixSyns and Harley get no new items. An older FelixSyn
  without an elytra just won't dive.
- **Fall damage:** their own moves don't give them fall damage, but pearls still cost them 5 like anyone.
- **Elytra cleanup:** a move that ends with the elytra still worn puts the chestplate back on.
- **Test command:** `/loafcraft technique <name> pearl_catch|diagonal_pearl|elytra_mace|wind_chain|wind_jump [target]`
  forces a move at the target (default: the nearest living thing within 24 blocks). It hands over any missing items
  and runs on its own until the move ends.

---

## 10. Part 6: personality, idle actions and social behavior, what was built

New: `brain/IdleActions.java` and `brain/ai/Situation.java`. Lines, defending and celebrating live in `LoafBrain`,
and each character has its own lines in its brain. Master BreadLoaf only gets fight lines: no idle actions, he's a
boss.

### Idle actions
Picked about every 7 seconds when they have nothing else to do. The choice depends on mood, time, who's around and
who they are.
- **Greeting a trusted player:** their owner, someone they like from memory, or, for Lily and Madison, anyone they
  trust. They turn, crouch-spam, wave and say hi. Once per player per 3 minutes.
- **Greeting a friend character:** walk over a bit, crouch twice, wave, and call them by name. Once per friend per
  5 minutes.
- **Happy jumps** when happy (ItzBreadLoaf and FelixSyn the most).
- **Looking at what you're holding:** within 5 blocks, sometimes naming it ("ooh, is that a golden apple?").
- **Inspecting dropped items:** walk over and look (players pick up what they walk over).
- **Sorting the hotbar:** looking down and going through it slot by slot (Madison the most).
- **Fiddling** with random slots when bored. The slot they had goes back at the end.
- **Sitting on a chair:** a stairs block near home, or where they stand, crouched and looking around (tired = more).
- **Morning stretch:** look up, crouch twice, a comment.
- **Stargazing** at night under open sky (Lily the most).
- **Watching a player work:** looking at the block a nearby building or mining player is looking at.
- **Tinkering:** place two blocks that break fast by hand (dirt, planks), look at them, then take them back. Any left
  over get cleaned up next time. Only when griefing is allowed.
- **Mining a bit:** one block of natural stone close by, never near home, with a pickaxe (miners the most). Only when
  griefing is allowed.
- **Fishing,** if they carry a rod.
- **Getting out of the rain:** run home with a comment.
- **Catching their breath** after a fight that left them under half health.
- **Celebrating a win:** two jumps, two arm swings and a line.
- **Glancing at commotion:** anything hurt within 12 blocks makes idle characters turn and look.
- **Stepping aside** when someone stands right inside them.

With their owner, tamed characters only do the ones that keep them near: greetings, jumps, looking at items,
sorting, fiddling and stargazing.

### Social
- **Defending friends:** when a friend nearby is hurt, fighters and Madison step in against the attacker (Lily too,
  once angry). Lily and Madison do this without the trust reset that hitting them causes. Others get scared or angry
  and say so.
  - Never against their own friends, their owner, or creative players.
  - Harley still never steps into FelixSyn's duels with ItzBreadLoaf.
- **Rivalry trash-talk:** ItzBreadLoaf and FelixSyn exchange short, family-friendly lines when a fight starts. The
  one answering waits 1.5 seconds so they don't talk over each other.
- **Hanging out:** friends time in the routine (part 3), plus greetings.

### Lines
- Each character has lines for 14 moments: greeting a player, greeting a friend, fight start, rival, win, low health,
  defending a friend, scared for a friend, resting, morning, night, rain, a held item, watching work. Each also has
  its own lines for the 6 moods.
- At most one line every 8 seconds, except rival, win and defending lines.
- New config option `personalityLines` turns these off. `actionBarMessages` still turns off every line.

### Human touches beyond the list in the request
1. Glancing at a nearby fight or hit.
2. Waving after a crouch greeting.
3. A morning stretch.
4. Stargazing.
5. Running home out of the rain.
6. Catching their breath after a close fight.
7. Celebrating a win.
8. Stepping aside when someone stands in them.
9. Watching a player build or mine.
10. Naming the item you're holding.
11. Tinkering with blocks and putting them back.
12. Delayed replies, so two characters don't talk over each other.
13. Not re-greeting the same person for a few minutes.
14. Friends defending friends as a group.

From earlier parts: mood-based speed, panic when nearly dead, mouse-like aim wobble, deliberate misclicks, random
strafe timing, and backing off to eat behind cover.

---

## 11. Part 7: optimization and debug tools, what was built

### Already there before this part
- A shared path-search budget: `maxPathSearchesPerTick`, 6 searches per tick for all characters together.
- A cap on the nodes per search (2,500).
- Characters only exist near real players (`activationRange`, 64 blocks) and are capped at `maxActiveCharacters`.

### New
- **Staggered thinking:** each character's tick counter starts at its own offset (from its UUID). Every "every N
  ticks" check lands on a different tick for each character instead of all at once: target scans, mood updates,
  greetings, routine checks.
- **Detail levels:** checked once a second.

  | Level | When | What runs |
  |---|---|---|
  | Full | Within `aiFullDetailRange` (32 blocks) of a real player, or fighting, tamed, dueling, testing a move, or Master BreadLoaf | Everything, every tick |
  | Simple | Out to `aiSimpleDetailRange` (56 blocks) | Thinks every 4th tick; keyboard input carries on in between, so they keep walking |
  | Paused | Beyond that | Stands still and doesn't think, until a player comes closer |

  A character that gets hurt always thinks, whatever its level.
- **Path cache:** paths are shared by all characters for 5 seconds, keyed by start block, goal block and reach (256
  kept). Characters heading for the same bell, bed or friend reuse one search. A path is now kept until the goal has
  moved 2 blocks, up from 1.5.
- **Pathfinding stays on the main thread on purpose:** reading chunks from another thread isn't safe in vanilla.
  The budget, the cache and the reuse give the same benefit without the risk. This is a deliberate change from the
  request, flagged in section 5.
- **Throttled checks:**
  - Line of sight in fights: at most every 4 ticks per target (it used to run several times a tick).
  - Mood: once a second.
  - Players nearby: every 2 seconds.
  - Idle scans: once a second.
  - Detail: once a second.
  - Greetings and work-station searches: every 5 seconds.
- **Timing:** each character's AI time per server tick is measured with `System.nanoTime` and smoothed.

### Commands
- `/loafcraft debug ai`: every active character's name, detail level, goal ("fighting X", "mace pearl_catch",
  "idle sit", "routine work"...), mood and AI time in µs per tick. Then the totals: characters per level, total
  milliseconds of AI per tick, and path searches and cache hits since the last check.
- `/loafcraft duel <a> <b>`: two characters fight each other for up to 2 minutes, or until one goes down. It overrides
  friendships and the usual rules (village and gear rules, Harley's duel rule) for that fight only.
- `/loafcraft technique` (part 5) and `/loafcraft mood` (part 3) complete the test tools.

### 30+ characters
`maxActiveCharacters` defaults to 20. Set it to 30 or more in the config to test bigger groups, and use
`/loafcraft debug ai` to read the real cost. **[check]** I couldn't measure tick times without running the game.

---

## 12. Part 8: balancing and final testing

In-game testing was skipped at the user's request (compile and offline checks only). What was checked instead:
- `./gradlew build`: passes.
- `./gradlew checkStructures`: all 174 structures pass (2,088 test builds) and all 190 loot tables use real items.
- `tools/boss_fight_sim.py`: the Master BreadLoaf numbers below.
- A review of the risky AI changes. It found and fixed the bugs below.

### Bugs found in review and fixed
1. **Lily and Madison re-selected their melee slot instantly every fight tick.** With part 4's slower hotbar
   switches, that cancelled any switch to another slot, so Archer Lily would never have got her bow out. They now
   draw the weapon only from their calm item, with the normal switch delay.
2. **FelixSyn and Harley grabbed their sword instantly every fight tick,** skipping the hotbar delay. Going back from
   the mace to the sword now takes their switch time.
3. **Asking for the slot already in hand while another switch was pending** queued a pointless delayed switch. It
   now just cancels the pending one.
4. **Crit chances shifted.** Part 4 made crits one decision per swing, where they used to be rolled every tick of the
   ~2-tick window (about 1−(1−c)²). That had quietly lowered everyone's crit rate.
   - Harley: preset crit chances changed to keep her exact old rate: Easy 0.44, Normal 0.64, Hard 0.84.
   - Normal defaults raised: ItzBreadLoaf 0.45 → 0.60, FelixSyn 0.60 → 0.75, Lily 0.25 → 0.30, Madison 0.30 → 0.35.
   - Master BreadLoaf's preset already meant per swing in the simulation, so it's unchanged.

### Final balance (Normal)

| | Reaction | Aim error / speed | Hotbar | Crit | W-tap / S-tap | Jump reset | Shield skill | Mistakes | Technique fails |
|---|---|---|---|---|---|---|---|---|---|
| ItzBreadLoaf | 200–300 ms | 4° / 30 | 4 ticks | 60% | 50% / 10% | 25% | 0.30 | 8% | 40% |
| ImLilyLol | 220–300 ms | 5° / 25 | 5 | 30% | 20% / 30% | 15% | 0.50 | 12% | 50% |
| Madison | 200–300 ms | 4.5° / 27 | 4 | 35% | 30% / 20% | 20% | 0.70 | 10% | 50% |
| FelixSyn | 170–250 ms | 3° / 35 | 3 | 75% | 50% / 30% | 40% | 0.40 | 6% | 15% |
| Harley | 150–220 ms | 2° / 40 | 2 | 64% (preset) | 70% / 40% | 60% | 0.70 | 3% | 10% |
| Master BreadLoaf | 300–400 ms (preset) | 5° / 28 | 4 | 25% (preset) | 40% / 20% | 30% | none | 12% whiffs (preset) | 25% (preset) |

Easy and Hard scale these automatically:
- Easy: slower reactions, 1.5× aim error, fewer tricks, more mistakes.
- Hard: quicker, 0.7× aim error, more tricks, fewer mistakes.

Every number is editable in `config/itzbreadloaf.json` under `skillProfiles`.

Master BreadLoaf, in the fight simulation, against an average player with a sword, a shield and 8 golden apples:

| | Easy | Normal | Hard |
|---|---|---|---|
| Plain diamond | 100% | 70% | 0% |
| Protection II + Sharpness II | 100% | 100% | 69% |
| Weak player, 4 apples | 81% | 0% | 0% |

His sword now also needs his crosshair on you (part 4), so the real Normal win rate should be at or above 70%.

### In-game test checklist (when you're ready to run the game)

| What | How | Expect |
|---|---|---|
| Boss nerf | Spawn egg or `/loafcraft boss`, fight in full diamond | 1 s warning before every smash and dive, no hit over 8 hearts, 100 health on Normal, no summons |
| Dragon timing | Kill the Ender Dragon | He appears after it dies, not during the fight |
| Human aim | Strafe around an ItzBreadLoaf | His view overshoots and lags; he misses when you strafe fast |
| Shields | Fight a Knight with an axe, then without | He doesn't block your axe (if skilled); against a sword, quick blocks only |
| Axe break | Give a Pirate an axe, hold your shield up | He switches to the axe, breaks the shield, switches back |
| Retreat | Fight Madison to low health with dirt in her inventory | She backs off, places blocks, eats |
| Techniques | `/loafcraft technique FelixSyn <move>` for each of the 5 moves | Each runs; pearl catch's second launch may not happen every time (vanilla) |
| Duel | `/loafcraft duel FelixSyn Harley` | They fight for up to 2 minutes |
| Mood | `/loafcraft mood <name> scared`, then `/loafcraft mood <name>` | Speed changes and a mood line; the readout shows mood, routine and style |
| Routine | `/time set 3000`, then noon, then 11000 | Work, then friends, then heading home |
| Idle | Stand near a character you fed, hold an item | Crouch greeting, looks at your item |
| Performance | Spawn 30 (raise `maxActiveCharacters`), `/loafcraft debug ai` | Total AI ms per tick; far characters show simple or paused |

### Things that rely on 1.21.11 or Carpet details I couldn't run
- **Jump resets:** a fake player's jump lands one tick after the hit.
- **Axe shield-breaking:** relies on `disable_blocking_for_seconds`.
- **The 5-tick shield raise:** relies on `blocks_attacks` block delay.
- **Pearl catch:** the second launch is unreliable in vanilla.
- **Wind jump and Wind Burst heights.**
- **Work stations:** opening and closing them through Carpet's use action and `closeContainer`.
- **Lecterns:** placing books during world generation.
- **Mixin target:** `LivingEntity.hurtServer` is confirmed in the 1.21.11 jar.
