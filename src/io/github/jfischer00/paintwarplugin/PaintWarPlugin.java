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
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;

public final class PaintWarPlugin extends JavaPlugin implements Listener {
	Map<String, PaintWarGame> games = new HashMap<String, PaintWarGame>();
	WorldEditPlugin worldedit = getWorldEdit();

	@Override
	public void onEnable() {
		saveDefaultConfig();

		// Add all commands
		String[] commands = { "pw" };

		for (int i = 0; i < commands.length; i++) {
			getCommand(commands[i]).setExecutor(new PaintWarPluginCommandExecutor(this));
		}

		this.getServer().getPluginManager().registerEvents(this, this);

		LoadGames();
	}

	@Override
	public void onDisable() {
		SaveGames();
		saveConfig();
	}

	private void LoadGames() {
		@SuppressWarnings("unchecked")
		List<Object> arenas = (List<Object>) getConfig().getList("arenas");

		for (int y = 0; y < arenas.size(); y++) {
			String name = "";
			Vector minv = new Vector(0, 0, 0);
			Vector maxv = new Vector(0, 0, 0);
			World world = null;

			@SuppressWarnings("unchecked")
			Map<String, Object> arena = (Map<String, Object>) arenas.get(y);

			for (Entry<String, Object> e : arena.entrySet()) {
				name = (String) e.getKey();

				@SuppressWarnings("unchecked")
				Map<String, Object> data = (Map<String, Object>) e.getValue();

				for (Entry<String, Object> e1 : data.entrySet()) {
					switch (e1.getKey()) {
						case "mincoords":
							@SuppressWarnings("unchecked")
							Map<String, Integer> mincoords = (Map<String, Integer>) e1.getValue();

							for (Entry<String, Integer> e2 : mincoords.entrySet()) {
								switch (e2.getKey()) {
									case "x":
										minv.setX(e2.getValue());
										break;
									case "y":
										minv.setY(e2.getValue());
										break;
									case "z":
										minv.setZ(e2.getValue());
										break;
									default:
										break;
								}
							}
							break;
						case "maxcoords":
							@SuppressWarnings("unchecked")
							Map<String, Integer> maxcoords = (Map<String, Integer>) e1.getValue();

							for (Entry<String, Integer> e2 : maxcoords.entrySet()) {
								switch (e2.getKey()) {
									case "x":
										maxv.setX(e2.getValue());
										break;
									case "y":
										maxv.setY(e2.getValue());
										break;
									case "z":
										maxv.setZ(e2.getValue());
										break;
									default:
										break;
								}
							}
							break;
						case "world":
							world = Bukkit.getWorld((String) e1.getValue());
							break;
						default:
							System.out.println("Error!");
							break;
					}
				}
			}

			PaintWarGame game = new PaintWarGame(this, name, world, minv, maxv);

			games.put(game.GetName(), game);
		}
	}

	private void SaveGames() {
		List<Object> arenas = new ArrayList<Object>();

		for (Entry<String, PaintWarGame> e : games.entrySet()) {
			PaintWarGame game = e.getValue();

			String world = game.GetWorld().getName();

			String name = game.GetName();

			Vector minv = game.GetMinLocation();
			Vector maxv = game.GetMaxLocation();

			Map<String, Integer> mincoords = new HashMap<String, Integer>();
			Map<String, Integer> maxcoords = new HashMap<String, Integer>();

			mincoords.put("x", (int) minv.getX());
			mincoords.put("y", (int) minv.getY());
			mincoords.put("z", (int) minv.getZ());

			maxcoords.put("x", (int) maxv.getX());
			maxcoords.put("y", (int) maxv.getY());
			maxcoords.put("z", (int) maxv.getZ());

			Map<String, Object> data = new HashMap<String, Object>();

			data.put("mincoords", mincoords);
			data.put("maxcoords", maxcoords);
			data.put("world", world);

			Map<String, Object> arena = new HashMap<String, Object>();

			arena.put(name, data);

			arenas.add(arena);
		}

		getConfig().set("arenas", arenas);
	}

	public WorldEditPlugin getWorldEdit() {
		Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

		if (p instanceof WorldEditPlugin) {
			return (WorldEditPlugin) p;
		} else {
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
				Player shooter = (Player) snowball.getShooter();

				// ...if the player is in a game...
				if (shooter.hasMetadata("team")) {

					for (Entry<String, PaintWarGame> e1 : games.entrySet()) {
						PaintWarGame game = e1.getValue();

						if (game.IsInGame(shooter)) {
							CuboidSelection s = new CuboidSelection(game.GetWorld(), game.GetMinLocationLoc(),
									game.GetMaxLocationLoc());

							BlockIterator iterator = new BlockIterator(e.getEntity().getWorld(),
									e.getEntity().getLocation().toVector(), e.getEntity().getVelocity().normalize(),
									0.0D, 4);

							Block hitBlock = null;

							while (iterator.hasNext()) {
								hitBlock = iterator.next();

								if (!hitBlock.getType().equals(Material.AIR)) {
									break;
								}
							}

							if (s.contains(hitBlock.getLocation())
									&& hitBlock.getLocation().getY() == s.getMinimumPoint().getY()) {
								hitBlock.setType(Material.STAINED_CLAY);

								// red = 14, blue = 11
								// Change block to team color
								if (shooter.getMetadata("team").get(0).asString().equalsIgnoreCase("red")) {
									hitBlock.setData((byte) 14);
								} else if (shooter.getMetadata("team").get(0).asString().equalsIgnoreCase("blue")) {
									hitBlock.setData((byte) 11);
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
		Player p = e.getPlayer();

		if (p.hasMetadata("team")) {
			for (Entry<String, PaintWarGame> e1 : games.entrySet()) {
				PaintWarGame game = e1.getValue();

				if (game.IsInGame(p)) {
					CuboidSelection s = new CuboidSelection(game.GetWorld(), game.GetMinLocationLoc(), game.GetMaxLocationLoc());
					
					if (!s.contains(e.getTo())) {
						e.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler
	public void onSnowballThrow(PlayerInteractEvent e) {
		Player player = e.getPlayer();

		if (player.hasMetadata("team")) {
			if (e.getMaterial().name().equals("IRON_BARDING")) {
				player.launchProjectile(Snowball.class);
			}
		}
	}
}
