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

import org.eclipse.jface.internal.provisional.databinding.DataBinding;
import org.eclipse.jface.internal.provisional.databinding.IDataBindingContext;
import org.eclipse.jface.internal.provisional.databinding.IUpdatableFactory;
import org.eclipse.jface.internal.provisional.databinding.Property;
import org.eclipse.jface.internal.provisional.databinding.SelectionAwareUpdatableCollection;
import org.eclipse.jface.internal.provisional.databinding.swt.SWTProperties;
import org.eclipse.jface.internal.provisional.databinding.swt.SWTUpdatableFactory;
import org.eclipse.jface.tests.databinding.scenarios.BindingScenariosTestSuite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 *
 */
public class CComboUpdatableCollectionTest extends AbstractGetAndSetSelectionUpdatableCollectionTest {

	/*
	 * Test method for 'org.eclipse.jface.tests.databinding.swt.CComboUpdatableCollection.getSelectedObject()'
	 */
	private CCombo ccombo;
	
	protected SelectionAwareUpdatableCollection getSelectionAwareUpdatable(String[] values) {
		Shell shell = BindingScenariosTestSuite.getShell();
		this.ccombo = new CCombo(shell, SWT.NONE);
		for (int i = 0; i < values.length; i++) {
			this.ccombo.add(values[i]);
		}
		IDataBindingContext ctx = DataBinding.createContext(new IUpdatableFactory[] {new SWTUpdatableFactory()});
		return (SelectionAwareUpdatableCollection) ctx.createUpdatable(new Property(ccombo, SWTProperties.ITEMS, String.class, new Boolean(true)));
	}
	
	protected Object getSelectedObjectOfControl() {
		int selectionIndex = this.ccombo.getSelectionIndex();
		if (selectionIndex != -1) {
			return this.ccombo.getItem(selectionIndex);
		} 
		return null;
	}
	
	protected void setSelectedValueOfControl(String value) {
		this.ccombo.setText(value);
	}
}
