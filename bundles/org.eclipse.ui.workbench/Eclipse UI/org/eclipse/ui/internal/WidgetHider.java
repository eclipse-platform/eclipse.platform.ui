/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.internal.dnd.DragUtil;

/**
 * This class can be instantiated on an SWT control, and will cause the control
 * to become invisible unless it is under the mouse pointer.
 * 
 * @since 3.0
 */
public class WidgetHider {
	
	private Control toHide;
	private Display display;
	private boolean enabled = false;
	
	private Listener mouseListener = new Listener() {
		public void handleEvent(Event event) {
			if (event.type == SWT.MouseMove) {
				
				if (toHide.isDisposed()) {
					// Shouldn't happen -- indicates leaky code. 
					dispose();
					return;
				}
				
				if (event.widget instanceof Control) {
				
					Rectangle displayWidgetBounds = DragUtil.getDisplayBounds(((Control)event.widget));
					
					Point position = new Point(event.x, event.y);
					position.x += displayWidgetBounds.x;
					position.y += displayWidgetBounds.y;
					
					Rectangle bounds = DragUtil.getDisplayBounds(toHide);
					
					toHide.setVisible(bounds.contains(position));
				}
			}
		}
	};
	
	public WidgetHider(Control toHide) {
		this.toHide = toHide;
		
		display = toHide.getDisplay();
		
		setEnabled(true);
	}
	
	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}
		this.enabled = enabled;
		
		if (enabled) {
			display.addFilter(SWT.MouseMove, mouseListener);
		} else {
			display.removeFilter(SWT.MouseMove, mouseListener);	
		}
		
	}
	
	public void dispose() {
		setEnabled(false);
	}
}
