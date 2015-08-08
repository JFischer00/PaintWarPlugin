package io.github.jfischer00.paintwarplugin;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;

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
		
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	@Override
	public void onDisable() {
		saveConfig();
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
