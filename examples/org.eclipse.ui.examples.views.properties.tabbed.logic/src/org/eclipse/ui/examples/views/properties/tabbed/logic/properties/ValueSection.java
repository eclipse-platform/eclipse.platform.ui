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

import org.eclipse.gef.examples.logicdesigner.model.LED;
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
 * The value section on the element tab.
 * 
 * @author Anthony Hunter
 */
public class ValueSection extends AbstractSection {

	Text valueText;

	/**
	 * A helper to listen for events that indicate that a text
	 * field has been changed.
	 */
	private TextChangeHelper listener = new TextChangeHelper() {
		public void textChanged(Control control) {
			((LED) getElement()).setValue(
				Integer.parseInt(valueText.getText()));
		}
	};

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySection#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(
		Composite parent,
		TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		
		Composite composite =
			getWidgetFactory().createFlatFormComposite(parent);
		FormData data;

		valueText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		valueText.setLayoutData(data);

		CLabel valueLabel = getWidgetFactory().createCLabel(composite, "Value:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right =
			new FormAttachment(valueText, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(valueText, 0, SWT.CENTER);
		valueLabel.setLayoutData(data);

		listener.startListeningForEnter(valueText);
		listener.startListeningTo(valueText);
	}

	/*
	 * @see org.eclipse.ui.views.properties.tabbed.view.ITabbedPropertySection#refresh()
	 */
	public void refresh() {
		Assert.isTrue(getElement() instanceof LED);
		listener.startNonUserChange();
		try {
			valueText.setText(
				Integer.toString(((LED) getElement()).getValue()));
		} finally {
			listener.finishNonUserChange();
		}
	}
}