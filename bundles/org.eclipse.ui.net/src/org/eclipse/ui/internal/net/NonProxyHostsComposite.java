/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * yyyymmdd bug      Email and other contact information
 * -------- -------- -----------------------------------------------------------
 * 20060217   127138 pmoogk@ca.ibm.com - Peter Moogk
 * 20070201   154100 pmoogk@ca.ibm.com - Peter Moogk, Port internet code from WTP to Eclipse base.
 *******************************************************************************/

package org.eclipse.ui.internal.net;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * This class is the Composite that consists of the controls for
 * "http.nonProxyHosts" and is used by InternetPreferencesPage.
 */
public class NonProxyHostsComposite extends Composite {
	private Table table_;
	TableViewer tableViewer_;
	private TreeSet tableValues_;
	private Button add_;
	private Button edit_;
	private Button remove_;

	public NonProxyHostsComposite(Composite parent, int style) {
		super(parent, style);
		createWidgets();
	}

	public void enableComposite(boolean enabled) {
		table_.setEnabled(enabled);
		add_.setEnabled(enabled);
		edit_.setEnabled(enabled);
		remove_.setEnabled(enabled);
	}

	protected void createWidgets() {
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 6;
		layout.verticalSpacing = 6;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		setLayout(layout);

		table_ = new Table(this, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.MULTI | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH
				| GridData.VERTICAL_ALIGN_FILL);

		table_.setLayoutData(data);
		table_.setHeaderVisible(false);
		table_.setLinesVisible(true);

		TableLayout tableLayout = new TableLayout();

		new TableColumn(table_, SWT.NONE);
		ColumnWeightData colData = new ColumnWeightData(100, 60, false);
		tableLayout.addColumnData(colData);

		table_.setLayout(tableLayout);

		tableViewer_ = new TableViewer(table_);
		tableViewer_.setContentProvider(new NonProxyHostsContentProvider());
		tableViewer_.setLabelProvider(new NonProxyHostsLabelProvider());

		tableViewer_
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						enableButtons();
					}
				});

		tableViewer_.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				editSelection();
			}
		});

		Composite buttonComp = new Composite(this, SWT.NONE);
		layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 8;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		buttonComp.setLayout(layout);
		data = new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.VERTICAL_ALIGN_FILL);
		buttonComp.setLayoutData(data);

		add_ = createButton(buttonComp, NetUIMessages.BUTTON_PREFERENCE_ADD);

		add_.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addHost();
			}
		});

		edit_ = createButton(buttonComp, NetUIMessages.BUTTON_PREFERENCE_EDIT);

		edit_.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editSelection();
			}
		});
		edit_.setEnabled(false);

		remove_ = createButton(buttonComp,
				NetUIMessages.BUTTON_PREFERENCE_REMOVE);

		remove_.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeFromList((IStructuredSelection) tableViewer_
						.getSelection());
				tableViewer_.refresh();
			}
		});
		remove_.setEnabled(false);
	}

	private Button createButton(Composite comp, String label) {
		Button button = new Button(comp, SWT.PUSH);
		button.setText(label);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.VERTICAL_ALIGN_BEGINNING);
		button.setLayoutData(data);
		return button;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		enableButtons();
	}

	public void setList(String[] hosts) {
		tableValues_ = new TreeSet(Arrays.asList(hosts));

		tableViewer_.setInput(tableValues_);
		tableViewer_.refresh();
	}

	public String[] getList() {
		return (String[]) tableValues_.toArray(new String[0]);
	}

	String getStringList(Iterator iterator) {
		StringBuffer buffer = new StringBuffer();

		if (iterator.hasNext()) {
			buffer.append((String) iterator.next());
		}

		while (iterator.hasNext()) {
			buffer.append(',');
			buffer.append((String) iterator.next());
		}

		return buffer.toString();
	}

	void removeFromList(IStructuredSelection selection) {
		tableValues_.removeAll(selection.toList());
	}

	void updateList(String value) {
		// Split the string with a delimiter of either a vertical bar, a space,
		// or a comma.
		String[] hosts = value.split("\\|| |,"); //$NON-NLS-1$

		tableValues_.addAll(Arrays.asList(hosts));
		tableValues_.remove(""); //$NON-NLS-1$
		tableViewer_.refresh();
	}

	void enableButtons() {
		boolean enabled = getEnabled();

		if (enabled) {
			boolean itemsSelected = !tableViewer_.getSelection().isEmpty();

			add_.setEnabled(true);
			edit_.setEnabled(itemsSelected);
			remove_.setEnabled(itemsSelected);
		} else {
			add_.setEnabled(false);
			edit_.setEnabled(false);
			remove_.setEnabled(false);
		}
	}

	void editSelection() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer_.getSelection();
		String selectedHosts = getStringList(selection.iterator());
		String value = promptForHost(selectedHosts);
		if (value != null) {
			removeFromList(selection);
			updateList(value);
		}
	}

	void addHost() {
		String value = promptForHost(null);
		if (value != null) {
			updateList(value);
		}
	}
	
	private String promptForHost(String selectedHosts) {
		InputDialog dialog = new InputDialog(getShell(),
				NetUIMessages.TITLE_PREFERENCE_HOSTS_DIALOG,
				NetUIMessages.LABEL_PREFERENCE_HOSTS_DIALOG, selectedHosts,
				null) {
			private ControlDecoration decorator;
			protected Control createDialogArea(Composite parent) {
				Control createDialogArea = super.createDialogArea(parent);
				decorator = new ControlDecoration(getText(), SWT.TOP | SWT.LEFT);
				decorator.setDescriptionText(NetUIMessages.NonProxyHostsComposite_0);
				decorator.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(
				FieldDecorationRegistry.DEC_INFORMATION).getImage());
				return createDialogArea;
			}
			public boolean close() {
				decorator.dispose();
				return super.close();
			}
		};
		int result = dialog.open();
		String value;
		if (result != Window.CANCEL) {
			value = dialog.getValue();
		} else {
			value = null;
		}
		return value;
	}
}
