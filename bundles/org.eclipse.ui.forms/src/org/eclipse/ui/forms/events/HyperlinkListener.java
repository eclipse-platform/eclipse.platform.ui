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
package org.eclipse.ui.forms.events;
/**
 * Classes that implement this interface will be notified when hyperlinks are
 * entered, exited and activated.
 * 
 * TODO (dejan) - interface should be named "IHyperlinkListener"
 * @see Hyperlink
 * @see ImageHyperlink
 * @see FormText
 * @since 3.0
 */
public interface HyperlinkListener {
	/**
	 * Sent when hyperlink is entered either by mouse entering the link client
	 * area, or keyboard focus switching to the hyperlink.
	 * 
	 * @param e
	 *            an event containing information about the hyperlink
	 */
	void linkEntered(HyperlinkEvent e);
	/**
	 * Sent when hyperlink is exited either by mouse exiting the link client
	 * area, or keyboard focus switching from the hyperlink.
	 * 
	 * @param e
	 *            an event containing information about the hyperlink
	 */
	void linkExited(HyperlinkEvent e);
	/**
	 * Sent when hyperlink is activated either by mouse click inside the link
	 * client area, or by pressing 'Enter' key while hyperlink has keyboard
	 * focus.
	 * 
	 * @param e
	 *            an event containing information about the hyperlink
	 */
	void linkActivated(HyperlinkEvent e);
}