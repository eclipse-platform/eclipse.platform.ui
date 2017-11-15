/*******************************************************************************
 * Copyright (c) 2003, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * The AnimationItem is the class that manages the animation for the progress.
 */
public abstract class AnimationItem {

	private AnimationManager animationManager;

    interface IAnimationContainer {
        /**
         * The animation has started.
         */
        public abstract void animationStart();

        /**
         * The animation has ended.
         */
        public abstract void animationDone();
    }

    //Create a containter that does nothing by default
    IAnimationContainer animationContainer = new IAnimationContainer() {
        @Override
		public void animationDone() {
            //Do nothing by default
        }

        @Override
		public void animationStart() {
            //Do nothing by default
        }
    };

    /**
     * Create a new instance of the receiver.
     *
     * @param workbenchWindow
     *            the window being created
     */
    public AnimationItem(AnimationManager animationManager) {
    	this.animationManager = animationManager;
    }

    /**
     * Create the canvas that will display the image.
     *
     * @param parent
     */
    public void createControl(Composite parent) {

        Control animationItem = createAnimationItem(parent);

		animationItem
				.addMouseListener(MouseListener.mouseDoubleClickAdapter(e -> ProgressManagerUtil.openProgressView()));
		animationItem.addDisposeListener(e -> animationManager.removeItem(AnimationItem.this));
        animationManager.addItem(this);
    }

    /**
     * Create the animation item control.
     * @param parent the parent Composite
     * @return Control
     */
    protected abstract Control createAnimationItem(Composite parent);

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
        event.gc.drawImage(image, 0, 0);
    }

    /**
     * Get the SWT control for the receiver.
     *
     * @return Control
     */
    public abstract Control getControl();

    /**
     * The animation has begun.
     */
    void animationStart() {
        animationContainer.animationStart();
    }

    /**
     * The animation has ended.
     */
    void animationDone() {
        animationContainer.animationDone();
    }

    /**
     * Get the preferred width of the receiver.
     *
     * @return int
     */
    public int getPreferredWidth() {
        return animationManager.getPreferredWidth() + 5;
    }

    /**
     * Set the container that will be updated when this runs.
     * @param container The animationContainer to set.
     */
    void setAnimationContainer(IAnimationContainer container) {
        this.animationContainer = container;
    }

	/**
	 * @return Returns the window.
	 */
//	public WorkbenchWindow getWindow() {
//		return window;
//	}
}
