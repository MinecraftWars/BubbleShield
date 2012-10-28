package net.sqdmc.bubbleshield;

import java.util.HashSet;
import java.util.Set;

public class Shield {
	
	private final Set<ShieldBase> storage = new HashSet<ShieldBase>();
	public final ShieldOwner owner;
	public int ShieldPower;
	public int ShieldPowerMax;
	public static int ShieldRadius = 16;
	
	public Shield(ShieldOwner owner) {
		this.owner = owner;
	}
	
	public void addShield(ShieldBase shieldBase) {
		this.storage.add(shieldBase);
	}
	
	public ShieldOwner getOwner(){
		return owner;
	}
	
	public int getShieldPowerMax()
	{
		return ShieldPowerMax;
	}
	
	public void setMaxShieldPower(int shieldMaxPowerMax)
	{
		this.ShieldPowerMax = shieldMaxPowerMax;
	}
	
	public Shield getShield()
	{
		return this;
	}
	
	public int getShieldPower()
	{
		return ShieldPower;
	}

	public void setShieldPower(int shieldpower)
	{
		this.ShieldPower = shieldpower;
	}
	
	@Override
	public String toString(){
		return this.owner.toString();
	}
}