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
package org.eclipse.ui.internal.progress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.misc.Assert;

/**
 * The IconAnimationItem is the item that shows progress using an icon.
 */
public class IconAnimationItem extends AnimationItem {

	Canvas imageCanvas;
	GC imageCanvasGC;
	ImageAnimationProcessor animationProcessor;

	/**
	 * Create an instance of the receiver in the window.
	 * 
	 * @param workbenchWindow
	 *            The window this is being created in.
	 * @param processor
	 *            The processor that handles this item.
	 */
	IconAnimationItem(WorkbenchWindow workbenchWindow,
			IAnimationProcessor processor) {
		super(workbenchWindow);
		
		Assert.isTrue(processor instanceof ImageAnimationProcessor);
		animationProcessor = (ImageAnimationProcessor) processor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AnimationItem#createAnimationItem(org.eclipse.swt.widgets.Control)
	 */
	protected Control createAnimationItem(Composite parent) {

		final AnimationManager manager = AnimationManager.getInstance();
		// Canvas to show the image.
		imageCanvas = new Canvas(parent, SWT.NONE);
		imageCanvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent event) {
				paintImage(event, animationProcessor.getImage(),
						animationProcessor.getImageData()[0]);
			}
		});
		imageCanvasGC = new GC(imageCanvas);
		imageCanvas.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				imageCanvasGC.dispose();
			}
		});

		imageCanvas.getAccessible().addAccessibleControlListener(
				new AccessibleControlAdapter() {
					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.swt.accessibility.AccessibleControlAdapter#getValue(org.eclipse.swt.accessibility.AccessibleControlEvent)
					 */
					public void getValue(AccessibleControlEvent arg0) {
						if (manager.isAnimated())
							arg0.result = ProgressMessages
									.getString("AnimationItem.InProgressStatus"); //$NON-NLS-1$
						else
							arg0.result = ProgressMessages
									.getString("AnimationItem.NotRunningStatus"); //$NON-NLS-1$
					}
				});

		return imageCanvas;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.progress.AnimationItem#getControl()
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
		return animationProcessor.getImageBounds();
	}
}
