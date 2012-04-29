/*******************************************************************************
 * Copyright (c) 2009, 2012 Siemens AG and others.
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
import javax.inject.Inject;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.demo.contacts.databinding.AggregateNameObservableValue;
import org.eclipse.e4.demo.contacts.model.Contact;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

@Creatable
public class DetailComposite extends Composite {

	private final MDirtyable dirtyable;
	private Contact originalContact;
	private Contact clonedContact;
	private boolean commitChanges = false;

	private Label imageLabel;

	private ImageData dummyPortrait;
	private boolean generalGroup;

	private final DataBindingContext dbc;
	private final WritableValue contactValue = new WritableValue();
	private final IObservableValue scaledImage;

	@Inject
	public DetailComposite(MDirtyable dirtyable, final Composite parent) {
		super(parent, SWT.NONE);
		this.dirtyable = dirtyable;

		parent.getShell().setBackgroundMode(SWT.INHERIT_DEFAULT);

		dbc = new DataBindingContext();

		URL url = FileLocator.find(Platform
				.getBundle("org.eclipse.e4.demo.contacts"), new Path(
				"images/dummy.png"), null);
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
		if (imageDescriptor != null) {
			dummyPortrait = imageDescriptor.getImageData();
		}

		final GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 5;
		setLayout(layout);

		// General
		final Composite composite = createComposite(this);

		createSeparator(composite, "General");

		createText(composite, "Title:", "title");
		createText(composite, "Name:", "name"); // Leads to Aggregate
		// "firstName" "middleName"
		// "lastName"
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
		final IObservableValue imageObservableValue = PojoObservables
				.observeDetailValue(contactValue, "image", ImageData.class);

		this.scaledImage = new ComputedValue() {
			private Image currentImage;

			@Override
			protected Object calculate() {
				ImageData imageData = (ImageData) imageObservableValue.getValue();
				if (imageData == null) {
					imageData = dummyPortrait;
				}
				double ratio = imageData.height / 85.0;
				int width = (int) (imageData.width / ratio);
				int height = (int) (imageData.height / ratio);
				ImageData scaledImageData = imageData.scaledTo(width, height);
				if (currentImage != null) {
					currentImage.dispose();
					currentImage = null;
				}
				currentImage = new Image(Display.getCurrent(), scaledImageData);
				return currentImage;
			}

			@Override
			public void dispose() {
				if (currentImage != null) {
					currentImage.dispose();
					currentImage = null;
				}
				super.dispose();
			}

		};

		dbc.bindValue(SWTObservables.observeImage(imageLabel), scaledImage,
				new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);

		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				scaledImage.dispose();
			}
		});

		commitChanges = true;
	}

	private void setDirty(boolean dirty) {
		dirtyable.setDirty(dirty);
	}

	public boolean checkEmptyString(Object testString) {
		if (testString == null || !(testString instanceof String)
				|| ((String) testString).trim().length() == 0) {
			return false;
		}
		return true;
	}

	private void createSeparator(Composite parent, String text) {
		generalGroup = text.equals("General");

		final Label label = new Label(parent, SWT.NONE);
		label.setText(text + "     ");
		WidgetElement.setID(label, "SeparatorLabel");
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalSpan = 3;
		label.setLayoutData(gridData);

		// final Label separator = new Label(parent, SWT.SEPARATOR
		// | SWT.HORIZONTAL);
		// GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		// gridData.horizontalIndent = -100;
		// gridData.verticalIndent = 5;
		// gridData.horizontalSpan = 2;
		// separator.setLayoutData(gridData);
	}

	private void createVerticalSpace(Composite parent) {
		final Label label2 = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);

		label2.setVisible(false);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalSpan = 3;
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
		label.setText(labelText + "   "); // the extra space is due to a bug in
		// font formatting when using css
		// styling
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gridData.horizontalIndent = 20;
		label.setLayoutData(gridData);

		final Text text = new Text(parent, SWT.NONE);

		GridData gridData2 = new GridData(GridData.FILL_HORIZONTAL);
		gridData2.horizontalIndent = 0;
		if (!generalGroup) {
			gridData2.horizontalSpan = 2;
		} else {
			gridData2.horizontalSpan = 1;
			if (labelText.equals("Title:")) {
				// The label image is set with data binding
				imageLabel = new Label(parent, SWT.NONE);
				GridData gridData3 = new GridData();
				gridData3.verticalSpan = 5;
				imageLabel.setLayoutData(gridData3);
			}
		}
		text.setLayoutData(gridData2);

		if (property != null) {
			if (property.equals("name")) {
				dbc.bindValue(SWTObservables.observeText(text, SWT.Modify),
						new AggregateNameObservableValue(contactValue));
			} else {
				dbc.bindValue(SWTObservables.observeText(text, SWT.Modify),
						PojoObservables.observeDetailValue(contactValue,
								property, String.class));
			}
		}

		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (commitChanges) {
					setDirty(true);
				}
			}
		});

		return text;
	}

	public Contact getOriginalContact() {
		return originalContact;
	}

	public Contact getModifiedContact() {
		return clonedContact;
	}

	private void setTextEnabled(Composite composite, boolean enabled) {
		for (Control control : composite.getChildren()) {
			if (control instanceof Composite) {
				setTextEnabled((Composite) control, enabled);
			} else if (control instanceof Text) {
				control.setEnabled(enabled);
			}
		}
	}

	public void update(final Contact contact) {
		if (contact == null) {
			commitChanges = false;
			setTextEnabled(this, false);
			contactValue.setValue(null);
		} else {
			setTextEnabled(this, true);

			commitChanges = false;
			try {
				clonedContact = (Contact) contact.clone();
				originalContact = contact;
				contactValue.setValue(clonedContact);
				commitChanges = true;
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
