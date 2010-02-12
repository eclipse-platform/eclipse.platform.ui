/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help;

/**
 * ICriterionValueDefinition represents one criterion value definition of one criterion.
 * It includes value id and its display name.
 * 
 * @since 3.5
 */
public interface ICriterionValueDefinition extends IUAElement {

    /**
     * Returns the id that this criterion value
     *
     * @return the id
     */
    public String getId();

    /**
     * Obtains the display name associated with this criterion value.
     * 
     * @return the name
     */
    public String getName();
}
