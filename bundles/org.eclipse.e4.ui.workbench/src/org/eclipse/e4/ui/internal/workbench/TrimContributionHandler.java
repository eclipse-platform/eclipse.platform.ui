/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.internal.workbench;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class TrimContributionHandler {

	@Inject
	private MApplication application;

	@Inject
	private IEventBroker broker;

	private Map<MTrimBar, List<MTrimElement>> trimContributions = new WeakHashMap<MTrimBar, List<MTrimElement>>();

	private EventHandler trimWidgetHandler = new EventHandler() {
		public void handleEvent(Event event) {
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (element instanceof MTrimBar) {
				Object value = event.getProperty(UIEvents.EventTags.NEW_VALUE);
				if (value == null) {
					cleanUp((MTrimBar) element);
				} else {
					contribute((MTrimBar) element);
				}
			}
		}
	};

	@PostConstruct
	void postConstruct() {
		broker.subscribe(UIEvents.buildTopic(UIEvents.UIElement.TOPIC, UIEvents.UIElement.WIDGET),
				trimWidgetHandler);
	}

	@PreDestroy
	void preDestroy() {
		broker.unsubscribe(trimWidgetHandler);
	}

	private void contribute(MTrimBar trimBar) {
		contribute(trimBar.getElementId(), trimBar);
	}

	private void contribute(String parentId, MTrimBar trimBar) {
		List<MTrimContribution> applicableContributions = new ArrayList<MTrimContribution>();
		for (MTrimContribution contribution : application.getTrimContributions()) {
			String targetId = contribution.getParentId();
			if (targetId != null && targetId.equals(parentId)) {
				applicableContributions.add(contribution);
			}
		}

		List<MTrimElement> contributions = ContributionsAnalyzer.addTrimBarContributions(trimBar,
				applicableContributions);
		trimContributions.put(trimBar, contributions);
	}

	private void cleanUp(MTrimBar trimBar) {
		List<MTrimElement> contributions = trimContributions.get(trimBar);
		if (contributions != null) {
			trimBar.getChildren().removeAll(contributions);
		}
	}
}
