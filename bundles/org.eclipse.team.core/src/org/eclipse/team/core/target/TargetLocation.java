package org.eclipse.team.core.target;

import org.eclipse.core.runtime.IPath;
import org.eclipse.team.core.TeamException;

public abstract class TargetLocation {

	/*
	 * Initialize a new TargetLocation of the given type.
	 */
	 
	public TargetLocation() {
		super();
	}
	
	public abstract TargetProvider newProvider(IPath path) throws TeamException;
	
	public abstract String getType();
	
	public abstract String getDisplayName();
	
	public abstract String getUniqueIdentifier();
	
	public abstract String encode();
	
	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object other) {
		if(this == other) return true;
		if(! (other instanceof TargetLocation)) return false;
		TargetLocation location = (TargetLocation)other;
		return getType().equals(location.getType()) && 
				getUniqueIdentifier().equals(location.getUniqueIdentifier());
	}
	
	/**
	 * @see Object#toString()
	 */
	public String toString() {
		return getType() + ":" + getUniqueIdentifier();
	}
}
