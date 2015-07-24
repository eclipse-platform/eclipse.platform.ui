/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.model;


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

	// public static ITree CATALOG_TREE;
	//
	// public static ITree CATEGORY_TREE;

	public static Signon SIGNON_ADMINISTRATOR;

	public static Signon SIGNON_JOEBLOGGS;

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

		// Signons
		SIGNON_ADMINISTRATOR = new Signon("Administrator", "Foo123Bar");
		SIGNON_JOEBLOGGS = new Signon("JoeBloggs", "Harry5Potter");
		CATALOG_2005.addSignon(SIGNON_ADMINISTRATOR);
		CATALOG_2005.addSignon(SIGNON_JOEBLOGGS);

		CART = FACTORY.createCart();

		// initTrees();
	}
}
