package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.ui.model.BranchTag;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Dialog to prompt the user to choose a tag for a selected resource
 */
public class TagSelectionDialog extends Dialog {
	private IResource resource;

	private CVSTag result;
	
	// widgets;
	private TableViewer tagTable;
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
		newShell.setText(Policy.bind("TagSelectionDialog.version"));
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
	
		Composite tagTypeComposite = new Composite(top, SWT.NULL);
		tagTypeComposite.setLayoutData(new GridData());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		tagTypeComposite.setLayout(layout);
		createLabel(tagTypeComposite, "Tag Type:");
		
		tagTypeCombo = new Combo(tagTypeComposite, SWT.READ_ONLY);
		tagTypeCombo.add("Version");
		tagTypeCombo.add("Branch");
		tagTypeCombo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				handleComboSelectionChanged();
			}
		});
		
		useDefinedTagButton = new Button(top, SWT.RADIO);
		useDefinedTagButton.setText("Use Defined Tag:");
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
		layout = new GridLayout();
		layout.marginWidth = 10;
		inner.setLayout(layout);
		tagTable = createTable(inner);
		tagTable.setContentProvider(getEditionsContentProvider());
		tagTable.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				if (element instanceof CVSTag) {
					return versionImage;
				} else if (element instanceof BranchTag) {
					return branchImage;
				}
				return null;
			}
			public String getText(Object element) {
				if (element instanceof CVSTag) {
					return ((CVSTag)element).getName();
				} else if (element instanceof BranchTag) {
					return ((BranchTag)element).getTag().getName();
				}
				return "";
			}
		});
		tagTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateEnablement();
			}
		});
		// select and close on double click
		tagTable.getTable().addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tagTable.getSelection();
				if (!selection.isEmpty())
					okPressed();
			}
		});
		tagTable.setSorter(new RepositorySorter());
		
		useSpecifiedTagButton = new Button(top, SWT.RADIO);
		useSpecifiedTagButton.setText("Use User-Specified Tag:");
		
		inner = new Composite(top, SWT.NULL);
		inner.setLayoutData(new GridData());
		layout = new GridLayout();
		layout.marginWidth = 10;
		inner.setLayout(layout);
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
		
		// add a listener to resize the columns when the shell is resized
		getShell().addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Table table = tagTable.getTable();
				setLayout(table);
				table.layout();
			}
		});
	
		messageLine = new Label(top, SWT.NONE);
		messageLine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		useDefinedTagButton.setSelection(true);
		tagTypeCombo.select(0);

		// initialize the table contents
		tagTable.setInput(resource);

		return top;
	}
	
	private void handleComboSelectionChanged() {
		tagTable.refresh(resource);
		// update the widget enablement
		updateEnablement();
	}
	
	private void handleRadioSelectionChanged() {
		if (useDefinedTagButton.getSelection()) {
			// Enable the table, disable the text widget
			tagTable.getTable().setEnabled(true);
			tagText.setEnabled(false);
		} else {
			// Disable the table, enable the text widget
			tagTable.getTable().setEnabled(false);
			tagText.setEnabled(true);
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
	
	/**
	 * Creates the group that displays lists of the available repositories
	 * and team streams.
	 *
	 * @param the parent composite to contain the group
	 * @return the group control
	 */
	protected TableViewer createTable(Composite parent) {
		Table table = new Table(parent, SWT.SINGLE | SWT.BORDER);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
	
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(false);
		layout.addColumnData(new ColumnWeightData(100, false));
	
		return new TableViewer(table);
	}
	
	/**
	 * Returns a content provider for repositories that only returns
	 * resource editions for the input resource.
	 */
	protected IStructuredContentProvider getEditionsContentProvider() {
		return new SimpleContentProvider() {
			public Object[] getElements(Object o) {
				if (o == null) return null;
				if (!(o instanceof IResource)) return null;
				IResource resource = (IResource)o;
				try {
					RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
					CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(resource);
					ICVSRemoteResource remoteResource = provider.getRemoteResource(resource);
					switch (tagTypeCombo.getSelectionIndex()) {
						case 1:
							// Branch tags
							return manager.getKnownBranchTags(remoteResource.getRepository());
						case 0:
						default:						
							// Version tags
							return manager.getKnownVersionTags(remoteResource, new NullProgressMonitor());
					}
				} catch (TeamException e) {
					ErrorDialog.openError(getShell(), null, null, e.getStatus());
					return null;
				}
			}
		};
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
			IStructuredSelection selection = (IStructuredSelection)tagTable.getSelection();
			Object o = selection.getFirstElement();
			if (o instanceof CVSTag) {
				result = (CVSTag)o;
			} else {
				result = ((BranchTag)o).getTag();
			}
		} else {
			String text = tagText.getText();
			int type = CVSTag.VERSION;
			switch (tagTypeCombo.getSelectionIndex()) {
				case 0:
					type = CVSTag.VERSION;
					break;
				case 1:
					type = CVSTag.BRANCH;
					break;
			}
			result = new CVSTag(text, type);
		}
		super.okPressed();
	}

	/**
	 * Sets a new layout for the given table.
	 */
	protected void setLayout(Table table) {
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(100, true));
		table.setLayout(layout);
	}

	/**
	 * Shows an error message in the message line.
	 */
	void showError(String errorMsg) {
		messageLine.setForeground(messageLine.getDisplay().getSystemColor(SWT.COLOR_RED));
		messageLine.setText(errorMsg == null ? "" : errorMsg);
	}

	/**
	 * Updates the dialog enablement.
	 */
	protected void updateEnablement() {
		if (useDefinedTagButton.getSelection()) {
			if (tagTable.getSelection().isEmpty()) {
				okButton.setEnabled(false);
				showError("Please select a tag.");
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
