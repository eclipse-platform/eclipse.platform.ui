package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.resource.JFaceResources;

public class MainForm extends UpdateWebForm {
	Image itemImage;
	
public MainForm(UpdateFormPage page) {
	super(page);
	itemImage = UpdateUIPluginImages.DESC_ITEM.createImage();
}

public void dispose() {
	itemImage.dispose();
	super.dispose();
}

public void initialize(Object modelObject) {
	setHeadingText("Welcome to Eclipse Update");
	setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
	setHeadingUnderlineImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_UNDERLINE));
	super.initialize(modelObject);
	//((Composite)getControl()).layout(true);
}

protected int getNumColumns() {
	return 1;
}

protected void createContents(Composite parent) {
	HTMLTableLayout layout = new HTMLTableLayout();
	parent.setLayout(layout);
	layout.leftMargin = layout.rightMargin = 10;
	layout.topMargin = 15;
	layout.horizontalSpacing = 0;
	layout.verticalSpacing = 0;
	layout.numColumns = 2;
	
	FormWidgetFactory factory = getFactory();
	
	Label topic;
	SelectableFormLabel link;
	Label text;
	IHyperlinkListener listener;

	listener = new HyperlinkAdapter() {
		public void linkActivated(Control link) {
			showView(UpdatePerspective.ID_LOCAL_SITE);
		}
	};
	topic = factory.createLabel(parent, null);
	topic.setImage(itemImage);
	link = new SelectableFormLabel(parent, SWT.NULL);
	link.setFont(JFaceResources.getBannerFont());
	link.setText("Feature Updates");
	factory.turnIntoHyperlink(link, listener);

	factory.createLabel(parent, null);	
	text = factory.createLabel(parent, null, SWT.WRAP);	
	text.setText(
	"To check for updates for features you have installed, "+
	"go to the \"My Eclipse\" view and expand \"Available Updates\" folder. "+
	"You can drag new updates into the Checklist folder.\n");
	TableData td = new TableData();
	//td.colspan = 2;
	text.setLayoutData(td);
	
	listener = new HyperlinkAdapter() {
		public void linkActivated(Control link) {
			showView(UpdatePerspective.ID_SITES);
		}
	};
	topic = factory.createLabel(parent, null);
	topic.setImage(itemImage);
	link = new SelectableFormLabel(parent, SWT.NULL);
	link.setFont(JFaceResources.getBannerFont());
	link.setText("New Feature Installs");
	factory.turnIntoHyperlink(link, listener);

	factory.createLabel(parent, null);	
	text = factory.createLabel(parent, null, SWT.WRAP);
	text.setText(
	"To install new features, open \"Update Sites\" view and expand"+
	"the desired site (this operation may take time). Select features and "+
	"read about them in the \"Details\" view. If you want to install a feature, "+
	"just drag it into the \"Checklist\" view.\n");
	td = new TableData();
	//td.colspan = 2;
	text.setLayoutData(td);
	
	listener = new HyperlinkAdapter() {
		public void linkActivated(Control link) {
			showView(UpdatePerspective.ID_LOCAL_SITE);
		}
	};
	topic = factory.createLabel(parent, null);
	topic.setImage(itemImage);
	link = new SelectableFormLabel(parent, SWT.NULL);
	link.setFont(JFaceResources.getBannerFont());
	link.setText("Uninstalling Features");
	factory.turnIntoHyperlink(link, listener);

	factory.createLabel(parent, null);		
	text = factory.createLabel(parent, null, SWT.WRAP);
	text.setText("To uninstall a feature, open \"My Eclipse\" view, "+
	"expand \"Installed Features\" folder, select the desired feature and drag it "+
	"into the \"Checklist\" folder.\n");
	td = new TableData();
	//td.colspan = 2;
	text.setLayoutData(td);

	listener = new HyperlinkAdapter() {
		public void linkActivated(Control link) {
			showView(UpdatePerspective.ID_HISTORY);
		}
	};
	topic = factory.createLabel(parent, null);
	topic.setImage(itemImage);
	link = new SelectableFormLabel(parent, SWT.NULL);
	link.setFont(JFaceResources.getBannerFont());
	link.setText("Installation History");
	factory.turnIntoHyperlink(link, listener);
	factory.createLabel(parent, null);	
	text = factory.createLabel(parent, null, SWT.WRAP);
	text.setText("You can review your installation history or revert to one of the previous stable states."+
	" To do so, open \"Update History\" view.\n");
	td = new TableData();
	//td.colspan = 2;
	text.setLayoutData(td);
}

private void showView(String viewId) {
	try {
		IViewPart part = UpdateUIPlugin.getActivePage().showView(viewId);
	}
	catch (PartInitException e) {
	}
}
}