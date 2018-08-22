/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
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

package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.ui.workbench.Selector;

public class ToolItemUpdater {

	private List<AbstractContributionItem> itemsToCheck = new ArrayList<>();
	private final List<AbstractContributionItem> orphanedToolItems = new ArrayList<>();

	void registerItem(AbstractContributionItem item) {
		if (!itemsToCheck.contains(item)) {
			itemsToCheck.add(item);
		}
	}

	void removeItem(AbstractContributionItem item) {
		itemsToCheck.remove(item);
	}

	public void updateContributionItems(Selector selector) {
		for (final AbstractContributionItem ci : itemsToCheck) {
			if (ci.getModel() != null && ci.getModel().getParent() != null) {
				if (selector.select(ci.getModel())) {
					ci.updateItemEnablement();
				}
			} else {
				orphanedToolItems.add(ci);
			}
		}
		if (!orphanedToolItems.isEmpty()) {
			itemsToCheck.removeAll(orphanedToolItems);
			orphanedToolItems.clear();
		}
	}
}
