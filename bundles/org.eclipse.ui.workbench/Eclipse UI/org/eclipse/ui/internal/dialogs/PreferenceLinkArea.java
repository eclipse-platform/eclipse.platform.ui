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
import java.text.MessageFormat;
import java.util.Iterator;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * The PreferenceLinkArea is the link area used to open a specific preference
 * page.
 */
public class PreferenceLinkArea extends LinkArea {

	/**
	 * Create a new instance of the receiver
	 * 
	 * @param parent
	 *            the parent Composite
	 * @param style
	 *            the SWT style
	 * @param pageId
	 *            the page id
	 * @param message
	 *            the message to use as text
	 * @param pageContainer -
	 *            The container another page will be opened in.
	 * @param pageData -
	 *            The data to apply to the page.
	 */
	public PreferenceLinkArea(Composite parent, int style, String pageId,
			String message, IWorkbenchPreferenceContainer pageContainer,
			Object pageData) {
		super(parent, style);
		setRunnable(getRunnable(pageContainer,pageId,pageData));
		IPreferenceNode node = getPreferenceNode(pageId);
		if (node == null) {
			throw new InvalidParameterException("Node not found");//$NON-NLS-1$
		}
		String result = MessageFormat.format(message, new String[] { node
				.getLabelText() });
		setText(result);
		
	}

	/**
	 * Get the runnable to open the node in the container.
	 * 
	 * @param container
	 * @param pageId
	 * @param data
	 * @return IRunnableContext
	 */
	private IRunnableContext getRunnable(
			final IWorkbenchPreferenceContainer container, final String pageId,
			final Object data) {
		return new IRunnableContext() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean,
			 *      boolean, org.eclipse.jface.operation.IRunnableWithProgress)
			 */
			public void run(boolean fork, boolean cancelable,
					IRunnableWithProgress runnable) throws InterruptedException {
				container.openPage(pageId, data);
			}
		};
	}

	/**
	 * Get the preference node with pageId.
	 * 
	 * @param pageId
	 * @return IPreferenceNode
	 */
	private IPreferenceNode getPreferenceNode(String pageId) {
		Iterator iterator = PlatformUI.getWorkbench().getPreferenceManager()
				.getElements(PreferenceManager.PRE_ORDER).iterator();
		while (iterator.hasNext()) {
			IPreferenceNode next = (IPreferenceNode) iterator.next();
			if (next.getId().equals(pageId))
				return next;
		}
		return null;
	}
}
