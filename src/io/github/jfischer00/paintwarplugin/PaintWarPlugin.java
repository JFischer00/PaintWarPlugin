package io.github.jfischer00.paintwarplugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaintWarPlugin extends JavaPlugin implements Listener {
	//Globals
	boolean gameStarted = false;
	List<Player> players = new ArrayList<Player>();
	
	@Override
	public void onEnable() {
		//Add all commands
		String[] commands = {"pwstart", "pwjoin", "pwstop", "pwlist", "pwleave", "pwstatus"};
		
		for (int i = 0; i < commands.length; i++) {
			getCommand(commands[i]).setExecutor(new PaintWarPluginCommandExecutor(this));
		}
	}
	
	@Override
	public void onDisable() {
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
				if (players.contains(shooter)) {
					snowball.getLocation().getBlock().setType(Material.STAINED_CLAY);
					
					//red = 14, blue = 11
					
					//Change block to team color
					if (shooter.getMetadata("team").get(0).asString().equalsIgnoreCase("red")) {
						snowball.getLocation().getBlock().setData((byte) 14);
					}
					else if (shooter.getMetadata("team").get(0).asString().equalsIgnoreCase("blue")) {
						snowball.getLocation().getBlock().setData((byte) 11);
					}
				}
			}
		}
	}
}
