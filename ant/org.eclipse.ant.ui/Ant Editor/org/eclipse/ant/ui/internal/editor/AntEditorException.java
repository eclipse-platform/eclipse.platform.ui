/*******************************************************************************
 * Copyright (c) 2002, 2003 GEBIT Gesellschaft fuer EDV-Beratung
 * und Informatik-Technologien mbH, 
 * Berlin, Duesseldorf, Frankfurt (Germany).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     GEBIT Gesellschaft fuer EDV-Beratung und Informatik-Technologien mbH - initial API and implementation
 *******************************************************************************/

package org.eclipse.ant.ui.internal.editor;

/**
 * Exception that might be thrown in the Ant Editor context.
 * 
 * @author Alf Schiefelbein
 */
public class AntEditorException extends RuntimeException {

    /**
     * Constructor for AntEditorException.
     */
    public AntEditorException() {
        super();
    }

    /**
     * Constructor for AntEditorException.
     * @param s
     */
    public AntEditorException(String s) {
        super(s);
    }
}