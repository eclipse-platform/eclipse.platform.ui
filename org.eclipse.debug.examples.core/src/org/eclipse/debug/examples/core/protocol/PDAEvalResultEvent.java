/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.core.protocol;

/**
 * Eval result event generated when an evaluation has completed. 
 * 
 * <pre>
 *    E: evalresult {result}
 * </pre>
 */
public class PDAEvalResultEvent extends PDAEvent {
    
    public final String fResult;
    
    public PDAEvalResultEvent(String message) {
        super(message);
        fResult = message.substring(getName(message).length() + 1);
    }
    
    public static boolean isEventMessage(String message) {
        return message.startsWith("evalresult");
    }
}
