package com.fawkes.plugin.collectables;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Menu {
	
	/*
	 * When you want to showcase a player's awards
	 * 
	 * Menu showcase = new Menu(awards, Category category)
	 * And then player.openInventory(showcase.toInventory());
	 * 
	 * awards = all the awards players has, category is the category he should be viewing
	 * 
	 * Most things, especially the private methods, should automatically do their thing (see constructor)
	 */
	
	Inventory inventory;

	ArrayList<ItemStack> items;
	Category category;
	String title;
	
	public Menu(ArrayList<Award> awards, Category category) {
		
		this.category = category;
		this.title = category.toString();
		
		if (category != Category.MAIN) {
			convertAwardsToItems(awards);
		}
		
		else {
			createMainMenu(awards);
		}
		
	}
	
	private void createMainMenu(ArrayList<Award> awards) {
		
		HashMap<Category, Integer> categoryCount = new HashMap<Category, Integer>();
		
		for (Award award : awards) {
			
			if (categoryCount.get(award) == null) {
				categoryCount.put(award.getCategory(), 1);
			} 
			
			else {
				categoryCount.put(award.getCategory(), categoryCount.get(award) + 1);
			}
			
		}
		
		Inventory inv = toInventory();
		
		// TODO: Create main menu as picture here https://gyazo.com/97079ef3ffc351ee05f34f240ea0b45a
		// Maybe make the items configurable in config.yml
		// Use inv.setItem(int slot, ItemStack item) to set them
		// Amount should be amount of awards players has in that category, so 
		// 		createItemSimple(Material material, category.getString(), Arraylist<String> lore, categoryCount.get(category));
		
		this.inventory = inv;
		
	}

	private void convertAwardsToItems(ArrayList<Award> awardsToConvert) {
		
		if (awardsToConvert == null) {
			return;
		}
		
		for (Award award: awardsToConvert) {
			
			if (!award.getCategory().equals(category)) {
				continue;
			}
			
			ItemStack item = createItem(Material.getMaterial(award.getMaterial()), award.getName(), award.getDescription(), Long.toString(award.getDate()), false);		
			items.add(item);
			
		}
			
	}
	
	private ItemStack createItemSimple(Material material, String name, ArrayList<String> lore, int amount) {
		
		ItemStack item = new ItemStack(material, amount);	
		ItemMeta meta = item.getItemMeta();		
		
		meta.setDisplayName(name);	
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		return item;
	}
	
	private ItemStack createItem(Material material, String name, ArrayList<String> lore, String date, boolean backbutton) {
		
		ItemStack item = new ItemStack(material);
		
		if (backbutton == true) {
			item = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());
		}
		
		ItemMeta meta = item.getItemMeta();
	
		if (date != null) {
			lore.add("");
			lore.add(date);
		}
		
		meta.setDisplayName(name);	
		meta.setLore(lore);
		item.setItemMeta(meta);
		
		return item;
	}
	
	private ItemStack createBackButton() {
		ArrayList<String> backbuttonLore = new ArrayList<String>();
		backbuttonLore.add(ChatColor.RED + "Click to go back");
		ItemStack backbutton = createItem(Material.WOOL, "Back", backbuttonLore, null, true);
		
		return backbutton;
	}
	
	public Inventory toInventory() {
		
		if (this.inventory != null) {
			return this.inventory;
		}
		
		Inventory inv = Bukkit.createInventory(null, 27, title);
		
		for (ItemStack item : items) {
			inv.addItem(item);
		}
		
		
		inv.setItem(27, createBackButton());
		
		return inv;
	}

}
