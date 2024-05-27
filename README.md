# RePaperUtils
A modular plugin providing wide variety of utility functionalities, developed with focus on ability to enabling and disabling modules during server operations.
Some of the modules were create to solve very specific issues that server owners of servers technically supported by ReModded (eg. [Denoria](https://denoria.pl) and [Terestia]()) have had.


## Commands
Main command is `/repaperutils` (`reu`).

| Argument                          | Description                                  | Permission                |
|-----------------------------------|----------------------------------------------|---------------------------|
| /repaperutils reload              | Reload a plugin configuration & all module.  | repaperutils.repaperutils |
| /repaperutils reload `<module>`   | Reload a specific module.                    | repaperutils.repaperutils |
| /repaperutils enable `<module>`   | Enable a specific module.                    | repaperutils.repaperutils |
| /repaperutils disable `<module>`  | Disable a specific module.                   | repaperutils.repaperutils |

## Modules

### [BlockPvPEscape](src/main/java/net/remodded/repaperutils/modules/BlockPvPEscapeModule.java)
NOTE: Requires plugins `Lands` and `PvPManager` to work.

Forbids player in active combat from entering non combat areas.



### [BlocksCommandsModule](src/main/java/net/remodded/repaperutils/modules/BlocksCommandsModule.java)
Executes command on player interaction with block.

#### Example Config
```yml
actions:                        # List of block interactions
  crafting:
    block: crafting_table       # Block to handle interaction with
    cancel: true                # Do cancel default interaction behaviour
    shift: false                # Requires shift being pressed
    commands:                   # List of commands to exectute (note: @p will be player)
    - say opened crafting      
```



### [DisableMobSpawners](src/main/java/net/remodded/repaperutils/modules/DisableMobSpawnersModule.java)
Forbids mob spawners from spawnig mobs.



### [DisablePortal](src/main/java/net/remodded/repaperutils/modules/DisablePortalsModule.java)
Disables portals from teleporting players.



### [EnchantmentsBlacklistModule](src/main/java/net/remodded/repaperutils/modules/EnchantmentsBlacklistModule.java)
Allows to entierly replace specific enchantments. Works during enchantment of an item and can replace already enchanted items.

#### Config

```yml
replaceExisting: false                        # Do replace echantments on existing items?
enchantments:                                 # Map of enchantment and it replacement
  minecraft:sharpness: minecraft:unbreaking
  minecraft:protection: minecraft:unbreakin
```



### [EntityBlacklist](src/main/java/net/remodded/repaperutils/modules/EntityBlacklistModule.java)
Forbids specific mobs from spawning.

#### Config
```yml
blacklist:          # List of mobs frobiden from spawning
  - minecraft:bat
```



### [Hardcore](src/main/java/net/remodded/repaperutils/modules/HardcoreModule.java)
Makes survial a little bit harder

```yml
blockAnimalBreading: true,          # Blocks breading of animals
blockChickenEggSpawn: true,         # Blocks spawnig chickens from thrown eggs

slowCropsGrowth: true,              # Slows crop grouth 4x
blockPlantGrowthWithoutSky: true    # Makes plant require direct sky access.
```



### [InvulnerabilityFix](src/main/java/net/remodded/repaperutils/modules/InvulnerabilityModule.java)
Disables Invulnerability flag on players server join.



### [MobSpawnSwitch](src/main/java/net/remodded/repaperutils/modules/MobSpawnSwitchModule.java)
Controls `doMobSpawning` gamerule based on online players count.

#### Config
```yml
worlds:             # List of dimmentions that are controlled
  - world   
onPlayerCount: 40   # Threshold of players bellow witch enable mob spawn.
offPlayerCount: 60  # Threshold of players above witch disable mob spawn.
```



### [PotionsBlacklist](src/main/java/net/remodded/repaperutils/modules/PotionsBlacklistModule.java)
Forbids brewing potions with specific effect.

```yml
potions:                        # List of forbidden potion effects
  - minecraft:instant_health
```



### [RenewableChests](src/main/java/net/remodded/repaperutils/modules/RenewableChestsModule.java)
Allows to create a chests witch loot renews.

#### Command

Command is `/renewablechests`.

| Argument                                                                         | Description                              | Permission                   |
|----------------------------------------------------------------------------------|------------------------------------------|------------------------------|
| /renewablechests add chest `<chestName>` `<blockPos>`                            | Creates renewable chest at location.     | repaperutils.renewablechests |
| /renewablechests add item `<chestName>` `<minAmmount>` `<maxAmmount>` `<change>` | Adds a held item to loot table of chest. | repaperutils.renewablechests |
| /renewablechests set time `<chestName>` `<hour>` `<minute>` `<second>`           | Sets a time of chest renewal.            | repaperutils.renewablechests |
| /renewablechests renew `<chestName>`                                             | Instantly renews a chest loot.           | repaperutils.renewablechests |




### [Restart](src/main/java/net/remodded/repaperutils/modules/RestartModule.java)
Add command to restart server with delay and bossbar coutdown message.

#### Config
```yml
defaultDelay: 300                         # Default restart delay (in seconds)
bossbarMessage: "&b&eRestart in &2{}"     # Bossbar countdown message (`{}` will be replaced with left time)
```



### [StartupCommands](src/main/java/net/remodded/repaperutils/modules/StartupCommandsModule.java)
Executes commands at server startup.

#### Config
```yml
delay: 200                  # Delay ater startap (in ticks)
commands:                   # List of commands to execute at startup
  - say Server started!!!
```



### [TextCommands](src/main/java/net/remodded/repaperutils/modules/TextCommandsModule.java)
Adds a simple commands that can replay with text.

#### Config
```yml
commands:       # Map of commands
  ping:         # Each command is a list of replay messages
    - &8Pong
```



### [TimedEffect](src/main/java/net/remodded/repaperutils/modules/TimedEffectModule.java)
Adds ability to set global potion effect for x seconds.

#### Command

Command is `/timedeffect`.

| Argument                                          | Description                          | Permission               |
|---------------------------------------------------|--------------------------------------|--------------------------|
| /timedeffect list                                 | Lists all active global effects.     | repaperutils.timedeffect |
| /timedeffect add `<effect>` `<time>` `[strength]` | Adds global effect for `x` secound.  | repaperutils.timedeffect |



### [Vouchers](src/main/java/net/remodded/repaperutils/modules/VoucherModule.java)
Adds voucher codes that can be claimed for in game items.

#### Command

Command is `/voucher`.

| Argument              | Description                         | Permission               |
|-----------------------|-------------------------------------|--------------------------|
| /voucher `<code>`     | Claim voucher code.                 | repaperutils.voucher     |
| /voucher add `<code>` | Creates voucher code for held item. | repaperutils.voucher.add |



### [WitherBuildBlocker](src/main/java/net/remodded/repaperutils/modules/WitherBuildBlockerModule.java)
Forbids bilding a wither.
