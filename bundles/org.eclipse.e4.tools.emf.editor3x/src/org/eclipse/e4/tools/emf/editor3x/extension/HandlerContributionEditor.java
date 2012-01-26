/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x.extension;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.internal.tools.wizards.classes.NewHandlerClassWizard;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
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
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;

public class HandlerContributionEditor implements IContributionClassCreator {
	public void createOpen(MContribution contribution, EditingDomain domain, IProject project, Shell shell) {
		createOpen(contribution, domain, project, shell, false);
	}
	
	private void createOpen(MContribution contribution, EditingDomain domain, IProject project, Shell shell, boolean forceNew) {
		if( contribution.getContributionURI() == null || contribution.getContributionURI().trim().length() == 0 || !contribution.getContributionURI().startsWith("bundleclass:") ) {
			NewHandlerClassWizard wizard = new NewHandlerClassWizard();
			wizard.init( null, new StructuredSelection(project));
			WizardDialog dialog = new WizardDialog(shell, wizard);
			if( dialog.open() == WizardDialog.OK ) {
				IFile f = wizard.getFile();
				ICompilationUnit el = JavaCore.createCompilationUnitFrom(f);
				try {
					String fullyQualified;
					if( el.getPackageDeclarations() != null && el.getPackageDeclarations().length > 0 ) {
						String packageName = el.getPackageDeclarations()[0].getElementName();
						String className = wizard.getDomainClass().getName();
						if( packageName.trim().length() > 0 ) {
							fullyQualified = packageName + "." + className;	
						} else {
							fullyQualified = className;
						}
					} else {
						fullyQualified = wizard.getDomainClass().getName();
					}
					
					Command cmd = SetCommand.create(domain, contribution, ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI, "bundleclass://" + Util.getBundleSymbolicName(f.getProject()) + "/" + fullyQualified);
					if( cmd.canExecute() ) {
						domain.getCommandStack().execute(cmd);
					}
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			URI uri = URI.createURI(contribution.getContributionURI());
			if (uri.hasAuthority() && uri.segmentCount() == 1) {
			    String symbolicName = uri.authority();
			    String fullyQualified = uri.segment(0);
				IProject p = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(symbolicName);
				
				if( ! p.exists() ) {
					for( IProject check : ResourcesPlugin.getWorkspace().getRoot().getProjects() ) {
						String name = Util.getBundleSymbolicName(check);
						if( symbolicName.equals(name) ) {
							p = check;
							break;
						}
					}
				}
				
				// TODO If this is not a WS-Resource we need to open differently
				if (p != null) {
					IJavaProject jp = JavaCore.create(p);
					try {
						IType t = jp.findType(fullyQualified);
						if( t != null ) {
							JavaUI.openInEditor(t);
						} else {
							if( MessageDialog.openQuestion(shell, "Class not found", "The class '"+fullyQualified+"' was not found. Would you like to start the class creation wizard?") ) {
								createOpen(contribution, domain, project, shell, true);
							}
						}
					} catch (JavaModelException e) {
						if( MessageDialog.openQuestion(shell, "Class not found", "The class '"+fullyQualified+"' was not found. Would you like to start the class creation wizard?") ) {
							createOpen(contribution, domain, project, shell, true);
						}
					} catch (PartInitException e) {
						MessageDialog.openError(shell, "Failed to open editor", e.getMessage());
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				MessageDialog.openError(shell, "Invalid URL",
						"The current url is invalid");
			}
		}
	}

	public boolean isSupported(EClass element) {
		return 
			Util.isTypeOrSuper(CommandsPackageImpl.Literals.HANDLER,element)
			|| 
			Util.isTypeOrSuper(MenuPackageImpl.Literals.DIRECT_MENU_ITEM, element)
			||
			Util.isTypeOrSuper(MenuPackageImpl.Literals.DIRECT_TOOL_ITEM, element);
	}
	
	
}