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

import junit.framework.TestCase;

/**
 * @since 1.0
 *
 */
public class AutoSelectTableViewerCollectionExtendedTest extends TestCase {
//
//	/*
//	 * Test method for 'org.eclipse.jface.internal.databinding.swt.CComboObservableCollection.getSelectedObject()'
//	 */
//	private TableViewer viewer;
//	
//	protected SelectionAwareObservableCollection getSelectionAwareObservable(String[] values) {
//		Shell shell = BindingScenariosTestSuite.getShell();
//		this.viewer = new TableViewer(shell, SWT.NONE);
//		TableViewerDescription description = new TableViewerDescription(viewer);
//		DataBindingContext ctx = DataBinding.createContext(new IObservableFactory[] {new AutoSelectTableViewerFactory()});
//		SelectionAwareObservableCollection  observableCollection = (SelectionAwareObservableCollection) ctx.createObservable(description);
//		observableCollection.setElements(Arrays.asList(values));
//		return observableCollection;
//	}
//	
//	protected Object getSelectedObjectOfControl() {
//		StructuredSelection selection = (StructuredSelection) this.viewer.getSelection();
//		if (selection.isEmpty()) {
//			return null;
//		}
//		return selection.getFirstElement();
//	}
//	
//	protected void setSelectedValueOfControl(String value) {
//		this.viewer.setSelection(new StructuredSelection(new String[]{value}));
//	}
//	
//	/*
//	 * Test method for 'org.eclipse.jface.internal.databinding.swt.CComboObservableCollection.setSelectedObject(Object)'
//	 */
//	public void testAutoSelect() {
//		SelectionAwareObservableCollection observable = getSelectionAwareObservable(new String[] {"foo", "bar"}); 
//		assertEquals("foo", getSelectedObjectOfControl());
//	}
}
