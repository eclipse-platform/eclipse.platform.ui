/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.ui.internal.search;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.eclipse.help.IToc;
import org.eclipse.help.internal.ui.IHelpUIConstants;
import org.eclipse.help.internal.ui.util.*;
import org.eclipse.jface.dialogs.DialogPage;
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
	private Combo searchWordCombo = null;
	private static java.util.List previousSearchQueryData =
		new java.util.ArrayList(20);
	private Button all;
	private Button selected;
	private Text selectedBooksText;
	//private Button headingsButton = null;
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
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		control.setLayoutData(gd);
		// Search Expression
		Label expressionLabel= new Label(control, SWT.LEFT);
		expressionLabel.setText(WorkbenchResources.getString("expression"));
		// Pattern combo
		searchWordCombo = new Combo(control, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = convertWidthInCharsToPixels(30);
		searchWordCombo.setLayoutData(gd);
		// Not done here to prevent page from resizing
		// fPattern.setItems(getPreviousSearchPatterns());
		searchWordCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (searchWordCombo.getSelectionIndex() < 0)
					return;
				int index =
					previousSearchQueryData.size() - 1 - searchWordCombo.getSelectionIndex();
				searchQueryData = (SearchQueryData) previousSearchQueryData.get(index);
				searchWordCombo.setText(searchQueryData.getSearchWord());
				all.setSelection(!searchQueryData.isBookFiltering());
				selected.setSelection(searchQueryData.isBookFiltering());
				displaySelectedBooks();
				//headingsButton.setSelection(searchOperation.getQueryData().isFieldsSearch());
			}
		});
		searchWordCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				scontainer.setPerformActionEnabled(searchWordCombo.getText().length() > 0);
			}
		});
		// Space
		Label space = new Label(control, SWT.NONE);
		// Syntax description
		Label label = new Label(control, SWT.LEFT);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
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
		Group filteringGroup = new Group(control, SWT.NONE);
		filteringGroup.setLayout(layout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		filteringGroup.setLayoutData(gd);
		filteringGroup.setText(WorkbenchResources.getString("limit_to"));
		layout = new GridLayout();
		layout.numColumns = 3;
		filteringGroup.setLayout(layout);
		//
		all = new Button(filteringGroup, SWT.RADIO);
		all.setSelection(!searchQueryData.isBookFiltering());
		all.setText(WorkbenchResources.getString("HelpSearchPage.allBooks"));
		gd = new GridData();
		gd.horizontalSpan = 3;
		all.setLayoutData(gd);
		all.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				searchQueryData.setBookFiltering(false);
			}
		});
		//
		selected = new Button(filteringGroup, SWT.RADIO);
		selected.setSelection(searchQueryData.isBookFiltering());
		selected.setText(WorkbenchResources.getString("HelpSearchPage.selectedBooks"));
		selected.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				searchQueryData.setBookFiltering(true);
			}
		});
		//
		selectedBooksText =
			new Text(filteringGroup, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		displaySelectedBooks();
		//
		Button chooseWorkingSet = new Button(filteringGroup, SWT.PUSH);
		chooseWorkingSet.setLayoutData(new GridData());
		chooseWorkingSet.setText(WorkbenchResources.getString("HelpSearchPage.choose"));
		SWTUtil.setButtonDimensionHint(chooseWorkingSet);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 8;
		gd.widthHint = SWTUtil.convertWidthInCharsToPixels(30, selectedBooksText);
		selectedBooksText.setLayoutData(gd);
		chooseWorkingSet.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SearchFilteringOptions dialog =
					new SearchFilteringOptions(selected.getShell(), searchQueryData);
				if (dialog.open() == dialog.OK) {
					all.setSelection(false);
					selected.setSelection(true);
					searchQueryData.setBookFiltering(true);
					searchQueryData.setSelecteBooks(dialog.getSelectedBooks());
					displaySelectedBooks();
				}
			}
		});
		setControl(control);
		WorkbenchHelp.setHelp(control, IHelpUIConstants.SEARCH_PAGE);
	}
	/**
	 * @see ISearchPage#performAction()
	 */
	public boolean performAction() {
		searchQueryData.setSearchWord(searchWordCombo.getText());
		searchQueryData.setFieldsSearch(false /*headingsButton.getSelection()*/
		);
		if (!previousSearchQueryData.contains(searchQueryData))
			previousSearchQueryData.add(searchQueryData);
		try {
			SearchUI.activateSearchResultView();
			scontainer.getRunnableContext().run(
				true,
				true,
				new SearchOperation(searchQueryData));
		} catch (InvocationTargetException ex) {
			return false;
		} catch (InterruptedException e) {
			return false;
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
		if (visible && searchWordCombo != null) {
			if (firstTime) {
				firstTime = false;
				// Set item and text here to prevent page from resizing
				String[] patterns = new String[previousSearchQueryData.size()];
				for (int i = 0; i < previousSearchQueryData.size(); i++) {
					patterns[previousSearchQueryData.size() - 1 - i] =
						((SearchQueryData) previousSearchQueryData.get(i)).getSearchWord();
				}
				searchWordCombo.setItems(patterns);
			}
			searchWordCombo.setFocus();
			scontainer.setPerformActionEnabled(searchWordCombo.getText().length() > 0);
		}
		super.setVisible(visible);
	}
	private void displaySelectedBooks() {
		String tocLabels = "";
		for (Iterator booksIt = searchQueryData.getSelectedBooks().iterator();
			booksIt.hasNext();
			) {
			String bookLabel = ((IToc) booksIt.next()).getLabel();
			if (tocLabels.length() <= 0)
				tocLabels = bookLabel;
			else
				tocLabels += WorkbenchResources.getString("HelpSearchPage.bookLabelSeparator")
					+ bookLabel;
		}
		selectedBooksText.setText(tocLabels);
	}
}