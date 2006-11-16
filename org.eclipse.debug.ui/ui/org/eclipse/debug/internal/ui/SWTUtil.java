/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;


import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * Utility class to simplify access to some SWT resources. 
 */
public class SWTUtil {
		
	/**
	 * Returns a width hint for a button control.
	 */
	public static int getButtonWidthHint(Button button) {
		button.setFont(JFaceResources.getDialogFont());
		PixelConverter converter= new PixelConverter(button);
		int widthHint= converter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}
	
	/**
	 * Sets width and height hint for the button control.
	 * <b>Note:</b> This is a NOP if the button's layout data is not
	 * an instance of <code>GridData</code>.
	 * 
	 * @param	the button for which to set the dimension hint
	 */		
	public static void setButtonDimensionHint(Button button) {
		Assert.isNotNull(button);
		Object gd= button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData)gd).widthHint= getButtonWidthHint(button);	
			((GridData)gd).horizontalAlignment = GridData.FILL;	 
		}
	}		
	
	
	/**
	 * Creates and returns a new push button with the given
	 * label and/or image.
	 * 
	 * @param parent parent control
	 * @param label button label or <code>null</code>
	 * @param image image of <code>null</code>
	 * 
	 * @return a new push button
	 */
	public static Button createPushButton(Composite parent, String label, Image image) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		if (image != null) {
			button.setImage(image);
		}
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		button.setLayoutData(gd);	
		SWTUtil.setButtonDimensionHint(button);
		return button;	
	}	

	/**
	 * Creates and returns a new radio button with the given
	 * label.
	 * 
	 * @param parent parent control
	 * @param label button label or <code>null</code>
	 * 
	 * @return a new radio button
	 */
	public static Button createRadioButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.RADIO);
		button.setFont(parent.getFont());
		if (label != null) {
			button.setText(label);
		}
		GridData gd = new GridData();
		button.setLayoutData(gd);	
		SWTUtil.setButtonDimensionHint(button);
		return button;	
	}	
	
	/**
	 * Creates a new label widget
	 * @param parent the parent composite to add this label widget to
	 * @param text the text for the label
	 * @param hspan the horizontal span to take up in the parent composite
	 * @return the new label
	 * @since 3.2
	 * 
	 */
	public static Label createLabel(Composite parent, String text, int hspan) {
		Label l = new Label(parent, SWT.NONE);
		l.setFont(parent.getFont());
		l.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = hspan;
		gd.grabExcessHorizontalSpace = false;
		l.setLayoutData(gd);
		return l;
	}
	
	/**
	 * Creates a new label widget
	 * @param parent the parent composite to add this label widget to
	 * @param text the text for the label
	 * @param font the font for the label
	 * @param hspan the horizontal span to take up in the parent composite
	 * @return the new label
	 * @since 3.3
	 */
	public static Label createLabel(Composite parent, String text, Font font, int hspan) {
		Label l = new Label(parent, SWT.NONE);
		l.setFont(font);
		l.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = hspan;
		l.setLayoutData(gd);
		return l;
	}
	
	/**
	 * Creates a wrapping label
	 * @param parent the parent composite to add this label to
	 * @param text the text to be displayed in the label
	 * @param hspan the horozontal span that label should take up in the parent composite
	 * @param wrapwidth the width hint that the label should wrap at
	 * @return a new label that wraps at a specified width
	 * @since 3.3
	 */
	public static Label createWrapLabel(Composite parent, String text, int hspan, int wrapwidth) {
		Label l = new Label(parent, SWT.NONE | SWT.WRAP);
		l.setFont(parent.getFont());
		l.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = hspan;
		gd.widthHint = wrapwidth;
		l.setLayoutData(gd);
		return l;
	}
	
	/**
	 * Creates a wrapping label
	 * @param parent the parent composite to add this label to
	 * @param text the text to be displayed in the label
	 * @param hspan the horozontal span that label should take up in the parent composite
	 * @return a new label that wraps at a specified width
	 * @since 3.3
	 */
	public static Label createWrapLabel(Composite parent, String text, int hspan) {
		Label l = new Label(parent, SWT.NONE | SWT.WRAP);
		l.setFont(parent.getFont());
		l.setText(text);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = hspan;
		l.setLayoutData(gd);
		return l;
	}
	
	/**
	 * Creates a new text widget 
	 * @param parent the parent composite to add this text widget to
	 * @param hspan the horizontal span to take up on the parent composite
	 * @return the new text widget
	 * @since 3.2
	 * 
	 */
	public static Text createSingleText(Composite parent, int hspan) {
    	Text t = new Text(parent, SWT.SINGLE | SWT.BORDER);
    	t.setFont(parent.getFont());
    	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    	gd.horizontalSpan = hspan;
    	t.setLayoutData(gd);
    	return t;
    }
	
	/**
	 * Creates a new text widget 
	 * @param parent the parent composite to add this text widget to
	 * @param style the style bits for the text widget
	 * @param hspan the horizontal span to take up on the parent composite
	 * @param fill the fill for the grid layout
	 * @return the new text widget
	 * @since 3.3
	 */
	public static Text createText(Composite parent, int style, int hspan, int fill) {
    	Text t = new Text(parent, style);
    	t.setFont(parent.getFont());
    	GridData gd = new GridData(fill);
    	gd.horizontalSpan = hspan;
    	t.setLayoutData(gd);
    	return t;
    }
	
	/**
	 * Creates a new text widget 
	 * @param parent the parent composite to add this text widget to
	 * @param style the style bits for the text widget
	 * @param hspan the horizontal span to take up on the parent composite
	 * @return the new text widget
	 * @since 3.3
	 */
	public static Text createText(Composite parent, int style, int hspan) {
    	Text t = new Text(parent, style);
    	t.setFont(parent.getFont());
    	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    	gd.horizontalSpan = hspan;
    	t.setLayoutData(gd);
    	return t;
    }
	
	/**
	 * Creates a new text widget 
	 * @param parent the parent composite to add this text widget to
	 * @param style the style bits for the text widget
	 * @param hspan the horizontal span to take up on the parent composite
	 * @param width the desired width of the text widget
	 * @param height the desired height of the text widget
	 * @param fill the fill style for the widget
	 * @return the new text widget
	 * @since 3.3
	 */
	public static Text createText(Composite parent, int style, int hspan, int width, int height, int fill) {
    	Text t = new Text(parent, style);
    	t.setFont(parent.getFont());
    	GridData gd = new GridData(fill);
    	gd.horizontalSpan = hspan;
    	gd.widthHint = width;
    	gd.heightHint = height;
    	t.setLayoutData(gd);
    	return t;
    }
	
	/**
	 * Creates a Group widget
	 * @param parent the parent composite to add this group to
	 * @param text the text for the heading of the group
	 * @param columns the number of columns within the group
	 * @param hspan the horizontal span the group should take up on the parent
	 * @param fill the style for how this composite should fill into its parent
	 * @return the new group
	 * @since 3.2
	 * 
	 */
	public static Group createGroup(Composite parent, String text, int columns, int hspan, int fill) {
    	Group g = new Group(parent, SWT.NONE);
    	g.setLayout(new GridLayout(columns, false));
    	g.setText(text);
    	g.setFont(parent.getFont());
    	GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
    	g.setLayoutData(gd);
    	return g;
    }
	
	/**
	 * Creates a Composite widget
	 * @param parent the parent composite to add this composite to
	 * @param columns the number of columns within the composite
	 * @param hspan the horizontal span the composite should take up on the parent
	 * @param fill the style for how this composite should fill into its parent
	 * @return the new group
	 * @since 3.3
	 */
	public static Composite createComposite(Composite parent, Font font, int columns, int hspan, int fill) {
    	Composite g = new Composite(parent, SWT.NONE);
    	g.setLayout(new GridLayout(columns, false));
    	g.setFont(font);
    	GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
    	g.setLayoutData(gd);
    	return g;
    }
	
	/**
	 * Creates a composite that uses the parents' font and has a gridlayout
	 * @param parent the parent to add the composite to
	 * @param columns the number of columns the composite should have
	 * @param hspan the horizontal span the new composite should take up in the parent
	 * @param fill the fill style of the composite {@link GridData}
	 * @return a new composite with a grid layout
	 * 
	 * @since 3.3
	 */
	public static Composite createComposite(Composite parent, int columns, int hspan, int fill) {
		Composite g = new Composite(parent, SWT.NONE);
    	g.setLayout(new GridLayout(columns, false));
    	g.setFont(parent.getFont());
    	GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
    	g.setLayoutData(gd);
    	return g;
	}
	
	/**
	 * Creates a vertical spacer for seperating components. If applied to a 
	 * <code>GridLayout</code>, this method will automatically span all of the columns of the parent
	 * to make vertical space
	 * 
	 * @param parent the parent composite to add this spacer to
	 * @param numlines the number of vertical lines to make as space
	 * @since 3.3
	 */
	public static void createVerticalSpacer(Composite parent, int numlines) {
		Label lbl = new Label(parent, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		Layout layout = parent.getLayout();
		if(layout instanceof GridLayout) {
			gd.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
		}
		gd.heightHint = numlines;
		lbl.setLayoutData(gd);
	}
	
	/**
	 * creates a horizontal spacer for seperating components
	 * @param comp
	 * @param numlines
	 * @since 3.3
	 */
	public static void createHorizontalSpacer(Composite comp, int numlines) {
		Label lbl = new Label(comp, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numlines;
		lbl.setLayoutData(gd);
	}
	
	/**
	 * Creates a Composite widget
	 * @param parent the parent composite to add this composite to
	 * @param columns the number of columns within the composite
	 * @param hspan the horizontal span the composite should take up on the parent
	 * @param fill the style for how this composite should fill into its parent
	 * @param marginwidth the width of the margin to place around the composite (default is 5, specified by GridLayout)
	 * @return the new group
	 * @since 3.3
	 */
	public static Composite createComposite(Composite parent, Font font, int columns, int hspan, int fill, int marginwidth, int marginheight) {
		Composite g = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(columns, false);
		layout.marginWidth = marginwidth;
		layout.marginHeight = marginheight;
    	g.setLayout(layout);
    	g.setFont(font);
    	GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
    	g.setLayoutData(gd);
    	return g;
	}
	
	/**
	 * This method is used to make a combo box
	 * @param parent the parent composite to add the new combo to
	 * @param style the style for the Combo
	 * @param hspan the horizontal span to take up on the parent composite
	 * @param fill how the combo will fill into the composite
	 * Can be one of <code>GridData.FILL_HORIZONAL</code>, <code>GridData.FILL_BOTH</code> or <code>GridData.FILL_VERTICAL</code>
	 * @param items the item to put into the combo
	 * @return a new Combo instance
	 * @since 3.3
	 */
	public static Combo createCombo(Composite parent, int style, int hspan, int fill, String[] items) {
		Combo c = new Combo(parent, style);
		c.setFont(parent.getFont());
		GridData gd = new GridData(fill);
		gd.horizontalSpan = hspan;
		c.setLayoutData(gd);
		c.setItems(items);
		c.select(0);
		return c;
	}
	
	/**
	 * This method is used to make a combo box with a default fill style of GridData.FILL_HORIZONTAL
	 * @param parent the parent composite to add the new combo to
	 * @param style the style for the Combo
	 * @param hspan the horizontal span to take up on the parent composite
	 * @param items the item to put into the combo
	 * @return a new Combo instance
	 * @since 3.3
	 */
	public static Combo createCombo(Composite parent, int style, int hspan, String[] items) {
		Combo c = new Combo(parent, style);
		c.setFont(parent.getFont());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = hspan;
		c.setLayoutData(gd);
		c.setItems(items);
		c.select(0);
		return c;
	}
	
	/**
	 * This method allows us to open the preference dialog on the specific page, in this case the perspective page
	 * @param id the id of pref page to show
	 * @param page the actual page to show
	 * @since 3.2
	 */
	public static void showPreferencePage(String id) {
		PreferencesUtil.createPreferenceDialogOn(DebugUIPlugin.getShell(), id, new String[] {id}, null).open();
	}
}
