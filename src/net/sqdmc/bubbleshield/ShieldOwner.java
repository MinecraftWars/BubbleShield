package net.sqdmc.bubbleshield;

abstract class ShieldOwner {
	
	abstract public String getOwner();
		
	/** Send message to the account holder. */
	abstract public void sendMessage(String message);

	abstract public int hashCode();
	abstract public boolean equals(Object other);
	
	@Override
	abstract public String toString();
}
