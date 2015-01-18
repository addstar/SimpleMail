package me.odium.test.commands;

import java.util.concurrent.ExecutionException;

import me.odium.test.DBConnection;
import me.odium.test.Lookup;
import me.odium.test.Lookup.LookupCallback;
import me.odium.test.Statements;
import me.odium.test.SimpleMailPlugin;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSendMail implements CommandExecutor {

	public SimpleMailPlugin plugin;

	public CommandSendMail(SimpleMailPlugin plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(final CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage("/sendmail <ExactPlayerName> <Message>");
			return true;
		}
		
		final String message = StringUtils.join(args, ' ', 1, args.length);
		
		Lookup.resolve(plugin, args[0], new LookupCallback() {
            @Override
            public void run(OfflinePlayer player) {
                if (player == null) {
                    sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "That player does not exist.");
                    return;
                }
                
                try {
                    int count = service.executeQueryInt(Statements.InboxCount, player.getUniqueId());
                    int maxSize = plugin.getConfig().getInt("MaxMailboxSize");
                    
                    if (count >= maxSize) {
                        sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "Player's Inbox is full");
                        return;
                    }
                    
                    service.executeUpdate(Statements.SendMail,
                            (sender instanceof Player ? ((Player)sender).getUniqueId() : null),
                            sender.getName(),
                            player.getUniqueId(),
                            player.getName(),
                            message);
                    
                    // Notify
                    sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.GREEN + "Message Sent to: " + ChatColor.WHITE + player.getName());
                    String msg = ChatColor.GRAY + "[SimpleMail] " + ChatColor.GREEN + "You've Got Mail!" + ChatColor.GOLD + " [/mail]";
                    if (player.isOnline()) {
                        player.getPlayer().sendMessage(msg);
                    } else {
                        plugin.SendPluginMessage("Message", player.getName(), msg);
                    }
                } catch (ExecutionException e) {
                    sender.sendMessage(ChatColor.RED + "An internal error occured while executing this command.");
                }
            }
        });

		return true;
	}

}