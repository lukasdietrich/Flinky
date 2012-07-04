package de.splashfish.flinky;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

public class ConfigHandler {
	
	private File 		cFile;
	private Properties 	config;
	
	private final HashMap<String, String> defaults = new HashMap<String, String>();
	
	public ConfigHandler(File rootFile) {
		cFile 	= new File(rootFile.getAbsolutePath() +"/config.yml");
		config 	= new Properties();
		
		defaults.put("flushEvery", 			"20");
		defaults.put("printWhenFlushed", 	"true");
		defaults.put("removeOlderThan", 	"0");
		defaults.put("advertEvery", 		"30");
		
		if(!cFile.exists()) {
			try {
				generateConfig();
			} catch (IOException e) { 
				Flinky.err("WAS NOT ABLE TO GENERATE CONFIGS FILE!"); 
			}
		}
		
		try {
			config.load(new FileInputStream(cFile));
		} catch (FileNotFoundException e) {
			Flinky.err("COULD NOT FIND THE CONFIGS FILE,- USING DEFAULTS!");
		} catch (IOException e) {
			Flinky.err("CONFIGS FILE IS NO PROPER FILE!\n\n"+ e.getMessage() +"\n\nUSING DEFAULTS!");
		}
	}
	
	public String getValue(String key) {
		if(config.containsKey(key)) {
			return config.getProperty(key);
		} else {
			return defaults.get(key);
		}
	}
	
	private void generateConfig() throws IOException {
		if(cFile instanceof File) {
			cFile.createNewFile();
			
			Set<String> set = defaults.keySet();
			for(String s : set) {
				config.setProperty(s, defaults.get(s));
			}
			
			config.store(new FileOutputStream(cFile), "This is the config-file of the 'Flinky'-Plugin!\n" +
													  "All lines with leading # are comments.\n" +
													  "Don't use tabs, this is a yml.");
		}
	}

}
