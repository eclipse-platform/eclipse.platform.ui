/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids - Bug 137923
 *******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

import com.ibm.icu.text.MessageFormat;
 
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
	 * The id for the 'No' button on the discard changes message box
	 * @since 3.3
	 */
	protected static final int ID_DISCARD_BUTTON = IDialogConstants.CLIENT_ID + 4;
	
	/**
	 * Constant specifying how wide this dialog is allowed to get (as a percentage of
	 * total available screen width) as a result of tab labels in the edit area.
	 */
	protected static final float MAX_DIALOG_WIDTH_PERCENT = 0.75f;
	/**
	 * Constant specifying how tall this dialog is allowed to get (as a percentage of
	 * total available screen height) as a result of preferred tab size.
	 */
	protected static final float MAX_DIALOG_HEIGHT_PERCENT = 0.60f;
	/**
	 * Size of this dialog if there is no preference specifying a size.
	 */
	protected static final Point DEFAULT_INITIAL_DIALOG_SIZE = new Point(800, 640);
	/**
	 * defines some default sashweights when we have a new workspace
	 * @since 3.2
	 */
	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] {190, 610};
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
	 * defines the empty string
	 * @since 3.2
	 */
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	/**
	 * defines the delimiter used in the persistence of the expanded state
	 * @since 3.2
	 */
	private static final String DELIMITER = ", "; //$NON-NLS-1$
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
	private static final String DIALOG_EXPANDED_NODES = IDebugUIConstants.PLUGIN_ID + ".EXPANDED_NODES"; //$NON-NLS-1$
	
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
	 * widgets
	 */
	private Control fLastControl;
	private SashForm fSashForm;
	private LaunchConfigurationView fLaunchConfigurationView;
	private LaunchConfigurationTabGroupViewer fTabViewer;
	private Button fProgressMonitorCancelButton;	
	private ProgressMonitorPart fProgressMonitorPart;
	private LaunchGroupExtension fGroup;
	private Image fBannerImage;
	
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
	 * Listener for a list
	 */
	private ListenerList changeListeners = new ListenerList();
	
	/**
	 * The number of 'long-running' operations currently taking place in this dialog
	 */	
	private long fActiveRunningOperations = 0;

	/**
	 * Double-click action
	 */
	private IAction fDoubleClickAction;
	
	/**
	 * Filters for the LCD
	 * @since 3.2
	 */
	private ClosedProjectFilter fClosedProjectFilter;
	private DeletedProjectFilter fDeletedProjectFilter;
	private LaunchConfigurationTypeFilter fLCTFilter;
	private WorkingSetsFilter fWorkingSetsFilter;

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
		} 
		else if (buttonId == ID_CLOSE_BUTTON) {
			handleClosePressed();
		} 
		else {
			super.buttonPressed(buttonId);
		}
	}

	/**
	 * Return whether the current configuration should be saved or discarded.  This involves determining
	 * if it is dirty, and if it is, asking the user what to do.
	 * 
	 * @return if we can discard the current config or not
	 */
	protected int shouldSaveCurrentConfig() {				
		if (getTabViewer().isDirty()) {
			if (getTabViewer().canSave()) {
				return showSaveChangesDialog();
			}
			return showUnsavedChangesDialog();
		}
		return IDialogConstants.NO_ID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
	    if (!isSafeToClose()) {
	        return false;
	    }
	    persistSashWeights();
	    persistExpansion();
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
        if (isHelpAvailable()) {
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

		/*
		 * Create the rest of the button bar, but tell it not to
		 * create a help button (we've already created it).
		 */
		boolean helpAvailable = isHelpAvailable();
		setHelpAvailable(false);
		super.createButtonBar(composite);
		setHelpAvailable(helpAvailable);
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
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(), getHelpContextId());
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
		tmanager.add(new Separator());
		tmanager.add(getCollapseAllAction());
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
        toolBar.setBackground(parent.getBackground());
        ToolBarManager toolBarManager= new ToolBarManager(toolBar);
        viewForm.setTopLeft(toolBar);
        viewForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Composite viewFormContents = new Composite(viewForm, SWT.FLAT);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 5;
        gridLayout.marginWidth = 5;
        viewFormContents.setLayout(gridLayout);
        viewFormContents.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		fLaunchConfigurationView = new LaunchConfigurationView(getLaunchGroup(), createViewerFilters());
		fLaunchConfigurationView.createLaunchDialogControl(viewFormContents);
		
	//create toolbar actions, we reuse the actions from the view so we wait until after
	//the view is created to add them to the toolbar
		createToolbarActions(toolBarManager);
		fDoubleClickAction = new Action() {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection)fLaunchConfigurationView.getViewer().getSelection();
				Object target = selection.getFirstElement();
				if (target instanceof ILaunchConfiguration) {
					if (getTabViewer().canLaunch() & getTabViewer().canLaunchWithModes() & !getTabViewer().hasDuplicateDelegates()) {
						handleLaunchPressed();
					}
				} else {
					getNewAction().run();
				}
			}
		};
		fLaunchConfigurationView.setAction(IDebugView.DOUBLE_CLICK_ACTION, fDoubleClickAction);
		Viewer viewer = fLaunchConfigurationView.getViewer();
		
		Control control = viewer.getControl();
		GridData gd = new GridData(GridData.FILL_BOTH);
		control.setLayoutData(gd);
        viewForm.setContent(viewFormContents);
		AbstractLaunchConfigurationAction.IConfirmationRequestor requestor = new AbstractLaunchConfigurationAction.IConfirmationRequestor() {
			public boolean getConfirmation() {
				int status = shouldSaveCurrentConfig();
				if(status == IDialogConstants.YES_ID) {
					fTabViewer.handleApplyPressed();
					return true;
				}
				else if(status == IDialogConstants.NO_ID) {
					fTabViewer.handleRevertPressed();
					return true;
				}
				return false;
			}
		};
		getDuplicateAction().setConfirmationRequestor(requestor);
		getNewAction().setConfirmationRequestor(requestor);
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
	 * Create the filters to be initially applied to the viewer.
	 * The initial filters are based on the persisted preferences
	 * @return the array of initial filters
	 * @since 3.2
	 */
	private ViewerFilter[] createViewerFilters() {
		ArrayList filters = new ArrayList();
		fClosedProjectFilter = new ClosedProjectFilter();
		if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_CLOSED)) {
			filters.add(fClosedProjectFilter);
		}
		fDeletedProjectFilter = new DeletedProjectFilter();
		if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_DELETED)) {
			filters.add(fDeletedProjectFilter);
		}
		fLCTFilter = new LaunchConfigurationTypeFilter();
		if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_FILTER_LAUNCH_TYPES)) {
			filters.add(fLCTFilter);
		}
		fWorkingSetsFilter = new WorkingSetsFilter();
		if(DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_FILTER_WORKING_SETS)) {
			filters.add(fWorkingSetsFilter);
		}
		return (ViewerFilter[]) filters.toArray(new ViewerFilter[filters.size()]);
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
			name = EMPTY_STRING;
		}
		return getLaunchManager().generateUniqueLaunchConfigurationNameFrom(name);
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
	
	/**
	 * Gets the collapse all action
	 * @return the collapse all action
	 * @since 3.2
	 */
	protected IAction getCollapseAllAction() {
		return fLaunchConfigurationView.getAction(CollapseAllLaunchConfigurationAction.ID_COLLAPSEALL_ACTION);
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
		int status = shouldSaveCurrentConfig();
		if(status != IDialogConstants.CANCEL_ID) {
			if(status != ID_DISCARD_BUTTON) {
				if(status == IDialogConstants.YES_ID) {
					getTabViewer().handleApplyPressed();
				}
				cancelPressed();
			}
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
 		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 		if (!selection.isEmpty()) {
			if (selection.size() == 1) {
				newInput = selection.getFirstElement();
			}
 		}
 		if (!isEqual(input, newInput)) {
 			LaunchConfigurationTabGroupViewer viewer = getTabViewer();
 			ILaunchConfiguration original = viewer.getOriginal();
 	 		if (original != null && newInput == null && getLaunchManager().getMovedTo(original) != null) {
 				return;
 			}
 			boolean deleted = false;
 			if (original != null) {
 				deleted = !original.exists();
 			}
			boolean renamed = false;
			if (newInput instanceof ILaunchConfiguration) {
				renamed = getLaunchManager().getMovedFrom((ILaunchConfiguration)newInput) != null;
			}
			Object in = input;
 			if (viewer.isDirty() && !deleted && !renamed) {
 				if(fLaunchConfigurationView != null) {
 					fLaunchConfigurationView.setAutoSelect(false);
 				}
 				int ret = showUnsavedChangesDialog();
 				boolean cansave = viewer.canSave();
 				if(ret == IDialogConstants.YES_ID) {
 					if(cansave) {
 						viewer.handleApplyPressed();
 					}
 					else {
 						viewer.handleRevertPressed();
 					}
 					in = newInput;
 				}
 				else if(ret == IDialogConstants.NO_ID) {
 					if(cansave) {
 						viewer.handleRevertPressed();
 						if(viewer.isDirty()) {
 							viewer.handleApplyPressed();
 						}
 						in = newInput;
 					}
 				}
 				if(fLaunchConfigurationView != null) {
	 				if(in != null) {
	 	 				fLaunchConfigurationView.getViewer().setSelection(new StructuredSelection(in));
	 	 			}
 					fLaunchConfigurationView.setAutoSelect(true);
 				}
  			}
 			else {
 				viewer.setInput(newInput);
 				if(newInput != null) {
	 				if(viewer.isDirty()) {
	 					viewer.handleApplyPressed();
	 				}
	 				if (getShell() != null && getShell().isVisible()) {
						resize();
	 				}
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
		if (getTabViewer().isDirty() & getTabViewer().canSave()) {
			getTabViewer().handleApplyPressed();
			config = getTabViewer().getOriginal();
		}
		String mode = getMode();
		close();
		if(config != null) {
			DebugUITools.launch(config, mode);
		}
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
			} 
			catch (CoreException e) {status = e.getStatus();} 
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
		IDialogSettings settings = getDialogSettings();
		if (fSashForm != null) {
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
		super.initializeBounds();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		IDialogSettings settings = getDialogSettings();
		if(settings.get(DIALOG_SASH_WEIGHTS_1) != null) {
			return super.getInitialSize();
		}
		return DEFAULT_INITIAL_DIALOG_SIZE;
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
		fLaunchConfigurationView.getFilteringTextControl().setFocus();
		restoreExpansion();
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
	 * saves which of the nodes are expanded at the time the dialog is closed
	 * @since 3.2
	 */
	protected void persistExpansion() {
		if(fLaunchConfigurationView != null) {
			IDialogSettings settings = getDialogSettings();
			TreeItem[] items = fLaunchConfigurationView.getTreeViewer().getTree().getItems();
			String value = settings.get(DIALOG_EXPANDED_NODES);
			if(value == null) {
				value = EMPTY_STRING;
			}
			ArrayList list = new ArrayList();
			String[] persisted = value.split(DELIMITER); 
			for(int i = 0; i < persisted.length; i++) {
				list.add(persisted[i]);
			}
			String type = EMPTY_STRING;
			//if the item is not in the list and is expanded add it, otherwise if it
			//is not expanded do a remove...either way for the else we query the list
			for(int i = 0; i < items.length; i++) {
				type = ((ILaunchConfigurationType)items[i].getData()).getIdentifier();
				if(!list.contains(type) & items[i].getExpanded()) {
					list.add(type);
				}
				else if(!items[i].getExpanded()) {
					list.remove(type);
				}
			}
			value = EMPTY_STRING;
			//build the preference string
			for(Iterator iter = list.iterator(); iter.hasNext();) {
				value += iter.next() + DELIMITER;
			}
			settings.put(DIALOG_EXPANDED_NODES, value);
		}
	}
	
	/**
	 * Restore the original expansion state of the nodes in the viewer
	 * @since 3.2
	 */
	protected void restoreExpansion() {
		if(fLaunchConfigurationView != null) {
			IDialogSettings settings = getDialogSettings();
			String value = settings.get(DIALOG_EXPANDED_NODES);
			if(value != null) {
				String[] nodes = value.split(DELIMITER);
				TreeItem[] items = fLaunchConfigurationView.getTreeViewer().getTree().getItems();
				ArrayList toexpand = new ArrayList();
				// if we have a selection make sure it is expanded
				if(fInitialSelection != null && !fInitialSelection.isEmpty()) {
					Object obj = fInitialSelection.getFirstElement();
					if(obj instanceof ILaunchConfigurationType) {
						toexpand.add(obj);
					}
					else if(obj instanceof ILaunchConfiguration) {
						try {
							toexpand.add(((ILaunchConfiguration) obj).getType());
						} 
						catch (CoreException e) {DebugUIPlugin.log(e);}					
					}
				}
				for(int i = 0; i < nodes.length; i++) {
					for(int k = 0; k < items.length; k++) {
						ILaunchConfigurationType type = (ILaunchConfigurationType)items[k].getData();
						if(type.getIdentifier().equals(nodes[i])) {
							toexpand.add(type);
						}
					}
				}
				fLaunchConfigurationView.getTreeViewer().setExpandedElements(toexpand.toArray());
			}
		}
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
	 * resize the dialog to show all relevant content, maintains aspect in all resolutions down to 1024x768
	 */
	protected void resize() {
 		if(getTabGroup() != null) {
 			Point shell = getShell().getSize();
 			int maxx = (int)(getDisplay().getBounds().width * MAX_DIALOG_WIDTH_PERCENT),
				maxy = (int) (getDisplay().getBounds().height * MAX_DIALOG_HEIGHT_PERCENT);
 			maxx = (maxx < DEFAULT_INITIAL_DIALOG_SIZE.x ? DEFAULT_INITIAL_DIALOG_SIZE.x : maxx);
 			maxy = (maxy < DEFAULT_INITIAL_DIALOG_SIZE.y ? DEFAULT_INITIAL_DIALOG_SIZE.y : maxy);
 			Point psize = getShell().computeSize(SWT.DEFAULT, maxy);
 			if((psize.x > maxx ? maxx : psize.x) > shell.x || (psize.y > maxy ? maxy : psize.y) > shell.y) {
				setShellSize(Math.min(psize.x, maxx), Math.min(psize.y, maxy));
 			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		if (getShell() != null && getShell().isVisible()) {
			// Save focus control
			fLastControl = getShell().getDisplay().getFocusControl();
			if (fLastControl != null && fLastControl.getShell() != getShell()) {
				fLastControl = null;
			}
			fProgressMonitorCancelButton.setEnabled(true);
			// Attach the progress monitor part to the cancel button
			fProgressMonitorPart.attachToCancelComponent(fProgressMonitorCancelButton);
			fProgressMonitorPart.getParent().setVisible(true);
			fProgressMonitorCancelButton.setFocus();
			fActiveRunningOperations++;
			try {
				ModalContext.run(runnable, fork, fProgressMonitorPart, getShell().getDisplay());
			} 
			finally {
				fActiveRunningOperations--;
				if (getShell() != null) {
					fProgressMonitorPart.getParent().setVisible(false);
					fProgressMonitorPart.removeFromCancelComponent(fProgressMonitorCancelButton);
					if (fLastControl != null) {
						fLastControl.setFocus();
					}
				}
			}
		} 
		else {
			PlatformUI.getWorkbench().getProgressService().run(fork, cancelable, runnable);
		}
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
	 * Returns the current launch manager
	 * @return the current launch manager
	 */
	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
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
	 * @return the return code based on the button selected.
	 * The value will be one of <code>YES_ID</code> or <code>NO_ID</code> from
	 * <code>IDialogConstants</code>.
	 */
	private int showDiscardChangesDialog() {
		StringBuffer buffer = new StringBuffer(MessageFormat.format(LaunchConfigurationsMessages.LaunchConfigurationDialog_The_configuration___35, new String[]{getTabViewer().getWorkingCopy().getName()})); 
		buffer.append(getTabViewer().getErrorMesssage());
		buffer.append(LaunchConfigurationsMessages.LaunchConfigurationDialog_Do_you_wish_to_discard_changes_37); 
		MessageDialog dialog = new MessageDialog(getShell(), 
												 LaunchConfigurationsMessages.LaunchConfigurationDialog_Discard_changes__38, 
												 null,
												 buffer.toString(),
												 MessageDialog.QUESTION,
												 new String[] {LaunchConfigurationsMessages.LaunchConfigurationDialog_Yes_32, 
															   LaunchConfigurationsMessages.LaunchConfigurationDialog_No_33},  
												 1);
		int val = IDialogConstants.NO_ID;
		if (dialog.open() == 0) {
			if (fLaunchConfigurationView != null) {
				fLaunchConfigurationView.setAutoSelect(false);
			}
			getTabViewer().handleRevertPressed();
			val = IDialogConstants.YES_ID;
			if (fLaunchConfigurationView != null) {
				fLaunchConfigurationView.setAutoSelect(true);
			}
		}
		if(val == IDialogConstants.NO_ID) {
			val = ID_DISCARD_BUTTON;
		}
		return val;
	}

	/**
	 * Create and return a dialog that asks the user whether they want to save
	 * unsaved changes.  
	 * 
	 * @return the return code based on the button selected.
	 * The value will be one of <code>YES_ID</code>, <code>NO_ID</code>, or <code>CANCEL_ID</code>, from
	 * <code>IDialogConstants</code>.
	 */
	private int showSaveChangesDialog() {
		String message = MessageFormat.format(LaunchConfigurationsMessages.LaunchConfigurationDialog_The_configuration___29, new String[]{getTabViewer().getWorkingCopy().getName()}); 
		MessageDialog dialog = new MessageDialog(getShell(), 
												 LaunchConfigurationsMessages.LaunchConfigurationDialog_Save_changes__31, 
												 null,
												 message,
												 MessageDialog.QUESTION,
												 new String[] {LaunchConfigurationsMessages.LaunchConfigurationDialog_Yes_32, 
															   LaunchConfigurationsMessages.LaunchConfigurationDialog_No_33,
															   LaunchConfigurationsMessages.LaunchConfigurationsDialog_c_ancel},  
												 0);
		int ret = dialog.open();
		int val = IDialogConstants.CANCEL_ID;
		if (ret == 0 || ret == 1) {
			if (ret == 0) {
				val = IDialogConstants.YES_ID;
			} 
			else {
				val = IDialogConstants.NO_ID;
			}
		}
		return val;
	}

	/**
	 * Show the user a dialog appropriate to whether the unsaved changes in the current config
	 * can be saved or not.  Return <code>true</code> if the user indicated that they wish to replace
	 * the current config, either by saving changes or by discarding the, return <code>false</code>
	 * otherwise.
	 * 
	 * @return returns the <code>showSaveChangesDialog</code> return value
	 */
	private int showUnsavedChangesDialog() {
		if (getTabViewer().canSave()) {
			return showSaveChangesDialog();
		}
		return showDiscardChangesDialog();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#updateButtons()
	 */
	public void updateButtons() {
		// New, Delete, & Duplicate toolbar actions
 		getNewAction().setEnabled(getNewAction().isEnabled());
		getDeleteAction().setEnabled(getDeleteAction().isEnabled());
		getDuplicateAction().setEnabled(getDuplicateAction().isEnabled());
		getTabViewer().refresh();
		getButton(ID_LAUNCH_BUTTON).setEnabled(getTabViewer().canLaunch() & getTabViewer().canLaunchWithModes() & !getTabViewer().hasDuplicateDelegates());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationDialog#updateMessage()
	 */
	public void updateMessage() {
		setErrorMessage(getTabViewer().getErrorMesssage());
		setMessage(getTabViewer().getMessage());	
	}
	
	/**
	 * Returns if there is a selection in the tree viewer or not
	 * @return true if something in the tree is selected, false otherwise
	 * @since 3.2
	 */
	public boolean isTreeSelectionEmpty() {
		return fLaunchConfigurationView.getTreeViewer().getSelection().isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(final PropertyChangeEvent event) {
		WorkbenchJob job = new WorkbenchJob(EMPTY_STRING) {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				TreeViewer viewer = fLaunchConfigurationView.getTreeViewer();
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
						viewer.refresh();
					}
				}
				return Status.OK_STATUS;
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
}