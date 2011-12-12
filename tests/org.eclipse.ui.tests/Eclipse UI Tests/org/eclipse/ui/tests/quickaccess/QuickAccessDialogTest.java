/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.quickaccess;

import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.quickaccess.SearchField;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * @since 3.4
 * 
 */
public class QuickAccessDialogTest extends UITestCase {

	/**
	 * @param testName
	 */
	public QuickAccessDialogTest(String testName) {
		super(testName);
	}

	public void testOpenQuickAccess() throws Exception {
		WorkbenchWindow workbenchWindow = (WorkbenchWindow) getWorkbench()
				.getActiveWorkbenchWindow();
		MWindow window = workbenchWindow.getModel();
		IHandlerService handlerService = (IHandlerService) workbenchWindow
				.getService(IHandlerService.class);
		handlerService
				.executeCommand("org.eclipse.ui.window.quickAccess", null); //$NON-NLS-1$
		EModelService modelService = window.getContext().get(
				EModelService.class);
		MToolControl control = (MToolControl) modelService.find(
				"SearchField", window); //$NON-NLS-1$
		final SearchField searchField = (SearchField) control.getObject();

		try {
			assertTrue("expecting items", processEventsUntil(new Condition() {
				public boolean compute() {
					return searchField.getTable().getItemCount() > 0;
				};
			}, 200));
			String oldFirstItemText = searchField.getTable().getItem(0)
					.getText(1);
			searchField.getFilterText().setText("e");
			int count1 = searchField.getTable().getItemCount();
			assertTrue("expecting matching items", count1 > 0);
			assertNotSame("expecting different item", oldFirstItemText,
					searchField.getTable().getItem(0).getText(1));
			searchField.toggleShowAllMatches();
			int count2 = searchField.getTable().getItemCount();
			assertTrue("still expecting matching items", count2 > 0);
			assertTrue("expecting more matching items", count2 > count1);
		} finally {
			searchField.close();
		}
	}

}
