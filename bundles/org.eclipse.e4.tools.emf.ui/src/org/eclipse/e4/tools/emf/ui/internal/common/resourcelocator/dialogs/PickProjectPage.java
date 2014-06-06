/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Wizard page to select a referenced project
 *
 * @author Steven Spungin
 *
 */
public class PickProjectPage extends WizardPage implements IWizardPage {

	private TableViewer viewer;
	private BundleImageCache imageCache;
	private Image imgProject;
	private IProject[] projects;
	private IEclipseContext context;

	protected PickProjectPage(IEclipseContext context) {
		super("Select Referenced Project");
		this.context = context;
		try {
			this.projects = context.get(IProject.class).getReferencedProjects();
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {

		imageCache = new BundleImageCache(parent.getDisplay(), getClass().getClassLoader());
		getShell().addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				imageCache.dispose();
				imgProject = null;
			}
		});
		imgProject = imageCache.create("/icons/full/obj16/projects.png"); //$NON-NLS-1$

		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		comp.setLayout(new GridLayout(1, false));

		viewer = new TableViewer(comp, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IProject project = (IProject) element;
				return project.getName();
			}

			@Override
			public Image getImage(Object element) {
				return imgProject;
			}
		});
		viewer.setContentProvider(ArrayContentProvider.getInstance());

		viewer.setInput(projects);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object firstElement = ((StructuredSelection) event.getSelection()).getFirstElement();
				context.set("projectToCopyTo", firstElement); //$NON-NLS-1$
				setPageComplete(firstElement != null);
				getContainer().updateButtons();
			}
		});

		String message = Messages.ReferencedProjectPickerDialog_selectReferencedProject;
		setMessage(message);
		getShell().setText(message);
		setTitle(message);

		Image image = context.get(BundleImageCache.class).create("/icons/full/obj16/projects.png"); //$NON-NLS-1$
		setImageDescriptor(ImageDescriptor.createFromImage(image));

		setControl(comp);
	}

	@Override
	public void setVisible(boolean visible) {
		Object object = context.get("projectToCopyTo"); //$NON-NLS-1$
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
