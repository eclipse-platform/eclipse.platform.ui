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

package org.eclipse.e4.ui.tests.application;

import junit.framework.TestCase;

import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextFunction;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.context.spi.IEclipseContextStrategy;
import org.eclipse.e4.core.services.context.spi.ISchedulerStrategy;
import org.eclipse.e4.ui.internal.services.ActiveContextsFunction;
import org.eclipse.e4.ui.internal.services.ContextCommandService;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.ECommandService;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.events.EventBrokerFactory;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.swt.Activator;
import org.eclipse.e4.workbench.ui.IExceptionHandler;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.internal.ActiveChildOutputFunction;
import org.eclipse.e4.workbench.ui.internal.ActivePartLookupFunction;
import org.eclipse.e4.workbench.ui.internal.ExceptionHandler;
import org.eclipse.e4.workbench.ui.internal.ReflectionContributionFactory;
import org.eclipse.e4.workbench.ui.internal.UIModelEventPublisher;
import org.eclipse.e4.workbench.ui.internal.WorkbenchLogger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public abstract class HeadlessStartupTest extends TestCase {

	protected MApplication application;

	protected IPresentationEngine renderer;

	@Override
	protected void setUp() throws Exception {
		IEclipseContext appContext = createAppContext();
		appContext.set(IContextConstants.DEBUG_STRING, "Application Context"); //$NON-NLS-1$
		application = createApplication(appContext, getAppURI());

		if (needsActiveChildEventHandling()) {
			addActiveChildEventHandling();
		}

		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		for (MWindow window : application.getChildren()) {
			renderer.removeGui(window);
		}
	}

	protected boolean needsActiveChildEventHandling() {
		return true;
	}

	private void addActiveChildEventHandling() {
		IEventBroker eventBroker = (IEventBroker) application.getContext().get(
				IEventBroker.class.getName());
		eventBroker.subscribe(UIModelEventPublisher.ElementContainer.Topic,
				null, new EventHandler() {
					public void handleEvent(Event event) {
						if (event.getProperty(
								UIModelEventPublisher.EventTags.AttName)
								.equals("activeChild")) {
							Object oldPart = event
									.getProperty(UIModelEventPublisher.EventTags.OldValue);
							Object newPart = event
									.getProperty(UIModelEventPublisher.EventTags.NewValue);
							if (oldPart instanceof MContext) {
								IEclipseContext context = (IEclipseContext) ((MContext) oldPart)
										.getContext().get(
												IContextConstants.PARENT);
								context.set(IServiceConstants.ACTIVE_CHILD,
										newPart == null ? null
												: ((MContext) newPart)
														.getContext());
								context.set(IServiceConstants.ACTIVE_PART,
										newPart);
							} else if (newPart instanceof MContext) {
								if (((MContext) newPart).getContext() == null) {
									return;
								}
								IEclipseContext context = (IEclipseContext) ((MContext) newPart)
										.getContext().get(
												IContextConstants.PARENT);
								context.set(IServiceConstants.ACTIVE_CHILD,
										((MContext) newPart).getContext());
								context.set(IServiceConstants.ACTIVE_PART,
										newPart);
							}
						}
					}
				}, true);
	}

	protected abstract String getAppURI();

	protected String getEngineURI() {
		return "platform:/plugin/org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.application.HeadlessContextPresentationEngine"; //$NON-NLS-1$
	}

	public void testGet_ActiveContexts() throws Exception {
		IEclipseContext context = application.getContext();

		assertNotNull(context.get(IServiceConstants.ACTIVE_CONTEXTS));
	}

	public void testGet_Selection() throws Exception {
		IEclipseContext context = application.getContext();

		assertNull(context.get(IServiceConstants.SELECTION));
	}

	public void testGet_ActiveChild() throws Exception {
		IEclipseContext context = application.getContext();

		assertNull(context.get(IServiceConstants.ACTIVE_CHILD));
	}

	public void testGet_ActivePart() throws Exception {
		IEclipseContext context = application.getContext();

		assertNull(context.get(IServiceConstants.ACTIVE_PART));
	}

	public void testGet_Input() throws Exception {
		IEclipseContext context = application.getContext();

		assertNull(context.get(IServiceConstants.INPUT));
	}

	public void testGet_PersistedState() throws Exception {
		IEclipseContext context = application.getContext();

		assertNull(context.get(IServiceConstants.PERSISTED_STATE));
	}

	public void testGet_ActivePartId() throws Exception {
		IEclipseContext context = application.getContext();
		assertNull(context.get(IServiceConstants.ACTIVE_PART_ID));
	}

	public void test_SwitchActivePartsInCode() throws Exception {
		IEclipseContext context = application.getContext();

		MPart[] parts = getTwoParts();

		parts[0].getParent().setActiveChild(parts[0]);
		assertEquals(parts[0].getId(), context
				.get(IServiceConstants.ACTIVE_PART_ID));

		parts[1].getParent().setActiveChild(parts[1]);
		assertEquals(parts[1].getId(), context
				.get(IServiceConstants.ACTIVE_PART_ID));
	}

	public void test_SwitchActivePartsInContext() throws Exception {
		IEclipseContext context = application.getContext();

		MPart[] parts = getTwoParts();

		context.set(IServiceConstants.ACTIVE_PART, parts[0]);
		assertEquals(parts[0].getId(), context
				.get(IServiceConstants.ACTIVE_PART_ID));

		context.set(IServiceConstants.ACTIVE_PART, parts[1]);
		assertEquals(parts[1].getId(), context
				.get(IServiceConstants.ACTIVE_PART_ID));
	}

	protected MPart[] getTwoParts() {
		MPart firstPart = getFirstPart();
		assertNotNull(firstPart);

		MPart secondPart = getSecondPart();
		assertNotNull(secondPart);

		assertFalse(firstPart.equals(secondPart));

		return new MPart[] { firstPart, secondPart };
	}

	protected abstract MPart getFirstPart();

	protected abstract MPart getSecondPart();

	private IEclipseContext createOSGiContext() {
		IEclipseContext serviceContext = EclipseContextFactory
				.createServiceContext(Activator.getDefault().getBundle()
						.getBundleContext());
		return serviceContext;
	}

	protected IEclipseContext createAppContext() {
		return createAppContext(createOSGiContext());
	}

	protected ISchedulerStrategy getAppSchedulerStrategy() {
		return null;
	}

	protected IEclipseContext createAppContext(IEclipseContext osgiContext) {
		assertNotNull(osgiContext);

		final IEclipseContext mainContext = createContext(osgiContext,
				getAppSchedulerStrategy());

		mainContext.set(IEclipseContext.class.getName(), mainContext);

		mainContext.set(IEventBroker.class.getName(), EventBrokerFactory
				.newEventBroker());
		mainContext.set(IContributionFactory.class.getName(),
				new ReflectionContributionFactory(
						(IExtensionRegistry) mainContext
								.get(IExtensionRegistry.class.getName())));
		mainContext.set(IExceptionHandler.class.getName(),
				new ExceptionHandler());
		mainContext.set(Logger.class.getName(), new WorkbenchLogger());

		mainContext.set(CommandManager.class.getName(), new CommandManager());
		mainContext.set(ContextManager.class.getName(), new ContextManager());
		mainContext.set(ECommandService.class.getName(),
				new ContextCommandService(mainContext));
		mainContext.set(IServiceConstants.ACTIVE_CONTEXTS,
				new ActiveContextsFunction());
		mainContext.set(IServiceConstants.ACTIVE_PART,
				new ActivePartLookupFunction());
		mainContext.runAndTrack(new Runnable() {
			public void run() {
				Object o = mainContext.get(IServiceConstants.ACTIVE_PART);
				if (o instanceof MPart) {
					mainContext.set(IServiceConstants.ACTIVE_PART_ID,
							((MPart) o).getId());
				}
			}

			/*
			 * For debugging purposes only
			 */
			@Override
			public String toString() {
				return IServiceConstants.ACTIVE_PART_ID;
			}
		});
		mainContext.set(IServiceConstants.ACTIVE_PART_ID,
				new ContextFunction() {

					@Override
					public Object compute(IEclipseContext context,
							Object[] arguments) {
						MApplicationElement element = (MApplicationElement) context
								.get(IServiceConstants.ACTIVE_PART);
						return element == null ? null : element.getId();
					}
				});
		mainContext.set(IServiceConstants.SELECTION,
				new ActiveChildOutputFunction(IServiceConstants.SELECTION));
		mainContext.set(IServiceConstants.INPUT, new ContextFunction() {
			public Object compute(IEclipseContext context, Object[] arguments) {
				Class adapterType = null;
				if (arguments.length > 0 && arguments[0] instanceof Class) {
					adapterType = (Class) arguments[0];
				}
				Object newInput = null;
				Object newValue = context.get(IServiceConstants.SELECTION);
				if (adapterType == null || adapterType.isInstance(newValue)) {
					newInput = newValue;
				} else if (newValue != null && adapterType != null) {
					IAdapterManager adapters = (IAdapterManager) context
							.get(IAdapterManager.class.getName());
					if (adapters != null) {
						Object adapted = adapters.loadAdapter(newValue,
								adapterType.getName());
						if (adapted != null) {
							newInput = adapted;
						}
					}
				}
				return newInput;
			}
		});

		return mainContext;
	}

	private IEclipseContext createContext(IEclipseContext parent,
			IEclipseContextStrategy strategy) {
		IEclipseContext eclipseContext = EclipseContextFactory.create(parent,
				strategy);
		return eclipseContext;
	}

	protected MApplication createApplication(IEclipseContext appContext,
			String appURI) throws Exception {
		URI initialWorkbenchDefinitionInstance = URI.createPlatformPluginURI(
				appURI, true);

		ResourceSet set = new ResourceSetImpl();
		set.getPackageRegistry().put("http://MApplicationPackage/",
				MApplicationPackage.eINSTANCE);

		Resource resource = set.getResource(initialWorkbenchDefinitionInstance,
				true);

		application = (MApplication) resource.getContents().get(0);
		appContext.set(MApplication.class.getName(), application);
		application.setContext(appContext);

		processPartContributions(application.getContext(), resource);

		renderer = createPresentationEngine(getEngineURI());

		EList<MWindow> windows = application.getChildren();
		for (MWindow wbw : windows) {
			createGUI(wbw);
		}

		// Hook the global notifications
		((Notifier) application).eAdapters().add(
				new UIModelEventPublisher(appContext));

		return application;
	}

	private void processPartContributions(IEclipseContext context,
			Resource resource) {
		IExtensionRegistry registry = (IExtensionRegistry) context
				.get(IExtensionRegistry.class.getName());
		String extId = "org.eclipse.e4.workbench.parts"; //$NON-NLS-1$
		IConfigurationElement[] parts = registry
				.getConfigurationElementsFor(extId);

		for (int i = 0; i < parts.length; i++) {
			MPart part = MApplicationFactory.eINSTANCE.createPart();
			part.setName(parts[i].getAttribute("label")); //$NON-NLS-1$
			part.setIconURI("platform:/plugin/" //$NON-NLS-1$
					+ parts[i].getContributor().getName() + "/" //$NON-NLS-1$
					+ parts[i].getAttribute("icon")); //$NON-NLS-1$
			part.setURI("platform:/plugin/" //$NON-NLS-1$
					+ parts[i].getContributor().getName() + "/" //$NON-NLS-1$
					+ parts[i].getAttribute("class")); //$NON-NLS-1$
			String parentId = parts[i].getAttribute("parentId"); //$NON-NLS-1$

			Object parent = findObject(resource.getAllContents(), parentId);
			if (parent instanceof MElementContainer<?>) {
				((MElementContainer<MPSCElement>) parent).getChildren().add(
						part);
			}
		}

	}

	private EObject findObject(TreeIterator<EObject> it, String id) {
		while (it.hasNext()) {
			EObject el = it.next();
			if (el instanceof MApplicationElement) {
				if (el.eResource().getURIFragment(el).equals(id)) {
					return el;
				}
			}
		}

		return null;
	}

	protected MApplicationElement findElement(String id) {
		return findElement(application, id);
	}

	private MApplicationElement findElement(MElementContainer<?> container,
			String id) {
		if (id.equals(container.getId())) {
			return container;
		}

		EList<?> children = container.getChildren();
		for (Object child : children) {
			MApplicationElement element = (MApplicationElement) child;
			if (element instanceof MElementContainer<?>) {
				MApplicationElement found = findElement(
						(MElementContainer<?>) element, id);
				if (found != null) {
					return found;
				}
			} else if (id.equals(element.getId())) {
				return element;
			}
		}
		return null;
	}

	private IPresentationEngine createPresentationEngine(
			String renderingEngineURI) {
		IContributionFactory contributionFactory = (IContributionFactory) application
				.getContext().get(IContributionFactory.class.getName());
		Object newEngine = contributionFactory.create(renderingEngineURI,
				application.getContext());
		return (IPresentationEngine) newEngine;
	}

	protected void createGUI(MUIElement uiRoot) {
		renderer.createGui(uiRoot);
	}
}
