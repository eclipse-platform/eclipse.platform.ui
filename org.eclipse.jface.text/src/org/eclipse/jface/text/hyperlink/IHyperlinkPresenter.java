/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.hyperlink;

import org.eclipse.jface.text.ITextViewer;


/**
 * A hyperlink presenter shows hyperlinks on the installed text viewer
 * and allows to pick one on of the hyperlinks.
 * <p>
 * In order to provide backward compatibility for clients of <code>IHyperlinkDetector</code>, extension
 * interfaces are used to provide a means of evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link IHyperlinkPresenterExtension} since version 3.4,
 * 		adds the ability to query  whether the currently shown hyperlinks
 *		can be hidden.
 * </li>
 * <li>{@link IHyperlinkPresenterExtension2} since version 3.7,
 *      adds the ability distinguish between the modes in which the
 *      control either takes focus or not when visible.
 * </li> 
 * </ul></p>
 * <p>
 * Clients may implement this interface. A default implementation is provided
 * through {@link org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter}.
 * </p>
 *
 * @see IHyperlinkPresenterExtension
 * @see IHyperlinkPresenterExtension2
 * @since 3.1
 */
public interface IHyperlinkPresenter {

	/**
	 * Tells whether this presenter is able to handle
	 * more than one hyperlink.
	 *
	 * @return <code>true</code> if this presenter can handle more than one hyperlink
	 */
	boolean canShowMultipleHyperlinks();

	/**
	 * Tells this hyperlink presenter to show the given
	 * hyperlinks on the installed text viewer.
	 *
	 * @param hyperlinks the hyperlinks to show
	 * @throws IllegalArgumentException if
	 * 			<ul>
	 * 				<li><code>hyperlinks</code> is empty</li>
	 * 				<li>{@link #canShowMultipleHyperlinks()} returns <code>false</code> and <code>hyperlinks</code> contains more than one element</li>
	 * 			</ul>
	 */
	void showHyperlinks(IHyperlink[] hyperlinks) throws IllegalArgumentException;

	/**
	 * Tells this hyperlink presenter to hide the hyperlinks
	 * requested to be shown by {@link #showHyperlinks(IHyperlink[])}.
	 */
	void hideHyperlinks();

	/**
	 * Installs this hyperlink presenter on the given text viewer.
	 *
	 * @param textViewer the text viewer
	 */
	void install(ITextViewer textViewer);

	/**
	 * Uninstalls this hyperlink presenter.
	 */
	void uninstall();
}
