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
 * 
 */
public class ProblemMarker extends ConcreteMarker {

    private int severity;

    public ProblemMarker(IMarker toCopy) {
        super(toCopy);

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.views.markers.internal.ConcreteMarker#refresh()
     */
    public void refresh() {
        super.refresh();
        severity = getMarker().getAttribute(IMarker.SEVERITY, -1);
    }

    public int getSeverity() {
        return severity;
    }
}
