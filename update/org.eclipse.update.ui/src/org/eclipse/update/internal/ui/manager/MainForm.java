package org.eclipse.update.internal.ui.manager;

import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;

public class MainForm extends UpdateWebForm {
	
public MainForm(UpdateFormPage page) {
	super(page);
}

public void initialize(Object modelObject) {
	setHeadingText("Welcome to Eclipse Update");
	setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
	super.initialize(modelObject);
	//((Composite)getControl()).layout(true);
}

protected int getNumColumns() {
	return 1;
}

protected void createClient(Composite parent) {
	//Composite container = factory.createComposite(parent);
	Composite container = new Composite(parent, SWT.NULL);
	HTMLTableLayout layout = new HTMLTableLayout();
	container.setLayout(layout);
	Label label = factory.createLabel(container, null, SWT.WRAP);
	label.setText("Some random text to be first in the form");
}

/*

public void createFormClient(Composite parent) {
	GridLayout layout = new GridLayout();
	//layout.numColumns = 2;
	layout.marginWidth = 10;
	//layout.makeColumnsEqualWidth = true;
	layout.horizontalSpacing = 15;
	parent.setLayout(layout);	
	
	GridData gd;
	Control child;
	
	FormWidgetFactory factory = getFactory();
	EmptySection section = new EmptySection() {
		public void titleActivated() {
			showView(UpdatePerspective.ID_LOCAL_SITE);
		}
	};
	section.setHeaderText("Feature Updates");
	section.setDescription(
	"To check for updates for features you have installed, "+
	"go to the \"My Eclipse\" view and expand \"Available Updates\" folder. "+
	"You can drag new updates into the Checklist folder.");
	child = section.createControl(parent, factory);
	//gd = new GridData(GridData.FILL_HORIZONTAL |GridData.VERTICAL_ALIGN_BEGINNING);
	//gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
	gd = new GridData(GridData.FILL_BOTH);
	child.setLayoutData(gd);
	
	section = new EmptySection() {
		public void titleActivated() {
			showView(UpdatePerspective.ID_SITES);
		}
	};
	section.setHeaderText("New Feature Installs");
	section.setDescription(
	"To install new features, open \"Update Sites\" view and expand"+
	"the desired site (this operation may take time). Select features and "+
	"read about them in the \"Details\" view. If you want to install a feature, "+
	"just drag it into the \"Checklist\" view.");
	child = section.createControl(parent, factory);
	//gd = new GridData(GridData.FILL_HORIZONTAL |GridData.VERTICAL_ALIGN_BEGINNING);
	gd = new GridData(GridData.FILL_BOTH);
	//gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
	child.setLayoutData(gd);
	
	section = new EmptySection() {
		public void titleActivated() {
			showView(UpdatePerspective.ID_LOCAL_SITE);
		}
	};
	section.setHeaderText("Uninstalling Features");
	section.setDescription("To uninstall a feature, open \"My Eclipse\" view, "+
	"expand \"Installed Features\" folder, select the desired feature and drag it "+
	"into the \"Checklist\" folder.");
	child = section.createControl(parent, factory);
	gd = new GridData(GridData.FILL_BOTH);
	//gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
	child.setLayoutData(gd);
	
	section = new EmptySection() {
		public void titleActivated() {
			showView(UpdatePerspective.ID_HISTORY);
		}
	};
	section.setHeaderText("Installation History");
	section.setDescription("You can review your installation history or revert to one of the previous stable states."+
	" To do so, open \"Update History\" view.");
	child = section.createControl(parent, factory);
	gd = new GridData(GridData.FILL_BOTH);
	//gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
	child.setLayoutData(gd);
}
private void showView(String viewId) {
	try {
		IViewPart part = UpdateUIPlugin.getActivePage().showView(viewId);
	}
	catch (PartInitException e) {
	}
}
*/
}