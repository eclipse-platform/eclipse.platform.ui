/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.progress;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.internal.WorkbenchWindow;

/**
 * The ProgressRegion is class for the region of the workbench where the
 * progress line and the animation item are shown.
 */
public class ProgressRegion {
    ProgressCanvasViewer viewer;

    AnimationItem item;

    Composite region;

    WorkbenchWindow workbenchWindow;

    /**
     * Create a new instance of the receiver.
     */
    public ProgressRegion() {
        //No default behavior.
    }

    /**
     * Create the contents of the receiver in the parent. Use the window for the
     * animation item.
     * 
     * @param parent
     *            The parent widget of the composite.
     * @param window
     *            The WorkbenchWindow this is in.
     * @return Control
     */
    public Control createContents(Composite parent, WorkbenchWindow window) {
        workbenchWindow = window;

        region = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.numColumns = 3;
        region.setLayout(gl);

        new Label(region, SWT.SEPARATOR);

        viewer = new ProgressCanvasViewer(region, SWT.NO_FOCUS, 1, 36);
        viewer.setUseHashlookup(true);
        Control viewerControl = viewer.getControl();
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = viewer.getSizeHints().x;
        viewerControl.setLayoutData(gd);

        int widthPreference = AnimationManager.getInstance()
                .getPreferredWidth() + 25;
        item = new ProgressAnimationItem(this);
        item.createControl(region);

        item.setAnimationContainer(new AnimationItem.IAnimationContainer() {
            /* (non-Javadoc)
             * @see org.eclipse.ui.internal.progress.AnimationItem.IAnimationContainer#animationDone()
             */
            public void animationDone() {
                //Add an extra refresh to the viewer in case
                //of stale input if the controls are not disposed
                if (viewer.getControl().isDisposed())
                    return;
                viewer.refresh();
            }

            /* (non-Javadoc)
             * @see org.eclipse.ui.internal.progress.AnimationItem.IAnimationContainer#animationStart()
             */
            public void animationStart() {
                // Nothing by default here.

            }
        });
        Control itemControl = item.getControl();
        gd = new GridData(GridData.FILL_VERTICAL);
        gd.widthHint = widthPreference;
        itemControl.setLayoutData(gd);

        viewerControl.addMouseListener(new MouseAdapter() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.MouseAdapter#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
             */
            public void mouseDoubleClick(MouseEvent e) {
                processDoubleClick();
            }
        });

        //Never show debug info
        IContentProvider provider = new ProgressViewerContentProvider(viewer,
                false);
        viewer.setContentProvider(provider);
        viewer.setInput(provider);
        viewer.setLabelProvider(new ProgressViewerLabelProvider(viewerControl));
        viewer.setSorter(ProgressManagerUtil.getProgressViewerSorter());
        return region;
    }

    /**
     * Return the animationItem for the receiver.
     * 
     * @return AnimationItem
     */
    public AnimationItem getAnimationItem() {
        return item;
    }

    /**
     * Return the control for the receiver.
     * 
     * @return Control
     */
    public Control getControl() {
        return region;
    }

    /**
     * Process the double click event.
     */
    public void processDoubleClick() {
        ProgressManagerUtil.openProgressView(workbenchWindow);
    }
}
