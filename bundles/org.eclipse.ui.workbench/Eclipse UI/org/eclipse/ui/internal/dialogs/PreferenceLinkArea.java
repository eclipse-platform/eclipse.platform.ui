/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.security.InvalidParameterException;
import java.util.Iterator;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;

import org.eclipse.ui.PlatformUI;

/**
 * The PreferenceLinkArea is the link area used to
 * open a specific preference page.
 */
public class PreferenceLinkArea extends LinkArea {

	public PreferenceLinkArea(Composite parent, int style, String pageId) {
		super(parent, style);
		IPreferenceNode node = getPreferenceNode(pageId);
		if (node == null) {
			throw new InvalidParameterException("Node not found");//$NON-NLS-1$
		}
		setRunnable(getRunnable(node));
		setText(node.getLabelText());

	}

	/**
	 * Get the runnable to open node.
	 * @param node
	 * @return IRunnableContext
	 */
	private IRunnableContext getRunnable(final IPreferenceNode node) {
		return new IRunnableContext(){
			/* (non-Javadoc)
			 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
			 */
			public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable)
					throws  InterruptedException {
				WorkbenchPreferenceDialog.createDialogOn(node.getId());
			}
		};
	}

	/**
	 * Get the preference node with pageId.
	 * @param pageId
	 * @return IPreferenceNode
	 */
	private IPreferenceNode getPreferenceNode(String pageId) {
		Iterator iterator = PlatformUI.getWorkbench().getPreferenceManager().getElements(
				PreferenceManager.PRE_ORDER).iterator();
		while (iterator.hasNext()) {
			IPreferenceNode next = (IPreferenceNode) iterator.next();
			if (next.getId().equals(pageId))
				return next;
		}
		return null;
	}
}
