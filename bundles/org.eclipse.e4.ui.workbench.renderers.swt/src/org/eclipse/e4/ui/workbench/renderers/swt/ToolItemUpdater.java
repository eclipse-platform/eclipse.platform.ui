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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.swt.widgets.Display;

public class ToolItemUpdater implements Runnable {

	private static int DELAY = 100;
	private long timestampOfEarliestQueuedUpdate = 0;
	private List<AbstractContributionItem> itemsToCheck = new ArrayList<>();
	private final List<AbstractContributionItem> orphanedToolItems = new ArrayList<>();
	private final Set<AbstractContributionItem> itemsToUpdateLater = new LinkedHashSet<>();

	public ToolItemUpdater() {
		String delayProperty = System.getProperty("ToolItemUpdaterDelayInMs"); //$NON-NLS-1$
		if (delayProperty != null) {
			DELAY = Integer.parseInt(delayProperty);
		}
	}

	void registerItem(AbstractContributionItem item) {
		if (!itemsToCheck.contains(item)) {
			itemsToCheck.add(item);
		}
	}

	void removeItem(AbstractContributionItem item) {
		itemsToCheck.remove(item);
	}

	public void updateContributionItems(Selector selector) {
		boolean doRunNow = false;
		for (final AbstractContributionItem ci : itemsToCheck) {
			if (ci.getModel() != null && ci.getModel().getParent() != null) {
				if (selector.select(ci.getModel())) {
					itemsToUpdateLater.add(ci);
					if (timestampOfEarliestQueuedUpdate == 0) {
						timestampOfEarliestQueuedUpdate = System.nanoTime();
					}
					if (System.nanoTime() - timestampOfEarliestQueuedUpdate > DELAY * 1_000_000) {
						// runnable was not called within the last DELAY milliseconds, do it now.
						// For scenario: a plugin is forcing that updateContributionItems is called
						// again and again in less than given DELAY frequency. TimerExec would then
						// never be executed.
						doRunNow = true;
					} else {
						Display.getDefault().timerExec(DELAY, this);
					}
				}
			} else {
				orphanedToolItems.add(ci);
			}
		}
		if (!orphanedToolItems.isEmpty()) {
			itemsToCheck.removeAll(orphanedToolItems);
			orphanedToolItems.clear();
		}
		if (doRunNow) {
			run();
		}
	}

	@Override
	public void run() {
		timestampOfEarliestQueuedUpdate = 0;
		AbstractContributionItem[] copy = itemsToUpdateLater.toArray(new AbstractContributionItem[] {});
		itemsToUpdateLater.clear();
		for (AbstractContributionItem it : copy) {
			it.updateItemEnablement();
		}
	}
}
