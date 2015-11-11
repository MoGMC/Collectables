package com.fawkes.plugin.collectables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MenuFactory {

	public static Inventory createMainMenu(List<QueryAward> awards) {

		Inventory inv = Bukkit.createInventory(null, 27, "main menu");

		// count the awards
		List<Category> categories = AwardFactory.getCategories(awards);

		int freq = 0;
		int counter = 0;

		for (Category c : Category.getCategories()) {

			freq = Collections.frequency(categories, c);

			if (freq == 0) {
				inv.setItem(11 + counter, new ItemStack(Category.getMaterial(c), 1));
				counter += 1;
				continue;

			}

			inv.setItem(11 + counter, new ItemStack(Category.getMaterial(c), freq));
			counter += 1;

		}

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

		return createItemInventory(playerName + "'s " + category.toString() + " awards", playerName, desiredAwards);

	}

	public static Inventory createItemInventory(String title, String playerName, List<QueryAward> awards) {

		Inventory inv = Bukkit.createInventory(null, 27, title);

		for (QueryAward a : awards) {
			inv.addItem(AwardFactory.getFormattedAward(a, playerName));

		}

		inv.setItem(26, backButton());

		return inv;

	}

	// TODO: should we really do this every time?
	public static ItemStack backButton() {

		ItemStack button = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());

		ItemMeta meta = button.getItemMeta();

		meta.setDisplayName("Back");

		meta.setLore(Arrays.asList(ChatColor.RED + "Click to go back"));

		button.setItemMeta(meta);

		return button;

	}

}
