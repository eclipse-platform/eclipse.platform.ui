/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MMenu;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MTestHarness;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.ui.internal.UIEventPublisher;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.AppElement;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.Command;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.Context;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.Contribution;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.ElementContainer;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.EventTags;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.Input;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.Parameter;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.UIElement;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.UIItem;
import org.eclipse.e4.workbench.ui.internal.IUIEvents.Window;
import org.eclipse.emf.common.notify.Notifier;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class UIEventsTest extends HeadlessStartupTest {

	class EventTester {
		String testerName;
		IEventBroker eventBroker;
		String topic;
		String[] attIds;
		boolean[] hasFired;

		EventHandler attListener = new EventHandler() {
			public void handleEvent(Event event) {
				assertTrue(event.getTopic().equals(topic),
						"Incorrect Topic: " + event.getTopic()); //$NON-NLS-1$

				String attId = (String) event.getProperty(EventTags.AttName);
				int attIndex = getAttIndex(attId);
				assertTrue(attIndex >= 0, "Unknown Attribite: " + attId); //$NON-NLS-1$
				hasFired[attIndex] = true;
			}
		};

		public EventTester(String name, String topic, String[] attIds,
				IEventBroker eventBroker) {
			this.testerName = name;
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

		public String[] getAttIds(boolean fired) {
			List<String> atts = new ArrayList<String>();
			for (int i = 0; i < hasFired.length; i++) {
				if (hasFired[i] == fired)
					atts.add(attIds[i]);
			}

			return (String[]) atts.toArray(new String[atts.size()]);
		}
	}

	public class AppElementTester extends EventTester {
		AppElementTester(IEventBroker eventBroker) {
			super("AppElement", AppElement.Topic,
					new String[] { AppElement.Id }, eventBroker);
		}
	}

	public class CommandTester extends EventTester {
		CommandTester(IEventBroker eventBroker) {
			super("Command", Command.Topic, new String[] { Command.Name,
					Command.URI, Command.Args, Command.Impl }, eventBroker);
		}
	}

	public class ContextTester extends EventTester {
		ContextTester(IEventBroker eventBroker) {
			super("Context", Context.Topic, new String[] { Context.Context,
					Context.Variables }, eventBroker);
		}
	}

	public class ContributionTester extends EventTester {
		ContributionTester(IEventBroker eventBroker) {
			super("Contribution", Contribution.Topic,
					new String[] { Contribution.URI, Contribution.State,
							Contribution.Object }, eventBroker);
		}
	}

	public class ElementContainerTester extends EventTester {
		ElementContainerTester(IEventBroker eventBroker) {
			super("ElementContainer", ElementContainer.Topic, new String[] {
					ElementContainer.Children, ElementContainer.ActiveChild },
					eventBroker);
		}
	}

	public class InputTester extends EventTester {
		InputTester(IEventBroker eventBroker) {
			super("Input", Input.Topic,
					new String[] { Input.URI, Input.Dirty }, eventBroker);
		}
	}

	public class ParameterTester extends EventTester {
		ParameterTester(IEventBroker eventBroker) {
			super("Parameter", Parameter.Topic, new String[] { Parameter.Tag,
					Parameter.Value }, eventBroker);
		}
	}

	public class UIElementTester extends EventTester {
		UIElementTester(IEventBroker eventBroker) {
			super("UIElement", UIElement.Topic, new String[] {
					UIElement.Factory, UIElement.Parent, UIElement.Visible,
					UIElement.Widget }, eventBroker);
		}
	}

	public class UIItemTester extends EventTester {
		UIItemTester(IEventBroker eventBroker) {
			super("UIItem", UIItem.Topic, new String[] { UIItem.Name,
					UIItem.IconURI, UIItem.Tooltip }, eventBroker);
		}
	}

	public class WindowTester extends EventTester {
		WindowTester(IEventBroker eventBroker) {
			super("Window", Window.Topic, new String[] { Window.MainMenu,
					Window.X, Window.Y, Window.Width, Window.Height },
					eventBroker);
		}
	}

	@Override
	protected String getAppURI() {
		return "org.eclipse.e4.ui.tests/xmi/contacts.xmi";
	}

	@Override
	protected MPart getFirstPart() {
		return (MPart) findElement("DetailsView");
	}

	@Override
	protected MPart getSecondPart() {
		return (MPart) findElement("ContactsView");
	}

	public void testAllTopics() {
		IEclipseContext appContext = application.getContext();
		IEventBroker eventBroker = (IEventBroker) appContext
				.get(IEventBroker.class.getName());

		// Create a tester for each topic
		AppElementTester appTester = new AppElementTester(eventBroker);
		CommandTester commandTester = new CommandTester(eventBroker);
		ContextTester contextTester = new ContextTester(eventBroker);
		ContributionTester contributionTester = new ContributionTester(
				eventBroker);
		ElementContainerTester elementContainerTester = new ElementContainerTester(
				eventBroker);
		InputTester inputTester = new InputTester(eventBroker);
		ParameterTester parameterTester = new ParameterTester(eventBroker);
		UIElementTester uiElementTester = new UIElementTester(eventBroker);
		UIItemTester uiItemTester = new UIItemTester(eventBroker);
		WindowTester windowTester = new WindowTester(eventBroker);

		// Create an array to check for 'cross talk' (i.e. events being fired
		// on incorrect topics
		EventTester[] allTesters = { appTester, commandTester, contextTester,
				contributionTester, elementContainerTester, inputTester,
				parameterTester, uiElementTester, uiItemTester, windowTester };

		// Create the test harness and hook up the event publisher
		MTestHarness allData = MApplicationFactory.eINSTANCE
				.createTestHarness();
		((Notifier) allData).eAdapters().add(new UIEventPublisher(appContext));

		// AppElement
		reset(allTesters);
		String newId = "Some New Id";
		allData.setId(newId);
		checkForFailures(allTesters, appTester);

		// Test that no-ops don't throw events
		appTester.reset();
		allData.setId(newId);
		assertTrue("event thrown on No-Op",
				appTester.getAttIds(true).length == 0);

		// Command
		reset(allTesters);
		IEclipseContext newContext = EclipseContextFactory.create();
		allData.setContext(newContext);
		allData.getVariables().add("foo");
		checkForFailures(allTesters, contextTester);

		// Context
		reset(allTesters);
		allData.setContext(EclipseContextFactory.create());
		allData.getVariables().add("A var");
		checkForFailures(allTesters, contextTester);

		// Contribution
		reset(allTesters);
		allData.setURI("Some URI");
		allData.setObject("Some onbject");
		allData.setPersistedState("Some state");
		checkForFailures(allTesters, contributionTester);

		// ElementContainer
		reset(allTesters);
		MMenu menu = MApplicationFactory.eINSTANCE.createMenu();
		allData.getChildren().add(menu);
		allData.setActiveChild(menu);
		checkForFailures(allTesters, elementContainerTester);

		// Input
		reset(allTesters);
		allData.setInputURI("New Input Uri");
		allData.setDirty(!allData.isDirty());
		checkForFailures(allTesters, inputTester);

		// Parameter
		reset(allTesters);
		allData.setTag("New Tag");
		allData.setValue("New Value");
		checkForFailures(allTesters, parameterTester);

		// UIElement
		reset(allTesters);
		MTestHarness newParent = MApplicationFactory.eINSTANCE
				.createTestHarness();
		allData.setFactory("New Factory");
		allData.setParent(newParent);
		allData.setVisible(!allData.isVisible());
		allData.setWidget("New Widget");
		checkForFailures(allTesters, uiElementTester);

		// UIItem
		reset(allTesters);
		allData.setName("New Name");
		allData.setIconURI("New Icon URI");
		allData.setTooltip("New Tooltip");
		checkForFailures(allTesters, uiItemTester);

		// Window tests
		reset(allTesters);
		MWindow window = application.getChildren().get(0);
		window.setX(1234);
		window.setY(1234);
		window.setWidth(1234);
		window.setHeight(1234);

		MMenu newMainMenu = MApplicationFactory.eINSTANCE.createMenu();
		window.setMainMenu(newMainMenu);
		checkForFailures(allTesters, windowTester);
	}

	/**
	 * @param allTesters
	 * @param tester
	 */
	private void checkForFailures(EventTester[] allTesters, EventTester tester) {
		ensureAllSet(tester);
		ensureNoCrossTalk(allTesters, tester);
	}

	/**
	 * Ensures that no events were picked up from topics other than the one we
	 * expect to see changes in.
	 * 
	 * @param tester
	 */
	private void ensureNoCrossTalk(EventTester[] allTesters, EventTester skipMe) {
		List<EventTester> badTesters = new ArrayList<EventTester>();
		for (EventTester t : allTesters) {
			if (t.equals(skipMe))
				continue;

			if (t.getAttIds(true).length > 0)
				badTesters.add(t);
		}

		if (badTesters.size() > 0) {
			String msg = "Events were fired in the wrong topic(s): "
					+ badTesters;
			fail(msg);
		}
	}

	/**
	 * @param tester
	 */
	private void ensureAllSet(EventTester tester) {
		String[] unfiredIds = tester.getAttIds(false);
		if (unfiredIds.length > 0) {
			String msg = "No event fired: " + unfiredIds;
			fail(msg);
		}
	}

	/**
	 * @param allTesters
	 */
	private void reset(EventTester[] allTesters) {
		for (EventTester t : allTesters) {
			t.reset();
		}
	}
}
