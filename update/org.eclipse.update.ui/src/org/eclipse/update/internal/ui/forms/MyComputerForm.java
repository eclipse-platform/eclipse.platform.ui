package org.eclipse.update.internal.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.update.internal.ui.pages.*;
import org.eclipse.update.internal.ui.parts.*;
import org.eclipse.update.internal.ui.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.ui.forms.internal.*;
import org.eclipse.swt.layout.*;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.swt.custom.BusyIndicator;
import java.net.URL;
import org.eclipse.core.runtime.CoreException;
import java.text.MessageFormat;


public class MyComputerForm extends UpdateWebForm {
	private SiteBookmark currentBookmark;
	private static final String KEY_TITLE = "MyComputerPage.title";
	private static final String KEY_NTITLE = "MyComputerPage.ntitle";
	private static final String KEY_DESC = "MyComputerPage.desc";
	
public MyComputerForm(UpdateFormPage page) {
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
	text.setText(UpdateUIPlugin.getResourceString(KEY_DESC));
}

public void expandTo(Object obj) {
	if (obj instanceof MyComputer) {
		inputChanged((MyComputer)obj);
	}
}

private void inputChanged(MyComputer myComputer) {
	String pattern = UpdateUIPlugin.getResourceString(KEY_TITLE);
	String name = System.getProperty("user.name");
	String message = MessageFormat.format(pattern, new Object[] {name});
	setHeadingText(message);
}

}