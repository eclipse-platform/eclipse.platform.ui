/*******************************************************************************
 * Copyright (C) 2014, 2015 Google Inc and others.
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
			Pattern.compile("([\\p{L}_$][\\p{L}\\p{N}_$]*\\.)+[\\p{L}_$][\\p{L}\\p{N}_$]*" //$NON-NLS-1$
					+ "|([\\p{L}_$][\\p{L}\\p{N}_$]*\\.?)*([\\?\\*][\\p{L}\\p{N}_$\\.]*)+"); //$NON-NLS-1$

	private Text textFilter;
	private String filter;

	public FilterInputDialog(Shell parentShell, String message) {
		super(parentShell);
		create();
		setMessage(message, IMessageProvider.INFORMATION);
	}

	@Override
	public void create() {
		super.create();
		setTitle(Messages.FilterInputDialog_header);
	}

    @Override
	protected void configureShell(Shell shell) {
        super.configureShell(shell);
		shell.setText(Messages.FilterInputDialog_title);
    }

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(new GridLayout(2, false));

		Label filterLabel = new Label(container, SWT.NONE);
		filterLabel.setText(Messages.FilterInputDialog_filter_input_label);

		textFilter = new Text(container, SWT.BORDER);
		textFilter.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		textFilter.addModifyListener(e -> checkInput());
		new Label(container, SWT.NONE); // Placeholder to push noteLabel to the second column.
		Label noteLabel = new Label(container, SWT.NONE);
		noteLabel.setText(Messages.FilterInputDialog_note_label);

		return area;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		checkInput();
		return contents;
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
