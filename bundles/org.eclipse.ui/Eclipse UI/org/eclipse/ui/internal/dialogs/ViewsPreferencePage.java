package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.help.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
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
	private Button openEmbedButton;
	private Button openFastButton;
	private Button openFloatButton;
	private int editorAlignment;
	private int viewAlignment;
	private int openViewMode;

	private static final String EDITORS_TITLE = WorkbenchMessages.getString("ViewsPreference.editors"); //$NON-NLS-1$
	private static final String EDITORS_TOP_TITLE = WorkbenchMessages.getString("ViewsPreference.editors.top"); //$NON-NLS-1$
	private static final String EDITORS_BOTTOM_TITLE = WorkbenchMessages.getString("ViewsPreference.editors.bottom"); //$NON-NLS-1$
	private static final String VIEWS_TITLE = WorkbenchMessages.getString("ViewsPreference.views"); //$NON-NLS-1$
	private static final String VIEWS_TOP_TITLE = WorkbenchMessages.getString("ViewsPreference.views.top"); //$NON-NLS-1$
	private static final String VIEWS_BOTTOM_TITLE = WorkbenchMessages.getString("ViewsPreference.views.bottom"); //$NON-NLS-1$
	private static final String OVM_TITLE = WorkbenchMessages.getString("OpenViewMode.title"); //$NON-NLS-1$
	private static final String OVM_EMBED = WorkbenchMessages.getString("OpenViewMode.embed"); //$NON-NLS-1$
	private static final String OVM_FAST = WorkbenchMessages.getString("OpenViewMode.fast"); //$NON-NLS-1$
	private static final String OVM_FLOAT = WorkbenchMessages.getString("OpenViewMode.float"); //$NON-NLS-1$
	private static final String NOTE_LABEL = WorkbenchMessages.getString("ViewsPreference.note"); //$NON-NLS-1$
	private static final String TAB_POSITIONS_LABEL = WorkbenchMessages.getString("ViewsPreference.tabPositions"); //$NON-NLS-1$
	private static final String APPLY_MESSAGE = WorkbenchMessages.getString("ViewsPreference.applyMessage"); //$NON-NLS-1$
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
	buttonComposite.setData(data);

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

	WorkbenchHelp.setHelp(parent, new DialogPageContextComputer(this, IHelpContextIds.VIEWS_PREFERENCE_PAGE));

	IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
	this.editorAlignment =
		store.getInt(IPreferenceConstants.EDITOR_TAB_POSITION);
	this.viewAlignment =
		store.getInt(IPreferenceConstants.VIEW_TAB_POSITION);
	openViewMode = 
		store.getInt(IPreferenceConstants.OPEN_VIEW_MODE);

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

	final Label noteLabel = new Label(messageComposite,SWT.BOLD );
	noteLabel.setText(NOTE_LABEL);
	noteLabel.setFont(JFaceResources.getBannerFont());
	noteLabel.setLayoutData(
		new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		
	final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if(JFaceResources.BANNER_FONT.equals(event.getProperty())) {
				noteLabel.setFont(JFaceResources.getFont(JFaceResources.BANNER_FONT));
			}
		}
	};
	
	noteLabel.addDisposeListener(new DisposeListener() {
		public void widgetDisposed(DisposeEvent event) {
			JFaceResources.getFontRegistry().removeListener(fontListener);
		}
	});
	
	createSpacer(composite);
	createOpenViewButtonGroup(composite);

	JFaceResources.getFontRegistry().addListener(fontListener);
	
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
	this.editorTopButton.setText(EDITORS_TOP_TITLE);
	this.editorTopButton.setSelection(this.editorAlignment == SWT.TOP);

	this.editorTopButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			editorAlignment = SWT.TOP;
		}
	});

	this.editorBottomButton = new Button(buttonComposite, SWT.RADIO);
	this.editorBottomButton.setText(EDITORS_BOTTOM_TITLE);
	this.editorBottomButton.setSelection(this.editorAlignment == SWT.BOTTOM);

	this.editorBottomButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			editorAlignment = SWT.BOTTOM;
		}
	});

}
/**
 * Create a composite that contains buttons for selecting open view mode.
 * @param composite Composite
 */
private void createOpenViewButtonGroup(Composite composite) {

	Label titleLabel = new Label(composite, SWT.NONE);
	titleLabel.setText(OVM_TITLE);

	Composite buttonComposite = new Composite(composite, SWT.LEFT);
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	buttonComposite.setLayout(layout);
	GridData data =
		new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL);
	composite.setData(data);

	openEmbedButton = new Button(buttonComposite, SWT.RADIO);
	openEmbedButton.setText(OVM_EMBED);
	openEmbedButton.setSelection(openViewMode == IPreferenceConstants.OVM_EMBED);
	openEmbedButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			openViewMode = IPreferenceConstants.OVM_EMBED;
		}
	});

	openFastButton = new Button(buttonComposite, SWT.RADIO);
	openFastButton.setText(OVM_FAST);
	openFastButton.setSelection(openViewMode == IPreferenceConstants.OVM_FAST);
	openFastButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			openViewMode = IPreferenceConstants.OVM_FAST;
		}
	});

	if (getShell().isReparentable()) {
		openFloatButton = new Button(buttonComposite, SWT.RADIO);
		openFloatButton.setText(OVM_FLOAT);
		openFloatButton.setSelection(openViewMode == IPreferenceConstants.OVM_FLOAT);
		openFloatButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openViewMode = IPreferenceConstants.OVM_FLOAT;
			}
		});
	}
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
	this.viewTopButton.setText(VIEWS_TOP_TITLE);
	this.viewTopButton.setSelection(this.viewAlignment == SWT.TOP);

	this.viewTopButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			viewAlignment = SWT.TOP;
		}
	});

	this.viewBottomButton = new Button(buttonComposite, SWT.RADIO);
	this.viewBottomButton.setText(VIEWS_BOTTOM_TITLE);
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

	int value = store.getDefaultInt(IPreferenceConstants.OPEN_VIEW_MODE);
	openEmbedButton.setSelection(value == IPreferenceConstants.OVM_EMBED);
	openFastButton.setSelection(value == IPreferenceConstants.OVM_FAST);
	if (openFloatButton != null) 
		openFloatButton.setSelection(value == IPreferenceConstants.OVM_FLOAT);

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

	// store the open view mode to setting
	store.setValue(IPreferenceConstants.OPEN_VIEW_MODE, openViewMode);

	return true;
}
}
