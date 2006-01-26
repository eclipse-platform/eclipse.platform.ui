/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2005-2006. All Rights Reserved.
 * 
 * Note to U.S. Government Users Restricted Rights:
 * Use, duplication or disclosure restricted by GSA ADP Schedule
 * Contract with IBM Corp. 
 *******************************************************************************/
package org.eclipse.jface.databinding.updatables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jface.databinding.AbstractUpdatableSet;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IReadableSet;
import org.eclipse.jface.util.Assert;

/**
 * Represents a set consisting of the union of elements from one or more other sets.
 * This object does not need to be explicitly disposed. If nobody is listening to
 * the UnionSet, the set will remove its listeners.
 * 
 * @since 3.2
 */
public final class UnionSet extends AbstractUpdatableSet {

	/**
	 * Set of ISetWithListeners
	 */
	private HashSet childSets = new HashSet();
	
	/**
	 * Map of elements onto Integer reference counts. This map is constructed
	 * when the first listener is added to the union set. Null if nobody is 
	 * listening to the UnionSet.
	 */
	private HashMap refCounts = null;

	private IChangeListener childListener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			switch (changeEvent.getChangeType()) {
			case ChangeEvent.ADD_MANY:
				processAdds((Collection)changeEvent.getNewValue());
				break;
				
			case ChangeEvent.REMOVE_MANY:
				processRemoves((Collection)changeEvent.getNewValue());
				break;
				
			case ChangeEvent.STALE:
				processStale(((Boolean)changeEvent.getNewValue()).booleanValue());
				break;
			}
		}
	};

	private int staleCount = 0;
	
    protected void processStale(final boolean isStale) {
    	if (isStale) {
    		staleCount++;
    		
    		if (staleCount == 1) {
    			fireStale(true);
    		}
    	} else {
    		staleCount--;
    		
    		if (staleCount == 0) {
    			fireStale(false);
    		}
    	}
	}
    
    public boolean isStale() {

    	// If we have a cached value
    	if (refCounts != null) {
    		return (staleCount > 0);
    	}
    
    	IReadableSet[] children = (IReadableSet[]) childSets.toArray(new IReadableSet[childSets.size()]);
    	for (int i = 0; i < children.length; i++) {
			IReadableSet readableSet = children[i];
			
			if (readableSet.isStale()) {
				return true;
			}
		}
    	return false;
    }
	
	/**
	 * Adds a new set to this union
	 * 
	 * @param s set to add to the union
	 */
	public void add(IReadableSet s) {
		Assert.isTrue(!isDisposed());
		Assert.isNotNull(s);
		
		childSets.add(s);
		
		// If we're maintaining a cached result...
		if (refCounts != null) {
			initChild(s);
		}
	}
	
	/**
	 * Removes a set from this union
	 * 
	 * @param s set to remove from the union
	 */
	public void remove(IReadableSet s) {
		Assert.isTrue(!isDisposed());
		Assert.isNotNull(s);
		
		childSets.remove(s);
		
		if (refCounts != null) {
			deinitChild(s);
		}
	}

	private void processRemoves(Collection added) {
		ArrayList removes = new ArrayList();
		
		for (Iterator iter = added.iterator(); iter.hasNext();) {
			Object next = iter.next();
			
			Integer refCount = (Integer) refCounts.get(next);
			if (refCount != null) {
				int refs = refCount.intValue();
				if (refs <= 1) {
					removes.add(next);
					refCounts.remove(next);
				} else {
					refCount = new Integer(refCount.intValue() - 1);
					refCounts.put(next, refCount);
				}
			}
		}
		
		fireRemoved(removes);
	}

	protected void firstListenerAdded() {
		super.firstListenerAdded();
		
		refCounts = new HashMap();
		staleCount = 0;
		IReadableSet[] children = (IReadableSet[]) childSets.toArray(new IReadableSet[childSets.size()]);
		for (int i = 0; i < children.length; i++) {
			IReadableSet next = children[i];
			
			next.addChangeListener(childListener);
			if (next.isStale()) {
				staleCount++;
			}
			incrementRefCounts(next.toCollection());
		}
	}
	
	protected void lastListenerRemoved() {
		super.lastListenerRemoved();
		
		IReadableSet[] children = (IReadableSet[]) childSets.toArray(new IReadableSet[childSets.size()]);
		for (int i = 0; i < children.length; i++) {
			IReadableSet next = children[i];
				
			next.removeChangeListener(childListener);
		}
		refCounts = null;
	}
	
	private void initChild(IReadableSet child) {
		Collection added = child.toCollection();
		child.addChangeListener(childListener);
		if (child.isStale()) {
			processStale(true);
		}
		processAdds(added);
	}

	private void deinitChild(IReadableSet child) {
		Collection removed = child.toCollection();
		child.removeChangeListener(childListener);
		if (child.isStale()) {
			processStale(false);
		}
		processRemoves(removed);
	}
	
	private void processAdds(Collection added) {
		ArrayList adds = incrementRefCounts(added);
		
		fireAdded(adds);
	}

	private ArrayList incrementRefCounts(Collection added) {
		ArrayList adds = new ArrayList();
		
		for (Iterator iter = added.iterator(); iter.hasNext();) {
			Object next = iter.next();
			
			Integer refCount = (Integer) refCounts.get(next);
			if (refCount == null) {
				adds.add(next);
				refCount = new Integer(1);
				refCounts.put(next, refCount);
			} else {
				refCount = new Integer(refCount.intValue() + 1);
				refCounts.put(next, refCount);
			}
		}
		return adds;
	}

	protected Collection computeElements() {
		// If there is no cached value, compute the union from scratch
		if (refCounts == null) {
			HashSet result = new HashSet();
			
			for (Iterator iter = childSets.iterator(); iter.hasNext();) {
				IReadableSet next = (IReadableSet) iter.next();
				
				result.addAll(next.toCollection());
			}
			
			return result;
		}
		
		// Else there is a cached value. Return it.
		return refCounts.keySet();
	}
}
