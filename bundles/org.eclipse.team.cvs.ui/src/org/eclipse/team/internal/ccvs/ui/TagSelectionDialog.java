package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement;
import org.eclipse.team.internal.ccvs.ui.merge.TagElement;
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement.ProjectElementSorter;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Dialog to prompt the user to choose a tag for a selected resource
 */
public class TagSelectionDialog extends Dialog {
	private ICVSFolder[] folders;
	private int includeFlags;
	private CVSTag result;
	
	public static final int INCLUDE_HEAD_TAG = ProjectElement.INCLUDE_HEAD_TAG;
	public static final int INCLUDE_BASE_TAG = ProjectElement.INCLUDE_BASE_TAG;
	public static final int INCLUDE_BRANCHES = ProjectElement.INCLUDE_BRANCHES;
	public static final int INCLUDE_VERSIONS = ProjectElement.INCLUDE_VERSIONS;
	public static final int INCLUDE_ALL_TAGS = ProjectElement.INCLUDE_ALL_TAGS;
	
	// widgets;
	private TreeViewer tagTree;
	private Button okButton;
	
	// dialog title, should indicate the action in which the tag selection
	// dialog is being shown
	private String title;
	private String message;
	
	private boolean recurse = true;
	private boolean showRecurse;
	
	// constants
	private static final int SIZING_DIALOG_WIDTH = 400;
	private static final int SIZING_DIALOG_HEIGHT = 250;
	
	/**
	 * Creates a new TagSelectionDialog.
	 * @param resource The resource to select a version for.
	 */
	public TagSelectionDialog(Shell parentShell, IProject[] projects, String title, String message, int includeFlags, boolean showRecurse) {
		this(parentShell, getCVSFoldersFor(projects), title, message, includeFlags, showRecurse); //$NON-NLS-1$		
	}
	
	private static ICVSFolder[] getCVSFoldersFor(IProject[] projects) {
		ICVSFolder[] folders = new ICVSFolder[projects.length];
		for (int i = 0; i < projects.length; i++) {
			folders[i] = CVSWorkspaceRoot.getCVSFolderFor(projects[i]);
		}
		return folders;
	}
	
	/**
	 * Creates a new TagSelectionDialog.
	 * @param resource The resource to select a version for.
	 */
	public TagSelectionDialog(Shell parentShell, ICVSFolder[] folders, String title, String message, int includeFlags, boolean showRecurse) {
		super(parentShell);
		this.folders = folders;
		this.title = title;
		this.message = message;
		this.includeFlags = includeFlags;
		this.showRecurse = showRecurse;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(title);
	}
	
	/**
	 * Creates this window's widgetry.
	 * <p>
	 * The default implementation of this framework method
	 * creates this window's shell (by calling <code>createShell</code>),
	 * its control (by calling <code>createContents</code>),
	 * and initializes this window's shell bounds 
	 * (by calling <code>initializeBounds</code>).
	 * This framework method may be overridden; however,
	 * <code>super.create</code> must be called.
	 * </p>
	 */
	public void create() {
		super.create();
		initialize();
	}
	
	/**
	 * Add buttons to the dialog's button bar.
	 *
	 * @param parent the button bar composite
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	/**
	 * Creates and returns the contents of the upper part 
	 * of this dialog (above the button bar).
	 * <p>
	 * The default implementation of this framework method
	 * creates and returns a new <code>Composite</code> with
	 * standard margins and spacing.
	 * Subclasses should override.
	 * </p>
	 *
	 * @param the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	protected Control createDialogArea(Composite parent) {
		Composite top = (Composite)super.createDialogArea(parent);
		
		Composite inner = new Composite(top, SWT.NULL);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = SIZING_DIALOG_WIDTH;
		data.heightHint = SIZING_DIALOG_HEIGHT;
		inner.setLayoutData(data);
		GridLayout layout = new GridLayout();
		inner.setLayout(layout);
		
		Label l = new Label (inner, SWT.NONE);
		l.setText(message); //$NON-NLS-1$
		
		tagTree = createTree(inner);
		tagTree.setInput(new ProjectElement(folders[0], includeFlags));
		tagTree.setSorter(new ProjectElementSorter());
		Runnable refresh = new Runnable() {
			public void run() {
				getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						tagTree.refresh();
					}
				});
			}
		};

		if(showRecurse) {
			final Button recurseCheck = new Button(top, SWT.CHECK);
			recurseCheck.setText(Policy.bind("TagSelectionDialog.recurseOption")); //$NON-NLS-1$
			recurseCheck.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					recurse = recurseCheck.getSelection();
				}
			});
			recurseCheck.setSelection(true);
		}

		
		TagConfigurationDialog.createTagDefinitionButtons(getShell(), top, folders, 
														  convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT), 
														  convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH),
														  refresh, refresh);
		
		Label seperator = new Label(top, SWT.SEPARATOR | SWT.HORIZONTAL);
		data = new GridData (GridData.FILL_BOTH);		
		data.horizontalSpan = 2;
		seperator.setLayoutData(data);

		updateEnablement();
		
		return top;
	}
	
	
	/**
	 * Utility method that creates a label instance
	 * and sets the default layout data.
	 *
	 * @param parent  the parent for the new label
	 * @param text  the text for the new label
	 * @return the new label
	 */
	protected Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData();
		data.horizontalSpan = 1;
		data.horizontalAlignment = GridData.FILL;
		label.setLayoutData(data);
		return label;
	}
	
	protected TreeViewer createTree(Composite parent) {
		Tree tree = new Tree(parent, SWT.SINGLE | SWT.BORDER);
		tree.setLayoutData(new GridData(GridData.FILL_BOTH));	
		TreeViewer result = new TreeViewer(tree);
		result.setContentProvider(new WorkbenchContentProvider());
		result.setLabelProvider(new WorkbenchLabelProvider());
		result.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateEnablement();
			}
		});
		// select and close on double click
		// To do: use defaultselection instead of double click
		result.getTree().addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tagTree.getSelection();
				if (!selection.isEmpty() && (selection.getFirstElement() instanceof TagElement)) {
					okPressed();
				}
			}
		});
		result.setSorter(new RepositorySorter());
		return result;
	}
	
	/**
	 * Returns the selected tag.
	 */
	public CVSTag getResult() {
		return result;
	}
	
	public boolean getRecursive() {
		return recurse;
	}

	/**
	 * Initializes the dialog contents.
	 */
	protected void initialize() {
		okButton.setEnabled(false);
	}
	
	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 * <p>
	 * The default implementation of this framework method sets
	 * this dialog's return code to <code>Window.OK</code>
	 * and closes the dialog. Subclasses may override.
	 * </p>
	 */
	protected void okPressed() {
		IStructuredSelection selection = (IStructuredSelection)tagTree.getSelection();
		Object o = selection.getFirstElement();
		TagElement element = (TagElement)o;
		result = element.getTag();
		super.okPressed();
	}

	
	/**
	 * Updates the dialog enablement.
	 */
	protected void updateEnablement() {
		if(okButton!=null) {
			IStructuredSelection selection = (IStructuredSelection)tagTree.getSelection();
			if (selection.isEmpty() || !(selection.getFirstElement() instanceof TagElement)) {
				okButton.setEnabled(false);
			} else {
				okButton.setEnabled(true);
			}
		}
	}
}
