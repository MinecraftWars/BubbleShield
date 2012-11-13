package net.sqdmc.bubbleshield;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ShieldOwnerPlayer extends ShieldOwner {

	private String shieldOwner;
	
	public ShieldOwnerPlayer(String name) {
		this.shieldOwner = name;
	}
	
	public ShieldOwnerPlayer(Player player) {
		this.shieldOwner = player.getName();
	}
	
	public ShieldOwnerPlayer(OfflinePlayer player) {
		this.shieldOwner = player.getName();
	}
	
	@Override
	public String getOwner() {
		return this.shieldOwner;
	}

	public void setPlayer(Player player){
		this.shieldOwner = player.getName();
	}

	@Override
	public void sendMessage(String message) {
		Player player = Bukkit.getPlayer(shieldOwner);
		
		if (player != null && player.isOnline()) {
			player.sendMessage(message);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((shieldOwner == null) ? 0 : shieldOwner.hashCode());
		return result;
	}

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ShieldOwnerPlayer other = (ShieldOwnerPlayer) obj;
        return shieldOwner.equals(other.shieldOwner);
    }
	
	@Override
	public String toString() {
		return this.shieldOwner;
	}
}
