/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.Assert;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryImplementation;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.model.ModelMessages;
import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringDescriptorSynchronizationProxy;
import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringHistoryMergeWizard;
import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

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
	private static final class RefactoringHistoryAcceptWizard extends RefactoringHistoryMergeWizard {

		/**
		 * Creates a new refactoring history accept wizard.
		 */
		public RefactoringHistoryAcceptWizard() {
			super(RefactoringUIMessages.RefactoringWizard_refactoring, ModelMessages.AcceptRefactoringsAction_wizard_title, ModelMessages.AcceptRefactoringsAction_wizard_description);
		}
	}

	/** The wizard height */
	private static final int SIZING_WIZARD_HEIGHT= 520;

	/** The wizard width */
	private static final int SIZING_WIZARD_WIDTH= 470;

	/** The synchronization context to use */
	private final ISynchronizationContext fContext;

	/** The refactoring descriptor proxies, or <code>null</code> */
	private RefactoringDescriptorProxy[] fProxies= null;

	/** The shell to use */
	private final Shell fShell;

	/**
	 * Creates a new accept refactorings action.
	 *
	 * @param context
	 *            the synchronization context
	 * @param shell
	 *            the shell to use
	 */
	public AcceptRefactoringsAction(final ISynchronizationContext context, final Shell shell) {
		Assert.isNotNull(context);
		Assert.isNotNull(shell);
		fContext= context;
		fShell= shell;
		setText(ModelMessages.AcceptRefactoringsAction_title);
		setToolTipText(ModelMessages.AcceptRefactoringsAction_tool_tip);
		setDescription(ModelMessages.AcceptRefactoringsAction_description);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEnabled() {
		if (fProxies != null && fProxies.length > 0) {
			for (int index= 0; index < fProxies.length; index++) {
				if (fProxies[index] instanceof RefactoringDescriptorSynchronizationProxy) {
					final RefactoringDescriptorSynchronizationProxy proxy= (RefactoringDescriptorSynchronizationProxy) fProxies[index];
					if (proxy.getDirection() == IThreeWayDiff.INCOMING)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		if (fProxies != null && fProxies.length > 0) {
			final RefactoringHistoryMergeWizard wizard= new RefactoringHistoryAcceptWizard();
			int result= Window.OK;
			try {
				final WizardDialog dialog= new WizardDialog(fShell, wizard);
				IProject project= null;
				Set proxies= new HashSet();
				for (int index= 0; index < fProxies.length; index++) {
					if (fProxies[index] instanceof RefactoringDescriptorSynchronizationProxy) {
						final RefactoringDescriptorSynchronizationProxy proxy= (RefactoringDescriptorSynchronizationProxy) fProxies[index];
						if (proxy.getDirection() == IThreeWayDiff.INCOMING)
							proxies.add(proxy);
					}
					String name= fProxies[index].getProject();
					if (name != null && !"".equals(name)) //$NON-NLS-1$
						project= ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				}
				wizard.setConfiguration(new RefactoringHistoryAcceptConfiguration(project));
				wizard.setInput(new RefactoringHistoryImplementation((RefactoringDescriptorProxy[]) proxies.toArray(new RefactoringDescriptorProxy[proxies.size()])));
				dialog.create();
				dialog.getShell().setSize(Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT);
				PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(), IRefactoringHelpContextIds.REFACTORING_ACCEPT_REFACTORING_PAGE);
				result= dialog.open();
			} finally {
				if (result != Window.CANCEL && fContext instanceof IMergeContext) {
					final IMergeContext context= (IMergeContext) fContext;
					wizard.resolveConflicts(context);
				}
			}
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