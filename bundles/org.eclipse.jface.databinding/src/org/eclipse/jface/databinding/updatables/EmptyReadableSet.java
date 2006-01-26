package org.eclipse.jface.databinding.updatables;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IReadableSet;

/**
 * Implements the empty set as an IReadableSet.
 * 
 * @since 3.2
 */
public class EmptyReadableSet implements IReadableSet {

	private static EmptyReadableSet instance;

	private EmptyReadableSet() {
	}
	
	/**
	 * Returns the shared instance of EmptyReadableSet
	 * 
	 * @return
	 */
	public static EmptyReadableSet getInstance() {
		if (instance == null) {
			instance = new EmptyReadableSet();
		}

		return instance;
	}
	
	public void addChangeListener(IChangeListener changeListener) {
	}
	
	public void dispose() {
	}
	
	public boolean isDisposed() {
		return false;
	}
	
	public boolean isStale() {
		return false;
	}
	
	public void removeChangeListener(IChangeListener changeListener) {
	}
	
	public Collection toCollection() {
		return Collections.EMPTY_SET;
	}
}
