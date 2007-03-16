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

import java.util.Vector;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.views.properties.ColorPropertyDescriptor;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySheetEntry;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * A User Element
 */
public class UserElement extends OrganizationElement {

    // Properties
    private Name fullName;

    private Address address;

    private String phoneNumber;

    private EmailAddress emailAddress;

    private Boolean coop;

    private Birthday birthday;

    private Float salary;

    private RGB hairColor;

    private RGB eyeColor;

    // Property default values
    private Name fullName_Default;

    private Address address_Default;

    private String phoneNumber_Default = "555-1111"; //$NON-NLS-1$

    private EmailAddress emailAddress_Default;

    private Boolean coop_Default;

    private Birthday birthday_Default;

    private Float salary_Default;

    private RGB hairColor_Default;

    private RGB eyeColor_Default;

    // Property unique keys
    public static final String P_ID_PHONENUMBER = "User.phonenumber"; //$NON-NLS-1$

    public static final String P_ID_ADDRESS = "User.address"; //$NON-NLS-1$

    public static final String P_ID_FULLNAME = "User.fullname"; //$NON-NLS-1$

    public static final String P_ID_EMAIL = "User.email"; //$NON-NLS-1$

    public static final String P_ID_COOP = "User.coop student"; //$NON-NLS-1$

    public static final String P_ID_BDAY = "User.birthday"; //$NON-NLS-1$

    public static final String P_ID_SALARY = "User.salary"; //$NON-NLS-1$

    public static final String P_ID_HAIRCOLOR = "User.haircolor"; //$NON-NLS-1$

    public static final String P_ID_EYECOLOR = "User.eyecolor"; //$NON-NLS-1$

    // Property display keys
    public static final String P_PHONENUMBER = MessageUtil
            .getString("phonenumber"); //$NON-NLS-1$

    public static final String P_ADDRESS = MessageUtil.getString("address"); //$NON-NLS-1$

    public static final String P_FULLNAME = MessageUtil.getString("fullname"); //$NON-NLS-1$

    public static final String P_EMAIL = MessageUtil.getString("email"); //$NON-NLS-1$

    public static final String P_COOP = MessageUtil.getString("coop_student"); //$NON-NLS-1$

    public static final String P_BDAY = MessageUtil.getString("birthday"); //$NON-NLS-1$

    public static final String P_SALARY = MessageUtil.getString("salary"); //$NON-NLS-1$

    public static final String P_HAIRCOLOR = MessageUtil.getString("haircolor"); //$NON-NLS-1$

    public static final String P_EYECOLOR = MessageUtil.getString("eyecolor"); //$NON-NLS-1$

    // Help context ids
    private static final String PHONE_NUMBER_CONTEXT = "org.eclipse.ui.examples.propertysheet.phone_number_context"; //$NON-NLS-1$

    private static final String ADDRESS_CONTEXT = "org.eclipse.ui.examples.propertysheet.address_context"; //$NON-NLS-1$

    private static final String FULL_NAME_CONTEXT = "org.eclipse.ui.examples.propertysheet.full_name_context"; //$NON-NLS-1$

    private static final String EMAIL_CONTEXT = "org.eclipse.ui.examples.propertysheet.email_context"; //$NON-NLS-1$

    private static final String COOP_CONTEXT = "org.eclipse.ui.examples.propertysheet.coop_context"; //$NON-NLS-1$

    private static final String BIRTHDAY_CONTEXT = "org.eclipse.ui.examples.propertysheet.birthday_context"; //$NON-NLS-1$

    private static final String SALARY_CONTEXT = "org.eclipse.ui.examples.propertysheet.salary_context"; //$NON-NLS-1$

    private static final String HAIR_COLOR__CONTEXT = "org.eclipse.ui.examples.propertysheet.hair_color_context"; //$NON-NLS-1$

    private static final String EYE_COLOR_CONTEXT = "org.eclipse.ui.examples.propertysheet.eye_color_context"; //$NON-NLS-1$

    // Property Category
    public static final String P_CONTACTINFO = MessageUtil.getString("contact"); //$NON-NLS-1$

    public static final String P_PERSONELINFO = MessageUtil
            .getString("personel"); //$NON-NLS-1$

    public static final String P_PERSONALINFO = MessageUtil
            .getString("personal"); //$NON-NLS-1$

    // Property Values
    public static final Integer P_VALUE_TRUE = new Integer(0);

    public static final Integer P_VALUE_FALSE = new Integer(1);

    public static final String P_VALUE_TRUE_LABEL = MessageUtil
            .getString("true"); //$NON-NLS-1$

    public static final String P_VALUE_FALSE_LABEL = MessageUtil
            .getString("false"); //$NON-NLS-1$

    static private class BooleanLabelProvider extends LabelProvider {
        public String getText(Object element) {
            String[] values = new String[] { P_VALUE_TRUE_LABEL,
                    P_VALUE_FALSE_LABEL };
            return values[((Integer) element).intValue()];
        }
    }

    //
    static private Vector descriptors;
    static {
        descriptors = new Vector();
        PropertyDescriptor propertyDescriptor;

        ///
        propertyDescriptor = new TextPropertyDescriptor(P_ID_PHONENUMBER,
                P_PHONENUMBER);
        propertyDescriptor.setCategory(P_CONTACTINFO);
        propertyDescriptor.setHelpContextIds(PHONE_NUMBER_CONTEXT);
        descriptors.addElement(propertyDescriptor);

        ///
        propertyDescriptor = new PropertyDescriptor(P_ID_ADDRESS, P_ADDRESS);
        propertyDescriptor.setCategory(P_CONTACTINFO);
        propertyDescriptor.setHelpContextIds(ADDRESS_CONTEXT);
        descriptors.addElement(propertyDescriptor);

        ///
        propertyDescriptor = new TextPropertyDescriptor(P_ID_EMAIL, P_EMAIL);
        propertyDescriptor.setCategory(P_CONTACTINFO);
        propertyDescriptor.setHelpContextIds(EMAIL_CONTEXT);
        propertyDescriptor.setValidator(new EmailAddressValidator());
        descriptors.addElement(propertyDescriptor);

        ///
        propertyDescriptor = new TextPropertyDescriptor(P_ID_FULLNAME,
                P_FULLNAME);
        propertyDescriptor.setCategory(P_PERSONELINFO);
        propertyDescriptor.setHelpContextIds(FULL_NAME_CONTEXT);
        descriptors.addElement(propertyDescriptor);

        ///
        propertyDescriptor = new PropertyDescriptor(P_ID_BDAY, P_BDAY);
        propertyDescriptor.setCategory(P_PERSONELINFO);
        propertyDescriptor.setHelpContextIds(BIRTHDAY_CONTEXT);
        descriptors.addElement(propertyDescriptor);

        ///
        propertyDescriptor = new ComboBoxPropertyDescriptor(P_ID_COOP, P_COOP,
                new String[] { P_VALUE_TRUE_LABEL, P_VALUE_FALSE_LABEL });
        propertyDescriptor.setCategory(P_PERSONELINFO);
        propertyDescriptor.setHelpContextIds(COOP_CONTEXT);
        propertyDescriptor.setLabelProvider(new BooleanLabelProvider());
        descriptors.addElement(propertyDescriptor);

        ///
        propertyDescriptor = new TextPropertyDescriptor(P_ID_SALARY, P_SALARY);
        //add custom validator to propertyDescriptor limiting salary values to be
        //greator than zero
        propertyDescriptor.setHelpContextIds(new Object[] { SALARY_CONTEXT });
        propertyDescriptor
                .setFilterFlags(new String[] { IPropertySheetEntry.FILTER_ID_EXPERT });
        propertyDescriptor.setValidator(new ICellEditorValidator() {
            public String isValid(Object value) {
                if (value == null)
                    return MessageUtil.getString("salary_is_invalid"); //$NON-NLS-1$
                //
                Float trySalary;
                try {
                    trySalary = new Float(Float.parseFloat((String) value));
                } catch (NumberFormatException e) {
                    return MessageUtil.getString("salary_is_invalid"); //$NON-NLS-1$
                }
                if (trySalary.floatValue() < 0.0)
                    return MessageUtil
                            .getString("salary_must_be_greator_than_zero"); //$NON-NLS-1$
                return null;
            }
        });
        propertyDescriptor.setCategory(P_PERSONELINFO);
        descriptors.addElement(propertyDescriptor);

        /// HairColor
        propertyDescriptor = new ColorPropertyDescriptor(P_ID_HAIRCOLOR,
                P_HAIRCOLOR);
        propertyDescriptor.setCategory(P_PERSONALINFO);
        propertyDescriptor.setHelpContextIds(HAIR_COLOR__CONTEXT);
        descriptors.addElement(propertyDescriptor);

        /// EyeColor
        propertyDescriptor = new ColorPropertyDescriptor(P_ID_EYECOLOR,
                P_EYECOLOR);
        propertyDescriptor.setCategory(P_PERSONALINFO);
        propertyDescriptor.setHelpContextIds(EYE_COLOR_CONTEXT);
        descriptors.addElement(propertyDescriptor);

        //gets descriptors from parent, warning name-space collision may occur
        Vector parentDescriptors = OrganizationElement.getDescriptors();
        for (int i = 0; i < parentDescriptors.size(); i++) {
            descriptors.addElement(parentDescriptors.elementAt(i));
        }
    }

    /**
     * Constructor. Default visibility only called from GroupElement.createSubGroup()
     * Creates a new UserElement within the passed parent GroupElement,
     * *
     * @param name the name 
     * @param parent the parent
     */
    UserElement(String name, GroupElement parent) {
        super(name, parent);
    }

    /**
     * Returns the address
     */
    private Address getAddress() {
        if (address == null)
            address = new Address();
        return address;
    }

    /**
     * Returns the birthday
     */
    private Birthday getBirthday() {
        if (birthday == null)
            birthday = new Birthday();
        return birthday;
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchAdapter
     */
    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    /**
     * Returns the coop
     */
    private Boolean getCoop() {
        if (coop == null)
            coop = new Boolean(false);
        return coop;
    }

    /**
     * Returns the descriptors
     */
    static Vector getDescriptors() {
        return descriptors;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource
     */
    public Object getEditableValue() {
        return this.toString();
    }

    /**
     * Returns the email address
     */
    EmailAddress getEmailAddress() {
        if (emailAddress == null)
            emailAddress = new EmailAddress();
        return emailAddress;
    }

    /**
     * Returns the eye color
     */
    private RGB getEyeColor() {
        if (eyeColor == null)
            eyeColor = new RGB(60, 60, 60);
        return eyeColor;
    }

    /**
     * Returns the full name
     */
    private Name getFullName() {
        if (fullName == null)
            fullName = new Name(getName());
        return fullName;
    }

    /**
     * Returns the hair color
     */
    private RGB getHairColor() {
        if (hairColor == null)
            hairColor = new RGB(255, 255, 255);
        return hairColor;
    }

    /**
     * Returns the phone number
     */
    private String getPhoneNumber() {
        return phoneNumber;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return (IPropertyDescriptor[]) getDescriptors().toArray(
                new IPropertyDescriptor[getDescriptors().size()]);
    }

    /** 
     * The <code>Userment</code> implementation of this
     * <code>IPropertySource</code> method returns the following properties
     *
     *	1) P_ADDRESS returns Address (IPropertySource), the address of this user
     * 	2) P_FULLNAME returns Name (IPropertySource), the full name of this user
     *  3) P_PHONENUMBER returns String, the phone number of this user
     *  4) P_EMAIL returns EmailAddress (IPropertySource), the email address of this user
     *  5) P_COOP returns Boolean, whether the individual is a coop student or not
     *  6) P_BDAY returns Birthday
     *  7) P_SALARY return java.lang.Float
     *  8) P_HAIRCOLOR, expects RGB
     *  9) P_EYECOLOR, expects RGB
     */
    public Object getPropertyValue(Object propKey) {
        if (propKey.equals(P_ID_ADDRESS))
            return getAddress();
        if (propKey.equals(P_ID_FULLNAME))
            return getFullName();
        if (propKey.equals(P_ID_PHONENUMBER))
            return getPhoneNumber();
        if (propKey.equals(P_ID_EMAIL))
            return getEmailAddress();
        if (propKey.equals(P_ID_COOP))
            return getCoop().equals(Boolean.TRUE) ? P_VALUE_TRUE
                    : P_VALUE_FALSE;
        if (propKey.equals(P_ID_BDAY))
            return getBirthday();
        if (propKey.equals(P_ID_SALARY))
            return getSalary().toString();
        if (propKey.equals(P_ID_HAIRCOLOR))
            return getHairColor();
        if (propKey.equals(P_ID_EYECOLOR))
            return getEyeColor();
        return super.getPropertyValue(propKey);
    }

    /**
     * Returns the salary
     */
    private Float getSalary() {
        if (salary == null)
            salary = new Float(0);
        return salary;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource
     */
    public boolean isPropertySet(Object property) {
        if (property.equals(P_ID_ADDRESS))
            return getAddress() != address_Default;
        if (property.equals(P_ID_FULLNAME))
            return getFullName() != fullName_Default;
        if (property.equals(P_ID_PHONENUMBER))
            return getPhoneNumber() != phoneNumber_Default;
        if (property.equals(P_ID_EMAIL))
            return getEmailAddress() != emailAddress_Default;
        if (property.equals(P_ID_COOP))
            return getCoop() != coop_Default;
        if (property.equals(P_ID_BDAY))
            return getBirthday() != birthday_Default;
        if (property.equals(P_ID_SALARY))
            return getSalary() != salary_Default;
        if (property.equals(P_ID_HAIRCOLOR))
            return getHairColor() != hairColor_Default;
        if (property.equals(P_ID_EYECOLOR))
            return getEyeColor() != eyeColor_Default;
        return false;
    }

    /* (non-Javadoc)
     * Method declared on OrganizationElement
     */
    public boolean isUser() {
        return true;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource
     */
    public void resetPropertyValue(Object property) {
        if (property.equals(P_ID_ADDRESS)) {
            setAddress(address_Default);
            return;
        }
        if (property.equals(P_ID_FULLNAME)) {
            setFullName(fullName_Default);
            return;
        }
        if (property.equals(P_ID_PHONENUMBER)) {
            setPhoneNumber(phoneNumber_Default);
            return;
        }
        if (property.equals(P_ID_EMAIL)) {
            setEmailAddress(emailAddress_Default);
            return;
        }
        if (property.equals(P_ID_COOP)) {
            setCoop(coop_Default);
        }
        if (property.equals(P_ID_BDAY)) {
            setBirthday(birthday_Default);
            return;
        }
        if (property.equals(P_ID_SALARY)) {
            setSalary(salary_Default);
            return;
        }
        if (property.equals(P_ID_HAIRCOLOR)) {
            setHairColor(hairColor_Default);
            return;
        }
        if (property.equals(P_ID_EYECOLOR)) {
            setEyeColor(eyeColor_Default);
            return;
        }
        super.resetPropertyValue(property);
    }

    /**
     * Sets the address
     */
    public void setAddress(Address newAddress) {
        address = newAddress;
    }

    /**
     * Sets the birthday
     */
    public void setBirthday(Birthday newBirthday) {
        birthday = new Birthday();
    }

    /**
     * Sets the coop
     */
    public void setCoop(Boolean newCoop) {
        coop = newCoop;
    }

    /**
     *Sets the email address
     */
    public void setEmailAddress(EmailAddress newEmailAddress) {
        emailAddress = newEmailAddress;
    }

    /**
     * Sets eye color
     */
    public void setEyeColor(RGB newEyeColor) {
        eyeColor = newEyeColor;
    }

    /**
     * Sets full name
     */
    public void setFullName(Name newFullName) {
        fullName = newFullName;
    }

    /**
     * Sets hair color
     */
    public void setHairColor(RGB newHairColor) {
        hairColor = newHairColor;
    }

    /**
     * Sets phone number
     */
    public void setPhoneNumber(String newPhoneNumber) {
        phoneNumber = newPhoneNumber;
    }

    /** 
     * The <code>OrganizationElement</code> implementation of this
     * <code>IPropertySource</code> method 
     * defines the following Setable properties
     *
     *	1) P_ADDRESS, expects Address
     *	2) P_FULLNAME, expects Name
     *	3) P_PHONENUMBER, expects String
     *  4) P_EMAIL, expects EmailAddress.
     *  5) P_BOOLEAN, expects Boolean, whether the individual is a coop student or not
     *  6) P_BDAY, expects Birthday
     *  7) P_SALARY, expects java.lang.Float
     *  8) P_HAIRCOLOR, expects RGB
     *  9) P_EYECOLOR, expects RGB
     */
    public void setPropertyValue(Object propKey, Object val) {
        // need to convert from String to domain objects
        if (propKey.equals(P_ID_ADDRESS)) {
            //setAddress((Address)val);
            return;
        }
        if (propKey.equals(P_ID_FULLNAME)) {
            setFullName(new Name((String) val));
            return;
        }
        if (propKey.equals(P_ID_PHONENUMBER)) {
            setPhoneNumber((String) val);
            return;
        }
        if (propKey.equals(P_ID_EMAIL)) {
            setEmailAddress(new EmailAddress((String) val));
            return;
        }
        if (propKey.equals(P_ID_COOP)) {
            setCoop(((Integer) val).equals(P_VALUE_TRUE) ? Boolean.TRUE
                    : Boolean.FALSE);
        }
        if (propKey.equals(P_ID_BDAY)) {
            //setBirthday((Birthday)val);
            return;
        }
        if (propKey.equals(P_ID_SALARY)) {
            try {
                setSalary(new Float(Float.parseFloat((String) val)));
            } catch (NumberFormatException e) {
                setSalary(salary_Default);
            }
            return;
        }
        if (propKey.equals(P_ID_HAIRCOLOR)) {
            setHairColor((RGB) val);
            return;
        }
        if (propKey.equals(P_ID_EYECOLOR)) {
            setEyeColor((RGB) val);
            return;
        }
        super.setPropertyValue(propKey, val);
    }

    /**
     * Sets the salary
     */
    void setSalary(Float newSalary) {
        salary = newSalary;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getForeground(java.lang.Object)
     */
    public RGB getForeground(Object element) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getBackground(java.lang.Object)
     */
    public RGB getBackground(Object element) {
        return null;
    }
}
