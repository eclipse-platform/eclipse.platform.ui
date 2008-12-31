/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;

import org.eclipse.jface.text.revisions.RevisionInformation;
import org.eclipse.jface.text.source.Annotation;

/**
 * Extension interface for {@link org.eclipse.ui.texteditor.ITextEditor}. Adds
 * the following functions:
 * <ul>
 * 	<li>annotation navigation</li>
 * 	<li>revision information display</li>
 * </ul>
 * <p>
 * This interface may be implemented by clients.
 * </p>
 *
 * @since 3.2
 */
public interface ITextEditorExtension4 {

	/**
	 * Jumps to the next annotation according to the given direction.
	 *
	 * @param forward <code>true</code> if search direction is forward, <code>false</code> if backward
	 * @return the selected annotation or <code>null</code> if none
	 */
	public Annotation gotoAnnotation(boolean forward);

	/**
	 * Shows revision information in this editor.
	 *
	 * @param info the revision information to display
	 * @param quickDiffProviderId the quick diff provider that matches the source of the revision
	 *        information
	 */
	public void showRevisionInformation(RevisionInformation info, String quickDiffProviderId);
}
