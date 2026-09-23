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
package org.eclipse.ui.internal.views.markers;

import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;

/**
 * The HighlightedResourcesView displays all highlighted resources.
 *
 * @since 3.4
 */
public class HighlightedResourcesView extends MarkerSupportView {

	public HighlightedResourcesView() {
		super(MarkerSupportRegistry.HIGHLIGHTED_RESOURCES_GENERATOR);
	}
}
