package net.sqdmc.bubbleshield;

import java.util.HashMap;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

public final class ShieldTimer extends TimerTask {
	
	private BubbleShield plugin;
	private final Integer duraID;
	private ShieldBase shieldbase;
	
	private Logger log = Bukkit.getServer().getLogger();
	
	public ShieldTimer(BubbleShield plugin, Integer duraID, ShieldBase shieldbase) {
		this.plugin = plugin;
		this.duraID = duraID;
		this.shieldbase = shieldbase;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		resetDurability(duraID, shieldbase);		
	}

	private void resetDurability(Integer id, ShieldBase shieldbase) {
		if (id == null) {
			return;
		}

		HashMap<Integer, Integer> map = plugin.getListener().getShieldDurability();
		
		if (map == null) {
			return;
		}

		//log.info(shieldbase.toString());
		//log.info(id.toString());
		
		Util.RegenPowerLoss(shieldbase);
		
		map.remove(id);
	}
}
