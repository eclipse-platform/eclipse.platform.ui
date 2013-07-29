/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Hendrik Still <hendrik.still@gammas.de> - bug 412273
 *******************************************************************************/
package org.eclipse.jface.viewers;

/**
 * Interface common to all objects that provide an input.
 * @param <I> Type of the input
 */
public interface IInputProvider<I> {
    /**
     * Returns the input.
     *
     * @return the input object
     */
    public I getInput();
}
