package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.jface.resource.JFaceResources;

public class MainForm extends UpdateWebForm {
private static final String KEY_TITLE = "HomePage.title";
private static final String KEY_UPDATES_TITLE = "HomePage.updates.title";
private static final String KEY_UPDATES_DESC = "HomePage.updates.desc";
private static final String KEY_INSTALLS_TITLE = "HomePage.installs.title";
private static final String KEY_INSTALLS_DESC = "HomePage.installs.desc";
private static final String KEY_UNINSTALLS_TITLE = "HomePage.uninstalls.title";
private static final String KEY_UNINSTALLS_DESC = "HomePage.uninstals.desc";
private static final String KEY_HISTORY_TITLE = "HomePage.history.title";
private static final String KEY_HISTORY_DESC = "HomePage.history.desc";

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
	setHeadingText(UpdateUIPlugin.getResourceString(KEY_TITLE));
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
			LocalSiteView view = (LocalSiteView)showView(UpdatePerspective.ID_LOCAL_SITE);
			if (view!=null) {
				view.selectUpdateObject();
			}
		}
	};
	topic = factory.createLabel(parent, null);
	topic.setImage(itemImage);
	link = new SelectableFormLabel(parent, SWT.NULL);
	link.setFont(JFaceResources.getBannerFont());
	link.setText(UpdateUIPlugin.getResourceString(KEY_UPDATES_TITLE));
	factory.turnIntoHyperlink(link, listener);

	factory.createLabel(parent, null);	
	text = factory.createLabel(parent, null, SWT.WRAP);	
	text.setText(UpdateUIPlugin.getResourceString(KEY_UPDATES_DESC));
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
	link.setText(UpdateUIPlugin.getResourceString(KEY_INSTALLS_TITLE));
	factory.turnIntoHyperlink(link, listener);

	factory.createLabel(parent, null);	
	text = factory.createLabel(parent, null, SWT.WRAP);
	text.setText(UpdateUIPlugin.getResourceString(KEY_INSTALLS_DESC));
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
	link.setText(UpdateUIPlugin.getResourceString(KEY_UNINSTALLS_TITLE));
	factory.turnIntoHyperlink(link, listener);

	factory.createLabel(parent, null);		
	text = factory.createLabel(parent, null, SWT.WRAP);
	text.setText(UpdateUIPlugin.getResourceString(KEY_UNINSTALLS_DESC));
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
	link.setText(UpdateUIPlugin.getResourceString(KEY_HISTORY_TITLE));
	factory.turnIntoHyperlink(link, listener);
	factory.createLabel(parent, null);	
	text = factory.createLabel(parent, null, SWT.WRAP);
	text.setText(UpdateUIPlugin.getResourceString(KEY_HISTORY_DESC));
	td = new TableData();
	//td.colspan = 2;
	text.setLayoutData(td);
}

private IViewPart showView(String viewId) {
	try {
		IViewPart part = UpdateUIPlugin.getActivePage().showView(viewId);
		return part;
	}
	catch (PartInitException e) {
		return null;
	}
}
}