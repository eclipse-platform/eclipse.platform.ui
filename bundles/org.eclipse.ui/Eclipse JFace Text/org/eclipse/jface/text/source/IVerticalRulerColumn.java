package org.eclipse.jface.text.source;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;


/**
 * @see org.eclipse.jface.text.source.CompositeRuler
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
