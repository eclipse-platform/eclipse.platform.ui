package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Table;

import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.ProductInfo;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.IPluginDescriptor;

/**
 * Displays information about the product plugins.
 *
 * @private
 *		This class is internal to the workbench and must not be called outside the workbench
 */
public class AboutPluginsDialog extends Dialog {

	/**
	 * Table height in dialog units (value 500).
	 */
	public int TABLE_HEIGHT = 200;

	private Table vendorInfo;
	private TableEditor tableEditor;
	private String columnTitles[] = {
		WorkbenchMessages.getString("AboutPluginsDialog.pluginName"), //$NON-NLS-1$
		WorkbenchMessages.getString("AboutPluginsDialog.description"), //$NON-NLS-1$
		WorkbenchMessages.getString("AboutPluginsDialog.version"), //$NON-NLS-1$
		WorkbenchMessages.getString("AboutPluginsDialog.provider"), //$NON-NLS-1$
		WorkbenchMessages.getString("AboutPluginsDialog.copyright"), //$NON-NLS-1$
		WorkbenchMessages.getString("AboutPluginsDialog.webSite"), //$NON-NLS-1$
	};

	private static IPluginDescriptor[] info;
	private static ProductInfo platformInfo;
/**
 * Constructor for AboutPluginsDialog
 */
public AboutPluginsDialog(Shell parentShell) {
	super(parentShell);
	if (info == null) {
		info = Platform.getPluginRegistry().getPluginDescriptors();
	}
	if (platformInfo == null){
		platformInfo = ((Workbench)PlatformUI.getWorkbench()).getProductInfo();
	}
}

/* (non-Javadoc)
 * Method declared on Window.
 */
protected void configureShell(Shell newShell) {
	super.configureShell(newShell);
	newShell.setText(WorkbenchMessages.format("AboutPluginsDialog.shellTitle", new Object[] {platformInfo.getName()})); //$NON-NLS-1$
	WorkbenchHelp.setHelp(newShell, new Object[] {IHelpContextIds.ABOUT_PLUGINS_DIALOG});
}
/**
 * Add buttons to the dialog's button bar.
 *
 * Subclasses should override.
 *
 * @param parent the button bar composite
 */
protected void createButtonsForButtonBar(Composite parent) {
	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
}
/**
 * Creates and returns the contents of the upper part 
 * of the dialog (above the button bar).
 *
 * Subclasses should overide.
 *
 * @param the parent composite to contain the dialog area
 * @return the dialog area control
 */
protected Control createDialogArea(Composite parent) {
		
	Composite outer = (Composite)super.createDialogArea(parent);
	outer.setSize(outer.computeSize(SWT.DEFAULT, SWT.DEFAULT));

	createTable(outer);
	createColumns();

 	GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
	gridData.grabExcessVerticalSpace = true;
	gridData.grabExcessHorizontalSpace = true;

	// suggest a height for the table
	gridData.heightHint = convertVerticalDLUsToPixels(TABLE_HEIGHT);
	vendorInfo.setLayoutData(gridData);

	return outer;
}
/**
 * Creates and returns the contents of the upper part 
 * of the dialog (above the button bar).
 *
 * Subclasses should overide.
 *
 * @param the parent composite to contain the dialog area
 * @return the dialog area control
 */
protected void createTable(Composite parent){
	vendorInfo = new Table(parent,SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER );
	vendorInfo.setHeaderVisible (true);
	vendorInfo.setLinesVisible(true);

	tableEditor = new TableEditor(vendorInfo);
	vendorInfo.addSelectionListener (new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
		
			/* Clean up any previous editor control */
			Control oldEditor = tableEditor.getEditor();
			if (oldEditor != null)
				oldEditor.dispose();	
		
			/* Identify the selected row */
			int index = vendorInfo.getSelectionIndex ();
			if (index == -1) return;
			TableItem item = vendorInfo.getItem (index);
			String currentValue = item.getText(5);
				
			/* The control that will be the editor must be a child of the Table */
			Text text = new Text(vendorInfo, SWT.NONE);
			text.setText(currentValue);
			text.selectAll();
				
			/* The text editor must have the same size as the cell and must
			 * not be any smaller than 50 pixels. */
			tableEditor.horizontalAlignment = SWT.LEFT;
			tableEditor.grabHorizontal = true;
			tableEditor.minimumWidth = 150;
		
			/* Open the text editor in the second column of the selected row. */
			tableEditor.setEditor (text, item, 5);
		
			/* Assign focus to the text control */
			text.setFocus ();
		}
	});
}

protected void createColumns(){
	/* create table headers */
	int[] columnWidths = {150, 150, 50, 100, 100, 150};
	for (int i = 0; i < columnTitles.length; i++) {
		TableColumn tableColumn = new TableColumn(vendorInfo, SWT.NULL);
		tableColumn.setWidth(columnWidths[i]);
		tableColumn.setText(columnTitles[i]);
	}	
		
	/* fill each row of the table with plugin registry info */
	for (int i=0; i < info.length; i++) {
		TableItem item = new TableItem (vendorInfo, SWT.NULL);

		/* fill each cell */

			String pluginName = info[i].getUniqueIdentifier().toString();
			// insert description code in the following line
			String description = "insert description here";
			String version = info[i].getVersionIdentifier().toString();
			String provider = info[i].getProviderName().toString();
			//insert copyright and website code in the following 2 lines
			String copyright = "insert copyright here";
			String webSite = "insert website here";
			String [] row = {pluginName, description, version, provider, copyright, webSite};

// TEST NUMBER OF ROWS
//	String pluginName = "insert description here";
//	// insert description code in the following line
//	String description = "insert description here";
//	String version = "insert version here";
//	String provider = "insert provider here";
//	//insert copyright and website code in the following 2 lines
//	String copyright = "insert copyright here";
//	String webSite = "insert website here";
//	String extra = "extra column";
//	String [] row = {pluginName, description, version, provider, copyright, webSite, extra};

			item.setText(row);

	}
		
}
		
}

