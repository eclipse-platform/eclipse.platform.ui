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

import org.eclipse.draw2d.geometry.Point;
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
 * The location section on the location tab.
 * 
 * @author Anthony Hunter 
 */
public class LocationSection extends AbstractSection {
	private Text xText;
	private Text yText;

	/**
	 * A helper to listen for events that indicate that a text
	 * field has been changed.
	 */
	private TextChangeHelper listener = new TextChangeHelper() {
		public void textChanged(Control control) {
			Point point = new Point();
			point.x = Integer.parseInt(xText.getText());
			point.y = Integer.parseInt(yText.getText());
			((LogicSubpart) getElement()).setLocation(point);
		}
	};

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySection#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		Composite composite =
			getWidgetFactory().createFlatFormComposite(parent);
		FormData data;

		xText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		xText.setLayoutData(data);

		CLabel xLabel = getWidgetFactory().createCLabel(composite, "X:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right =
			new FormAttachment(xText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(xText, 0, SWT.CENTER);
		xLabel.setLayoutData(data);

		yText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(xText, 0, SWT.LEFT);
		data.right = new FormAttachment(xText, 0, SWT.RIGHT);
		data.top =
			new FormAttachment(
				xText,
				ITabbedPropertyConstants.VSPACE,
				SWT.BOTTOM);
		yText.setLayoutData(data);

		CLabel yLabel = getWidgetFactory().createCLabel(composite, "Y:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right =
			new FormAttachment(yText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(yText, 0, SWT.CENTER);
		yLabel.setLayoutData(data);

		listener.startListeningForEnter(xText);
		listener.startListeningTo(xText);
		listener.startListeningForEnter(yText);
		listener.startListeningTo(yText);
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.view.ITabbedPropertySection#refresh()
	 */
	public void refresh() {
		Assert.isTrue(getElement() instanceof LogicSubpart);
		listener.startNonUserChange();
		try {
			Point point = ((LogicSubpart) getElement()).getLocation();
			xText.setText(Integer.toString(point.x));
			yText.setText(Integer.toString(point.y));
		} finally {
			listener.finishNonUserChange();
		}
	}
}
