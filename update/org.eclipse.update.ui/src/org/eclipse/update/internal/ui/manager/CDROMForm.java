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

public class CDROMForm extends UpdateWebForm {
	private Label url;
	private SiteBookmark currentBookmark;
	
public CDROMForm(UpdateFormPage page) {
	super(page);
}

public void dispose() {
	super.dispose();
}

public void initialize(Object modelObject) {
	setHeadingText("Compact Disc");
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
	text.setText("Insert the Compact Disc with an Eclipse-based product, expand the Compact Disc object and browse the available features.");
}

public void expandTo(Object obj) {
	if (obj instanceof CDROM) {
		inputChanged((CDROM)obj);
	}
}

private void inputChanged(CDROM cdrom) {
	if (cdrom.isAvailable()) {
		setHeadingText("Compact Disc - "+cdrom.getName());
	}
	else {
		setHeadingText("Compact Disc (not available)");
	}
}

}