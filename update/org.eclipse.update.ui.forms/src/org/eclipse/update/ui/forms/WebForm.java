package org.eclipse.update.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import java.util.*;
import org.eclipse.ui.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.*;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.*;

/**
 * This form implementation assumes that it contains
 * children that do not have independent dimensions.
 * In other words, these widgets are not capable
 * of answering their preferred size. Instead,
 * desired width must be supplied to get the
 * preferred height. These forms are layed out
 * top to bottom, left to right and use
 * a layout algorithm very similar to
 * HTML tables. Scrolling is not optional
 * for this type of presentation - 
 * scroll bars will show up when needed.
 */

public class WebForm extends AbstractSectionForm {
protected ScrolledComposite scrollComposite;
private Composite control;
protected HTMLTableLayout layout;
private FormText formText;

public WebForm() {
}

public Control createControl(Composite parent) {
	scrollComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
	Composite form = factory.createComposite(scrollComposite);
	form.setBackground(form.getDisplay().getSystemColor(SWT.COLOR_YELLOW));

	scrollComposite.setContent(form);
    //scrollComposite.setBackground(factory.getBackgroundColor());
    scrollComposite.addListener (SWT.Resize,  new Listener () {
		public void handleEvent (Event e) {
			Rectangle ssize = scrollComposite.getClientArea();
			int swidth = ssize.width;
			Point size = layout.computeSize(control, swidth, SWT.DEFAULT, true);
			System.out.println("size.x="+size.x+", swidth="+swidth);
			if (size.x < swidth) size.x = swidth;
			//Rectangle trim = control.computeTrim(0, 0, size.x, size.y);
			//size = new Point(trim.width, trim.height);
			control.setSize(size);
		}
	});
	layout = new HTMLTableLayout();
	layout.leftMargin = layout.rightMargin = 0;
	layout.topMargin = layout.bottomMargin = 0;
	form.setLayout(layout);
	formText = new FormText(form, SWT.WRAP);
	//formText.setBackground(factory.getBackgroundColor());
	formText.setBackground(formText.getDisplay().getSystemColor(SWT.COLOR_GREEN));
	formText.setForeground(factory.getForegroundColor());
	formText.setFont(titleFont);
	TableData td = new TableData();
	td.align = TableData.FILL;
	formText.setLayoutData(td);
	this.control = form;
	createClient(form);
	form.setFocus();
	return scrollComposite;
}

protected void createClient(Composite parent) {
	Composite comp = factory.createComposite(parent);
	//TableData td = new TableData();
	//td.colspan = getNumColumns();
	//td.align = TableData.FILL;
	//td.valign = TableData.FILL;
	//comp.setLayoutData(td);
}

public Control getControl() {
	return control;
}

public void setHeadingText(String text) {
	super.setHeadingText(text);
	if (formText!=null)
	   formText.setText(text);
}

public void setHeadingImage(Image image) {
	super.setHeadingImage(image);
	if (formText!=null)
	   formText.setBackgroundImage(image);
}

public void setHeadingVisible(boolean newHeadingVisible) {
	super.setHeadingVisible(newHeadingVisible);
	if (control != null)
		control.layout(true);
}

public void propertyChange(PropertyChangeEvent event) {
	titleFont = JFaceResources.getHeaderFont();
	if (control!=null) { 
		formText.setFont(titleFont);
		control.layout();
	}
}

}
