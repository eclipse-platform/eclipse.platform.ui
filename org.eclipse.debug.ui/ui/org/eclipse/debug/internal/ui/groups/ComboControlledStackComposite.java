/*******************************************************************************
 *  Copyright (c) 2009, 2016 QNX Software Systems and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      QNX Software Systems - initial API and implementation
 *      Freescale Semiconductor
 *******************************************************************************/
package org.eclipse.debug.internal.ui.groups;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * Stack Composite - Switch between panes controlled by combo box
 * <p>
 * Copied from CDT (org.eclipse.cdt.launch)
 */
class ComboControlledStackComposite extends Composite {
	private Composite fArea;
	private Combo fCombo;
	private Map<String, Composite> tabMap; // label ==> tab
	private Map<String, String> capMap = new TreeMap<>();
	private StackLayout layout;
	private Label fLabel;

	public ComboControlledStackComposite(Composite parent, int style) {
		super(parent, style);
		tabMap = new LinkedHashMap<String, Composite>();
		setLayout(new GridLayout(2, false));
		createContents(this);
	}

	public void setLabelText(String label) {
		fLabel.setText(label);
	}

	private static String capitalize(String l) {
		return l.substring(0, 1).toUpperCase() + l.substring(1);
	}

	public void addItem(String label, Composite tab) {
		tabMap.put(label, tab);
		String cap = capitalize(label);
		fCombo.add(cap);
		capMap.put(cap, label);
		if (layout.topControl==null) {
			layout.topControl = tab;
			fCombo.setText(cap);
		}
	}

	public void deleteItem(String label) {
		if (capMap.get(fCombo.getText()).equals(label)) {
			setSelection(fCombo.getItem(0));
		}
		Composite tab = tabMap.get(label);
		if (tab != null) {
			tab.dispose();
			tabMap.remove(label);
			capMap.remove(capitalize(label));
		}
	}

	public void setSelection(String label) {
		fCombo.setText(capitalize(label));
		setPage(label);
	}

	protected void createContents(Composite parent) {
		fLabel = createLabel(this);
		fCombo = createCombo(this);
		GridData cgd = new GridData(GridData.FILL_HORIZONTAL);

		fCombo.setLayoutData(cgd);
		fArea = createTabArea(this);
		GridData agd = new GridData(GridData.FILL_BOTH);
		agd.horizontalSpan = 2;
		fArea.setLayoutData(agd);
	}


	public Composite getStackParent() {
		return fArea;
	}

	public Label getLabel() {
		return fLabel;
	}

	/**
	 * @return the underlying combo, should NOT be used to get the actual text,
	 *         use {@link #getSelection()} instead.
	 */
	public Combo getCombo() {
		return fCombo;
	}

	public String getSelection() {
		return capMap.get(fCombo.getText());
	}

	protected Composite createTabArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		layout = new StackLayout();
		comp.setLayout(layout);

		return comp;
	}


	protected Label createLabel(Composite parent) {
		Label label = new Label(parent, SWT.WRAP);
	    return label;
    }

	protected Combo createCombo(Composite parent) {
		Combo box = new Combo(parent, SWT.READ_ONLY);
		box.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String name = fCombo.getText();
				comboSelected(name);
			}
		});
		return box;
	}

	protected void comboSelected(String label) {
		setPage(capMap.get(label));
	}

	protected void setPage(String label) {
		layout.topControl = tabMap.get(label);
		getStackParent().layout();
	}

	public Control getTopControl() {
		return layout != null ? layout.topControl : null;
	}
}
