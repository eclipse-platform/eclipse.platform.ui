package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.preference.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.internal.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.util.ArrayList;

public class PerspectivesPreferencePage extends PreferencePage
	implements IWorkbenchPreferencePage
{
	private IWorkbench workbench;
	private PerspectiveRegistry perspectiveRegistry;
	private ArrayList perspectives;
	private String defaultPerspectiveId;
	private ArrayList perspToDelete = new ArrayList();
	private ArrayList perspToRevert = new ArrayList();
	private List list;
	private Button revertButton;
	private Button deleteButton;
	private Button setDefaultButton;

	private static final int LIST_WIDTH = 200;
	private static final int LIST_HEIGHT = 200;
/**
 * Creates the page's UI content.
 */
protected Control createContents(Composite parent) {

	WorkbenchHelp.setHelp(parent, IHelpContextIds.PERSPECTIVES_PREFERENCE_PAGE);

	noDefaultAndApplyButton();
	
	// define container & its gridding
	Composite pageComponent = new Composite(parent, SWT.NULL);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	layout.marginWidth = 0;
	layout.marginHeight = 0;
	pageComponent.setLayout(layout);
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	pageComponent.setLayoutData(data);

	// Add the label
	Label label = new Label(pageComponent, SWT.LEFT);
	label.setText(WorkbenchMessages.getString("PerspectivesPreference.available")); //$NON-NLS-1$
	data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.horizontalSpan = 2;
	label.setLayoutData(data);
		
	// Add perspective list.
	list = new List(pageComponent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	list.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			updateButtons();
		}
	});
	GridData spec = new GridData(GridData.FILL_BOTH);
	spec.grabExcessHorizontalSpace = true;
	spec.grabExcessVerticalSpace = true;
	spec.widthHint = LIST_WIDTH;
	spec.heightHint = LIST_HEIGHT;
	list.setLayoutData(spec);

	// Populate the perspective list
	IPerspectiveDescriptor[] persps = perspectiveRegistry.getPerspectives();
	perspectives = new ArrayList(persps.length);
	for (int i = 0; i < persps.length; i++)
		perspectives.add(i, persps[i]);
	defaultPerspectiveId = perspectiveRegistry.getDefaultPerspective();
	updateList();
	
	// Create vertical button bar.
	Composite buttonBar = (Composite)createVerticalButtonBar(pageComponent);
	data = new GridData(GridData.FILL_BOTH);
	buttonBar.setLayoutData(data);

	// Return results.
	return pageComponent;
}
/**
 * Creates a new vertical button with the given id.
 * <p>
 * The default implementation of this framework method
 * creates a standard push button, registers for selection events
 * including button presses and help requests, and registers
 * default buttons with its shell.
 * The button id is stored as the buttons client data.
 * </p>
 *
 * @param parent the parent composite
 * @param buttonId the id of the button (see
 *  <code>IDialogConstants.*_ID</code> constants 
 *  for standard dialog button ids)
 * @param label the label from the button
 * @param defaultButton <code>true</code> if the button is to be the
 *   default button, and <code>false</code> otherwise
 */
protected Button createVerticalButton(Composite parent, String label, boolean defaultButton) {
	Button button = new Button(parent, SWT.PUSH);

	button.setText(label);
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
	int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
	data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	button.setLayoutData(data);
	
	button.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			verticalButtonPressed(event.widget);
		}
	});
	button.setToolTipText(label);
	if (defaultButton) {
		Shell shell = parent.getShell();
		if (shell != null) {
			shell.setDefaultButton(button);
		}
	}
	button.setFont(parent.getFont());
	return button;
}
/**
 * Creates and returns the vertical button bar.
 *
 * @param parent the parent composite to contain the button bar
 * @return the button bar control
 */
protected Control createVerticalButtonBar(Composite parent) {
	// Create composite.
	Composite composite = new Composite(parent, SWT.NONE);

	// create a layout with spacing and margins appropriate for the font size.
	GridLayout layout = new GridLayout();
	layout.numColumns = 1;
	layout.marginWidth = 5;
	layout.marginHeight = 0;
	layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
	layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
	composite.setLayout(layout);

	// Add the buttons to the button bar.
	setDefaultButton = createVerticalButton(composite,WorkbenchMessages.getString("PerspectivesPreference.MakeDefault"), false);  //$NON-NLS-1$
	setDefaultButton.setToolTipText(WorkbenchMessages.getString("PerspectivesPreference.MakeDefaultTip")); //$NON-NLS-1$
	
	revertButton = createVerticalButton(composite, WorkbenchMessages.getString("PerspectivesPreference.Reset"), false); //$NON-NLS-1$
	revertButton.setToolTipText(WorkbenchMessages.getString("PerspectivesPreference.ResetTip")); //$NON-NLS-1$
	
	deleteButton = createVerticalButton(composite, WorkbenchMessages.getString("PerspectivesPreference.Delete"), false);  //$NON-NLS-1$
	deleteButton.setToolTipText(WorkbenchMessages.getString("PerspectivesPreference.DeleteTip")); //$NON-NLS-1$
	updateButtons();

	return composite;
}
/**
 * @see IWorkbenchPreferencePage
 */
public void init(IWorkbench aWorkbench){
	this.workbench = aWorkbench;
	this.perspectiveRegistry = (PerspectiveRegistry) workbench.getPerspectiveRegistry();
}
/**
 * Apply the user's changes if any
 */
public boolean performOk() {
	// Set the default perspective
	if (!defaultPerspectiveId.equals(perspectiveRegistry.getDefaultPerspective()))
		perspectiveRegistry.setDefaultPerspective(defaultPerspectiveId);

	// Delete the perspectives
	for (int i = 0; i < perspToDelete.size(); i++)
		perspectiveRegistry.deletePerspective((IPerspectiveDescriptor) perspToDelete.get(i));
		
	// Revert the perspectives
	for (int i = 0; i < perspToRevert.size(); i++)
		((PerspectiveDescriptor) perspToRevert.get(i)).revertToPredefined();
		
	// Update perspective history.
	((Workbench)workbench).getPerspectiveHistory().refreshFromRegistry();

	return true;
}
/**
 * Update the button enablement state.
 */
protected void updateButtons() {
	// Get selection.
	int index = list.getSelectionIndex();

	// Map it to the perspective descriptor	
	PerspectiveDescriptor desc = null;
	if (index > -1)
		desc = (PerspectiveDescriptor)perspectives.get(index);

	// Do enable.
	if (desc != null) {
		revertButton.setEnabled(desc.isPredefined() &&
			desc.hasCustomFile() &&
			!perspToRevert.contains(desc));	
		deleteButton.setEnabled(!desc.isPredefined());
		setDefaultButton.setEnabled(true);
	} else {
		revertButton.setEnabled(false);	
		deleteButton.setEnabled(false);
		setDefaultButton.setEnabled(false);
	}
}
/**
 * Update the list items.
 */
protected void updateList() {
	list.removeAll();
	for (int i = 0; i < perspectives.size(); i++) {
		IPerspectiveDescriptor desc = (IPerspectiveDescriptor) perspectives.get(i);
		String label = desc.getLabel();
		if (desc.getId().equals(defaultPerspectiveId))
			label = WorkbenchMessages.format("PerspectivesPreference.defaultLabel", new Object[] {label}); //$NON-NLS-1$
		list.add(label, i);
	}
}
/**
 * Notifies that this page's button with the given id has been pressed.
 *
 * @param buttonId the id of the button that was pressed (see
 *  <code>IDialogConstants.*_ID</code> constants)
 */
protected void verticalButtonPressed(Widget button) {
	// Get selection.
	int index = list.getSelectionIndex();

	// Map it to the perspective descriptor	
	PerspectiveDescriptor desc = null;
	if (index > -1)
		desc = (PerspectiveDescriptor)perspectives.get(index);
	else
		return;

	// Take action.
	if (button == revertButton) {
		if (desc.isPredefined() && !perspToRevert.contains(desc)) {
			perspToRevert.add(desc);
		}
	} else if (button == deleteButton) {
		if (!desc.isPredefined() && !perspToDelete.contains(desc)) {
			perspToDelete.add(desc);
			perspToRevert.remove(desc);
			perspectives.remove(desc);
			updateList();
		}
	} else if (button == setDefaultButton) {
		defaultPerspectiveId = desc.getId();
		updateList();
		list.setSelection(index);
	}

	updateButtons();
}
}
