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
 * Started event generated when a new thread is started.  A started event 
 * is always sent for the first thread when a PDA program is started.
 * 
 * <pre>
 *    E: started {thread_id}
 * </pre>
 */
public class PDAStartedEvent extends PDARunControlEvent {
    
    public PDAStartedEvent(String message) {
        super(message);
    }
    
    public static boolean isEventMessage(String message) {
        return message.startsWith("started"); //$NON-NLS-1$
    }
}
