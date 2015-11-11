package com.fawkes.plugin.collectables;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class CollectablesPlugin extends JavaPlugin {
	// only allow player that was given key/crate to use it

	private static FileConfiguration config;
	private static final int showcaseSize = 18;

	private Database db;
	AwardFactory af;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		config = getConfig();

		// Set up the little awards database
		try {
			af = new AwardFactory(config.getString("awardfile"));

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
		List<QueryAward> awardslist;

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
		for (QueryAward a : awardslist) {

			showcase.addItem(af.getFormattedAward(a, player.getName()));

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
	public boolean hasAward(UUID uuid, String awardId) {
		return db.doesExist(uuid, awardId);

	}

	// assuming you did all the checks before
	public void setLevel(UUID uuid, String awardId, int level) throws SQLException {
		db.setLevel(uuid, awardId, level);

	}

	// assuming you did all the checks before
	public void giveAward(UUID uuid, String awardId, int baseLevel) throws SQLException {
		db.giveAward(uuid, awardId, System.currentTimeMillis(), baseLevel);

	}

}
