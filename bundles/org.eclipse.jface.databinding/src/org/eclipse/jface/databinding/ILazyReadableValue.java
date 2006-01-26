package org.eclipse.jface.databinding;

/**
 * Updatable that returns a scalar. This differs from IReadableValue in that
 * it only sends the DIRTY event but will not necessarily provide any information
 * about what changed in the event object. 
 * 
 * <p>
 * This permits a more efficient implementation where the value is computed lazily 
 * in the getter, but listeners must be prepared to call getValue() if they need to
 * know the object's current value.
 * </p>
 * 
 * <p>
 * Not intended to be implemented by clients. Clients should subclass the abstract class
 * LazyUpdatableValue.
 * </p>
 * 
 * <p>
 * Fires events: DIRTY, STALE
 * </p>
 * 
 * @since 3.2
 */
public interface ILazyReadableValue extends IReadableValue {
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
