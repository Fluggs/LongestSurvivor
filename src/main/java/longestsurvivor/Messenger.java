package longestsurvivor;

import org.bukkit.Bukkit;

/**
 * This class handles communication with the players and the logs.
 * @author fluggs
 *
 */
public class Messenger {
	LongestSurvivor instance;
	
	public Messenger(LongestSurvivor instance) {
		this.instance = instance;
	}
	public void debug(String msg) {
		return;
		// this.instance.getLogger().info(msg);
	}
	public void error(String msg) {
		this.instance.getLogger().info(msg);
	}
	public static void broadcast(String msg) {
		Bukkit.broadcastMessage(msg);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "discord bcast " + msg);
	}
	
	public static void disqualify(PlayerTime pt) {
		pt.player.sendMessage("Longest Survivor: You are out!\n");
		broadcast(pt.player.getName() + " is disqualified! They survived " + String.valueOf(pt.survivalTime) + " minutes." );
	}
}
