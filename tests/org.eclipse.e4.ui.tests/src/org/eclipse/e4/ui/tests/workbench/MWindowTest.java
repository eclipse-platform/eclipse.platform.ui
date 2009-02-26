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
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.ApplicationFactory;
import org.eclipse.e4.ui.model.application.MContributedPart;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MSashForm;
import org.eclipse.e4.ui.model.application.MStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.tests.Activator;
import org.eclipse.e4.workbench.ui.internal.ReflectionContributionFactory;
import org.eclipse.e4.workbench.ui.internal.Workbench;
import org.eclipse.e4.workbench.ui.renderers.swt.PartRenderer;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

/**
 *
 */
public class MWindowTest extends TestCase {
	private IEclipseContext appContext;
	private IContributionFactory contributionFactory;

	private IEclipseContext getAppContext() {
		if (appContext == null) {
			IEclipseContext serviceContext = EclipseContextFactory
					.createServiceContext(Activator.getDefault().getBundle()
							.getBundleContext());
			appContext = EclipseContextFactory.create(serviceContext, null);
			appContext.set(IContextConstants.DEBUG_STRING, "application"); //$NON-NLS-1$
		}
		return appContext;
	}

	private IContributionFactory getCFactory() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		if (topWidget != null) {
			topWidget.dispose();
			topWidget = null;
		}
	}

	public void testCreateWindow() {
		final MWindow<MPart<?>> window = ApplicationFactory.eINSTANCE
				.createMWindow();
		window.setHeight(300);
		window.setWidth(400);
		window.setName("MyWindow");
		Realm.runWithDefault(SWTObservables.getRealm(getDisplay()),
				new Runnable() {

					public void run() {
						IEclipseContext context = getAppContext();
						PartRenderer renderer = new PartRenderer(getCFactory(),
								context);
						Workbench.initializeRenderer(RegistryFactory
								.getRegistry(), renderer, appContext,
								getCFactory());
						Object o = renderer.createGui(window);
						assertNotNull(o);
						topWidget = (Widget) o;
						assertTrue(topWidget instanceof Shell);
						assertEquals("MyWindow", ((Shell) topWidget).getText());
					}
				});
	}

	public void testCreateView() {
		final MWindow<MPart<?>> window = ApplicationFactory.eINSTANCE
				.createMWindow();
		window.setHeight(300);
		window.setWidth(400);
		window.setName("MyWindow");
		MSashForm<MPart<?>> sash = ApplicationFactory.eINSTANCE
				.createMSashForm();
		window.getChildren().add(sash);
		MStack stack = ApplicationFactory.eINSTANCE.createMStack();
		sash.getChildren().add(stack);
		MContributedPart<MPart<?>> contributedPart = ApplicationFactory.eINSTANCE
				.createMContributedPart();
		stack.getChildren().add(contributedPart);
		contributedPart.setName("Sample View");
		contributedPart
				.setURI("platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		Realm.runWithDefault(SWTObservables.getRealm(getDisplay()),
				new Runnable() {
					public void run() {
						IEclipseContext context = getAppContext();
						PartRenderer renderer = new PartRenderer(getCFactory(),
								context);
						Workbench.initializeRenderer(RegistryFactory
								.getRegistry(), renderer, appContext,
								getCFactory());
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
						CTabFolder folder = (CTabFolder) sashChildren[0];
						assertEquals(1, folder.getItemCount());
						Control c = folder.getItem(0).getControl();
						assertTrue(c instanceof Composite);
						Control[] viewPart = ((Composite) c).getChildren();
						assertEquals(1, viewPart.length);
						assertTrue(viewPart[0] instanceof Tree);
					}
				});
	}
}
