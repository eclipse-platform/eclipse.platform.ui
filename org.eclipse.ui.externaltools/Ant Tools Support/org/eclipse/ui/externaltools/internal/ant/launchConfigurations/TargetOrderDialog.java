/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.externaltools.internal.model.IExternalToolsHelpContextIds;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Dialog to specify target execution order
 */
public class TargetOrderDialog extends Dialog implements ISelectionChangedListener {
	
	private Button fUp;
	private Button fDown;
	private TableViewer fViewer;
	private AntTargetContentProvider fContentProvider;
	private Object[] fTargets;

	/**
	 * Constructs the dialog.
	 * 
	 * @param parentShell
	 */
	public TargetOrderDialog(Shell parentShell, Object[] targets) {
		super(parentShell);
		fTargets = targets;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		getShell().setText(AntLaunchConfigurationMessages.getString("TargetOrderDialog.Order_Targets_1")); //$NON-NLS-1$
		
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		
		Label label = new Label(comp, SWT.NONE);
		label.setText(AntLaunchConfigurationMessages.getString("TargetOrderDialog.&Specify_target_execution_order__2")); //$NON-NLS-1$
		label.setFont(comp.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);		
		
		createTargetList(comp);
		
		createButtons(comp);
		
		updateButtons();
		
		return comp;
	}

	/**
	 * Create button area & buttons
	 * 
	 * @param comp
	 */
	private void createButtons(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.verticalAlignment = GridData.BEGINNING;
		comp.setLayout(layout);
		comp.setLayoutData(gd);
		
		fUp = new Button(comp, SWT.PUSH);
		fUp.setFont(parent.getFont());
		fUp.setText(AntLaunchConfigurationMessages.getString("TargetOrderDialog.&Up_3")); //$NON-NLS-1$
		setButtonLayoutData(fUp);
		fUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleUpPressed();
			}
		});
		
		fDown = new Button(comp, SWT.PUSH);
		fDown.setFont(parent.getFont());
		fDown.setText(AntLaunchConfigurationMessages.getString("TargetOrderDialog.&Down_4")); //$NON-NLS-1$
		setButtonLayoutData(fDown);
		fDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleDownPressed();
			}
		});
		
	}

	/**
	 * Down
	 */
	protected void handleDownPressed() {
		int[] selections = fViewer.getTable().getSelectionIndices();
		for (int i = 0; i < selections.length; i++) {
			fContentProvider.moveDownTarget(selections[i]);
		}
		fTargets = fContentProvider.getElements(null);
		fViewer.refresh();
		updateButtons();
	}

	/**
	 * Up
	 */
	protected void handleUpPressed() {
		int[] selections = fViewer.getTable().getSelectionIndices();
		for (int i = 0; i < selections.length; i++) {
			fContentProvider.moveUpTarget(selections[i]);
		}		
		fTargets = fContentProvider.getElements(null);
		fViewer.refresh();
		updateButtons();
	}

	/**
	 * Creates a list viewer for the targets
	 * 
	 * @param comp
	 */
	private void createTargetList(Composite comp) {
		fViewer = new TableViewer(comp, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		fViewer.setLabelProvider(new TargetTableLabelProvider());
		fContentProvider = new AntTargetContentProvider();
		fViewer.setContentProvider(fContentProvider);
		fViewer.setInput(fTargets);
		fViewer.addSelectionChangedListener(this);
		Table table = fViewer.getTable();
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		gd.widthHint = 250;		
		table.setLayoutData(gd);
	}
	
	/**
	 * Returns the ordered targets
	 */
	public Object[] getTargets() {
		return fTargets;
	}
	
	/**
	 * Update button enablement
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		updateButtons();
	}
	
	private void updateButtons() {
		int[] selections = fViewer.getTable().getSelectionIndices();
		int last = fTargets.length - 1;
		boolean up = true && selections.length > 0;
		boolean down = true && selections.length > 0;
		for (int i = 0; i < selections.length; i++) {
			if (selections[i] == 0) {
				up = false;
			}
			if (selections[i] == last) {
				down = false;
			}
		}
		fUp.setEnabled(up);
		fDown.setEnabled(down);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		WorkbenchHelp.setHelp(shell, IExternalToolsHelpContextIds.TARGET_ORDER_DIALOG);
	}
}
