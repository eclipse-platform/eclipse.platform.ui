package org.eclipse.update.internal.ui.forms;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.internal.ui.pages.*;
import org.eclipse.update.internal.ui.parts.*;

public class PropertyWebForm extends UpdateWebForm {

	/**
	 * Constructor for PropertyWebForm.
	 * @param page
	 */
	public PropertyWebForm(IUpdateFormPage page) {
		super(page);
	}
	
protected Label createProperty(Composite parent, String name) {
	createHeading(parent, name);
	Label label = factory.createLabel(parent, null);
	label.setText("");
	label.setLayoutData(createPropertyLayoutData());
	return label;
}

protected Object createPropertyLayoutData() {
	GridData gd = new GridData();
	gd.horizontalIndent = 10;
	return gd;
}

protected Label createHeading(Composite parent, String text) {
	Label l = factory.createHeadingLabel(parent, text);
	Color hc;
    hc = factory.getColor(factory.COLOR_COMPOSITE_SEPARATOR);	
  	l.setForeground(hc);
	return l;
}

}

