/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.progress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.progress.WorkbenchJob;

public class AnimationItem {

	WorkbenchWindow window;
	private ProgressFloatingWindow floatingWindow;
	private boolean showingDetails = true;
	Canvas imageCanvas;
	GC imageCanvasGC;
	//An object used to preven concurrent modification issues
	private Object windowLock = new Object();

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param workbenchWindow
	 *            the window being created
	 * @param manager
	 *            the AnimationManager that will run this item.
	 */

	public AnimationItem(WorkbenchWindow workbenchWindow) {
		this.window = workbenchWindow;
	}

	/**
	 * Create the canvas that will display the image.
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {

		final AnimationManager manager = AnimationManager.getInstance();
		// Canvas to show the image.
		imageCanvas = new Canvas(parent, SWT.NONE);
		imageCanvas.setBackground(
			parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		imageCanvas.setToolTipText(ProgressMessages.getString("AnimationItem.HoverHelp")); //$NON-NLS-1$

		imageCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				paintImage(
					event,
					manager.getImage(),
					manager.getImageData()[0]);
			}
		});

		imageCanvasGC = new GC(imageCanvas);
		imageCanvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				imageCanvasGC.dispose();
			}
		});

		imageCanvas.addMouseListener(new MouseListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDoubleClick(MouseEvent arg0) {
				if (showingDetails)
					closeFloatingWindow();
				else
					openFloatingWindow();

				//Toggle the details flag
				showingDetails = !showingDetails;
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseDown(MouseEvent arg0) {
				//Do nothing
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
			 */
			public void mouseUp(MouseEvent arg0) {
				//Do nothing

			}
		});

		imageCanvas
			.getAccessible()
			.addAccessibleControlListener(new AccessibleControlAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.accessibility.AccessibleControlAdapter#getValue(org.eclipse.swt.accessibility.AccessibleControlEvent)
			 */
			public void getValue(AccessibleControlEvent arg0) {
				if (manager.isAnimated())
					arg0.result = ProgressMessages.getString("AnimationItem.InProgressStatus"); //$NON-NLS-1$
				else
					arg0.result = ProgressMessages.getString("AnimationItem.NotRunningStatus"); //$NON-NLS-1$
			}
		});

		imageCanvas.addHelpListener(new HelpListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.HelpListener#helpRequested(org.eclipse.swt.events.HelpEvent)
			 */
			public void helpRequested(HelpEvent e) {
				// XXX Auto-generated method stub

			}
		});

		manager.addItem(this);

	}

	/**
	 * Paint the image in the canvas.
	 * 
	 * @param event
	 *            The PaintEvent that generated this call.
	 * @param image
	 *            The image to display
	 * @param imageData
	 *            The array of ImageData. Required to show an animation.
	 */
	void paintImage(PaintEvent event, Image image, ImageData imageData) {

		Image paintImage = image;

		int w = imageData.width;
		int h = imageData.height;
		event.gc.drawImage(
			paintImage,
			0,
			0,
			imageData.width,
			imageData.height,
			imageData.x,
			imageData.y,
			w,
			h);
	}

	/**
	 * Get the SWT control for the receiver.
	 * 
	 * @return Control
	 */
	public Control getControl() {
		return imageCanvas;
	}

	/**
	 * Get the bounds of the image being displayed here.
	 * 
	 * @return Rectangle
	 */
	public Rectangle getImageBounds() {
		return AnimationManager.getInstance().getImageBounds();
	}

	/**
	 * Open a floating window for the receiver.
	 * 
	 * @param event
	 */
	void openFloatingWindow() {
		//Do we already have one?
		if(floatingWindow != null)
			return;
		
		//Don't bother if there is nothing showing yet
		if(!window.getShell().isVisible())
			return;
		
		floatingWindow =
			new ProgressFloatingWindow(window.getShell(), imageCanvas);

		WorkbenchJob floatingJob = new WorkbenchJob(ProgressMessages.getString("AnimationItem.openFloatingWindowJob")) { //$NON-NLS-1$
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				synchronized (windowLock) {
					if (floatingWindow == null)
						return Status.CANCEL_STATUS;
					else {
						//Do not bother if the control is disposed
						if(getControl().isDisposed()){
							floatingWindow = null;
							return Status.CANCEL_STATUS;
						}
						else{
							floatingWindow.open();
							return Status.OK_STATUS;
						}
					}
				}

			}
		};
		floatingJob.setSystem(true);
		floatingJob.schedule(500);

	}

	/**
	 * The animation has begun.
	 */
	void animationStart() {
		if (showingDetails)
			openFloatingWindow();
	}

	/**
	 * The animation has ended.
	 */
	void animationDone() {
		closeFloatingWindow();
	}

	/**
	 * Close the floating window.
	 */
	private void closeFloatingWindow() {
		synchronized (windowLock) {
			if (floatingWindow != null) {
				floatingWindow.close();
				floatingWindow = null;
			}
		}

	}

	/**
	 * Get the preferred width of the receiver.
	 * 
	 * @return
	 */
	public int getPreferredWidth() {
		return AnimationManager.getInstance().getPreferredWidth() + 5;
	}

}
