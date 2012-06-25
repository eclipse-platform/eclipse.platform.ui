/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.impl.StringToObjectMapImpl;
import org.eclipse.e4.ui.model.application.impl.StringToStringMapImpl;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.UIEvents.EventTypes;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EContentAdapter;

/**
 * Transforms E4 MPart events into 3.x legacy events.
 */
public class UIEventPublisher extends EContentAdapter {

	private IEclipseContext context;

	/**
	 * @param e4Context
	 */
	public UIEventPublisher(IEclipseContext e4Context) {
		this.context = e4Context;
	}

	public void notifyChanged(Notification notification) {
		super.notifyChanged(notification);

		// Ignore events that did not change the model value
		if (notification.isTouch())
			return;

		// Format the EMF event as an E4 UIEvent
		Map<String, Object> argMap = new HashMap<String, Object>();

		String topic = formatData(notification, argMap);

		if (topic != null) {
			IEventBroker eventManager = context.get(IEventBroker.class);
			eventManager.send(topic, argMap);
		}
	}

	/**
	 * Large hack here. Open to better suggestions
	 * 
	 * When the model SET changes occur to the values of maps, the NOTIFIER is the map and not the
	 * element containing the map. (Note that map addition and removal events DO come in with the
	 * NOTIFIER set to the element containing the map)
	 * 
	 * In the problematic case, the containing model element can be found in the NOTIFIER's
	 * eContainer. However, the FEATURE (field) of the eContainer containing the map is not readily
	 * available.
	 * 
	 * We had considered a strategy where you recursively check all fields of the eContainer for an
	 * object that IS the NOTIFIER, however these events happen a lot and the resulting code would
	 * have very poor performance. Additionally, all I could get from the eContainer was an EMF
	 * DelegatingMap, while the NOTIFIER is an instance of the contained delegated map
	 * (StringToObjectImpl for example). It would appear some reflection tricks to read
	 * protected/private methods would be required to get the correct objects to compare.
	 * 
	 * At this time there are only two maps used in our model. A StringToObject map used in
	 * MApplicationElement.transientData a StringToString map used in
	 * MApplicationElement.persistentState.
	 * 
	 * The large hack employed is to take this knowledge and hard hard code the topic based on the
	 * type of the NOTIFIER.
	 * 
	 * One problem with this approach is if a developer modifies the model to contain another field
	 * using a map events would be generated with the wrong topic information. This will lead to
	 * hard to identify bugs.
	 * 
	 * To combat this I have added guard code that explicitly confirms that a StringToObject change
	 * is being originated by transientData and a StringToString change is being originated by
	 * persistentState. If the guard check fails an IllegalArgumentException is thrown with
	 * instructions to modify UIEventPublisher.
	 * 
	 * Clearly this is a sub-optimal solution and would like to hear suggestions for improvement.
	 * Likely there is some "simple" EMF wisdom we are missing to make this work with a couple of
	 * annotations, a white swan and a full moon.
	 */
	private String formatData(final Notification notification, Map<String, Object> argMap) {
		MApplicationElement appElement = null;
		EStructuralFeature feature = null;
		String attributeName = null;
		String topic = null;

		Object notifier = notification.getNotifier();
		Object oldValue = null;
		Object newValue = null;
		Object position = null;

		if (notifier instanceof MApplicationElement) {
			// Most EMF events will be these. Even map add and remove events
			appElement = (MApplicationElement) notifier;
			feature = (EStructuralFeature) notification.getFeature();
			attributeName = feature.getName();
			topic = getTopic(feature, getEventType(notification));
			switch (notification.getEventType()) {
			case Notification.MOVE:
				// for MOVE, oldValue is actually the source position
				oldValue = notification.getOldValue();
				newValue = notification.getNewValue();
				position = notification.getPosition();
				break;
			case Notification.ADD_MANY:
				newValue = notification.getNewValue();
				position = notification.getPosition();
				break;
			case Notification.REMOVE_MANY:
				oldValue = notification.getOldValue();
				position = notification.getNewValue();
				break;
			case Notification.ADD:
			case Notification.REMOVE:
				oldValue = notification.getOldValue();
				newValue = notification.getNewValue();
				position = notification.getPosition();
				break;
			case Notification.SET:
			case Notification.UNSET:
				oldValue = notification.getOldValue();
				newValue = notification.getNewValue();
				break;
			default:
				Activator.log(IStatus.ERROR, getClass().getName()
						+ ": unhandled EMF Notification code: " //$NON-NLS-1$
						+ notification.getEventType());
			}
		} else if (notifier instanceof StringToObjectMapImpl) {
			// These are SET events on StringToObjectMap only
			// StringToObjectMap is ONLY used by MApplicationData.transientData
			appElement = (MApplicationElement) ((StringToObjectMapImpl) notifier).eContainer();

			// Guard code to detect if some other model field is using a StringToObjectMap
			final String key = ((StringToObjectMapImpl) notifier).getKey();
			Object storedNewValue = appElement.getTransientData().get(key);
			Object notificationNewValue = notification.getNewValue();

			// Identity check by design. If these are not the same object then the event came from
			// a different model object than we expected. Warn the developer
			if (notificationNewValue != storedNewValue) {
				throw new IllegalArgumentException(
						"A StringToObjectMap that was NOT MApplicationElement.transientData changed.  You must modify UIEventPublisher appropriately"); //$NON-NLS-1$
			}

			attributeName = UIEvents.ApplicationElement.TRANSIENTDATA;
			topic = getTopic(attributeName, getEventType(notification));

			// We need to send MapEntries for the old and new values.
			oldValue = createMapEntry(key, notification.getOldValue());
			newValue = createMapEntry(key, notification.getNewValue());
		} else if (notifier instanceof StringToStringMapImpl) {
			// These are SET events on StringToStringMap only
			// StringToStringMap is ONLY used by MApplicationData.persistedState
			appElement = (MApplicationElement) ((StringToStringMapImpl) notifier).eContainer();

			// Guard code to detect if some other model field is using a StringToStringMap
			final String key = ((StringToStringMapImpl) notifier).getKey();
			Object storedNewValue = appElement.getPersistedState().get(key);
			Object notificationNewValue = notification.getNewValue();

			// Identity check by design. If these are not the same object then the event came from
			// a different model object than we expected. Warn the developer
			if (notificationNewValue != storedNewValue) {
				throw new IllegalArgumentException(
						"A StringToStringMap that was NOT MApplicationElement.persistedState changed.  You must modify UIEventPublisher appropriately"); //$NON-NLS-1$
			}

			attributeName = UIEvents.ApplicationElement.PERSISTEDSTATE;
			topic = getTopic(attributeName, getEventType(notification));
			oldValue = createMapEntry(key, notification.getOldValue());
			newValue = createMapEntry(key, notification.getNewValue());
		} else {
			// Unhandled notification type. Ignore event
			return null;
		}

		argMap.put(EventTags.TYPE, getEventType(notification));
		argMap.put(EventTags.ELEMENT, appElement);
		argMap.put(EventTags.ATTNAME, attributeName);

		// no need to include UNSET
		if (notification.getEventType() == Notification.SET
				|| notification.getEventType() == Notification.MOVE
				|| notification.getEventType() == Notification.ADD
				|| notification.getEventType() == Notification.ADD_MANY
				|| notification.getEventType() == Notification.REMOVE
				|| notification.getEventType() == Notification.REMOVE_MANY) {
			if (newValue != null) {
				argMap.put(EventTags.NEW_VALUE, newValue);
			}
			if (oldValue != null) {
				argMap.put(EventTags.OLD_VALUE, oldValue);
			}
			if (position != null) {
				argMap.put(EventTags.POSITION, position);
			}
		}

		if (appElement instanceof MUIElement) {
			argMap.put(EventTags.WIDGET, ((MUIElement) appElement).getWidget());
		}

		return topic;
	}

	private String getEventType(Notification notification) {
		switch (notification.getEventType()) {
		case Notification.ADD:
			return EventTypes.ADD;

		case Notification.ADD_MANY:
			return EventTypes.ADD_MANY;

		case Notification.REMOVE:
			return EventTypes.REMOVE;

		case Notification.REMOVE_MANY:
			return EventTypes.REMOVE_MANY;

		case Notification.MOVE:
			return EventTypes.MOVE;

			// case Notification.UNSET: doesn't appear to be generated
		case Notification.SET:
			return EventTypes.SET;
		}

		return "UNKNOWN"; //$NON-NLS-1$
	}

	private Map.Entry<String, Object> createMapEntry(final String key, final Object value) {
		return new Map.Entry<String, Object>() {
			public String getKey() {
				return key;
			}

			public Object getValue() {
				return value;
			}

			public Object setValue(Object value) {
				throw new UnsupportedOperationException();
			}
		};
	}

	private String getTopic(EStructuralFeature eFeature, String type) {
		EClass eContainingClass = eFeature.getEContainingClass();
		return UIEvents.UIModelTopicBase + UIEvents.TOPIC_SEP
				+ eContainingClass.getEPackage().getName() + UIEvents.TOPIC_SEP
				+ eContainingClass.getName() + UIEvents.TOPIC_SEP + eFeature.getName()
				+ UIEvents.TOPIC_SEP + type;
	}

	private String getTopic(String attributeName, String type) {
		String topicBase = "org/eclipse/e4/ui/model/application/ApplicationElement/"; //$NON-NLS-1$
		return topicBase + attributeName + UIEvents.TOPIC_SEP + type;
	}
}
