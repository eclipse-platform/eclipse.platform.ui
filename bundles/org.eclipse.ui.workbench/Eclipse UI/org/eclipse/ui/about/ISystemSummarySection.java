/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.about;

import java.io.PrintWriter;

/**
 * Extensions to <code>org.eclipse.ui.systemSummaryExtensions</code> must provide
 * an implementation of this interface.  The class must provide a default
 * constructor.  A new instance of the class will be created each time the system
 * summary is created. 
 * 
 * @since 3.0
 */
public interface ISystemSummarySection {
    /**
     * A method that puts the section's information into the system summary's
     * configuration details log.
     * @param writer puts information into the system summary
     */
    public void write(PrintWriter writer);
}
