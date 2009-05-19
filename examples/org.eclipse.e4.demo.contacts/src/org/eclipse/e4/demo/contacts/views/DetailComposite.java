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

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.demo.contacts.model.Contact;
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

	private final Text fullNameText;
	private final Text companyText;
	private final Text jobTitleText;
	private final Text emailText;
	private final Text webPageText;
	private final Text phoneText;
	private final Text mobileText;
	private final Text streetText;
	private final Text cityText;
	private final Text zipText;
	private final Text stateText;
	private final Text countryText;
	private final Text noteText;

	private final Color bgColor;
	private Label imageLabel;

	private Image dummyPortrait;
	private boolean generalGroup;

	public DetailComposite(final Composite parent, final int style,
			final boolean isEnabled, final ModifyListener modifyListener,
			final Contact contact) {

		super(parent, style);

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
		fullNameText = createText(composite, "Full Name:");
		companyText = createText(composite, "Company:");
		jobTitleText = createText(composite, "Job Title:");
		noteText = createText(composite, "Note:");

		createVerticalSpace(composite);

		// Business Address
		createSeparator(composite, "Business Address ");
		streetText = createText(composite, "Street:");
		cityText = createText(composite, "City:");
		zipText = createText(composite, "ZIP:");
		stateText = createText(composite, "State/Prov:");
		countryText = createText(composite, "Country:");
		createVerticalSpace(composite);

		// Business Phone
		createSeparator(composite, "Business Phones ");
		phoneText = createText(composite, "Phone:");
		mobileText = createText(composite, "Mobile:");
		createVerticalSpace(composite);

		// Business Internet
		createSeparator(composite, "Business Internet");
		emailText = createText(composite, "Email:");
		webPageText = createText(composite, "Web Page:");
		createVerticalSpace(composite);
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

	private Text createText(final Composite parent, final String labelText) {
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
				imageLabel = new Label(parent, SWT.NONE);
				imageLabel.setImage(dummyPortrait);
				GridData gridData3 = new GridData();
				gridData3.verticalSpan = 4;
				imageLabel.setLayoutData(gridData3);
			}
		}
		text.setLayoutData(gridData2);
		return text;
	}

	private static String toOriginalOrEmpty(final String s) {
		return (s != null) ? s : "";
	}

	public void update(final Contact contact) {
		fullNameText.setText(toOriginalOrEmpty(contact.toString()));
		companyText.setText(toOriginalOrEmpty(contact.getCompany()));
		jobTitleText.setText(toOriginalOrEmpty(contact.getJobTitle()));
		noteText.setText(toOriginalOrEmpty(contact.getNote()));
		streetText.setText(toOriginalOrEmpty(contact.getStreet()));
		cityText.setText(toOriginalOrEmpty(contact.getCity()));
		zipText.setText(toOriginalOrEmpty(contact.getZip()));
		stateText.setText(toOriginalOrEmpty(contact.getState()));
		countryText.setText(toOriginalOrEmpty(contact.getCountry()));
		phoneText.setText(toOriginalOrEmpty(contact.getPhone()));
		mobileText.setText(toOriginalOrEmpty(contact.getMobile()));
		emailText
				.setText(DetailComposite.toOriginalOrEmpty(contact.getEmail()));
		webPageText.setText(DetailComposite.toOriginalOrEmpty(contact
				.getWebPage()));
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
