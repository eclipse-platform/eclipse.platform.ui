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

import java.util.List;

/**
 * Listener interface that listens for changes in resource markers.
 */
public interface IMarkerChangedListener {

    /**
     * @param additions new markers added
     * @param removals markers removed
     * @param changes changed markers
     */
    public void markerChanged(List additions, List removals, List changes);

}