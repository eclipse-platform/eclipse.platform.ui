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
// FIXME: we cannot talk to the internal classes
import org.eclipse.ui.internal.*;

public class LocalSiteForm extends UpdateWebForm {
private static final String KEY_TITLE = "LocalSitePage.title";

	private Label url;
	private SiteBookmark currentBookmark;
	private	PlatformInfo 	platformInfo;	//the platform info
	private	ProductInfo 	productInfo;	//the product info
	private Image image;

public LocalSiteForm(UpdateFormPage page) {
	super(page);
	platformInfo = ((Workbench)PlatformUI.getWorkbench()).getPlatformInfo();
	productInfo = ((Workbench)PlatformUI.getWorkbench()).getProductInfo();
	image =  productInfo.getAboutImage();	// may be null
}

public void dispose() {
	if (image!=null)
	   image.dispose();
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
	FormWidgetFactory factory = getFactory();
	HTMLTableLayout layout = new HTMLTableLayout();
	parent.setLayout(layout);
	layout.leftMargin = layout.rightMargin = 10;
	layout.topMargin = 10;
	layout.horizontalSpacing = 20;
	layout.verticalSpacing = 20;
	layout.numColumns = image!=null?2 : 1;
	
	if (image!=null) {
		Label ilabel = factory.createLabel(parent, null);
		ilabel.setImage(image);
	}

	// text on the right
	Label label = factory.createLabel(parent, null, SWT.WRAP );
	TableData data = new TableData();
	data.align = TableData.FILL;
	label.setText(productText());
	label.setLayoutData(data);
	
	Composite sep = factory.createCompositeSeparator(parent);
	data = new TableData();
	data.align = TableData.FILL;
	data.heightHint = 1;
	data.colspan = image!=null?2 : 1;
	sep.setLayoutData(data);
	
	if (image!=null) factory.createLabel(parent, null);
	
	// text on the right
	label = factory.createLabel(parent, null, SWT.WRAP );
	data = new TableData();
	data.align = TableData.FILL;
	label.setText(platformText());
	label.setLayoutData(data);
	
	sep = factory.createCompositeSeparator(parent);
	data = new TableData();
	data.align = TableData.FILL;
	data.heightHint = 1;
	data.colspan = image!=null?2:1;
	sep.setLayoutData(data);
}

public void expandTo(Object obj) {
}

private void inputChanged(SiteBookmark site) {
}

/**
 * Answer the platform text to show on the right side of the dialog.
 */ 
protected String platformText() {
	if (platformInfo.getBuildID().length() == 0) {
		return WorkbenchMessages.format("AboutText.withoutBuildNumber", new Object[] {platformInfo.getDetailedName(),platformInfo.getVersion(),platformInfo.getCopyright()}); //$NON-NLS-1$
	} else {
		return WorkbenchMessages.format("AboutText.withBuildNumber", new Object[] {platformInfo.getDetailedName(),platformInfo.getVersion(),platformInfo.getBuildID(),platformInfo.getCopyright()}); //$NON-NLS-1$
	}
}
/**
 * Answer the product text to show on the right side of the dialog.
 */ 
protected String productText() {
	if (productInfo.getBuildID().length() == 0) {
		return WorkbenchMessages.format("AboutText.withoutBuildNumber", new Object[] {productInfo.getDetailedName(),productInfo.getVersion(),productInfo.getCopyright()}); //$NON-NLS-1$
	} else {
		return WorkbenchMessages.format("AboutText.withBuildNumber", new Object[] {productInfo.getDetailedName(),productInfo.getVersion(),productInfo.getBuildID(),productInfo.getCopyright()}); //$NON-NLS-1$
	}
}

}