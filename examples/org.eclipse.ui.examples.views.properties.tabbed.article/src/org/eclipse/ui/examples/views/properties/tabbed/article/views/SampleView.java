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

import java.util.ArrayList;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * Sample view for the example.
 *
 * @author Anthony Hunter
 */
public class SampleView
	extends ViewPart
	implements ITabbedPropertySheetPageContributor {

	private ListViewer viewer;

	private Group grp1;

	/**
	 * The constructor.
	 */
	public SampleView() {
		//
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		// create all the GUI controls
		// create two groups
		viewer = new ListViewer(parent, SWT.SINGLE);

		grp1 = new Group(parent, SWT.NONE);
		grp1.setText("Preview");//$NON-NLS-1$
		RowLayout rowLayout = new RowLayout();
		grp1.setLayout(rowLayout);

		Button btn = new Button(grp1, SWT.PUSH);
		btn.setText("Hello");//$NON-NLS-1$

		// fill in the element
		ArrayList<ButtonElement> ctlList = new ArrayList<>();
		ButtonElement btnEl = new ButtonElement(btn, "Button");//$NON-NLS-1$
		ctlList.add(btnEl);

		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setInput(ctlList);
		getSite().setSelectionProvider(viewer);

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IPropertySheetPage.class)
			return adapter.cast(new TabbedPropertySheetPage(this));
		return super.getAdapter(adapter);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public String getContributorId() {
		return getSite().getId();
	}

}