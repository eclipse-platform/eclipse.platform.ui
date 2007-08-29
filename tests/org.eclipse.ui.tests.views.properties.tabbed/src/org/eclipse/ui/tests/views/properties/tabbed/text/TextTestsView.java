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
package org.eclipse.ui.tests.views.properties.tabbed.text;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * A text view to test dynamic contributions to the tabbed properties view.
 * 
 * @author Anthony Hunter
 */
public class TextTestsView extends ViewPart implements
		ITabbedPropertySheetPageContributor {

	public static final String TEXT_TESTS_VIEW_ID = "org.eclipse.ui.tests.views.properties.tabbed.text.TextTestsView"; //$NON-NLS-1$

	private TabbedPropertySheetPage tabbedPropertySheetPage;

	private TextViewer viewer;

	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		FormLayout layout = new FormLayout();
		layout.marginWidth = ITabbedPropertyConstants.HSPACE;
		layout.marginHeight = ITabbedPropertyConstants.VSPACE;
		layout.spacing = ITabbedPropertyConstants.VMARGIN;
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Enter text, selected words becomes tabs and sections");
		FormData data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		label.setLayoutData(data);

		viewer = new TextViewer(composite, SWT.WRAP | SWT.H_SCROLL |
				SWT.V_SCROLL | SWT.BORDER);
		viewer.setDocument(new Document());
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(label, 0);
		data.bottom = new FormAttachment(100, 0);
		viewer.getControl().setLayoutData(data);

		getSite().setSelectionProvider(viewer);
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySheetPage.class) {
			if (tabbedPropertySheetPage == null) {
				tabbedPropertySheetPage = new TabbedPropertySheetPage(this);
			}
			return tabbedPropertySheetPage;
		}
		return super.getAdapter(adapter);
	}

	public String getContributorId() {
		return "org.eclipse.ui.tests.views.properties.tabbed.text"; //$NON-NLS-1$
	}

	public TabbedPropertySheetPage getTabbedPropertySheetPage() {
		return tabbedPropertySheetPage;
	}

	public TextViewer getViewer() {
		return viewer;
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

}