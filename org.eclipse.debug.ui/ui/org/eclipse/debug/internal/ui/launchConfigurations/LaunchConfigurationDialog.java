package org.eclipse.debug.internal.ui.launchConfigurations;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The dialog used to edit and launch launch configurations.
 */
public class LaunchConfigurationDialog extends Dialog {
	
	/**
	 * Context in which to display/select
	 * launch configurations from. Can be
	 * a project, workspace root, or <code>null</code>
	 * (empty)
	 */
	private IResource fResource;
	
	/**
	 * The tree of launch configurations
	 */
	private TreeViewer fConfigTree;
	
	/**
	 * The text widget displaying the selected project
	 */
	private Text fProjectText;
	
	/**
	 * The button used to select a project from a list
	 */
	private Button fProjectBrowseButton;
	
	/**
	 * The (radio) button used to select a project
	 */
	private Button fProjectButton;	
	
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
	 * This dialog has 'Save & Launch', 'Launch', and 'Cancel'
	 * buttons.
	 * 
	 * @see Dialog#createButtonsForButtonBar(Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, ID_SAVE_AND_LAUNCH_BUTTON, LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.S&ave_and_Launch_1"), false); //$NON-NLS-1$
		createButton(parent, ID_LAUNCH_BUTTON, LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.&Launch_2"), true); //$NON-NLS-1$
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}	

	/**
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		GridData gd;
		Composite composite = (Composite)super.createDialogArea(parent);
		GridLayout topLevelLayout = (GridLayout) composite.getLayout();
		topLevelLayout.numColumns = 2;
		
		// Build the project/workspace selection area
		// and put it into the composite.		
		Composite projectSelectionArea = createProjectSelectionArea(composite);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		projectSelectionArea.setLayoutData(gd);
		
		// Build the launch configuration selection area
		// and put it into the composite.
		Composite launchConfigSelectionArea = createLaunchConfigurationSelectionArea(composite);
		gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = 175;
		gd.heightHint = 300;
		launchConfigSelectionArea.setLayoutData(gd);
	
		// Build the launch configuration edit area
		// and put it into the composite.
		Composite launchConfigurationEditArea = createLaunchConfigurationEditArea(composite);
		gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = 300;
		launchConfigurationEditArea.setLayoutData(gd);
			
		// Build the separator line
		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);
	
		initializeSettings();
		
		return composite;
	}
	
	/**
	 * Initialize the dialog settings
	 */
	protected void initializeSettings() {
		// set the default project setting, if any
		setContext(getContext());
	}
	
	/**
	 * Creates the project selection area of the dialog. This
	 * allows the user to display launch configurations in a
	 * specific project or all configurations in the workspace.
	 * 
	 * @return the composite used for project selection
	 */ 
	protected Composite createProjectSelectionArea(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		c.setLayout(layout);
		
		GridData gd;
		
		Label scopeLabel = new Label(c, SWT.HORIZONTAL | SWT.LEFT);
		scopeLabel.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Show_Launch_Configurations_from__3")); //$NON-NLS-1$
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 3;
		scopeLabel.setLayoutData(gd);

		Button projectButton = new Button(c, SWT.RADIO | SWT.LEFT);
		projectButton.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.&Project__4")); //$NON-NLS-1$
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 1;
		projectButton.setLayoutData(gd);
		setProjectButton(projectButton);				
		
		Text projectText = new Text(c, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		projectText.setLayoutData(gd);
		setProjectTextWidget(projectText);
		
		projectButton.addSelectionListener(
			new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent event) {
					if (((Button)(event.getSource())).getSelection()) {
						setContext(getProjectFromText());
					}
				}
			}
		);			
		
		projectText.addModifyListener(
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					IProject project = getProjectFromText();
					IResource context = getContext();
					if (context == project) {
						// no update
						return;
					}
					if (context == null || project == null) {
						setContext(project);
						return;
					}
					if (context.equals(project)) {
						return;
					} else {
						setContext(project);
					}
				}
			}
		);
		
		Button browseProjectsButton = new Button(c, SWT.PUSH | SWT.CENTER);
		browseProjectsButton.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.&Browse..._5")); //$NON-NLS-1$
		gd = new GridData(GridData.END);
		gd.horizontalSpan = 1;
		browseProjectsButton.setLayoutData(gd);
		setProjectBrowseButton(browseProjectsButton);
		
		browseProjectsButton.addSelectionListener(
			new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent event) {
					ListSelectionDialog dialog = new ListSelectionDialog(
						getShell(),
						ResourcesPlugin.getWorkspace().getRoot(),
						new WorkbenchContentProvider(),
						new WorkbenchLabelProvider(),
						LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Choose_a_project_6") //$NON-NLS-1$
					);
					dialog.open();
					Object[] result = dialog.getResult();
					if (result != null && result.length == 1) {
						setContext((IProject)result[0]);
					}
				}
			}
		);
				
		Button workspaceButton = new Button(c, SWT.RADIO | SWT.LEFT);
		workspaceButton.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.&Workspace_7")); //$NON-NLS-1$
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 3;
		workspaceButton.setLayoutData(gd);	
		
		workspaceButton.addSelectionListener(
			new SelectionAdapter() { 
				public void widgetSelected(SelectionEvent event) {
					if (((Button)(event.getSource())).getSelection()) {
						setContext(ResourcesPlugin.getWorkspace().getRoot());
					}
				}
			}
		);			
		
		return c;
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
		tree.getControl().setLayoutData(gd);
		tree.setContentProvider(new LaunchConfigurationContentProvider());
		tree.setLabelProvider(DebugUITools.newDebugModelPresentation());
		setTreeViewer(tree);
		
		Button newButton = new Button(c, SWT.PUSH | SWT.CENTER);
		newButton.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.&New_8")); //$NON-NLS-1$
		gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 1;
		newButton.setLayoutData(gd);
		
		Button removeButton = new Button(c, SWT.PUSH | SWT.CENTER);
		removeButton.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.&Delete_9")); //$NON-NLS-1$
		gd = new GridData(GridData.CENTER);
		gd.horizontalSpan = 1;
		removeButton.setLayoutData(gd);			
		
		Button copyButton = new Button(c, SWT.PUSH | SWT.CENTER);
		copyButton.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.&Copy_10")); //$NON-NLS-1$
		gd = new GridData(GridData.END);
		gd.horizontalSpan = 1;
		copyButton.setLayoutData(gd);		
		
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
		c.setLayout(new GridLayout());
		createButton(c, 2000, LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.edit_area_11"), false); //$NON-NLS-1$
		return c;
	}	
	
	/**
	 * Sets the title for the dialog.
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(Shell);
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Launch_Configurations_12")); //$NON-NLS-1$
		newShell.setSize(400, 400);
	}
	
	/**
	 * Sets the context for the launch cofigurations that
	 * are to be displayed in the launch configuration selection
	 * area. The context can be set to a specific project
	 * (i.e. display only those configurations in a specific
	 * project), the workspace, or <code>null</code> (no/empty context).
	 * 
	 * @param context a project or <code>null</code>
	 */
	public void setContext(IResource context) {
		fResource = context;
		if (isVisible()) {
			getTreeViewer().setInput(context);
			if (context instanceof IProject) {
				getProjectButton().setSelection(true);
				getProjectTextWidget().setEnabled(true);
				getProjectBrowseButton().setEnabled(true);
				if (!context.getName().equals(getProjectTextWidget().getText())) {
					getProjectTextWidget().setText(context.getName());
				}
			} else if (context instanceof IWorkspaceRoot) {
				getProjectButton().setSelection(false);
				getProjectBrowseButton().setSelection(false);
				getProjectTextWidget().setEnabled(false);
				getProjectBrowseButton().setEnabled(false);
			}
		}
	}
	
	/**
	 * Returns the context from which launch cofigurations
	 * are displayed. Returns <code>null</code> no configurations
	 * are displayed (empty conetxt).
	 * 
	 * @return project, workspace root, or <code>null</code> if none
	 */
	public IResource getContext() {
		return fResource;
	}
	
	/**
	 * Returns the current project context or <code>null</code>
	 * if the current context is not a project.
	 * 
	 * @return the current project context or <code>null</code>
	 */
	public IProject getProject() {
		if (getContext() instanceof IProject) {
			return (IProject)getContext();
		}
		return null;
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
	 * Sets the text widget used to display the selected project name
	 * 
	 * @param widget the text widget used to display the selected project name
	 */
	private void setProjectTextWidget(Text widget) {
		fProjectText = widget;
	}
	
	/**
	 * Returns the text widget used to display the selected project name
	 * 
	 * @return the text widget used to display the selected project name
	 */
	protected Text getProjectTextWidget() {
		return fProjectText;
	}
	
	/**
	 * Sets the button used to select a project from a list
	 * 
	 * @param widget the button used to select a project from a list
	 */
	private void setProjectBrowseButton(Button widget) {
		fProjectBrowseButton = widget;
	}
	
	/**
	 * Returns the button used to select a project from a list
	 * 
	 * @return the button used to select a project from a list
	 */
	protected Button getProjectBrowseButton() {
		return fProjectBrowseButton;
	}
	
	/**
	 * Sets the (radio) button used to select a project
	 * 
	 * @param widget the button used to select a project
	 */
	private void setProjectButton(Button widget) {
		fProjectButton = widget;
	}
	
	/**
	 * Returns the (radio) button used to select a project
	 * 
	 * @return the button used to select a project
	 */
	protected Button getProjectButton() {
		return fProjectButton;
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
					if (getProject() == null) {
						// all configs in workspace of a specific type
						return getLaunchManager().getLaunchConfigurations(type);
					} else {
						// configs in a project of a specifc type
						return getLaunchManager().getLaunchConfigurations(getProject(), type);
					}
				} catch (CoreException e) {
					DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Error_13"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.An_exception_occurred_while_retrieving_launch_configurations._14"), e.getStatus()); //$NON-NLS-2$ //$NON-NLS-1$
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
					DebugUIPlugin.errorDialog(getShell(), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.Error_15"), LaunchConfigurationsMessages.getString("LaunchConfigurationDialog.An_exception_occurred_while_retrieving_launch_configurations._16"), e.getStatus()); //$NON-NLS-2$ //$NON-NLS-1$
				}
			} else if (element instanceof ILaunchConfigurationType) {
				return getContext();
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
	 * Returns the project currently specified based on the
	 * text in the project text widget, possibly <code>null</code>.
	 * 
	 * @return the project currently specified based on the
	 *  text in the project text widget, possibly <code>null</code>
	 */
	protected IProject getProjectFromText() {
		String text = getProjectTextWidget().getText();
		if (text == null || text.length() == 0) {
			return null;
		}
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(text);
		if (project.exists() && project.isOpen()) {
			return project;
		}
		return null;
	}
}