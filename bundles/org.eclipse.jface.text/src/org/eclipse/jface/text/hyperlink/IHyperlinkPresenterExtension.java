/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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