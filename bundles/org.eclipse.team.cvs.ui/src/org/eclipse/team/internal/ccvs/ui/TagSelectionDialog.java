package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.merge.ProjectElement;
import org.eclipse.team.internal.ccvs.ui.merge.TagElement;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Dialog to prompt the user to choose a tag for a selected resource
 */
public class TagSelectionDialog extends Dialog {
	private IResource resource;

	private CVSTag result;
	
	// widgets;
	private TreeViewer tagTree;
	private Button okButton;
	private Label messageLine;
	private Button useDefinedTagButton;
	private Button useSpecifiedTagButton;
	private Combo tagTypeCombo;
	private Text tagText;
	private Image versionImage;
	private Image branchImage;
	
	// constants
	private static final int SIZING_DIALOG_WIDTH = 400;
	private static final int SIZING_DIALOG_HEIGHT = 250;
	
	/**
	 * Creates a new TagSelectionDialog.
	 * @param resource The resource to select a version for.
	 */
	public TagSelectionDialog(Shell parentShell, IResource resource) {
		super(parentShell);
		this.resource = resource;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Policy.bind("TagSelectionDialog.Select_a_Tag_1")); //$NON-NLS-1$
		// set F1 help
		//WorkbenchHelp.setHelp(newShell, new Object[] {IVCMHelpContextIds.VERSION_SELECTION_DIALOG});
	}
	
	public boolean close() {
		versionImage.dispose();
		branchImage.dispose();
		return super.close();
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
		versionImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_PROJECT_VERSION).createImage();
		branchImage = CVSUIPlugin.getPlugin().getImageDescriptor(ICVSUIConstants.IMG_TAG).createImage();

		Composite top = (Composite)super.createDialogArea(parent);
	
		useDefinedTagButton = new Button(top, SWT.RADIO);
		useDefinedTagButton.setText(Policy.bind("TagSelectionDialog.Use_Defined_Tag__2")); //$NON-NLS-1$
		useDefinedTagButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				handleRadioSelectionChanged();
			}
		});
		
		Composite inner = new Composite(top, SWT.NULL);
		GridData data = new GridData();
		data.widthHint = SIZING_DIALOG_WIDTH;
		data.heightHint = SIZING_DIALOG_HEIGHT;
		inner.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 10;
		inner.setLayout(layout);
		tagTree = createTree(inner);
		
		useSpecifiedTagButton = new Button(top, SWT.RADIO);
		useSpecifiedTagButton.setText(Policy.bind("TagSelectionDialog.Use_User-Specified_Tag__3")); //$NON-NLS-1$
		
		inner = new Composite(top, SWT.NULL);
		inner.setLayoutData(new GridData());
		layout = new GridLayout();
		layout.marginWidth = 10;
		layout.numColumns = 2;
		inner.setLayout(layout);
		createLabel(inner, Policy.bind("TagSelectionDialog.Tag_name__4")); //$NON-NLS-1$
		tagText = new Text(inner, SWT.SINGLE | SWT.BORDER);
		tagText.setEnabled(false);
		tagText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				updateEnablement();
			}
		});
		data = new GridData();
		data.widthHint = 250;
		tagText.setLayoutData(data);

		createLabel(inner, Policy.bind("TagSelectionDialog.Tag_type__5")); //$NON-NLS-1$
		tagTypeCombo = new Combo(inner, SWT.READ_ONLY);
		tagTypeCombo.setEnabled(false);
		tagTypeCombo.add(Policy.bind("TagSelectionDialog.Version_6")); //$NON-NLS-1$
		tagTypeCombo.add(Policy.bind("TagSelectionDialog.Branch_7")); //$NON-NLS-1$
		
		messageLine = new Label(top, SWT.NONE);
		messageLine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		useDefinedTagButton.setSelection(true);
		tagTypeCombo.select(0);

		// initialize the table contents
		try {
			new ProgressMonitorDialog(getShell()).run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.beginTask(Policy.bind("TagSelectionDialog.fetching"), 100);
						monitor.subTask(Policy.bind("TagSelectionDialog.preparing"));
						CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(resource);
						monitor.worked(50);
						monitor.subTask(Policy.bind("TagSelectionDialog.fetching"));
						tagTree.setInput(new ProjectElement((ICVSRemoteFolder)CVSWorkspaceRoot.getRemoteResourceFor(resource.getProject()), getShell()));
					} catch (TeamException e) {
						new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InvocationTargetException e) {
			// To do: Error dialog
			if (e.getTargetException() instanceof TeamException) {
				CVSUIPlugin.log(((TeamException)e.getTargetException()).getStatus());
			} else {
				CVSUIPlugin.log(new Status(IStatus.ERROR, CVSUIPlugin.ID, 0, Policy.bind("internal"), e.getTargetException()));
			}
		} catch (InterruptedException e) {
			// Ignore
		}

		return top;
	}
	
	private void handleRadioSelectionChanged() {
		if (useDefinedTagButton.getSelection()) {
			// Enable the table, disable the text widget
			tagTree.getTree().setEnabled(true);
			tagText.setEnabled(false);
			tagTypeCombo.setEnabled(false);
		} else {
			// Disable the table, enable the text widget
			tagTree.getTree().setEnabled(false);
			tagText.setEnabled(true);
			tagTypeCombo.setEnabled(true);
		}
		// update the widget enablement
		updateEnablement();
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

	/**
	 * Initializes the dialog contents.
	 */
	protected void initialize() {
		okButton.setEnabled(false);
		showError(null);
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
		if (useDefinedTagButton.getSelection()) {
			IStructuredSelection selection = (IStructuredSelection)tagTree.getSelection();
			Object o = selection.getFirstElement();
			TagElement element = (TagElement)o;
			result = element.getTag();
		} else {
			String text = tagText.getText();
			int type;
			switch (tagTypeCombo.getSelectionIndex()) {
				case 0:
					type = CVSTag.VERSION;
					break;
				case 1:
					type = CVSTag.BRANCH;
					break;
				default:
					type = CVSTag.HEAD;
					break;
			}
			result = new CVSTag(text, type);
		}
		super.okPressed();
	}

	/**
	 * Shows an error message in the message line.
	 */
	void showError(String errorMsg) {
		messageLine.setForeground(messageLine.getDisplay().getSystemColor(SWT.COLOR_RED));
		messageLine.setText(errorMsg == null ? "" : errorMsg); //$NON-NLS-1$
	}

	/**
	 * Updates the dialog enablement.
	 */
	protected void updateEnablement() {
		if (useDefinedTagButton.getSelection()) {
			IStructuredSelection selection = (IStructuredSelection)tagTree.getSelection();
			if (selection.isEmpty() || !(selection.getFirstElement() instanceof TagElement)) {
				okButton.setEnabled(false);
				showError(Policy.bind("TagSelectionDialog.Please_select_a_tag_9")); //$NON-NLS-1$
			} else {
				okButton.setEnabled(true);
				showError(null);
			}
		} else {
			String tag = tagText.getText();
			IStatus result = CVSTag.validateTagName(tag);
			if (result.isOK()) {
				okButton.setEnabled(true);
				showError(null);
			} else {
				okButton.setEnabled(false);
				showError(result.getMessage());
			}
		}
	}
}
