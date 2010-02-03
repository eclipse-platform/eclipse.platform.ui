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

package org.eclipse.ui.keys;

import org.eclipse.ui.internal.e4.compatibility.E4Util;

/**
 * A cache for formatters. It keeps a few instances of pre-defined instances of
 * <code>IKeyFormatter</code> available for use. It also allows the default
 * formatter to be changed.
 * 
 * @deprecated Please use org.eclipse.jface.bindings.keys.KeyFormatterFactory
 * @since 3.0
 * @see org.eclipse.ui.keys.IKeyFormatter
 */
public final class KeyFormatterFactory {
    /**
     * Provides an instance of <code>CompactKeyFormatter</code>.
     * 
     * @return The compact formatter; never <code>null</code>.
     */
    public static final IKeyFormatter getCompactKeyFormatter() {
		// TODO compat: getCompactKeyFormatter
		E4Util.unsupported("getCompactKeyFormatter"); //$NON-NLS-1$
		return null;
    }

    /**
     * An accessor for the current default key formatter.
     * 
     * @return The default formatter; never <code>null</code>.
     */
    public static IKeyFormatter getDefault() {
		// TODO compat: getDefault
		E4Util.unsupported("getDefault"); //$NON-NLS-1$
		return null;
    }

    /**
     * Provides an instance of <code>EmacsKeyFormatter</code>.
     * 
     * @return The Xemacs formatter; never <code>null</code>.
     */
    public static IKeyFormatter getEmacsKeyFormatter() {
		// TODO compat: getEmacsKeyFormatter
		E4Util.unsupported("getEmacsKeyFormatter"); //$NON-NLS-1$
		return null;
    }

    /**
     * Provides an instance of <code>FormalKeyFormatter</code>.
     * 
     * @return The formal formatter; never <code>null</code>.
     */
    public static IKeyFormatter getFormalKeyFormatter() {
		// TODO compat: getFormalKeyFormatter
		E4Util.unsupported("getFormalKeyFormatter"); //$NON-NLS-1$
		return null;
    }

    /**
     * Sets the default key formatter.
     * 
     * @param defaultKeyFormatter
     *            the default key formatter. Must not be <code>null</code>.
     */
    public static void setDefault(IKeyFormatter defaultKeyFormatter) {
		// TODO compat: setDefault
		E4Util.unsupported("setDefault"); //$NON-NLS-1$
    }

    private KeyFormatterFactory() {
        // Not to be constructred.
    }
}
