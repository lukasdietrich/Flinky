package de.splashfish.flinky;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class FreezeNatureHandler {

	private List<World>   	worlds	= new ArrayList<World>();
	private List<Boolean> 	frozen 	= new ArrayList<Boolean>();
	private List<Boolean>	sun		= new ArrayList<Boolean>();
	private List<Long>		time	= new ArrayList<Long>();
	
	private Timer t = new Timer();
	private TimerTask tk = new TimerTask(){
		@Override
		public void run() {
			doChanges();
		}
	};
	
	private void doChanges(){
		if(worlds != null){
			for(int i = 0; i < worlds.size(); i++){
				if(frozen.get(i)){
					worlds.get(i).setFullTime(time.get(i));
				}
			}
		}
	}
	
	private Server server;
	public FreezeNatureHandler(PluginManager pm, Plugin plug) {
		pm.registerEvents(new NatureChangeListener(sun, worlds), plug);
		server = plug.getServer();
		
		t.schedule(tk, 10000, 30000);
	}
	
	public void disable() {
		t.cancel();
	}
	
	public boolean handleCommands(CommandSender sender, Command cmd, String label, String[] args) {
		Player p = null;
		boolean b = false;
		if(sender instanceof Player) {
			p = (Player)sender;
			if(p.isOp())
				b = true;
		} else {
			sender.sendMessage(ChatColor.GOLD+"The commands 'freezetime' and 'foreversun' are just for ingame-players.");
		}
		
		if(b && (cmd.getName().equalsIgnoreCase("freezetime") || cmd.getName().equalsIgnoreCase("foreversun"))){
			int ind = worldIndex(p.getWorld());
			if(ind == -1){
				worlds.add(p.getWorld());
				time.add(0L);
				frozen.add(false);
				sun.add(false);
				ind = worlds.size()-1;
			}
			
			if(cmd.getName().equalsIgnoreCase("freezetime")){
				if(frozen.get(ind)){
					frozen.set(ind, false);
					server.broadcastMessage(ChatColor.GOLD+"Time in the '"+worlds.get(ind).getName()+"' is unfrozen!");
				}else{
					time.set(ind, worlds.get(ind).getFullTime());
					frozen.set(ind, true);
					server.broadcastMessage(ChatColor.GOLD+"Time in the '"+worlds.get(ind).getName()+"' is frozen!");
				}
			}else if(cmd.getName().equalsIgnoreCase("foreversun")){
				if(sun.get(ind)){
					sun.set(ind, false);
					server.broadcastMessage(ChatColor.GOLD+"'"+worlds.get(ind).getName()+"' is not forever sunny anymore!");
				}else{
					if(worlds.get(ind).hasStorm())
						worlds.get(ind).setStorm(false);
					if(worlds.get(ind).isThundering())
						worlds.get(ind).setThundering(false);
					
					sun.set(ind, true);
					server.broadcastMessage(ChatColor.GOLD+"'"+worlds.get(ind).getName()+"' is now sunny forever!");
				}
			}
				
				return true;
		}
		return false;
	}
	
	private int worldIndex(World w){
		int j = -1;
		if(worlds != null && worlds.size() > 0)
			for(int i = 0; i < worlds.size(); i++){
				if(w.getName().equals(worlds.get(i).getName()))
					j = i;
			}
		return j;
	}
	
}

class NatureChangeListener implements Listener {
	List<Boolean> sun;
	List<World> worlds;
	
	public NatureChangeListener(List<Boolean> sun, List<World> worlds){
		this.sun = sun;
		this.worlds = worlds;
	}
	
	@EventHandler(priority = EventPriority.LOWEST) public void onWeatherChange(WeatherChangeEvent event){
		if(sun != null && sun.size() != 0){
			int ind = worldIndex(event.getWorld());
				if(ind != -1 && sun.get(ind)){
					event.setCancelled(true);
				}
		}
	}
	
	private int worldIndex(World w){
		int j = -1;
		if(worlds != null && worlds.size() > 0)
			for(int i = 0; i < worlds.size(); i++){
				if(w.getName().equals(worlds.get(i).getName()))
					j = i;
			}
		return j;
	}
}
