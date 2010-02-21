/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

/**
 * A theme element whose value may default to that of another theme element.
 * 
 * @since 3.0
 */
public interface IHierarchalThemeElementDefinition extends
        IThemeElementDefinition {

    /**
     * Return the id of the element this element defaults to.
     * 
     * @return the id of the element this element defaults to, or 
     * <code>null</code> if it does not default to another element.
     */
    String getDefaultsTo();
}
