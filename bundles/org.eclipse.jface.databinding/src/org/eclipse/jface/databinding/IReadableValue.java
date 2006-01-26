package org.eclipse.jface.databinding;

/**
 * Updatable that returns a scalar. Unlike ILazyReadableValue, change notifications from
 * an IReadableValue will include the current state of the value. 
 *  
 * <p>
 * Fires events: CHANGE, STALE
 * </p>
 */
public interface IReadableValue extends IUpdatable {
	/**
	 * Returns the current value, which must be an instance of the value type
	 * returned by getValueType(). If the value type is an object type, then the
	 * returned value may be </code>null</code>. Fires a ChangeEvent.CHANGE event
	 * whenever the result of this method changes.
	 * 
	 * @TrackedGetter This method will notify UpdateTracker that the reciever has been read from
	 * 
	 * @return the current value
	 */
	public Object getValue();
}
