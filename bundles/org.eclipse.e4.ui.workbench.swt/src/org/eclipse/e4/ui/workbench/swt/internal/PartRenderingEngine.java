/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.Map.Entry;
import javax.inject.Inject;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.StatusReporter;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.annotations.PreDestroy;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.context.spi.IEclipseContextStrategy;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.internal.core.services.bundle.BundleContextStrategy;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MGenericStack;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.IResourceUtiltities;
import org.eclipse.e4.workbench.ui.IWorkbench;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.e4.workbench.ui.internal.Activator;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
import org.eclipse.e4.workbench.ui.internal.Policy;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.bindings.keys.formatting.KeyFormatterFactory;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.testing.TestableObject;
import org.osgi.framework.Bundle;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class PartRenderingEngine implements IPresentationEngine {
	public static final String engineURI = "platform:/plugin/org.eclipse.e4.ui.workbench.swt/"
			+ "org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine";

	private String defaultRenderingFactoryId = "org.eclipse.e4.ui.workbench.renderers.default";
	private String curFactoryId = defaultRenderingFactoryId;
	IRendererFactory curFactory = null;

	// Life Cycle handlers
	private EventHandler toBeRenderedHandler = new EventHandler() {
		public void handleEvent(Event event) {

			MUIElement changedElement = (MUIElement) event
					.getProperty(UIEvents.EventTags.ELEMENT);

			// If the parent isn't displayed who cares?
			MElementContainer<?> parent = changedElement.getParent();
			AbstractPartRenderer parentRenderer = parent != null ? getRendererFor(parent)
					: null;
			if (parentRenderer == null)
				return;

			if (changedElement.isToBeRendered()) {
				Activator.trace(Policy.DEBUG_RENDERER, "visible -> true", null); //$NON-NLS-1$

				// Note that the 'createGui' protocol calls 'childAdded'
				createGui(changedElement);
			} else {
				Activator
						.trace(Policy.DEBUG_RENDERER, "visible -> false", null); //$NON-NLS-1$

				// Note that the 'createGui' protocol calls 'childRemoved'
				removeGui(changedElement);
			}

		}
	};

	private EventHandler childrenHandler = new EventHandler() {
		public void handleEvent(Event event) {

			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (!(changedObj instanceof MElementContainer<?>))
				return;

			MElementContainer<MUIElement> changedElement = (MElementContainer<MUIElement>) changedObj;
			boolean isApplication = changedObj instanceof MApplication;

			// If the parent isn't in the UI then who cares?
			AbstractPartRenderer renderer = getRendererFor(changedElement);
			if (!isApplication && renderer == null)
				return;

			String eventType = (String) event
					.getProperty(UIEvents.EventTags.TYPE);
			if (UIEvents.EventTypes.ADD.equals(eventType)) {
				Activator.trace(Policy.DEBUG_RENDERER, "Child Added", null); //$NON-NLS-1$
				MUIElement added = (MUIElement) event
						.getProperty(UIEvents.EventTags.NEW_VALUE);

				// OK, we have a new -visible- part we either have to create
				// it or host it under the correct parent. Note that we
				// explicitly do *not* render non-selected elements in
				// stacks (to support lazy loading).
				boolean isStack = changedObj instanceof MGenericStack<?>;
				boolean hasWidget = added.getWidget() != null;
				boolean isSelected = added == changedElement
						.getSelectedElement();
				boolean renderIt = !isStack || hasWidget || isSelected;
				if (renderIt) {
					// NOTE: createGui will call 'childAdded' if successful
					Widget w = (Widget) createGui(added);
					if (w instanceof Control && !(w instanceof Shell)) {
						((Control) w).getShell().layout(
								new Control[] { (Control) w }, SWT.DEFER);
					}
				} else {
					if (renderer != null)
						renderer.childRendered(changedElement, added);
				}
			} else if (UIEvents.EventTypes.REMOVE.equals(eventType)) {
				Activator.trace(Policy.DEBUG_RENDERER, "Child Removed", null); //$NON-NLS-1$
				MUIElement removed = (MUIElement) event
						.getProperty(UIEvents.EventTags.OLD_VALUE);
				// Removing invisible elements is a NO-OP as far as the
				// renderer is concerned
				if (!removed.isToBeRendered())
					return;

				if (removed.getWidget() instanceof Control) {
					Control ctrl = (Control) removed.getWidget();
					ctrl.setLayoutData(null);
					ctrl.getParent().layout(new Control[] { ctrl },
							SWT.CHANGED | SWT.DEFER);
				}

				if (renderer != null)
					renderer.hideChild(changedElement, removed);
			}
		}
	};

	private IEclipseContext appContext;

	protected Shell testShell;

	protected MApplication theApp;

	@Inject
	@Optional
	protected IEventBroker eventBroker;

	@Inject
	protected Logger logger;

	public PartRenderingEngine() {
		super();
	}

	public PartRenderingEngine(String curFactoryId) {
		this.curFactoryId = curFactoryId;
	}

	/**
	 * Initialize a part renderer from the extension point.
	 * 
	 * @param context
	 *            the context for the part factories
	 */
	@PostConstruct
	void initialize(IEclipseContext context) {
		this.appContext = context;

		// initialize the correct key-binding display formatter
		KeyFormatterFactory.setDefault(SWTKeySupport
				.getKeyFormatterForPlatform());

		IExtensionRegistry registry = (IExtensionRegistry) context
				.get(IExtensionRegistry.class.getName());
		IConfigurationElement[] factories = registry
				.getConfigurationElementsFor("org.eclipse.e4.workbench.rendererfactory"); //$NON-NLS-1$
		for (int i = 0; i < factories.length; i++) {
			String id = factories[i].getAttribute("id"); //$NON-NLS-1$
			if (!curFactoryId.equals(id))
				continue;

			IRendererFactory factory = null;
			try {
				factory = (IRendererFactory) factories[i]
						.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				e.printStackTrace();
			}

			if (factory != null) {
				factory.init(context);
				curFactory = factory;
			}
		}

		// Add the renderer to the context
		context.set(IPresentationEngine.class.getName(), this);

		// Hook up the widget life-cycle subscriber
		if (eventBroker != null) {
			eventBroker.subscribe(UIEvents.buildTopic(UIEvents.UIElement.TOPIC,
					UIEvents.UIElement.TOBERENDERED), toBeRenderedHandler);
			eventBroker.subscribe(UIEvents.buildTopic(
					UIEvents.ElementContainer.TOPIC,
					UIEvents.ElementContainer.CHILDREN), childrenHandler);
		}
	}

	@PreDestroy
	private void contextDisposed() {
		if (eventBroker == null)
			return;
		eventBroker.unsubscribe(toBeRenderedHandler);
		eventBroker.unsubscribe(childrenHandler);
	}

	private static void populateModelInterfaces(MContext contextModel,
			IEclipseContext context, Class<?>[] interfaces) {
		for (Class<?> intf : interfaces) {
			Activator.trace(Policy.DEBUG_CONTEXTS,
					"Adding " + intf.getName() + " for " //$NON-NLS-1$ //$NON-NLS-2$
							+ contextModel.getClass().getName(), null);
			context.set(intf.getName(), contextModel);

			populateModelInterfaces(contextModel, context, intf.getInterfaces());
		}
	}

	public Object createGui(MUIElement element, Object parent) {
		if (!element.isToBeRendered())
			return null;

		if (element instanceof MContext) {
			MContext ctxt = (MContext) element;
			// Assert.isTrue(ctxt.getContext() == null,
			// "Before rendering Context should be null");
			if (ctxt.getContext() == null) {
				IEclipseContext parentContext = element.getParent() == null ? appContext
						: getContext(element.getParent());
				IEclipseContextStrategy strategy;
				if (element instanceof MContribution) {
					IContributionFactory contributionFactory = (IContributionFactory) parentContext
							.get(IContributionFactory.class.getName());
					MContribution contribution = (MContribution) element;
					Bundle bundle = contributionFactory.getBundle(contribution
							.getURI());
					strategy = new BundleContextStrategy(bundle);
				} else {
					strategy = null;
				}
				IEclipseContext lclContext = EclipseContextFactory.create(
						parentContext, strategy);
				populateModelInterfaces(ctxt, lclContext, element.getClass()
						.getInterfaces());
				ctxt.setContext(lclContext);

				// make sure the context knows about these variables that have
				// been defined in the model
				for (String variable : ctxt.getVariables()) {
					lclContext.declareModifiable(variable);
				}

				for (Entry<String, String> property : ctxt.getProperties()) {
					lclContext.set(property.getKey(), property.getValue());
				}

				E4Workbench.processHierarchy(element);
			}
		}

		// Has this already been created? if so treat as a reparent
		if (element.getWidget() != null) {
			if (parent instanceof Composite
					&& element.getWidget() instanceof Control) {
				// Re-parent the control
				final Composite p = (Composite) parent;
				Control c = (Control) element.getWidget();
				if (c.getParent() == parent)
					return c;

				c.setVisible(true);
				c.setLayoutData(null);
				c.setParent(p);
				final Control[] changed = { c };

				// Defer the layout in order to allow the rendering to finish
				c.getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (p.isDisposed())
							return;
						p.layout(changed, SWT.CHANGED | SWT.DEFER);
						p.getShell().layout(changed, SWT.CHANGED | SWT.DEFER);
					}
				});

				// Re-parent the context
				if (element instanceof MContext) {
					IEclipseContext ec = ((MContext) element).getContext();
					IEclipseContext pc = getContext(element.getParent());
					ec.set(IContextConstants.PARENT, pc);
				}

				getRendererFor(element.getParent()).childRendered(
						element.getParent(), element);
				return c;
			}
		}

		// Create a control appropriate to the part
		Object newWidget = createWidget(element, parent);

		// Remember that we've created the control
		if (newWidget != null) {
			AbstractPartRenderer renderer = getRendererFor(element);

			// Have the renderer hook up any widget specific listeners
			renderer.hookControllerLogic(element);

			// Process its internal structure through the renderer that created
			// it
			if (element instanceof MElementContainer) {
				renderer.processContents((MElementContainer<MUIElement>) element);
			}

			// Allow a final chance to set up
			renderer.postProcess(element);

			// Now that we have a widget let the parent (if any) know
			if (element.getParent() instanceof MUIElement) {
				MElementContainer<MUIElement> parentElement = element
						.getParent();
				AbstractPartRenderer parentRenderer = getRendererFor(parentElement);
				if (parentRenderer != null)
					parentRenderer.childRendered(parentElement, element);
			}
		} else {
			// failed to create the widget, dispose its context if necessary
			if (element instanceof MContext) {
				MContext ctxt = (MContext) element;
				IEclipseContext lclContext = ctxt.getContext();
				if (lclContext instanceof IDisposable) {
					((IDisposable) lclContext).dispose();
				}
			}
		}

		return newWidget;
	}

	private IEclipseContext getContext(MElementContainer<MUIElement> parent) {
		MUIElement uiElement = parent;
		while (uiElement != null) {
			if (uiElement instanceof MContext) {
				return ((MContext) uiElement).getContext();
			}
			uiElement = uiElement.getParent();
		}
		return null;
	}

	public Object createGui(MUIElement element) {
		// Obtain the necessary parent and context
		Object parent = null;
		MUIElement parentME = element.getParent();
		if (parentME != null) {
			AbstractPartRenderer renderer = getRendererFor(parentME);
			if (renderer != null) {
				parent = renderer.getUIContainer(element);
			}
		}

		return createGui(element, parent);
	}

	/**
	 * @param element
	 */
	public void removeGui(MUIElement element) {
		AbstractPartRenderer renderer = getRendererFor(element);
		Assert.isNotNull(renderer);

		MUIElement parent = element.getParent();
		AbstractPartRenderer parentRenderer = parent != null ? getRendererFor(parent)
				: null;
		if (parentRenderer != null) {
			parentRenderer.hideChild(element.getParent(), element);
		}

		renderer.disposeWidget(element);

		// unset the client object
		if (element instanceof MContribution) {
			((MContribution) element).setObject(null);
		}

		// dispose the context
		if (element instanceof MContext) {
			MContext ctxt = (MContext) element;
			IEclipseContext lclContext = ctxt.getContext();
			IEclipseContext parentContext = (IEclipseContext) lclContext
					.get(IContextConstants.PARENT);
			ctxt.setContext(null);
			if (lclContext instanceof IDisposable) {
				((IDisposable) lclContext).dispose();
			}
		}
	}

	protected Object createWidget(MUIElement element, Object parent) {
		AbstractPartRenderer renderer = getRenderer(element, parent);
		if (renderer != null) {
			Object newWidget = renderer.createWidget(element, parent);
			if (newWidget != null) {
				renderer.bindWidget(element, newWidget);
				return newWidget;
			}
		}

		return null;
	}

	private AbstractPartRenderer getRenderer(MUIElement uiElement, Object parent) {
		return curFactory.getRenderer(uiElement, parent);
	}

	protected AbstractPartRenderer getRendererFor(MUIElement element) {
		return (AbstractPartRenderer) element.getRenderer();
	}

	/*
	 * For use when there is no real styling engine present. Has no behaviour
	 * but conforms to IStylingEngine API.
	 * 
	 * @param appContext
	 */
	private static void initializeNullStyling(IEclipseContext appContext) {
		appContext.set(IStylingEngine.SERVICE_NAME, new IStylingEngine() {
			public void setClassname(Object widget, String classname) {
			}

			public void setId(Object widget, String id) {
			}

			public void style(Object widget) {
			}
		});
	}

	public Object run(final MApplicationElement uiRoot,
			final IEclipseContext runContext) {
		final Display display = Display.getDefault();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				String cssURI = (String) runContext
						.get(E4Workbench.CSS_URI_ARG);
				if (cssURI != null) {
					String cssResourcesURI = (String) runContext
							.get(E4Workbench.CSS_RESOURCE_URI_ARG);
					CSSStylingSupport.initializeStyling(display, cssURI,
							cssResourcesURI, runContext);
				} else {
					initializeNullStyling(runContext);
				}

				// Register an SWT resource handler
				runContext.set(IResourceUtiltities.class.getName(),
						new ResourceUtility(Activator.getDefault()
								.getBundleAdmin()));

				// set up the keybinding manager
				try {
					KeyBindingDispatcher dispatcher = (KeyBindingDispatcher) ContextInjectionFactory
							.make(KeyBindingDispatcher.class, runContext);
					runContext.set(KeyBindingDispatcher.class.getName(),
							dispatcher);
					org.eclipse.swt.widgets.Listener listener = dispatcher
							.getKeyDownFilter();
					display.addFilter(SWT.KeyDown, listener);
					display.addFilter(SWT.Traverse, listener);
				} catch (InvocationTargetException e) {
					if (logger != null) {
						logger.error(e);
					}
				} catch (InstantiationException e) {
					if (logger != null) {
						logger.error(e);
					}
				}

				// Show the initial UI

				// HACK!! we should loop until the display gets disposed...
				// ...then we listen for the last 'main' window to get disposed
				// and dispose the Display
				testShell = null;
				theApp = null;
				boolean spinOnce = true;
				if (uiRoot instanceof MApplication) {
					spinOnce = false; // loop until the app closes
					theApp = (MApplication) uiRoot;
					// long startTime = System.currentTimeMillis();
					for (MWindow window : theApp.getChildren()) {
						testShell = (Shell) createGui(window);
					}
					// long endTime = System.currentTimeMillis();
					// System.out.println("Render: " + (endTime - startTime));
				} else if (uiRoot instanceof MUIElement) {
					if (uiRoot instanceof MWindow) {
						testShell = (Shell) createGui((MUIElement) uiRoot);
					} else {
						// Special handling for partial models (for testing...)
						testShell = new Shell(display, SWT.SHELL_TRIM);
						createGui((MUIElement) uiRoot, testShell);
					}
				}

				TestableObject testableObject = (TestableObject) runContext
						.get(TestableObject.class.getName());
				if (testableObject instanceof E4Testable) {
					((E4Testable) testableObject).init(display,
							(IWorkbench) runContext.get(IWorkbench.class
									.getName()));
				}
				// Spin the event loop until someone disposes the display
				while (testShell != null && !testShell.isDisposed()
						&& !display.isDisposed()) {
					try {
						if (!display.readAndDispatch()) {
							runContext.processWaiting();
							if (spinOnce)
								return;
							display.sleep();
						}
					} catch (ThreadDeath th) {
						throw th;
					} catch (Exception ex) {
						handle(ex, runContext);
					} catch (Error err) {
						handle(err, runContext);
					}
				}
			}

			private void handle(Throwable ex, final IEclipseContext appContext) {
				StatusReporter statusReporter = (StatusReporter) appContext
						.get(StatusReporter.class.getName());
				if (statusReporter != null) {
					statusReporter.show(StatusReporter.ERROR, "Internal Error",
							ex);
				} else {
					if (logger != null) {
						logger.error(ex);
					}
				}
			}
		});

		return IApplication.EXIT_OK;
	}

	public void stop() {
		if (theApp != null) {
			for (MWindow window : theApp.getChildren()) {
				if (window.getWidget() != null) {
					((Shell) window.getWidget()).close();
				}
			}
		} else if (testShell != null && !testShell.isDisposed()) {
			testShell.close();
		}
	}
}
