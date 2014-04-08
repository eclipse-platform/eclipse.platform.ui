/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432555
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty;

import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.Messages;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Extends TitleAreaDialog to include a filter text box.<br />
 * If no item is selected, the dialog result is Dialog.Cancel, even if OK is
 * pressed. A result of Dialog.Ok is ensured to have at least 1 selected item.
 *
 * @author Steven Spungin
 *
 */
public class TitleAreaFilterDialog extends TitleAreaDialog {

	private ILabelProvider labelProvider;
	private TableViewer viewer;
	private Object result;
	private Text text;

	public TitleAreaFilterDialog(Shell parentShell, ILabelProvider labelProvider) {
		super(parentShell);
		this.labelProvider = labelProvider;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);

		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		comp.setLayout(new GridLayout(1, false));

		text = new Text(comp, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setMessage(Messages.TitleAreaFilterDialog_enterFilterText);

		viewer = new TableViewer(comp, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(ArrayContentProvider.getInstance());

		viewer.setFilters(new ViewerFilter[] { new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return isFiltered(element, text.getText());
			}
		} });

		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				viewer.refresh();
			}
		});

		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				result = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
				if (result != null) {
					okPressed();
				}
			}
		});

		return comp;
	}

	/**
	 * Default returns true if string value contains the filterText, and it is
	 * case insensitive.
	 *
	 * @param element
	 * @param filterText
	 * @return
	 */
	protected boolean isFiltered(Object element, String filterText) {
		filterText = filterText.toLowerCase();
		if (filterText.isEmpty()) {
			return true;
		} else if (element.toString().toLowerCase().contains(filterText)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void okPressed() {
		result = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
		if (result == null) {
			cancelPressed();
		} else {
			super.okPressed();
		}
	}

	public void setElements(Object[] array) {
		viewer.setInput(array);
	}

	public Object getFirstElement() {
		return result;
	}

	protected TableViewer getViewer() {
		return viewer;
	}

	protected Text getText() {
		return text;
	}

	public List<?> asList() {
		Object firstElement = getFirstElement();
		if (firstElement == null) {
			return Collections.EMPTY_LIST;
		} else {
			return Arrays.asList(firstElement);
		}
	}

}
