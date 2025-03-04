package org.eclipse.e4.ui.workbench.addons.minmax;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

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
import org.eclipse.e4.ui.model.application.ui.MUIElement;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class MaximizeBugTest {

	private static final int USE_CORRECT_PLACEHOLDER_ID = 0;
	private static final int USE_PERSPECTIVE_IN_SUBWINDOW = 1;
	private static final int MAXIMIZE_MAIN_FIRST = 2;
	private static final int ADD_SUBWINDOW_TO_PERSPECTIVE = 3;

	private MPartStack partStackMain;
	private MPlaceholder placeholderMain;

	private MPartStack partStackSub;
	private MPlaceholder placeholderSub;
	private Shell shell;
	private MTrimmedWindow windowSub;
	private IEclipseContext appContext;
	private IPresentationEngine renderer;
	private MTrimmedWindow window;

	@AfterEach
	public void tearDown() {
		renderer.removeGui(window);
		renderer.stop();
		shell.dispose();
		appContext.dispose();
	}

	private void prepareApplicationModel(boolean useCorrectPlaceholderId, boolean usePerspektiveInSubWindow,
			boolean addSubwindowToPerspective) {

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

		MArea areaMain = AdvancedFactoryImpl.eINSTANCE.createArea();
		MPartStack partStackAreaMain = BasicFactoryImpl.eINSTANCE
				.createPartStack();
		MPart partAreaMain = BasicFactoryImpl.eINSTANCE.createPart();

		placeholderMain = AdvancedFactoryImpl.eINSTANCE.createPlaceholder();
		placeholderMain.setElementId("placeholderMain");
		application.getChildren().add(window);
		application.setSelectedElement(window);

		window.getChildren().add(perspectiveStackMain);
		window.setSelectedElement(perspectiveStackMain);

		perspectiveStackMain.getChildren().add(perspectiveMain);
		perspectiveStackMain.setSelectedElement(perspectiveMain);

		perspectiveMain.getChildren().add(containerMain);
		perspectiveMain.setSelectedElement(containerMain);

		containerMain.getChildren().add(partStackMain);
		containerMain.getChildren().add(placeholderMain);

		partStackMain.getChildren().add(partMain);

		areaMain.getChildren().add(partStackAreaMain);
		partStackAreaMain.getChildren().add(partAreaMain);

		placeholderMain.setRef(areaMain);

		window.getSharedElements().add(areaMain);

		windowSub = BasicFactoryImpl.eINSTANCE.createTrimmedWindow();
		windowSub.setElementId("SubWindow");
		MPartSashContainer containerSub = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		partStackSub = BasicFactoryImpl.eINSTANCE.createPartStack();
		partStackSub.setElementId("partStackSub");
		MPart partSub = BasicFactoryImpl.eINSTANCE.createPart();

		placeholderSub = AdvancedFactoryImpl.eINSTANCE.createPlaceholder();
		placeholderSub.setElementId("placeholderSub");
		MArea areaSub = AdvancedFactoryImpl.eINSTANCE.createArea();
		MPartStack partStackAreaSub = BasicFactoryImpl.eINSTANCE
				.createPartStack();
		MPart partAreaSub = BasicFactoryImpl.eINSTANCE.createPart();
		placeholderSub.setRef(areaSub);
		if (usePerspektiveInSubWindow) {
			MPerspectiveStack perspectiveStackSub = AdvancedFactoryImpl.eINSTANCE
					.createPerspectiveStack();
			perspectiveStackSub.setElementId("perspectiveStackSub");
			MPerspective perspectiveSub = AdvancedFactoryImpl.eINSTANCE
					.createPerspective();
			perspectiveSub.setElementId("perspectiveSub");
			perspectiveStackSub.getChildren().add(perspectiveSub);
			perspectiveStackSub.setSelectedElement(perspectiveSub);

			perspectiveSub.getChildren().add(containerSub);
			perspectiveSub.setSelectedElement(containerSub);

			windowSub.getChildren().add(perspectiveStackSub);
			windowSub.setSelectedElement(perspectiveStackSub);
		} else {
			windowSub.getChildren().add(containerSub);
			windowSub.setSelectedElement(containerSub);
		}

		containerSub.getChildren().add(partStackSub);
		containerSub.getChildren().add(placeholderSub);

		partStackSub.getChildren().add(partSub);

		areaSub.getChildren().add(partStackAreaSub);
		partStackAreaSub.getChildren().add(partAreaSub);

		windowSub.getSharedElements().add(areaSub);
		if (addSubwindowToPerspective) {
			perspectiveMain.getWindows().add(windowSub);
		} else {
			window.getWindows().add(windowSub);
		}
		// set correct ids
		if (useCorrectPlaceholderId) {
			placeholderMain.setElementId("org.eclipse.ui.editorss");
			placeholderSub.setElementId("org.eclipse.ui.editorss");
		} else {
			placeholderMain.setElementId("placeholderMain");
			placeholderSub.setElementId("placeholderSub");
		}

		// instantiate addon
		MAddon minMaxAddon = ApplicationFactoryImpl.eINSTANCE.createAddon();
		minMaxAddon.setElementId("MinMaxAddon"); //$NON-NLS-1$
		minMaxAddon
				.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.MinMaxAddon"); //$NON-NLS-1$

		appContext = E4Application.createDefaultContext();
		appContext.set(Display.class, Display.getDefault());
		appContext.set(MApplication.class.getName(), application);
		appContext.set(MWindow.class, window);
		appContext.set(UISynchronize.class, new DisplayUISynchronize(Display.getDefault()));
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

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPlaceholderMax(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));
	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPartStackMax(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));
	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testSubPlaceholderMax(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));
	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testSubPartStackMax(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));
	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPlaceholderMaxThenUnzoom(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		placeholderMain.getTags().remove(IPresentationEngine.MAXIMIZED);

		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));
	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPartStackMaxThenUnzoom(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));
		partStackMain.getTags().remove(IPresentationEngine.MAXIMIZED);

		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));
	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testSubPlaceholderMaxThenUnzoom(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));
		placeholderSub.getTags().remove(IPresentationEngine.MAXIMIZED);

		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));
	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testSubPartStackMaxThenUnzoom(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));
		partStackSub.getTags().remove(IPresentationEngine.MAXIMIZED);

		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));
	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPlaceholderMaxSubPlaceholderMax(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		if (parameters[MAXIMIZE_MAIN_FIRST]) {
			placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);
			placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);
		} else {
			placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);
			placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);
		}

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPlaceholderMaxSubPartStackMax(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		if (parameters[MAXIMIZE_MAIN_FIRST]) {
			placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);
			partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);
		} else {
			partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);
			placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);
		}

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPartStackMaxSubPlaceholderMax(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		if (parameters[MAXIMIZE_MAIN_FIRST]) {
			partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);
			placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);
		} else {
			placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);
			partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);
		}

		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPartStackMaxSubPartStackMax(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		if (parameters[MAXIMIZE_MAIN_FIRST]) {
			partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);
			partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);
		} else {
			partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);
			partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);
		}

		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPlaceholderMaxSubPlaceholderMaxUnzoomMain(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		if (parameters[MAXIMIZE_MAIN_FIRST]) {
			placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);
			placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);
		} else {
			placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);
			placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);
		}

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		placeholderMain.getTags().remove(IPresentationEngine.MAXIMIZED);

		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));
		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPlaceholderMaxSubPartStackMaxUnzoomMain(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		if (parameters[MAXIMIZE_MAIN_FIRST]) {
			placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);
			partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);
		} else {
			partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);
			placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);
		}

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		placeholderMain.getTags().remove(IPresentationEngine.MAXIMIZED);

		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));
		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPartStackMaxSubPlaceholderMaxUnzoomMain(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		if (parameters[MAXIMIZE_MAIN_FIRST]) {
			partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);
			placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);
		} else {
			placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);
			partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);
		}

		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		partStackMain.getTags().remove(IPresentationEngine.MAXIMIZED);

		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));
		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPartStackMaxSubPartStackMaxUnzoomMain(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		if (parameters[MAXIMIZE_MAIN_FIRST]) {
			partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);
			partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);
		} else {
			partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);
			partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);
		}

		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		partStackMain.getTags().remove(IPresentationEngine.MAXIMIZED);

		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));
		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));
		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPlaceholderMaxSubPlaceholderMaxUnzoomSub(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		if (parameters[MAXIMIZE_MAIN_FIRST]) {
			placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);
			placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);
		} else {
			placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);
			placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);
		}

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		placeholderSub.getTags().remove(IPresentationEngine.MAXIMIZED);

		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));
		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPlaceholderMaxSubPartStackMaxUnzoomSub(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		if (parameters[MAXIMIZE_MAIN_FIRST]) {
			placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);
			partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);
		} else {
			partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);
			placeholderMain.getTags().add(IPresentationEngine.MAXIMIZED);
		}

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		partStackSub.getTags().remove(IPresentationEngine.MAXIMIZED);

		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));
		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPartStackMaxSubPlaceholderMaxUnzoomSub(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		if (parameters[MAXIMIZE_MAIN_FIRST]) {
			partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);
			placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);
		} else {
			placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);
			partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);
		}

		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		placeholderSub.getTags().remove(IPresentationEngine.MAXIMIZED);

		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(partStackSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));
		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testMainPartStackMaxSubPartStackMaxUnzoomSub(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		if (parameters[MAXIMIZE_MAIN_FIRST]) {
			partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);
			partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);
		} else {
			partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);
			partStackMain.getTags().add(IPresentationEngine.MAXIMIZED);
		}

		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		partStackSub.getTags().remove(IPresentationEngine.MAXIMIZED);

		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderSub.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		assertTrue(partStackMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));
		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertTrue(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MAXIMIZED));

		partStackMain.getTags().remove(IPresentationEngine.MAXIMIZED);

		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));
		assertFalse(placeholderMain.getTags().contains(
				IPresentationEngine.MINIMIZED));
	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testSubWindowWithSubWindowMaximizeStack(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		MUIElement[] subWindowElements = prepareSubWindow();

		partStackSub.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(placeholderSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		for (MUIElement element : subWindowElements) {
			assertFalse(element.getTags().contains(
					IPresentationEngine.MINIMIZED_BY_ZOOM));
			assertFalse(element.getTags().contains(
					IPresentationEngine.MAXIMIZED));
		}
	}

	@ParameterizedTest
	@MethodSource("createTestParameters")
	public void testSubWindowWithSubWindowMaximizePlaceholder(boolean[] parameters) {
		prepareApplicationModel(parameters[USE_CORRECT_PLACEHOLDER_ID], parameters[USE_PERSPECTIVE_IN_SUBWINDOW],
				parameters[ADD_SUBWINDOW_TO_PERSPECTIVE]);

		MUIElement[] subWindowElements = prepareSubWindow();

		placeholderSub.getTags().add(IPresentationEngine.MAXIMIZED);

		assertTrue(partStackSub.getTags().contains(
				IPresentationEngine.MINIMIZED_BY_ZOOM));

		for (MUIElement element : subWindowElements) {
			assertFalse(element.getTags().contains(
					IPresentationEngine.MINIMIZED_BY_ZOOM));
			assertFalse(element.getTags().contains(
					IPresentationEngine.MAXIMIZED));
		}
	}

	private MUIElement[] prepareSubWindow() {
		MWindow subSubWindow = BasicFactoryImpl.eINSTANCE.createTrimmedWindow();
		subSubWindow.setElementId("SubSubWindow");
		MPartSashContainer containerSubSub = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
		MPartStack partStackSubSub = BasicFactoryImpl.eINSTANCE
				.createPartStack();
		partStackSubSub.setElementId("partStackSubSub");
		MPart partSubSub = BasicFactoryImpl.eINSTANCE.createPart();

		MPlaceholder placeholderSubSub = AdvancedFactoryImpl.eINSTANCE
				.createPlaceholder();
		placeholderSubSub.setElementId("placeholderSubSub");
		MArea areaSubSub = AdvancedFactoryImpl.eINSTANCE.createArea();
		MPartStack partStackAreaSubSub = BasicFactoryImpl.eINSTANCE
				.createPartStack();
		MPart partAreaSubSub = BasicFactoryImpl.eINSTANCE.createPart();
		placeholderSubSub.setRef(areaSubSub);
		subSubWindow.getChildren().add(containerSubSub);
		subSubWindow.setSelectedElement(containerSubSub);

		containerSubSub.getChildren().add(partStackSubSub);
		containerSubSub.getChildren().add(placeholderSubSub);

		partStackSubSub.getChildren().add(partSubSub);

		areaSubSub.getChildren().add(partStackAreaSubSub);
		partStackAreaSubSub.getChildren().add(partAreaSubSub);

		subSubWindow.getSharedElements().add(areaSubSub);
		windowSub.getWindows().add(subSubWindow);
		return new MUIElement[] { partStackSubSub, placeholderSubSub };
	}

	/**
	 * All parameterized test methods receive their parameters from this static
	 * method.<br />
	 * <br />
	 * The {@code boolean} arrays returned consist of
	 * <li>use correct placeholder id for editor</li>
	 * <li>use sub-window perspective</li>
	 * <li>maximize main first</li>
	 * <li>add sub-window to perspective</li>
	 *
	 * @return a stream of boolean[]
	 */
	static Stream<boolean[]> createTestParameters() {
		return Stream.of( //
				new boolean[] { true, true, true, true }, //
				new boolean[] { true, true, false, true }, //
				new boolean[] { true, false, true, true }, //
				new boolean[] { true, false, false, true }, //
				new boolean[] { true, true, true, false }, //
				new boolean[] { true, true, false, false }, //
				new boolean[] { true, false, true, false }, //
				new boolean[] { true, false, false, false } //
				// Extremely evil
				// new boolean[] { false, true, true, true }, //
				// new boolean[] { false, true, false, true }, //
				// new boolean[] { false, false, true, true }, //
				// new boolean[] { false, false, false, true }, //
				// new boolean[] { false, true, true, false }, //
				// new boolean[] { false, true, false, false }, //
				// new boolean[] { false, false, true, false }, //
				// new boolean[] { false, false, false, false } //
		);
	}
}
