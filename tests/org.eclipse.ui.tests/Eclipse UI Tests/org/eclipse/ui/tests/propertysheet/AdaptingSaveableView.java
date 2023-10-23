/*******************************************************************************
 * Copyright (c) 2016 Andrey Loskutov <loskutov@gmx.de>.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	public static final String ID_ADAPTING_SAVEABLE = AdaptingSaveableView.class.getName();

	private final Map<Object, Object> adaptersMap;

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
