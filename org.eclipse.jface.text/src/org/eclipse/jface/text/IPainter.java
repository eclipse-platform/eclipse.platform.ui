/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;



public interface IPainter {
	
	/** Paint reasons */
	int SELECTION=		0;
	int TEXT_CHANGE=	1;
	int KEY_STROKE=		2;
	int MOUSE_BUTTON= 4;
	int INTERNAL=			8;
	int CONFIGURATION= 16;
	

	/**	
	 * Disposes this painter.
	 * <p>
	 * XXX: The relationship with deactivate is not yet defined.
	 * </p>
	 * */
	void dispose();
	
	void paint(int reason);

	/**
	 * Deactivates the painter.
	 * <p>
	 * XXX: The relationship with dispose is not yet defined.
	 * </p>
	 */
	void deactivate(boolean redraw);
	
	void setPositionManager(IPaintPositionManager manager);
}
