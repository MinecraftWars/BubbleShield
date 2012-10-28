package net.sqdmc.bubbleshield;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.logging.Logger;

import net.sqdmc.bubbleshield.ShieldOwnerFaction;
import net.sqdmc.bubbleshield.Shield;
import net.sqdmc.bubbleshield.ShieldBase;
import net.sqdmc.bubbleshield.BubbleShield;
import net.sqdmc.bubbleshield.ShieldStorage;
import net.sqdmc.bubbleshield.ShieldTimer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.Faction;

public class ShieldListener implements Listener {
	
	private BubbleShield plugin;
	private Logger log = Bukkit.getServer().getLogger();
	
	BSConfiguration config;
	
	ShieldStorage shieldstorage;
	ArrayList<ShieldBase> ShieldBases; // initialize somehow
	
	private HashMap<Integer, Integer> ShieldDurability = new HashMap<Integer, Integer>();
	private HashMap<Integer, Timer> shieldTimer = new HashMap<Integer, Timer>();
	
	public ShieldListener(BubbleShield plugin) {
		this.plugin = plugin;
		config = plugin.getBSConfig();
		if (shieldstorage != null)
		{
			
		}
		else
		{
			log.info("[BubbleShield] : Creating new shieldstorage...");
			shieldstorage = new ShieldStorage();
		}
		
		ShieldBases = shieldstorage.GetShieldBases();
	}
	
	/* =============================================================================
	 * EXPLODE!
	 * 
	 */

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityExplode(EntityExplodeEvent event)
	{
	    if (event == null || event.isCancelled()) {
	        return;
	    }

		/*ShieldBases = shieldstorage.GetShieldBases();
	    
	    List<Block> blocks = event.blockList();
	    
	    for (Block block : blocks) {
	        // assumes set of shields is available
	        for (ShieldBase shieldBase : ShieldBases) {
	        	//log.info("[BubbleShield] : " + "ShieldBases[n]: " +shieldBase.getShieldBaseLocString());
	            if (blockProtected(block,shieldBase)) {
	            	log.info("[BubbleShield] : " + "Shield Damage Taken!" + block.getLocation().toString());
	                decreaseDurability(shieldBase);
	                if (!event.isCancelled()) event.setCancelled(true);
	                return;
	            }
	        }
	    }*/
	    
	    int radius = Shield.ShieldRadius;
	    final Entity detonator = event.getEntity();
		final Location detonatorLoc;

		if (detonator == null) {
			log.info("[BubbleShield] : " + "onEntityExplode() detonator Null!");
			detonatorLoc = event.getLocation();
			return;
		}
		else {	
			detonatorLoc = detonator.getLocation();
		}
	    
		/*for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					
					Location targetLoc;
					if (detonator != null)
						targetLoc = new Location(detonator.getWorld(), detonatorLoc.getX() + x, detonatorLoc.getY() + y, detonatorLoc.getZ() + z);
					else
						targetLoc = new Location(event.getLocation().getWorld(), event.getLocation().getX() + x, event.getLocation().getY() + y, event.getLocation().getZ() + z);
					
					log.info("[BubbleShield] : " + targetLoc.toString());
					
	            	ShieldBase shieldBase = Util.getShieldBase(targetLoc);
	            	if (shieldBase == null)
	            		return;
					
					if (detonatorLoc.distance(targetLoc) <= radius) {	
					
		            	log.info("[BubbleShield] : " + "Shield Damage Taken!");
		            			            	
		                decreaseDurability(shieldBase);
		                if (!event.isCancelled()) event.setCancelled(true);
		                return;
					}
					
					//if (!event.isCancelled()) event.setCancelled(true);
	                //return;
				}
			}*/
		
		// calculate sphere around detonator
		// calculate sphere around detonator
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					Location targetLoc;
					if (detonator != null) {
						targetLoc = new Location(detonator.getWorld(), detonatorLoc.getX() + x, detonatorLoc.getY() + y, detonatorLoc.getZ() + z);
						}
					else {
						targetLoc = new Location(event.getLocation().getWorld(), event.getLocation().getX() + x, event.getLocation().getY() + y, event.getLocation().getZ() + z);
					}
					
					if (detonatorLoc.distance(targetLoc) <= radius) {						
						Block signBlock = targetLoc.getBlock();
						if (signBlock.getType() == Material.SIGN || signBlock.getType() == Material.SIGN_POST){
							//log.info("TNT Exploded");
								
							Block ShieldBlock = signBlock.getRelative(BlockFace.DOWN);
							if (ShieldBlock.getType() == Material.SPONGE) {				
								Faction faction = Board.getFactionAt(targetLoc);
									
								Sign s = (Sign) signBlock.getState();
								String shi = s.getLine(0);
								//String p = s.getLine(1);
								//String fp = s.getLine(1);
								int maxpower = Integer.parseInt(s.getLine(2));
								int pow = Integer.parseInt(s.getLine(3));

								if (!shi.equalsIgnoreCase("[shield]"))
								{
								    return;
								}							    						   
									    
								ShieldOwnerFaction fSheildowner = new ShieldOwnerFaction(faction);
								Shield shield = new Shield(fSheildowner);
								ShieldBase shieldBase = new ShieldBase(ShieldBlock, signBlock, shield, targetLoc.getWorld(), targetLoc.getBlockX(), targetLoc.getBlockY() -1, targetLoc.getBlockZ());
								shieldBase.setShieldMaxPower(maxpower);
								
								//String shieldlocation = targetLoc.getWorld().getName() + "," + targetLoc.getBlockX() + "," + targetLoc.getBlockY() + "," + targetLoc.getBlockZ();
								
								ShieldBases = shieldstorage.GetShieldBases();
								
								//log.info("[BubbleShield] : onEntityExplode() " + shieldBase.getShieldBaseLocString());
								boolean bShieldExists = shieldstorage.checkShieldExist(shieldBase, ShieldBases);
								
								if (!bShieldExists)
								{
									shieldBase.destroy();
									TNTBreakShield(ShieldBlock);
									log.info("[BubbleShield] : Destroyed sbandoned ShieldBase at " + shieldBase.getShieldBaseLocString());
									return;
								}
								
								if (bShieldExists)
								{					
									Integer representation = targetLoc.getWorld().hashCode() + targetLoc.getBlockX() * 2389 + targetLoc.getBlockY() * 4027 + targetLoc.getBlockZ() * 2053;						
										
									pow--;
									shield.setShieldPower(pow);
											
									if (pow <= 0) {
										TNTBreakShield(ShieldBlock);
										return;
									}
									if (pow > 0){
										String newpower = String.valueOf(pow);
										s.setLine(3, newpower);
										s.update();
										shield.owner.sendMessage("Shield Power at " + newpower + "!");

										if (ShieldDurability.containsKey(representation)) {
											int currentDurability = (int) ShieldDurability.get(representation);
											currentDurability++;
											
											if (checkIfMax(currentDurability)) {
												// counter has reached max durability
												//log.info("[BubbleShield] : onEntityExplode() " + "Hit Max Shield Dura");
												TNTBreakShield(ShieldBlock);
												faction.setPowerLoss(0);
												ResetTime(representation, targetLoc);
												return;
											} else {
												// counter has not reached max durability yet
												ShieldDurability.put(representation, currentDurability);
												//log.info("[BubbleShield] : onEntityExplode() " + "Set already, set Shield Dura");
												
												startNewTimer(representation, shieldBase);
											}
										} else 
										{
											ShieldDurability.put(representation, 1);
											//log.info("[BubbleShield] : onEntityExplode() " + "Set New Shield Dura");
											shieldBase.setShieldMaxPower(pow);
											startNewTimer(representation, shieldBase);

											if (checkIfMax(1)) {
												TNTBreakShield(ShieldBlock);
												ResetTime(representation, targetLoc);
												//log.info("[BubbleShield] : onEntityExplode() " + "Hit Max");
												return;
											}
										}
									}
								}
											
								event.setCancelled(true);
								return;
							
							}
						}
					}
				}
			}								
		}
		
	}
	
	@SuppressWarnings("unused")
	private boolean CheckShield(ShieldBase shieldbase) {
		if (shieldbase == null)
			return false;
		ShieldBases = shieldstorage.GetShieldBases();
		if (shieldstorage.checkShieldExist(shieldbase, ShieldBases))
			return true;
		
		return false;
	}

	/** 
	    is block protected by a given shieldbase?
	*/
	@SuppressWarnings("unused")
	private boolean blockProtected(Block block, ShieldBase shieldBase) {
	    int radius = Shield.ShieldRadius;
	    
	    // first linear checks
	    if (!block.getWorld().equals(shieldBase.world)) return false;
		if (Math.abs(block.getX() - shieldBase.x)>radius  || Math.abs(block.getY() - shieldBase.y)>radius || Math.abs(block.getZ() - shieldBase.z)>radius) {
	        return false;
	    }
	    
	    Location shieldLoc = new Location(shieldBase.world, shieldBase.x, shieldBase.y, shieldBase.z);
	    double distSquared = block.getLocation().distanceSquared(shieldLoc);
	    
	    return radius*radius <= distSquared;
	}

	@SuppressWarnings("unused")
	private void decreaseDurability(ShieldBase shieldBase) {
	    // manage durability stuff
		Location shieldLoc = new Location(shieldBase.world, shieldBase.x, shieldBase.y, shieldBase.z);
		
		Block signBlock = shieldLoc.getBlock();
		//Block shieldBlock = shieldLoc.getBlock();
		
		Block ShieldBlock = signBlock.getRelative(BlockFace.DOWN);
		if (ShieldBlock.getType() == Material.SPONGE) {				
			Faction faction = Board.getFactionAt(shieldLoc);
			
		    Sign s = (Sign) signBlock.getState();
		    String shi = s.getLine(0);
		    //String p = s.getLine(1);
		    //String fp = s.getLine(1);
		    int maxpower = Integer.parseInt(s.getLine(2));
		    int pow = Integer.parseInt(s.getLine(3));

		    if (!shi.equalsIgnoreCase("[shield]"))
		    {
		    	return;
		    }	
		    
		    ShieldOwnerFaction fSheildowner = new ShieldOwnerFaction(faction);
			Shield shield = new Shield(fSheildowner);
			//ShieldBase shieldBase = new ShieldBase(ShieldBlock, signBlock, shield, targetLoc.getWorld(), targetLoc.getBlockX(), targetLoc.getBlockY(), targetLoc.getBlockZ());
			shieldBase.setShieldMaxPower(maxpower);
			
			
			String shieldlocation = shieldLoc.getWorld().getName() + "," + shieldLoc.getBlockX() + "," + shieldLoc.getBlockY() + "," + shieldLoc.getBlockZ();
			
			if (shieldBase.getShieldBaseLocString().equals(shieldlocation))
			{					
				//Integer representation = targetLoc.getWorld().hashCode() + targetLoc.getBlockX() * 2389 + targetLoc.getBlockY() * 4027 + targetLoc.getBlockZ() * 2053;						
				
				pow--;
				shield.setShieldPower(pow);
				
				if (pow <= 0)
					TNTBreakShield(ShieldBlock);
				if (pow > 0){
					String newpower = String.valueOf(pow);
					s.setLine(3, newpower);
					s.update();
					shield.owner.sendMessage("Shield Power at " + newpower + "!");
				}
				else
					TNTBreakShield(ShieldBlock);
			}
		}
	}
	
	
	/* ================================================================
	 *  CREATE
	 * 
	 * ================================================================ */
	@EventHandler
	public void createShield(SignChangeEvent event) {
		Player player = event.getPlayer();
		Faction faction = Board.getFactionAt(event.getBlock().getLocation());
					
		ShieldOwnerFaction fshieldowner;
		
		String line0 = event.getLine(0);
		String line1 = event.getLine(1);
		String shieldPower = line1;
		if (line0.equalsIgnoreCase("[shield]") && (line1 != null && !line1.equals("") )) {		
			fshieldowner = new ShieldOwnerFaction(faction);
			event.setLine(1, faction.getTag());
			event.setLine(2, shieldPower);
			event.setLine(3, shieldPower);
		} else return; // not for us!
		
		Block signBlock = event.getBlock();
		Block ShieldBlock = signBlock.getRelative(BlockFace.DOWN);
		
		if (!Util.isNumeric(shieldPower)) {
			fshieldowner.sendMessage("Shield Power must be a number on the second line.");
			signBlock.breakNaturally();
			return;
		}
		if (Integer.parseInt(shieldPower) < 1) {
			fshieldowner.sendMessage("Shield must have a power of greater than zero.");
			signBlock.breakNaturally();
			return;
		}
		if (faction.getId().equals("-2") || faction.getId().equals("-1")  || faction.getId().equals("0") ) {
			fshieldowner.sendMessage("You can only create Shields in land you own.");
			signBlock.breakNaturally();
			return;
		}
		
		if (ShieldBlock.getType() == Material.SPONGE) {
			
			Block Sponge = (Block)ShieldBlock;	
			
			if (Integer.parseInt(shieldPower) > config.getMaxPowerCost() || Integer.parseInt(shieldPower) > faction.getPower())
			{
				//log.info("[BubbleShield] : " + "Not enough power to create Shield for player "+ player.getName());
				fshieldowner.sendMessage("Not enough power to create Shield!");
				signBlock.breakNaturally();
				Sponge.breakNaturally();
				return;			
			}
					
			if (shieldstorage.getBlockShieldBase() != null){
				if (shieldstorage.getShields().containsKey(fshieldowner)){
					//log.info("[BubbleShield] : " + fshieldowner.getId() + " Already has a shield!");
					fshieldowner.sendMessage("You already have a Shield.");
					Sponge.breakNaturally();
					return;
				}
			}
			
			Shield shield = Util.getShield(fshieldowner, shieldstorage);
			
			shield.setShieldPower(Integer.parseInt(shieldPower));
			shield.setMaxShieldPower(Integer.parseInt(shieldPower));
						
			ShieldBase shieldbase = new ShieldBase(Sponge, signBlock, shield, ShieldBlock.getWorld(),ShieldBlock.getX(),ShieldBlock.getY(),ShieldBlock.getZ());
			
			shieldbase.setShieldMaxPower( Integer.parseInt(shieldPower));
			
			shieldstorage.addBlockShieldBase(signBlock, shieldbase);
			shieldstorage.addBlockShieldBase(ShieldBlock, shieldbase);
			
			faction.setPowerLoss(-Integer.parseInt(shieldPower));
		
			log.info("[BubbleShield] : " + "Shield created by "+ player.getName() + " At location: " + shieldbase.getShieldBaseLocString() + " For Faction: " + shield.getOwner().getId());
			fshieldowner.sendMessage("Shield Created");
			
			try {
				config.SaveShieldsToFile();
				config.loadDurabilityFromFile();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/*
	 * DESTROY
	 */
	
	@EventHandler
	public void shieldBroken(BlockBreakEvent event) {
		Block block = event.getBlock();
		ShieldBase shieldBase = shieldstorage.getBlockShieldBase().get(block);
		if (shieldBase != null) {
			Shield shield = shieldBase.shield;
			shieldBase.destroy();
			ShieldOwnerFaction fShieldOwner = new ShieldOwnerFaction(Board.getFactionAt(block));
			shieldstorage.removeShields(fShieldOwner);
			shieldstorage.removeBlockShieldBase(shieldBase.sponge);
			shieldstorage.removeBlockShieldBase(shieldBase.sign);
			
			Faction faction = Board.getFactionAt(event.getBlock().getLocation());
			faction.setPowerLoss(0);
			
			shield.owner.sendMessage("Shield Destroyed!");
			
			try {
				config.SaveShieldsToFile();
				config.loadDurabilityFromFile();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void TNTBreakShield(Block shieldblock)
	{
		ShieldBase shieldBase = shieldstorage.getBlockShieldBase().get(shieldblock);	
		if (shieldBase != null) {
			Shield shield = shieldBase.shield;
			ShieldOwnerFaction fShieldOwner = new ShieldOwnerFaction(Board.getFactionAt(shieldblock));
			shieldstorage.removeShields(fShieldOwner);
			shieldstorage.removeBlockShieldBase(shieldBase.sponge);
			shieldstorage.removeBlockShieldBase(shieldBase.sign);
			shieldBase.destroy();
			
			int explosionpower = Math.round(shieldBase.shield.getShieldPowerMax() / 4);
			//log.info("Explosion Power: " + explosionpower + "  MaxShield: " + shieldBase.shield.getShieldPowerMax());
			
			if (explosionpower >= 11)
				explosionpower = 11;
			else if (explosionpower <= 2)
				explosionpower = 2;
			
			log.info("[BubbleShield] : " + "Shield exploded with Explosion Power of: " + explosionpower + " For Faction " + shieldBase.shield.owner.getId());
			
			shieldblock.getWorld().createExplosion(shieldblock.getLocation(), explosionpower, true);
			shieldblock.breakNaturally();
			
			Faction faction = Board.getFactionAt(shieldblock);
			shield.owner.sendMessage("Shield Destroyed!");
			
			faction.setPowerLoss(0);
			
			try {
				config.SaveShieldsToFile();
				config.loadDurabilityFromFile();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	
	/* =========================================================================
	 *  TIMERS
	 * 
	 * 
	 */
	 
	private void startNewTimer(Integer representation, ShieldBase shieldbase) {
		if (shieldTimer.get(representation) != null) {
			shieldTimer.get(representation).cancel();
		}

		Timer timer = new Timer();
		timer.schedule(new ShieldTimer(plugin, representation, shieldbase), config.getRegenTime());

		shieldTimer.put(representation, timer);
	}
	
	private boolean checkIfMax(int value) {
		return value == config.getMaxPowerCost();
	}
	
	private void ResetTime(Integer representation, Location at) {
		ShieldDurability.remove(representation);

		if (shieldTimer.get(representation) != null) {
			shieldTimer.get(representation).cancel();
		}

		shieldTimer.remove(representation);
	}
	
	/**
	 * Returns the HashMap containing all saved durabilities.
	 * 
	 * @return the HashMap containing all saved durabilities
	 */
	public HashMap<Integer, Integer> getShieldDurability() {
		return ShieldDurability;
	}
	
	/**
	 * Sets the HashMap containing all saved durabilities.
	 * 
	 * @param map
	 *            the HashMap containing all saved durabilities
	 */
	public void setShieldDurability(HashMap<Integer, Integer> map) {
		if (map == null) {
			return;
		}

		ShieldDurability = map;
	}

	/**
	 * Returns the HashMap containing all saved durability timers.
	 * 
	 * @return the HashMap containing all saved durability timers
	 */
	public HashMap<Integer, Timer> getObsidianTimer() {
		return shieldTimer;
	}

	/**
	 * Sets the HashMap containing all saved durability timers.
	 * 
	 * @param map
	 *            the HashMap containing all saved durability timers
	 */
	public void setShieldTimer(HashMap<Integer, Timer> map) {
		if (map == null) {
			return;
		}

		shieldTimer = map;
	}

	public HashMap<ShieldOwner, Shield> getShields() {
		return shieldstorage.getShields();
	}
	
	public void setShields(HashMap<ShieldOwner, Shield> map) {
		if (map == null) {
			return;
		}
		
		shieldstorage.setShields(map);
	}

	public HashMap<Block, ShieldBase> getShieldsBase() {
		return shieldstorage.getBlockShieldBase();
	}
	
	public void setShieldBase(HashMap<Block, ShieldBase> map) {
		if (map == null) {
			return;
		}

		shieldstorage.setBlockShieldBase(map);
	}
}
