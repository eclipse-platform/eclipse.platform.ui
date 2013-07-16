/*******************************************************************************
 * Copyright (c) 2009, 2013 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     IBM Corporation - bug fixing
 *******************************************************************************/
package org.eclipse.debug.examples.core.pda.protocol;

/**
 * Exited event generated when a thread has exited.
 * 
 * <pre>
 *    E: started {thread_id}
 * </pre>
 */
public class PDAExitedEvent extends PDARunControlEvent {
    
    public PDAExitedEvent(String message) {
        super(message);
    }
    
    public static boolean isEventMessage(String message) {
        return message.startsWith("exited"); //$NON-NLS-1$
    }
}
