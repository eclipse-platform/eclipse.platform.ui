/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties.tabbed.internal.view;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;


/**
 * The title in the tabbed property sheet page.
 * 
 * @author Anthony Hunter
 */
public class TabbedPropertyTitle
	extends Composite {

	private CLabel label;

	private Image image = null;

	private String text = null;
	
	private static final String BLANK = ""; //$NON-NLS-1$

	/**
	 * Width of the margin that will be added around the control.
	 */
	public int marginWidth = 4;

	/**
	 * Height of the margin that will be added around the control.
	 */
	public int marginHeight = 4;

	private TabbedPropertySheetWidgetFactory factory;

	/**
	 * Constructor for TabbedPropertyTitle.
	 * 
	 * @param parent
	 *            the parent composite.
	 * @param factory
	 *            the widget factory for the tabbed property sheet
	 */
	public TabbedPropertyTitle(Composite parent,
			TabbedPropertySheetWidgetFactory factory) {
		super(parent, SWT.NO_FOCUS);
		this.factory = factory;

		this.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				if (image == null && (text == null || text.equals(BLANK))) {
					label.setVisible(false);
				} else {
					label.setVisible(true);
					drawTitleBackground(e);
				}
			}
		});

		factory.getColors().initializeSectionToolBarColors();
		setBackground(factory.getColors().getBackground());
		setForeground(factory.getColors().getForeground());

		FormLayout layout = new FormLayout();
		layout.marginWidth = ITabbedPropertyConstants.HSPACE + 6;
		layout.marginHeight = 5;
		setLayout(layout);

		label = factory.createCLabel(this, BLANK);
		label.setBackground(new Color[] {
			factory.getColors().getColor(FormColors.TB_BG),
			factory.getColors().getColor(FormColors.TB_GBG)}, new int[] {100},
			true);
		label.setFont(JFaceResources.getBannerFont());
		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.bottom = new FormAttachment(100, 0);
		label.setLayoutData(data);

		/*
		 * setImage(PlatformUI.getWorkbench().getSharedImages().getImage(
		 * ISharedImages.IMG_OBJ_ELEMENT));
		 */
	}

	/**
	 * @param e
	 */
	protected void drawTitleBackground(PaintEvent e) {
		Color bg = factory.getColors().getColor(FormColors.TB_BG);
		Color gbg = factory.getColors().getColor(FormColors.TB_GBG);
		Color border = factory.getColors().getColor(FormColors.TB_BORDER);
		Rectangle bounds = getClientArea();
		Point tsize = null;
		Point labelSize = null;
		int twidth = bounds.width - marginWidth - marginWidth;
		if (label != null)
			labelSize = label.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		if (labelSize != null)
			twidth -= labelSize.x + 4;
		int tvmargin = 4;
		int theight = getHeight();
		if (tsize != null)
			theight += Math.max(theight, tsize.y);
		if (labelSize != null)
			theight = Math.max(theight, labelSize.y);
		theight += tvmargin + tvmargin;
		int midpoint = (theight * 66) / 100;
		int rem = theight - midpoint;
		GC gc = e.gc;
		gc.setForeground(bg);
		gc.setBackground(gbg);
		gc.fillGradientRectangle(marginWidth, marginHeight, bounds.width - 1
			- marginWidth - marginWidth, midpoint - 1, true);
		gc.setForeground(gbg);
		gc.setBackground(getBackground());
		gc.fillGradientRectangle(marginWidth, marginHeight + midpoint - 1,
			bounds.width - 1 - marginWidth - marginWidth, rem - 1, true);
		gc.setForeground(border);
		gc.drawLine(marginWidth, marginHeight + 2, marginWidth, marginHeight
			+ theight - 1);
		gc.drawLine(marginWidth, marginHeight + 2, marginWidth + 2,
			marginHeight);
		gc.drawLine(marginWidth + 2, marginHeight, bounds.width - marginWidth
			- 3, marginHeight);
		gc.drawLine(bounds.width - marginWidth - 3, marginHeight, bounds.width
			- marginWidth - 1, marginHeight + 2);
		gc.drawLine(bounds.width - marginWidth - 1, marginHeight + 2,
			bounds.width - marginWidth - 1, marginHeight + theight - 1);
	}

	/**
	 * Set the text label.
	 * 
	 * @param text
	 *            the text label.
	 */
	public void setTitle(String text, Image image) {
		this.text = text;
		this.image = image;
		if (text != null) {
			label.setText(text);
		} else {
			label.setText(BLANK); 
		}
		label.setImage(image);
		redraw();
	}

	/**
	 * @return the height of the title.
	 */
	public int getHeight() {
		Shell shell = new Shell();
		GC gc = new GC(shell);
		gc.setFont(getFont());
		Point point = gc.textExtent(BLANK);
		point.x++;
		int textOrImageHeight = Math.max(point.x, 16);
		gc.dispose();
		shell.dispose();
		return textOrImageHeight + 8;
	}
}
