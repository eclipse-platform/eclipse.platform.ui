/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands.gestures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.commands.*;

public final class Capture {

	public static Capture create() {
		return new Capture();
	}		

	private List captureListeners;
	private boolean capturing;
	private Control control;
	private int data;
	private MouseListener mouseListener;
	private MouseMoveListener mouseMoveListener;
	private int pen;
	private List points;	
	
	private Capture() {
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
					points.add(Point.create(mouseEvent.x, mouseEvent.y));
					control.addMouseMoveListener(mouseMoveListener);
				}
			}

			public void mouseUp(MouseEvent mouseEvent) {
				if (capturing && mouseEvent.button == pen) {
					control.removeMouseMoveListener(mouseMoveListener);
					points.add(Point.create(mouseEvent.x, mouseEvent.y));
					Gesture gesture = Gesture.create(data, pen, (Point[]) points.toArray(new Point[points.size()]));
					capturing = false;
					data = 0;
					pen = 0;
					points.clear();
					Iterator iterator = captureListeners.iterator();

					while (iterator.hasNext())
						((CaptureListener) iterator.next()).gesture(gesture);
				}
			}
		};

		mouseMoveListener = new MouseMoveListener() {
			public void mouseMove(MouseEvent mouseEvent) {
				if (capturing)
					points.add(Point.create(mouseEvent.x, mouseEvent.y));
			}
		};	
		
		points = new ArrayList();
	}

	public void addCaptureListener(CaptureListener captureListener) {
		captureListeners.add(captureListener);	
	}

	public Control getControl() {
		return control;	
	}

	public void removeCaptureListener(CaptureListener captureListener) {
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
