/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

/**
 * @since 3.1
 */
public interface IPersistableFactoryIdentifier {
    /**
     * Returns the id of the element factory which should be used to re-create this
     * object.
     * <p>
     * Factory ids are declared in extensions to the standard extension point
     * <code>"org.eclipse.ui.elementFactories"</code>.
     * </p>
     * 
     * @return the element factory id
     * @see IElementFactory
     */
    public String getFactoryId();
}
