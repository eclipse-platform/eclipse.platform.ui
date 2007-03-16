/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation 
 *  	Dan Rubel <dan_rubel@instantiations.com>
 *        - Fix for bug 11490 - define hidden view (placeholder for view) in plugin.xml 
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.ui.IPageLayout;

/**
 * Encapsulates the perspective layout information for a view.
 * 
 * @since 3.0
 */
public class ViewLayoutRec {
    boolean isCloseable = true;

    boolean isMoveable = true;

    boolean isStandalone = false;

    boolean showTitle = true;

    float fastViewWidthRatio = IPageLayout.INVALID_RATIO;
}
