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
 * Steven Spungin <steven@spungin.tv> - initial API and implementation, Ongoing Maintenance
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.BundleImageCache;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Wizard page to select a referenced project
 *
 * @author Steven Spungin
 */
public class PickProjectPage extends WizardPage {

	private TableViewer viewer;
	private BundleImageCache imageCache;
	private Image imgProject;
	private IProject[] projects;
	private final IEclipseContext context;

	protected PickProjectPage(IEclipseContext context) {
		super(Messages.PickProjectPage_SelectReferencedProject);
		this.context = context;
		try {
			projects = context.get(IProject.class).getReferencedProjects();
		} catch (final CoreException e) {
			setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {

		imageCache = new BundleImageCache(parent.getDisplay(), getClass().getClassLoader());
		getShell().addDisposeListener(e -> {
			imageCache.dispose();
			imgProject = null;
		});
		imgProject = imageCache.create("/icons/full/obj16/projects.png"); //$NON-NLS-1$

		final Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(1, false));

		viewer = new TableViewer(comp, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				final IProject project = (IProject) element;
				return project.getName();
			}

			@Override
			public Image getImage(Object element) {
				return imgProject;
			}
		});
		viewer.setContentProvider(ArrayContentProvider.getInstance());

		viewer.setInput(projects);

		viewer.addSelectionChangedListener(event -> {
			final Object firstElement = ((StructuredSelection) event.getSelection()).getFirstElement();
			context.set("projectToCopyTo", firstElement); //$NON-NLS-1$
			setPageComplete(firstElement != null);
			getContainer().updateButtons();
		});

		final String message = Messages.ReferencedProjectPickerDialog_selectReferencedProject;
		setMessage(message);
		getShell().setText(message);
		setTitle(message);

		final Image image = context.get(BundleImageCache.class).create("/icons/full/obj16/projects.png"); //$NON-NLS-1$
		setImageDescriptor(ImageDescriptor.createFromImage(image));

		setControl(comp);
	}

	@Override
	public void setVisible(boolean visible) {
		final Object object = context.get("projectToCopyTo"); //$NON-NLS-1$
		if (visible) {
			if (object != null) {
				viewer.setSelection(new StructuredSelection(object));
			} else {
				viewer.setSelection(new StructuredSelection());
			}
		}
		setPageComplete(viewer.getSelection().isEmpty() == false);
		super.setVisible(visible);
	}

}
