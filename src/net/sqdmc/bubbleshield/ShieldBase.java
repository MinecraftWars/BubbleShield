package net.sqdmc.bubbleshield;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
//import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class ShieldBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final Block sponge;
	public final Block sign;
	public final Shield shield;
	public final World world;
	public int MaxPower;
	public final int x;
	public final int y;
	public final int z;  
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		//result = prime * result + ((sponge == null) ? 0 : sponge.getbgetBlock().hashCode());
		result = prime * result + ((sign == null) ? 0 : sign.getLocation().hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		// FIXME probably need to manually implement based on block locations
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ShieldBase other = (ShieldBase) obj;
		if (shield == null) {
			if (other.shield != null)
				return false;
		} else if (!shield.equals(other.shield))
			return false;
		if (sign == null) {
			if (other.sign != null)
				return false;
		} else if (!sign.equals(other.sign))
			return false;
		return true;
	}
	
	public ShieldBase(Block sponge, Block sign, Shield owner, World world, int x, int y, int z) {
		this.sponge = sponge;
		this.sign = sign;
		this.shield = owner;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.shield.addShield(this);
	}
	
	public int getShieldMaxPower()
	{
		return this.MaxPower;
	}
	
	public void setShieldMaxPower(int power) {
		this.MaxPower = power;
	}

	public void destroy() {
		sign.breakNaturally();
		sponge.breakNaturally();
	}
	
	public String getShieldBaseLocString() {
		return world.getName() + "," + x  + "," + y + "," + z;
	}
	
	public String getShieldBaseString(){
		return this.shield.getOwner().getId() + "," + world.getName() + "," + x  + "," + y + "," + z;
	}
	
	@Override
	public String toString(){
		return world.getName() + ","+ x  + "," + y + "," + z;
	}
}
