/*******************************************************************************
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2005-2006. All Rights Reserved. 
 * 
 * Note to U.S. Government Users Restricted Rights:  Use, 
 * duplication or disclosure restricted by GSA ADP Schedule 
 * Contract with IBM Corp.
 *******************************************************************************/
package org.eclipse.jface.databinding.updatables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jface.databinding.AbstractUpdatableSet;
import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IReadableSet;
import org.eclipse.jface.databinding.IUpdatableFunction;
import org.eclipse.jface.util.Assert;

/**
 * A ConvertingSet is the result of a transformation applied to some input set.
 * 
 * @since 3.2
 */
public class ConvertingSet extends AbstractUpdatableSet {

    private IReadableSet input;

    private IChangeListener inputListener = new IChangeListener() {
    	public void handleChange(ChangeEvent changeEvent) {
    		Object source = changeEvent.getSource();
    		switch(changeEvent.getChangeType()) {
	    		case ChangeEvent.ADD_MANY:
	    			if (source == input) {
		    			// Fired by input
		    			processAdds((Collection)changeEvent.getNewValue());
	    			}
	    			break;
	    		case ChangeEvent.REMOVE_MANY:
	    			if (source == input) {
		    			// Fired by input
		    			processRemoves((Collection)changeEvent.getNewValue());
	    			}
	    			break;
	    		case ChangeEvent.CHANGE_MANY:
	    			if (source == func) {
		    			// Fired by func
		    			processChanges((Collection)changeEvent.getNewValue());
	    			}
	    			break;
	    			
	    		case ChangeEvent.STALE: 
	    			boolean funcStale = func.isStale();
	    			boolean inputStale = input.isStale();
	    			boolean bothStale = funcStale && inputStale;
	    			boolean noneStale = !funcStale && !inputStale;
	    			
	    			if (noneStale) {
	    				fireStale(false);
	    			} else {
	    				if (!bothStale) {
	    					fireStale(true);
	    				}
	    			}
	    			
	    			break;
	    		default:
    		}
    	}
    };
    
    /**
     * Holds a bi-directional mapping from domain elements onto their values, or null
     * if we are not maintaining a cached result. 
     */
    private InverseMapping converter = null;

    private IUpdatableFunction func;

    
    /**
     * Creates a new set whose elements are the result of applying the given function
     * to all the elements in the input set. 
     * 
     * @param input
     * @param f
     */
    public ConvertingSet(IReadableSet input, IUpdatableFunction f) {
        super();

        this.input = input;
        this.func = f;
    }

    public boolean isStale() {
        return func.isStale() || input.isStale();	
    }
    
	private void processAdds(Collection added) {
    	// Identity optimization: if we're using the identity function,
    	// just rebroadcast the add event.
    	if (func == IdentityFunction.getInstance()) {
    		fireAdded(added);
    		return;
    	}
    	
    	ArrayList adds = addMappings(added);

        fireAdded(adds);
    }

	private ArrayList addMappings(Collection added) {
		ArrayList adds = new ArrayList();
    	
    	for (Iterator iter = added.iterator(); iter.hasNext();) {
			Object next = iter.next();
			 
			Object target = func.calculate(next);
			
			if (target != null) {
				Collection keys = converter.addMapping(next, target);
				
				// If this is the first key to map onto this value
				if (keys.size() == 1) {
					adds.add(target);
				}
			}
		}
		return adds;
		
	}

    private void processRemoves(Collection removed) {
    	// Identity optimization: if we're using the identity function,
    	// just rebroadcast the remove event.
    	if (converter == null) {
    		fireRemoved(removed);
    		return;
    	}
    	
    	ArrayList removes = new ArrayList();
    	
    	for (Iterator iter = removed.iterator(); iter.hasNext();) {
			Object next = iter.next();
			
			// Look for a cached result
			Object target = converter.getValue(next);
			
			if (target != null) {
				Collection keys = converter.removeMapping(next);
				
				// If this was the last key to map onto this value
				if (keys.isEmpty()) {
					removes.add(target);
				}
			}
		}

        fireRemoved(removes);
    }
    
    protected void processChanges(Collection collection) {
    	// The identity function should never fire a change event, so ensure that we're not
    	// using the identity optimizations.
    	Assert.isNotNull(converter, "The identity function should never fire a change event"); //$NON-NLS-1$
    	
    	processRemoves(collection);
    	processAdds(collection);
	}

	public Object getAdapter(Object element) {
		// Optimize for identity function: just return the element verbatim
		if (func == IdentityFunction.getInstance()) {
			return element;
		}
		
		// If we have a cached value, use it
		if (converter != null) {
			return converter.getValue(element);
		}
		
		// Else convert the element
		return func.calculate(element);
    }
    
    /**
     * Returns all elements in the input set that convert to the the given
     * element in the reciever.
     * 
     * @param adapter
     * @return
     */
    public Collection getElements(Object adapter) {
    	// Optimize for identity function: just return the element verbatim
    	if (func == IdentityFunction.getInstance()) {
    		return Collections.singleton(adapter);
    	}
    
    	// If we have a cached value, use it (cached values are maintained if anyone
    	// is listening to this set)
    	if (converter != null) {
    		converter.getInverse(adapter);
    	}
    	
    	// Else do an exhaustive search for elements that convert to the given object
    	Collection result = new HashSet();
    	Collection rawInput = input.toCollection();
    	for (Iterator iter = rawInput.iterator(); iter.hasNext();) {
			Object next = iter.next();
			
			Object converted = func.calculate(next);
			if (converted != null) {
				if (converted == adapter) {
					result.add(next);
				}
			}
		}
    	
    	return result;    	
    }
    
    protected Collection computeElements() {
    	// Optimize for the identity function: just return the input elements verbatim
    	if (func == IdentityFunction.getInstance()) {
    		return input.toCollection();
    	}
    	
    	// If we're maintaining a cached result, return the cached value
    	if (converter != null) {
    		return converter.getValues();
    	}
    	
    	// Otherwise, recompute the result
    	Collection result = new HashSet();
    	Collection rawInput = input.toCollection();
    	for (Iterator iter = rawInput.iterator(); iter.hasNext();) {
			Object next = iter.next();
			
			Object converted = func.calculate(next);
			if (converted != null) {
				result.add(converted);
			}
		}
    	
    	return result;
    }

    protected void firstListenerAdded() {
        this.func.addChangeListener(inputListener);
        this.input.addChangeListener(inputListener);

        converter = new InverseMapping();
        
        addMappings(input.toCollection());
    	
    	super.firstListenerAdded();
    }
    
    protected void lastListenerRemoved() {
    	this.func.removeChangeListener(inputListener);
    	this.func.removeChangeListener(inputListener);
    	if (converter != null) {
    		converter.dispose();
    		converter = null;
    	}
    	super.lastListenerRemoved();
    }

}
