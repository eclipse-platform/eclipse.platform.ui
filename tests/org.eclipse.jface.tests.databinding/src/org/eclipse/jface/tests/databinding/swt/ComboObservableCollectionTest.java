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
public class ComboObservableCollectionTest extends AbstractGetAndSetSelectionObservableCollectionTest {
//
//	/*
//	 * Test method for 'org.eclipse.jface.internal.databinding.swt.CComboObservableCollection.getSelectedObject()'
//	 */
//	private Combo combo;
//	
//	protected SelectionAwareObservableCollection getSelectionAwareObservable(String[] values) {
//		Shell shell = BindingScenariosTestSuite.getShell();
//		this.combo = new Combo(shell, SWT.NONE);
//		for (int i = 0; i < values.length; i++) {
//			this.combo.add(values[i]);
//		}
//		DataBindingContext ctx = DataBinding.createContext(new IObservableFactory[] {new SWTObservableFactory()});
//		return (SelectionAwareObservableCollection) ctx.createObservable(new Property(combo, SWTProperties.ITEMS, String.class, new Boolean(true)));
//	}
//	
//	protected Object getSelectedObjectOfControl() {
//		int selectionIndex = this.combo.getSelectionIndex();
//		if (selectionIndex != -1) {
//			return this.combo.getItem(selectionIndex);
//		} 
//		return null;
//	}
//	
//	protected void setSelectedValueOfControl(String value) {
//		this.combo.select(this.combo.indexOf(value));
//	}
}
