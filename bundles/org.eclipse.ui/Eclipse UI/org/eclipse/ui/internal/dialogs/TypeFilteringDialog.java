package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.FileEditorMapping;
import org.eclipse.jface.dialogs.InputDialog;
import java.util.ArrayList;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.dialogs.FileEditorMappingContentProvider;
import org.eclipse.ui.dialogs.FileEditorMappingLabelProvider;
import org.eclipse.ui.help.*;
import org.eclipse.ui.internal.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * The TypeSelectionDialog is a SelectionDialog that allows the user to select a file editor.
 */
public class TypeFilteringDialog extends SelectionDialog {
	Button addTypesButton;

	Collection initialSelections;

	// the visual selection widget group
	private CheckboxTableViewer listViewer;

	// sizing constants
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 250;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 300;

	private final static String ADD_TYPE_TITLE = WorkbenchMessages.getString("TypesFiltering.addExtension"); //$NON-NLS-1$
	private final static String TYPE_DELIMITER = WorkbenchMessages.getString("TypesFiltering.typeDelimiter"); //$NON-NLS-1$
	Text userDefinedText;

	IFileEditorMapping[] currentInput;
/**
 * Creates a type selection dialog using the supplied entries. Set the initial selections to those
 * whose extensions match the preselections.
 */
public TypeFilteringDialog(Shell parentShell, Collection preselections) {
	super(parentShell);
	setTitle(WorkbenchMessages.getString("TypesFiltering.title")); //$NON-NLS-1$
	this.initialSelections = preselections;
	setMessage(WorkbenchMessages.getString("TypesFiltering.message")); //$NON-NLS-1$
}
/**
 * Add the selection and deselection buttons to the dialog.
 * @param composite org.eclipse.swt.widgets.Composite
 */
private void addSelectionButtons(Composite composite) {

	Composite buttonComposite = new Composite(composite, SWT.RIGHT);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	buttonComposite.setLayout(layout);
	GridData data =
		new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
	data.grabExcessHorizontalSpace = true;
	composite.setData(data);

	Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, WorkbenchMessages.getString("WizardTransferPage.selectAll"), false); //$NON-NLS-1$

	SelectionListener listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			listViewer.setAllChecked(true);
		}
	};
	selectButton.addSelectionListener(listener);


	Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, WorkbenchMessages.getString("WizardTransferPage.deselectAll"), false); //$NON-NLS-1$

	listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			listViewer.setAllChecked(false);

		}
	};
	deselectButton.addSelectionListener(listener);
	

}
/**
 * Add the currently-specified extensions.
 */
private void addUserDefinedEntries(List result) {

	StringTokenizer tokenizer =
		new StringTokenizer(userDefinedText.getText(), TYPE_DELIMITER);

	//Allow the *. and . prefix and strip out the extension
	while (tokenizer.hasMoreTokens()) {
		String currentExtension = tokenizer.nextToken().trim();
		if (!currentExtension.equals("")) { //$NON-NLS-1$
			if (currentExtension.startsWith("*."))//$NON-NLS-1$
				result.add(currentExtension.substring(2));
			else {
				if (currentExtension.startsWith("."))//$NON-NLS-1$
					result.add(currentExtension.substring(1));
				else
					result.add(currentExtension);
			}
		}
	}
}
/**
 * Visually checks the previously-specified elements in this dialog's list 
 * viewer.
 */
private void checkInitialSelections() {

	IFileEditorMapping editorMappings[] =
		PlatformUI.getWorkbench().getEditorRegistry().getFileEditorMappings();
	ArrayList selectedMappings = new ArrayList();

	for (int i = 0; i < editorMappings.length; i++) {
		IFileEditorMapping mapping = editorMappings[i];
		if (this.initialSelections.contains(mapping.getExtension())){
			listViewer.setChecked(mapping, true);
			selectedMappings.add(mapping.getExtension());
		}
	}

	//Now add in the ones not selected to the user defined list
	Iterator initialIterator = this.initialSelections.iterator();
	StringBuffer entries = new StringBuffer();
	while(initialIterator.hasNext()){
		String nextExtension = (String) initialIterator.next();
		if(!selectedMappings.contains(nextExtension)){
			entries.append(nextExtension);
			entries.append(',');
		}
	}
	this.userDefinedText.setText(entries.toString());
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	WorkbenchHelp.setHelp(shell, new Object[] {IHelpContextIds.TYPE_FILTERING_DIALOG});
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createDialogArea(Composite parent) {
	// page group
	Composite composite = (Composite) super.createDialogArea(parent);

	createMessageArea(composite);

	listViewer = new CheckboxTableViewer(composite, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_BOTH);
	data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
	data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
	listViewer.getTable().setLayoutData(data);

	listViewer.setLabelProvider(FileEditorMappingLabelProvider.INSTANCE);
	listViewer.setContentProvider(FileEditorMappingContentProvider.INSTANCE);

	addSelectionButtons(composite);

	createUserEntryGroup(composite);

	initializeViewer();

	// initialize page
	if (this.initialSelections != null && !this.initialSelections.isEmpty())
		checkInitialSelections();

	return composite;
}
/**
 * Create the group that shows the user defined entries for the dialog.
 * @param parent the parent this is being created in.
 */
private void createUserEntryGroup(Composite parent) {

	// destination specification group
	Composite userDefinedGroup = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	userDefinedGroup.setLayout(layout);
	userDefinedGroup.setLayoutData(
		new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

	new Label(userDefinedGroup, SWT.NONE).setText(WorkbenchMessages.getString("TypesFiltering.otherExtensions")); //$NON-NLS-1$

	// user defined entry field
	userDefinedText = new Text(userDefinedGroup, SWT.SINGLE | SWT.BORDER);
	GridData data =
		new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
	userDefinedText.setLayoutData(data);
}
/**
 * Return the input to the dialog.
 */
private IFileEditorMapping[] getInput() {

	//Filter the mappings to be just those with a wildcard extension
	if (currentInput == null) {
		List wildcardEditors = new ArrayList();
		IFileEditorMapping [] allMappings =
			PlatformUI.getWorkbench().getEditorRegistry().getFileEditorMappings();
		for (int i = 0; i < allMappings.length; i++) {
			if (allMappings[i].getName().equals("*"))//$NON-NLS-1$
				wildcardEditors.add(allMappings[i]);
		}
		currentInput = new IFileEditorMapping[wildcardEditors.size()];
		wildcardEditors.toArray(currentInput);
	}

	return currentInput;
}
/**
 * Initializes this dialog's viewer after it has been laid out.
 */
private void initializeViewer() {
	listViewer.setInput(getInput());
}
/**
 * The <code>ListSelectionDialog</code> implementation of this 
 * <code>Dialog</code> method builds a list of the selected elements for later
 * retrieval by the client and closes this dialog.
 */
protected void okPressed() {

	// Get the input children.
	IFileEditorMapping[] children = getInput();

	List list = new ArrayList();

	// Build a list of selected children.
	for (int i = 0; i < children.length; ++i) {
		IFileEditorMapping element = children[i];
		if (listViewer.getChecked(element))
			list.add(element.getExtension());
	}

	addUserDefinedEntries(list);
	setResult(list);
	super.okPressed();
}
}
