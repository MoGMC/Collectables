package com.fawkes.plugin.collectables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuFactory {

	private static HashMap<UUID, Menu> openInventories = new HashMap<UUID, Menu>();

	public static String BACK = ChatColor.RESET + "Back";

	public static Inventory createMainMenu(List<QueryAward> awards) {

		Inventory inv = Bukkit.createInventory(null, 36, "Main Menu");

		// if awards are null, return empty inventory.
		if (awards.isEmpty()) {
			ItemStack nothing = new ItemStack(Material.POTATO, 1);

			ItemMeta nmeta = nothing.getItemMeta();
			nmeta.setDisplayName(ChatColor.RESET + "Nothing Here.");
			nothing.setItemMeta(nmeta);

			inv.setItem(13, nothing);

			return inv;

		}

		// count the awards
		List<Category> categories = AwardFactory.getCategories(awards);

		int freq = 0;
		int counter = 0;

		for (Category c : Category.getCategories()) {

			freq = Collections.frequency(categories, c);

			ItemStack button;

			if (freq == 0) {
				button = new ItemStack(c.getMaterial(), 1);

			} else {
				button = new ItemStack(c.getMaterial(), freq);

			}

			// set button name and misc
			ItemMeta meta = button.getItemMeta();

			meta.setDisplayName(ChatColor.RESET + c.toString() + " Awards");
			meta.setLore(CollectablesPlugin.getLore(c.toString()));

			button.setItemMeta(meta);

			// set its respective slot in the menu
			inv.setItem(11 + counter, button);

			counter += 1;

		}

		// add info paper
		ItemStack info = new ItemStack(Material.PAPER, 1);

		ItemMeta imeta = info.getItemMeta();

		imeta.setDisplayName(ChatColor.RESET + ChatColor.BOLD.toString() + "What are awards?");

		imeta.setLore(CollectablesPlugin.getLore("info"));

		info.setItemMeta(imeta);

		inv.setItem(27, info);

		return inv;

	}

	public static Inventory createSortedInventory(Category category, String playerName, List<QueryAward> awards) {

		// sort out all of the awards that are in the category we want
		List<QueryAward> desiredAwards = new ArrayList<QueryAward>();

		for (QueryAward a : awards) {
			// if the award is the category we want, add it to desired awards.
			if (AwardFactory.getCategory(a.getId()) == category) {
				desiredAwards.add(a);

			}
		}

		return createItemInventory(playerName + "'s " + category.toString() + " Awards", playerName, desiredAwards);

	}

	public static Inventory createItemInventory(String title, String playerName, List<QueryAward> awards) {

		Inventory inv = Bukkit.createInventory(null, 36, title);

		inv.setItem(35, backButton());

		// if awards are null, return empty inventory.
		if (awards.isEmpty()) {
			return inv;

		}

		for (QueryAward a : awards) {
			inv.addItem(AwardFactory.getFormattedAward(a, playerName));

		}

		return inv;

	}

	public static Inventory createShowcase(String title, String playerName, List<QueryAward> awards) {

		Inventory inv = Bukkit.createInventory(null, 18, title);

		for (QueryAward a : awards) {
			inv.addItem(AwardFactory.getFormattedAward(a, playerName));

		}

		return inv;

	}

	// TODO: should we really do this every time?
	public static ItemStack backButton() {

		ItemStack button = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());

		ItemMeta meta = button.getItemMeta();

		meta.setDisplayName(BACK);

		meta.setLore(Arrays.asList(ChatColor.RED + "Click to go back"));

		button.setItemMeta(meta);

		return button;

	}

	/*
	 * Cloud storage for open inventories or something
	 * 
	 */

	public static void registerOpenMenu(UUID uuid, Menu menu) {
		openInventories.put(uuid, menu);

	}

	public static Menu getOpenMenu(UUID uuid) {
		return openInventories.get(uuid);

	}

	public static void removeOpenMenu(UUID uuid) {
		openInventories.remove(uuid);

	}

	public static boolean hasMenuOpen(UUID uuid) {
		return openInventories.containsKey(uuid);

	}

}