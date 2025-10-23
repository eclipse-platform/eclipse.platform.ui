/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.search.core.tests;



import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.search2.internal.ui.InternalSearchUI;

public class QueryManagerTest {

	@Test
	public void testRemoveQuery() {
		ISearchQuery query= new NullQuery();
		InternalSearchUI.getInstance().addQuery(query);
		InternalSearchUI.getInstance().removeQuery(query);
		ISearchQuery[] all= InternalSearchUI.getInstance().getQueries();
		for (ISearchQuery a : all) {
			if (query == a) {
				assertTrue(false);
			}
		}
	}

	@Test
	public void testAddQuery() {
		ISearchQuery query= new NullQuery();
		InternalSearchUI.getInstance().addQuery(query);
		ISearchQuery[] all= NewSearchUI.getQueries();
		for (ISearchQuery a : all) {
			if (query == a) {
				return;
			}
		}
		assertTrue(false);
	}

	@Test
	public void testQueryListener() {
		final boolean [] wasAdded= { false };
		final boolean [] wasRemoved= { false };
		IQueryListener l= new IQueryListener() {
			@Override
			public void queryAdded(ISearchQuery query) {
				wasAdded[0]= true;
			}
			@Override
			public void queryRemoved(ISearchQuery query) {
				wasRemoved[0]= true;
			}
			@Override
			public void queryStarting(ISearchQuery query) {
				// not interested
			}
			@Override
			public void queryFinished(ISearchQuery query) {
				// not interested
			}
		} ;
		NewSearchUI.addQueryListener(l);
		ISearchQuery query= new NullQuery();
		InternalSearchUI.getInstance().addQuery(query);
		assertTrue(wasAdded[0]);
		InternalSearchUI.getInstance().removeQuery(query);
		assertTrue(wasRemoved[0]);
		InternalSearchUI.getInstance().removeQueryListener(l);
		wasAdded[0]= false;
		wasRemoved[0]= false;
		InternalSearchUI.getInstance().addQuery(query);
		assertFalse(wasAdded[0]);
		InternalSearchUI.getInstance().removeQuery(query);
		assertFalse(wasRemoved[0]);
	}
}
