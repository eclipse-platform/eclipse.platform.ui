package org.eclipse.help.ui.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.help.IAppServer;
import org.eclipse.help.internal.ui.WorkbenchHelpPlugin;
import org.eclipse.help.internal.ui.*;
import org.eclipse.help.internal.ui.util.WorkbenchResources;
import org.eclipse.help.internal.util.URLCoder;
import org.eclipse.help.ui.browser.IBrowser;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.search.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
/**
 * HelpSearchPage
 */
public class HelpSearchPage extends DialogPage implements ISearchPage {
	private static final int ENTRY_FIELD_LENGTH = 256;
	private static final int ENTRY_FIELD_ROW_COUNT = 1;
	private Combo patternCombo = null;
	private static java.util.List previousSearchQueryData =
		new java.util.ArrayList(20);
	private Button advancedButton = null;
	//private Button headingsButton = null;
	//private SearchFilteringOptions filteringOptions = null;
	private ISearchPageContainer scontainer = null;
	private boolean firstTime = true;
	// Search query based on the data entered in the UI
	private SearchQueryData searchQueryData;
	/**
	 * Search Page
	 */
	public HelpSearchPage() {
		super();
		searchQueryData = new SearchQueryData();
	}
	public void createControl(Composite parent) {
		Composite control = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		control.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		control.setLayoutData(gd);
		// Search Expression
		Group group = new Group(control, SWT.NONE);
		layout = new GridLayout();
		group.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		group.setLayoutData(gd);
		group.setText(WorkbenchResources.getString("expression"));
		// Pattern combo
		patternCombo = new Combo(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(30);
		patternCombo.setLayoutData(gd);
		// Not done here to prevent page from resizing
		// fPattern.setItems(getPreviousSearchPatterns());
		patternCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (patternCombo.getSelectionIndex() < 0)
					return;
				int index =
					previousSearchQueryData.size() - 1 - patternCombo.getSelectionIndex();
				searchQueryData = (SearchQueryData) previousSearchQueryData.get(index);
				patternCombo.setText(searchQueryData.getExpression());
				//headingsButton.setSelection(searchOperation.getQueryData().isFieldsSearch());
				//filteringOptions.setExcludedCategories(
				//searchOperation.getQueryData().getExcludedCategories());
			}
		});
		patternCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				scontainer.setPerformActionEnabled(patternCombo.getText().length() > 0);
			}
		});
		// Syntax description
		Label label = new Label(group, SWT.LEFT);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(gd);
		label.setText(WorkbenchResources.getString("expression_label"));
		// Headings only button
		//		headingsButton = new Button(control, SWT.CHECK);
		//		gd = new GridData();
		//		gd.horizontalAlignment = gd.BEGINNING;
		//		gd = new GridData();
		//		gd.verticalAlignment = gd.VERTICAL_ALIGN_BEGINNING;
		//		headingsButton.setLayoutData(gd);
		//		headingsButton.setText(WorkbenchResources.getString("Search_headers_only"));
		// Filtering group
		//		Group filteringGroup = new Group(control, SWT.NONE);
		//		layout = new GridLayout();
		//		filteringGroup.setLayout(layout);
		//		gd = new GridData(GridData.FILL_BOTH);
		//		gd.heightHint = 100;
		//		filteringGroup.setLayoutData(gd);
		//		filteringOptions =
		//			new SearchFilteringOptions(filteringGroup, searchOperation.getQueryData());
		//		filteringGroup.setText(WorkbenchResources.getString("limit_to"));
		setControl(control);
		WorkbenchHelp.setHelp(control, new String[] { SearchUIConstants.SEARCH_PAGE });
	}
	/**
	 * @see ISearchPage#performAction()
	 */
	public boolean performAction() {
		searchQueryData.setExpression(patternCombo.getText());
		searchQueryData.setFieldsSearch(false
		/*headingsButton.getSelection()*/
		);
		//java.util.List excluded = filteringOptions.getExcludedCategories();
		searchQueryData.setCategoryFiltering(false
		/*excluded.size() > 0*/
		);
		searchQueryData.setExcludedCategories(new ArrayList(0)
		/*excluded*/
		);
		if (!previousSearchQueryData.contains(searchQueryData))
			previousSearchQueryData.add(searchQueryData);
		IRunnableContext context = null;
		scontainer.getRunnableContext();
		Shell shell = patternCombo.getShell();

				try {
					//Help.displayHelp(topicsURL);
					IAppServer appServer = WorkbenchHelpPlugin.getDefault().getAppServer();
					if (appServer == null)
						return true; // may want to display an error message
						
					String url = 
						"http://"
							+ appServer.getHost()
							+ ":"
							+ appServer.getPort()
							+ "/help?tab=search&query="+URLCoder.encode(searchQueryData.getExpression());
					IBrowser browser=WorkbenchHelpPlugin.getDefault().getHelpBrowser();
					browser.displayURL(url);
				} catch (Exception e) {
				}


		return true;
	}
	public void setContainer(ISearchPageContainer container) {
		scontainer = container;
	}
	/*
	 * Implements method from IDialogPage
	 */
	public void setVisible(boolean visible) {
		if (visible && patternCombo != null) {
			if (firstTime) {
				firstTime = false;
				// Set item and text here to prevent page from resizing
				String[] patterns = new String[previousSearchQueryData.size()];
				for (int i = 0; i < previousSearchQueryData.size(); i++) {
					patterns[previousSearchQueryData.size() - 1 - i] =
						((SearchQueryData) previousSearchQueryData.get(i))
							.getExpression();
				}
				patternCombo.setItems(patterns);
				//initializePatternControl();
			}
			patternCombo.setFocus();
			scontainer.setPerformActionEnabled(patternCombo.getText().length() > 0);
		}
		super.setVisible(visible);
	}
}