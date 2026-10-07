/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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

package org.eclipse.e4.ui.internal.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.renderers.swt.CTabRendering;

/**
 * Single place for the "show pinned editors first" behavior of the editor
 * drop-down lists (chevron list and Ctrl+E). The preference only affects the
 * order of these lists, tabs are never moved.
 */
public final class PinnedEditorsOrder {

	private PinnedEditorsOrder() {
		// utility class
	}

	/**
	 * @return whether pinned editors should be listed before the other editors
	 */
	public static boolean isEnabled() {
		return Platform.getPreferencesService().getBoolean(
				CTabRendering.PREF_QUALIFIER_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT,
				CTabRendering.SHOW_PINNED_EDITORS_FIRST,
				CTabRendering.SHOW_PINNED_EDITORS_FIRST_DEFAULT,
				null);
	}

	/**
	 * @return whether the given part is pinned
	 */
	public static boolean isPinned(MPart part) {
		return part.getTags().contains(IPresentationEngine.ADORNMENT_PIN);
	}

	/**
	 * Returns the given elements with the pinned ones first. The relative order
	 * inside the pinned and inside the other elements is preserved. If the
	 * preference is disabled, the elements are returned unchanged.
	 *
	 * @param elements the elements in their regular order
	 * @param isPinned tells whether an element is pinned
	 * @return a list with the pinned elements first
	 */
	public static <T> List<T> pinnedFirst(List<T> elements, Predicate<? super T> isPinned) {
		if (!isEnabled()) {
			return elements;
		}
		List<T> result = new ArrayList<>(elements.size());
		for (T element : elements) {
			if (isPinned.test(element)) {
				result.add(element);
			}
		}
		for (T element : elements) {
			if (!isPinned.test(element)) {
				result.add(element);
			}
		}
		return result;
	}
}
