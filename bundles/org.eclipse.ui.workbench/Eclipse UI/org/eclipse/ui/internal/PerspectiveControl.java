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
package org.eclipse.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * The PerspectiveControl is the control used to display the currently selected
 * perspective and other available ones.
 */
public class PerspectiveControl {

	IDEWorkbenchWindow window;
	Canvas canvas;

	/**
	 * Create a new instance of the receiver with the supplied window.
	 * 
	 * @param controlWindow
	 */
	public PerspectiveControl(IDEWorkbenchWindow controlWindow) {
		super();
		window = controlWindow;
	}

	/**
	 * Create the control for the receiver.
	 * 
	 * @param parent
	 */
	void createControl(Composite parent) {
		canvas = new Canvas(parent, SWT.NULL);

		canvas.addMouseMoveListener(new MouseMoveListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseMove(MouseEvent e) {
				System.out.println("Move");

			}
		});

		canvas.addMouseListener(new MouseAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseAdapter#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDown(MouseEvent e) {
				System.out.println("Click");
			}
		});

		canvas.addKeyListener(new KeyAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
			 */
			public void keyReleased(KeyEvent e) {
				System.out.println("Released");
			}
		});

		canvas.addPaintListener(new PaintListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
			 */
			public void paintControl(PaintEvent event) {
				
				ImageDescriptor descriptor =
					window
						.getPerspectiveService()
						.getActivePerspective()
						.getImageDescriptor();
				Image image = descriptor.createImage();
				ImageData imageData = descriptor.getImageData();

				int w = imageData.width;
				int h = imageData.height;
				event.gc.drawImage(
					image,
					0,
					0,
					imageData.width,
					imageData.height,
					imageData.x + 5,
					imageData.y + 5,
					w,
					h);

				event.gc.drawText(
					window
						.getPerspectiveService()
						.getActivePerspective()
						.getLabel(),
					10 + imageData.width,
					10);

			}
		});

	}

	/**
	 * Return the control for the receiver.
	 * 
	 * @return Control
	 */
	Control getControl() {
		return canvas;
	}

}
