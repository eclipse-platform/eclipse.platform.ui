package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
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
	 * Text height in dialog units (value 100).
	 */
	public int TEXT_HEIGHT = 100;

	private final String NAME = WorkbenchMessages.getString("AboutPluginsDialog.pluginName");	//$NON-NLS-1$
	private final String DESCRIPTION = 	WorkbenchMessages.getString("AboutPluginsDialog.description");	//$NON-NLS-1$
	private final String VERSION = WorkbenchMessages.getString("AboutPluginsDialog.version");	//$NON-NLS-1$
	private final String PROVIDER = WorkbenchMessages.getString("AboutPluginsDialog.provider");	//$NON-NLS-1$
	private final String COPYRIGHT = WorkbenchMessages.getString("AboutPluginsDialog.copyright");	//$NON-NLS-1$
	private final String WEBSITE = WorkbenchMessages.getString("AboutPluginsDialog.webSite");	//$NON-NLS-1$

	private String labelNames[] = {
		NAME, 
		DESCRIPTION, 
		VERSION, 
		PROVIDER, 
		COPYRIGHT, 
		WEBSITE 
	};

	private IPluginDescriptor[] pluginInfo;
	private ProductInfo platformInfo;
	private List pluginsList;
/**
 * Constructor for AboutPluginsDialog
 */
public AboutPluginsDialog(Shell parentShell) {
	super(parentShell);
	if (pluginInfo == null) {
		pluginInfo = Platform.getPluginRegistry().getPluginDescriptors();
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
 * Creates and returns the contents of the dialog.
 *
 * Subclasses should overide.
 *
 * @param the parent composite to contain the dialog area
 * @return the dialog area control
 */
protected Control createDialogArea(Composite parent) {
		
	Composite outer = (Composite)super.createDialogArea(parent);
	outer.setSize(outer.computeSize(SWT.DEFAULT, SWT.DEFAULT));

	createList(outer);
	createText(outer);

 	GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
	gridData.grabExcessVerticalSpace = true;
	gridData.grabExcessHorizontalSpace = true;

	return outer;
}

/**
 * Creates the list showing all plugin names.
 *
 * @param the parent composite to contain the dialog area
 */
protected void createList(Composite parent){
	GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 2;
    parent.setLayout(gridLayout);
    
	pluginsList = new List(parent, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
	String [] pluginNames = new String[pluginInfo.length];
	for (int i=0; i < pluginInfo.length; i++){
		pluginNames[i] = pluginInfo[i].getUniqueIdentifier().toString();
	}
	
	pluginsList.setItems(pluginNames);
	GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
	gridData.verticalSpan = 4;
	gridData.horizontalSpan = 2;
	int listHeight = pluginsList.getItemHeight() * 12;
	Rectangle trim = pluginsList.computeTrim(0, 0, 0, listHeight);
	gridData.heightHint = trim.height;
	gridData.grabExcessVerticalSpace = true;
	pluginsList.setLayoutData(gridData);
}

/**
 * Create the text showing selected plugin atrributes.
 *
 * @param the parent composite to contain the dialog area
 */
protected void createText(Composite parent){
 	int style = SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER;
 	String text = "default string";
	Display display;
 	Color bg = null;

 	Group pluginInfo = new Group(parent, SWT.NULL);
 	pluginInfo.setText("About " + pluginsList.getSelection() + " Plugin");
 	GridLayout gLayout= new GridLayout();
 	gLayout.numColumns = 2;
 	pluginInfo.setLayout(gLayout);
 	GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 	gridData.horizontalSpan = 2;
 	pluginInfo.setLayoutData(gridData);
 	
	for (int i=1; i < labelNames.length; i++){

		if (labelNames[i].equalsIgnoreCase(DESCRIPTION)){
		 	style = SWT.MULTI | SWT.READ_ONLY | SWT.BORDER;
		  	text = "This is where the plugin description info will be displayed.";
		}
		if (labelNames[i].equalsIgnoreCase(VERSION))
		 	text = "This is where the plugin version info will be displayed.";
		if (labelNames[i].equalsIgnoreCase(PROVIDER))
			text = "This is where the plugin provider info will be displayed.";
		if (labelNames[i].equalsIgnoreCase(COPYRIGHT))
	 		text = "This is where the plugin copyright info will be displayed.";
		if (labelNames[i].equalsIgnoreCase(WEBSITE)){
 		 	text = "This is where the plugin web site info will be displayed.";
		 	/* set background to blue here
			//display = parent.getDisplay();
			bg = display.getSystemColor(SWT.COLOR_BLUE);
			*/
		}
		
		new Label(pluginInfo, SWT.NULL).setText(labelNames[i]);
		
		Text infoField = new Text(parent, style);
		infoField.setText(text);
		if (bg != null)
			infoField.setBackground(bg);

	 	gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
		//gridData.verticalSpan = 4;
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		//gridData.heightHint = convertVerticalDLUsToPixels(TEXT_HEIGHT);
		infoField.setLayoutData(gridData);
	}
//	String description = "insert description here";
//	String version = info[i].getVersionIdentifier().toString();
//	String provider = info[i].getProviderName().toString();
//	//insert copyright and website code in the following 2 lines
//	String copyright = "insert copyright here";
//	String webSite = "insert website here";
}

protected void createColumns(){
//	/* create table headers */
//	int[] columnWidths = {150, 150, 50, 100, 100, 150};
//	for (int i = 0; i < columnTitles.length; i++) {
//		TableColumn tableColumn = new TableColumn(vendorInfo, SWT.NULL);
//		tableColumn.setWidth(columnWidths[i]);
//		tableColumn.setText(columnTitles[i]);
//	}	
//		
//	/* fill each row of the table with plugin registry info */
//	for (int i=0; i < info.length; i++) {
//		TableItem item = new TableItem (vendorInfo, SWT.NULL);
//
//		/* fill each cell */
//
//			String pluginName = info[i].getUniqueIdentifier().toString();
//			// insert description code in the following line
//			String description = "insert description here";
//			String version = info[i].getVersionIdentifier().toString();
//			String provider = info[i].getProviderName().toString();
//			//insert copyright and website code in the following 2 lines
//			String copyright = "insert copyright here";
//			String webSite = "insert website here";
//			String [] row = {pluginName, description, version, provider, copyright, webSite};

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

//			item.setText(row);

//	}
		
}
		
}

