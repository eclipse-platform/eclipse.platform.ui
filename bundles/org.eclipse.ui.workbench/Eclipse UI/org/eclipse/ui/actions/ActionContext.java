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
package org.eclipse.ui.actions;

import org.eclipse.jface.viewers.ISelection;

/**
 * An <code>ActionContext</code> represents the context used to determine
 * which actions are added by an <code>ActionGroup</code>, and what their 
 * enabled state should be.
 * <p>
 * This class encapsulates a selection and an input element.
 * Clients may subclass this class to add more information to the context.
 * </p>
 */
public class ActionContext {

    /**
     * The selection.
     */
    private ISelection selection;

    /**
     * The input element.
     */
    private Object input;

    /**
     * Creates a new action context with the given selection.
     */
    public ActionContext(ISelection selection) {
        setSelection(selection);
    }

    /**
     * Returns the selection.
     */
    public ISelection getSelection() {
        return selection;
    }

    /**
     * Sets the selection.
     */
    public void setSelection(ISelection selection) {
        this.selection = selection;
    }

    /**
     * Returns the input element.
     */
    public Object getInput() {
        return input;
    }

    /**
     * Sets the input element.
     * 
     * @param input
     */
    public void setInput(Object input) {
        this.input = input;
    }
}
