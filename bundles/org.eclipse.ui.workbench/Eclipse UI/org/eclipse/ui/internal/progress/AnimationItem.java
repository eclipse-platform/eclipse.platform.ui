/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * The AnimationItem is the class that manages the animation for the progress.
 */
public abstract class AnimationItem {
    WorkbenchWindow window;

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
        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.progress.AnimationItem.IAnimationContainer#animationDone()
         */
        public void animationDone() {
            //Do nothing by default
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.progress.AnimationItem.IAnimationContainer#animationStart()
         */
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
    public AnimationItem(WorkbenchWindow workbenchWindow) {
        this.window = workbenchWindow;
    }

    /**
     * Create the canvas that will display the image.
     * 
     * @param parent
     */
    public void createControl(Composite parent) {

        Control animationItem = createAnimationItem(parent);

        animationItem.addMouseListener(new MouseListener() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
             */
            public void mouseDoubleClick(MouseEvent arg0) {
                ProgressManagerUtil.openProgressView(AnimationItem.this.window);
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
        animationItem.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                AnimationManager.getInstance().removeItem(AnimationItem.this);
            }
        });
        AnimationManager.getInstance().addItem(this);
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
        return AnimationManager.getInstance().getPreferredWidth() + 5;
    }

    /**
     * Set the container that will be updated when this runs.
     * @param container The animationContainer to set.
     */
    void setAnimationContainer(IAnimationContainer container) {
        this.animationContainer = container;
    }
}
