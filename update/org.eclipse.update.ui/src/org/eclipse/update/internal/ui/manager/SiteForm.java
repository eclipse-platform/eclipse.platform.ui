package org.eclipse.update.internal.ui.manager;

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

public class SiteForm extends UpdateWebForm {
	
public SiteForm(UpdateFormPage page) {
	super(page);
}

public void dispose() {
	super.dispose();
}

public void initialize(Object modelObject) {
	setHeadingText("Site Page");
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
	layout.verticalSpacing = 20;
	layout.numColumns = 1;
	
	FormWidgetFactory factory = getFactory();
	
	Label text = factory.createLabel(parent, null, SWT.WRAP);
	text.setText("Expand the site folder to browse the features available for download.");

	Label link;
	IHyperlinkListener listener;

	listener = new HyperlinkAdapter() {
		public void linkActivated(Control link) {
		}
	};
	link = factory.createLabel(parent, null);
	link.setText("Site home page");
	factory.turnIntoHyperlink(link, listener);
}

public void expandTo(Object obj) {
	if (obj instanceof SiteBookmark) {
		inputChanged((SiteBookmark)obj);
	}
}

private void inputChanged(SiteBookmark site) {
	setHeadingText(site.getName());
	((Composite)getControl()).layout();
	getControl().redraw();
}

}