package org.eclipse.update.ui.forms.internal;
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

/**
 * This form implementation assumes that it contains
 * form sections and that they fill up the screen.
 * Typically, the form client will use up the entire
 * parent area below the heading. Sections must
 * be able to return their preferred size in both
 * dimensions. If they contain wrappable text,
 * WebForm should be used instead.
 */


public class SectionForm extends AbstractSectionForm implements PaintListener {
	private Composite control;
	private int TITLE_HMARGIN = 10;
	private int TITLE_VMARGIN = 5;

	class FormLayout extends Layout {
		protected Point computeSize(
			Composite composite,
			int wHint,
			int hHint,
			boolean flushCache) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT)
				return new Point(wHint, hHint);
			int x = 0;
			Control client = composite.getChildren()[0];
			Point csize = client.computeSize(widthHint, heightHint, flushCache);
			if (headingVisible) csize.y += getTitleHeight();
			return csize;
		}
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle clientArea = composite.getClientArea();
			Control client = composite.getChildren()[0];
			int theight = headingVisible?getTitleHeight() : 0;
			client.setBounds(
				clientArea.x,
				clientArea.y + theight,
				clientArea.width,
				clientArea.height - theight);
		}
	}
	private int widthHint=SWT.DEFAULT;
	private int heightHint=SWT.DEFAULT;

public SectionForm() {
}

public Control createControl(Composite parent) {
	Composite canvas = new Composite(parent, SWT.NONE);
	canvas.setBackground(factory.getBackgroundColor());
	canvas.setForeground(factory.getForegroundColor());
	canvas.addPaintListener(this);
	canvas.setLayout(new FormLayout());
	Composite formParent = factory.createComposite(canvas);
	createFormClient(formParent);
	this.control = canvas;
	canvas.setFocus();
	return canvas;
}

protected void createFormClient(Composite parent) {
	factory.createComposite(parent);
}

public Control getControl() {
	return control;
}

private int getTitleHeight() {
	int imageHeight = 0;
	if (headingImage!=null && SWT.getPlatform().equals("motif")==false) {
		imageHeight = headingImage.getBounds().height;
	}
	GC gc = new GC(control);
	gc.setFont(titleFont);
	FontMetrics fm = gc.getFontMetrics();
	int fontHeight = fm.getHeight();
	gc.dispose();

	int height =  fontHeight + TITLE_VMARGIN + TITLE_VMARGIN;
	return Math.max(height, imageHeight);
}

private void paint(Control form, GC gc) {
	Rectangle bounds = form.getBounds();
	int height = getTitleHeight();
	if (headingImage != null) {
		Rectangle imageBounds = headingImage.getBounds();
		int x = bounds.width - imageBounds.width;
		int y = 0;
		//x = Math.max(x, 0);
		x = 0;
		if (headingBackground != null) {
			gc.setBackground(headingBackground);
			gc.fillRectangle(0, 0, bounds.width, height);
		}
		if (SWT.getPlatform().equals("motif")==false) {
	       gc.drawImage(headingImage, x, y);
		}
		if (headingForeground != null)
			gc.setForeground(headingForeground);
		else
			gc.setForeground(factory.getForegroundColor());
		gc.setFont(titleFont);
		gc.drawText(getHeadingText(), TITLE_HMARGIN, TITLE_VMARGIN, true);
	} else {
		gc.setFont(titleFont);
		gc.setBackground(factory.getColor(factory.DEFAULT_HEADER_COLOR));
		//gc.fillRectangle(TITLE_HMARGIN, TITLE_VMARGIN, bounds.width-TITLE_HMARGIN*2, height-TITLE_VMARGIN*2);
		gc.setForeground(factory.getForegroundColor());
		gc.drawText(getHeadingText(), TITLE_HMARGIN, TITLE_VMARGIN, true);
	}
}

public final void paintControl(PaintEvent event) {
	if (!headingVisible) return;
	GC gc = event.gc;
	Control form = (Control)event.widget;
	paint(form, gc);
}

public void setHeadingVisible(boolean newHeadingVisible) {
	super.setHeadingVisible(newHeadingVisible);
	if (control != null)
		control.layout(true);
}

public void propertyChange(PropertyChangeEvent event) {
	titleFont = JFaceResources.getHeaderFont();
	if (control!=null) { 
		control.layout(true);
		control.redraw();
	}
}

}
