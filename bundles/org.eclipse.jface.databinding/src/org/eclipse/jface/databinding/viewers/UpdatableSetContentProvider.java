/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2005-2006. All Rights Reserved. 
 * 
 * Note to U.S. Government Users Restricted Rights:  Use, 
 * duplication or disclosure restricted by GSA ADP Schedule 
 * Contract with IBM Corp.
 *******************************************************************************/
package org.eclipse.jface.databinding.viewers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.databinding.AbstractUpdatableSet;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IReadableSet;
import org.eclipse.jface.databinding.updatables.EmptyReadableSet;
import org.eclipse.jface.viewers.AbstractListViewer;
import org.eclipse.jface.viewers.AcceptAllFilter;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public final class UpdatableSetContentProvider implements IStructuredContentProvider {
	
    private IReadableSet readableSet = EmptyReadableSet.getInstance();

    private Viewer viewer;

    private IFilter filter = AcceptAllFilter.getInstance();

    private class KnownElementsSet extends AbstractUpdatableSet {
        protected Collection computeElements() {
            return readableSet.toCollection();
        }

        /*package*/ void doFireAdd(Collection added) {
            fireAdded(added);
        }

        /*package*/ void doFireRemove(Collection removed) {
            fireRemoved(removed);
        }
        
        /*package*/ void doFireStale(boolean isStale) {
        	fireStale(isStale);
        }
    }

    /**
     * This readableSet returns the same elements as the input readableSet. However, it only fires
     * events AFTER the elements have been added or removed from the viewer.
     */
    private KnownElementsSet elements = new KnownElementsSet();
    
    private IChangeListener listener = new IChangeListener() {
        
    	public void handleChange(ChangeEvent changeEvent) {
    		if (isDisposed()) {
    			return;
    		}
    		
    		switch (changeEvent.getChangeType()) {
    		case ChangeEvent.ADD_MANY:
    			added((Collection)changeEvent.getNewValue());
    			break;
    		case ChangeEvent.REMOVE_MANY:
    			doRemove((Collection)changeEvent.getNewValue());
    			break;
    		case ChangeEvent.STALE:
    			elements.doFireStale(((Boolean)changeEvent.getNewValue()).booleanValue());
    			break;
    		}
    	}
    	
        private void added(Collection added) {
            final List filtered = new ArrayList();

            for (Iterator iter = added.iterator(); iter.hasNext();) {
				Object object = iter.next();
				
                if (filter.select(object)) {
                    filtered.add(object);
                }
            }

            doAdd(filtered);
        }

    };

    public UpdatableSetContentProvider() {
    }

    public void setFilter(IFilter filter) {
        this.filter = filter;
    }

    public Object[] getElements(Object inputElement) {
        return readableSet.toCollection().toArray();
    }

    private void doAdd(Collection added) {
    	elements.doFireAdd(added);
    	
    	Object[] toAdd = added.toArray();
    	
    	if (viewer instanceof TableViewer) {
    		TableViewer tv = (TableViewer) viewer;
    		tv.add(toAdd);
    	} else if (viewer instanceof AbstractListViewer) {
    		AbstractListViewer lv = (AbstractListViewer) viewer;
    		
    		lv.add(toAdd);    		
    	}
    }
    
    private void doRemove(Collection toRemove) {
    	Object[] removed = toRemove.toArray();
    	if (viewer instanceof TableViewer) {
    		TableViewer tv = (TableViewer) viewer;
    		tv.remove(removed);
    	} else if (viewer instanceof AbstractListViewer) {
    		AbstractListViewer lv = (AbstractListViewer) viewer;
    		
    		lv.remove(removed);
    	}
    	
    	elements.doFireRemove(toRemove);
    }

    /**
     * Returns the readableSet of elements known to this content provider. Items are added
     * to this readableSet before being added to the viewer, and they are removed after
     * being removed from the viewer. The readableSet is always updated after the viewer. This 
     * is intended for use by label providers, as it will always return the items that 
     * need labels. 
     * 
     * @return readableSet of items that will need labels
     */
    public IReadableSet getKnownElements() {
        return elements;
    }
    
    public void dispose() {
        setInput(null);
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        this.viewer = viewer;

        if (!(viewer instanceof TableViewer || viewer instanceof AbstractListViewer)) {
        	throw new IllegalArgumentException("This content provider only works with TableViewer of AbstractListViewer"); //$NON-NLS-1$
        }

        if (newInput != null && !(newInput instanceof IReadableSet)) {
        	throw new IllegalArgumentException("This content provider only works with input of type IReadableSet"); //$NON-NLS-1$
        }
        
        setInput((IReadableSet) newInput);
    }

    private void setInput(IReadableSet newSet) {
        if (newSet == null) {
            newSet = EmptyReadableSet.getInstance();
        }
        
        boolean wasStale = false;
        if (readableSet != null) {
        	readableSet.removeChangeListener(listener);
        	wasStale = readableSet.isStale();
        }
        
        Collection oldCollection = readableSet.toCollection();
        Collection newCollection = newSet.toCollection();
        HashSet additions = new HashSet();
        HashSet removals = new HashSet();
        
        additions.addAll(newCollection);
        additions.removeAll(oldCollection);
        
        removals.addAll(oldCollection);
        removals.removeAll(newCollection);
        
        readableSet = newSet;

        doAdd(additions);
        doRemove(removals);
        
        if (readableSet != null) {
        	readableSet.addChangeListener(listener);
        }
        
        boolean isStale = (readableSet != null && readableSet.isStale());
        if (isStale != wasStale) {
        	elements.doFireStale(isStale);
        }
    }

    private boolean isDisposed() {
        return viewer.getControl() == null || viewer.getControl().isDisposed();
    }
}
