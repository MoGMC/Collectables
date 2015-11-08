package com.fawkes.plugin.collectables;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

	private Database db;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		config = getConfig();

		/* Load award yaml */
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

		// Connect to MySQL server
		try {
			db = new Database();

		} catch (SQLException e) {
			Bukkit.getLogger().severe("Failed to connect to MySQL database, disabling.");
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

	public Inventory getShowcase(Player player) {

		Inventory showcase = Bukkit.createInventory(null, showcaseSize, player.getDisplayName() + "'s showcase");

		// query database
		ArrayList<Award> awardslist;
		try {
			awardslist = db.queryShowcase(player.getUniqueId());

		} catch (SQLException e) {
			player.sendMessage(ChatColor.DARK_RED
					+ "Database error while running getShowcase. Please report on the forums along with the following number: "
					+ System.currentTimeMillis());
			e.printStackTrace();

			return showcase;

		}

		// if player doesn't have any awards, return empty inventory.
		if (awardslist.isEmpty()) {
			return showcase;
		}

		// loop thru all awards and set em up
		for (Award a : awardslist) {

			String rootAward = "award." + a.getId();

			Bukkit.getLogger().info(rootAward);

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
			description.add(0, ChatColor.GRAY + "Level " + a.getLevel() + " " + awards.getString(rootAward + ".type"));

			// add "granted to PLAYER on DATE"
			description.add("");
			description.add(ChatColor.GRAY + "Granted to " + player.getName() + " on " + new Date(a.date).toString());

			// set lore to description in config
			meta.setLore(description);

			award.setItemMeta(meta);

			showcase.addItem(award);

		}

		return showcase;

	}

	/* API methods */

	public boolean awardExists(String awardID) {
		return config.contains("award." + awardID);

	}

	public boolean doesExist(String path) {
		return config.contains(path);

	}

	// might throw an error
	public boolean hasAward(UUID uuid, short awardId) {
		return db.doesExist(uuid, awardId);

	}

	// assuming you did all the checks before
	public void setLevel(UUID uuid, short awardId, int level) throws SQLException {
		db.setLevel(uuid, awardId, level);

	}

	// assuming you did all the checks before
	public void giveAward(UUID uuid, short awardId, int baseLevel) throws SQLException {
		db.giveAward(uuid, awardId, System.currentTimeMillis(), baseLevel);

	}

}
