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
package org.eclipse.debug.internal.core.sourcelookup.containers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;

/**
 * A source container of source containers.
 *  
 * @since 3.0
 */
public abstract class CompositeSourceContainer extends AbstractSourceContainer {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#isComposite()
	 */
	public boolean isComposite() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceContainer#findSourceElements(java.lang.String, boolean)
	 */
	public Object[] findSourceElements(String name, boolean duplicates) throws CoreException {
		ISourceContainer[] containers = getSourceContainers();
		List results = null;
		if (duplicates) {
			results = new ArrayList();
		}
		for (int i = 0; i < containers.length; i++) {
			ISourceContainer container = containers[i];
			Object[] objects = container.findSourceElements(name, duplicates);
			if (objects.length > 0) {
				if (duplicates) {
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
			return new Object[0];
		}
		return results.toArray();
	}
}
