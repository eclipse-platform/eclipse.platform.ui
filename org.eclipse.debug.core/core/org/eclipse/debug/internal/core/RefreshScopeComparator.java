/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import java.util.Comparator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.RefreshUtil;

/**
 * Compares refresh scope attributes as the format has changed from a working
 * set memento to an XML memento of resource paths. Avoids migrating attribute
 * to new format until something else in the configuration changes.
 * 
 * @since 3.6
 */
public class RefreshScopeComparator implements Comparator {

	public int compare(Object o1, Object o2) {
		String m1 = (String) o1;
		String m2 = (String) o2;
		try {
			IResource[] r1 = RefreshUtil.toResources(m1);
			IResource[] r2 = RefreshUtil.toResources(m2);
			if (r1.length == r2.length) {
				for (int i = 0; i < r2.length; i++) {
					if (!r1[i].equals(r2[i])) {
						return -1;
					}
				}
				return 0;
			}
		} catch (CoreException e) {
			return -1;
		}
		return -1;
	}

}
