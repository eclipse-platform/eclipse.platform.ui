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
import org.eclipse.swt.program.Program;

/**
 * This particular hyperlink action handles 
 * links by opening the resident web browser using 
 * the arg attribute in the hyperlink as the URL.
 * This action needs to be registered with RichText
 * widget using RichText.URL_HANDLER_ID key, and
 * reference in the markup using 'urlHandler' href.
 */
public class RichTextHTTPAction extends RichTextHyperlinkAction {
/**
 * The default constructor.
 *
 */
	public RichTextHTTPAction () {
	}
/**
 * Creates the action using the status line manager.
 * @param manager status line manager
 */
	public RichTextHTTPAction(IStatusLineManager manager) {
		super(manager);
	}
/**
 * Handles link activation by opening the resident
 * browser using the value of the 'arg' attribute
 * as the URL.
 */
	public void linkActivated(IRichTextHyperlink link) {
		Program.launch(link.getArg());
	}
/**
 * When the link is entered, description is set to
 * the value of the 'arg' attribute.
 * @param link the entered link
 */
	public void linkEntered(IRichTextHyperlink link) {
		setDescription(link.getArg());
		super.linkEntered(link);
	}
}
