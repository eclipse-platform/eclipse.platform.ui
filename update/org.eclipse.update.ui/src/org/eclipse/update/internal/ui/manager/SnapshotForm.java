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
import java.util.Date;

public class SnapshotForm extends PropertyWebForm {
	private static final String KEY_CREATED_ON = "SnapshotPage.createdOn";
	private static final String KEY_CURRENT_CONFIG = "SnapshotPage.currentConfig";
	private static final String KEY_YES = "SnapshotPage.yes";
	private static final String KEY_NO = "SnapshotPage.no";
	
	private IInstallConfiguration currentConfiguration;
	private Label dateLabel;
	private Label currentLabel;
	private ActivitySection activitySection;
	private RevertSection revertSection;
	private IUpdateModelChangedListener modelListener;
	
public SnapshotForm(UpdateFormPage page) {
	super(page);
}

public void dispose() {
	super.dispose();
}

public void initialize(Object modelObject) {
	setHeadingText("");
	setHeadingImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_BANNER));
	setHeadingUnderlineImage(UpdateUIPluginImages.get(UpdateUIPluginImages.IMG_FORM_UNDERLINE));
	super.initialize(modelObject);
	UpdateModel model = UpdateUIPlugin.getDefault().getUpdateModel();
	modelListener = new IUpdateModelChangedListener() {
		public void objectAdded(Object parent, Object child) {
		}
		public void objectRemoved(Object parent, Object child) {
		}
		public void objectChanged(Object obj, String property) {
			if (obj.equals(currentConfiguration)) {
				inputChanged(currentConfiguration);
			}
		}
	};
	model.addUpdateModelChangedListener(modelListener);
}

protected void createContents(Composite parent) {
	HTMLTableLayout layout = new HTMLTableLayout();
	parent.setLayout(layout);
	layout.leftMargin = layout.rightMargin = 10;
	layout.topMargin = 10;
	layout.horizontalSpacing = 0;
	layout.verticalSpacing = 0;
	layout.numColumns = 1;
	
	FormWidgetFactory factory = getFactory();
	
	dateLabel = createProperty(parent, UpdateUIPlugin.getResourceString(KEY_CREATED_ON));
	currentLabel = createProperty(parent, UpdateUIPlugin.getResourceString(KEY_CURRENT_CONFIG));
	factory.createLabel(parent,null);
	
	activitySection = new ActivitySection((UpdateFormPage)getPage());
	Control control = activitySection.createControl(parent, factory);
	TableData td = new TableData();
	//td.align = TableData.FILL;
	//td.grabHorizontal = true;
	td.valign = TableData.TOP;
	//td.colspan = 2;
	control.setLayoutData(td);
	
	revertSection = new RevertSection((UpdateFormPage)getPage());
	control = revertSection.createControl(parent, factory);
	td = new TableData();
	td.align = TableData.FILL;
	td.grabHorizontal = true;
	td.valign = TableData.TOP;
	control.setLayoutData(td);
	
	registerSection(activitySection);
	registerSection(revertSection);
}

protected Object createPropertyLayoutData() {
	TableData td = new TableData();
	//td.indent = 10;
	return td;
}

public void expandTo(Object obj) {
	if (obj instanceof IInstallConfiguration) {
		inputChanged((IInstallConfiguration)obj);
	}
}

private void inputChanged(IInstallConfiguration configuration) {
	setHeadingText(configuration.getLabel());
	Date date = configuration.getCreationDate();
	dateLabel.setText(date.toString());
	String isCurrent = configuration.isCurrent()?
		UpdateUIPlugin.getResourceString(KEY_YES): 
		UpdateUIPlugin.getResourceString(KEY_NO);
	currentLabel.setText(isCurrent);

	activitySection.configurationChanged(configuration);
	revertSection.configurationChanged(configuration);
	// reflow
	dateLabel.getParent().layout(true);
	((Composite)getControl()).layout(true);
	getControl().redraw();
	updateSize();
	currentConfiguration = configuration;
}

}