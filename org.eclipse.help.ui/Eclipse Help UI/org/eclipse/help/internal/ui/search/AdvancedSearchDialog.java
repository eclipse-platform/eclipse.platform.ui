package org.eclipse.help.internal.ui.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import java.util.*;
import java.util.List;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.events.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.contributions.*;
import org.eclipse.help.internal.contributions.xml.*;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.help.internal.ui.IHelpUIConstants;

/**
 * SearchPage
 */
public class AdvancedSearchDialog extends Dialog {
	private Composite control;
	private Button withinCheckBox = null;
	private Button fieldsSearchCheckBox = null;
	private Button categoryFilteringCheckBox = null;
	private SearchFilteringOptions options;
	// the underlying model for the UI; shared with the page
	private HelpSearchQuery query;
	/**
	 * Search Page
	 * @parameter workbook workbook that this page is part of
	 */
	public AdvancedSearchDialog(Shell parent, HelpSearchQuery query) {
		super(parent);
		this.query = query;
	}
	private Button createCheckBox(Composite parent, String label ) {
		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(label);
		checkBox.setAlignment(SWT.LEFT);
		GridData gd = new GridData();
		gd.horizontalAlignment = gd.FILL;
		gd.horizontalSpan = 2; // on a row by itself
		checkBox.setLayoutData(gd);
		return checkBox;
	}
	protected Control createDialogArea(Composite parent) {

		control = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		//layout.verticalSpacing = 5;
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		control.setLayout(layout);
		control.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create the "search headers only" check box
		fieldsSearchCheckBox = createCheckBox(control, WorkbenchResources.getString("Search_headers_only"));
		fieldsSearchCheckBox.setSelection(query.isFieldsSearch());
		
		// Create the "search within results" check box
		withinCheckBox = createCheckBox(control, WorkbenchResources.getString("Search_within_results"));
		withinCheckBox.setSelection(query.isSearchWithinLastResults());
		
		// Create the "enable category filtering"
		categoryFilteringCheckBox = createCheckBox(control, WorkbenchResources.getString("Enable_Filtering"));
		categoryFilteringCheckBox.setSelection(query.isCategoryFiltering());

		SelectionListener listener2 = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.widget).getSelection()) {
					//advancedButton.setText(WorkbenchResources.getString("Disable_Filtering"));
					setOptionsVisible(true);
				} else {
					//advancedButton.setText(WorkbenchResources.getString("Enable_Filtering"));
					setOptionsVisible(false);
				}
			}
		};
		categoryFilteringCheckBox.addSelectionListener(listener2);
		options = new SearchFilteringOptions(control, query);
		setOptionsVisible(query.isCategoryFiltering());
		
		/*WorkbenchHelp.setHelp(
			control,
			new String[] {
				IHelpUIConstants.SEARCH_PAGE,
				IHelpUIConstants.NAVIGATION_VIEWER,
				IHelpUIConstants.EMBEDDED_HELP_VIEW});*/
		WorkbenchHelp.setHelp(
			control.getShell(),
			new String[] {
				IHelpUIConstants.ADVANCED_SEARCH});

		return control;
	}
	public Control getControl() {
		return control;
	}
   /**
 	* Notifies that the ok button of this dialog has been pressed.
 	*/
	protected void okPressed() {
		// set the UI values on the query
		query.setFieldsSearch(fieldsSearchCheckBox.getSelection());
		query.setSearchWithinLastResults(withinCheckBox.getSelection());
		query.setCategoryFiltering(categoryFilteringCheckBox.getSelection());
		query.setExcludedCategories(options.getExcludedCategories());
		
		super.okPressed();
	}
	protected void setOptionsVisible(boolean enabled) {
		// enable or disable the control
		options.setEnabled(enabled);
	}
}
