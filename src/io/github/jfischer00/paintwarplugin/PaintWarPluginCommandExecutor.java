package io.github.jfischer00.paintwarplugin;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.bukkit.selections.Selection;

public class PaintWarPluginCommandExecutor implements CommandExecutor {
	// Make a PaintWarPlugin class reference for this class
	PaintWarPlugin paintwar;

	public PaintWarPluginCommandExecutor(PaintWarPlugin plugin) {
		//Set it to a parameter in the constructor
		paintwar = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// Help command (/pw)
		if ((args.length == 0 && sender.hasPermission("paintwar.base")) || (args[0].equalsIgnoreCase("help") && sender.hasPermission("paintwar.base"))) {
			String message = "" + ChatColor.DARK_BLUE + ChatColor.BOLD + "PaintWar Commands and Usage:\n" + ChatColor.RESET + ChatColor.YELLOW +
							 "    create: Create a PaintWar game.\n" +
							 "    delete: Delete a PaintWar game.\n" +
							 "    start: Start a PaintWar game.\n" +
							 "    stop: Stop a PaintWar game.\n" +
							 "    join: Join a PaintWar game.\n" +
							 "    leave: Leave a PaintWar game.\n" +
							 "    list: List all players in a PaintWar game.\n" +
							 "    status: Show the status of a PaintWar game.";
			
			paintwar.sendMessage(sender, message);
		}
		// Create a PaintWar game (/pw create)
		else if (args[0].equalsIgnoreCase("create") && sender.hasPermission("paintwar.create")) {
			// Needs a name
			if (args.length == 2) {
				// Has to be a Player
				if (sender instanceof Player) {
					// Convert the sender to a Player
					Player p = (Player) sender;
					// Get WE selection
					Selection s = paintwar.getWorldEdit().getSelection(p);
					
					// Coordinate variables
					int minX, minY, minZ, maxX, maxY, maxZ;
					
					// Set coordinate variables
					minX = s.getMinimumPoint().getBlockX();
					minY = s.getMinimumPoint().getBlockY();
					minZ = s.getMinimumPoint().getBlockZ();
					maxX = s.getMaximumPoint().getBlockX();
					maxY = s.getMaximumPoint().getBlockY();
					maxZ = s.getMaximumPoint().getBlockZ();
					
					// Make and set the world
					World world = s.getWorld();
					
					// Has to be a unique name
					if (!paintwar.games.containsKey(args[1])) {
						// Make the game and add it to the list
						PaintWarGame game = new PaintWarGame(paintwar, args[1], world, new Vector(minX, minY, minZ), new Vector(maxX, maxY, maxZ));
						paintwar.games.put(game.GetName(), game);
						
						paintwar.sendMessage(sender, ChatColor.GREEN + "PaintWar game with name " + game.GetName() + " created! Start it with /pw start " + game.GetName());
					}
					else {
						paintwar.sendMessage(sender, ChatColor.RED + "A PaintWar game with name " + args[1] + " already exists!");
					}
				}
				else {
					paintwar.sendMessage(sender, ChatColor.RED + "You must be a player to create PaintWar arenas!");
				}
			}
			else {
				paintwar.sendMessage(sender, ChatColor.RED + "Incorrect arguments! Usage: /pw create <name>");
			}
		}
		// Start a PaintWar game (/pw start)
		else if (args[0].equalsIgnoreCase("start") && sender.hasPermission("paintwar.start")) {
			// Need a game name to start
			if (args.length == 2) {
				// Game needs to exist
				if (paintwar.games.containsKey(args[1])) {
					// Get the game from the name
					PaintWarGame game = paintwar.games.get(args[1]);
					// Try to start it
					if (game.Start()) {
						paintwar.sendMessage(sender, ChatColor.GREEN + "PaintWar game with name " + args[1] + " has been started! Join using /pw join " + args[1] + ".");
					}
					else {
						paintwar.sendMessage(sender, ChatColor.RED + "PaintWar game with name " + args[1] + " has already been started! Join using /pw join " + args[1] + ".");
					}
				}
				else {
					paintwar.sendMessage(sender, ChatColor.RED + "No PaintWar game with name " + args[1] + " exists!");
				}
			}
			else {
				paintwar.sendMessage(sender, ChatColor.RED + "Incorrect arguments! Usage: /pw start <name>");
			}
		}
		// Join a PaintWar game (/pw join)
		else if (args[0].equalsIgnoreCase("join") && sender.hasPermission("paintwar.join")) {
			
			// Only players can join PaintWar games
			if (sender instanceof Player) {
				
				// Get the player from the sender
				Player player = (Player) sender;
				
				// Needs a name to join a game
				if (args.length == 2) {
					
					// Game needs to exist
					if (paintwar.games.containsKey(args[1])) {
						
						// Get the game from the name
						PaintWarGame game = paintwar.games.get(args[1]);
						// Try to join the game
						if (game.Join(player)) {
							paintwar.sendMessage(sender, ChatColor.GREEN + "PaintWar game with name " + game.GetName() + " joined! You are on the " + game.GetTeam(player) + " team.");
						}
						else {
							paintwar.sendMessage(sender, ChatColor.RED + "You are already in a PaintWar game!");
						}
					}
					else {
						paintwar.sendMessage(sender, ChatColor.RED + "No PaintWar game with name " + args[1] + " exists!");
					}
				}
				else {
					paintwar.sendMessage(sender, ChatColor.RED + "Incorrect arguments! Usage: /pw join <name>");
				}
			}
			else {
				paintwar.sendMessage(sender, ChatColor.RED + "Only players can join PaintWar games.");
			}
		}
		// Stop a PaintWar game (/pw stop)
		else if (args[0].equalsIgnoreCase("stop") && sender.hasPermission("paintwar.stop")) {
			// Need a name to stop a game
			if (args.length == 2) {
				// Game needs to exist
				if (paintwar.games.containsKey(args[1])) {
					// Get the game from the name
					PaintWarGame game = paintwar.games.get(args[1]);
					// Try to stop the game
					if (game.Stop()) {
						paintwar.sendMessage(sender, ChatColor.GREEN + "PaintWar game with name " + game.GetName() + " stopped!");
					}
					else {
						paintwar.sendMessage(sender, ChatColor.RED + "PaintWar game with name " + game.GetName() + " is not running!");
					}
				}
				else {
					paintwar.sendMessage(sender, ChatColor.RED + "No PaintWar game with name " + args[1] + " exists!");
				}
			}
			else {
				paintwar.sendMessage(sender, ChatColor.RED + "Incorrect arguments! Usage: /pw stop <name>");
			}
		}
		// List players in a PaintWar game (/pw list)
		else if (args[0].equalsIgnoreCase("list") && sender.hasPermission("paintwar.list")) {
			// Need a name to list the players in a game
			if (args.length == 2) {
				// Game needs to exist
				if (paintwar.games.containsKey(args[1])) {
					// Get the-you know what, forget it
					PaintWarGame game = paintwar.games.get(args[1]);
					// Get the players from the game
					Map<String, Player> players = game.GetPlayers();
					// Start forming the message
					String message = ChatColor.GREEN + "Players currently playing PaintWar in arena " + game.GetName() + ":\n";
					
					// Team lists
					String redList = "";
					String blueList = "";
					
					// Loop through the players in the game
					for (Entry<String, Player> e : players.entrySet()) {
						// Make a Player
						Player player = (Player) e.getValue();
						// Get their team
						String res = game.GetTeam(player);
						
						// Put them on the right list
						if (res.equals("red")) {
							redList += player.getDisplayName().toString() + " ";
						}
						else if (res.equals("blue")) {
							blueList += player.getDisplayName().toString() + " ";
						}
					}
					
					// Add the team lists to the message
					message += ChatColor.RED + "  Red Team:\n    " + redList + "\n" + ChatColor.BLUE + "  Blue Team:\n    " + blueList;
					
					// Send the message
					paintwar.sendMessage(sender, message);
				}
				else {
					paintwar.sendMessage(sender, ChatColor.RED + "No PaintWar game with name " + args[1] + " exists!");
				}
			}
			// Otherwise list the games
			else {
				// Main message
				String message = ChatColor.GREEN + "PaintWar games:\n";
				
				// index
				int i = 0;
				// Amount of games
				int size = paintwar.games.size();
				
				// Loop through games
				for (Entry<String, PaintWarGame> e : paintwar.games.entrySet()) {
					// Increment index
					i++;
					
					// Do cool formatting based on whether we're on the last game or not
					if (i == size) {
						message += e.getKey();
					}
					else {
						message += e.getKey() + ", ";
					}
				}
				
				// Send the message
				paintwar.sendMessage(sender, message);
			}
		}
		// Leave a PaintWar game (/pw leave)
		else if (args[0].equalsIgnoreCase("leave") && sender.hasPermission("paintwar.leave")) {
			// Need to be a Player to leave a game
			if (sender instanceof Player) {
				// Get a Player from the sender
				Player player = (Player) sender;
				
				// Loop through games
				for (Entry<String, PaintWarGame> e : paintwar.games.entrySet()) {
					// Get the game
					PaintWarGame game = e.getValue();
					
					// Are they in this one?
					if (game.IsInGame(player)) {
						// Leave the game
						game.Leave(player);
						paintwar.sendMessage(sender, ChatColor.GREEN + "You have successfully left PaintWar game " + game.GetName() + "!");
						return true;
					}
				}
				
				paintwar.sendMessage(sender, ChatColor.RED + "You are not in a PaintWar game!");
			}
			else {
				paintwar.sendMessage(sender, ChatColor.RED + "You must be a player to leave PaintWar games!");
			}
		}
		// Check a PaintWar game status (/pw status)
		else if (args[0].equalsIgnoreCase("status") && sender.hasPermission("paintwar.status")) {
			// Need a name to check the status
			if (args.length == 2) {
				// Needs to exist
				if (paintwar.games.containsKey(args[1])) {
					// Get it
					PaintWarGame game = paintwar.games.get(args[1]);
					
					// Is it running?
					if (game.IsGameRunning()) {
						paintwar.sendMessage(sender, ChatColor.GREEN + "PaintWar game with name " + game.GetName() + " is running!\nTime Left: " + game.GetTimeLeft() + " seconds");
					}
					else {
						paintwar.sendMessage(sender, ChatColor.RED + "PaintWar game with name " + game.GetName() + " is not running!");
					}
				}
				else {
					paintwar.sendMessage(sender, ChatColor.RED + "No PaintWar game with name " + args[1] + " exists!");
				}
			}
			else {
				paintwar.sendMessage(sender, ChatColor.RED + "Incorrect arguments! Usage: /pw status <name>");
			}
		}
		// Delete a PaintWar game (/pw delete)
		else if (args[0].equalsIgnoreCase("delete") && sender.hasPermission("paintwar.delete")) {
			// Need a name to delete a game
			if (args.length == 2) {
				// Needs to exist
				if (paintwar.games.containsKey(args[1])) {
					// Get it
					PaintWarGame game = paintwar.games.get(args[1]);
					
					// Goodbye!
					game.Remove();
					
					paintwar.sendMessage(sender, ChatColor.GREEN + "PaintWar game with name " + args[1] + " deleted!");
				}
				else {
					paintwar.sendMessage(sender, ChatColor.RED + "No PaintWar game with name " + args[1] + " exists!");
				}
			}
			else {
				paintwar.sendMessage(sender, ChatColor.RED + "Incorrect arguments! Usage: /pw delete <name>");
			}
		}
		return true;
	}
}