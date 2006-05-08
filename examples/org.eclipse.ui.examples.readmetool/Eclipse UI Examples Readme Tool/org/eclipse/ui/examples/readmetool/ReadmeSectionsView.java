/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.readmetool;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;

/**
 * This class demonstrates a simple view containing a single viewer.
 */
public class ReadmeSectionsView extends ViewPart implements ISelectionListener {
    ListViewer viewer;

    /**
     * Creates a new ReadmeSectionsView .
     */
    public ReadmeSectionsView() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchPart
     */
    public void createPartControl(Composite parent) {
        viewer = new ListViewer(parent);

        PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(),
				IReadmeConstants.SECTIONS_VIEW_CONTEXT);

        // if the objects in the viewer implement IWorkbenchAdapter,
        // these generic content and label providers can be used.
        viewer.setContentProvider(new WorkbenchContentProvider());
        viewer.setLabelProvider(new WorkbenchLabelProvider());

        // add myself as a global selection listener
        getSite().getPage().addSelectionListener(this);

        // prime the selection
        selectionChanged(null, getSite().getPage().getSelection());
    }

    /**
     * The <code>ReadmeSectionView</code> implementation of this 
     * <code>IWorkbenchPart</code> method runs super
     * and removes itself from the global selection listener. 
     */
    public void dispose() {
        super.dispose();
        getSite().getPage().removeSelectionListener(this);
    }

    /* (non-Javadoc)
     * Method declared on ISelectionListener
     */
    public void selectionChanged(IWorkbenchPart part, ISelection sel) {
        //if the selection is a readme file, get its sections.
        AdaptableList input = ReadmeModelFactory.getInstance().getSections(sel);
        viewer.setInput(input);
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchPart
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }
}
