/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Ongoing Maintenance
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.BundleImageCache;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs.ProjectFolderPickerDialog.ProjectContentProvider;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs.ProjectFolderPickerDialog.ProjectLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Wizard page to select a project folder
 *
 * @author Steven Spungin
 *
 */
public class PickProjectFolderPage extends WizardPage {

	private TreeViewer viewer;
	private String value;
	private IEclipseContext context;
	private IPath path;

	private Label label2;
	private Label label3;
	private Label lblResourcePath;

	protected PickProjectFolderPage(IEclipseContext context) {
		super(Messages.PickProjectFolderPage_SelectProjectFolder, Messages.PickProjectFolderPage_SelectProjectFolder, null);
		this.context = context;

		setMessage(Messages.NonReferencedResourceDialog_selectProjectToReceiveCopy);
		Image image = context.get(BundleImageCache.class).create("/icons/full/wizban/plugin_wiz.gif"); //$NON-NLS-1$
		setImageDescriptor(ImageDescriptor.createFromImage(image));
		setPageComplete(false);
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		Composite ret = new Composite(parent, SWT.NONE);
		// ret.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ret.setLayout(new GridLayout(1, false));

		viewer = new TreeViewer(ret);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ProjectContentProvider());
		viewer.setLabelProvider(new ProjectLabelProvider());
		viewer.expandToLevel(2);

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				onChanged();
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				onChanged();
			}
		});

		Composite compPath = new Composite(ret, SWT.NONE);
		compPath.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		compPath.setLayout(new GridLayout(2, false));

		Label label = new Label(compPath, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText(Messages.ProjectFolderPickerDialog_sourceResourceName);

		label2 = new Label(compPath, SWT.NONE);
		label2.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		label3 = new Label(compPath, SWT.NONE);
		label3.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

		lblResourcePath = new Label(compPath, SWT.NONE);
		lblResourcePath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		compPath.setVisible(false);

		String message = Messages.ProjectFolderPickerDialog_6;

		getShell().setText(message);
		setTitle(message);
		setMessage(message);
		setControl(ret);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			IProject project = (IProject) context.get("projectToCopyTo"); //$NON-NLS-1$
			viewer.setInput(project);

			Object object = context.get("folderToCopyTo.obj"); //$NON-NLS-1$
			if (object != null) {
				viewer.setSelection(new StructuredSelection(object));
			} else {
				viewer.setSelection(new StructuredSelection());
			}
			setPageComplete(viewer.getSelection().isEmpty() == false);

			path = new Path((String) context.get("srcPath")); //$NON-NLS-1$
			label2.setText(path.lastSegment());
			label3.setText(Messages.ProjectFolderPickerDialog_sourceResourceDirectory);
			lblResourcePath.setText(path.removeLastSegments(1).toOSString());
		}
		super.setVisible(visible);
	}

	protected void onChanged() {
		Object selected = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		if (selected == null || selected instanceof String) {
			value = ""; //$NON-NLS-1$
		} else {
			IResource resource = (IResource) selected;
			value = resource.getFullPath().removeFirstSegments(1).toOSString();
		}
		context.set("folderToCopyTo", value); //$NON-NLS-1$
		context.set("folderToCopyTo.obj", selected); //$NON-NLS-1$
		setPageComplete(selected != null);
	}
}
