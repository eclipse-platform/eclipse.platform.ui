/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;


/**
 * Common super class for implementations of source lookup participants.
 * <p>
 * Clients implementing source lookup participants should subclass this class.
 * </p>
 * @since 3.0
 */
public abstract class AbstractSourceLookupParticipant implements ISourceLookupParticipant {
	
	private ISourceLookupDirector fDirector;
	
	protected static final Object[] EMPTY = new Object[0]; 
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#init(org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector)
	 */
	public void init(ISourceLookupDirector director) {
		fDirector = director;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#dispose()
	 */
	public void dispose() {
		fDirector = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#findSourceElements(java.lang.Object)
	 */
	public Object[] findSourceElements(Object object) throws CoreException {
		List results = null;
		if (isFindDuplicates()) {
			results = new ArrayList();
		}
		String name = getSourceName(object);
		if (name != null) {
			ISourceContainer[] containers = getSourceContainers();
			for (int i = 0; i < containers.length; i++) {
				ISourceContainer container = getDelegateContainer(containers[i]);
				if (container != null) {
					Object[] objects = container.findSourceElements(name);
					if (objects.length > 0) {
						if (isFindDuplicates()) {
							for (int j = 0; j < objects.length; j++) {
								results.add(objects[j]);
							}
						} else {
							if (objects.length == 1) {
								return objects;
							} else {
								return new Object[]{objects[0]};
							}
						}
					}
				}
			}
		}
		if (results == null) {
			return EMPTY;
		}
		return results.toArray();
	}	
	
	/**
	 * Returns the source container to search in place of the given source
	 * container, or <code>null</code> if the given source container is not
	 * to be searched. The default implementation does not translate source
	 * containers. Subclasses should override if required.
	 *  
	 * @param container the source container about to be searched (proxy)
	 * @return the source container to be searched (delegate), or <code>null</code>
	 * 	if the source container should not be searched
	 */
	protected ISourceContainer getDelegateContainer(ISourceContainer container) {
		return container;
	}
	
	/**
	 * Returns the source lookup director this participant is registered with
	 * or <code>null</code> if none.
	 * 
	 * @return the source lookup director this participant is registered with
	 *  or <code>null</code> if none
	 */
	protected ISourceLookupDirector getDirector() {
		return fDirector;
	}
	
	/**
	 * Returns whether this participant's source lookup director is configured
	 * to search for duplicate source elements.
	 * 
	 * @return whether this participant's source lookup director is configured
	 * to search for duplicate source elements
	 */
	protected boolean isFindDuplicates() {
		return getDirector().isFindDuplicates();
	}	
	
	/**
	 * Returns the source containers currently registered with this participant's
	 * source lookup director.
	 * 
	 * @return the source containers currently registered with this participant's
	 * source lookup director
	 */
	protected ISourceContainer[] getSourceContainers() {
		return getDirector().getSourceContainers();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#sourceContainersChanged(org.eclipse.debug.internal.core.sourcelookup.ISourceLookupDirector)
	 */
	public void sourceContainersChanged(ISourceLookupDirector director) {
	}
}
