/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text.source;


import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.text.ITextViewer;


/**
 * This interface defines a visual component which may serve
 * text viewers as a line oriented annotation presentation 
 * area. This interfaces comprises three contracts:
 * <ul>
 * <li>	The vertical ruler retrieves the annotations it presents from an annotation model.
 * <li>	The ruler is a visual component which must be integrated in a hierarchy of SWT controls.
 * <li> The ruler provides interested clients with mapping and
 * 		interaction information. This covers the mapping between
 * 		coordinates of the ruler's control and line numbers based 
 * 		on the connected text viewer's document (<code>IVerticalRulerInfo</code>).
 * </ul>
 * Clients may implement this interface or use the default implementation provided
 * by <code>VerticalRuler</code>.
 *  
 * @see ITextViewer
 * @see IVerticalRulerInfo
 */
public interface IVerticalRuler extends IVerticalRulerInfo {

	/**
	 * Associates an annotation model with this ruler.
	 * If the ruler is visible it must display those annotions
	 * of the annotation model whose visual representation overlaps
	 * with the viewport of the rulers source viewer.
	 * A value <code>null</code> is acceptable and clears the ruler.
	 *
	 * @param model the new annotation model, may be <code>null</code>
	 */
	void setModel(IAnnotationModel model);
		
	/**
	 * Returns the current annotation model of this ruler or <code>null</code>
	 * if the ruler has no model.
	 *
	 * @return this ruler's annotation model or <code>null</code> if there is no model
	 */
	IAnnotationModel getModel();
		
	/**
	 * Forces the vertical ruler to synchronize itself with its 
	 * annotation model and its viewer's viewport.
	 */
	void update();
	
	/**
	 * Creates the ruler's SWT control.
	 *
	 * @param parent the parent control of the ruler's control
	 * @param textViewer the text viewer to which this ruler belongs
	 * @return the ruler's SWT control
	 */
	Control createControl(Composite parent, ITextViewer textViewer);
}
