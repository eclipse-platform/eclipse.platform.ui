/*******************************************************************************
 * Copyright (c) 2009 Siemens AG and others.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Kai Tödter - initial implementation
 ******************************************************************************/

package org.eclipse.e4.demo.contacts.views;

import java.net.URL;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.demo.contacts.model.Contact;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class DetailComposite extends Composite {

	private final Color bgColor;
	private Label imageLabel;

	private Image dummyPortrait;
	private boolean generalGroup;

	private final DataBindingContext dbc;
	private final WritableValue contactValue = new WritableValue();

	public DetailComposite(final Composite parent, final int style,
			final boolean isEnabled, final ModifyListener modifyListener,
			final Contact contact) {

		super(parent, style);
		dbc = new DataBindingContext();

		URL url = FileLocator.find(Platform
				.getBundle("org.eclipse.e4.demo.contacts"), new Path(
				"images/dummy.png"), null);
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
		if (imageDescriptor != null) {
			dummyPortrait = imageDescriptor.createImage();
		}

		final GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 5;
		setLayout(layout);
		bgColor = Display.getCurrent().getSystemColor(
				SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
		setBackground(bgColor);

		// Name
		final Composite composite = createComposite(this);
		composite.setBackground(bgColor);

		createSeparator(composite, "General");
		Text fullNameText = createText(composite, "Full Name:", null);
		dbc.bindValue(SWTObservables.observeText(fullNameText, SWT.Modify),
				new ComputedValue() {

					@Override
					protected Object calculate() {
						Object firstName = PojoObservables.observeDetailValue(
								contactValue, "firstName", String.class)
								.getValue();
						Object lastName = PojoObservables.observeDetailValue(
								contactValue, "lastName", String.class)
								.getValue();
						if (firstName != null && lastName != null) {
							return firstName + " " + lastName;
						} else {
							return "";
						}
					}
				});

		createText(composite, "Company:", "company");
		createText(composite, "Job Title:", "jobTitle");
		createText(composite, "Note:", "note");

		createVerticalSpace(composite);

		// Business Address
		createSeparator(composite, "Business Address ");
		createText(composite, "Street:", "street");
		createText(composite, "City:", "city");
		createText(composite, "ZIP:", "zip");
		createText(composite, "State/Prov:", "state");
		createText(composite, "Country:", "country");
		createVerticalSpace(composite);

		// Business Phone
		createSeparator(composite, "Business Phones ");
		createText(composite, "Phone:", "phone");
		createText(composite, "Mobile:", "mobile");
		createVerticalSpace(composite);

		// Business Internet
		createSeparator(composite, "Business Internet");
		createText(composite, "Email:", "email");
		createText(composite, "Web Page:", "webPage");
		createVerticalSpace(composite);

		// Bind the image

		// This has to be improved using data binding. Could'd not find out how
		// to deal with layouting the image so far

		// dbc.bindValue(SWTObservables.observeImage(imageLabel),
		// PojoObservables
		// .observeDetailValue(contactValue, "scaledImage", Image.class));

	}

	private void createSeparator(Composite parent, String text) {
		generalGroup = text.equals("General");

		final Label label = new Label(parent, SWT.NONE);
		label.setBackground(bgColor);
		label.setText(text + "     ");
		label.setForeground(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_BLUE));
		label.setData("org.eclipse.e4.ui.css.id", "SeparatorLabel");
		final Label separator = new Label(parent, SWT.SEPARATOR
				| SWT.HORIZONTAL);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalIndent = -100;
		gridData.verticalIndent = 5;
		gridData.horizontalSpan = 2;
		separator.setLayoutData(gridData);
	}

	private void createVerticalSpace(Composite parent) {
		final Label label2 = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);

		label2.setVisible(false);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalSpan = 4;
		label2.setLayoutData(gridData);
	}

	private static Composite createComposite(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		composite.setLayout(new GridLayout(3, false));
		return composite;
	}

	private Text createText(final Composite parent, final String labelText,
			final String property) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText(labelText);
		label.setBackground(bgColor);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalIndent = 20;
		label.setLayoutData(gridData);

		final Text text = new Text(parent, SWT.BORDER);
		// text.setEnabled(isEnabled);
		GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL);
		gridData2.horizontalIndent = 0;
		if (!generalGroup) {
			gridData2.horizontalSpan = 2;
		} else {
			gridData2.horizontalSpan = 1;
			if (labelText.equals("Full Name:")) {
				// This has to be improved using data binding. Could'd not find
				// out how
				// to deal with layouting the image so far

				imageLabel = new Label(parent, SWT.NONE);
				imageLabel.setImage(dummyPortrait);
				GridData gridData3 = new GridData();
				gridData3.verticalSpan = 4;
				imageLabel.setLayoutData(gridData3);
			}
		}
		text.setLayoutData(gridData2);

		if (property != null) {
			dbc.bindValue(SWTObservables.observeText(text, SWT.Modify),
					PojoObservables.observeDetailValue(contactValue, property,
							String.class));
		}

		return text;
	}

	public void update(final Contact contact) {
		contactValue.setValue(contact);
		// This has to be improved using data binding. Could'd not find out how
		// to deal with layouting the image so far
		if (contact.getScaledImage() == null) {
			imageLabel.setImage(dummyPortrait);
		} else {
			imageLabel.setImage(contact.getScaledImage());
		}
		imageLabel.pack();
		imageLabel.update();
		imageLabel.redraw();
	}
}
