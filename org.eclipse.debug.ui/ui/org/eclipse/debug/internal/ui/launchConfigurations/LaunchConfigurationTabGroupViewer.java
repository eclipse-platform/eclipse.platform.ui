/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;


import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * A viewer that displays tabs for a launch configuration, with apply and revert
 * buttons.
 */
public class LaunchConfigurationTabGroupViewer extends Viewer {
	
	/**
	 * Containing launch dialog
	 */
	private ILaunchConfigurationDialog fDialog;
	
	/**
	 * The this viewer's input
	 */
	private Object fInput;
	
	/**
	 * The launch configuration (original) being edited
	 */
	private ILaunchConfiguration fOriginal;
	
	/**
	 * The working copy of the original
	 */
	private ILaunchConfigurationWorkingCopy fWorkingCopy;
	
	/**
	 * This view's control, which contains a composite area of controls
	 */
	private Composite fViewerControl;
	
	/**
	 * The composite which is hidden/displayed as tabs are required.
	 */
	private Composite fVisibleArea;
	
	/**
	 * Name label widget
	 */
	private Label fNameLabel;
	
	/**
	 * Name text widget
	 */
	private Text fNameWidget;
	
	/**
	 * Composite containing the launch config tab widgets
	 */
	private Composite fTabComposite;
	
	/**
	 * Tab folder
	 */
	private TabFolder fTabFolder;
	
	/**
	 * The current tab group being displayed
	 */
	private ILaunchConfigurationTabGroup fTabGroup;

	/**
	 * The type of config tabs are currently displayed
	 * for
	 */
	private ILaunchConfigurationType fTabType;	
	
	/**
	 * Index of the active tab
	 */
	private int fCurrentTabIndex = -1;
	
	/**
	 * Apply & Revert buttons
	 */
	private Button fApplyButton;
	private Button fRevertButton;
	
	/**
	 * Whether tabs are currently being disposed or initialized
	 */
	private boolean fDisposingTabs = false;
	private boolean fInitializingTabs = false;

	/**
	 * Controls when the redraw flag is set on the visible area
	 */
	private boolean fRedraw = true;

	/**
	 * The description of the currently selected launch configuration or
	 * launch configuration type or <code>null</code> if none.
	 */
	private String fDescription = null;
	
	/**
	 * A placeholder for switching between the tabs for a config and the getting started tab
	 * @since 3.2
	 */
	private Composite fTabPlaceHolder = null;
	
	/**
	 * A new composite replacing the perspectives tab
	 * @since 3.2
	 */
	private Composite fGettingStarted = null;

	/**
	 * Constructs a viewer in the given composite, contained by the given
	 * launch configuration dialog.
	 * 
	 * @param parent composite containing this viewer
	 * @param dialog containing launch configuration dialog
	 */
	public LaunchConfigurationTabGroupViewer(Composite parent, ILaunchConfigurationDialog dialog) {
		super();
		fDialog = dialog;
		createControl(parent);
	}
	
	/**
	 * Cleanup
	 */
	public void dispose() {
		disposeTabGroup();
	}

	/**
	 * Dispose the active tab group, if any.
	 */
	protected void disposeTabGroup() {
		if (getTabGroup() != null) {
			getTabGroup().dispose();
			setTabGroup(null);
			setTabType(null);
		}
	}	
	
	/**
	 * Creates this viewer's control This area displays the name of the launch
	 * configuration currently being edited, as well as a tab folder of tabs
	 * that are applicable to the launch configuration.
	 *
	 * @return the composite used for launch configuration editing
	 */
	private void createControl(Composite parent) {
		Font font = parent.getFont();
		fViewerControl = new Composite(parent, SWT.NONE);
		GridLayout outerCompLayout = new GridLayout();
		outerCompLayout.numColumns = 1;
		outerCompLayout.marginHeight = 0;
		outerCompLayout.marginWidth = 0;
		fViewerControl.setLayout(outerCompLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		fViewerControl.setLayoutData(gd);
		
		Composite container = new Composite(fViewerControl, SWT.NONE);
		outerCompLayout = new GridLayout();
		outerCompLayout.numColumns = 2;
		outerCompLayout.marginHeight = 0;
		outerCompLayout.marginWidth = 5;
		container.setLayout(outerCompLayout);
		gd = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);
		setVisibleArea(container);

		fNameLabel = new Label(container, SWT.HORIZONTAL | SWT.LEFT);
		fNameLabel.setText(LaunchConfigurationsMessages.LaunchConfigurationDialog__Name__16); 
		gd = new GridData(GridData.BEGINNING);
		fNameLabel.setLayoutData(gd);
		fNameLabel.setFont(font);
		
		Text nameText = new Text(container, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		nameText.setLayoutData(gd);
		nameText.setFont(font);
		setNameWidget(nameText);

		getNameWidget().addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					handleNameModified();
				}
			}
		);

		Label spacer = new Label(container, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 2;
		spacer.setLayoutData(gd);
		
		/*
		 * fix for bug 66576 and 79709
		 */
		fTabPlaceHolder = new Composite(container, SWT.NONE);
		fTabPlaceHolder.setLayout(new StackLayout());
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		fTabPlaceHolder.setLayoutData(gd);
		
		fGettingStarted = new Composite(fTabPlaceHolder, SWT.NONE);
		fGettingStarted.setLayout(new GridLayout());
		gd = new GridData(GridData.FILL_BOTH);
		fGettingStarted.setLayoutData(gd);
		
		createGettingStarted(fGettingStarted);
		
		fTabComposite = new Composite(fTabPlaceHolder, SWT.NONE);
		GridLayout outerTabCompositeLayout = new GridLayout();
		outerTabCompositeLayout.marginHeight = 0;
		outerTabCompositeLayout.marginWidth = 0;
		fTabComposite.setLayout(outerTabCompositeLayout);
		gd = new GridData(GridData.FILL_BOTH);
		fTabComposite.setLayoutData(gd);

		createTabFolder(fTabComposite);
		
		Composite buttonComp = new Composite(container, SWT.NONE);
		GridLayout buttonCompLayout = new GridLayout();
		buttonCompLayout.numColumns = 2;
		buttonComp.setLayout(buttonCompLayout);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 2;
		buttonComp.setLayoutData(gd);

		setApplyButton(new Button(buttonComp, SWT.PUSH));
		getApplyButton().setText(LaunchConfigurationsMessages.LaunchConfigurationDialog__Apply_17); 
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		getApplyButton().setLayoutData(gd);
		getApplyButton().setFont(font);
		SWTUtil.setButtonDimensionHint(getApplyButton());
		getApplyButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleApplyPressed();
			}
		});

		setRevertButton(new Button(buttonComp, SWT.PUSH));
		getRevertButton().setText(LaunchConfigurationsMessages.LaunchConfigurationDialog_Revert_2);   
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		getRevertButton().setLayoutData(gd);
		getRevertButton().setFont(font);
		SWTUtil.setButtonDimensionHint(getRevertButton());
		getRevertButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleRevertPressed();
			}
		});

	}
	
	/**
	 * Simple method to create a spacer in the page
	 * 
	 * @param composite the composite to add the spacer to
	 * @param columnSpan the amount of space for the spacer
	 * @since 3.2
	 */
	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}// end createSpacer
	
	/**
	 * Creates some help text for the tab group launch types
	 * @param parent thep arent composite
	 * @since 3.2
	 */
	private void createGettingStarted(Composite parent) {
		Font font = parent.getFont();
		GridData gd = null;
		Label label = new Label(parent, SWT.LEFT | SWT.WRAP);
		label.setFont(font);
		label.setText(LaunchConfigurationsMessages.LaunchConfigurationTabGroupViewer_1);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = parent.getBounds().width - 30;
		label.setLayoutData(gd);
		createSpacer(parent, 1);
		label = new Label(parent, SWT.LEFT | SWT.WRAP);
		label.setFont(font);
		label.setText(LaunchConfigurationsMessages.LaunchConfigurationTabGroupViewer_2);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = parent.getBounds().width - 30;
		label.setLayoutData(gd);
		label = new Label(parent, SWT.LEFT | SWT.WRAP);
		label.setFont(font);
		label.setText(LaunchConfigurationsMessages.LaunchConfigurationTabGroupViewer_3);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = parent.getBounds().width - 30;
		label.setLayoutData(gd);
		label = new Label(parent, SWT.LEFT | SWT.WRAP);
		label.setFont(font);
		label.setText(LaunchConfigurationsMessages.LaunchConfigurationTabGroupViewer_4);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = parent.getBounds().width - 30;
		label.setLayoutData(gd);		
	}
	
	/**
	 * Creates the tab folder for displaying config instances
	 * @param parent
	 */
	private void createTabFolder(Composite parent) {
		Point size = null;
		if (fTabFolder != null) {
			size = fTabFolder.getSize();
			fTabFolder.dispose();
		}
		fTabFolder = new TabFolder(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		fTabFolder.setLayoutData(gd);
		fTabFolder.setFont(parent.getFont());
		if (size != null) {
			fTabFolder.setSize(size);
		}
		getTabFolder().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (!isInitializingTabs()) {
					handleTabSelected();
				}
			}
		});		
	}
	
	/**
	 * Sets the apply button
	 */
	private void setApplyButton(Button button) {
		fApplyButton = button;
	}
	
	/**
	 * Returns the apply button
	 */
	protected Button getApplyButton() {
		return fApplyButton;
	}	
	
	/**
	 * Sets the revert button
	 */
	private void setRevertButton(Button button) {
		fRevertButton = button;
	}	
	
	/**
	 * Returns the revert button
	 */
	protected Button getRevertButton() {
		return fRevertButton;
	}	
	
	/**
	 * Sets the tab folder
	 */
	protected TabFolder getTabFolder() {
		return fTabFolder;
	}
		
	/**
	 * Returns the name widget
	 */
	private Text getNameWidget() {
		return fNameWidget;
	}
	
	/**
	 * Sets the name widget
	 */
	private void setNameWidget(Text nameText) {
		fNameWidget = nameText;
	}
	
	/**
	 * Sets the current name
	 */
	public void setName(String name) {
		if (getWorkingCopy() != null) {
			if (name == null) {
				name = ""; //$NON-NLS-1$
			}
			getNameWidget().setText(name.trim());
			refreshStatus();
		}
	}	

	/**
	 * @see org.eclipse.jface.viewers.Viewer#getControl()
	 */
	public Control getControl() {
		return fViewerControl;
	}
	
	/**
	 * Returns the shell this viewer is contained in.
	 */
	protected Shell getShell() {
		return getControl().getShell();
	}

	/**
	 * @see org.eclipse.jface.viewers.IInputProvider#getInput()
	 */
	public Object getInput() {
		return fInput;
	}

	/**
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		if (getActiveTab() == null) {
			return new StructuredSelection();
		} 
		return new StructuredSelection(getActiveTab());
	}

	/**
	 * @see org.eclipse.jface.viewers.Viewer#refresh()
	 */
	public void refresh() {
		if (isInitializingTabs()) {
			return;
		}
		
		ILaunchConfigurationTab[] tabs = getTabs();
		if (!isInitializingTabs() && tabs != null) {
			// update the working copy from the active tab
			getActiveTab().performApply(getWorkingCopy());
			updateButtons();
			// update error ticks
			TabFolder folder = getTabFolder();
			for (int i = 0; i < tabs.length; i++) {
				ILaunchConfigurationTab tab = tabs[i];
				tab.isValid(getWorkingCopy());
				boolean error = tab.getErrorMessage() != null;
				TabItem item = folder.getItem(i);
				setTabIcon(item, error, tab);
			}		
		}
	}

	private void updateButtons() {
		boolean dirty = isDirty();
		getApplyButton().setEnabled(dirty && canSave());
		getRevertButton().setEnabled(dirty);
	}
	
	/**
	 * Set the specified tab item's icon to an error icon if <code>error</code> is true,
	 * or a transparent icon of the same size otherwise.
	 */
	private void setTabIcon(TabItem tabItem, boolean error, ILaunchConfigurationTab tab) {
		Image image = null;
		if (error) {
			image = DebugUIPlugin.getDefault().getLaunchConfigurationManager().getErrorTabImage(tab);
		} else {
			image = tab.getImage();
		}
		tabItem.setImage(image);
	}	

	/**
	 * @see org.eclipse.jface.viewers.Viewer#setInput(java.lang.Object)
	 */
	public void setInput(Object input) {
		if (input == null) {
			if (fInput == null) {
				return;
			}
			inputChanged(input);
		} else {
			if (!input.equals(fInput)) {
				inputChanged(input);
			}
		}
	}
	
	/**
	 * The input has changed to the given object, possibly <code>null</code>.
	 * 
	 * @param input the new input, possibly <code>null</code>
	 */
	protected void inputChanged(Object input) {
		fInput = input;
		Runnable r = new Runnable() {
			public void run() {
				try {
					if (fInput instanceof ILaunchConfiguration) {
						/*
						 * fix for bug 66576 and 79709
						 */
						((StackLayout)fTabPlaceHolder.getLayout()).topControl = fTabComposite;
						fTabPlaceHolder.layout(true);
						ILaunchConfiguration configuration = (ILaunchConfiguration)fInput;
						setOriginal(configuration);
						setWorkingCopy(configuration.getWorkingCopy());
						displayInstanceTabs();
					} else if (fInput instanceof ILaunchConfigurationType) {
						/*
						 * fix for bug 66576 and 79709
						 */
						fDescription = getDescription((ILaunchConfigurationType)fInput);
						setNoInput();
						refreshStatus();
					} else {
						setNoInput();
					}//end else
					setRedraw(true);
				} catch (CoreException ce) {
					errorDialog(ce);
					setNoInput();
					setRedraw(true);
				}
			}
		};
		BusyIndicator.showWhile(getShell().getDisplay(), r);
	}
	
	private void setNoInput() {
		setOriginal(null);
		setWorkingCopy(null);
		fNameLabel.setVisible(false);
		fNameWidget.setVisible(false);
		((StackLayout)fTabPlaceHolder.getLayout()).topControl = fGettingStarted;
		fTabPlaceHolder.layout(true);
		disposeExistingTabs();				
	}
	
    protected void setFocusOnName() {
        fNameWidget.setFocus();
        
    }
    
	private void setRedraw(boolean b) {
		if (fRedraw != b) {
			fRedraw = b;
			getVisibleArea().setRedraw(fRedraw);
		}	
	}	
	/**
	 * Displays tabs for the current working copy
	 */
	protected void displayInstanceTabs() {
		// Turn on initializing flag to ignore message updates
		setInitializingTabs(true);

		ILaunchConfigurationType type = null;
		try {
			type = getWorkingCopy().getType();
			showInstanceTabsFor(type);
		} catch (CoreException e) {
			errorDialog(e);
			setInitializingTabs(false);
			return;
		}

		// show the name area
		fNameLabel.setVisible(true);
		fNameWidget.setVisible(true);
		// Update the name field before to avoid verify error
		getNameWidget().setText(getWorkingCopy().getName());

		// Retrieve the current tab group.  If there is none, clean up and leave
		ILaunchConfigurationTabGroup tabGroup = getTabGroup();
		if (tabGroup == null) {
			IStatus status = new Status(IStatus.ERROR, DebugUIPlugin.getUniqueIdentifier(), 0, MessageFormat.format(LaunchConfigurationsMessages.LaunchConfigurationTabGroupViewer_No_tabs_defined_for_launch_configuration_type__0__1, new String[]{type.getName()}), null); 
			CoreException e = new CoreException(status);
			errorDialog(e);
			setInitializingTabs(false);
			return;
		}

		// Update the tabs with the new working copy
		tabGroup.initializeFrom(getWorkingCopy());

		// Update the name field after in case client changed it
		getNameWidget().setText(getWorkingCopy().getName());
		
		fCurrentTabIndex = getTabFolder().getSelectionIndex();

		// Turn off initializing flag to update message
		setInitializingTabs(false);
		
		if (!getVisibleArea().isVisible()) {
			getVisibleArea().setVisible(true);
		}
		
		refreshStatus();		
	}
	
	/**
	 * Populate the tabs in the configuration edit area to be appropriate to the current
	 * launch configuration type.
	 */
	private void showInstanceTabsFor(ILaunchConfigurationType configType) {

		// Don't do any work if the current tabs are for the current config type
		if (getTabType() != null && getTabType().equals(configType)) { 
			return;
		}
		
		// try to keep on same tab
		Class tabKind = null;
		if (getActiveTab() != null) {
			tabKind = getActiveTab().getClass();
		}
		
		// Build the new tabs
		ILaunchConfigurationTabGroup group = null;
		try {
			group = createGroup(configType);
		} catch (CoreException ce) {
			DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.LaunchConfigurationDialog_Error_19, LaunchConfigurationsMessages.LaunchConfigurationDialog_Exception_occurred_creating_launch_configuration_tabs_27,ce); // 
			return;
		}

		showTabsFor(group);
		setTabGroup(group);
		setTabType(configType);
		
		// select same tab as before, if possible
		ILaunchConfigurationTab[] tabs = getTabs();
		for (int i = 0; i < tabs.length; i++) {
			ILaunchConfigurationTab tab = tabs[i];
			if (tab.getClass().equals(tabKind)) {
				setActiveTab(tab);
				break;
			}
		}
		
		fDescription = getDescription(configType);
	}	

	/**
	 * Returns the description of the given configuration type
	 * in the current mode or <code>null</code> if none.
	 * 
	 * @param configType the config type
	 * @return the description of the given configuration type or <code>null</code>
	 */
	private String getDescription(ILaunchConfigurationType configType) {
		String description = null;
		if(configType != null) {
			String mode = fDialog.getMode();
			description = LaunchConfigurationPresentationManager.getDefault().getDescription(configType, mode);
		}	
		if (description == null)
			description = ""; //$NON-NLS-1$
		return description;
	}
	
	/**
	 * Create the tabs in the configuration edit area for the given tab group.
	 */
	private void showTabsFor(ILaunchConfigurationTabGroup tabGroup) {
		// turn off redraw
		setRedraw(false);
		// Dispose the current tabs
		disposeExistingTabs();

		setTabGroup(tabGroup);

		// Create the Control for each tab
		ILaunchConfigurationTab[] tabs = tabGroup.getTabs();
		for (int i = 0; i < tabs.length; i++) {
			TabItem tab = new TabItem(getTabFolder(), SWT.NONE);
			String name = tabs[i].getName();
			if (name == null) {
				name = LaunchConfigurationsMessages.LaunchConfigurationDialog_unspecified_28; 
			}
			tab.setText(name);
			Image image = tabs[i].getImage();
			tab.setImage(image);
			tabs[i].createControl(tab.getParent());
			Control control = tabs[i].getControl();
			if (control != null) {
				tab.setControl(control);
			}
		}

	}	
	
	/**
	 * Returns tab group for the given type of launch configuration.
	 * Tabs are initialized to be contained in this dialog.
	 *
	 * @exception CoreException if unable to instantiate a tab group
	 */
	protected ILaunchConfigurationTabGroup createGroup(final ILaunchConfigurationType configType) throws CoreException {
		// Use a final Object array to store the tab group and any exception that
		// results from the Runnable
		final Object[] finalArray = new Object[2];
		Runnable runnable = new Runnable() {
			public void run() {
				ILaunchConfigurationTabGroup tabGroup = null;
				try {
					tabGroup = LaunchConfigurationPresentationManager.getDefault().getTabGroup(configType, getLaunchConfigurationDialog().getMode());
					finalArray[0] = tabGroup;
				} catch (CoreException ce) {
					finalArray[1] = ce;
					return;
				}
				tabGroup.createTabs(getLaunchConfigurationDialog(), getLaunchConfigurationDialog().getMode());
				ILaunchConfigurationTab[] tabs = tabGroup.getTabs();
				for (int i = 0; i < tabs.length; i++) {
					tabs[i].setLaunchConfigurationDialog(getLaunchConfigurationDialog());
				}
			}
		};

		// Creating the tabs can result in plugin loading, so we show the busy cursor
		BusyIndicator.showWhile(getControl().getDisplay(), runnable);

		// Re-throw any CoreException if there was one
		if (finalArray[1] != null) {
			throw (CoreException)finalArray[1];
		}

		// Otherwise return the tab group
		return (ILaunchConfigurationTabGroup)finalArray[0];
	}	

	/**
	 * @see org.eclipse.jface.viewers.Viewer#setSelection(org.eclipse.jface.viewers.ISelection, boolean)
	 */
	public void setSelection(ISelection selection, boolean reveal) {
		if (getWorkingCopy() != null) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection structuredSelection = (IStructuredSelection)selection;
				Object object = structuredSelection.getFirstElement();
				if (object instanceof ILaunchConfigurationTab) {
					ILaunchConfigurationTab[] tabs = getTabs();
					for (int i = 0; i < tabs.length; i++) {
						ILaunchConfigurationTab tab = tabs[i];
						if (tab.equals(object)) {
							fCurrentTabIndex = i;
							getTabFolder().setSelection(i);
						}
						return;
					}
				}
			}
		}
			
	}

	/**
	 * Returns the tabs currently being displayed, or
	 * <code>null</code> if none.
	 *
	 * @return currently displayed tabs, or <code>null</code>
	 */
	public ILaunchConfigurationTab[] getTabs() {
		if (getTabGroup() != null) {
			return getTabGroup().getTabs();
		}
		return null;
	}

	/**
	 * Returns the currently active <code>ILaunchConfigurationTab</code>
	 * being displayed, or <code>null</code> if there is none.
	 *
	 * @return currently active <code>ILaunchConfigurationTab</code>, or <code>null</code>.
	 */
	public ILaunchConfigurationTab getActiveTab() {
		TabFolder folder = getTabFolder();
		ILaunchConfigurationTab[] tabs = getTabs();
		if (folder != null && tabs != null) {
			int pageIndex = folder.getSelectionIndex();
			if (pageIndex >= 0) {
				return tabs[pageIndex];
			}
		}
		return null;
	}
	
	/**
	 * Returns whether the launch configuration being edited is dirty (i.e.
	 * needs saving)
	 * 
	 * @return whether the launch configuration being edited needs saving
	 */
	public boolean isDirty() {
		ILaunchConfigurationWorkingCopy workingCopy = getWorkingCopy();
		if (workingCopy == null) {
			return false;
		}

		// Working copy hasn't been saved
		if (workingCopy.getOriginal() == null) {
			return true;
		}

		ILaunchConfiguration original = getOriginal();
		return !original.contentsEqual(workingCopy);
	}
	
	/**
	 * Update apply & revert buttons, as well as buttons and message on the
	 * launch config dialog.
	 */
	protected void refreshStatus() {
		if (!isInitializingTabs()) {
			getLaunchConfigurationDialog().updateButtons();
			getLaunchConfigurationDialog().updateMessage();
		}
	}	
	
	/**
	 * Returns the containing launch dialog
	 */
	protected ILaunchConfigurationDialog getLaunchConfigurationDialog() {
		return fDialog;
	}
	
	/**
	 * Sets the launch configuration being displayed/edited, possilby
	 * <code>null</code>.
	 */
	private void setOriginal(ILaunchConfiguration configuration) {
		fOriginal = configuration;
	}
	
	/**
	 * Returns the original launch configuration being edited, possibly
	 * <code>null</code>.
	 * 
	 * @return ILaunchConfiguration
	 */
	protected ILaunchConfiguration getOriginal() {
		return fOriginal;
	}
	
	/**
	 * Sets the working copy used to edit the original.
	 */
	private void setWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
		fWorkingCopy = workingCopy;
	}
	
	/**
	 * Returns the working copy used to edit the original, possibly
	 * <code>null</code>.
 	 */
	protected ILaunchConfigurationWorkingCopy getWorkingCopy() {
		return fWorkingCopy;
	}
	
	/**
	 * Return whether the current configuration can be saved.
	 * <p>
	 * Note this is NOT the same thing as the config simply being valid. It
	 * is possible to save a config that does not validate. This method
	 * determines whether the config can be saved without causing a serious
	 * error. For example, a shared config that has no specified location would
	 * cause this method to return <code>false</code>.
	 * </p>
	 */
	public boolean canSave() {
		if (isInitializingTabs()) {
			return false;
		}
		// First make sure that name doesn't prevent saving the config
		try {
			verifyName();
		} catch (CoreException ce) {
			return false;
		}

		// Next, make sure none of the tabs object to saving the config
		ILaunchConfigurationTab[] tabs = getTabs();
		if (tabs == null) {
			return false;
		}
		for (int i = 0; i < tabs.length; i++) {
			if (!tabs[i].canSave()) {
				return false;
			}
		}
		return true;
	}	
	
	/**
	 * @see ILaunchConfigurationDialog#canLaunch()
	 */
	public boolean canLaunch() {
		if(isInitializingTabs()) {
			return false;
		}
		if (getWorkingCopy() == null) {
			return false;
		}
		try {
			verifyName();
		} catch (CoreException e) {
			return false;
		}

		ILaunchConfigurationTab[] tabs = getTabs();
		if (tabs == null) {
			return false;
		}
		for (int i = 0; i < tabs.length; i++) {
			if (!tabs[i].isValid(getWorkingCopy())) {
				return false;
			}
		}
		return true;
	}	
	
	/**
	 * Returns the current error message or <code>null</code> if none.
	 */
	public String getErrorMesssage() {
		if (isInitializingTabs()) {
			return null;
		}
		
		if (getWorkingCopy() == null) {
			return null;
		}
		try {
			verifyName();
		} catch (CoreException ce) {
			return ce.getStatus().getMessage();
		}
	
		String message = null;
		ILaunchConfigurationTab activeTab = getActiveTab();
		if (activeTab == null) {
			return null;
		} 
		message = activeTab.getErrorMessage();
		if (message != null) {
			return message;
		}
		
		ILaunchConfigurationTab[] allTabs = getTabs();
		for (int i = 0; i < allTabs.length; i++) {
			ILaunchConfigurationTab tab = allTabs[i];
			if (tab == activeTab) {
				continue;
			}
			message = tab.getErrorMessage();
			if (message != null) {
				StringBuffer temp= new StringBuffer();
				temp.append('[');
				temp.append(DebugUIPlugin.removeAccelerators(tab.getName()));
				temp.append("]: "); //$NON-NLS-1$
				temp.append(message);
				return temp.toString();
			}
		}
		return null;
	}	
	
	/**
	 * Returns the current message or <code>null</code> if none.
	 * @return Returns an appropriate message for display to user. The message returned will be:
	 * The message defined by the visible tab,
	 * or The tab group description for the particular launch mode,
	 * or The generic tab group description,
	 * or <code>null</code> if no message is defined 
	 */
	public String getMessage() {
		if (isInitializingTabs()) {
			return null;
		}
		
		String message = fDescription;
		
		ILaunchConfigurationTab tab = getActiveTab();
		if (tab != null) {
			String tabMessage = tab.getMessage();
			if (tabMessage != null) {
				message = tabMessage;
			}
		}
		
		return message;
	}	
		
	/**
	 * Verify that the launch configuration name is valid.
	 */
	protected void verifyName() throws CoreException {
		if (fNameWidget.isVisible()) {
			String currentName = getNameWidget().getText().trim();
	
			// If there is no name, complain
			if (currentName.length() < 1) {
				throw new CoreException(new Status(IStatus.ERROR,
													 DebugUIPlugin.getUniqueIdentifier(),
													 0,
													 LaunchConfigurationsMessages.LaunchConfigurationDialog_Name_required_for_launch_configuration_11, 
													 null));
			}
	
			// See if name contains any 'illegal' characters
			IStatus status = ResourcesPlugin.getWorkspace().validateName(currentName, IResource.FILE);
			if (status.getCode() != IStatus.OK) {
				throw new CoreException(new Status(IStatus.ERROR,
													 DebugUIPlugin.getUniqueIdentifier(),
													 0,
													 status.getMessage(),
													 null));
			}
			
			// See if name contains any characters that we deem illegal.
			// '@' and '&' are disallowed because they corrupt menu items.
			char[] disallowedChars = new char[] { '@', '&' };
			for (int i = 0; i < disallowedChars.length; i++) {
				char c = disallowedChars[i];
				if (currentName.indexOf(c) > -1) {
					throw new CoreException(new Status(IStatus.ERROR,
														DebugUIPlugin.getUniqueIdentifier(),
														0,
														MessageFormat.format(LaunchConfigurationsMessages.LaunchConfigurationTabGroupViewer_0, new String[] { new String(new char[] {c}), currentName }), 
														null));
				}
			}
	
			// Otherwise, if there's already a config with the same name, complain
			if (!getOriginal().getName().equals(currentName)) {
				if (getLaunchManager().isExistingLaunchConfigurationName(currentName)) {
					throw new CoreException(new Status(IStatus.ERROR,
														 DebugUIPlugin.getUniqueIdentifier(),
														 0,
														 LaunchConfigurationsMessages.LaunchConfigurationDialog_Launch_configuration_already_exists_with_this_name_12, 
														 null));
				}
			}
		}
	}
	
	private void setDisposingTabs(boolean disposing) {
		fDisposingTabs = disposing;
	}

	private boolean isDisposingTabs() {
		return fDisposingTabs;
	}
	
	private void setInitializingTabs(boolean initializing) {
		fInitializingTabs = initializing;
	}

	private boolean isInitializingTabs() {
		return fInitializingTabs;
	}		
	
	private void disposeExistingTabs() {
		setDisposingTabs(true);
		TabItem[] oldTabs = getTabFolder().getItems();
		for (int i = 0; i < oldTabs.length; i++) {
			oldTabs[i].dispose();
		}
		createTabFolder(fTabComposite);
		disposeTabGroup();
		setDisposingTabs(false);
	}	
	
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
	
	/**
	 * Returns the type that tabs are currently displayed
	 * for, or <code>null</code> if none.
	 *
	 * @return launch configuration type or <code>null</code>
	 */
	private ILaunchConfigurationType getTabType() {
		return fTabType;
	}

	/**
	 * Sets the type that tabs are currently displayed
	 * for, or <code>null</code> if none.
	 *
	 * @param tabType launch configuration type
	 */
	private void setTabType(ILaunchConfigurationType tabType) {
		fTabType = tabType;
	}	
	
	/**
	 * Sets the current tab group being displayed
	 *
	 * @param group the current tab group being displayed
	 */
	private void setTabGroup(ILaunchConfigurationTabGroup group) {
		fTabGroup = group;
	}

	/**
	 * Returns the current tab group
	 *
	 * @return the current tab group, or <code>null</code> if none
	 */
	public ILaunchConfigurationTabGroup getTabGroup() {
		return fTabGroup;
	}
	
	/**
	 * Notification that a tab has been selected
	 *
	 * Disallow tab changing when the current tab is invalid.
	 * Update the config from the tab being left, and refresh
	 * the tab being entered.
	 */
	protected void handleTabSelected() {
		if (isDisposingTabs() || isInitializingTabs()) {
			return;
		}
		ILaunchConfigurationTab[] tabs = getTabs();
		if (fCurrentTabIndex == getTabFolder().getSelectionIndex() || tabs == null || tabs.length == 0 || fCurrentTabIndex > (tabs.length - 1)) {
			return;
		}
		if (fCurrentTabIndex != -1) {
			ILaunchConfigurationTab tab = tabs[fCurrentTabIndex];
			ILaunchConfigurationWorkingCopy wc = getWorkingCopy();
			if (wc != null) {
				tab.deactivated(wc);
				getActiveTab().activated(wc);
			}
		}
		fCurrentTabIndex = getTabFolder().getSelectionIndex();
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		fireSelectionChanged(event);
	}
	
	/**
	 * Notification the name field has been modified
	 */
	protected void handleNameModified() {
		getWorkingCopy().rename(getNameWidget().getText().trim());
		refreshStatus();
	}		
	
	/**
	 * Notification that the 'Apply' button has been pressed
	 */
	protected void handleApplyPressed() {
		try {
			// trim name
			Text widget = getNameWidget();
			String name = widget.getText();
			String trimmed = name.trim();

			// update launch config
			setInitializingTabs(true);
			if (!name.equals(trimmed)) {
				widget.setText(trimmed);
			}
			getWorkingCopy().rename(trimmed);
			getTabGroup().performApply(getWorkingCopy());
			setInitializingTabs(false);
			//
			
			if (isDirty()) {
				getWorkingCopy().doSave();
			}
			updateButtons();
		} catch (CoreException e) {
			DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.LaunchConfigurationDialog_Launch_Configuration_Error_46, LaunchConfigurationsMessages.LaunchConfigurationDialog_Exception_occurred_while_saving_launch_configuration_47, e); // 
			return;
		}
	}

	/**
	 * Notification that the 'Revert' button has been pressed
	 */
	protected void handleRevertPressed() {
		inputChanged(getOriginal());
	}	
	
	/**
	 * Show an error dialog on the given exception.
	 *
	 * @param exception
	 */
	protected void errorDialog(CoreException exception) {
		ErrorDialog.openError(getShell(), null, null, exception.getStatus());
	}	
	
	protected void setVisibleArea(Composite control) {
		fVisibleArea = control;
	}
	
	protected Composite getVisibleArea() {
		return fVisibleArea;
	}
	
	/**
	 * Sets the displayed tab to the given tab. Has no effect if the specified
	 * tab is not one of the tabs being displayed in the dialog currently.
	 * 
	 * @param tab the tab to display/activate
	 */
	public void setActiveTab(ILaunchConfigurationTab tab) {
		ILaunchConfigurationTab[] tabs = getTabs();
		for (int i = 0; i < tabs.length; i++) {
			ILaunchConfigurationTab configurationTab = tabs[i];
			if (configurationTab.equals(tab)) {
				setActiveTab(i);
				return;
			}
		}
	}
	
	/**
	 * Sets the displayed tab to the tab with the given index. Has no effect if
	 * the specified index is not within the limits of the tabs returned by
	 * <code>getTabs()</code>.
	 * 
	 * @param index the index of the tab to dispay
	 */
	public void setActiveTab(int index) {
		ILaunchConfigurationTab[] tabs = getTabs();
		if (index >= 0 && index < tabs.length) {
			getTabFolder().setSelection(index);
			handleTabSelected();
		}
	}
}
