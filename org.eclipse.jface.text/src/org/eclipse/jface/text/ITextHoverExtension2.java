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
package org.eclipse.jface.text;


/**
 * Extension interface for {@link org.eclipse.jface.text.ITextHover}.
 * <p>
 * Provides a way for hovers to return hover-specific information objects.
 * </p>
 *
 * @see org.eclipse.jface.text.ITextHover
 * @since 3.4
 */
public interface ITextHoverExtension2 {

	/**
	 * Returns the information which should be presented when a hover popup is shown
	 * for the specified hover region. The hover region has the same semantics
	 * as the region returned by {@link ITextHover#getHoverRegion(ITextViewer, int)}.
	 * If the returned information is <code>null</code>, no hover popup will be shown.
	 * <p>
	 * <strong>Note:</strong> Implementers have to ensure that {@link ITextHoverExtension#getHoverControlCreator()}
	 * returns {@link IInformationControl}s that implement
	 * {@link IInformationControlExtension2} and whose
	 * {@link IInformationControlExtension2#setInput(Object)} can handle the
	 * information objects returned by this method.</p>
	 * <p>
	 * Callers should ignore the text returned by {@link ITextHover#getHoverInfo(ITextViewer, IRegion)}.</p>
	 *
	 * @param textViewer the viewer on which the hover popup should be shown
	 * @param hoverRegion the text range in the viewer which is used to determine
	 * 		the hover display information
	 * @return the hover popup display information, or <code>null</code> if none available
	 */
	Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion);

}
