/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.internal.cheatsheets.CheatSheetStopWatch;

public abstract class Page {
	protected Composite cheatSheetComposite;

	protected final static int HORZ_SCROLL_INCREMENT = 20;
	protected final static int VERT_SCROLL_INCREMENT = 20;

	//Colors
	protected Color backgroundColor;
	private Color[] colorArray;
	private final RGB bottomRGB = new RGB(255, 255, 255);
	private final RGB midRGB = new RGB(242, 252, 254);
	private final RGB topRGB = new RGB(232, 242, 254);

	protected FormToolkit toolkit;
	protected ScrolledForm form;

	public Page() {
	}

	public Control getControl() {
		return cheatSheetComposite;
	}

	public void createPart(Composite parent) {
		init(parent.getDisplay());

		cheatSheetComposite = new Composite(parent, SWT.NONE);
		cheatSheetComposite.setRedraw(false);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.numColumns = 1;
		cheatSheetComposite.setLayout(layout);
		cheatSheetComposite.setBackground(backgroundColor);
		cheatSheetComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		createTitleArea(cheatSheetComposite);
		createInfoArea(cheatSheetComposite);
		cheatSheetComposite.setRedraw(true);
	}
	
	/**
	 * Creates the main composite area of the view.
	 *
	 * @param parent the SWT parent for the title area composite
	 * @return the created info area composite
	 */
	protected void createInfoArea(Composite parent) {
		CheatSheetStopWatch.startStopWatch("Page.createInfoArea()"); //$NON-NLS-1$
		toolkit = new FormToolkit(parent.getDisplay());
		CheatSheetStopWatch.printLapTime("Page.createInfoArea()", "Time in Page.createInfoArea() after new FormToolkit(): "); //$NON-NLS-1$ //$NON-NLS-2$
		form = toolkit.createScrolledForm(parent);
		CheatSheetStopWatch.printLapTime("Page.createInfoArea()", "Time in Page.createInfoArea() after createScrolledForm(): "); //$NON-NLS-1$ //$NON-NLS-2$
		form.setLayoutData(new GridData(GridData.FILL_BOTH));
		CheatSheetStopWatch.printLapTime("Page.createInfoArea()", "Time in Page.createInfoArea() after setLayoutData(): "); //$NON-NLS-1$ //$NON-NLS-2$
		TableWrapLayout layout = new TableWrapLayout();
		CheatSheetStopWatch.printLapTime("Page.createInfoArea()", "Time in Page.createInfoArea() after new FormTableWrapLayout(): "); //$NON-NLS-1$ //$NON-NLS-2$
		layout.numColumns = 2;
		layout.verticalSpacing = 3;
		form.getBody().setLayout(layout);

		CheatSheetStopWatch.printLapTime("Page.createInfoArea()", "Time in Page.createInfoArea() end of method: "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Creates the cheatsheet's title areawhich will consists
	 * of a title and image.
	 *
	 * @param parent the SWT parent for the title area composite
	 */
	private void createTitleArea(Composite parent) {
		// Message label
		final CLabel messageLabel = new CLabel(parent, SWT.NONE);
		messageLabel.setBackground(colorArray, new int[] { 85, 100 }, true);
	
		messageLabel.setText(getTitle());
		messageLabel.setFont(JFaceResources.getHeaderFont());
		GridData ldata = new GridData(GridData.FILL_HORIZONTAL);
		ldata.grabExcessHorizontalSpace = true;
		messageLabel.setLayoutData(ldata);
	
		final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (JFaceResources.HEADER_FONT.equals(event.getProperty())) {
					messageLabel.setFont(JFaceResources.getHeaderFont());
				}
			}
		};
	
		messageLabel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				JFaceResources.getFontRegistry().removeListener(fontListener);
			}
		});
	
		JFaceResources.getFontRegistry().addListener(fontListener);
	
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		messageLabel.setLayoutData(gridData);
	}

	public void dispose() {
		for (int i = 0; i < colorArray.length; i++) {
			if (colorArray[i] != null)
				colorArray[i].dispose();
		}

		if (cheatSheetComposite != null)
			cheatSheetComposite.dispose();
		
		if(toolkit != null) {
			toolkit.dispose();
		}
	}
	
	protected void init(Display display) {
		// Get the background color for the cheatsheet controls				
		backgroundColor = JFaceColors.getBannerBackground(display);

		colorArray = new Color[] { new Color(display, topRGB), new Color(display, midRGB), new Color(display, bottomRGB)};
	}
	
	protected abstract String getTitle();
}
