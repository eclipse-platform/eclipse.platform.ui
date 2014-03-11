/*******************************************************************************
 * Copyright (c) 2009, 2014 Siemens AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 *
 * Contributors:
 *     Kai Tödter - initial implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 427896
 ******************************************************************************/

package org.eclipse.e4.demo.contacts.views;

import javax.inject.Inject;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.e4.demo.contacts.model.Contact;
import org.eclipse.e4.demo.contacts.model.ContactsRepositoryFactory;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class ListView {

	private final TableViewer contactsViewer;

	@Inject
	private ESelectionService selectionService;

	@Inject
	public ListView(Composite parent, EMenuService menuService) {
		// Table composite (because of TableColumnLayout)
		final Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		final TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);

		// Table viewer
		contactsViewer = new TableViewer(tableComposite, SWT.FULL_SELECTION);
		contactsViewer.getTable().setHeaderVisible(true);
		// contactsViewer.getTable().setLinesVisible(true);
		contactsViewer.setComparator(new ViewerComparator() {
			@Override
			public int compare(final Viewer viewer, final Object obj1,
					final Object obj2) {

				if (obj1 instanceof Contact && obj2 instanceof Contact) {
					String lastName1 = ((Contact) obj1).getLastName();
					String lastName2 = ((Contact) obj2).getLastName();
					return lastName1.compareTo(lastName2);
				}
				throw new IllegalArgumentException(
						"Can only compare two Contacts.");

			}
		});

		contactsViewer
		.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				selectionService.setSelection(selection
						.getFirstElement());
			}
		});

		// First name column
		final TableViewerColumn firstNameColumn = new TableViewerColumn(
				contactsViewer, SWT.NONE);
		firstNameColumn.getColumn().setText("First Name");
		tableColumnLayout.setColumnData(firstNameColumn.getColumn(),
				new ColumnWeightData(40));

		// Last name column
		final TableViewerColumn lastNameColumn = new TableViewerColumn(
				contactsViewer, SWT.NONE);
		lastNameColumn.getColumn().setText("Last Name");
		tableColumnLayout.setColumnData(lastNameColumn.getColumn(),
				new ColumnWeightData(60));

		menuService.registerContextMenu(contactsViewer.getControl(),
				"contacts.popup");

		ObservableListContentProvider contentProvider = new ObservableListContentProvider();

		contactsViewer.setContentProvider(contentProvider);

		IObservableMap[] attributes = BeansObservables.observeMaps(
				contentProvider.getKnownElements(), Contact.class,
				new String[] { "firstName", "lastName" });
		contactsViewer.setLabelProvider(new ObservableMapLabelProvider(
				attributes));

		contactsViewer.setInput(ContactsRepositoryFactory
				.getContactsRepository().getAllContacts());

		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	@Focus
	void setFocus() {
		contactsViewer.getControl().setFocus();
	}
}
