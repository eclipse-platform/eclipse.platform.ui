/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring.preferences;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A dialog box used to input new stack traces to filter out.
 */
public class FilterInputDialog extends TitleAreaDialog {
	private Text textFilter;
	private String filter;

	public FilterInputDialog(Shell parentShell) {
		super(parentShell);
		this.create();
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.FilterInputDialog_filter_input_header);
		setMessage(Messages.FilterInputDialog_filter_input_message,
				IMessageProvider.INFORMATION);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(2, false);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(layout);

		createFilterText(container);

		return area;
	}

	private void createFilterText(Composite container) {
		Label filterLabel = new Label(container, SWT.NONE);
		filterLabel.setText(Messages.FilterInputDialog_filter_input_label);

		GridData dataFilterInput = new GridData();
		dataFilterInput.grabExcessHorizontalSpace = true;
		dataFilterInput.horizontalAlignment = GridData.FILL;

		textFilter = new Text(container, SWT.BORDER);
		textFilter.setLayoutData(dataFilterInput);
	}

	@Override
	protected boolean isResizable() {
		return false;
	}

	private void saveInput() {
		filter = textFilter.getText();
	}

	@Override
	protected void okPressed() {
		saveInput();
		textFilter.clearSelection();
		super.okPressed();
	}

	/**
	 * Returns a {@code String} of the user-defined filter.
	 */
	 public String getInput() {
		 return filter;
	 }
}
