package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Arrays;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The dialog used to edit and launch launch configurations.
 */
public class LaunchConfigurationDialog extends Dialog implements ISelectionChangedListener, ILaunchConfigurationListener {
	
	/**
	 * The tree of launch configurations
	 */
	private TreeViewer fConfigTree;
	
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
	
	/**
	 * Constructs a new launch configuration dialog on the given
	 * parent shell.
	 * 
	 * @param shell the parent shell, or <code>null</code> to
	 *  create a top-level shell
	 */
	public LaunchConfigurationDialog(Shell shell) {
		super(shell);
	}
	
	/**
	 * A launch configuration dialog overrides this method
	 * to create a custom set of buttons in the button bar.
	 * This dialog has 'Save & Lanuch', 'Lanuch', and 'Cancel'
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
	 * @see Dialog#createContenst(Composite)
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

// only allow workspace context		
		// Build the project/workspace selection area
		// and put it into the composite.		
		
//		Composite projectSelectionArea = createProjectSelectionArea(composite);
//		gd = new GridData(GridData.FILL_HORIZONTAL);
//		gd.horizontalSpan = 2;
//		projectSelectionArea.setLayoutData(gd);
		
		// Build the launch configuration selection area
		// and put it into the composite.
		Composite launchConfigSelectionArea = createLaunchConfigurationSelectionArea(composite);
		gd = new GridData(GridData.FILL_VERTICAL);
		launchConfigSelectionArea.setLayoutData(gd);
	
		// Build the launch configuration edit area
		// and put it into the composite.
		Composite launchConfigrationEditArea = createLaunchConfigurationEditArea(composite);
		gd = new GridData(GridData.FILL_BOTH);
		launchConfigSelectionArea.setLayoutData(gd);
			
		// Build the separator line
		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);
		
		return composite;
	}
	
	/**
	 * Initialize the dialog settings
	 */
	protected void initializeSettings() {
		// set the default selection, if any
		getTreeViewer().setInput(ResourcesPlugin.getWorkspace().getRoot());
		getTreeViewer().setSelection(getSelection());
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
					newPressed();
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
					removePressed();
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
					copyPressed();
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
		nameLabel.setText("Name:");
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
		
		TabFolder tabFolder = new TabFolder(c, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.heightHint = 300;
		gd.widthHint = 325;
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
	 * Sets the title for the dialog.
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(Shell);
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Launch Configurations");
		//newShell.setSize(400, 400);
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
	 * Notification selection has changed in the lanuch configuration
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
 		
 		// XXX: prompt for save of current configuration 			
 		
 		// enable buttons
 		boolean singleSelection = selection.size() == 1;
 		
		getLaunchButton().setEnabled(singleSelection);
 		getSaveAndLaunchButton().setEnabled(singleSelection);
 		getNewButton().setEnabled(singleSelection);
 		getCopyButton().setEnabled(singleSelection);

 		getDeleteButton().setEnabled(selection.size() >= 1);
 		
 		getSaveButton().setEnabled(false);
 		
 		if (singleSelection && selection.getFirstElement() instanceof ILaunchConfiguration) {
 			// single configuratino selected
 			ILaunchConfiguration newConfig = (ILaunchConfiguration)selection.getFirstElement();
 			setLaunchConfiguration(newConfig);
 		} else if (singleSelection && selection.getFirstElement() instanceof ILaunchConfigurationType) {
 			// single configuration type
 			
 		} else {
 			// multi-selection
 		}

 	}
 
 	/**
 	 * Sets the 'save & lanuch' button.
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
 	 * Sets the configuration to display/edit.
 	 * Updates the tab folder to the appropriate
 	 * pages.
 	 * 
 	 * @param config the launch configuration to display/edit
 	 */
 	protected void setLaunchConfiguration(ILaunchConfiguration config) {
 		try {
 			ILaunchConfigurationType newType = config.getType();
			ILaunchConfigurationType prevType = null;
			if (fWorkingCopy != null) {
				prevType = fWorkingCopy.getType();
			}
			
			if (!newType.equals(prevType)) {
				// dipose the current tabs and replace with new ones 
				TabItem[] oldTabs = getTabFolder().getItems();
				ILaunchConfigurationTab[] tabs = getTabs();
				for (int i = 0; i < oldTabs.length; i++) {
					oldTabs[i].dispose();
					tabs[i].dispose();
				}
				// build the new tabs
		 		LaunchConfigurationTabExtension[] exts = LaunchConfigurationPresentationManager.getDefault().getTabs(newType);
		 		tabs = new ILaunchConfigurationTab[exts.length];
		 		for (int i = 0; i < exts.length; i++) {
		 			TabItem tab = new TabItem(getTabFolder(), SWT.NONE);
		 			String name = exts[i].getName();
		 			if (name == null) {
		 				name = "unspecified";
		 			}
		 			tab.setText(name);
		 			tabs[i] = (ILaunchConfigurationTab)exts[i].getConfigurationElement().createExecutableExtension("class");
		 			Control control = tabs[i].createTabControl(tab);
		 			if (control != null) {
			 			tab.setControl(control);
		 			}
		 		}
		 		setTabs(tabs);		
			}
			
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

	 		
 		} catch (CoreException e) {
 			DebugUIPlugin.errorDialog(getShell(), "Error", "Exception occurred creating tabs.",e.getStatus());
 			return;
 		}
 		
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
 	
	/*
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

	/*
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
	protected void newPressed() {
		// prompt for save of current working copy
		
		// if a type is selected, create a new empty config
		// if a config is selected, copy it
		IStructuredSelection sel = (IStructuredSelection)getSelection();
		Object obj = sel.getFirstElement();
		if (obj instanceof ILaunchConfiguration) {
			
		} else {
			ILaunchConfigurationType type = (ILaunchConfigurationType)obj;
			try {
				ILaunchConfigurationWorkingCopy wc = type.newInstance(null, "name");
				setLaunchConfiguration(wc);
			} catch (CoreException e) {
				 DebugUIPlugin.errorDialog(getShell(), "Error", "Exception occurred creating new launch configuration.",e.getStatus());
	 			return;
			}
		}
	}	
	
	/**
	 * Notification the 'remove' button has been pressed
	 */
	protected void removePressed() {
	}	
	
	/**
	 * Notification the 'copy' button has been pressed
	 */
	protected void copyPressed() {
	}	
}


