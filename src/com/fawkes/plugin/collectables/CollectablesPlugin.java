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

import com.fawkes.plugin.collectables.command.GiveAwardCommand;
import com.fawkes.plugin.collectables.command.RemoveAward;
import com.fawkes.plugin.collectables.command.ShowcaseCommand;

public class CollectablesPlugin extends JavaPlugin {
	// only allow player that was given key/crate to use it

	private static FileConfiguration config;
	private static CollectablesPlugin plugin;

	private Database db;
	AwardFactory af;

	@Override
	public void onEnable() {
		plugin = this;

		saveDefaultConfig();
		config = getConfig();

		refreshAwards();

		refreshDatabase();

		// register Listeners
		Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

		// register commands
		getCommand("giveaward").setExecutor(new GiveAwardCommand());
		getCommand("showcase").setExecutor(new ShowcaseCommand());
		getCommand("removeaward").setExecutor(new RemoveAward());

		// register this as an api for bukkit
		this.getServer().getServicesManager().register(CollectablesPlugin.class, this, this, ServicePriority.Normal);

	}

	public void refreshAwards() {

		// Set up the little awards database
		try {
			AwardFactory.refresh(config.getString("awardfile"));

		} catch (IOException e) {

			Bukkit.getLogger().severe("Failed to fetch awards.yml, disabling.");
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;

		}

	}

	public void refreshDatabase() {

		// Connect to MySQL server
		try {
			db = new Database();

		} catch (SQLException e) {
			Bukkit.getLogger().severe("Failed to connect to MySQL database, disabling.");
			e.printStackTrace();
			Bukkit.getPluginManager().disablePlugin(this);
			return;

		}

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		String cmd = command.getName().toLowerCase();

		if (cmd.equals("removeaward")) {

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

		if (cmd.equals("refreshawards")) {
			refreshAwards();

			sender.sendMessage("Refreshed awards.");

			return true;

		}

		if (cmd.equals("refreshawardsdb")) {
			refreshDatabase();

			sender.sendMessage("Refreshsed awards database.");

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

	public int getAwardCount(UUID uuid) throws SQLException {
		return db.getAwardCount(uuid);

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
		giveAward(uuid, a, true, true);

	}

	public void giveAward(UUID uuid, QueryAward a, boolean callEvent, boolean showMessages) throws SQLException {

		// call custom event
		if (callEvent) {
			Bukkit.getPluginManager().callEvent(new AwardGiveEvent(uuid, a));

		}

		db.giveAward(uuid, a);

		// wowo alert!

		OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);

		// if they're online now, display now. if not, store it away.
		if (p.isOnline()) {

			if (showMessages) {
				sendAwardMessages(p.getPlayer(), AwardFactory.getName(a));

			} else {
				sendAwardMessagesToPlayer(p.getPlayer(), AwardFactory.getName(a));

			}

		} else {
			storeOfflineAward(uuid, a);

		}

	}

	// assuming you did checks before?
	public boolean removeAward(UUID uuid, String awardid) throws SQLException {
		return db.removeAward(uuid, awardid);

	}

	/* same below about assuming you did checks beforehand. */

	public ResultSet getOfflineAwards(UUID uuid) throws SQLException {
		return db.getOfflineAwards(uuid);

	}

	public void storeOfflineAward(UUID uuid, QueryAward a) throws SQLException {
		db.storeOfflineAward(uuid, a);

	}

	public void clearOfflineAwards(UUID uuid) throws SQLException {
		db.clearOfflineAwards(uuid);

	}

	public void sendAwardMessagesToPlayer(Player p, String awardName) {

		StringBuilder s = new StringBuilder(ChatColor.GOLD.toString());

		s.append("You have the received the award: [");
		s.append(awardName);
		s.append(ChatColor.GOLD.toString());
		s.append("]! Type /showcase to view all of your awards.");

		p.sendMessage(s.toString());

	}

	public void sendAwardMessagesToServer(Player p, String awardName) {

		StringBuilder s1 = new StringBuilder(ChatColor.GREEN.toString());

		s1.append(p.getDisplayName());
		s1.append(ChatColor.GREEN.toString());
		s1.append(" has just received the award: [");
		s1.append(awardName);
		s1.append(ChatColor.GREEN.toString());
		s1.append("]!");

		Bukkit.broadcastMessage(s1.toString());

	}

	public void sendAwardMessages(Player p, String awardName) {

		sendAwardMessagesToPlayer(p, awardName);

		sendAwardMessagesToServer(p, awardName);

	}

}
