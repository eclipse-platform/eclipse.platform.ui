package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Dialog to prompt the user to choose a resource version for a given resource.
 */
public class VersionSelectionDialog extends Dialog {
	private IResource resource;

	private CVSTag result;
	
	// widgets;
	private TableViewer editionTable;
	private Button okButton;
	private Label messageLine;

	private Image versionImage;
	
	// constants
	private static final int SIZING_DIALOG_WIDTH = 400;
	private static final int SIZING_DIALOG_HEIGHT = 250;
	
	/**
	 * Creates a new VersionSelectionDialog.
	 * @param resource The resource to select a version for.
	 */
	public VersionSelectionDialog(Shell parentShell, IResource resource) {
		super(parentShell);
		this.resource = resource;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Policy.bind("VersionSelectionDialog.version"));
		// set F1 help
		//WorkbenchHelp.setHelp(newShell, new Object[] {IVCMHelpContextIds.VERSION_SELECTION_DIALOG});
	}
	
	public boolean close() {
		versionImage.dispose();
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

		Composite top = (Composite)super.createDialogArea(parent);
		GridData data = (GridData)top.getLayoutData();
		data.widthHint = SIZING_DIALOG_WIDTH;
		data.heightHint = SIZING_DIALOG_HEIGHT;
	
		createLabel(top, Policy.bind("VersionSelectionDialog.versionsTitle"));
		editionTable = createTable(top);
		editionTable.setContentProvider(getEditionsContentProvider());
		editionTable.setLabelProvider(new LabelProvider() {
			public Image getImage(Object element) {
				return versionImage;
			}
			public String getText(Object element) {
				return ((CVSTag)element).getName();
			}
		});
		editionTable.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleEditionSelectionChanged();
			}
		});
		// select and close on double click
		editionTable.getTable().addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				IStructuredSelection selection = (IStructuredSelection)editionTable.getSelection();
				if (!selection.isEmpty())
					okPressed();
			}
		});
		
		// set the sorter
		editionTable.setSorter(new RepositorySorter());
		
		// add a listener to resize the columns when the shell is resized
		getShell().addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Table table = editionTable.getTable();
				setLayout(table);
				table.layout();
			}
		});
	
		messageLine = new Label(top, SWT.NONE);
		messageLine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		// initialize the table contents
		editionTable.setInput(resource);
		
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
					return manager.getKnownVersionTags(remoteResource, new NullProgressMonitor());
				} catch (TeamException e) {
					CVSUIPlugin.log(e.getStatus());
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
	 * The edition selection has changed.  Update the dialog
	 * accordingly.
	 */
	protected void handleEditionSelectionChanged() {
		//as long as an edition is selected, we're ok to close
		updateEnablement(null);
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
		IStructuredSelection selection = (IStructuredSelection)editionTable.getSelection();
		result = (CVSTag)selection.getFirstElement();
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
	 * Updates the dialog enablement.  If msg is null, then it is
	 * ok to complete the dialog, otherwise msg is the error message
	 * to display to the user.
	 */
	protected void updateEnablement(String msg) {
		if (msg != null) {
			okButton.setEnabled(false);
			showError(msg);
		} else {
			okButton.setEnabled(true);
			showError(null);
		}
	}
}
