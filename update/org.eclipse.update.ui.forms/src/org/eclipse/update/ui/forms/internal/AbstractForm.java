package org.eclipse.update.ui.forms.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.*;
import org.eclipse.ui.*;

/**
 * This class implements IForm interface and
 * provides some common services to its subclasses.
 * It handles some common properties like heading
 * text, image and colors, as well as
 * font change processing.
 */

public abstract class AbstractForm implements IForm, IPropertyChangeListener {
	protected FormWidgetFactory factory;
	protected Color headingBackground;
	protected Color headingForeground;
	protected boolean headingVisible=true;
	protected Image headingImage;
	protected String headingText;
	protected Font titleFont;
	
	public AbstractForm() {
		factory = new FormWidgetFactory();
	   	titleFont = JFaceResources.getHeaderFont();
   		JFaceResources.getFontRegistry().addListener(this);
	}

	/**
	 * @see IForm#commitChanges(boolean)
	 */
	public void commitChanges(boolean onSave) {
	}


	/**
	 * @see IForm#createControl(Composite)
	 */
	public abstract Control createControl(Composite parent);

	/**
	 * @see IForm#dispose()
	 */
	public void dispose() {
		factory.dispose();
		JFaceResources.getFontRegistry().removeListener(this);
	}


	/**
	 * @see IForm#doGlobalAction(String)
	 */
	public void doGlobalAction(String actionId) {
	}


	/**
	 * @see IForm#expandTo(Object)
	 */
	public void expandTo(Object object) {
	}


	/**
	 * @see IForm#getControl()
	 */
	public abstract Control getControl();

	/**
	 * @see IForm#getFactory()
	 */
	public FormWidgetFactory getFactory() {
		return factory;
	}


	/**
	 * @see IForm#getHeadingBackground()
	 */
	public Color getHeadingBackground() {
		return headingBackground;
	}


	/**
	 * @see IForm#getHeadingForeground()
	 */
	public Color getHeadingForeground() {
		return headingForeground;
	}


	/**
	 * @see IForm#getHeadingImage()
	 */
	public Image getHeadingImage() {
		return headingImage;
	}


	/**
	 * @see IForm#getHeading()
	 */
	public String getHeadingText() {
		return headingText;
	}


	/**
	 * @see IForm#initialize(Object)
	 */
	public void initialize(Object model) {
	}


	/**
	 * @see IForm#isHeadingVisible()
	 */
	public boolean isHeadingVisible() {
		return headingVisible;
	}


	/**
	 * @see IForm#registerSection(FormSection)
	 */
	public void registerSection(FormSection section) {
	}


	/**
	 * @see IForm#setFocus()
	 */
	public void setFocus() {
	}


	/**
	 * @see IForm#setHeadingBackground(Color)
	 */
	public void setHeadingBackground(Color newHeadingBackground) {
		this.headingBackground = newHeadingBackground;
	}


	/**
	 * @see IForm#setHeadingForeground(Color)
	 */
	public void setHeadingForeground(Color newHeadingForeground) {
		this.headingForeground = newHeadingForeground;
	}


	/**
	 * @see IForm#setHeadingImage(Image)
	 */
	public void setHeadingImage(Image headingImage) {
		this.headingImage = headingImage;
	}


	/**
	 * @see IForm#setHeadingVisible(boolean)
	 */
	public void setHeadingVisible(boolean newHeadingVisible) {
		this.headingVisible = newHeadingVisible;
	}


	/**
	 * @see IForm#setHeading(String)
	 */
	public void setHeadingText(String headingText) {
		this.headingText = headingText;
	}


	/**
	 * @see IForm#update()
	 */
	public void update() {
	}
	
protected boolean canPerformDirectly(String id, Control control) {
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


}

