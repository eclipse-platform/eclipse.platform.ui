/*******************************************************************************
 * Copyright (c) 2014, 2017 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 436848
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.BundleImageCache;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Shell;

/**
 * A Wizard for resolving unreferenced resources
 *
 * @author Steven Spungin
 */
public class NonReferencedResourceWizard extends DynamicWizard {

	private final NonReferencedActionPage nonReferencedActionPage;
	protected IProject project;
	IEclipseContext wizContext;

	BundleImageCache imageCache;

	public NonReferencedResourceWizard(Shell parentShell, IProject project, String bundle, IFile file,
			String installLocation, IEclipseContext context) {
		this.project = project;

		wizContext = context.createChild();
		wizContext.set(IProject.class, project);
		wizContext.set("srcPath", file.getFullPath().toOSString()); //$NON-NLS-1$
		wizContext.set(Runnable.class, null);
		wizContext.set("resolvedFile", null); //$NON-NLS-1$

		imageCache = new BundleImageCache(parentShell.getDisplay(), getClass().getClassLoader(), context);
		parentShell.addDisposeListener(e -> imageCache.dispose());
		wizContext.set(BundleImageCache.class, imageCache);

		nonReferencedActionPage = new NonReferencedActionPage(project, bundle, file, installLocation, wizContext);
	}

	@Override
	public boolean performFinish() {
		final Runnable action = wizContext.get(Runnable.class);
		if (action != null) {
			action.run();
		}
		return true;
	}

	public IFile getResult() {
		return (IFile) wizContext.get("resolvedFile"); //$NON-NLS-1$
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page instanceof NonReferencedActionPage) {
			clearDynamicPages();
			final NonReferencedAction action = wizContext.get(NonReferencedAction.class);
			if (action == null) {
				return null;
			}
			switch (action) {
			case COPY: {
				final PickProjectFolderPage pickProjectFolderPage = new PickProjectFolderPage(wizContext);
				addPage(pickProjectFolderPage);
				return pickProjectFolderPage;
			}
			case COPY_TO_OTHER:
				final PickProjectPage pickProjectPage = new PickProjectPage(wizContext);
				addPage(pickProjectPage);
				final PickProjectFolderPage pickProjectFolderPage = new PickProjectFolderPage(wizContext);
				addPage(pickProjectFolderPage);
				return pickProjectPage;
			case USE_ANYWAY:
				return null;
			case IMPORT:
				// TODO Page to set version
				return null;
			case REQUIRE:
				// TODO Page to set version
				return null;
			case CONVERT_AND_REQUIRE:
				// TODO Page for new bundle's information
				return null;
			default:
				return null;
			}
		}
		return super.getNextPage(page);
	}

	@Override
	public void addPages() {
		addPage(nonReferencedActionPage);
	}
}
