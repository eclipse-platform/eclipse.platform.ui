/*******************************************************************************
 * Copyright (c) 2022 Enda O'Brien and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which accompanies this distribution,
 * and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.markers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;
import org.eclipse.ui.tests.ScopeAreaTestView;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.junit.After;
import org.junit.Test;

public class ScopeAreaTest {
	ScopeAreaTestView view;
	Composite composite;

	@After
	public void cleanup() {
		if (view.getDialog() != null) {
			view.getDialog().close();
		}
	}

	@Test
	public void canCreateCustomScopeArea() throws Exception {
		view = (ScopeAreaTestView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(ScopeAreaTestView.ID);

		openFiltersDialog(view);

		assertTrue(MarkerMessages.filtersDialog_anyResource,
				isButtonAvailable(view.getScopeArea(), MarkerMessages.filtersDialog_anyResource));
		assertFalse(MarkerMessages.filtersDialog_anyResourceInSameProject,
				isButtonAvailable(view.getScopeArea(), MarkerMessages.filtersDialog_anyResourceInSameProject));
		assertFalse(MarkerMessages.filtersDialog_anyResourceInSameProject,
				isButtonAvailable(view.getScopeArea(), MarkerMessages.filtersDialog_anyResourceInSameProject));
		assertFalse(MarkerMessages.filtersDialog_selectedAndChildren,
				isButtonAvailable(view.getScopeArea(), MarkerMessages.filtersDialog_selectedAndChildren));
		assertTrue(MarkerMessages.filtersDialog_selectedResource,
				isButtonAvailable(view.getScopeArea(), MarkerMessages.filtersDialog_selectedResource));
	}

	void openFiltersDialog(MarkerSupportView view) {
		Method openFiltersDialog;
		try {
			openFiltersDialog = ExtendedMarkersView.class.getDeclaredMethod("openFiltersDialog");
			openFiltersDialog.setAccessible(true);
			openFiltersDialog.invoke(view);
		} catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	boolean isButtonAvailable(Composite composite, String buttonText) {
		Control[] children = composite.getChildren();
		for (Control ctrl : children) {
			if (ctrl instanceof Button) {
				if (((Button) ctrl).getText().contains(buttonText)) {
					return true;
				}
			} else if (ctrl instanceof Composite && isButtonAvailable((Composite) ctrl, buttonText)) {
				return true;
			}
		}
		return false;
	}
}
