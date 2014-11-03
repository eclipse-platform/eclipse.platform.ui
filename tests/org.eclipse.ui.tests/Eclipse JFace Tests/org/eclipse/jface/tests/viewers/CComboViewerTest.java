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
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.0
 */
public class CComboViewerTest extends StructuredViewerTest {
    public CComboViewerTest(String name) {
        super(name);
    }

    @Override
	protected StructuredViewer createViewer(Composite parent) {
    	CCombo cCombo = new CCombo(parent, SWT.READ_ONLY | SWT.BORDER);
        ComboViewer viewer = new ComboViewer(cCombo);
        viewer.setContentProvider(new TestModelContentProvider());
        return viewer;
    }

    @Override
	protected int getItemCount() {
        TestElement first = fRootElement.getFirstChild();
        CCombo list = (CCombo) fViewer.testFindItem(first);
        return list.getItemCount();
    }

    @Override
	protected String getItemText(int at) {
        CCombo list = (CCombo) fViewer.getControl();
        return list.getItem(at);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(CComboViewerTest.class);
    }

    /**
     * TODO: Determine if this test is applicable to ComboViewer
     */
    @Override
	public void testInsertChild() {

    }
}
