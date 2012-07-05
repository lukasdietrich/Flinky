package de.splashfish.flinky;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Commands {
	
	private String ap = "flinky.ticketadmin";
	private JavaPlugin plug;
	private TicketHandler th;
	private UpdateManager um;
	public Commands(JavaPlugin plug, TicketHandler th, UpdateManager um) {
		this.plug = plug;
		this.th = th;
		this.um = um;
	}
	
	public boolean flinky(CommandSender sender, String label, String[] args) {
		if(sender.hasPermission("flinky.general") && args.length > 0) {
			if(args[0].equalsIgnoreCase("backup")) {
				return backup(sender, label, args);
			} else if(args[0].equalsIgnoreCase("update")) {
				return um.invokeUpdate(sender);
			}
		}
		return false;
	}
	
	private boolean backup(CommandSender sender, String label, String[] args) {
		if(BackupHandler.backup(sender, plug.getDataFolder())) {
			sender.sendMessage(ChatColor.DARK_PURPLE +"Backup started!");
		} else {
			sender.sendMessage(ChatColor.DARK_PURPLE +"Somebody already started a backup!");
		}
		return true;
	}
	
	public boolean ban(Player player, CommandSender sender, String label, String[] args) {
		if (player instanceof Player && player.isOp() && args.length == 1){
			String toban = args[0];
			
			if(plug.getServer().dispatchCommand(plug.getServer().getConsoleSender(), "ban "+ toban)) {
				player.sendMessage(ChatColor.BLUE +"Player named '"+ toban +"' is banned!");
			}
			
			File[] tmpfiles = new File(plug.getDataFolder()+"/users/").listFiles();
			for(int i = 0; i < tmpfiles.length; i++) {
				if(tmpfiles[i].getName().equalsIgnoreCase(toban +".profile")) {
					try {
						String line;
						BufferedReader br = new BufferedReader(new FileReader(tmpfiles[i]));
						while((line = br.readLine()) != null) {
							plug.getServer().dispatchCommand(plug.getServer().getConsoleSender(), "banip "+ line);
							player.sendMessage(ChatColor.BLUE +"The ip '"+ line +"' is associated with "+ toban +", banned!");
						}
						br.close();
					} catch (FileNotFoundException e) {
						// impossible
					} catch (CommandException e) {
						Flinky.err("error while trying to execute external command");
					} catch (IOException e) {
						Flinky.err("error while trying to execute external command");
					}
					break;
				}
			}
			
			return true;
		}
		return false;
	}
	
	public boolean unban(Player player, CommandSender sender, String label, String[] args) {
		if (player instanceof Player && player.isOp() && args.length == 1){
			String toban = args[0];
			
			if(plug.getServer().dispatchCommand(plug.getServer().getConsoleSender(), "unban "+ toban)) {
				player.sendMessage(ChatColor.BLUE +"Player named '"+ toban +"' is unbanned!");
			}
			
			File[] tmpfiles = new File(plug.getDataFolder()+"/users/").listFiles();
			for(int i = 0; i < tmpfiles.length; i++) {
				if(tmpfiles[i].getName().equalsIgnoreCase(toban +".profile")) {
					try {
						String line;
						BufferedReader br = new BufferedReader(new FileReader(tmpfiles[i]));
						while((line = br.readLine()) != null) {
							plug.getServer().dispatchCommand(plug.getServer().getConsoleSender(), "unbanip "+ line);
							player.sendMessage(ChatColor.BLUE +"The ip '"+ line +"' is associated with "+ toban +", unbanned!");
						}
						br.close();
					} catch (FileNotFoundException e) {
						// impossible
					} catch (CommandException e) {
						Flinky.err("error while trying to execute external command");
					} catch (IOException e) {
						Flinky.err("error while trying to execute external command");
					}
					break;
				}
			}
			
			return true;
		}
		return false;
	}
	
	public boolean ticket(Player player, CommandSender sender, String label, String[] args) {
		if(args.length == 0) {
			sender.sendMessage(new String[] {
				ChatColor.GRAY +"-----------"+ChatColor.RED+"TICKET"+ChatColor.GRAY+"-----------",
				ChatColor.RED  +"/ticket new <message>",
				ChatColor.RED  +"/ticket list",
				ChatColor.RED  +"/ticket close <id>",
				ChatColor.GRAY +"----------------------------",
			});
		} else {
			String innercmd = args[0];
			String message = null;
			if(args.length >= 2) {
				message = "";
				for(int i = 1; i < args.length; i++) {
					message += args[i];
					if(i < args.length-1) {
						message += " ";
					}
				}
			}
			
			if(innercmd.equalsIgnoreCase("new") && args.length >= 2 && player instanceof Player) {
				try {
					th.addTicket(player, message);
				} catch (SQLException e) {
					player.sendMessage(ChatColor.RED+"ERROR CODE #46201, please tell!");
					player.sendMessage(ChatColor.RED+e.getLocalizedMessage());
				}
			} else if(innercmd.equalsIgnoreCase("list") && args.length == 1 && player instanceof Player) {
				if(player.hasPermission(ap)) {
					try {
						th.getTicketsAdmin(player);
					} catch (SQLException e) {
						player.sendMessage(ChatColor.RED+"ERROR CODE #46202, please tell!");
						player.sendMessage(ChatColor.RED+e.getLocalizedMessage());
					}
				} else {
					try {
						th.getTickets(player);
					} catch (SQLException e) {
						player.sendMessage(ChatColor.RED+"ERROR CODE #46202, please tell!");
						player.sendMessage(ChatColor.RED+e.getLocalizedMessage());
					}
				}
			} else if(innercmd.equalsIgnoreCase("close") && args.length == 2 && player instanceof Player) {
				if(player.hasPermission(ap)) {
					try {
						th.closeTicketAdmin(player, Integer.parseInt(args[1]));
					} catch (SQLException e) {
						player.sendMessage(ChatColor.RED+"ERROR CODE #46202, please tell!");
						player.sendMessage(ChatColor.RED+e.getLocalizedMessage());
					} catch (NumberFormatException e) {
						player.sendMessage(ChatColor.RED+""+args[1]+" is not a number!");
					}
				} else {
					try {
						th.closeTicket(player, Integer.parseInt(args[1]));
					} catch (SQLException e) {
						player.sendMessage(ChatColor.RED+"ERROR CODE #46202, please tell!");
						player.sendMessage(ChatColor.RED+e.getLocalizedMessage());
					} catch (NumberFormatException e) {
						player.sendMessage(ChatColor.RED+""+args[1]+" is not a number!");
					}
				}
			} else if(innercmd.equalsIgnoreCase("reply") && args.length >= 2 && player instanceof Player) {
				if(player.hasPermission(ap)) {
					try {
						th.replyTicket(player, Integer.parseInt(args[1]), message.substring(message.indexOf(" ")));
						return true;
					} catch (SQLException e) {
						player.sendMessage(ChatColor.RED+"ERROR CODE #46204, please tell!");
						player.sendMessage(ChatColor.RED+e.getLocalizedMessage());
					} catch (NumberFormatException e) {
						player.sendMessage(ChatColor.RED+""+args[1]+" is not a number!");
					}
				} else {
					player.sendMessage(ChatColor.RED +"You don't have permission!");
				}
			} else {
				return false;
			}
		}
		return true;
	}
}
