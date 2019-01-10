/*******************************************************************************
 * Copyright (c) 2003, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 485843
 *******************************************************************************/
package org.eclipse.ui.internal.views.log;

import java.util.StringTokenizer;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PlatformUI;

public class FilterDialog extends TrayDialog {

	Button okButton;

	// entries count limit
	private Button limit;
	Text limitText;
	Text maxLogTailSizeText;

	// entry types filter
	private Button errorCheckbox;
	private Button warningCheckbox;
	private Button infoCheckbox;
	private Button okCheckbox;

	// show all sessions
	private Button showAllButton;

	// filter stack trace elements in EventDetailsDialog
	private Button filterEnabled;
	private Button addFilter;
	private Button removeFilter;
	private List filterList;

	private IMemento memento;

	public FilterDialog(Shell parentShell, IMemento memento) {
		super(parentShell);
		this.memento = memento;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IHelpContextIds.LOG_FILTER);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		createEventTypesGroup(container);
		createLimitSection(container);
		createSessionSection(container);
		createFilterSection(container);

		Dialog.applyDialogFont(container);
		return container;
	}

	private void createEventTypesGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 275;
		group.setLayoutData(gd);
		group.setText(Messages.LogView_FilterDialog_eventTypes);

		okCheckbox = new Button(group, SWT.CHECK);
		okCheckbox.setText(Messages.LogView_FilterDialog_ok);
		okCheckbox.setSelection(memento.getString(LogView.P_LOG_OK).equals("true")); //$NON-NLS-1$

		infoCheckbox = new Button(group, SWT.CHECK);
		infoCheckbox.setText(Messages.LogView_FilterDialog_information);
		infoCheckbox.setSelection(memento.getString(LogView.P_LOG_INFO).equals("true")); //$NON-NLS-1$

		warningCheckbox = new Button(group, SWT.CHECK);
		warningCheckbox.setText(Messages.LogView_FilterDialog_warning);
		warningCheckbox.setSelection(memento.getString(LogView.P_LOG_WARNING).equals("true")); //$NON-NLS-1$

		errorCheckbox = new Button(group, SWT.CHECK);
		errorCheckbox.setText(Messages.LogView_FilterDialog_error);
		errorCheckbox.setSelection(memento.getString(LogView.P_LOG_ERROR).equals("true")); //$NON-NLS-1$
	}

	private void createLimitSection(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		limit = new Button(comp, SWT.CHECK);
		limit.setText(Messages.LogView_FilterDialog_limitTo);
		limit.setSelection(memento.getString(LogView.P_USE_LIMIT).equals("true")); //$NON-NLS-1$
		limit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				limitText.setEnabled(((Button) e.getSource()).getSelection());
			}
		});

		limitText = new Text(comp, SWT.BORDER);
		limitText.addVerifyListener(e -> {
			if (Character.isLetter(e.character)) {
				e.doit = false;
			}
		});
		limitText.addModifyListener(e -> {
			try {
				if (okButton == null)
					return;
				int value = Integer.parseInt(limitText.getText());
				okButton.setEnabled(value > 0);
			} catch (NumberFormatException e1) {
				okButton.setEnabled(false);
			}
		});
		limitText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		limitText.setText(memento.getString(LogView.P_LOG_LIMIT));
		limitText.setEnabled(limit.getSelection());

		Label maxLogTailSizeLabel = new Label(comp, SWT.NONE);
		maxLogTailSizeLabel.setText(Messages.LogView_FilterDialog_maxLogTailSize);

		maxLogTailSizeText = new Text(comp, SWT.BORDER);
		maxLogTailSizeText.addVerifyListener(e -> {
			if (Character.isLetter(e.character)) {
				e.doit = false;
			}
		});

		maxLogTailSizeText.addModifyListener(e -> {
			try {
				if (okButton == null)
					return;
				int value = Integer.parseInt(maxLogTailSizeText.getText());
				okButton.setEnabled(value > 0);
			} catch (NumberFormatException e1) {
				okButton.setEnabled(false);
			}
		});
		maxLogTailSizeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		maxLogTailSizeText.setText(memento.getString(LogView.P_LOG_MAX_TAIL_SIZE));
		maxLogTailSizeText.setEnabled(limit.getSelection());
	}

	private void createSessionSection(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label label = new Label(container, SWT.NONE);
		label.setText(Messages.LogView_FilterDialog_eventsLogged);

		showAllButton = new Button(container, SWT.RADIO);
		showAllButton.setText(Messages.LogView_FilterDialog_allSessions);
		GridData gd = new GridData();
		gd.horizontalIndent = 20;
		showAllButton.setLayoutData(gd);

		Button button = new Button(container, SWT.RADIO);
		button.setText(Messages.LogView_FilterDialog_recentSession);
		gd = new GridData();
		gd.horizontalIndent = 20;
		button.setLayoutData(gd);

		if (memento.getString(LogView.P_SHOW_ALL_SESSIONS).equals("true")) { //$NON-NLS-1$
			showAllButton.setSelection(true);
		} else {
			button.setSelection(true);
		}
	}

	private void createFilterSection(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		filterEnabled = new Button(comp, SWT.CHECK);
		filterEnabled.setText(Messages.FilterDialog_EnableFiltersCheckbox);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		filterEnabled.setLayoutData(gd);
		filterEnabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setStackTraceFilterEnabled(filterEnabled.getSelection());
			}

		});

		filterList = new List(comp, SWT.BORDER | SWT.MULTI);
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		gd.verticalSpan = 3;
		gd.widthHint = 280;
		gd.horizontalIndent = 20;
		filterList.setLayoutData(gd);
		filterList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeFilter.setEnabled(true);
			}
		});

		addFilter = new Button(comp, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		addFilter.setLayoutData(gd);
		addFilter.setText(Messages.FilterDialog_Add);
		addFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addFilter();
			}
		});

		removeFilter = new Button(comp, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		removeFilter.setLayoutData(gd);
		removeFilter.setText(Messages.FilterDialog_Remove);
		removeFilter.setEnabled(false);
		removeFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				removeFilter();
			}
		});

		// load preferences
		Boolean enable = memento.getBoolean(EventDetailsDialog.FILTER_ENABLED);
		enable = enable == null ? Boolean.FALSE : enable;

		filterEnabled.setSelection(enable.booleanValue());
		setStackTraceFilterEnabled(enable.booleanValue());

		String filters = memento.getString(EventDetailsDialog.FILTER_LIST);
		if (filters != null) {
			StringTokenizer st = new StringTokenizer(filters, ";"); //$NON-NLS-1$
			while (st.hasMoreElements()) {
				filterList.add(st.nextToken());
			}
		}
	}

	private void addFilter() {
		IInputValidator validator = newText -> newText.indexOf(';') >= 0
				? Messages.FilterDialog_FilterShouldntContainSemicolon
				: null;
		InputDialog dialog = new InputDialog(getShell(), Messages.FilterDialog_AddFilterTitle, Messages.FilterDialog_AddFliterLabel, null, validator);
		if (dialog.open() == Window.OK) {
			String value = dialog.getValue().trim();

			if (value.length() > 0) {
				filterList.add(value);
			}
		}
	}

	private void removeFilter() {
		String[] selected = filterList.getSelection();
		for (String element : selected) {
			filterList.remove(element);
		}
		removeFilter.setEnabled(false);
	}

	private void setStackTraceFilterEnabled(boolean enabled) {
		filterList.setEnabled(enabled);
		addFilter.setEnabled(enabled);
		removeFilter.setEnabled(enabled && filterList.getSelectionIndex() != -1);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void okPressed() {
		memento.putString(LogView.P_LOG_OK, okCheckbox.getSelection() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(LogView.P_LOG_INFO, infoCheckbox.getSelection() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(LogView.P_LOG_WARNING, warningCheckbox.getSelection() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(LogView.P_LOG_ERROR, errorCheckbox.getSelection() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(LogView.P_LOG_LIMIT, limitText.getText());
		memento.putString(LogView.P_USE_LIMIT, limit.getSelection() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$
		memento.putString(LogView.P_LOG_MAX_TAIL_SIZE, maxLogTailSizeText.getText());
		memento.putString(LogView.P_SHOW_ALL_SESSIONS, showAllButton.getSelection() ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$

		// store Event Dialog stack trace filter preferences
		memento.putBoolean(EventDetailsDialog.FILTER_ENABLED, filterEnabled.getSelection());

		StringBuilder sb = new StringBuilder();
		String[] items = filterList.getItems();
		for (int i = 0; i < items.length; i++) {
			sb.append(items[i]);
			if (i < items.length - 1) {
				sb.append(";"); //$NON-NLS-1$
			}
		}
		memento.putString(EventDetailsDialog.FILTER_LIST, sb.toString());

		super.okPressed();
	}

}
