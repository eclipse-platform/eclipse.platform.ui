package org.eclipse.team.core.target;

/**
 * @version 	1.0
 * @author
 */
public interface ITargetFactory {
	
	/**
	 * Return a new target location for the given encoded description.
	 */	
	public TargetLocation decode(String serializedLocation);

	/**
	 * Return a new target provider for the given location.
	 */	
	public TargetProvider newProvider(TargetLocation location);

}
