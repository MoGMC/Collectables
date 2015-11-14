package com.fawkes.plugin.collectables;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class CollectablesPlugin extends JavaPlugin {
	// only allow player that was given key/crate to use it

	private static FileConfiguration config;
	private static CollectablesPlugin plugin;

	private Database db;
	AwardFactory af;

	@Override
	public void onEnable() {
		this.plugin = this;

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

		// register Listeners
		Bukkit.getPluginManager().registerEvents(new MenuListener(), this);

		// register this as an api for bukkit
		this.getServer().getServicesManager().register(CollectablesPlugin.class, this, this, ServicePriority.Normal);

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		String cmd = command.getName().toLowerCase();

		if (cmd.equalsIgnoreCase("showcase")) {
			// display showcase

			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players can use this command.");
				return false;

			}

			Player player = (Player) sender;

			Player target = player;

			// looking at other people's things
			if (args.length != 0) {

				Player splayer = (Player) Bukkit.getOfflinePlayer(args[0]);

				if (splayer == null) {
					sender.sendMessage(ChatColor.DARK_RED + "Could not find player \"" + args[0] + "\"");
					return true;
				}

				target = splayer;

			}

			List<QueryAward> awardsList = null;

			awardsList = getAwards(target.getUniqueId(), player);

			Menu menu = new Menu(target.getName(), awardsList);

			// register the new open menu under the player who is OPENING it.
			// (not the target!)

			MenuFactory.registerOpenMenu(player.getUniqueId(), menu);

			player.openInventory(menu.getMain());

			return true;

		}

		if (cmd.equals("giveaward")) {

			if (args.length != 3) {
				sender.sendMessage("/giveaward <player name> <award> <level>");
				return true;

			}

			if (!AwardFactory.exists(args[1])) {
				sender.sendMessage("No such award: " + args[1]);
				return true;

			}

			int level;

			try {
				level = Integer.valueOf(args[2]);

			} catch (NumberFormatException e) {
				sender.sendMessage("No such number: " + args[2]);
				return true;

			}

			OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);

			if (p == null) {
				sender.sendMessage("No such player: " + args[0]);
				return true;

			}

			try {
				giveAward(p.getUniqueId(), args[1], level);
				sender.sendMessage("Gave.");

			} catch (SQLException e) {
				sender.sendMessage("Could not award player. SQL error at time: " + System.currentTimeMillis());
				e.printStackTrace();

			}

			return true;

		}

		if (cmd.equals("removeaward")) {

			if (args.length != 2) {
				sender.sendMessage("/removeaward <player name> <award>");
				return true;

			}

			if (!AwardFactory.exists(args[1])) {
				sender.sendMessage("No such award: " + args[1]);
				return true;

			}

			OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);

			if (p == null) {
				sender.sendMessage("No such player: " + args[0]);
				return true;

			}

			try {
				sender.sendMessage("Query returned: " + db.removeAward(p.getUniqueId(), args[1]));

			} catch (SQLException e) {
				sender.sendMessage(
						"Could not remove award from player. SQL error at time: " + System.currentTimeMillis());
				e.printStackTrace();

			}

			return true;

		}

		return false;

	}

	public static CollectablesPlugin getPlugin() {
		return plugin;
	}

	/* static methods */

	/* API methods */

	public boolean awardExists(String awardID) {
		return config.contains("award." + awardID);

	}

	public List<QueryAward> getAwards(UUID uuid, Player asker) {
		List<QueryAward> awardsList = null;

		try {
			awardsList = db.queryShowcase(uuid);

		} catch (SQLException e) {
			asker.sendMessage(ChatColor.DARK_RED
					+ "Database error while running getShowcase. Please report on the forums along with the following number: "
					+ System.currentTimeMillis());
			e.printStackTrace();
		}

		return awardsList;

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

		// wowo alert!

		Player p = Bukkit.getPlayer(uuid);

		if (p == null) {
			return;

		}

		p.sendMessage(ChatColor.GOLD + " You have received the award: "
				+ ChatColor.translateAlternateColorCodes('&', AwardFactory.getName(awardId)) + ChatColor.GOLD
				+ ". Type /showcase to view your awards!");

	}

	// assuming you did checks before?
	public void removeAward(UUID uuid, String awardId) throws SQLException {
		db.removeAward(uuid, awardId);

	}

}
