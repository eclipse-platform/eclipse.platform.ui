/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.sourcelookup.containers;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;

/**
 * A source container of source containers.
 *  
 * @since 3.0
 */
public abstract class CompositeSourceContainer extends AbstractSourceContainer {
	
	private ISourceContainer[] fContainers;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#isComposite()
	 */
	public boolean isComposite() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String)
	 */
	public Object[] findSourceElements(String name) throws CoreException {
		ISourceContainer[] containers = getSourceContainers();
		List results = null;
		if (isFindDuplicates()) {
			results = new ArrayList();
		}
		for (int i = 0; i < containers.length; i++) {
			ISourceContainer container = containers[i];
			Object[] objects = container.findSourceElements(name);
			if (objects.length > 0) {
				if (isFindDuplicates()) {
					for (int j = 0; j < objects.length; j++) {
						results.add(objects[j]);
					}
				} else {
					if (objects.length == 1) {
						return objects;
					}
					return new Object[]{objects[0]};
				}
			}
		}
		if (results == null) {
			return EMPTY;
		}
		return results.toArray();
	}
	
	/**
	 * Creates the source containers in this composite container.
	 * Subclasses should override this methods.
	 * 
	 * @throws CoreException if unable to create the containers
	 */
	protected abstract ISourceContainer[] createSourceContainers() throws CoreException;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#getSourceContainers()
	 */
	public ISourceContainer[] getSourceContainers() throws CoreException {
		if (fContainers == null) {
			fContainers = createSourceContainers();
			for (int i = 0; i < fContainers.length; i++) {
				ISourceContainer container = fContainers[i];
				container.init(getDirector());
			}			
		}
		return fContainers;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (fContainers != null) {
			for (int i = 0; i < fContainers.length; i++) {
				ISourceContainer container = fContainers[i];
				container.dispose();
			}
		}
		fContainers = null;
	}
}
