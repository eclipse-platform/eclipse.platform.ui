package org.eclipse.jface.internal.databinding.api.observable;

/**
 * @since 3.2
 * 
 */
public interface IObservable {

	/**
	 * @param listener
	 */
	public void addChangeListener(IChangeListener listener);

	/**
	 * @param listener
	 */
	public void removeChangeListener(IChangeListener listener);

	/**
	 * @param listener
	 */
	public void addStaleListener(IStaleListener listener);

	/**
	 * @param listener
	 */
	public void removeStaleListener(IStaleListener listener);

	/**
	 * @return true if this observable's state is stale and will change soon.
	 * 
	 * @TrackedGetterO
	 */
	public boolean isStale();
	
	/**
	 * 
	 */
	public void dispose();
}
