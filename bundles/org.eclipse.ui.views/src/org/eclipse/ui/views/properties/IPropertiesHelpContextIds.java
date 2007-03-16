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
package org.eclipse.ui.views.properties;

import org.eclipse.ui.PlatformUI;

/**
 * Help context ids for the properties view.
 * <p>
 * This interface contains constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 */
/*package*/interface IPropertiesHelpContextIds {
    public static final String PREFIX = PlatformUI.PLUGIN_ID + "."; //$NON-NLS-1$

    // Actions
    public static final String CATEGORIES_ACTION = PREFIX
            + "properties_categories_action_context"; //$NON-NLS-1$

    public static final String DEFAULTS_ACTION = PREFIX
            + "properties_defaults_action_context"; //$NON-NLS-1$

    public static final String FILTER_ACTION = PREFIX
            + "properties_filter_action_context"; //$NON-NLS-1$

    public static final String COPY_PROPERTY_ACTION = PREFIX
            + "properties_copy_action_context"; //$NON-NLS-1$

    // Views
    public static final String PROPERTY_SHEET_VIEW = PREFIX
            + "property_sheet_view_context"; //$NON-NLS-1$
}
