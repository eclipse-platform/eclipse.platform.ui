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


public class Form implements PaintListener, IPropertyChangeListener {
	private String title;
	private Vector sections;
	private Composite control;
	private Font titleFont;
	private FormWidgetFactory factory;
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
	private int readOnly;
	private org.eclipse.swt.graphics.Color headingBackground;
	private Color headingForeground;
	private Image headingImage;
	private boolean headingVisible=true;

public Form() {
	factory = new FormWidgetFactory();
   	titleFont = JFaceResources.getHeaderFont();
   	JFaceResources.getFontRegistry().addListener(this);
}
private boolean canPerformDirectly(String id, Control control) {
	if (control instanceof Text) {
		Text text = (Text)control;
		if (id.equals(IWorkbenchActionConstants.CUT)) {
			text.cut();
			return true;
		}
		if (id.equals(IWorkbenchActionConstants.COPY)) {
			text.copy();
			return true;
		}
		if (id.equals(IWorkbenchActionConstants.PASTE)) {
			text.paste();
			return true;
		}
		if (id.equals(IWorkbenchActionConstants.SELECT_ALL)) {
			text.selectAll();
			return true;
		}
		if (id.equals(IWorkbenchActionConstants.DELETE)) {
			int count = text.getSelectionCount();
			if (count==0) {
				int caretPos = text.getCaretPosition();
				text.setSelection(caretPos, caretPos+1);
			}
			text.insert("");
			return true;
		}
	}
	return false;
}
public void commitChanges(boolean onSave) {
	if (sections != null) {
		for (Iterator iter = sections.iterator(); iter.hasNext();) {
			FormSection section = (FormSection) iter.next();
			if (section.isDirty()) section.commitChanges(onSave);
		}
	}
}
public Control createControl(Composite parent) {
	Canvas canvas = new Canvas(parent, SWT.NONE);
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
public void dispose() {
	factory.dispose();
	if (sections != null) {
		for (Iterator iter = sections.iterator(); iter.hasNext();) {
			FormSection section = (FormSection) iter.next();
			section.dispose();
		}
	}
	JFaceResources.getFontRegistry().removeListener(this);
}
public void doGlobalAction(String actionId) {
	Display display = control.getDisplay();
	Control focusControl = display.getFocusControl();
	if (focusControl==null) return;

	if (canPerformDirectly(actionId, focusControl)) return;
	Composite parent = focusControl.getParent();
	FormSection targetSection=null;
	while (parent!=null) {
		Object data = parent.getData();
		if (data!=null && data instanceof FormSection) {
			targetSection = (FormSection)data;
			break;
		}
		parent = parent.getParent();
	}
	if (targetSection!=null) {
		targetSection.doGlobalAction(actionId);
	}
}
public void expandTo(Object object) {}
public org.eclipse.swt.widgets.Composite getControl() {
	return control;
}
public FormWidgetFactory getFactory() {
	return factory;
}
public org.eclipse.swt.graphics.Color getHeadingBackground() {
	return headingBackground;
}
public org.eclipse.swt.graphics.Color getHeadingForeground() {
	return headingForeground;
}
public Image getHeadingImage() {
	return headingImage;
}
public int getHeightHint() {
	return heightHint;
}
public int getReadOnly() {
	return readOnly;
}
public String getTitle() {
	return title;
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
public int getWidthHint() {
	return widthHint;
}
public void initialize(Object model) {
	if (sections != null) {
		for (Iterator iter = sections.iterator(); iter.hasNext();) {
			FormSection section = (FormSection) iter.next();
			section.initialize(model);
		}
	}
}
public boolean isHeadingVisible() {
	return headingVisible;
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
		gc.drawText(getTitle(), TITLE_HMARGIN, TITLE_VMARGIN, true);
	} else {
		gc.setFont(titleFont);
		gc.setBackground(factory.getColor(factory.DEFAULT_HEADER_COLOR));
		//gc.fillRectangle(TITLE_HMARGIN, TITLE_VMARGIN, bounds.width-TITLE_HMARGIN*2, height-TITLE_VMARGIN*2);
		gc.setForeground(factory.getForegroundColor());
		gc.drawText(getTitle(), TITLE_HMARGIN, TITLE_VMARGIN, true);
	}
}
public final void paintControl(PaintEvent event) {
	if (!headingVisible) return;
	GC gc = event.gc;
	Control form = (Control)event.widget;
	paint(form, gc);
}
public void registerSection(FormSection section) {
	if (sections == null)
		sections = new Vector();
	sections.addElement(section);
}
public void setFocus() {
	if (sections != null && sections.size()>0) {
		FormSection firstSection = (FormSection)sections.firstElement();
		firstSection.setFocus();
	}
}
public void setHeadingBackground(org.eclipse.swt.graphics.Color newHeadingBackground) {
	headingBackground = newHeadingBackground;
}
public void setHeadingForeground(org.eclipse.swt.graphics.Color newHeadingForeground) {
	headingForeground = newHeadingForeground;
}
public void setHeadingImage(Image headingImage) {
	this.headingImage = headingImage;
}
public void setHeadingVisible(boolean newHeadingVisible) {
	if (newHeadingVisible != headingVisible) {
		headingVisible = newHeadingVisible;
		if (control != null)
			control.layout(true);
	}
}
public void setHeightHint(int newHeightHint) {
	heightHint = newHeightHint;
}
public void setReadOnly(int newReadOnly) {
	readOnly = newReadOnly;
}
public void setTitle(java.lang.String newTitle) {
	title = newTitle;
	if (control!=null) control.redraw();
}
public void setWidthHint(int newWidthHint) {
	widthHint = newWidthHint;
}
public void update() {
	if (sections != null) {
		for (Iterator iter = sections.iterator(); iter.hasNext();) {
			FormSection section = (FormSection) iter.next();
			section.update();
		}
	}
}
public void propertyChange(PropertyChangeEvent arg0) {
	titleFont = JFaceResources.getHeaderFont();
	if (control!=null) { 
		control.layout(true);
		control.redraw();
	}
}

}
