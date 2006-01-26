package org.eclipse.jface.databinding;


/**
 * Represents an ordered list.
 * 
 * Fires event types: ADD, REMOVE, STALE
 * 
 * @since 3.2
 */
public interface IReadableList extends IReadable {
	/**
	 * Returns the element in the list at the given index.
	 * 
	 * @TrackedGetter This method will notify UpdateTracker that the reciever has been read from
	 * 
	 * @param index
	 * @return the element at the given index
	 */
	public Object getElement(int index);
	
	/**
	 * Returns the size of the list.
	 * 
	 * @TrackedGetter This method will notify UpdateTracker that the reciever has been read from
	 * 
	 * @return the size
	 */
	public int getSize();
}
