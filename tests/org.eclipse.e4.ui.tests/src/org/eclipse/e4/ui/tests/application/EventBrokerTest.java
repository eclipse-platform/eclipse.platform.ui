/*******************************************************************************
 * Copyright (c) 2012, 2015 Brian de Alwis and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brian de Alwis (MTI) - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.service.event.EventHandler;

/**
 * Simple tests of the event broker
 */
public class EventBrokerTest extends UITest {
	private static final String TEST_TOPIC = "test/EventBrokerTest";

	private AtomicInteger seen;
	private IEclipseContext context;

	@BeforeEach
	@Override
	public void setUp() throws Exception {
		super.setUp();
		seen = new AtomicInteger(0);
		context = application.getContext().createChild(getClass().getName());
	}

	@AfterEach
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		context.dispose();
	}

	@Test
	public void testPublish() {
		IEventBroker eb = context.get(IEventBroker.class);
		assertNotNull(eb);

		eb.subscribe(TEST_TOPIC, event -> {
			if (TEST_TOPIC.equals(event.getTopic())) {
				seen.incrementAndGet();
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
		subscriber.subscribe(TEST_TOPIC, event -> {
			if (TEST_TOPIC.equals(event.getTopic())) {
				seen.incrementAndGet();
			}
		});

		publisher.send(TEST_TOPIC, new Object());
		assertEquals(1, seen.get());

		child.dispose(); // subscriber should unsubscribe from notifications

		publisher.send(TEST_TOPIC, new Object());
		assertEquals(1, seen.get(), "event broker did not properly unsubscribe on dispose");
	}

	@Test
	public void testMultipleSubscriptions() {
		IEventBroker eb = context.get(IEventBroker.class);
		assertNotNull(eb);
		EventHandler handler = event -> {
			if (TEST_TOPIC.equals(event.getTopic())) {
				seen.incrementAndGet();
			}
		};

		eb.subscribe(TEST_TOPIC, handler);
		eb.subscribe("*", handler);
		eb.send(TEST_TOPIC, new Object());
		assertEquals(2, seen.get());

		eb.unsubscribe(handler);
		eb.send(TEST_TOPIC, new Object());
		assertEquals(2, seen.get(), "subscription was not removed");
	}

}
