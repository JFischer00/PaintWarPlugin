package io.github.jfischer00.paintwarplugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class PaintWarGame {
	// Stuff for timer
	private int time;
	private int taskID;
	
	// Set the timer to an amount of time
	private void setTimer(int amount) {
		time = amount;
	}
	
	// Get the amount of time left
	public int GetTimeLeft() {
		return time;
	}
	
	// Start the time
	private void startTimer() {
		// Make a new task
		taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(paintwar, new Runnable() {
			@Override
			public void run() {
				// Stop at 0
				if (time == 0) {
					Stop();
					return;
				}
				
				// Set players' XP bars to timer
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (IsInGame(p)) {
						p.setLevel(time);
					}
				}
				
				// Decrement the time
				time--;
			}
		}, 0L, 20L);
	}
	
	// Stop the timer
	private void stopTimer() {
		Bukkit.getScheduler().cancelTask(taskID);
	}
	
	// Name, players, world, location
	private String name;
	private Map<String, Player> players;
	private World world;
	private Location min;
	private Location max;
	
	// Scores
	private int redScore;
	private int blueScore;
	
	// is game started, red players, and blue players
	private boolean gameStarted;
	private int redCount;
	private int blueCount;
	
	// PaintWarPlugin reference for this class
	private PaintWarPlugin paintwar;
	
	// Add a player
	private void addPlayer(Player player) {
		// Put them on a team
		setTeam(player);
		
		// Clear their inventory and give them the "gun"
		player.getInventory().clear();
		player.getItemInHand().setType(Material.IRON_BARDING);
		
		System.out.println("Blue: " + blueCount + "\nRed: " + redCount);
	}
	
	// Which world is it in?
	public World GetWorld() {
		return world;
	}
	
	// What's the minimum location (as a Vector)?
	public Vector GetMinLocation() {
		return new Vector(min.getX(), min.getY(), min.getZ());
	}
	
	// What's the minimum location (as a Location)?
	public Location GetMinLocationLoc() {
		return min;
	}
	
	// What's the maximum location (as a Vector)?
	public Vector GetMaxLocation() {
		return new Vector(max.getX(), max.getY(), max.getZ());
	}
	
	// What's the maximum location (as a Location)?
	public Location GetMaxLocationLoc() {
		return max;
	}
	
	// Is the game running?
	public boolean IsGameRunning() {
		return gameStarted;
	}
	
	// What's this game's name?
	public String GetName() {
		return name;
	}
	
	// Who's playing in this game?
	public HashMap<String, Player> GetPlayers() {
		return (HashMap<String, Player>) players;
	}
	
	// Constructor
	public PaintWarGame(PaintWarPlugin paintwar, String name, World world, Vector minLocation, Vector maxLocation) {
		// Set everything
		this.paintwar = paintwar;
		this.name = name;
		this.world = world;
		this.min = new Location(world, minLocation.getX(), minLocation.getY(), minLocation.getZ());
		this.max = new Location(world, maxLocation.getX(), maxLocation.getY(), maxLocation.getZ());
		players = new HashMap<String, Player>();
		
		// Reset
		reset();
	}
	
	// Reset the game
	private void reset() {
		players.clear();
		gameStarted = false;
		redCount = 0;
		blueCount = 0;
		time = 0;
		redScore = 0;
		blueScore = 0;
	}

	// Start it
	public boolean Start() {
		// Is it running?
		if (!gameStarted) {
			//Start everything
			gameStarted = true;
			setTimer(300);
			startTimer();
			
			return true;
		}
		else {
			// Nope
			return false;
		}
	}
	
	private void givePlayerMoney(Player player, double amount) {
		paintwar.economy.depositPlayer(player, amount);
	}
	
	// Stop it
	public boolean Stop() {
		// Is it running?
		if (gameStarted) {
			String winner = "";
			
			// Determine winner
			if (redScore > blueScore) {
				winner = "red";
			}
			else if (blueScore > redScore) {
				winner = "blue";
			}
			else {
				winner = "tie";
			}
			
			// Do win/lose and get rid of players
			for (Entry<String, Player> e : players.entrySet()) {
				Player player = e.getValue();
				
				// Send message based on win, tie, or loss
				if (GetTeam(player) == winner) {
					if (paintwar.useVault) {
						player.sendMessage(ChatColor.GREEN + "Congratulations! Your team won! You get $50!");
						givePlayerMoney(player, 50.0);
					}
					else {
						player.sendMessage(ChatColor.GREEN + "Congratulations! Your team won!");
					}
				}
				else if (winner == "tie") {
					player.sendMessage(ChatColor.YELLOW + "Well done. Your team tied.");
				}
				else {
					player.sendMessage(ChatColor.RED + "So sorry. Your team lost.");
				}
				
				Leave(player);
			}
			
			// Reset everything
			reset();
			stopTimer();
			
			return true;
		}
		else {
			// Nope
			return false;
		}
	}

	// Add team scores
	@SuppressWarnings("deprecation")
	public boolean AddScore(String team, Location loc) {
		// If the block is stained clay
		if (loc.getBlock().getType() == Material.STAINED_CLAY) {
			// If the team and block are red
			if (team == "red" && loc.getBlock().getData() == 14) {
				// Add red team score
				redScore++;
			}
			// If the team and block are blue
			else if (team == "blue" && loc.getBlock().getData() == 11) {
				// Add blue team score
				blueScore++;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
		return true;
	}
	
	// Join the game
	public boolean Join(Player player) {
		// Are they in a game already?
		if (!player.hasMetadata("team")) {
			// Put them on a team and get them in the game
			addPlayer(player);
			
			player.teleport(new Location(this.GetWorld(), ((this.GetMaxLocationLoc().getX() + this.GetMinLocationLoc().getX()) / 2), ((this.GetMaxLocationLoc().getY() + this.GetMinLocationLoc().getY()) / 2), ((this.GetMaxLocationLoc().getZ() + this.GetMinLocationLoc().getZ()) / 2)));
			
			return true;
		}
		else {
			// Nope
			return false;
		}
	}
	
	// Which team?
	private void setTeam(Player player) {
		// More reds, blues, or even?
		if (redCount > blueCount) {
			// Put them on blue
			player.setMetadata("team", new FixedMetadataValue(paintwar, "blue"));
			players.put(player.getName(), player);
			blueCount++;
		}
		else if (blueCount > redCount) {
			// Put them on red
			player.setMetadata("team", new FixedMetadataValue(paintwar, "red"));
			players.put(player.getName(), player);
			redCount++;
		}
		else {
			// Pick a random team
			Random rand = new Random();
			
			// Which team?
			if (rand.nextInt(2) == 0) {
				// Red
				player.setMetadata("team", new FixedMetadataValue(paintwar, "red"));
				players.put(player.getName(), player);
				redCount++;
			}
			else {
				// Blue
				player.setMetadata("team", new FixedMetadataValue(paintwar, "blue"));
				players.put(player.getName(), player);
				blueCount++;
			}
		}
	}

	// Which team?
	public String GetTeam(Player player) {
		// Are they in this game?
		if (IsInGame(player)) {
			// Are they on blue, red, or error?
			if (player.getMetadata("team").get(0).asString().equals("blue")) {
				return "blue";
			}
			else if (player.getMetadata("team").get(0).asString().equals("red")) {
				return "red";
			}
			else {
				return "error1";
			}
		}
		else {
			return "error2";
		}
	}
	
	// Delete this game
	public void Remove() {
		// Stop
		Stop();
		
		// Goodbye!
		paintwar.games.remove(name);
		paintwar.getConfig().set(name, null);
	}
	
	// Leave the game
	public boolean Leave(Player player) {
		// Are they in this game?
		if (IsInGame(player)) {
			// Are they red or blue?
			if (GetTeam(player).equals("blue")) {
				blueCount--;
			}
			else if (GetTeam(player).equals("red")) {
				redCount--;
			}
			
			// Get them out of here!
			restoreInventory(player);
			player.removeMetadata("team", paintwar);
			players.remove(player.getName());
			
			return true;
		}
		else {
			return false;
		}
	}
	
	// Is player in this game?
	public boolean IsInGame(Player player) {
		return players.containsKey(player.getName());
	}
	
	// Give players back their inventory
	private void restoreInventory(Player player) {
		player.getInventory().setContents(players.get(player.getName()).getInventory().getContents());
	}
}