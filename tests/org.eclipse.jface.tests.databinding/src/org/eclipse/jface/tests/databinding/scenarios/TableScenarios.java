/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import org.eclipse.jface.databinding.Property;
import org.eclipse.jface.databinding.viewers.TableViewerDescription;
import org.eclipse.jface.tests.databinding.scenarios.model.Account;
import org.eclipse.jface.tests.databinding.scenarios.model.Catalog;
import org.eclipse.jface.tests.databinding.scenarios.model.PhoneConverter;
import org.eclipse.jface.tests.databinding.scenarios.model.SampleData;
import org.eclipse.jface.tests.databinding.scenarios.model.Signon;
import org.eclipse.jface.tests.databinding.scenarios.model.StateConverter;
import org.eclipse.jface.tests.databinding.scenarios.model.Transportation;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * To run the tests in this class, right-click and select "Run As JUnit Plug-in
 * Test". This will also start an Eclipse instance. To clean up the launch
 * configuration, open up its "Main" tab and select "[No Application] - Headless
 * Mode" as the application to run.
 */

public class TableScenarios extends ScenariosTestCase {

	private TableViewer tableViewer;

	private Catalog catalog;

	private TableColumn firstNameColumn;

	private TableColumn lastNameColumn;

	private TableColumn stateColumn;

	protected void setUp() throws Exception {
		super.setUp();
		getComposite().setLayout(new FillLayout());
		tableViewer = new TableViewer(getComposite());
		firstNameColumn = new TableColumn(tableViewer.getTable(), SWT.NONE);
		firstNameColumn.setWidth(50);
		lastNameColumn = new TableColumn(tableViewer.getTable(), SWT.NONE);
		lastNameColumn.setWidth(50);		
		stateColumn = new TableColumn(tableViewer.getTable(), SWT.NONE);
		stateColumn.setWidth(50);		

		catalog = SampleData.CATALOG_2005; // Lodging source
	}

	protected void tearDown() throws Exception {
		// do any teardown work here
		super.tearDown();
		tableViewer.getTable().dispose();
		tableViewer = null;
		firstNameColumn = null;
		lastNameColumn = null;
		stateColumn = null;
	}

	private String getValue(String text) {
		if (text==null)
			return "";
		return text;
	}
	
	public void testScenario01() {
		// Show that a TableViewer with three columns renders the accounts
		Account[] accounts = catalog.getAccounts();

		TableViewerDescription tableViewerDescription = new TableViewerDescription(
				tableViewer);
		tableViewerDescription.addColumn(0, "firstName");
		tableViewerDescription.addColumn(1, "lastName");
		tableViewerDescription.addColumn(2, "state");
		getDbc().bind(tableViewerDescription,
				new Property(catalog, "accounts"), null);

		// Verify the data in the table columns matches the accounts
		for (int i = 0; i < accounts.length; i++) {
			Account account = catalog.getAccounts()[i];
			String col_0 = ((ITableLabelProvider) tableViewer
					.getLabelProvider()).getColumnText(account, 0); 
			assertEquals(getValue(account.getFirstName()), col_0);
			String col_1 = ((ITableLabelProvider) tableViewer
					.getLabelProvider()).getColumnText(account, 1);
			assertEquals(getValue(account.getLastName()), col_1);
			String col_2 = ((ITableLabelProvider) tableViewer
					.getLabelProvider()).getColumnText(account, 2);
			assertEquals(getValue(account.getState()), col_2);

		}
	}

	public void testScenario02() throws SecurityException, IllegalArgumentException {
		// Show that a TableViewer with three columns can be used to update
		// columns
		Account[] accounts = catalog.getAccounts();

		TableViewerDescription tableViewerDescription = new TableViewerDescription(
				tableViewer);
		tableViewerDescription.addColumn("firstName");
		tableViewerDescription.addColumn("lastName", null,
				new PhoneConverter());
		tableViewerDescription.addColumn("state", null,
				new StateConverter());
		getDbc().bind(tableViewerDescription,
				new Property(catalog, "accounts"), null);

		Account account = accounts[0];
		// Select the first item in the table
		tableViewer.editElement(account, 0);
		// Set the text property of the cell editor which is now active over the "firstName" column
		CellEditor[] cellEditors = tableViewer.getCellEditors();
		TextCellEditor firstNameEditor = (TextCellEditor) cellEditors[0];
		// Change the firstName and test it goes to the model
		enterText((Text) firstNameEditor.getControl(), "Bill");
		// Check whether the model has changed
		assertEquals("Bill",account.getFirstName());
	}
	
	public void testScenario04() {
		// Show that when an item is added to a collection the table gets an extra item
		Account[] accounts = catalog.getAccounts();	
		
		TableViewerDescription tableViewerDescription = new TableViewerDescription(
				tableViewer);
		tableViewerDescription.addColumn("firstName");
		tableViewerDescription.addColumn("lastName");
		tableViewerDescription.addColumn("state");
		getDbc().bind(tableViewerDescription,
				new Property(catalog, "accounts"), null);
		
		// Verify the number of accounts matches the number of items in the table
		assertEquals(tableViewer.getTable().getItemCount(),accounts.length);
		// Add a new account and verify that the number of items in the table increases
		Account newAccount = new Account();
		newAccount.setFirstName("Finbar");
		newAccount.setLastName("McGoo");
		newAccount.setLastName("NC");
		catalog.addAccount(newAccount);
		// The number of items should have gone up by one
		assertEquals(tableViewer.getTable().getItemCount(),accounts.length + 1);
		// The number of items should still match the number of accounts (i.e. test the model)
		assertEquals(tableViewer.getTable().getItemCount(),catalog.getAccounts().length);
		// Remove the account that was just added
		catalog.removeAccount(newAccount);
		// The number of items should match the original
		assertEquals(tableViewer.getTable().getItemCount(),accounts.length);
		// The number of items should still match the number of accounts (i.e. test the model is reset)
		assertEquals(tableViewer.getTable().getItemCount(),catalog.getAccounts().length);		
		
	}
		
	public void testScenario03() {
		// Show that converters work for table columns
		Account[] accounts = catalog.getAccounts();

		TableViewerDescription tableViewerDescription = new TableViewerDescription(
				tableViewer);
		tableViewerDescription.addColumn("lastName");
		tableViewerDescription.addColumn("phone", null,
				new PhoneConverter());
		tableViewerDescription.addColumn("state", null,
				new StateConverter());
		getDbc().bind(tableViewerDescription,
				new Property(catalog, "accounts"), null);

		// Verify that the data in the the table columns matches the expected
		// What we are looking for is that the phone numbers are converterted to
		// nnn-nnn-nnnn and that
		// the state letters are converted to state names
		// Verify the data in the table columns matches the accounts
		PhoneConverter phoneConverter = new PhoneConverter();
		StateConverter stateConverter = new StateConverter();
		for (int i = 0; i < accounts.length; i++) {
			Account account = catalog.getAccounts()[i];
			// Check the phone number
			String col_phone = ((ITableLabelProvider) tableViewer
					.getLabelProvider()).getColumnText(account, 1);
			assertEquals(getValue((String)phoneConverter
					.convertModelToTarget(account.getPhone())), col_phone);
			String col_state = ((ITableLabelProvider) tableViewer
					.getLabelProvider()).getColumnText(account, 2);
			assertEquals(getValue((String)stateConverter
					.convertModelToTarget(account.getState())), col_state);
		}
	}
	
	public void testScenario05() {
		// Show that when the model changes then the UI refreshes to reflect this

		TableViewerDescription tableViewerDescription = new TableViewerDescription(
				tableViewer);
		tableViewerDescription.addColumn("lastName");
		tableViewerDescription.addColumn("phone", null,
				new PhoneConverter());
		tableViewerDescription.addColumn("state", null,
				new StateConverter());
		getDbc().bind(tableViewerDescription,
				new Property(catalog, "accounts"), null);
		
		Account account = catalog.getAccounts()[0];
		String lastName = tableViewer.getTable().getItem(0).getText(0);
		// Check the firstName in the TableItem is the same as the model
		assertEquals(lastName,account.getLastName());
		// Now change the model and check again
		account.setLastName("Gershwin");
		lastName = tableViewer.getTable().getItem(0).getText(0);	
		assertEquals(lastName,account.getLastName());		
		
	}
	
	public void testScenario06(){
		// Check that defaulting of converters, validators and cell editors work based on the explicit type of the column being specified
		TableViewerDescription tableViewerDescription = new TableViewerDescription(
				tableViewer);
		tableViewerDescription.addColumn("price");
		tableViewerDescription.getColumn(0).setPropertyType(Double.TYPE);
		getDbc().bind(tableViewerDescription,
				new Property(catalog, "transporations"), null);
		Transportation transporation = catalog.getTransporations()[0];
		tableViewer.editElement(transporation, 0);
		// Set the text property of the cell editor which is now active over the "firstName" column
		CellEditor[] cellEditors = tableViewer.getCellEditors();
		TextCellEditor priceEditor = (TextCellEditor) cellEditors[0];
		// Change the firstName and test it goes to the model
		enterText((Text) priceEditor.getControl(), "123.45");
		// Verify the model is updated
		assertEquals(transporation.getPrice(),123.45,0);
		
	}
	
	public void testScenario07(){
		// Verify that even when a column's property type is not set, that it is worked out lazily from the target type 
		TableViewerDescription tableViewerDescription = new TableViewerDescription(
				tableViewer);
		tableViewerDescription.addColumn("price");
		// The column's type is not set to be Double.TYPE.  This will be inferred once the first Transportation object is set
		// into the UpdatableCollection
		getDbc().bind(tableViewerDescription,
				new Property(catalog, "transporations"), null);
		Transportation transporation = catalog.getTransporations()[0];
		tableViewer.editElement(transporation, 0);
		// Set the text property of the cell editor which is now active over the "firstName" column
		CellEditor[] cellEditors = tableViewer.getCellEditors();
		TextCellEditor priceEditor = (TextCellEditor) cellEditors[0];
		// Change the firstName and test it goes to the model
		enterText((Text) priceEditor.getControl(), "123.45");
		// Verify the model is updated
		assertEquals(transporation.getPrice(),123.45,0);
		
	}
	
	public void testScenario08(){
		// Verify that binding to a Collection property (rather than an array) works
		TableViewerDescription tableViewerDescription = new TableViewerDescription(
				tableViewer);
		tableViewerDescription.addColumn("userId");
		tableViewerDescription.addColumn("password");	
		getDbc().bind(tableViewerDescription,
				new Property(catalog, "signons"), null);	
		Signon firstSignon = (Signon) catalog.getSignons().get(0);	
		// Verify the UI matches the model
		TableItem firstTableItem = tableViewer.getTable().getItem(0);
		assertEquals(firstTableItem.getText(1),firstSignon.getPassword());
		// Change the model and ensure the UI refreshes
		firstSignon.setPassword("Eclipse123Rocks");
		assertEquals("Eclipse123Rocks",firstSignon.getPassword());		
		assertEquals(firstTableItem.getText(1),firstSignon.getPassword());
		// Change the GUI and ensure the model refreshes
		tableViewer.editElement(firstSignon, 1);
		CellEditor[] cellEditors = tableViewer.getCellEditors();
		TextCellEditor passwordEditor = (TextCellEditor) cellEditors[1];
		enterText((Text) passwordEditor.getControl(), "Cricket11Players");
		assertEquals("Cricket11Players",firstSignon.getPassword());
		
	}
	
}
