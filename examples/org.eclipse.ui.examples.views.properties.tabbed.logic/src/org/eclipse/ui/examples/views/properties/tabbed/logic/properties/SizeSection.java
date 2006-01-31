/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.logic.properties;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.examples.logicdesigner.model.LogicSubpart;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The size section on the size tab. TODO: Properties View for Aurora (Anthony
 * Hunter)
 * 
 * @author Anthony Hunter
 */
public class SizeSection
	extends AbstractSection {

	Text widthText;

	Text heightText;

	/**
	 * A helper to listen for events that indicate that a text field has been
	 * changed.
	 */
	private TextChangeHelper listener = new TextChangeHelper() {

		public void textChanged(Control control) {
			Dimension dimension = new Dimension();
			dimension.width = Integer.parseInt(widthText.getText());
			dimension.height = Integer.parseInt(heightText.getText());
			((LogicSubpart) getElement()).setSize(dimension);
		}
	};

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySection#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		Composite composite = getWidgetFactory()
			.createFlatFormComposite(parent);
		FormData data;

		widthText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		widthText.setLayoutData(data);

		CLabel widthLabel = getWidgetFactory()
			.createCLabel(composite, "Width:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(widthText,
			-ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(widthText, 0, SWT.CENTER);
		widthLabel.setLayoutData(data);

		heightText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(widthText, 0, SWT.LEFT);
		data.right = new FormAttachment(widthText, 0, SWT.RIGHT);
		data.top = new FormAttachment(widthText,
			ITabbedPropertyConstants.VSPACE, SWT.BOTTOM);
		heightText.setLayoutData(data);

		CLabel heightLabel = getWidgetFactory().createCLabel(composite,
			"Height:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(heightText,
			-ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(heightText, 0, SWT.CENTER);
		heightLabel.setLayoutData(data);

		listener.startListeningForEnter(heightText);
		listener.startListeningTo(heightText);
		listener.startListeningForEnter(widthText);
		listener.startListeningTo(widthText);
	}

	/*
	 * @see org.eclipse.ui.views.properties.tabbed.view.ITabbedPropertySection#refresh()
	 */
	public void refresh() {
		Assert.isTrue(getElement() instanceof LogicSubpart);
		listener.startNonUserChange();
		try {
			Dimension dimension = ((LogicSubpart) getElement()).getSize();
			widthText.setText(Integer.toString(dimension.width));
			heightText.setText(Integer.toString(dimension.height));
		} finally {
			listener.finishNonUserChange();
		}
	}
}