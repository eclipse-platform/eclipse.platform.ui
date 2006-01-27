/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.actions;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryImplementation;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.model.ModelMessages;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryWizard;

/**
 * Action to accept pending refactorings to execute them on the local workspace.
 * 
 * @since 3.2
 */
public final class AcceptRefactoringsAction extends Action {

	/** Refactoring history accept configuration */
	private static final class RefactoringHistoryAcceptConfiguration extends RefactoringHistoryControlConfiguration {

		/**
		 * Creates a new refactoring history accept configuration.
		 * 
		 * @param project
		 *            the project, or <code>null</code>
		 */
		public RefactoringHistoryAcceptConfiguration(final IProject project) {
			super(project, false, false);
		}

		/**
		 * {@inheritDoc}
		 */
		public String getProjectPattern() {
			return ModelMessages.AcceptRefactoringsAction_wizard_project_pattern;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getWorkspaceCaption() {
			return ModelMessages.AcceptRefactoringsAction_wizard_workspace_caption;
		}
	}

	/** The refactoring history accept wizard */
	private static final class RefactoringHistoryAcceptWizard extends RefactoringHistoryWizard {

		/** The refactoring descriptor, or <code>null</code> */
		private RefactoringDescriptor fDescriptor;

		/**
		 * Creates a new refactoring history accept wizard.
		 */
		public RefactoringHistoryAcceptWizard() {
			super(RefactoringUIMessages.RefactoringWizard_refactoring, ModelMessages.AcceptRefactoringsAction_wizard_title, ModelMessages.AcceptRefactoringsAction_wizard_description);
		}

		/**
		 * {@inheritDoc}
		 */
		protected RefactoringStatus aboutToPerformRefactoring(final Refactoring refactoring, final RefactoringDescriptor descriptor, final IProgressMonitor monitor) {
			Assert.isNotNull(descriptor);
			fDescriptor= descriptor;
			return super.aboutToPerformRefactoring(refactoring, descriptor, monitor);
		}

		/**
		 * {@inheritDoc}
		 */
		protected RefactoringStatus refactoringPerformed(final Refactoring refactoring, final IProgressMonitor monitor) {
			Assert.isNotNull(monitor);
			try {
				monitor.beginTask("", 1); //$NON-NLS-1$
				if (fDescriptor != null && !fDescriptor.isUnknown())
					RefactoringHistoryService.getInstance().addRefactoringDescriptor(fDescriptor, new SubProgressMonitor(monitor, 1));
				return super.refactoringPerformed(refactoring, monitor);
			} finally {
				monitor.done();
			}
		}
	}

	/** The wizard height */
	private static final int SIZING_WIZARD_HEIGHT= 520;

	/** The wizard width */
	private static final int SIZING_WIZARD_WIDTH= 470;

	/** The refactoring descriptor proxies, or <code>null</code> */
	private RefactoringDescriptorProxy[] fProxies= null;

	/** The shell to use */
	private final Shell fShell;

	/**
	 * Creates a new accept refactorings action.
	 * 
	 * @param shell
	 *            the shell to use
	 */
	public AcceptRefactoringsAction(final Shell shell) {
		Assert.isNotNull(shell);
		fShell= shell;
		setText(ModelMessages.AcceptRefactoringsAction_title);
		setToolTipText(ModelMessages.AcceptRefactoringsAction_tool_tip);
		setDescription(ModelMessages.AcceptRefactoringsAction_description);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEnabled() {
		return fProxies != null && fProxies.length > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		if (fProxies != null && fProxies.length > 0) {
			final RefactoringHistoryWizard wizard= new RefactoringHistoryAcceptWizard();
			final WizardDialog dialog= new WizardDialog(fShell, wizard);
			IProject project= null;
			for (int index= 0; index < fProxies.length; index++) {
				String name= fProxies[index].getProject();
				if (name != null && !"".equals(name)) //$NON-NLS-1$
					project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			}
			wizard.setConfiguration(new RefactoringHistoryAcceptConfiguration(project));
			wizard.setInput(new RefactoringHistoryImplementation(fProxies));
			dialog.create();
			dialog.getShell().setSize(Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IRefactoringHelpContextIds.REFACTORING_ACCEPT_REFACTORING_PAGE);
			dialog.open();
		}
	}

	/**
	 * Sets the refactoring descriptor proxies to accept.
	 * 
	 * @param proxies
	 *            the refactoring descriptor proxies
	 */
	public void setRefactoringDescriptors(final RefactoringDescriptorProxy[] proxies) {
		Assert.isNotNull(proxies);
		fProxies= proxies;
	}
}