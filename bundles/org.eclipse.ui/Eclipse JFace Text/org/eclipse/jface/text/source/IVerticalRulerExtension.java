package org.eclipse.jface.text.source;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.swt.graphics.Font;

/**
 * Extension interface to <code>IVerticalRuler</code>.
 */
public interface IVerticalRulerExtension {
	
	/**
	 * Sets the font of this vertical ruler.
	 * 
	 * @param font the new font of the vertical ruler
	 */
	void setFont(Font font);
	
	/**
	 * Sets the location of the last mouse button activity.
	 */
	void setLocationOfLastMouseButtonActivity(int x, int y);

}
