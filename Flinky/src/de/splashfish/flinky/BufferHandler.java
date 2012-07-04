package de.splashfish.flinky;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.entity.Player;

public class BufferHandler {
	
	private final DecimalFormat df = new DecimalFormat("00");
	private final String[] month = {
			"Jan", "Feb", "Mar", "Apr", "May", "Jun",
			"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
	};
	
	private File			rootFile;
	private File			curFile;
	private File			usrFile;
	private StringBuffer	buffer	 	= 	new StringBuffer();
	private boolean 		shouldprint;
	
	public BufferHandler(File rootFile) {
		this.rootFile = rootFile;
		this.shouldprint = Flinky.globalVar("printWhenFlushed").toLowerCase() == "true";
		this.usrFile = new File(rootFile.getAbsolutePath() + "/users/");
		
		if(!usrFile.exists())
			usrFile.mkdir();

		Timer t = new Timer();
		t.schedule(new TimerTask() {
			public void run() {
				updateFile();
			}
		}, 5000, Integer.parseInt(Flinky.globalVar("flushEvery"))*1000);
	}
	
	private void updateFile() {
		int currentEnd = buffer.length();
		if(currentEnd > 0) {
			Calendar date = Calendar.getInstance();
			String relative = date.get(Calendar.YEAR) + "/" + month[date.get(Calendar.MONTH)] + "/" + df.format(date.get(Calendar.DAY_OF_MONTH)) + ".log";
			curFile = new File(rootFile.getAbsolutePath() + "/" + relative);
			
			String[] tmps = curFile.getAbsolutePath().split(String.valueOf(File.separatorChar));
			File tmpf = new File("/"+ tmps[0]);
			
			for(int i = 1; i < tmps.length - 1; i++) {
				tmpf = new File(tmpf.getAbsolutePath() +"/"+ tmps[i]);
				if(!tmpf.exists()) {
					tmpf.mkdir();
					Flinky.print("created sub-dir: .. "+ tmps[i]);
				}
			}
					
			if(!curFile.exists()) {
				try {
					curFile.createNewFile();
					Flinky.print("created new log-file: plugins/Flinky/" + relative);
				} catch (IOException e) {
					Flinky.err("error while creating: plugins/Flinky/" + relative);
				}
				
			}
			
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(curFile, true));
				String[] packets = buffer.toString().split("\n");
				
				for(String packet : packets) {
					bw.append(packet);
					bw.newLine();
				}
				
				buffer.delete(0, currentEnd);
				
			} catch (IOException e) {
				Flinky.err("ERROR WHILE FLUSHING OUTPUT!\nAWAITING NEXT FLUSH IN 20000ms");
			} finally {
				try { 
					bw.close();
					if(shouldprint)
						Flinky.print("flushed "+ (currentEnd*2) +"bytes!"); 
				} catch (IOException e) { /*?*/ }
			}
		}
	}
	
	public void log(String arg) {
		Calendar date = Calendar.getInstance();
		buffer.append("["+
				df.format(date.get(Calendar.HOUR_OF_DAY))+":"+
				df.format(date.get(Calendar.MINUTE))+":"+
				df.format(date.get(Calendar.SECOND))+
		"] "+ arg +"\n");
	}
	
	public void profile(Player player, String ip) {
		File userfile = new File(usrFile.getAbsolutePath() +"/"+ player.getName() +".profile");
		StringBuffer ips = new StringBuffer();
		if(!userfile.exists()) {
			try {
				userfile.createNewFile();
			} catch (IOException e) {
				Flinky.err("error while creating: plugins/Flinky/users/"+ player.getName() +".profile");
			}
		} else {
			try {
				String line;
				BufferedReader ur = new BufferedReader(new FileReader(userfile));
				while((line = ur.readLine()) != null) {
					ips.append(line +" ");
				}
				ur.close();
			} catch (FileNotFoundException e) {
				// isnt possible
			} catch (IOException e) {
				Flinky.err("error while reading: plugins/Flinky/users/"+ player.getName() +".profile");
			}
		}
		
		if(!ips.toString().contains(ip)) {
			try {
				BufferedWriter uw = new BufferedWriter(new FileWriter(userfile, true));
				uw.append(ip);
				uw.newLine();
				uw.close();
			} catch (IOException e) {
				Flinky.err("error while writing: plugins/Flinky/users/"+ player.getName() +".profile");
			}
		}
	}
	
}
