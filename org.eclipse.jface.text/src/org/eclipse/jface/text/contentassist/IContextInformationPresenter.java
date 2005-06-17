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
package org.eclipse.jface.text.contentassist;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;


/**
 * A context information presenter determines the presentation
 * of context information depending on a given document position.
 * <p>
 * The interface can be implemented by clients.
 * </p>
 *
 * @since 2.0
 */
public interface IContextInformationPresenter {

	/**
	 * Installs this presenter for the given context information.
	 *
	 * @param info the context information which this presenter should style
	 * @param viewer the text viewer on which the information is presented
	 * @param offset the document offset for which the information has been computed
	 */
	void install(IContextInformation info, ITextViewer viewer, int offset);

	/**
	 * Updates the given presentation of the given context information
	 * at the given document position. Returns whether update changed the
	 * presentation.
	 *
	 * @param offset the current offset within the document
	 * @param presentation the presentation to be updated
	 * @return <code>true</code> if the given presentation has been changed
	 */
	boolean updatePresentation(int offset, TextPresentation presentation);
}
