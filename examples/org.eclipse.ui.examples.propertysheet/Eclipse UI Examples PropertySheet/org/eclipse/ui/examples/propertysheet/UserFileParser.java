/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.propertysheet;

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
        GroupElement root = new GroupElement(
                MessageUtil.getString("Everybody"), null); //$NON-NLS-1$
        GroupElement userGroup = root.createSubGroup(MessageUtil
                .getString("Company_Inc")); //$NON-NLS-1$
        GroupElement ottGroup = userGroup.createSubGroup(MessageUtil
                .getString("Waterloo_Lab")); //$NON-NLS-1$
        userGroup.createSubGroup(MessageUtil.getString("Toronto_Lab")); //$NON-NLS-1$
        userGroup.createSubGroup(MessageUtil.getString("Hamilton_Lab")); //$NON-NLS-1$
        userGroup.createSubGroup(MessageUtil.getString("London_Lab")); //$NON-NLS-1$
        userGroup.createSubGroup(MessageUtil.getString("Grimsby_Lab")); //$NON-NLS-1$
        GroupElement uiTeam = ottGroup.createSubGroup(MessageUtil
                .getString("Team1")); //$NON-NLS-1$
        //
        UserElement user1 = uiTeam.createUser("richard"); //$NON-NLS-1$
        user1.setFullName(new Name(MessageUtil.getString("Richard_Zokol"))); //$NON-NLS-1$
        user1.setEmailAddress(new EmailAddress(MessageUtil
                .getString("rzokol@company.com"))); //$NON-NLS-1$
        user1.setPhoneNumber("x789"); //$NON-NLS-1$
        user1
                .setAddress(new Address(
                        new StreetAddress(232, MessageUtil
                                .getString("Champlain")), MessageUtil.getString("Hull"), new Integer(5), MessageUtil.getString("A1B2C3"))); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
        user1.setBirthday(new Birthday(18, 1, 1981));
        user1.setCoop(Boolean.TRUE);
        user1.setHairColor(new RGB(0, 0, 0));
        user1.setEyeColor(new RGB(0, 0, 0));
        //
        UserElement user2 = uiTeam.createUser("george"); //$NON-NLS-1$
        user2.setFullName(new Name(MessageUtil.getString("George_Knudson"))); //$NON-NLS-1$
        user2.setEmailAddress(new EmailAddress(MessageUtil
                .getString("gknudson@company.com"))); //$NON-NLS-1$
        user2.setPhoneNumber("x678"); //$NON-NLS-1$
        user2
                .setAddress(new Address(
                        new StreetAddress(),
                        MessageUtil.getString("Toronto"), new Integer(4), MessageUtil.getString("A1B2C3"))); //$NON-NLS-2$ //$NON-NLS-1$
        user2.setBirthday(new Birthday(7, 5, 1978));
        user2.setCoop(Boolean.TRUE);
        user2.setHairColor(new RGB(0, 0, 0));
        user2.setEyeColor(new RGB(0, 0, 0));

        //
        UserElement user3 = uiTeam.createUser("arnold"); //$NON-NLS-1$
        user3.setFullName(new Name(MessageUtil.getString("Arnold_Palmer"))); //$NON-NLS-1$
        user3.setEmailAddress(new EmailAddress(MessageUtil
                .getString("apalmer@company.com"))); //$NON-NLS-1$
        user3.setPhoneNumber("x567"); //$NON-NLS-1$
        user3
                .setAddress(new Address(
                        new StreetAddress(),
                        MessageUtil.getString("Ottawa"), new Integer(4), MessageUtil.getString("A1B2C3"))); //$NON-NLS-2$ //$NON-NLS-1$
        user3.setBirthday(new Birthday(11, 23, 1962));
        user3.setHairColor(new RGB(0, 0, 0));
        user3.setEyeColor(new RGB(0, 0, 0));

        //
        UserElement user4 = uiTeam.createUser("lee"); //$NON-NLS-1$
        user4.setFullName(new Name(MessageUtil.getString("Lee_Trevino"))); //$NON-NLS-1$
        user4.setEmailAddress(new EmailAddress(MessageUtil
                .getString("ltrevino@company.com"))); //$NON-NLS-1$
        user4.setPhoneNumber("x456"); //$NON-NLS-1$
        user4
                .setAddress(new Address(
                        new StreetAddress(),
                        MessageUtil.getString("Ottawa"), new Integer(4), MessageUtil.getString("A1B2C3"))); //$NON-NLS-2$ //$NON-NLS-1$
        //
        UserElement user5 = uiTeam.createUser("tiger"); //$NON-NLS-1$
        user5.setFullName(new Name(MessageUtil.getString("Tiger_Woods"))); //$NON-NLS-1$
        user5.setEmailAddress(new EmailAddress(MessageUtil
                .getString("twoods@company.com"))); //$NON-NLS-1$
        user5.setPhoneNumber("x345"); //$NON-NLS-1$
        user5
                .setAddress(new Address(
                        new StreetAddress(),
                        MessageUtil.getString("Ottawa"), new Integer(4), MessageUtil.getString("A1B2C3"))); //$NON-NLS-2$ //$NON-NLS-1$
        //
        UserElement user6 = uiTeam.createUser("jack"); //$NON-NLS-1$
        user6.setFullName(new Name(MessageUtil.getString("Jack_Nicklaus"))); //$NON-NLS-1$
        user6.setEmailAddress(new EmailAddress(MessageUtil
                .getString("jnicklaus@company.com"))); //$NON-NLS-1$
        user6.setPhoneNumber("x234 "); //$NON-NLS-1$
        user6
                .setAddress(new Address(
                        new StreetAddress(),
                        MessageUtil.getString("Ottawa"), new Integer(4), MessageUtil.getString("A1B2C3"))); //$NON-NLS-2$ //$NON-NLS-1$
        //
        UserElement greg = uiTeam.createUser("weslock"); //$NON-NLS-1$
        greg.setFullName(new Name(MessageUtil.getString("Weslock"))); //$NON-NLS-1$
        greg.setEmailAddress(new EmailAddress(MessageUtil
                .getString("weslock@company.com"))); //$NON-NLS-1$
        greg.setPhoneNumber("x123"); //$NON-NLS-1$
        greg
                .setAddress(new Address(
                        new StreetAddress(),
                        MessageUtil.getString("Ottawa"), new Integer(4), MessageUtil.getString("A1B2C3"))); //$NON-NLS-2$ //$NON-NLS-1$

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
