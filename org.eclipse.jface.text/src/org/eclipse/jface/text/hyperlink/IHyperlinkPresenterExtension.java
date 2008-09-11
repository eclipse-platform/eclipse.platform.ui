/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
 * Extends {@link IHyperlinkPresenter} with ability
 * to query whether the currently shown hyperlinks
 * can be hidden.
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @since 3.4
 */
public interface IHyperlinkPresenterExtension {

	/**
	 * Tells whether the currently shown hyperlinks
	 * can be hidden.
	 *
	 * @return <code>true</code> if the hyperlink manager can hide the current hyperlinks
	 */
	boolean canHideHyperlinks();

}