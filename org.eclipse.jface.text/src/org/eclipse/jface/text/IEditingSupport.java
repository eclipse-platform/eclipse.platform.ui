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
package org.eclipse.jface.text;


/**
 * Implemented by tools supporting the editing process.
 * <p>
 * Clients may ask an <code>IEditingSupport</code> whether it is currently
 * displaying a shell that has focus, and whether it is the origin of a document
 * event. Depending on the answers to these queries, clients may decide to react
 * differently to incoming notifications about events. For example, a special
 * editing mode, that usually deactivates when the main shell looses focus, may
 * decide to not deactivate if the focus event was triggered by an
 * <code>IEditingSupport</code>.
 * </p>
 * <p>
 * Clients may implement this interface.
 * </p>
 *
 * @see IEditingSupportRegistry
 * @since 3.1
 */
public interface IEditingSupport {
	/**
	 * Returns <code>true</code> if the receiver is the originator of a
	 * <code>DocumentEvent</code> and if that <code>event</code> is related
	 * to <code>subjectRegion</code>.
	 * <p>
	 * The relationship between <code>event</code> and
	 * <code>subjectRegion</code> is not always obvious. Often, the main
	 * editing area being monitored by the caller will be at
	 * <code>subjectRegion</code>, when the receiver modifies the underlying
	 * document at a different location without wanting to interrupt the normal
	 * typing flow of the user.
	 * </p>
	 * <p>
	 * An example would be an editor that automatically increments the section
	 * number of the next section when the user typed in a new section title. In
	 * this example, the subject region is the current typing location, while
	 * the increment results in a document change further down in the text.
	 * </p>
	 *
	 * @param event the <code>DocumentEvent</code> in question
	 * @param subjectRegion the region that the caller is interested in
	 * @return <code>true</code> if <code>event</code> was triggered by the
	 *         receiver and relates to <code>subjectRegion</code>
	 */
	boolean isOriginator(DocumentEvent event, IRegion subjectRegion);

	/**
	 * Returns <code>true</code> if the receiver is showing a shell which has
	 * focus, <code>false</code> if it does not have focus or the helper has
	 * no shell.
	 *
	 * @return <code>true</code> if the support's shell has focus,
	 *         <code>false</code> otherwise
	 */
	boolean ownsFocusShell();
}
