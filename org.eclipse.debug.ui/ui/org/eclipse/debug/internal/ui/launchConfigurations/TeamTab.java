package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * This tab appears in the LaunchConfigurationDialog for all launch configuration
 * types.  It collects information that governs where the configuration is stored,
 * and whether or not it is shared via standard VCM mechanisms.
 */
public class TeamTab implements ILaunchConfigurationTab {

	// Flag that when true, prevents the owning dialog's status area from getting updated.
	// Used when multiple config attributes are getting updated at once.
	private boolean fBatchUpdate = false;
	
	// Local/shared UI widgets
	private Label fLocalSharedLabel;
	private Button fLocalRadioButton;
	private Button fSharedRadioButton;
	
	// Shared location UI widgets
	private Label fSharedLocationLabel;
	private Text fSharedLocationText;
	private Button fSharedLocationButton;
	
	// The launch configuration dialog that owns this tab
	private ILaunchConfigurationDialog fLaunchConfigurationDialog;
	
	// The launch config working copy providing the values shown on this tab
	private ILaunchConfigurationWorkingCopy fWorkingCopy;
	
	private static final String SHARED_LOCATION_CONTAINER_KEY = "shared_location_container_key";

	protected void setLaunchDialog(ILaunchConfigurationDialog dialog) {
		fLaunchConfigurationDialog = dialog;
	}
	
	protected ILaunchConfigurationDialog getLaunchDialog() {
		return fLaunchConfigurationDialog;
	}
	
	protected void setWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
		fWorkingCopy = workingCopy;
	}
	
	protected ILaunchConfigurationWorkingCopy getWorkingCopy() {
		return fWorkingCopy;
	}
	
	/**
	 * @see ILaunchConfigurationTab#createTabControl(TabItem)
	 */
	public Control createTabControl(ILaunchConfigurationDialog dialog, TabItem tabItem) {
		setLaunchDialog(dialog);
		
		Composite comp = new Composite(tabItem.getParent(), SWT.NONE);
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);		
		GridData gd;

		createVerticalSpacer(comp);
		
		Composite radioComp = new Composite(comp, SWT.NONE);
		GridLayout radioLayout = new GridLayout();
		radioLayout.marginHeight = 0;
		radioLayout.marginWidth = 0;
		radioComp.setLayout(radioLayout);
		
		setLocalSharedLabel(new Label(radioComp, SWT.NONE));
		getLocalSharedLabel().setText("Type of launch configuration");
		
		setLocalRadioButton(new Button(radioComp, SWT.RADIO));
		getLocalRadioButton().setText("Local");
		setSharedRadioButton(new Button(radioComp, SWT.RADIO));
		getSharedRadioButton().setText("Shared");
		getSharedRadioButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleSharedRadioButtonSelected();
			}
		});
		
		Composite locationComp = new Composite(comp, SWT.NONE);
		GridLayout locationLayout = new GridLayout();
		locationLayout.numColumns = 2;
		locationLayout.marginHeight = 0;
		locationLayout.marginWidth = 0;
		locationComp.setLayout(locationLayout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		locationComp.setLayoutData(gd);
		
		setSharedLocationLabel(new Label(locationComp, SWT.NONE));
		getSharedLocationLabel().setText("Location of shared configuration");
		gd = new GridData();
		gd.horizontalSpan = 2;
		getSharedLocationLabel().setLayoutData(gd);
		
		setSharedLocationText(new Text(locationComp, SWT.SINGLE | SWT.BORDER));
		gd = new GridData(GridData.FILL_HORIZONTAL);
		getSharedLocationText().setLayoutData(gd);
		getSharedLocationText().addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateConfigFromLocalShared();
			}
		});
		
		setSharedLocationButton(new Button(locationComp, SWT.PUSH));
		getSharedLocationButton().setText("Browse");	
		getSharedLocationButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleSharedLocationButtonSelected();
			}
		});	

		getLocalRadioButton().setSelection(true);
		setSharedEnabled(false);

		return comp;
	}

	/**
	 * Create some empty space 
	 */
	protected void createVerticalSpacer(Composite comp) {
		new Label(comp, SWT.NONE);
	}

	protected void setBatchUpdate(boolean update) {
		fBatchUpdate = update;
	}
	
	protected boolean isBatchUpdate() {
		return fBatchUpdate;
	}

	protected void setSharedLocationButton(Button sharedLocationButton) {
		this.fSharedLocationButton = sharedLocationButton;
	}

	protected Button getSharedLocationButton() {
		return fSharedLocationButton;
	}

	protected void setSharedLocationText(Text sharedLocationText) {
		this.fSharedLocationText = sharedLocationText;
	}

	protected Text getSharedLocationText() {
		return fSharedLocationText;
	}

	protected void setSharedLocationLabel(Label sharedLocationLabel) {
		this.fSharedLocationLabel = sharedLocationLabel;
	}

	protected Label getSharedLocationLabel() {
		return fSharedLocationLabel;
	}

	protected void setLocalSharedLabel(Label localSharedLabel) {
		fLocalSharedLabel = localSharedLabel;
	}

	protected Label getLocalSharedLabel() {
		return fLocalSharedLabel;
	}

 	protected void setLocalRadioButton(Button button) {
 		fLocalRadioButton = button;
 	}
 	
 	protected Button getLocalRadioButton() {
 		return fLocalRadioButton;
 	} 	
 	
 	protected void setSharedRadioButton(Button button) {
 		fSharedRadioButton = button;
 	}
 	
 	protected Button getSharedRadioButton() {
 		return fSharedRadioButton;
 	} 	
 	
	protected void handleSharedRadioButtonSelected() {
		setSharedEnabled(isShared());
		updateConfigFromLocalShared();
	}
	
	protected void setSharedEnabled(boolean enable) {
		getSharedLocationLabel().setEnabled(enable);
		getSharedLocationText().setEnabled(enable);
		getSharedLocationButton().setEnabled(enable);
	}
	
	protected boolean isShared() {
		return getSharedRadioButton().getSelection();
	}
	
	protected void handleSharedLocationButtonSelected() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(),
																	   getWorkspaceRoot(),
																	   false,
																	   "Select a location for the launch configuration");
		
		IContainer currentContainer = (IContainer)getSharedLocationText().getData(SHARED_LOCATION_CONTAINER_KEY);
		if (currentContainer != null) {
			IPath path = currentContainer.getFullPath();
			dialog.setInitialSelections(new Object[] {path});
		}
		
		
		dialog.open();
		Object[] results = dialog.getResult();		
		if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
			IPath path = (IPath)results[0];
			IContainer container = (IContainer) getWorkspaceRoot().findMember(path);
			String containerName = path.toString();
			getSharedLocationText().setText(containerName);
			getSharedLocationText().setData(SHARED_LOCATION_CONTAINER_KEY, container);
		}		
	}
	
	/**
	 * @see ILaunchConfigurationTab#setLaunchConfiguration(ILaunchConfigurationWorkingCopy)
	 */
	public void setLaunchConfiguration(ILaunchConfigurationWorkingCopy launchConfiguration) {
		if (launchConfiguration.equals(getWorkingCopy())) {
			return;
		}
		
		setBatchUpdate(true);
		updateWidgetsFromConfig(launchConfiguration);
		setBatchUpdate(false);

		setWorkingCopy(launchConfiguration);
	}

	/**
	 * Set values for all UI widgets in this tab using values kept in the specified
	 * launch configuration.
	 */
	protected void updateWidgetsFromConfig(ILaunchConfiguration config) {
		updateLocalSharedFromConfig(config);
		updateSharedLocationFromConfig(config);
	}
	
	protected void updateLocalSharedFromConfig(ILaunchConfiguration config) {
		boolean isShared = !config.isLocal();
		getSharedRadioButton().setSelection(isShared);
	}
	
	protected void updateSharedLocationFromConfig(ILaunchConfiguration config) {
		IFile file = config.getFile();
		if (file != null) {
			IPath path = file.getFullPath();
			IContainer container = (IContainer) getWorkspaceRoot().findMember(path);
			String containerName = path.toString();
			getSharedLocationText().setData(SHARED_LOCATION_CONTAINER_KEY, container);
			getSharedLocationText().setText(containerName);
		}
	}
	
	protected void updateConfigFromLocalShared() {
		if (getWorkingCopy() != null) {
			if (isShared()) {
				IContainer container = (IContainer) getSharedLocationText().getData(SHARED_LOCATION_CONTAINER_KEY);
				getWorkingCopy().setContainer(container);
			} else {
				getWorkingCopy().setContainer(null);
			}
		}
	}
	
	/**
	 * @see ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
	}

	/**
	 * Convenience method to get the shell.  It is important that the shell be the one 
	 * associated with the launch configuration dialog, and not the active workbench
	 * window.
	 */
	private Shell getShell() {
		return getLocalSharedLabel().getShell();
	}
	
	/**
	 * Convenience method for getting the workspace root.
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

}

