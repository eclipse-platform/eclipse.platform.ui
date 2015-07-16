/*******************************************************************************
 * Copyright (c) 2014, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.ui.workbench.Selector;

public class ToolItemUpdater {

	private List<HandledContributionItem> itemsToCheck = new ArrayList<>();
	private final List<HandledContributionItem> orphanedToolItems = new ArrayList<>();

	private List<DirectContributionItem> directItemsToCheck = new ArrayList<>();
	private final List<DirectContributionItem> directOrphanedToolItems = new ArrayList<>();

	void registerItem(HandledContributionItem item) {
		if (!itemsToCheck.contains(item)) {
			itemsToCheck.add(item);
		}
	}

	void removeItem(HandledContributionItem item) {
		itemsToCheck.remove(item);
	}

	void registerItem(DirectContributionItem item) {
		if (!directItemsToCheck.contains(item)) {
			directItemsToCheck.add(item);
		}
	}

	void removeItem(DirectContributionItem item) {
		directItemsToCheck.remove(item);
	}

	public void updateContributionItems(Selector selector) {
		for (final HandledContributionItem hci : itemsToCheck) {
			if (hci.getModel() != null && hci.getModel().getParent() != null
					&& selector.select(hci.getModel())) {
				hci.updateItemEnablement();
			} else {
				orphanedToolItems.add(hci);
			}
		}
		if (!orphanedToolItems.isEmpty()) {
			itemsToCheck.removeAll(orphanedToolItems);
			orphanedToolItems.clear();
		}

		for (final DirectContributionItem dci : directItemsToCheck) {
			if (dci.getModel() != null && dci.getModel().getParent() != null && selector.select(dci.getModel())) {
				dci.updateItemEnablement();
			} else {
				directOrphanedToolItems.add(dci);
			}
		}
		if (!directOrphanedToolItems.isEmpty()) {
			directItemsToCheck.removeAll(directOrphanedToolItems);
			directOrphanedToolItems.clear();
		}
	}
}
