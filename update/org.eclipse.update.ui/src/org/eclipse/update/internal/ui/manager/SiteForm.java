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
import org.eclipse.swt.custom.BusyIndicator;
import java.net.URL;
import org.eclipse.core.runtime.CoreException;

public class SiteForm extends UpdateWebForm {
	private Label url;
	private SiteBookmark currentBookmark;
	
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
	url = factory.createHeadingLabel(parent, null);
	
	Label text = factory.createLabel(parent, null, SWT.WRAP);
	text.setText("Expand the site folder to browse the features available for download.");

	Label link;
	IHyperlinkListener listener;

	listener = new HyperlinkAdapter() {
		public void linkActivated(Control link) {
			if (currentBookmark==null) return;
			BusyIndicator.showWhile(getControl().getDisplay(),
			new Runnable() {
				public void run() {
					try {
						if (!currentBookmark.isSiteConnected()) {
							currentBookmark.connect();
						}
						ISite site = currentBookmark.getSite();
						if (site!=null) {
							URL infoURL = site.getInfoURL();
							if (infoURL!=null) {
								DetailsView dv = (DetailsView)getPage().getView();
								dv.showURL(infoURL.toString());
							}
						}
					}
					catch (CoreException e) {
						System.out.println(e);
					}
				}
			});
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
	url.setText(site.getURL().toString());
	url.getParent().layout();
	((Composite)getControl()).layout();
	getControl().redraw();
	currentBookmark = site;
}

}