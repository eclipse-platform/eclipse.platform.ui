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
package org.eclipse.ui.examples.views.properties.tabbed.article.views;

import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
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
 * The Label section on the Button tab.
 * 
 * @author Anthony Hunter
 */
public class LabelSection
    extends AbstractPropertySection {

    private Text labelText;

    private ButtonElement buttonElement;

    private ModifyListener listener = new ModifyListener() {

        public void modifyText(ModifyEvent arg0) {
			ButtonElementProperties properties = (ButtonElementProperties) Adapters.adapt(buttonElement, IPropertySource.class);
			properties.setPropertyValue(ButtonElementProperties.PROPERTY_TEXT, labelText.getText());
        }
    };

    public void setInput(IWorkbenchPart part, ISelection selection) {
        super.setInput(part, selection);
        Assert.isTrue(selection instanceof IStructuredSelection);
        Object input = ((IStructuredSelection) selection).getFirstElement();
        Assert.isTrue(input instanceof ButtonElement);
        this.buttonElement = (ButtonElement) input;
    }

    public void createControls(Composite parent,
            TabbedPropertySheetPage aTabbedPropertySheetPage) {
        super.createControls(parent, aTabbedPropertySheetPage);
        Composite composite = getWidgetFactory()
            .createFlatFormComposite(parent);
        FormData data;

        labelText = getWidgetFactory().createText(composite, ""); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        labelText.setLayoutData(data);
        labelText.addModifyListener(listener);

        CLabel labelLabel = getWidgetFactory()
            .createCLabel(composite, "Label:"); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(labelText,
            -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(labelText, 0, SWT.CENTER);
        labelLabel.setLayoutData(data);
    }

    public void refresh() {
		labelText.removeModifyListener(listener);
		ButtonElementProperties properties = (ButtonElementProperties) Adapters.adapt(buttonElement, IPropertySource.class);
        labelText.setText(properties.strText);
        labelText.addModifyListener(listener);
    }
}
