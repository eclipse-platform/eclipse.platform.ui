package org.eclipse.debug.internal.ui.preferences;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.preferences.DebugActionGroupsManager.DebugActionGroup;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * Preference page to display the available debug action groups, and
 * solicits a list of selections from the user.
 */
public class DebugActionGroupsPreferencePage  extends PreferencePage implements IWorkbenchPreferencePage {
	// widgets.
	private CheckboxTableViewer fDebugActionGroupViewer;
	private Label actionLabel;
	private TableViewer actionViewer;
	private boolean fStateChanged= false;

	// sizing constants
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 100;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 200;
	
	/**
	 * Creates an action set selection preference page.
	 */
	public DebugActionGroupsPreferencePage() {
		super();
		setPreferenceStore(DebugUIPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * @see PreferencePage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		WorkbenchHelp.setHelp(getControl(), IDebugHelpContextIds.DEBUG_ACTION_GROUPS_PREFERENCE_PAGE);
	}
	
	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = true;
		layout.marginHeight=0;
		layout.marginWidth=0;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);	

		// description
		Label descLabel = new Label(composite, SWT.WRAP);
		descLabel.setText(DebugUIMessages.getString("DebugActionGroupsPreferencePage.Select_the_action_groups_to_be_displayed_in_Debug_views_1")); //$NON-NLS-1$
		descLabel.setFont(parent.getFont());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		descLabel.setLayoutData(data);

		// Setup the action set list selection...
		// ...first a composite group
		Composite actionSetGroup = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		actionSetGroup.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		actionSetGroup.setLayoutData(data);

		// ...second the label
		Label selectionLabel = new Label(actionSetGroup, SWT.NONE);
		selectionLabel.setText(DebugUIMessages.getString("DebugActionGroupsPreferencePage.&Available_Debug_Action_Groups__2")); //$NON-NLS-1$
		selectionLabel.setFont(parent.getFont());

		// ...third the checkbox list
		Table viewActionSetTable= new Table(actionSetGroup, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		
		TableLayout tableLayout= new TableLayout();
		ColumnLayoutData[] columnLayoutData= new ColumnLayoutData[1];
		columnLayoutData[0]= new ColumnWeightData(100);		
		tableLayout.addColumnData(columnLayoutData[0]);
		viewActionSetTable.setLayout(tableLayout);
		new TableColumn(viewActionSetTable, SWT.NONE);

		fDebugActionGroupViewer = new CheckboxTableViewer(viewActionSetTable);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		fDebugActionGroupViewer.getTable().setLayoutData(data);
		fDebugActionGroupViewer.setLabelProvider(new DebugActionGroupsLabelProvider());
		IContentProvider contentProvider= new DebugActionGroupsContentProvider(fDebugActionGroupViewer);
		fDebugActionGroupViewer.setContentProvider(contentProvider);
		fDebugActionGroupViewer.setInput(DebugUIPlugin.getDefault());
		fDebugActionGroupViewer.setSorter(new WorkbenchViewerSorter());
		fDebugActionGroupViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				DebugActionGroup viewActionSet = null;
				if (sel.getFirstElement() instanceof DebugActionGroup) {
					viewActionSet = (DebugActionGroup) sel.getFirstElement();
				}
				if (viewActionSet != actionViewer.getInput()) {
					actionViewer.setInput(viewActionSet);
				}
			}
		});
		fDebugActionGroupViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				DebugActionGroup viewActionSet = (DebugActionGroup)event.getElement();
				viewActionSet.setVisible(event.getChecked());
				fStateChanged= true;
			}
		});

		// Setup the action list for the action set selected...
		// ...first a composite group
		Composite actionGroup = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		actionGroup.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		actionGroup.setLayoutData(data);

		// ...second the label
		actionLabel = new Label(actionGroup, SWT.NONE);
		actionLabel.setText(DebugUIMessages.getString("DebugActionGroupsPreferencePage.Actions_in_Group__3")); //$NON-NLS-1$

		// ...third the list of actions
		actionViewer = new TableViewer(actionGroup, SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		actionViewer.getTable().setLayoutData(data);
		actionViewer.setLabelProvider(new DebugActionGroupsLabelProvider());
		actionViewer.setContentProvider(new DebugActionGroupsActionContentProvider());
		actionViewer.setSorter(new WorkbenchViewerSorter());
		
		Display disp = Display.getCurrent();
		Color clr = disp.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		actionViewer.getTable().setBackground(clr);
		return composite;
	}
	
	/**
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	/**
	 * @see IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (fStateChanged) {
			DebugActionGroupsManager.getDefault().updateDebugActionGroups();
			persistDebugActionGroups();
		}
		return true;
	}
	
	protected void persistDebugActionGroups() {
		IStructuredContentProvider contentProvider= (IStructuredContentProvider)fDebugActionGroupViewer.getContentProvider();
		Object[] debugActionGroups= contentProvider.getElements(null);
		List enabled = new ArrayList(debugActionGroups.length);
		List disabled = new ArrayList(debugActionGroups.length);
		for (int i = 0; i < debugActionGroups.length; i++) {
			DebugActionGroup group = (DebugActionGroup)debugActionGroups[i];
			if (group.isVisible()) {
				enabled.add(group.getId());
			} else {
				disabled.add(group.getId());
			}
		}
		String prefEnabled = DebugActionGroupsManager.getDefault().serializeList(enabled);
		String prefDisabled = DebugActionGroupsManager.getDefault().serializeList(disabled);
		getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_ENABLED_DEBUG_ACTION_GROUPS, prefEnabled);
		getPreferenceStore().setValue(IDebugPreferenceConstants.PREF_DISABLED_DEBUG_ACTION_GROUPS, prefDisabled);
	}
}