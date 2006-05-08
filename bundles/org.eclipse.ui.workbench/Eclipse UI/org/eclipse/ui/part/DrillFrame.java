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
package org.eclipse.ui.part;

import java.util.List;

/* (non-Javadoc)
 * A <code>DrillFrame</code> is used to record the input element and
 * selection state for one frame in a <code>DrillDownTreeViewer</code>.
 * This class is not intended for use beyond the package.
 */
/* package */class DrillFrame {
    Object fElement;

    Object fPropertyName;

    List fExpansion = null;

    /**
     * Allocates a new DrillFrame.
     *
     * @param oElement the tree input element
     * @param strPropertyName the visible tree property
     * @param vExpansion the current expansion state of the tree
     */
    public DrillFrame(Object oElement, Object strPropertyName, List vExpansion) {
        fElement = oElement;
        fPropertyName = strPropertyName;
        fExpansion = vExpansion;
    }

    /**
     * Compares two Objects for equality.
     * <p>
     *
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        // Compare handles.
        if (this == obj) {
			return true;
		}

        // Compare class.
        if (!(obj instanceof DrillFrame)) {
			return false;
		}

        // Compare contents.
        DrillFrame oOther = (DrillFrame) obj;
        return ((fElement == oOther.fElement) && (fPropertyName
                .equals(oOther.fPropertyName)));
    }

    /**
     * Returns the input element.
     *
     * @return the input element
     */
    public Object getElement() {
        return fElement;
    }

    /**
     * Returns the expansion state for a tree.
     *
     * @return the expansion state for a tree
     */
    public List getExpansion() {
        return fExpansion;
    }

    /**
     * Returns the property name.
     *
     * @return the property name
     */
    public Object getPropertyName() {
        return fPropertyName;
    }
}
