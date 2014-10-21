/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * Sopot Cela <sopotcela@gmail.com>
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x.extension;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.internal.tools.wizards.classes.NewPartClassWizard;
import org.eclipse.e4.tools.emf.editor3x.Messages;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;

@SuppressWarnings("restriction")
public class PartContributionEditor implements IContributionClassCreator {
	@Override
	public void createOpen(MContribution contribution, EditingDomain domain,
		IProject project, Shell shell) {
		createOpen(contribution, domain, project, shell, false);
	}

	private void createOpen(MContribution contribution, EditingDomain domain,
		IProject project, Shell shell, boolean forceNew) {
		if (forceNew || contribution.getContributionURI() == null
			|| contribution.getContributionURI().trim().length() == 0
			|| !contribution.getContributionURI().startsWith("bundleclass:")) { //$NON-NLS-1$
			final NewPartClassWizard wizard = new NewPartClassWizard(contribution.getContributionURI());
			wizard.init(null, new StructuredSelection(project));
			final WizardDialog dialog = new WizardDialog(shell, wizard);
			if (dialog.open() == Window.OK) {
				final IFile f = wizard.getFile();
				final ICompilationUnit el = JavaCore.createCompilationUnitFrom(f);
				try {
					String fullyQualified;
					if (el.getPackageDeclarations() != null && el.getPackageDeclarations().length > 0) {
						final String packageName = el.getPackageDeclarations()[0].getElementName();
						final String className = wizard.getDomainClass().getName();
						if (packageName.trim().length() > 0) {
							fullyQualified = packageName + "." + className; //$NON-NLS-1$
						} else {
							fullyQualified = className;
						}
					} else {
						fullyQualified = wizard.getDomainClass().getName();
					}

					final Command cmd = SetCommand.create(domain, contribution,
						ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI,
						"bundleclass://" + Util.getBundleSymbolicName(f.getProject()) + "/" + fullyQualified); //$NON-NLS-1$ //$NON-NLS-2$
					if (cmd.canExecute()) {
						domain.getCommandStack().execute(cmd);
					}
				} catch (final JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			final URI uri = URI.createURI(contribution.getContributionURI());
			if (uri.hasAuthority() && uri.segmentCount() == 1) {
				final String symbolicName = uri.authority();
				final String fullyQualified = uri.segment(0);
				IProject p = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(symbolicName);

				if (!p.exists()) {
					for (final IProject check : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
						final String name = Util.getBundleSymbolicName(check);
						if (symbolicName.equals(name)) {
							p = check;
							break;
						}
					}
				}

				// TODO If this is not a WS-Resource we need to open differently
				if (p != null) {
					final IJavaProject jp = JavaCore.create(p);
					IType t = null;
					try {
						if (p.exists()) {
							t = jp.findType(fullyQualified);
						}
						else
						{
							final IJavaProject pprim = JavaCore.create(project);
							t = pprim.findType(fullyQualified);
						}
						if (t != null) {
							JavaUI.openInEditor(t);
						} else {
							createOpen(contribution, domain, project, shell, true);
						}
					} catch (final JavaModelException e) {
						createOpen(contribution, domain, project, shell, true);
					} catch (final PartInitException e) {
						MessageDialog.openError(shell, Messages.ContributionEditor_FailedToOpenEditor, e.getMessage());
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				MessageDialog.openError(shell, Messages.ContributionEditor_InvalidURL,
					Messages.ContributionEditor_CurrentURLIsInvalid);
			}
		}
	}

	@Override
	public boolean isSupported(EClass element) {
		return Util.isTypeOrSuper(BasicPackageImpl.Literals.PART, element);
	}

}
