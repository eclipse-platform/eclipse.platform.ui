package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
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
	
	/**
	 * The starting mode, as specified by the caller
	 */
	private String fInitialMode;
	
	/**
	 * The (initial) selection for the launch configuration
	 * tree.
	 */
	private ISelection fSelection = new StructuredSelection();
	
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
	
	private Button fRadioRunButton;
	private Button fRadioDebugButton;
	
	private Label fMessageLabel;
	
	private Label fModeLabel;
	
	/**
	 * The text widget displaying the name of the
	 * lanuch configuration under edit
	 */
	private Text fNameText;	
	
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
	
	/**
	 * The launch configuration type for which the tabs are currently set up
	 */
	private ILaunchConfigurationType fWorkingType;
	
	/**
	 * The current tab extensions being displayed
	 */
	private ILaunchConfigurationTab[] fTabs;
	
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
	
	protected static final String LAUNCH_STATUS_OK_MESSAGE = "Ready to launch";
	
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
		fInitialMode = mode;
	}
	
	protected void resolveWorkbenchSelection(IStructuredSelection selection) {
		fWorkbenchSelection = null;
		if ((selection == null) || (selection.size() < 1)) {
			return;
		}
		fWorkbenchSelection = selection.getFirstElement();
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

		// Build the launch configuration banner area
		// and put it into the composite.
		Composite launchConfigBannerArea = createLaunchConfigurationBannerArea(composite);		

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
	 * config tree.
	 */
	protected void initializeFirstConfig() {
		if (fWorkbenchSelection == null) {
			return;
		}
		
		Object selectee = null;
		if (fWorkbenchSelection instanceof ILaunchConfiguration) {
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
		IStructuredSelection selection = new StructuredSelection(fWorkbenchSelection);
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
			return;
		}
		
		ILaunchConfigurationWorkingCopy workingCopy = null;
		try {
			workingCopy = configType.newInstance(null, DEFAULT_NEW_CONFIG_NAME);
			workingCopy.initializeDefaults(fWorkbenchSelection);
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
		if (fWorkbenchSelection instanceof IResource) {
			project = ((IResource)fWorkbenchSelection).getProject();
		} else if (fWorkbenchSelection instanceof IAdaptable) {
			IResource resource = (IResource) ((IAdaptable)fWorkbenchSelection).getAdapter(IResource.class);
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
		if (fInitialMode.equals(ILaunchManager.DEBUG_MODE)) {
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
	
	/**
	 * If the verify fails, we catch a <code>CoreException</code>, in which
	 * case we extract the error message and set it in the status area.
	 * Otherwise, we set the "OK to launch" message in the status area.
	 * 
	 * @see ILaunchConfigurationDialog#refreshStatus()
	 */
	public void refreshStatus() {
		try {
			getWorkingCopy().verify(getMode());
		} catch (CoreException ce) {
			String message = ce.getStatus().getMessage();
			setStatusErrorMessage(message);
			return;
		}
		setStatusOKMessage();
	}
	
	protected void setStatusErrorMessage(String message) {
		setStatusMessage(message);
		getMessageLabel().setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
	}
	
	protected void setStatusOKMessage() {
		setStatusMessage(LAUNCH_STATUS_OK_MESSAGE);
		getMessageLabel().setForeground(getDisplay().getSystemColor(SWT.COLOR_GREEN));		
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
		c.setLayout(layout);
		
		GridData gd;
		
		TreeViewer tree = new TreeViewer(c);
		gd = new GridData(GridData.FILL_VERTICAL);
		gd.horizontalSpan = 3;
		gd.widthHint = 175;
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
		
		Button removeButton = new Button(c, SWT.PUSH | SWT.CENTER);
		removeButton.setText("&Delete");
		gd = new GridData(GridData.CENTER);
		gd.horizontalSpan = 1;
		removeButton.setLayoutData(gd);
		setDeleteButton(removeButton);
		
		removeButton.addSelectionListener(
			new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent event) {
					handleRemovePressed();
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
		
		nameText.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getWorkingCopy().rename(getNameTextWidget().getText());
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
		gd.heightHint = 300;
		gd.widthHint = 350;
		tabFolder.setLayoutData(gd);
		setTabFolder(tabFolder);
		
		Button saveButton = new Button(c, SWT.PUSH | SWT.CENTER);
		saveButton.setText("&Save");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 2;
		saveButton.setLayoutData(gd);
		setSaveButton(saveButton);
		
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
		topLayout.numColumns = 3;
		comp.setLayout(topLayout);
		
		fModeLabel = new Label(comp, SWT.NONE);		
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		comp.setLayoutData(gd);
		
		Composite radioComposite = new Composite(comp, SWT.NONE);
		GridLayout radioLayout = new GridLayout();
		radioLayout.numColumns = 1;
		radioComposite.setLayout(radioLayout);
		gd = new GridData(GridData.FILL_BOTH);
		radioComposite.setLayoutData(gd);
		
		setRadioRunButton(new Button(radioComposite, SWT.RADIO));
		getRadioRunButton().setText("Run");
		setRadioDebugButton(new Button(radioComposite, SWT.RADIO));
		getRadioDebugButton().setText("Debug");
		getRadioDebugButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				setModeLabelState();
			}
		});
		
		setMessageLabel(new Label(comp, SWT.NONE));
		getMessageLabel().setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));
		getMessageLabel().setText("User message goes here");
		getMessageLabel().setAlignment(SWT.RIGHT);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		getMessageLabel().setLayoutData(gd);
		
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
		
	/**
	 * Content prodiver for launch configuration tree
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
					DebugUIPlugin.errorDialog(getShell(), "Error", "An exception occurred while retrieving lanuch configurations.", e.getStatus());
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
				try {
					return ((ILaunchConfiguration)element).getType();
				} catch (CoreException e) {
					DebugUIPlugin.errorDialog(getShell(), "Error", "An exception occurred while retrieving lanuch configurations.", e.getStatus());
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

		/*
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
	 * Sets the specified selection in the launch configuration
	 * selection tree.
	 * 
	 * @param selection the items to select
	 */
	public void setSelection(ISelection selection) {
		fSelection = selection;
		if (isVisible()) {
			getTreeViewer().setSelection(selection);
		}
	}
	
	/**
	 * Returns the current selection in the launch configuration
	 * tree (or what should be initially selected on startup
	 * 
	 * @return selection
	 */
	protected ISelection getSelection() {
		return fSelection;
	}
	
	/**
	 * Notification selection has changed in the launch configuration
	 * tree. 
	 * <p>
	 * If the currently displayed configuration is not saved,
	 * prompt for saving before moving on to the new selection.
	 * </p>
	 * 
	 * @param event selection changed event
	 */
 	public void selectionChanged(SelectionChangedEvent event) {
 		IStructuredSelection selection = (IStructuredSelection)event.getSelection();
 		fSelection = selection;
 		
 		// ToDo: prompt for save of current configuration 			
 		
 		// enable buttons
 		boolean singleSelection = selection.size() == 1;
 		
		getLaunchButton().setEnabled(singleSelection);
 		getSaveAndLaunchButton().setEnabled(singleSelection);
 		getNewButton().setEnabled(singleSelection);
 		getCopyButton().setEnabled(singleSelection);

 		getDeleteButton().setEnabled(selection.size() >= 1);
 		
 		getSaveButton().setEnabled(false);
 		
 		if (singleSelection && selection.getFirstElement() instanceof ILaunchConfiguration) {
 			ILaunchConfiguration selectedConfig = (ILaunchConfiguration)selection.getFirstElement();
 			setLaunchConfiguration(selectedConfig);
 		} else if (singleSelection && selection.getFirstElement() instanceof ILaunchConfigurationType) {
 			//setTabsForConfigType((ILaunchConfigurationType)selection.getFirstElement());
			handleNewPressed();
 		} else {
 			// multi-selection
 		}

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
 	 * Sets the 'lanuch' button.
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
 	 * Sets the configuration to display/edit.
 	 * Updates the tab folder to the appropriate
 	 * pages.
 	 * 
 	 * @param config the launch configuration to display/edit
 	 */
 	protected void setLaunchConfiguration(ILaunchConfiguration config) {
		try {
			setTabsForConfigType(config.getType());
			
			if (config.isWorkingCopy()) {
		 		fWorkingCopy = (ILaunchConfigurationWorkingCopy)config;
			} else {
				fWorkingCopy = config.getWorkingCopy();
			}
	 		// update the name field
	 		getNameTextWidget().setText(config.getName());
	 		
	 		// update the tabs with the new working copy
	 		ILaunchConfigurationTab[] tabs = getTabs();
	 		for (int i = 0; i < tabs.length; i++) {
				tabs[i].setLaunchConfiguration(fWorkingCopy);
	 		}	 		
	 		refreshStatus();
		} catch (CoreException ce) {
 			DebugUIPlugin.errorDialog(getShell(), "Error", "Exception occurred setting launch configuration", ce.getStatus());
 			return;					
		}
 	} 
 	
 	/**
 	 * Populate the tabs in the configuration edit area to be appropriate to the current
 	 * launch configuration type.
 	 */
 	protected void setTabsForConfigType(ILaunchConfigurationType configType) {		
		// only do work if the config type has changed
		if (configType.equals(fWorkingType)) {
			return;
		}
						
		// dispose the current tabs and replace with new ones 
		TabItem[] oldTabs = getTabFolder().getItems();
		ILaunchConfigurationTab[] tabs = getTabs();
		for (int i = 0; i < oldTabs.length; i++) {
			oldTabs[i].dispose();
			tabs[i].dispose();
		}
		
		// build the new tabs
 		LaunchConfigurationTabExtension[] exts = LaunchConfigurationPresentationManager.getDefault().getTabs(configType);
 		tabs = new ILaunchConfigurationTab[exts.length];
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
 		fWorkingType = configType;	
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
		getTreeViewer().refresh();
	}

	/**
	 * @see ILaunchConfigurationListener#launchConfigurationChanged(ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		if (configuration.isWorkingCopy()) {
			if (getWorkingCopy().equals(configuration)) {
				workingCopyChanged();
			}
		}
	}

	/**
	 * @see ILaunchConfigurationListener#launchConfigurationRemoved(ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		getTreeViewer().refresh();
	}

	/**
	 * Notification that the current working copy being
	 * displayed/edited has changed in some way. Update
	 * buttons.
	 */
	protected void workingCopyChanged() {
		ILaunchConfigurationWorkingCopy wc = getWorkingCopy();
		
		getSaveButton().setEnabled(wc.isDirty());
		getSaveAndLaunchButton().setEnabled(wc.isDirty());
	}

	/**
	 * Notification the 'new' button has been pressed
	 */
	protected void handleNewPressed() {
		
		// ToDo: prompt for save of current working copy
		
		// If a config type is selected, create a new config of that type initialized to 
		// fWorkbenchSelection.  If a config is selected, create of new config of the
		// same type as the selected config
		try {
			ILaunchConfigurationType type = null;
			IStructuredSelection sel = (IStructuredSelection)getSelection();
			Object obj = sel.getFirstElement();
			if (obj instanceof ILaunchConfiguration) {
				type = ((ILaunchConfiguration)obj).getType();
			} else {
				type = (ILaunchConfigurationType)obj;
			}
			ILaunchConfigurationWorkingCopy wc = type.newInstance(null, DEFAULT_NEW_CONFIG_NAME);
			if (fWorkbenchSelection != null) {
				wc.initializeDefaults(fWorkbenchSelection);
			}
			setLaunchConfiguration(wc);
		} catch (CoreException ce) {
			DebugUIPlugin.errorDialog(getShell(), "Error", "Exception occurred creating new launch configuration.", ce.getStatus());
 			return;			
		}
	}	
	
	/**
	 * Notification the 'remove' button has been pressed
	 */
	protected void handleRemovePressed() {
	}	
	
	/**
	 * Notification the 'copy' button has been pressed
	 */
	protected void handleCopyPressed() {
	}	
	
	/**
	 * Notification the 'save & launch' button has been pressed
	 */
	protected void handleSaveAndLaunchPressed() {
		try {
			getWorkingCopy().doSave();
		} catch (CoreException ce) {			
		}
		handleLaunchPressed();
	}
	
	/**
	 * Notification the 'launch' button has been pressed
	 */
	protected void handleLaunchPressed() {
		ILaunch launch = null;
		try {
			launch = getWorkingCopy().launch(getMode());
		} catch (CoreException ce) {
			return;
		}
		
		if (launch != null) {
			getLaunchManager().registerLaunch(launch);
		}
	}
}


