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

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Control;

public final class XCapture {

	public static XCapture create() {
		return new XCapture();
	}		

	private List captureListeners;
	private boolean capturing;
	private Control control;
	private int data;
	private MouseListener mouseListener;
	private MouseMoveListener mouseMoveListener;
	private int pen;
	private List points;	
	
	private XCapture() {
		super();
		captureListeners = new ArrayList();

		mouseListener = new MouseListener() {
			public void mouseDoubleClick(MouseEvent mouseEvent) {
			}

			public void mouseDown(MouseEvent mouseEvent) {
				if (!capturing) {
					capturing = true;
					data = mouseEvent.stateMask;
					pen = mouseEvent.button;
					points.clear();
					points.add(new XPoint(mouseEvent.x, mouseEvent.y));
					control.addMouseMoveListener(mouseMoveListener);
				}
			}

			public void mouseUp(MouseEvent mouseEvent) {
				if (capturing && mouseEvent.button == pen) {
					control.removeMouseMoveListener(mouseMoveListener);
					points.add(new XPoint(mouseEvent.x, mouseEvent.y));
					XCaptureEvent captureEvent = XCaptureEvent.create(data, pen, (XPoint[]) points.toArray(new XPoint[points.size()]));
					capturing = false;
					data = 0;
					pen = 0;
					points.clear();
					Iterator iterator = captureListeners.iterator();

					while (iterator.hasNext())
						((XCaptureListener) iterator.next()).capture(captureEvent);
				}
			}
		};

		mouseMoveListener = new MouseMoveListener() {
			public void mouseMove(MouseEvent mouseEvent) {
				if (capturing)
					points.add(new XPoint(mouseEvent.x, mouseEvent.y));
			}
		};	
		
		points = new ArrayList();
	}

	public void addCaptureListener(XCaptureListener captureListener) {
		captureListeners.add(captureListener);	
	}

	public Control getControl() {
		return control;	
	}

	public void removeCaptureListener(XCaptureListener captureListener) {
		captureListeners.remove(captureListener);			
	}

	public void setControl(Control control) {
		if (this.control != control) {
			if (this.control != null) {
				control.removeMouseMoveListener(mouseMoveListener);
				control.removeMouseListener(mouseListener);
			}
			
			this.control = control;
			capturing = false;
			data = 0;
			pen = 0;
			points.clear();

			if (this.control != null)
				control.addMouseListener(mouseListener);
		}
	}
}
