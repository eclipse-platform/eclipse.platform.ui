package org.eclipse.ui.examples.propertysheet;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.*;

/**
 * Example IPropertySource who itself is NOT editable, but whose children are.
 * The values of the children determine the value of the birthday.
 */
public class Birthday implements IPropertySource {

	//Properties
	private Integer day, month, year;

	//Property unique keys
	public static final String P_ID_DAY = "Birthday.day";
	public static final String P_ID_MONTH = "Birthday.month";
	public static final String P_ID_YEAR = "Birthday.year";
	
	//Property display keys
	public static final String P_DAY = "day";
	public static final String P_MONTH = "month";
	public static final String P_YEAR = "year";

	//default values	
	private static final Integer DAY_DEFAULT = new Integer(1);
	private static final Integer MONTH_DEFAULT = new Integer(1);
	private static final Integer YEAR_DEFAULT = new Integer(2000);

	//static date formater
	private static final DateFormat formatter  = new SimpleDateFormat("EEEE, MMMM d, yyyy");

	static private class DayLabelProvider extends LabelProvider {
		public String getText(Object element) {
			String[] dayValues = new String[] {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"};
			return dayValues[((Integer)element).intValue()];
		}
	}
	
	static private class MonthLabelProvider extends LabelProvider {
		public String getText(Object element) {
			String[] monthValues = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
			return monthValues[((Integer)element).intValue()];
		}
	}

	//
	private static Vector descriptors;
	static {
		descriptors = new Vector();

		///
		String[] dayValues = new String[] {"1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"};
		ComboBoxPropertyDescriptor days = new ComboBoxPropertyDescriptor(P_ID_DAY, P_DAY, dayValues);
		days.setLabelProvider(new DayLabelProvider());
		descriptors.addElement(days);

		///
		String[] monthValues = new String[] {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
		ComboBoxPropertyDescriptor months = new ComboBoxPropertyDescriptor(P_ID_MONTH, P_MONTH, monthValues);
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
	if(day == null)
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
	if(month == null)
		month = MONTH_DEFAULT;
	return month;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public IPropertyDescriptor[] getPropertyDescriptors() {
	return (IPropertyDescriptor[])getDescriptors().toArray(new IPropertyDescriptor[getDescriptors().size()]);
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
	if(year == null)
		year = YEAR_DEFAULT;
	return year;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public boolean isPropertySet(Object property) {
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
	};
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
		setDay(new Integer(((Integer)value).intValue() + 1));
		return;
	}
	if (P_ID_MONTH.equals(name)) {
		setMonth(new Integer(((Integer)value).intValue() + 1));
		return;
	}
	if (P_ID_YEAR.equals(name)) {
		try {
			setYear(new Integer((String)value));
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
 * The value as displayed in the Property Sheet. Will not print default values
 * @return java.lang.String
 */
public String toString() {
	Date bday =(new GregorianCalendar(getYear().intValue(), getMonth().intValue() - 1, getDay().intValue())).getTime();
	return formatter.format(bday);
}
}
