package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.update.internal.ui.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.search.*;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.update.ui.forms.internal.engine.FormEngine;

public class SearchForm extends UpdateWebForm {
	private static final String KEY_TITLE = "SearchPage.title";
	private static final String KEY_LAST_SEARCH = "SearchPage.lastSearch";
	private static final String KEY_NO_LAST_SEARCH = "SearchPage.noLastSearch";
	private static final String KEY_SEARCH_NOW = "SearchPage.searchNow";
	private static final String KEY_CANCEL = "SearchPage.cancel";
	private static final String KEY_DESC = "SearchPage.desc";
	private static final String KEY_QUERY = "SearchPage.query.label";
	private static final String KEY_CATEGORY = "SearchPage.query.category";
	private static final String KEY_OPTIONS = "SearchPage.options.label";
	private static final String KEY_MY_COMPUTER_CHECK =
		"SearchPage.options.myComputerCheck";
	private static final String KEY_MY_COMPUTER_TITLE =
		"SearchPage.options.myComputerSettings.title";
	private static final String KEY_MY_COMPUTER_MORE =
		"SearchPage.options.myComputerSettings";
	private static final String KEY_FULL_MODE_CHECK =
		"SearchPage.options.fullModeCheck";
	private static final String UPDATES_IMAGE_ID = "updates";
	private static final String SETTINGS_SECTION = "SearchForm";
	private static final String S_FULL_MODE = "fullMode";

	private FormEngine descLabel;
	private Label infoLabel;
	private ExpandableGroup queryGroup;
	private ExpandableGroup optionsGroup;
	private CCombo categoryCombo;
	private Button myComputerCheck;
	private Button myComputerSettings;
	private Button fullModeCheck;
	private Button searchButton;
	private PageBook pagebook;
	private SearchMonitor monitor;
	//private UpdateSearchProgressMonitor statusMonitor;
	private SearchResultSection searchResultSection;
	private IDialogSettings settings;
	private SearchObject searchObject;
	private ArrayList categories = new ArrayList();
	private Hashtable descTable = new Hashtable();
	private ISearchCategory currentCategory;

	abstract class OptionsGroup extends ExpandableGroup {
		protected SelectableFormLabel createTextLabel(
			Composite parent,
			FormWidgetFactory factory) {
			SelectableFormLabel label = super.createTextLabel(parent, factory);
			label.setFont(JFaceResources.getBannerFont());
			return label;
		}
		public void expanded() {
			reflow();
			updateSize();
		}
		public void collapsed() {
			reflow();
			updateSize();
		}
	}

	class SearchMonitor extends ProgressMonitorPart {
		public SearchMonitor(Composite parent) {
			super(parent, null);
			setBackground(factory.getBackgroundColor());
			fLabel.setBackground(factory.getBackgroundColor());
			fProgressIndicator.setBackground(factory.getBackgroundColor());
		}
		public void done() {
			super.done();
			updateButtonText();
			Date date = new Date();
			String pattern = UpdateUIPlugin.getResourceString(KEY_LAST_SEARCH);
			String text = UpdateUIPlugin.getFormattedMessage(pattern, date.toString());
			infoLabel.setText(text);
			reflow(true);
			searchObject.detachProgressMonitor(this);
//			if (statusMonitor != null) {
//				searchObject.detachProgressMonitor(statusMonitor);
//				statusMonitor = null;
//			}
			enableOptions(true);
		}
	}

	public SearchForm(UpdateFormPage page) {
		super(page);
		UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
		IDialogSettings master = UpdateUIPlugin.getDefault().getDialogSettings();
		settings = master.getSection(SETTINGS_SECTION);
		if (settings == null)
			settings = master.addNewSection(SETTINGS_SECTION);
	}

	public void dispose() {
		if (searchObject != null) {
			detachFrom(searchObject);
		}
		if (searchResultSection != null)
			searchResultSection.dispose();
		super.dispose();
	}

	private void detachFrom(SearchObject obj) {
		obj.detachProgressMonitor(monitor);
//		if (statusMonitor != null)
//			obj.detachProgressMonitor(statusMonitor);
	}

	public void initialize(Object modelObject) {
		updateHeadingText(null);
		setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
		setHeadingUnderlineImage(
			UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_UNDERLINE));
		super.initialize(modelObject);
	}
	
	private void updateHeadingText(SearchObject obj) {
		String title = UpdateUIPlugin.getResourceString(KEY_TITLE);
		if (obj!=null)
			title += " - "+obj.getName();
		setHeadingText(title);
	}

	protected void createContents(Composite parent) {
		HTMLTableLayout layout = new HTMLTableLayout();
		parent.setLayout(layout);
		layout.leftMargin = 10;
		layout.rightMargin = 1;
		layout.topMargin = 10;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 10;
		layout.numColumns = 2;

		FormWidgetFactory factory = getFactory();
		TableData td;

		descLabel = factory.createFormEngine(parent);
		descLabel.load(UpdateUIPlugin.getResourceString(KEY_DESC), true, true);
		td = new TableData();
		td.colspan = 2;
		descLabel.setLayoutData(td);

		queryGroup = new OptionsGroup() {
			public void fillExpansion(Composite expansion, FormWidgetFactory factory) {
				fillQueryGroup(expansion, factory);
			}
		};

		Composite optionContainer = factory.createComposite(parent);
		layout = new HTMLTableLayout();
		layout.numColumns = 2;
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		layout.horizontalSpacing = 15;
		optionContainer.setLayout(layout);
		td = new TableData();
		td.colspan = 2;
		optionContainer.setLayoutData(td);

		queryGroup.setText(UpdateUIPlugin.getResourceString(KEY_QUERY));
		queryGroup.createControl(optionContainer, factory);
		//td = new TableData();
		//td.colspan = 2;
		//td.align = TableData.FILL;
		//queryGroup.getControl().setLayoutData(td);

		optionsGroup = new OptionsGroup() {
			public void fillExpansion(Composite expansion, FormWidgetFactory factory) {
				GridLayout layout = new GridLayout();
				layout.numColumns = 2;
				expansion.setLayout(layout);

				myComputerCheck = factory.createButton(expansion, null, SWT.CHECK);
				myComputerCheck.setText(
					UpdateUIPlugin.getResourceString(KEY_MY_COMPUTER_CHECK));
				myComputerCheck.setSelection(searchObject.getSearchMyComputer());
				myComputerCheck.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						myComputerSettings.setEnabled(myComputerCheck.getSelection());
						searchObject.setSearchMyComputer(myComputerCheck.getSelection());
					}
				});

				myComputerSettings = factory.createButton(expansion, null, SWT.PUSH);
				myComputerSettings.setText(
					UpdateUIPlugin.getResourceString(KEY_MY_COMPUTER_MORE));
				myComputerSettings.setEnabled(myComputerCheck.getSelection());
				myComputerSettings.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						MyComputerSearchDialog sd =
							new MyComputerSearchDialog(myComputerSettings.getShell());
						sd.create();
						sd.getShell().setText(UpdateUIPlugin.getResourceString(KEY_MY_COMPUTER_TITLE));
						sd.open();
					}
				});

				fullModeCheck = factory.createButton(expansion, null, SWT.CHECK);
				fullModeCheck.setText(UpdateUIPlugin.getResourceString(KEY_FULL_MODE_CHECK));
				fullModeCheck.setSelection(settings.getBoolean(S_FULL_MODE));
				fullModeCheck.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						toggleMode(fullModeCheck.getSelection());
					}
				});
				GridData gd = new GridData();
				gd.horizontalSpan = 2;
				fullModeCheck.setLayoutData(gd);
			}
		};
		optionsGroup.setText(UpdateUIPlugin.getResourceString(KEY_OPTIONS));
		optionsGroup.createControl(optionContainer, factory);
		//td = new TableData();
		//td.colspan = 2;
		//td.align = TableData.FILL;
		//optionsGroup.getControl().setLayoutData(td);

		Composite sep = factory.createCompositeSeparator(parent);
		td = new TableData();
		td.align = TableData.FILL;
		td.heightHint = 1;
		td.colspan = 2;
		sep.setBackground(factory.getColor(factory.COLOR_BORDER));
		sep.setLayoutData(td);

		infoLabel = factory.createLabel(parent, null);
		infoLabel.setText(UpdateUIPlugin.getResourceString(KEY_NO_LAST_SEARCH));
		td = new TableData(TableData.LEFT, TableData.MIDDLE);
		infoLabel.setLayoutData(td);

		searchButton =
			factory.createButton(
				parent,
				UpdateUIPlugin.getResourceString(KEY_SEARCH_NOW),
				SWT.PUSH);
		searchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				performSearch();
			}
		});
		td = new TableData(TableData.LEFT, TableData.MIDDLE);
		td.indent = 10;
		searchButton.setLayoutData(td);

		monitor = new SearchMonitor(parent);
		td = new TableData();
		td.align = TableData.FILL;
		td.colspan = 2;
		monitor.setLayoutData(td);
		if (searchObject != null && searchObject.isSearchInProgress()) {
			// sync up with the search
			catchUp();
		}
		searchResultSection = new SearchResultSection((UpdateFormPage) getPage());
		Control control = searchResultSection.createControl(parent, factory);
		td = new TableData();
		td.align = TableData.FILL;
		td.colspan = 2;
		td.grabHorizontal = true;
		control.setLayoutData(td);
		searchResultSection.setFullMode(settings.getBoolean(S_FULL_MODE));
	}

	private void fillQueryGroup(Composite container, FormWidgetFactory factory) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		container.setLayout(layout);
		factory.createLabel(container, UpdateUIPlugin.getResourceString(KEY_CATEGORY));
		categoryCombo = new CCombo(container, SWT.READ_ONLY | SWT.FLAT);
		categoryCombo.setBackground(factory.getBackgroundColor());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		categoryCombo.setLayoutData(gd);

		pagebook = new PageBook(container, SWT.NULL);
		pagebook.setBackground(factory.getBackgroundColor());
		gd = new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		pagebook.setLayoutData(gd);
		SearchCategoryDescriptor[] descriptors =
			SearchCategoryRegistryReader.getDefault().getCategoryDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			ISearchCategory category = descriptors[i].createCategory();
			if (category != null) {
				categories.add(category);
				descTable.put(category, descriptors[i]);
				categoryCombo.add(descriptors[i].getName());
				category.createControl(pagebook, factory);
			}
		}
		categoryCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = categoryCombo.getSelectionIndex();
				ISearchCategory category = (ISearchCategory) categories.get(index);
				switchTo(category);
				searchObject.setCategoryId(category.getId());
			}
		});
		categoryCombo.select(0);
		factory.paintBordersFor(container);
	}

	private void switchTo(ISearchCategory category) {
		pagebook.showPage(category.getControl());
		currentCategory = category;
		SearchCategoryDescriptor desc = (SearchCategoryDescriptor)descTable.get(category);
		descLabel.load(desc.getDescription(), true, true);
		reflow();
		updateSize();
	}

	private void selectCategory(SearchObject obj) {
		for (int i = 0; i < categories.size(); i++) {
			ISearchCategory category = (ISearchCategory) categories.get(i);
			if (category.getId().equals(obj.getCategoryId())) {
				categoryCombo.select(i);
				switchTo(category);
				category.load(obj.getSettings());
				searchResultSection.setSearchString(category.getCurrentSearch());
				categoryCombo.setEnabled(!obj.isCategoryFixed());
				break;
			}
		}
	}

	private void reflow(boolean searchFinished) {
		if (searchFinished)
			searchResultSection.searchFinished();
		else
			searchResultSection.reflow();
		descLabel.getParent().layout(true);
		((Composite) getControl()).layout(true);
		updateSize();
	}

	private void reflow() {
		reflow(false);
	}

	private void toggleMode(final boolean fullMode) {
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				searchResultSection.setFullMode(fullMode);
				descLabel.getParent().layout(true);
				((Composite) getControl()).layout(true);
				updateSize();
			}
		});
		settings.put(S_FULL_MODE, fullMode);
	}

	private void performSearch() {
		if (searchObject != null) {
			if (searchObject.isSearchInProgress()) {
				stopSearch();
			} else {
				if (!startSearch())
					return;
			}
			updateButtonText();
		}
	}

	private boolean startSearch() {
		try {
			searchObject.attachProgressMonitor(monitor);
			//attachStatusLineMonitor();
			enableOptions(false);
			searchObject.startSearch(getControl().getDisplay(), getQueries());
			//searchResultSection.setSearchObject(searchObject);
			searchResultSection.searchStarted();
		} catch (InvocationTargetException e) {
			UpdateUIPlugin.logException(e);
			return false;
		} catch (InterruptedException e) {
			UpdateUIPlugin.logException(e);
			return false;
		}
		return true;
	}

	private ISearchQuery[] getQueries() {
		int index = categoryCombo.getSelectionIndex();
		ISearchCategory category = (ISearchCategory) categories.get(index);
		searchResultSection.setSearchString(category.getCurrentSearch());
		return category.getQueries();
	}

	private void catchUp() {
		searchObject.attachProgressMonitor(monitor);
		//attachStatusLineMonitor();
		enableOptions(false);
		updateButtonText();
	}

//	private void attachStatusLineMonitor() {
//		if (statusMonitor != null)
//			return;
//		IViewSite vsite = getPage().getView().getViewSite();
//		IStatusLineManager manager = vsite.getActionBars().getStatusLineManager();
//		manager = getRootManager(manager);
//
//		statusMonitor = new UpdateSearchProgressMonitor(manager);
//		searchObject.attachProgressMonitor(statusMonitor);
//	}

	private IStatusLineManager getRootManager(IStatusLineManager manager) {
		IContributionManager parent = manager;

		while (parent instanceof SubStatusLineManager) {
			IContributionManager newParent = ((SubStatusLineManager) parent).getParent();
			if (newParent == null)
				break;
			parent = newParent;
		}
		return (IStatusLineManager) parent;
	}

	private void stopSearch() {
		searchObject.stopSearch();
	}

	private void enableOptions(boolean enable) {
		myComputerCheck.setEnabled(enable);
	}

	private void updateButtonText() {
		boolean inSearch = searchObject != null && searchObject.isSearchInProgress();
		if (inSearch)
			searchButton.setText(UpdateUIPlugin.getResourceString(KEY_CANCEL));
		else
			searchButton.setText(UpdateUIPlugin.getResourceString(KEY_SEARCH_NOW));
		searchButton.getParent().layout(true);
	}

	public void expandTo(Object obj) {
		if (obj instanceof SearchObject) {
			inputChanged((SearchObject) obj);
		}
	}
	private void inputChanged(final SearchObject obj) {
		BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
			public void run() {
				if (searchObject != null) {
					if (searchObject == obj)
						return;
					if (currentCategory != null) {
						currentCategory.store(searchObject.getSettings());
						searchObject.setCategoryId(currentCategory.getId());
					}
					detachFrom(searchObject);
				}
				searchObject = obj;
				updateHeadingText(searchObject);
				selectCategory(obj);
				searchResultSection.setSearchObject(searchObject);
				if (searchObject.isSearchInProgress()) {
					// sync up with the search
					catchUp();
				}
			}
		});
	}
}