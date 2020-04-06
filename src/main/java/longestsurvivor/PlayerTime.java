package longestsurvivor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;


/**
 * This class handles and holds information of exactly one player, including yml storage.
 * @author fluggs
 *
 */
public class PlayerTime {
	public static final String pfileDir = "/playerData";
	
	private LongestSurvivor instance;
	private File pfile;
	private YamlConfiguration yaml;
	private Config config;
	private Location lastPos;
	private Boolean doAfk;
	private int afkCtr;
	public long survivalTime;
	public Boolean isActive;  // set to false when player already died
	public Player player;
	
	//inside yaml
	private static String counterPath = "survivaltime";
	private static String isActivePath = "isactive";
	private static String namePath = "name";
	
	public PlayerTime(LongestSurvivor instance, Player p) {
		this.instance = instance;
		this.config = instance.config;
		this.player = p;
		
		if (config.afkTime <= 0) {
			this.doAfk = false;
		}
		else {
			this.doAfk = true;
			this.afkCtr = config.afkTime;
		}
		
		// setup player file
		this.pfile = new File(instance.getDataFolder() + pfileDir, p.getUniqueId() + ".yml");
		Boolean fileExisted = true;

		if (!pfile.exists()) {
			fileExisted = false;
			try {
				pfile.createNewFile();
			} catch(Exception e) {
				p.sendMessage(ChatColor.RED + "Error creating your deathtime file, sorry :(");
			}
		}
		
		load(fileExisted);
	}
	
	/**
	 * Loads the config from pfile.
	 * @param fileExisted Determines whether the file existed on FS or not.
	 */
	private void load(Boolean fileExisted) {
		this.yaml = YamlConfiguration.loadConfiguration(pfile);
		
		// existing player
		if (fileExisted) {
			if (!yaml.isSet(counterPath)) {
				this.instance.messenger.error("ERROR: Deathtime: survivaltime not found in yaml");
			}
			else {
				this.survivalTime = yaml.getLong(counterPath);
			}
			if (!yaml.isSet(isActivePath)) {
				this.instance.messenger.error("ERROR: Deathtime: isactive not found in yaml");
			}
			else {
				this.isActive = yaml.getBoolean(isActivePath);
			}
		}
		
		// new player
		else {
			this.survivalTime = 0L;
			this.isActive = true;
		}
	}
	
	/**
	 * Updates the playertime yml.
	 */
	public void save() {
		yaml.set(namePath, this.player.getName());
		yaml.set(counterPath, this.survivalTime);
		yaml.set(isActivePath, this.isActive);
		try {
			yaml.save(pfile);
			this.instance.messenger.debug("Wrote player file: " + String.valueOf(survivalTime) + "; " + Boolean.valueOf(isActive));
		} catch (IOException e) {
			this.instance.messenger.error("Error on writing player file for player " + this.player.getName());
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds one minute to the player if it is active and has not died yet. Also handles afk.
	 */
	public void tick() {
		this.instance.messenger.debug("Ticking player " + this.player.getName());
		if (!this.isActive) {
			return;
		}
		
		// check afk
		Location pos = this.player.getLocation();
		if (this.doAfk && pos.equals(this.lastPos)) {
			this.afkCtr--;
			
			// afk
			if (this.afkCtr <= 0) {
				this.instance.messenger.debug(this.player.getName() + " is afk.");
				this.afkCtr = 0;
				return;
			}
		}
		else {
			this.afkCtr = this.config.afkTime;
			this.lastPos = pos;
			this.survivalTime++;
			this.save();
		}
		
	}
		
	/**
	 * Processes a death event.
	 * Determines whether event was a death event that counts. Used to exclude player kills and inform the players.
	 * @param event
	 * @return true if the death results in a dq, false if not
	 */
	public Boolean death(PlayerDeathEvent event) {
		assert event.getEntity() == this.player;
		
		if (!this.isActive) {
			this.instance.messenger.debug("Ignoring death of inactive player " + this.player.getName());
			return false;
		}
		
		// Do not count kills
		if (this.config.ignorePlayerKills && player.getKiller() instanceof Player) {
			this.instance.messenger.debug("Ignoring playerkill on " + this.player.getName());
			return false;
		}
		
		this.instance.messenger.debug("Killing " + this.player.getName());
		Messenger.disqualify(this);
		this.isActive = false;
		this.save();
		return true;
	}
	
	/**
	 * Sets the player to inactive to reset players timer on next reconnect
	 */
	public void resetPlayer() {
		this.isActive = false;
	}
	
	/**
	 * Removes all player files to reset the timer on next reconnect.
	 * @param instance
	 */
	public static void resetAllPlayers(LongestSurvivor instance) {
		instance.messenger.debug("Removing all player files");
		File playerdir = new File(instance.getDataFolder() + pfileDir);
		for (File pf : playerdir.listFiles())
			pf.delete();
	}
	
	/**
	 * Returns all player scores
	 * @param instance
	 * @return all playerscores as name, counter value and isActive 
	 */
	public static Triplet<String, Long, Boolean>[] getScores(LongestSurvivor instance) {
		instance.messenger.debug("Removing all player files");
		File playerdir = new File(instance.getDataFolder() + pfileDir);
		File[] filelist = playerdir.listFiles();
		@SuppressWarnings("unchecked")
		Triplet<String, Long, Boolean>[] scores = // crappy java
				(Triplet<String, Long, Boolean>[]) Array.newInstance(Triplet.class, filelist.length);
		
		for (int i = 0; i < filelist.length; i++) {
			File pf = filelist[i];
			String playername = "";
			long timerValue = 0;
			boolean isPlayerActive = false;
			
			YamlConfiguration config = YamlConfiguration.loadConfiguration(pf);
			if(config.isSet(namePath))
				playername = config.getString(namePath);
			if (config.isSet(counterPath))
				timerValue = config.getLong(counterPath);
			if (config.isSet(isActivePath))
				isPlayerActive = config.getBoolean(isActivePath);
			
			if(!playername.isBlank())
				scores[i] = new Triplet<String, Long, Boolean>(playername, timerValue, isPlayerActive);
		}
		
		Arrays.sort(scores, Comparator.comparing(Triplet<String, Long, Boolean>::getSecond).reversed());
		
		return scores;
	}
}
