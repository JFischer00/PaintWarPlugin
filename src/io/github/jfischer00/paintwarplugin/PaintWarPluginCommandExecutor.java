package io.github.jfischer00.paintwarplugin;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.bukkit.selections.Selection;

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
			
		if (args.length == 0) {
			String message = "" + ChatColor.DARK_BLUE + ChatColor.BOLD + "PaintWar Commands and Usage:\n" + ChatColor.RESET + ChatColor.YELLOW +
							 "    create: Create a PaintWar game.\n" +
							 "    delete: Delete a PaintWar game.\n" +
							 "    start: Start a PaintWar game.\n" +
							 "    stop: Stop a PaintWar game.\n" +
							 "    join: Join a PaintWar game.\n" +
							 "    leave: Leave a PaintWar game.\n" +
							 "    list: List all players in a PaintWar game.\n" +
							 "    status: Show the status of a PaintWar game.";
			
			sendMessage(sender, message);
		}
		//Create a PaintWar game (/pw create)
		else if (args[0].equalsIgnoreCase("create")) {
			if (args.length == 2) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					Selection s = paintwar.getWorldEdit().getSelection(p);
					
					int minX, minY, minZ, maxX, maxY, maxZ;
					
					minX = s.getMinimumPoint().getBlockX();
					minY = s.getMinimumPoint().getBlockY();
					minZ = s.getMinimumPoint().getBlockZ();
					maxX = s.getMaximumPoint().getBlockX();
					maxY = s.getMaximumPoint().getBlockY();
					maxZ = s.getMaximumPoint().getBlockZ();
					
					if (!paintwar.games.containsKey(args[1])) {
						PaintWarGame game = new PaintWarGame(paintwar, args[1], new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));
						paintwar.games.put(game.GetName(), game);
						
						sendMessage(sender, ChatColor.GREEN + "PaintWar game with name " + game.GetName() + " created! Start it with /pw start " + game.GetName());
					}
					else {
						sendMessage(sender, ChatColor.RED + "A PaintWar game with name " + args[1] + " already exists!");
					}
				}
				else {
					sendMessage(sender, ChatColor.RED + "You must be a player to create PaintWar arenas!");
				}
			}
			else {
				sendMessage(sender, ChatColor.RED + "Incorrect arguments! Usage: /pw create <name>");
			}
		}
		else if (args[0].equalsIgnoreCase("start")) {
			if (args.length == 2) {
				if (paintwar.games.containsKey(args[1])) {
					PaintWarGame game = paintwar.games.get(args[1]);
					if (game.Start()) {
						sendMessage(sender, ChatColor.GREEN + "PaintWar game with name " + args[1] + " has been started! Join using /pw join " + args[1] + ".");
					}
					else {
						sendMessage(sender, ChatColor.RED + "PaintWar game with name " + args[1] + " has already been started! Join using /pw join " + args[1] + ".");
					}
				}
				else {
					sendMessage(sender, ChatColor.RED + "No PaintWar game with name " + args[1] + " exists!");
				}
			}
			else {
				sendMessage(sender, ChatColor.RED + "Incorrect arguments! Usage: /pw start <name>");
			}
		}
		//Join a PaintWar game (/pwjoin)
		else if (args[0].equalsIgnoreCase("join")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				
				if (args.length == 2) {
					if (paintwar.games.containsKey(args[1])) {
						PaintWarGame game = paintwar.games.get(args[1]);
						if (game.Join(player)) {
							sendMessage(sender, ChatColor.GREEN + "PaintWar game with name " + game.GetName() + " joined! You are on the " + game.GetTeam(player) + " team.");
						}
						else {
							sendMessage(sender, ChatColor.RED + "You are already in a PaintWar game!");
						}
					}
					else {
						sendMessage(sender, ChatColor.RED + "No PaintWar game with name " + args[1] + " exists!");
					}
				}
				else {
					sendMessage(sender, ChatColor.RED + "Incorrect arguments! Usage: /pw join <name>");
				}
			}
			else {
				sendMessage(sender, ChatColor.RED + "Only players can join PaintWar games.");
			}
		}
		//Stop a PaintWar game (/pw stop)
		else if (args[0].equalsIgnoreCase("stop")) {
			if (args.length == 2) {
				if (paintwar.games.containsKey(args[1])) {
					PaintWarGame game = paintwar.games.get(args[1]);
					
					if (game.Stop()) {
						sendMessage(sender, ChatColor.GREEN + "PaintWar game with name " + game.GetName() + " stopped!");
					}
					else {
						sendMessage(sender, ChatColor.RED + "PaintWar game with name " + game.GetName() + " is not running!");
					}
				}
				else {
					sendMessage(sender, ChatColor.RED + "No PaintWar game with name " + args[1] + " exists!");
				}
			}
			else {
				sendMessage(sender, ChatColor.RED + "Incorrect arguments! Usage: /pw stop <name>");
			}
		}
		//List players in a PaintWar game (/pw list)
		else if (args[0].equalsIgnoreCase("list")) {
			if (args.length == 2) {
				if (paintwar.games.containsKey(args[1])) {
					PaintWarGame game = paintwar.games.get(args[1]);
					Map<String, Player> players = game.GetPlayers();
					String message = ChatColor.GREEN + "Players currently playing PaintWar in arena " + game.GetName() + ":\n";
					
					String redList = "";
					String blueList = "";
					
					for (Entry<String, Player> e : players.entrySet()) {
						Player player = (Player) e.getValue();
						String res = game.GetTeam(player);
						
						if (res.equals("red")) {
							redList += player.getDisplayName().toString() + " ";
						}
						else if (res.equals("blue")) {
							blueList += player.getDisplayName().toString() + " ";
						}
					}
					
					message += ChatColor.RED + "  Red Team:\n    " + redList + "\n" + ChatColor.BLUE + "  Blue Team:\n    " + blueList;
					
					sendMessage(sender, message);
				}
				else {
					sendMessage(sender, ChatColor.RED + "No PaintWar game with name " + args[1] + " exists!");
				}
			}
			else {
				String message = ChatColor.GREEN + "PaintWar games:\n";
				
				int i = 0;
				int size = paintwar.games.size();
				
				for (Entry<String, PaintWarGame> e : paintwar.games.entrySet()) {
					i++;
					
					if (i == size) {
						message += e.getKey();
					}
					else {
						message += e.getKey() + ", ";
					}
				}
				
				sendMessage(sender, message);
			}
		}
		//Leave a PaintWar game (/pw leave)
		else if (args[0].equalsIgnoreCase("leave")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				
				if (args.length == 2) {
					if (paintwar.games.containsKey(args[1])) {
						PaintWarGame game = paintwar.games.get(args[1]);
						
						if (game.Leave(player)) {
							sendMessage(sender, ChatColor.GREEN + "You have left PaintWar game with name " + game.GetName() + ".");
						}
						else {
							sendMessage(sender, ChatColor.RED + "You are not in PaintWar game with name " + game.GetName() + "!");
						}
					}
					else {
						sendMessage(sender, ChatColor.RED + "No PaintWar game with name " + args[1] + " exists!");
					}
				}
				else {
					sendMessage(sender, ChatColor.RED + "Incorrect arguments! Usage: /pw leave <name>");
				}
			}
			else {
				sendMessage(sender, ChatColor.RED + "You must be a player to leave PaintWar games!");
			}
		}
		//Check a PaintWar game status (/pw status)
		else if (args[0].equalsIgnoreCase("status")) {
			if (args.length == 2) {
				if (paintwar.games.containsKey(args[1])) {
					PaintWarGame game = paintwar.games.get(args[1]);
					
					if (game.IsGameRunning()) {
						sendMessage(sender, ChatColor.GREEN + "PaintWar game with name " + game.GetName() + " is running!");
					}
					else {
						sendMessage(sender, ChatColor.RED + "PaintWar game with name " + game.GetName() + " is not running!");
					}
				}
				else {
					sendMessage(sender, ChatColor.RED + "No PaintWar game with name " + args[1] + " exists!");
				}
			}
			else {
				sendMessage(sender, ChatColor.RED + "Incorrect arguments! Usage: /pw status <name>");
			}
		}
		else if (args[0].equalsIgnoreCase("delete")) {
			if (args.length == 2) {
				if (paintwar.games.containsKey(args[1])) {
					PaintWarGame game = paintwar.games.get(args[1]);
					
					game.Remove();
					
					sendMessage(sender, ChatColor.GREEN + "PaintWar game with name " + args[1] + " deleted!");
				}
				else {
					sendMessage(sender, ChatColor.RED + "No PaintWar game with name " + args[1] + " exists!");
				}
			}
			else {
				sendMessage(sender, ChatColor.RED + "Incorrect arguments! Usage: /pw delete <name>");
			}
		}
		return true;
	}
}
