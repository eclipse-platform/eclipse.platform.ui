/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.components.services;

import org.eclipse.core.runtime.IStatus;

/**
 * Service for logging errors. Messages logged using this interface are intended
 * to be read by the application developer, and will not normally be shown to the
 * end-user. Implementations of this interface will determine where exceptions and 
 * errors should be recorded. 
 * 
 * <p>
 * Not intended to be implemented by clients.
 * </p>
 * 
 * <p>EXPERIMENTAL: The components framework is currently under active development. All
 * aspects of this class including its existence, name, and public interface are likely
 * to change during the development of Eclipse 3.1</p>
 * 
 * @since 3.1
 */
public interface ISystemLog {
    
    /**
     * Writes the given status message to the log.
     * 
     * @param toLog status message to log
     */
    public void log(IStatus toLog);
    
    /**
     * Writes the given exception to the log.
     * 
     * @param t the exception to log
     */
    public void log(Throwable t);
}
