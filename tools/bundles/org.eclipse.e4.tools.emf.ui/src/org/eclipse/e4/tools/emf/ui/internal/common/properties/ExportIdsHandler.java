/*******************************************************************************
 * Copyright (c) 2014, 2017 MEDEVIT, FHV and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marco Descher <marco@descher.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.properties;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.ResourceProvider;
import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import jakarta.inject.Named;

/**
 * This handler exports all Id values within the application model to a file for
 * static reference. Currently the location of this file is fixed to be in the
 * location of the selected projects main package with name
 * <code>AppModelId.java</code>.
 */
public class ExportIdsHandler {
	public static final String DEFAULT_APPMODELID_CLASSNAME = "AppModelId"; //$NON-NLS-1$

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, @Translation Messages messages, IModelResource resource, IResourcePool pool, IProject project) {
		TitleAreaDialog dialog = new ExportIdDialog(shell, messages, resource.getRoot(), pool, project);
		dialog.open();
	}

	static class ExportIdDialog extends TitleAreaDialog {
		private final Messages messages;
		private final IObservableList<?> list;
		private final IResourcePool pool;
		private final JavaClass clazz;
		private CheckboxTableViewer viewer;
		private Text textClassName;

		public ExportIdDialog(Shell parentShell, Messages messages, IObservableList<?> list, IResourcePool pool,
				IProject project) {
			super(parentShell);
			this.messages = messages;
			this.list = list;
			this.pool = pool;

			clazz = new JavaClass();
			@SuppressWarnings("restriction")
			boolean hasJavaNature = org.eclipse.jdt.internal.core.JavaProject.hasJavaNature(project);
			if (hasJavaNature) {
				try {
					IJavaProject javaProject = JavaCore.create(project);
					for (IPackageFragmentRoot iPackageFragmentRoot : javaProject.getAllPackageFragmentRoots()) {
						if (iPackageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
							clazz.packageFragment = iPackageFragmentRoot.createPackageFragment(project.getName(), false, new NullProgressMonitor());
							break;
						}
					}
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
			} else {
				setErrorMessage(messages.ExportIdsHandler_Dialog_SelectProject);
				getButton(IDialogConstants.OK_ID).setEnabled(false);
			}
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			getShell().setText(messages.ExportIdsHandler_Dialog_ShellTitle);
			setTitle(messages.ExportIdsHandler_Dialog_DialogTitle);
			setMessage(messages.ExportIdsHandler_Dialog_DialogMessage);
			setTitleImage(pool.getImageUnchecked(ResourceProvider.IMG_Wizban16_extstr_wiz));

			Composite content = (Composite) super.createDialogArea(parent);
			content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			Composite container = new Composite(content, SWT.NONE);
			container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			GridLayout gl = new GridLayout(2, false);
			gl.numColumns = 2;
			gl.marginBottom = 4;

			container.setLayout(gl);
			Label lblTargetClass = new Label(container, SWT.NONE);
			lblTargetClass.setText(messages.ExportIdsHandler_Dialog_TargetClassName);

			textClassName = new Text(container, SWT.BORDER);
			textClassName.setText(DEFAULT_APPMODELID_CLASSNAME);

			GridData gdcn = new GridData();
			gdcn.horizontalAlignment = SWT.FILL;
			gdcn.grabExcessHorizontalSpace = true;

			textClassName.setLayoutData(gdcn);
			Table t = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CHECK);

			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = t.getItemHeight() * 18;
			container.setLayoutData(gd);

			GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd2.horizontalSpan = 2;
			gd2.heightHint = t.getItemHeight() * 17;
			t.setHeaderVisible(true);
			t.setLinesVisible(true);
			t.setLayoutData(gd2);

			viewer = new CheckboxTableViewer(t);

			{
				TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						return ""; //$NON-NLS-1$
					}
				});
			}

			{
				TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
				column.getColumn().setText(messages.ExportIdsHandler_Dialog_ElementName);
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entry e = (Entry) element;
						return ((EObject) e.object).eClass().getName();
					}
				});
			}

			{
				TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
				column.getColumn().setText(messages.ExportIdsHandler_Dialog_Key);
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entry e = (Entry) element;
						return e.idFieldKey;
					}
				});
			}

			{
				TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
				column.getColumn().setText(messages.ExportIdsHandler_Dialog_Id_Value);
				column.setLabelProvider(new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						Entry e = (Entry) element;
						return e.elementId;
					}
				});
			}

			for (int i = 1; i < viewer.getTable().getColumnCount(); i++) {
				TableColumn c = viewer.getTable().getColumn(i);
				c.pack();
				if (c.getWidth() < 120) {
					c.setWidth(120);
				}
			}

			{
				Label l = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
				l.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, 3, 1));
			}

			List<Entry> entries = new ArrayList<>();
			TreeIterator<EObject> it = EcoreUtil.getAllContents(list);

			while (it.hasNext()) {
				Object next = it.next();
				if (next instanceof MApplicationElement) {
					MApplicationElement o = (MApplicationElement) next;
					if (o.getElementId() != null && o.getElementId().length() > 1) {
						String idFieldKey = findIdFieldKey(o);
						entries.add(new Entry(o, idFieldKey, o.getElementId()));
					}
				}
			}

			entries.sort(null);

			viewer.setContentProvider(ArrayContentProvider.getInstance());
			viewer.setInput(entries);
			viewer.setAllChecked(true);

			for (int i = 1; i < viewer.getTable().getColumnCount(); i++) {
				TableColumn c = viewer.getTable().getColumn(i);
				c.pack();
				if (c.getWidth() < 120) {
					c.setWidth(120);
				}
			}

			return container;
		}

		@Override
		protected void okPressed() { // See AbstractNewClassWizard
			clazz.name = textClassName.getText();

			Object[] els = viewer.getCheckedElements();
			if (els.length > 0) {
				try {
					String content = compileFileContent(els);

					IPackageFragment fragment = clazz.packageFragment;
					String cuName = clazz.name + ".java"; //$NON-NLS-1$
					ICompilationUnit unit = fragment.getCompilationUnit(cuName);
					IResource resource = unit.getResource();
					IFile file = (IFile) resource;

					try (ByteArrayInputStream stream = new ByteArrayInputStream(content.toString().getBytes())) {
						if (file.exists()) {
							file.delete(true, new NullProgressMonitor());
						}

						createParent(file.getParent());
						// NPE
						file.create(stream, IResource.KEEP_HISTORY, new NullProgressMonitor());

					}
					super.okPressed();
				} catch (CoreException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private String compileFileContent(Object[] els) {
			StringBuilder b = new StringBuilder();
			b.append("package " + clazz.packageFragment.getElementName() + ";" + System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$
			b.append(System.lineSeparator());
			b.append("public class " + clazz.name + " {" + System.lineSeparator()); //$NON-NLS-1$ //$NON-NLS-2$

			for (Object o : els) {
				Entry e = (Entry) o;
				b.append("\tpublic static final String " + e.idFieldKey + " = \"" + e.elementId + "\";" + System.lineSeparator()); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
			}

			b.append("}"); //$NON-NLS-1$
			return b.toString();
		}

		private void createParent(IContainer container) throws CoreException {
			if (!container.exists()) {

				createParent(container.getParent());

				if (container instanceof IFolder) {
					IFolder f = (IFolder) container;
					f.create(true, true, new NullProgressMonitor());
				}
			}
		}
	}

	private static String findIdFieldKey(MApplicationElement object) {
		StringBuilder sb = new StringBuilder();
		sb.append(((EObject) object).eClass().getName());
		sb.append("_"); //$NON-NLS-1$
		sb.append(replaceInvalidChar(object.getElementId()));
		return sb.toString().toUpperCase();
	}

	private static String replaceInvalidChar(String elementId) {
		return elementId.replaceAll("[^A-Za-z0-9]", "_"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private static class Entry implements Comparable<Entry> {
		private final MApplicationElement object;
		private final String idFieldKey;
		private final String elementId;

		public Entry(MApplicationElement object, String idFieldKey, String elementId) {
			this.object = object;
			this.idFieldKey = idFieldKey;
			this.elementId = elementId;
		}

		@Override
		public int compareTo(Entry o) {
			return this.idFieldKey.compareTo(o.idFieldKey);
		}
	}

	/**
	 * @see org.eclipse.e4.internal.tools.wizards.classes.AbstractNewClassPage
	 */
	private static class JavaClass {
		private IPackageFragment packageFragment;
		private String name;
	}

}