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
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarContribution;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class ToolBarContributionHandler {

	@Inject
	private MApplication application;

	@Inject
	private IEventBroker broker;

	private Map<MToolBar, List<MToolBarElement>> toolBarContributions = new WeakHashMap<MToolBar, List<MToolBarElement>>();

	private EventHandler partObjectHandler = new EventHandler() {
		public void handleEvent(Event event) {
			Object value = event.getProperty(UIEvents.EventTags.NEW_VALUE);
			if (value != null) {
				Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
				if (element instanceof MPart) {
					contribute((MPart) element);
				}
			}
		}
	};

	private EventHandler partWidgetHandler = new EventHandler() {
		public void handleEvent(Event event) {
			Object value = event.getProperty(UIEvents.EventTags.NEW_VALUE);
			if (value == null) {
				Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
				if (element instanceof MPart) {
					cleanUp((MPart) element);
				}
			}
		}
	};

	// private EventHandler partObjectHandler2 = new EventHandler() {
	// public void handleEvent(Event event) {
	// Object value = event.getProperty(UIEvents.EventTags.NEW_VALUE);
	// if (value instanceof MTrimBar) {
	// contribute((MTrimBar) value);
	// }
	// }
	// };

	@PostConstruct
	void postConstruct() {
		broker.subscribe(
				UIEvents.buildTopic(UIEvents.Contribution.TOPIC, UIEvents.Contribution.OBJECT),
				partObjectHandler);
		broker.subscribe(UIEvents.buildTopic(UIEvents.UIElement.TOPIC, UIEvents.UIElement.WIDGET),
				partWidgetHandler);
		// broker.subscribe(UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
		// UIEvents.ElementContainer.CHILDREN), partObjectHandler2);
	}

	@PreDestroy
	void preDestroy() {
		broker.unsubscribe(partObjectHandler);
		broker.unsubscribe(partWidgetHandler);
		// broker.unsubscribe(partObjectHandler2);
	}

	private void contribute(MPart part) {
		MToolBar toolBar = part.getToolbar();
		if (toolBar != null) {
			contribute(part.getElementId(), toolBar);
		}
	}

	private void cleanUp(MPart part) {
		MToolBar toolBar = part.getToolbar();
		if (toolBar != null) {
			List<MToolBarElement> contributions = toolBarContributions.get(toolBar);
			if (contributions != null) {
				toolBar.getChildren().removeAll(contributions);
			}
		}
	}

	private void contribute(String parentId, MToolBar toolBar) {
		List<MToolBarContribution> applicableContributions = new ArrayList<MToolBarContribution>();
		for (MToolBarContribution contribution : application.getToolBarContributions()) {
			String targetId = contribution.getParentId();
			if (targetId != null && targetId.equals(parentId)) {
				applicableContributions.add(contribution);
			}
		}

		List<MToolBarElement> contributions = ContributionsAnalyzer.addToolBarContributions(
				toolBar, applicableContributions);
		toolBarContributions.put(toolBar, contributions);
	}

	// private void contribute(MTrimBar trimBar) {
	// contribute(trimBar.getElementId(), trimBar);
	// }
	//
	// private void contribute(String parentId, MTrimBar trimBar) {
	// List<MTrimContribution> applicableContributions = new ArrayList<MTrimContribution>();
	// for (MTrimContribution contribution : application.getTrimContributions()) {
	// String targetId = contribution.getParentId();
	// if (targetId != null && targetId.equals(parentId)) {
	// applicableContributions.add(contribution);
	// }
	// }
	//
	// ContributionsAnalyzer.addTrimBarContributions(trimBar, applicableContributions);
	// }
}
