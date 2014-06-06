/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *	   Lars Vogel <lars.vogel@gmail.com> - Enhancements
 *     Steven Spungin <steven@spungin.tv> - Bug 424730, Bug 436847
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.common.IClassContributionProvider.ContributionData;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.ClassContributionCollector;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.TargetPlatformClassContributionCollector;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.TargetPlatformContributionCollector;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class ContributionClassDialog extends FilteredContributionDialog {

	private IProject project;
	private MApplicationElement contribution;
	private EditingDomain editingDomain;
	private EStructuralFeature feature;
	private Messages Messages;

	public ContributionClassDialog(Shell parentShell, IEclipseContext context, IProject project, EditingDomain editingDomain, MApplicationElement contribution, EStructuralFeature feature, Messages Messages) {
		super(parentShell, context);
		this.project = project;
		this.contribution = contribution;
		this.editingDomain = editingDomain;
		this.feature = feature;
		this.Messages = Messages;
	}

	@Override
	protected Image getTitleImage() {
		return new Image(getShell().getDisplay(), getClass().getClassLoader().getResourceAsStream("/icons/full/wizban/newclass_wiz.png")); //$NON-NLS-1$
	}

	@Override
	protected void okPressed() {
		IStructuredSelection s = (IStructuredSelection) getViewer().getSelection();
		if (!s.isEmpty()) {
			ContributionData cd = (ContributionData) s.getFirstElement();
			ContributionDataFile cdf = new ContributionDataFile(cd);
			IFile file = checkResourceAccessible(cdf, cd.installLocation);
			if (file != null) {
				if (file instanceof ContributionDataFile) {
					cdf = (ContributionDataFile) file;
					cd = cdf.getContributionData();
				}
				String uri = "bundleclass://" + cd.bundleName + "/" + cd.className; //$NON-NLS-1$ //$NON-NLS-2$
				Command cmd = SetCommand.create(editingDomain, contribution, feature, uri);
				if (cmd.canExecute()) {
					editingDomain.getCommandStack().execute(cmd);
					super.okPressed();
				}
			}
		}
	}

	@Override
	protected ClassContributionCollector getCollector() {
		switch (getScope()) {
		case TARGET_PLATFORM:
		case WORKSPACE:
			if (collector instanceof TargetPlatformContributionCollector == false) {
				collector = TargetPlatformClassContributionCollector.getInstance();
			}
			break;
		case PROJECT:
			Bundle bundle = FrameworkUtil.getBundle(ContributionClassDialog.class);
			BundleContext context = bundle.getBundleContext();
			ServiceReference<?> ref = context.getServiceReference(ClassContributionCollector.class.getName());
			if (ref != null) {
				collector = (ClassContributionCollector) context.getService(ref);
			} else {
				collector = null;
			}
			break;
		default:
			collector = null;
			break;
		}
		return collector;
	}

	@Override
	protected String getShellTitle() {
		return Messages.ContributionClassDialog_DialogTitle;
	}

	@Override
	protected String getDialogTitle() {
		return Messages.ContributionClassDialog_DialogTitle;
	}

	@Override
	protected String getDialogMessage() {
		return Messages.ContributionClassDialog_DialogMessage;
	}

	@Override
	protected String getResourceNameText() {
		return Messages.ContributionClassDialog_Label_Classname;
	}

	@Override
	protected String getFilterTextMessage() {
		return Messages.ContributionClassDialog_FilterText_Message;
	}
}