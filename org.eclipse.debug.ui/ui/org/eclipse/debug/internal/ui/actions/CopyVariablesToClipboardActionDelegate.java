/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;

 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Used to copy the values of variables to the clipboard from
 * the Variables and Expression views.
 */
public class CopyVariablesToClipboardActionDelegate extends CopyToClipboardActionDelegate {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.CopyToClipboardActionDelegate#pruneSelection()
	 */
	protected Iterator pruneSelection() {
		IStructuredSelection selection= (IStructuredSelection)getViewer().getSelection();
		List elements= new ArrayList(selection.size());
		Iterator iter= selection.iterator();
		while (iter.hasNext()) {
			Object element= iter.next();
			if (isEnabledFor(element)) {
				if(walkHierarchy(element, elements)) {
					elements.add(element);
				}
			}
		}
		
		return elements.iterator();
	}

	/**
	 * Only append children that are visible in the tree viewer
	 */
	protected boolean shouldAppendChildren(Object e) {
		return((TreeViewer)getViewer()).getExpandedState(e);
	}
	
	/**
	 * @see AbstractDebugActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
		return element instanceof IDebugElement;
	}
	
	protected String getActionId() {
		return IDebugView.COPY_ACTION + ".Variables"; //$NON-NLS-1$
	}
}
