/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;



/**
 * Provides a hover popup which appears on top of the text viewer with
 * relevant display information. If the text hover does not provide information
 * no hover popup is shown. Any implementer of this interface must be capable of
 * operating in a non-UI thread.<p>
 * Clients may implement this interface.
 *
 * @see org.eclipse.jface.text.ITextViewer
 */
public interface ITextHover {
	
	/**
	 * Returns the text which should be presented if a hover popup is shown
	 * for the specified hover region. The hover region has the same semantics
	 * as the region returned by <code>getHoverRegion</code>. If the returned
	 * string is <code>null</code> or empty no hover popup will be shown.
	 * 
	 * @param textViewer the viewer on which the hover popup should be shown
	 * @param hoverRegion the text range in the viewer which is used to determine 
	 * 		the hover display information
	 * @return the hover popup display information	 	  
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
