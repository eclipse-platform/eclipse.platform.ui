package org.eclipse.jface.preference;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.dialogs.IDialogConstants;

/**
 * A field editor for a color type preference.
 */
public class ColorFieldEditor extends FieldEditor {

	/**
	 * The color button, or <code>null</code> if none.
	 */
	private Button colorButton;

	/**
	 * The chosen color.
	 */
	private RGB chosenColor;

	/**
	 * The current color, or <code>null</code> if none.
	 */
	private Color color;

	/**
	 * The color image displayed on the button.
	 */
	private Image image;

	/**
	 * The extent of the color image <code>image</code>.
	 */
	private Point extent;
/**
 * Creates a new color field editor 
 */
protected ColorFieldEditor() {
}
/**
 * Creates a color field editor.
 * 
 * @param name the name of the preference this field editor works on
 * @param labelText the label text of the field editor
 * @param parent the parent of the field editor's control
 */
public ColorFieldEditor(String name, String labelText, Composite parent) {
	super(name, labelText, parent);
}
/* (non-Javadoc)
 * Method declared on FieldEditor.
 */
protected void adjustForNumColumns(int numColumns) {
	((GridData)colorButton.getLayoutData()).horizontalSpan = numColumns - 1;
}
/**
 * Computes the size of the color image displayed on the button.
 * <p>
 * This is an internal method and should not be called by clients.
 * </p>
 */
protected Point computeImageSize(Control window) {
	// Make the image height as high as a corresponding character. This
	// makes sure that the button has the same size as a "normal" text
	// button.	
	GC gc = new GC(window);
	Font f = JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);
	gc.setFont(f);
	int height = gc.getFontMetrics().getHeight();
	gc.dispose();
	Point p = new Point(height * 3 - 6, height);
	return p;
}
/* (non-Javadoc)
 * Method declared on FieldEditor.
 */
protected void doFillIntoGrid(Composite parent, int numColumns) {
	Control control = getLabelControl(parent);
	GridData gd = new GridData();
	gd.horizontalSpan = numColumns - 1;
	control.setLayoutData(gd);
		
	colorButton = getChangeControl(parent);
	gd = new GridData();
	gd.heightHint = convertVerticalDLUsToPixels(colorButton, IDialogConstants.BUTTON_HEIGHT);
	int widthHint = convertHorizontalDLUsToPixels(colorButton, IDialogConstants.BUTTON_WIDTH);
	gd.widthHint = Math.max(widthHint, colorButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	colorButton.setLayoutData(gd);
}
/* (non-Javadoc)
 * Method declared on FieldEditor.
 */
protected void doLoad() {
	if (colorButton == null)
		return;
	RGB rgb = PreferenceConverter.getColor(getPreferenceStore(), getPreferenceName());
	updateColorImage(colorButton.getDisplay(), rgb);
}
/* (non-Javadoc)
 * Method declared on FieldEditor.
 */
protected void doLoadDefault() {
	if (colorButton == null)
		return;
	RGB rgb = PreferenceConverter.getDefaultColor(getPreferenceStore(), getPreferenceName());
	updateColorImage(colorButton.getDisplay(), rgb);
}
/* (non-Javadoc)
 * Method declared on FieldEditor.
 */
protected void doStore() {
	PreferenceConverter.setValue(getPreferenceStore(), getPreferenceName(), chosenColor);
}
/**
 * Returns the change button for this field editor.
 *
 * @return the change button
 */
protected Button getChangeControl(Composite parent) {
	if (colorButton == null) {
		extent= computeImageSize(parent);
		image= new Image(parent.getDisplay(), extent.x, extent.y);
		
		colorButton= new Button(parent, SWT.PUSH);

		GC gc= new GC(image);
		gc.setBackground(colorButton.getBackground());
		gc.fillRectangle(0,0, extent.x, extent.y);
		gc.dispose();
		
		colorButton.setImage(image);
		colorButton.setFont(parent.getFont());
		colorButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				ColorDialog colorDialog= new ColorDialog(colorButton.getShell());
				colorDialog.setRGB(chosenColor);
				RGB newColor = colorDialog.open();
				if (newColor != null) {
					RGB oldValue= chosenColor;
					updateColorImage(colorButton.getDisplay(), newColor);
					setPresentsDefaultValue(false);
					fireValueChanged(VALUE, oldValue, newColor);
				}
				
			}
		});
		colorButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				if (image != null) 
					image.dispose();
				if (color != null)	
					color.dispose();
				image= null;
				color= null;
				colorButton= null;					
			}
		});
	} else {
		checkParent(colorButton, parent);
	}	
	
	setButtonLayoutData(colorButton);
	return colorButton;
}
/* (non-Javadoc)
 * Method declared on FieldEditor.
 */
public int getNumberOfControls() {
	return 2;
}
/**
 * Updates the color image displayed on the button to match the given
 * color.
 * <p>
 * This is an internal method and should not be called by clients.
 * </p>
 */
protected void updateColorImage(final Display display, final RGB rgb) {
	GC gc = new GC(image);
	gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
	gc.drawRectangle(0, 2, extent.x - 1, extent.y - 4);
	if (color != null) {
		color.dispose();
	}
	color = new Color(display, rgb);
	gc.setBackground(color);
	gc.fillRectangle(1, 3, extent.x - 2, extent.y - 5);
	gc.dispose();
	colorButton.setImage(image);
	chosenColor = rgb;
}
}
