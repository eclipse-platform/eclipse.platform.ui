/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * Interface for reusable editors. 
 * 
 * An editors may support changing its input so that 
 * the workbench may change its contents instead of 
 * opening a new editor.
 */
public interface IReusableEditor extends IEditorPart {
    /**
     * Sets the input to this editor.
     * 
     * <p><b>Note:</b> Clients must fire the {@link IEditorPart#PROP_INPUT } 
     * property change within their implementation of 
     * <code>setInput()</code>.<p>
     *
     * @param input the editor input
     */
    public void setInput(IEditorInput input);
}

