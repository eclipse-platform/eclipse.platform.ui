package org.eclipse.e4.ui.workbench.addons.minmax;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MaximizeBugTest {

	@Parameters(name = "org.eclipse.ui.editorss: {0} - subwindowPerspective: {1} - maximizeMainFirst: {2} - addSubwindowToPerspective:{3}")
	public static Collection<Object[]> data() {
		final List<Object[]> data = new ArrayList<Object[]>();
		// useCorrectPlaceholderId,usePerspektiveInSubWindow,maximizeMainFirst,addSubwindowToPerspective
		// working
		data.add(new Object[] { true, true, true, true });
		data.add(new Object[] { true, true, false, true });
		data.add(new Object[] { true, false, true, true });
		data.add(new Object[] { true, false, false, true });

		data.add(new Object[] { true, true, true, false });
		data.add(new Object[] { true, true, false, false });
		// not working
		data.add(new Object[] { true, false, true, false });
		data.add(new Object[] { true, false, false, false });

		// Extremely evil
		// data.add(new Object[] { false, true,true,true });
		// data.add(new Object[] { false, true,false,true });
		// data.add(new Object[] { false, false,true,true });
		// data.add(new Object[] { false, false,false,true });
		// data.add(new Object[] { false, true,true,false });
		// data.add(new Object[] { false, true,false,false });
		// data.add(new Object[] { false, false,true,false });
		// data.add(new Object[] { false, false,false,false });

		return data;
	}

	private MPartStack partStackMain;
	private MPlaceholder placeholderMain;

	private MPartStack partStackSub;
	private MPlaceholder placeholderSub;
	private boolean useCorrectPlaceholderId;
	private boolean usePerspektiveInSubWindow;
	private boolean maximizeMainFirst;
	private boolean addSubwindowToPerspective;
	private Shell shell;
	private MTrimmedWindow windowSub;

	public MaximizeBugTest(boolean useCorrectPlaceholderId,
			boolean usePerspektiveInSubWindow, boolean maximizeMainFirst,
			boolean addSubwindowToPerspective) {
		this.useCorrectPlaceholderId = useCorrectPlaceholderId;
		this.usePerspektiveInSubWindow = usePerspektiveInSubWindow;
		this.maximizeMainFirst = maximizeMainFirst;
		this.addSubwindowToPerspective = addSubwindowToPerspective;

	}

	@Before
	public void before() {
		prepareApplicationModel();
	}

	@After
	public void tearDown() {
		shell.dispose();
	}

	private void prepareApplicationModel() {
		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();

		MTrimmedWindow window = BasicFactoryImpl.eINSTANCE
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
		if (addSubwindowToPerspective)
			perspectiveMain.getWindows().add(windowSub);
		else
			window.getWindows().add(windowSub);
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

		IEclipseContext appContext = E4Application.createDefaultContext();
		appContext.set(Display.class, Display.getDefault());
		appContext.set(MApplication.class.getName(), application);
		appContext.set(MWindow.class, window);
		appContext.set(UISynchronize.class, new UISynchronize() {

			public void syncExec(Runnable runnable) {
				runnable.run();
			}

			public void asyncExec(Runnable runnable) {
				runnable.run();
			}
		});
		appContext.set(EModelService.class, new ModelServiceImpl(appContext));

		ContextInjectionFactory.setDefault(appContext);
		IPresentationEngine newEngine = ContextInjectionFactory.make(
				PartRenderingEngine.class, appContext);

		appContext.set(IPresentationEngine.class, newEngine);
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

		shell = (Shell) newEngine.createGui(window);
	}

	@Test
	public void testMainPlaceholderMax() {
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

	@Test
	public void testMainPartStackMax() {
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

	@Test
	public void testSubPlaceholderMax() {
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

	@Test
	public void testSubPartStackMax() {
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

	@Test
	public void testMainPlaceholderMaxThenUnzoom() {
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

	@Test
	public void testMainPartStackMaxThenUnzoom() {
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

	@Test
	public void testSubPlaceholderMaxThenUnzoom() {
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

	@Test
	public void testSubPartStackMaxThenUnzoom() {
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

	@Test
	public void testMainPlaceholderMaxSubPlaceholderMax() {
		if (maximizeMainFirst) {
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

	@Test
	public void testMainPlaceholderMaxSubPartStackMax() {
		if (maximizeMainFirst) {
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

	@Test
	public void testMainPartStackMaxSubPlaceholderMax() {

		if (maximizeMainFirst) {
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

	@Test
	public void testMainPartStackMaxSubPartStackMax() {

		if (maximizeMainFirst) {
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

	@Test
	public void testMainPlaceholderMaxSubPlaceholderMaxUnzoomMain() {

		if (maximizeMainFirst) {
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

	@Test
	public void testMainPlaceholderMaxSubPartStackMaxUnzoomMain() {

		if (maximizeMainFirst) {
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

	@Test
	public void testMainPartStackMaxSubPlaceholderMaxUnzoomMain() {

		if (maximizeMainFirst) {
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

	@Test
	public void testMainPartStackMaxSubPartStackMaxUnzoomMain() {

		if (maximizeMainFirst) {
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

	@Test
	public void testMainPlaceholderMaxSubPlaceholderMaxUnzoomSub() {

		if (maximizeMainFirst) {
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

	@Test
	public void testMainPlaceholderMaxSubPartStackMaxUnzoomSub() {

		if (maximizeMainFirst) {
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

	@Test
	public void testMainPartStackMaxSubPlaceholderMaxUnzoomSub() {

		if (maximizeMainFirst) {
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

	@Test
	public void testMainPartStackMaxSubPartStackMaxUnzoomSub() {

		if (maximizeMainFirst) {
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

	@Test
	public void testSubWindowWithSubWindowMaximizeStack() {
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

	@Test
	public void testSubWindowWithSubWindowMaximizePlaceholder() {
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
}
