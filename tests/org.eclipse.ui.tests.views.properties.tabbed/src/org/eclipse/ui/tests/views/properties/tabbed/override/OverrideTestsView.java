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
package org.eclipse.ui.tests.views.properties.tabbed.override;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.tests.views.properties.tabbed.model.Element;
import org.eclipse.ui.tests.views.properties.tabbed.override.folders.OverrideTestsTabFolderPropertySheetPage;
import org.eclipse.ui.tests.views.properties.tabbed.views.TestsPerspective;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * The override tests view.
 *
 * @author Anthony Hunter
 * @since 3.4
 */
public class OverrideTestsView extends ViewPart implements
		ITabbedPropertySheetPageContributor, ISelectionChangedListener {

	public static final String OVERRIDE_TESTS_VIEW_ID = "org.eclipse.ui.tests.views.properties.tabbed.override.OverrideTestsView"; //$NON-NLS-1$

	private OverrideTestsContentProvider overrideTestsContentProvider;

	private ArrayList selection = new ArrayList();

	private OverrideTestsSelectionProvider selectionProvider;

	private TabbedPropertySheetPage tabbedPropertySheetPage;

	private TableViewer viewer;

	@Override
	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL |
				SWT.V_SCROLL);
		overrideTestsContentProvider = new OverrideTestsContentProvider();
		viewer.setContentProvider(overrideTestsContentProvider);
		viewer.setLabelProvider(new OverrideTestsLabelProvider());
		viewer.setInput(getViewSite());
		selectionProvider = new OverrideTestsSelectionProvider(viewer);
		getSite().setSelectionProvider(selectionProvider);
		selection.add(viewer);
		viewer.addSelectionChangedListener(this);
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (IPropertySheetPage.class.equals(adapter)) {
			if (TestsPerspective.TESTS_PERSPECTIVE_ID.equals(getSite()
					.getWorkbenchWindow().getActivePage().getPerspective()
					.getId())) {
				if (tabbedPropertySheetPage == null) {
					tabbedPropertySheetPage = new TabbedPropertySheetPage(this);
				}
				return tabbedPropertySheetPage;
			}
			return new OverrideTestsTabFolderPropertySheetPage();
		}
		return super.getAdapter(adapter);
	}

	@Override
	public String getContributorId() {
		return "org.eclipse.ui.tests.views.properties.tabbed.override"; //$NON-NLS-1$
	}

	/**
	 * Get the currently selected element in the view.
	 *
	 * @return the currently selected element in the view.
	 */
	public ISelection getSelection() {
		return selectionProvider.getSelection();
	}

	/**
	 * Get the tabbed property sheet page for the view.
	 *
	 * @return the tabbed property sheet page for the view.
	 */
	public TabbedPropertySheetPage getTabbedPropertySheetPage() {
		return tabbedPropertySheetPage;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		SelectionChangedEvent newEvent = new SelectionChangedEvent(
				selectionProvider, selectionProvider.getSelection());
		selectionProvider.selectionChanged(newEvent);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * Set the selected element to be the same type as the provided class.
	 *
	 * @param elementClass
	 *            the provided class.
	 */
	public void setSelection(Class elementClass) {
		if (elementClass == null) {
			viewer.setSelection(StructuredSelection.EMPTY);
		}
		Object[] elements = overrideTestsContentProvider.getElements(null);
		for (int i = 0; i < elements.length; i++) {
			Element element = (Element) elements[i];
			Class clazz = element.getClass();
			if (elementClass == clazz) {
				StructuredSelection newSelection = new StructuredSelection(
						element);
				viewer.setSelection(newSelection);
				break;
			}
		}
	}
}