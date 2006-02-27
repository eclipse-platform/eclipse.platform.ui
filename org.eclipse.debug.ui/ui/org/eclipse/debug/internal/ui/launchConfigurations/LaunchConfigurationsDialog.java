/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTabGroup;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;
 
/**
 * The dialog used to edit and launch launch configurations.
 */
public class LaunchConfigurationsDialog extends TitleAreaDialog implements ILaunchConfigurationDialog, IPageChangeProvider,  IPropertyChangeListener {

	/**
	 * Keep track of the currently visible dialog instance
	 */
	private static ILaunchConfigurationDialog fgCurrentlyVisibleLaunchConfigurationDialog;
	
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
	 * Constant specifying how wide this dialog is allowed to get (as a percentage of
	 * total available screen width) as a result of tab labels in the edit area.
	 */
	protected static final float MAX_DIALOG_WIDTH_PERCENT = 0.50f;
		
	/**
	 * Constant specifying how tall this dialog is allowed to get (as a percentage of
	 * total available screen height) as a result of preferred tab size.
	 */
	protected static final float MAX_DIALOG_HEIGHT_PERCENT = 0.50f;
		
	/**
	 * Size of this dialog if there is no preference specifying a size.
	 */
	protected static final Point DEFAULT_INITIAL_DIALOG_SIZE = new Point(640, 560);
	
	/**
	 * defines some default sashweights when we have a new workspace
	 * @since 3.2
	 */
	protected static final int[] DEFAULT_SASH_WEIGHTS = new int[] {190, 450};
	
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
	 * Constant specifying that a new launch configuration dialog was not opened.  Instead
	 * an existing launch configuration dialog was used.
	 */
	public static final int LAUNCH_CONFIGURATION_DIALOG_REUSE_OPEN = 4;
	
	/**
	 * Returns the currently visible dialog
	 * @return the currently visible launch dialog
	 */
	public static ILaunchConfigurationDialog getCurrentlyVisibleLaunchConfigurationDialog() {
		return fgCurrentlyVisibleLaunchConfigurationDialog;
	}
	
	/**
	 * Sets which launch dialog is currently the visible one
	 * @param dialog the dialog to set as the visible one
	 */
	public static void setCurrentlyVisibleLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		fgCurrentlyVisibleLaunchConfigurationDialog = dialog;
	}
	
	/**
	 * The Composite used to insert an adjustable 'sash' between the tree and the tabs.
	 */
	private SashForm fSashForm;
			
	/**
	 * The launch configuration selection area.
	 */
	private Composite fSelectionArea;
	
	/**
	 * Tree view of launch configurations
	 */
	private LaunchConfigurationView fLaunchConfigurationView;
	
	/**
	 * Tab edit area
	 */
	private LaunchConfigurationTabGroupViewer fTabViewer;
	
	/**
	 * The launch configuration edit area.
	 */
	private Composite fEditArea;

	/**
	 * The 'cancel' button that appears when the in-dialog progress monitor is shown.
	 */
	private Button fProgressMonitorCancelButton;	
	
	/**
	 * When this dialog is opened in <code>LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_SELECTION</code>
	 * mode, this specifies the selection that is initially shown in the dialog.
	 */
	private IStructuredSelection fInitialSelection;
		
	/**
	 * The status to open the dialog on, or <code>null</code> if none.
	 */
	private IStatus fInitialStatus;

	/**
	 * progress monitor part
	 */
	private ProgressMonitorPart fProgressMonitorPart;
	
	/**
	 * Default cursor for waiting
	 */
	private Cursor waitCursor;
	
	/**
	 * Default cursor  for the arrow
	 */
	private Cursor arrowCursor;
	
	/**
	 * Listener for a list
	 */
	private ListenerList changeListeners = new ListenerList();
	
	/**
	 * The number of 'long-running' operations currently taking place in this dialog
	 */	
	private long fActiveRunningOperations = 0;
	
	/**
	 * The launch group being displayed
	 */
	private LaunchGroupExtension fGroup;
	
	/**
	 * Banner image
	 */
	private Image fBannerImage;
	
	/**
	 * Double-click action
	 */
	private IAction fDoubleClickAction;

	/**
	 * the label for the viewer about items filtered
	 * @since 3.2
	 */
	private Label fFilteringLabel;
	
	/**
	 * the current launch manager
	 * 
	 * @since 3.2
	 */
	private ILaunchManager fLaunchManager = DebugPlugin.getDefault().getLaunchManager();
	
	/**
	 * Filters for the LCD
	 * @since 3.2
	 */
	private ClosedProjectFilter fClosedProjectFilter;
	private DeletedProjectFilter fDeletedProjectFilter;
	private LaunchConfigurationTypeFilter fLCTFilter;
	private WorkingSetsFilter fWorkingSetsFilter;
	
	
	/**
	 * Specifies how this dialog behaves when opened.  Value is one of the 
	 * 'LAUNCH_CONFIGURATION_DIALOG' constants defined in this class.
	 */
	private int fOpenMode = LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_LAST_LAUNCHED;

	/**
	 * dialog settings
	 */
	private static final String DIALOG_SASH_WEIGHTS_1 = IDebugUIConstants.PLUGIN_ID + ".DIALOG_SASH_WEIGHTS_1"; //$NON-NLS-1$
	private static final String DIALOG_SASH_WEIGHTS_2 = IDebugUIConstants.PLUGIN_ID + ".DIALOG_SASH_WEIGHTS_2"; //$NON-NLS-1$
	
	/**
	 * Constructs a new launch configuration dialog on the given
	 * parent shell.
	 * 
	 * @param shell the parent shell
	 * @param group the group of launch configuration to display
	 */
	public LaunchConfigurationsDialog(Shell shell, LaunchGroupExtension group) {
		super(shell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setLaunchGroup(group);
	}
	
	/**
	 * Adds content to the dialog area
	 * 
	 * @param dialogComp
	 */
	protected void addContent(Composite dialogComp) {
		GridData gd;
		Composite topComp = new Composite(dialogComp, SWT.NONE);
		gd = new GridData(GridData.FILL_BOTH);
		topComp.setLayoutData(gd);
		GridLayout topLayout = new GridLayout(2, false);
		topLayout.marginHeight = 5;
		topLayout.marginWidth = 5;
		topComp.setLayout(topLayout);
		
		// Set the things that TitleAreaDialog takes care of 
		setTitle(LaunchConfigurationsMessages.LaunchConfigurationDialog_Create__manage__and_run_launch_configurations_8); 
		setMessage(LaunchConfigurationsMessages.LaunchConfigurationDialog_Ready_to_launch_2); 
		setModeLabelState();
		
		// Create the SashForm that contains the selection area on the left,
		// and the edit area on the right
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		SashForm sash = new SashForm(topComp, SWT.SMOOTH);
		sash.setOrientation(SWT.HORIZONTAL);
		sash.setLayoutData(gd);
		sash.setFont(dialogComp.getFont());
		sash.setVisible(true);
		fSashForm = sash;
		
		// Build the launch configuration selection area and put it into the composite.
		Control launchConfigSelectionArea = createLaunchConfigurationSelectionArea(fSashForm);
		gd = new GridData(GridData.FILL_VERTICAL);
		launchConfigSelectionArea.setLayoutData(gd);
		
		// Build the launch configuration edit area and put it into the composite.
		Composite editAreaComp = createLaunchConfigurationEditArea(fSashForm);
		setEditArea(editAreaComp);
		gd = new GridData(GridData.FILL_BOTH);
		editAreaComp.setLayoutData(gd);
		
		dialogComp.layout(true);
		applyDialogFont(dialogComp);
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IPageChangeProvider#addPageChangedListener(org.eclipse.jface.dialogs.IPageChangedListener)
	 */
	public void addPageChangedListener(IPageChangedListener listener) {
		changeListeners.add(listener);
	}
	
	/**
	 * Handle the 'close' & 'launch' buttons here, all others are handled
	 * in <code>Dialog</code>
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
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
	 * Return whether the current configuration can be discarded.  This involves determining
	 * if it is dirty, and if it is, asking the user what to do.
	 * 
	 * @return if we can discard the current config or not
	 */
	private boolean canDiscardCurrentConfig() {				
		if (getTabViewer().isDirty()) {
			return showUnsavedChangesDialog();
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
	    if (!isSafeToClose()) {
	        return false;
	    }
	    persistSashWeights();
		setCurrentlyVisibleLaunchConfigurationDialog(null);
		getBannerImage().dispose();
		getTabViewer().dispose();
		if (fLaunchConfigurationView != null) {
			fLaunchConfigurationView.dispose();
		}
		DebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		return super.close();
	}
	
	/**
	 * Sets the title for the dialog, and establishes the help context.
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell);
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(getShellTitle());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#create()
	 */
	public void create() {
		super.create();
		// bug 27011
		if (getTabViewer().getInput() == null) {
			getTabViewer().inputChanged(null);
		}			
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.marginLeft = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setFont(font);
		
		// create help control if needed
        if (isHelpAvailable() || TrayDialog.isDialogHelpAvailable()) {
        	createHelpControl(composite);
        }

		Composite monitorComposite = new Composite(composite, SWT.NULL);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		monitorComposite.setLayout(layout);
		monitorComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout pmLayout = new GridLayout();
		fProgressMonitorPart = new ProgressMonitorPart(monitorComposite, pmLayout);
		fProgressMonitorPart.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fProgressMonitorPart.setFont(font);
		
		fProgressMonitorCancelButton = createButton(monitorComposite, ID_CANCEL_BUTTON, LaunchConfigurationsMessages.LaunchConfigurationDialog_Cancel_3, true);
		fProgressMonitorCancelButton.setFont(font);
		monitorComposite.setVisible(false);

		
		/* * This code is duplicated from Dialog#createButtonBar. We do not want
		 * to call the TrayDialog implementation because it adds a help control.
		 * Since this is a custom button bar, we explicitly added the help control
		 * in an appropriate place above, and we use the Dialog implementation of
		 * createButtonBar, which does not add a help control.
		 */
		Composite buttonComposite = new Composite(composite, SWT.NONE);
		// create a layout with spacing and margins appropriate for the font
		// size.
		layout = new GridLayout();
		layout.numColumns = 0; // this is incremented by createButton
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
		buttonComposite.setLayoutData(data);
		buttonComposite.setFont(composite.getFont());
		
		// Add the buttons to the button bar.
		createButtonsForButtonBar(buttonComposite);
		return composite;
	}

	/**
	 * A launch configuration dialog overrides this method
	 * to create a custom set of buttons in the button bar.
	 * This dialog has 'Launch' and 'Cancel'
	 * buttons.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, ID_LAUNCH_BUTTON, getLaunchButtonText(), true);
        button.setEnabled(false);
		createButton(parent, ID_CLOSE_BUTTON, LaunchConfigurationsMessages.LaunchConfigurationDialog_Close_1, false);  
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		initializeContent();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getDialogArea(), getHelpContextId());
		return contents;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite dialogComp = (Composite)super.createDialogArea(parent);
		addContent(dialogComp);
		return dialogComp;
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
		setTabViewer(new LaunchConfigurationTabGroupViewer(parent, this));
		getTabViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleTabSelectionChanged();
			}
		});
		return (Composite)getTabViewer().getControl();
	}
    
	/**
	 * Creates all of the actions for the toolbar
	 * @param toolbar
	 * @since 3.2
	 */
	protected void createToolbarActions(ToolBarManager tmanager) {
		tmanager.add(getNewAction());
		tmanager.add(getDuplicateAction());
		tmanager.add(getDeleteAction());
		tmanager.add(getFilterAction());
		tmanager.update(true);

		DebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}
    
	/**
	 * Creates the launch configuration selection area of the dialog.
	 * This area displays a tree of launch configurations that the user
	 * may select, and allows users to create new configurations, and
	 * delete and duplicate existing configurations.
	 * 
	 * @return the composite used for launch configuration selection area
	 */ 
	protected Control createLaunchConfigurationSelectionArea(Composite parent) {
        Composite comp = new Composite(parent, SWT.FLAT);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        comp.setLayout(gridLayout);
        comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        ViewForm viewForm = new ViewForm(comp, SWT.FLAT | SWT.BORDER);
        
        ToolBar toolBar = new ToolBar(viewForm, SWT.FLAT);
        viewForm.setTopLeft(toolBar);
        ToolBarManager toolBarManager= new ToolBarManager(toolBar);
        
		fSelectionArea = viewForm;
        viewForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Composite viewFormContents = new Composite(viewForm, SWT.FLAT);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 5;
        gridLayout.marginWidth = 5;
        viewFormContents.setLayout(gridLayout);
        viewFormContents.setBackground(new Color(parent.getDisplay(), 255, 255, 255));
        
		
		fLaunchConfigurationView = new LaunchConfigurationView(getLaunchGroup());
		fLaunchConfigurationView.createLaunchDialogControl(viewFormContents);
		
	//create toolbar actions, we reuse the actions form the view so we wait until after
	//the view is created to add them to the toolbar
		createToolbarActions(toolBarManager);
		
		fDoubleClickAction = new Action() {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection)fLaunchConfigurationView.getViewer().getSelection();
				Object target = selection.getFirstElement();
				if (target instanceof ILaunchConfiguration) {
					if (getTabViewer().canLaunch()) {
						handleLaunchPressed();
					}
				} else {
					getNewAction().run();
					refreshFilteringLabel();
				}
			}
		};
		fLaunchConfigurationView.setAction(IDebugView.DOUBLE_CLICK_ACTION, fDoubleClickAction);
		Viewer viewer = fLaunchConfigurationView.getViewer();
		
		//set up the filters
		fClosedProjectFilter = new ClosedProjectFilter();
		if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_CLOSED)) {
			((StructuredViewer)viewer).addFilter(fClosedProjectFilter);
		}
		fDeletedProjectFilter = new DeletedProjectFilter();
		if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_DELETED)) {
			((StructuredViewer)viewer).addFilter(fDeletedProjectFilter);
		}
		fLCTFilter = new LaunchConfigurationTypeFilter();
		if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_TYPES)) {
			((StructuredViewer)viewer).addFilter(fLCTFilter);
		}
		fWorkingSetsFilter = new WorkingSetsFilter();
		if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_FILTER_WORKING_SETS)) {
			((StructuredViewer)viewer).addFilter(fWorkingSetsFilter);
		}
		
		Control control = viewer.getControl();
		GridData gd = new GridData(GridData.FILL_BOTH);
		control.setLayoutData(gd);
        viewForm.setContent(viewFormContents);
		
		fFilteringLabel = new Label(viewFormContents, SWT.NONE);
		fFilteringLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | SWT.BEGINNING));
		fFilteringLabel.setBackground(new Color(parent.getDisplay(), 255, 255, 255));
		refreshFilteringLabel();
		
		// confirmation requestors
		AbstractLaunchConfigurationAction.IConfirmationRequestor requestor = new AbstractLaunchConfigurationAction.IConfirmationRequestor() {
			public boolean getConfirmation() {
				return canDiscardCurrentConfig();
			}
		};
		getDuplicateAction().setConfirmationRequestor(requestor);
		getNewAction().setConfirmationRequestor(requestor);
		
		//listeners
		((StructuredViewer) viewer).addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleLaunchConfigurationSelectionChanged(event);
				getNewAction().setEnabled(getNewAction().isEnabled());
				getDeleteAction().setEnabled(getDeleteAction().isEnabled());
				getDuplicateAction().setEnabled(getDuplicateAction().isEnabled());
			}
		});
       
		return comp;
	}	

    /**
	 * Returns whether the given categories are equal.
	 * 
	 * @param c1 category identifier or <code>null</code>
	 * @param c2 category identifier or <code>null</code>
	 * @return boolean
	 */
	private boolean equalCategories(String c1, String c2) {
		if (c1 == null || c2 == null) {
			return c1 == c2;
		}
		return c1.equals(c2);
	} 
	
	/**
	 * Displays how many of the items showing matched the filtering currently in operation
	 * @since 3.2
	 */
	public void refreshFilteringLabel() {
		try {
			int total = 0;
			ILaunchConfiguration[] configs = fLaunchManager.getLaunchConfigurations();
			for(int i = 0; i < configs.length; i++) {
				if(configs[i].supportsMode(getMode()) & !configs[i].getAttribute(IDebugUIConstants.ATTR_PRIVATE, false) & equalCategories(configs[i].getCategory(), getLaunchGroup().getCategory())) {
					total++;
				}
			}
			ILaunchConfigurationType[] types = fLaunchManager.getLaunchConfigurationTypes();
			for(int i = 0; i < types.length; i++) {
				if(types[i].supportsMode(getMode()) & types[i].isPublic() & equalCategories(types[i].getCategory(), getLaunchGroup().getCategory())) {
					total++;
				}
			}
			TreeViewer viewer = ((TreeViewer)fLaunchConfigurationView.getViewer());
			IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
			viewer.getTree().selectAll();
			int filtered = ((IStructuredSelection)viewer.getSelection()).size();
			viewer.getTree().deselectAll();
			viewer.setSelection(sel);
			fFilteringLabel.setText(MessageFormat.format(LaunchConfigurationsMessages.LaunchConfigurationsDialog_6, new Object[] {new Integer(filtered), new Integer(total)}));
			
		}
		catch(CoreException e) {e.printStackTrace();}
	}
	
	/**
	 * Set the initial selection in the tree.
	 */
	public void doInitialTreeSelection() {
		fLaunchConfigurationView.getViewer().setSelection(fInitialSelection);
	}	
	
	/**
     * Notifies any selection changed listeners that the selected page
     * has changed.
     * Only listeners registered at the time this method is called are notified.
     *
     * @param event a selection changed event
     *
     * @see IPageChangedListener#pageChanged
     */
    protected void firePageChanged(final PageChangedEvent event) {
        Object[] listeners = changeListeners.getListeners();
        for (int i = 0; i < listeners.length; ++i) {
            final IPageChangedListener l = (IPageChangedListener) listeners[i];
            SafeRunner.run(new SafeRunnable() {
                public void run() {
                    l.pageChanged(event);
                }
            });
        }
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#generateName(java.lang.String)
	 */
	public String generateName(String name) {
		if (name == null) {
			name = ""; //$NON-NLS-1$
		}
		return fLaunchManager.generateUniqueLaunchConfigurationNameFrom(name);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#getActiveTab()
	 */
	public ILaunchConfigurationTab getActiveTab() {
		return getTabViewer().getActiveTab();
	}
	
	/**
	 * Returns the banner image to display in the title area
	 */
	protected Image getBannerImage() {
		if (fBannerImage == null) {
			ImageDescriptor descriptor = getLaunchGroup().getBannerImageDescriptor(); 
			if (descriptor != null) {
				fBannerImage = descriptor.createImage();
			} 		
		}
		return fBannerImage;
	}
	
	/**
	 * Gets the delete menu action
	 * 
	 * @return the delete menu action
	 */
	protected AbstractLaunchConfigurationAction getDeleteAction() {
		return (AbstractLaunchConfigurationAction)fLaunchConfigurationView.getAction(DeleteLaunchConfigurationAction.ID_DELETE_ACTION);
	}

	/**
	 * Gets the filter action
	 * @return the filter menu action
	 * @since 3.2
	 */
	protected IAction getFilterAction() {
		return fLaunchConfigurationView.getAction(FilterLaunchConfigurationAction.ID_FILTER_ACTION);
	}
	
	 /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     * @since 3.2
     */
    protected IDialogSettings getDialogBoundsSettings() {
    	return getDialogSettings();
    }
    
	/**
	 * Returns the dialog settings for this dialog. Subclasses should override
	 * <code>getDialogSettingsSectionName()</code>.
	 * 
	 * @return IDialogSettings
	 */
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
		if (section == null) {
			section = settings.addNewSection(getDialogSettingsSectionName());
		} 
		return section;
	}

	/**
	 * Returns the name of the section that this dialog stores its settings in
	 * 
	 * @return String
	 */
	protected String getDialogSettingsSectionName() {
		return IDebugUIConstants.PLUGIN_ID + ".LAUNCH_CONFIGURATIONS_DIALOG_SECTION"; //$NON-NLS-1$
	}	
		
	/**
	 * Gets the current display
	 * 
	 * @return the display
	 */
	protected Display getDisplay() {
		Shell shell = getShell();
		if (shell != null) {
			return shell.getDisplay();
		} 
		return Display.getDefault();
	}
  	
  	/**
  	 * Gets the duplicate menu action
  	 * 
  	 * @return the duplicate menu action
  	 */
  	protected AbstractLaunchConfigurationAction getDuplicateAction() {
		return (AbstractLaunchConfigurationAction)fLaunchConfigurationView.getAction(DuplicateLaunchConfigurationAction.ID_DUPLICATE_ACTION);
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
	 * Gets the help context id
	 * 
	 * @return the help context id
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG;
	}
 
 	/**
	 * Returns the status the dialog was opened on or <code>null</code> if none.
	 * 
	 * @return IStatus
	 */
	protected IStatus getInitialStatus() {
		return fInitialStatus;
	}
 	 	 	
 	/**
	 * Return the last launched configuration in the workspace.
	 * 
	 * @return the last launched configuration
	 */
	protected ILaunchConfiguration getLastLaunchedWorkbenchConfiguration() {
		return DebugUIPlugin.getDefault().getLaunchConfigurationManager().getLastLaunch(getLaunchGroup().getIdentifier());
	}

 	/**
	 * Returns the appropriate text for the launch button - run or debug.
	 * 
	 * @return the launch button text
	 */
	protected String getLaunchButtonText() {
		return DebugPlugin.getDefault().getLaunchManager().getLaunchMode(getMode()).getLabel();
	}
 	 
 	/**
	 * Returns the launch group being displayed.
	 * 
	 * @return launch group
	 */
	public LaunchGroupExtension getLaunchGroup() {
		return fGroup;
	}
 	 	 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#getMode()
 	 */
 	public String getMode() {
 		return getLaunchGroup().getMode();
 	} 	
 	
	/**
	 * Gets the new menu action
	 * 
	 * @return the new menu action
	 */
	protected AbstractLaunchConfigurationAction getNewAction() {
		return (AbstractLaunchConfigurationAction)fLaunchConfigurationView.getAction(CreateLaunchConfigurationAction.ID_CREATE_ACTION);
	}
	
	/**
	 * returns the open mode
	 * 
	 * @return the open mode
	 */
	protected int getOpenMode() {
		return fOpenMode;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IPageChangeProvider#getSelectedPage()
	 */
	public Object getSelectedPage() {
		return getActiveTab();
	}
	
	/**
	 * Returns the title of the shell
	 * @return the shell title
	 */
	protected String getShellTitle() {
		String title = DebugUIPlugin.removeAccelerators(getLaunchGroup().getLabel());
		if (title == null) {
			title = LaunchConfigurationsMessages.LaunchConfigurationDialog_Launch_Configurations_18; 
		}
		return title;
	}

	/**
 	 * Returns the current tab group
 	 * 
 	 * @return the current tab group, or <code>null</code> if none
 	 */
 	public ILaunchConfigurationTabGroup getTabGroup() {
 		if (getTabViewer() != null) {
 			return getTabViewer().getTabGroup();
 		}
 		return null;
 	}

	/* (non-Javadoc)
 	 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#getTabs()
 	 */
 	public ILaunchConfigurationTab[] getTabs() {
 		if (getTabGroup() == null) {
 			return null;
 		} 
 		return getTabGroup().getTabs();
 	}

	/**
	 * Returns the viewer used to display the tabs for a launch configuration.
	 * 
	 * @return LaunchConfigurationTabGroupViewer
	 */
	protected LaunchConfigurationTabGroupViewer getTabViewer() {
		return fTabViewer;
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
	 * Notification that selection has changed in the launch configuration tree.
	 * <p>
	 * If the currently displayed configuration is not saved,
	 * prompt for saving before moving on to the new selection.
	 * </p>
	 * 
	 * @param event selection changed event
	 */
 	protected void handleLaunchConfigurationSelectionChanged(SelectionChangedEvent event) {
 		Object input = getTabViewer().getInput();
 		Object newInput = null;
 		ISelection selection = event.getSelection();
 		if (!selection.isEmpty()) {
 			if (selection instanceof IStructuredSelection) {
 				//fix for bug 111079
 				Button button = getButton(ID_LAUNCH_BUTTON);
 				IStructuredSelection structuredSelection = (IStructuredSelection)selection;
 				if (structuredSelection.size() == 1) {
 					newInput = structuredSelection.getFirstElement();
 					button.setEnabled(true);
 				}//end if
 				else {
 					button.setEnabled(false);
 				}//end else
 			}
 		}
 		ILaunchConfiguration original = getTabViewer().getOriginal();
 		if (original != null && newInput == null && fLaunchManager.getMovedTo(original) != null) {
			// the current config is about to be deleted ignore this change
			return;
		}
 		
 		if (!isEqual(input, newInput)) {
 			ILaunchConfigurationTabGroup group = getTabGroup();
 			if (original != null) {
 				boolean deleted = !original.exists();
 				boolean renamed = false;
 				if (newInput instanceof ILaunchConfiguration) {
 					ILaunchConfiguration lc = (ILaunchConfiguration)newInput;
 					renamed = fLaunchManager.getMovedFrom(lc) != null;
 				}
	 			if (getTabViewer().isDirty() && !deleted && !renamed) {
	 				IStructuredSelection sel;
	 				if (!showUnsavedChangesDialog()) {
	 					sel = new StructuredSelection(input);
	 				}
	 				else {
	 					sel = new StructuredSelection(newInput);
	 				}
	 				fLaunchConfigurationView.getViewer().setSelection(sel);
	 				return;
	 			}
 			}
			getTabViewer().setInput(newInput);
 			// bug 14758 - if the newly selected config is dirty, save its changes
 			if (getTabViewer().isDirty()) {
 				getTabViewer().handleApplyPressed();
 			} 
 			// bug 14758			
 			ILaunchConfigurationTabGroup newGroup = getTabGroup();
 			if (!isEqual(group, newGroup)) {
 				if (isVisible()) {
 					resize();
 				}
 			}
 		}
  	}
	
	/**
	 * Notification the 'launch' button has been pressed.
	 * Save and launch.
	 */
	protected void handleLaunchPressed() {
		ILaunchConfiguration config = getTabViewer().getOriginal(); 
		if (getTabViewer().isDirty()) {
			getTabViewer().handleApplyPressed();
			config = getTabViewer().getOriginal();
		}
		String mode = getMode();
		close();
		if(config != null) {
			DebugUITools.launch(config, mode);
		}//end if
	}

	/**
	 * Consult a status handler for the given status, if any. The status handler
	 * is passed this launch config dialog as an argument.
	 * 
	 * @param status the status to be handled
	 */
	public void handleStatus(IStatus status) {		
		IStatusHandler handler = DebugPlugin.getDefault().getStatusHandler(status);
		if (handler != null) {
			try {
				handler.handleStatus(status, this);
				return;
			} catch (CoreException e) {
				status = e.getStatus();
			} 
		}
		// if no handler, or handler failed, display error/warning dialog
		String title = null;
		switch (status.getSeverity()) {
			case IStatus.ERROR:
				title = LaunchConfigurationsMessages.LaunchConfigurationsDialog_Error_1; 
				break;
			case IStatus.WARNING:
				title = LaunchConfigurationsMessages.LaunchConfigurationsDialog_Warning_2; 
				break;
			default:
				title = LaunchConfigurationsMessages.LaunchConfigurationsDialog_Information_3; 
				break;
		}
		ErrorDialog.openError(getShell(), title, null, status);
	}
	
	/**
	 * Notification that tab selection has changed.
	 *
	 */
	protected void handleTabSelectionChanged() {
		updateMessage();
		firePageChanged(new PageChangedEvent(this, getSelectedPage()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#initializeBounds()
	 */
	protected void initializeBounds() {
		if (fSashForm != null) {
			IDialogSettings settings = getDialogSettings();
			int w1, w2;
			try {
				w1 = settings.getInt(DIALOG_SASH_WEIGHTS_1);
				w2 = settings.getInt(DIALOG_SASH_WEIGHTS_2);
			}
			catch(NumberFormatException nfe) {
				w1 = DEFAULT_SASH_WEIGHTS[0];
				w2 = DEFAULT_SASH_WEIGHTS[1];
			}
			fSashForm.setWeights(new int[] {w1, w2});
		}
		resize();
	}

	/**
	 * Performs initialization of the content by setting the initial tree selection
	 */
	protected void initializeContent() {
		doInitialTreeSelection();
		IStatus status = getInitialStatus();
		if (status != null) {
			handleStatus(status);
		}
	}
	
	/**
	 * Compares two objects to determine their equality
	 * 
	 * @param o1 the first object
	 * @param o2 the object to compare to object one
	 * @return true if they are equal, false if object 1 is null, the result of o1.equals(o2) otherwise
	 */
	protected boolean isEqual(Object o1, Object o2) {
  		if (o1 == o2) {
  			return true;
  		} else if (o1 == null) {
  			return false;
  		} else {
  			return o1.equals(o2);
  		}
  	}

	/**
	 * Returns whether the dialog can be closed
	 * 
	 * @return whether the dialog can be closed
	 */
	protected boolean isSafeToClose() {
	    return fActiveRunningOperations == 0;
	}
	
	/**
	 * Returns whether this dialog is currently open
	 * 
	 * @return if the current shell is visible or not
	 */
	private boolean isVisible() {
		return getShell() != null && getShell().isVisible();
	}
	
	/**
	 * Determine the initial configuration for this dialog.
	 * Open the dialog in the mode set using #setOpenMode(int) and return one of
	 * <code>Window. OK</code> or <code>Window.CANCEL</code>.
	 * 
	 * @see org.eclipse.jface.window.Window#open()
	 * @return the int status of opening the dialog
	 */
	public int open() {		
		int mode = getOpenMode();
		setCurrentlyVisibleLaunchConfigurationDialog(this);	
		if (mode == LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_LAST_LAUNCHED) {
			ILaunchConfiguration lastLaunchedConfig = getLastLaunchedWorkbenchConfiguration();
			if (lastLaunchedConfig != null) {
				setInitialSelection(new StructuredSelection(lastLaunchedConfig));
			}			
		}
		return super.open();
	}
	
	/**
	 * Save the current sash weights
	 */
	protected void persistSashWeights() {
		IDialogSettings settings = getDialogSettings();
		if (fSashForm != null) {
			int[] sashWeights = fSashForm.getWeights();
			settings.put(DIALOG_SASH_WEIGHTS_1, sashWeights[0]);
			settings.put(DIALOG_SASH_WEIGHTS_2, sashWeights[1]);
		}
	}
	
	/**
	 * Update buttons and message.
	 */
	protected void refreshStatus() {
		updateMessage();
		updateButtons();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IPageChangeProvider#removePageChangedListener(org.eclipse.jface.dialogs.IPageChangedListener)
	 */
	public void removePageChangedListener(IPageChangedListener listener) {
		changeListeners.remove(listener);
	}

	/**
	 * resize the dialog to show all relevant content
	 */
	protected void resize() {
		if(getTabGroup() != null) {
			Point contentSize = fTabViewer.getTabFolder().computeSize(SWT.DEFAULT, SWT.DEFAULT);
			int maxAllowedWidth = (int) (getDisplay().getBounds().width * MAX_DIALOG_WIDTH_PERCENT);
			int otherWidth = fSashForm.SASH_WIDTH + fSelectionArea.getBounds().width;
			int totalWidth = contentSize.x + otherWidth;
			if (totalWidth > maxAllowedWidth) {
				contentSize.x = maxAllowedWidth - otherWidth;
			}
			if(contentSize.x < DEFAULT_INITIAL_DIALOG_SIZE.x) {
				contentSize.x = DEFAULT_INITIAL_DIALOG_SIZE.x;
			}
			int maxAllowedHeight = (int) (getDisplay().getBounds().height * MAX_DIALOG_HEIGHT_PERCENT);
			contentSize.y = Math.min(contentSize.y, maxAllowedHeight);
			if(contentSize.y < DEFAULT_INITIAL_DIALOG_SIZE.y) {
				contentSize.y = DEFAULT_INITIAL_DIALOG_SIZE.y;
			}
			fEditArea.layout(true);
			Rectangle rect = fTabViewer.getTabFolder().getClientArea();
			Point containerSize = new Point(rect.width, rect.height);
			int hdiff = contentSize.x - containerSize.x;
			int vdiff = contentSize.y - containerSize.y;
			if (hdiff > 0 || vdiff > 0) {
				int[] newSashWeights = null;
				if (hdiff > 0) {
					newSashWeights = new int[2];
					int lhs = fSelectionArea.getBounds().width;
					if(lhs < 170) {
						lhs = 190;
					}
					newSashWeights[0] = lhs;
					newSashWeights[1] = fEditArea.getBounds().width+hdiff;
				}
				Point shellSize = getShell().getSize();
				//padding for margins, etc.
				int offset = 10;
				if(getTabGroup() != null) {
					offset = fTabViewer.getTabGroup().getTabs().length*3;
				}
				setShellSize(shellSize.x + Math.max(0, hdiff) + offset, shellSize.y + Math.max(0, vdiff));
				if (newSashWeights != null) {
					fSashForm.setWeights(newSashWeights);
				}
			} 
		}
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
			if (b != null) {
				w.setEnabled(b.booleanValue());
			}
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
		restoreEnableState(getButton(ID_LAUNCH_BUTTON), state, "launch");//$NON-NLS-1$
		restoreEnableState(getButton(ID_CLOSE_BUTTON), state, "close");//$NON-NLS-1$
		ControlEnableState treeState = (ControlEnableState) state.get("selectionarea");//$NON-NLS-1$
		if (treeState != null) {
			treeState.restore();
		}
		ControlEnableState tabState = (ControlEnableState) state.get("editarea");//$NON-NLS-1$
		tabState.restore();
	}

	/***************************************************************************************
	 * 
	 * ProgressMonitor & IRunnableContext related methods
	 * 
	 ***************************************************************************************/

	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		if (isVisible()) {
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
				fProgressMonitorCancelButton.setCursor(arrowCursor);
				fProgressMonitorCancelButton.setEnabled(true);
				
				// Deactivate shell
				savedState = saveUIState();
				if (focusControl != null) {
					savedState.put(FOCUS_CONTROL, focusControl);
				}	
				// Attach the progress monitor part to the cancel button
				fProgressMonitorPart.attachToCancelComponent(fProgressMonitorCancelButton);
				fProgressMonitorPart.getParent().setVisible(true);
				fProgressMonitorCancelButton.setFocus();
			}
			fActiveRunningOperations++;
			try {
				ModalContext.run(runnable, fork, fProgressMonitorPart, getShell().getDisplay());
			} 
			finally {
				fActiveRunningOperations--;
				stopped(savedState);
			}
		} 
		else {
			PlatformUI.getWorkbench().getProgressService().run(fork, cancelable, runnable);
		}
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
			h.put(key, Boolean.valueOf(w.isEnabled()));
			w.setEnabled(enabled);
		}
	}

	/**
	 * Captures and returns the enabled/disabled state of the wizard dialog's
	 * buttons and the tree of controls for the currently showing page. All
	 * these controls are disabled in the process, with the possible exception of
	 * the Cancel button.
	 *
	 * @return a map containing the saved state suitable for restoring later
	 *   with <code>restoreUIState</code>
	 * @see #restoreUIState
	 */
	private Map saveUIState() {
		Map savedState= new HashMap(4);
		saveEnableStateAndSet(getButton(ID_LAUNCH_BUTTON), savedState, "launch", false);//$NON-NLS-1$
		saveEnableStateAndSet(getButton(ID_CLOSE_BUTTON), savedState, "close", false);//$NON-NLS-1$
		if (fSelectionArea != null) {
			savedState.put("selectionarea", ControlEnableState.disable(fSelectionArea));//$NON-NLS-1$
		}
		savedState.put("editarea", ControlEnableState.disable(getEditArea()));//$NON-NLS-1$
		return savedState;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#setActiveTab(org.eclipse.debug.ui.ILaunchConfigurationTab)
	 */
	public void setActiveTab(ILaunchConfigurationTab tab) {
		getTabViewer().setActiveTab(tab);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#setActiveTab(int)
	 */
	public void setActiveTab(int index) {
		getTabViewer().setActiveTab(index);
	}

	/**
	 * Sets the given cursor for all shells currently active
	 * for this window's display.
	 *
	 * @param cursor the cursor
	 */
	private void setDisplayCursor(Cursor cursor) {
		Shell[] shells = getShell().getDisplay().getShells();
		for (int i = 0; i < shells.length; i++) {
			shells[i].setCursor(cursor);
		}
	}
	
	/**
	 * Sets the launch configuration edit area control.
	 * 
	 * @param editArea control
	 */
	protected void setEditArea(Composite editArea) {
		fEditArea = editArea;
	}
	
	/**
	 * Sets the initial selection for the dialog when opened in 
	 * <code>LAUNCH_CONFIGURATION_DIALOG_OPEN_ON_SELECTION</code> mode.
	 */
	public void setInitialSelection(IStructuredSelection selection) {
		fInitialSelection = selection;
	}
	
	/**
	 * Sets the status to open the dialog on.
	 * 
	 * @param status the initial status for the dialog
	 */
	public void setInitialStatus(IStatus status) {
		fInitialStatus = status;
	}
	
	/**
	 * Sets the launch group to display.
	 * 
	 * @param group launch group
	 */
	protected void setLaunchGroup(LaunchGroupExtension group) {
		fGroup = group;
	}
	
	/**
	 * Set the title area image based on the mode this dialog was initialized with
	 */
	protected void setModeLabelState() {
		setTitleImage(getBannerImage());
	}		

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#setName(java.lang.String)
	 */
	public void setName(String name) {
		getTabViewer().setName(name);
	}
	
	/**
	 * Set the flag indicating how this dialog behaves when the <code>open()</code> method is called.
	 * Valid values are defined by the LAUNCH_CONFIGURATION_DIALOG... constants in this class.
	 */
	public void setOpenMode(int mode) {
		fOpenMode = mode;
	}

	/**
 	 * Increase the size of this dialog's <code>Shell</code> by the specified amounts.
 	 * Do not increase the size of the Shell beyond the bounds of the Display.
 	 */
	protected void setShellSize(int width, int height) {
		Rectangle bounds = getShell().getMonitor().getBounds();
		getShell().setSize(Math.min(width, bounds.width), Math.min(height, bounds.height));
	}

	/**
	 * Sets the viewer used to display the tabs for a launch configuration.
	 * 
	 * @param viewer the new view to set
	 */
	protected void setTabViewer(LaunchConfigurationTabGroupViewer viewer) {
		fTabViewer = viewer;
	}

	/**
	 * Create and return a dialog that asks the user whether they want to discard
	 * unsaved changes.
	 *   
	 * @return Return <code>true</code> if they chose to discard changes,
	 * <code>false</code> otherwise.
	 */
	private boolean showDiscardChangesDialog() {
		StringBuffer buffer = new StringBuffer(MessageFormat.format(LaunchConfigurationsMessages.LaunchConfigurationDialog_The_configuration___35, new String[]{getTabViewer().getWorkingCopy().getName()})); 
		buffer.append(getTabViewer().getErrorMesssage());
		buffer.append(LaunchConfigurationsMessages.LaunchConfigurationDialog_Do_you_wish_to_discard_changes_37); 
		MessageDialog dialog = new MessageDialog(getShell(), 
												 LaunchConfigurationsMessages.LaunchConfigurationDialog_Discard_changes__38, 
												 null,
												 buffer.toString(),
												 MessageDialog.QUESTION,
												 new String[] {LaunchConfigurationsMessages.LaunchConfigurationDialog_Yes_32, LaunchConfigurationsMessages.LaunchConfigurationDialog_No_33}, // 
												 1);
		if (dialog.open() == IDialogConstants.OK_ID) {
			return true;
		}
		return false;
	}

	/**
	 * Create and return a dialog that asks the user whether they want to save
	 * unsaved changes.  
	 * 
	 * @return Return <code>true </code> if they chose to save changes,
	 * <code>false</code> otherwise.
	 */
	private boolean showSaveChangesDialog() {
		String message = MessageFormat.format(LaunchConfigurationsMessages.LaunchConfigurationDialog_The_configuration___29, new String[]{getTabViewer().getWorkingCopy().getName()}); 
		MessageDialog dialog = new MessageDialog(getShell(), 
												 LaunchConfigurationsMessages.LaunchConfigurationDialog_Save_changes__31, 
												 null,
												 message,
												 MessageDialog.QUESTION,
												 new String[] {LaunchConfigurationsMessages.LaunchConfigurationDialog_Yes_32, LaunchConfigurationsMessages.LaunchConfigurationDialog_No_33},  
												 0);
		int ret = dialog.open();
		if (ret == IDialogConstants.OK_ID) {
			fLaunchConfigurationView.setAutoSelect(false);
			getTabViewer().handleApplyPressed();
			fLaunchConfigurationView.setAutoSelect(true);
			return true;
		}
		return false;
	}

	/**
	 * Show the user a dialog appropriate to whether the unsaved changes in the current config
	 * can be saved or not.  Return <code>true</code> if the user indicated that they wish to replace
	 * the current config, either by saving changes or by discarding the, return <code>false</code>
	 * otherwise.
	 * 
	 * @return returns the <code>showSaveChangesDialog</code> return value
	 */
	private boolean showUnsavedChangesDialog() {
		if (getTabViewer().canSave()) {
			return showSaveChangesDialog();
		}
		return showDiscardChangesDialog();
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
			fProgressMonitorPart.getParent().setVisible(false);
			fProgressMonitorPart.removeFromCancelComponent(fProgressMonitorCancelButton);
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#updateButtons()
	 */
	public void updateButtons() {
		// New, Delete, & Duplicate toolbar actions
 		getNewAction().setEnabled(getNewAction().isEnabled());
		getDeleteAction().setEnabled(getDeleteAction().isEnabled());
		getDuplicateAction().setEnabled(getDuplicateAction().isEnabled());
		
		// Launch button
		getTabViewer().refresh();
		getButton(ID_LAUNCH_BUTTON).setEnabled(getTabViewer().canLaunch());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#updateMessage()
	 */
	public void updateMessage() {
		setErrorMessage(getTabViewer().getErrorMesssage());
		setMessage(getTabViewer().getMessage());				
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(final PropertyChangeEvent event) {
		WorkbenchJob job = new WorkbenchJob("") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
				TreeViewer viewer = fLaunchConfigurationView.getTreeViewer();
				TreeSelection sel = (TreeSelection)viewer.getSelection();
				TreePath path = null;
				int pidx = -1, cidx = -1;
				if(!sel.isEmpty()) {
					path = sel.getPaths()[0];
					pidx = findIndexOfParent(path.getFirstSegment());
					cidx = findIndexOfChild(path.getFirstSegment(), path.getLastSegment());
				}
				boolean newvalue = Boolean.valueOf(event.getNewValue().toString()).booleanValue();
				if(event.getProperty().equals(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_CLOSED)) {
					updateFilter(newvalue, fClosedProjectFilter);
				}
				else if(event.getProperty().equals(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_DELETED)) {
					updateFilter(newvalue, fDeletedProjectFilter);
				}
				else if(event.getProperty().equals(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_TYPES)) {
					updateFilter(newvalue, fLCTFilter);
				}
				else if(event.getProperty().equals(IInternalDebugUIConstants.PREF_FILTER_WORKING_SETS)) {
					updateFilter(newvalue, fWorkingSetsFilter);
				}
				else if(event.getProperty().equals(IInternalDebugUIConstants.PREF_FILTER_TYPE_LIST)) {
					if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_TYPES)) {
						viewer.removeFilter(fLCTFilter);
						viewer.addFilter(fLCTFilter);
					}
				}
				viewer.expandAll();
				refreshFilteringLabel();
				updateSelection(path, pidx, cidx);
				return null;
			}
		};
		
		job.runInUIThread(new NullProgressMonitor());
	}

	/**
	 * Updates the state of a filter based on the state variable
	 * @param state if the filter needs to be added or removed, true indicates add, false indicates remove
	 * @param filter the filter to update
	 */
	private void updateFilter(boolean state, ViewerFilter filter) {
		TreeViewer viewer = (TreeViewer)fLaunchConfigurationView.getViewer();
		if(state) {
			viewer.addFilter(filter);
		}
		else {
			viewer.removeFilter(filter);
		}
	}
	
	/**
	 * updates the selection after a filtering has taken place
	 * @param path the <code>TreePath</code> to the last selected item
	 * @param pidx the original index of the parent item
	 * @param cidx the original index of the child item
	 * @since 3.2
	 */
	private void updateSelection(TreePath path, int pidx, int cidx) {
		TreeViewer viewer = fLaunchConfigurationView.getTreeViewer();
		Tree tree = viewer.getTree();
		int pcount = tree.getItemCount();
		if(tree.getItemCount() == 0) {
			setErrorMessage(null);
			setMessage(LaunchConfigurationsMessages.LaunchConfigurationsDialog_7);
			updateButtons();
		}
		else if(path != null) {
			Object sel = path.getLastSegment();
			int pidex = findIndexOfParent(path.getFirstSegment());
			if(path.getSegmentCount() == 1) {
				if(pidex == -1) {
					if(pidx > pcount) {
						pidx = pcount-1;
					}
					sel = (pidx == 0 ? tree.getItem(pidx).getData() : tree.getItem(pidx-1).getData());
				}
				else {
					sel = tree.getItem(pidex).getData();
				}
			}
			else {
				if(pidex == -1) {
					if(pidx > pcount) {
						pidx = pcount-1;
					}
					sel = (pidx == 0 ? tree.getItem(pidx).getData() : tree.getItem(pidx-1).getData());
				}
				else {
					int cidex = findIndexOfChild(path.getFirstSegment(), path.getLastSegment());
					TreeItem parent = tree.getItem(pidex);
					int ccount = parent.getItemCount();
					if(cidex == -1) {
						if(parent.getItemCount() == 0) {
							sel = parent.getData();
						}
						else {
							if(cidx > ccount) {
								cidx = ccount-1;
							}
							sel = (cidx == 0 ? parent.getItem(cidx).getData() : parent.getItem(cidx-1).getData());
						}
					}
					else {
						sel = parent.getItem(cidex).getData();
					}
				}
			}
			viewer.setSelection(new StructuredSelection(sel));
		}
		else {
			setErrorMessage(null);
			setMessage(LaunchConfigurationsMessages.LaunchConfigurationDialog_Ready_to_launch_2);
		}
	}
	
	/**
	 * finds the given parent item in the viewer, in this case the parent item will always be an
	 * <code>ILaunchConfigurationType</code>
	 * @param parent the parent item to find
	 * @return the index of the parent item or -1 if not found
	 * @since 3.2
	 */
	private int findIndexOfParent(Object parent) {
		Tree tree = fLaunchConfigurationView.getTreeViewer().getTree();
		TreeItem[] roots = tree.getItems();
		for(int i = 0; i < roots.length; i++) {
			if(roots[i].getData().equals(parent)) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Finds the index of a child item in the entire tree using the parent node and the child node
	 * derived from a <code>TreePath</code>
	 * @param parent the parent, in this case always an <code>ILaunchConfigurationType</code>
	 * @param child the child to find within the parent, in this case always an <code>ILaunchConfiguration,</code>
	 * @return the index of the child or -1 if not found
	 * @since 3.2
	 */
	private int findIndexOfChild(Object parent, Object child) {
		Tree tree = fLaunchConfigurationView.getTreeViewer().getTree();
		int pidx = findIndexOfParent(parent);
		if(pidx != -1) {
			TreeItem root = tree.getItem(pidx);
			TreeItem[] children = root.getItems();
			for(int j = 0; j < children.length; j++) {
				if(children[j].getData().equals(child)) {
					return j;
				}
			}
		}
		return -1;
	}
}