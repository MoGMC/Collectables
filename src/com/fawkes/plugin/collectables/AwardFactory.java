package com.fawkes.plugin.collectables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AwardFactory {

	/*
	 * this class doesn't really even need to be instance-ized
	 */

	static SimpleDateFormat dateformat = new SimpleDateFormat("MMMM d, yyyy @ hh:mm:ss");

	// private final File awards = new File("awards.yml");
	private static YamlConfiguration awards = null;

	public static void refresh(String url) throws IOException {
		/* Load award yaml from url */

		URL website = new URL(url);

		ReadableByteChannel rbc = Channels.newChannel(website.openStream());

		File file = new File("awards.yml");

		FileOutputStream fos = new FileOutputStream(file);

		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

		fos.close();

		awards = YamlConfiguration.loadConfiguration(file);

		// set dateformat to be UTC

		dateformat.setTimeZone(TimeZone.getTimeZone("UTC"));

	}

	public static ItemStack getFormattedAward(QueryAward a, String name) {

		String rootAward = a.getId();

		// create the actual award to display TODO: maybe add level
		// indicator as quantity in stack?
		ItemStack award = new ItemStack(Material.getMaterial(awards.getString(rootAward + ".display")), 1);

		// get item meta to modify
		ItemMeta meta = award.getItemMeta();

		// set name to award's name in config
		meta.setDisplayName(getName(a));

		// get item description
		List<String> description = awards.getStringList(rootAward + ".description");

		// add color codes to all the lines of the description
		for (int line = 0; line < description.size(); line++) {
			description.set(line, ChatColor.translateAlternateColorCodes('&', description.get(line)));

		}

		// add "level X item" branding
		description.add(0, "");
		description.add(0, ChatColor.GRAY + "Level " + a.getLevel() + " " + awards.getString(rootAward + ".type"));

		// add "granted to PLAYER on DATE"
		description.add("");
		description.add(ChatColor.GRAY + "Granted to " + name + " on");
		description.add(ChatColor.GRAY + formatDate(a.getDate()));

		// set lore to description in config
		meta.setLore(description);

		award.setItemMeta(meta);

		return award;

	}

	public static Category getCategory(String awardId) {
		return Category.getCategory(awards.getString(awardId + ".category"));

	}

	public static String getName(QueryAward a) {

		if (a.hasMeta()) {
			return getColoredName(a.getId()).replace("{wildcard}", a.getMeta());

		}

		return getColoredName(a.getId());

	}

	private static String getColoredName(String awardId) {
		return ChatColor.translateAlternateColorCodes('&', awards.getString(awardId + ".name"));

	}

	public static boolean exists(String path) {
		return awards.contains(path);

	}

	public static Set<String> getListOfAwards() {
		return awards.getKeys(false);
	}

	public static String formatDate(long date) {
		return dateformat.format(date);

	}

	public static List<Category> getCategories(List<QueryAward> awards) {
		ArrayList<Category> categories = new ArrayList<Category>();

		for (QueryAward a : awards) {
			categories.add(getCategory(a.getId()));

		}

		return categories;

	}

}
