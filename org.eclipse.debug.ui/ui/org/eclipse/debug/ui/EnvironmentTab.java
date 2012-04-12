/*******************************************************************************
 * Copyright (c) 2000, 2012 Keith Seitz and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Keith Seitz (keiths@redhat.com) - initial implementation
 *     IBM Corporation - integration and code cleanup
 *     Jan Opacki (jan.opacki@gmail.com) bug 307139
 *******************************************************************************/
package org.eclipse.debug.ui;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.AbstractDebugCheckboxSelectionDialog;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.MultipleInputDialog;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.internal.ui.launchConfigurations.EnvironmentVariable;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationsMessages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.ibm.icu.text.MessageFormat;

/**
 * Launch configuration tab for configuring the environment passed
 * into Runtime.exec(...) when a config is launched.
 * <p>
 * Clients may call {@link #setHelpContextId(String)} on this tab prior to control
 * creation to alter the default context help associated with this tab. 
 * </p>
 * <p>
 * This class may be instantiated.
 * </p> 
 * @since 3.0
 * @noextend This class is not intended to be sub-classed by clients.
 */
public class EnvironmentTab extends AbstractLaunchConfigurationTab {

	protected TableViewer environmentTable;
	protected String[] envTableColumnHeaders = {
		LaunchConfigurationsMessages.EnvironmentTab_Variable_1, 
		LaunchConfigurationsMessages.EnvironmentTab_Value_2, 
	};
	private static final String NAME_LABEL= LaunchConfigurationsMessages.EnvironmentTab_8; 
	private static final String VALUE_LABEL= LaunchConfigurationsMessages.EnvironmentTab_9; 
	protected static final String P_VARIABLE = "variable"; //$NON-NLS-1$
	protected static final String P_VALUE = "value"; //$NON-NLS-1$
	protected Button envAddButton;
	protected Button envEditButton;
	protected Button envRemoveButton;
	protected Button appendEnvironment;
	protected Button replaceEnvironment;
	protected Button envSelectButton;
	
	/**
	 * Content provider for the environment table
	 */
	protected class EnvironmentVariableContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			EnvironmentVariable[] elements = new EnvironmentVariable[0];
			ILaunchConfiguration config = (ILaunchConfiguration) inputElement;
			Map m;
			try {
				m = config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
			} catch (CoreException e) {
				DebugUIPlugin.log(new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "Error reading configuration", e)); //$NON-NLS-1$
				return elements;
			}
			if (m != null && !m.isEmpty()) {
				elements = new EnvironmentVariable[m.size()];
				String[] varNames = new String[m.size()];
				m.keySet().toArray(varNames);
				for (int i = 0; i < m.size(); i++) {
					elements[i] = new EnvironmentVariable(varNames[i], (String) m.get(varNames[i]));
				}
			}
			return elements;
		}
		public void dispose() {
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput == null){
				return;
			}
			if (viewer instanceof TableViewer){
				TableViewer tableViewer= (TableViewer) viewer;
				if (tableViewer.getTable().isDisposed()) {
					return;
				}
				tableViewer.setComparator(new ViewerComparator() {
					public int compare(Viewer iviewer, Object e1, Object e2) {
						if (e1 == null) {
							return -1;
						} else if (e2 == null) {
							return 1;
						} else {
							return ((EnvironmentVariable)e1).getName().compareToIgnoreCase(((EnvironmentVariable)e2).getName());
						}
					}
				});
			}
		}
	}
	
	/**
	 * Label provider for the environment table
	 */
	public class EnvironmentVariableLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) 	{
			String result = null;
			if (element != null) {
				EnvironmentVariable var = (EnvironmentVariable) element;
				switch (columnIndex) {
					case 0: // variable
						result = var.getName();
						break;
					case 1: // value
						result = var.getValue();
						break;
				}
			}
			return result;
		}
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return DebugPluginImages.getImage(IDebugUIConstants.IMG_OBJS_ENV_VAR);
			}
			return null;
		}
	}

	/**
	 * Constructs a new tab with default context help.
	 */
	public EnvironmentTab() {
		setHelpContextId(IDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_ENVIRONMENT_TAB);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		// Create main composite
		Composite mainComposite = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_HORIZONTAL);
		setControl(mainComposite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), getHelpContextId());
		
		createEnvironmentTable(mainComposite);
		createTableButtons(mainComposite);
		createAppendReplace(mainComposite);
		
		Dialog.applyDialogFont(mainComposite);
	}

	/**
	 * Creates and configures the widgets which allow the user to
	 * choose whether the specified environment should be appended
	 * to the native environment or if it should completely replace it.
	 * @param parent the composite in which the widgets should be created
	 */
	protected void createAppendReplace(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, 1, 2, GridData.FILL_HORIZONTAL);
		appendEnvironment= createRadioButton(comp, LaunchConfigurationsMessages.EnvironmentTab_16); 
		appendEnvironment.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		replaceEnvironment= createRadioButton(comp, LaunchConfigurationsMessages.EnvironmentTab_17); 
	}
	
	/**
	 * Updates the enablement of the append/replace widgets. The
	 * widgets should disable when there are no environment variables specified.
	 */
	protected void updateAppendReplace() {
		boolean enable= environmentTable.getTable().getItemCount() > 0;
		appendEnvironment.setEnabled(enable);
		replaceEnvironment.setEnabled(enable);
	}
	
	/**
	 * Creates and configures the table that displayed the key/value
	 * pairs that comprise the environment.
	 * @param parent the composite in which the table should be created
	 */
	protected void createEnvironmentTable(Composite parent) {
		Font font = parent.getFont();
		// Create label, add it to the parent to align the right side buttons with the top of the table
		SWTFactory.createLabel(parent, LaunchConfigurationsMessages.EnvironmentTab_Environment_variables_to_set__3, 2);
		// Create table composite
		Composite tableComposite = SWTFactory.createComposite(parent, font, 1, 1, GridData.FILL_BOTH, 0, 0);
		// Create table
		environmentTable = new TableViewer(tableComposite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		Table table = environmentTable.getTable();
		table.setLayout(new GridLayout());
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setFont(font);
		environmentTable.setContentProvider(new EnvironmentVariableContentProvider());
		environmentTable.setLabelProvider(new EnvironmentVariableLabelProvider());
		environmentTable.setColumnProperties(new String[] {P_VARIABLE, P_VALUE});
		environmentTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleTableSelectionChanged(event);
			}
		});
		environmentTable.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!environmentTable.getSelection().isEmpty()) {
					handleEnvEditButtonSelected();
				}
			}
		});
		// Create columns
		final TableColumn tc1 = new TableColumn(table, SWT.NONE, 0);
		tc1.setText(envTableColumnHeaders[0]);
		final TableColumn tc2 = new TableColumn(table, SWT.NONE, 1);
		tc2.setText(envTableColumnHeaders[1]);
		final Table tref = table;
		final Composite comp = tableComposite;
		tableComposite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Rectangle area = comp.getClientArea();
				Point size = tref.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				ScrollBar vBar = tref.getVerticalBar();
				int width = area.width - tref.computeTrim(0,0,0,0).width - 2;
				if (size.y > area.height + tref.getHeaderHeight()) {
					Point vBarSize = vBar.getSize();
					width -= vBarSize.x;
				}
				Point oldSize = tref.getSize();
				if (oldSize.x > area.width) {
					tc1.setWidth(width/2-1);
					tc2.setWidth(width - tc1.getWidth());
					tref.setSize(area.width, area.height);
				} else {
					tref.setSize(area.width, area.height);
					tc1.setWidth(width/2-1);
					tc2.setWidth(width - tc1.getWidth());
				}
			}
		});
	}
	
	/**
	 * Responds to a selection changed event in the environment table
	 * @param event the selection change event
	 */
	protected void handleTableSelectionChanged(SelectionChangedEvent event) {
		int size = ((IStructuredSelection)event.getSelection()).size();
		envEditButton.setEnabled(size == 1);
		envRemoveButton.setEnabled(size > 0);
	}
	
	/**
	 * Creates the add/edit/remove buttons for the environment table
	 * @param parent the composite in which the buttons should be created
	 */
	protected void createTableButtons(Composite parent) {
		// Create button composite
		Composite buttonComposite = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END, 0, 0);

		// Create buttons
		envAddButton = createPushButton(buttonComposite, LaunchConfigurationsMessages.EnvironmentTab_New_4, null); 
		envAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleEnvAddButtonSelected();
			}
		});
		envSelectButton = createPushButton(buttonComposite, LaunchConfigurationsMessages.EnvironmentTab_18, null); 
		envSelectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleEnvSelectButtonSelected();
			}
		});
		envEditButton = createPushButton(buttonComposite, LaunchConfigurationsMessages.EnvironmentTab_Edit_5, null); 
		envEditButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleEnvEditButtonSelected();
			}
		});
		envEditButton.setEnabled(false);
		envRemoveButton = createPushButton(buttonComposite, LaunchConfigurationsMessages.EnvironmentTab_Remove_6, null); 
		envRemoveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleEnvRemoveButtonSelected();
			}
		});
		envRemoveButton.setEnabled(false);
	}
	
	/**
	 * Adds a new environment variable to the table.
	 */
	protected void handleEnvAddButtonSelected() {
		MultipleInputDialog dialog = new MultipleInputDialog(getShell(), LaunchConfigurationsMessages.EnvironmentTab_22); 
		dialog.addTextField(NAME_LABEL, null, false);
		dialog.addVariablesField(VALUE_LABEL, null, true);
		
		if (dialog.open() != Window.OK) {
			return;
		}
		
		String name = dialog.getStringValue(NAME_LABEL);
		String value = dialog.getStringValue(VALUE_LABEL);
		
		if (name != null && value != null && name.length() > 0 && value.length() >0) {
			addVariable(new EnvironmentVariable(name.trim(), value.trim()));
			updateAppendReplace();
		}
	}
	
	/**
	 * Attempts to add the given variable. Returns whether the variable
	 * was added or not (as when the user answers not to overwrite an
	 * existing variable).
	 * @param variable the variable to add
	 * @return whether the variable was added
	 */
	protected boolean addVariable(EnvironmentVariable variable) {
		String name= variable.getName();
		TableItem[] items = environmentTable.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			EnvironmentVariable existingVariable = (EnvironmentVariable) items[i].getData();
			if (existingVariable.getName().equals(name)) {
				boolean overWrite= MessageDialog.openQuestion(getShell(), LaunchConfigurationsMessages.EnvironmentTab_12, MessageFormat.format(LaunchConfigurationsMessages.EnvironmentTab_13, new String[] {name})); // 
				if (!overWrite) {
					return false;
				}
				environmentTable.remove(existingVariable);
				break;
			}
		}
		environmentTable.add(variable);
		updateLaunchConfigurationDialog();
		return true;
	}
	
	/**
	 * Displays a dialog that allows user to select native environment variables 
	 * to add to the table.
	 */
	private void handleEnvSelectButtonSelected() {
		//get Environment Variables from the OS
		Map envVariables = getNativeEnvironment();
		
		//get Environment Variables from the table
		TableItem[] items = environmentTable.getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			EnvironmentVariable var = (EnvironmentVariable) items[i].getData();
			envVariables.remove(var.getName());
		}
		
		NativeEnvironmentSelectionDialog dialog = new NativeEnvironmentSelectionDialog(getShell(), envVariables); 
		dialog.setTitle(LaunchConfigurationsMessages.EnvironmentTab_20); 
		
		int button = dialog.open();
		if (button == Window.OK) {
			Object[] selected = dialog.getResult();		
			for (int i = 0; i < selected.length; i++) {
				environmentTable.add(selected[i]);				
			}
		}
		
		updateAppendReplace();
		updateLaunchConfigurationDialog();
	}

	/**
	 * Gets native environment variable from the LaunchManager. Creates EnvironmentVariable objects.
	 * @return Map of name - EnvironmentVariable pairs based on native environment.
	 */
	private Map getNativeEnvironment() {
		Map stringVars = DebugPlugin.getDefault().getLaunchManager().getNativeEnvironmentCasePreserved();
		HashMap vars = new HashMap();
		for (Iterator i = stringVars.keySet().iterator(); i.hasNext(); ) {
			String key = (String) i.next();
			String value = (String) stringVars.get(key);
			vars.put(key, new EnvironmentVariable(key, value));
		}
		return vars;
	}

	/**
	 * Creates an editor for the value of the selected environment variable.
	 */
	private void handleEnvEditButtonSelected() {
		IStructuredSelection sel= (IStructuredSelection) environmentTable.getSelection();
		EnvironmentVariable var= (EnvironmentVariable) sel.getFirstElement();
		if (var == null) {
			return;
		}
		String originalName= var.getName();
		String value= var.getValue();
		MultipleInputDialog dialog= new MultipleInputDialog(getShell(), LaunchConfigurationsMessages.EnvironmentTab_11); 
		dialog.addTextField(NAME_LABEL, originalName, false);
		if(value != null && value.indexOf(System.getProperty("line.separator")) > -1) { //$NON-NLS-1$
			dialog.addMultilinedVariablesField(VALUE_LABEL, value, true);
		}
		else {
			dialog.addVariablesField(VALUE_LABEL, value, true);
		}
		
		if (dialog.open() != Window.OK) {
			return;
		}
		String name= dialog.getStringValue(NAME_LABEL);
		value= dialog.getStringValue(VALUE_LABEL);
		if (!originalName.equals(name)) {
			if (addVariable(new EnvironmentVariable(name, value))) {
				environmentTable.remove(var);
			}
		} else {
			var.setValue(value);
			environmentTable.update(var, null);
			updateLaunchConfigurationDialog();
		}
	}

	/**
	 * Removes the selected environment variable from the table.
	 */
	private void handleEnvRemoveButtonSelected() {
		IStructuredSelection sel = (IStructuredSelection) environmentTable.getSelection();
		environmentTable.getControl().setRedraw(false);
		for (Iterator i = sel.iterator(); i.hasNext(); ) {
			EnvironmentVariable var = (EnvironmentVariable) i.next();	
			environmentTable.remove(var);
		}
		environmentTable.getControl().setRedraw(true);
		updateAppendReplace();
		updateLaunchConfigurationDialog();
	}

	/**
	 * Updates the environment table for the given launch configuration
	 * @param configuration the configuration to use as input for the backing table
	 */
	protected void updateEnvironment(ILaunchConfiguration configuration) {
		environmentTable.setInput(configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.removeAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		boolean append= true;
		try {
			append = configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
		} catch (CoreException e) {
			DebugUIPlugin.log(e.getStatus());
		}
		if (append) {
			appendEnvironment.setSelection(true);
	        replaceEnvironment.setSelection(false);
		} else {
			replaceEnvironment.setSelection(true);
			appendEnvironment.setSelection(false);
		}
		updateEnvironment(configuration);
		updateAppendReplace();
	}
	
	/**
	 * Stores the environment in the given configuration
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {	
		// Convert the table's items into a Map so that this can be saved in the
		// configuration's attributes.
		TableItem[] items = environmentTable.getTable().getItems();
		Map map = new HashMap(items.length);
		for (int i = 0; i < items.length; i++)
		{
			EnvironmentVariable var = (EnvironmentVariable) items[i].getData();
			map.put(var.getName(), var.getValue());
		} 
		if (map.size() == 0) {
			configuration.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
		} else {
			configuration.setAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, map);
		}
		
		if(appendEnvironment.getSelection()) {
			ILaunchConfiguration orig = configuration.getOriginal();
			boolean hasTrueValue = false;
			if(orig != null) {
				try {
					hasTrueValue = orig.hasAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES) &&
						orig.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
				} catch (CoreException e) {
					DebugUIPlugin.log(e.getStatus());
				}
			}
			if (hasTrueValue) {
				configuration.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
			} else {
				configuration.removeAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES);
			}
		} else {
			configuration.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, false);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LaunchConfigurationsMessages.EnvironmentTab_Environment_7; 
	}
	
	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 * 
	 * @since 3.3
	 */
	public String getId() {
		return "org.eclipse.debug.ui.environmentTab"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return DebugPluginImages.getImage(IDebugUIConstants.IMG_OBJS_ENVIRONMENT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing when activated
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#deactivated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void deactivated(ILaunchConfigurationWorkingCopy workingCopy) {
		// do nothing when deactivated
	}
	
	/**
	 * This dialog allows users to select one or more known native environment variables from a list.
	 */
	private class NativeEnvironmentSelectionDialog extends AbstractDebugCheckboxSelectionDialog {
		
		private Object fInput;
		
		public NativeEnvironmentSelectionDialog(Shell parentShell, Object input) {
			super(parentShell);
			fInput = input;
			setShellStyle(getShellStyle() | SWT.RESIZE);
			setShowSelectAllButtons(true);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getDialogSettingsId()
		 */
		protected String getDialogSettingsId() {
			return IDebugUIConstants.PLUGIN_ID + ".ENVIRONMENT_TAB.NATIVE_ENVIROMENT_DIALOG"; //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getHelpContextId()
		 */
		protected String getHelpContextId() {
			return IDebugHelpContextIds.SELECT_NATIVE_ENVIRONMENT_DIALOG;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerInput()
		 */
		protected Object getViewerInput() {
			return fInput;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerLabel()
		 */
		protected String getViewerLabel() {
			return LaunchConfigurationsMessages.EnvironmentTab_19;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getLabelProvider()
		 */
		protected IBaseLabelProvider getLabelProvider() {
			return new ILabelProvider() {
				public Image getImage(Object element) {
					return DebugPluginImages.getImage(IDebugUIConstants.IMG_OBJS_ENVIRONMENT);
				}
				public String getText(Object element) {
					EnvironmentVariable var = (EnvironmentVariable) element;
					return MessageFormat.format(LaunchConfigurationsMessages.EnvironmentTab_7, new String[] {var.getName(), var.getValue()}); 
				}
				public void addListener(ILabelProviderListener listener) {
				}
				public void dispose() {
				}
				public boolean isLabelProperty(Object element, String property) {
					return false;
				}
				public void removeListener(ILabelProviderListener listener) {
				}				
			};
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getContentProvider()
		 */
		protected IContentProvider getContentProvider() {
			return new IStructuredContentProvider() {
				public Object[] getElements(Object inputElement) {
					EnvironmentVariable[] elements = null;
					if (inputElement instanceof HashMap) {
						Comparator comparator = new Comparator() {
							public int compare(Object o1, Object o2) {
								String s1 = (String)o1;
								String s2 = (String)o2;
								return s1.compareTo(s2);
							}
						};
						TreeMap envVars = new TreeMap(comparator);
						envVars.putAll((Map)inputElement);
						elements = new EnvironmentVariable[envVars.size()];
						int index = 0;
						for (Iterator iterator = envVars.keySet().iterator(); iterator.hasNext(); index++) {
							Object key = iterator.next();
							elements[index] = (EnvironmentVariable) envVars.get(key);
						}
					}
					return elements;
				}
				public void dispose() {	
				}
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}
			};
		}
	}
}
