package io.github.jfischer00.paintwarplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;

import net.milkbowl.vault.economy.Economy;

public final class PaintWarPlugin extends JavaPlugin implements Listener {
	// Map of games with names as keys
	Map<String, PaintWarGame> games = new HashMap<String, PaintWarGame>();

	// Plugin references
	WorldEditPlugin worldedit = getWorldEdit();
	Economy economy;

	boolean useVault;

	@Override
	public void onEnable() {
		// Save the default config if no config exists
		saveDefaultConfig();

		// Add all commands
		String[] commands = { "pw" };

		for (int i = 0; i < commands.length; i++) {
			getCommand(commands[i]).setExecutor(new PaintWarPluginCommandExecutor(this));
		}

		SignsHandler signHandler = new SignsHandler(this);

		// Allow this class to handle events (probably not best practice,
		// possible change?)
		this.getServer().getPluginManager().registerEvents(this, this);
		this.getServer().getPluginManager().registerEvents(signHandler, this);

		useVault = getConfig().getBoolean("config.useVault");

		if (useVault)
			economy = getEconomy();

		// Load games from the config.yml
		LoadGames();
	}

	@Override
	public void onDisable() {
		// Save games to the config
		SaveGames();

		getConfig().set("config.useVault", useVault);

		// Save the config
		saveConfig();
	}

	// Utility for sending messages to console and player
	public void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(message);
	}

	public void sendMessage(Player player, String message) {
		player.sendMessage(message);
	}

	private void LoadGames() {
		// Get list of arenas from config (suppressing because I know what type
		// it is [or I can blame the user :D])
		@SuppressWarnings("unchecked")
		List<Object> arenas = (List<Object>) getConfig().getList("arenas");

		// Loop through all arenas
		for (int y = 0; y < arenas.size(); y++) {
			// Set properties
			String name = "";
			Vector minv = new Vector(0, 0, 0);
			Vector maxv = new Vector(0, 0, 0);
			World world = null;

			// One arena (same suppression reason as above)
			@SuppressWarnings("unchecked")
			Map<String, Object> arena = (Map<String, Object>) arenas.get(y);

			// Loop through its properties
			for (Entry<String, Object> e : arena.entrySet()) {
				// Set the name
				name = (String) e.getKey();

				// More suppressed stuff
				@SuppressWarnings("unchecked")
				Map<String, Object> data = (Map<String, Object>) e.getValue();

				// Loop through that stuff
				for (Entry<String, Object> e1 : data.entrySet()) {
					// Check what data we're looking at
					switch (e1.getKey()) {
					// minimum coordinates
					case "mincoords":
						// Again with the suppression (I don't like warnings)
						@SuppressWarnings("unchecked")
						Map<String, Integer> mincoords = (Map<String, Integer>) e1.getValue();

						// Loop through minimum coordinates
						for (Entry<String, Integer> e2 : mincoords.entrySet()) {
							// Is it x, y, or z?
							switch (e2.getKey()) {
							// It's x
							case "x":
								// Set the minimum x coordinate
								minv.setX(e2.getValue());
								break;
							// y
							case "y":
								// Do the same thing for the y
								minv.setY(e2.getValue());
								break;
							// z
							case "z":
								// And the z
								minv.setZ(e2.getValue());
								break;
							default:
								// Shouldn't ever happen (probably should put an
								// error here)
								break;
							}
						}
						break;
					// It's maximum coordinates
					case "maxcoords":
						// No explanation, you've had too many already
						@SuppressWarnings("unchecked")
						Map<String, Integer> maxcoords = (Map<String, Integer>) e1.getValue();

						// Loop the maximum coordinates
						for (Entry<String, Integer> e2 : maxcoords.entrySet()) {
							// x, y, or z?
							switch (e2.getKey()) {
							// x
							case "x":
								// set
								maxv.setX(e2.getValue());
								break;
							// y
							case "y":
								// set
								maxv.setY(e2.getValue());
								break;
							// z
							case "z":
								// set
								maxv.setZ(e2.getValue());
								break;
							default:
								// Shouldn't happen
								break;
							}
						}
						break;
					// It's the world!
					case "world":
						// Set the world
						world = Bukkit.getWorld((String) e1.getValue());
						break;
					default:
						// Shouldn't happen
						System.out.println("Error!");
						break;
					}
				}
			}

			// Finally! Make the game...
			PaintWarGame game = new PaintWarGame(this, name, world, minv, maxv);

			// ...and add it
			games.put(game.GetName(), game);
		}
	}

	private void SaveGames() {
		// Blank arenas list
		List<Object> arenas = new ArrayList<Object>();

		// Loop through each game
		for (Entry<String, PaintWarGame> e : games.entrySet()) {
			// Get the game
			PaintWarGame game = e.getValue();

			// Get the world
			String world = game.GetWorld().getName();

			// Get the name
			String name = game.GetName();

			// What do you think these get?
			Vector minv = game.GetMinLocation();
			Vector maxv = game.GetMaxLocation();

			// Minimum and maximum coordinates
			Map<String, Integer> mincoords = new HashMap<String, Integer>();
			Map<String, Integer> maxcoords = new HashMap<String, Integer>();

			// Add the values
			mincoords.put("x", (int) minv.getX());
			mincoords.put("y", (int) minv.getY());
			mincoords.put("z", (int) minv.getZ());

			// Add more values
			maxcoords.put("x", (int) maxv.getX());
			maxcoords.put("y", (int) maxv.getY());
			maxcoords.put("z", (int) maxv.getZ());

			// Random data (jk :D)
			Map<String, Object> data = new HashMap<String, Object>();

			// Give it all the info
			data.put("mincoords", mincoords);
			data.put("maxcoords", maxcoords);
			data.put("world", world);

			// Make the arena
			Map<String, Object> arena = new HashMap<String, Object>();

			// Give it the data
			arena.put(name, data);

			// Add it to the list
			arenas.add(arena);
		}

		// Set the config arenas to the arenas list
		getConfig().set("arenas", arenas);
	}

	public WorldEditPlugin getWorldEdit() {
		// Get the WorldEdit plugin
		Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

		// If it's WorldEdit...
		if (p instanceof WorldEditPlugin) {
			// return it
			return (WorldEditPlugin) p;
		} else {
			// Shouldn't happen (probably should add error handling)
			return null;
		}
	}

	public Economy getEconomy() {
		// Get the economy provider
		RegisteredServiceProvider<Economy> ecoProvider = getServer().getServicesManager()
				.getRegistration(Economy.class);

		// If it exists
		if (ecoProvider != null) {
			// Return it
			return ecoProvider.getProvider();
		} else {
			// Shouldn't happen (error handling location?)
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onSnowballHit(ProjectileHitEvent e) {
		// If it's a snowball...
		if (e.getEntity() instanceof Snowball) {
			Snowball snowball = (Snowball) e.getEntity();

			// ...if a player threw it...
			if (snowball.getShooter() instanceof Player) {
				// Make a Player from the Entity
				Player shooter = (Player) snowball.getShooter();

				// ...if the player is in a game...
				if (shooter.hasMetadata("team")) {

					// Loop through the games
					for (Entry<String, PaintWarGame> e1 : games.entrySet()) {
						// Make a game
						PaintWarGame game = e1.getValue();

						// If the Player is in this game...
						if (game.IsInGame(shooter)) {
							// Make a WE selection
							CuboidSelection s = new CuboidSelection(game.GetWorld(), game.GetMinLocationLoc(),
									game.GetMaxLocationLoc());

							// This is used to find the right block
							BlockIterator iterator = new BlockIterator(e.getEntity().getWorld(),
									e.getEntity().getLocation().toVector(), e.getEntity().getVelocity().normalize(),
									0.0D, 4);

							// Make a block
							Block hitBlock = null;

							// Loop through possible blocks
							while (iterator.hasNext()) {
								// Set the hitBlock to the current block we're
								// checking
								hitBlock = iterator.next();

								// If it's not air, STOP!
								if (!hitBlock.getType().equals(Material.AIR)) {
									break;
								}
							}

							// If the block is on the ground of the WE
							// selection...
							if (s.contains(hitBlock.getLocation())
									&& hitBlock.getLocation().getY() == s.getMinimumPoint().getY()) {
								// Set it to stained clay
								hitBlock.setType(Material.STAINED_CLAY);

								// red = 14, blue = 11 (data values)
								// Change block to team color
								if (shooter.getMetadata("team").get(0).asString().equalsIgnoreCase("red")) {
									// Red
									hitBlock.setData((byte) 14);
									game.AddScore("red", hitBlock.getLocation());
								} else if (shooter.getMetadata("team").get(0).asString().equalsIgnoreCase("blue")) {
									// Blue
									hitBlock.setData((byte) 11);
									game.AddScore("blue", hitBlock.getLocation());
								}
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		// Get the Player from the event
		Player p = e.getPlayer();

		// If they're in a game...
		if (p.hasMetadata("team")) {
			// Loop through games
			for (Entry<String, PaintWarGame> e1 : games.entrySet()) {
				// Get the game
				PaintWarGame game = e1.getValue();

				// If the player's in the game...
				if (game.IsInGame(p)) {
					// Get the WE selection
					CuboidSelection s = new CuboidSelection(game.GetWorld(), game.GetMinLocationLoc(),
							game.GetMaxLocationLoc());

					// Stop movement from the arena
					if (!s.contains(e.getTo())) {
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onSnowballThrow(PlayerInteractEvent e) {
		// Get the Player from the event
		Player player = e.getPlayer();

		// If they're in a game...
		if (player.hasMetadata("team")) {
			// If they're holding the "gun"...
			if (e.getMaterial().name().equals("IRON_BARDING")) {
				// SHOOT!
				player.launchProjectile(Snowball.class);
			}
		}
	}
}