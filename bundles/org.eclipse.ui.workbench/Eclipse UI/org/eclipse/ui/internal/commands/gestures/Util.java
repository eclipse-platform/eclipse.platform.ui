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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.commands.*;

public final class Util {

	public final static char EAST = 'E';
	public final static char NORTH = 'N';
	public final static char SOUTH = 'S';
	public final static char WEST = 'W';

	public static String keys(int data) {
		int count = 0;
		StringBuffer stringBuffer = new StringBuffer();		

		if ((data & SWT.ALT) > 0) {
			if (count > 0)
				stringBuffer.append(',');
	
			stringBuffer.append("alt");
			count++;
		}

		if ((data & SWT.COMMAND) > 0) {
			if (count > 0)
				stringBuffer.append(',');
	
			stringBuffer.append("command");
			count++;
		}

		if ((data & SWT.CTRL) > 0) {
			if (count > 0)
				stringBuffer.append(',');
	
			stringBuffer.append("ctrl");
			count++;
		}

		if ((data & SWT.SHIFT) > 0) {
			if (count > 0)
				stringBuffer.append(',');
	
			stringBuffer.append("shift");
			count++;
		}
	
		if (count == 0)
			stringBuffer.append("none");
			
		return stringBuffer.toString();
	}

	public static String recognize(Point[] points, int grid) {
		char c = 0;
		StringBuffer stringBuffer = new StringBuffer();
		int x0 = 0;
		int y0 = 0;

		for (int i = 0; i < points.length; i++) {
			Point point = points[i];

			if (i == 0) {
				x0 = point.getX();
				y0 = point.getY();
				continue;
			}

			int x1 = point.getX();
			int y1 = point.getY();
			int dx = (x1 - x0) / grid;
			int dy = (y1 - y0) / grid;

			if ((dx != 0) || (dy != 0)) {
				if (dx > 0 && c != EAST) {
					stringBuffer.append(c = EAST);
				} else if (dx < 0 && c != WEST) {
					stringBuffer.append(c = WEST);
				} else if (dy > 0 && c != SOUTH) {
					stringBuffer.append(c = SOUTH);
				} else if (dy < 0 && c != NORTH) {
					stringBuffer.append(c = NORTH);
				}

				x0 = x1;
				y0 = y1;
			}
		}

		return stringBuffer.toString();
	}
	
	public static void main(String[] args) {
		final int HEIGHT = 300;
		final int WIDTH = 400;

		Display display = new Display();
		Rectangle bounds = display.getBounds();
		Shell shell = new Shell(display);

		if (bounds.height >= HEIGHT && bounds.width >= WIDTH)
			shell.setBounds((bounds.x + bounds.width - WIDTH) / 2, (bounds.y + bounds.height - HEIGHT) / 2, WIDTH, HEIGHT);

		shell.setText(Util.class.getName());
		shell.open();
		Capture capture = Capture.create();

		capture.addCaptureListener(new CaptureListener() {
			public void gesture(Gesture gesture) {
				System.out.println("Pen: " + gesture.getPen() + "   Keys: " + keys(gesture.getData()) +  "   Points: " + gesture.getPoints().length +
					"   Gesture: " + recognize(gesture.getPoints(), 20));			
			}
		});

		capture.setControl(shell);

		while (!shell.isDisposed())
			if (!display.readAndDispatch())
				display.sleep();

		display.dispose();
	}
}
