package de.splashfish.flinky;

import java.io.File;
import java.sql.SQLException;
import java.util.TimerTask;

import org.bukkit.GameMode;
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
	
	public ChatListener(File rootFile, TicketHandler th) {
		buffer = new BufferHandler(rootFile);
		this.th = th;
	}

	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint1(PlayerChatEvent e) {
		buffer.log(e.getPlayer().getName() +": "+ e.getMessage());
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint2(PlayerCommandPreprocessEvent e) {
		if(e.getMessage().toLowerCase().startsWith("/login")) {
			buffer.log(e.getPlayer().getName() +" tries to login via AuthMe");
		} else if(e.getMessage().toLowerCase().startsWith("/register")) {
			buffer.log(e.getPlayer().getName() +" tries to register via AuthMe");
		} else {
			buffer.log(e.getPlayer().getName() +": "+ e.getMessage());
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint3(PlayerLoginEvent e) {
		buffer.log(e.getPlayer().getName() +"@"+ e.getAddress().getHostAddress() +" logged in");
		buffer.profile(e.getPlayer(), e.getAddress().getHostAddress());
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint4(PlayerQuitEvent e) {
		buffer.log(e.getPlayer().getName() +" logged out");
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint5(PlayerDeathEvent e) {
		buffer.log(e.getDeathMessage());
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint6(PlayerKickEvent e) {
		buffer.log(e.getPlayer().getName() +" was kicked because '"+ e.getReason() +"'");
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void chatPrint7(ServerCommandEvent e) {
		if(!e.getCommand().toLowerCase().startsWith("rtping"))
			buffer.log("Console: /"+ e.getCommand());
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
