package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.*;
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

public class UpdatesForm extends UpdateWebForm {
	private Label descLabel;
	private Label infoLabel;
	private ExpandableGroup optionsGroup;
	private Button cdromCheck;
	private Button searchButton;
	private PageBook pagebook;
	private SearchMonitor monitor;
	private AvailableUpdates updates;
	
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
		enableOptions(true);
	}
}
	
public UpdatesForm(UpdateFormPage page) {
	super(page);
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	updates = model.getUpdates();
}

public void dispose() {
	super.dispose();
}

public void initialize(Object modelObject) {
	setHeadingText("Available Updates");
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
	descLabel.setText("Search for updates to the features you have previously installed. If needed, modify the search options."+
	"If a search has already been performed during this session, you can browse the result by expanding the object.");
	TableData td = new TableData();
	td.colspan = 2;
	descLabel.setLayoutData(td);
	
	optionsGroup = new ExpandableGroup() {
		public void fillExpansion(Composite expansion, FormWidgetFactory factory) {
			GridLayout layout = new GridLayout();
			expansion.setLayout(layout);
			
			cdromCheck = factory.createButton(expansion, null, SWT.CHECK);
			cdromCheck.setText("Include CD-ROM in the search");
		}
		protected Label createTextLabel(Composite parent, FormWidgetFactory factory) {
			Label label = super.createTextLabel(parent, factory);
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
	optionsGroup.setText("Search Options");
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
	infoLabel.setText("Last search: n/a");
	td = new TableData(TableData.LEFT, TableData.MIDDLE);
	infoLabel.setLayoutData(td);
	
	searchButton = factory.createButton(parent, "&Search Now", SWT.PUSH);
	searchButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			performSearch();
		}
	});
	td = new TableData(TableData.LEFT, TableData.MIDDLE);
	searchButton.setLayoutData(td);
	
	monitor = new SearchMonitor(parent);
	td = new TableData();
	td.align = TableData.FILL;
	td.colspan = 2;
	monitor.setLayoutData(td);
}

private void reflow() {
	descLabel.getParent().layout(true);
	((Composite)getControl()).layout(true);
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

private void stopSearch() {
	updates.stopSearch();
	updates.detachProgressMonitor(monitor);
	enableOptions(true);
}

private void enableOptions(boolean enable) {
	cdromCheck.setEnabled(enable);
}

private void updateButtonText() {
	boolean inSearch = updates.isSearchInProgress();
	if (inSearch)
		searchButton.setText("&Cancel");
	else
		searchButton.setText("&Search Now");
}

public void expandTo(Object obj) {
}

}