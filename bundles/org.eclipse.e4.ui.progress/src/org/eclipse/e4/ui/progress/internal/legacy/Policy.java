/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal.legacy;

/**
 * A common facility for parsing the <code>org.eclipse.ui/.options</code>
 * file.
 *
 * @since 2.1
 */
public class Policy {
    public static boolean DEFAULT = false;


    /**
     * Whether or not to show system jobs at all times.
     */
    public static boolean DEBUG_SHOW_ALL_JOBS = DEFAULT;

}
