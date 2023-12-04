/*******************************************************************************
 * Copyright (c) 2012, 2015 IBM Corporation and others.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.emf.common.util.EList;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;

/**
 * Tests the translation of the EMF Notification.* types to UIEvents.EventType.*
 * by performing various manipulations on an MApplication's elementId and tags.
 */
public class UIEventTypesTest extends HeadlessApplicationElementTest {
	private Event event;
	private int eventCount;

	@Override
	protected MApplicationElement createApplicationElement(IEclipseContext appContext) throws Exception {
		return MApplicationFactory.INSTANCE.createApplication();
	}

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		IEventBroker appEB = rule.getApplicationContext().get(IEventBroker.class);

		eventCount = 0;
		appEB.subscribe(UIEvents.ApplicationElement.TOPIC_TAGS, event -> {
			eventCount++;
			UIEventTypesTest.this.event = event;
		});
		appEB.subscribe(UIEvents.ApplicationElement.TOPIC_ELEMENTID, event -> {
			eventCount++;
			UIEventTypesTest.this.event = event;
		});

	}

	@Test
	public void testAdd() {
		applicationElement.getTags().add("0");
		assertEquals(1, eventCount);
		assertNotNull(event);
		assertEquals(applicationElement, event.getProperty(UIEvents.EventTags.ELEMENT));
		assertEquals(UIEvents.ApplicationElement.TAGS, event.getProperty(UIEvents.EventTags.ATTNAME));
		assertEquals(UIEvents.EventTypes.ADD, event.getProperty(UIEvents.EventTags.TYPE));
		assertEquals(0, event.getProperty(UIEvents.EventTags.POSITION));
		assertEquals(null, event.getProperty(UIEvents.EventTags.OLD_VALUE));
		assertEquals("0", event.getProperty(UIEvents.EventTags.NEW_VALUE));
	}

	@Test
	public void testAddMany() {
		applicationElement.getTags().addAll(Arrays.asList("0", "1", "2"));
		assertEquals(1, eventCount);
		assertNotNull(event);
		assertEquals(applicationElement, event.getProperty(UIEvents.EventTags.ELEMENT));
		assertEquals(UIEvents.ApplicationElement.TAGS, event.getProperty(UIEvents.EventTags.ATTNAME));
		assertEquals(UIEvents.EventTypes.ADD_MANY, event.getProperty(UIEvents.EventTags.TYPE));
		assertEquals(0, event.getProperty(UIEvents.EventTags.POSITION));
		assertEquals(null, event.getProperty(UIEvents.EventTags.OLD_VALUE));
		assertEquals(Arrays.asList("0", "1", "2"), event.getProperty(UIEvents.EventTags.NEW_VALUE));
	}

	@Test
	public void testRemove() {
		applicationElement.getTags().addAll(Arrays.asList("0", "1", "2"));
		assertEquals(1, eventCount);
		assertNotNull(event);
		event = null;

		applicationElement.getTags().remove("1");
		assertEquals(2, eventCount);
		assertNotNull(event);
		assertEquals(applicationElement, event.getProperty(UIEvents.EventTags.ELEMENT));
		assertEquals(1, event.getProperty(UIEvents.EventTags.POSITION));
		assertEquals(UIEvents.ApplicationElement.TAGS, event.getProperty(UIEvents.EventTags.ATTNAME));
		assertEquals(UIEvents.EventTypes.REMOVE, event.getProperty(UIEvents.EventTags.TYPE));
		assertEquals("1", event.getProperty(UIEvents.EventTags.OLD_VALUE));
		assertEquals(null, event.getProperty(UIEvents.EventTags.NEW_VALUE));
	}

	@Test
	public void testRemoveMany() {
		applicationElement.getTags().addAll(Arrays.asList("0", "1", "2"));
		assertEquals(1, eventCount);
		assertNotNull(event);
		event = null;

		applicationElement.getTags().removeAll(Arrays.asList("2", "0"));
		assertEquals(2, eventCount);
		assertNotNull(event);
		assertEquals(applicationElement, event.getProperty(UIEvents.EventTags.ELEMENT));
		assertEquals(UIEvents.ApplicationElement.TAGS, event.getProperty(UIEvents.EventTags.ATTNAME));
		assertEquals(UIEvents.EventTypes.REMOVE_MANY, event.getProperty(UIEvents.EventTags.TYPE));
		assertEquals(Arrays.asList("0", "2"), event.getProperty(UIEvents.EventTags.OLD_VALUE));
		assertTrue(event.getProperty(UIEvents.EventTags.POSITION) instanceof int[]);
		int[] removedPositions = (int[]) event.getProperty(UIEvents.EventTags.POSITION);
		assertEquals(0, removedPositions[0]);
		assertEquals(2, removedPositions[1]);
	}

	@Test
	public void testRetain() {
		applicationElement.getTags().addAll(Arrays.asList("0", "1", "2"));
		assertEquals(1, eventCount);
		assertNotNull(event);
		event = null;

		// retain should generate two REMOVE events
		applicationElement.getTags().retainAll(Arrays.asList("0"));

		assertEquals(3, eventCount);
		assertNotNull(event);
		assertEquals(applicationElement, event.getProperty(UIEvents.EventTags.ELEMENT));
		assertEquals(UIEvents.ApplicationElement.TAGS, event.getProperty(UIEvents.EventTags.ATTNAME));
		assertEquals(UIEvents.EventTypes.REMOVE, event.getProperty(UIEvents.EventTags.TYPE));
	}

	@Test
	public void testClear() {
		applicationElement.getTags().addAll(Arrays.asList("0", "1", "2"));
		assertEquals(1, eventCount);
		assertNotNull(event);
		event = null;

		applicationElement.getTags().clear();
		assertEquals(2, eventCount);
		assertNotNull(event);
		assertEquals(applicationElement, event.getProperty(UIEvents.EventTags.ELEMENT));
		assertEquals(UIEvents.ApplicationElement.TAGS, event.getProperty(UIEvents.EventTags.ATTNAME));
		assertEquals(UIEvents.EventTypes.REMOVE_MANY, event.getProperty(UIEvents.EventTags.TYPE));
		assertEquals(Arrays.asList("0", "1", "2"), event.getProperty(UIEvents.EventTags.OLD_VALUE));
		assertEquals(null, event.getProperty(UIEvents.EventTags.NEW_VALUE));
		assertEquals(null, event.getProperty(UIEvents.EventTags.POSITION));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testMove() {
		applicationElement.getTags().addAll(Arrays.asList("0", "1", "2"));
		assertEquals(1, eventCount);
		assertNotNull(event);
		event = null;

		((EList) applicationElement.getTags()).move(0, 2);
		assertEquals(2, eventCount);
		assertNotNull(event);
		assertEquals(applicationElement, event.getProperty(UIEvents.EventTags.ELEMENT));
		assertEquals(UIEvents.ApplicationElement.TAGS, event.getProperty(UIEvents.EventTags.ATTNAME));
		assertEquals(UIEvents.EventTypes.MOVE, event.getProperty(UIEvents.EventTags.TYPE));
		assertEquals("2", event.getProperty(UIEvents.EventTags.NEW_VALUE));
		assertEquals("former position", 2, event.getProperty(UIEvents.EventTags.OLD_VALUE));
		assertEquals("new position", 0, event.getProperty(UIEvents.EventTags.POSITION));
	}

	@Test
	public void testSet() {
		// set the elementId to "aaa" and then to null
		applicationElement.setElementId("aaa");
		assertEquals(1, eventCount);
		assertNotNull(event);
		assertEquals(applicationElement, event.getProperty(UIEvents.EventTags.ELEMENT));
		assertEquals(UIEvents.ApplicationElement.ELEMENTID, event.getProperty(UIEvents.EventTags.ATTNAME));
		assertEquals(UIEvents.EventTypes.SET, event.getProperty(UIEvents.EventTags.TYPE));
		assertEquals(null, event.getProperty(UIEvents.EventTags.OLD_VALUE));
		assertEquals("aaa", event.getProperty(UIEvents.EventTags.NEW_VALUE));

		applicationElement.setElementId(null);
		assertEquals(2, eventCount);
		assertNotNull(event);
		assertEquals(applicationElement, event.getProperty(UIEvents.EventTags.ELEMENT));
		assertEquals(UIEvents.ApplicationElement.ELEMENTID, event.getProperty(UIEvents.EventTags.ATTNAME));
		assertEquals(UIEvents.EventTypes.SET, event.getProperty(UIEvents.EventTags.TYPE));
		assertEquals("aaa", event.getProperty(UIEvents.EventTags.OLD_VALUE));
		assertEquals(null, event.getProperty(UIEvents.EventTags.NEW_VALUE));
	}

}
