package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;

public class MainForm extends UpdateForm {
	
	private PopularSitesSection bookmarkSection;
	
class EmptySection extends FormSection {
	public EmptySection() {
		setAddSeparator(false);
		setTitleAsHyperlink(true);
	}
	public Composite createClient(Composite parent, FormWidgetFactory factory) {
		return null;
	}
}

	public MainForm(UpdateFormPage page) {
		super(page);
		setVerticalFit(true);
	}
	
public void initialize(Object modelObject) {
	setTitle("Welcome to Eclipse Update");
	setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
	super.initialize(modelObject);
	getControl().layout(true);
}

public void createFormClient(Composite parent) {
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	layout.marginWidth = 10;
	//layout.makeColumnsEqualWidth = true;
	layout.horizontalSpacing = 15;
	parent.setLayout(layout);	
	
	FormWidgetFactory factory = getFactory();
	bookmarkSection = new PopularSitesSection(getPage());
	Control child = bookmarkSection.createControl(parent, factory);
	GridData gd = new GridData(GridData.FILL_VERTICAL);
	gd.widthHint = 200;
	child.setLayoutData(gd);
	
	Composite column = factory.createComposite(parent);
	gd = new GridData(GridData.FILL_BOTH);
	column.setLayoutData(gd);
	layout = new GridLayout();
	column.setLayout(layout);
	
	EmptySection section = new EmptySection() {
		public void titleActivated() {
			goToPage(UpdateManager.UPDATE_PAGE);
		}
	};
	section.setHeaderText("Feature Updates");
	section.setDescription("Go here to download the latest updates for the currently installed features.");
	child = section.createControl(column, factory);
	//gd = new GridData(GridData.FILL_HORIZONTAL |GridData.VERTICAL_ALIGN_BEGINNING);
	gd = new GridData(GridData.FILL_BOTH);
	child.setLayoutData(gd);
	
	section = new EmptySection() {
		public void titleActivated() {
			goToPage(UpdateManager.INSTALL_PAGE);
		}
	};
	section.setHeaderText("New Feature Installs");
	section.setDescription("Browse Eclipse sites and discover new features to add to your workbench.");
	child = section.createControl(column, factory);
	//gd = new GridData(GridData.FILL_HORIZONTAL |GridData.VERTICAL_ALIGN_BEGINNING);
	gd = new GridData(GridData.FILL_BOTH);
	child.setLayoutData(gd);
	
	section = new EmptySection() {
		public void titleActivated() {
			goToPage(UpdateManager.HISTORY_PAGE);
		}
	};
	section.setHeaderText("Installation History");
	section.setDescription("Review your installation history or choose to revert to one of the previous stable states.");
	child = section.createControl(column, factory);
	gd = new GridData(GridData.FILL_BOTH);
	child.setLayoutData(gd);
}
private void goToPage(String pageId) {
	getPage().getEditor().showPage(pageId);
}
}