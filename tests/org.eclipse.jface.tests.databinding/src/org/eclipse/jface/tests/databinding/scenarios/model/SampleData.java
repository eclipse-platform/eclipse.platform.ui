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
package org.eclipse.jface.tests.databinding.scenarios.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.databinding.BeanUpdatableFactory;
import org.eclipse.jface.databinding.DataBinding;
import org.eclipse.jface.databinding.IDataBindingContext;
import org.eclipse.jface.databinding.ITree;
import org.eclipse.jface.databinding.IUpdatableFactory;
import org.eclipse.jface.databinding.swt.SWTUpdatableFactory;
import org.eclipse.jface.databinding.viewers.ViewersUpdatableFactory;
import org.eclipse.swt.widgets.Control;

public class SampleData {

	public static Category WINTER_CATEGORY;

	public static Category SUMMER_CATEGORY;

	public static Adventure BEACH_HOLIDAY;

	public static Adventure RAFTING_HOLIDAY;

	public static Adventure WINTER_HOLIDAY;

	public static Adventure ICE_FISHING;

	public static Lodging FIVE_STAR_HOTEL;

	public static Lodging YOUTH_HOSTEL;

	public static Lodging CAMP_GROUND;

	public static Catalog CATALOG_2005;

	public static Transportation GREYHOUND_BUS;

	public static Transportation EXECUTIVE_JET;

	public static Account PRESIDENT;

	public static Account DENTIST;

	public static Account SANTA_CLAUS;

	public static Cart CART;

	public static AdventureFactory FACTORY;
	
	public static ITree CATEGORY_TREE;

	private static SWTUpdatableFactory swtUpdatableFactory = new SWTUpdatableFactory();

	static {
		initializeData();
	}

	public static void initializeData() {

		FACTORY = new AdventureFactory();

		CATALOG_2005 = FACTORY.createCatalog();

		// Categories
		WINTER_CATEGORY = FACTORY.createCategory();
		WINTER_CATEGORY.setName("Freeze Adventures");
		WINTER_CATEGORY.setId("100");
		CATALOG_2005.addCategory(WINTER_CATEGORY);

		SUMMER_CATEGORY = FACTORY.createCategory();
		SUMMER_CATEGORY.setName("Hot Adventures");
		SUMMER_CATEGORY.setId("200");
		CATALOG_2005.addCategory(SUMMER_CATEGORY);

		// Adventures
		WINTER_HOLIDAY = FACTORY.createAdventure();
		WINTER_HOLIDAY.setDescription("Winter holiday in France");
		WINTER_HOLIDAY.setName("Ski Alps");
		WINTER_HOLIDAY.setLocation("Chamonix");
		WINTER_HOLIDAY.setPrice(4000.52d);
		WINTER_HOLIDAY.setId("150");
		WINTER_HOLIDAY.setMaxNumberOfPeople(3);		
		WINTER_CATEGORY.addAdventure(WINTER_HOLIDAY);

		ICE_FISHING = FACTORY.createAdventure();
		ICE_FISHING.setDescription("Ice Fishing in Helsinki");
		ICE_FISHING.setName("Ice Fishing");
		ICE_FISHING.setLocation("Finland");
		ICE_FISHING.setPrice(375.55d);
		WINTER_CATEGORY.addAdventure(ICE_FISHING);

		BEACH_HOLIDAY = FACTORY.createAdventure();
		BEACH_HOLIDAY.setDescription("Beach holiday in Spain");
		BEACH_HOLIDAY.setName("Playa");
		BEACH_HOLIDAY.setLocation("Lloret de Mar");
		BEACH_HOLIDAY.setPrice(2000.52d);
		BEACH_HOLIDAY.setId("250");
		SUMMER_CATEGORY.addAdventure(BEACH_HOLIDAY);

		RAFTING_HOLIDAY = FACTORY.createAdventure();
		RAFTING_HOLIDAY
				.setDescription("White water rafting on the Ottawa river");
		RAFTING_HOLIDAY.setName("Whitewater");
		RAFTING_HOLIDAY.setLocation("Ottawa");
		RAFTING_HOLIDAY.setPrice(8000.52d);
		RAFTING_HOLIDAY.setId("270");
		SUMMER_CATEGORY.addAdventure(RAFTING_HOLIDAY);

		// Lodgings
		FIVE_STAR_HOTEL = FACTORY.createLodging();
		FIVE_STAR_HOTEL.setDescription("Deluxe palace");
		FIVE_STAR_HOTEL.setName("Flashy");
		YOUTH_HOSTEL = FACTORY.createLodging();
		YOUTH_HOSTEL.setDescription("Youth Hostel");
		YOUTH_HOSTEL.setName("Basic");
		CAMP_GROUND = FACTORY.createLodging();
		CAMP_GROUND.setDescription("Camp ground");
		CAMP_GROUND.setName("WetAndCold");
		CATALOG_2005.addLodging(FIVE_STAR_HOTEL);
		CATALOG_2005.addLodging(YOUTH_HOSTEL);
		CATALOG_2005.addLodging(CAMP_GROUND);
		WINTER_HOLIDAY.setDefaultLodging(YOUTH_HOSTEL);

		// Transporation
		GREYHOUND_BUS = FACTORY.createTransportation();
		GREYHOUND_BUS.setArrivalTime("14:30");
		GREYHOUND_BUS.setPrice(25.50);		
		CATALOG_2005.addTransportation(GREYHOUND_BUS);
		EXECUTIVE_JET = FACTORY.createTransportation();
		EXECUTIVE_JET.setArrivalTime("11:10");
		EXECUTIVE_JET.setPrice(1500.99);
		CATALOG_2005.addTransportation(EXECUTIVE_JET);

		// Accounts
		PRESIDENT = FACTORY.createAccount();
		PRESIDENT.setFirstName("George");
		PRESIDENT.setLastName("Bush");
		PRESIDENT.setState("TX");
		PRESIDENT.setPhone("1112223333");
		PRESIDENT.setCountry("U.S.A");
		DENTIST = FACTORY.createAccount();
		DENTIST.setFirstName("Tooth");
		DENTIST.setLastName("Fairy");
		DENTIST.setState("CA");
		DENTIST.setPhone("4543219876");
		DENTIST.setCountry("PainLand");
		SANTA_CLAUS = FACTORY.createAccount();
		SANTA_CLAUS.setFirstName("Chris");
		SANTA_CLAUS.setLastName("Chringle");
		SANTA_CLAUS.setState("WI");
		SANTA_CLAUS.setPhone("8617429856");
		SANTA_CLAUS.setCountry("NorthPole");
		CATALOG_2005.addAccount(PRESIDENT);
		CATALOG_2005.addAccount(DENTIST);
		CATALOG_2005.addAccount(SANTA_CLAUS);

		CART = FACTORY.createCart();
		
		CATEGORY_TREE = new ITree() {
			Catalog catalog = CATALOG_2005;						
			public boolean hasChildren(Object element) {
				if (element instanceof Catalog) {					
					return true;  
				}
				else if (element instanceof Category) {
					Adventure[] list = ((Category)element).getAdventures();
					return list==null?true:list.length>0;
				}
				else if (element instanceof Lodging) {
					
				}
				return false;				
			}
			public Object getParent(Object element) {
				if (element instanceof Adventure) {
					Category[] categories = catalog.getCategories();
					for (int i = 0; i < categories.length; i++) {						
						if (Arrays.asList(categories[i].getAdventures()).contains(element))
							return categories[i];						
					}
				}
				else if (element instanceof Lodging)
					return catalog;
				else if (element instanceof Account)
					return catalog;
				else if (element instanceof Category)
					return catalog;
				return null;					
			}
			public void setChildren(Object parentElement, Object[] children) {
				// ReadOnly for Adding Elements
			}
			public Object[] getChildren(Object parentElement) {
				if (parentElement==null)
					return new Object[] { catalog };
				else if (parentElement instanceof Catalog) {
					List list = new ArrayList();					
					list.addAll(Arrays.asList(((Catalog)parentElement).getCategories()));
					list.addAll(Arrays.asList(((Catalog)parentElement).getLodgings()));
					list.addAll(Arrays.asList(((Catalog)parentElement).getAccounts()));
					return list.toArray();
				}
				else if (parentElement instanceof Category)
				   return ((Category)parentElement).getAdventures();				
				return null;
			}			
		};
	}

	public static IDataBindingContext getDatabindingContext(Control aControl) {
		IDataBindingContext result = DataBinding.createContext(aControl,
				new IUpdatableFactory[] { new BeanUpdatableFactory(),
						swtUpdatableFactory, new ViewersUpdatableFactory() });
		return result;
	}

	public static SWTUpdatableFactory getSWTUpdatableFactory() {
		return swtUpdatableFactory;
	}

}
