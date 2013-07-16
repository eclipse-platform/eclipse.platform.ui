/*******************************************************************************
 * Copyright (c) 2008, 2013 Wind River Systems and others.
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
 * Resumes the execution of the whole virtual machine  
 * 
 * <pre>
 *    C: vmresume
 *    R: ok
 *    E: vmresumed client
 *    
 * Errors:
 *    error: vm already running
 * </pre>
 */

public class PDAVMResumeCommand extends PDACommand {

    public PDAVMResumeCommand() {
        super("vmresume"); //$NON-NLS-1$
    }
    

    public PDACommandResult createResult(String resultText) {
        return new PDACommandResult(resultText);
    }
}
