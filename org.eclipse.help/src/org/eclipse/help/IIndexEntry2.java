/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
 * IIndexEntry2 is an index entry which may have see elements as children
 * 
 * @since 3.5
 */
public interface IIndexEntry2 extends IIndexEntry {

    /**
     * Obtains see references for this entry
     * 
     * @return array of ITopic
     */
    public IIndexSee[] getSees();

}
