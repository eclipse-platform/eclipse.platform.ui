package org.eclipse.ui.examples.propertysheet;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * This class is an example of the implementation of a simple parser.
 */
public class UserFileParser {
/**
 * Return the fabricated result for this example.
 *
 */
private static IAdaptable getFabricatedResult() {
	// returns fabricated input. 
	GroupElement root = new GroupElement("Everybody", null);
	GroupElement userGroup = root.createSubGroup("Company Inc.");
	GroupElement ottGroup = userGroup.createSubGroup("Waterloo Lab");
	userGroup.createSubGroup("Toronto Lab");
	userGroup.createSubGroup("Hamilton Lab");
	userGroup.createSubGroup("London Lab");
	userGroup.createSubGroup("Grimsby Lab");
	GroupElement uiTeam = ottGroup.createSubGroup("Team1");
	//
	UserElement user1 = uiTeam.createUser("richard");
	user1.setFullName(new Name("Richard Zokol"));
	user1.setEmailAddress(new EmailAddress("rzokol@company.com"));
	user1.setPhoneNumber("x789");
	user1.setAddress(new Address(new StreetAddress(232, "Champlain"), "Hull", new Integer(5), "A1B2C3"));
	user1.setBirthday(new Birthday(18, 1, 1981));
	user1.setCoop(new Boolean(true));
	user1.setHairColor(new RGB(0, 0, 0));
	user1.setEyeColor(new RGB(0, 0, 0));
	//
	UserElement user2 = uiTeam.createUser("george");
	user2.setFullName(new Name("George Knudson"));
	user2.setEmailAddress(new EmailAddress("gknudson@company.com"));
	user2.setPhoneNumber("x678");
	user2.setAddress(new Address(new StreetAddress(), "Toronto", new Integer(4), "A1B2C3"));
	user2.setBirthday(new Birthday(7, 5, 1978));
	user2.setCoop(new Boolean(true));
	user2.setHairColor(new RGB(0, 0, 0));
	user2.setEyeColor(new RGB(0, 0, 0));

	//
	UserElement user3 = uiTeam.createUser("arnold");
	user3.setFullName(new Name("Arnold Palmer"));
	user3.setEmailAddress(new EmailAddress("apalmer@company.com"));
	user3.setPhoneNumber("x567");
	user3.setAddress(new Address(new StreetAddress(), "Ottawa", new Integer(4), "A1B2C3"));
	user3.setBirthday(new Birthday(11, 23, 1962));
	user3.setHairColor(new RGB(0, 0, 0));
	user3.setEyeColor(new RGB(0, 0, 0));
	
	//
	UserElement user4 = uiTeam.createUser("lee");
	user4.setFullName(new Name("Lee Trevino"));
	user4.setEmailAddress(new EmailAddress("ltrevino@company.com"));
	user4.setPhoneNumber("x456");
	user4.setAddress(new Address(new StreetAddress(), "Ottawa", new Integer(4), "A1B2C3"));
	//
	UserElement user5 = uiTeam.createUser("tiger");
	user5.setFullName(new Name("Tiger Woods"));
	user5.setEmailAddress(new EmailAddress("twoods@company.com"));
	user5.setPhoneNumber("x345");
	user5.setAddress(new Address(new StreetAddress(), "Ottawa", new Integer(4), "A1B2C3"));
	//
	UserElement user6 = uiTeam.createUser("jack");
	user6.setFullName(new Name("Jack Nicklaus"));
	user6.setEmailAddress(new EmailAddress("jnicklaus@company.com"));
	user6.setPhoneNumber("x234 ");
	user6.setAddress(new Address(new StreetAddress(), "Ottawa ", new Integer(4), "A1B2C3"));
	//
	UserElement greg = uiTeam.createUser("weslock");
	greg.setFullName(new Name("Weslock"));
	greg.setEmailAddress(new EmailAddress("weslock@company.com"));
	greg.setPhoneNumber("x123");
	greg.setAddress(new Address(new StreetAddress(), "Ottawa", new Integer(4), "A1B2C3"));

	return root;
}
/**
 * Parse the input given by the argument. For this example we do no parsing and return
 * a fabricated result.
 *
 */
public IAdaptable parse(IDocumentProvider documentProvider) {

	return getFabricatedResult();
}
}
