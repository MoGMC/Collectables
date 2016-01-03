package com.fawkes.plugin.collectables.command;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.fawkes.plugin.collectables.AwardFactory;
import com.fawkes.plugin.collectables.CollectablesPlugin;

public class RemoveAward extends BaseCommand {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
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
			sender.sendMessage(
					"Query returned: " + CollectablesPlugin.getPlugin().removeAward(p.getUniqueId(), args[1]));

		} catch (SQLException e) {
			sender.sendMessage("Could not remove award from player. SQL error at time: " + System.currentTimeMillis());
			e.printStackTrace();

		}

		return true;
	}

}
