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
 * VM Resumed event generated when the whole virtual machine is resumed.  When 
 * the VM is resumed all previously suspended threads are resumed as well. 
 * 
 * <pre>
 *    E: vmresumed [reason]
 * </pre>
 * 
 * <code>[reason]</code> is the cause of the resume: and it's optional:
 * <ul>
 *   <li><code>step</code> - a step request has been initiated</li>
 *   <li><code>client</code> - a client request to resume has been initiated</li>
 * </ul>
 */
public class PDAVMResumedEvent extends PDARunControlEvent {
    
    public PDAVMResumedEvent(String message) {
        super(message);
    }
    
    public static boolean isEventMessage(String message) {
        return message.startsWith("vmresumed"); //$NON-NLS-1$
    }
}
