package io.github.jfischer00.paintwarplugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignsHandler implements Listener {
	PaintWarPlugin paintwar;
	
	@EventHandler
	public void onSignClicked(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			
			if (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN) {
				Sign sign = (Sign) block.getState();
				String[] lines = sign.getLines();
				
				if (lines[0].equals("[pw]") && lines[1].equals("join")) {
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
	
	/*@EventHandler
	public void onSignPlaced(SignChangeEvent event) {
		String[] lines = event.getLines();
		
		if (lines[0].equals("[pw]") && lines[1].equals("status")) {
			if (paintwar.games.keySet().contains(lines[2])) {
				PaintWarGame game = paintwar.games.get(lines[2]);
			
				Bukkit.getServer().getScheduler().runTaskAsynchronously(paintwar, new Runnable() {
					@Override
					public void run() {
						if (game.IsGameRunning()) {
							event.setLine(0, ChatColor.AQUA + game.GetName());
							event.setLine(1, ChatColor.GREEN + "Running");
						}
						else {
							event.setLine(0, ChatColor.AQUA + game.GetName());
							event.setLine(1, ChatColor.RED + "Stopped");
						}
					}
				});
			}
		}
	}*/
	
	public SignsHandler(PaintWarPlugin paintwar) {
		this.paintwar = paintwar;
	}
}