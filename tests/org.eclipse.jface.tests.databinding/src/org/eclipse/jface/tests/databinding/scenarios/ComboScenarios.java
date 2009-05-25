/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 116920, 160000
 *     Matthew Hall - bugs 260329, 260337
 *******************************************************************************/
package org.eclipse.jface.tests.databinding.scenarios;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.examples.databinding.model.Account;
import org.eclipse.jface.examples.databinding.model.Adventure;
import org.eclipse.jface.examples.databinding.model.Catalog;
import org.eclipse.jface.examples.databinding.model.Lodging;
import org.eclipse.jface.examples.databinding.model.SampleData;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;

public class ComboScenarios extends ScenariosTestCase {

	protected ComboViewer cviewer = null;

	protected Combo combo = null;

	protected Catalog catalog = null;

	ILabelProvider lodgingLabelProvider = new LabelProvider() {
		public String getText(Object element) {
			return ((Lodging) element).getName();
		}
	};

	ILabelProvider accountLabelProvider = new LabelProvider() {
		public String getText(Object element) {
			return ((Account) element).getCountry();
		}
	};

	protected void setUp() throws Exception {
		super.setUp();
		getComposite().setLayout(new FillLayout());

		combo = new Combo(getComposite(), SWT.READ_ONLY | SWT.DROP_DOWN);
		cviewer = new ComboViewer(combo);

		catalog = SampleData.CATALOG_2005; // Lodging source
	}

	protected void tearDown() throws Exception {
		combo.dispose();
		combo = null;
		cviewer = null;
		super.tearDown();
	}

	protected Object getViewerSelection() {
		return ((IStructuredSelection) cviewer.getSelection())
				.getFirstElement();
	}

	/**
	 * @return the ComboViewer's domain object list
	 */
	protected List getViewerContent(ComboViewer cviewer) {
		Object[] elements = ((IStructuredContentProvider) cviewer
				.getContentProvider()).getElements(null);
		if (elements != null)
			return Arrays.asList(elements);
		return null;
	}

	/**
	 * 
	 * @return the combo's items (String[]), which is the same thing as the
	 *         Viewer's labels
	 * 
	 */
	protected List getComboContent() {
		String[] elements = combo.getItems();
		if (elements != null)
			return Arrays.asList(elements);
		return null;
	}

	protected List getColumn(Object[] list, String feature) {
		List result = new ArrayList();
		if (list == null || list.length == 0)
			return result;
		String getterName = "get"
				+ feature.substring(0, 1).toUpperCase(Locale.ENGLISH)
				+ feature.substring(1);
		try {
			Method getter = list[0].getClass().getMethod(getterName,
					new Class[0]);
			try {
				for (int i = 0; i < list.length; i++) {
					result.add(getter.invoke(list[i], new Object[0]));
				}
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			}
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		return result;
	}

	/**
	 * This test case deal with the 3rd scenario, using vanilla bindings: Ensure
	 * a valid content and selection are bounded correctly Bind a collection of
	 * Lodgings to a ComboViewer Bind the ComboViewer's selection to the
	 * defaultLodging of an Adventure
	 * 
	 * This test does not deal with null values, empty content, changed content,
	 * property change of content elements, etc.
	 * 
	 */
	public void test_ROCombo_Scenario03_vanilla() {
		IObservableList lodgings = BeansObservables.observeList(Realm
				.getDefault(), catalog, "lodgings");
		ViewerSupport.bind(cviewer, lodgings, BeanProperties.value(
				Lodging.class, "name"));

		Adventure skiAdventure = SampleData.WINTER_HOLIDAY; // selection will

		// Ensure that cv's content now has the catalog's lodgings
		assertArrayEquals(catalog.getLodgings(), getViewerContent(cviewer)
				.toArray());

		// Ensure that the cv's labels are the same as the lodging descriptions
		assertEquals(getColumn(catalog.getLodgings(), "name"),
				getComboContent());

		getDbc().bindValue(ViewersObservables.observeSingleSelection(cviewer),
				BeansObservables.observeValue(skiAdventure, "defaultLodging"));

		// Check to see that the initial selection is the currentDefault Lodging
		assertEquals(getViewerSelection(), skiAdventure.getDefaultLodging());

		// Change the selection of the ComboViewer to all possible lodgings, and
		// verify that skiAdventure's default lodging was changed accordingly
		for (int i = 0; i < catalog.getLodgings().length; i++) {
			Object selection = catalog.getLodgings()[i];
			cviewer.setSelection(new StructuredSelection(selection));
			assertEquals(selection, skiAdventure.getDefaultLodging());
			assertEquals(getViewerSelection(), skiAdventure.getDefaultLodging());
		}

	}

	/**
	 * This test case deal with the 3rd scenario, and focuses on the collection
	 * binding to the combo. It will bind a collection, add/remove/change
	 * elements in the collection, and change element's properties to ensure
	 * that the combo's labels were updated appropriatly.
	 * 
	 * it also induce null values in properties, and elments.
	 * 
	 * This test does not deal with the combo's selection.
	 */
	public void test_ROCombo_Scenario03_collectionBindings() {
		// column binding
		// Bind the ComboViewer's content to the available lodging
		IObservableList lodgings = BeansObservables.observeList(Realm
				.getDefault(), catalog, "lodgings");
		ViewerSupport.bind(cviewer, lodgings, BeanProperties.value(
				Lodging.class, "name"));

		// Ensure that cv's content now has the catalog's lodgings
		assertArrayEquals(catalog.getLodgings(), getViewerContent(cviewer)
				.toArray());

		// Ensure that the cv's labels are the same as the lodging descriptions
		assertEquals(getColumn(catalog.getLodgings(), "name"),
				getComboContent());

		// Add a lodging in the middle (not supported by the model right now)
		// Lodging lodging = SampleData.FACTORY.createLodging();
		// lodging.setName("Middle Lodging");
		// catalog.addLodging(lodging);
		// assertEquals(getViewerContent(cviewer).get(2), lodging);

		// Add a lodging at the end
		Lodging lodging = SampleData.FACTORY.createLodging();
		lodging.setName("End Lodging");
		catalog.addLodging(lodging);
		int index = getComboContent().size() - 1;
		assertEquals(getViewerContent(cviewer).get(index), lodging);

		// Delete the first Lodging
		catalog.removeLodging(catalog.getLodgings()[0]);
		// Ensure that the cv's labels are the same as the lodging descriptions
		assertEquals(getColumn(catalog.getLodgings(), "name"),
				getComboContent());

		// Delete middle Lodging
		catalog.removeLodging(catalog.getLodgings()[2]);
		// Ensure that the cv's labels are the same as the lodging descriptions
		assertEquals(getColumn(catalog.getLodgings(), "name"),
				getComboContent());

		// Change the names of all Lodging
		for (int i = 0; i < catalog.getLodgings().length; i++) {
			Lodging l = catalog.getLodgings()[i];
			l.setName("Changed: " + l.getName());
		}
		spinEventLoop(0); // force Async. efforts
		assertEquals(getColumn(catalog.getLodgings(), "name"),
				getComboContent());

		// Set to null value
		Lodging l = catalog.getLodgings()[0];
		assertEquals(combo.getItem(0), l.getName());
		l.setName(null);
		assertEquals("", combo.getItem(0));

		// set to empty list
		while (catalog.getLodgings().length > 0) {
			catalog.removeLodging(catalog.getLodgings()[0]);
			assertEquals(getColumn(catalog.getLodgings(), "name"),
					getComboContent());
		}
	}

	/**
	 * This scenario tests a simple SWT combo with a set item list where the
	 * selection is bouded to a String property
	 */
	// public void test_ROCombo_Scenario01() {
	//
	// // Read-Only Combo will not change its text property on a call to
	// // setText()
	//
	// String[] items = new String[] { "FairyLand", "TuneLand", "NoWereLand",
	// "TinkerLand", "DreamLand" };
	// combo.setItems(items);
	// Account account = catalog.getAccounts()[0];
	//
	// // simple Combo's selection bound to the Account's country property
	// getDbc().bind(new Property(combo, SWTProperties.SELECTION),
	// new Property(account, "country"), null);
	//
	// // Drive the combo selection
	// int index = 3;
	// combo.setText(items[index]); // this should drive the selection
	// assertEquals(account.getCountry(), items[index]);
	//
	// // Set the country, and ensure selection is set property
	// index = 1;
	// account.setCountry(items[index]);
	// assertEquals(index, combo.getSelectionIndex());
	// assertEquals(combo.getText(), items[index]);
	//
	// index = combo.getSelectionIndex();
	// String txt = combo.getText();
	// // Set the country to something that is not in the Combo's list
	// account.setCountry("FooBar");
	// // Combo's selection will not Change
	// assertEquals(combo.getSelectionIndex(), index);
	// assertEquals(combo.getText(), txt);
	//
	// }
	/**
	 * This scenario tests a simple SWT combo that is bound to a list of Country
	 * objects. The Country object's name property is listed in the Combo.
	 * 
	 * The Combo's selection is bounded to the Country property of an Account.
	 */
	// public void test_ROCombo_Scenario02_SWTCombo() {
	//
	// // Create a list of Strings for the countries
	// IObservableList list = new WritableList();
	// for (int i = 0; i < catalog.getAccounts().length; i++)
	// list.add(catalog.getAccounts()[i].getCountry());
	//
	// // Bind the combo's content to that of the String based list
	// getDbc().bind(combo, list, null);
	// assertEquals(Arrays.asList(combo.getItems()), list);
	//
	// Account account = catalog.getAccounts()[0];
	//
	// // simple Combo's selection bound to the Account's country property
	// getDbc().bind(new Property(combo, SWTProperties.SELECTION),
	// new Property(account, "country"), null);
	//
	// // Drive the combo selection
	// String selection = (String) list.get(2);
	// combo.setText(selection); // this should drive the selection
	// assertEquals(account.getCountry(), selection);
	//
	// }
	/**
	 * This scenario tests a simple SWT combo that is bound to a list of Country
	 * objects. The Country object's name property is listed in the Combo.
	 * 
	 * The Combo's selection is bounded to the Country property of an Account.
	 */
	// public void test_ROCombo_Scenario02_ComboViewer() {
	//
	// // Account label provider will fill the combo with the country
	// cviewer.setLabelProvider(accountLabelProvider);
	// // Bind the ComboViewer's content to the available accounts
	// getDbc().bind(
	// cviewer,
	// new ListModelDescription(new Property(catalog, "accounts"),
	// "country"), null);
	//
	// // Ensure that cv's content now has the catalog's accounts
	// assertArrayEquals(catalog.getAccounts(), getViewerContent(cviewer)
	// .toArray());
	// // Ensure that the cv's labels are the same as the account countries
	// assertEquals(getColumn(catalog.getAccounts(), "country"),
	// getComboContent());
	//
	// Account account = SampleData.FACTORY.createAccount();
	//
	// // Use the Viewers visual Combo (Strings) to set the account's country
	// getDbc().bind(new Property(combo, SWTProperties.SELECTION),
	// new Property(account, "country"), null);
	//
	// // Change the selection of the ComboViewer to all possible accounts, and
	// // verify that the account's Country is being changed correctly.
	// for (int i = 0; i < catalog.getAccounts().length; i++) {
	// Account selection = catalog.getAccounts()[i];
	// cviewer.setSelection(new StructuredSelection(selection));
	// assertEquals(selection.getCountry(), account.getCountry());
	// }
	//
	// }
	/**
	 * This test ensure that multiple combos can be bound to the same deomain
	 * model
	 */
	public void test_ROCombo_multipleBindings() {
		Adventure skiAdventure = SampleData.WINTER_HOLIDAY; // for selection

		// Bind the ComboViewer's content to the available lodging
		IObservableList lodgings = BeansObservables.observeList(Realm
				.getDefault(), catalog, "lodgings");
		ViewerSupport.bind(cviewer, lodgings, BeanProperties.value(
				Lodging.class, "name"));

		// Ensure that cv's content now has the catalog's lodgings
		assertArrayEquals(catalog.getLodgings(), getViewerContent(cviewer)
				.toArray());

		// Ensure that the cv's labels are the same as the lodging descriptions
		assertEquals(getColumn(catalog.getLodgings(), "name"),
				getComboContent());

		ComboViewer otherViewer = new ComboViewer(getComposite(), SWT.NONE);
		ViewerSupport.bind(otherViewer, lodgings, BeanProperties.value(
				Lodging.class, "name"));

		// Ensure that cv's content now has the catalog's lodgings
		assertArrayEquals(catalog.getLodgings(), getViewerContent(otherViewer)
				.toArray());

		// Bind both selections to the same thing
		IObservableValue selection = ViewersObservables
				.observeSingleSelection(cviewer);
		getDbc().bindValue(selection,
				BeansObservables.observeValue(skiAdventure, "defaultLodging"));

		IObservableValue otherSelection = ViewersObservables
				.observeSingleSelection(otherViewer);
		getDbc().bindValue(otherSelection,
				BeansObservables.observeValue(skiAdventure, "defaultLodging"));

		Lodging lodging = catalog.getLodgings()[0];

		// Ensure that setting the selection is driven forward to the other
		// combo
		cviewer.setSelection(new StructuredSelection(lodging));
		assertEquals(((IStructuredSelection) cviewer.getSelection())
				.getFirstElement(), ((IStructuredSelection) otherViewer
				.getSelection()).getFirstElement());

		// Change the list of one combo, and ensure it updates the other combo
		catalog.removeLodging(lodging);
		assertEquals(getViewerContent(cviewer), getViewerContent(otherViewer));

	}

	/**
	 * This scenario tests a simple SWT CCombo that is bound to a list of
	 * Country objects. The Country object's name property is listed in the
	 * Combo.
	 * 
	 * The Combo's selection is bounded to the Country property of an Account.
	 */
	public void test_ROCombo_SWTCCombo() {

		// Create a list of Strings for the countries
		IObservableList list = new WritableList();
		for (int i = 0; i < catalog.getAccounts().length; i++)
			list.add(catalog.getAccounts()[i].getCountry());

		CCombo ccombo = new CCombo(getComposite(), SWT.READ_ONLY
				| SWT.DROP_DOWN);

		// Bind the combo's content to that of the String based list
		getDbc().bindList(SWTObservables.observeItems(ccombo), list);
		assertEquals(Arrays.asList(ccombo.getItems()), list);

		Account account = catalog.getAccounts()[0];

		// simple Combo's selection bound to the Account's country property
		IObservableValue comboSelection = SWTObservables
				.observeSelection(ccombo);
		getDbc().bindValue(comboSelection,
				BeansObservables.observeValue(account, "country"));

		// Drive the combo selection
		String selection = (String) list.get(2);
		ccombo.setText(selection); // this should drive the selection
		assertEquals(account.getCountry(), selection);

	}

	/**
	 * This scenario tests a simple SWT CCombo that is bound to a list of
	 * Country objects. The Country object's name property is listed in the
	 * Combo.
	 * 
	 * The Combo's selection is bounded to the Country property of an Account.
	 */
	public void test_WCombo_SWTCCombo() {

		// Create a list of Strings for the countries
		IObservableList list = new WritableList();
		for (int i = 0; i < catalog.getAccounts().length; i++)
			list.add(catalog.getAccounts()[i].getCountry());

		CCombo ccombo = new CCombo(getComposite(), SWT.READ_ONLY
				| SWT.DROP_DOWN);

		// Bind the combo's content to that of the String based list
		getDbc().bindList(SWTObservables.observeItems(ccombo), list);
		assertEquals(Arrays.asList(ccombo.getItems()), list);

		Account account = catalog.getAccounts()[0];

		// simple Combo's selection bound to the Account's country property
		IObservableValue comboSelection = SWTObservables
				.observeSelection(ccombo);
		getDbc().bindValue(comboSelection,
				BeansObservables.observeValue(account, "country"));

		// Drive the combo selection
		String selection = (String) list.get(2);
		ccombo.setText(selection); // this should drive the selection
		assertEquals(account.getCountry(), selection);

		selection = (String) list.get(1);
		account.setCountry(selection);
		assertEquals(selection, ccombo.getItem(ccombo.getSelectionIndex()));
		assertEquals(selection, ccombo.getText());

		selection = "country not in list";
		account.setCountry(selection);
		assertEquals(-1, ccombo.getSelectionIndex());
		assertEquals(selection, ccombo.getText());
	}

	/**
	 * This scenario tests a simple SWT CCombo that is bound to a list of
	 * Country objects. The Country object's name property is listed in the
	 * Combo.
	 * 
	 * The Combo's selection is bounded to the Country property of an Account.
	 */
	public void test_ROCombo_SWTList() {

		// Create a list of Strings for the countries
		IObservableList list = new WritableList();
		for (int i = 0; i < catalog.getAccounts().length; i++)
			list.add(catalog.getAccounts()[i].getCountry());

		org.eclipse.swt.widgets.List swtlist = new org.eclipse.swt.widgets.List(
				getComposite(), SWT.READ_ONLY | SWT.SINGLE);

		// Bind the combo's content to that of the String based list
		getDbc().bindList(SWTObservables.observeItems(swtlist), list);
		assertEquals(Arrays.asList(swtlist.getItems()), list);

		Account account = catalog.getAccounts()[0];

		// simple Combo's selection bound to the Account's country property
		IObservableValue listSelection = SWTObservables
				.observeSelection(swtlist);
		getDbc().bindValue(listSelection,
				BeansObservables.observeValue(account, "country"));

		String selection = (String) list.get(2);
		swtlist.select(2); // this should drive the selection
		swtlist.notifyListeners(SWT.Selection, null); // Force notification
		assertEquals(account.getCountry(), selection);

	}

}
