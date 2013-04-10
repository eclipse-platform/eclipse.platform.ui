/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.CSSConstants;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;

public class StackRendererTest extends TestCase {
	private IEclipseContext context;
	private E4Workbench wb;
	private MPart part;
	private CTabItemStylingMethodsListener executedMethodsListener;

	@Override
	protected void setUp() throws Exception {
		context = E4Application.createDefaultContext();
		context.set(E4Workbench.PRESENTATION_URI_ARG,
				PartRenderingEngine.engineURI);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		MPartStack partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setLabel("some title");

		application.getChildren().add(window);
		application.setSelectedElement(window);

		window.getChildren().add(partStack);
		partStack.getChildren().add(part);

		application.setContext(context);
		context.set(MApplication.class.getName(), application);

		executedMethodsListener = new CTabItemStylingMethodsListener(part);

		wb = new E4Workbench(application, context);
		wb.getContext().set(
				IStylingEngine.class,
				(IStylingEngine) Proxy.newProxyInstance(getClass()
						.getClassLoader(),
						new Class<?>[] { IStylingEngine.class },
						executedMethodsListener));

		wb.createAndRunUI(window);
		while (Display.getDefault().readAndDispatch())
			;
	}

	@Override
	protected void tearDown() throws Exception {
		if (wb != null) {
			wb.close();
		}
		context.dispose();
	}

	public void testTagsChangeHandlerWhenBusyTagAddEvent() throws Exception {
		part.getTags().add(CSSConstants.CSS_BUSY_CLASS);

		assertEquals(1,
				executedMethodsListener
						.getMethodExecutionCount("setClassnameAndId(.+)"));
		assertTrue(executedMethodsListener
				.isMethodExecuted("setClassnameAndId(.+MPart "
						+ CSSConstants.CSS_BUSY_CLASS + ".+)"));
	}

	public void testTagsChangeHandlerWhenBusyTagRemoveEvent() throws Exception {
		part.getTags().add(CSSConstants.CSS_BUSY_CLASS);
		part.getTags().remove(CSSConstants.CSS_BUSY_CLASS);

		assertEquals(2,
				executedMethodsListener
						.getMethodExecutionCount("setClassnameAndId(.+)"));
		assertTrue(executedMethodsListener
				.isMethodExecuted("setClassnameAndId(.+MPart "
						+ CSSConstants.CSS_BUSY_CLASS + ".+)"));
		assertTrue(executedMethodsListener
				.isMethodExecuted("setClassnameAndId(.+MPart.+)"));
	}

	public void testTagsChangeHandlerWhenNotBusyTagModifiedEvent()
			throws Exception {
		part.getTags().add("not busy tag");

		assertEquals(0,
				executedMethodsListener
						.getMethodExecutionCount("setClassnameAndId(.+)"));
	}

	public void testTagsChangeHandlerWhenNotTagReleatedEvent() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(UIEvents.EventTags.ELEMENT, part);

		context.get(IEventBroker.class).send(
				UIEvents.ApplicationElement.TOPIC_ELEMENTID.replace(
						UIEvents.ALL_SUB_TOPICS, UIEvents.EventTypes.ADD),
				params);

		assertEquals(0,
				executedMethodsListener
						.getMethodExecutionCount("setClassnameAndId(.+)"));
	}

	// helper functions
	private static class CTabItemStylingMethodsListener implements
			InvocationHandler {
		private MPart part;
		private List<String> methods;

		public CTabItemStylingMethodsListener(MPart part) {
			this.part = part;
			methods = new ArrayList<String>();
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (isTabItemForPart(args[0])) {
				methods.add(String.format("%s(%s)", method.getName(),
						Arrays.toString(args)));
			}
			return null;
		}

		private boolean isTabItemForPart(Object obj) {
			return obj instanceof CTabItem
					&& part.getLabel().equals(((CTabItem) obj).getText());
		}

		public boolean isMethodExecuted(String methodPattern) {
			return getMethodExecutionCount(methodPattern) > 0;
		}

		public int getMethodExecutionCount(String methodPattern) {
			int result = 0;
			for (String method : methods) {
				if (method.matches(methodPattern)) {
					result++;
				}
			}
			return result;
		}
	}
}
