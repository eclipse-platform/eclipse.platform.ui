/*******************************************************************************
 * Copyright (c) 2001, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.properties.tabbed;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A FormToolkit customized for use by tabbed property sheet page.
 * 
 * @author Anthony Hunter
 */
public class TabbedPropertySheetWidgetFactory
	extends FormToolkit {

	/**
	 * private constructor.
	 */
	public TabbedPropertySheetWidgetFactory() {
		super(Display.getCurrent());
	}

	public CTabFolder createTabFolder(Composite parent, int style) {
		CTabFolder tabFolder = new CTabFolder(parent, style);
		return tabFolder;
	}

	public CTabItem createTabItem(CTabFolder tabFolder, int style) {
		CTabItem tabItem = new CTabItem(tabFolder, style);
		return tabItem;
	}

	public List createList(Composite parent, int style) {
		List list = new org.eclipse.swt.widgets.List(
			parent, style);
		return list;
	}

	public Composite createComposite(Composite parent, int style) {
		Composite c = super.createComposite(parent, style);
		paintBordersFor(c);
		return c;
	}

	public Composite createComposite(Composite parent) {
		Composite c = createComposite(parent, SWT.NONE);
		return c;
	}

	public Composite createPlainComposite(Composite parent, int style) {
		Composite c = super.createComposite(parent, style);
		c.setBackground(parent.getBackground());
		paintBordersFor(c);
		return c;
	}

	public ScrolledComposite createScrolledComposite(Composite parent, int style) {
		ScrolledComposite scrolledComposite = new ScrolledComposite(parent,
			style);
		return scrolledComposite;
	}

	public CCombo createCCombo(Composite parent, int comboStyle) {
		CCombo combo = new CCombo(parent, comboStyle);
		adapt(combo, true, false);
		return combo;
	}

	public CCombo createCCombo(Composite parent) {
		return createCCombo(parent, SWT.FLAT | SWT.READ_ONLY);
	}

	public Group createGroup(Composite parent, String text) {
		Group group = new Group(parent, SWT.SHADOW_NONE);
		group.setText(text);
		group.setBackground(getColors().getBackground());
		group.setForeground(getColors().getForeground());
		return group;
	}

	public Composite createFlatFormComposite(Composite parent) {
		Composite composite = createComposite(parent);
		FormLayout layout = new FormLayout();
		layout.marginWidth = ITabbedPropertyConstants.HSPACE + 2;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE;
		layout.spacing = ITabbedPropertyConstants.VMARGIN + 1;
		composite.setLayout(layout);
		return composite;
	}

	public CLabel createCLabel(Composite parent, String text) {
		return createCLabel(parent, text, SWT.NONE);
	}

	public CLabel createCLabel(Composite parent, String text, int style) {
		final CLabel label = new CLabel(parent, style);
		label.setBackground(parent.getBackground());
		label.setText(text);
		return label;
	}
	
	/**
	 * @see org.eclipse.ui.forms.widgets.FormToolkit#dispose()
	 */
	public void dispose() {
		if (getColors() != null) {
			super.dispose();
		}
	}
}
