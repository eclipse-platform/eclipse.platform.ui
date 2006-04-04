/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.resources;

import org.eclipse.core.internal.events.ResourceDelta;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;

public class DeltaDebugListener implements IResourceChangeListener {
	/**
	 * @see IResourceChangeListener#resourceChanged
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta == null)
			return;
		try {
			System.out.println();
			visitingProcess(delta);
		} catch (CoreException e) {
			// XXX: dropping exceptions
		}
	}

	protected boolean visit(IResourceDelta change) {
		System.out.println(((ResourceDelta) change).toDebugString());
		return true;
	}

	/**
	 * Processes the given change by traversing its nodes and calling
	 * <code>visit</code> for each.
	 *
	 * @see #visit
	 * @exception CoreException if the operation fails
	 */
	protected void visitingProcess(IResourceDelta change) throws CoreException {
		if (!visit(change))
			return;
		int kind = IResourceDelta.ADDED | IResourceDelta.REMOVED | IResourceDelta.CHANGED;
		int memberFlags = IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS | IContainer.INCLUDE_PHANTOMS;
		IResourceDelta[] children = change.getAffectedChildren(kind, memberFlags);
		for (int i = 0; i < children.length; i++)
			visitingProcess(children[i]);
	}
}
