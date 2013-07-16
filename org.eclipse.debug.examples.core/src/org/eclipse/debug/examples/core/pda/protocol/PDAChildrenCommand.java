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
 * Retrieves data stack information 
 * 
 * <pre>
 *    C: children {thread_id} {frame_id} {variable_name}
 *    R: {child variable 1}|{child variable 2}|{child variable 3}|...|
 *    
 * Errors:
 *    error: invalid thread
 * </pre>
 */
public class PDAChildrenCommand extends PDACommand {

    public PDAChildrenCommand(int threadId, int frameId, String name  ) {
        super("children " + threadId + " " + frameId + " " + name); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public PDACommandResult createResult(String resultText) {
        return new PDAListResult(resultText);
    }
}
