package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.update.core.*;
import org.eclipse.update.ui.internal.model.*;
import org.eclipse.swt.custom.BusyIndicator;
import java.net.URL;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.events.*;
import org.eclipse.ui.part.*;
import org.eclipse.jface.wizard.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.action.IStatusLineManager;
import java.util.Date;

public class UpdatesForm extends UpdateWebForm {
private static final String KEY_TITLE = "UpdatesPage.title";
private static final String KEY_LAST_SEARCH = "UpdatesPage.lastSearch";
private static final String KEY_NO_LAST_SEARCH = "UpdatesPage.noLastSearch";
private static final String KEY_SEARCH_NOW = "UpdatesPage.searchNow";
private static final String KEY_CANCEL = "UpdatesPage.cancel";
private static final String KEY_DESC = "UpdatesPage.desc";
private static final String KEY_OPTIONS = "UpdatesPage.options.label";
private static final String KEY_CDROM_CHECK = "UpdatesPage.options.cdromCheck";
private static final String KEY_FULL_MODE_CHECK = "UpdatesPage.options.fullModeCheck";

	private Label descLabel;
	private Label infoLabel;
	private ExpandableGroup optionsGroup;
	private Button cdromCheck;
	private Button fullModeCheck;
	private Button searchButton;
	private PageBook pagebook;
	private SearchMonitor monitor;
	private AvailableUpdates updates;
	private UpdateSearchProgressMonitor statusMonitor;
	private SearchResultSection searchResultSection;
	
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
		reflow();
		updateSize();
		enableOptions(true);
	}
}
	
public UpdatesForm(UpdateFormPage page) {
	super(page);
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	updates = model.getUpdates();
}

public void dispose() {
	updates.detachProgressMonitor(monitor);
	if (statusMonitor!=null)
	   updates.detachProgressMonitor(statusMonitor);
	super.dispose();
}

public void initialize(Object modelObject) {
	setHeadingText(UpdateUIPlugin.getResourceString(KEY_TITLE));
	setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
	setHeadingUnderlineImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_UNDERLINE));
	super.initialize(modelObject);
	//((Composite)getControl()).layout(true);
}

protected void createContents(Composite parent) {
	HTMLTableLayout layout = new HTMLTableLayout();
	parent.setLayout(layout);
	layout.leftMargin = layout.rightMargin = 10;
	layout.topMargin = 10;
	layout.horizontalSpacing = 0;
	layout.verticalSpacing = 10;
	layout.numColumns = 2;
	
	FormWidgetFactory factory = getFactory();
	
	descLabel = factory.createLabel(parent, null, SWT.WRAP);
	descLabel.setText(UpdateUIPlugin.getResourceString(KEY_DESC));
	TableData td = new TableData();
	td.colspan = 2;
	descLabel.setLayoutData(td);
	
	optionsGroup = new ExpandableGroup() {
		public void fillExpansion(Composite expansion, FormWidgetFactory factory) {
			GridLayout layout = new GridLayout();
			expansion.setLayout(layout);
			
			cdromCheck = factory.createButton(expansion, null, SWT.CHECK);
			cdromCheck.setText(UpdateUIPlugin.getResourceString(KEY_CDROM_CHECK));
			fullModeCheck = factory.createButton(expansion, null, SWT.CHECK);
			fullModeCheck.setText(UpdateUIPlugin.getResourceString(KEY_FULL_MODE_CHECK));
			fullModeCheck.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					toggleMode(fullModeCheck.getSelection());
				}
			});
		}
		protected SelectableFormLabel createTextLabel(Composite parent, FormWidgetFactory factory) {
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
	};
	optionsGroup.setText(UpdateUIPlugin.getResourceString(KEY_OPTIONS));
	optionsGroup.createControl(parent, factory);
	td = new TableData();
	td.colspan = 2;
	optionsGroup.getControl().setLayoutData(td);
	
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
	
	searchButton = factory.createButton(parent, 
		UpdateUIPlugin.getResourceString(KEY_SEARCH_NOW), SWT.PUSH);
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
	if (updates.isSearchInProgress()) {
		// sync up with the search
		catchUp();
	}
	searchResultSection = new SearchResultSection((UpdateFormPage)getPage());
	Control control = searchResultSection.createControl(parent, factory);
	td = new TableData();
	td.align = TableData.FILL;
	td.colspan = 2;
	td.grabHorizontal = true;
	control.setLayoutData(td);
	
	registerSection(searchResultSection);
}

private void reflow() {
	searchResultSection.reflow();
	descLabel.getParent().layout(true);
	((Composite)getControl()).layout(true);
}

private void toggleMode(final boolean fullMode) {
	BusyIndicator.showWhile(getControl().getDisplay(), new Runnable() {
		public void run() {
			searchResultSection.setFullMode(fullMode);
			descLabel.getParent().layout(true);
			((Composite)getControl()).layout(true);	
		}
	});
}

private void performSearch() {
	if (updates.isSearchInProgress()) {
		stopSearch();
	}
	else {
		if (!startSearch()) return;
	}
	updateButtonText();
}

private boolean startSearch() {
	try {
	   updates.attachProgressMonitor(monitor);
	   attachStatusLineMonitor();
	   updates.startSearch(getControl().getDisplay());
	   enableOptions(false);
	}
	catch (InvocationTargetException e) {
		UpdateUIPlugin.logException(e);
		return false;
	}
	catch (InterruptedException e) {
		UpdateUIPlugin.logException(e);
		return false;
	}
	return true;
}

private void catchUp() {
   updates.attachProgressMonitor(monitor);
   attachStatusLineMonitor();
   enableOptions(false);
   updateButtonText();
}

private void attachStatusLineMonitor() {
	if (statusMonitor!=null) return;
   	IViewSite vsite = getPage().getView().getViewSite();
   	IStatusLineManager manager = vsite.getActionBars().getStatusLineManager();
   	statusMonitor = new UpdateSearchProgressMonitor(manager);
   	updates.attachProgressMonitor(statusMonitor);	
}

private void stopSearch() {
	updates.stopSearch();
	updates.detachProgressMonitor(monitor);
	if (statusMonitor!=null)
	   updates.detachProgressMonitor(statusMonitor);
	enableOptions(true);
}

private void enableOptions(boolean enable) {
	cdromCheck.setEnabled(enable);
}

private void updateButtonText() {
	boolean inSearch = updates.isSearchInProgress();
	if (inSearch)
		searchButton.setText(UpdateUIPlugin.getResourceString(KEY_CANCEL));
	else
		searchButton.setText(UpdateUIPlugin.getResourceString(KEY_SEARCH_NOW));
	searchButton.getParent().layout(true);
}

public void expandTo(Object obj) {
}

}