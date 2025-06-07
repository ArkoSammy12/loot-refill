# Loot Refill

This is a Fabric server-side Minecraft mod that allows chests and other contains with naturally generated loot to refill themselves automatically.
## Configuration

The mod includes several settings accessible via a config file named `lootrefill.toml` which can be found in the server's config folder. The settings can be edited in-game or from the console via commands starting with `/lootrefill config`, and they can be reloaded with `/lootrefill config reload`.
The following is the default configuration file. Each setting is commented with an explanation as to what it does.

```toml
[preferences]
	#(Default = 1800) The time in seconds between each refill. Note that a refill only occurs whenever a loot container is opened.
	time_until_refill = 1800
	#(Default = -1) The max amount of refills allowed per container. -1 is the minimum value and enables unlimited refills. 0 disables loot refills. For a given container, this value can be overridden by setting the individual max refill amount for that container.
	max_refills = 1
	#(Default = false) If enabled, a container will only refill if the amount of times a player has refilled a container is lower than the max refill setting value.
	per_player_refill_counts = false
	#(Default = true) Prevents refilling containers when they are not fully empty.
	refill_only_when_empty = true
	#(Default = false) If enabled, containers with a set loot table id and a refill count lower than the max refill setting value, or if the max refill amount is set to unlimited, will be unbreakable by players. They can still be broken by indirect means such as explosions.
	protect_loot_containers = false
```

## Manually set a loot container's loot table id and max refill amount

Since there is no way to retroactively assign loot table ids for already looted naturally generated containers, the mod includes a command to set one manually:

`/lootrefill setLootTableId <loot_table_id>`

where `<loot_table_id>` is the registry key identifier of the loot table id you wish to assign.

You can also specify the coordinates and dimension of the loot containers for use from a console:

```
/lootrefill add_loot_table_id <loot_table_id> <x> <y> <z> <dimension>
```

You can also set the max refill amount for a specific loot container by using:

`/lootrefill setMaxRefillAmount <max_refill_amount>`

where `<max_refill_amount>` is an integer value. By default, all containers have an individual max refill amount of `-1`. Setting it to 0 or higher will make the specific container use its individual max refill amount instead of the one specified in the config.

Finally, this command also accepts a position and dimension argument for use from the console:

`/lootrefill setMaxRefillAmount <max_refill_amount> <x> <y> <z> <dimension>`

## Support

If you would like to report a bug, or make a suggestion, you can do so via the mod's [issue tracker](https://github.com/ArkoSammy12/loot-refill/issues).

## Building

Clone this repository on your PC, then open your command line prompt on the main directory of the mod, and run the command: `gradlew build`. Once the build is successful, you can find the mod under `/loot-refill/build/libs`. Use the .jar file without the `"sources"`.