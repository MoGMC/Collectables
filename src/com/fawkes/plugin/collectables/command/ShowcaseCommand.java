package com.fawkes.plugin.collectables.command;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.fawkes.plugin.collectables.CollectablesPlugin;
import com.fawkes.plugin.collectables.Menu;
import com.fawkes.plugin.collectables.MenuFactory;
import com.fawkes.plugin.collectables.QueryAward;

public class ShowcaseCommand extends BaseCommand {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
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

		awardsList = CollectablesPlugin.getPlugin().getAwards(uuid, sender);

		Menu menu = new Menu(name, awardsList);

		// register the new open menu under the player who is OPENING it.
		// (not the target!)

		MenuFactory.registerOpenMenu(origUuid, menu);

		((Player) sender).openInventory(menu.getMain());

		return true;

	}

}
