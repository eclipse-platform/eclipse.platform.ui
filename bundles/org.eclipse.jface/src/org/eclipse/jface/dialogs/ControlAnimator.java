/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.dialogs;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 * ControlAnimator provides a simple implementation to display or hide a control. 
 * Other animations will be written as subclasses of this class.  By setting the animator
 * in the method {@link org.eclipse.jface.util.Policy#setAnimator(ControlAnimator)} 
 * a new type of animator can be plugged into JFace.
 * </p>
 * 
 * This class is not intended to be extended by clients.
 * 
 * @since 3.2
 */


public class ControlAnimator {
	
	/**
	 * A constant denoting the CLOSED animation state of 
	 * a control (value is 0)
	 */
	public static final int CLOSED = 0;

	/**
	 * A constant denoting the OPENING animation state of 
	 * a control (value is 1)
	 */
	public static final int OPENING = 1;

	/**
	 * A constant denoting the OPEN animation state of 
	 * a control (value is 2)
	 */
	public static final int OPEN = 2;

	/**
	 * A constant denoting the CLOSING animation state of 
	 * a control (value is 3)
	 */
	public static final int CLOSING = 3;
	
	private int state = CLOSED;
	
	/**
	 * Displays or hides the given control.
	 * 
	 * @param visible <code>true</code> if the control should be shown, 
	 * 		  and <code>false</code> otherwise.
	 * @param control the control to be displayed or hidden.
	 */
	public void setVisible(boolean visible, Control control){
		control.setVisible(visible);
		Rectangle parentBounds = control.getParent().getBounds();
		int bottom = parentBounds.y + parentBounds.height;		
		final int endY = visible ? bottom - control.getBounds().height
				: bottom;
		Point loc = control.getLocation();
		control.setLocation(loc.x,endY);
		setAnimationState(visible ? OPEN: CLOSED);
	}
	
	/**
	 * Sets the state of the control and whether or not
	 * it should be visible. The value should be one of 
	 * the following: {@link #OPENING}, {@link #OPEN}, 
	 * {@link #CLOSING}, or {@link #CLOSED}
	 * 
	 * @param state	the desired state of the control
	 */
	public void setAnimationState(int state) {
		this.state = state;
	}

	/**
	 * Returns the current state of the control.
	 * 
	 * @return the current state of the control: {@link #OPENING}, 
	 * 		   {@link #OPEN}, {@link #CLOSING}, or {@link #CLOSED}
	 */
	public int getAnimationState() {
		return state;
	}	
}
