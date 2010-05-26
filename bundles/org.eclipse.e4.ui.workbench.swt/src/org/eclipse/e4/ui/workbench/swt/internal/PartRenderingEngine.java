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

import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.e4.workbench.modeling.EModelService;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.testing.TestableObject;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class PartRenderingEngine implements IPresentationEngine {
	public static final String engineURI = "platform:/plugin/org.eclipse.e4.ui.workbench.swt/"
			+ "org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine";

	private static final String defaultFactoryUrl = "platform:/plugin/org.eclipse.e4.ui.workbench.renderers.swt/"
			+ "org.eclipse.e4.workbench.ui.renderers.swt.WorkbenchRendererFactory";
	private String factoryUrl;

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
	EModelService modelService;

	@Inject
	protected Logger logger;

	@Inject
	public PartRenderingEngine(
			@Named(E4Workbench.RENDERER_FACTORY_URI) @Optional String factoryUrl) {
		if (factoryUrl == null) {
			factoryUrl = defaultFactoryUrl;
		}
		this.factoryUrl = factoryUrl;
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

		// Add the renderer to the context
		context.set(IPresentationEngine.class.getName(), this);

		IRendererFactory factory = null;
		IContributionFactory contribFactory = context
				.get(IContributionFactory.class);
		try {
			factory = (IRendererFactory) contribFactory.create(factoryUrl,
					context);
		} catch (Exception e) {
			logger.warn(e, "Could not create rendering factory");
		}

		// Try to load the default one
		if (factory == null) {
			try {
				factory = (IRendererFactory) contribFactory.create(
						defaultFactoryUrl, context);
			} catch (Exception e) {
				logger.error(e, "Could not create default rendering factory");
			}
		}

		if (factory == null) {
			throw new IllegalStateException(
					"Could not create any rendering factory. Aborting ...");
		}

		curFactory = factory;

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
	void contextDisposed() {
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

	private String getContextName(MUIElement element) {
		StringBuilder builder = new StringBuilder(element.getClass()
				.getSimpleName());
		String elementId = element.getElementId();
		if (elementId != null && elementId.length() != 0) {
			builder.append(" (").append(elementId).append(") ");
		}
		builder.append("Context");
		return builder.toString();
	}

	public Object createGui(MUIElement element, Object parent) {
		if (!element.isToBeRendered())
			return null;

		if (element.getWidget() != null) {
			return element.getWidget();
		}

		if (element instanceof MContext) {
			MContext ctxt = (MContext) element;
			// Assert.isTrue(ctxt.getContext() == null,
			// "Before rendering Context should be null");
			if (ctxt.getContext() == null) {
				IEclipseContext parentContext = null;
				if (element.getCurSharedRef() != null) {
					MPlaceholder ph = element.getCurSharedRef();
					parentContext = getContext(ph.getParent());
				} else if (parentContext == null && element.getParent() != null) {
					parentContext = getContext(element.getParent());
				}
				if (parentContext == null)
					parentContext = appContext;
				IEclipseContext lclContext = parentContext
						.createChild(getContextName(element));
				populateModelInterfaces(ctxt, lclContext, element.getClass()
						.getInterfaces());
				ctxt.setContext(lclContext);

				// System.out.println("New Context: " + lclContext.toString()
				// + " parent: " + parentContext.toString());

				// make sure the context knows about these variables that have
				// been defined in the model
				for (String variable : ctxt.getVariables()) {
					lclContext.declareModifiable(variable);
				}

				Map<String, String> props = ctxt.getProperties();
				for (String key : props.keySet()) {
					lclContext.set(key, props.get(key));
				}

				E4Workbench.processHierarchy(element);
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
		if (parent instanceof MContext) {
			return ((MContext) parent).getContext();
		}
		return modelService.getContainingContext(parent);
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

		MUIElement parent = element.getParent();
		AbstractPartRenderer parentRenderer = parent != null ? getRendererFor(parent)
				: null;
		if (parentRenderer != null) {
			parentRenderer.hideChild(element.getParent(), element);
		}

		AbstractPartRenderer renderer = getRendererFor(element);
		if (renderer != null) {
			renderer.disposeWidget(element);
		}

		// unset the client object
		if (element instanceof MContribution) {
			((MContribution) element).setObject(null);
		}

		// dispose the context
		if (element instanceof MContext) {
			MContext ctxt = (MContext) element;
			IEclipseContext lclContext = ctxt.getContext();
			if (lclContext != null) {
				IEclipseContext parentContext = lclContext.getParent();
				Object child = parentContext
						.get(IContextConstants.ACTIVE_CHILD);
				if (child == lclContext) {
					parentContext.set(IContextConstants.ACTIVE_CHILD, null);
				}

				ctxt.setContext(null);
				if (lclContext instanceof IDisposable) {
					((IDisposable) lclContext).dispose();
				}
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

	public Object run(final MApplicationElement uiRoot,
			final IEclipseContext runContext) {
		final Display display = Display.getDefault();
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
			public void run() {
				String cssURI = (String) runContext
						.get(E4Workbench.CSS_URI_ARG);
				String cssResourcesURI = (String) runContext
						.get(E4Workbench.CSS_RESOURCE_URI_ARG);
				CSSStylingSupport.initializeStyling(display, cssURI,
						cssResourcesURI, runContext);

				// Register an SWT resource handler
				runContext.set(IResourceUtiltities.class.getName(),
						new ResourceUtility(Activator.getDefault()
								.getBundleAdmin()));

				// set up the keybinding manager
				KeyBindingDispatcher dispatcher = (KeyBindingDispatcher) ContextInjectionFactory
						.make(KeyBindingDispatcher.class, runContext);
				runContext.set(KeyBindingDispatcher.class.getName(), dispatcher);
				org.eclipse.swt.widgets.Listener listener = dispatcher
						.getKeyDownFilter();
				display.addFilter(SWT.KeyDown, listener);
				display.addFilter(SWT.Traverse, listener);

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
						createGui(window);
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
				while (((testShell != null && !testShell.isDisposed()) || (!theApp
						.getChildren().isEmpty() && someAreVisible(theApp
						.getChildren())))
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

	protected boolean someAreVisible(List<MWindow> windows) {
		for (MWindow win : windows) {
			if (win.isToBeRendered() && win.isVisible()
					&& win.getWidget() != null) {
				return true;
			}
		}
		return false;
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
