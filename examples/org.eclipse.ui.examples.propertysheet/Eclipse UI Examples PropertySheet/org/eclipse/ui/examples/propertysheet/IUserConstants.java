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
package org.eclipse.ui.examples.propertysheet;

/**
 * This interface contains constants for use only within the
 * property sheet example.
 */
public interface IUserConstants {
    public static final String PLUGIN_ID = "org.eclipse.ui.examples.propertysheet"; //$NON-NLS-1$

    public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$

    public static final String P_CONTENT_OUTLINE = PREFIX + "content_outline"; //$NON-NLS-1$

    public static final String EXTENSION = "usr"; //$NON-NLS-1$

    public static final String ATT_CLASS = "class"; //$NON-NLS-1$
}