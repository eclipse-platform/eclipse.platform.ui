package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.PixelConverter;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * The dialog used to edit and launch launch configurations.
 */
public class LaunchConfigurationDialog extends TitleAreaDialog 
										implements ISelectionChangedListener, 
													ILaunchConfigurationListener, 
													ILaunchConfigurationDialog, 
													IDoubleClickListener {

	/**
	 * The tree of launch configurations
	 */
	private TreeViewer fConfigTree;
	
	/**
	 * The workbench context present when this dialog is opened.
	 */
	private Object fContext;
	
	/**
	 * The IResource corresponding to <code>fContext</code>.
	 */
	private IResource fResourceContext;
	
	/**
	 * The mode (run or debug) for this dialog.
	 */
	private String fMode;
	
	/**
	 * The Composite used to insert an adjustable 'sash' between the tree and the tabs.
	 */
	private SashForm fSashForm;
	
	/**
	 * Default weights for the SashForm that specify how wide the selection and
	 * edit areas aree relative to each other.
	 */
	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] {11, 30};
	
	/**
	 * The launch configuration selection area.
	 */
	private Composite fSelectionArea;	
	
	/**
	 * The launch configuration edit area.
	 */
	private Composite fEditArea;
	
	/**
	 * The 'New configuration' action.
	 */
	private ButtonAction fButtonActionNew;
	
	/**
	 * The 'Duplicate configuration' action.
	 */
	private ButtonAction fButtonActionDuplicate;
	
	/**
	 * The 'Delete configuration' action.
	 */
	private ButtonAction fButtonActionDelete;
	
	/**
	 * The 'apply' button
	 */
	private Button fApplyButton;	
	
	/**
	 * The 'revert' button
	 */
	private Button fRevertButton;
	
	/**
	 * The 'cancel' button that appears when the in-dialog progress monitor is shown.
	 */
	private Button fProgressMonitorCancelButton;
	
	/**
	 * Flag indicating if the progress monitor part's Cancel button has been pressed.
	 */
	private boolean fCancelButtonPressed;
	
	/**
	 * The text widget displaying the name of the
	 * launch configuration under edit
	 */
	private Text fNameText;
	
	private String fLastSavedName = null;
	
	/**
	 * Container for the edit area <code>TabFolder</code>
	 */
	private Composite fTabComposite;
	
	/**
	 * The tab folder that contains tabs for the selected configuration
	 */
	private TabFolder fTabFolder;
	
	/**
	 * The current (working copy) launch configuration
	 * being displayed/edited or <code>null</code> if
	 * none
	 */
	private ILaunchConfigurationWorkingCopy fWorkingCopy;
	
	/**
	 * The actual (non-working copy) launch configuration that underlies the current working copy
	 */
	private ILaunchConfiguration fUnderlyingConfig;
	
	/**
	 * Clients of this dialog may set an 'initial configuration type', which means that when
	 * the dialog is opened, a configuration of that type will be created, initialized, and
	 * saved.  Note that the initial config type is ignored if single-click launching is enabled.
	 */
	private ILaunchConfigurationType fInitialConfigType;
	
	/**
	 * When this dialog is opened in <code>LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_SELECTION</code>
	 * mode, this specifies the selection that is initially shown in the dialog.
	 */
	private IStructuredSelection fInitialSelection;
	
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
	 * The index of the currently selected tab
	 */
	private int fCurrentTabIndex;
	
	private ProgressMonitorPart fProgressMonitorPart;
	private Cursor waitCursor;
	private Cursor arrowCursor;
	private MessageDialog fWindowClosingDialog;
	
	/**
	 * Whether initlialing tabs
	 */
	private boolean fInitializingTabs = false;
		
	/**
	 * Indicates if selection changes in the tree should be ignored
	 */
	private boolean fIgnoreSelectionChanges = false;
	
	/**
	 * Previously selected element in the tree
	 */
	private Object fSelectedTreeObject;
	
	/**
	 * The number of 'long-running' operations currently taking place in this dialog
	 */	
	private long fActiveRunningOperations = 0;
		
	/**
	 * Id for 'Launch' button.
	 */
	protected static final int ID_LAUNCH_BUTTON = IDialogConstants.CLIENT_ID + 1;
	
	/**
	 * Id for 'Close' button.
	 */
	protected static final int ID_CLOSE_BUTTON = IDialogConstants.CLIENT_ID + 2;
	
	/**
	 * Id for 'Cancel' button.
	 */
	protected static final int ID_CANCEL_BUTTON = IDialogConstants.CLIENT_ID + 3;
	
	/**
	 * Constrant String used as key for setting and retrieving current Control with focus
	 */
	private static final String FOCUS_CONTROL = "focusControl";//$NON-NLS-1$

	/**
	 * The height in pixels of this dialog's progress indicator
	 */
	private static int PROGRESS_INDICATOR_HEIGHT = 18;

	/**
	 * Constant specifying how wide this dialog is allowed to get (as a percentage of
	 * total available screen width) as a result of tab labels in the edit area.
	 */
	private static final float MAX_DIALOG_WIDTH_PERCENT = 0.75f;

	/**
	 * Empty array
	 */
	protected static final Object[] EMPTY_ARRAY = new Object[0];	
	
	protected static final String DEFAULT_NEW_CONFIG_NAME = LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.New_configuration_1"); //$NON-NLS-1$
	
	/**
	 * Size of this dialog if there is no preference specifying a size.
	 */
	protected static final Point DEFAULT_INITIAL_DIALOG_SIZE = new Point(620, 560);

	/**
	 * Status area messages
	 */
	protected static final String LAUNCH_STATUS_OK_MESSAGE = LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Ready_to_launch_2"); //$NON-NLS-1$
	protected static final String LAUNCH_STATUS_STARTING_FROM_SCRATCH_MESSAGE 
										= LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Select_a_configuration_to_launch_or_a_config_type_to_create_a_new_configuration_3"); //$NON-NLS-1$

	private String fCantSaveErrorMessage;

	/**
	 * Constant specifying that the launch configuration dialog should not actually open,
	 * but instead should attempt to re-launch the last configuration that was sucessfully
	 * launched in the workspace.  If there is no last launched configuration, just open the dialog.
	 */
	public static final int LAUNCH_CONFIGURATION_DIALOG_LAUNCH_LAST = 0;
	
	/**
	 * Constant specifying that this dialog should be opened with a new configuration of a type
	 * specified via <code>setInitialConfigType()</code> selected.
	 */
	public static final int LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_NEW_CONFIG_OF_TYPE = 1;
	
	/**
	 * Constant specifying that this dialog should be opened with the last configuration launched
	 * in the workspace selected.
	 */
	public static final int LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_LAST_LAUNCHED = 2;

	/**
	 * Constant specifying that this dialog should be opened with the value specified via 
	 * <code>setInitialSelection()</code> selected.
	 */
	public static final int LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_SELECTION = 3;
	
	/**
	 * Specifies how this dialog behaves when opened.  Value is one of the 
	 * 'LAUNCH_CONFIGURATION_DIALOG' constants defined in this class.
	 */
	private int fOpenMode = LAUNCH_CONFIGURATION_DIALOG_LAUNCH_LAST;
	
	/**
	 * Constructs a new launch configuration dialog on the given
	 * parent shell.
	 * 
	 * @param shell the parent shell
	 * @param selection the selection used to initialize this dialog, typically the 
	 *  current workbench selection
	 * @param mode one of <code>ILaunchManager.RUN_MODE</code> or 
	 *  <code>ILaunchManager.DEBUG_MODE</code>
	 */
	public LaunchConfigurationDialog(Shell shell, IStructuredSelection selection, String mode) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setContext(resolveContext(selection));
		setMode(mode);
	}
	
	/**
	 * Set the flag indicating how this dialog behaves when the <code>open()</code> method is called.
	 * Valid values are defined by the LAUNCH_CONFIGURATION_DIALOG... constants in this class.
	 */
	public void setOpenMode(int mode) {
		fOpenMode = mode;
	}
	
	protected int getOpenMode() {
		return fOpenMode;
	}
	
	/**
	 * Returns the Object to be used as context for this dialog, derived from the specified selection.
	 * If the specified selection has as its first element an IFile whose extension matches
	 * <code>ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION</code>, then return
	 * the launch configuration declared in the IFile.  Otherwise, return the first element 
	 * in the specified selection.
	 */
	protected Object resolveContext(IStructuredSelection selection) {
		
		// Empty selection means no context
		if ((selection == null) || (selection.isEmpty())) {
			return null;
		} 

		// If first element is a launch config file, create a launch configuration from it
		// and make this the context, otherwise just return the first element
		Object firstSelected = selection.getFirstElement();
		if (firstSelected instanceof IFile) {
			IFile file = (IFile) firstSelected;
			if (file.getFileExtension().equals(ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION)) {
				return getLaunchManager().getLaunchConfiguration(file);
			}
		}
		return firstSelected;
	}
	
	/**
	 * A launch configuration dialog overrides this method
	 * to create a custom set of buttons in the button bar.
	 * This dialog has 'Launch' and 'Cancel'
	 * buttons.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ID_LAUNCH_BUTTON, getLaunchButtonText(), true);
		createButton(parent, ID_CLOSE_BUTTON, LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Close_1"), false);  //$NON-NLS-1$
	}
	
	/**
	 * Handle the 'save and launch' & 'launch' buttons here, all others are handled
	 * in <code>Dialog</code>
	 * 
	 * @see Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == ID_LAUNCH_BUTTON) {
			handleLaunchPressed();
		} else if (buttonId == ID_CLOSE_BUTTON) {
			handleClosePressed();
		} else {
			super.buttonPressed(buttonId);
		}
	}

	/**
	 * Returns the appropriate text for the launch button - run or debug.
	 */
	protected String getLaunchButtonText() {
		if (getMode() == ILaunchManager.DEBUG_MODE) {
			return LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Deb&ug_4"); //$NON-NLS-1$
		} else {
			return LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.R&un_5"); //$NON-NLS-1$
		}
	}

	/**
	 * @see Dialog#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		initializeSashForm();
		createContextMenu(getTreeViewer().getControl());
		getLaunchManager().addLaunchConfigurationListener(this);
		initializeBounds();
		doInitialTreeSelection();
		return contents;
	}

	/**
	 * Initialize the relative weights (widths) of the 2 sides of the sash.
	 */
	protected void initializeSashForm() {
		int[] sashWeights = DEFAULT_SASH_WEIGHTS;
		String sashWeightString = getPreferenceStore().getString(IDebugPreferenceConstants.PREF_LAUNCH_CONFIGURATION_DIALOG_SASH_WEIGHTS);
		if (sashWeightString.length() > 0) {
			Point sashWeightPoint = parseCoordinates(sashWeightString);
			if (sashWeightPoint != null) {
				sashWeights[0] = sashWeightPoint.x;
				sashWeights[1] = sashWeightPoint.y;
			}
		}
		getSashForm().setWeights(sashWeights);
	}
	
	/**
	 * Creates a pop-up menu on the specified control.
	 */
	protected void createContextMenu(Control menuControl) {
		MenuManager menuMgr= new MenuManager("#PopUp"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager mgr) {
				fillContextMenu(mgr);
			}
		});
		Menu menu= menuMgr.createContextMenu(menuControl);
		menuControl.setMenu(menu);
	}
	
	/**
	 * Fill the specified context menu with the basic launch configuration
	 * management actions - New, Duplicate & Delete.
	 */
	protected void fillContextMenu(IMenuManager menu) {	
		if (getButtonActionNew().isEnabled()) {
			menu.add(getButtonActionNew());
		}
		if (getButtonActionDuplicate().isEnabled()) {
			menu.add(getButtonActionDuplicate());
		}
		if (getButtonActionDelete().isEnabled()) {
			menu.add(getButtonActionDelete());
		}
	}	
	
	/**
	 * Set the initial selection in the tree.
	 */
	protected void doInitialTreeSelection() {
		getTreeViewer().setSelection(getInitialSelection());
	}
	
	/**
	 * Write out this dialog's Shell size, location & sash weights to the preference store.
	 */
	protected void persistShellGeometry() {
		Point shellLocation = getShell().getLocation();
		Point shellSize = getShell().getSize();
		int[] sashWeights = getSashForm().getWeights();
		String locationString = serializeCoords(shellLocation);
		String sizeString = serializeCoords(shellSize);
		String sashWeightString = serializeCoords(new Point(sashWeights[0], sashWeights[1]));
		getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_LAUNCH_CONFIGURATION_DIALOG_LOCATION, locationString);
		getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_LAUNCH_CONFIGURATION_DIALOG_SIZE, sizeString);
		getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_LAUNCH_CONFIGURATION_DIALOG_SASH_WEIGHTS, sashWeightString);
	}
	
	/**
	 * @see Window#close()
	 */
	public boolean close() {
		getLaunchManager().removeLaunchConfigurationListener(this);
		persistShellGeometry();
		return super.close();
	}
	
	/**
	 * Determine the first configuration for this dialog.  If single-click launching is 
	 * enabled, launch the configuration WITHOUT realizing the dialog.  If single-click 
	 * launching was successful, this method returns 
	 * <code>ILaunchConfigurationDialog.SINGLE_CLICK_LAUNCHED</code>.  Otherwise, open the
	 * dialog in the specified mode.
	 * 
	 * @see Window#open()
	 */
	public int open() {		
		int mode = getOpenMode();	
		if (mode == LAUNCH_CONFIGURATION_DIALOG_LAUNCH_LAST) {
			return doLastLaunchedConfig(true);
		} else if (mode == LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_LAST_LAUNCHED) {
			return doLastLaunchedConfig(false);
		} else if (mode == LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_NEW_CONFIG_OF_TYPE) {
			return openDialogOnNewConfigOfSpecifiedType();
		} else if (mode == LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_SELECTION) {
			return openDialogOnSelection();
		}		
		return super.open();
	}
	
	/**
	 * Retrieve the last launched configuration in the workspace.  If <code>launch</code>
	 * is <code>true</code>, launch this configuration without showing the dialog, otherwise 
	 * just set the initial selection in the dialog to the last launched configuration.
	 */
	protected int doLastLaunchedConfig(boolean launch) {
		ILaunchConfiguration lastLaunchedConfig = getLastLaunchedWorkbenchConfiguration();
		if (launch) {
			try {
				if (lastLaunchedConfig != null) {
					if (lastLaunchedConfig.supportsMode(getMode())) {
						fUnderlyingConfig = lastLaunchedConfig;
						doLaunch(lastLaunchedConfig);
					} else {
						// If we're trying to launch, but the last launched config doesn't 
						// support the current mode of the dialog, show an error dialog
						String configName = lastLaunchedConfig.getName();
						String title = LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Cannot_relaunch_1"); //$NON-NLS-1$
						String message = MessageFormat.format(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Cannot_relaunch_[{1}]_because_it_does_not_support_{2}_mode_2"), new String[] {configName, getMode()}); //$NON-NLS-1$
						MessageDialog.openError(getShell(), title, message);										
					}
					return ILaunchConfigurationDialog.LAUNCHED_BEFORE_OPENING;
				}
			} catch(CoreException e) {
				DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Launch_Configuration_Error_6"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Exception_occurred_processing_launch_configuration._See_log_for_more_information_7"), e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		if (lastLaunchedConfig != null) {
			setInitialSelection(new StructuredSelection(lastLaunchedConfig));
		}			
		return super.open();
	}
	
	/**
	 * Realize this dialog so that a new configuration of the type that was specified via
	 * <code>setInitialConfigType()</code> is selected.
	 */
	protected int openDialogOnNewConfigOfSpecifiedType() {
		ILaunchConfigurationType configType = getInitialConfigType();
		ILaunchConfiguration config = null;
		if (configType != null) {
			config = createConfigOfType(configType);			
		}		
		if (config != null) {
			setInitialSelection(new StructuredSelection(config));
		}
		return super.open();
	}
	
	/**
	 * Open this dialog with the selection set to the value specified by 
	 * <code>setInitialSelection()</code>.
	 */
	protected int openDialogOnSelection() {
		// Nothing special is required, the dialog will open and whatever was specified
		// via setInitialSelection() will be selected in the tree
		return super.open();
	}
	
	/**
	 * Return the last launched configuration in the workspace.
	 */
	protected ILaunchConfiguration getLastLaunchedWorkbenchConfiguration() {
		LaunchConfigurationHistoryElement historyElement = DebugUIPlugin.getLaunchConfigurationManager().getLastLaunch();
		if (historyElement != null) {
			return historyElement.getLaunchConfiguration();
		}
		return null;			
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		GridData gd;
		Composite dialogComp = (Composite)super.createDialogArea(parent);
		Composite topComp = new Composite(dialogComp, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		topComp.setLayoutData(gd);
		GridLayout topLayout = new GridLayout();
		topLayout.numColumns = 2;
		topLayout.marginHeight = 5;
		topLayout.marginWidth = 0;
		topComp.setLayout(topLayout);

		// Set the things that TitleAreaDialog takes care of
		setTitle(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Create,_manage,_and_run_launch_configurations_8")); //$NON-NLS-1$
		setMessage(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Ready_to_launch_2")); //$NON-NLS-1$
		setModeLabelState();

		// Create the SashForm that contains the selection area on the left,
		// and the edit area on the right
		setSashForm(new SashForm(topComp, SWT.NONE));
		getSashForm().setOrientation(SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		getSashForm().setLayoutData(gd);
		
		// Build the launch configuration selection area and put it into the composite.
		Composite launchConfigSelectionArea = createLaunchConfigurationSelectionArea(getSashForm());
		gd = new GridData(GridData.FILL_VERTICAL);
		launchConfigSelectionArea.setLayoutData(gd);
	
		// Build the launch configuration edit area and put it into the composite.
		Composite editAreaComp = createLaunchConfigurationEditArea(getSashForm());
		gd = new GridData(GridData.FILL_BOTH);
		editAreaComp.setLayoutData(gd);
			
		// Build the separator line that demarcates the button bar
		Label separator = new Label(topComp, SWT.HORIZONTAL | SWT.SEPARATOR);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);
		
		dialogComp.layout(true);
		
		return dialogComp;
	}
	
	/**
	 * Create and return a launch configuration of the specified type.
	 * This method is intended to be called before the UI has been realized, such as in
	 * the case of single-click launching or creating a config for an initial configuration
	 * type.
	 */
	protected ILaunchConfiguration createConfigOfType(ILaunchConfigurationType configType) {		
		ILaunchConfigurationWorkingCopy workingCopy = null;
		try {
			workingCopy = configType.newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(DEFAULT_NEW_CONFIG_NAME));
		} catch (CoreException ce) {
			DebugUIPlugin.log(ce);
			return null;
		}

		ILaunchConfiguration config = null;
		try {
	 		ILaunchConfigurationTabGroup group= createGroup(configType);
	 		group.setDefaults(workingCopy);
	 		group.dispose();

 			// Assign a name to the config if it doesn't already have one
	 		if (workingCopy.getName().trim().length() == 0) {
	 			IResource res = getResourceContext();
	 			String name = "";       //$NON-NLS-1$
	 			if (res != null) {
	 				name = res.getName();
	 			}
	 			name = generateName(name);
	 			workingCopy.rename(name);
	 		}
	 		
	 		config = workingCopy.doSave();
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
			return null;
		}
		
		return config;
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
					tabGroup = LaunchConfigurationPresentationManager.getDefault().getTabGroup(configType);
					finalArray[0] = tabGroup;
				} catch (CoreException ce) {
					finalArray[1] = ce;
					return;
				}
		 		tabGroup.createTabs(LaunchConfigurationDialog.this, getMode());
		 		ILaunchConfigurationTab[] tabs = tabGroup.getTabs();
		 		for (int i = 0; i < tabs.length; i++) {
		 			tabs[i].setLaunchConfigurationDialog(LaunchConfigurationDialog.this);
		 		}
 			}
 		};
 		
 		// Creating the tabs can result in plugin loading, so we show the busy cursor
 		BusyIndicator.showWhile(getDisplay(), runnable);
 		
 		// Re-throw any CoreException if there was one
 		if (finalArray[1] != null) {
 			throw (CoreException)finalArray[1];
 		}
 		
 		// Otherwise return the tab group
 		return (ILaunchConfigurationTabGroup)finalArray[0];
	}
	
	/**
	 * Returns the selected IResource context from the workbench,
	 * or <code>null</code> if there was no context in the workbench.
	 */
	protected IResource getResourceContext() {
		if (fResourceContext == null) {
			Object workbenchSelection = getContext();
			if (workbenchSelection instanceof IResource) {
				fResourceContext = (IResource)workbenchSelection;
			} else if (workbenchSelection instanceof IAdaptable) {
				fResourceContext = (IResource) ((IAdaptable)workbenchSelection).getAdapter(IResource.class);
			}
		}
		return fResourceContext;		
	}
	
	/**
	 * Set the title area image based on the mode this dialog was initialized with
	 */
	protected void setModeLabelState() {
		Image image;
		if (getMode().equals(ILaunchManager.DEBUG_MODE)) {
			image = DebugUITools.getImage(IDebugUIConstants.IMG_WIZBAN_DEBUG);
		} else {
			image = DebugUITools.getImage(IDebugUIConstants.IMG_WIZBAN_RUN);
		}
		setTitleImage(image);
	}
	
	/**
	 * Convenience method to set the selection on the configuration tree.
	 */
	protected void setTreeViewerSelection(ISelection selection) {
		getTreeViewer().setSelection(selection);
	}
	
	private void setLastSavedName(String lastSavedName) {
		this.fLastSavedName = lastSavedName;
	}

	private String getLastSavedName() {
		return fLastSavedName;
	}
	
	/**
	 * Update buttons and message.
	 */
	protected void refreshStatus() {
		updateButtons();
		updateMessage();
	}
	
	/**
	 * Verify the attributes common to all launch configuration.
	 * Indicate failure by throwing a <code>CoreException</code>.
	 */
	protected void verifyStandardAttributes() throws CoreException {
		verifyName();
	}
	
	/**
	 * Verify that the launch configuration name is valid.
	 */
	protected void verifyName() throws CoreException {
		String currentName = getNameTextWidget().getText().trim();

		// If there is no name, complain
		if (currentName.length() < 1) {
			throw new CoreException(new Status(IStatus.ERROR,
												 DebugUIPlugin.getUniqueIdentifier(),
												 0,
												 LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Name_required_for_launch_configuration_11"), //$NON-NLS-1$
												 null));			
		}

		// If the name hasn't changed from the last saved name, do nothing
		if (currentName.equals(getLastSavedName())) {
			return;
		}	
		
		// See if name contains any 'illegal' characters
		IStatus status = ResourcesPlugin.getWorkspace().validateName(currentName, IResource.FILE);
		if (status.getCode() != IStatus.OK) {
			throw new CoreException(new Status(IStatus.ERROR,
												 DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
												 0,
												 status.getMessage(),
												 null));									
		}			
		
		// Otherwise, if there's already a config with the same name, complain
		if (getLaunchManager().isExistingLaunchConfigurationName(currentName)) {
			throw new CoreException(new Status(IStatus.ERROR,
												 DebugUIPlugin.getDefault().getDescriptor().getUniqueIdentifier(),
												 0,
												 LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Launch_configuration_already_exists_with_this_name_12"), //$NON-NLS-1$
												 null));						
		}						
	}
	
	/**
	 * If the name is valid, rename the current launch configuration.  Otherwise, show an
	 * appropriate error message.
	 */
	protected void updateConfigFromName() {
		if (getLaunchConfiguration() != null) {
			try {
				verifyName();
			} catch (CoreException ce) {
				refreshStatus();
				return;				
			}
						
			getLaunchConfiguration().rename(getNameTextWidget().getText().trim());
			refreshStatus();
		}
	}
	
	protected Display getDisplay() {
		Shell shell = getShell();
		if (shell != null) {
			return shell.getDisplay();
		} else {
			return Display.getDefault();
		}
	}
		
	/**
	 * Creates the launch configuration selection area of the dialog.
	 * This area displays a tree of launch configurations that the user
	 * may select, and allows users to create new configurations, and
	 * delete and duplicate existing configurations.
	 * 
	 * @return the composite used for launch configuration selection area
	 */ 
	protected Composite createLaunchConfigurationSelectionArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setSelectionArea(comp);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 5;
		comp.setLayout(layout);
		
		Label treeLabel = new Label(comp, SWT.NONE);
		treeLabel.setText("Launch Con&figurations:");
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		treeLabel.setLayoutData(gd);
		
		TreeViewer tree = new TreeViewer(comp);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		// Set width hint to 0 to force tree to only be as wide as the combined
		// width of the 'New' & 'Delete' buttons.  Otherwise tree wants to be much wider.
		gd.widthHint = 0;
		tree.getControl().setLayoutData(gd);
		tree.setContentProvider(new LaunchConfigurationContentProvider());
		tree.setLabelProvider(DebugUITools.newDebugModelPresentation());
		tree.setSorter(new WorkbenchViewerSorter());
		setTreeViewer(tree);
		tree.addSelectionChangedListener(this);
		tree.setInput(ResourcesPlugin.getWorkspace().getRoot());
		tree.expandAll();
		tree.addDoubleClickListener(this);
		tree.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		
		Button newButton = SWTUtil.createPushButton(comp, LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Ne&w_13"), null); //$NON-NLS-1$
		setButtonActionNew(new ButtonActionNew(newButton.getText(), newButton));
		
		Button deleteButton = SWTUtil.createPushButton(comp, LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Dele&te_14"), null); //$NON-NLS-1$
		setButtonActionDelete(new ButtonActionDelete(deleteButton.getText(), deleteButton));
		
		setButtonActionDuplicate(new ButtonActionDuplicate(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Duplicate_1"), null)); //$NON-NLS-1$
		
		return comp;
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
		Composite outerComp = new Composite(parent, SWT.NONE);
		GridLayout outerCompLayout = new GridLayout();
		outerCompLayout.numColumns = 1;
		outerCompLayout.marginHeight = 0;
		outerCompLayout.marginWidth = 0;
		outerComp.setLayout(outerCompLayout);
		
		Composite comp = new Composite(outerComp, SWT.NONE);
		setEditArea(comp);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 5;
		comp.setLayout(layout);		
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);
		
		Label nameLabel = new Label(comp, SWT.HORIZONTAL | SWT.LEFT);
		nameLabel.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.&Name__16")); //$NON-NLS-1$
		gd = new GridData(GridData.BEGINNING);
		nameLabel.setLayoutData(gd);
		
		Text nameText = new Text(comp, SWT.SINGLE | SWT.BORDER);
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
		
		Label spacer = new Label(comp, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 2;
		spacer.setLayoutData(gd);
		
		fTabComposite = new Composite(comp, SWT.NONE);
		GridLayout outerTabCompositeLayout = new GridLayout();
		outerTabCompositeLayout.marginHeight = 0;
		outerTabCompositeLayout.marginWidth = 0;
		fTabComposite.setLayout(outerTabCompositeLayout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		fTabComposite.setLayoutData(gd);		
		
		TabFolder tabFolder = new TabFolder(fTabComposite, SWT.NONE);
		setTabFolder(tabFolder);
		gd = new GridData(GridData.FILL_BOTH);
		tabFolder.setLayoutData(gd);
		getTabFolder().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleTabSelected();
			}
		});
		
		Composite buttonComp = new Composite(comp, SWT.NONE);
		GridLayout buttonCompLayout = new GridLayout();
		buttonCompLayout.numColumns = 2;
		buttonComp.setLayout(buttonCompLayout);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 2;
		buttonComp.setLayoutData(gd);
		
		setApplyButton(new Button(buttonComp, SWT.PUSH));
		getApplyButton().setText(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.&Apply_17")); //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		getApplyButton().setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(getApplyButton());
		getApplyButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleApplyPressed();
			}
		});
		
		setRevertButton(new Button(buttonComp, SWT.PUSH));
		getRevertButton().setText(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Revert_2"));   //$NON-NLS-1$
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		getRevertButton().setLayoutData(gd);
		SWTUtil.setButtonDimensionHint(getRevertButton());
		getRevertButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleRevertPressed();
			}
		});
		
		return outerComp;
	}	
	
	/**
	 * @see Dialog#createButtonBar(Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout pmLayout = new GridLayout();
		pmLayout.numColumns = 3;
		setProgressMonitorPart(new ProgressMonitorPart(composite, pmLayout, PROGRESS_INDICATOR_HEIGHT));
		Button cancelButton = createButton(getProgressMonitorPart(), ID_CANCEL_BUTTON, LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Cancel_3"), true); //$NON-NLS-1$
		setProgressMonitorCancelButton(cancelButton);
		getProgressMonitorCancelButton().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				setCancelButtonPressed(true);
			}
		});
		getProgressMonitorPart().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		getProgressMonitorPart().setVisible(false);

		return super.createButtonBar(composite);
	}
	
	/**
	 * Sets the title for the dialog, and establishes the help context.
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(Shell);
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Launch_Configurations_18")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(
			shell,
			IDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG);
	}
	
	/**
	 * @see Window#getInitialLocation(Point)
	 */
	protected Point getInitialLocation(Point initialSize) {	
		String locationString = getPreferenceStore().getString(IDebugPreferenceConstants.PREF_LAUNCH_CONFIGURATION_DIALOG_LOCATION);
		if (locationString.length() > 0) {
			Point locationPoint = parseCoordinates(locationString);
			if (locationPoint != null) {
				return locationPoint;
			}
		}
		return super.getInitialLocation(initialSize);
	}

	/**
	 * @see Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		String sizeString = getPreferenceStore().getString(IDebugPreferenceConstants.PREF_LAUNCH_CONFIGURATION_DIALOG_SIZE);
		if (sizeString.length() > 0) {
			Point sizePoint = parseCoordinates(sizeString);
			if (sizePoint != null) {
				return sizePoint;
			}
		}
		return DEFAULT_INITIAL_DIALOG_SIZE;
	}
	
	/**
	 * Given a coordinate String of the form "123x456" return a Point object whose
	 * X value is 123 and Y value is 456.  Return <code>null</code> if the String
	 * is not in the specified form.
	 */
	protected Point parseCoordinates(String coordString) {
		int byIndex = coordString.indexOf('x');
		if (byIndex < 0) {
			return null;
		}
		
		try {
			int x = Integer.parseInt(coordString.substring(0, byIndex));
			int y = Integer.parseInt(coordString.substring(byIndex + 1));			
			return new Point(x, y);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}
	
	/**
	 * Given a Point object, return a String of the form "XCoordxYCoord".
	 */
	protected String serializeCoords(Point coords) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(coords.x);
		buffer.append('x');
		buffer.append(coords.y);
		return buffer.toString();
	}
	
	private void setSashForm(SashForm sashForm) {
		fSashForm = sashForm;
	}
	
	protected SashForm getSashForm() {
		return fSashForm;
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
	
	protected IStructuredSelection getTreeViewerSelection() {
		return (IStructuredSelection)getTreeViewer().getSelection();
	}
	
	protected Object getTreeViewerFirstSelectedElement() {
		IStructuredSelection selection = getTreeViewerSelection();
		if (selection == null) {
			return null;
		}
		return selection.getFirstElement();
	}
		
	/**
	 * Content provider for launch configuration tree.
	 */
	class LaunchConfigurationContentProvider implements ITreeContentProvider {
		
		/**
		 * Actual launch configurations have no children.  Launch configuration types have
		 * all configurations of that type as children, minus any configurations that are 
		 * marked as private.
		 * 
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ILaunchConfiguration) {
				return EMPTY_ARRAY;
			} else if (parentElement instanceof ILaunchConfigurationType) {
				try {
					ILaunchConfigurationType type = (ILaunchConfigurationType)parentElement;
					ILaunchConfiguration[] allConfigs = getLaunchManager().getLaunchConfigurations(type);
					ArrayList filteredConfigs = new ArrayList(allConfigs.length);
					for (int i = 0; i < allConfigs.length; i++) {
						ILaunchConfiguration config = allConfigs[i];
						if (config.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false)) {
							continue;
						}
						filteredConfigs.add(config);
					}
					return filteredConfigs.toArray();
				} catch (CoreException e) {
					DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Error_19"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.An_exception_occurred_while_retrieving_launch_configurations_20"), e); //$NON-NLS-1$ //$NON-NLS-2$
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
					DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Error_19"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.An_exception_occurred_while_retrieving_launch_configurations_20"), e); //$NON-NLS-1$ //$NON-NLS-2$
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
		 * Return only the launch configuration types that support the current mode AND
		 * are marked as 'public'.
		 * 
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		public Object[] getElements(Object inputElement) {
			ILaunchConfigurationType[] allTypes = getLaunchManager().getLaunchConfigurationTypes();
			ArrayList list = new ArrayList(allTypes.length);
			String mode = getMode();
			for (int i = 0; i < allTypes.length; i++) {
				ILaunchConfigurationType configType = allTypes[i];
				if (configType.supportsMode(mode) && configType.isPublic()) {
					list.add(configType);
				}
			}			
			return list.toArray();
		}

		/**
		 * @see IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/**
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
	 * Notification that selection has changed in the launch configuration tree.
	 * <p>
	 * If the currently displayed configuration is not saved,
	 * prompt for saving before moving on to the new selection.
	 * </p>
	 * 
	 * @param event selection changed event
	 */
 	public void selectionChanged(SelectionChangedEvent event) {
 		
 		// Ignore selectionChange events that occur while saving
 		if (ignoreSelectionChanges()) {
 			return;
 		}
 		
 		// Get the new selection		
 		IStructuredSelection selection = (IStructuredSelection)event.getSelection();
 		if (selection.isEmpty()) {
 			getEditArea().setVisible(false);
 			setWorkingCopy(null);
 			setSelectedTreeObject(null);
 			updateButtons();
 			return;
 		}
 		
 		// Get details of the new selection
 		Object firstSelectedElement = selection.getFirstElement();		
 		boolean singleSelection = selection.size() == 1;
		boolean configSelected = firstSelectedElement instanceof ILaunchConfiguration;

 		// If selection is the same, don't bother
 		Object lastSelectedTreeObj = getSelectedTreeObject();
 		if (singleSelection && (lastSelectedTreeObj != null) && lastSelectedTreeObj.equals(firstSelectedElement)) {
 			getEditArea().setVisible(lastSelectedTreeObj instanceof ILaunchConfiguration);
 			return;
 		}
 		
		// Take care of any unsaved changes.  If the user aborts, reset selection
		// to whatever it was previously selected
		boolean canReplaceConfig = canDiscardCurrentConfig();
 		if (!canReplaceConfig) {
 			StructuredSelection prevSelection;
			if (lastSelectedTreeObj == null) {
				prevSelection = StructuredSelection.EMPTY;
			} else {
				prevSelection = new StructuredSelection(lastSelectedTreeObj);
			}
 			setTreeViewerSelection(prevSelection);
 			return;
 		}			
 				 		 		 
		// If a config is selected, update the edit area for it, if a config type is
		// selected, clear the edit area
 		if (singleSelection && configSelected) {
 			ILaunchConfiguration config = (ILaunchConfiguration) firstSelectedElement; 			
 			setLastSavedName(config.getName());
 			setLaunchConfiguration(config, false);
 		} else if (singleSelection && firstSelectedElement instanceof ILaunchConfigurationType) {
			if (canReplaceConfig) {
				clearLaunchConfiguration();
				getEditArea().setVisible(false);
				disposeExistingTabs();
			}
 		} else {
 			// multi-selection
 			clearLaunchConfiguration();
 			getEditArea().setVisible(false);
 		}
 		
 		updateButtons();
 		if (singleSelection) {
	 		setSelectedTreeObject(firstSelectedElement);
 		} else {
 			setSelectedTreeObject(null);
 		}
 	}
 	
 	protected void setProgressMonitorPart(ProgressMonitorPart part) {
 		fProgressMonitorPart = part;
 	}
 	
 	protected ProgressMonitorPart getProgressMonitorPart() {
 		return fProgressMonitorPart;
 	}
 	
 	protected void setProgressMonitorCancelButton(Button button) {
 		fProgressMonitorCancelButton = button;
 	}
 	
 	protected Button getProgressMonitorCancelButton() {
 		return fProgressMonitorCancelButton;
 	}
 	
 	/**
 	 * Sets the configuration to display/edit.
 	 * Updates the tab folder to contain the appropriate pages.
 	 * Sets all configuration-related state appropriately.
 	 * 
 	 * @param config the launch configuration to display/edit
 	 * @param init whether to initialize the config with default values
 	 */
 	protected void setLaunchConfiguration(ILaunchConfiguration config, boolean init) {
		try {
			
			// turn on initializing flag to ignore message updates
			setInitializingTabs(true);
			
			getEditArea().setVisible(true);
			showTabsForConfigType(config.getType());
			
			if (config.isWorkingCopy()) {
		 		setWorkingCopy((ILaunchConfigurationWorkingCopy)config);
			} else {
				setWorkingCopy(config.getWorkingCopy());
			}
			fUnderlyingConfig = getLaunchConfiguration().getOriginal();
	 		
	 		// update the name field before to avoid verify error 
	 		getNameTextWidget().setText(config.getName());	 		
	 			 		
	 		// Set the defaults for all tabs before any are initialized
	 		// so that every tab can see ALL the default values
	 		if (init) {
				getTabGroup().setDefaults(getLaunchConfiguration());
	 		}

	 		// update the tabs with the new working copy	 		
			getTabGroup().initializeFrom(getLaunchConfiguration());
	 		
	 		// update the name field after in case client changed it 
	 		getNameTextWidget().setText(config.getName());
	 				
	 		// turn off initializing flag to update message
			setInitializingTabs(false);
			
	 		refreshStatus();
	 		
		} catch (CoreException ce) {
 			DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Error_19"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Exception_occurred_setting_launch_configuration_24"), ce); //$NON-NLS-1$ //$NON-NLS-2$
 			clearLaunchConfiguration();
 			return;					
		}
 	} 
 	
 	/**
 	 * Clears the configuration being shown/edited.   
 	 * Resets all configuration-related state.
 	 */
 	protected void clearLaunchConfiguration() {
 		setWorkingCopy(null);
 		fUnderlyingConfig = null;
 		setLastSavedName(null);
 		getNameTextWidget().setText("");  //$NON-NLS-1$
 		refreshStatus();
 	}
 	
 	/**
 	 * Populate the tabs in the configuration edit area to be appropriate to the current
 	 * launch configuration type.
 	 */
 	protected void showTabsForConfigType(ILaunchConfigurationType configType) {		
 		
 		// Don't do any work if the current tabs are for the current config type
 		if (getTabType() != null && getTabType().equals(configType)) {
 			return;
 		}
 		
 		// Avoid flicker
 		getEditArea().setVisible(false);
 		
		// Dispose the current tabs
		disposeExistingTabs();

		// Build the new tabs
 		ILaunchConfigurationTabGroup group = null;
 		try {
	 		group = createGroup(configType);
 		} catch (CoreException ce) {
 			DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Error_19"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Exception_occurred_creating_launch_configuration_tabs_27"),ce); //$NON-NLS-1$ //$NON-NLS-2$
 			return;
 		}
 		
 		// Create the Control for each tab, and determine the maximum tab dimensions
 		PixelConverter pixelConverter = new PixelConverter(getTabFolder());
 		int runningTabWidth = 0;
 		ILaunchConfigurationTab[] tabs = group.getTabs();
 		Point contentSize = new Point(0, 0);
 		for (int i = 0; i < tabs.length; i++) {
 			TabItem tab = new TabItem(getTabFolder(), SWT.NONE);
 			String name = tabs[i].getName();
 			if (name == null) {
 				name = LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.unspecified_28"); //$NON-NLS-1$
 			}
 			tab.setText(name);
 			Image image = tabs[i].getImage();
 			tab.setImage(image);
 			runningTabWidth += pixelConverter.convertWidthInCharsToPixels(name.length() + 5);
 			if (image != null) {
	 			runningTabWidth += image.getBounds().width;
 			}
 			tabs[i].createControl(tab.getParent());
 			Control control = tabs[i].getControl();
 			if (control != null) {
	 			tab.setControl(control);
	 			Point size = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	 			if (size.x > contentSize.x) {
	 				contentSize.x = size.x;
	 			}
	 			if (size.y > contentSize.y) {
	 				contentSize.y = size.y;
	 			}
 			}
 		}
 		
 		// Determine if more space is needed to show all tab labels across the top of the
 		// tab folder.  If so, only increase size of dialog to some percent of the available
 		// screen real estate.
		if (runningTabWidth > contentSize.x) {
			int maxAllowedWidth = (int) (getDisplay().getBounds().width * MAX_DIALOG_WIDTH_PERCENT);
			int otherWidth = getSashForm().SASH_WIDTH + getSelectionArea().getBounds().width;
			int totalWidth = runningTabWidth + otherWidth;
			if (totalWidth > maxAllowedWidth) {
				contentSize.x = maxAllowedWidth - otherWidth;
			} else {
				contentSize.x = runningTabWidth;
			} 
		}
 		
 		// Adjust the maximum tab dimensions to account for the extra space required for the tab labels
 		Rectangle tabFolderBoundingBox = getTabFolder().computeTrim(0, 0, contentSize.x, contentSize.y);
		contentSize.x = tabFolderBoundingBox.width;
		contentSize.y = tabFolderBoundingBox.height;

 		// Force recalculation of sizes	
		getTabFolder().layout(true);
		
		// Calculate difference between required space for tab folder and current size,
		// then increase size of this dialog's Shell by that amount
 		Rectangle rect = fTabComposite.getClientArea();
		Point containerSize= new Point(rect.width, rect.height);
		int hdiff= contentSize.x - containerSize.x;
		int vdiff= contentSize.y - containerSize.y;
		// Only increase size of dialog, never shrink it
		if (hdiff > 0 || vdiff > 0) {
			int[] newSashWeights = null;
			if (hdiff > 0) {			
				newSashWeights = calculateNewSashWeights(hdiff);				
			}
			hdiff= Math.max(0, hdiff);
			vdiff= Math.max(0, vdiff);
			Shell shell= getShell();
			Point shellSize= shell.getSize();
			setShellSize(shellSize.x + hdiff, shellSize.y + vdiff);
			// Adjust the sash weights so that all of the increase in width
			// is given to the tab area
			if (newSashWeights != null) {
				getSashForm().setWeights(newSashWeights);
			}
		}

 		setTabGroup(group);
 		setTabType(configType);
 		getEditArea().setVisible(true);
 	}
 	
 	/**
 	 * Calculate & return a 2 element integer array that specifies the relative 
 	 * weights of the selection area and the edit area, based on the specified
 	 * increase in width of the owning shell.  The point of this method is calculate 
 	 * sash weights such that when the shell gets wider, all of the increase in width
 	 * is given to the edit area (tab folder), and the selection area (tree) stays
 	 * the same width.
 	 */
	protected int[] calculateNewSashWeights(int widthIncrease) {
		int[] newWeights = new int[2];
		newWeights[0] = getSelectionArea().getBounds().width;
		newWeights[1] = getEditArea().getBounds().width + widthIncrease;
		return newWeights;
	}

 	/**
 	 * Increase the size of this dialog's <code>Shell</code> by the specified amounts.
 	 * Do not increase the size of the Shell beyond the bounds of the Display.
 	 */
	private void setShellSize(int width, int height) {
		Rectangle bounds = getShell().getDisplay().getBounds();
		getShell().setSize(Math.min(width, bounds.width), Math.min(height, bounds.height));
	}
	
 	protected void disposeExistingTabs() {
		TabItem[] oldTabs = getTabFolder().getItems();
		if (getTabGroup() != null) {
			getTabGroup().dispose();
		}
		for (int i = 0; i < oldTabs.length; i++) {
			oldTabs[i].dispose();
		} 		
		setTabGroup(null);
		setTabType(null);
 	}
 	
 	/**
 	 * Sets the current launch configuration that is being
 	 * displayed/edited.
 	 */
 	protected void setWorkingCopy(ILaunchConfigurationWorkingCopy workingCopy) {
 		fWorkingCopy = workingCopy;
 	}
 	
 	protected boolean isWorkingCopyDirty() {
 		ILaunchConfigurationWorkingCopy workingCopy = getLaunchConfiguration();
 		if (workingCopy == null) {
 			return false;
 		}
 		
 		// Working copy hasn't been saved
 		if (workingCopy.getOriginal() == null) {
 			return true;
 		}
 		
 		// Name has changed.  Normally, this would be caught in the 'contentsEqual'
 		// check below, however there are some circumstances where this fails, such as
 		// when the name is invalid
 		if (isNameDirty()) {
 			return true;
 		}

 		updateWorkingCopyFromPages();
 		ILaunchConfiguration original = workingCopy.getOriginal();
 		return !original.contentsEqual(workingCopy);
 	}
 	
 	/**
 	 * Return <code>true</code> if the name has been modified since the last time it was saved.
 	 */
 	protected boolean isNameDirty() {
 		String currentName = getNameTextWidget().getText().trim();
 		return !currentName.equals(getLastSavedName());
 	}
 	
 	protected void setContext(Object context) {
 		fContext = context;
 	}
 	
 	protected Object getContext() {
 		return fContext;
 	}
 	 	
 	protected void setMode(String mode) {
 		fMode = mode;
 	}
 	
 	/** 
 	 * @see ILaunchConfigurationDialog#getMode()
 	 */
 	public String getMode() {
 		return fMode;
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
 	 * Sets the 'apply' button.
 	 * 
 	 * @param button the 'apply' button.
 	 */	
 	private void setApplyButton(Button button) {
 		fApplyButton = button;
 	}
 	
 	/**
 	 * Returns the 'apply' button
 	 * 
 	 * @return the 'apply' button
 	 */
 	protected Button getApplyButton() {
 		return fApplyButton;
 	}	
 	
 	/**
 	 * Sets the 'revert' button.
 	 * 
 	 * @param button the 'revert' button.
 	 */	
 	private void setRevertButton(Button button) {
 		fRevertButton = button;
 	}
 	
 	/**
 	 * Returns the 'revert' button
 	 * 
 	 * @return the 'revert' button
 	 */
 	protected Button getRevertButton() {
 		return fRevertButton;
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
 	 * @see ILaunchConfigurationDialog#getTabs()
 	 */
 	public ILaunchConfigurationTab[] getTabs() {
 		if (getTabGroup() == null) {
 			return null;
 		} else {
 			return getTabGroup().getTabs();
 		}
 	} 	
 	
	/**
	 * @see ILaunchConfigurationListener#launchConfigurationAdded(ILaunchConfiguration)
	 */
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
		setIgnoreSelectionChanges(true);
		try {
			setWorkingCopy(configuration.getWorkingCopy());
			fUnderlyingConfig = configuration;
		} catch (CoreException e) {
			DebugUIPlugin.log(e);
		}
		getTreeViewer().refresh();	
		updateButtons();
		setIgnoreSelectionChanges(false);
	}

	/**
	 * @see ILaunchConfigurationListener#launchConfigurationChanged(ILaunchConfiguration)
	 */
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
	}

	/**
	 * @see ILaunchConfigurationListener#launchConfigurationRemoved(ILaunchConfiguration)
	 */
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
		getTreeViewer().remove(configuration);		
	}
	
	protected void setIgnoreSelectionChanges(boolean ignore) {
		fIgnoreSelectionChanges = ignore;
	}
	
	protected boolean ignoreSelectionChanges() {
		return fIgnoreSelectionChanges;
	}
	
	/**
	 * Return whether the current configuration can be discarded.  This involves determining
	 * if it is dirty, and if it is, asking the user what to do.
	 */
	protected boolean canDiscardCurrentConfig() {		
		// If there is no working copy, there's no problem, return true
		ILaunchConfigurationWorkingCopy workingCopy = getLaunchConfiguration();
		if (workingCopy == null) {
			return true;
		}
		
		if (isWorkingCopyDirty()) {
			return showUnsavedChangesDialog();
		} else {
			return true;
		}
	}
	
	/**
	 * Show the user a dialog appropriate to whether the unsaved changes in the current config
	 * can be saved or not.  Return <code>true</code> if the user indicated that they wish to replace
	 * the current config, either by saving changes or by discarding the, return <code>false</code>
	 * otherwise.
	 */
	protected boolean showUnsavedChangesDialog() {
		if (canSaveConfig()) {
			return showSaveChangesDialog();
		} else {
			return showDiscardChangesDialog();
		}
	}
	
	/**
	 * Create and return a dialog that asks the user whether they want to save
	 * unsaved changes.  Return <code>true </code> if they chose to save changes,
	 * <code>false</code> otherwise.
	 */
	protected boolean showSaveChangesDialog() {
		StringBuffer buffer = new StringBuffer(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.The_configuration___29")); //$NON-NLS-1$
		buffer.append(getLaunchConfiguration().getName());
		buffer.append(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.__has_unsaved_changes.__Do_you_wish_to_save_them__30")); //$NON-NLS-1$
		MessageDialog dialog = new MessageDialog(getShell(), 
												 LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Save_changes__31"), //$NON-NLS-1$
												 null,
												 buffer.toString(),
												 MessageDialog.QUESTION,
												 new String[] {LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Yes_32"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.No_33"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Cancel_34")}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
												 0);
		// If user clicked 'Cancel' or closed dialog, return false
		int selectedButton = dialog.open();
		if ((selectedButton < 0) || (selectedButton == 2)) {
			return false;
		}
		
		// If they hit 'Yes', save the working copy 
		if (selectedButton == 0) {
			saveConfig();
		}
		
		return true;
	}
	
	/**
	 * Create and return a dialog that asks the user whether they want to discard
	 * unsaved changes.  Return <code>true</code> if they chose to discard changes,
	 * <code>false</code> otherwise.
	 */
	protected boolean showDiscardChangesDialog() {
		StringBuffer buffer = new StringBuffer(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.The_configuration___35")); //$NON-NLS-1$
		buffer.append(getNameTextWidget().getText());
		buffer.append(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.__has_unsaved_changes_that_CANNOT_be_saved_because_of_the_following_error_36")); //$NON-NLS-1$
		buffer.append(fCantSaveErrorMessage);
		buffer.append(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Do_you_wish_to_discard_changes_37")); //$NON-NLS-1$
		MessageDialog dialog = new MessageDialog(getShell(), 
												 LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Discard_changes__38"), //$NON-NLS-1$
												 null,
												 buffer.toString(),
												 MessageDialog.QUESTION,
												 new String[] {LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Yes_32"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.No_33")}, //$NON-NLS-1$ //$NON-NLS-2$
												 1);
		// If user clicked 'Yes', return true
		int selectedButton = dialog.open();
		if (selectedButton == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Return <code>true</code> if the current configuration can be saved, <code>false</code>
	 * otherwise.  Note this is NOT the same thing as the config simply being valid.  It is
	 * possible to save a config that does not validate.  This method determines whether the
	 * config can be saved without causing a serious error.  For example, a shared config that
	 * has no specified location would cause this method to return <code>false</code>.
	 */
	protected boolean canSaveConfig() {
		
		fCantSaveErrorMessage = null;

		// First make sure that name doesn't prevent saving the config
		try {
			verifyName();
		} catch (CoreException ce) {
			fCantSaveErrorMessage = ce.getStatus().getMessage();
			return false;
		}
		
		// Next, make sure none of the tabs object to saving the config
		ILaunchConfigurationTab[] tabs = getTabs();
		if (tabs == null) {
			fCantSaveErrorMessage = LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.No_tabs_found_41"); //$NON-NLS-1$
			return false;
		}
		for (int i = 0; i < tabs.length; i++) {
			if (!tabs[i].canSave()) {
				fCantSaveErrorMessage = tabs[i].getErrorMessage();
				return false;
			}
		}
		return true;		
	}

	/**
	 * Notification the 'New' action has been activated.
	 */
	protected void handleNewAction() {
		
		// Take care of any unsaved changes
		if (!canDiscardCurrentConfig()) {
			return;
		}
		
		// The 'New' action should only be enabled if a single config type is selected
		Object selectedElement = getTreeViewerFirstSelectedElement();
		if (!(selectedElement instanceof ILaunchConfigurationType)) {
			return;
		}
		
		// Construct a new config of the selected type and select the result in the tree
		ILaunchConfigurationType configType = (ILaunchConfigurationType) selectedElement;
		constructNewConfig(configType);
		getTreeViewer().setSelection(new StructuredSelection(fUnderlyingConfig));		
	}	
	
	/**
	 * Notification the 'Duplicate' action has been activated.
	 */
	protected void handleDuplicateAction() {

		// Take care of any unsaved changes
		if (!canDiscardCurrentConfig()) {
			return;
		}
		
		// The 'Duplicate' action should only be enabled if a single config is selected
		Object selectedElement = getTreeViewerFirstSelectedElement();
		if (!(selectedElement instanceof ILaunchConfiguration)) {
			return;
		}
		
		// Duplicate the selected config and select the dupe in the tree
		ILaunchConfiguration copyFromConfig = (ILaunchConfiguration) selectedElement;
		String newName = getLaunchManager().generateUniqueLaunchConfigurationNameFrom(copyFromConfig.getName());
		try {
			ILaunchConfigurationWorkingCopy newWorkingCopy = copyFromConfig.copy(newName);
			setLaunchConfiguration(newWorkingCopy, false);
			doSave();
			getTreeViewer().setSelection(new StructuredSelection(fUnderlyingConfig));
		} catch (CoreException ce) {
			DebugUIPlugin.log(ce);			
		}							
	}
	
	/**
	 * Create a new configuration of the specified type and select it in the tree.
	 */
	protected void doHandleNewConfiguration(ILaunchConfigurationType configType) {
		constructNewConfig(configType);
		getTreeViewer().setSelection(new StructuredSelection(fUnderlyingConfig));		
	}
	
	/**
	 * Make a copy of the specified configuration and select it in the tree.
	 */
	protected void doHandleCopyConfiguration(ILaunchConfiguration copyFromConfig) {
		String newName = getLaunchManager().generateUniqueLaunchConfigurationNameFrom(copyFromConfig.getName());
		try {
			ILaunchConfigurationWorkingCopy newWorkingCopy = copyFromConfig.copy(newName);
			setLaunchConfiguration(newWorkingCopy, false);
			doSave();
			getTreeViewer().setSelection(new StructuredSelection(fUnderlyingConfig));
		} catch (CoreException ce) {
			DebugUIPlugin.log(ce);			
		}					
	}
	
	/** 
	 * If a config type is selected, create a new config of that type initialized to 
	 * fWorkbenchSelection.  If a config is selected, create of new config of the
	 * same type as the selected config.
	 * protected void constructNewConfig() {
	 */
	protected void constructNewConfig(ILaunchConfigurationType configType) {	
		try {
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(DEFAULT_NEW_CONFIG_NAME));
			setLastSavedName(null);
			setLaunchConfiguration(wc, true);
			doSave();
		} catch (CoreException ce) {
			DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Error_19"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Exception_creating_new_launch_configuration_45"), ce); //$NON-NLS-1$ //$NON-NLS-2$
 			clearLaunchConfiguration();
 			return;			
		}		
	}
	
	/**
	 * Notification the 'Delete' action has been activated
	 */
	protected void handleDeleteAction() {		
		IStructuredSelection selection = getTreeViewerSelection();
		
		// The 'Delete' button is disabled if the selection contains anything other than configurations (no types)
		ILaunchConfiguration firstSelectedConfig = (ILaunchConfiguration) selection.getFirstElement();
		ILaunchConfigurationType firstSelectedConfigType = null;
		try {
			firstSelectedConfigType = firstSelectedConfig.getType();
		} catch (CoreException ce) {
			DebugUIPlugin.log(ce);						
		}
		
		// Initialize data used to set the selection after deletion
		int typeIndex= -1; // The index of the deleted configuration's type
		int configIndex= -1; // The index of the deleted configuration
		TreeItem[] items= getTreeViewer().getTree().getItems();
		TreeItem typeItem;
		for (int i= 0, numTypes= items.length; i < numTypes; i++) {
			typeItem= items[i];
			if (typeItem.getData() == firstSelectedConfigType) {
				typeIndex= i;
				TreeItem[] configs= typeItem.getItems();
				for (int j= 0, numConfigs= configs.length; j < numConfigs; j++) {
					if (configs[j].getData() == firstSelectedConfig) {
						configIndex= j;
						break;
					}
				}
			}
			
		}

		// Make the user confirm the deletion
		String dialogMessage = selection.size() > 1 ? LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Do_you_wish_to_delete_the_selected_launch_configurations__1") : LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Do_you_wish_to_delete_the_selected_launch_configuration__2"); //$NON-NLS-1$ //$NON-NLS-2$
		boolean ok = MessageDialog.openQuestion(this.getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Confirm_Launch_Configuration_Deletion_3"), dialogMessage); //$NON-NLS-1$
		if (!ok) {
			return;
		}


		Iterator iterator = selection.iterator();
		while (iterator.hasNext()) {
			clearLaunchConfiguration();
			Object selectedElement = iterator.next();
			if (selectedElement instanceof ILaunchConfiguration) {
				try {
					((ILaunchConfiguration)selectedElement).delete();
				} catch (CoreException ce) {
					DebugUIPlugin.log(ce);			
				}
			}
		}
		
		IStructuredSelection newSelection= null;
		if (typeIndex != -1 && configIndex != -1) {
			// Reset selection to the next config
			TreeItem[] configItems= getTreeViewer().getTree().getItems()[typeIndex].getItems();
			int numItems= configItems.length;
			if (numItems > configIndex) { // Select the item at the same index as the deleted
				newSelection= new StructuredSelection(configItems[configIndex].getData());
			} else if (numItems > 0) { // Deleted the last item(s). Select the last item
				newSelection= new StructuredSelection(configItems[numItems - 1].getData());
			}
		} 
		if (newSelection == null) {
			// Reset selection to the config type of the first selected configuration
			newSelection = new StructuredSelection(firstSelectedConfigType);
		}
		getTreeViewer().setSelection(newSelection);
	}	
	
	/**
	 * Notification the 'Close' button has been pressed.
	 */
	protected void handleClosePressed() {
		if (canDiscardCurrentConfig()) {
			cancelPressed();
		}
	}

	/**
	 * Notification that the 'Apply' button has been pressed
	 */
	protected void handleApplyPressed() {
		saveConfig();
		getTreeViewer().setSelection(new StructuredSelection(fUnderlyingConfig));
	}
	
	/**
	 * Notification that the 'Revert' button has been pressed
	 */
	protected void handleRevertPressed() {
		setLaunchConfiguration(getLaunchConfiguration().getOriginal(), false);
	}
	
	protected void saveConfig() {
		try {
			// trim name
			Text widget = getNameTextWidget();
			widget.setText(widget.getText().trim());
			doSave();
		} catch (CoreException e) {
			DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Launch_Configuration_Error_46"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Exception_occurred_while_saving_launch_configuration_47"), e); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		
		updateButtons();		
	}
	
	/**
	 * Notification that a tab has been selected
	 * 
	 * Disallow tab changing when the current tab is invalid.
	 * Update the config from the tab being left, and refresh
	 * the tab being entered.
	 */
	protected void handleTabSelected() {
		ILaunchConfigurationTab[] tabs = getTabs();
		if (fCurrentTabIndex == getTabFolder().getSelectionIndex() || tabs == null || tabs.length == 0 || fCurrentTabIndex > (tabs.length - 1)) {
			return;
		}
		if (fCurrentTabIndex != -1) {
			ILaunchConfigurationTab tab = tabs[fCurrentTabIndex];
			ILaunchConfigurationWorkingCopy wc = getLaunchConfiguration();
			if (wc != null) {
				// apply changes when leaving a tab
				tab.performApply(getLaunchConfiguration());
				// re-initialize a tab when entering it
				getActiveTab().initializeFrom(wc);
			}
		}
		fCurrentTabIndex = getTabFolder().getSelectionIndex();
		refreshStatus();
	}	
	
	/**
	 * Iterate over the pages to update the working copy
	 */
	protected void updateWorkingCopyFromPages() {
		ILaunchConfigurationWorkingCopy workingCopy = getLaunchConfiguration();
		if (getTabGroup() != null) {
			getTabGroup().performApply(workingCopy);
		}
	}
	
	/**
	 * Do the save
	 */
	protected void doSave() throws CoreException {
		ILaunchConfigurationWorkingCopy workingCopy = getLaunchConfiguration();
		updateWorkingCopyFromPages();
		if (isWorkingCopyDirty()) {
			fUnderlyingConfig = workingCopy.doSave();
			setWorkingCopy(fUnderlyingConfig.getWorkingCopy());
			setLastSavedName(fUnderlyingConfig.getName()); 
		}
	}
	
	/**
	 * Notification the 'launch' button has been pressed.
	 * Save and launch.
	 */
	protected void handleLaunchPressed() {
		int result = CANCEL;
		try {
			doSave();
			result = doLaunch(getLaunchConfiguration());
		} catch (CoreException e) {
			DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Launch_Configuration_Error_6"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Exception_occurred_while_launching_configuration._See_log_for_more_information_49"), e); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (result == OK) {
			if (fUnderlyingConfig != null) {
				try {
					getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_LAST_LAUNCH_CONFIGURATION_SELECTION, fUnderlyingConfig.getMemento());
				} catch (CoreException e) {
					DebugUIPlugin.log(e);
				}
			}
			close();
		} else {
			getShell().setFocus();
		}
	}
	
	/**
	 * Save the working copy if necessary, then launch the underlying configuration.
	 * 
	 * @return one of CANCEL or OK
	 */
	protected int doLaunch(ILaunchConfiguration config) throws CoreException {
		
		if (!DebugUITools.saveAndBuildBeforeLaunch()) {
			return CANCEL;
		}
		
		// If the configuration is a working copy and is dirty or doesn't yet exist, save it
		if (config instanceof ILaunchConfigurationWorkingCopy) {
			ILaunchConfigurationWorkingCopy workingCopy = (ILaunchConfigurationWorkingCopy) config;
			if (isWorkingCopyDirty() || !workingCopy.exists()) {
				fUnderlyingConfig = workingCopy.doSave();
			} 
		}
		
		// liftoff
		ILaunch launch = launchWithProgress();
		
		// If the launch was cancelled, get out.  Otherwise, notify the tabs of the successful launch.
		if (cancelButtonPressed()) {
			launch.terminate();
			return CANCEL;
		} else if (launch != null) {
			ILaunchConfigurationTabGroup group = getTabGroup();
			boolean disposeTabs = false;
			if (group == null) {
				// when launching without realizing this dialog, the tabs
				// may not exist - create and then dispose so we can notify them of a launch
				disposeTabs = true;
				group = createGroup(config.getType());
			}
			group.launched(launch);
			if (disposeTabs) {
				group.dispose();
			}
		}
		
		return OK;
	}
	
	/**
	 * @return the resulting launch, or <code>null</code> if cancelled.
	 * @exception CoreException if an exception occurrs launching
	 */
	private ILaunch launchWithProgress() throws CoreException {
		final ILaunch[] launchResult = new ILaunch[1];
		// Do the launch
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					launchResult[0] = fUnderlyingConfig.launch(getMode(), monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
		try {
			run(true, true, runnable);
		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				throw (CoreException)t;
			} else {
				IStatus status = new Status(IStatus.ERROR, IDebugUIConstants.PLUGIN_ID, DebugException.INTERNAL_ERROR, LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Exception_occurred_while_launching_50"), t); //$NON-NLS-1$
				throw new CoreException(status);
			}
		} finally {
			//remove any "error" launch
			ILaunchManager manager= DebugPlugin.getDefault().getLaunchManager();
			ILaunch[] launches= manager.getLaunches();
			for (int i = 0; i < launches.length; i++) {
				ILaunch iLaunch = launches[i];
				if (!iLaunch.hasChildren()) {
					manager.removeLaunch(iLaunch);
				}
			}
		}
				
		return launchResult[0];		
	}
	
	protected IPreferenceStore getPreferenceStore() {
		return DebugUIPlugin.getDefault().getPreferenceStore();
	}

	/***************************************************************************************
	 * 
	 * ProgressMonitor & IRunnableContext related methods
	 * 
	 ***************************************************************************************/

	/**
	 * @see IRunnableContext#run(boolean, boolean, IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		if (isVisible()) {
			// The operation can only be canceled if it is executed in a separate thread.
			// Otherwise the UI is blocked anyway.
			Object state = aboutToStart();
			fActiveRunningOperations++;
			try {
				ModalContext.run(runnable, fork, fProgressMonitorPart, getShell().getDisplay());
			} finally {
				fActiveRunningOperations--;
				stopped(state);
			}
		} else {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(DebugUIPlugin.getShell());
			dialog.run(fork, cancelable, runnable);
		}
	}
	
	/**
	 * About to start a long running operation triggered through
	 * the dialog. Shows the progress monitor and disables the dialog's
	 * buttons and controls.
	 *
	 * @return the saved UI state
	 */
	private Object aboutToStart() {
		Map savedState = null;
		if (getShell() != null) {
			// Save focus control
			Control focusControl = getShell().getDisplay().getFocusControl();
			if (focusControl != null && focusControl.getShell() != getShell()) {
				focusControl = null;
			}
			
			// Set the busy cursor to all shells.
			Display d = getShell().getDisplay();
			waitCursor = new Cursor(d, SWT.CURSOR_WAIT);
			setDisplayCursor(waitCursor);
					
			// Set the arrow cursor to the cancel component.
			arrowCursor= new Cursor(d, SWT.CURSOR_ARROW);
			getProgressMonitorCancelButton().setCursor(arrowCursor);
	
			// Deactivate shell
			savedState = saveUIState();
			if (focusControl != null) {
				savedState.put(FOCUS_CONTROL, focusControl);
			}
				
			// Attach the progress monitor part to the cancel button
			getProgressMonitorCancelButton().setEnabled(true);
			setCancelButtonPressed(false);
			getProgressMonitorPart().attachToCancelComponent(getProgressMonitorCancelButton());
			getProgressMonitorPart().setVisible(true);
			getProgressMonitorCancelButton().setFocus();
		}
		return savedState;
	}

	/**
	 * A long running operation triggered through the dialog
	 * was stopped either by user input or by normal end.
	 * Hides the progress monitor and restores the enable state
	 * of the dialog's buttons and controls.
	 *
	 * @param savedState the saved UI state as returned by <code>aboutToStart</code>
	 * @see #aboutToStart
	 */
	private void stopped(Object savedState) {
		if (getShell() != null) {
			getProgressMonitorPart().setVisible(false);
			getProgressMonitorPart().removeFromCancelComponent(getProgressMonitorCancelButton());
			Map state = (Map)savedState;
			restoreUIState(state);
	
			setDisplayCursor(null);	
			waitCursor.dispose();
			waitCursor = null;
			arrowCursor.dispose();
			arrowCursor = null;
			Control focusControl = (Control)state.get(FOCUS_CONTROL);
			if (focusControl != null) {
				focusControl.setFocus();
			}
		}
	}

	/**
	 * Captures and returns the enabled/disabled state of the wizard dialog's
	 * buttons and the tree of controls for the currently showing page. All
	 * these controls are disabled in the process, with the possible excepton of
	 * the Cancel button.
	 *
	 * @return a map containing the saved state suitable for restoring later
	 *   with <code>restoreUIState</code>
	 * @see #restoreUIState
	 */
	private Map saveUIState() {
		Map savedState= new HashMap(10);
		saveEnableStateAndSet(getButtonActionNew().getButton(), savedState, "new", false);//$NON-NLS-1$
		saveEnableStateAndSet(getButtonActionDelete().getButton(), savedState, "delete", false);//$NON-NLS-1$
		saveEnableStateAndSet(getApplyButton(), savedState, "apply", false);//$NON-NLS-1$
		saveEnableStateAndSet(getRevertButton(), savedState, "revert", false);//$NON-NLS-1$
		saveEnableStateAndSet(getButton(ID_LAUNCH_BUTTON), savedState, "launch", false);//$NON-NLS-1$
		saveEnableStateAndSet(getButton(ID_CLOSE_BUTTON), savedState, "close", false);//$NON-NLS-1$
		savedState.put("editarea", ControlEnableState.disable(getEditArea()));//$NON-NLS-1$
		savedState.put("tree", ControlEnableState.disable(getTreeViewer().getControl()));//$NON-NLS-1$
		return savedState;
	}

	/**
	 * Saves the enabled/disabled state of the given control in the
	 * given map, which must be modifiable.
	 *
	 * @param w the control, or <code>null</code> if none
	 * @param h the map (key type: <code>String</code>, element type:
	 *   <code>Boolean</code>)
	 * @param key the key
	 * @param enabled <code>true</code> to enable the control, 
	 *   and <code>false</code> to disable it
	 * @see #restoreEnableStateAndSet
	 */
	private void saveEnableStateAndSet(Control w, Map h, String key, boolean enabled) {
		if (w != null) {
			h.put(key, new Boolean(w.isEnabled()));
			w.setEnabled(enabled);
		}
	}

	/**
	 * Restores the enabled/disabled state of the wizard dialog's
	 * buttons and the tree of controls for the currently showing page.
	 *
	 * @param state a map containing the saved state as returned by 
	 *   <code>saveUIState</code>
	 * @see #saveUIState
	 */
	private void restoreUIState(Map state) {
		restoreEnableState(getButtonActionNew().getButton(), state, "new");//$NON-NLS-1$
		restoreEnableState(getButtonActionDelete().getButton(), state, "delete");//$NON-NLS-1$
		restoreEnableState(getApplyButton(), state, "apply");//$NON-NLS-1$
		restoreEnableState(getRevertButton(), state, "revert");//$NON-NLS-1$
		restoreEnableState(getButton(ID_LAUNCH_BUTTON), state, "launch");//$NON-NLS-1$
		restoreEnableState(getButton(ID_CLOSE_BUTTON), state, "close");//$NON-NLS-1$
		ControlEnableState tabState = (ControlEnableState) state.get("editarea");//$NON-NLS-1$
		tabState.restore();
		ControlEnableState treeState = (ControlEnableState) state.get("tree");//$NON-NLS-1$
		treeState.restore();		
	}

	/**
	 * Restores the enabled/disabled state of the given control.
	 *
	 * @param w the control
	 * @param h the map (key type: <code>String</code>, element type:
	 *   <code>Boolean</code>)
	 * @param key the key
	 * @see #saveEnableStateAndSet
	 */
	private void restoreEnableState(Control w, Map h, String key) {
		if (w != null) {
			Boolean b = (Boolean) h.get(key);
			if (b != null)
				w.setEnabled(b.booleanValue());
		}
	}
	
	protected void setCancelButtonPressed(boolean pressed) {
		fCancelButtonPressed = pressed;
	}
	
	protected boolean cancelButtonPressed() {
		return fCancelButtonPressed;
	}

	/**
	 * Sets the given cursor for all shells currently active
	 * for this window's display.
	 *
	 * @param cursor the cursor
	 */
	private void setDisplayCursor(Cursor cursor) {
		Shell[] shells = getShell().getDisplay().getShells();
		for (int i = 0; i < shells.length; i++)
			shells[i].setCursor(cursor);
	}
	
	/**
	 * Checks whether it is alright to close this dialog
	 * and performed standard cancel processing. If there is a
	 * long running operation in progress, this method posts an
	 * alert message saying that the dialog cannot be closed.
	 * 
	 * @return <code>true</code> if it is alright to close this dialog, and
	 *  <code>false</code> if it is not
	 */
	private boolean okToClose() {
		if (fActiveRunningOperations > 0) {
			synchronized (this) {
				fWindowClosingDialog = createDialogClosingDialog();
			}	
			fWindowClosingDialog.open();
			synchronized (this) {
				fWindowClosingDialog = null;
			}
			return false;
		}
		
		return true;
	}

	/**
	 * Creates and return a new wizard closing dialog without opening it.
	 */ 
	private MessageDialog createDialogClosingDialog() {
		MessageDialog result= new MessageDialog(
			getShell(),
			JFaceResources.getString("WizardClosingDialog.title"),//$NON-NLS-1$
			null,
			JFaceResources.getString("WizardClosingDialog.message"),//$NON-NLS-1$
			MessageDialog.QUESTION,
			new String[] {IDialogConstants.OK_LABEL},
			0 ); 
		return result;
	}
	
	/**
	 * @see ILaunchConfigurationDialog#canLaunch()
	 */
	public boolean canLaunch() {
		try {
			verifyStandardAttributes();
		} catch (CoreException e) {
			return false;
		}
		
		ILaunchConfigurationTab[] tabs = getTabs();
		if (tabs == null) {
			return false;
		}
		for (int i = 0; i < tabs.length; i++) {
			if (!tabs[i].isValid(getLaunchConfiguration())) {
				return false;
			}
		}
		return true;
	}

	protected ILaunchConfigurationWorkingCopy getLaunchConfiguration() {
		return fWorkingCopy;
	}

	/**
	 * @see ILaunchConfigurationDialog#updateButtons()
	 */
	public void updateButtons() {
		if (isInitializingTabs()) {
			return;
		}
		
		// Get the current selection
		IStructuredSelection sel = (IStructuredSelection)getTreeViewer().getSelection();
		boolean singleSelection = sel.size() == 1;
		boolean firstItemLaunchConfig = sel.getFirstElement() instanceof ILaunchConfiguration;
		boolean firstItemLaunchConfigType = sel.getFirstElement() instanceof ILaunchConfigurationType;
		
		// New action
 		getButtonActionNew().setEnabled(singleSelection && firstItemLaunchConfigType);
		
		// Duplicate action
		getButtonActionDuplicate().setEnabled(singleSelection && firstItemLaunchConfig);
		
		// Delete action
		if (sel.isEmpty()) {
			getButtonActionDelete().setEnabled(false); 		
		} else {
			Iterator iter = sel.iterator();
			boolean enable = true;
			while (iter.hasNext()) {
				if (iter.next() instanceof ILaunchConfigurationType) {
					enable = false;
					break;
				}
			}
			getButtonActionDelete().setEnabled(enable);
		}
		

		// Apply & Launch buttons
		if (sel.isEmpty()) {
			getApplyButton().setEnabled(false);
			getButton(ID_LAUNCH_BUTTON).setEnabled(false);
		} else {
			boolean canLaunch = canLaunch();
			getApplyButton().setEnabled(canLaunch);
			getButton(ID_LAUNCH_BUTTON).setEnabled(canLaunch);		
		}
		
		// Revert button
		if (sel.isEmpty() || sel.size() > 1) {
			getRevertButton().setEnabled(false);
		} else {
			if (firstItemLaunchConfig && isWorkingCopyDirty()) {
				getRevertButton().setEnabled(true);
			} else {
				getRevertButton().setEnabled(false);
			}
		}
	}
	
	/**
	 * @see ILaunchConfigurationDialog#getActiveTab()
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
	 * Returns the currently active TabItem
	 * 
	 * @return launch configuration tab item
	 */
	protected TabItem getActiveTabItem() {
		TabFolder folder = getTabFolder();
		TabItem tabItem = null;
		int selectedIndex = folder.getSelectionIndex();
		if (selectedIndex >= 0) {
			tabItem = folder.getItem(selectedIndex);
		}		
		return tabItem;
	}

	/**
	 * @see ILaunchConfigurationDialog#updateMessage()
	 */
	public void updateMessage() {
		if (isInitializingTabs()) {
			return;
		}
		
		// If there is no current working copy, show a default informational message and clear the error message
		if (getLaunchConfiguration() == null) {
			setErrorMessage(null);
			setMessage(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Select_a_type_of_configuration_to_create,_and_press___new__51")); //$NON-NLS-1$
			return;
		}
		
		try {
			verifyStandardAttributes();
		} catch (CoreException ce) {
			setErrorMessage(ce.getMessage());
			return;
		}
		
		// Get the active tab.  If there isn't one, clear the informational & error messages
		ILaunchConfigurationTab activeTab = getActiveTab();
		if (activeTab == null) {
			setMessage(null);
			setErrorMessage(null);
			return;
		}
		
		// Always set the informational (non-error) message based on the active tab		
		setMessage(activeTab.getMessage());
		
		// The bias is to show the active page's error message, but if there isn't one,
		// show the error message for one of the other tabs that has an error.  Set the icon
		// for all tabs according to whether they contain errors.
		String errorMessage = checkTabForError(activeTab);
		boolean errorOnActiveTab = errorMessage != null;
		setTabIcon(getActiveTabItem(), errorOnActiveTab, activeTab);
		
		ILaunchConfigurationTab[] allTabs = getTabs();
		for (int i = 0; i < allTabs.length; i++) {
			if (getTabFolder().getSelectionIndex() == i) {
				continue;
			}
			String tabError = checkTabForError(allTabs[i]);				
			TabItem tabItem = getTabFolder().getItem(i);
			boolean errorOnTab = tabError != null;
			setTabIcon(tabItem, errorOnTab, allTabs[i]);
			if (errorOnTab && !errorOnActiveTab) {
				errorMessage = '[' + removeAmpersandsFrom(tabItem.getText()) + "]: " + tabError; //$NON-NLS-1$
			}
		}
		setErrorMessage(errorMessage);				
	}
	
	/**
	 * Force the tab to update it's error state and return any error message.
	 */
	protected String checkTabForError(ILaunchConfigurationTab tab) {
		tab.isValid(getLaunchConfiguration());
		return tab.getErrorMessage();
	}
	
	/**
	 * Set the specified tab item's icon to an error icon if <code>error</code> is true,
	 * or a transparent icon of the same size otherwise.
	 */
	protected void setTabIcon(TabItem tabItem, boolean error, ILaunchConfigurationTab tab) {
		Image image = null;
		if (error) {			
			image = LaunchConfigurationManager.getDefault().getErrorTabImage(tab);
		} else {
			image = tab.getImage();
		}
		tabItem.setImage(image);								
	}
	
	/**
	 * Return a copy of the specified string 
	 */
	protected String removeAmpersandsFrom(String string) {
		String newString = new String(string);
		int index = newString.indexOf('&');
		while (index != -1) {
			newString = string.substring(0, index) + newString.substring(index + 1, newString.length());
			index = newString.indexOf('&');
		}
		return newString;
	}

	/**
	 * Returns the launch configuration selection area control.
	 * 
	 * @return control
	 */
	protected Composite getSelectionArea() {
		return fSelectionArea;
	}

	/**
	 * Sets the launch configuration selection area control.
	 * 
	 * @param editArea control
	 */
	private void setSelectionArea(Composite selectionArea) {
		fSelectionArea = selectionArea;
	}

	/**
	 * Returns the launch configuration edit area control.
	 * 
	 * @return control
	 */
	protected Composite getEditArea() {
		return fEditArea;
	}

	/**
	 * Sets the launch configuration edit area control.
	 * 
	 * @param editArea control
	 */
	private void setEditArea(Composite editArea) {
		fEditArea = editArea;
	}

	/**
	 * Returns the type that tabs are currently displayed
	 * for, or <code>null</code> if none.
	 * 
	 * @return launch configuration type or <code>null</code>
	 */
	protected ILaunchConfigurationType getTabType() {
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

	protected Object getSelectedTreeObject() {
		return fSelectedTreeObject;
	}
	
	protected void setSelectedTreeObject(Object obj) {
		fSelectedTreeObject = obj;
	}	
	
	/**
	 * @see ILaunchConfigurationDialog#setName(String)
	 */
	public void setName(String name) {
		if (isVisible()) {
			if (name == null) {
				name = ""; //$NON-NLS-1$
			}
			fNameText.setText(name.trim());
			refreshStatus();
		}
	}
	
	/**
	 * @see ILaunchConfigurationDialog#generateName(String)
	 */
	public String generateName(String name) {
		if (name == null) {
			name = ""; //$NON-NLS-1$
		}
		return getLaunchManager().generateUniqueLaunchConfigurationNameFrom(name);
	}

	/**
	 * @see IDoubleClickListener#doubleClick(DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {		
		ISelection selection = event.getSelection();		
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			Object firstSelected = structuredSelection.getFirstElement();
			if (firstSelected instanceof ILaunchConfigurationType) {
				if (getButtonActionNew().isEnabled()) {
					getButtonActionNew().run();
				}
			} else if (firstSelected instanceof ILaunchConfiguration) {
				if (canLaunch()) {
					handleLaunchPressed();
				}				
			}
		}		
	}
	
	/**
	 * Sets whether this dialog is initializing pages
	 * and should not bother to refresh status (butttons
	 * and message).
	 */
	private void setInitializingTabs(boolean init) {
		fInitializingTabs = init;
	}
	
	/**
	 * Returns whether this dialog is initializing pages
	 * and should not bother to refresh status (butttons
	 * and message).
	 */
	protected boolean isInitializingTabs() {
		return fInitializingTabs;
	}	
	
	/**
	 * Returns the initial launch configuration type, or <code>null</code> if none has been set.
	 */
	protected ILaunchConfigurationType getInitialConfigType() {
		return fInitialConfigType;
	}
	
	/**
	 * Sets the initial launch configuration type to be used when this dialog is opened.
	 */
	public void setInitialConfigType(ILaunchConfigurationType configType) {
		fInitialConfigType = configType;
	}
	
	/**
	 * Returns the initial selection shown in this dialog when opened in
	 * <code>LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_SELECTION</code> mode.
	 */
	protected IStructuredSelection getInitialSelection() {
		return fInitialSelection;
	}
	
	/**
	 * Sets the initial selection for the dialog when opened in 
	 * <code>LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_SELECTION</code> mode.
	 */
	public void setInitialSelection(IStructuredSelection selection) {
		fInitialSelection = selection;
	}
	
	/**
	 * Handles key events in tree viewer. Specifically
	 * when the delete key is pressed.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0) {
			if (getButtonActionDelete().isEnabled()) {
				getButtonActionDelete().run();
			}
		}
	}
	
	protected void setButtonActionNew(ButtonAction action) {
		fButtonActionNew = action;
	}
	
	protected ButtonAction getButtonActionNew() {
		return fButtonActionNew;
	}

	protected void setButtonActionDuplicate(ButtonAction action) {
		fButtonActionDuplicate = action;
	}
	
	protected ButtonAction getButtonActionDuplicate() {
		return fButtonActionDuplicate;
	}

	protected void setButtonActionDelete(ButtonAction action) {
		fButtonActionDelete = action;
	}
	
	protected ButtonAction getButtonActionDelete() {
		return fButtonActionDelete;
	}

	/**
	 * Extension of <code>Action</code> that manages a <code>Button</code>
	 * widget.  This allows common handling for actions that must appear in
	 * a pop-up menu and also as a (non-toolbar) button in the UI.
	 */
	private abstract class ButtonAction extends Action {
		
		protected Button fButton;
		
		/**
		 * Construct a ButtonAction handler.  All details of the specified
		 * <code>Button</code>'s layout and appearance should be handled 
		 * external to this class.
		 */
		public ButtonAction(String text, Button button) {
			super(text);
			fButton = button;
			if (fButton != null) {
				fButton.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent evt) {
						ButtonAction.this.run();
					}
				});
			}
		}
		
		public Button getButton() {
			return fButton;
		}
		
		/**
		 * @see IAction#setEnabled(boolean)
		 */
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			if (fButton != null) {
				fButton.setEnabled(enabled);
			}
		}
	}
	
	/**
	 * Handler for creating a new configuration.
	 */
	private class ButtonActionNew extends ButtonAction {
		
		public ButtonActionNew(String text, Button button) {
			super(text, button);
		}
		
		public void run() {
			handleNewAction();
		}
	}

	/**
	 * Handler for duplicating a configuration.
	 */
	private class ButtonActionDuplicate extends ButtonAction {
		
		public ButtonActionDuplicate(String text, Button button) {
			super(text, button);
		}
		
		public void run() {
			handleDuplicateAction();
		}
	}

	/**
	 * Handler for deleting a configuration.
	 */
	private class ButtonActionDelete extends ButtonAction {
		
		public ButtonActionDelete(String text, Button button) {
			super(text, button);
		}
		
		public void run() {
			handleDeleteAction();
		}
	}

}