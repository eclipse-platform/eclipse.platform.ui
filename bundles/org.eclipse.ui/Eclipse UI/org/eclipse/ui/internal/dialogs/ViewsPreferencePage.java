package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * The ViewsPreferencePage is the page used to set preferences for the look of the
 * views in the workbench.
 */
public class ViewsPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {
	private Button editorTopButton;
	private Button editorBottomButton;
	private Button viewTopButton;
	private Button viewBottomButton;
	private int editorAlignment;
	private int viewAlignment;

	private static final String EDITORS_TITLE = WorkbenchMessages.getString("Editors"); //$NON-NLS-1$
	private static final String VIEWS_TITLE = WorkbenchMessages.getString("Views"); //$NON-NLS-1$
	private static final String TOP_TITLE = WorkbenchMessages.getString("Top"); //$NON-NLS-1$
	private static final String BOTTOM_TITLE = WorkbenchMessages.getString("Bottom"); //$NON-NLS-1$
	private static final String TAB_POSITIONS_LABEL = WorkbenchMessages.getString("ViewPreferences.tabPositions"); //$NON-NLS-1$

	private static final String APPLY_MESSAGE =
		WorkbenchMessages.getString("ViewPreferences.applyMessage"); //$NON-NLS-1$
/**
 * Create a composite that for creating the tab toggle buttons.
 * @param composite Composite
 * @param store IPreferenceStore
 */
private Composite createButtonGroup(Composite composite) {

	Composite buttonComposite = new Composite(composite, SWT.LEFT);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	buttonComposite.setLayout(layout);
	GridData data =
		new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
	composite.setData(data);

	return buttonComposite;

}
/**
 * Creates and returns the SWT control for the customized body 
 * of this preference page under the given parent composite.
 * <p>
 * This framework method must be implemented by concrete
 * subclasses.
 * </p>
 *
 * @param parent the parent composite
 * @return the new control
 */
protected Control createContents(Composite parent) {

	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	this.editorAlignment =
		store.getInt(IPreferenceConstants.EDITOR_TAB_POSITION);
	this.viewAlignment =
		store.getInt(IPreferenceConstants.VIEW_TAB_POSITION);

	Composite composite = new Composite(parent, SWT.NULL);
	composite.setLayout(new GridLayout());
	composite.setLayoutData(
		new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));

	Composite parentGroup = createGroup(composite);

	Composite buttonComposite = createButtonGroup(parentGroup);
	createEditorTabButtonGroup(buttonComposite);
	createViewTabButtonGroup(buttonComposite);

	createSpacer(composite);

	Composite messageComposite = new Composite(composite, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	messageComposite.setLayout(layout);
	messageComposite.setLayoutData(
		new GridData(GridData.HORIZONTAL_ALIGN_FILL));

	Label noteLabel = new Label(messageComposite,SWT.BOLD );
	noteLabel.setText(WorkbenchMessages.getString("ViewPreferences.note")); //$NON-NLS-1$
	noteLabel.setFont(JFaceResources.getBannerFont());
	noteLabel.setLayoutData(
		new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
	
	Label messageLabel = new Label(messageComposite,SWT.NONE);
	messageLabel.setText(APPLY_MESSAGE);
	
	return composite;
}
/**
 * Create a composite that contains buttons for selecting tab position for the edit selection. 
 * @param composite Composite
 * @param store IPreferenceStore
 */
private void createEditorTabButtonGroup(Composite composite) {

	Label titleLabel = new Label(composite, SWT.NONE);
	titleLabel.setText(EDITORS_TITLE);

	Composite buttonComposite = createButtonGroup(composite);

	this.editorTopButton = new Button(buttonComposite, SWT.RADIO);
	this.editorTopButton.setText(TOP_TITLE);
	this.editorTopButton.setSelection(this.editorAlignment == SWT.TOP);

	this.editorTopButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			editorAlignment = SWT.TOP;
		}
	});

	this.editorBottomButton = new Button(buttonComposite, SWT.RADIO);
	this.editorBottomButton.setText(BOTTOM_TITLE);
	this.editorBottomButton.setSelection(this.editorAlignment == SWT.BOTTOM);

	this.editorBottomButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			editorAlignment = SWT.BOTTOM;
		}
	});

}
/**
 * Create a group for encapsualting the buttons.
 * @param composite Composite
 * @param store IPreferenceStore
 */
private Composite createGroup(Composite composite) {

	Group group = new Group(composite, SWT.SHADOW_NONE);
	group.setText(TAB_POSITIONS_LABEL);
	//GridLayout
	group.setLayout(new GridLayout());
	//GridData
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	group.setLayoutData(data);

	return group;

}
/**
 * Creates a horizontal spacer line that fills the width of its container.
 *
 * @param parent the parent control
 */
private void createSpacer(Composite parent) {
	Label spacer = new Label(parent, SWT.NONE);
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.verticalAlignment = GridData.BEGINNING;
	spacer.setLayoutData(data);
}
/**
 * Create a composite that contains buttons for selecting tab position for the view selection. 
 * @param composite Composite
 * @param store IPreferenceStore
 */
private void createViewTabButtonGroup(Composite composite) {

	Label titleLabel = new Label(composite, SWT.NONE);
	titleLabel.setText(VIEWS_TITLE);

	Composite buttonComposite = createButtonGroup(composite);

	this.viewTopButton = new Button(buttonComposite, SWT.RADIO);
	this.viewTopButton.setText(TOP_TITLE);
	this.viewTopButton.setSelection(this.viewAlignment == SWT.TOP);

	this.viewTopButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			viewAlignment = SWT.TOP;
		}
	});

	this.viewBottomButton = new Button(buttonComposite, SWT.RADIO);
	this.viewBottomButton.setText(BOTTOM_TITLE);
	this.viewBottomButton.setSelection(this.viewAlignment == SWT.BOTTOM);

	this.viewBottomButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			viewAlignment = SWT.BOTTOM;
		}
	});

}
/**
 * Returns preference store that belongs to the our plugin.
 *
 * @return the preference store for this plugin
 */
protected IPreferenceStore doGetPreferenceStore() {
	return WorkbenchPlugin.getDefault().getPreferenceStore();
}
/**
 * Initializes this preference page for the given workbench.
 * <p>
 * This method is called automatically as the preference page is being created
 * and initialized. Clients must not call this method.
 * </p>
 *
 * @param workbench the workbench
 */
public void init(org.eclipse.ui.IWorkbench workbench) {}
/**
 * The default button has been pressed. 
 */
protected void performDefaults() {
	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	int editorTopValue =
		store.getDefaultInt(IPreferenceConstants.EDITOR_TAB_POSITION);
	editorTopButton.setSelection(editorTopValue == SWT.TOP);
	editorBottomButton.setSelection(editorTopValue == SWT.BOTTOM);

	int viewTopValue =
		store.getDefaultInt(IPreferenceConstants.VIEW_TAB_POSITION);
	viewTopButton.setSelection(viewTopValue == SWT.TOP);
	viewBottomButton.setSelection(viewTopValue == SWT.BOTTOM);

	super.performDefaults();
}
/**
 *	The user has pressed Ok.  Store/apply this page's values appropriately.
 */
public boolean performOk() {
	IPreferenceStore store = getPreferenceStore();

	// store the editor tab value to setting
	store.setValue(IPreferenceConstants.EDITOR_TAB_POSITION, editorAlignment);

	// store the view tab value to setting
	store.setValue(IPreferenceConstants.VIEW_TAB_POSITION, viewAlignment);

	return true;
}
}
