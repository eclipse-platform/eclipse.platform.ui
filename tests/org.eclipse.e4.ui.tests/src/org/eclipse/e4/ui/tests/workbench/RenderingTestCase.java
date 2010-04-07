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
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.tests.Activator;
import org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
import org.eclipse.e4.workbench.ui.internal.ReflectionContributionFactory;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

/**
 * This is the base class for tests involving the interactions between the UI
 * Model and the SWT Renderer. It is optimized to reduce the overhead
 * <p>
 * It provides various utility methods but also enforces automatic tests to
 * ensure that all models rendered through it conform to the expected structural
 * result:
 * <ol>
 * <li>All visible UI Model elements result in a widget</li>
 * <li>The UI Model element and its SWT Widget are correctly 'bound'</li>
 * <li>The correct SWT widget is created for a given UI Model element</li>
 * <ul>
 * <li>MWindow -> Shell</li>
 * <li>MSashForm -> SashForm</li>
 * <li>MStack -> CTabFol2der</li>
 * <li>MContributedPart -> Composite</li>
 * </ul>
 * <li>The name of an MItem is correctly set in the resulting SWT widget</li>
 * <li>The Image of an MItem is correctly set in the resulting SWT widget</li>
 * <li>The Tooltip Text of an MItem is correctly set in the resulting SWT widget
 * </li>
 * </ol>
 * </p>
 */
public class RenderingTestCase extends TestCase {
	// 'constant' fields: static for all tests
	protected static IContributionFactory contributionFactory = new ReflectionContributionFactory(
			RegistryFactory.getRegistry());

	protected static IEclipseContext serviceContext = EclipseContextFactory
			.getServiceContext(Activator.getDefault().getBundle()
					.getBundleContext());

	protected static MApplication app = MApplicationFactory.eINSTANCE
			.createApplication();

	protected static Display display = Display.getCurrent() != null ? Display
			.getCurrent() : new Display();

	// 'transient' fields: re-created for each test
	protected IPresentationEngine renderer;
	protected IEclipseContext appContext;
	protected Widget topWidget;

	protected long startTime;

	/**
	 * Test cases derived from this class are used to test the Model <-> (SWT)
	 * UI relationships. These tests will ensure that the correct controls are
	 * created and that the various listeners (data bindings...) behave
	 * correctly.
	 */
	public RenderingTestCase() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		System.out.println("Setup");
		startTime = System.currentTimeMillis();
		super.setUp();

		// Create a fresh org.eclipse.e4.ui.model.application context for each
		// test
		IEclipseContext serviceContext = EclipseContextFactory
				.getServiceContext(Activator.getDefault().getBundle()
						.getBundleContext());

		appContext = E4Workbench.createWorkbenchContext(serviceContext,
				RegistryFactory.getRegistry(), null, null);
		MApplication app = MApplicationFactory.eINSTANCE.createApplication();

		appContext.set(MApplication.class.getName(), app);
		appContext.set(IContributionFactory.class.getName(),
				contributionFactory);
		appContext.set(IEclipseContext.class.getName(), appContext);

		app.setContext(appContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		System.out.print("tearDown");
		super.tearDown();

		// dispose the current context
		appContext = null;
		renderer = null;

		if (topWidget != null) {
			if (!topWidget.isDisposed())
				topWidget.dispose();

			// TBD: Should check the model to make sure it's been cleaned
			// here...
			topWidget = null;
		}
		System.out.println("  time: "
				+ (System.currentTimeMillis() - startTime) + "ms");
	}

	protected void processEventLoop() {
		if (display != null) {
			while (display.readAndDispatch())
				;
		}
	}

	protected Widget createModel(final MWindow window) {
		MApplication application = (MApplication) appContext
				.get(MApplication.class.getName());
		application.getChildren().add(window);

		final Widget[] renderedObject = new Widget[1];
		renderedObject[0] = null;
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				// Capture the expected 'invariant' results
				// SWTResult expectedResults = createSWTResultTree(modelRoot);

				renderer = (PartRenderingEngine) contributionFactory.create(
						PartRenderingEngine.engineURI, appContext);
				Object o = renderer.createGui(window);
				assertTrue("No widget rendered for: " + window.toString(),
						o != null);
				assertTrue("Rendered object is not a Widget: "
						+ o.getClass().getName(), o instanceof Widget);

				renderedObject[0] = (Widget) o;

				// Test the invariants
				// checkResults(expectedResults, renderedObject[0]);
			}
		});
		return renderedObject[0];
	}

	public void checkResults(SWTResult expected, Widget renderedObject) {
		// assertNotNull("The Control is Null " + expected.clazz.getName(),
		// renderedObject);
		// boolean classOK = (expected.clazz == renderedObject.getClass());
		// assertTrue("Class mismatch; expected: " + expected.clazz.getName()
		// + " actual: " + renderedObject.getClass().getName(), classOK);
		// if (expected.text != null) {
		// if (renderedObject instanceof Shell) {
		// String shellText = ((Shell) renderedObject).getText();
		// assertTrue("Text mismatch; expected: " + expected.text
		// + " actual: " + shellText, expected.text
		// .equals(shellText));
		// }
		// }
		// if (!expected.kids.isEmpty()) {
		// if (renderedObject instanceof Composite) {
		// Control[] controlKids = ((Composite) renderedObject)
		// .getChildren();
		// // Special check to remove the 'Sash' elements from a SashForm
		// if (renderedObject instanceof SashForm) {
		// List<Control> nonSashes = new ArrayList<Control>();
		// for (int i = 0; i < controlKids.length; i++) {
		// if (controlKids[i] instanceof Sash)
		// continue;
		// nonSashes.add(controlKids[i]);
		// }
		// controlKids = new Control[nonSashes.size()];
		// int count = 0;
		// for (Iterator<Control> iterator = nonSashes.iterator(); iterator
		// .hasNext();) {
		// Control ctrl = iterator.next();
		// controlKids[count++] = ctrl;
		// }
		// // controlKids = (Control[]) nonSashes.toArray();
		// }
		//
		// assertTrue("Child count mismatch; expected: "
		// + expected.kids.size() + "actual: "
		// + controlKids.length,
		// expected.kids.size() == controlKids.length);
		// Iterator kidIter = expected.kids.iterator();
		// for (Control kid : controlKids) {
		// SWTResult currKid = (SWTResult) kidIter.next();
		// checkResults(currKid, kid);
		// }
		// }
		// }
	}

	// public static SWTResult createSWTResultTree(MPart element) {
	// if (!element.isVisible())
	// return null;
	//
	// // Determine the expected control type based on the model's type
	// // NOTE: would be nice to get this from the various factories
	// Class elementClass = element.getClass();
	// Class expectedClass = null;
	// if (elementClass == WorkbenchWindowImpl.class) {
	// expectedClass = Shell.class;
	// } else if (elementClass == PartSashContainerImpl.class) {
	// expectedClass = SashForm.class;
	// } else if (elementClass == PartStackImpl.class) {
	// expectedClass = CTabFolder.class;
	// } else if (elementClass == PartImpl.class) {
	// expectedClass = Composite.class;
	// }
	//
	// // Capture the name
	// String theName = null;
	// if (element instanceof MItem) {
	// theName = ((MItem) element).getName();
	// }
	//
	// // Create a result entry for each child element
	// EList<?> list = element.getChildren();
	// SWTResult[] childList = null;
	// if (!list.isEmpty()) {
	// childList = new SWTResult[list.size()];
	// for (int i = 0; i < list.size(); i++) {
	// childList[i] = createSWTResultTree((MPart) list.get(i));
	// }
	// }
	// return new SWTResult(expectedClass, theName, childList);
	// }

}
