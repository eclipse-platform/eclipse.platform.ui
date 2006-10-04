/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Chris Gross (schtoo@schtoo.com) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.dialogs;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * 
 * A listener which is notified when the current page of the multi-page dialog
 * is changing.
 * 
 * @see IPageTransitionProvider
 * @see PageTransitionEvent
 * @since 3.3
 */
public interface IPageTransitionListener {
	
	/**
	 * Notifies that the selected page is changing.  The doit field of the
	 * PageTransitionEvent can be set to false to prevent the page from changing.
	 * 
	 * @param event
	 *            event object describing the change
	 */
	public void pageTransition(PageTransitionEvent event);

}
