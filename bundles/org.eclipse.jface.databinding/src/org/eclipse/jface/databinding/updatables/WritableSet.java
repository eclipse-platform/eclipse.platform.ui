package org.eclipse.jface.databinding.updatables;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.jface.databinding.AbstractUpdatableSet;
import org.eclipse.jface.databinding.IUpdatableSet;
import org.eclipse.jface.util.Assert;

/**
 * Represents a writable implementation of IReadableSet.
 */
public class WritableSet extends AbstractUpdatableSet implements IUpdatableSet {

	private HashSet data = new HashSet();
	
	/**
	 * Clears the elements in the reciever 
	 */
	public void clear() {
		Assert.isTrue(!isDisposed());
		
		data.clear();
	}

	/**
	 * Adds the given element to the set. Has no effect if the set already contains the element.
	 * 
	 * @param toAdd element to add
	 */
	public void add(Object toAdd) {
		addAll(Collections.singleton(toAdd));
	}
	
	/**
	 * Removes the given element from the set. Has no effect if the set does not contain the
	 * element
	 * 
	 * @param toRemove element to remove
	 */
	public void remove(Object toRemove) {
		removeAll(Collections.singleton(toRemove));
	}
	
	/**
	 * Adds the given elements to the reciever. Elements already contained in the reciever are ignored.
	 * 
	 * @param toAdd elements to add
	 */
	public void addAll(Collection toAdd) {
		Assert.isTrue(!isDisposed());
		Assert.isNotNull(toAdd);
		
		HashSet added = new HashSet();
		added.addAll(toAdd);
		added.removeAll(data);

		data.addAll(added);
		fireAdded(added);
	}
	
	/**
	 * Removes the given elements from the reciever. Elements not in the reciever are ignored.
	 * 
	 * @param toAdd elements to add
	 */	
	public void removeAll(Collection toRemove) {
		Assert.isNotNull(toRemove);
		Assert.isTrue(!isDisposed());

		HashSet removed = new HashSet();
		removed.addAll(toRemove);
		removed.retainAll(data);
		
		data.removeAll(removed);
		fireRemoved(removed);
	}
	
	protected Collection computeElements() {
		return data;
	}
}
