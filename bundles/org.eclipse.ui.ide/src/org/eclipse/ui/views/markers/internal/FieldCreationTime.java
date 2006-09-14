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

package org.eclipse.ui.views.markers.internal;

import org.eclipse.swt.graphics.Image;

/**
 * Creation time field. Designed to display and compare creation times of IMarker objects.
 */
public class FieldCreationTime extends AbstractField {

    private String description;

    private Image image;

    /**
     * The constructor
     */
    public FieldCreationTime() {
        description = MarkerMessages.description_creationTime;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#getDescriptionImage()
     */
    public Image getDescriptionImage() {
        return image;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderText()
     */
    public String getColumnHeaderText() {
        return description;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderImage()
     */
    public Image getColumnHeaderImage() {
        return image;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
     */
    public String getValue(Object obj) {
        if (obj == null || !(obj instanceof ConcreteMarker)) {
            return ""; //$NON-NLS-1$
        }
        ConcreteMarker marker = (ConcreteMarker) obj;
        return String.valueOf(marker.getCreationTime());
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
     */
    public Image getImage(Object obj) {
        return null;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null || !(obj1 instanceof ConcreteMarker)
                || !(obj2 instanceof ConcreteMarker)) {
            return 0;
        }

        ConcreteMarker marker1 = (ConcreteMarker) obj1;
        ConcreteMarker marker2 = (ConcreteMarker) obj2;

        long value = marker1.getCreationTime() - marker2.getCreationTime();
        if (value < 0) {
            return -1;
        } else if (value > 0) {
            return 1;
        } else {
			return 0;
		}
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getDefaultDirection()
	 */
	public int getDefaultDirection() {
		return TableComparator.ASCENDING;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getPreferredWidth()
	 */
	public int getPreferredWidth() {
		return 0;
	}
}
