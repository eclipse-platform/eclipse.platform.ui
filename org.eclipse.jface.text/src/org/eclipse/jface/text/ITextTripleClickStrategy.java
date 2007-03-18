/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 * A text triple click strategy defines the reaction of a text viewer to mouse
 * triple click events. The strategy must be installed on an
 * {@link org.eclipse.jface.text.ITextViewer} that also implements
 * {@link org.eclipse.jface.text.ITextViewerExtension7}.
 * <p>
 * <strong>Note:</strong> If a {@link ITextDoubleClickStrategy} also applies
 * then it will be fired before the triple click strategy.
 * </p>
 * <p>
 * Clients may implement this interface or use the standard implementation
 * <code>LineSelectionTextTripleClickStrategy</code>.</p>
 *
 * @see org.eclipse.jface.text.ITextViewer
 * @since 3.3
 */
public interface ITextTripleClickStrategy {

	/**
	 * The mouse has been triple clicked on the given text viewer.
	 *
	 * @param viewer the viewer into which has been triple clicked
	 */
	void tripleClicked(ITextViewer viewer);
}
