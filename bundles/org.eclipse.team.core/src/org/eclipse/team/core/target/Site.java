package org.eclipse.team.core.target;

import org.eclipse.core.runtime.IPath;
import org.eclipse.team.core.TeamException;

public abstract class Site {

	/*
	 * Initialize a new Site of the given type.
	 */
	 
	public Site() {
		super();
	}
	
	public abstract TargetProvider newProvider(IPath intrasitePath) throws TeamException;
	
	public abstract String getType();
	
	public abstract String getDisplayName();
	
	public abstract String getUniqueIdentifier();
	
	public abstract String encode();
	
	public abstract IPath getPath();
	
	/**
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object other) {
		if(this == other) return true;
		if(! (other instanceof Site)) return false;
		Site location = (Site)other;
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
