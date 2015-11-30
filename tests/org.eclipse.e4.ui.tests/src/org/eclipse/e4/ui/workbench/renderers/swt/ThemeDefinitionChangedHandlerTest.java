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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.resources.IResourcesRegistry;
import org.eclipse.e4.ui.css.swt.resources.ResourceByDefinitionKey;
import org.eclipse.e4.ui.css.swt.resources.SWTResourcesRegistry;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.renderers.swt.WBWRenderer.ThemeDefinitionChangedHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Resource;
import org.junit.Test;
import org.osgi.service.event.Event;

/**
 *
 */
public class ThemeDefinitionChangedHandlerTest {

	@Test
	public void testHandleEventWhenThemeChanged() throws Exception {
		// given
		final MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(MBasicFactory.INSTANCE.createWindow());
		application.getChildren().add(MBasicFactory.INSTANCE.createWindow());

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(IEventBroker.DATA, application);

		Event event = new Event("topic", params);

		// resources removed from registry that have to be disposed
		Resource resource1 = mock(Resource.class);
		doReturn(false).when(resource1).isDisposed();

		Resource resource2 = mock(Resource.class);
		doReturn(true).when(resource2).isDisposed();

		Object resource3 = new Object();

		List<Object> removedResources = new ArrayList<Object>();
		removedResources.add(resource1);
		removedResources.add(resource2);
		removedResources.add(resource3);
		//

		SWTResourcesRegistry registry = mock(SWTResourcesRegistry.class);
		doReturn(removedResources).when(registry)
				.removeResourcesByKeyTypeAndType(ResourceByDefinitionKey.class,
						Font.class, Color.class);

		CSSEngine engine = mock(CSSEngine.class);
		doReturn(registry).when(engine).getResourcesRegistry();

		ThemeDefinitionChangedHandlerTestable handler = spy(new ThemeDefinitionChangedHandlerTestable());
		doReturn(engine).when(handler).getEngine(any(MWindow.class));

		// when
		handler.handleEvent(event);

		// then
		verify(engine, times(1)).reapply();

		verify(handler, times(1)).removeResources(registry);
		assertEquals(1, handler.getUnusedResources().size());
		assertTrue(handler.getUnusedResources().contains(resource1));

		verify(resource1, times(1)).isDisposed();
		verify(resource1, never()).dispose();

		verify(resource2, times(1)).isDisposed();
		verify(resource2, never()).dispose();
	}

	@Test
	public void testHandleEventWhenElementIsNotMApplication() throws Exception {
		// given
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(IEventBroker.DATA, MBasicFactory.INSTANCE.createWindow());

		Event event = new Event("topic", params);

		CSSEngine engine = mock(CSSEngine.class);

		ThemeDefinitionChangedHandlerTestable handler = spy(new ThemeDefinitionChangedHandlerTestable());
		doReturn(engine).when(handler).getEngine(any(MWindow.class));

		// when
		handler.handleEvent(event);

		// then
		verify(engine, never()).reapply();
		verify(handler, never()).removeResources(any(IResourcesRegistry.class));
		assertEquals(0, handler.getUnusedResources().size());
	}

	@Test
	public void testHandleEventWhenCSSEngineNotFoundForWidget()
			throws Exception {
		// given
		MWindow window1 = MBasicFactory.INSTANCE.createWindow();
		MWindow window2 = MBasicFactory.INSTANCE.createWindow();

		final MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		application.getChildren().add(window1);
		application.getChildren().add(window2);

		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(IEventBroker.DATA, application);

		Event event = new Event("topic", params);

		SWTResourcesRegistry registry = mock(SWTResourcesRegistry.class);

		CSSEngine engine = mock(CSSEngine.class);
		doReturn(registry).when(engine).getResourcesRegistry();

		ThemeDefinitionChangedHandlerTestable handler = spy(new ThemeDefinitionChangedHandlerTestable());
		doReturn(null).when(handler).getEngine(window1);
		doReturn(engine).when(handler).getEngine(window2);

		// when
		handler.handleEvent(event);

		// then
		verify(engine, times(1)).reapply();
		verify(handler, times(1)).removeResources(registry);
		assertEquals(0, handler.getUnusedResources().size());
	}

	@Test
	public void testDisposeHandler() throws Exception {
		// given
		ThemeDefinitionChangedHandlerTestable handler = spy(new ThemeDefinitionChangedHandlerTestable());

		Resource resource1 = mock(Resource.class);
		doReturn(false).when(resource1).isDisposed();
		handler.getUnusedResources().add(resource1);

		Resource resource2 = mock(Resource.class);
		doReturn(true).when(resource2).isDisposed();
		handler.getUnusedResources().add(resource2);

		// when
		handler.dispose();

		// then
		assertTrue(handler.getUnusedResources().isEmpty());

		verify(resource1, times(1)).isDisposed();
		verify(resource1, times(1)).dispose();

		verify(resource2, times(1)).isDisposed();
		verify(resource2, never()).dispose();
	}

	protected static class ThemeDefinitionChangedHandlerTestable extends
			ThemeDefinitionChangedHandler {
		List<Object> processedRemovedResources;

		@Override
		public CSSEngine getEngine(MWindow window) {
			return super.getEngine(window);
		}

		@Override
		public List<Object> removeResources(IResourcesRegistry registry) {
			return super.removeResources(registry);
		}

		public Set<Resource> getUnusedResources() {
			return unusedResources;
		}
	}
}