package org.eclipse.update.internal.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.update.internal.ui.UpdateUIPlugin;
import org.eclipse.update.internal.ui.model.SiteBookmark;
import org.eclipse.update.internal.ui.pages.UpdateFormPage;
import org.eclipse.update.internal.ui.parts.AboutInfo;
import org.eclipse.update.internal.ui.views.DetailsView;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.update.ui.forms.internal.engine.*;

public class LocalSiteForm extends UpdateWebForm {
	private static final String KEY_TITLE = "LocalSitePage.title";

	private Label url;
	private SiteBookmark currentBookmark;
	private AboutInfo aboutInfo;
	private Image image;

	public LocalSiteForm(UpdateFormPage page) {
		super(page);
		aboutInfo = UpdateUIPlugin.getDefault().getAboutInfo();
		ImageDescriptor desc = aboutInfo.getAboutImage();
		if (desc != null)
			image = desc.createImage(); // may be null
	}

	public void dispose() {
		if (image != null)
			image.dispose();
		super.dispose();
	}

	public void initialize(Object modelObject) {
		setHeadingText(UpdateUIPlugin.getResourceString(KEY_TITLE));
		super.initialize(modelObject);
	}

	protected void createContents(Composite parent) {
		FormWidgetFactory factory = getFactory();
		HTMLTableLayout layout = new HTMLTableLayout();
		parent.setLayout(layout);
		layout.leftMargin = layout.rightMargin = 10;
		layout.topMargin = 10;
		layout.horizontalSpacing = 20;
		layout.verticalSpacing = 20;
		layout.numColumns = image != null ? 2 : 1;

		if (image != null) {
			Label ilabel = factory.createLabel(parent, null);
			ilabel.setImage(image);
		}

		HTTPAction action = new HTTPAction() {
			public void linkActivated(IHyperlinkSegment link) {
				((DetailsView) getPage().getView()).showURL(link.getText());
			}
		};
		IActionBars bars = getPage().getView().getViewSite().getActionBars();
		action.setStatusLineManager(bars.getStatusLineManager());
		// text on the right
		FormEngine engine = factory.createFormEngine(parent);
		engine.registerTextObject(FormEngine.URL_HANDLER_ID, action);
		engine.setParagraphsSeparated(false);
		engine.setHyperlinkSettings(factory.getHyperlinkHandler());
		engine.marginWidth = 1;
		TableData data = new TableData();
		data.align = TableData.FILL;
		engine.load(getProductText(), false, true);
		engine.setLayoutData(data);
		setFocusControl(engine);

		Composite sep = factory.createCompositeSeparator(parent);
		data = new TableData();
		data.align = TableData.FILL;
		data.heightHint = 1;
		data.colspan = image != null ? 2 : 1;
		sep.setLayoutData(data);
	}

	public void expandTo(Object obj) {
	}

	private void inputChanged(SiteBookmark site) {
	}
	private String getProductText() {
		return aboutInfo.getAboutText();
	}
}