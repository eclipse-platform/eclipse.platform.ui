/*******************************************************************************
 * Copyright (c) 2016 Andrey Loskutov <loskutov@gmx.de>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.propertysheet;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.tests.api.MockViewPart;

/**
 * A view which implements {@link ISaveablePart}
 *
 * @since 3.5
 */
public class AdaptingSaveableView extends MockViewPart implements ISaveablePart {

	@SuppressWarnings("hiding")
	public static final String ID = AdaptingSaveableView.class.getName();

	private Map<Object, Object> adaptersMap;

	public boolean dirty;

	public AdaptingSaveableView() {
		adaptersMap = new HashMap<>();
	}

	public <T> void setAdapter(Class<T> clazz, T adapter) {
		adaptersMap.put(clazz, adapter);
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return adapter.cast(adaptersMap.get(adapter));
	}

	public void setSelection(Object selection) {
		getSelectionProvider().setSelection(new StructuredSelection(selection));
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return false;
	}
}
