package org.eclipse.e4.ui.workbench.addons.minmax;

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
public class MaximizePartSashContainerPlaceholderTest {

	@Parameters(name = "org.eclipse.ui.editorss: {0}")
	public static Collection<Object[]> data() {
		final List<Object[]> data = new ArrayList<Object[]>();
		// useCorrectPlaceholderId
		data.add(new Object[] { true });
		data.add(new Object[] { false });

		return data;
	}

	private MPartStack partStackMain;
	private MPlaceholder placeholderMain;

	private boolean useCorrectPlaceholderId;
	private Shell shell;

	public MaximizePartSashContainerPlaceholderTest(boolean useCorrectPlaceholderId) {
		this.useCorrectPlaceholderId = useCorrectPlaceholderId;

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

		MPartSashContainer patSashConatiner = BasicFactoryImpl.eINSTANCE
				.createPartSashContainer();
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

		patSashConatiner.getChildren().add(partStackAreaMain);
		partStackAreaMain.getChildren().add(partAreaMain);

		placeholderMain.setRef(patSashConatiner);

		window.getSharedElements().add(patSashConatiner);

		// set correct ids
		if (useCorrectPlaceholderId) {
			placeholderMain.setElementId("org.eclipse.ui.editorss");
		} else {
			placeholderMain.setElementId("placeholderMain");
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
	}

}
