package com.fawkes.plugin.collectables;

import java.util.List;

import org.bukkit.inventory.Inventory;

public class Menu {

	/*
	 * wrapper containing personal information for storage, should be destroyed
	 * on inventory exit event.
	 */

	// player name is name of the showcase's owner
	String playerName;

	List<QueryAward> awards;

	public Menu(String playerName, List<QueryAward> awards) {
		this.playerName = playerName;
		this.awards = awards;

	}

	public Inventory getMain() {
		return MenuFactory.createMainMenu(awards);

	}

	public Inventory getCategory(Category category) {
		return MenuFactory.createSortedInventory(category, playerName, awards);

	}

}
