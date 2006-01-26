package org.eclipse.jface.databinding;

import java.util.Collection;

/**
 * Represents an unordered set.
 * 
 * Fires event types: ADD_MANY, REMOVE_MANY, and STALE.
 * IReadableSet fires all additions and removals using the *_MANY events. Listeners can
 * safely ignore the ADD, REMOVE, and REPLACE events without loss of data. 
 * 
 * @since 3.2
 *
 */
public interface IReadableSet extends IReadable {
	/**
	 * Returns the elements in the set as a Java collection
	 * 
	 * @TrackedGetter This method will notify UpdateTracker that the reciever has been read from
	 * 
	 * @return the elements in the set as a (possibly empty) Java collection. Never null. Will not
	 * contain duplicates.
	 */
	public Collection toCollection();
}
