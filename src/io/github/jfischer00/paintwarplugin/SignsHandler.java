package io.github.jfischer00.paintwarplugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignsHandler implements Listener {
	PaintWarPlugin paintwar;
	
	@EventHandler
	public void clickHandler(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			
			if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
				Sign sign = (Sign) block.getState();
				String[] lines = sign.getLines();
				
				if (lines[0].equals(ChatColor.BLUE + "PaintWar") && lines[1].equals(ChatColor.DARK_GREEN + "join")) {
					if (paintwar.games.keySet().contains(lines[2])) {
						PaintWarGame game = paintwar.games.get(lines[2]);
						Player player = event.getPlayer();
						
						if (game.Join(player)) {
							paintwar.sendMessage(player, ChatColor.GREEN + "PaintWar game with name " + game.GetName() + " joined! You are on the " + game.GetTeam(player) + " team.");
						}
						else {
							paintwar.sendMessage(player, ChatColor.RED + "You are already in a PaintWar game!");
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void changeColor(SignChangeEvent event) {
		String[] lines = event.getLines();
		
		if (lines[0].equals("[pw]"))
			event.setLine(0, ChatColor.BLUE + "PaintWar");
		else
			return;
		
		if (lines[1].equals("join")) {
			event.setLine(1, ChatColor.DARK_GREEN + "join");
		}
		/*else if (lines[1].equals("status")) {
			event.setLine(1, lines[2]);
			event.setLine(2, ChatColor.DARK_RED + "Waiting");
			event.setLine(3, ChatColor.DARK_AQUA + "0/4");
		}
		
		event.setCancelled(true);
		
		statusCreation(event);*/
	}
	
	/*public void statusCreation(SignChangeEvent event) {
		String[] lines = event.getLines();
		
		for (String l : lines)
			System.out.println(l);
		
		if (lines[0].equals(ChatColor.BLUE + "PaintWar") && lines[1].equals("status")) {
			if (paintwar.games.containsKey(lines[2])) {
				PaintWarGame game = paintwar.games.get(lines[2]);
				
				BukkitScheduler scheduler = paintwar.getServer().getScheduler();
				scheduler.scheduleSyncRepeatingTask(paintwar, new Runnable() {
					@Override
					public void run() {
						if (game.IsGameRunning())
							event.setLine(2, "Running");
						else
							event.setLine(2, "Stopped");
						
						event.setLine(3, Integer.toString(game.GetPlayerData().size()));
						
						System.out.println("updated");
					}
				}, 0L, 1200L);
			}
		}
	}*/
	
	public SignsHandler(PaintWarPlugin paintwar) {
		this.paintwar = paintwar;
	}
}