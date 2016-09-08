package io.github.jfischer00.paintwarplugin;

import org.bukkit.inventory.ItemStack;

public class PlayerData {
	private String name;
	private ItemStack[] inventory;
	private int exp;
	
	public PlayerData(String name, ItemStack[] inventory, int exp) {
		this.name = name;
		this.inventory = inventory;
		this.exp = exp;
	}
	
	public String getName() {
		return name;
	}
	
	public ItemStack[] getInventory() {
		return inventory;
	}
	
	public int getExp() {
		return exp;
	}
}
