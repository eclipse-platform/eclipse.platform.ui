/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.views.properties.tabbed.override.items;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.tests.views.properties.tabbed.model.Information;

/**
 * An item for when the Information element is the selected element in the
 * override tests view.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class InformationItem implements IOverrideTestsItem {

	private Composite composite;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.tests.views.properties.tabbed.override.items.IOverrideTestsItem#createControls(org.eclipse.swt.widgets.Composite)
	 */
	public void createControls(Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		composite = toolkit.createComposite(parent);
		composite.setLayout(new FillLayout());

		ScrolledForm form = toolkit.createScrolledForm(composite);
		form.getBody().setLayout(new TableWrapLayout());

		Section section = toolkit.createSection(form.getBody(),
				Section.DESCRIPTION);
		TableWrapData td = new TableWrapData(TableWrapData.FILL,
				TableWrapData.TOP);
		td.grabHorizontal = true;
		section.setLayoutData(td);
		section.setText(getText() + " Properties"); //$NON-NLS-1$
		toolkit.createCompositeSeparator(section);
		section.setDescription("Set the properties of the selected " + //$NON-NLS-1$
				getText() + " element.");//$NON-NLS-1$

		Composite sectionClient = toolkit.createComposite(section);
		FormLayout layout = new FormLayout();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.spacing = 2;
		sectionClient.setLayout(layout);
		section.setClient(sectionClient);
		toolkit.paintBordersFor(sectionClient);

		Button radioLeft = toolkit.createButton(sectionClient, "Choice 1",//$NON-NLS-1$
				SWT.RADIO);
		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(0, 5);
		radioLeft.setLayoutData(data);

		Button radioRight = toolkit.createButton(sectionClient, "Choice 2",//$NON-NLS-1$
				SWT.RADIO);
		data = new FormData();
		data.left = new FormAttachment(radioLeft, 5);
		data.top = new FormAttachment(0, 5);
		radioRight.setLayoutData(data);

		Button radioRight2 = toolkit.createButton(sectionClient, "Choice 3",//$NON-NLS-1$
				SWT.RADIO);
		data = new FormData();
		data.left = new FormAttachment(radioRight, 5);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 5);
		radioRight2.setLayoutData(data);

		Button flag = toolkit.createButton(sectionClient,
				"Value of the flag property", SWT.CHECK);//$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(radioLeft, 5);
		flag.setLayoutData(data);

		Label nameLabel = toolkit.createLabel(sectionClient, "Text Property:"); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.top = new FormAttachment(flag, 5);
		nameLabel.setLayoutData(data);

		Text nameText = toolkit.createText(sectionClient, ""); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(nameLabel, 5);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(flag, 5);
		nameText.setLayoutData(data);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.tests.views.properties.tabbed.override.items.IOverrideTestsItem#dispose()
	 */
	public void dispose() {
		if (composite != null && !composite.isDisposed()) {
			composite.dispose();
			composite = null;
		}
	}

	public Composite getComposite() {
		return composite;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.tests.views.properties.tabbed.override.items.IOverrideTestsItem#getElement()
	 */
	public Class getElement() {
		return Information.class;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.tests.views.properties.tabbed.override.items.IOverrideTestsItem#getImage()
	 */
	public Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJS_INFO_TSK);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.tests.views.properties.tabbed.override.items.IOverrideTestsItem#getText()
	 */
	public String getText() {
		return "Information"; //$NON-NLS-1$
	}
}
