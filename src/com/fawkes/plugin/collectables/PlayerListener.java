package com.fawkes.plugin.collectables;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

	/*
	 * listens for joins and gives em their waiting awards
	 */

	// TODO: remove static things.

	private CollectablesPlugin plugin;

	public PlayerListener() {
		plugin = CollectablesPlugin.getPlugin();

	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		// gets list of waiting awards

		try {
			ResultSet rs = plugin.getOfflineAwards(e.getPlayer().getUniqueId());

			if (!rs.isBeforeFirst()) {
				// no results in the ResultSet!
				return;

			}

			Player p = e.getPlayer();

			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

				@Override
				public void run() {

					// if they went offline, cancel.
					if (!p.isOnline()) {
						return;

					}

					try {

						// there are things in the ResultSet!
						while (rs.next()) {
							// we already gave it to them, we just have to
							// display
							// nice
							// messages.
							plugin.sendAwardMessages(p, rs.getString(2));
							// TODO: maybe switch to column name instead of
							// index?

						}

						plugin.clearOfflineAwards(p.getUniqueId());

					} catch (SQLException sql) {
						Bukkit.getLogger().severe("Could not give player their offline awards inside the runnable.");
						sql.printStackTrace();

					}

				}

			}, 70L);

		} catch (SQLException sql) {
			Bukkit.getLogger().severe("Could not give player their offline awards.");
			sql.printStackTrace();

		}

	}

}
