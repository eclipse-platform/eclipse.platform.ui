/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.search;

import org.eclipse.update.core.*;

/**
 * Search results are collected by implementing this interface
 * and passing it to the search request. If the implementation is
 * visual, it is recommended that the match is shown as soon
 * as it is collected (rather than kept in a list and presented
 * at the end of the search).
 */
public interface IUpdateSearchResultCollector {
/**
 * Called when a matching feature has been found during
 * the search.
 * @param match the matching feature
 */
   void accept(IFeature match);
}