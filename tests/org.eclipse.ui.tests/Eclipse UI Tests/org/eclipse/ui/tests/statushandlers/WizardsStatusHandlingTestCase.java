/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.statushandlers;

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import org.eclipse.ui.tests.SwtLeakTestWatcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

/**
 * Tests whether the errors in wizards are handled properly
 *
 * @since 3.3
 */
public class WizardsStatusHandlingTestCase {
	@Rule
	public TestWatcher swtLeakTestWatcher = new SwtLeakTestWatcher();

	private static int SEVERITY = IStatus.ERROR;

	private static Class<CoreException> EXCEPTION_CLASS = CoreException.class;

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

	@After
	public void tearDown() throws Exception {
		TestStatusHandler.uninstall();
	}

	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
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

	@Test
	public void testWizardWithNoDefaultContructor() throws Exception {
		processEvents();

		final CustomWizardDialog dialog = exportWizard();
		try {
			dialog.setBlockOnOpen(false);
			dialog.open();

			processEvents();

			// selecting FaultyExportWizard
			IWizardPage currenPage = dialog.getCurrentPage();
			Composite control = (Composite) currenPage.getControl();
			Control[] widgets = control.getChildren();

			Table table = (Table) widgets[1];

			for (int i = 0; i < table.getItemCount(); i++) {
				if (table.getItem(i).getText().equals(FAULTY_WIZARD_NAME)) {
					table.select(i);
					table.notifyListeners(SWT.Selection, new Event());
					processEvents();
					break;
				}
			}

			// pressing "Next"
			TestStatusHandler.install();

			dialog.nextPressed2();

			processEvents();
			assertStatusAdapter(TestStatusHandler.getLastHandledStatusAdapter());
			assertEquals(TestStatusHandler.getLastHandledStyle(), StatusManager.SHOW);
		} finally {
			dialog.close();
		}
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
