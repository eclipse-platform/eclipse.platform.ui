package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.program.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.ProgramImageDescriptor;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.util.*;
import java.io.File;


/**
 * This class is used to allow the user to select a dialog from the set of
 * internal and external editors.
 *
 * @private
 *      This class is internal to the workbench and must not be called outside the workbench
*/

public class EditorSelectionDialog extends Dialog implements Listener {
	private EditorDescriptor selectedEditor;
	private Button externalButton;
	private Table editorTable;
	private Button browseExternalEditorsButton;
	private Button internalButton;
	private static final String EditorSelectionDialog = "FileSystemExportPage1.CreateDirectoriesForSelectedContainers";//$NON-NLS-1$
	private static final String STORE_ID_INTERNAL_EXTERNAL = "EditorSelectionDialog.STORE_ID_INTERNAL_EXTERNAL";//$NON-NLS-1$
	private String message = WorkbenchMessages.getString("EditorSelection.chooseAnEditor"); //$NON-NLS-1$
	// collection of IEditorDescriptor
	private IEditorDescriptor[] externalEditors;
	private IEditorDescriptor[] internalEditors;
	private Image[] externalEditorImages;
	private Image[] internalEditorImages;
	private static final String Executable_Filter;
	static {
		if(SWT.getPlatform().equals("win32")) {//$NON-NLS-1$
			Executable_Filter = "*.exe";//$NON-NLS-1$
		} else {
			Executable_Filter = "*";	//$NON-NLS-1$
		}
	}
public EditorSelectionDialog(Shell parentShell) {
	super(parentShell);
}
	/**
	 * Called just before Shell becomes visible.
	 * It is ok to access SWT widgets because aboutToShow is always called from
	 * within UI thread.
	 */
	protected void aboutToShow() {
		updateEnableState();
	}
/**
 * This method is called if a button has been pressed.
 */
protected void buttonPressed(int buttonId) {
	if (buttonId == IDialogConstants.OK_ID) 
		saveWidgetValues();
	super.buttonPressed(buttonId); 
}
/**
 * Close the window.
 */
public boolean close() {
	if (internalEditorImages != null) {
		for (int i = 0; i < internalEditorImages.length; i++) {
			internalEditorImages[i].dispose();
		}
		internalEditorImages = null;
	}
	if (externalEditorImages != null) {
		for (int i = 0; i < externalEditorImages.length; i++) {
			externalEditorImages[i].dispose();
		}
		externalEditorImages = null;
	}       
	return super.close();
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	shell.setText(WorkbenchMessages.getString("EditorSelection.title")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(shell, new Object[] {IHelpContextIds.EDITOR_SELECTION_DIALOG});
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
	// create main group
	Composite contents = (Composite)super.createDialogArea(parent);
	((GridLayout)contents.getLayout()).numColumns = 2;

	// begin the layout
	Label textLabel = new Label(contents,SWT.NONE);
	textLabel.setText(message);
	GridData data = new GridData();
	data.horizontalSpan = 2;
	textLabel.setLayoutData(data);

	internalButton = new Button(contents, SWT.RADIO | SWT.LEFT);
	internalButton.setText(WorkbenchMessages.getString("EditorSelection.internal")); //$NON-NLS-1$
	internalButton.addListener(SWT.Selection, this);
	data = new GridData();
	data.horizontalSpan = 1;
	internalButton.setLayoutData(data);

	externalButton = new Button(contents, SWT.RADIO | SWT.LEFT);
	externalButton.setText(WorkbenchMessages.getString("EditorSelection.external")); //$NON-NLS-1$
	externalButton.addListener(SWT.Selection, this);
	data = new GridData();
	data.horizontalSpan = 1;
	externalButton.setLayoutData(data);
		
	editorTable = new Table(contents, SWT.SINGLE | SWT.BORDER);
	editorTable.addListener(SWT.Selection, this);
	editorTable.addListener(SWT.DefaultSelection, this);
	data = new GridData();
	data.horizontalAlignment= data.FILL;
	data.grabExcessHorizontalSpace= true;
	data.verticalAlignment= data.FILL;
	data.grabExcessVerticalSpace= true;
	data.horizontalSpan = 2;
	editorTable.setLayoutData(data);
	data.heightHint = editorTable.getItemHeight()*12;
	
	browseExternalEditorsButton = new Button(contents, SWT.PUSH);
	browseExternalEditorsButton.setText(WorkbenchMessages.getString("EditorSelection.browse")); //$NON-NLS-1$
	browseExternalEditorsButton.addListener(SWT.Selection, this);
	data = new GridData();
	data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
	int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
	data.widthHint = Math.max(widthHint, browseExternalEditorsButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	browseExternalEditorsButton.setLayoutData(data);
	
	restoreWidgetValues();  // Place buttons to the appropriate state
	
	fillEditorTable();

	updateEnableState();
	
	return contents;
}
protected void fillEditorTable() {
	editorTable.removeAll();
	editorTable.update();
	IEditorDescriptor[] editors;
	Image[] images;
	if (internalButton.getSelection()) {
		editors = getInternalEditors();
		images = internalEditorImages;
	} else {
		editors = getExternalEditors();
		images = externalEditorImages;
	}

	// 1FWHIEX: ITPUI:WINNT - Need to call setRedraw
	editorTable.setRedraw(false);
	for (int i = 0; i < editors.length; i++) {
		TableItem item = new TableItem(editorTable, SWT.NULL);
		item.setData(editors[i]);
		item.setText(editors[i].getLabel());
		item.setImage(images[i]);
	}
	editorTable.setRedraw(true);
}
/**
 * Return the dialog store to cache values into
 */
 
protected IDialogSettings getDialogSettings() {
	IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
	IDialogSettings section = workbenchSettings.getSection("EditorSelectionDialog");//$NON-NLS-1$
	if(section == null)
		section = workbenchSettings.addNewSection("EditorSelectionDialog");//$NON-NLS-1$
	return section;
}
/**
 * Get a list of registered programs from the OS
 */
protected IEditorDescriptor[] getExternalEditors() {
	if (externalEditors == null) {
		// Since this can take a while, show the busy
		// cursor. If the dialog is not yet visible,
		// then use the parent shell.
		Control shell = getShell();
		if (!shell.isVisible()) {
			Control topShell = shell.getParent();
			if (topShell != null)
				shell = topShell;
		}
		Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
		shell.setCursor(busy);
		// Get the external editors available
		EditorRegistry reg = (EditorRegistry)WorkbenchPlugin.getDefault().getEditorRegistry();
		externalEditors = reg.getSortedEditorsFromOS();
		externalEditorImages = getImages(externalEditors);
		// Clean up
		shell.setCursor(null);
		busy.dispose();
	}
	return externalEditors;
}
/**
 * Returns an array of images for the given array of editors
 */
protected Image[] getImages(IEditorDescriptor[] editors) {
	Image[] images = new Image[editors.length];
	for (int i = 0; i < editors.length; i++) {
		images[i] = editors[i].getImageDescriptor().createImage();
	}
	return images;
}
/**
 * Returns the internal editors
 */
protected IEditorDescriptor[] getInternalEditors() {
	if (internalEditors == null) {
		EditorRegistry reg = (EditorRegistry)WorkbenchPlugin.getDefault().getEditorRegistry();
		internalEditors = reg.getSortedEditorsFromPlugins();
		internalEditorImages = getImages(internalEditors);
	}
	return internalEditors;
}
/**
 * Return the editor the user selected
 */
public IEditorDescriptor getSelectedEditor() {
	return selectedEditor;
}
public void handleEvent(Event event) {
	if (event.widget == externalButton) {
		fillEditorTable();
	} else if (event.widget == browseExternalEditorsButton) {
		promptForExternalEditor();
	} else if (event.widget == editorTable) {
		if (editorTable.getSelectionIndex() != -1) {
			selectedEditor = (EditorDescriptor)editorTable.getSelection()[0].getData();
		} else {
			selectedEditor = null;
		}
	}
	updateEnableState();
}
protected void promptForExternalEditor() {
	FileDialog dialog = new FileDialog(getShell());
	dialog.setFilterExtensions(new String[] {Executable_Filter});
	String result = dialog.open();
	if (result != null) {
		EditorDescriptor editor = new EditorDescriptor();
		editor.setFileName(result);
		editor.setID(result);
		//Isolate the program name (no directory or extension)
		int start = result.lastIndexOf(File.separator);
		String name;
		if (start != -1) {
			name = result.substring(start + 1);
		} else {
			name = result;
		}
		int end = name.lastIndexOf('.');
		if (end != -1) {
			name = name.substring(0, end);
		}
		editor.setName(name);
		// get the program icon without storing it in the registry
		ImageDescriptor imageDescriptor = new ProgramImageDescriptor(result, 0);
		editor.setImageDescriptor(imageDescriptor);
		// pretend we had obtained it from the list of os registered editors
		TableItem ti = new TableItem(editorTable, SWT.NULL);
		ti.setData(editor);
		ti.setText(editor.getLabel());
		Image image = editor.getImageDescriptor().createImage();
		ti.setImage(image);

		// need to pass an array to setSelection -- 1FSKYVO: SWT:ALL - inconsistent setSelection api on Table 
		editorTable.setSelection(new TableItem[] {ti});
		editorTable.showSelection();
		editorTable.setFocus(); 
		selectedEditor = editor;

		/* add to our collection of cached external editors in case the user
		flips back and forth between internal/external */
		IEditorDescriptor[] newEditors = new IEditorDescriptor[externalEditors.length + 1];
		System.arraycopy(externalEditors,0,newEditors,0,externalEditors.length);
		newEditors[newEditors.length-1] = editor;
		externalEditors = newEditors;
		
		Image[] newImages = new Image[externalEditorImages.length+1];
		System.arraycopy(externalEditorImages, 0, newImages, 0, externalEditorImages.length);
		newImages[newImages.length-1] = image;
		externalEditorImages = newImages;
	}
}
/**
 *  Use the dialog store to restore widget values to the values that they held
 *  last time this wizard was used to completion
 */
protected void restoreWidgetValues() {
	IDialogSettings settings = getDialogSettings();
	boolean wasExternal = settings.getBoolean(STORE_ID_INTERNAL_EXTERNAL);  
	internalButton.setSelection(!wasExternal);
	externalButton.setSelection(wasExternal);
}
/**
 *  Since Finish was pressed, write widget values to the dialog store so that they
 *  will persist into the next invocation of this wizard page
 */
protected void saveWidgetValues() {
	IDialogSettings settings = getDialogSettings();
	// record whether use was viewing internal or external editors
	settings.put(STORE_ID_INTERNAL_EXTERNAL,!internalButton.getSelection());
}
/**
 * Set the message displayed by this message dialog
 */
public void setMessage(String aMessage) {
	message = aMessage;
}
public void updateEnableState() {
	boolean enableExternal = externalButton.getSelection();
	browseExternalEditorsButton.setEnabled(enableExternal);
}
}
