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
package org.eclipse.debug.internal.ui.actions.variables;

 
import org.eclipse.debug.internal.ui.actions.CopyToClipboardActionDelegate;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Used to copy the values of variables to the clipboard from
 * the Variables and Expression views.
 */
public class CopyVariablesToClipboardActionDelegate extends CopyToClipboardActionDelegate {

	/**
	 * Only append children that are expanded in the tree viewer
	 */
	protected boolean shouldAppendChildren(TreeItem item) {
		return item.getExpanded();
	}
	
	protected String getActionId() {
		return IDebugView.COPY_ACTION + ".Variables"; //$NON-NLS-1$
	}
}
