/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.statushandlers;

import org.eclipse.core.internal.registry.RegistryMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.ExportWizard;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.tests.harness.util.DialogCheck;
import org.eclipse.ui.tests.harness.util.UITestCase;

import junit.framework.TestCase;

/**
 * Tests whether the errors in wizards are handled properly
 *
 * @since 3.3
 */
public class WizardsStatusHandlingTestCase extends TestCase {

	private static int SEVERITY = IStatus.ERROR;

	private static Class EXCEPTION_CLASS = CoreException.class;

	private static String MESSAGE = WorkbenchMessages.WorkbenchWizard_errorMessage;

	private static String EXCEPTION_MESSAGE = NLS
			.bind(
					RegistryMessages.plugin_instantiateClassError,
					new Object[] { "org.eclipse.ui.tests",
							"org.eclipse.ui.tests.statushandlers.FaultyExportWizard" });

	private static String EXCEPTION_MESSAGE2 = NLS
			.bind(
					RegistryMessages.plugin_loadClassError,
					new Object[] { "org.eclipse.ui.tests",
							"org.eclipse.ui.tests.statushandlers.FaultyExportWizard" });

	private static String PLUGIN_ID = "org.eclipse.ui.tests";

	private static String FAULTY_WIZARD_NAME = "FaultyExportWizard";

	public WizardsStatusHandlingTestCase(String name) {
		super(name);
	}

	private Shell getShell() {
		return DialogCheck.getShell();
	}

	private IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	class CustomWizardDialog extends WizardDialog {
		public CustomWizardDialog(Shell shell, IWizard wizard) {
			super(shell, wizard);
		}

		public void nextPressed2() {
			super.nextPressed();
		}
	}

	private CustomWizardDialog exportWizard() {
		ExportWizard wizard = new ExportWizard();
		wizard.init(getWorkbench(), null);
		CustomWizardDialog dialog = new CustomWizardDialog(getShell(), wizard);
		dialog.create();
		return dialog;
	}

	public void testWizardWithNoDefaultContructor() {
		UITestCase.processEvents();

		final CustomWizardDialog dialog = exportWizard();
		dialog.setBlockOnOpen(false);
		dialog.open();

		UITestCase.processEvents();

		// selecting FaultyExportWizard
		IWizardPage currenPage = dialog.getCurrentPage();
		Composite control = (Composite) currenPage.getControl();
		Control[] widgets = control.getChildren();

		Table table = (Table) widgets[1];

		for (int i = 0; i < table.getItemCount(); i++) {
			if (table.getItem(i).getText().equals(FAULTY_WIZARD_NAME)) {
				table.select(i);
				table.notifyListeners(SWT.Selection, new Event());
				UITestCase.processEvents();
				break;
			}
		}

		// pressing "Next"
		dialog.nextPressed2();

		UITestCase.processEvents();
		assertStatusAdapter(TestStatusHandler.getLastHandledStatusAdapter());
		assertEquals(TestStatusHandler.getLastHandledStyle(),
				StatusManager.SHOW);
	}

	/**
	 * Checks whether the last handled status is correct
	 */
	private void assertStatusAdapter(StatusAdapter statusAdapter) {
		IStatus status = statusAdapter.getStatus();
		assertEquals(SEVERITY, status.getSeverity());
		assertEquals(PLUGIN_ID, status.getPlugin());
		assertEquals(MESSAGE, status.getMessage());
		assertEquals(EXCEPTION_CLASS, status.getException().getClass());
		assertTrue(createIncorrectExceptionMessage(status.getException()
				.getMessage()), EXCEPTION_MESSAGE.equals(status.getException()
				.getMessage())
				|| EXCEPTION_MESSAGE2
						.equals(status.getException().getMessage()));
	}

	private String createIncorrectExceptionMessage(String exceptionMessage) {
		return "expected:<" + EXCEPTION_MESSAGE + "> or <" + EXCEPTION_MESSAGE2
				+ "> but was:<" + exceptionMessage + ">";
	}
}
