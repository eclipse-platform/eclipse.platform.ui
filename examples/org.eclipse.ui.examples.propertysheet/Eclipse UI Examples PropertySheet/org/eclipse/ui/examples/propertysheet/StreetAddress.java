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

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * PropertySource containing street information pertenant to Address
 */
public class StreetAddress implements IPropertySource {

    //properties
    private Integer buildNo;

    private String aptBox;

    private String streetName;

    //default property values
    private static final Integer BUILD_NO_DEFAULT = new Integer(0);

    private static final String APTBOX_DEFAULT = MessageUtil
            .getString("unspecified"); //$NON-NLS-1$

    private static final String STREETNAME_DEFAULT = MessageUtil
            .getString("unspecified"); //$NON-NLS-1$

    //property unique keys
    public static final String P_ID_BUILD_NO = "Street.buildingNo"; //$NON-NLS-1$

    public static final String P_ID_APTBOX = "Street.aptNo"; //$NON-NLS-1$

    public static final String P_ID_STREET = "Street.street"; //$NON-NLS-1$

    //property display keys
    public static final String P_BUILD_NO = MessageUtil
            .getString("building_number"); //$NON-NLS-1$

    public static final String P_APTBOX = MessageUtil
            .getString("apt.no_or_box.no"); //$NON-NLS-1$

    public static final String P_STREET = MessageUtil.getString("street"); //$NON-NLS-1$

    private static Vector<TextPropertyDescriptor> descriptors;

    static {
        descriptors = new Vector<>();
        descriptors.addElement(new TextPropertyDescriptor(P_ID_BUILD_NO,
                P_BUILD_NO));
        descriptors
                .addElement(new TextPropertyDescriptor(P_ID_APTBOX, P_APTBOX));
        descriptors
                .addElement(new TextPropertyDescriptor(P_ID_STREET, P_STREET));
    }

    /**
     * Street Default Constructor.
     */
    public StreetAddress() {
        super();
    }

    /**
     * Convenience Street constructor. AptBox set to default
     */
    public StreetAddress(int buildNo, String streetName) {
        super();
        setBuildNo(new Integer(buildNo));
        setStreetName(streetName);
    }

    /**
     * Convenience Street constructor.
     */
    public StreetAddress(int buildNo, String aptBox, String streetName) {
        super();
        setBuildNo(new Integer(buildNo));
        setAptBox(aptBox);
        setStreetName(streetName);
    }

    @Override
	public boolean equals(Object ob) {
        return toString().equals(ob.toString());
    }

    /**
     * the appartment number
     */
    private String getAptBox() {
        if (aptBox == null)
            aptBox = APTBOX_DEFAULT;
        return aptBox;
    }

    /**
     * Returns the building number
     */
    private Integer getBuildNo() {
        if (buildNo == null)
            buildNo = BUILD_NO_DEFAULT;
        return buildNo;
    }

    /**
     * Returns the descriptors
     */
    private static Vector<TextPropertyDescriptor> getDescriptors() {
        return descriptors;
    }

    @Override
	public Object getEditableValue() {
        return this.toString();
    }

    @Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
        return getDescriptors().toArray(
                new IPropertyDescriptor[getDescriptors().size()]);
    }

    /**
     * The <code>Name</code> implementation of this
     * <code>IPropertySource</code> method returns the following properties
     *
     * 	1) P_BUILD_NO returns java.lang.Integer
     * 	2) P_APTBOX returns java.lang.String
     *	3) P_STREET returns java.lang.String
     */
    @Override
	public Object getPropertyValue(Object propKey) {
        if (propKey.equals(P_ID_BUILD_NO))
            return getBuildNo().toString();
        if (propKey.equals(P_ID_APTBOX))
            return getAptBox();
        if (propKey.equals(P_ID_STREET))
            return getStreetName();
        return null;
    }

    /**
     * Returns the street name
     */
    private String getStreetName() {
        if (streetName == null)
            streetName = STREETNAME_DEFAULT;
        return streetName;
    }

    @Override
	public int hashCode() {
        return toString().hashCode();
    }

    @Override
	public boolean isPropertySet(Object property) {
        if (property.equals(P_ID_BUILD_NO))
            return getBuildNo() != BUILD_NO_DEFAULT;
        if (property.equals(P_ID_APTBOX))
            return getAptBox() != APTBOX_DEFAULT;
        if (property.equals(P_ID_STREET))
            return getStreetName() != STREETNAME_DEFAULT;
        return false;
    }

    @Override
	public void resetPropertyValue(Object property) {
        if (property.equals(P_ID_BUILD_NO)) {
            setBuildNo(BUILD_NO_DEFAULT);
            return;
        }
        if (property.equals(P_ID_APTBOX)) {
            setAptBox(APTBOX_DEFAULT);
            return;
        }
        if (property.equals(P_ID_STREET)) {
            setStreetName(STREETNAME_DEFAULT);
            return;
        }
    }

    /**
     * Sets the appartment number
     */
    private void setAptBox(String newAptBox) {
        aptBox = newAptBox;
    }

    /**
     * Sets the building number
     */
    private void setBuildNo(Integer newBuildNo) {
        buildNo = newBuildNo;
    }

    /**
     * The <code>Name</code> implementation of this
     * <code>IPropertySource</code> method
     * defines the following Setable properties
     *
     * 	1) P_BUILD_NO expects java.lang.Integer
     * 	2) P_APTBOX expects java.lang.String
     *	3) P_STREET expects java.lang.String
     */
    @Override
	public void setPropertyValue(Object name, Object value) {
        if (name.equals(P_ID_BUILD_NO)) {
            try {
                setBuildNo(new Integer(Integer.parseInt((String) value)));
            } catch (NumberFormatException e) {
                setBuildNo(BUILD_NO_DEFAULT);
            }
            return;
        }
        if (name.equals(P_ID_APTBOX)) {
            setAptBox((String) value);
            return;
        }
        if (name.equals(P_ID_STREET)) {
            setStreetName((String) value);
            return;
        }
    }

    /**
     * Sets the street name
     */
    private void setStreetName(String newStreetName) {
        streetName = newStreetName;
    }

    /**
     * The value as displayed in the Property Sheet. Will not print default values
     * @return java.lang.String
     */
    @Override
	public String toString() {
        StringBuffer outStringBuffer = new StringBuffer();
        if (!getAptBox().equals(APTBOX_DEFAULT)) {
            outStringBuffer.append(getAptBox());
            outStringBuffer.append(", "); //$NON-NLS-1$
        }
        if (!getBuildNo().equals(BUILD_NO_DEFAULT)) {
            outStringBuffer.append(getBuildNo());
            outStringBuffer.append(" "); //$NON-NLS-1$
        }
        if (!getStreetName().equals(STREETNAME_DEFAULT)) {
            outStringBuffer.append(getStreetName());
        }
        return outStringBuffer.toString();
    }
}
