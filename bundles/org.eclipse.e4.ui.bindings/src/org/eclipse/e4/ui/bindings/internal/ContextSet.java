/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.e4.ui.bindings.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;

public class ContextSet {
	public static ContextSet EMPTY = new ContextSet(Collections.<Context> emptyList());

	public static class CComp implements Comparator<Context> {
		private final ContextManager manager;

		public CComp(ContextManager manager) {
			this.manager = manager;
		}

		@Override
		public int compare(Context o1, Context o2) {
			if (o1.equals(o2)) {
				return 0;
			}
			int l1 = getLevel(o1);
			int l2 = getLevel(o2);
			if (l1 != l2) {
				return l1 - l2;
			}
			return o1.getId().compareTo(o2.getId());
		}

		private int getLevel(Context c) {
			int l = 0;
			try {
				String parentId = c.getParentId();
				while (parentId != null) {
					l++;
					Context context = manager.getContext(parentId);
					parentId = context.getParentId();
				}
			} catch (NotDefinedException e) {
				// ignore
			}
			return l;
		}
	}

	private static Comparator<Context> CONTEXT_COMP = null;

	public static void setComparator(Comparator<Context> comp) {
		CONTEXT_COMP = comp;
	}

	public static Comparator<Context> getComparator() {
		return CONTEXT_COMP;
	}

	private final List<Context> contexts;

	public ContextSet(Collection<Context> c) {
		contexts = new ArrayList<>(c);
		contexts.sort(CONTEXT_COMP);
	}

	public List<Context> getContexts() {
		return contexts;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof ContextSet)) {
			return false;
		}
		return contexts.equals(((ContextSet) o).contexts);
	}

	@Override
	public int hashCode() {
		return contexts.hashCode();
	}
}
