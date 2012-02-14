/*******************************************************************************
 * Copyright (c) 2009, 2010 Siemens AG and others.
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

import org.eclipse.e4.ui.workbench.swt.modeling.EMenuService;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.e4.demo.contacts.model.Contact;
import org.eclipse.e4.demo.contacts.model.ContactsRepositoryFactory;
import org.eclipse.e4.ui.di.Focus;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
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
		contactsViewer.setComparator(new ContactViewerComparator());

		contactsViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						IStructuredSelection selection = (IStructuredSelection) event
								.getSelection();
						selectionService.setSelection(selection.getFirstElement());
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

		menuService.registerContextMenu(contactsViewer.getControl(), "contacts.popup");
		
		ObservableListContentProvider contentProvider = new ObservableListContentProvider();

		contactsViewer.setContentProvider(contentProvider);

		IObservableMap[] attributes = BeansObservables.observeMaps(
				contentProvider.getKnownElements(), Contact.class,
				new String[] { "firstName", "lastName" });
		contactsViewer.setLabelProvider(new ObservableMapLabelProvider(
				attributes));

		contactsViewer.setInput(ContactsRepositoryFactory.getContactsRepository().getAllContacts());

		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	@Focus
	void setFocus() {
		contactsViewer.getControl().setFocus();
	}
}
