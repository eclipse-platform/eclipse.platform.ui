/*******************************************************************************
 * Copyright (c) 2008, 2017 Versant  and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
		IAdapterFactory {

	private ISelection fSelection;
	private IWorkbenchPart fPart;

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		// singleton cleanup
		fSelection = null;
		fPart = null;
		return (T) this;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IPropertySheetPage.class };
	}

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
