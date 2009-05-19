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

import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.demo.contacts.model.Contact;
import org.eclipse.e4.demo.contacts.model.ContactsRepositoryFactory;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class ListView implements IDisposable {

	private final TableViewer contactsViewer;

	public ListView(Composite parent, final IEclipseContext outputContext) {
		// Table composite (because of TableColumnLayout)
		final Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true));
		final TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);

		// Table viewer
		contactsViewer = new TableViewer(tableComposite, SWT.FULL_SELECTION);
		contactsViewer.getTable().setHeaderVisible(true);
		contactsViewer.getTable().setLinesVisible(true);
		contactsViewer.setContentProvider(new ArrayContentProvider());
		contactsViewer.setComparator(new ContactViewerComparator());

		contactsViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						StructuredSelection selection = (StructuredSelection) event
								.getSelection();
						outputContext.set(IServiceConstants.SELECTION,
								selection.size() == 1 ? selection
										.getFirstElement() : selection
										.toArray());
					}
				});

		// First name column
		final TableViewerColumn firstNameColumn = new TableViewerColumn(
				contactsViewer, SWT.NONE);
		firstNameColumn.getColumn().setText("First name");
		tableColumnLayout.setColumnData(firstNameColumn.getColumn(),
				new ColumnWeightData(40));
		firstNameColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(final Object element) {
				return ((Contact) element).getFirstName();
			}
		});

		// Last name column
		final TableViewerColumn lastNameColumn = new TableViewerColumn(
				contactsViewer, SWT.NONE);
		lastNameColumn.getColumn().setText("Last name");
		tableColumnLayout.setColumnData(lastNameColumn.getColumn(),
				new ColumnWeightData(60));
		lastNameColumn.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(final Object element) {
				return ((Contact) element).getLastName();
			}
		});
		contactsViewer.setInput(ContactsRepositoryFactory
				.getContactsRepository().getAllContacts());

		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}
}
