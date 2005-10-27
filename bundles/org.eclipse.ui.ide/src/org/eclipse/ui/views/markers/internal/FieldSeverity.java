/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
 * The FieldSeverity is the field for setting severities.
 *
 */
public class FieldSeverity implements IField {

    private String description;

    /**
     * Create a new instance of the receiver.
     */
    public FieldSeverity() {
        description = MarkerMessages.problemSeverity_description;
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
        return null;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderText()
     */
    public String getColumnHeaderText() {
        return ""; //$NON-NLS-1$
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#getColumnHeaderImage()
     */
    public Image getColumnHeaderImage() {
        return null;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#getValue(java.lang.Object)
     */
    public String getValue(Object obj) {
    	return Util.EMPTY_STRING;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
     */
    public Image getImage(Object obj) {
        if (obj == null || !(obj instanceof ProblemMarker)) {
            return null;
        }

       return Util.getImage(((ProblemMarker) obj).getSeverity());
    }
 

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null || !(obj1 instanceof ProblemMarker)
                || !(obj2 instanceof ProblemMarker)) {
            return 0;
        }

        int severity1 = ((ProblemMarker) obj1).getSeverity();
        int severity2 = ((ProblemMarker) obj2).getSeverity();
        return severity1 - severity2;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#getCategoryValue(java.lang.Object)
	 */
	public String getCategoryValue(Object obj) {
		return Util.getSeverityText(((ProblemMarker) obj).getSeverity());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.views.markers.internal.IField#isCategoryField()
	 */
	public boolean isCategoryField() {
		return true;
	}

}
