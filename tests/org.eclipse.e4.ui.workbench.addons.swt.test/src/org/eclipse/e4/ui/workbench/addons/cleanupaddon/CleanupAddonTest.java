/*******************************************************************************
 * Copyright (c) 2024 Vector Informatik GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.cleanupaddon;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.function.Supplier;

import org.eclipse.core.runtime.Platform.OS;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.internal.workbench.ModelServiceImpl;
import org.eclipse.e4.ui.internal.workbench.PartServiceImpl;
import org.eclipse.e4.ui.internal.workbench.PartStackUtil;
import org.eclipse.e4.ui.internal.workbench.UIEventPublisher;
import org.eclipse.e4.ui.internal.workbench.swt.CSSRenderingUtils;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.swt.DisplayUISynchronize;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CleanupAddonTest {
	private static final Duration TIMEOUT = Duration.ofSeconds(30);

	private Shell shell;
	private MArea editorArea;
	private MPartStack partStack1;
	private MPart partInPartStack1;
	private MPartStack partStack2;
	private MPartStack partStackMain;
	private MPlaceholder placeholderMain;
	private IPresentationEngine renderer;
	private MTrimmedWindow window;
	private IEclipseContext appContext;

	@Before
	public void before() {
		prepareApplicationModel();
	}

	@After
	public void tearDown() {
		renderer.removeGui(window);
		renderer.stop();
		shell.dispose();
		appContext.dispose();
	}

	private void prepareApplicationModel() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE.createApplication();

		window = BasicFactoryImpl.eINSTANCE.createTrimmedWindow();
		window.setElementId("MainWindow");
		MPerspectiveStack perspectiveStackMain = AdvancedFactoryImpl.eINSTANCE.createPerspectiveStack();
		perspectiveStackMain.setElementId("perspectiveStackMain");
		MPerspective perspectiveMain = AdvancedFactoryImpl.eINSTANCE.createPerspective();
		perspectiveMain.setElementId("perspectiveMain");

		MPartSashContainer containerMain = BasicFactoryImpl.eINSTANCE.createPartSashContainer();

		partStackMain = BasicFactoryImpl.eINSTANCE.createPartStack();
		partStackMain.setElementId("mainPartStack");
		MPart partMain = BasicFactoryImpl.eINSTANCE.createPart();
		partStackMain.getChildren().add(partMain);

		containerMain.getChildren().add(partStackMain);

		placeholderMain = AdvancedFactoryImpl.eINSTANCE.createPlaceholder();
		placeholderMain.setElementId("org.eclipse.ui.editorss");

		containerMain.getChildren().add(placeholderMain);

		perspectiveMain.getChildren().add(containerMain);
		perspectiveMain.setSelectedElement(containerMain);
		perspectiveStackMain.getChildren().add(perspectiveMain);
		perspectiveStackMain.setSelectedElement(perspectiveMain);
		window.getChildren().add(perspectiveStackMain);
		window.setSelectedElement(perspectiveStackMain);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		editorArea = AdvancedFactoryImpl.eINSTANCE.createArea();

		partStack1 = BasicFactoryImpl.eINSTANCE.createPartStack();
		partInPartStack1 = BasicFactoryImpl.eINSTANCE.createPart();
		partStack1.getChildren().add(partInPartStack1);
		editorArea.getChildren().add(partStack1);

		partStack2 = BasicFactoryImpl.eINSTANCE.createPartStack();
		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		partStack2.getChildren().add(part2);
		editorArea.getChildren().add(partStack2);

		window.getSharedElements().add(editorArea);
		placeholderMain.setRef(editorArea);
		editorArea.setCurSharedRef(placeholderMain);

		// instantiate addon
		MAddon cleanupAddon = ApplicationFactoryImpl.eINSTANCE.createAddon();
		cleanupAddon.setElementId("CleanupAddon"); //$NON-NLS-1$
		cleanupAddon.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.cleanupaddon.CleanupAddon"); //$NON-NLS-1$

		appContext = E4Application.createDefaultContext();
		Display display = Display.getDefault();
		appContext.set(Display.class, display);
		appContext.set(MApplication.class.getName(), application);
		appContext.set(MWindow.class, window);
		appContext.set(UISynchronize.class, new DisplayUISynchronize(display));
		appContext.set(EModelService.class, new ModelServiceImpl(appContext));

		ContextInjectionFactory.setDefault(appContext);
		renderer = ContextInjectionFactory.make(PartRenderingEngine.class, appContext);

		appContext.set(IPresentationEngine.class, renderer);
		appContext.set(EPartService.class, ContextInjectionFactory.make(PartServiceImpl.class, appContext));
		application.setContext(appContext);

		final UIEventPublisher ep = new UIEventPublisher(appContext);
		((Notifier) application).eAdapters().add(ep);
		appContext.set(UIEventPublisher.class, ep);
		appContext.set(MAddon.class, cleanupAddon);

		ContextInjectionFactory.setDefault(appContext);
		ContextInjectionFactory.make(CleanupAddon.class, appContext);

		appContext.set(IResourceUtilities.class, new ISWTResourceUtilities() {

			@Override
			public ImageDescriptor imageDescriptorFromURI(URI iconPath) {
				try {
					return ImageDescriptor.createFromURL(new URL(iconPath.toString()));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			public Image adornImage(Image toAdorn, Image adornment) {
				return null;
			}
		});
		appContext.set(CSSRenderingUtils.class, new CSSRenderingUtils());
		E4Application.initializeServices(application);

		shell = (Shell) renderer.createGui(window);
	}

	@Test
	public void testRemovingPrimaryDataStackTransfersToRemainingStack() {
		assumeFalse(
				"not working reliably on Mac, see https://github.com/eclipse-platform/eclipse.platform.ui/issues/1784",
				OS.isMac());

		PartStackUtil.initializeAsPrimaryDataStack(partStack1);
		assertTrue(PartStackUtil.isPrimaryDataStack(partStack1));
		assertFalse(PartStackUtil.isPrimaryDataStack(partStack2));

		closeAllPartsOfPartStack1();
		waitForCondition(() -> !partStack1.isToBeRendered(), TIMEOUT);

		assertFalse(partStack1.isToBeRendered());
		assertTrue(PartStackUtil.isPrimaryDataStack(partStack2));
		assertTrue(partStack2.isToBeRendered());
	}

	private void closeAllPartsOfPartStack1() {
		IEclipseContext partContext = partInPartStack1.getContext();
		EPartService partService = partContext.get(EPartService.class);
		partService.hidePart(partInPartStack1);
	}

	private static void waitForCondition(Supplier<Boolean> condition, Duration timeout) {
		Duration timeoutTime = Duration.ofMillis(System.currentTimeMillis()).plus(timeout);
		while (!condition.get() && !timeoutTime.minusMillis(System.currentTimeMillis()).isNegative()) {
			Display.getCurrent().readAndDispatch();
		}
	}

}
