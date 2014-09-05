/*******************************************************************************
 * Copyright (c) 2008, 2009 Versant  and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe (Versant Corp.) - https://bugs.eclipse.org/248103
 ******************************************************************************/

package org.eclipse.ui.tests.propertysheet;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

/**
 * @since 3.4
 * 
 */
public class TestPropertySheetPage extends PropertySheetPage implements
		IPropertySheetPage, IAdapterFactory {

	private ISelection fSelection;
	private IWorkbenchPart fPart;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
	 * java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		// singleton cleanup
		fSelection = null;
		fPart = null;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return new Class[] { IPropertySheetPage.class };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.views.properties.PropertySheetPage#selectionChanged(org
	 * .eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		super.selectionChanged(part, selection);
		fPart = part;
		fSelection = selection;
	}

	/**
	 * @return Returns the selection.
	 */
	public ISelection getSelection() {
		return fSelection;
	}

	/**
	 * @return Returns the part.
	 */
	public IWorkbenchPart getPart() {
		return fPart;
	}
}
