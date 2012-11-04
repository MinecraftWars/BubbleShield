package net.sqdmc.bubbleshield;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	ArrayList<ShieldBase> ShieldBases;
	
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

		ShieldBases = shieldstorage.GetShieldBases();
	    
	    List<Block> blocks = event.blockList();
	    
	    for (Block block : blocks) {
	        // assumes set of shields is available
	        for (ShieldBase shieldBase : ShieldBases) {
	        	
	        	boolean bShieldExists = shieldstorage.GetShieldBases().contains(shieldBase);
				if (!bShieldExists)
				{
					shieldBase.destroy();
					log.info("[BubbleShield] : Destroyed sbandoned ShieldBase at " + shieldBase.getShieldBaseLocString());
					return;
				}
				
	            if (blockProtected(block,shieldBase) && shieldstorage.affectedBlockCount < config.getAffectedBlockCountMax()) {
	            	//log.info("[BubbleShield] : " + "Shield Damage Taken! " + shieldBase.getShieldBaseLocString());
	                decreaseDurability(shieldBase);
	                
	                shieldstorage.affectedBlockCount++;
	                
	                Timer timer = new Timer();
	        		timer.schedule(new HitTimer(plugin), 500);
	        		
	                if (!event.isCancelled()) event.setCancelled(true);
	                return;
	            }
	            else if (blockProtected(block,shieldBase)) {
	            	if (!event.isCancelled()) event.setCancelled(true);
	                return;
	            }
	        }
	    }
		
	}
	
	/** 
	    is block protected by a given shieldbase?
	*/
	private boolean blockProtected(Block block, ShieldBase shieldBase) {
		double radius = config.getShieldRadius();
	    
	    // first linear checks
	    if (!block.getWorld().getName().equals(shieldBase.world.getName())) return false;
		if (Math.abs(block.getX() - shieldBase.x)>radius  || Math.abs(block.getY() - shieldBase.y)>radius || Math.abs(block.getZ() - shieldBase.z)>radius) {
	        return false;
	    }
	    
	    Location shieldLoc = new Location(shieldBase.world, shieldBase.x, shieldBase.y, shieldBase.z);
	    double distSquared = shieldLoc.distanceSquared(block.getLocation());
	    
	    return radius*radius >= distSquared;
	}

	private void decreaseDurability(ShieldBase shieldBase) {
	    // manage durability stuff
		Location signLoc = new Location(shieldBase.world, shieldBase.x, (shieldBase.y+1), shieldBase.z);
		Location spongeLoc = new Location(shieldBase.world, shieldBase.x, (shieldBase.y), shieldBase.z);
		
		Block signBlock = signLoc.getBlock();
		Block ShieldBlock = spongeLoc.getBlock(); //signBlock.getRelative(BlockFace.DOWN);
		
		//log.info("[BubbleShield] : decreaseDurability() " + signLoc.toString() + " " + signBlock.getType());
		
		if ((signBlock.getType() == Material.SIGN || signBlock.getType() == Material.SIGN_POST || signBlock.getType() == Material.WALL_SIGN ) && ShieldBlock.getType() == Material.SPONGE){			
			Faction faction = Board.getFactionAt(signLoc);
					
			Sign s = (Sign) signBlock.getState();
			String shi = s.getLine(0);
			int maxpower = Integer.parseInt(s.getLine(2));
			int pow = Integer.parseInt(s.getLine(3));

			if (!shi.equalsIgnoreCase("[shield]"))
			{
			    return;
			}							    						   
					    
			ShieldOwnerFaction fSheildowner = new ShieldOwnerFaction(faction);
			Shield shield = new Shield(fSheildowner);
			shieldBase.setShieldMaxPower(maxpower);

			ShieldBases = shieldstorage.GetShieldBases();
				
			//log.info("[BubbleShield] : decreaseDurability() " + spongeLoc.toString());
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
				Integer representation = spongeLoc.getWorld().hashCode() + spongeLoc.getBlockX() * 2389 + spongeLoc.getBlockY() * 4027 + spongeLoc.getBlockZ() * 2053;						
						
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
							ResetTime(representation, spongeLoc);
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
							ResetTime(representation, spongeLoc);
							//log.info("[BubbleShield] : onEntityExplode() " + "Hit Max");
							return;
						}
					}
					
				}
				
				return;
			}
		}
	
	}
	
	/* ================================================================
	 *  REGEN
	 * 
	 * ================================================================ */
	
	public void RegenPowerLoss(ShieldBase shieldBase)
	{
		if (shieldBase != null) {
			Sign sign = null;
			Location signlocation = new Location(shieldBase.world, shieldBase.x, (shieldBase.y+1), shieldBase.z);
			Location sponglocation = new Location(shieldBase.world, shieldBase.x, (shieldBase.y), shieldBase.z);
			
			try {
				sign = (Sign) signlocation.getBlock().getState();
			} catch (java.lang.ClassCastException e) {
				return;
			}
			
			int max = Integer.parseInt(sign.getLine(2));
			int currentpower = Integer.parseInt(sign.getLine(3));

			if ( currentpower < max ) {
				String newpower = String.valueOf(currentpower+1);	
				shieldBase.shield.setShieldPower(currentpower+1);
				sign.setLine(3, newpower);
				sign.update();
				shieldBase.shield.owner.sendMessage("Shield Power at " + newpower + "!");
				
				Integer representation = shieldBase.world.hashCode() + shieldBase.x * 2389 + shieldBase.y * 4027 + shieldBase.z * 2053;						
							
				if (ShieldDurability.containsKey(representation)) {
					int currentDurability = (int) ShieldDurability.get(representation);
					currentDurability++;
					
					if (checkIfMax(currentDurability)) {
						// counter has reached max durability
						//log.info("[BubbleShield] : onEntityExplode() " + "Hit Max Shield Dura");
						ResetTime(representation, sponglocation);
						return;
					} else {
						// counter has not reached max durability yet
						ShieldDurability.put(representation, currentDurability);
						//log.info("[BubbleShield] : onEntityExplode() " + "Set already, set Shield Dura");
						
						startNewTimer(representation, shieldBase);
					}
				} else {
					ShieldDurability.put(representation, 1);
					//log.info("[BubbleShield] : onEntityExplode() " + "Set New Shield Dura");
					startNewTimer(representation, shieldBase);

					if (checkIfMax(1)) {
						ResetTime(representation, sponglocation);
						//log.info("[BubbleShield] : onEntityExplode() " + "Hit Max");
						return;
					}
				}
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
        	if (!player.hasPermission("bubbleshield.create")) {
        		player.sendMessage("You do not have permission to create this shield.");
        		return;
        	}
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
			player.sendMessage("You can only create Shields in land you own.");
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
			
			int shieldCount = Util.getShieldCount(shieldstorage, fshieldowner.getId());
			if (shieldstorage.getBlockShieldBase() != null){
				if (shieldCount > config.getMaxShieldCount()){
					//log.info("[BubbleShield] : " + fshieldowner.getId() + " Has the maximum amount of Shields.!");
					fshieldowner.sendMessage("You have the maximum amount of Shields.");
					Sponge.breakNaturally();
					return;
				}
				else if (shieldCount == 0) {
					faction.setPowerLoss(0);
				}
			}
			
			Shield shield = Util.getShield(fshieldowner, shieldstorage);
			
			shield.setShieldPower(Integer.parseInt(shieldPower));
			shield.setMaxShieldPower(Integer.parseInt(shieldPower));
						
			ShieldBase shieldbase = new ShieldBase(Sponge, signBlock, shield, ShieldBlock.getWorld(),ShieldBlock.getX(),ShieldBlock.getY(),ShieldBlock.getZ());
			
			shieldbase.setShieldMaxPower(Integer.parseInt(shieldPower));
			
			shieldstorage.addBlockShieldBase(signBlock, shieldbase);
			shieldstorage.addBlockShieldBase(ShieldBlock, shieldbase);
			
			faction.addPowerLoss(-Integer.parseInt(shieldPower));
		
			log.info("[BubbleShield] : " + "Shield created by "+ player.getName() + " At location: " + shieldbase.getShieldBaseLocString() + " For Faction: " + shield.getOwner().getId() + " With power of: " + shieldPower);
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
			Player player = event.getPlayer();
			
			Location signLoc = new Location(shieldBase.world, shieldBase.x, (shieldBase.y+1), shieldBase.z);
			Block signBlock = signLoc.getBlock();
			
			Sign sign = (Sign) signBlock.getState();
			int maxpower = Integer.parseInt(sign.getLine(2));
			int pow = Integer.parseInt(sign.getLine(3));
			
			if (pow != maxpower) {
				//shield.owner.sendMessage("Can not break shield unless it is fully charged!");
				player.sendMessage("Can not break shield unless it is fully charged!");
				event.setCancelled(true);
				return;
			}
			
			shieldBase.destroy();
			ShieldOwnerFaction fShieldOwner = new ShieldOwnerFaction(Board.getFactionAt(block));
			shieldstorage.removeShields(fShieldOwner);
			shieldstorage.removeBlockShieldBase(shieldBase.sponge);
			shieldstorage.removeBlockShieldBase(shieldBase.sign);
			
			Faction faction = Board.getFactionAt(event.getBlock().getLocation());
			faction.addPowerLoss(maxpower);
			
			shield.owner.sendMessage("Shield Destroyed!");
			
			try {
				config.SaveShieldsToFile();
				config.LoadShieldFromFile();
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
			
			Sign sign = (Sign) shieldBase.sign.getState();
			faction.addPowerLoss(Integer.parseInt(sign.getLine(2)));
			
			try {
				config.SaveShieldsToFile();
				config.LoadShieldFromFile();
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
