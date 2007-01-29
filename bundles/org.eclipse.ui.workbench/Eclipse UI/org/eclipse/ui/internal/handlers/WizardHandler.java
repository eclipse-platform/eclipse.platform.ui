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
package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ExportResourcesAction;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.actions.NewWizardAction;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.IWizardRegistry;

/**
 * Abstract handler for commands that launch the import, export and new wizards.
 * <p>
 * This class is only intended to be extended by the three inner classes (<code>Export</code>,
 * <code>Import</code> and <code>New</code>) defined here.
 * </p>
 * 
 * @since 3.2
 */
public abstract class WizardHandler extends AbstractHandler {

	/**
	 * Default handler for launching export wizards.
	 */
	public static final class Export extends WizardHandler {

		protected IAction createWizardChooserDialogAction(
				IWorkbenchWindow window) {
			return new ExportResourcesAction(window);
		}

		protected String getWizardIdParameterId() {
			return "exportWizardId"; //$NON-NLS-1$
		}

		protected IWizardRegistry getWizardRegistry() {
			return PlatformUI.getWorkbench().getExportWizardRegistry();
		}

	}

	/**
	 * Default handler for launching import wizards.
	 */
	public static final class Import extends WizardHandler {

		protected IAction createWizardChooserDialogAction(
				IWorkbenchWindow window) {
			return new ImportResourcesAction(window);
		}

		protected String getWizardIdParameterId() {
			return "importWizardId"; //$NON-NLS-1$
		}

		protected IWizardRegistry getWizardRegistry() {
			return PlatformUI.getWorkbench().getImportWizardRegistry();
		}

	}

	/**
	 * Default handler for launching new wizards.
	 */
	public static final class New extends WizardHandler {

		protected IAction createWizardChooserDialogAction(
				IWorkbenchWindow window) {
			return new NewWizardAction(window);
		}

		protected String getWizardIdParameterId() {
			return "newWizardId"; //$NON-NLS-1$
		}

		protected IWizardRegistry getWizardRegistry() {
			return PlatformUI.getWorkbench().getNewWizardRegistry();
		}

	}

	/**
	 * Returns an <code>IAction</code> that opens a dialog to allow the user
	 * to choose a wizard.
	 * 
	 * @param window
	 *            The workbench window to use when constructing the action.
	 * @return An <code>IAction</code> that opens a dialog to allow the user
	 *         to choose a wizard.
	 */
	protected abstract IAction createWizardChooserDialogAction(
			IWorkbenchWindow window);

	public Object execute(ExecutionEvent event) throws ExecutionException {

		String wizardId = event.getParameter(getWizardIdParameterId());

		IWorkbenchWindow activeWindow = HandlerUtil
				.getActiveWorkbenchWindowChecked(event);

		if (wizardId == null) {
			IAction wizardAction = createWizardChooserDialogAction(activeWindow);
			wizardAction.run();
		} else {

			IWizardRegistry wizardRegistry = getWizardRegistry();
			IWizardDescriptor wizardDescriptor = wizardRegistry
					.findWizard(wizardId);
			if (wizardDescriptor == null) {
				throw new ExecutionException("unknown wizard: " + wizardId); //$NON-NLS-1$
			}

			try {
				IWorkbenchWizard wizard = wizardDescriptor.createWizard();
				wizard.init(PlatformUI.getWorkbench(),
						StructuredSelection.EMPTY);

				Shell parent = activeWindow.getShell();
				WizardDialog dialog = new WizardDialog(parent, wizard);
				dialog.create();
				dialog.open();

			} catch (CoreException ex) {
				throw new ExecutionException("error creating wizard", ex); //$NON-NLS-1$
			}

		}

		return null;
	}

	/**
	 * Returns the id of the parameter used to indicate which wizard this
	 * command should launch.
	 * 
	 * @return The id of the parameter used to indicate which wizard this
	 *         command should launch.
	 */
	protected abstract String getWizardIdParameterId();

	/**
	 * Returns the wizard registry for the concrete <code>WizardHandler</code>
	 * implementation class.
	 * 
	 * @return The wizard registry for the concrete <code>WizardHandler</code>
	 *         implementation class.
	 */
	protected abstract IWizardRegistry getWizardRegistry();

}
