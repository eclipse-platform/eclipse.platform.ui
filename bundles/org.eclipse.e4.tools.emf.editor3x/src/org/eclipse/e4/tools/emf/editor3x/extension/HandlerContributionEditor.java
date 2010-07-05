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

import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.internal.tools.wizards.classes.NewHandlerClassWizard;
import org.eclipse.e4.tools.emf.ui.common.IContributionClassCreator;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.commands.MHandler;
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
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class HandlerContributionEditor implements IContributionClassCreator {

	public void createOpen(MContribution contribution, EditingDomain domain, IProject project, Shell shell) {
		if( contribution.getContributionURI() == null || contribution.getContributionURI().trim().length() == 0 ) {
			NewHandlerClassWizard wizard = new NewHandlerClassWizard();
			wizard.init( null, new StructuredSelection(project));
			WizardDialog dialog = new WizardDialog(shell, wizard);
			if( dialog.open() == WizardDialog.OK ) {
				IFile f = wizard.getFile();
				ICompilationUnit el = JavaCore.createCompilationUnitFrom(f);
				try {
					String packageName = el.getPackageDeclarations()[0].getElementName();
					String className = wizard.getDomainClass().getName();
					Command cmd = SetCommand.create(domain, contribution, ApplicationPackageImpl.Literals.CONTRIBUTION__CONTRIBUTION_URI, "platform:/plugin/" + f.getProject().getName() + "/" + packageName+"."+className);
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
			IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(uri.segment(1));
			//TODO If this is not a WS-Resource we need to open differently 
			if( p != null ) {
				IJavaProject jp = JavaCore.create(p);
				try {
					IType t = jp.findType(uri.segment(2));
					JavaUI.openInEditor(t);
				} catch (JavaModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isSupported(EClass element) {
		return 
			isTypeOrSuper(CommandsPackageImpl.Literals.HANDLER,element)
			|| 
			isTypeOrSuper(MenuPackageImpl.Literals.DIRECT_MENU_ITEM, element)
			||
			isTypeOrSuper(MenuPackageImpl.Literals.DIRECT_TOOL_ITEM, element);
	}
	
	private boolean isTypeOrSuper(EClass eClass, EClass element) {
		return eClass.equals(element) || element.getEAllSuperTypes().contains(eClass);
	}
}