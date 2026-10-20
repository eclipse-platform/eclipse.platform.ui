/*******************************************************************************
 * Copyright (c) 2026 Aleksandar Kurtakov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.minmax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.internal.workbench.ModelServiceImpl;
import org.eclipse.e4.ui.internal.workbench.PartServiceImpl;
import org.eclipse.e4.ui.internal.workbench.UIEventPublisher;
import org.eclipse.e4.ui.internal.workbench.swt.CSSRenderingUtils;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.swt.DisplayUISynchronize;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TrimStackTest {

	private IEclipseContext appContext;
	private IPresentationEngine renderer;
	private EModelService modelService;
	private MTrimmedWindow window;
	private MPartStack minimizedStack;
	private MPart part;
	private Shell shell;

	@BeforeEach
	public void setUp() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE.createApplication();

		window = BasicFactoryImpl.eINSTANCE.createTrimmedWindow();
		window.setElementId("MainWindow");
		MPartSashContainer container = BasicFactoryImpl.eINSTANCE.createPartSashContainer();
		minimizedStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		minimizedStack.setElementId("minimizedStack");
		part = BasicFactoryImpl.eINSTANCE.createPart();
		part.setElementId("part");
		MPartStack otherStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		otherStack.setElementId("otherStack");
		MPart otherPart = BasicFactoryImpl.eINSTANCE.createPart();

		application.getChildren().add(window);
		application.setSelectedElement(window);
		window.getChildren().add(container);
		window.setSelectedElement(container);
		container.getChildren().add(minimizedStack);
		container.getChildren().add(otherStack);
		minimizedStack.getChildren().add(part);
		minimizedStack.setSelectedElement(part);
		otherStack.getChildren().add(otherPart);
		otherStack.setSelectedElement(otherPart);

		MAddon minMaxAddon = ApplicationFactoryImpl.eINSTANCE.createAddon();
		minMaxAddon.setElementId("MinMaxAddon"); //$NON-NLS-1$
		minMaxAddon.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.MinMaxAddon"); //$NON-NLS-1$

		appContext = E4Application.createDefaultContext();
		appContext.set(Display.class, Display.getDefault());
		appContext.set(MApplication.class.getName(), application);
		appContext.set(MWindow.class, window);
		appContext.set(UISynchronize.class, new DisplayUISynchronize(Display.getDefault()));
		modelService = new ModelServiceImpl(appContext);
		appContext.set(EModelService.class, modelService);

		ContextInjectionFactory.setDefault(appContext);
		renderer = ContextInjectionFactory.make(PartRenderingEngine.class, appContext);

		appContext.set(IPresentationEngine.class, renderer);
		appContext.set(EPartService.class, ContextInjectionFactory.make(PartServiceImpl.class, appContext));
		application.setContext(appContext);

		final UIEventPublisher ep = new UIEventPublisher(appContext);
		((Notifier) application).eAdapters().add(ep);
		appContext.set(UIEventPublisher.class, ep);

		appContext.set(MAddon.class, minMaxAddon);

		ContextInjectionFactory.setDefault(appContext);
		ContextInjectionFactory.make(MinMaxAddon.class, appContext);

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
		shell.setSize(800, 600);
		shell.open();
		processEvents();
	}

	@AfterEach
	public void tearDown() {
		renderer.removeGui(window);
		renderer.stop();
		shell.dispose();
		appContext.dispose();
	}

	/**
	 * The view of a minimized stack shown from the trim fills its CTabFolder,
	 * also when the CTabFolder keeps its bounds while the view was laid out
	 * elsewhere, e.g. in another perspective (bug 434062).
	 */
	@Test
	public void testShownStackLaysOutHostedView() {
		minimizedStack.getTags().add(IPresentationEngine.MINIMIZED);
		processEvents();
		MToolControl toolControl = (MToolControl) modelService
				.find(TrimStackIdHelper.createTrimStackId(minimizedStack, null, window), window);
		assertNotNull(toolControl, "No trim stack for the minimized stack");
		TrimStack trimStack = (TrimStack) toolControl.getObject();
		CTabFolder ctf = (CTabFolder) minimizedStack.getWidget();
		Control partControl = (Control) part.getWidget();

		trimStack.showStack(true);
		processEvents();
		assertEquals(ctf.getClientArea(), partControl.getBounds(), "Part does not fill the shown stack");

		trimStack.showStack(false);
		processEvents();
		// as if the part had been laid out in a stack of another size
		partControl.setSize(10, 10);
		trimStack.showStack(true);
		processEvents();
		assertEquals(ctf.getClientArea(), partControl.getBounds(), "Part does not fill the stack shown again");
		trimStack.showStack(false);
	}

	private void processEvents() {
		while (Display.getDefault().readAndDispatch()) {
			// spin
		}
	}
}
