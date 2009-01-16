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
 * No Such Label event generated when the PDA program encounters an call to a
 * non-existant label in a PDA program.
 * 
 * <pre>
 *    E: no such label {label}
 * </pre>
 */
public class PDANoSuchLabelEvent extends PDAEvent {
    
    public final String fLabel;
    
    public PDANoSuchLabelEvent(String message) {
        super(message);
        fLabel = message.substring(getName(message).length() + 1);
    }
    
    public static boolean isEventMessage(String message) {
        return message.startsWith("no such label");
    }
    
    protected String getName(String message) {
        if (isEventMessage(message)) {
            return "no such label";
        }
        throw new IllegalArgumentException("Invalid event: " + message);
    }
}
