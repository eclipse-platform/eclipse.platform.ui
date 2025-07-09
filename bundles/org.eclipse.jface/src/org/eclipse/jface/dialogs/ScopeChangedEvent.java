/*******************************************************************************
 * Copyright (c) 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Inc. - copied and modified from PageChangedEvent.java
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import java.util.EventObject;

/**
 * Event object describing a page selection change. The source of these events
 * is a page change provider.
 *
 * @see IPageChangeProvider
 * @see IPageChangedListener
 *
 * @since 3.38
 */
public class ScopeChangedEvent extends EventObject {

	private static final long serialVersionUID = -2652600407410991930L;

	/**
	 * The changed scope.
	 */
	private final int scope;

	/**
	 * Creates a new event for the given source and new scope.
	 *
	 * @param source the page change provider
	 * @param scope  the new scope. In the JFace provided dialogs this will be an
	 *               <code>ISearchPageContainer</code> constant.
	 */
	public ScopeChangedEvent(IPageChangeProvider source,
			int scope) {
		super(source);
		this.scope = scope;
	}

	/**
	 * Returns the new scope.
	 *
	 * @return the new scope. In dialogs implemented by JFace, this will be an
	 *         <code>ISearchPageContainer</code> constant.
	 */
	public int getScope() {
		return scope;
	}

	/**
	 * Returns the scope change provider that is the source of this event.
	 *
	 * @return the originating scope change provider
	 */
	public IScopeChangeProvider getPageChangeProvider() {
		return (IScopeChangeProvider) getSource();
	}
}
