/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/


package org.eclipse.ui.texteditor;


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * A form consisting of a title, a banner, and an info text. Banner and info text are
 * separated by a separator line. This form must be handled like an SWT widget.
 *
 * @since 2.0
 * @deprecated since 3.0. there is no replacement, use org.eclipse.ui.forms to define a component with a similar look and function.
 */
public class InfoForm {

	/** The form's root widget */
	private ScrolledComposite fScrolledComposite;
	/** The background color */
	private Color fBackgroundColor;
	/** The foreground color */
	private Color fForegroundColor;
	/** The separator's color */
	private Color fSeparatorColor;
	/** The form header */
	private Label fHeader;
	/** The form banner */
	private Label fBanner;
	/** The form text */
	private StyledText fText;
	/** The preference change listener */
	private IPropertyChangeListener fPropertyChangeListener;

	/**
	 * Creates a new info form.
	 * @param parent the parent composite
	 */
	public InfoForm(Composite parent) {

		Display display= parent.getDisplay();
		fBackgroundColor= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		fForegroundColor= display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		fSeparatorColor= new Color(display, 152, 170, 203);

		fPropertyChangeListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				handlePropertyChange(event);
			}
		};
		JFaceResources.getFontRegistry().addListener(fPropertyChangeListener);

		fScrolledComposite= new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		fScrolledComposite.setAlwaysShowScrollBars(false);
		fScrolledComposite.setExpandHorizontal(true);
		fScrolledComposite.setExpandVertical(true);
		fScrolledComposite.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				JFaceResources.getFontRegistry().removeListener(fPropertyChangeListener);
				fScrolledComposite= null;
				fSeparatorColor.dispose();
				fSeparatorColor= null;
				fHeader= null;
				fBanner= null;
				fText= null;
			}
		});

		Composite composite= createComposite(fScrolledComposite);
		composite.setLayout(new GridLayout());

		fHeader= createHeader(composite, null);
		createLabel(composite, null);
		createLabel(composite, null);

		fBanner= createBanner(composite, null);

		Composite separator= createCompositeSeparator(composite);
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint= 2;
		separator.setLayoutData(data);

		fText= createText(composite, null);
		createLabel(composite, null);

		fScrolledComposite.setContent(composite);
		fScrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		createActionControls(composite);
	}

	/**
	 * Hook method for creating an appropriate action control.
	 * @param parent the action control's parent control
	 */
	protected void createActionControls(Composite parent) {
	}

	/**
	 * Returns the control of this form.
	 * @return the root control of this form
	 */
	public Control getControl() {
		return fScrolledComposite;
	}

	/**
	 * Sets the header text of this info form.
	 * @param header the header text
	 */
	public void setHeaderText(String header) {
		fHeader.setText(header);
	}

	/**
	 * Sets the banner text of this info form.
	 * @param banner the banner text
	 */
	public void setBannerText(String banner) {
		fBanner.setText(banner);
	}

	/**
	 * Sets the info of this info form
	 * @param info the info text
	 */
	public void setInfo(String info) {
		fText.setText(info);
	}

    /**
	 * Handles the property change.
	 *
	 * @param event the property change event object describing which property changed and how
	 */
	protected void handlePropertyChange(PropertyChangeEvent event) {

		if (fHeader != null)
			fHeader.setFont(JFaceResources.getHeaderFont());

		if (fBanner != null)
			fBanner.setFont(JFaceResources.getBannerFont());

		Control control= fScrolledComposite.getContent();
		fScrolledComposite.setMinSize(control.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		fScrolledComposite.setContent(control);

		fScrolledComposite.layout(true);
		fScrolledComposite.redraw();
	}

	/*
	 * @see org.eclipse.update.ui.forms.internal.FormWidgetFactory#createComposite(Composite)
	 */
	private Composite createComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(fBackgroundColor);
		return composite;
	}

	/*
	 * @see org.eclipse.update.ui.forms.internal.FormWidgetFactory#createCompositeSeparator(Composite)
	 */
	private Composite createCompositeSeparator(Composite parent) {
		Composite composite = new Composite(parent, SWT.NO_FOCUS);
		composite.setBackground(fSeparatorColor);
		return composite;
	}

	/*
	 * @see org.eclipse.update.ui.forms.internal.FormWidgetFactory#createLabel(Composite, String)
	 */
	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(data);

		if (text != null)
			label.setText(text);
		label.setBackground(fBackgroundColor);
		label.setForeground(fForegroundColor);
		return label;
	}
	
	private StyledText createText(Composite parent, String text) {
		StyledText widget = new StyledText(parent, SWT.READ_ONLY | SWT.MULTI);
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		widget.setLayoutData(data);

		if (text != null)
			widget.setText(text);
		widget.setBackground(fBackgroundColor);
		widget.setForeground(fForegroundColor);
		widget.setCaret(null);
		return widget;
	}

	/*
	 * @see org.eclipse.update.ui.forms.internal.FormWidgetFactory#createHeader(Composite, String)
	 */
	private Label createHeader(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		label.setLayoutData(data);

		if (text != null)
			label.setText(text);
		label.setBackground(fBackgroundColor);
		label.setForeground(fForegroundColor);
		label.setFont(JFaceResources.getHeaderFont());
		return label;
	}

	/*
	 * @see org.eclipse.update.ui.forms.internal.FormWidgetFactory#createBanner(Composite, String)
	 */
	private Label createBanner(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		if (text != null)
			label.setText(text);
		label.setBackground(fBackgroundColor);
		label.setForeground(fForegroundColor);
		label.setFont(JFaceResources.getBannerFont());
		return label;
	}
}
