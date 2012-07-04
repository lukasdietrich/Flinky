package de.splashfish.flinky;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class BackupHandler {
	
	private static boolean isWorking = false;
	public static boolean backup(final CommandSender sender, final File data) {
		if(!isWorking) {
			new Thread(new Runnable() {
				@Override public void run() {
					isWorking = true;
					
					File backup = new File(data.getAbsolutePath()+"/../../Flinky_bak.zip");
					File backupold = new File(backup.getAbsolutePath()+".old");
					
						try {
							if(backup.exists())
								backup.renameTo(backupold);
							
							ZipOutputStream zipout = new ZipOutputStream(new FileOutputStream(backup));
								addDirectory(data.getAbsolutePath().length()+1, zipout, data);
							zipout.close();
							sender.sendMessage(ChatColor.DARK_PURPLE + "Flinky files are all backed up!");
							
							if(backupold.exists())
								backupold.delete();
						} catch (FileNotFoundException e) {
							sender.sendMessage(ChatColor.DARK_PURPLE + "Flinky data wasn't found :o !");
							if(backup.exists())
								backup.delete();
							backupold.renameTo(backup);
						} catch (IOException e) {
							sender.sendMessage(ChatColor.DARK_PURPLE + "An error occured while backing up! Please retry!");
							if(backup.exists())
								backup.delete();
							backupold.renameTo(backup);
						}
					
					isWorking = false;
				}
			}).start();;
			return true;
		} else {
			return false;
		}
	}
	
	private static void addDirectory(int rootlength, ZipOutputStream zout, File fileSource) throws IOException {
		File[] files = fileSource.listFiles();

		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				addDirectory(rootlength, zout, files[i]);
				continue;
			}

			byte[] buffer = new byte[1024];
			FileInputStream fin = new FileInputStream(files[i]);

			zout.putNextEntry(new ZipEntry(files[i].getAbsolutePath().substring(rootlength)));
			int length;

			while ((length = fin.read(buffer)) > 0) {
				zout.write(buffer, 0, length);
			}

			zout.closeEntry();
			fin.close();

		}
	}

}
