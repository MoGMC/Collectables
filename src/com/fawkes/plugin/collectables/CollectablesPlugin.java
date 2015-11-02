package com.fawkes.plugin.collectables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class CollectablesPlugin extends JavaPlugin {
	// only allow player that was given key/crate to use it

	private static FileConfiguration config;
	private static final int showcaseSize = 18;

	// private final File awards = new File("awards.yml");
	private static YamlConfiguration awards = null;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		config = getConfig();

		try {

			URL website = new URL(config.getString("awardfile"));

			ReadableByteChannel rbc = Channels.newChannel(website.openStream());

			File file = new File("awards.yml");

			@SuppressWarnings("resource")
			FileOutputStream fos = new FileOutputStream(file);

			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

			awards = YamlConfiguration.loadConfiguration(file);

		} catch (IOException e) {
			Bukkit.getLogger().severe("Failed to fetch awards.yml, disabling.");
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;

		}

		// register this as an api for bukkit
		this.getServer().getServicesManager().register(CollectablesPlugin.class, this, this, ServicePriority.Normal);

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		String cmd = command.getName();

		if (cmd.equalsIgnoreCase("showcase")) {
			// display showcase

			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players can use this command.");
				return false;

			}

			Player player = (Player) sender;

			// looking at other people's things
			if (args.length != 0) {

				Player splayer = (Player) Bukkit.getOfflinePlayer(args[0]);

				if (splayer == null) {
					sender.sendMessage(ChatColor.DARK_RED + "Could not find player \"" + args[0] + "\"");
					return true;

				}

				player.openInventory(getShowcase(splayer));

				return true;

			}

			player.openInventory(getShowcase(player));

			return true;

		}

		return false;

	}

	public static boolean hasAward(UUID uuid, String awardID) {
		return doesExist("player." + uuid + ".awards." + awardID);

	}

	// assuming you did all the checks before
	public void setLevel(UUID uuid, String awardID, String level) {
		config.set("player." + uuid + ".awards." + awardID + ".level", level);

		this.saveConfig();

	}

	// assuming you did all the checks before
	public void giveAward(UUID uuid, String awardID, String baseLevel) {
		String root = "player." + uuid + ".awards." + awardID;
		config.set(root + ".date", System.currentTimeMillis());
		config.set(root + ".level", baseLevel);

		this.saveConfig();

	}

	public static boolean awardExists(String awardID) {
		return config.contains("award." + awardID);

	}

	public static boolean doesExist(String path) {
		return config.contains(path);

	}

	public Inventory getShowcase(Player player) {

		Inventory showcase = Bukkit.createInventory(null, showcaseSize, player.getDisplayName() + "'s showcase");

		// if player doesn't have any awards, return empty inventory.
		if (!doesExist("player." + player.getUniqueId())) {
			return showcase;

		}

		String rootPath = "player." + player.getUniqueId() + ".awards";

		// get all awards player has
		Set<String> awardNames = config.getConfigurationSection(rootPath).getKeys(false);

		rootPath += ".";

		// loop thru all awards and set em up
		for (String awardName : awardNames) {

			String rootAward = "award." + awardName;

			// create the actual award to display TODO: maybe add level
			// indicator as quantity in stack?
			ItemStack award = new ItemStack(Material.getMaterial(awards.getString(rootAward + ".display")), 1);

			// get item meta to modify
			ItemMeta meta = award.getItemMeta();

			// set name to award's name in config
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', awards.getString(rootAward + ".name")));

			// get item description
			List<String> description = awards.getStringList(rootAward + ".description");

			// add color codes to all the lines of the description
			for (int line = 0; line < description.size(); line++) {
				description.set(line, ChatColor.translateAlternateColorCodes('&', description.get(line)));

			}

			// add "level X item" branding
			description.add(0, "");
			description.add(0, ChatColor.GRAY + "Level " + config.getString(rootPath + awardName + ".level") + " "
					+ awards.getString(rootAward + ".type"));

			// add "granted to PLAYER on DATE"
			description.add("");
			description.add(ChatColor.GRAY + "Granted to " + player.getName() + " on "
					+ new Date(config.getLong(rootPath + awardName + ".date")).toString());

			// set lore to description in config
			meta.setLore(description);

			award.setItemMeta(meta);

			showcase.addItem(award);

		}

		return showcase;

	}

}
