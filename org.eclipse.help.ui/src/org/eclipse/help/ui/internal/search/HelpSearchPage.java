package org.eclipse.help.ui.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import org.eclipse.help.internal.ui.util.WorkbenchResources;
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
	private static java.util.List previousSearchOperations =
		new java.util.ArrayList(20);
	private Button advancedButton = null;
	//private Button headingsButton = null;
	//private SearchFilteringOptions filteringOptions = null;
	private ISearchPageContainer scontainer = null;
	private boolean firstTime = true;
	// Search query based on the data entered in the UI
	private SearchOperation searchOperation;
	/**
	 * Search Page
	 */
	public HelpSearchPage() {
		super();
		searchOperation = new SearchOperation();
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
					previousSearchOperations.size() - 1 - patternCombo.getSelectionIndex();
				searchOperation = (SearchOperation) previousSearchOperations.get(index);
				patternCombo.setText(searchOperation.getQueryData().getExpression());
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
		searchOperation.getQueryData().setExpression(patternCombo.getText());
		searchOperation.getQueryData().setFieldsSearch(false
		/*headingsButton.getSelection()*/
		);
		//java.util.List excluded = filteringOptions.getExcludedCategories();
		searchOperation.getQueryData().setCategoryFiltering(false
		/*excluded.size() > 0*/
		);
		searchOperation.getQueryData().setExcludedCategories(new ArrayList(0)
		/*excluded*/
		);
		if (!previousSearchOperations.contains(searchOperation))
			previousSearchOperations.add(searchOperation);
		IRunnableContext context = null;
		scontainer.getRunnableContext();
		Shell shell = patternCombo.getShell();
		if (context == null)
			context = new ProgressMonitorDialog(shell);
		try {
			context.run(true, true, searchOperation);
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
		if (visible && patternCombo != null) {
			if (firstTime) {
				firstTime = false;
				// Set item and text here to prevent page from resizing
				String[] patterns = new String[previousSearchOperations.size()];
				for (int i = 0; i < previousSearchOperations.size(); i++) {
					patterns[previousSearchOperations.size() - 1 - i] =
						((SearchOperation) previousSearchOperations.get(i))
							.getQueryData()
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