package org.eclipse.jface.databinding;

public interface IReadable {

	/**
	 * Add the given change listener to this updatable. Has no effect if an
	 * identical listener is already registered.
	 * <p>
	 * Change listeners are informed about state changes that affect the value
	 * or structure of this updatable object.
	 * </p>
	 * 
	 * @param changeListener
	 */
	public void addChangeListener(IChangeListener changeListener);

	/**
	 * Removes a change listener from this updatable. Has no effect if an
	 * identical listener is not registered.
	 * @param changeListener 
	 */
	public void removeChangeListener(IChangeListener changeListener);
	
	/**
	 * Returns true iff this set is "stale" (that is, the 
	 * value is being recomputed asynchronously, and is likely to change soon).
	 * The reciever will fire a ChangeEvent.STALE event whenever the value
	 * of this method changes.
	 * 
	 * @return true iff this set is stale
	 */
	public boolean isStale();
	
}
