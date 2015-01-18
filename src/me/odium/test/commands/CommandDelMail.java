package me.odium.test.commands;

import java.util.concurrent.ExecutionException;
import me.odium.test.DBConnection;
import me.odium.test.Statements;
import me.odium.test.SimpleMailPlugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandDelMail implements CommandExecutor {

	public SimpleMailPlugin plugin;

	public CommandDelMail(SimpleMailPlugin plugin) {
		this.plugin = plugin;
	}

	DBConnection service = DBConnection.getInstance();

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    if (args.length != 1) {
            return false;
        }
	    
	    Player player = null;
		if (sender instanceof Player) {
			player = (Player) sender;
		}

		// Parse the message id
		int messageId;
		try {
		    messageId = Integer.parseInt(args[0]);
		    if (messageId < 0) {
		        sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "The message does not exist");
		        return true;
		    }
		} catch (NumberFormatException e) {
		    sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "The message does not exist");
            return true;
		}
		
		try {
		    // Check that the user owns the message and that it exists
		    if (service.executeQueryInt(Statements.CheckMessageOwn, messageId, player.getUniqueId()) == 0) {
                sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.RED + "The message does not exist");
                return true;
            }
            
		    // Delete it
            service.executeUpdate(Statements.Delete, messageId, player.getUniqueId());
            sender.sendMessage(ChatColor.GRAY + "[SimpleMail] " + ChatColor.GREEN + "Message Deleted.");
		} catch (ExecutionException e) {
		    sender.sendMessage(ChatColor.RED + "An internal error occured while executing this command.");
		}

		return true;
	}
}