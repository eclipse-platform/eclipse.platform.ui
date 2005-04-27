/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.search;

/**
 * The generic search scope object. This is a tagging interface
 * since each search engine is expected to have its own non-overlapping
 * set of scopes that users can include in or exclude from the search. 
 * Clients are expected to create scope objects that implement this
 * interface and pass them to the search engine.
 * 
 * @since 3.1
 */
public interface ISearchScope {
}
