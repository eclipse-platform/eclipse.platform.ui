package org.eclipse.e4.ui.workbench.addons.minmax;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

public class MaximizableChildrenTag {

	private static final String MAXIMIZEABLE_CHILDREN = IPresentationEngine.MIN_MAXIMIZEABLE_CHILDREN_AREA_TAG;
	private Shell shell;
	private MPartStack partStack1;
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
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		window = BasicFactoryImpl.eINSTANCE
				.createTrimmedWindow();
		window.setElementId("MainWindow");
		MPerspectiveStack perspectiveStackMain = AdvancedFactoryImpl.eINSTANCE
				.createPerspectiveStack();
		perspectiveStackMain.setElementId("perspectiveStackMain");
		MPerspective perspectiveMain = AdvancedFactoryImpl.eINSTANCE
				.createPerspective();
		perspectiveMain.setElementId("perspectiveMain");

		MPartSashContainer containerMain = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();

		partStackMain = BasicFactoryImpl.eINSTANCE.createPartStack();
		partStackMain.setElementId("mainPartStack");
		MPart partMain = BasicFactoryImpl.eINSTANCE.createPart();
		partStackMain.getChildren().add(partMain);

		containerMain.getChildren().add(partStackMain);

		placeholderMain = AdvancedFactoryImpl.eINSTANCE.createPlaceholder();
//		placeholderMain.setElementId("placeholderMain");
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

		MArea mArea = AdvancedFactoryImpl.eINSTANCE.createArea();
		mArea.getTags().add(MAXIMIZEABLE_CHILDREN);

		partStack1 = BasicFactoryImpl.eINSTANCE
				.createPartStack();
		MPart part1 = BasicFactoryImpl.eINSTANCE.createPart();
		partStack1.getChildren().add(part1);
		mArea.getChildren().add(partStack1);

		partStack2 = BasicFactoryImpl.eINSTANCE
				.createPartStack();
		MPart part2 = BasicFactoryImpl.eINSTANCE.createPart();
		partStack2.getChildren().add(part2);
		mArea.getChildren().add(partStack2);

		window.getSharedElements().add(mArea);
		placeholderMain.setRef(mArea);
		mArea.setCurSharedRef(placeholderMain);

		// instantiate addon
		MAddon minMaxAddon = ApplicationFactoryImpl.eINSTANCE.createAddon();
		minMaxAddon.setElementId("MinMaxAddon"); //$NON-NLS-1$
		minMaxAddon
				.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.MinMaxAddon"); //$NON-NLS-1$

		appContext = E4Application.createDefaultContext();
		appContext.set(Display.class, Display.getDefault());
		appContext.set(MApplication.class.getName(), application);
		appContext.set(MWindow.class, window);
		appContext.set(UISynchronize.class, new UISynchronize() {

			@Override
			public void syncExec(Runnable runnable) {
				runnable.run();
			}

			@Override
			public void asyncExec(Runnable runnable) {
				runnable.run();
			}
		});
		appContext.set(EModelService.class, new ModelServiceImpl(appContext));

		ContextInjectionFactory.setDefault(appContext);
		renderer = ContextInjectionFactory.make(
				PartRenderingEngine.class, appContext);

		appContext.set(IPresentationEngine.class, renderer);
		appContext
				.set(EPartService.class, ContextInjectionFactory.make(
						PartServiceImpl.class, appContext));
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
					return ImageDescriptor.createFromURL(new URL(iconPath
							.toString()));
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
	public void testMainPartStackMax() {
		partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStack1.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStack1.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(partStack2.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStack2.getTags().contains(
				IPresentationEngine.MAXIMIZED));
	}

	@Test
	public void testAreaMax() {
		placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStack1.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStack1.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(partStack2.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStack2.getTags().contains(
				IPresentationEngine.MAXIMIZED));
	}

	@Test
	public void testPartStack1Max() {
		partStack1.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertTrue(partStack2.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStack2.getTags().contains(
				IPresentationEngine.MAXIMIZED));
	}

	@Test
	public void testPartStack2Max() {
		partStack2.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertTrue(partStack1.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStack1.getTags().contains(
				IPresentationEngine.MAXIMIZED));
	}
}
