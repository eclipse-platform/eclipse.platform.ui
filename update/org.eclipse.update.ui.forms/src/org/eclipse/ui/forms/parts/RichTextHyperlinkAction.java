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
package org.eclipse.ui.forms.parts;

import org.eclipse.jface.action.IStatusLineManager;

/**
 * Handles events related to rich text hyperlinks. If status line manager is
 * provided, action description will be set on the status line when link is
 * entered, and removed when exited. Clients must override 'linkActivated' to
 * handle the activation event.
 */
public class RichTextHyperlinkAction {
	private IStatusLineManager manager;
	private String description;

	/**
	 * The default constructor.
	 *  
	 */
	public RichTextHyperlinkAction() {
	}
	/**
	 * Creates the action and associates it with the status line manager.
	 * 
	 * @param manager
	 *            status line manager
	 */
	public RichTextHyperlinkAction(IStatusLineManager manager) {
		setStatusLineManager(manager);
	}
	/**
	 * Called by the rich text control when hyperlink is activated. The default
	 * implementation does nothing.
	 * 
	 * @param link
	 *            the active link
	 */
	public void linkActivated(final IRichTextHyperlink link) {
	}

	/**
	 * Called by the rich text control when hyperlink is entered. The default
	 * implementation sets the description on the status line manager.
	 * 
	 * @param link
	 *            entered link
	 */
	public void linkEntered(IRichTextHyperlink link) {
		if (manager != null && description != null) {
			manager.setMessage(description);
		}
	}
	/**
	 * Called by the rich text control when hyperlink is exited. The default
	 * implementation removes the description from the status line manager.
	 * 
	 * @param link
	 *            exited link
	 */
	public void linkExited(IRichTextHyperlink link) {
		if (manager != null && description != null) {
			manager.setMessage(null);
		}
	}
	/**
	 * Sets the status line manager to be used by this action.
	 * 
	 * @param manager
	 *            the status line manager
	 */
	public void setStatusLineManager(IStatusLineManager manager) {
		this.manager = manager;
	}
	/**
	 * Returns the description of this action.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * Sets the description of this action. The description will be shown on
	 * the status line.
	 * 
	 * @param description
	 *            the description of the action
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
