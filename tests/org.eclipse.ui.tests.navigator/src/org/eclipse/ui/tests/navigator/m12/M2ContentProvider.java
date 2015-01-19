/*******************************************************************************
 * Copyright (c) 2009, 2010 Fair Isaac Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fair Isaac Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.navigator.m12;

import org.eclipse.ui.tests.navigator.m12.model.M2Core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** Replaces M1Files whose name contain the character 2 with M2Files. */
public class M2ContentProvider extends ResourceWrapperContentProvider {
	@Override
	public void getPipelinedChildren(Object aParent, Set currentChildren) {
		List newElements = new ArrayList();
		for (Iterator it = currentChildren.iterator(); it.hasNext();) {
			Object child = it.next();
			Object newChild = M2Core.getModelObject(child);
			if (newChild != null) {
				it.remove();
				newElements.add(newChild);
			}
		}
		currentChildren.addAll(newElements);
	}

	@Override
	public boolean hasPipelinedChildren(Object anInput, boolean currentHasChildren) {
		return currentHasChildren;
	}

	@Override
	protected Object _convertToModelObject(Object object) {
		return M2Core.getModelObject(object);
	}

	public static int getInterceptAddCount() {
		return getCounter(M2ContentProvider.class.getName(), INTERCEPT_ADD);
	}

	/**
	 * @return Returns the _interceptRemoveCount.
	 */
	public static int getInterceptRemoveCount() {
		return getCounter(M2ContentProvider.class.getName(), INTERCEPT_REMOVE);
	}

	/**
	 * @return Returns the _interceptRefreshCount.
	 */
	public static int getInterceptRefreshCount() {
		return getCounter(M2ContentProvider.class.getName(), INTERCEPT_REFRESH);
	}

	/**
	 * @return Returns the _interceptUpdateCount.
	 */
	public static int getInterceptUpdateCount() {
		return getCounter(M2ContentProvider.class.getName(), INTERCEPT_UPDATE);
	}

	public static void resetCounters() {
		resetCounters(M2ContentProvider.class.getName());
	}
}