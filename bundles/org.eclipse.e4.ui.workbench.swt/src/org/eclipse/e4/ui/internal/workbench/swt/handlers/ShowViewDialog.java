/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids - bug 128526, bug 128529
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * The Show View dialog. The real one is in
 * org.eclipse.ui.internal.dialogs.ShowViewDialog; this is a temporary
 * placeholder as the original dialog brings lots of extra things in.
 */
public class ShowViewDialog extends Dialog {

	private class DescriptorProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return ((MApplication) inputElement).getDescriptors().toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private MPartDescriptor[] selectedDescriptors;

	private MApplication application;

	TableViewer tableViewer;

	public ShowViewDialog(Shell shell, MApplication application) {
		super(shell);
		this.application = application;
	}

	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer
				.getSelection();
		ArrayList<MPartDescriptor> tmp = new ArrayList<MPartDescriptor>(
				selection.size());
		for (Iterator<MPartDescriptor> i = selection.iterator(); i.hasNext();) {
			tmp.add(i.next());
		}
		selectedDescriptors = new MPartDescriptor[tmp.size()];
		tmp.toArray(selectedDescriptors);

		super.okPressed();
	}

	protected void cancelPressed() {
		selectedDescriptors = null;
		super.cancelPressed();
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Show View");
	}

	/**
	 * Creates and returns the contents of the upper part of this dialog (above
	 * the button bar).
	 * 
	 * @param parent
	 *            the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setFont(parent.getFont());

		tableViewer = new TableViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.BORDER | SWT.MULTI);
		tableViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				if (!(element instanceof MPartDescriptor))
					return super.getText(element);
				return ((MPartDescriptor) element).getLabel();
			}
		});
		tableViewer.setContentProvider(new DescriptorProvider());
		tableViewer.setComparator(new ViewerComparator());
		tableViewer.setInput(application);

		layoutTopControl(tableViewer.getControl());
		applyDialogFont(composite);
		return composite;
	}

	public MPartDescriptor[] getSelection() {
		return selectedDescriptors;
	}

	/**
	 * Layout the top control.
	 * 
	 * @param control
	 *            the control.
	 */
	private void layoutTopControl(Control control) {
		GridData spec = new GridData(GridData.FILL_BOTH);
		spec.widthHint = 250;
		spec.heightHint = 300;
		control.setLayoutData(spec);
	}

	protected boolean isResizable() {
		return true;
	}
}
