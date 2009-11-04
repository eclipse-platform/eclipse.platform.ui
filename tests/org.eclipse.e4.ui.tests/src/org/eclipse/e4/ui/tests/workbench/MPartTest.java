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

package org.eclipse.e4.ui.tests.workbench;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartSashContainer;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.events.EventBrokerFactory;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.ui.tests.Activator;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.widgets.CTabItem;
import org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine;
import org.eclipse.e4.workbench.ui.internal.ReflectionContributionFactory;
import org.eclipse.e4.workbench.ui.internal.UIEventPublisher;
import org.eclipse.e4.workbench.ui.internal.Workbench;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class MPartTest extends TestCase {
	private IEclipseContext appContext;
	private static IContributionFactory contributionFactory;

	private IEclipseContext getAppContext() {
		if (appContext == null) {

			IEclipseContext serviceContext = EclipseContextFactory
					.createServiceContext(Activator.getDefault().getBundle()
							.getBundleContext());
			appContext = Workbench.createWorkbenchContext(serviceContext,
					RegistryFactory.getRegistry(), null, null);
			MApplication app = MApplicationFactory.eINSTANCE
					.createApplication();
			appContext.set(MApplication.class.getName(), app);
			appContext.set(IContributionFactory.class.getName(), getCFactory());
			appContext.set(IEclipseContext.class.getName(), appContext);
			appContext.set(IEventBroker.class.getName(), EventBrokerFactory
					.newEventBroker());
			app.setContext(appContext);
		}
		return appContext;
	}

	static IContributionFactory getCFactory() {
		if (contributionFactory == null) {
			contributionFactory = new ReflectionContributionFactory(
					RegistryFactory.getRegistry());
		}
		return contributionFactory;
	}

	private Display getDisplay() {
		display = Display.getCurrent();
		if (display == null) {
			display = new Display();
		}
		return display;
	}

	protected void processEventLoop() {
		if (display != null) {
			while (display.readAndDispatch())
				;
		}
	}

	private Widget topWidget;
	private Display display;

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (topWidget != null) {
			topWidget.dispose();
			topWidget = null;
		}
	}

	public void testCreateView() {
		final MWindow window = createWindowWithOneView("Part Name");
		((Notifier) window.getParent()).eAdapters().add(
				new UIEventPublisher(getAppContext()));
		Realm.runWithDefault(SWTObservables.getRealm(getDisplay()),
				new Runnable() {
					public void run() {
						IEclipseContext context = getAppContext();

						PartRenderingEngine renderer = (PartRenderingEngine) getCFactory()
								.create(PartRenderingEngine.engineURI, context);

						Object o = renderer.createGui(window);
						assertNotNull(o);
						topWidget = (Widget) o;
						assertTrue(topWidget instanceof Shell);
						Shell shell = (Shell) topWidget;
						assertEquals("MyWindow", shell.getText());
						Control[] controls = shell.getChildren();
						assertEquals(1, controls.length);
						SashForm sash = (SashForm) controls[0];
						Control[] sashChildren = sash.getChildren();
						assertEquals(1, sashChildren.length);

						// HACK: see bug #280632 - always a composite around
						// CTabFolder so can implement margins
						Composite marginHolder = (Composite) sashChildren[0];
						assertEquals(1, marginHolder.getChildren().length);
						CTabFolder folder = (CTabFolder) marginHolder
								.getChildren()[0];
						CTabItem item = folder.getItem(0);
						assertEquals("Part Name", item.getText());

						MPartSashContainer container = (MPartSashContainer) window
								.getChildren().get(0);
						MPartStack stack = (MPartStack) container.getChildren()
								.get(0);
						MPart part = stack.getChildren().get(0);

						part.setName("Another Name");
						assertEquals("Another Name", item.getText());
					}
				});
	}

	private MWindow createWindowWithOneView(String partName) {
		MApplication application = (MApplication) getAppContext().get(
				MApplication.class.getName());

		final MWindow window = MApplicationFactory.eINSTANCE.createWindow();
		window.setHeight(300);
		window.setWidth(400);
		window.setName("MyWindow");
		MPartSashContainer sash = MApplicationFactory.eINSTANCE
				.createPartSashContainer();
		window.getChildren().add(sash);
		MPartStack stack = MApplicationFactory.eINSTANCE.createPartStack();
		sash.getChildren().add(stack);
		MPart contributedPart = MApplicationFactory.eINSTANCE.createPart();
		stack.getChildren().add(contributedPart);
		contributedPart.setName(partName);
		contributedPart
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");

		application.getChildren().add(window);

		return window;
	}

}
