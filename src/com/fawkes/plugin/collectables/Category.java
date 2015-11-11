package com.fawkes.plugin.collectables;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

public enum Category {

	MAIN("Main Menu"), HOLIDAY("holiday"), MONTHLY("monthly"), MISC("miscellaneous"), ACHIEVEMENT(
			"achievement"), EXCLUSIVE("exclusive");

	String string;

	Category(String string) {
		this.string = string;
	}

	public String getString() {
		return string;
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

	public static Material getMaterial(Category category) {

		switch (category) {

		case HOLIDAY:
			return Material.SAPLING;
		case MONTHLY:
			return Material.PAPER;
		case MISC:
			return Material.SLIME_BALL;
		case ACHIEVEMENT:
			return Material.STICK;
		case EXCLUSIVE:
			return Material.GOLD_INGOT;

		default:
			return Material.BAKED_POTATO;

		}

	}

	public static List<Category> getCategories() {
		return Arrays.asList(HOLIDAY, MONTHLY, MISC, ACHIEVEMENT, EXCLUSIVE);

	}

}
