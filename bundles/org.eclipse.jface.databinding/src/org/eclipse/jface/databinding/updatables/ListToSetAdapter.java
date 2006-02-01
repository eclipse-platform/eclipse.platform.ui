package org.eclipse.jface.databinding.updatables;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.jface.databinding.AbstractUpdatableSet;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IReadableList;
import org.eclipse.jface.util.Assert;

/**
 * Converts an IReadableList into an IReadableSet
 * 
 * @since 3.2
 */
public class ListToSetAdapter extends AbstractUpdatableSet {

	private IReadableList input;
	
	/**
	 * Cached value. Null if there is no cached value. The cache is only maintained 
	 * if this object has at least one listener.
	 */
	private HashSet elements;
	
	private IChangeListener listener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			switch(changeEvent.getChangeType()) {
			case ChangeEvent.CHANGE:
				if(changeEvent.getPosition() == ChangeEvent.POSITION_UNKNOWN) {
					HashSet oldElements = elements;
					doComputeElements();
					HashSet addedElements = new HashSet(elements);
					HashSet removedElements = new HashSet(oldElements);
					addedElements.removeAll(oldElements);
					removedElements.removeAll(elements);
					fireRemoved(removedElements);
					fireAdded(addedElements);
				}
				break;
			case ChangeEvent.ADD:
				Object added = changeEvent.getNewValue();
				if (!elements.contains(added)) {
					elements.add(added);
					fireAdded(Collections.singleton(added));
				}
				break;
			case ChangeEvent.REMOVE:
				Object removed = changeEvent.getNewValue();
				if (elements.contains(removed)) {
					elements.remove(removed);
					fireRemoved(Collections.singleton(removed));
				}
				break;
				
			case ChangeEvent.STALE:
				fireStale(input.isStale());
				break;
			}
		}
	};
	
	public ListToSetAdapter(IReadableList input) {
		Assert.isNotNull(input);
		this.input = input;
	}
	
	public boolean isStale() {
		return input.isStale();
	}
	
	protected Collection computeElements() {
		// If there is a cached value return it
		if (elements != null) {
			return elements;
		}
		
		// Else recompute the value from scratch
		return doComputeElements();
	}

	protected void firstListenerAdded() {
		input.addChangeListener(listener);
		elements = doComputeElements();
		super.firstListenerAdded();
	}
	
	protected void lastListenerRemoved() {
		input.removeChangeListener(listener);
		elements = null;
		super.lastListenerRemoved();
	}
	
	private HashSet doComputeElements() {
		elements = new HashSet();
		for (int idx = 0; idx < input.getSize(); idx++) {
			elements.add(input.getElement(idx));
		}
		return elements;
	}
}
