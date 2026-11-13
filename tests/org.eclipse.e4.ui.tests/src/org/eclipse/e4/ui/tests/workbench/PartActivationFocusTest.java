/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.e4.ui.tests.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.contexts.RunAndTrack;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.tests.harness.util.DisplayHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * A focus change that lands in another part while a part is being activated
 * must not start a competing activation.
 */
public class PartActivationFocusTest {

	private IEclipseContext appContext;
	private E4Workbench wb;
	private EPartService partService;
	private MWindow window;
	private MPartStack stack;
	private MPart partA;
	private MPart partB;
	private MPart partC;
	private MPart lazyPart;
	private final List<String> activated = new ArrayList<>();

	@BeforeEach
	public void setUp() {
		appContext = E4Application.createDefaultContext();
		appContext.set(IWorkbench.PRESENTATION_URI_ARG, PartRenderingEngine.engineURI);
		EModelService ems = appContext.get(EModelService.class);

		window = ems.createModelElement(MWindow.class);
		window.setWidth(600);
		window.setHeight(400);
		MPartSashContainer sash = ems.createModelElement(MPartSashContainer.class);
		sash.setHorizontal(true);
		window.getChildren().add(sash);
		window.setSelectedElement(sash);
		stack = ems.createModelElement(MPartStack.class);
		MPartStack otherStack = ems.createModelElement(MPartStack.class);
		sash.getChildren().add(stack);
		sash.getChildren().add(otherStack);
		sash.setSelectedElement(stack);
		partA = createPart(ems, stack, "A", TextPart.class);
		partB = createPart(ems, stack, "B", TextPart.class);
		lazyPart = createPart(ems, stack, "lazy", RefocusingPart.class);
		partC = createPart(ems, otherStack, "C", TextPart.class);

		MApplication application = ems.createModelElement(MApplication.class);
		application.getChildren().add(window);
		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);
		partService = window.getContext().get(EPartService.class);
		partService.activate(partA);
		partService.activate(partC);
		partService.activate(partB);
		processEvents();

		Shell shell = (Shell) window.getWidget();
		shell.forceActive();
		DisplayHelper.waitForCondition(shell.getDisplay(), 5000, () -> text(partB).isFocusControl());
		// without focus events no competing activation can happen
		assumeTrue(text(partB).isFocusControl(), "focus events are not delivered");

		partService.addPartListener(new IPartListener() {
			@Override
			public void partActivated(MPart part) {
				activated.add(part.getElementId());
			}

			@Override
			public void partBroughtToTop(MPart part) {
			}

			@Override
			public void partDeactivated(MPart part) {
			}

			@Override
			public void partHidden(MPart part) {
			}

			@Override
			public void partVisible(MPart part) {
			}
		});
	}

	@AfterEach
	public void tearDown() {
		wb.close();
		appContext.dispose();
	}

	@Test
	public void testPartCreationRefocusingPreviousPart() {
		partService.activate(lazyPart);
		processEvents();

		assertEquals(List.of("lazy"), activated);
		assertEquals(lazyPart, partService.getActivePart());
		assertEquals(lazyPart, window.getContext().get(IServiceConstants.ACTIVE_PART));
		assertEquals(lazyPart, stack.getSelectedElement());
		assertTrue(((RefocusingPart) lazyPart.getObject()).text.isFocusControl());
	}

	@Test
	public void testFocusStolenDuringContextActivation() {
		stealFocusToPartCWhenPartABecomesActive();

		partService.activate(partA);
		processEvents();

		assertEquals(List.of("A"), activated);
		assertEquals(partA, partService.getActivePart());
		assertEquals(partA, window.getContext().get(IServiceConstants.ACTIVE_PART));
		assertTrue(text(partA).isFocusControl());
	}

	@Test
	public void testFocusStolenDuringContextActivationWithoutFocusRequest() {
		stealFocusToPartCWhenPartABecomesActive();

		partService.activate(partA, false);
		processEvents();

		// the activation does not touch the focus, so the part owning the focus is active
		assertEquals(partC, partService.getActivePart());
		assertEquals(partC, window.getContext().get(IServiceConstants.ACTIVE_PART));
		assertTrue(text(partC).isFocusControl());
	}

	private void stealFocusToPartCWhenPartABecomesActive() {
		boolean[] armed = { true };
		window.getContext().runAndTrack(new RunAndTrack() {
			@Override
			public boolean changed(IEclipseContext context) {
				if (armed[0] && context.get(IServiceConstants.ACTIVE_PART) == partA) {
					armed[0] = false;
					text(partC).setFocus();
				}
				return armed[0];
			}
		});
	}

	private MPart createPart(EModelService ems, MPartStack parent, String id, Class<?> contribution) {
		MPart part = ems.createModelElement(MPart.class);
		part.setElementId(id);
		part.setLabel(id);
		part.setContributionURI(
				"bundleclass://" + FrameworkUtil.getBundle(getClass()).getSymbolicName() + '/' + contribution.getName());
		parent.getChildren().add(part);
		return part;
	}

	private static Text text(MPart part) {
		return ((TextPart) part.getObject()).text;
	}

	private void processEvents() {
		Display display = Display.getCurrent();
		while (display.readAndDispatch()) {
			appContext.processWaiting();
		}
	}

	public static class TextPart {
		@Inject
		Composite parent;
		Text text;

		@PostConstruct
		void create() {
			text = new Text(parent, SWT.SINGLE);
		}

		@Focus
		void focus() {
			text.setFocus();
		}
	}

	/**
	 * Like an editor initialized through WorkbenchWindow.run: disables the
	 * window's controls while working and then gives the focus back to the
	 * control that had it, which belongs to the previously active part.
	 */
	public static class RefocusingPart extends TextPart {
		@Override
		@PostConstruct
		void create() {
			super.create();
			Shell shell = parent.getShell();
			Control previousFocus = shell.getDisplay().getFocusControl();
			List<Control> disabled = new ArrayList<>();
			for (Control child : shell.getChildren()) {
				if (child.isEnabled()) {
					child.setEnabled(false);
					disabled.add(child);
				}
			}
			disabled.forEach(child -> child.setEnabled(true));
			if (previousFocus != null) {
				previousFocus.forceFocus();
			}
		}
	}
}
