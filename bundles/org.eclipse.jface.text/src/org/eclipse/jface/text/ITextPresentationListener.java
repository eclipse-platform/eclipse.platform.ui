/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
package org.eclipse.jface.text;


/**
 * Text presentation listeners registered with an
 * {@link org.eclipse.jface.text.ITextViewer} are informed when a
 * {@link org.eclipse.jface.text.TextPresentation} is about to be applied to the
 * text viewer. The listener can apply changes to the text presentation and thus
 * participate in the process of text presentation creation.
 *
 * @since 3.0
 */
public interface ITextPresentationListener {

	/**
	 * This method is called when a text presentation is about to be applied to
	 * the text viewer. The receiver is allowed to change the text presentation
	 * during that call.
	 *
	 * @param textPresentation the current text presentation
	 */
	public void applyTextPresentation(TextPresentation textPresentation);
}
