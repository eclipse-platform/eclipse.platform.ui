/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.e4.ui.internal.workbench.swt.CSSConstants;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer.TabStateHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;

public class TabStateHandlerTest {
	private StackRendererTestable renderer;
	private TabStateHandler handler;
	private Shell shell;

	@Before
	public void setUp() throws Exception {
		shell = new Shell();
		renderer = new StackRendererTestable();
		handler = renderer.new TabStateHandler();
	}

	@After
	public void tearDown() throws Exception {
		shell.dispose();
	}

	@Test
	public void testValidateElement() throws Exception {
		assertTrue(handler.validateElement(MBasicFactory.INSTANCE.createPart()));
		assertTrue(handler.validateElement(MBasicFactory.INSTANCE
				.createPartStack()));
	}

	@Test
	public void testValidateElementWhenInvalidElement() throws Exception {
		assertFalse(handler.validateElement(MBasicFactory.INSTANCE
				.createTrimBar()));
		assertFalse(handler.validateElement(null));
	}

	@Test
	public void testValidateValues() throws Exception {
		assertTrue(handler.validateValues(null,
				placeHolder(MBasicFactory.INSTANCE.createPart())));
		assertTrue(handler.validateValues(null, CSSConstants.CSS_BUSY_CLASS));
		assertTrue(handler.validateValues(CSSConstants.CSS_BUSY_CLASS, null));
		assertTrue(handler.validateValues(null,
				CSSConstants.CSS_CONTENT_CHANGE_CLASS));
	}

	@Test
	public void testValidateValuesWhenInvalidValue() throws Exception {
		assertFalse(handler.validateValues(null,
				MBasicFactory.INSTANCE.createPart()));
		assertFalse(handler.validateValues(null, "new not supported tag"));
	}

	@Test
	public void testHandleEventWhenTabBusyEvent() throws Exception {
		// given
		MPart part = MBasicFactory.INSTANCE.createPart();
		CTabFolder tabFolder = new CTabFolder(shell, SWT.NONE);
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);

		renderer.tabItemForPart = tabItem;

		// when
		handler.handleEvent(event(new EventParam(UIEvents.EventTags.ELEMENT,
				part), new EventParam(UIEvents.EventTags.NEW_VALUE,
				CSSConstants.CSS_BUSY_CLASS), new EventParam(
				UIEvents.EventTags.OLD_VALUE, null)));

		// then
		assertEquals(0, part.getTags().size());
		assertTrue(renderer.setCSSInfoExecuted);
		assertTrue(renderer.reapplyStylesExecuted);

		tabItem.dispose();
		tabFolder.dispose();
	}

	@Test
	public void testHandleEventWhenTabIdleEvent() throws Exception {
		// given
		MPart part = MBasicFactory.INSTANCE.createPart();
		CTabFolder tabFolder = new CTabFolder(shell, SWT.NONE);
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);

		renderer.tabItemForPart = tabItem;

		// when
		handler.handleEvent(event(new EventParam(UIEvents.EventTags.ELEMENT,
				part), new EventParam(UIEvents.EventTags.NEW_VALUE, null),
				new EventParam(UIEvents.EventTags.OLD_VALUE,
						CSSConstants.CSS_BUSY_CLASS)));

		// then
		assertEquals(0, part.getTags().size());
		assertTrue(renderer.setCSSInfoExecuted);
		assertTrue(renderer.reapplyStylesExecuted);

		tabItem.dispose();
		tabFolder.dispose();
	}

	@Test
	public void testHandleEventWhenTabContentChangedEventAndTabInactive()
			throws Exception {
		// given
		MPart part = MBasicFactory.INSTANCE.createPart();
		CTabFolder tabFolder = new CTabFolder(shell, SWT.NONE);
		CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NONE);
		CTabItem tabItem2 = new CTabItem(tabFolder, SWT.NONE);

		part.getTags().add(CSSConstants.CSS_CONTENT_CHANGE_CLASS);
		tabFolder.setSelection(tabItem2);
		renderer.tabItemForPart = tabItem1;

		// when
		handler.handleEvent(event(new EventParam(UIEvents.EventTags.ELEMENT,
				part), new EventParam(UIEvents.EventTags.NEW_VALUE,
				CSSConstants.CSS_CONTENT_CHANGE_CLASS), new EventParam(
				UIEvents.EventTags.OLD_VALUE, null)));

		// then
		assertTrue(part.getTags().contains(CSSConstants.CSS_HIGHLIGHTED_CLASS));
		assertFalse(part.getTags().contains(
				CSSConstants.CSS_CONTENT_CHANGE_CLASS));
		assertTrue(renderer.setCSSInfoExecuted);
		assertTrue(renderer.reapplyStylesExecuted);

		tabItem1.dispose();
		tabItem2.dispose();
		tabFolder.dispose();
	}

	@Test
	public void testHandleEventWhenTabContentChangedEventAndTabActive()
			throws Exception {
		// given
		MPart part = MBasicFactory.INSTANCE.createPart();
		CTabFolder tabFolder = new CTabFolder(shell, SWT.NONE);
		CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NONE);
		CTabItem tabItem2 = new CTabItem(tabFolder, SWT.NONE);

		part.getTags().add(CSSConstants.CSS_CONTENT_CHANGE_CLASS);
		tabFolder.setSelection(tabItem1);
		renderer.tabItemForPart = tabItem1;

		// when
		handler.handleEvent(event(new EventParam(UIEvents.EventTags.ELEMENT,
				part), new EventParam(UIEvents.EventTags.NEW_VALUE,
				CSSConstants.CSS_CONTENT_CHANGE_CLASS), new EventParam(
				UIEvents.EventTags.OLD_VALUE, null)));

		// then
		assertFalse(part.getTags().contains(CSSConstants.CSS_HIGHLIGHTED_CLASS));
		assertFalse(part.getTags().contains(
				CSSConstants.CSS_CONTENT_CHANGE_CLASS));
		assertTrue(renderer.setCSSInfoExecuted);
		assertTrue(renderer.reapplyStylesExecuted);

		tabItem1.dispose();
		tabItem2.dispose();
		tabFolder.dispose();
	}

	@Test
	public void testHandleEventWhenTabActivateEventAndItsContentChanged()
			throws Exception {
		// given
		MPart part = MBasicFactory.INSTANCE.createPart();
		CTabFolder tabFolder = new CTabFolder(shell, SWT.NONE);
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);

		part.getTags().add(CSSConstants.CSS_HIGHLIGHTED_CLASS);
		renderer.tabItemForPart = tabItem;

		// when
		handler.handleEvent(event(new EventParam(UIEvents.EventTags.ELEMENT,
				part), new EventParam(UIEvents.EventTags.NEW_VALUE,
				placeHolder(part)), new EventParam(
				UIEvents.EventTags.OLD_VALUE, null)));

		// then
		assertFalse(part.getTags().contains(CSSConstants.CSS_HIGHLIGHTED_CLASS));
		assertTrue(renderer.setCSSInfoExecuted);
		assertTrue(renderer.reapplyStylesExecuted);

		tabItem.dispose();
		tabFolder.dispose();
	}

	@Test
	public void testHandleEventWhenTabActivateEventAndTabItemForPartNotFound()
			throws Exception {
		// given
		MPart part = MBasicFactory.INSTANCE.createPart();
		CTabFolder tabFolder = new CTabFolder(shell, SWT.NONE);
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE);

		part.getTags().add(CSSConstants.CSS_HIGHLIGHTED_CLASS);
		renderer.tabItemForPart = null; // just to expose the scenario condition

		// when
		handler.handleEvent(event(new EventParam(UIEvents.EventTags.ELEMENT,
				part), new EventParam(UIEvents.EventTags.NEW_VALUE,
				placeHolder(part)), new EventParam(
				UIEvents.EventTags.OLD_VALUE, null)));

		// then
		assertTrue(part.getTags().contains(CSSConstants.CSS_HIGHLIGHTED_CLASS));
		assertFalse(renderer.setCSSInfoExecuted);
		assertFalse(renderer.reapplyStylesExecuted);

		tabItem.dispose();
		tabFolder.dispose();
	}

	// helper functions
	private static class StackRendererTestable extends StackRenderer {
		boolean setCSSInfoExecuted;
		boolean reapplyStylesExecuted;
		CTabItem tabItemForPart;

		@Override
		public void setCSSInfo(MUIElement me, Object widget) {
			setCSSInfoExecuted = true;
		}

		@Override
		protected void reapplyStyles(Widget widget) {
			reapplyStylesExecuted = true;
		}

		@Override
		public CTabItem findItemForPart(MPart part) {
			return tabItemForPart;
		}
	}

	private MPlaceholder placeHolder(final MPart part) {
		return (MPlaceholder) Proxy.newProxyInstance(getClass()
				.getClassLoader(), new Class<?>[] { MPlaceholder.class },
				new InvocationHandler() {
					@Override
					public Object invoke(Object arg0, Method method,
							Object[] arg2) throws Throwable {
						if ("getRef".equals(method.getName())) {
							return part;
						}
						return null;
					}
				});
	}

	private class EventParam implements Map.Entry<String, Object> {
		private String key;
		private Object value;

		public EventParam(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public Object setValue(Object arg0) {
			return null;
		}
	}

	private Event event(EventParam... params) {
		HashMap<String, Object> paramsMap = new HashMap<String, Object>();
		for (EventParam param : params) {
			paramsMap.put(param.getKey(), param.getValue());
		}
		return new Event("topic", paramsMap);
	}
}