/*******************************************************************************
 * Copyright (c) 2012, 2015 Brian de Alwis and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brian de Alwis (MTI) - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Simple tests of the event broker
 */
public class EventBrokerTest extends UITest {
	private static final String TEST_TOPIC = "test/EventBrokerTest";

	private AtomicInteger seen;
	private IEclipseContext context;

	@Before
	@Override
	public void setUp() throws Exception {
		super.setUp();
		seen = new AtomicInteger(0);
		context = application.getContext().createChild(getClass().getName());
	}

	@After
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		context.dispose();
	}

	@Test
	public void testPublish() {
		IEventBroker eb = context.get(IEventBroker.class);
		assertNotNull(eb);

		eb.subscribe(TEST_TOPIC, new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				if (TEST_TOPIC.equals(event.getTopic())) {
					seen.incrementAndGet();
				}
			}
		});
		eb.send(TEST_TOPIC, new Object());
		assertEquals(1, seen.get());
	}

	/**
	 * ensure handlers are automatically unsubscribed when a broker is disposed
	 */
	@Test
	public void testUnsubscribeOnDispose() {
		// create two IEBs: the parent to publish the event, the child to
		// receive
		IEventBroker publisher = context.get(IEventBroker.class);
		assertNotNull(publisher);
		IEclipseContext child = context.createChild();
		IEventBroker subscriber = child.get(IEventBroker.class);
		assertNotNull(subscriber);
		subscriber.subscribe(TEST_TOPIC, new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				if (TEST_TOPIC.equals(event.getTopic())) {
					seen.incrementAndGet();
				}
			}
		});

		publisher.send(TEST_TOPIC, new Object());
		assertEquals(1, seen.get());

		child.dispose(); // subscriber should unsubscribe from notifications

		publisher.send(TEST_TOPIC, new Object());
		assertEquals("event broker did not properly unsubscribe on dispose", 1,
				seen.get());
	}

	@Test
	public void testMultipleSubscriptions() {
		IEventBroker eb = context.get(IEventBroker.class);
		assertNotNull(eb);
		EventHandler handler = new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				if (TEST_TOPIC.equals(event.getTopic())) {
					seen.incrementAndGet();
				}
			}
		};

		eb.subscribe(TEST_TOPIC, handler);
		eb.subscribe("*", handler);
		eb.send(TEST_TOPIC, new Object());
		assertEquals(2, seen.get());

		eb.unsubscribe(handler);
		eb.send(TEST_TOPIC, new Object());
		assertEquals("subscription was not removed", 2, seen.get());
	}

}
