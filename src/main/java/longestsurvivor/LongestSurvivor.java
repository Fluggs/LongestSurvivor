package longestsurvivor;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import longestsurvivor.PlayerTime;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class LongestSurvivor extends JavaPlugin implements Listener {
	final ArrayList<PlayerTime> playerList = new ArrayList<PlayerTime>();
	final ReentrantLock deathListLock = new ReentrantLock();
	final ArrayList<PlayerDeathEvent> deathList = new ArrayList<PlayerDeathEvent>();
	
	public Messenger messenger;
	public Config config;
	
	@Override
	public void onEnable() {
		this.messenger = new Messenger(this);
		this.messenger.log("Loading LongestSurvivor ...");
		this.saveDefaultConfig();
		try {
			this.config = new Config(this);
		} catch (Exception e) {
			this.messenger.log("LongestSurvivor: Failed to read config");
			e.printStackTrace();
			this.getServer().getPluginManager().disablePlugin(this);
		}
		getServer().getPluginManager().registerEvents(this, this);
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
		    playerList.add(new PlayerTime(this, player));
		}
		this.messenger.log("Scheduling ticker ...");
		Bukkit.getServer().getScheduler().runTaskTimer(this, new Tick(this), 20*60L, 20*60L);
		this.messenger.log("LongestSurvivor loaded");
	}
	
	@Override
	public void onDisable() {
		PlayerLoginEvent.getHandlerList().unregister((Listener)this);
		PlayerDeathEvent.getHandlerList().unregister((Listener)this);
		PlayerQuitEvent.getHandlerList().unregister((Listener)this);
		for (PlayerTime pt : playerList) {
			pt.save();
		}
	}
	
	/**
	 * Marks a death for later processing.
	 * @param event Event that is fired
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeath(PlayerDeathEvent event) {
		this.deathListLock.lock();
		try {
			deathList.add(event);
		}
		finally {
			this.deathListLock.unlock();
		}
	}
}