package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
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
 * A form consisting of a title, a banner, and a info text. Banner and info text are
 * separated by a separator line. This form must be handled like a SWT widget. 
 */
public class InfoForm {
				
	private ScrolledComposite fScrolledComposite;
	private Color fBackgroundColor;
	private Color fForegroundColor;
	private Color fSeparatorColor;
	
	private Label fHeader;
	private Label fBanner;
	private Label fText;
	
	private IPropertyChangeListener fPropertyChangeListener;
	
	/**
	 * Creates a new info form.
	 * @param parent the parent
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
					
		fText= createLabel(composite, null);
		createLabel(composite, null);
		
		fScrolledComposite.setContent(composite);
		fScrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		createActionControls(composite);
	}

	/**
	 * Hook method for creating an appropriate action control.
	 */
	protected void createActionControls(Composite parent) {
	}
	
	/**
	 * Returns the control of this form.
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

	/*
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
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

	// --- copied from org.eclipse.update.ui.forms.internal.FormWidgetFactory

	private Composite createComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(fBackgroundColor);
		return composite;
	}

	private Composite createCompositeSeparator(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setBackground(fSeparatorColor);
		return composite;
	}
		
	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		if (text != null)
			label.setText(text);
		label.setBackground(fBackgroundColor);
		label.setForeground(fForegroundColor);
		return label;
	}

	private Label createHeader(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		if (text != null)
			label.setText(text);
		label.setBackground(fBackgroundColor);
		label.setForeground(fForegroundColor);
		label.setFont(JFaceResources.getHeaderFont());
		return label;
	}

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