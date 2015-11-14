package com.fawkes.plugin.collectables;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class MenuListener implements Listener {

	/* intense menu code */

	// INVENTORY CLICK EVENTS
	// check if back button is hit; if so go to main menu
	// cancel taking awards from inventory
	// check for MAIN MENU CLICKS to open different categories

	// ON INVENTORY CLOSE EVENT
	// run delete on the user's open menus (if they have one open)

	@EventHandler
	public void onClick(InventoryClickEvent e) {

		if (e.getWhoClicked() instanceof Player == false) {
			return;

		}

		// If player doesnt have menu open
		if (!MenuFactory.hasMenuOpen(e.getWhoClicked().getUniqueId())) {
			return;

		}

		ItemStack itemClicked = e.getCurrentItem();
		Player player = (Player) e.getWhoClicked();

		/*
		 * No idea how you want me to check if MAIN menu is open
		 * 
		 * if (menu that is open == MAIN menu) {
		 * 
		 * Do similar as I did below with back button to check what item was
		 * pressed And then use changeMenu(category, player), getting category
		 * from the clicked item's name
		 * 
		 * }
		 */

		if (itemClicked == null || itemClicked.getType().equals(Material.AIR)) {
			return;

		}

		if (e.getInventory().getName().equals("main menu")) {

			changeMenu(Category.getCategory(itemClicked.getType()), player);

			e.setCancelled(true);

			return;

		}

		// after this, we know the player is in a submenu.

		// Check if back button is pressed to go back to main menu
		// "back" isnt really going to change
		if (itemClicked.getItemMeta().getDisplayName().equals(MenuFactory.BACK)) {
			changeMenu(Category.MAIN, player);

		}

		// To make sure player doesnt take item from the showcase
		e.setCancelled(true);

	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {

		if (e.getPlayer() instanceof Player == false) {
			return;
		}

		if (!MenuFactory.hasMenuOpen(e.getPlayer().getUniqueId())) {
			return;
		}

		MenuFactory.removeOpenMenu(e.getPlayer().getUniqueId());
	}

	// TODO: elminate any static plugin things

	public void changeMenu(Category newCategory, Player player) {

		// Because of safety closing / opening inventories need to be run 1 tick
		// later
		// More info:
		// https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/inventory/InventoryClickEvent.html

		Bukkit.getScheduler().scheduleSyncDelayedTask(CollectablesPlugin.getPlugin(), new Runnable() {
			@Override
			public void run() {

				player.closeInventory();

				// Not sure if InventoryCloseEvent will trigger and remove the
				// open menu before the code below happens, hope it does

				Menu menu = new Menu(player.getName(),
						CollectablesPlugin.getPlugin().getAwards(player.getUniqueId(), player));

				if (newCategory.equals(Category.MAIN)) {
					player.openInventory(menu.getMain());

				} else {
					player.openInventory(menu.getCategory(newCategory));

				}

				MenuFactory.registerOpenMenu(player.getUniqueId(), menu);

			}
		}, 1L);

	}

}
