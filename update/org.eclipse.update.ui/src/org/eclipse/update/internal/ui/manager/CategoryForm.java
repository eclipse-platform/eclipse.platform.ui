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

public class CategoryForm extends UpdateWebForm {
	private final static String KEY_TITLE = "CategoryPage.title";
	private final static String KEY_MORE_INFO = "CategoryPage.moreInfo";
	SiteCategory currentCategory;
	Label textLabel;
	SelectableFormLabel link;
	
public CategoryForm(UpdateFormPage page) {
	super(page);
}

public void dispose() {
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
	layout.verticalSpacing = 20;
	layout.numColumns = 1;
	
	FormWidgetFactory factory = getFactory();
	textLabel = factory.createLabel(parent, null, SWT.WRAP);
	
	IHyperlinkListener listener;

	listener = new HyperlinkAdapter() {
		public void linkActivated(Control link) {
			if (currentCategory==null) return;
			BusyIndicator.showWhile(getControl().getDisplay(),
			new Runnable() {
				public void run() {
					ICategory category = currentCategory.getCategory();
					if (category!=null) {
						IURLEntry info = category.getDescription();
						URL infoURL = info.getURL();
						if (infoURL!=null) {
							DetailsView dv = (DetailsView)getPage().getView();
							dv.showURL(infoURL.toString());
						}
					}
				}
			});
		}
	};
	link = new SelectableFormLabel(parent, SWT.NULL);
	link.setText(UpdateUIPlugin.getResourceString(KEY_MORE_INFO));
	factory.turnIntoHyperlink(link, listener);
	link.setVisible(false);
}

public void expandTo(Object obj) {
	if (obj instanceof SiteCategory) {
		inputChanged((SiteCategory)obj);
	}
}

private void inputChanged(SiteCategory category) {
	setHeadingText(category.getCategory().getLabel());
	IURLEntry info = category.getCategory().getDescription();
	if (info!=null) {
		String text = info.getAnnotation();
		if (text!=null)
		   textLabel.setText(text);
		else
		   textLabel.setText("");
		link.setVisible(info.getURL()!=null);
	}
	else {
		textLabel.setText("");
		link.setVisible(false);
	}
	textLabel.getParent().layout();
	((Composite)getControl()).layout();
	getControl().redraw();
	currentCategory = category;
}

}