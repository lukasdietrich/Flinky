package de.splashfish.flinky;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import lib.PatPeter.SQLibrary.*;

public class TicketHandler {
	private SQLite db;
	private File file;
	
	public TicketHandler(Logger log, File path) {
		file = new File(path.getAbsolutePath()+"/ticketsystem");
		
		db = new SQLite(log, "[DB]", "tickets", file.getAbsolutePath());
		
		if(!db.checkTable("tickets")) {
			db.createTable("CREATE TABLE tickets(id INTEGER PRIMARY KEY ASC, player, message)");
			db.createTable("CREATE TABLE answers(id INTEGER PRIMARY KEY ASC, player, message, ticket)");
		}
		
	}
	
	public void disable() {
		try {
			db.getConnection().close();
		} catch (SQLException e) {} finally {
			db.close();
		}
	}
	
	public void addTicket(Player player, String msg) throws SQLException {
		if(countTickets(player) < 5) { 
			Connection conn = db.getConnection();
			PreparedStatement prepare = conn.prepareStatement("INSERT INTO tickets (player, message) VALUES (?, ?)");
			conn.setAutoCommit(false);
			 
			prepare.setString(1, player.getName());
			prepare.setString(2, msg);
			
			prepare.executeUpdate();
			 
			conn.commit();
			 
			conn.setAutoCommit(true);
			
			prepare.close();
			player.sendMessage(ChatColor.GREEN+"Ticket sent!");
		} else {
			player.sendMessage(ChatColor.RED+"You can only have 5 tickets at a time!");
		}
	}
	
	public void getTicketsAdmin(Player player) throws SQLException {
		Connection conn = db.getConnection();
		PreparedStatement prepare = conn.prepareStatement("SELECT * FROM tickets ORDER BY id ASC LIMIT 5");
		conn.setAutoCommit(false);
		 
		ResultSet rs = prepare.executeQuery();
		
		player.sendMessage(ChatColor.GRAY +"-----------"+ ChatColor.RED +"LAST-5"+ ChatColor.GRAY +"-----------");
		while(true) {
			if(!rs.next())
				break;
			player.sendMessage(ChatColor.RED+"["+rs.getString("id")+", "+rs.getString("player")+"] "+rs.getString("message"));
		}
		player.sendMessage(ChatColor.GRAY +"----------------------------");
		
		conn.commit();
		
		conn.setAutoCommit(true);
		
		prepare.close();
		
		if(rs != null) {
			rs.close();
		}
	}
	
	public void getTickets(Player player) throws SQLException {
		Connection conn = db.getConnection();
		PreparedStatement prepare = conn.prepareStatement("SELECT * FROM tickets WHERE player='"+ player.getName() +"' ORDER BY id ASC");
		conn.setAutoCommit(false);
		 
		ResultSet rs = prepare.executeQuery();
		
		player.sendMessage(ChatColor.GRAY +"--------"+ ChatColor.RED +"YOUR-TICKETS"+ ChatColor.GRAY +"--------");
		while(true) {
			if(!rs.next())
				break;
			player.sendMessage(ChatColor.RED+"["+rs.getString("id")+"] "+rs.getString("message"));
		}
		player.sendMessage(ChatColor.GRAY +"----------------------------");
		
		conn.commit();
		
		conn.setAutoCommit(true);
		
		prepare.close();
		
		if(rs != null) {
			rs.close();
		}
	}
	
	private String[] getTicket(int id) throws SQLException {
		Connection conn = db.getConnection();
		PreparedStatement prepare = conn.prepareStatement("SELECT * FROM tickets WHERE id='"+ id +"'");
		conn.setAutoCommit(false);
		 
		String[] retval = new String[2];
		
		ResultSet rs = prepare.executeQuery();
		while(true) {
			if(!rs.next())
				break;
			retval[0] = rs.getString("message");
			retval[1] = rs.getString("player");
		}
		
		conn.commit();
		
		conn.setAutoCommit(true);
		
		prepare.close();
		
		if(rs != null) {
			rs.close();
		}
		return retval;
	}
	
	private int countTickets(Player player) throws SQLException {
		Connection conn = db.getConnection();
		PreparedStatement prepare = conn.prepareStatement("SELECT id FROM tickets WHERE player='"+ player.getName() +"'");
		conn.setAutoCommit(false);
		 
		ResultSet rs = prepare.executeQuery();
		
		int i = 0;
		while(true) {
			if(!rs.next())
				break;
			i++;
		}
		
		conn.commit();
		
		conn.setAutoCommit(true);
		
		prepare.close();
		
		if(rs != null) {
			rs.close();
		}
		
		return i;
	}
	
	public void closeTicketAdmin(Player player, int id) throws SQLException {
		Connection conn = db.getConnection();
		PreparedStatement prepare = conn.prepareStatement("DELETE FROM tickets WHERE id=?");
		conn.setAutoCommit(false);
		 
		prepare.setInt(1, id);
		
		int chk = prepare.executeUpdate();
		if(chk == 0) {
			player.sendMessage(ChatColor.RED+"There's no ticket with ID #"+id+"!");
		} else {
			player.sendMessage(ChatColor.GREEN +""+ chk +" of tickets got removed!");
		} 
		
		conn.commit();
		 
		conn.setAutoCommit(true);
		
		prepare.close();
		
	}
	
	public void closeTicket(Player player, int id) throws SQLException {
		Connection conn = db.getConnection();
		PreparedStatement prepare = conn.prepareStatement("DELETE FROM tickets WHERE player=? AND id=?");
		conn.setAutoCommit(false);
		 
		prepare.setString(1, player.getName());
		prepare.setInt(2, id);
		
		int chk = prepare.executeUpdate();
		if(chk == 0) {
			player.sendMessage(ChatColor.RED+"There's no ticket with ID #"+id+", you own!");
		} else {
			player.sendMessage(ChatColor.GREEN +""+ chk +" of your tickets got removed!");
		} 
		
		conn.commit();
		 
		conn.setAutoCommit(true);
		
		prepare.close();
	}
	
	public void showReplies(Player player) throws SQLException {
		Connection conn = db.getConnection();
		PreparedStatement prepare = conn.prepareStatement("SELECT * FROM answers WHERE player='"+ player.getName() +"' ORDER BY id ASC");
		conn.setAutoCommit(false);
		
		ResultSet rs = prepare.executeQuery();
		while(true) {
			if(!rs.next())
				break;
			player.sendMessage(new String[] {
					ChatColor.GREEN +"Your ticket '"+ rs.getString("ticket") +"' got replied!",
					ChatColor.YELLOW + rs.getString("message")
			});
		}
		
		conn.commit();
		
		conn.setAutoCommit(true);
		
		prepare.close();
		
		if(rs != null) {
			rs.close();
		}
		
		conn = db.getConnection();
		prepare = conn.prepareStatement("DELETE FROM answers WHERE player=?");
		conn.setAutoCommit(false);
		 
		prepare.setString(1, player.getName());
		
		prepare.executeUpdate();
		
		conn.commit();
		 
		conn.setAutoCommit(true);
		
		prepare.close();
	}
	
	public void replyTicket(Player player, int id, String answer) throws SQLException {
		String[] ticketmsg_player = getTicket(id);
		
		if(Bukkit.getPlayer(ticketmsg_player[1]).isOnline()) {
			
			Bukkit.getPlayer(ticketmsg_player[1]).sendMessage(new String[] {
					ChatColor.GREEN +"Your ticket '"+ ticketmsg_player[0] +"' got replied!",
					ChatColor.YELLOW + answer
			});
			
			closeTicketAdmin(player, id);
		} else {
		
			Connection conn = db.getConnection();
			PreparedStatement prepare = conn.prepareStatement("INSERT INTO answers (player, message, ticket) VALUES (?, ?, ?)");
			conn.setAutoCommit(false);
			 
			prepare.setString(1, ticketmsg_player[1]);
			prepare.setString(2, answer);
			prepare.setString(3, ticketmsg_player[0]);
			
			prepare.executeUpdate();
			 
			conn.commit();
			 
			conn.setAutoCommit(true);
			
			prepare.close();
			
			closeTicketAdmin(player, id);
			
		}
		
		player.sendMessage(ChatColor.GREEN+"Ticket replied!");
	}
}
