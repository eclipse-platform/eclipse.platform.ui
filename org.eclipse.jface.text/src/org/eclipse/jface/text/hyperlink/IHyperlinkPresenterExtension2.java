/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.hyperlink;


/**
 * Extends {@link IHyperlinkPresenter} with ability to distinguish between the modes in which the
 * control either takes focus or not when visible.
 * <p>
 * Clients may implement this interface.
 * </p>
 * 
 * @since 3.7
 */
public interface IHyperlinkPresenterExtension2 {

	/**
	 * Tells this hyperlink presenter to show the given hyperlinks on the installed text viewer and
	 * specifies whether or not the control takes focus when visible.
	 * 
	 * @param activeHyperlinks the hyperlinks to show
	 * @param takesFocusWhenVisible <code>true</code> if the control takes the focus when visible,
	 *            <code>false</code> otherwise. Will be ignored if there is only one hyperlink to
	 *            show.
	 */
	public void showHyperlinks(IHyperlink[] activeHyperlinks, boolean takesFocusWhenVisible);

}
