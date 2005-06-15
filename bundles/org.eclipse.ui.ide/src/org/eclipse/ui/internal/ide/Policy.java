/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import org.eclipse.core.runtime.Platform;

public class Policy {
    public static final boolean DEFAULT = false;

    public static boolean DEBUG_OPEN_ERROR_DIALOG = DEFAULT;
    public static boolean DEBUG_GC = DEFAULT;

    static {
        if (getDebugOption("/debug")) { //$NON-NLS-1$
            DEBUG_OPEN_ERROR_DIALOG = getDebugOption("/debug/internalerror/openDialog"); //$NON-NLS-1$
            DEBUG_GC = getDebugOption("/debug/gc"); //$NON-NLS-1$
        }
    }

    private static boolean getDebugOption(String option) {
        return "true".equalsIgnoreCase(Platform.getDebugOption(IDEWorkbenchPlugin.IDE_WORKBENCH + option)); //$NON-NLS-1$
    }
}
