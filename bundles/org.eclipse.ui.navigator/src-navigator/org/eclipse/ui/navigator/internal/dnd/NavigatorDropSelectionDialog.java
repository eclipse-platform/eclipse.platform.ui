/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on May 7, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.navigator.internal.dnd;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.navigator.internal.NavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;


/**
 * @author jsholl
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Generation - Code and Comments
 */
public class NavigatorDropSelectionDialog extends Dialog {

	public static final String SKIP_ON_SINGLE_SELECTION = NavigatorMessages.getString("NavigatorDropSelectionDialog.0"); //$NON-NLS-1$

	private DropHandlerDescriptor[] descriptors;
	private Button[] radios;
	private Button skipDialogOnSingleSelection;
	private Text descriptionText;
	private DropHandlerDescriptor selectedDescriptor;
	private boolean checkedDefault = false;

	public NavigatorDropSelectionDialog(Shell parentShell, DropHandlerDescriptor[] descriptors) {
		super(parentShell);
		this.descriptors = descriptors;
	}

	protected Control createDialogArea(Composite parent) {
		getShell().setText(NavigatorMessages.getString("NavigatorDropSelectionDialog.1")); //$NON-NLS-1$
		Composite superComposite = (Composite) super.createDialogArea(parent);

		Composite composite = new Composite(superComposite, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		radios = new Button[descriptors.length];

		Group radioGroup = new Group(composite, SWT.SHADOW_NONE);
		radioGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout groupLayout = new GridLayout();
		groupLayout.marginHeight = 0;
		groupLayout.marginWidth = 0;
		groupLayout.verticalSpacing = 0;
		groupLayout.horizontalSpacing = 0;
		groupLayout.numColumns = 1;
		radioGroup.setLayout(groupLayout);

		final int arrayLength = descriptors.length; // = radios.length
		for (int i = 0; i < arrayLength; i++) {
			radios[i] = new Button(radioGroup, SWT.RADIO);
			radios[i].setText(descriptors[i].getName());

			radios[i].addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Object source = e.getSource();
					for (int j = 0; j < arrayLength; j++) {
						if (source == radios[j]) {
							selectedDescriptor = descriptors[j];
							descriptionText.setText(selectedDescriptor.getDescription());
							return;
						}
					}
					selectedDescriptor = null;
					descriptionText.setText(""); //$NON-NLS-1$
				}
			});
		}

		descriptionText = new Text(composite, SWT.BORDER | SWT.WRAP);
		GridData descriptionTextGridData = new GridData(GridData.FILL_HORIZONTAL);
		descriptionTextGridData.heightHint = convertHeightInCharsToPixels(3);
		descriptionText.setLayoutData(descriptionTextGridData);
		descriptionText.setBackground(superComposite.getBackground());

		skipDialogOnSingleSelection = new Button(composite, SWT.CHECK);
		skipDialogOnSingleSelection.setText(NavigatorMessages.getString("NavigatorDropSelectionDialog.3")); //$NON-NLS-1$
		skipDialogOnSingleSelection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		checkedDefault = NavigatorPlugin.getDefault().getDialogSettings().getBoolean(NavigatorDropSelectionDialog.SKIP_ON_SINGLE_SELECTION);
		skipDialogOnSingleSelection.setSelection(checkedDefault);

		setDefaultSelection();
		return composite;
	}

	protected void okPressed() {
		if (checkedDefault != skipDialogOnSingleSelection.getSelection()) {
			NavigatorPlugin.getDefault().getDialogSettings().put(NavigatorDropSelectionDialog.SKIP_ON_SINGLE_SELECTION, skipDialogOnSingleSelection.getSelection());
		}
		super.okPressed();
	}

	private void setDefaultSelection() {
		radios[0].setSelection(true);
		selectedDescriptor = descriptors[0];
		descriptionText.setText(selectedDescriptor.getDescription());
	}

	/**
	 * @return Returns the selectedDescriptor.
	 */
	public DropHandlerDescriptor getSelectedDescriptor() {
		return selectedDescriptor;
	}
}