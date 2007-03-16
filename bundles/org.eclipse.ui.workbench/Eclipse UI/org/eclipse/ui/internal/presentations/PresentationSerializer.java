/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import java.util.Collections;
import java.util.List;

import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IPresentationSerializer;

/**
 * This class is used to map IPresentableParts onto string IDs
 */
public class PresentationSerializer implements IPresentationSerializer {

    private List parts = Collections.EMPTY_LIST;

    public PresentationSerializer(List presentableParts) {
        parts = presentableParts;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentationSerializer#getId(org.eclipse.ui.presentations.IPresentablePart)
     */
    public String getId(IPresentablePart part) {
        int index = parts.indexOf(part);

        return "" + index; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.presentations.IPresentationSerializer#getPart(java.lang.String)
     */
    public IPresentablePart getPart(String id) {
        try {
            Integer integer = new Integer(id);
            int index = integer.intValue();

            IPresentablePart result = (IPresentablePart) parts.get(index);
            return result;

        } catch (NumberFormatException e) {
        } catch (IndexOutOfBoundsException e) {
        }

        return null;
    }

    /**
     * Prevent this object from being used further. Ensure that none
     * of the methods return anything useful in order to discourage clients
     * from hanging onto references to this object.
     */
    public void dispose() {
        parts = Collections.EMPTY_LIST;
    }

}
