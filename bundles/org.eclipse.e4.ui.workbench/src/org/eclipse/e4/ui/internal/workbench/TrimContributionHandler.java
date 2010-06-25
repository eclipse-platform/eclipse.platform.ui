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
import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.internal.expressions.ReferenceExpression;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MCoreExpression;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MTrimContribution;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ExpressionContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class TrimContributionHandler {

	@Inject
	private MApplication application;

	@Inject
	private IEventBroker broker;

	@Inject
	private EModelService modelService;

	private Map<MTrimBar, List<MUIElement>> trimContributions = new WeakHashMap<MTrimBar, List<MUIElement>>();

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

	private boolean isVisible(MTrimContribution trimContribution, ExpressionContext eContext) {
		if (trimContribution.getVisibleWhen() == null) {
			return true;
		}
		MCoreExpression exp = (MCoreExpression) trimContribution.getVisibleWhen();
		Expression ref = null;
		if (exp.getCoreExpression() instanceof Expression) {
			ref = (Expression) exp.getCoreExpression();
		} else {
			ref = new ReferenceExpression(exp.getCoreExpressionId());
			exp.setCoreExpression(ref);
		}
		try {
			return ref.evaluate(eContext) != EvaluationResult.FALSE;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void contribute(MTrimBar trimBar) {
		cleanUp(trimBar);
		contribute(trimBar.getElementId(), trimBar);
	}

	private void contribute(String parentId, MTrimBar trimBar) {
		IEclipseContext parentContext = modelService.getContainingContext(trimBar);
		ExpressionContext eContext = new ExpressionContext(parentContext);
		
		List<MTrimContribution> applicableContributions = new ArrayList<MTrimContribution>();
		for (MTrimContribution contribution : application.getTrimContributions()) {
			String targetId = contribution.getParentId();
			if (targetId != null && targetId.equals(parentId) && isVisible(contribution, eContext)) {
				applicableContributions.add(contribution);
			}
		}

		List<MUIElement> contributions = ContributionsAnalyzer.addTrimBarContributions(trimBar,
				applicableContributions);
		trimContributions.put(trimBar, contributions);
	}

	public void cleanUp(MTrimBar trimBar) {
		List<MUIElement> contributions = trimContributions.remove(trimBar);
		if (contributions != null) {
			for (MUIElement contribution : contributions) {
				contribution.setToBeRendered(false);
				if (!trimBar.getChildren().remove(contribution)) {
					for (MTrimElement child : trimBar.getChildren()) {
						if (child instanceof MToolBar) {
							if (((MToolBar) child).getChildren().remove(contribution)) {
								break;
							}
						}
					}
				}
			}
		}
	}
}
