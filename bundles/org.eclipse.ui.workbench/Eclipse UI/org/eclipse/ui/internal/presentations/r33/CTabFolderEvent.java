/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.r33;


import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

/**
 * 
 */
public class CTabFolderEvent extends TypedEvent {
	/**
	 * The tab item for the operation.
	 */
 	public Widget item;

 	/**
	 * A flag indicating whether the operation should be allowed.
	 * Setting this field to <code>false</code> will cancel the operation.
	 * Applies to the close and showList events.
	 */
 	public boolean doit;

	/**
	 * The widget-relative, x coordinate of the chevron button
	 * at the time of the event.  Applies to the showList event.
	 * 
 	 * @since 3.0
	 */
 	public int x;
 	/**
 	 * The widget-relative, y coordinate of the chevron button
	 * at the time of the event.  Applies to the showList event.
	 * 
	 * @since 3.0
	 */
	public int y;
	/**
	 * The width of the chevron button at the time of the event.
	 * Applies to the showList event.
	 * 
	 * @since 3.0
	 */
	public int width;
	/**
	 * The height of the chevron button at the time of the event.
	 * Applies to the showList event.
	 * 
	 * @since 3.0
	 */
	public int height;

	static final long serialVersionUID = 3760566386225066807L;
	
/**
 * Constructs a new instance of this class.
 *
 * @param w the widget that fired the event
 */
CTabFolderEvent(Widget w) {
	super(w);
}

}
