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
package org.eclipse.jface.bindings;

import java.util.Comparator;
import java.util.Map;

/**
 * 
 * There is an ordering in which the search is applied. This ordering is the
 * priority certain properties are given within the binding. The order for
 * bindings is scheme, context, type, platform, and locale.
 * 
 * There are some some special cases that need mentioning. First of all, there
 * is a linear ordering to schemes, types, platforms and locales. This is not
 * the case for contexts. It is possible for two active contexts to be siblings
 * or even completely even completely unrelated. So, if an inheritance
 * relationship is defined, then a conflict can be resolved. If two bindings
 * belong to contexts who are not ancestors/descendents of each other, then a
 * conflict arises.
 * 
 * The second thing to consider is that it is possible to unbind something. An
 * unbinding is identified by a null command identifier. An unbinding has to
 * match on almost other property -- including the trigger, but excluding the
 * type. The trigger needs to be included so that we know how to match
 * (otherwise all bindings in a particular context, a particular platform and a
 * particular locale would be removed). The type needs to be excluded so that
 * the user can override system bindings.
 * 
 * @since 3.1
 */
final class BindingComparator implements Comparator {

	/**
	 * The tree of contexts to be used for all of the comparison. All of the
	 * keys should be active context identifiers (i.e., never <code>null</code>).
	 * The values will be their parents (i.e., possibly <code>null</code>).
	 * Both keys and values are context identifiers (<code>String</code>).
	 * This map may be empty, but is should never be <code>null</code>.
	 */
	private final Map contextTree;

	/**
	 * The array of scheme identifiers, starting with the active scheme and
	 * moving up through its parents.
	 */
	private final String[] schemeIds;

	/**
	 * Constructs a new instance of <code>BindingComparator</code>.
	 * 
	 * @param activeContextTree
	 *            The tree of contexts to be used for all of the comparison. All
	 *            of the keys should be active context identifiers (i.e., never
	 *            <code>null</code>). The values will be their parents (i.e.,
	 *            possibly <code>null</code>). Both keys and values are
	 *            context identifiers (<code>String</code>). This map may be
	 *            empty, but is should never be <code>null</code>.
	 * @param activeSchemeIds
	 *            The array of scheme identifiers, starting with the active
	 *            scheme and moving up through its parents. This value may be
	 *            empty, but it is never <code>null</code>.
	 */
	BindingComparator(final Map activeContextTree,
			final String[] activeSchemeIds) {
		contextTree = activeContextTree;
		schemeIds = activeSchemeIds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(final Object object1, final Object object2) {
		final Binding binding1 = (Binding) object1;
		final Binding binding2 = (Binding) object2;

		final String scheme1 = binding1.getSchemeId();
		final String scheme2 = binding2.getSchemeId();
		Binding mostSpecificScheme = null;
		if (!scheme1.equals(scheme2)) {
			for (int i = 0; i < schemeIds.length; i++) {
				final String schemeId = schemeIds[i];
				if (scheme1.equals(schemeId)) {
					mostSpecificScheme = binding1;
					break;
				}
				if (scheme2.equals(schemeId)) {
					mostSpecificScheme = binding2;
					break;
				}
			}
		}
		
		if (mostSpecificScheme == binding1) {
			return -1;
		}
		if (mostSpecificScheme == binding2) {
			return 1;
		}
		return 0;
	}

}
