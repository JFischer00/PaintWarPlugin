package io.github.jfischer00.paintwarplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public final class PaintWarPlugin extends JavaPlugin implements Listener {
	Map<String, PaintWarGame> games = new HashMap<String, PaintWarGame>();

	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		//Add all commands
		String[] commands = {"pw"};

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
		try {
			@SuppressWarnings("unchecked")
			List<Map<String, Map<String, Map<String, Integer>>>> arenas = (List<Map<String, Map<String, Map<String, Integer>>>>) getConfig().getList("arenas");
			
			for (int y = 0; y < arenas.size(); y++) {
				Map<String, Map<String, Map<String, Integer>>> arena = arenas.get(y);
				
				for (Entry<String, Map<String, Map<String, Integer>>> e : arena.entrySet()) {
					String name = e.getKey();
					
					Vector mincoords = new Vector(0, 0, 0);
					Vector maxcoords = new Vector(0, 0, 0);
					
					Map<String, Map<String, Integer>> allcoords = e.getValue();
					
					for (Entry<String, Map<String, Integer>> e1 : allcoords.entrySet()) {
						Map<String, Integer> coords = e1.getValue();
						
						for (Entry<String, Integer> e2 : coords.entrySet()) {
							if (e1.getKey().equals("minlocation")) {
								switch (e2.getKey()) {
									case "x": mincoords.setX(e2.getValue());
											  break;
									case "y": mincoords.setY(e2.getValue());
											  break;
									case "z": mincoords.setZ(e2.getValue());
											  break;
									default: getLogger().log(Level.SEVERE, "Invalid coordinates");
											 break;
								}
							}
							else if (e1.getKey().equals("maxlocation")) {
								switch (e2.getKey()) {
									case "x": maxcoords.setX(e2.getValue());
											  break;
									case "y": maxcoords.setY(e2.getValue());
											  break;
									case "z": maxcoords.setZ(e2.getValue());
											  break;
									default: getLogger().log(Level.SEVERE, "Invalid coordinates");
											 break;
								}
							}
						}	
					}
					
					PaintWarGame game = new PaintWarGame(this, name, mincoords, maxcoords);
					
					games.put(game.GetName(), game);
				}
			}
		}
		catch (NullPointerException e) {
			
		}
	}

	private void SaveGames() {
		List<Map<String, Map<String, Map<String, Integer>>>> arenas = new ArrayList<Map<String, Map<String, Map<String, Integer>>>>();
		
		for (Entry<String, PaintWarGame> e : games.entrySet()) {
			Map<String, Map<String, Map<String, Integer>>> arena = new HashMap<String, Map<String, Map<String, Integer>>>();
			Map<String, Map<String, Integer>> allcoords = new HashMap<String, Map<String, Integer>>();
			Map<String, Integer> mincoords = new HashMap<String, Integer>();
			Map<String, Integer> maxcoords = new HashMap<String, Integer>();
			
			PaintWarGame game = e.getValue();
			
			mincoords.put("x", (int) game.GetMinLocation().getX());
			mincoords.put("y", (int) game.GetMinLocation().getY());
			mincoords.put("z", (int) game.GetMinLocation().getZ());
			
			maxcoords.put("x", (int) game.GetMaxLocation().getX());
			maxcoords.put("y", (int) game.GetMaxLocation().getY());
			maxcoords.put("z", (int) game.GetMaxLocation().getZ());
			
			allcoords.put("minlocation", mincoords);
			allcoords.put("maxlocation", maxcoords);
			
			arena.put(game.GetName(), allcoords);
			
			arenas.add(arena);
		}
		
		getConfig().set("arenas", arenas);
	}

	public WorldEditPlugin getWorldEdit() {
		Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		
		if (p instanceof WorldEditPlugin) {
			return (WorldEditPlugin) p;
		}
		else {
			return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onSnowballHit(ProjectileHitEvent e) {
		//If it's a snowball...
		if (e.getEntity() instanceof Snowball) {
			Snowball snowball = (Snowball)e.getEntity();

			//...if a player threw it...
			if (snowball.getShooter() instanceof Player) {
				Player shooter = (Player)snowball.getShooter();

				//...if the player is in a game...
				if (shooter.hasMetadata("team")) {
					BlockIterator iterator = new BlockIterator(e.getEntity().getWorld(), e.getEntity().getLocation().toVector(), e.getEntity().getVelocity().normalize(), 0.0D, 4);

					Block hitBlock = null;

					while (iterator.hasNext()) {
						hitBlock = iterator.next();

						if (!hitBlock.getType().equals(Material.AIR)) {
							break;
						}
					}

					hitBlock.setType(Material.STAINED_CLAY);
					
					//red = 14, blue = 11
					//Change block to team color
					if (shooter.getMetadata("team").get(0).asString().equalsIgnoreCase("red")) {
						hitBlock.setData((byte)14);
					}
					else if (shooter.getMetadata("team").get(0).asString().equalsIgnoreCase("blue")) {
						hitBlock.setData((byte)11);
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
