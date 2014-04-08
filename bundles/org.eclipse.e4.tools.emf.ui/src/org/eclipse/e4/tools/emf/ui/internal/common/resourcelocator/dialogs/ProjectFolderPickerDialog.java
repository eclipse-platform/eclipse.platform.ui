/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 424730
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs;

import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages;

import java.net.URL;
import java.util.ArrayList;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class ProjectFolderPickerDialog extends TitleAreaDialog {

	private TreeViewer viewer;
	private IProject project;
	private String srcPath;
	private String value;

	protected ProjectFolderPickerDialog(Shell parentShell, IProject project, String srcPath) {
		super(parentShell);
		this.project = project;
		this.srcPath = srcPath;
	}

	static class ProjectContentProvider implements ITreeContentProvider {

		private IProject project;

		public ProjectContentProvider() {
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.project = (IProject) newInput;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return new Object[] { project.getName() };
		}

		@Override
		public Object[] getChildren(final Object parentElement) {
			if (parentElement instanceof String) {
				return getChildren(project);
			}
			final IResource resource = (IResource) parentElement;
			final ArrayList<Object> list = new ArrayList<Object>();
			IResourceProxyVisitor visitor = new IResourceProxyVisitor() {

				@Override
				public boolean visit(IResourceProxy proxy) throws CoreException {
					if (proxy.getType() == IResource.FOLDER && proxy.requestResource().getParent() == resource) {
						if (proxy.requestResource().equals(resource) == false) {
							list.add(proxy.requestResource());
						}
					}
					return true;
				}
			};
			try {
				resource.accept(visitor, IResource.DEPTH_ONE);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return list.toArray(new Object[0]);
		}

		@Override
		public Object getParent(Object element) {
			IResource resource = (IResource) element;
			return resource.getParent();
		}

		Boolean found = false;

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof String) {
				return true;
			}
			final IResource resource = (IResource) element;
			try {
				found = false;
				resource.accept(new IResourceProxyVisitor() {

					@Override
					public boolean visit(IResourceProxy proxy) throws CoreException {
						if (proxy.getType() == IResource.FOLDER && proxy.requestResource().equals(resource) == false) {
							found = true;
							return false;
						}
						return true;
					}
				}, IResource.DEPTH_ONE);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return found;
		}
	}

	static class ProjectLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof String) {
				return element.toString();
			}
			IResource resource = (IResource) element;
			return resource.getName();
		}

		@Override
		public Image getImage(Object element) {
			try {
				if (element instanceof String) {
					return new Image(Display.getDefault(), new URL(Messages.ProjectFolderPickerDialog_0).openStream());
				}
				return new Image(Display.getDefault(), new URL("platform:/plugin/org.eclipse.ui.ide/icons/full/obj16/folder.png").openStream()); //$NON-NLS-1$
			} catch (Exception e) {
				return super.getImage(element);
			}
		}
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);

		Button button = new Button(parent, SWT.PUSH);
		button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		button.setText(Messages.ProjectFolderPickerDialog_useSourceDirectory);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				value = Path.fromOSString(srcPath).removeLastSegments(1).toOSString();
				ProjectFolderPickerDialog.super.okPressed();
			}
		});
		button.moveAbove(getButton(IDialogConstants.CANCEL_ID));

		((GridLayout) parent.getLayout()).numColumns = 3;
	}

	@Override
	protected void okPressed() {
		Object selected = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		if (selected == null || selected instanceof String) {
			value = ""; //$NON-NLS-1$
		} else {
			IResource resource = (IResource) selected;
			value = resource.getFullPath().removeFirstSegments(1).toOSString();
		}

		super.okPressed();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// TODO Auto-generated method stub
		Composite ret = (Composite) super.createDialogArea(parent);

		viewer = new TreeViewer(ret);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ProjectContentProvider());
		viewer.setLabelProvider(new ProjectLabelProvider());
		viewer.setInput(project);
		viewer.expandToLevel(2);

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});

		if (srcPath != null) {

			IPath path = Path.fromOSString(srcPath);

			Composite compPath = new Composite(ret, SWT.NONE);
			compPath.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			compPath.setLayout(new GridLayout(2, false));

			Label label = new Label(compPath, SWT.NONE);
			label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			label.setText(Messages.ProjectFolderPickerDialog_sourceResourceName);

			Label label2 = new Label(compPath, SWT.NONE);
			label2.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			label2.setText(path.lastSegment());

			Label label3 = new Label(compPath, SWT.NONE);
			label3.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			label3.setText(Messages.ProjectFolderPickerDialog_sourceResourceDirectory);

			Label lblResourcePath = new Label(compPath, SWT.NONE);
			lblResourcePath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			lblResourcePath.setText(path.removeLastSegments(1).toOSString());
		}

		String message = Messages.ProjectFolderPickerDialog_6;
		getShell().setText(message);
		setTitle(message);
		setMessage(message);
		return ret;
	}

	public String getValue() {
		return value;
	}

}
