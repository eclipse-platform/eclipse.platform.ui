/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.misc;

public class UIHackFinder {
    /**
     * Used to mark code/functionality that we may want in the future.
     */
    public static void fixFuture() {
    }

    /**
     * Used to mark code that must be fixed up related to error handling.
     */
    public static void fixHandler() {
    }

    /**
     * Used to mark code that must be fixed up related to ISV or plugin.
     */
    public static void fixPR() {
    }

    /**
     * Used to mark code that must be fixed up.
     */
    public static void fixUI() {
    }
}
