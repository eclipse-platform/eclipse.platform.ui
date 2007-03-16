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
package org.eclipse.ui.part;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

/**
 * Class <code>DrillDownComposite</code> implements a simple web
 * style navigation metaphor.  Home, back, and "drill into" buttons are
 * added to a tree viewer for easier navigation.
 * <p>
 * To use the DrillDownComposite..
 * </p>
 * <ul>
 * <li>Create an instance of <code>DrillDownComposite</code>.</li>
 * <li>Create a tree viewer.  </li>
 * <li>Pass the second tree viewer into the composite by 
 *   calling <code>setChildTree</code>.</li>
 * </ol>
 */
public class DrillDownComposite extends Composite {
    private ToolBarManager toolBarMgr;

    private TreeViewer fChildTree;

    private DrillDownAdapter adapter;

    /**
     * Constructs a new DrillDownTreeViewer.
     *
     * @param parent the parent composite for this control
     * @param style the SWT style for this control
     */
    public DrillDownComposite(Composite parent, int style) {
        super(parent, style);
        createNavigationButtons();
    }

    /**
     * Creates the navigation buttons for this viewer.
     */
    protected void createNavigationButtons() {
        GridData gid;
        GridLayout layout;

        // Define layout.
        layout = new GridLayout();
        layout.marginHeight = layout.marginWidth = layout.horizontalSpacing = layout.verticalSpacing = 0;
        setLayout(layout);

        // Create a toolbar.
        toolBarMgr = new ToolBarManager(SWT.FLAT);
        ToolBar toolBar = toolBarMgr.createControl(this);
        gid = new GridData();
        gid.horizontalAlignment = GridData.FILL;
        gid.verticalAlignment = GridData.BEGINNING;
        toolBar.setLayoutData(gid);
    }

    /**
     * Sets the child viewer.  This method should only be called once, after the
     * viewer has been created.
     *
     * @param aViewer the new child viewer
     */
    public void setChildTree(TreeViewer aViewer) {
        // Save viewer.
        fChildTree = aViewer;

        // Create adapter.
        adapter = new DrillDownAdapter(fChildTree);
        adapter.addNavigationActions(toolBarMgr);
        toolBarMgr.update(true);

        // Set tree layout.
        fChildTree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        layout();
    }
}
