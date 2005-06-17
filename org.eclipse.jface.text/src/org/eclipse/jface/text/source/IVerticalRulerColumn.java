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
package org.eclipse.jface.text.source;


import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * A vertical ruler column is an element that can be added to a composite
 * vertical ruler ({@link org.eclipse.jface.text.source.CompositeRuler}). A
 * composite vertical ruler is a vertical ruler with  dynamically changing
 * appearance and behavior depending on its actual arrangement of ruler columns.
 * A vertical ruler column supports a subset of the contract of a vertical
 * ruler.
 *
 * @see org.eclipse.jface.text.source.CompositeRuler
 * @since 2.0
 */
public interface IVerticalRulerColumn {

	/**
	 * Associates an annotation model with this ruler column.
	 * A value <code>null</code> is acceptable and clears the ruler.
	 *
	 * @param model the new annotation model, may be <code>null</code>
	 */
	void setModel(IAnnotationModel model);

	/**
	 * Redraws this column.
	 */
	void redraw();

	/**
	 * Creates the column's SWT control.
	 *
	 * @param parentRuler the parent ruler of this column
	 * @param parentControl the control of the parent ruler
	 * @return the column's SWT control
	 */
	Control createControl(CompositeRuler parentRuler, Composite parentControl);

	/**
	 * Returns the column's SWT control.
	 *
	 * @return the column's SWT control
	 */
	Control getControl();

	/**
	 * Returns the width of this column's control.
	 *
	 * @return the width of this column's control
	 */
	int getWidth();

	/**
	 * Sets the font of this ruler column.
	 *
	 * @param font the new font of the ruler column
	 */
	void setFont(Font font);
}
