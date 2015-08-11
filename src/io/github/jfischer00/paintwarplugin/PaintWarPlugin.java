package io.github.jfischer00.paintwarplugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
		//Add all commands
		String[] commands = {"pw"};

		for (int i = 0; i < commands.length; i++) {
			getCommand(commands[i]).setExecutor(new PaintWarPluginCommandExecutor(this));
		}

		this.getServer().getPluginManager().registerEvents(this, this);
		
		LoadGames();
	}
	
	private void LoadGames() {
		List<Map<?, ?>> test = getConfig().getMapList("test");
		
		for (Map<?, ?> e : test) {
			for (Entry<?, ?> i : e.entrySet()) {
				System.out.println(i.getKey());
			}
		}
	}

	private void SaveGames() {
		for (int i = 0; i < 6; i++) {
			Vector minloc = new Vector(i, i, i);
			Vector maxloc = new Vector(i + 1, i + 1, i + 1);
			String name = "test" + i;
			
			getConfig().set("test." + name + ".minlocation.x", minloc.getX());
			getConfig().set("test." + name + ".minlocation.y", minloc.getY());
			getConfig().set("test." + name + ".minlocation.z", minloc.getZ());
			
			getConfig().set("test." + name + ".maxlocation.x", maxloc.getX());
			getConfig().set("test." + name + ".maxlocation.y", maxloc.getY());
			getConfig().set("test." + name + ".maxlocation.z", maxloc.getZ());
		}
	}
	
	@Override
	public void onDisable() {
		SaveGames();
		saveConfig();
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
