package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * The dialog used to edit and launch launch configurations.
 */
public class LaunchConfigurationDialog extends Dialog implements ISelectionChangedListener, 
																	 ILaunchConfigurationListener,
																	 ILaunchConfigurationDialog {
	
	/**
	 * The tree of launch configurations
	 */
	private TreeViewer fConfigTree;
	
	private Object fWorkbenchSelection;
	
	private Object fSelectedTreeObject;
	
	/**
	 * The starting mode, as specified by the caller
	 */
	private String fInitialMode;
	
	/**
	 * The 'new' button to create a new configuration
	 */
	private Button fNewButton;
	
	/**
	 * The 'delete' button to delete selected configurations
	 */
	private Button fDeleteButton;
	
	/**
	 * The 'copy' button to create a copy of the selected config
	 */
	private Button fCopyButton;
	
	/**
	 * The 'save & launch' button
	 */
	private Button fSaveAndLaunchButton;
	
	/**
	 * The 'save' button
	 */
	private Button fSaveButton;	
	
	/**
	 * The 'launch' button
	 */
	private Button fLaunchButton;
	
	/**
	 * The mutually exclusive radio buttons that specify the launch mode
	 */
	private Button fRadioRunButton;
	private Button fRadioDebugButton;
	
	/**
	 * Background color for everything in the 'wizard' banner area at the top of the dialog
	 */
	private Color fBannerBackground;
	
	/**
	 * Image for the launch mode
	 */
	private Label fModeLabel;
	
	/**
	 * The status area message
	 */
	private Label fMessageLabel;
	
	/**
	 * The status area image
	 */
	private Label fStatusImageLabel;
	
	/**
	 * The text widget displaying the name of the
	 * launch configuration under edit
	 */
	private Text fNameText;
	
	private String fLastSavedName = null;
	
	private String[] fSortedConfigNames = null;
	
	/**
	 * The tab folder
	 */
	private TabFolder fTabFolder;
	
	/**
	 * The current (working copy) launch configuration
	 * being displayed/edited or <code>null</code> if
	 * none
	 */
	private ILaunchConfigurationWorkingCopy fWorkingCopy;
	
	private boolean fWorkingCopyVerifyState = false;
	
	/**
	 * The current tab extensions being displayed
	 */
	private ILaunchConfigurationTab[] fTabs;
	
	/**
	 * Indicates whether callbacks on 'launchConfigurationChanged' should be treated
	 * as user changes.
	 */
	private boolean fChangesAreUserChanges = false;
	
	/**
	 * Indicates whether the current working copy has been dirtied by the user.
	 * This is the same as the more general notion of 'isDirty' on ILaunchConfigurationWorkingCopy,
	 * except that initializing defaults does not count as user dirty.
	 */
	private boolean fWorkingCopyUserDirty = false;
	
	/**
	 * Id for 'Save & Launch' button.
	 */
	protected static final int ID_SAVE_AND_LAUNCH_BUTTON = IDialogConstants.CLIENT_ID + 1;
		
	/**
	 * Id for 'Launch' button.
	 */
	protected static final int ID_LAUNCH_BUTTON = IDialogConstants.CLIENT_ID + 2;
	
	/**
	 * Empty array
	 */
	protected static final Object[] EMPTY_ARRAY = new Object[0];	
	
	protected static final String DEFAULT_NEW_CONFIG_NAME = "New configuration";
	
	/**
	 * Status area messages
	 */
	protected static final String LAUNCH_STATUS_OK_MESSAGE = "Ready to launch";
	protected static final String LAUNCH_STATUS_STARTING_FROM_SCRATCH_MESSAGE 
										= "Select a configuration to launch or a config type to create a new configuration";
	
	/**
	 * Constructs a new launch configuration dialog on the given
	 * parent shell.
	 * 
	 * @param shell the parent shell, or <code>null</code> to
	 *  create a top-level shell
	 */
	public LaunchConfigurationDialog(Shell shell, IStructuredSelection selection, String mode) {
		super(shell);
		resolveWorkbenchSelection(selection);
		setInitialMode(mode);
	}
	
	protected void resolveWorkbenchSelection(IStructuredSelection selection) {
		if ((selection == null) || (selection.size() < 1)) {
			setWorkbenchSelection(null);
		} else {
			setWorkbenchSelection(selection.getFirstElement());
		}
	}
	
	/**
	 * A launch configuration dialog overrides this method
	 * to create a custom set of buttons in the button bar.
	 * This dialog has 'Save & Launch', 'Launch', and 'Cancel'
	 * buttons.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		setSaveAndLaunchButton(createButton(parent, ID_SAVE_AND_LAUNCH_BUTTON, "S&ave and Launch", false));
		setLaunchButton(createButton(parent, ID_LAUNCH_BUTTON, "&Launch", true));
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}	

	/**
	 * Handle the 'save and launch' & 'launch' buttons here, all others are handled
	 * in <code>Dialog</code>
	 * 
	 * @see Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == ID_SAVE_AND_LAUNCH_BUTTON) {
			handleSaveAndLaunchPressed();
		} else if (buttonId == ID_LAUNCH_BUTTON) {
			handleLaunchPressed();
		} else {
			super.buttonPressed(buttonId);
		}
	}

	/**
	 * @see Dialog#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		initializeSettings();
		getLaunchManager().addLaunchConfigurationListener(this);
		return contents;
	}
	
	/**
	 * @see Window#close()
	 */
	public boolean close() {
		getLaunchManager().removeLaunchConfigurationListener(this);
		return super.close();
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		GridData gd;
		Composite composite = (Composite)super.createDialogArea(parent);
		GridLayout topLevelLayout = (GridLayout) composite.getLayout();
		topLevelLayout.numColumns = 2;
		topLevelLayout.marginHeight = 0;
		topLevelLayout.marginWidth = 0;

		// Build the launch configuration banner area
		// and put it into the composite.
		Composite launchConfigBannerArea = createLaunchConfigurationBannerArea(composite);		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		launchConfigBannerArea.setLayoutData(gd);
		
		// Build the launch configuration selection area
		// and put it into the composite.
		Composite launchConfigSelectionArea = createLaunchConfigurationSelectionArea(composite);
		gd = new GridData(GridData.FILL_VERTICAL);
		launchConfigSelectionArea.setLayoutData(gd);
	
		// Build the launch configuration edit area
		// and put it into the composite.
		Composite launchConfigEditArea = createLaunchConfigurationEditArea(composite);
		gd = new GridData(GridData.FILL_BOTH);
		launchConfigEditArea.setLayoutData(gd);
			
		// Build the separator line
		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);
		
		return composite;
	}
	
	/**
	 * Initialize the dialog settings.  This means setting the input for the launch
	 * config tree, setting the initial selection in the tree, and initializing the
	 * edit area tabs.  Also set the state of the mode buttons.
	 */
	protected void initializeSettings() {
		getTreeViewer().setInput(ResourcesPlugin.getWorkspace().getRoot());		
		initializeFirstConfig();
		initializeModeButtons();		
	}
	
	/**
	 * Based on the current workbench selection, set the selection in the launch 
	 * config tree when this dialog is first realized.
	 */
	protected void initializeFirstConfig() {
		Object workbenchSelection = getWorkbenchSelection();
		if (workbenchSelection == null) {
			clearLaunchConfiguration();
			return;
		}
		
		if (workbenchSelection instanceof ILaunchConfiguration) {
			initializeFirstConfigForConfiguration();
		} else {
			initializeFirstConfigForConfigurationType();
		}
	}
	
	/**
	 * If an actual <code>ILaunchConfiguration</code> was selected in
	 * the workbench, select it in the tree.  	
	 */
	protected void initializeFirstConfigForConfiguration() {
		IStructuredSelection selection = new StructuredSelection(getWorkbenchSelection());
		setTreeViewerSelection(selection);
	}
	
	/**
	 * Something other than an <code>ILaunchConfiguration</code> was selected in
	 * the workbench, so try to determine an <code>ILaunchConfigurationType</code>
	 * from the selection, then create a new working copy of that type, initialize
	 * its default values, set this new launch configuration so that the edit area
	 * tabs get populated, and finally make sure the config type is selected in the 
	 * configuration tree.
	 */
	protected void initializeFirstConfigForConfigurationType() {
		ILaunchConfigurationType configType = determineConfigTypeFromSelection();
		if (configType == null) {
			clearLaunchConfiguration();
			return;
		}
		
		ILaunchConfigurationWorkingCopy workingCopy = null;
		try {
			setChangesAreUserChanges(false);
			workingCopy = configType.newInstance(null, DEFAULT_NEW_CONFIG_NAME);
			workingCopy.initializeDefaults(getWorkbenchSelection());
		} catch (CoreException ce) {
			return;	
		}
		
		setLaunchConfiguration(workingCopy);
		IStructuredSelection selection = new StructuredSelection(configType);
		setTreeViewerSelection(selection);
	}
	
	/**
	 * Attempt to determine the launch config type most closely associated
	 * with the current workbench selection.
	 */
	protected ILaunchConfigurationType determineConfigTypeFromSelection() {
		
		IProject project = null;
		Object workbenchSelection = getWorkbenchSelection();
		if (workbenchSelection instanceof IResource) {
			project = ((IResource)workbenchSelection).getProject();
		} else if (workbenchSelection instanceof IAdaptable) {
			IResource resource = (IResource) ((IAdaptable)workbenchSelection).getAdapter(IResource.class);
			if (resource != null) {
				project = resource.getProject();
			}
		}
		ILaunchConfigurationType type = null;
		if (project != null) {
			try {
				type = getLaunchManager().getDefaultLaunchConfigurationType(project);
			} catch (CoreException ce) {
				return null;
			}
		}
		return type;
		
		/*   //MAJOR HACK for quick&dirty testing
		ILaunchConfigurationType[] types = getLaunchManager().getLaunchConfigurationTypes();		
		return types[1];
		*/   //END OF MAJOR HACK		
	}
	
	/**
	 * Set the state of the Run/Debug radio buttons based on any initial mode that was
	 * passed in to this dialog.
	 */
	protected void initializeModeButtons() {
		if (getInitialMode().equals(ILaunchManager.DEBUG_MODE)) {
			getRadioDebugButton().setSelection(true);
		} else {
			getRadioRunButton().setSelection(true);
		}
		setModeLabelState();
	}
	
	protected void setModeLabelState() {
		Image image;
		if (getRadioDebugButton().getSelection()) {
			image = DebugUITools.getImage(IDebugUIConstants.IMG_WIZBAN_DEBUG);
		} else {
			image = DebugUITools.getImage(IDebugUIConstants.IMG_WIZBAN_RUN);
		}
		fModeLabel.setImage(image);
	}
	
	/**
	 * Convenience method to set the selection on the configuration tree.
	 */
	protected void setTreeViewerSelection(ISelection selection) {
		getTreeViewer().setSelection(selection);
	}
	
	/**
	 * Return whether the debug mode radio button is currently selected.
	 * Because there are only two modes, RUN & DEBUG, a return value of false
	 * can be interpreted to mean the RUN mode is selected.
	 */
	protected String getMode() {
		if (getRadioDebugButton().getSelection()) {
			return ILaunchManager.DEBUG_MODE;
		}
		return ILaunchManager.RUN_MODE;
	}

	private void setLastSavedName(String lastSavedName) {
		this.fLastSavedName = lastSavedName;
	}

	private String getLastSavedName() {
		return fLastSavedName;
	}
	
	/**
	 * If verifying fails, we catch a <code>CoreException</code>, in which
	 * case we extract the error message and set it in the status area.
	 * Otherwise, we set the "ready to launch" message in the status area.
	 * 
	 * @see ILaunchConfigurationDialog#refreshStatus()
	 */
	public void refreshStatus() {
		
		// If there is no working copy, then the user is starting from scratch
		ILaunchConfigurationWorkingCopy workingCopy = getWorkingCopy();
		if (workingCopy == null) {
			setWorkingCopyVerifyState(false);
			setStatusStartingFromScratchMessage();
			setEnableStateEditButtons();
			return;
		}
		
		// Verify the working copy.  Any CoreExceptions indicate a problem with
		// the working copy, so update the status area and internal state accordingly
		try {
			getWorkingCopy().verify(getMode());
			verifyStandardAttributes();
		} catch (CoreException ce) {
			setWorkingCopyVerifyState(false);
			String message = ce.getStatus().getMessage();
			setStatusErrorMessage(message);
			setEnableStateEditButtons();
			return;
		}	
		
		// Otherwise the verify was successful, update status area and internal state 	
		setWorkingCopyVerifyState(true);
		setStatusOKMessage();
		setEnableStateEditButtons();
	}
	
	/**
	 * Verify the attributes common to all launch configuration.
	 * To be consistent with <code>ILaunchConfiguration.verify</code>,
	 * indicate failure by throwing a <code>CoreException</code>.
	 */
	protected void verifyStandardAttributes() throws CoreException {
		verifyName();
	}
	
	/**
	 * Verify that there are no name collisions.
	 */
	protected void verifyName() throws CoreException {
		String currentName = getNameTextWidget().getText();

		// If there is no name, complain
		if (currentName.trim().length() < 1) {
			throw new CoreException(new Status(IStatus.ERROR,
												 DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
												 0,
												 "Name required for launch configuration.",
												 null));			
		}

		// If the name hasn't changed from the last saved name, do nothing
		if (currentName.equals(getLastSavedName())) {
			return;
		}				
		
		// Otherwise, if there's already a config with the same name, complain
		if (configExists(currentName)) {
			throw new CoreException(new Status(IStatus.ERROR,
												 DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
												 0,
												 "Launch configuration already exists with this name.",
												 null));						
		}						
	}
	
	/**
	 * Return whether or not there is an existing launch configuration with
	 * the specified name.
	 */
	protected boolean configExists(String name) {
		String[] sortedConfigNames = getAllSortedConfigNames();
		int index = Arrays.binarySearch(sortedConfigNames, name);
		if (index < 0) {
			return false;
		} 
		return true;
	}
	
	protected void updateConfigFromName() {
		if (getWorkingCopy() != null) {
			getWorkingCopy().rename(getNameTextWidget().getText());
			refreshStatus();
		}
	}
	
	/**
	 * Return a sorted array of the names of all <code>ILaunchConfiguration</code>s in 
	 * the workspace.
	 */
	protected String[] getAllSortedConfigNames() {
		if (fSortedConfigNames == null) {
			ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations();
			fSortedConfigNames = new String[configs.length];
			for (int i = 0; i < configs.length; i++) {
				fSortedConfigNames[i] = configs[i].getName();
			}
			Arrays.sort(fSortedConfigNames);
		}
		return fSortedConfigNames;
	}
	
	protected void clearConfigNameCache() {
		fSortedConfigNames = null;
	}
	
	protected void setStatusErrorMessage(String message) {
		setStatusMessage(message);
		getStatusImageLabel().setImage(DebugUITools.getImage(IDebugUIConstants.IMG_WIZBAN_FAIL));
		getStatusImageLabel().setVisible(true);
	}
	
	protected void setStatusOKMessage() {
		setStatusMessage(LAUNCH_STATUS_OK_MESSAGE);
		getStatusImageLabel().setImage(DebugUITools.getImage(IDebugUIConstants.IMG_WIZBAN_OK));
		getStatusImageLabel().setVisible(true);
	}
	
	protected void setStatusStartingFromScratchMessage() {
		setStatusMessage(LAUNCH_STATUS_STARTING_FROM_SCRATCH_MESSAGE);
		getStatusImageLabel().setImage(DebugUITools.getImage(IDebugUIConstants.IMG_WIZBAN_OK));		
		getStatusImageLabel().setVisible(false);
	}
	
	/**
	 * Convenience method to set text on the banner area status label.
	 */
	protected void setStatusMessage(String message) {
		getMessageLabel().setText(message);
	}
	
	protected Display getDisplay() {
		return getShell().getDisplay();
	}
		
	/**
	 * Creates the launch configuration selection area of the dialog.
	 * This area displays a tree of launch configrations that the user
	 * may select, and allows users to create new configurations, and
	 * delete and copy existing configurations.
	 * 
	 * @return the composite used for launch configuration selection
	 */ 
	protected Composite createLaunchConfigurationSelectionArea(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 5;
		c.setLayout(layout);
		
		GridData gd;
		
		TreeViewer tree = new TreeViewer(c);
		gd = new GridData(GridData.FILL_VERTICAL);
		gd.horizontalSpan = 3;
		gd.widthHint = 200;
		gd.heightHint = 375;
		tree.getControl().setLayoutData(gd);
		tree.setContentProvider(new LaunchConfigurationContentProvider());
		tree.setLabelProvider(DebugUITools.newDebugModelPresentation());
		setTreeViewer(tree);
		tree.addSelectionChangedListener(this);
		
		Button newButton = new Button(c, SWT.PUSH | SWT.CENTER);
		newButton.setText("&New");
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 1;
		newButton.setLayoutData(gd);
		setNewButton(newButton);
		
		newButton.addSelectionListener(
			new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent event) {
					handleNewPressed();
				}
			}
		);				
		
		Button deleteButton = new Button(c, SWT.PUSH | SWT.CENTER);
		deleteButton.setText("&Delete");
		gd = new GridData(GridData.CENTER);
		gd.horizontalSpan = 1;
		deleteButton.setLayoutData(gd);
		setDeleteButton(deleteButton);
		
		deleteButton.addSelectionListener(
			new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent event) {
					handleDeletePressed();
				}
			}
		);			
		
		Button copyButton = new Button(c, SWT.PUSH | SWT.CENTER);
		copyButton.setText("&Copy");
		gd = new GridData(GridData.END);
		gd.horizontalSpan = 1;
		copyButton.setLayoutData(gd);
		setCopyButton(copyButton);
		
		copyButton.addSelectionListener(
			new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent event) {
					handleCopyPressed();
				}
			}
		);			
		
		return c;
	}	
	
	/**
	 * Creates the launch configuration edit area of the dialog.
	 * This area displays the name of the launch configuration
	 * currently being edited, as well as a tab folder of tabs
	 * that are applicable to the launch configuration.
	 * 
	 * @return the composite used for launch configuration editing
	 */ 
	protected Composite createLaunchConfigurationEditArea(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 5;
		c.setLayout(layout);
		
		GridData gd;
		
		Label nameLabel = new Label(c, SWT.HORIZONTAL | SWT.LEFT);
		nameLabel.setText("Name");
		gd = new GridData(GridData.BEGINNING);
		nameLabel.setLayoutData(gd);		
		
		Text nameText = new Text(c, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		nameText.setLayoutData(gd);
		setNameTextWidget(nameText);
		
		getNameTextWidget().addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					updateConfigFromName();
				}
			}
		);		
		
		Label spacer = new Label(c, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 2;
		spacer.setLayoutData(gd);
		
		TabFolder tabFolder = new TabFolder(c, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.heightHint = 375;
		gd.widthHint = 375;
		tabFolder.setLayoutData(gd);
		setTabFolder(tabFolder);
		
		Button saveButton = new Button(c, SWT.PUSH | SWT.CENTER);
		saveButton.setText("&Save");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 2;
		saveButton.setLayoutData(gd);
		setSaveButton(saveButton);
		getSaveButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleSavePressed();
			}
		});
		
		return c;
	}	
	
	/**
	 * Creates the launch configuration banner area of the dialog.
	 * This area displays radio buttons for the launch mode, as well
	 * as a label for communicating errors to the user.
	 * 
	 * @return the composite used for the launch configuration banner area
	 */ 
	protected Composite createLaunchConfigurationBannerArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topLayout.marginHeight = 0;
		topLayout.marginWidth = 0;
		topLayout.numColumns = 4;
		comp.setLayout(topLayout);
		comp.setBackground(getBannerBackground());
		GridData gd;
		
		Composite statusImageComp = new Composite(comp, SWT.NONE);
		statusImageComp.setBackground(getBannerBackground());
		GridLayout statusImageLayout = new GridLayout();
		statusImageLayout.marginHeight = 0;
		statusImageLayout.marginWidth = 4;
		statusImageComp.setLayout(statusImageLayout);
		
		setStatusImageLabel(new Label(statusImageComp, SWT.NONE));
		getStatusImageLabel().setAlignment(SWT.RIGHT);
		getStatusImageLabel().setBackground(getBannerBackground());
		// This is necessary so that the 'status image label' gets sized properly
		// but doesn't appear yet
		getStatusImageLabel().setImage(DebugUITools.getImage(IDebugUIConstants.IMG_WIZBAN_OK));
		getStatusImageLabel().setVisible(false);
		
		setMessageLabel(new Label(comp, SWT.NONE));
		getMessageLabel().setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));
		getMessageLabel().setBackground(getBannerBackground());
		getMessageLabel().setAlignment(SWT.LEFT);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		getMessageLabel().setLayoutData(gd);
		
		Composite radioComposite = new Composite(comp, SWT.NONE);
		radioComposite.setBackground(getBannerBackground());
		GridLayout radioLayout = new GridLayout();
		radioLayout.numColumns = 1;
		radioComposite.setLayout(radioLayout);
		//gd = new GridData(GridData.FILL_BOTH);
		//radioComposite.setLayoutData(gd);
		
		setRadioRunButton(new Button(radioComposite, SWT.RADIO));
		getRadioRunButton().setBackground(getBannerBackground());
		getRadioRunButton().setText("Run");
		setRadioDebugButton(new Button(radioComposite, SWT.RADIO));
		getRadioDebugButton().setBackground(getBannerBackground());
		getRadioDebugButton().setText("Debug");
		getRadioDebugButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				setModeLabelState();
			}
		});
		
		fModeLabel = new Label(comp, SWT.NONE);
		
		return comp;
	}

	/**
	 * Sets the title for the dialog.
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(Shell);
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Launch Configurations");
	}
	
	/**
	 * Sets the tree viewer used to display launch configurations.
	 * 
	 * @param viewer the tree viewer used to display launch
	 *  configurations
	 */
	private void setTreeViewer(TreeViewer viewer) {
		fConfigTree = viewer;
	}
	
	/**
	 * Returns the tree viewer used to display launch configurations.
	 * 
	 * @param the tree viewer used to display launch configurations
	 */
	protected TreeViewer getTreeViewer() {
		return fConfigTree;
	}
	
	protected Object getTreeViewerFirstSelectedElement() {
		IStructuredSelection selection = (IStructuredSelection)getTreeViewer().getSelection();
		if (selection == null) {
			return null;
		}
		return selection.getFirstElement();
	}
		
	/**
	 * Content provider for launch configuration tree
	 */
	class LaunchConfigurationContentProvider implements ITreeContentProvider {
		
		/**
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ILaunchConfiguration) {
				return EMPTY_ARRAY;
			} else if (parentElement instanceof ILaunchConfigurationType) {
				try {
					ILaunchConfigurationType type = (ILaunchConfigurationType)parentElement;
					// all configs in workspace of a specific type
					return getLaunchManager().getLaunchConfigurations(type);
				} catch (CoreException e) {
					DebugUIPlugin.errorDialog(getShell(), "Error", "An exception occurred while retrieving launch configurations.", e.getStatus());
				}
			} else {
				return getLaunchManager().getLaunchConfigurationTypes();
			}
			return EMPTY_ARRAY;
		}

		/**
		 * @see ITreeContentProvider#getParent(Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof ILaunchConfiguration) {
				if (!((ILaunchConfiguration)element).exists()) {
					return null;
				}
				try {
					return ((ILaunchConfiguration)element).getType();
				} catch (CoreException e) {
					DebugUIPlugin.errorDialog(getShell(), "Error", "An exception occurred while retrieving launch configurations.", e.getStatus());
				}
			} else if (element instanceof ILaunchConfigurationType) {
				return ResourcesPlugin.getWorkspace().getRoot();
			}
			return null;
		}

		/**
		 * @see ITreeContentProvider#hasChildren(Object)
		 */
		public boolean hasChildren(Object element) {
			if (element instanceof ILaunchConfiguration) {
				return false;
			} else {
				return getChildren(element).length > 0;
			}
		}

		/**
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			return getLaunchManager().getLaunchConfigurationTypes();
		}

		/**
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}
	
	/**
	 * Returns the launch manager.
	 * 
	 * @return the launch manager
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	/**
	 * Returns whether this dialog is currently open
	 */
	protected boolean isVisible() {
		return getTreeViewer() != null;
	}	
		
	/**
	 * Notification selection has changed in the launch configuration tree.
	 * <p>
	 * If the currently displayed configuration is not saved,
	 * prompt for saving before moving on to the new selection.
	 * </p>
	 * 
	 * @param event selection changed event
	 */
 	public void selectionChanged(SelectionChangedEvent event) {
 		
 		// Get the selection		
 		IStructuredSelection selection = (IStructuredSelection)event.getSelection();
 		Object firstSelectedElement = selection.getFirstElement();		
 		boolean singleSelection = selection.size() == 1;
		boolean configSelected = firstSelectedElement instanceof ILaunchConfiguration;

 		// If selection is the same, don't bother
 		if (getSelectedTreeObject() == firstSelectedElement) {
 			return;
 		}
 		
		// Take care of any unsaved changes.  If the user aborts, reset selection
		// to whatever it was previously
 		if (!canReplaceWorkingCopy()) {
 			StructuredSelection prevSelection;
 			Object selectedTreeObject = getSelectedTreeObject();
			if (selectedTreeObject == null) {
				prevSelection = new StructuredSelection();
			} else {
				prevSelection = new StructuredSelection(selectedTreeObject);
			}
 			setTreeViewerSelection(prevSelection);
 			return;
 		}			
 				
 		// Enable tree-related buttons
 		setEnableStateTreeButtons(configSelected);
 		 		 
		// If a config is selected, update the edit area for it, if a config type is
		// selected, behave as if user clicked 'New'
 		if (singleSelection && configSelected) {
 			ILaunchConfiguration config = (ILaunchConfiguration) firstSelectedElement; 			
 			setLastSavedName(config.getName());
 			setLaunchConfiguration(config);
 		} else if (singleSelection && firstSelectedElement instanceof ILaunchConfigurationType) {
			constructNewConfig();
 		} else {
 			// multi-selection
 			clearLaunchConfiguration();
 		}
 		
 		setSelectedTreeObject(firstSelectedElement);
 	}
 	
 	/**
 	 * Set the enabled state of the buttons that relate to the tree viewer.  Note that
 	 * the 'New' button is always enabled.
 	 */
 	protected void setEnableStateTreeButtons(boolean enable) {
		getCopyButton().setEnabled(enable);
		getDeleteButton().setEnabled(enable); 		
 	}
 	
 	/**
 	 * Set the enabled state of the buttons that appear on the edit side of the dialog
 	 * (the 'save' & 'launch' buttons).
 	 */
 	protected void setEnableStateEditButtons() {
		boolean verifies = getWorkingCopyVerifyState();
		boolean dirty = isWorkingCopyDirty();
		getSaveButton().setEnabled(verifies && dirty);
		getSaveAndLaunchButton().setEnabled(verifies && dirty);
		getLaunchButton().setEnabled(verifies);
 		
 	}
 
 	/**
 	 * Sets the 'save & launch' button.
 	 * 
 	 * @param button the 'save & launch' button.
 	 */	
 	private void setSaveAndLaunchButton(Button button) {
 		fSaveAndLaunchButton = button;
 	}
 	
 	/**
 	 * Returns the 'save & launch' button
 	 * 
 	 * @return the 'save & launch' button
 	 */
 	protected Button getSaveAndLaunchButton() {
 		return fSaveAndLaunchButton;
 	}
 	
 	/**
 	 * Sets the 'launch' button.
 	 * 
 	 * @param button the 'launch' button.
 	 */	
 	private void setLaunchButton(Button button) {
 		fLaunchButton = button;
 	} 	
 	
 	/**
 	 * Returns the 'launch' button
 	 * 
 	 * @return the 'launch' button
 	 */
 	protected Button getLaunchButton() {
 		return fLaunchButton;
 	} 	
 	
 	/**
 	 * Sets the 'new' button.
 	 * 
 	 * @param button the 'new' button.
 	 */	
 	private void setNewButton(Button button) {
 		fNewButton = button;
 	} 	
 	
  	/**
 	 * Returns the 'new' button
 	 * 
 	 * @return the 'new' button
 	 */
 	protected Button getNewButton() {
 		return fNewButton;
 	}
 	
 	/**
 	 * Sets the 'delete' button.
 	 * 
 	 * @param button the 'delete' button.
 	 */	
 	private void setDeleteButton(Button button) {
 		fDeleteButton = button;
 	} 	
 	
 	/**
 	 * Returns the 'delete' button
 	 * 
 	 * @return the 'delete' button
 	 */
 	protected Button getDeleteButton() {
 		return fDeleteButton;
 	}
 	 	
 	/**
 	 * Sets the 'copy' button.
 	 * 
 	 * @param button the 'copy' button.
 	 */	
 	private void setCopyButton(Button button) {
 		fCopyButton = button;
 	} 	
 	
 	/**
 	 * Returns the 'copy' button
 	 * 
 	 * @return the 'copy' button
 	 */
 	protected Button getCopyButton() {
 		return fCopyButton;
 	} 	
 	
 	/**
 	 * Sets the 'debug' radio button.
 	 * 
 	 * @param button the 'debug' button.
 	 */	
 	protected void setRadioDebugButton(Button button) {
 		fRadioDebugButton = button;
 	}
 	
 	/**
 	 * Returns the 'debug' radio button
 	 * 
 	 * @return the 'debug' button
 	 */
 	protected Button getRadioDebugButton() {
 		return fRadioDebugButton;
 	} 	
 	
 	/**
 	 * Sets the 'run' radio button.
 	 * 
 	 * @param button the 'run' button.
 	 */	
 	protected void setRadioRunButton(Button button) {
 		fRadioRunButton = button;
 	}
 	
 	/**
 	 * Returns the 'run' radio button
 	 * 
 	 * @return the 'run' button
 	 */
 	protected Button getRadioRunButton() {
 		return fRadioRunButton;
 	} 	
 	
 	/**
 	 * Sets the 'message' label.
 	 * 
 	 * @param label the 'message' label.
 	 */	
	protected void setMessageLabel(Label label) {
		fMessageLabel = label;
	}
	
 	/**
 	 * Returns the 'message' label
 	 * 
 	 * @return the 'message' label
 	 */
	protected Label getMessageLabel() {
		return fMessageLabel;
	}
	
 	/**
 	 * Sets the 'status image' label.
 	 * 
 	 * @param label the 'status image' label.
 	 */	
	protected void setStatusImageLabel(Label label) {
		fStatusImageLabel = label;
	}
	
 	/**
 	 * Returns the 'status image' label
 	 * 
 	 * @return the 'status image' label
 	 */
	protected Label getStatusImageLabel() {
		return fStatusImageLabel;
	}
	
	/**
	 * Return the banner background color using a lazy fetching policy
	 */
	protected Color getBannerBackground() {
		if (fBannerBackground == null) {
			fBannerBackground = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		}
		return fBannerBackground;
	}
	
 	/**
 	 * Sets the configuration to display/edit.
 	 * Updates the tab folder to contain the appropriate pages.
 	 * Sets all configuration-related state appropriately.
 	 * 
 	 * @param config the launch configuration to display/edit
 	 */
 	protected void setLaunchConfiguration(ILaunchConfiguration config) {
		try {
			setTabsForConfigType(config.getType());
			
			if (config.isWorkingCopy()) {
		 		setWorkingCopy((ILaunchConfigurationWorkingCopy)config);
			} else {
				setWorkingCopy(config.getWorkingCopy());
			}
	 		// update the name field
	 		getNameTextWidget().setText(config.getName());
	 		
	 		// Reset internal flags so user edits are recognized as making the
	 		// working copy 'user dirty'
	 		setChangesAreUserChanges(true);
	 		setWorkingCopyUserDirty(false);
	 		
	 		// update the tabs with the new working copy
	 		ILaunchConfigurationTab[] tabs = getTabs();
	 		for (int i = 0; i < tabs.length; i++) {
				tabs[i].setLaunchConfiguration(getWorkingCopy());
	 		}	 		
	 		refreshStatus();
		} catch (CoreException ce) {
 			DebugUIPlugin.errorDialog(getShell(), "Error", "Exception occurred setting launch configuration", ce.getStatus());
 			clearLaunchConfiguration();
 			return;					
		}
 	} 
 	
 	/**
 	 * Clears the configuration being shown/edited.  
 	 * Removes all tabs from the tab folder.  
 	 * Resets all configuration-related state.
 	 */
 	protected void clearLaunchConfiguration() {
 		setWorkingCopy(null);
 		setWorkingCopyVerifyState(false);
		setChangesAreUserChanges(false);
 		setWorkingCopyUserDirty(false);
 		setLastSavedName(null);
 		getNameTextWidget().setText(""); 
 		disposeExistingTabs();
 		refreshStatus();
 	}
 	
 	/**
 	 * Populate the tabs in the configuration edit area to be appropriate to the current
 	 * launch configuration type.
 	 */
 	protected void setTabsForConfigType(ILaunchConfigurationType configType) {		
		// dispose the current tabs
		disposeExistingTabs();

		// build the new tabs
 		LaunchConfigurationTabExtension[] exts = LaunchConfigurationPresentationManager.getDefault().getTabs(configType);
 		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[exts.length];
 		for (int i = 0; i < exts.length; i++) {
 			TabItem tab = new TabItem(getTabFolder(), SWT.NONE);
 			String name = exts[i].getName();
 			if (name == null) {
 				name = "unspecified";
 			}
 			tab.setText(name);
 			try {
	 			tabs[i] = (ILaunchConfigurationTab)exts[i].getConfigurationElement().createExecutableExtension("class");
 			} catch (CoreException ce) {
	 			DebugUIPlugin.errorDialog(getShell(), "Error", "Exception occurred creating launch configuration tabs",ce.getStatus());
 				return; 				
 			}
 			Control control = tabs[i].createTabControl(this, tab);
 			if (control != null) {
	 			tab.setControl(control);
 			}
 		}
 		setTabs(tabs);	
 	}
 	
 	protected void disposeExistingTabs() {
		TabItem[] oldTabs = getTabFolder().getItems();
		ILaunchConfigurationTab[] tabs = getTabs();
		for (int i = 0; i < oldTabs.length; i++) {
			oldTabs[i].dispose();
			tabs[i].dispose();
		} 		
		setTabs(null);
 	}
 	
 	/**
 	 * Sets the current launch configuration that is being
 	 * displayed/edited.
 	 */
 	protected void setWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
 		fWorkingCopy = workingCopy;
 	}
 	
 	/**
 	 * Returns the current launch configuration that is being
 	 * displayed/edited.
 	 * 
 	 * @return current configuration being displayed
 	 */
 	protected ILaunchConfigurationWorkingCopy getWorkingCopy() {
 		return fWorkingCopy;
 	}
 	
 	protected void setWorkingCopyVerifyState(boolean state) {
 		fWorkingCopyVerifyState = state;
 	}
 	
 	protected boolean isWorkingCopyDirty() {
 		ILaunchConfigurationWorkingCopy workingCopy = getWorkingCopy();
 		if (workingCopy == null) {
 			return false;
 		}
 		return workingCopy.isDirty();
 	}
 	
 	protected boolean getWorkingCopyVerifyState() {
 		return fWorkingCopyVerifyState;
 	}
 	
 	protected void setChangesAreUserChanges(boolean state) {
 		fChangesAreUserChanges = state;
 	}
 	
 	protected boolean areChangesUserChanges() {
 		return fChangesAreUserChanges;
 	}
 	
 	protected void setWorkingCopyUserDirty(boolean dirty) {
 		fWorkingCopyUserDirty = dirty;
 	}
 	
 	protected boolean isWorkingCopyUserDirty() {
 		return fWorkingCopyUserDirty;
 	}
 	
 	protected void setWorkbenchSelection(Object selection) {
 		fWorkbenchSelection = selection;
 	}
 	
 	protected Object getWorkbenchSelection() {
 		return fWorkbenchSelection;
 	}
 	
 	protected void setSelectedTreeObject(Object selection) {
 		fSelectedTreeObject = selection;
 	}
 	
 	protected Object getSelectedTreeObject() {
 		return fSelectedTreeObject;
 	}
 	
 	protected void setInitialMode(String mode) {
 		fInitialMode = mode;
 	}
 	
 	protected String getInitialMode() {
 		return fInitialMode;
 	}
 	
	/**
	 * Sets the text widget used to display the name
	 * of the configuration being displayed/edited
	 * 
	 * @param widget the text widget used to display the name
	 *  of the configuration being displayed/edited
	 */
	private void setNameTextWidget(Text widget) {
		fNameText = widget;
	}
	
	/**
	 * Returns the text widget used to display the name
	 * of the configuration being displayed/edited
	 * 
	 * @return the text widget used to display the name
	 *  of the configuration being displayed/edited
	 */
	protected Text getNameTextWidget() {
		return fNameText;
	} 
	
 	/**
 	 * Sets the 'save' button.
 	 * 
 	 * @param button the 'save' button.
 	 */	
 	private void setSaveButton(Button button) {
 		fSaveButton = button;
 	}
 	
 	/**
 	 * Returns the 'save' button
 	 * 
 	 * @return the 'save' button
 	 */
 	protected Button getSaveButton() {
 		return fSaveButton;
 	}	
 	
 	/**
 	 * Sets the tab folder
 	 * 
 	 * @param folder the tab folder
 	 */	
 	private void setTabFolder(TabFolder folder) {
 		fTabFolder = folder;
 	}
 	
 	/**
 	 * Returns the tab folder
 	 * 
 	 * @return the tab folder
 	 */
 	protected TabFolder getTabFolder() {
 		return fTabFolder;
 	}	 	
 	
 	/**
 	 * Sets the current tab extensions being displayed
 	 * 
 	 * @param tabs the current tab extensions being displayed
 	 */
 	private void setTabs(ILaunchConfigurationTab[] tabs) {
 		fTabs = tabs;
 	}
 	
 	/**
 	 * Returns the current tab extensions being displayed
 	 * 
 	 * @return the current tab extensions being displayed
 	 */
 	protected ILaunchConfigurationTab[] getTabs() {
 		return fTabs;
 	} 	
 	
	/**
	 * @see ILaunchConfigurationListener#launchConfigurationAdded(ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		clearConfigNameCache();
		getTreeViewer().refresh();		
	}

	/**
	 * @see ILaunchConfigurationListener#launchConfigurationChanged(ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		if (areChangesUserChanges()) {
			setWorkingCopyUserDirty(true);
		}
	}

	/**
	 * @see ILaunchConfigurationListener#launchConfigurationRemoved(ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		getTreeViewer().remove(configuration);		
	}
	
	/**
	 * Return whether the current working copy can be replaced with a new working copy.
	 */
	protected boolean canReplaceWorkingCopy() {
		
		// If there is no working copy, there's no problem, return true
		ILaunchConfigurationWorkingCopy workingCopy = getWorkingCopy();
		if (workingCopy == null) {
			return true;
		}
		
		// If the working copy doesn't verify, show user dialog asking if they wish
		// to discard their changes.  Otherwise, if the working copy is dirty,
		// show a slightly different 'save changes' dialog.
		if (isWorkingCopyUserDirty() && !getWorkingCopyVerifyState()) {
			StringBuffer buffer = new StringBuffer("The configuration \"");
			buffer.append(getWorkingCopy().getName());
			buffer.append("\" CANNOT be saved.  Do you wish to discard changes?");
			return MessageDialog.openQuestion(getShell(), "Discard changes?", buffer.toString());
		} else {
			if (isWorkingCopyUserDirty()) {
				return showSaveChangesDialog();
			} else {
				return true;
			}
		}
	}
	
	/**
	 * Show the user a dialog with specified title, message and buttons.  
	 * Return true if the user hit 'CANCEL', false otherwise.
	 */
	protected boolean showSaveChangesDialog() {
		StringBuffer buffer = new StringBuffer("The configuration \"");
		buffer.append(getWorkingCopy().getName());
		buffer.append("\" has unsaved changes.  Do you wish to save them?");
		MessageDialog dialog = new MessageDialog(getShell(), 
												 "Save changes?",
												 null,
												 buffer.toString(),
												 MessageDialog.QUESTION,
												 new String[] {"Yes", "No", "Cancel"},
												 0);
		int selectedButton = dialog.open();
		
		// If the user hit cancel or closed the dialog, return true
		if ((selectedButton < 0) || selectedButton == 2) {
			return false;
		}
		
		// If they hit 'Yes', save the working copy 
		if (selectedButton == 0) {
			handleSavePressed();
		}
		
		return true;
	}

	/**
	 * Notification the 'new' button has been pressed
	 */
	protected void handleNewPressed() {
		
		// Take care of any unsaved changes
		if (!canReplaceWorkingCopy()) {
			return;
		}
		
		constructNewConfig();		
	}	
	
	/** 
	 * If a config type is selected, create a new config of that type initialized to 
	 * fWorkbenchSelection.  If a config is selected, create of new config of the
	 * same type as the selected config.
	 * protected void constructNewConfig() {
	 */
	protected void constructNewConfig() {	
		try {
			ILaunchConfigurationType type = null;

			Object obj = getTreeViewerFirstSelectedElement();
			if (obj instanceof ILaunchConfiguration) {
				type = ((ILaunchConfiguration)obj).getType();
			} else {
				type = (ILaunchConfigurationType)obj;
			}
			setChangesAreUserChanges(false);
			ILaunchConfigurationWorkingCopy wc = type.newInstance(null, DEFAULT_NEW_CONFIG_NAME);
			Object workbenchSelection = getWorkbenchSelection();
			if (workbenchSelection != null) {
				wc.initializeDefaults(workbenchSelection);
			}
			setLastSavedName(null);
			setLaunchConfiguration(wc);
		} catch (CoreException ce) {
			DebugUIPlugin.errorDialog(getShell(), "Error", "Exception creating new launch configuration.", ce.getStatus());
 			clearLaunchConfiguration();
 			return;			
		}		
	}
	
	/**
	 * Notification the 'delete' button has been pressed
	 */
	protected void handleDeletePressed() {
		try {
			Object firstElement = getTreeViewerFirstSelectedElement();
			if (firstElement instanceof ILaunchConfiguration) {
				ILaunchConfiguration config = (ILaunchConfiguration) firstElement;
				clearLaunchConfiguration();
				config.delete();
			}
		} catch (CoreException ce) {
		}
	}	
	
	/**
	 * Notification the 'copy' button has been pressed
	 */
	protected void handleCopyPressed() {
		Object selectedElement = getTreeViewerFirstSelectedElement();
		if (selectedElement instanceof ILaunchConfiguration) {
			ILaunchConfiguration selectedConfig = (ILaunchConfiguration) selectedElement;
			String newName = generateNewNameFrom(selectedConfig.getName());
			try {
				ILaunchConfigurationWorkingCopy newWorkingCopy = selectedConfig.copy(newName);
				ILaunchConfigurationType configType = newWorkingCopy.getType();
				
				IStructuredSelection selection = new StructuredSelection(configType);
				setTreeViewerSelection(selection);
				setLaunchConfiguration(newWorkingCopy);
			} catch (CoreException ce) {				
			}			
		}
	}	
	
	/**
	 * Construct a new config name using the name of the given config as a starting point.
	 * The new name is guaranteed not to collide with any existing config name.
	 */
	protected String generateNewNameFrom(String startingName) {
		String newName = startingName;
		int index = 1;
		while (configExists(newName)) {
			StringBuffer buffer = new StringBuffer(startingName);
			buffer.append(" (copy#");
			buffer.append(String.valueOf(index));
			buffer.append(')');	
			index++;
			newName = buffer.toString();		
		}		
		return newName;
	}
	
	/**
	 * Notification the 'save & launch' button has been pressed
	 */
	protected void handleSaveAndLaunchPressed() {
		handleSavePressed();
		handleLaunchPressed();
	}
	
	/**
	 * Notification that the 'save' button has been pressed
	 */
	protected void handleSavePressed() {
		ILaunchConfiguration config = null;
		try {
			config = getWorkingCopy().doSave();
		} catch (CoreException ce) {			
		}	
		setLastSavedName(getWorkingCopy().getName());	
		setWorkingCopyUserDirty(false);
		
		setEnableStateEditButtons();

		//IStructuredSelection selection = new StructuredSelection(config);
		//setTreeViewerSelection(selection);
	}
	
	/**
	 * Notification the 'launch' button has been pressed
	 */
	protected void handleLaunchPressed() {
		try {
			getWorkingCopy().launch(getMode());
		} catch (CoreException ce) {
		}		
		close();
	}
}


