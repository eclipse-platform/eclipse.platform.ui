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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Example IPropertySource who itself is NOT editable, but whose children are.
 * The values of the children determine the value of the birthday.
 */
public class Birthday implements IPropertySource {

    //Properties
    private Integer day, month, year;

    //Property unique keys
    public static final String P_ID_DAY = "Birthday.day"; //$NON-NLS-1$

    public static final String P_ID_MONTH = "Birthday.month"; //$NON-NLS-1$

    public static final String P_ID_YEAR = "Birthday.year"; //$NON-NLS-1$

    //Property display keys
    public static final String P_DAY = MessageUtil.getString("day"); //$NON-NLS-1$

    public static final String P_MONTH = MessageUtil.getString("month"); //$NON-NLS-1$

    public static final String P_YEAR = MessageUtil.getString("year"); //$NON-NLS-1$

    //default values	
    private static final Integer DAY_DEFAULT = new Integer(1);

    private static final Integer MONTH_DEFAULT = new Integer(1);

    private static final Integer YEAR_DEFAULT = new Integer(2000);

    //static date formater
    private static final DateFormat formatter = new SimpleDateFormat(
            "EEEE, MMMM d, yyyy"); //$NON-NLS-1$

    static private class DayLabelProvider extends LabelProvider {
        public String getText(Object element) {
            String[] dayValues = new String[] {
                    "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }; //$NON-NLS-31$ //$NON-NLS-30$ //$NON-NLS-29$ //$NON-NLS-28$ //$NON-NLS-27$ //$NON-NLS-26$ //$NON-NLS-25$ //$NON-NLS-24$ //$NON-NLS-23$ //$NON-NLS-22$ //$NON-NLS-21$ //$NON-NLS-20$ //$NON-NLS-19$ //$NON-NLS-18$ //$NON-NLS-17$ //$NON-NLS-16$ //$NON-NLS-15$ //$NON-NLS-14$ //$NON-NLS-13$ //$NON-NLS-12$ //$NON-NLS-11$ //$NON-NLS-10$ //$NON-NLS-9$ //$NON-NLS-8$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
            return dayValues[((Integer) element).intValue()];
        }
    }

    static private class MonthLabelProvider extends LabelProvider {
        public String getText(Object element) {
            String[] monthValues = new String[] {
                    MessageUtil.getString("January"), MessageUtil.getString("February"), MessageUtil.getString("March"), MessageUtil.getString("April"), MessageUtil.getString("May"), MessageUtil.getString("June"), MessageUtil.getString("July"), MessageUtil.getString("August"), MessageUtil.getString("September"), MessageUtil.getString("October"), MessageUtil.getString("November"), MessageUtil.getString("December") }; //$NON-NLS-12$ //$NON-NLS-11$ //$NON-NLS-10$ //$NON-NLS-9$ //$NON-NLS-8$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
            return monthValues[((Integer) element).intValue()];
        }
    }

    //
    private static Vector descriptors;
    static {
        descriptors = new Vector();

        ///
        String[] dayValues = new String[] {
                "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31" }; //$NON-NLS-31$ //$NON-NLS-30$ //$NON-NLS-29$ //$NON-NLS-28$ //$NON-NLS-27$ //$NON-NLS-26$ //$NON-NLS-25$ //$NON-NLS-24$ //$NON-NLS-23$ //$NON-NLS-22$ //$NON-NLS-21$ //$NON-NLS-20$ //$NON-NLS-19$ //$NON-NLS-18$ //$NON-NLS-17$ //$NON-NLS-16$ //$NON-NLS-15$ //$NON-NLS-14$ //$NON-NLS-13$ //$NON-NLS-12$ //$NON-NLS-11$ //$NON-NLS-10$ //$NON-NLS-9$ //$NON-NLS-8$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
        ComboBoxPropertyDescriptor days = new ComboBoxPropertyDescriptor(
                P_ID_DAY, P_DAY, dayValues);
        days.setLabelProvider(new DayLabelProvider());
        descriptors.addElement(days);

        ///
        String[] monthValues = new String[] {
                MessageUtil.getString("January"), MessageUtil.getString("February"), MessageUtil.getString("March"), MessageUtil.getString("April"), MessageUtil.getString("May"), MessageUtil.getString("June"), MessageUtil.getString("July"), MessageUtil.getString("August"), MessageUtil.getString("September"), MessageUtil.getString("October"), MessageUtil.getString("November"), MessageUtil.getString("December") }; //$NON-NLS-12$ //$NON-NLS-11$ //$NON-NLS-10$ //$NON-NLS-9$ //$NON-NLS-8$ //$NON-NLS-7$ //$NON-NLS-6$ //$NON-NLS-5$ //$NON-NLS-4$ //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
        ComboBoxPropertyDescriptor months = new ComboBoxPropertyDescriptor(
                P_ID_MONTH, P_MONTH, monthValues);
        months.setLabelProvider(new MonthLabelProvider());
        descriptors.addElement(months);

        ///
        descriptors.addElement(new TextPropertyDescriptor(P_ID_YEAR, P_YEAR));
    }

    /**
     * Address Default Constructor
     */
    Birthday() {
        super();
    }

    /**
     * Convenience Address Constructor
     */
    public Birthday(int day, int month, int year) {
        super();
        setDay(new Integer(day));
        setMonth(new Integer(month));
        setYear(new Integer(year));
    }

    /**
     * Returns the day
     */
    private Integer getDay() {
        if (day == null)
            day = DAY_DEFAULT;
        return day;
    }

    /**
     * Standard Accessor
     */
    private static Vector getDescriptors() {
        return descriptors;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource
     */
    public Object getEditableValue() {
        return this.toString();
    }

    /**
     * Returns the month
     */
    private Integer getMonth() {
        if (month == null)
            month = MONTH_DEFAULT;
        return month;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource
     */
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return (IPropertyDescriptor[]) getDescriptors().toArray(
                new IPropertyDescriptor[getDescriptors().size()]);
    }

    /** 
     * The <code>Birthday</code> implementation of this
     * <code>IPropertySource</code> method returns the following properties
     *
     * 	1) P_DAY returns java.lang.Integer
     * 	2) P_MONTH returns java.lang.Integer
     *  3) P_YEAR returns java.lang.Integer
     *	4) P_STREET returns java.lang.String
     */
    public Object getPropertyValue(Object propKey) {
        if (propKey.equals(P_ID_DAY))
            return new Integer(getDay().intValue() - 1);
        if (propKey.equals(P_ID_MONTH))
            return new Integer(getMonth().intValue() - 1);
        if (propKey.equals(P_ID_YEAR))
            return getYear().toString();
        return null;
    }

    /**
     * Returns the year
     */
    private Integer getYear() {
        if (year == null)
            year = YEAR_DEFAULT;
        return year;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource
     */
    public boolean isPropertySet(Object property) {
        if (P_ID_DAY.equals(property))
            return getDay() != DAY_DEFAULT;
        if (P_ID_MONTH.equals(property))
            return getMonth() != MONTH_DEFAULT;
        if (P_ID_YEAR.equals(property))
            return getYear() != YEAR_DEFAULT;
        return false;
    }

    /* (non-Javadoc)
     * Method declared on IPropertySource
     */
    public void resetPropertyValue(Object property) {
        if (P_ID_DAY.equals(property)) {
            setDay(DAY_DEFAULT);
            return;
        }
        if (P_ID_MONTH.equals(property)) {
            setMonth(MONTH_DEFAULT);
            return;
        }
        if (P_ID_YEAR.equals(property)) {
            setYear(YEAR_DEFAULT);
            return;
        }
    }

    /**
     * Sets the day
     */
    private void setDay(Integer newDay) {
        day = newDay;
    }

    /**
     * Sets the month
     */
    private void setMonth(Integer newMonth) {
        month = newMonth;
    }

    /** 
     * The <code>Birthday</code> implementation of this
     * <code>IPropertySource</code> method 
     * defines the following Setable properties
     *
     * 	1) P_DAY expects java.lang.Integer
     * 	2) P_MONTH expects java.lang.Integer
     *  3) P_YEAR expects java.lang.Integer
     */
    public void setPropertyValue(Object name, Object value) {
        if (P_ID_DAY.equals(name)) {
            setDay(new Integer(((Integer) value).intValue() + 1));
            return;
        }
        if (P_ID_MONTH.equals(name)) {
            setMonth(new Integer(((Integer) value).intValue() + 1));
            return;
        }
        if (P_ID_YEAR.equals(name)) {
            try {
                setYear(new Integer((String) value));
            } catch (NumberFormatException e) {
                setYear(YEAR_DEFAULT);
            }
            return;
        }
    }

    /**
     * Sets the year
     */
    private void setYear(Integer newYear) {
        year = newYear;
    }

    /**
     * The value as displayed in the Property Sheet.
     * @return java.lang.String
     */
    public String toString() {
        Date bday = (new GregorianCalendar(getYear().intValue(), getMonth()
                .intValue() - 1, getDay().intValue())).getTime();
        return formatter.format(bday);
    }
}
