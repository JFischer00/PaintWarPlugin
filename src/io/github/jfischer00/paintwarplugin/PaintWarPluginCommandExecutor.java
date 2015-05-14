package io.github.jfischer00.paintwarplugin;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class PaintWarPluginCommandExecutor implements CommandExecutor {
	PaintWarPlugin paintwar;
	
	public PaintWarPluginCommandExecutor(PaintWarPlugin plugin) {
		paintwar = plugin;
	}

	public void sendMessage(CommandSender sender, String message) {
		sender.sendMessage(message);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		//Start a PaintWar game (/pwstart)
		if (cmd.getName().equalsIgnoreCase("pwstart")) {
			//If there is no game running...
			if (!paintwar.gameStarted) {
				//...start one
				paintwar.gameStarted = true;
				sendMessage(sender, ChatColor.GREEN + "PaintWar game started! Join using /pwjoin");
				return true;
			}
			//Game already started
			else {
				sendMessage(sender, ChatColor.RED + "A PaintWar is already running!");
				return false;
			}
		}
		//Join a PaintWar game (/pwjoin)
		else if (cmd.getName().equalsIgnoreCase("pwjoin")) {
			//If there is a game running...
			if (paintwar.gameStarted) {
				//...and if you are a player...
				if (sender instanceof Player) {
					//...and if you are not already in a game...
					if (!paintwar.players.contains(sender)) {
						//...join the game
						Player currentPlayer = (Player)sender;
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "sudo " + currentPlayer.getName() + " unlimited 332");
						
						int redCount = 0;
						int blueCount = 0;
						
						//Get each team's player count
						for (int i = 0; i < paintwar.players.size(); i++) {
							if (paintwar.players.get(i).getMetadata("team").get(0).asString().equalsIgnoreCase("red")) {
								redCount++;
							}
							else if (paintwar.players.get(i).getMetadata("team").get(0).asString().equalsIgnoreCase("blue")) {
								blueCount++;
							}
						}
						
						//Choose team based on player count
						if (redCount > blueCount) {
							currentPlayer.setMetadata("team", new FixedMetadataValue(paintwar, "blue"));
							paintwar.players.add(currentPlayer);
						}
						else if (blueCount > redCount) {
							currentPlayer.setMetadata("team", new FixedMetadataValue(paintwar, "red"));
							paintwar.players.add(currentPlayer);
						}
						else {
							Random rand = new Random();
							
							if (rand.nextInt(2) == 0) {
								currentPlayer.setMetadata("team", new FixedMetadataValue(paintwar, "red"));
								paintwar.players.add(currentPlayer);
							}
							else {
								currentPlayer.setMetadata("team", new FixedMetadataValue(paintwar, "blue"));
								paintwar.players.add(currentPlayer);
							}
						}
						
						sendMessage(sender, ChatColor.GREEN + "PaintWar game joined! You are on team " + currentPlayer.getMetadata("team").get(0).asString() + ".");
						return true;
					}
					//You are already in a game
					else {
						sendMessage(sender, ChatColor.RED + "You are already in a game!");
						return false;
					}
				}
				//You are not a player
				else {
					sendMessage(sender, ChatColor.RED + "Only players can join PaintWar games.");
					return false;
				}
			}
			//No game started
			else {
				sendMessage(sender, ChatColor.RED + "No PaintWar game is currently running!");
				return false;
			}
		}
		//Stop a PaintWar game (/pwstop)
		else if (cmd.getName().equalsIgnoreCase("pwstop")) {
			//If there is a game running...
			if (paintwar.gameStarted) {
				//...stop it
				paintwar.gameStarted = false;
				
				//Clear metadata
				for (int i = 0; i < paintwar.players.size(); i++) {
					paintwar.players.get(i).removeMetadata("team", paintwar);
				}
				
				paintwar.players.clear();
				
				sendMessage(sender, ChatColor.RED + "PaintWar game stopped!");
				return true;
			}
			//No game started
			else {
				sendMessage(sender, ChatColor.RED + "No PaintWar game is currently running!");
				return false;
			}
		}
		//List players in a PaintWar game (/pwlist)
		else if (cmd.getName().equalsIgnoreCase("pwlist")) {
			//If there is a game running...
			if (paintwar.gameStarted) {
				//...list players
				String message = ChatColor.GREEN + "Players currently playing PaintWar:\n";
				
				String redList = "";
				String blueList = "";
				
				for (int i = 0; i < paintwar.players.size(); i++) {
					if (paintwar.players.get(i).getMetadata("team").get(0).asString().equalsIgnoreCase("red")) {
						redList += paintwar.players.get(i).getDisplayName() + " ";
					}
					else if (paintwar.players.get(i).getMetadata("team").get(0).asString().equalsIgnoreCase("blue")) {
						blueList += paintwar.players.get(i).getDisplayName() + " ";
					}
				}
				
				message += ChatColor.RED + "  Red Team:\n  " + redList + "\n" + ChatColor.BLUE + "  Blue Team:\n  " + blueList;
				
				sendMessage(sender, message);
				return true;
			}
			//No game started
			else {
				sendMessage(sender, ChatColor.RED + "No PaintWar game is currently running!");
				return false;
			}
		}
		//Leave a PaintWar game (/pwleave)
		else if (cmd.getName().equalsIgnoreCase("pwleave")) {
			//If you are a player...
			if (sender instanceof Player) {
				//...and you are in the game...
				if (paintwar.players.contains((Player)sender)) {
					//...leave the game
					paintwar.players.get(paintwar.players.indexOf(sender)).removeMetadata("team", paintwar);
					paintwar.players.remove((Player)sender);
					
					sendMessage(sender, ChatColor.GREEN + "You have left the PaintWar game.");
					return true;
				}
				//You are not in the game
				else {
					sendMessage(sender, ChatColor.RED + "You are not in a PaintWar game!");
					return false;
				}
			}
			//You are not a player
			else {
				sendMessage(sender, ChatColor.RED + "Only players can leave PaintWar games.");
				return false;
			}
		}
		//Check a PaintWar game status (/pwstatus)
		else if (cmd.getName().equalsIgnoreCase("pwstatus")) {
			if (paintwar.gameStarted) {
				sendMessage(sender, ChatColor.GREEN + "A PaintWar game is running.");
				return true;
			}
			else {
				sendMessage(sender, ChatColor.RED + "A PaintWar game is not running.");
				return false;
			}
		}
		return false;
	}
}
