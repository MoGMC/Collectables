package com.fawkes.plugin.collectables.command;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.fawkes.plugin.collectables.AwardFactory;
import com.fawkes.plugin.collectables.CollectablesPlugin;
import com.fawkes.plugin.collectables.QueryAward;

public class GiveAwardCommand extends BaseCommand {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		boolean hasmeta = false;

		if (args.length != 3) {
			if (args.length != 4) {
				sender.sendMessage("/giveaward <player name> <award> <level> [<meta>]");
				return true;

			}

			// they might be sending awildcard!

			hasmeta = true;

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

			QueryAward a = new QueryAward(args[1], System.currentTimeMillis(), level);

			if (hasmeta) {
				a.addMeta(args[3]);
				sender.sendMessage("Added metadata: " + args[3]);

			}

			CollectablesPlugin.getPlugin().giveAward(p.getUniqueId(), a);

			sender.sendMessage("Sent.");

		} catch (SQLException e) {
			sender.sendMessage("Could not award player. SQL error at time: " + System.currentTimeMillis());
			e.printStackTrace();

		}

		return true;
	}

}
