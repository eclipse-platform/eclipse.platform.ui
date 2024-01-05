/*******************************************************************************
 * Copyright (c) 2009, 2024 IBM Corporation and others.
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

package org.eclipse.e4.ui.tests.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.UIEventPublisher;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.tests.model.test.MTestFactory;
import org.eclipse.e4.ui.tests.model.test.MTestHarness;
import org.eclipse.e4.ui.workbench.UIEvents.ApplicationElement;
import org.eclipse.e4.ui.workbench.UIEvents.Command;
import org.eclipse.e4.ui.workbench.UIEvents.Context;
import org.eclipse.e4.ui.workbench.UIEvents.Contribution;
import org.eclipse.e4.ui.workbench.UIEvents.Dirtyable;
import org.eclipse.e4.ui.workbench.UIEvents.ElementContainer;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.UIEvents.Parameter;
import org.eclipse.e4.ui.workbench.UIEvents.UIElement;
import org.eclipse.e4.ui.workbench.UIEvents.UILabel;
import org.eclipse.e4.ui.workbench.UIEvents.Window;
import org.eclipse.emf.common.notify.Notifier;
import org.junit.Test;
import org.osgi.service.event.EventHandler;

public class UIEventsTest extends HeadlessApplicationElementTest {

	static class EventTester {
		String testerName;
		IEventBroker eventBroker;
		String topic;
		String[] attIds;
		boolean[] hasFired;

		EventHandler attListener = event -> {
			// In case of * topic check that that event topic starts with the same prefix
			if (topic.endsWith("*")) {
				assertTrue("Incorrect Topic.", event.getTopic().startsWith(topic.substring(0, topic.length() - 2)));
			} else {
				assertEquals("Incorrect Topic.", topic, event.getTopic());
			}

			String attId = (String) event.getProperty(EventTags.ATTNAME);
			int attIndex = getAttIndex(attId);
			assertTrue("Unknown Attribite: " + attId, attIndex >= 0); //$NON-NLS-1$
			hasFired[attIndex] = true;
		};

		public EventTester(String name, String topic, String[] attIds,
				IEventBroker eventBroker) {
			this.testerName = name;
			this.topic = topic;
			this.attIds = attIds;
			this.eventBroker = eventBroker;

			hasFired = new boolean[attIds.length];
			reset();

			eventBroker.subscribe(this.topic, attListener);
		}

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
			List<String> atts = new ArrayList<>();
			for (int i = 0; i < hasFired.length; i++) {
				if (hasFired[i] == fired)
					atts.add(attIds[i]);
			}

			return atts.toArray(new String[atts.size()]);
		}
	}

	public static class AppElementTester extends EventTester {
		AppElementTester(IEventBroker eventBroker) {
			super("AppElement", ApplicationElement.TOPIC_ALL, new String[] {
					ApplicationElement.ELEMENTID, ApplicationElement.TAGS,
					ApplicationElement.PERSISTEDSTATE }, eventBroker);
		}
	}

	public static class CommandTester extends EventTester {
		CommandTester(IEventBroker eventBroker) {
			super("Command", Command.TOPIC_ALL,
					new String[] { Command.COMMANDNAME }, eventBroker);
		}
	}

	public static class ContextTester extends EventTester {
		ContextTester(IEventBroker eventBroker) {
			super("Context", Context.TOPIC_ALL, new String[] { Context.CONTEXT,
					Context.VARIABLES }, eventBroker);
		}
	}

	public static class ContributionTester extends EventTester {
		ContributionTester(IEventBroker eventBroker) {
			super("Contribution", Contribution.TOPIC_ALL, new String[] {
					Contribution.CONTRIBUTIONURI, Contribution.OBJECT },
					eventBroker);
		}
	}

	public static class ElementContainerTester extends EventTester {
		ElementContainerTester(IEventBroker eventBroker) {
			super("ElementContainer", ElementContainer.TOPIC_ALL,
					new String[] { ElementContainer.CHILDREN,
							ElementContainer.SELECTEDELEMENT }, eventBroker);
		}
	}

	public static class DirtyableTester extends EventTester {
		DirtyableTester(IEventBroker eventBroker) {
			super("Dirtyable", Dirtyable.TOPIC_ALL,
					new String[] { Dirtyable.DIRTY }, eventBroker);
		}
	}

	public static class ParameterTester extends EventTester {
		ParameterTester(IEventBroker eventBroker) {
			super("Parameter", Parameter.TOPIC_ALL, new String[] {
					Parameter.NAME, Parameter.VALUE }, eventBroker);
		}
	}

	public static class UIElementTester extends EventTester {
		UIElementTester(IEventBroker eventBroker) {
			super("UIElement", UIElement.TOPIC_ALL, new String[] {
					UIElement.RENDERER, UIElement.TOBERENDERED,
					UIElement.PARENT, UIElement.ONTOP, UIElement.VISIBLE,
					UIElement.CONTAINERDATA, UIElement.WIDGET }, eventBroker);
		}
	}

	public static class UIItemTester extends EventTester {
		UIItemTester(IEventBroker eventBroker) {
			super("UIItem", UILabel.TOPIC_ALL, new String[] { UILabel.LABEL,
					UILabel.ICONURI, UILabel.TOOLTIP }, eventBroker);
		}
	}

	public static class WindowTester extends EventTester {
		WindowTester(IEventBroker eventBroker) {
			super("Window", Window.TOPIC_ALL, new String[] { Window.MAINMENU,
					Window.X, Window.Y, Window.WIDTH, Window.HEIGHT },
					eventBroker);
		}
	}

	@Override
	protected MApplicationElement createApplicationElement(
			IEclipseContext appContext) throws Exception {
		MApplication application = MApplicationFactory.INSTANCE
				.createApplication();
		application.getChildren().add(MBasicFactory.INSTANCE.createWindow());
		return application;
	}

	@Test
	public void testAllTopics() {
		IEventBroker eventBroker = rule.getApplicationContext().get(IEventBroker.class);

		// Create a tester for each topic
		AppElementTester appTester = new AppElementTester(eventBroker);
		CommandTester commandTester = new CommandTester(eventBroker);
		ContextTester contextTester = new ContextTester(eventBroker);
		ContributionTester contributionTester = new ContributionTester(
				eventBroker);
		ElementContainerTester elementContainerTester = new ElementContainerTester(
				eventBroker);
		DirtyableTester dirtyableTester = new DirtyableTester(eventBroker);
		ParameterTester parameterTester = new ParameterTester(eventBroker);
		UIElementTester uiElementTester = new UIElementTester(eventBroker);
		UIItemTester uiItemTester = new UIItemTester(eventBroker);
		WindowTester windowTester = new WindowTester(eventBroker);

		// Create an array to check for 'cross talk' (i.e. events being fired
		// on incorrect topics
		EventTester[] allTesters = { appTester, commandTester, contextTester,
				contributionTester, elementContainerTester,
				parameterTester, uiElementTester, uiItemTester, windowTester };

		// Create the test harness and hook up the event publisher
		MTestHarness allData = MTestFactory.eINSTANCE.createTestHarness();
		final UIEventPublisher ep = new UIEventPublisher(rule.getApplicationContext());
		((Notifier) allData).eAdapters().add(ep);
		rule.getApplicationContext().set(UIEventPublisher.class, ep);

		// AppElement
		reset(allTesters);
		String newId = "Some New Id";
		allData.setElementId(newId);
		allData.getTags().add("Testing");

		allData.getPersistedState().put("testing", "Some state");
		checkForFailures(allTesters, appTester);

		// Test that no-ops don't throw events
		appTester.reset();
		allData.setElementId(newId);
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
		allData.setContributionURI("Some URI");
		allData.setObject("Some onbject");
		checkForFailures(allTesters, contributionTester);

		// ElementContainer
		reset(allTesters);
		MMenu menu = MMenuFactory.INSTANCE.createMenu();
		allData.getChildren().add(menu);
		allData.setSelectedElement(menu);
		checkForFailures(allTesters, elementContainerTester);

		// Dirtyable
		reset(allTesters);
		allData.setDirty(!allData.isDirty());
		checkForFailures(allTesters, dirtyableTester);

		// Parameter
		reset(allTesters);
		allData.setName("New Tag");
		allData.setValue("New Value");
		checkForFailures(allTesters, parameterTester);

		// UIElement
		reset(allTesters);
		MTestHarness newParent = MTestFactory.eINSTANCE.createTestHarness();
		allData.setRenderer("New Renderer");
		allData.setParent(newParent);
		allData.setToBeRendered(!allData.isToBeRendered());
		allData.setVisible(!allData.isVisible());
		allData.setOnTop(!allData.isOnTop());
		allData.setWidget("New Widget");
		allData.setContainerData("new Data");
		checkForFailures(allTesters, uiElementTester);

		// UIItem
		reset(allTesters);
		allData.setLabel("New Name");
		allData.setIconURI("New Icon URI");
		allData.setTooltip("New Tooltip");
		checkForFailures(allTesters, uiItemTester);

		// Window tests
		reset(allTesters);
		MWindow window = ((MApplication) applicationElement).getChildren().get(
				0);
		window.setX(1234);
		window.setY(1234);
		window.setWidth(1234);
		window.setHeight(1234);

		MMenu newMainMenu = MMenuFactory.INSTANCE.createMenu();
		window.setMainMenu(newMainMenu);
		checkForFailures(allTesters, windowTester);
	}

	// Verify bug 374534
	@Test
	public void testBrokerCleanup() {
		final String testTopic = "test/374534";
		IEventBroker appEB = rule.getApplicationContext().get(IEventBroker.class);

		IEclipseContext childContext = rule.getApplicationContext().createChild();
		IEventBroker childEB = childContext.get(IEventBroker.class);
		assertNotEquals("child context has same IEventBroker", appEB, childEB);

		final boolean[] seen = { false };
		childEB.subscribe(testTopic, event -> seen[0] = true);

		// ensure the EBs are wired up
		assertFalse(seen[0]);
		appEB.send(testTopic, null);
		assertTrue(seen[0]);

		seen[0] = false;
		childContext.dispose();
		appEB.send(testTopic, null);
		assertFalse(seen[0]);
	}

	private void checkForFailures(EventTester[] allTesters, EventTester tester) {
		ensureAllSet(tester);
		ensureNoCrossTalk(allTesters, tester);
	}

	/**
	 * Ensures that no events were picked up from topics other than the one we
	 * expect to see changes in.
	 */
	private void ensureNoCrossTalk(EventTester[] allTesters, EventTester skipMe) {
		List<EventTester> badTesters = new ArrayList<>();
		for (EventTester t : allTesters) {
			if (t.equals(skipMe))
				continue;

			if (t.getAttIds(true).length > 0)
				badTesters.add(t);
		}

		if (!badTesters.isEmpty()) {
			String msg = "Events were fired in the wrong topic(s): "
					+ badTesters;
			fail(msg);
		}
	}

	private void ensureAllSet(EventTester tester) {
		String[] unfiredIds = tester.getAttIds(false);
		if (unfiredIds.length > 0) {
			StringBuilder msg = new StringBuilder("No event fired:").append(unfiredIds);
			for (String unfiredId : unfiredIds) {
				msg.append(' ').append(unfiredId);
			}
			fail(msg.toString());
		}
	}

	private void reset(EventTester[] allTesters) {
		for (EventTester t : allTesters) {
			t.reset();
		}
	}
}
