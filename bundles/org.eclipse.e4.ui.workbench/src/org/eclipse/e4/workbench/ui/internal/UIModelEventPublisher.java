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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Transforms E4 MPart events into 3.x legacy events.
 */
public class UIModelEventPublisher extends EContentAdapter {

	private IEclipseContext context;

	/**
	 * String equivalents to the EMF Notification event type literals
	 */
	String f = MApplicationPackage.Literals.APPLICATION_ELEMENT.getName();

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

	// Event data Tags
	public static class EventTags {
		public static final String Element = "Changed Element"; //$NON-NLS-1$
		public static final String Type = "Event Type"; //$NON-NLS-1$
		public static final String AttName = "Att Name"; //$NON-NLS-1$
		public static final String OldValue = "Old Value"; //$NON-NLS-1$
		public static final String NewValue = "New Value"; //$NON-NLS-1$
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
	// Event 'buckets' are based on the EMF class name of the data classes
	public static final String UITopicBase = "org/eclipse/e4/ui/model"; //$NON-NLS-1$

	public static final class AppElement {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.APPLICATION_ELEMENT.getName();
		public static final String Id = MApplicationPackage.Literals.APPLICATION_ELEMENT__ID
				.getName();
	}

	public static final class Command {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.COMMAND.getName();

		public static final String URI = MApplicationPackage.Literals.COMMAND__COMMAND_URI
				.getName();
		public static final String Name = MApplicationPackage.Literals.COMMAND__COMMAND_NAME
				.getName();
		public static final String Impl = MApplicationPackage.Literals.COMMAND__IMPL.getName();
		public static final String Args = MApplicationPackage.Literals.COMMAND__ARGS.getName();
	}

	public static final class Context {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.CONTEXT.getName();
		public static final String Context = MApplicationPackage.Literals.CONTEXT__CONTEXT
				.getName();
		public static final String Variables = MApplicationPackage.Literals.CONTEXT__VARIABLES
				.getName();
	}

	public static final class Contribution {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.CONTRIBUTION.getName();

		public static final String URI = MApplicationPackage.Literals.CONTRIBUTION__URI.getName();
		public static final String State = MApplicationPackage.Literals.CONTRIBUTION__PERSISTED_STATE
				.getName();
		public static final String Object = MApplicationPackage.Literals.CONTRIBUTION__OBJECT
				.getName();
	}

	public static final class Input {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.INPUT.getName();

		public static final String Dirty = MApplicationPackage.Literals.INPUT__DIRTY.getName();
		public static final String URI = MApplicationPackage.Literals.INPUT__INPUT_URI.getName();
	}

	public static final class Parameter {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.PARAMETER.getName();

		public static final String Tag = MApplicationPackage.Literals.PARAMETER__TAG.getName();
		public static final String Value = MApplicationPackage.Literals.PARAMETER__VALUE.getName();
	}

	public static final class UIItem {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.UI_ITEM.getName();

		public static final String Name = MApplicationPackage.Literals.UI_ITEM__NAME.getName();
		public static final String IconURI = MApplicationPackage.Literals.UI_ITEM__ICON_URI
				.getName();
		public static final String Tooltip = MApplicationPackage.Literals.UI_ITEM__TOOLTIP
				.getName();
	}

	public static final class UIElement {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.UI_ELEMENT.getName();

		public static final String Factory = MApplicationPackage.Literals.UI_ELEMENT__FACTORY
				.getName();
		public static final String Parent = MApplicationPackage.Literals.UI_ELEMENT__PARENT
				.getName();
		public static final String Visible = MApplicationPackage.Literals.UI_ELEMENT__VISIBLE
				.getName();
		public static final String Widget = MApplicationPackage.Literals.UI_ELEMENT__WIDGET
				.getName();
	}

	public static final class ElementContainer {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.ELEMENT_CONTAINER.getName();

		public static final String Children = MApplicationPackage.Literals.ELEMENT_CONTAINER__CHILDREN
				.getName();
		public static final String ActiveChild = MApplicationPackage.Literals.ELEMENT_CONTAINER__ACTIVE_CHILD
				.getName();
	}

	public static final class Window {
		public static final String Topic = UITopicBase + '/'
				+ MApplicationPackage.Literals.WINDOW.getName();

		public static final String X = MApplicationPackage.Literals.WINDOW__X.getName();
		public static final String Y = MApplicationPackage.Literals.WINDOW__Y.getName();
		public static final String Width = MApplicationPackage.Literals.WINDOW__WIDTH.getName();
		public static final String Height = MApplicationPackage.Literals.WINDOW__HEIGHT.getName();
		public static final String MainMenu = MApplicationPackage.Literals.WINDOW__MAIN_MENU
				.getName();
	}

	/**
	 * Constructor.
	 * 
	 * @param e4Context
	 * @param partList
	 */
	public UIModelEventPublisher(IEclipseContext e4Context) {
		this.context = e4Context;
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

		argMap.put(EventTags.Type, eventTypes[notification.getEventType()]);
		argMap.put(EventTags.Element, appElement);
		argMap.put(EventTags.AttName, ((EStructuralFeature) feature).getName());

		if (notification.getEventType() == Notification.SET) {
			argMap.put(EventTags.NewValue, notification.getNewValue());
			argMap.put(EventTags.OldValue, notification.getOldValue());
		}

		String topic = getTopic((String) argMap.get(EventTags.AttName));
		if (topic == null) {
			EObjectImpl eoi = (EObjectImpl) appElement;
			String notifierClass = eoi.eClass().getName();
			topic = UITopicBase + '/' + notifierClass;
		}
		return topic;
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

	class EventTester {
		IEventBroker eventBroker;
		String topic;
		String[] attIds;
		boolean[] hasFired;

		EventHandler attListener = new EventHandler() {
			public void handleEvent(Event event) {
				assertTrue(event.getTopic().equals(topic), "Incorrect Topic: " + event.getTopic()); //$NON-NLS-1$

				String attId = (String) event.getProperty(EventTags.AttName);
				int attIndex = getAttIndex(attId);
				assertTrue(attIndex >= 0, "Unknown Attribite: " + attId); //$NON-NLS-1$
				hasFired[attIndex] = true;
			}
		};

		public EventTester(String topic, String[] attIds, IEventBroker eventBroker) {
			this.topic = topic;
			this.attIds = attIds;
			this.eventBroker = eventBroker;

			hasFired = new boolean[attIds.length];
			reset();

			eventBroker.subscribe(topic, attListener);
		}

		/**
		 * @param b
		 * @param string
		 */
		protected void assertTrue(boolean b, String string) {
		}

		/**
		 * @param attId
		 * @return
		 */
		protected int getAttIndex(String attId) {
			for (int i = 0; i < attIds.length; i++) {
				if (attIds[i].equals(attId))
					return i;
			}
			return -1;
		}

		public void dispose() {
			eventBroker.unsubscribe(attListener);
		}

		public void reset() {
			for (int i = 0; i < hasFired.length; i++)
				hasFired[i] = false;
		}

		public String[] firedAttIds() {
			List<String> atts = new ArrayList<String>();
			for (int i = 0; i < hasFired.length; i++) {
				if (hasFired[i])
					atts.add(attIds[i]);
			}

			return (String[]) atts.toArray();
		}

	}

	public class AppElementTester extends EventTester {
		AppElementTester(IEventBroker eventBroker) {
			super(AppElement.Topic, new String[] { AppElement.Id }, eventBroker);
		}
	}

	public class CommandTester extends EventTester {
		CommandTester(IEventBroker eventBroker) {
			super(Command.Topic, new String[] { Command.Name, Command.URI, Command.Args,
					Command.Impl }, eventBroker);
		}
	}

	public class ContextTester extends EventTester {
		ContextTester(IEventBroker eventBroker) {
			super(Context.Topic, new String[] { Context.Context, Context.Variables }, eventBroker);
		}
	}

	public class ContributionTester extends EventTester {
		ContributionTester(IEventBroker eventBroker) {
			super(Contribution.Topic, new String[] { Contribution.URI, Contribution.State,
					Contribution.Object }, eventBroker);
		}
	}

	public class ElementContainerTester extends EventTester {
		ElementContainerTester(IEventBroker eventBroker) {
			super(ElementContainer.Topic, new String[] { ElementContainer.Children,
					ElementContainer.ActiveChild }, eventBroker);
		}
	}

	public class InputTester extends EventTester {
		InputTester(IEventBroker eventBroker) {
			super(Input.Topic, new String[] { Input.URI, Input.Dirty }, eventBroker);
		}
	}

	public class ParameterTester extends EventTester {
		ParameterTester(IEventBroker eventBroker) {
			super(Parameter.Topic, new String[] { Parameter.Tag, Parameter.Value }, eventBroker);
		}
	}

	public class UIElementTester extends EventTester {
		UIElementTester(IEventBroker eventBroker) {
			super(UIElement.Topic, new String[] { UIElement.Factory, UIElement.Parent,
					UIElement.Visible, UIElement.Widget }, eventBroker);
		}
	}

	public class UIItemTester extends EventTester {
		UIItemTester(IEventBroker eventBroker) {
			super(UIItem.Topic, new String[] { UIItem.Name, UIItem.IconURI, UIItem.Tooltip },
					eventBroker);
		}
	}

	public class WindowTester extends EventTester {
		WindowTester(IEventBroker eventBroker) {
			super(Window.Topic, new String[] { Window.MainMenu, Window.X, Window.Y, Window.Width,
					Window.Height }, eventBroker);
		}
	}
}
