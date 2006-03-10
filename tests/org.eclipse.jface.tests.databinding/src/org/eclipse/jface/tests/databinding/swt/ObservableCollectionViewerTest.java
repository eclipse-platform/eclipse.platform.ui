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


/**
 * @since 1.0
 *
 */
public class ObservableCollectionViewerTest extends AbstractGetAndSetSelectionObservableCollectionTest {
//
//	/*
//	 * Test method for 'org.eclipse.jface.internal.databinding.swt.CComboObservableCollection.getSelectedObject()'
//	 */
//	private AbstractListViewer viewer;
//	
//	protected SelectionAwareObservableCollection getSelectionAwareObservable(String[] values) {
//		Shell shell = BindingScenariosTestSuite.getShell();
//		this.viewer = new ListViewer(shell, SWT.NONE);
//		DataBindingContext ctx = DataBinding.createContext(new IObservableFactory[] {new SWTObservableFactory()});		
//		SelectionAwareObservableCollection  observableCollection = (SelectionAwareObservableCollection) ctx.createObservable(new Property(viewer, ViewersProperties.CONTENT, String.class, new Boolean(true)));
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
}
