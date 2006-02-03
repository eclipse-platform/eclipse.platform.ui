/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.article.views;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * Sample view for the example.
 * 
 * @author Anthony Hunter
 *
 */
public class SampleView
    extends ViewPart
    implements ITabbedPropertySheetPageContributor {

    private ListViewer viewer;

    private Group grp1;

    /**
     * The constructor.
     */
    public SampleView() {
        //
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     */
    public void createPartControl(Composite parent) {
        // create all the GUI controls
        // create two groups
        viewer = new ListViewer(parent, SWT.SINGLE);

        grp1 = new Group(parent, SWT.NONE);
        grp1.setText("Preview");//$NON-NLS-1$
        RowLayout rowLayout = new RowLayout();
        grp1.setLayout(rowLayout);

        Button btn = new Button(grp1, SWT.PUSH);
        btn.setText("Hello");//$NON-NLS-1$

        // fill in the element
        ArrayList ctlList = new ArrayList();
        ButtonElement btnEl = new ButtonElement(btn, "Button");//$NON-NLS-1$
        ctlList.add(btnEl);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.setInput(ctlList);
        getSite().setSelectionProvider(viewer);

    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(Class)
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IPropertySheetPage.class)
            return new TabbedPropertySheetPage(this);
        return super.getAdapter(adapter);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {
        super.dispose();
    }

    public String getContributorId() {
        return getSite().getId();
    }

}