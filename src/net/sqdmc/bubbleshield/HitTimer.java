package net.sqdmc.bubbleshield;

import java.util.TimerTask;

public class HitTimer extends TimerTask  {
	
	private BubbleShield plugin;

	public HitTimer(BubbleShield plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		plugin.getListener().shieldstorage.affectedBlockCount = 0;
	}

}
