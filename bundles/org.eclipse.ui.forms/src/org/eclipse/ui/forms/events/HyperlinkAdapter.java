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
package org.eclipse.ui.forms.events;
/**
 * This adapter class provides default implementations for the methods
 * described by the <code>HyperlinkListener</code> interface.
 * <p>
 * Classes that wish to deal with <code>HyperlinkEvent</code> s can extend
 * this class and override only the methods which they are interested in.
 * </p>
 * 
 * @see IHyperlinkListener
 * @see HyperlinkEvent
 * @since 3.0
 */
public class HyperlinkAdapter implements IHyperlinkListener {
	/**
	 * Sent when the link is entered. The default behaviour is to do nothing.
	 * 
	 * @param e
	 *            the event
	 */
	public void linkEntered(HyperlinkEvent e) {
	}
	/**
	 * Sent when the link is exited. The default behaviour is to do nothing.
	 * 
	 * @param e
	 *            the event
	 */
	public void linkExited(HyperlinkEvent e) {
	}
	/**
	 * Sent when the link is activated. The default behaviour is to do nothing.
	 * 
	 * @param e
	 *            the event
	 */
	public void linkActivated(HyperlinkEvent e) {
	}
}
