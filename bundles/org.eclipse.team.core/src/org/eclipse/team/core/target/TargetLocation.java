package org.eclipse.team.core.target;

/**
 * @version 	1.0
 * @author
 */
public abstract class TargetLocation {

	/*
	 * Initialize a new TargetLocation of the given type.
	 */
	 
	public TargetLocation() {
		super();
	}
	
	public abstract String getType();
	public abstract String encode();
}
