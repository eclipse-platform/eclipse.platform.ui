package org.eclipse.core.runtime;

public interface IBundleGroupProvider {
	/**
	 * Returns the human-readable name of this bundle group provider.
	 * @return the name of this bundle group provider
	 */
	public String getName();
	
	/**
	 * Returns the bundle groups provided by this provider.
	 * @return the bundle groups provided by this provider
	 */
	public IBundleGroup[] getBundleGroups();
}