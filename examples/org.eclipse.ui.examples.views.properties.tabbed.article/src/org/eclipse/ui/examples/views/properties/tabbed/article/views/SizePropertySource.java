/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.article.views;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Property source for the size properties.
 * 
 * @author Anthony Hunter
 */
public class SizePropertySource
    implements IPropertySource {

    protected ButtonElement element;

    /**
     * The width.
     */
    public static String ID_WIDTH = "Width"; //$NON-NLS-1$

    /**
     * The height.
     */
    public static String ID_HEIGHT = "Height"; //$NON-NLS-1$

    protected static IPropertyDescriptor[] descriptors;

    static {
        descriptors = new IPropertyDescriptor[] {
            new TextPropertyDescriptor(ID_WIDTH, "width"),//$NON-NLS-1$
            new TextPropertyDescriptor(ID_HEIGHT, "height")//$NON-NLS-1$
        };
    }

    protected Point point = null;

    /**
     * The constructor for SizePropertySource.
     * 
     * @param m_element
     *            the button element.
     * @param point
     *            the size of the button element.
     */
    public SizePropertySource(ButtonElement m_element, Point point) {
        this.point = new Point(point.x, point.y);
        element = m_element;
    }

    /**
     * Fire a property change event.
     * 
     * @param propName
     *            the name of the property change.
     */
    protected void firePropertyChanged() {
        Control ctl = element.getControl();

        if (ctl == null) {
            // the GUIView is probably hidden in this case
            return;
        }
        ctl.setSize(point);
    }

    public Object getEditableValue() {
        return this;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        return descriptors;
    }

    public Object getPropertyValue(Object propName) {
        if (ID_WIDTH.equals(propName)) {
            return new String(new Integer(point.x).toString());
        }
        if (ID_HEIGHT.equals(propName)) {
            return new String(new Integer(point.y).toString());
        }
        return null;
    }

    /**
     * Retrieve the value of the size property.
     * 
     * @return the value of the size property.
     */
    public Point getValue() {
        return new Point(point.x, point.y);
    }

    /**
     * @see org.eclipse.ui.views.properties.IPropertySource#isPropertySet(Object)
     */
    public boolean isPropertySet(Object propName) {
        if (ID_WIDTH.equals(propName) || ID_HEIGHT.equals(propName))
            return true;
        return false;
    }

    public void resetPropertyValue(Object propName) {
        //
    }

    public void setPropertyValue(Object propName, Object value) {
        int newInt;
        try {
            newInt = Integer.parseInt((String) value);
        } catch (NumberFormatException e) {
            newInt = -1;
        }

        if (newInt > 0) {
            if (ID_WIDTH.equals(propName)) {
                point.x = newInt;
            } else if (ID_HEIGHT.equals(propName)) {
                point.y = newInt;
            }
        }
        firePropertyChanged();
    }

    public String toString() {
        return point.toString();
    }

}
