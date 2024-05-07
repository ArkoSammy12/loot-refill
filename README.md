# Loot Refill

This is a Fabric server-side Minecraft mod that allows chests and other contains with naturally generated loot to refill themselves automatically.

## Features

The mod adds 4 Minecraft gamerules for server-admins to configure the mod with. These are:

- `secondsUntilLootRefill` - The number of seconds between each loot refill. The Default value is 1800 seconds (30 minutes).
- `maxAmountOfLootRefills` - The maximum number of loot refills allowed per container. The Default value is -1, which corresponds to unlimited refills. A value of 0 corresponds to disabling the refilling of loot.
- `refillLootOnlyWhenEmpty` - If set to `true`, the container will only begin its timer countdown once all items have been removed from it. Once finished, the container will only refill its loot if it is still empty. Default value is `true`.
- `protectLootContainers` - If set to `true`, the container will be unable to broken by players. It is still vulnerable to other forms of destruction, such as explosions. Default value is `false`.

## Manually add a loot table to a container block

Since this mod uses NBT data to store the loot table IDs of naturally generated chests, this means that chests and other container blocks generated before this mod is added to the server will not be taken into account by this mod. To mitigate this, the mod includes a command to manually add a loot table ID to a container block.

To use this command, you must first have a permission level of four or higher, then look at the container block that you wish to add a loot table ID to, and run the following command:

```
/lootrefill add_loot_table_id <loot_table_id> 
```

Alternatively, you can run this command by specifying the container's coordinates as well as the dimension the container is in. This allows you to run the command via the server's console:

```
/lootrefill add_loot_table_id <loot_table_id> <x> <y> <z> <dimension>
```

## Support

If you would like to report a bug, or make a suggestion, you can do so via the mod's [issue tracker](https://github.com/ArkoSammy12/loot-refill/issues) or join my [Discord server](https://discord.gg/wScNgcvJ3y).

## Building

Clone this repository on your PC, then open your command line prompt on the main directory of the mod, and run the command: `gradlew build`. Once the build is successful, you can find the mod under `/loot-refill/build/libs`. Use the .jar file without the `"sources"`.