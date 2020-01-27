package longestsurvivor;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import longestsurvivor.LongestSurvivor;


/**
 * This class handles and holds information of exactly one player, including yml storage.
 * @author fluggs
 *
 */
public class PlayerTime {
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
	private String counterPath = "survivaltime";
	private String isActivePath = "isactive";
	
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
		this.pfile = new File(instance.getDataFolder() + "/playerData", p.getUniqueId() + ".yml");
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
				Bukkit.getServer().getConsoleSender().sendMessage("ERROR: Deathtime: survivaltime not found in yaml");
			}
			else {
				this.survivalTime = yaml.getLong(counterPath);
			}
			if (!yaml.isSet(isActivePath)) {
				Bukkit.getServer().getConsoleSender().sendMessage("ERROR: Deathtime: isactive not found in yaml");
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
		yaml.set(counterPath, this.survivalTime);
		yaml.set(isActivePath, this.isActive);
		try {
			yaml.save(pfile);
			this.instance.messenger.log("Wrote player file: " + String.valueOf(survivalTime) + "; " + Boolean.valueOf(isActive));
		} catch (IOException e) {
			this.instance.messenger.log("Error on writing player file");
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds one minute to the player if it is active and has not died yet. Also handles afk.
	 */
	public void tick() {
		this.instance.messenger.log("Ticking player " + this.player.getName());
		if (!this.isActive) {
			return;
		}
		
		// check afk
		Location pos = this.player.getLocation();
		if (this.doAfk && pos.equals(this.lastPos)) {
			this.afkCtr--;
			
			// afk
			if (this.afkCtr <= 0) {
				this.instance.messenger.log(this.player.getName() + " is afk.");
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
	 * @return true if the death should count, false if not
	 */
	public Boolean death(PlayerDeathEvent event) {
		assert event.getEntity() == this.player;
		
		// Do not count kills
		if (this.config.ignorePlayerKills && player.getKiller() instanceof Player) {
			this.instance.messenger.log("Ignoring playerkill on " + this.player.getName());
			return false;
		}
		
		this.instance.messenger.log("Killing " + this.player.getName());
		Messenger.disqualify(this);
		this.isActive = false;
		this.save();
		return true;
	}
}
