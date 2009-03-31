/*******************************************************************************
 *  Copyright (c) 2006, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.internal.ui.viewers.model.ITreeModelContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * Customized tree viewer for the Memory View
 * This Tree Viewer has a specialized update policy for the memory view.
 * When the model fires a ADDED delta, the update policy handles the event as follows:
 * If the ADDED delta is accompanied by SELECT, and the added element is an memory blok, then
 * the udpate policy asks the Memory View if the it is currently pinned to a memory block.  If the view
 * is currently pinned, then the SELECT delta is ignored.
 * 
 * If the ADDED delta and SELECT delta are recieved in separate nodes, then the delta will be handled as-is and would
 * not take the pinning state of the memory view into account. 
 *
 */
public class MemoryViewTreeViewer extends TreeModelViewer {

	public MemoryViewTreeViewer(Composite parent, int style,
			IPresentationContext context) {
		super(parent, style, context);
	}
	
	/* 
	 * Need to have a customized content provider to define a special update policy for the Memory View
	 * (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.InternalTreeModelViewer#createContentProvider()
	 */
	protected ITreeModelContentProvider createContentProvider() {
		return new MemoryViewTreeModelContentProvider();
	}

}
