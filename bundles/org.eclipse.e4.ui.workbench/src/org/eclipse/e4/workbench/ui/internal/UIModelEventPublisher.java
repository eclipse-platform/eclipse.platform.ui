/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.internal;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EContentAdapter;

/**
 * Transforms E4 MPart events into 3.x legacy events.
 */
public class UIModelEventPublisher extends EContentAdapter {

	private IEclipseContext context;

	/**
	 * String equivalents to the EMF Notification event type literals
	 */
	private String[] eventTypes = { "Create", //$NON-NLS-1$
			"Set", //$NON-NLS-1$
			"Unset", //$NON-NLS-1$
			"Add", //$NON-NLS-1$
			"Remove", //$NON-NLS-1$
			"AddMany", //$NON-NLS-1$
			"RemoveMany", //$NON-NLS-1$
			"Move", //$NON-NLS-1$
			"RemovingAdapter", //$NON-NLS-1$
			"Resolve", //$NON-NLS-1$
			"EventTypeCount" //$NON-NLS-1$
	};

	// Event 'buckets'
	public static final String UITopicBase = "org/eclipse/e4/ui/model"; //$NON-NLS-1$

	public static final String AppElementTopic = UITopicBase + "/ApplicationElement"; //$NON-NLS-1$
	public static final String CommandTopic = UITopicBase + "/Command"; //$NON-NLS-1$
	public static final String ContextTopic = UITopicBase + "/Context"; //$NON-NLS-1$
	public static final String ContributionTopic = UITopicBase + "/Contribution"; //$NON-NLS-1$
	public static final String InputTopic = UITopicBase + "/Input"; //$NON-NLS-1$
	public static final String ParameterTopic = UITopicBase + "/Parameeter"; //$NON-NLS-1$
	public static final String UIItemTopic = UITopicBase + "/UIItem"; //$NON-NLS-1$
	public static final String UIElementTopic = UITopicBase + "/UIElement"; //$NON-NLS-1$
	public static final String ElementContainerTopic = UITopicBase + "/ElementContainer"; //$NON-NLS-1$
	public static final String WindowTopic = UITopicBase + "/Window"; //$NON-NLS-1$

	private Map<String, String> topicMap = new HashMap<String, String>();

	/**
	 * Constructor.
	 * 
	 * @param e4Context
	 * @param partList
	 */
	public UIModelEventPublisher(IEclipseContext e4Context) {
		this.context = e4Context;
		initializeTopicMap();
	}

	/**
	 * This is a hand-coded scrape of the current UI model. This really should be generated from the
	 * model itself. It's used to break the model's events into different topics based on the
	 * abstract data element being modified. This allows a client to, for example, listen only to
	 * UIItem changes rather than getting spammed with all UI Model events.
	 * 
	 * <b>NOTE:</b> Due to the current implementation we are required to use *unique* names for each
	 * of the data properties. This is to avoid name clashing issues should both data elements be
	 * inherited by some concrete leaf. We should look into how to handle this more elegantly...
	 */
	private void initializeTopicMap() {
		// Application Element
		topicMap.put("id", AppElementTopic); //$NON-NLS-1$

		// Command
		topicMap.put("commandURI", CommandTopic); //$NON-NLS-1$
		topicMap.put("CommandName", CommandTopic); //$NON-NLS-1$
		topicMap.put("impl", CommandTopic); //$NON-NLS-1$
		topicMap.put("args", CommandTopic); //$NON-NLS-1$

		// Context
		topicMap.put("context", ContextTopic); //$NON-NLS-1$
		topicMap.put("variables", ContextTopic); //$NON-NLS-1$

		// Contribution
		topicMap.put("object", ContributionTopic); //$NON-NLS-1$
		topicMap.put("persistedState", ContributionTopic); //$NON-NLS-1$
		topicMap.put("URI", ContributionTopic); //$NON-NLS-1$

		// Input
		topicMap.put("inputURI", InputTopic); //$NON-NLS-1$
		topicMap.put("dirty", InputTopic); //$NON-NLS-1$

		// Parameter
		topicMap.put("tag", ParameterTopic); //$NON-NLS-1$
		topicMap.put("value", ParameterTopic); //$NON-NLS-1$

		// UIItem
		topicMap.put("name", UIItemTopic); //$NON-NLS-1$
		topicMap.put("iconURI", UIItemTopic); //$NON-NLS-1$
		topicMap.put("tooltip", UIItemTopic); //$NON-NLS-1$

		// UIElement
		topicMap.put("widget", UIElementTopic); //$NON-NLS-1$
		topicMap.put("factory", UIElementTopic); //$NON-NLS-1$
		topicMap.put("visible", UIElementTopic); //$NON-NLS-1$
		topicMap.put("parent", UIElementTopic); //$NON-NLS-1$

		// ElementContainer
		topicMap.put("children", ElementContainerTopic); //$NON-NLS-1$
		topicMap.put("activeChild", ElementContainerTopic); //$NON-NLS-1$

		// Window
		topicMap.put("x", WindowTopic); //$NON-NLS-1$
		topicMap.put("y", WindowTopic); //$NON-NLS-1$
		topicMap.put("width", WindowTopic); //$NON-NLS-1$
		topicMap.put("height", WindowTopic); //$NON-NLS-1$
		topicMap.put("mainMenu", WindowTopic); //$NON-NLS-1$
	}

	public void notifyChanged(Notification notification) {
		super.notifyChanged(notification);

		// Format the event
		Map<String, Object> argMap = new HashMap<String, Object>();
		String topic = formatData(notification, argMap);

		if (topic != null) {
			//System.out.println("UI Model Event: " + topic + " args: " + argMap); //$NON-NLS-1$ //$NON-NLS-2$
			IEventBroker eventManager = (IEventBroker) context.get(IEventBroker.class.getName());
			eventManager.send(topic, argMap);
		} else {
			//System.out.println("Event of unknown type received from the model"); //$NON-NLS-1$
		}
	}

	/**
	 * @param notification
	 * @param argMap
	 * @return
	 */
	private String formatData(Notification notification, Map<String, Object> argMap) {
		// The unchecked casts below represent 'asserts'
		MApplicationElement appElement = (MApplicationElement) notification.getNotifier();
		EStructuralFeature feature = (EStructuralFeature) notification.getFeature();

		argMap.put("type", eventTypes[notification.getEventType()]); //$NON-NLS-1$
		argMap.put("changedElement", appElement); //$NON-NLS-1$
		argMap.put("attribute", ((EStructuralFeature) feature).getName()); //$NON-NLS-1$

		if (notification.getEventType() == Notification.SET) {
			argMap.put("newValue", notification.getNewValue()); //$NON-NLS-1$
			argMap.put("oldValue", notification.getOldValue()); //$NON-NLS-1$
		}

		return topicMap.get(feature.getName());
	}
}
