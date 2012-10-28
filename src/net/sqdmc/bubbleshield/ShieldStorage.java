package net.sqdmc.bubbleshield;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

public class ShieldStorage {
	
	private String Id;
	private HashMap<ShieldOwner, Shield> shields; // = new HashMap<ShieldOwner, Shield>();
	private HashMap<Block, ShieldBase> blockShieldBase; // = new HashMap<Block, ShieldBase>();
	
	private Logger log = Bukkit.getServer().getLogger();

	public ShieldStorage()
	{
		Id = "0";
		shields = new HashMap<ShieldOwner, Shield>();
		blockShieldBase = new HashMap<Block, ShieldBase>();
	}
	
	public ShieldStorage(Map<String, Object> serialData){
		
	}
	
	public HashMap<ShieldOwner, Shield> getShields()
	{
		return this.shields;
	}
	
	public void setShields(HashMap<ShieldOwner, Shield> map){
		this.shields = map;
	}
	
	public HashMap<Block, ShieldBase> getBlockShieldBase()
	{
		return this.blockShieldBase;
	}
	
	public ShieldBase GetShieldBase(ShieldBase shieldbase)
	{
		return this.blockShieldBase.get(shieldbase);
	}
	
	public ArrayList<ShieldBase> GetShieldBases()
	{
		ArrayList<ShieldBase> shieldBases = null;;
		shieldBases = new ArrayList<ShieldBase>();
		
		log.info("[BubbleShield] : " + "GetShieldBases()");
		
		for (ShieldBase sb : blockShieldBase.values()) {
			shieldBases.add(sb);
			log.info("[BubbleShield] : " + "Owner: " + sb.shield.owner.getId().toString() + " Location: " + sb.getShieldBaseLocString());
		}
		
		return shieldBases;
	}
	
	public void setBlockShieldBase(HashMap<Block, ShieldBase> map){
		this.blockShieldBase = map;
	}
	
	public void addBlockShieldBase(Block signBlock, ShieldBase shieldbase){
		blockShieldBase.put(signBlock, shieldbase);
	}
	
	public void addShield(ShieldOwner owner,Shield shield){
		shields.put(owner,shield);
	}
	
	public void removeBlockShieldBase(Block block){
		blockShieldBase.remove(block);
	}
	
	public void removeShields(ShieldOwner owner){
		shields.remove(owner);
	}
	
	public boolean checkShieldExist(ShieldBase newShieldBase, ArrayList<ShieldBase> ShieldBases)
	{
		for (ShieldBase sb : blockShieldBase.values()) {
			if (sb.getShieldBaseLocString().equalsIgnoreCase(newShieldBase.getShieldBaseLocString())) {
			
				return true;
			}
		}
		
		if (getBlockShieldBase().containsKey(newShieldBase) || getBlockShieldBase().containsValue(newShieldBase) ) {
			return true;
		}
		return false;
	}
	
	public boolean checkShieldOwnerExist(ShieldOwner shieldOwner, ShieldBase newShieldBase)
	{
		if (shields.get(shieldOwner).getOwner().equals(newShieldBase.shield.getOwner())) {
			return true;
		}
		
		return false;
	}
}