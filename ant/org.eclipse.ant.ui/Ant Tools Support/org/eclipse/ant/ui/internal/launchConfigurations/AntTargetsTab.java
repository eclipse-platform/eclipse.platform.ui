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
package org.eclipse.ant.ui.internal.launchConfigurations;


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ant.core.TargetInfo;
import org.eclipse.ant.ui.internal.model.AntUIImages;
import org.eclipse.ant.ui.internal.model.AntUIPlugin;
import org.eclipse.ant.ui.internal.model.AntUtil;
import org.eclipse.ant.ui.internal.model.IAntUIConstants;
import org.eclipse.ant.ui.internal.model.IAntUIHelpContextIds;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.variables.VariableContextManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.model.ToolUtil;
import org.eclipse.ui.help.WorkbenchHelp;

public class AntTargetsTab extends AbstractLaunchConfigurationTab {
	
	private TargetInfo fDefaultTarget = null;
	private TargetInfo[] fAllTargets= null;
	private List fOrderedTargets = null;
	
	private String fLocation= null;
	
	private CheckboxTableViewer fTableViewer = null;
	private Label fSelectionCountLabel = null;
	private Text fTargetOrderText = null;
	private Button fOrderButton = null;
	
	private ILaunchConfiguration fLaunchConfiguration;
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		WorkbenchHelp.setHelp(getControl(), IAntUIHelpContextIds.ANT_TARGETS_TAB);
		 
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);		
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		comp.setFont(font);
		
		Label label = new Label(comp, SWT.NONE);
		label.setFont(font);
		label.setText(AntLaunchConfigurationMessages.getString("AntTargetsTab.Check_targets_to_e&xecute__1")); //$NON-NLS-1$
				
		createTargetsTable(comp);
		
		fSelectionCountLabel = new Label(comp, SWT.NONE);
		fSelectionCountLabel.setFont(font);
		fSelectionCountLabel.setText(AntLaunchConfigurationMessages.getString("AntTargetsTab.0_out_of_0_selected_2")); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fSelectionCountLabel.setLayoutData(gd);
		
		// spacer
		label = new Label(comp, SWT.NONE);
		label.setFont(font);
		
		label = new Label(comp, SWT.NONE);
		label.setText(AntLaunchConfigurationMessages.getString("AntTargetsTab.Target_execution_order__3")); //$NON-NLS-1$
		label.setFont(font);
		
		Composite orderComposite = new Composite(comp, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		orderComposite.setLayoutData(gd);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		orderComposite.setLayout(layout);
		orderComposite.setFont(font);
				
		fTargetOrderText = new Text(orderComposite, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
		fTargetOrderText.setFont(font);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 40;
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		fTargetOrderText.setLayoutData(gd);

		fOrderButton = createPushButton(orderComposite, AntLaunchConfigurationMessages.getString("AntTargetsTab.&Order..._4"), null); //$NON-NLS-1$
		gd = (GridData)fOrderButton.getLayoutData();
		gd.verticalAlignment = GridData.BEGINNING;
		fOrderButton.setFont(font);
		fOrderButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleOrderPressed();
			}
		});
	}
	
	private void handleOrderPressed() {
		TargetOrderDialog dialog = new TargetOrderDialog(getShell(), fOrderedTargets.toArray());
		int ok = dialog.open();
		if (ok == Dialog.OK) {
			fOrderedTargets.clear();
			Object[] targets = dialog.getTargets();
			for (int i = 0; i < targets.length; i++) {
				fOrderedTargets.add(targets[i]);
				updateSelectionCount();
				updateLaunchConfigurationDialog();
			}
		}
	}
	
	private void createTargetsTable(Composite parent) {
		Table table= new Table(parent, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.RESIZE);
		
		GridData data= new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		data.widthHint = 250;
		table.setLayoutData(data);
		table.setFont(parent.getFont());
				
		table.setHeaderVisible(true);
		table.setLinesVisible(true);		

		TableLayout tableLayout= new TableLayout();
		ColumnWeightData weightData = new ColumnWeightData(40, true);
		tableLayout.addColumnData(weightData);
		weightData = new ColumnWeightData(60, true);
		tableLayout.addColumnData(weightData);		
		table.setLayout(tableLayout);

		TableColumn column1= new TableColumn(table, SWT.NULL);
		column1.setText(AntLaunchConfigurationMessages.getString("AntTargetsTab.Name_5")); //$NON-NLS-1$
			
		TableColumn column2= new TableColumn(table, SWT.NULL);
		column2.setText(AntLaunchConfigurationMessages.getString("AntTargetsTab.Description_6")); //$NON-NLS-1$
		
		fTableViewer = new CheckboxTableViewer(table);
		fTableViewer.setLabelProvider(new TargetTableLabelProvider());
		fTableViewer.setContentProvider(new AntTargetContentProvider());
		
		fTableViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection= event.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection ss= (IStructuredSelection)selection;
					Object element= ss.getFirstElement();
					boolean checked= !fTableViewer.getChecked(element);
					fTableViewer.setChecked(element, checked);
					updateOrderedTargets(element , checked);
				}
			}
		});
		
		fTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateOrderedTargets(event.getElement(), event.getChecked());
			}
		});
	}
	
	private void updateOrderedTargets(Object element , boolean checked) {
		if (checked) {
			 fOrderedTargets.add(element);
		} else {
			fOrderedTargets.remove(element);
		}	 
		updateSelectionCount();
		updateLaunchConfigurationDialog();	
	}
	
	private void updateSelectionCount() {
		Object[] checked = fTableViewer.getCheckedElements();
		String numSelected = Integer.toString(checked.length);
		int length= 0;
		if (fAllTargets != null) {
			length= fAllTargets.length;
		}
		String total = Integer.toString(length);
		fSelectionCountLabel.setText(MessageFormat.format(AntLaunchConfigurationMessages.getString("AntTargetsTab.{0}_out_of_{1}_selected_7"), new String[]{numSelected, total})); //$NON-NLS-1$
		
		fOrderButton.setEnabled(checked.length > 1);
		
		StringBuffer buffer = new StringBuffer();
		Iterator iter = fOrderedTargets.iterator();
		while (iter.hasNext()) {
			buffer.append(((TargetInfo)iter.next()).getName());
			buffer.append(", "); //$NON-NLS-1$
		}
		if (buffer.length() > 2) {
			// remove trailing comma
			buffer.setLength(buffer.length() - 2);
		}
		fTargetOrderText.setText(buffer.toString());
	}
	
	private TargetInfo[] getTargets() {
		if (fAllTargets == null) {
			setErrorMessage(null);
			MultiStatus status = new MultiStatus(IAntUIConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
			String expandedLocation = ToolUtil.expandFileLocation(fLocation, VariableContextManager.getDefault().getVariableContext(), status);
			if (expandedLocation != null && status.isOK()) {
				try {
					String[] arguments = ExternalToolsUtil.getArguments(fLaunchConfiguration, VariableContextManager.getDefault().getVariableContext());
					fAllTargets = AntUtil.getTargets(expandedLocation, arguments, fLaunchConfiguration);
				} catch (CoreException ce) {
					IStatus exceptionStatus= ce.getStatus();
					IStatus[] children= exceptionStatus.getChildren();
					StringBuffer message= new StringBuffer(ce.getMessage());
					for (int i = 0; i < children.length; i++) {
						message.append(' ');
						IStatus childStatus = children[i];
						message.append(childStatus.getMessage());
					}
					setErrorMessage(message.toString());
					fAllTargets= null;
					return fAllTargets;
				}
				for (int i=0; i < fAllTargets.length; i++) {
					if (fAllTargets[i].isDefault()) {
						fDefaultTarget = fAllTargets[i];
						break;
					}
				}
			}
				
			if (fAllTargets != null) {
				Arrays.sort(fAllTargets, new Comparator() {
					public int compare(Object o1, Object o2) {
						TargetInfo t1= (TargetInfo)o1;
						TargetInfo t2= (TargetInfo)o2;
						return t1.getName().compareToIgnoreCase(t2.getName());
					}
					public boolean equals(Object obj) {
						return false;
					}
				});
			}
		}
		return fAllTargets;
	}
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		fLaunchConfiguration= configuration;
		setErrorMessage(null);
		setMessage(null);
		String configTargets= null;
		String newLocation= null;
		fOrderedTargets = new ArrayList();
		try {
			configTargets= configuration.getAttribute(IAntUIConstants.ATTR_ANT_TARGETS, (String)null);
			newLocation= configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, (String)null);
		} catch (CoreException ce) {
			AntUIPlugin.log(AntLaunchConfigurationMessages.getString("AntTargetsTab.Error_reading_configuration_12"), ce); //$NON-NLS-1$
		}
		
		if (newLocation == null) {
			fAllTargets= null;
			fLocation= newLocation;
			setExecuteInput(new TargetInfo[0]);
			fTableViewer.setInput(new TargetInfo[0]);
			return; 
		}
		
		if (!newLocation.equals(fLocation)) {
			fAllTargets= null;
			fLocation= newLocation;
		}
		
		TargetInfo[] allInfos= getTargets();
		if (allInfos == null) {
			setExecuteInput(new TargetInfo[0]);
			fTableViewer.setInput(new TargetInfo[0]);
			return; 
		}
		
		String[] targetNames= AntUtil.parseRunTargets(configTargets);
		if (targetNames.length == 0) {
			fOrderedTargets.add(fDefaultTarget);
			fTableViewer.setAllChecked(false);
			setExecuteInput(allInfos);
			if (fDefaultTarget != null) {
				fTableViewer.setChecked(fDefaultTarget, true);
			}
			updateSelectionCount();
			return;
		}
		
		setExecuteInput(allInfos);
		fTableViewer.setAllChecked(false);
		for (int i = 0; i < targetNames.length; i++) {
			for (int j = 0; j < fAllTargets.length; j++) {
				if (targetNames[i].equals(fAllTargets[j].getName())) {
					fOrderedTargets.add(fAllTargets[j]);
					fTableViewer.setChecked(fAllTargets[j], true);
				}
			}
		}
		updateSelectionCount();
	}
	
	/**
	 * Sets the execute table's input to the given input.
	 */
	private void setExecuteInput(Object input) {
		fTableViewer.setInput(input);
		updateSelectionCount();
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {		
		if (fOrderedTargets.size() == 1) {
			TargetInfo item = (TargetInfo)fOrderedTargets.get(0);
			if (item.isDefault()) {
				configuration.setAttribute(IAntUIConstants.ATTR_ANT_TARGETS, (String)null);
				return;
			}
		} else if (fOrderedTargets.size() == 0) {
			return;
		}
		
		StringBuffer buff= new StringBuffer();
		Iterator iter = fOrderedTargets.iterator();
		String targets = null;
		while (iter.hasNext()) {
			TargetInfo item = (TargetInfo)iter.next();
			buff.append(item.getName());
			buff.append(',');
		}
		if (buff.length() > 0) {
			targets= buff.toString();
		}  

		configuration.setAttribute(IAntUIConstants.ATTR_ANT_TARGETS, targets);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return AntLaunchConfigurationMessages.getString("AntTargetsTab.Tar&gets_14"); //$NON-NLS-1$
	}
		
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return AntUIImages.getImage(IAntUIConstants.IMG_TAB_ANT_TARGETS);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (fAllTargets == null && getErrorMessage() != null) {
			//error in parsing;
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		
		if (fAllTargets != null && fTableViewer.getCheckedElements().length == 0) {
			setErrorMessage(AntLaunchConfigurationMessages.getString("AntTargetsTab.No_targets")); //$NON-NLS-1$
			return false;
		}
		return super.isValid(launchConfig);
	}
}
