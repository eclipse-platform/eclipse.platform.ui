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

package org.eclipse.ui.internal.gestures;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.keys.SWTKeySupport;

public final class GestureSupport {

    public static void main(String[] args) {
        final int HEIGHT = 300;
        final int WIDTH = 400;
        Display display = new Display();
        Rectangle bounds = display.getBounds();
        Shell shell = new Shell(display);

        if (bounds.height >= HEIGHT && bounds.width >= WIDTH)
            shell.setBounds((bounds.x + bounds.width - WIDTH) / 2, (bounds.y
                    + bounds.height - HEIGHT) / 2, WIDTH, HEIGHT);

        shell.setText(GestureSupport.class.getName());
        shell.open();
        Capture capture = new Capture();

        capture.addCaptureListener(new ICaptureListener() {
            public void capture(CaptureEvent captureEvent) {
                System.out.println("Pen: " + captureEvent.getPen() + //$NON-NLS-1$ 
                        " Key Stroke: " //$NON-NLS-1$
                        + SWTKeySupport
                                .convertAcceleratorToKeyStroke(captureEvent
                                        .getData()) + //$NON-NLS-1$ 
                        " Points: " + captureEvent.getPoints().length + //$NON-NLS-1$
                        " Gesture: " + recognize(captureEvent.getPoints(), 20)); //$NON-NLS-1$		
            }
        });

        capture.setControl(shell);

        while (!shell.isDisposed())
            if (!display.readAndDispatch())
                display.sleep();

        display.dispose();
    }

    public static String recognize(Point[] points, int sensitivity) {
        char stroke = '\0';
        StringBuffer sequence = new StringBuffer();
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
            int dx = (x1 - x0) / sensitivity;
            int dy = (y1 - y0) / sensitivity;

            if (dx != 0 || dy != 0) {
                if (dx > 0 && stroke != 'E')
                    sequence.append(stroke = 'E');
                else if (dx < 0 && stroke != 'W')
                    sequence.append(stroke = 'W');
                else if (dy > 0 && stroke != 'S')
                    sequence.append(stroke = 'S');
                else if (dy < 0 && stroke != 'N')
                    sequence.append(stroke = 'N');

                x0 = x1;
                y0 = y1;
            }
        }

        return sequence.toString();
    }

    private GestureSupport() {
    }
}