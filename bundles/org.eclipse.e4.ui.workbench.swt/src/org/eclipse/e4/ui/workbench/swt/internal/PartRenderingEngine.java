/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.swt.internal;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.IResourceUtiltities;
import org.eclipse.e4.workbench.ui.internal.Activator;
import org.eclipse.e4.workbench.ui.internal.E4Workbench;
import org.eclipse.e4.workbench.ui.internal.IUIEvents;
import org.eclipse.e4.workbench.ui.internal.Policy;
import org.eclipse.e4.workbench.ui.internal.UISchedulerStrategy;
import org.eclipse.e4.workbench.ui.internal.Workbench;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class PartRenderingEngine implements IPresentationEngine {
	public static final String engineURI = "platform:/plugin/org.eclipse.e4.ui.workbench.swt/"
			+ "org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine";

	private String defaultRenderingFactoryId = "org.eclipse.e4.ui.workbench.renderers.default";
	private String curFactoryId = defaultRenderingFactoryId;
	IRendererFactory curFactory = null;

	// Life Cycle handlers
	private EventHandler visibilityHandler = new EventHandler() {
		public void handleEvent(Event event) {
			String attName = (String) event
					.getProperty(IUIEvents.EventTags.AttName);
			if (!IUIEvents.UIElement.Visible.equals(attName))
				return;

			MUIElement changedElement = (MUIElement) event
					.getProperty(IUIEvents.EventTags.Element);

			// If the parent isn't displayed who cares?
			MElementContainer<?> parent = changedElement.getParent();
			AbstractPartRenderer parentFactory = parent != null ? getFactoryFor(parent)
					: null;
			if (parentFactory == null)
				return;

			if (changedElement.isVisible()) {
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
			MElementContainer<MUIElement> changedElement = (MElementContainer<MUIElement>) event
					.getProperty(IUIEvents.EventTags.Element);

			// If the parent isn't in the UI then who cares?
			AbstractPartRenderer factory = getFactoryFor(changedElement);
			if (factory == null)
				return;

			String eventType = (String) event
					.getProperty(IUIEvents.EventTags.Type);
			if (IUIEvents.EventTypes.Add.equals(eventType)) {
				Activator.trace(Policy.DEBUG_RENDERER, "Child Added", null); //$NON-NLS-1$
				MUIElement added = (MUIElement) event
						.getProperty(IUIEvents.EventTags.NewValue);

				// OK, we have a new -visible- part we either have to create
				// it or host it under the correct parent
				if (added.getWidget() == null)
					// NOTE: createGui will call 'childAdded' if successful
					createGui(added);
				else {
					factory.childRendered(changedElement, added);
				}
			} else if (IUIEvents.EventTypes.Remove.equals(eventType)) {
				Activator.trace(Policy.DEBUG_RENDERER, "Child Removed", null); //$NON-NLS-1$
				MUIElement removed = (MUIElement) event
						.getProperty(IUIEvents.EventTags.OldValue);
				// Removing invisible elements is a NO-OP as far as the
				// renderer is concerned
				if (!removed.isVisible())
					return;

				factory.hideChild(changedElement, removed);
			}
		}
	};

	private IEclipseContext appContext;

	protected Shell hackShell;

	protected MApplication theApp;

	public PartRenderingEngine(IEclipseContext context) {
		initialize(context);
	}

	/**
	 * Initialize a part renderer from the extension point.
	 * 
	 * @param registry
	 *            the registry for the EP
	 * @param r
	 *            the created renderer
	 * @param context
	 *            the context for the part factories
	 * @param f
	 *            the IContributionFactory already provided to <code>r</code>
	 */
	public void initialize(IEclipseContext context) {
		this.appContext = context;

		IExtensionRegistry registry = (IExtensionRegistry) context
				.get(IExtensionRegistry.class.getName());
		IConfigurationElement[] factories = registry
				.getConfigurationElementsFor("org.eclipse.e4.workbench.rendererfactory"); //$NON-NLS-1$
		for (int i = 0; i < factories.length; i++) {
			String id = factories[i].getAttribute("id");
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
		IEventBroker eventBroker = (IEventBroker) context
				.get(IEventBroker.class.getName());
		eventBroker.subscribe(IUIEvents.UIElement.Topic, visibilityHandler);
		eventBroker
				.subscribe(IUIEvents.ElementContainer.Topic, childrenHandler);
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
		if (!element.isVisible())
			return null;

		if (element instanceof MContext) {
			MContext ctxt = (MContext) element;
			// Assert.isTrue(ctxt.getContext() == null,
			// "Before rendering Context should be null");
			if (ctxt.getContext() == null) {
				IEclipseContext parentContext = element.getParent() == null ? appContext
						: getContext(element.getParent());
				IEclipseContext lclContext = EclipseContextFactory.create(
						parentContext, UISchedulerStrategy.getInstance());
				populateModelInterfaces(ctxt, lclContext, element.getClass()
						.getInterfaces());
				ctxt.setContext(lclContext);

				// make sure the context knows about these variables that have
				// been defined in the model
				for (String variable : ctxt.getVariables()) {
					lclContext.declareModifiable(variable);
				}

				Workbench.processHierarchy(element);
			}
		}

		// Create a control appropriate to the part
		Object newWidget = createWidget(element, parent);

		// Remember that we've created the control
		if (newWidget != null) {
			// Process its internal structure through the factory that created
			// it
			AbstractPartRenderer factory = getFactoryFor(element);

			factory.hookControllerLogic(element);

			if (element instanceof MElementContainer) {
				factory
						.processContents((MElementContainer<MUIElement>) element);
			}

			factory.postProcess(element);

			// Now that we have a widget let the parent (if any) know
			if (element.getParent() instanceof MUIElement) {
				MElementContainer<MUIElement> parentElement = element
						.getParent();
				AbstractPartRenderer parentFactory = getFactoryFor(parentElement);
				if (parentFactory != null)
					parentFactory.childRendered(parentElement, element);
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
			AbstractPartRenderer factory = getFactoryFor(parentME);
			if (factory != null) {
				parent = factory.getUIContainer(element);
			}
		}

		return createGui(element, parent);
	}

	/**
	 * @param element
	 */
	public void removeGui(MUIElement element) {
		AbstractPartRenderer factory = getFactoryFor(element);
		assert (factory != null);

		MUIElement parent = element.getParent();
		AbstractPartRenderer parentFactory = parent != null ? getFactoryFor(parent)
				: null;
		if (parentFactory != null) {
			parentFactory.hideChild(element.getParent(), element);
		}

		if (factory != null)
			factory.disposeWidget(element);
		else
			System.out.println("Null factory in removeGui");

		// dispose the context
		if (element instanceof MContext) {
			MContext ctxt = (MContext) element;
			IEclipseContext lclContext = ctxt.getContext();
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

	protected void setFactoryFor(MUIElement element,
			AbstractPartRenderer factory) {
		element.setFactory(factory);
	}

	protected AbstractPartRenderer getFactoryFor(MUIElement element) {
		return (AbstractPartRenderer) element.getFactory();
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
			final IEclipseContext appContext) {
		final Display display = Display.getDefault();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				String cssURI = (String) appContext
						.get(E4Workbench.CSS_URI_ARG);
				if (cssURI != null) {
					String cssResourcesURI = (String) appContext
							.get(E4Workbench.CSS_RESOURCE_URI_ARG);
					CSSStylingSupport.initializeStyling(display, cssURI,
							cssResourcesURI, appContext);
				} else {
					initializeNullStyling(appContext);
				}

				// Register an SWT resource handler
				appContext.set(IResourceUtiltities.class.getName(),
						new ResourceUtility(Activator.getDefault()
								.getBundleAdmin()));

				// set up the keybinding manager
				KeyBindingDispatcher dispatcher = new KeyBindingDispatcher();
				ContextInjectionFactory.inject(dispatcher, appContext);
				org.eclipse.swt.widgets.Listener listener = dispatcher
						.getKeyDownFilter();
				display.addFilter(SWT.KeyDown, listener);
				display.addFilter(SWT.Traverse, listener);

				// Show the initial UI

				// HACK!! we should loop until the display gets disposed...
				// ...then we listen for the last 'main' window to get disposed
				// and dispose the Display
				hackShell = null;
				theApp = null;
				boolean spinOnce = true;
				if (uiRoot instanceof MApplication) {
					spinOnce = false; // loop until the app closes
					theApp = (MApplication) uiRoot;
					for (MWindow window : theApp.getChildren()) {
						hackShell = (Shell) createGui(window);
					}
				} else if (uiRoot instanceof MUIElement) {
					if (uiRoot instanceof MWindow) {
						hackShell = (Shell) createGui((MUIElement) uiRoot);
					} else {
						// Special handling for partial models (for testing...)
						hackShell = new Shell(display, SWT.SHELL_TRIM);
						createGui((MUIElement) uiRoot, hackShell);
					}
				}

				// Spin the event loop until someone disposes the display
				while (!hackShell.isDisposed() && !display.isDisposed()) {
					try {
						if (!display.readAndDispatch()) {
							if (spinOnce)
								return;
							display.sleep();
						}
					} catch (ThreadDeath th) {
						throw th;
					} catch (Exception ex) {
						ex.printStackTrace();
					} catch (Error err) {
						err.printStackTrace();
					}
				}
			}
		});

		return IApplication.EXIT_OK;
	}

	public void stop() {
		if (theApp == null)
			return;

		for (MWindow window : theApp.getChildren()) {
			if (window.getWidget() != null) {
				((Shell) window.getWidget()).close();
			}
		}
	}
}
