/*******************************************************************************
 * Copyright (c) 2006, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.article.views;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The size section on the button tab.
 *
 * @author Anthony Hunter
 */
public class SizeSection
	extends AbstractPropertySection {

	private Text widthText;

	private Text heightText;

	private ButtonElement buttonElement;

	private final ModifyListener listener = arg0 -> {
		ButtonElementProperties properties = (ButtonElementProperties) Adapters.adapt(buttonElement, IPropertySource.class);
		SizePropertySource sizePropertySource = (SizePropertySource) properties
				.getPropertyValue(ButtonElementProperties.PROPERTY_SIZE);
		sizePropertySource.setPropertyValue(SizePropertySource.ID_HEIGHT,
			heightText.getText());
		sizePropertySource.setPropertyValue(SizePropertySource.ID_WIDTH,
			widthText.getText());
	};

	@Override
	public void setInput(IWorkbenchPart part, ISelection selection) {
		super.setInput(part, selection);
		Assert.isTrue(selection instanceof IStructuredSelection);
		Object input = ((IStructuredSelection) selection).getFirstElement();
		Assert.isTrue(input instanceof ButtonElement);
		this.buttonElement = (ButtonElement) input;
	}

	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		Composite composite = getWidgetFactory()
			.createFlatFormComposite(parent);
		FormData data;

		CLabel widthLabel = getWidgetFactory()
			.createCLabel(composite, "Width:"); //$NON-NLS-1$
		widthText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
		CLabel heightLabel = getWidgetFactory().createCLabel(composite,
			"Height:"); //$NON-NLS-1$
		heightText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$

		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(widthText,
			-ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(0, 0);
		widthLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
		data.right = new FormAttachment(heightLabel,
			-ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(0, 0);
		widthText.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(50, 0);
		// data.right = new FormAttachment(heightText,
		// -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(0, 0);
		heightLabel.setLayoutData(data);

		data = new FormData();
		data.left = new FormAttachment(heightLabel,
			ITabbedPropertyConstants.HSPACE);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		heightText.setLayoutData(data);

		heightText.addModifyListener(listener);
		widthText.addModifyListener(listener);
	}

	@Override
	public void refresh() {
		heightText.removeModifyListener(listener);
		widthText.removeModifyListener(listener);
		ButtonElementProperties properties = (ButtonElementProperties) Adapters.adapt(buttonElement, IPropertySource.class);
		widthText.setText(Integer.toString(properties.ptSize.x));
		heightText.setText(Integer.toString(properties.ptSize.y));
		heightText.addModifyListener(listener);
		widthText.addModifyListener(listener);
	}
}