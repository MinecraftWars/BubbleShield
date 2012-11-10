package net.sqdmc.bubbleshield;

import org.bukkit.World;
import org.bukkit.block.Block;


public class ShieldBase {
	public final Block sponge;
	public final Block sign;
	public final Shield shield;
	public final World world;
	public int MaxPower;
	public final int x;
	public final int y;
	public final int z;
	public enum ShieldType { Faction, Player };
	public ShieldType type;
	
	@Override
	public int hashCode() {
		return (sponge == null) ? 0 : sponge.getLocation().hashCode();
	}

	public boolean equals(Object obj) {
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
		return this.type + "," + this.shield.getShieldOwner().getOwner() + "," + world.getName() + "," + x  + "," + y + "," + z;
	}
	
	@Override
	public String toString(){
		return world.getName() + ","+ x  + "," + y + "," + z;
	}
	
	
	public void setType(ShieldType type) {
		this.type = type;
	}
	
	public void setType(String string) {
		this.type = ShieldType.valueOf(string);		
	}
	
	public ShieldType getType() {
		return this.type;
	}
}
