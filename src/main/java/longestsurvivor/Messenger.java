package longestsurvivor;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
	
	public static void sendScores(CommandSender sender, Triplet<String, Long, Boolean>[] scores) {
		sender.sendMessage("Longest Survivor Highscore:\nPlayer name, time count, Is active?\n");
		for(Triplet<String, Long, Boolean> playerdata : scores) {
			String name = playerdata.getFirst();
			long time = playerdata.getSecond();
			String isActiveStr = playerdata.getThird() ? "active!" : "out!";
			String msg = String.format("%1$10s | %2$4d min | Player is %3$s", name, time, isActiveStr);
			sender.sendMessage(msg);
		}
	}
	
	public static void disqualify(PlayerTime pt) {
		pt.player.sendMessage("Longest Survivor: You are out!\n");
		broadcast(pt.player.getName() + " is disqualified! They survived " + String.valueOf(pt.survivalTime) + " minutes." );
	}
	
	public static void resetPlayer(Player p) {
		p.sendMessage("Your Longest Survivor timer was resetted. Please reconnect to start again!\n");
		broadcast("Longest Survivor timer for " + p.getName() + " was resetted.");
	}
	
	public static void resetAll() {
		broadcast("Longest Survivor timer for all players was resetted. Plase reconnect to start again!");
	}
}
