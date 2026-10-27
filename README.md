# openscape

openscape is an Old School RuneScape server you can run yourself. It runs revision 233 of the game and is
built on [RS Mod](https://github.com/rsmod/rsmod).

The goal is simple: make it play like the real OSRS, using the numbers from the
[OSRS Wiki](https://oldschool.runescape.wiki/). Quests aren't included, so anything a quest would unlock
already works as if you had finished it. Every account is a member.

I'm building it outward from Lumbridge, one area at a time.

![openscape roadmap](docs/roadmap.png)

## What you can do right now

- Explore **Lumbridge, Draynor Village, Al Kharid and Varrock**. The NPCs, shops, banks, doors and
  dialogue all work, and every monster you can attack has its proper drops.
- Train **Attack, Strength, Defence, Ranged, Hitpoints, Prayer, Magic, Mining, Smithing, Crafting,
  Woodcutting, Fishing, Cooking, Firemaking, Runecraft and Thieving**.
- Fight with prayers, the standard and Ancient spellbooks, Vengeance and special attacks.
- Die like you would in OSRS: you lose items, a gravestone holds them for you, and Death's Office can
  give them back.
- Watch out for aggressive monsters. Like in OSRS, they attack you unless your combat level is more
  than double theirs, and they leave you alone once you have spent 10 minutes in the same area.

## What's missing

- Monsters that should use magic or ranged (dark wizards, for example) still walk up and hit you in
  melee. I'm fixing that now, together with Fletching, Herblore and the rest of Magic.
- Agility, Slayer, Hunter, Construction and Farming aren't in yet.

The roadmap above shows what comes after that.

## Running it yourself

You'll need **Java 21** ([Adoptium](https://adoptium.net/temurin/releases/?version=21) works) and
**Git**. Building uses a few GB of memory, so if your PC has 8 GB, close other programs first.

**1. Download and set it up.** You only do this once:

```sh
git clone https://github.com/sixk-tagged/openscape.git
cd openscape
./gradlew install --console=plain
```

On Windows, type `gradlew.bat` instead of `./gradlew`.

This downloads the game cache, builds the server's own copy of it (that part takes a few minutes) and
creates the server's keys in the `.data` folder.

**2. Start the server:**

```sh
./gradlew run --console=plain
```

It's ready when the log says `Bound to ports: 43594`.

If you prefer IntelliJ IDEA, open the folder, let it load the Gradle project and run `GameServer`.
There are screenshots of each step in [docs/images](docs/images).

## Logging in

You'll need [RSProx](https://github.com/blurite/rsprox), a RuneLite-based client for private servers.
To tell it about your server, add this to `~/.rsprox/proxy-targets.yaml` (on Windows that's
`C:\Users\<you>\.rsprox\proxy-targets.yaml`):

```yaml
config:
  - name: openscape
    jav_config_url: https://client.blurite.io/jav_local_233.ws
    revision: 233
    modulus: <your modulus>
    runelite_bootstrap_commithash: cea91b9921a3647683ba8a5c22ec75c752c91b07
```

Replace `<your modulus>` with the long number after `Modulus:` in `.data/client.key`.

Then start RSProx, pick `openscape` and log in with any username. Your account is created the first
time you log in.

## Playing with friends

The server starts in `dev` mode, which is meant for testing on your own: every account is an admin,
passwords are ignored and XP is 150x. If other people are going to play, switch to `main` mode in
`.data/server.toml` (this file appears after the first start):

```toml
realm = 'main'
world = 1
first_launch = false
```

| | `dev` (default) | `main` |
|---|---|---|
| Meant for | testing on your own PC | a server other people play on |
| Admin commands | every account | nobody |
| Passwords | ignored | checked |
| XP rate | 150x | 1x |

Each mode keeps its own characters. On `main`, the first time someone logs in with a new username,
that account is created with the password they typed.

For people outside your home network to connect, you need to open TCP port 43594, and they need a
client config whose world list points at your address instead of `127.0.0.1`.

Never share `.data/game.key`. It's your server's private key. Players only ever need the modulus from
`client.key`.

## Changing the code

These are the commands you'll use most:

| Command | What it does |
|---|---|
| `./gradlew run` | Starts the server. |
| `./gradlew packCache` | Rebuilds the server's game cache. Run it after you change the map, types or symbols, and before integration tests, or they will hang. |
| `./gradlew test` | Runs the unit tests. |
| `./gradlew :content:skills:mining:integration` | Runs one module's integration tests, which simulate the game. Swap in any module's path. |
| `./gradlew :server:shared:integration` | Loads every script together and fails if two of them handle the same thing, which would stop the server from starting. |
| `./gradlew spotlessApply` | Formats the code. CI checks the formatting, so run it before you commit. |

Player saves live in `.data/saves/game.db` (an SQLite file). Back it up if your server has anything on
it you care about.

## Credits and license

openscape is a fork of [RS Mod](https://github.com/rsmod/rsmod) and keeps its
[ISC license](LICENSE.md). Big thanks to the RS Mod contributors, to Blurite for
[RSProx](https://github.com/blurite/rsprox), and to the [OSRS Wiki](https://oldschool.runescape.wiki/)
for all the data.

This is a fan project. It isn't affiliated with or endorsed by Jagex. Old School RuneScape and
RuneScape are trademarks of Jagex Ltd.
