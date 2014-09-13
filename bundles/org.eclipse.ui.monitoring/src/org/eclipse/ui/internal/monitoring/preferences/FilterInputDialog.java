/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring.preferences;

import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A dialog box used to input new stack traces to filter out.
 */
public class FilterInputDialog extends TitleAreaDialog {
	private static final Pattern methodNamePattern =
			Pattern.compile("([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)+[\\p{L}_$][\\p{L}\\p{N}_$]*");

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
		setMessage(Messages.FilterInputDialog_filter_input_message, IMessageProvider.INFORMATION);
	}

    @Override
	protected void configureShell(Shell shell) {
        super.configureShell(shell);
		shell.setText(Messages.FilterInputDialog_filter_input_title);
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

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		checkInput();
		return contents;
	}

	private void createFilterText(Composite container) {
		Label filterLabel = new Label(container, SWT.NONE);
		filterLabel.setText(Messages.FilterInputDialog_filter_input_label);

		textFilter = new Text(container, SWT.BORDER);
		textFilter.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		textFilter.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				checkInput();
			}
		});
	}

	private void checkInput() {
		filter = textFilter.getText().trim();
		Button okButton = getButton(IDialogConstants.OK_ID);
		String errorMessage = null;
		if (!methodNamePattern.matcher(filter).matches()) {
			if (!filter.isEmpty()) {
				errorMessage = Messages.FilterInputDialog_invalid_method_name;
			}
			okButton.setEnabled(false);
		} else {
			okButton.setEnabled(true);
		}
		setErrorMessage(errorMessage);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	/**
	 * Returns a {@code String} of the user-defined filter.
	 */
	public String getFilter() {
		return filter;
	}
}
