package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.ant.core.TargetInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.externaltools.internal.ant.model.AntUtil;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.internal.model.ToolUtil;
import org.eclipse.ui.externaltools.internal.variable.ExpandVariableContext;

public class AntTargetsTab extends AbstractLaunchConfigurationTab {

	private TabFolder tabFolder;
	
	private TabItem executeTabItem;
	private TabItem orderTabItem;
	
	private Button upButton;
	private Button downButton;
	
	private Label descriptionLabel;
	
	private CheckboxTableViewer executeTargetsTable;
	private TableViewer orderTargetsTable;
	
	private TargetInfo defaultTarget = null;
	private TargetInfo[] allTargets= null;
	private Text descriptionField;
	
	private String location= null;
	
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		//TODO: set help context ID
		//WorkbenchHelp.setHelp(getControl(), IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_CLASSPATH_TAB); 
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 1;
		comp.setLayout(topLayout);		

		tabFolder = new TabFolder(comp, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(gridData);
		tabFolder.setFont(font);
		
		createExecuteTargetsList(tabFolder);
		
		createOrderTargetsList(tabFolder);

		createDescriptionField(comp);
	}
	
	/**
	 * Creates a button bank containing the buttons for moving
	 * targets in the order list.
	 */
	private void createOrderButtonComposite(Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);

		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		buttonComposite.setLayoutData(gridData);
		
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttonComposite.setLayout(layout);
		buttonComposite.setFont(parent.getFont());
		
		upButton = createPushButton(buttonComposite, AntLaunchConfigurationMessages.getString("AntTargetsTab.U&p_3"), null); //$NON-NLS-1$
		upButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleMoveUp();
					updateLaunchConfigurationDialog();
				}
			});
				
		downButton = createPushButton(buttonComposite, AntLaunchConfigurationMessages.getString("AntTargetsTab.&Down_4"), null); //$NON-NLS-1$
		downButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					handleMoveDown();
					updateLaunchConfigurationDialog();
				}
			});
	}
	
	/**
	 * Creates a button bank containing the buttons for the 
	 * execute targets tab
	 */
	private void createExecuteButtonComposite(Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);

		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
		buttonComposite.setLayoutData(gridData);
	
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		buttonComposite.setLayout(layout);
		buttonComposite.setFont(parent.getFont());
	
		Button selectAllButton = createPushButton(buttonComposite, AntLaunchConfigurationMessages.getString("AntTargetsTab.&Select_All_1"), null); //$NON-NLS-1$
		selectAllButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					executeTargetsTable.setAllChecked(true);
					orderTargetsTable.setInput(executeTargetsTable.getCheckedElements());
					updateLaunchConfigurationDialog();
				}
			});
	
		Button deselectAllButton = createPushButton(buttonComposite, AntLaunchConfigurationMessages.getString("AntTargetsTab.&Deselect_All_2"), null); //$NON-NLS-1$
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					executeTargetsTable.setAllChecked(false);
					orderTargetsTable.setInput(new TargetInfo[0]);
					updateLaunchConfigurationDialog();
				}
			});	
			
		Button addButton = createPushButton(buttonComposite, AntLaunchConfigurationMessages.getString("AntTargetsTab.&Add_Duplicates..._3"), null); //$NON-NLS-1$
		addButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					addTargets();
					updateLaunchConfigurationDialog();
				}
			});
	}
	
	private TargetInfo[] getTargets() {
		if (allTargets == null) {
			setErrorMessage(null);
			MultiStatus status = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
			String expandedLocation = ToolUtil.expandFileLocation(location, ExpandVariableContext.EMPTY_CONTEXT, status);
			if (expandedLocation != null && status.isOK()) {
				try {
					allTargets = AntUtil.getTargets(expandedLocation);
				} catch (CoreException ce) {
					setErrorMessage(ce.getMessage());
					allTargets= null;
					return allTargets;
				}
				for (int i=0; i < allTargets.length; i++) {
					if (allTargets[i].isDefault()) {
						defaultTarget = allTargets[i];
						break;
					}
				}
			}
				
			if (allTargets != null) {
				Arrays.sort(allTargets, new Comparator() {
					public int compare(Object o1, Object o2) {
						TargetInfo t1= (TargetInfo)o1;
						TargetInfo t2= (TargetInfo)o2;
						return t1.getName().compareTo(t2.getName());
					}
					public boolean equals(Object obj) {
						return false;
					}
				});
			}
		}
		return allTargets;
	}
				
	private void addTargets() {
		
		TargetInfo[] targets= getTargets();
		AntTargetLabelProvider labelProvider= new AntTargetLabelProvider(null);			
		SelectionDialog dialog= new ListSelectionDialog(getShell(), targets, new AntTargetContentProvider(), labelProvider, AntLaunchConfigurationMessages.getString("AntTargetsTab.Select_&Ant_Targets__6")); //$NON-NLS-1$
		
		if (dialog.open() == SelectionDialog.OK) {
			AntTargetContentProvider contentProvider= (AntTargetContentProvider)executeTargetsTable.getContentProvider();
			contentProvider.addAll(Arrays.asList(dialog.getResult()));
		}
		updateItemColoring(executeTargetsTable);
	}
	
	/**
	 * Moves the selected targets up in the list of active targets
	 */
	private void handleMoveUp() {
		int indices[] = orderTargetsTable.getTable().getSelectionIndices();
		if (indices.length == 0) {
			return;
		}
		int newIndices[] = new int[indices.length];
		if (indices[0] == 0) {
			// Only perform the move if the items have somewhere to move to
			return;
		}
		AntTargetContentProvider contentProvider= (AntTargetContentProvider)orderTargetsTable.getContentProvider();
		for (int i = 0; i < newIndices.length; i++) {
			int index = indices[i];
			contentProvider.moveUpTarget(index);
			newIndices[i] = index - 1;
		}
		orderTargetsTable.refresh();
		// TODO: Remove the call to deselectAll() once Bug 30745 is fixed
		orderTargetsTable.getTable().deselectAll();
		orderTargetsTable.getTable().select(newIndices);
		updateButtonEnablement();
		updateItemColoring(orderTargetsTable);
	}
	
	/**
	 * Moves the selected targets down in the list of active targets
	 */
	private void handleMoveDown() {
		int indices[] = orderTargetsTable.getTable().getSelectionIndices();
		if (indices.length == 0) {
			return;
		}
		int newIndices[] = new int[indices.length];
		if (indices[indices.length - 1] == orderTargetsTable.getTable().getItemCount() - 1) {
			// Only perform the move if the items have somewhere to move to
			return;
		}
		AntTargetContentProvider contentProvider= (AntTargetContentProvider)orderTargetsTable.getContentProvider();
		for (int i= indices.length - 1; i >= 0; i--) {
			int index = indices[i];
			contentProvider.moveDownTarget(index);
			newIndices[i] = index + 1;
		}
		orderTargetsTable.refresh();
		// TODO: Remove the call to deselectAll() once Bug 30745 is fixed
		orderTargetsTable.getTable().deselectAll();
		orderTargetsTable.getTable().select(newIndices);
		updateButtonEnablement();
		updateItemColoring(orderTargetsTable);
	}

	/*
	 * Creates the list of targets that will be used when the tool is run.
	 */
	private void createExecuteTargetsList(TabFolder folder) {
		Font font = folder.getFont();
		
		Composite comp = new Composite(folder, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		comp.setLayout(layout);
		
		executeTabItem = new TabItem(folder, SWT.NONE, 0);
		executeTabItem.setText(AntLaunchConfigurationMessages.getString("AntTargetsTab.&Execute_4")); //$NON-NLS-1$
		executeTabItem.setControl(comp);
						
		executeTargetsTable = CheckboxTableViewer.newCheckList(comp, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		executeTargetsTable.setContentProvider(new AntTargetContentProvider());
		AntTargetLabelProvider labelProvider= new AntTargetLabelProvider(executeTargetsTable);
		executeTargetsTable.setLabelProvider(labelProvider);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		executeTargetsTable.getControl().setLayoutData(gridData);
		executeTargetsTable.getTable().setFont(font);
		executeTargetsTable.setSorter(new ViewerSorter() {
			/**
			 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer viewer, Object e1, Object e2) {
				return e1.toString().compareToIgnoreCase(e2.toString());
			}
		});
		
		executeTabItem.setData(executeTargetsTable);
				
		executeTargetsTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)executeTargetsTable.getSelection();
				if (selection.size() == 1) {
					targetsSelected((TargetInfo)selection.getFirstElement());
				} else {
					targetsSelected(null);
				}
			}
		});
		
		executeTargetsTable.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateOrderTargetsTable(event.getChecked(), event.getElement());
			}
		});
		
		executeTargetsTable.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				int index= executeTargetsTable.getTable().getSelectionIndex();
				TableItem item= executeTargetsTable.getTable().getItem(index);
				item.setChecked(!item.getChecked());
				updateOrderTargetsTable(item.getChecked(), item.getData());
			}
		});
		
		createExecuteButtonComposite(comp);
	}
	
	private void updateOrderTargetsTable(boolean isChecked, Object targetInfo) {
		if (isChecked) {
			((AntTargetContentProvider)orderTargetsTable.getContentProvider()).add(targetInfo);
		} else {
			((AntTargetContentProvider)orderTargetsTable.getContentProvider()).removeTarget(targetInfo);
		}
		updateItemColoring(orderTargetsTable);
		updateLaunchConfigurationDialog();
	}
	
	/**
	 * Creates the list of targets that can be used to order the execution of
	 * the targets.
	 */
	private void createOrderTargetsList(TabFolder folder) {
		Font font = folder.getFont();
		
		Composite comp = new Composite(folder, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		comp.setLayout(layout);
	
		orderTabItem = new TabItem(folder, SWT.NONE, 1);
		orderTabItem.setText(AntLaunchConfigurationMessages.getString("AntTargetsTab.&Order_5")); //$NON-NLS-1$
		orderTabItem.setControl(comp);
					
		orderTargetsTable = new TableViewer(comp, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		orderTargetsTable.setContentProvider(new AntTargetContentProvider());
		AntTargetLabelProvider labelProvider= new AntTargetLabelProvider(orderTargetsTable);
	
		orderTargetsTable.setLabelProvider(labelProvider);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		orderTargetsTable.getControl().setLayoutData(gridData);
		orderTargetsTable.getTable().setFont(font);
		orderTabItem.setData(orderTargetsTable);
		
		orderTargetsTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)orderTargetsTable.getSelection();
				if (selection.size() == 1) {
					targetsSelected((TargetInfo)selection.getFirstElement());
				} else {
					targetsSelected(null);
				}
				updateButtonEnablement();
			}
		});
		
		createOrderButtonComposite(comp);
	}
	
	/**
	 * A target was selected; update the description field.
	 */
	private void targetsSelected(TargetInfo target) {
		String description= ""; //$NON-NLS-1$
		if (target != null) {
			description =target.getDescription();
			if (description == null) {
				description = ""; //$NON-NLS-1$
			}
		}
		descriptionField.setText(description);
	}
	
	/**
	 * Creates the text field which displays the description of the selected
	 * target.
	 */
	private void createDescriptionField(Composite parent) {
		Font font = parent.getFont();
		
		descriptionLabel = new Label(parent, SWT.NONE);
		descriptionLabel.setFont(font);
		descriptionLabel.setText(AntLaunchConfigurationMessages.getString("AntTargetsTab.Target_description__10")); //$NON-NLS-1$
		
		descriptionField = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL | SWT.READ_ONLY);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = 40;
		descriptionField.setLayoutData(data);
		descriptionField.setFont(font);
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
		setErrorMessage(null);
		setMessage(null);
		String configTargets= null;
		String newLocation= null;
		try {
			configTargets= configuration.getAttribute(IExternalToolConstants.ATTR_ANT_TARGETS, (String)null);
			newLocation= configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, (String)null);
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log(AntLaunchConfigurationMessages.getString("AntTargetsTab.Error_reading_configuration_12"), ce); //$NON-NLS-1$
		}
		
		if (newLocation == null) {
			allTargets= null;
			location= newLocation;
			setExecuteInput(new TargetInfo[0]);
			orderTargetsTable.setInput(new TargetInfo[0]);
			return; 
		}
		
		if (!newLocation.equals(location)) {
			allTargets= null;
			location= newLocation;
		}
		
		TargetInfo[] allInfos= getTargets();
		if (allInfos == null) {
			setExecuteInput(new TargetInfo[0]);
			orderTargetsTable.setInput(new TargetInfo[0]);
			return; 
		}
		String[] targetNames= AntUtil.parseRunTargets(configTargets);
		if (targetNames.length == 0) {
			executeTargetsTable.setAllChecked(false);
			setExecuteInput(allInfos);
			if (defaultTarget != null) {
				executeTargetsTable.setChecked(defaultTarget, true);
				orderTargetsTable.setInput(new TargetInfo[0]);
				((AntTargetContentProvider)orderTargetsTable.getContentProvider()).add(defaultTarget);
				updateItemColoring(orderTargetsTable);
			}
			return;
		}
		
		TargetInfo[] targetInfos= new TargetInfo[targetNames.length];
		TargetInfo[] allWithDuplicates= initializeTargetInfos(allInfos, targetNames, targetInfos);
		
		setExecuteInput(allWithDuplicates);
		executeTargetsTable.setAllChecked(false);
		TableItem[] items= executeTargetsTable.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			TableItem item= items[i];
			for (int j = 0; j < targetInfos.length; j++) {
				TargetInfo info = targetInfos[j];
				if (info != null && info.equals(item.getData())) {
					item.setChecked(true);
				}
			}
		}
		orderTargetsTable.setInput(new TargetInfo[0]);
		for (int j = 0; j < targetInfos.length; j++) {
			TargetInfo info = targetInfos[j];
			if (info != null) {
				((AntTargetContentProvider)orderTargetsTable.getContentProvider()).add(info);
			}
		}
		updateItemColoring(orderTargetsTable);
	}
	
	/**
	 * Sets the execute table's input to the given input.
	 */
	private void setExecuteInput(Object input) {
		executeTargetsTable.setInput(input);
		updateItemColoring(executeTargetsTable);
	}
	
	private TargetInfo[] initializeTargetInfos(TargetInfo[] allInfos, String[] targetNames, TargetInfo[] targetInfos) {
		List targetInfosWithDuplicates= new ArrayList(allInfos.length *2);
		TargetInfo info;
		for (int j = 0; j < targetNames.length; j++) {
			String name = targetNames[j];
			for (int i = 0; i < allInfos.length; i++) {
				info = allInfos[i];
				if (info.getName().equals(name)) {
					targetInfosWithDuplicates.add(info);
					targetInfos[j]= info;
					break;
				}
			}
		}
		
		for (int i = 0; i < allInfos.length; i++) {
			info = allInfos[i];
			if (!targetInfosWithDuplicates.contains(info)) {
				targetInfosWithDuplicates.add(info);				
			}
		}
		return (TargetInfo[])targetInfosWithDuplicates.toArray(new TargetInfo[targetInfosWithDuplicates.size()]);
		
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String targets= null;
		AntTargetContentProvider orderContentProvider= (AntTargetContentProvider)orderTargetsTable.getContentProvider();
		Object[] items= orderContentProvider.getElements(null);
		if (items.length == 0) {
			//the user never went to the order tab
			items= executeTargetsTable.getCheckedElements();
		} 
		
		if (items.length == 1) {
			TargetInfo item = (TargetInfo)items[0];
			if (item.isDefault()) {
				configuration.setAttribute(IExternalToolConstants.ATTR_ANT_TARGETS, (String)null);
				return;
			}
		} else if (items.length == 0) {
			return;
		}
		
		StringBuffer buff= new StringBuffer();
		for (int i = 0; i < items.length; i++) {
			TargetInfo item = (TargetInfo)items[i];
			buff.append(item.getName());
			buff.append(',');
		}
		if (buff.length() > 0) {
			targets= buff.toString();
		}  

		configuration.setAttribute(IExternalToolConstants.ATTR_ANT_TARGETS, targets);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return AntLaunchConfigurationMessages.getString("AntTargetsTab.Tar&gets_14"); //$NON-NLS-1$
	}
	
	/**
	 * Updates the enabled state of the upButton and downButton buttons based
	 * on the current list selection.
	 */
	private void updateButtonEnablement() {
		// Disable upButton and downButton buttons if there is not one
		// target selected in the order list.
		if (orderTargetsTable.getTable().getSelectionCount() == 0) {
			upButton.setEnabled(false);
			downButton.setEnabled(false);
			return;	
		}
		
		int indices[]= orderTargetsTable.getTable().getSelectionIndices();
		upButton.setEnabled(indices[0] != 0);
		downButton.setEnabled(indices[indices.length - 1] != orderTargetsTable.getTable().getItemCount() - 1);
	}

	private void updateItemColoring(TableViewer viewer) {
		if (viewer != null &&  !viewer.getTable().isDisposed()) {
			TableItem[] items = viewer.getTable().getItems();
			for (int i = 0; i < items.length; i++) {
				TableItem item = items[i];
				TargetInfo info = (TargetInfo) item.getData();
				if (info.isDefault()) {
					item.setForeground(viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_BLUE));
				} else {
					item.setForeground(null);
				}
			}
		}
	}
		
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return ExternalToolsImages.getImage(IExternalToolConstants.IMG_TAB_ANT_TARGETS);
	}
	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#canSave()
	 */
	public boolean canSave() {
		return isValid(null);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (allTargets == null && getErrorMessage() != null) {
			//error in parsing;
			return false;
		}
		setErrorMessage(null);
		setMessage(null);
		
		if (allTargets != null && executeTargetsTable.getCheckedElements().length == 0) {
			setErrorMessage(AntLaunchConfigurationMessages.getString("AntTargetsTab.No_targets")); //$NON-NLS-1$
			return false;
		}
		return super.isValid(launchConfig);
	}
}
