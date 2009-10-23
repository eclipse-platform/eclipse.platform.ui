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
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.AppElement;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.Command;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.Context;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.Contribution;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.ElementContainer;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.EventTags;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.EventTypes;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.Input;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.Parameter;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.UIElement;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.UIItem;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.Window;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EContentAdapter;

/**
 * Transforms E4 MPart events into 3.x legacy events.
 */
public class UIEventPublisher extends EContentAdapter {

	private IEclipseContext context;

	/**
	 * Constructor.
	 * 
	 * @param e4Context
	 * @param partList
	 */
	public UIEventPublisher(IEclipseContext e4Context) {
		this.context = e4Context;
	}

	public void notifyChanged(Notification notification) {
		super.notifyChanged(notification);

		// Inhibit No-Ops
		if (notification.isTouch())
			return;

		// Format the event
		Map<String, Object> argMap = new HashMap<String, Object>();
		String topic = formatData(notification, argMap);

		if (topic != null) {
			//System.out.println("UI Model Event: " + topic + " args: " + argMap); //$NON-NLS-1$ //$NON-NLS-2$
			IEventBroker eventManager = (IEventBroker) context.get(IEventBroker.class.getName());
			eventManager.send(topic, argMap);
		} else {
			System.out.println("Event of unknown type received from the model"); //$NON-NLS-1$
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

		argMap.put(EventTags.Type, getEventType(notification));
		argMap.put(EventTags.Element, appElement);
		argMap.put(EventTags.AttName, ((EStructuralFeature) feature).getName());

		if (notification.getEventType() == Notification.SET
				|| notification.getEventType() == Notification.ADD
				|| notification.getEventType() == Notification.REMOVE) {
			argMap.put(EventTags.NewValue, notification.getNewValue());
			argMap.put(EventTags.OldValue, notification.getOldValue());
		}

		if (appElement instanceof MUIElement) {
			argMap.put(EventTags.Widget, ((MUIElement) appElement).getWidget());
		}
		String topic = getTopic((String) argMap.get(EventTags.AttName));
		if (topic == null) {
			EObjectImpl eoi = (EObjectImpl) appElement;
			String notifierClass = eoi.eClass().getName();
			topic = IUIEvents.UITopicBase + '/' + notifierClass;
		}
		return topic;
	}

	private String getEventType(Notification notification) {
		switch (notification.getEventType()) {
		case Notification.ADD:
			return EventTypes.Add;
		case Notification.REMOVE:
			return EventTypes.Remove;
		case Notification.SET:
			return EventTypes.Set;
		}

		return "<unhandled event type"; //$NON-NLS-1$
	}

	/**
	 * Map the attribute back to the correct topic.
	 * 
	 * @param attId
	 * @return
	 */
	private String getTopic(String attName) {
		if (AppElement.Id.equals(attName))
			return AppElement.Topic;
		else if (Command.Args.equals(attName))
			return Command.Topic;
		else if (Command.Impl.equals(attName))
			return Command.Topic;
		else if (Command.Name.equals(attName))
			return Command.Topic;
		else if (Command.URI.equals(attName))
			return Command.Topic;
		else if (Context.Context.equals(attName))
			return Context.Topic;
		else if (Context.Variables.equals(attName))
			return Context.Topic;
		else if (Contribution.Object.equals(attName))
			return Contribution.Topic;
		else if (Contribution.State.equals(attName))
			return Contribution.Topic;
		else if (Contribution.URI.equals(attName))
			return Contribution.Topic;
		else if (ElementContainer.Children.equals(attName))
			return ElementContainer.Topic;
		else if (ElementContainer.ActiveChild.equals(attName))
			return ElementContainer.Topic;
		else if (Input.Dirty.equals(attName))
			return Input.Topic;
		else if (Input.URI.equals(attName))
			return Input.Topic;
		else if (Parameter.Tag.equals(attName))
			return Parameter.Topic;
		else if (Parameter.Value.equals(attName))
			return Parameter.Topic;
		else if (UIElement.Factory.equals(attName))
			return UIElement.Topic;
		else if (UIElement.Parent.equals(attName))
			return UIElement.Topic;
		else if (UIElement.Visible.equals(attName))
			return UIElement.Topic;
		else if (UIElement.Widget.equals(attName))
			return UIElement.Topic;
		else if (UIItem.IconURI.equals(attName))
			return UIItem.Topic;
		else if (UIItem.Name.equals(attName))
			return UIItem.Topic;
		else if (UIItem.Tooltip.equals(attName))
			return UIItem.Topic;
		else if (Window.MainMenu.equals(attName))
			return Window.Topic;
		else if (Window.X.equals(attName))
			return Window.Topic;
		else if (Window.Y.equals(attName))
			return Window.Topic;
		else if (Window.Width.equals(attName))
			return Window.Topic;
		else if (Window.Height.equals(attName))
			return Window.Topic;

		return null;
	}
}
