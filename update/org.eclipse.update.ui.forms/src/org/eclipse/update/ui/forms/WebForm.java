package org.eclipse.update.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.swt.layout.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
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
private Composite client;
private final static int HMARGIN = 5;
private final static int VMARGIN = 5;

class WebFormLayout extends Layout {

protected void layout(Composite parent, boolean changed) {
	Rectangle bounds = parent.getClientArea();
	int x =0;
	int y = 0;
	if (isHeadingVisible()) {
		y = getHeadingHeight(parent);
	}
	Point csize = client.computeSize(bounds.width, SWT.DEFAULT, changed);
	client.setBounds(x, y, csize.x, csize.y);
}

private int getHeadingHeight(Composite parent) {
	int width = parent.getSize().x;
	int height =0;
	int imageHeight = 0;
	if (getHeadingImage()!=null) {
		Rectangle ibounds = getHeadingImage().getBounds();
		imageHeight = ibounds.height;
	}
	GC gc = new GC(parent);
	gc.setFont(titleFont);
	int textWidth = width - 2*HMARGIN;
	height = FormText.computeWrapHeight(gc, getHeadingText(), textWidth);
	height += 2*VMARGIN;
	height = Math.max(height, imageHeight);
	return height;
}

protected Point computeSize(Composite parent, int wHint, int hHint, boolean changed) {
	int width = wHint;
	int height = 0;
	if (isHeadingVisible()) {
		height = getHeadingHeight(parent);
	}
	Point csize = client.computeSize(width, SWT.DEFAULT, changed);
	width = csize.x;
	height += csize.y;
	return new Point (width, height);
}
}

public WebForm() {
}

public Control createControl(Composite parent) {
	scrollComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
	scrollComposite.setBackground(factory.getBackgroundColor());
	final Composite form = factory.createComposite(scrollComposite);

	scrollComposite.setContent(form);
    scrollComposite.addListener (SWT.Resize,  new Listener () {
		public void handleEvent (Event e) {
			Rectangle ssize = scrollComposite.getClientArea();
			int swidth = ssize.width;
			WebFormLayout layout = (WebFormLayout)form.getLayout();
			Point size = layout.computeSize(form, swidth, SWT.DEFAULT, true);
			if (size.x < swidth) size.x = swidth;
			Rectangle trim = form.computeTrim(0, 0, size.x, size.y);
			size = new Point(trim.width, trim.height);
			control.setSize(size);
		}
	});
	WebFormLayout layout = new WebFormLayout();
	form.setLayout(layout);
	form.addPaintListener(new PaintListener() {
		public void paintControl(PaintEvent e) {
			paint(e);
		}
	});
	this.control = form;
	client = factory.createComposite(form);
	createContents(client);
	form.setFocus();
	return scrollComposite;
}

protected void createContents(Composite parent) {
}

public Control getControl() {
	return control;
}

public void setHeadingVisible(boolean newHeadingVisible) {
	super.setHeadingVisible(newHeadingVisible);
	if (control != null)
		control.layout();
}

public void propertyChange(PropertyChangeEvent event) {
	titleFont = JFaceResources.getHeaderFont();
	if (control!=null) { 
		control.layout();
	}
}

private void paint(PaintEvent e) {
	GC gc = e.gc;
	if (headingImage!=null) {
		gc.drawImage(headingImage, 0, 0);
	}
	Point size = control.getSize();
	if (getHeadingBackground()!=null)
		gc.setBackground(getHeadingBackground());
	if (getHeadingForeground()!=null)
		gc.setForeground(getHeadingForeground());
	gc.setFont(titleFont);
	FormText.paintWrapText(gc, size, getHeadingText(), HMARGIN, VMARGIN);
}

}
