package com.fawkes.plugin.collectables;

import java.io.IOException;
import java.sql.ResultSet;
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
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

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

			String name = sender.getName();
			UUID uuid = ((Player) sender).getUniqueId();
			UUID origUuid = uuid;

			// looking at other people's things
			if (args.length != 0) {

				OfflinePlayer splayer = Bukkit.getOfflinePlayer(args[0]);

				if (splayer == null) {
					sender.sendMessage(ChatColor.DARK_RED + "Could not find player \"" + args[0] + "\"");
					return true;

				}

				name = splayer.getName();
				uuid = splayer.getUniqueId();

			}

			List<QueryAward> awardsList = null;

			awardsList = getAwards(uuid, sender);

			Menu menu = new Menu(name, awardsList);

			// register the new open menu under the player who is OPENING it.
			// (not the target!)

			MenuFactory.registerOpenMenu(origUuid, menu);

			((Player) sender).openInventory(menu.getMain());

			return true;

		}

		if (cmd.equals("giveaward")) {

			boolean wildcard = false;

			if (args.length != 3) {
				if (args.length != 4) {
					sender.sendMessage("/giveaward <player name> <award> <level> [<wildcard>]");
					return true;

				}

				// they might be sending awildcard!

				wildcard = true;

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

				if (wildcard) {
					giveAward(p.getUniqueId(),
							new QueryWildcardAward(args[1], System.currentTimeMillis(), level, args[3]));
					sender.sendMessage("Sent a wildcard award.");

				} else {
					giveAward(p.getUniqueId(), new QueryAward(args[1], System.currentTimeMillis(), level));
					sender.sendMessage("Sent a regular award.");

				}

			} catch (SQLException e) {
				sender.sendMessage("Could not award player. SQL error at time: " + System.currentTimeMillis());
				e.printStackTrace();

			}

			return true;

		}

		if (cmd.equals("removeaward")) {

			if (args.length != 3) {
				sender.sendMessage("/removeaward <player name> <award> <wildcard (true/false)>");
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

			boolean wildcard = Boolean.valueOf(args[2]);

			try {
				sender.sendMessage("Query returned: " + db.removeAward(p.getUniqueId(), args[1], wildcard));

			} catch (SQLException e) {
				sender.sendMessage(
						"Could not remove award from player. SQL error at time: " + System.currentTimeMillis());
				e.printStackTrace();

			}

			return true;

		}

		if (cmd.equals("listawards")) {

			StringBuilder s = new StringBuilder("List of all awards: ");

			for (String a : AwardFactory.getListOfAwards()) {
				s.append(a);
				s.append(" ");

			}

			sender.sendMessage(s.toString());

			return true;

		}

		return false;

	}

	public static CollectablesPlugin getPlugin() {
		return plugin;
	}

	/* static methods */

	public static List<String> getLore(String path) {

		List<String> lore = config.getStringList(path);

		// add color codes to all the lines of the lore
		for (int line = 0; line < lore.size(); line++) {
			lore.set(line, ChatColor.translateAlternateColorCodes('&', lore.get(line)));

		}

		return lore;

	}

	/* API methods */

	public boolean awardExists(String awardID) {
		return config.contains("award." + awardID);

	}

	public List<QueryAward> getAwards(UUID uuid, CommandSender sender) {
		List<QueryAward> awardsList = null;

		try {
			awardsList = db.queryShowcase(uuid);

		} catch (SQLException e) {
			sender.sendMessage(ChatColor.DARK_RED
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
	public void giveAward(UUID uuid, QueryAward a) throws SQLException {

		db.giveAward(uuid, a);

		// wowo alert!

		OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);

		// if they're online now, display now. if not, store it away.
		if (p.isOnline()) {
			sendAwardMessages(p.getPlayer(), AwardFactory.getName(a));

		} else {
			storeOfflineAward(uuid, a.getId());

		}

	}

	// assuming you did checks before?
	public void removeAward(UUID uuid, String awardid, boolean wildcard) throws SQLException {
		db.removeAward(uuid, awardid, wildcard);

	}

	/* same below about assuming you did checks beforehand. */

	public ResultSet getOfflineAwards(UUID uuid) throws SQLException {
		return db.getOfflineAwards(uuid);

	}

	public void storeOfflineAward(UUID uuid, String awardId) throws SQLException {
		db.storeOfflineAward(uuid, awardId);

	}

	public void clearOfflineAwards(UUID uuid) throws SQLException {
		db.clearOfflineAwards(uuid);

	}

	public void sendAwardMessages(Player p, String awardName) {

		StringBuilder s = new StringBuilder(ChatColor.GOLD.toString());

		s.append("You have the received the award: [");
		s.append(awardName);
		s.append(ChatColor.GOLD.toString());
		s.append("]! Type /showcase to view all of your awards.");

		p.sendMessage(s.toString());

		StringBuilder s1 = new StringBuilder(ChatColor.GREEN.toString());

		s1.append(p.getDisplayName());
		s1.append(" has just received the award: [");
		s1.append(awardName);
		s1.append(ChatColor.GREEN.toString());
		s1.append("]!");

		Bukkit.broadcastMessage(s1.toString());

	}

}
