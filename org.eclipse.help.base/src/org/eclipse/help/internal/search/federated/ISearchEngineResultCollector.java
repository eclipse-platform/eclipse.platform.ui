/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search.federated;

/**
 * A collector for the search hits (asynchronously) returned by the federated search participants.
 */
public interface ISearchEngineResultCollector {
	void add(ISearchEngineResult searchResult);
    void add(ISearchEngineResult[] searchResults);
}
