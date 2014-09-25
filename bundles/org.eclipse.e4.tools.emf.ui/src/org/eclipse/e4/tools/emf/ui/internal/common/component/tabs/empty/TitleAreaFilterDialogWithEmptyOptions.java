/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 432555
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty;

import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.Messages;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * A standard ElementListSelectionDialog with additional options for including,
 * excluding, and selecting empty values.
 *
 * @author Steven Spungin
 *
 */
public class TitleAreaFilterDialogWithEmptyOptions extends TitleAreaFilterDialog {
	private Button btnExcludeEmptyValues;
	private Button btnOnlyEmptyValues;
	private Button btnIncludeEmptyValues;
	private EmptyFilterOption emptyFilterOption;
	private boolean bShowEmptyOptions = true;
	private Composite compOptions;
	private Composite parent;

	public TitleAreaFilterDialogWithEmptyOptions(Shell parent, ILabelProvider renderer) {
		super(parent, renderer);
	}

	@Override
	protected org.eclipse.swt.widgets.Control createDialogArea(Composite parent) {
		this.parent = parent;
		Composite comp = (Composite) super.createDialogArea(parent);

		// Label labelEmptyInfo = new Label(comp, SWT.NONE);
		// labelEmptyInfo.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
		// false, false));
		// labelEmptyInfo.setText("An empty value is defined as a null object, an empty string, or an empty collection.");

		compOptions = new Composite(comp, SWT.NONE);
		compOptions.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		compOptions.setLayout(new RowLayout());

		// labelEmptyInfo.moveAbove(getText());
		compOptions.moveAbove(getText());

		btnExcludeEmptyValues = new Button(compOptions, SWT.RADIO);
		btnExcludeEmptyValues.setText(Messages.TitleAreaFilterDialogWithEmptyOptions_excludeEmptyValues);
		btnExcludeEmptyValues.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEmptyFilterOption(EmptyFilterOption.EXCLUDE);
			}
		});

		btnIncludeEmptyValues = new Button(compOptions, SWT.RADIO);
		btnIncludeEmptyValues.setText(Messages.TitleAreaFilterDialogWithEmptyOptions_includeEmptyValues);
		btnIncludeEmptyValues.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEmptyFilterOption(EmptyFilterOption.INCLUDE);
			}
		});

		btnOnlyEmptyValues = new Button(compOptions, SWT.RADIO);
		btnOnlyEmptyValues.setText(Messages.TitleAreaFilterDialogWithEmptyOptions_onlyEmptyValues);
		btnOnlyEmptyValues.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setEmptyFilterOption(EmptyFilterOption.ONLY);
			}
		});

		String toolTip = Messages.TitleAreaFilterDialogWithEmptyOptions_emptyValueDescription;
		btnExcludeEmptyValues.setToolTipText(toolTip);
		btnIncludeEmptyValues.setToolTipText(toolTip);
		btnOnlyEmptyValues.setToolTipText(toolTip);

		setEmptyFilterOption(EmptyFilterOption.EXCLUDE);

		updateEmptyOptionState();

		parent.pack();
		return comp;
	}

	@Override
	public boolean close() {
		return super.close();
	}

	public EmptyFilterOption getEmptyFilterOption() {
		return emptyFilterOption;
	}

	public void setEmptyFilterOption(EmptyFilterOption emptyFilterOption) {
		this.emptyFilterOption = emptyFilterOption;
		updateUi();
	}

	private void updateUi() {
		if (btnExcludeEmptyValues == null) {
			return;
		}
		switch (emptyFilterOption) {
		case EXCLUDE:
			btnExcludeEmptyValues.setSelection(true);
			btnIncludeEmptyValues.setSelection(false);
			btnOnlyEmptyValues.setSelection(false);
			break;
		case INCLUDE:
			btnExcludeEmptyValues.setSelection(false);
			btnIncludeEmptyValues.setSelection(true);
			btnOnlyEmptyValues.setSelection(false);
			break;
		case ONLY:
			btnExcludeEmptyValues.setSelection(false);
			btnIncludeEmptyValues.setSelection(false);
			btnOnlyEmptyValues.setSelection(true);
			break;
		default:
			break;
		}
	}

	public void setShowEmptyOptions(boolean bShow) {
		this.bShowEmptyOptions = bShow;
		updateEmptyOptionState();
	}

	private void updateEmptyOptionState() {
		if (compOptions == null) {
			return;
		}
		compOptions.setVisible(bShowEmptyOptions);
		((GridData) compOptions.getLayoutData()).exclude = bShowEmptyOptions == false;
		parent.layout();
	}
}