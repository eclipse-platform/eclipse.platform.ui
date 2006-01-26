/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.viewers;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.IReadableSet;

/**
 * @since 3.2
 *
 */
public abstract class ListeningLabelProvider extends ViewerLabelProvider {
	
	private IChangeListener listener = new IChangeListener() {
		public void handleChange(ChangeEvent changeEvent) {
			if (changeEvent.getChangeType() == ChangeEvent.ADD_MANY) {
				Collection added = (Collection)changeEvent.getNewValue();
				
				for (Iterator iter = added.iterator(); iter.hasNext();) {
					Object next = (Object) iter.next();
					
					addListenerTo(next);
				}
			} else if (changeEvent.getChangeType() == ChangeEvent.REMOVE_MANY) {
				Collection removed = (Collection)changeEvent.getNewValue();
				
				for (Iterator iter = removed.iterator(); iter.hasNext();) {
					Object next = (Object) iter.next();
					
					removeListenerFrom(next);
				}				
			}
		}	
	};
	private IReadableSet items;
	
	public ListeningLabelProvider(IReadableSet itemsThatNeedLabels) {
		this.items = itemsThatNeedLabels;
		items.addChangeListener(listener);
		for (Iterator iter = items.toCollection().iterator(); iter.hasNext();) {
			Object next = (Object) iter.next();
			
			addListenerTo(next);
		}
	}
	
	/**
	 * @param next
	 */
	protected abstract void removeListenerFrom(Object next);

	/**
	 * @param next
	 */
	protected abstract void addListenerTo(Object next);

	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.viewers.ViewerLabelProvider#dispose()
	 */
	public void dispose() {
		for (Iterator iter = items.toCollection().iterator(); iter.hasNext();) {
			Object next = (Object) iter.next();
			
			removeListenerFrom(next);
		}
		items.removeChangeListener(listener);
		super.dispose();
	}
}
