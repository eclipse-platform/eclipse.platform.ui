/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.mapping;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;

/**
 * A abstract implementation of {@link ICompareAdapter}. Most of the methods
 * are no-ops except for the {@link #asCompareInput(ISynchronizationContext, Object) }
 * which will convert file objects to an appropriate compare input.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public class AbstractCompareAdapter implements ICompareAdapter {

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ICompareAdapter#prepareContext(org.eclipse.team.ui.mapping.ISynchronizationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void prepareContext(ISynchronizationContext context, IProgressMonitor monitor) throws CoreException {
		// Do nothing by default
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ICompareAdapter#asCompareInput(org.eclipse.team.ui.mapping.ISynchronizationContext, java.lang.Object)
	 */
	public ICompareInput asCompareInput(ISynchronizationContext context, Object o) {
		if (o instanceof IResource) {
			IResource resource = (IResource) o;
			if (resource.getType() == IResource.FILE) {
				SyncInfo info = context.getSyncInfoTree().getSyncInfo(resource);
				if (info != null)
					return new SyncInfoModelElement(null, info);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ICompareAdapter#findStructureViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.viewers.Viewer, org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.CompareConfiguration)
	 */
	public Viewer findStructureViewer(Composite parent, Viewer oldViewer,
			ICompareInput input, CompareConfiguration configuration) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ICompareAdapter#findContentViewer(org.eclipse.swt.widgets.Composite, org.eclipse.jface.viewers.Viewer, org.eclipse.compare.structuremergeviewer.ICompareInput, org.eclipse.compare.CompareConfiguration)
	 */
	public Viewer findContentViewer(Composite parent, Viewer oldViewer,
			ICompareInput input, CompareConfiguration configuration) {
		return null;
	}

}
