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
package org.eclipse.ui.tests.util;

import org.eclipse.swt.SWT;

/**
 * The Platform Util class is used to test for which platform we are in
 */
public class PlatformUtil {

    public static boolean onLinux() {

        String platform = SWT.getPlatform();
        return (platform.equals("motif") || platform.equals("gtk"));
    }
    
    /**
     * Determine if we are running on the Mac platform.
     * 
     * @return true if we are runnig on the Mac platform.
     */
    public static boolean onMac() {
        String platform = SWT.getPlatform();
        return platform.equals("carbon");
    }
}