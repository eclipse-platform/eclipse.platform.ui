/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.graphics.Image;

public class FieldPriority implements IField {

    static final String DESCRIPTION_IMAGE_PATH = "obj16/header_priority.gif"; //$NON-NLS-1$

    static final String HIGH_PRIORITY_IMAGE_PATH = "obj16/hprio_tsk.gif"; //$NON-NLS-1$

    static final String LOW_PRIORITY_IMAGE_PATH = "obj16/lprio_tsk.gif"; //$NON-NLS-1$

    private String description;

    private Image image;

    public FieldPriority() {
        description = Messages.getString("priority.description"); //$NON-NLS-1$
        image = ImageFactory.getImage(DESCRIPTION_IMAGE_PATH);
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
        return ""; //$NON-NLS-1$
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
        return ""; //$NON-NLS-1$
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#getImage(java.lang.Object)
     */
    public Image getImage(Object obj) {
        if (obj == null || !(obj instanceof TaskMarker)) {
            return null;
        }
        try {
            int priority = ((TaskMarker) obj).getPriority();
            if (priority == IMarker.PRIORITY_HIGH) {
                return ImageFactory.getImage(HIGH_PRIORITY_IMAGE_PATH);
            }
            if (priority == IMarker.PRIORITY_LOW) {
                return ImageFactory.getImage(LOW_PRIORITY_IMAGE_PATH);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.IField#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object obj1, Object obj2) {
        if (obj1 == null || obj2 == null || !(obj1 instanceof TaskMarker)
                || !(obj2 instanceof TaskMarker)) {
            return 0;
        }
        int priority1 = ((TaskMarker) obj1).getPriority();
        int priority2 = ((TaskMarker) obj2).getPriority();
        return priority1 - priority2;
    }

}