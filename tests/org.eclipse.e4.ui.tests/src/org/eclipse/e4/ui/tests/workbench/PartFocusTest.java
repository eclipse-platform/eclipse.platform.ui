/*******************************************************************************
 * Copyright (c) 2010, 2023 IBM Corporation and others.
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

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUILabel;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * Ensure that setting focus to a widget within an non-active part causes the
 * part to be activated while not changing the focus.
 */
@Ignore("See bug 505678")
public class PartFocusTest {

	protected IEclipseContext appContext;
	protected E4Workbench wb;

	protected EPartService eps;
	protected MWindow window;

	protected MPart part;
	protected MToolControl toolControl;

	protected MPart otherPart;
	private EModelService ems;

	@Before
	public void setUp() throws Exception {
		appContext = E4Application.createDefaultContext();
		appContext.set(IWorkbench.PRESENTATION_URI_ARG,
				PartRenderingEngine.engineURI);

		ems = appContext.get(EModelService.class);

		window = ems.createModelElement(MWindow.class);
		window.setWidth(500);
		window.setHeight(500);

		MPartSashContainer sash = ems.createModelElement(MPartSashContainer.class);
		window.getChildren().add(sash);
		window.setSelectedElement(sash);

		MPartStack stack = ems.createModelElement(MPartStack.class);
		sash.getChildren().add(stack);
		sash.setSelectedElement(stack);

		part = ems.createModelElement(MPart.class);
		part.setElementId("Part");
		part.setLabel("Part");
		part.setToolbar(ems.createModelElement(MToolBar.class));
		part.setContributionURI(asURI(PartBackend.class));
		stack.getChildren().add(part);

		toolControl = ems.createModelElement(MToolControl.class);
		toolControl.setElementId("ToolControl");
		toolControl.setContributionURI(asURI(TextField.class));
		part.getToolbar().getChildren().add(toolControl);

		stack = ems.createModelElement(MPartStack.class);
		sash.getChildren().add(stack);
		sash.setSelectedElement(stack);

		otherPart = ems.createModelElement(MPart.class);
		otherPart.setElementId("OtherPart");
		otherPart.setLabel("OtherPart");
		otherPart.setContributionURI(asURI(PartBackend.class));
		stack.getChildren().add(otherPart);

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		eps = window.getContext().get(EPartService.class);
		// ensure the parts are populated and the contributions instantiated
		eps.activate(part);
		eps.activate(otherPart);
		processEvents();

		// ensure our model backend objects are created
		assertNotNull(part.getObject());
		assertNotNull(toolControl.getObject());
		assertNotNull(otherPart.getObject());

		assertNotNull(part.getWidget());
		assertNotNull(toolControl.getWidget());
		assertNotNull(otherPart.getWidget());

		// ensure focus is set to otherPart.text1
		eps.activate(otherPart);
		processEvents();
		assertTrue(((PartBackend) otherPart.getObject()).text1.isFocusControl());
	}

	@After
	public void tearDown() throws Exception {
		if (wb != null) {
			wb.close();
		}
		appContext.dispose();
	}

	private void processEvents() {
		// renderer.run(window, appContext);
		Display display = Display.getCurrent();
		if (display.isDisposed()) {
			return;
		}
		try {
			while (display.readAndDispatch()) {
				appContext.processWaiting();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testFocusChangesOnExplicitPartActivation() {
		assertFalse(((PartBackend) part.getObject()).text1.isFocusControl());
		eps.activate(part);
		processEvents();
		assertTrue(((PartBackend) part.getObject()).text1.isFocusControl());
	}

	@Ignore
	@Test
	public void XXXtestNoFocusChangeOnExplicitWidgetSelection() {
		assertFalse(((PartBackend) part.getObject()).text1.isFocusControl());
		((TextField) toolControl.getObject()).text.setFocus();
		processEvents();
		assertEquals(part.getElementId(), eps.getActivePart().getElementId());
		assertFalse(((PartBackend) part.getObject()).text1.isFocusControl());
		assertTrue(((TextField) toolControl.getObject()).text.isFocusControl());
	}

	@Test
	public void testNoActivationOnExplicitInPartWidgetSelection() {
		assertEquals(otherPart, eps.getActivePart());
		assertTrue(((PartBackend) otherPart.getObject()).text1.isFocusControl());

		final boolean[] changed = new boolean[] { false };
		eps.addPartListener(new IPartListener() {
			@Override
			public void partVisible(MPart part) {
				changed[0] = true;
			}

			@Override
			public void partHidden(MPart part) {
				changed[0] = true;
			}

			@Override
			public void partDeactivated(MPart part) {
				changed[0] = true;
			}

			@Override
			public void partBroughtToTop(MPart part) {
				changed[0] = true;
			}

			@Override
			public void partActivated(MPart part) {
				changed[0] = true;
			}
		});

		((PartBackend) otherPart.getObject()).text2.setFocus();
		processEvents();
		assertTrue(((PartBackend) otherPart.getObject()).text2.isFocusControl());
		assertFalse(changed[0]);

	}

	/**
	 * Generate a platform URI referencing the provided class.
	 *
	 * @param clazz
	 *            the class to be referenced
	 * @return the platform-based URI: bundleclass://X/X.Y
	 */
	private static String asURI(Class<?> clazz) {
		return "bundleclass://" + FrameworkUtil.getBundle(clazz).getSymbolicName() + '/' + clazz.getName();
	}

}

/**
 * A simple part backend that creates two text fields. On a focus request, set
 * the focus to the first text field.
 */
class PartBackend {
	@Inject
	public Composite parent;
	@Inject
	MPart part;

	public Text text1;
	public Text text2;

	public PartBackend() {
	}

	@PostConstruct
	public void init() {
		text1 = new Text(parent, SWT.SINGLE);
		text2 = new Text(parent, SWT.SINGLE);
		text1.setText(part.getLabel() + " text1");
		text1.setText(part.getLabel() + " text2");
	}

	@Focus
	public void setFocus() {
		// can test if called by checking if text1.isFocusControl()
		// System.out.println(part.getLabel() + ": setFocus to text1");
		text1.setFocus();
	}

	@PreDestroy
	public void dispose() {
		// System.out.println(part.getLabel() + ": destroyed");
	}

}

/**
 * A simple text field for a tool control.
 */
class TextField {
	@Inject
	Composite parent;
	@Inject
	MUILabel element;

	public Text text;

	public TextField() {
	}

	@PostConstruct
	public void init() {
		// System.out.println("TextField created");
		text = new Text(parent, SWT.SINGLE);
		text.setText(element.getLabel() + " text");
	}

	@PreDestroy
	public void dispose() {
		// System.out.println("TextField destroyed");
	}
}
