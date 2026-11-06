# Mahito Soul Regen (Jujutsu Craft addon, Forge 1.20.1)

Damage Mahito takes from anyone without soul perception regenerates quickly. This is on top of
the damage reduction Jujutsu Craft already applies; the addon does not change that reduction.
Soul observers hurt him normally.

Built against Jujutsu Craft v51. Values below were read from that jar (decompiled `WhenEntityAttacked2Procedure`).

JJC's own reduction: damage to Mahito from an attacker without soul perception is halved (x0.5), except while Mahito has
`unstable` or `domain_amplification`, or for attacks flagged `ignore`. JJC cancels the original hit and re-applies the
reduced damage; this addon heals back what is left of that re-applied hit. By default it skips the same exempt cases.

## Who counts as Mahito
- Entity `jujutsucraft:mahito`
- Any player whose technique is Mahito's (technique id 15)
- Anything tagged `/tag <name> add mahito_regen`

## Who counts as a soul observer (no regen against them)
- Any entity type in JJC's own tag `forge:soul_perception` (Sukuna, Itadori, Toji, Mahito, Maki, Panda, Mahoraga, ...)
- Players who completed the `jujutsucraft:observation_of_the_soul` advancement
- Anything tagged `/tag <name> add soul_observer`, or listed in `extraObserverEntityIds`

## Build

**Easiest (no setup): GitHub builds it for you**
1. Create a new GitHub repository and upload everything in this folder (keep the `.github` folder).
2. Open the repo's **Actions** tab. The "Build mod jar" workflow runs automatically (or press "Run workflow").
3. When it finishes, open the run and download the `mahitosoulregen-jar` artifact. Unzip it and use the file `mahitosoulregen-1.0.0.jar`.

**Locally:** install JDK 17 and Gradle 8.1.1, then run `gradle build` in this folder. The jar appears in `build/libs/`.

Put the jar in `mods/` next to Jujutsu Craft (Forge 1.20.1, 47.x). No Jujutsu Craft jar is needed to compile.

## Tune (config/mahitosoulregen-common.toml)
- `regenPerTick` (default 0.05): fraction of the remaining unhealed damage restored each tick. Healing starts fast and tapers off: 0.05 is about half back in 0.7s and 95% in 3s; 0.15 is about half in 0.2s and 95% in 1s.
- `instantFraction` (default 0 = off): optional extra instant reduction
- `environmentCountsAsUnobserved` (default false): also regen fire/fall/etc. damage, which JJC does not reduce
- `mirrorJjcExemptions` (default true) and `exemptVictimEffects`: no regen when JJC's own reduction is skipped


