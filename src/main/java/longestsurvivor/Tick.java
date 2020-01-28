package longestsurvivor;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

public class Tick implements Runnable {
	private LongestSurvivor instance;
	
	public Tick(LongestSurvivor instance) {
		this.instance = instance;
	}
	
	public void run() {
		ArrayList<PlayerTime> playerList = this.instance.playerList;
		ArrayList<PlayerDeathEvent> deathList = this.instance.deathList;
		ReentrantLock deathListLock = this.instance.deathListLock;
		this.instance.messenger.debug("Running tick()");
		
		// Process logins
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			Boolean found = false;
			for (PlayerTime pt : playerList) {
				if (player == pt.player) {
					found = true;
					break;
				}
			}
				
			if (!found) {
				playerList.add(new PlayerTime(this.instance, player));
			}
		}
		
		// Process deaths
		deathListLock.lock();
		for (PlayerDeathEvent death : deathList) {
			this.instance.messenger.debug("Processing a death");
			Boolean found = false;
			for (PlayerTime pt : playerList) {
				if (pt.player == death.getEntity()) {
					pt.death(death);
					found = true;
					break;
				}
			}
			
			// Player already logged out
			if (!found) {
				PlayerTime pt = new PlayerTime(this.instance, death.getEntity());
				pt.death(death);
			}
		}
		deathList.clear();
		deathListLock.unlock();
		
		// Process logouts
		ArrayList<PlayerTime> toRemove = new ArrayList<PlayerTime>();
		for (PlayerTime pt : playerList) {
			if (!Bukkit.getServer().getOnlinePlayers().contains(pt.player)) {
				toRemove.add(pt);
			}
		}
		for (PlayerTime pt : toRemove) {
			pt.save();
			playerList.remove(pt);
		}
		
		// Tick players
		for (PlayerTime pt : playerList) {
			pt.tick();
		}
	}

}
