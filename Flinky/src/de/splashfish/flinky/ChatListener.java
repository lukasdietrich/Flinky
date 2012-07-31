package de.splashfish.flinky;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class ChatListener implements Listener {

	private TicketHandler th;
	private BufferHandler buffer;
	private ArrayList<CommandSender> realtime = new ArrayList<CommandSender>();
	
	public ChatListener(File rootFile, TicketHandler th) {
		buffer = new BufferHandler(rootFile);
		this.th = th;
	}

	public boolean toggleRealtimeOutput(CommandSender sender) {
		if(sender.isOp() && !realtime.contains(sender)) {
			realtime.add(sender);
			return true;
		} else {
			realtime.remove(sender);
			return false;
		}
	}
	
	private void printToPlayers(String s) {
		for(int i = 0; i < realtime.size(); i++) {
			try {
				CommandSender p = realtime.get(i);
				if(p instanceof Player && !((Player)p).isOnline())
					realtime.remove(p);
				else
					p.sendMessage(ChatColor.DARK_AQUA +"[FLINKY]"+ ChatColor.GOLD +" "+ s);
			} catch (ConcurrentModificationException e) {
				// sender seems to be not accessible anymore
				realtime.remove(i);
				i--;
			}
		}
	}
	
	private void log(String message, Player p) {
		if(p instanceof Player) {
			Location loc = p.getLocation();
			message = "["+ loc.getBlockX() +", "+ loc.getBlockY() +", "+ loc.getBlockZ() +"] " + message;
		}
		
		buffer.log(message);
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint1(PlayerChatEvent e) {
		log(e.getPlayer().getName() +": "+ e.getMessage(), e.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint2(PlayerCommandPreprocessEvent e) {
		if(e.getMessage().toLowerCase().startsWith("/login")) {
			log(e.getPlayer().getName() +" tries to login via AuthMe", e.getPlayer());
		} else if(e.getMessage().toLowerCase().startsWith("/register")) {
			log(e.getPlayer().getName() +" tries to register via AuthMe", e.getPlayer());
		} else {
			log(e.getPlayer().getName() +": "+ e.getMessage(), e.getPlayer());
			printToPlayers(e.getPlayer().getName() +": "+ e.getMessage());
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint3(PlayerLoginEvent e) {
		log(e.getPlayer().getName() +"@"+ e.getAddress().getHostAddress() +" logged in", e.getPlayer());
		buffer.profile(e.getPlayer(), e.getAddress().getHostAddress());
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint4(PlayerQuitEvent e) {
		log(e.getPlayer().getName() +" logged out", e.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint5(PlayerDeathEvent e) {
		log(e.getDeathMessage(), e.getEntity());
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint6(PlayerKickEvent e) {
		log(e.getPlayer().getName() +" was kicked because '"+ e.getReason() +"'", e.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint7(ServerCommandEvent e) {
		if(!e.getCommand().toLowerCase().startsWith("rtping")) {
			log("Console: /"+ e.getCommand(), null);
			printToPlayers("Console: /"+ e.getCommand());
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void showTicketListener(final PlayerJoinEvent e) {
		
		final String onJoin = Flinky.globalVar("onJoinGamemode");
		if(onJoin.length() == 1) {
			DelayTask.invoke(new TimerTask() {

				@Override public void run() {
					switch(onJoin.charAt(0)) {
					case '0': e.getPlayer().setGameMode(GameMode.SURVIVAL); break;
					case '1': e.getPlayer().setGameMode(GameMode.CREATIVE); break;
					}
				}
				
			}, 2000);

		}
		
		if(e.getPlayer().hasPermission("flinky.ticketadmin")) {
			DelayTask.invoke(new TimerTask() {

				@Override public void run() {
					try {
						th.hasNewTickets(e.getPlayer());
					} catch (SQLException e) {}
				}
				
			}, 5000);
		}
		
		DelayTask.invoke(new TimerTask() {

			@Override public void run() {
				try {
					th.showReplies(e.getPlayer());
				} catch (SQLException e) {}
			}
			
		}, 6500);
	}
	
}
