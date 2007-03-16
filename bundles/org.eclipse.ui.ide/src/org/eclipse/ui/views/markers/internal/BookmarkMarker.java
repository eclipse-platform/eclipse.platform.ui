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

import org.eclipse.core.resources.IMarker;

/**
 * Represents a marker visible in the Bookmarks view. Currently, this adds no additional
 * fields to the ConcreteMarker class. However, if additional fields were added to the
 * bookmark view that are not general to all views, these fields would be added to this
 * class.
 */
public class BookmarkMarker extends ConcreteMarker {

    /**
     * @param toCopy
     */
    public BookmarkMarker(IMarker toCopy) {
        super(toCopy);
    }

}
