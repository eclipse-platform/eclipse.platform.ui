/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.internal.cheatsheets.CheatSheetStopWatch;

public abstract class Page {
	protected final static int HORZ_SCROLL_INCREMENT = 20;

	protected final static int VERT_SCROLL_INCREMENT = 20;

	// Colors
	protected Color backgroundColor;

	private Color[] colorArray;

	protected FormToolkit toolkit;

	protected ScrolledForm form;

	public Page() {
	}

	public Control getControl() {
		return form;
	}

	public void createPart(Composite parent) {
		init(parent.getDisplay());
		CheatSheetStopWatch.startStopWatch("Page.createInfoArea()"); //$NON-NLS-1$
		CheatSheetStopWatch
				.printLapTime(
						"Page.createInfoArea()", "Time in Page.createInfoArea() after new FormToolkit(): "); //$NON-NLS-1$ //$NON-NLS-2$
		form = toolkit.createScrolledForm(parent);
		form.setData("novarrows", Boolean.TRUE); //$NON-NLS-1$
		form.setText(getTitle());
		form.setDelayedReflow(true);
		CheatSheetStopWatch
				.printLapTime(
						"Page.createInfoArea()", "Time in Page.createInfoArea() after createScrolledForm(): "); //$NON-NLS-1$ //$NON-NLS-2$
		form.setLayoutData(new GridData(GridData.FILL_BOTH));
		CheatSheetStopWatch
				.printLapTime(
						"Page.createInfoArea()", "Time in Page.createInfoArea() after setLayoutData(): "); //$NON-NLS-1$ //$NON-NLS-2$
		TableWrapLayout layout = new TableWrapLayout();
		CheatSheetStopWatch
				.printLapTime(
						"Page.createInfoArea()", "Time in Page.createInfoArea() after new FormTableWrapLayout(): "); //$NON-NLS-1$ //$NON-NLS-2$
		layout.numColumns = 2;
		layout.verticalSpacing = 3;
		form.getBody().setLayout(layout);

		CheatSheetStopWatch
				.printLapTime(
						"Page.createInfoArea()", "Time in Page.createInfoArea() end of method: "); //$NON-NLS-1$ //$NON-NLS-2$		
	}

	/**
	 * Creates the cheatsheet's title areawhich will consists of a title and
	 * image.
	 * 
	 * @param parent
	 *            the SWT parent for the title area composite
	 */
	/*
	 * private void createTitleArea(Composite parent) { // Message label final
	 * CLabel messageLabel = new CLabel(parent, SWT.NONE);
	 * messageLabel.setBackground(colorArray, new int[] { 85, 100 }, true);
	 * 
	 * messageLabel.setText(getTitle());
	 * messageLabel.setFont(JFaceResources.getHeaderFont()); GridData ldata =
	 * new GridData(GridData.FILL_HORIZONTAL); ldata.grabExcessHorizontalSpace =
	 * true; messageLabel.setLayoutData(ldata);
	 * 
	 * final IPropertyChangeListener fontListener = new
	 * IPropertyChangeListener() { public void
	 * propertyChange(PropertyChangeEvent event) { if
	 * (JFaceResources.HEADER_FONT.equals(event.getProperty())) {
	 * messageLabel.setFont(JFaceResources.getHeaderFont()); } } };
	 * 
	 * messageLabel.addDisposeListener(new DisposeListener() { public void
	 * widgetDisposed(DisposeEvent event) {
	 * JFaceResources.getFontRegistry().removeListener(fontListener); } });
	 * 
	 * JFaceResources.getFontRegistry().addListener(fontListener);
	 * 
	 * GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
	 * messageLabel.setLayoutData(gridData); }
	 */

	public void dispose() {
		if (form != null) {
			form.dispose();
		}
		if (colorArray != null) {
			for (int i = 0; i < colorArray.length; i++) {
				if (colorArray[i] != null)
					colorArray[i].dispose();
			}
		}
		if (toolkit != null) {
			toolkit.dispose();
		}
		form = null;
		toolkit = null;
		colorArray = null;
	}

	protected void init(Display display) {
		toolkit = new FormToolkit(display);
		backgroundColor = toolkit.getColors().getBackground();
	}

	protected abstract String getTitle();

	public abstract void initialized();
	
}