/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 * Computes the information to be shown in a hover popup which appears on top of
 * the text viewer's text widget when a hover event occurs. If the text hover
 * does not provide information no hover popup is shown. Any implementer of this
 * interface must be capable of operating in a non-UI thread.
 * <p>
 *
 * In order to provide backward compatibility for clients of
 * <code>ITextHover</code>, extension interfaces are used as a means of
 * evolution. The following extension interfaces exist:
 * <ul>
 * <li>{@link org.eclipse.jface.text.ITextHoverExtension} since version 3.0
 *     allowing a text hover to provide a creator for the hover control. This allows
 *     for sophisticated hovers in a way that information computed by the hover can
 *     be displayed in the best possible form.</li>
 * <li>{@link org.eclipse.jface.text.ITextHoverExtension2} since version 3.4
 *     allowing a text hover to return hover-specific information objects.</li>
 * </ul></p>
 * <p>
 * Clients may implement this interface.</p>
 *
 * @see org.eclipse.jface.text.ITextHoverExtension
 * @see org.eclipse.jface.text.ITextHoverExtension2
 * @see org.eclipse.jface.text.ITextViewer
 */
public interface ITextHover {

	/**
	 * Returns the information which should be presented when a hover popup is shown
	 * for the specified hover region. The hover region has the same semantics
	 * as the region returned by <code>getHoverRegion</code>. If the returned
	 * information is <code>null</code> or empty no hover popup will be shown.
	 *
	 * @param textViewer the viewer on which the hover popup should be shown
	 * @param hoverRegion the text range in the viewer which is used to determine
	 * 		the hover display information
	 * @return the hover popup display information, or <code>null</code> if none available
	 * @deprecated As of 3.4, replaced by {@link ITextHoverExtension2#getHoverInfo2(ITextViewer, IRegion)}
	 */
	String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion);

	/**
	 * Returns the text region which should serve as the source of information
	 * to compute the hover popup display information. The popup has been requested
	 * for the given offset.<p>
	 * For example, if hover information can be provided on a per method basis in a
	 * source viewer, the offset should be used to find the enclosing method and the
	 * source range of the method should be returned.
	 *
	 * @param textViewer the viewer on which the hover popup should be shown
	 * @param offset the offset for which the hover request has been issued
	 * @return the hover region used to compute the hover display information
	 */
	IRegion getHoverRegion(ITextViewer textViewer, int offset);
}
