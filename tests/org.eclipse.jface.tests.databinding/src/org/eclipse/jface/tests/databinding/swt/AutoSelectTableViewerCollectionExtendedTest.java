/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.databinding.swt;

import java.util.Arrays;

import junit.framework.TestCase;

import org.eclipse.jface.internal.provisional.databinding.DataBinding;
import org.eclipse.jface.internal.provisional.databinding.IDataBindingContext;
import org.eclipse.jface.internal.provisional.databinding.IUpdatableFactory;
import org.eclipse.jface.internal.provisional.databinding.SelectionAwareUpdatableCollection;
import org.eclipse.jface.internal.provisional.databinding.viewers.AutoSelectTableViewerFactory;
import org.eclipse.jface.internal.provisional.databinding.viewers.TableViewerDescription;
import org.eclipse.jface.tests.databinding.scenarios.BindingScenariosTestSuite;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 *
 */
public class AutoSelectTableViewerCollectionExtendedTest extends TestCase {

	/*
	 * Test method for 'org.eclipse.jface.tests.databinding.swt.CComboUpdatableCollection.getSelectedObject()'
	 */
	private TableViewer viewer;
	
	protected SelectionAwareUpdatableCollection getSelectionAwareUpdatable(String[] values) {
		Shell shell = BindingScenariosTestSuite.getShell();
		this.viewer = new TableViewer(shell, SWT.NONE);
		TableViewerDescription description = new TableViewerDescription(viewer);
		IDataBindingContext ctx = DataBinding.createContext(new IUpdatableFactory[] {new AutoSelectTableViewerFactory()});
		SelectionAwareUpdatableCollection  updatableCollection = (SelectionAwareUpdatableCollection) ctx.createUpdatable(description);
		updatableCollection.setElements(Arrays.asList(values));
		return updatableCollection;
	}
	
	protected Object getSelectedObjectOfControl() {
		StructuredSelection selection = (StructuredSelection) this.viewer.getSelection();
		if (selection.isEmpty()) {
			return null;
		}
		return selection.getFirstElement();
	}
	
	protected void setSelectedValueOfControl(String value) {
		this.viewer.setSelection(new StructuredSelection(new String[]{value}));
	}
	
	/*
	 * Test method for 'org.eclipse.jface.tests.databinding.swt.CComboUpdatableCollection.setSelectedObject(Object)'
	 */
	public void testAutoSelect() {
		SelectionAwareUpdatableCollection updatable = getSelectionAwareUpdatable(new String[] {"foo", "bar"}); 
		assertEquals("foo", getSelectedObjectOfControl());
	}
}
