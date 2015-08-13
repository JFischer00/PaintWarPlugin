package io.github.jfischer00.paintwarplugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class PaintWarGame {
	private String name;
	private Map<String, Player> players;
	private Vector minLocation = new Vector(0, 0, 0);
	private Vector maxLocation = new Vector(0, 0, 0);
	
	private boolean gameStarted;
	private int redCount;
	private int blueCount;
	
	private PaintWarPlugin paintwar;
	
	private void addPlayer(Player player) {
		setTeam(player);
		
		player.getInventory().clear();
		player.getItemInHand().setType(Material.IRON_BARDING);
	}
	
	public Vector GetMinLocation() {
		return minLocation;
	}
	
	public Vector GetMaxLocation() {
		return maxLocation;
	}
	
	public boolean IsGameRunning() {
		return gameStarted;
	}
	
	public String GetName() {
		return name;
	}
	
	public HashMap<String, Player> GetPlayers() {
		return (HashMap<String, Player>) players;
	}
	
	public PaintWarGame(PaintWarPlugin paintwar, String name, Vector minLocation, Vector maxLocation) {
		this.paintwar = paintwar;
		this.name = name;
		this.minLocation = minLocation;
		this.maxLocation = maxLocation;
		players = new HashMap<String, Player>();
		reset();
	}
	
	private void reset() {
		players.clear();
		gameStarted = false;
		redCount = 0;
		blueCount = 0;
	}

	public boolean Start() {
		if (!gameStarted) {
			gameStarted = true;
			
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean Stop() {
		if (gameStarted) {
			for (Entry<String, Player> e : players.entrySet()) {
				Leave(e.getValue());
			}
			
			reset();
			
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean Join(Player player) {
		if (!player.hasMetadata("team")) {
			setTeam(player);
			addPlayer(player);
			
			return true;
		}
		else {
			return false;
		}
	}
	
	private void setTeam(Player player) {
		if (redCount > blueCount) {
			player.setMetadata("team", new FixedMetadataValue(paintwar, "blue"));
			players.put(player.getName(), player);
			blueCount++;
		}
		else if (blueCount > redCount) {
			player.setMetadata("team", new FixedMetadataValue(paintwar, "red"));
			players.put(player.getName(), player);
			redCount++;
		}
		else {
			Random rand = new Random();
			
			if (rand.nextInt(2) == 0) {
				player.setMetadata("team", new FixedMetadataValue(paintwar, "red"));
				players.put(player.getName(), player);
				redCount++;
			}
			else {
				player.setMetadata("team", new FixedMetadataValue(paintwar, "blue"));
				players.put(player.getName(), player);
				blueCount++;
			}
		}
	}

	public String GetTeam(Player player) {
		if (IsInGame(player)) {
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
	
	public void Remove() {
		Stop();
		
		paintwar.games.remove(name);
		paintwar.getConfig().set(name, null);
	}
	
	public boolean Leave(Player player) {
		if (IsInGame(player)) {
			if (GetTeam(player).equals("blue")) {
				blueCount--;
			}
			else if (GetTeam(player).equals("red")) {
				redCount--;
			}
			
			restoreInventory(player);
			player.removeMetadata("team", paintwar);
			players.remove(player.getName());
			
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean IsInGame(Player player) {
		return players.containsKey(player.getName());
	}
	
	private void restoreInventory(Player player) {
		player.getInventory().setContents(players.get(player.getName()).getInventory().getContents());
	}
}
