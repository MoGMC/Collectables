package com.fawkes.plugin.collectables;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

public enum Category {

	MAIN("Main Menu", Material.POISONOUS_POTATO), HOLIDAY("Holiday", Material.SAPLING), MONTHLY("Monthly",
			Material.PAPER), MISC("Misc", Material.SLIME_BALL), ACHIEVEMENT("Achievement",
					Material.STICK), EXCLUSIVE("Exclusive", Material.GOLD_INGOT);

	String string;
	Material material;

	Category(String string, Material material) {
		this.string = string;
		this.material = material;

	}

	public String toString() {
		return string;

	}

	public Material getMaterial() {
		return material;

	}

	public static Category getCategory(String category) {

		switch (category.toLowerCase()) {

		case "holiday":
			return HOLIDAY;
		case "monthly":
			return MONTHLY;
		case "misc":
			return MISC;
		case "achievement":
			return ACHIEVEMENT;
		case "exclusive":
			return EXCLUSIVE;
		case "main":
			return MAIN;

		default:
			return null;

		}

	}

	public static Category getCategory(Material material) {

		switch (material) {

		case SAPLING:
			return HOLIDAY;
		case PAPER:
			return MONTHLY;
		case SLIME_BALL:
			return MISC;
		case STICK:
			return ACHIEVEMENT;
		case GOLD_INGOT:
			return EXCLUSIVE;

		default:
			return MAIN;

		}

	}

	public static List<Category> getCategories() {
		return Arrays.asList(HOLIDAY, MONTHLY, MISC, ACHIEVEMENT, EXCLUSIVE);

	}

}
