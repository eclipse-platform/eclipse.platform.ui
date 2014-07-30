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
package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelContentProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

public class TestTableTree extends TestBrowser {

    TableTreeViewer fViewer;

    Action fExpandAllAction;

    public TestTableTree() {
        super();
        fExpandAllAction = new ExpandAllAction("Expand All", this);
    }

    @Override
	public Viewer createViewer(Composite parent) {
        TableTreeViewer viewer = new TableTreeViewer(parent);
        viewer.setContentProvider(new TestModelContentProvider());
        viewer.setLabelProvider(new TestTableTreeLabelProvider());
        viewer.getTableTree().getTable().setLinesVisible(true);

        TableLayout layout = new TableLayout();
        viewer.getTableTree().getTable().setLayout(layout);
        viewer.getTableTree().getTable().setHeaderVisible(true);
        String headers[] = { "First Column", "Second Column" };

        ColumnLayoutData layouts[] = { new ColumnWeightData(100),
                new ColumnWeightData(100) };

        final TableColumn columns[] = new TableColumn[headers.length];

        for (int i = 0; i < headers.length; i++) {
            layout.addColumnData(layouts[i]);
            TableColumn tc = new TableColumn(viewer.getTableTree().getTable(),
                    SWT.NONE, i);
            tc.setResizable(layouts[i].resizable);
            tc.setText(headers[i]);
            columns[i] = tc;
        }
        if (fViewer == null)
            fViewer = viewer;

        return viewer;
    }

    public static void main(String[] args) {
        TestBrowser browser = new TestTableTree();
        if (args.length > 0 && args[0].equals("-twopanes"))
            browser.show2Panes();
        browser.setBlockOnOpen(true);
        browser.open(TestElement.createModel(3, 10));
    }

    /**
     * Adds the expand all action to the tests menu.
     */
    @Override
	protected void viewerFillMenuBar(MenuManager mgr) {
        MenuManager testMenu = (MenuManager) (mgr.findMenuUsingPath("tests"));
        testMenu.add(new Separator());
        testMenu.add(fExpandAllAction);
    }
}
