package longestsurvivor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import org.bukkit.configuration.file.YamlConfiguration;
 
/**
 * Handles the configuration file.
 */
public class Config {
	
	private static YamlConfiguration yaml;
	private static File cFile;
	
	public String debugPath = "debug";
	public Boolean debug = false;
	
	public String ignorePlayerKillsPath = "ignoreplayerkills";
	public Boolean ignorePlayerKills = true;
	
	public String afkTimePath = "afktime";
	public int afkTime = 5;
	
	public String acceptNewPlayersPath = "acceptnewplayers";
	public Boolean acceptNewPlayers = true;
	
	public String discordSRVPath = "discordsrv";
	public Boolean discordSRV = false;
 
	/**
	 * Gets the configuration file.
	 * 
	 * @return Configuration file
	 */
	public static File getConfigFile() {
		return cFile;
	}
	
	/**
	 * Loads the configuration file from the .jar.
	 */
	public Config(LongestSurvivor instance) throws Exception {
		cFile = new File(instance.getDataFolder(), "config.yml");
		if (!cFile.exists()) {
			instance.getDataFolder().mkdir();
			InputStream jarURL = Config.class.getResourceAsStream("/config.yml");
			copyFile(jarURL, cFile);
		}
		yaml = new YamlConfiguration();
		yaml.load(cFile);
	}
	
	/**
	 * Copies a file to a new location.
	 * 
	 * @param in InputStream
	 * @param out File
	 */
	static private void copyFile(InputStream in, File out) throws Exception {
		InputStream fis = in;
		FileOutputStream fos = new FileOutputStream(out);
		try {
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (fis != null) {
				fis.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}
}
