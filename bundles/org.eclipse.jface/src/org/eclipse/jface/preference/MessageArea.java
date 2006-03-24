/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.preference;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

/**
 * Instances of this class provide a message area to display
 * a message and an associated image. 
 *
 * @since 3.2
 * 
 * This class is not intended to be extended by clients.
 * 
 */
class MessageArea extends Composite {
	
	private int BORDER_MARGIN = IDialogConstants.HORIZONTAL_SPACING/2;

	private DecoratedField messageField;

	private Composite container;
	

	/**
	 * Constructs a new MessageArea with an empty decorated field.
	 * Calls to <code>setText(String text)</code> and <code>setImage(Image image)</code> 
	 * are required in order to fill the message area. 
	 * 
	 * @param parent the parent composite
	 * @param style the SWT style bits
	 */
	public MessageArea(Composite parent, int style) {
		super(parent, style);
		container = new Composite(this, style);
		GridLayout glayout = new GridLayout();
		glayout.numColumns = 2;
		glayout.marginWidth = 0;
		glayout.marginHeight = 0;
		container.setLayout(glayout);

		messageField = new DecoratedField(container,SWT.READ_ONLY,new TextControlCreator());
		messageField.getLayoutControl().setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
       
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				onPaint(e);
			}
		});
		
		// sets the layout and size to account for the BORDER_MARGIN between
		// the border drawn around the container and the decorated field.
		setLayout(new Layout() {
			public void layout(Composite parent, boolean changed) {
				Rectangle carea = getClientArea();
				container.setBounds(carea.x + BORDER_MARGIN, carea.y + BORDER_MARGIN,
							carea.width - (2 * BORDER_MARGIN), 
							carea.height - (2 * BORDER_MARGIN));
			}

			public Point computeSize(Composite parent, int wHint,
					int hHint, boolean changed) {
				Point size;
				size = container.computeSize(wHint, hHint,
						changed);
				
				// size set to account for the BORDER_MARGIN on 
				// all sides of the decorated field
				size.x += 4;
				size.y += 4;
				return size;
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setBackground(org.eclipse.swt.graphics.Color)
	 */
	public void setBackground(Color bg) {
		super.setBackground(bg);
		messageField.getLayoutControl().setBackground(bg);
		messageField.getControl().setBackground(bg);
		container.setBackground(bg);
	}

	/**
	 * Sets the text in the decorated field which will be displayed
	 * in the message area.
	 * 
	 * @param text the text to be displayed in the message area
	 * 
	 * @see org.eclipse.swt.widgets.Text#setText(String string)
	 */
	public void setText(String text) {
		((Text) messageField.getControl()).setText(text);
	}

	/**
	 * Adds an image to decorated field to be shown in the message area.
	 * 
	 * @param image desired image to be shown in the MessageArea
	 */
	public void setImage(Image image) {
		FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
		registry.registerFieldDecoration("errorImage", "Error", image);  //$NON-NLS-1$//$NON-NLS-2$
		messageField.addFieldDecoration(registry.getFieldDecoration("errorImage"), //$NON-NLS-1$
										SWT.LEFT | SWT.TOP, false);
	}

	/**
	 *	Draws the message area composite with rounded corners.
	 */
	private void onPaint(PaintEvent e) {
		Rectangle carea = getClientArea();
		e.gc.setForeground(getForeground());
		
		// draws the polyline to be rounded in a 2 pixel squared area
		e.gc.drawPolyline(new int[] { carea.x, carea.y + carea.height - 1,
				carea.x, carea.y + 2, carea.x + 2, carea.y,
				carea.x + carea.width - 3, carea.y,
				carea.x + carea.width - 1, carea.y + 2,
				carea.x + carea.width - 1, carea.y + carea.height - 1 });
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setFont(org.eclipse.swt.graphics.Font)
	 */
	public void setFont(Font font) {
		super.setFont(font);
		((Text) messageField.getControl()).setFont(font);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Control#setToolTipText(java.lang.String)
	 */
	public void setToolTipText(String text){
		super.setToolTipText(text);
		((Text) messageField.getControl()).setToolTipText(text);
	}
}

