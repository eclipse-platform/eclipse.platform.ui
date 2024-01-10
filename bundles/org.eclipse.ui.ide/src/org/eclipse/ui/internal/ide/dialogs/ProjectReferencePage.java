/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Sebastian Davids <sdavids@gmx.de> - Bug 137478 [Preferences] ProjectReferencePage has too wide left margin
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.progress.IProgressService;

/**
 * A property page for viewing and modifying the set
 * of projects referenced by a given project.
 */
public class ProjectReferencePage extends PropertyPage {
	private IProject project;

	private boolean modified = false;

	//widgets
	private CheckboxTableViewer listViewer;

	/*
	 * @see PreferencePage#createContents
	 */
	@Override
	protected Control createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				IIDEHelpContextIds.PROJECT_REFERENCE_PROPERTY_PAGE);

		Composite composite = new Composite(parent, SWT.NONE);

		initialize();

		createDescriptionLabel(composite);

		listViewer = CheckboxTableViewer.newCheckList(composite, SWT.TOP
				| SWT.BORDER);

		if(!project.isOpen())
			listViewer.getControl().setEnabled(false);

		listViewer.setLabelProvider(WorkbenchLabelProvider
				.getDecoratingWorkbenchLabelProvider());
		listViewer.setContentProvider(getContentProvider(project));
		listViewer.setComparator(new ViewerComparator());
		listViewer.setInput(project.getWorkspace());
		try {
			listViewer.setCheckedElements(project.getDescription()
					.getReferencedProjects());
		} catch (CoreException e) {
			//don't initial-check anything
		}

		//check for initial modification to avoid work if no changes are made
		listViewer.addCheckStateListener(event -> modified = true);

		applyDialogFont(composite);

		GridLayoutFactory.fillDefaults().generateLayout(composite);

		return composite;
	}

	/**
	 * Returns a content provider for the list dialog. It
	 * will return all projects in the workspace except
	 * the given project, plus any projects referenced
	 * by the given project which do no exist in the
	 * workspace.
	 * @param project the project to provide content for
	 * @return the content provider that shows the project content
	 */
	protected static IStructuredContentProvider getContentProvider(
			final IProject project) {
		return new WorkbenchContentProvider() {
			@Override
			public Object[] getChildren(Object o) {
				if (!(o instanceof IWorkspace)) {
					return new Object[0];
				}

				// Collect all the projects in the workspace except the given project
				IProject[] projects = ((IWorkspace) o).getRoot().getProjects();
				List<IProject> referenced = new ArrayList<>(projects.length);
				boolean found = false;
				for (IProject currentProject : projects) {
					if (!found && currentProject.equals(project)) {
						found = true;
						continue;
					}
					referenced.add(currentProject);
				}

				// Add any referenced that do not exist in the workspace currently
				try {
					IProject[] referencedProjects = project.getDescription().getReferencedProjects();
					for (IProject referencedProject : referencedProjects) {
						if (!referenced.contains(referencedProject)) {
							referenced.add(referencedProject);
						}
					}
				} catch (CoreException e) {
					//Ignore core exceptions
				}

				return referenced.toArray();
			}
		};
	}

	/**
	 * Handle the exception thrown when saving.
	 * @param e the exception
	 */
	protected void handle(InvocationTargetException e) {
		IStatus error;
		Throwable target = e.getTargetException();
		if (target instanceof CoreException) {
			error = ((CoreException) target).getStatus();
		} else {
			String msg = target.getMessage();
			if (msg == null) {
				msg = IDEWorkbenchMessages.Internal_error;
			}
			error = new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
					1, msg, target);
		}
		ErrorDialog.openError(getControl().getShell(), null, null, error);
	}

	/**
	 * Initializes a ProjectReferencePage.
	 */
	private void initialize() {
		project = Adapters.adapt(getElement(), IProject.class);
		if (project == null) {
			IResource resource = Adapters.adapt(getElement(), IResource.class);
			Assert.isNotNull(resource, "unable to adapt element to a project"); //$NON-NLS-1$
			project = resource.getProject();
		}
		noDefaultAndApplyButton();
		setDescription(NLS.bind(IDEWorkbenchMessages.ProjectReferencesPage_label, project.getName()));
	}

	/**
	 * @see PreferencePage#performOk
	 */
	@Override
	public boolean performOk() {
		if (!modified) {
			return true;
		}
		Object[] checked = listViewer.getCheckedElements();
		final IProject[] refs = new IProject[checked.length];
		System.arraycopy(checked, 0, refs, 0, checked.length);
		IRunnableWithProgress runnable = monitor -> {

			try {
		IProjectDescription description = project.getDescription();
		description.setReferencedProjects(refs);
		project.setDescription(description, monitor);
			} catch (CoreException e) {
		throw new InvocationTargetException(e);
			}
		};
		IProgressService service = PlatformUI.getWorkbench().getProgressService();
		try {
			service.run(false, false, runnable);
		} catch (InterruptedException e) {
			//Ignore interrupted exceptions
		} catch (InvocationTargetException e) {
			handle(e);
			return false;
		}
		return true;
	}
}
