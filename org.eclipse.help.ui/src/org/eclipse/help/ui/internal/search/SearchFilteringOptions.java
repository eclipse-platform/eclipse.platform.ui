package org.eclipse.help.ui.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.help.IToc;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.ui.IHelpUIConstants;
import org.eclipse.help.internal.ui.util.WorkbenchResources;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
/**
 * Displays Search Filtering Options Dialog.
 */
public class SearchFilteringOptions extends Dialog {
	private Table booksTable;
	private SearchQueryData queryData;
	private List allBooks;
	private List selectedBooks;
	/**
	 * Constructor
	 */
	public SearchFilteringOptions(Shell parentShell, SearchQueryData queryData) {
		super(parentShell);
		this.queryData = queryData;
		IToc tocs[] = HelpSystem.getTocManager().getTocs(queryData.getLocale());
		allBooks = new ArrayList(tocs.length);
		for (int i = 0; i < tocs.length; i++)
			allBooks.add(tocs[i]);
		queryData.getSelectedBooks();
		selectedBooks = new ArrayList(queryData.getSelectedBooks().size());
		selectedBooks.addAll(queryData.getSelectedBooks());
	}
	/**
	 * Fills in the dialog area with text and checkboxes
	 * @param the parent composite to contain the dialog area
	 * @return the dialog area control
	 */
	protected Control createDialogArea(Composite parent) {
		WorkbenchHelp.setHelp(parent, IHelpUIConstants.SEARCH_FILTERING_OPTIONS);
		Composite mainComposite = new Composite(parent, SWT.NULL);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		//data.grabExcessHorizontalSpace = true;
		mainComposite.setLayoutData(data);
		GridLayout layout = new GridLayout();
		mainComposite.setLayout(layout);
		Label description = new Label(mainComposite, SWT.NULL);
		description.setText(
			WorkbenchResources.getString("SearchFilteringOptions.description"));
		booksTable = new Table(mainComposite, SWT.CHECK | SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = convertHeightInCharsToPixels(19);
		gd.widthHint = convertWidthInCharsToPixels(48);
		booksTable.setLayoutData(gd);
		// populate table with books
		for (Iterator it = allBooks.iterator(); it.hasNext();) {
			TableItem item = new TableItem(booksTable, SWT.NONE);
			IToc toc = (IToc) it.next();
			item.setText(toc.getLabel());
			if (selectedBooks.contains(toc))
				item.setChecked(true);
			else
				item.setChecked(false);
		}
		return mainComposite;
	}
	/**
	 * Returns a list of books that are to be included in search
	 */
	public List getSelectedBooks() {
		return selectedBooks;
	}
	protected void okPressed() {
		selectedBooks = new ArrayList();
		for (int i = 0; i < allBooks.size(); i++) {
			if (booksTable.getItem(i).getChecked())
				selectedBooks.add(allBooks.get(i));
		}
		super.okPressed();
	}
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(WorkbenchResources.getString("SearchFilteringOptions.title"));
	}
}