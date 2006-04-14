/*******************************************************************************
 * Copyright (c) 2006 The Pampered Chef and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     The Pampered Chef - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable.day.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

/**
 * Represents a graphical item inside a multi-day event editor.
 * 
 * @since 3.2
 */
public class CalendarableEventControl extends Canvas  {

	private static final int MARGIN = 3;
	private Label label = null;
	private Color BORDER_COLOR;
	private Color BACKGROUND_COLOR;

   /**
	 * Constructs a new instance of this class given its parent
	 * and a style value describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in
	 * class <code>SWT</code> which is applicable to instances of this
	 * class, or must be built by <em>bitwise OR</em>'ing together 
	 * (that is, using the <code>int</code> "|" operator) two or more
	 * of those <code>SWT</code> style constants. The class description
	 * lists the style constants that are applicable to the class.
	 * Style bits are also inherited from superclasses.
	 * </p>
	 *
	 * @param parent a composite control which will be the parent of the new instance (cannot be null)
	 * @param style the style of control to construct
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
	 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
	 * </ul>
	 *
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public CalendarableEventControl(Composite parent, int style) {
		super(parent, style);
		Display display = parent.getDisplay();
		BORDER_COLOR = new Color(display, 215, 215, 245);
		BACKGROUND_COLOR = new Color(display, 240, 240, 255);
		initialize();
	}
	
	public void dispose() {
		super.dispose();
		BORDER_COLOR.dispose();
		BACKGROUND_COLOR.dispose();
	}

	/**
	 * Create the event control's layout
	 */
	private void initialize() {
		setBackground(BACKGROUND_COLOR);
        label = new Label(this, SWT.WRAP);
        label.setText("Label");
        label.setBackground(BACKGROUND_COLOR);
        FillLayout fillLayout = new FillLayout();
        fillLayout.marginHeight = MARGIN;
        fillLayout.marginWidth = MARGIN;
        this.setBackground(BORDER_COLOR);
        this.setLayout(fillLayout);
	}

	/**
	 * @param text
	 */
	public void setText(String text) {
		label.setText(text);
	}
		
} // @jve:decl-index=0:visual-constraint="10,10"
