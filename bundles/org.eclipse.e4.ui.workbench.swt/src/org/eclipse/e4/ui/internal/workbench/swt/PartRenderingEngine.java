/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.InjectionException;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.statusreporter.StatusReporter;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.e4.ui.css.core.util.impl.resources.OSGiResourceLocator;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelper;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.swt.factories.IRendererFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.bindings.keys.formatting.KeyFormatterFactory;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.PlatformAdmin;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.testing.TestableObject;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSStyleDeclaration;

public class PartRenderingEngine implements IPresentationEngine {
	public static final String EARLY_STARTUP_HOOK = "runEarlyStartup";

	public static final String engineURI = "bundleclass://org.eclipse.e4.ui.workbench.swt/"
			+ "org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine";

	private static final String defaultFactoryUrl = "bundleclass://org.eclipse.e4.ui.workbench.renderers.swt/"
			+ "org.eclipse.e4.ui.workbench.renderers.swt.WorkbenchRendererFactory";
	private String factoryUrl;

	IRendererFactory curFactory = null;

	private Map<String, AbstractPartRenderer> customRendererMap = new HashMap<String, AbstractPartRenderer>();

	org.eclipse.swt.widgets.Listener keyListener;

	// Life Cycle handlers
	private EventHandler toBeRenderedHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {

			MUIElement changedElement = (MUIElement) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			MElementContainer<?> parent = changedElement.getParent();

			// Handle Detached Windows
			if (parent == null) {
				parent = (MElementContainer<?>) ((EObject) changedElement)
						.eContainer();
			}

			// menus are not handled here... ??
			if (parent instanceof MMenu)
				return;

			// If the parent isn't visible we don't care (The application is
			// never rendered)
			boolean okToRender = parent instanceof MApplication
					|| parent.getWidget() != null;

			if (changedElement.isToBeRendered() && okToRender) {
				Activator.trace(Policy.DEBUG_RENDERER, "visible -> true", null); //$NON-NLS-1$

				// Note that the 'createGui' protocol calls 'childAdded'
				Object w = createGui(changedElement);
				if (w instanceof Control && !(w instanceof Shell)) {
					fixZOrder(changedElement);
				}
			} else {
				Activator
						.trace(Policy.DEBUG_RENDERER, "visible -> false", null); //$NON-NLS-1$

				// Ensure that the element about to be removed is not the
				// selected element
				if (parent.getSelectedElement() == changedElement)
					parent.setSelectedElement(null);

				if (okToRender) {
					// Un-maximize the element before tearing it down
					if (changedElement.getTags().contains(MAXIMIZED))
						changedElement.getTags().remove(MAXIMIZED);

					// Note that the 'removeGui' protocol calls 'childRemoved'
					removeGui(changedElement);
				}
			}

		}
	};

	private EventHandler visibilityHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			MUIElement changedElement = (MUIElement) event
					.getProperty(UIEvents.EventTags.ELEMENT);
			MUIElement parent = changedElement.getParent();
			if (parent == null) {
				parent = (MUIElement) ((EObject) changedElement).eContainer();
				if (parent == null) {
					return;
				}
			}

			AbstractPartRenderer renderer = (AbstractPartRenderer) parent
					.getRenderer();
			if (renderer == null || parent instanceof MToolBar)
				return;

			// Re-parent the control based on the visible state
			if (changedElement.isVisible()) {
				if (changedElement.isToBeRendered()) {
					if (changedElement.getWidget() instanceof Control) {
						// Ensure that the control is under its 'real' parent if
						// it's visible
						Composite realComp = (Composite) renderer
								.getUIContainer(changedElement);
						Control ctrl = (Control) changedElement.getWidget();
						ctrl.setParent(realComp);
						fixZOrder(changedElement);
					}

					if (parent instanceof MElementContainer<?>) {
						renderer.childRendered(
								(MElementContainer<MUIElement>) parent,
								changedElement);
					}
				}
			} else {
				// Put the control under the 'limbo' shell
				if (changedElement.getWidget() instanceof Control) {
					Control ctrl = (Control) changedElement.getWidget();

					if (!(ctrl instanceof Shell)) {
						ctrl.getShell().layout(new Control[] { ctrl },
								SWT.DEFER);
					}

					ctrl.setParent(getLimboShell());
				}

				if (parent instanceof MElementContainer<?>) {
					renderer.hideChild((MElementContainer<MUIElement>) parent,
							changedElement);
				}
			}
		}
	};

	private EventHandler trimHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (!(changedObj instanceof MTrimmedWindow))
				return;

			MTrimmedWindow window = (MTrimmedWindow) changedObj;
			if (window.getWidget() == null)
				return;

			if (UIEvents.isADD(event)) {
				for (Object o : UIEvents.asIterable(event,
						UIEvents.EventTags.NEW_VALUE)) {
					MUIElement added = (MUIElement) o;
					if (added.isToBeRendered())
						createGui(added, window.getWidget(),
								window.getContext());
				}
			} else if (UIEvents.isREMOVE(event)) {
				for (Object o : UIEvents.asIterable(event,
						UIEvents.EventTags.NEW_VALUE)) {
					MUIElement removed = (MUIElement) o;
					if (removed.getRenderer() != null)
						removeGui(removed);
				}
			}
		}
	};

	private EventHandler childrenHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {

			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (!(changedObj instanceof MElementContainer<?>))
				return;

			MElementContainer<MUIElement> changedElement = (MElementContainer<MUIElement>) changedObj;
			boolean isApplication = changedObj instanceof MApplication;

			boolean menuChild = changedObj instanceof MMenu;
			// If the parent isn't in the UI then who cares?
			AbstractPartRenderer renderer = getRendererFor(changedElement);
			if ((!isApplication && renderer == null) || menuChild)
				return;

			if (UIEvents.isADD(event)) {
				Activator.trace(Policy.DEBUG_RENDERER, "Child Added", null); //$NON-NLS-1$
				for (Object o : UIEvents.asIterable(event,
						UIEvents.EventTags.NEW_VALUE)) {
					MUIElement added = (MUIElement) o;

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
						Object w = createGui(added);
						if (w instanceof Control && !(w instanceof Shell)) {
							final Control ctrl = (Control) w;
							fixZOrder(added);
							if (!ctrl.isDisposed()) {
								ctrl.getShell().layout(new Control[] { ctrl },
										SWT.DEFER);
							}
						}
					} else {
						if (renderer != null && added.isToBeRendered())
							renderer.childRendered(changedElement, added);
					}

					// If the element being added is a placeholder, check to see
					// if
					// it's 'globally visible' and, if so, remove all other
					// 'local' placeholders referencing the same element.
					int newLocation = modelService.getElementLocation(added);
					if (newLocation == EModelService.IN_SHARED_AREA
							|| newLocation == EModelService.OUTSIDE_PERSPECTIVE) {
						MWindow topWin = modelService
								.getTopLevelWindowFor(added);
						modelService.hideLocalPlaceholders(topWin, null);
					}
				}
			} else if (UIEvents.isREMOVE(event)) {
				Activator.trace(Policy.DEBUG_RENDERER, "Child Removed", null); //$NON-NLS-1$
				for (Object o : UIEvents.asIterable(event,
						UIEvents.EventTags.OLD_VALUE)) {
					MUIElement removed = (MUIElement) o;
					// Removing invisible elements is a NO-OP as far as the
					// renderer is concerned
					if (!removed.isToBeRendered())
						continue;

					if (removed.getWidget() instanceof Control) {
						Control ctrl = (Control) removed.getWidget();
						ctrl.setLayoutData(null);
						ctrl.getParent().layout(new Control[] { ctrl },
								SWT.CHANGED | SWT.DEFER);
					}

					// Ensure that the element about to be removed is not the
					// selected element
					if (changedElement.getSelectedElement() == removed)
						changedElement.setSelectedElement(null);

					if (renderer != null)
						renderer.hideChild(changedElement, removed);
				}
			}
		}
	};

	private EventHandler windowsHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			childrenHandler.handleEvent(event);
		}
	};

	private StylingPreferencesHandler cssThemeChangedHandler;

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

	private Shell limbo;

	private MUIElement removeRoot = null;

	@Inject
	public PartRenderingEngine(
			@Named(E4Workbench.RENDERER_FACTORY_URI) @Optional String factoryUrl) {
		if (factoryUrl == null) {
			factoryUrl = defaultFactoryUrl;
		}
		this.factoryUrl = factoryUrl;
	}

	protected void fixZOrder(MUIElement element) {
		MElementContainer<MUIElement> parent = element.getParent();
		if (parent == null) {
			Object container = ((EObject) element).eContainer();
			if (container instanceof MElementContainer<?>) {
				parent = (MElementContainer<MUIElement>) container;
			}
		}
		if (parent == null || !(element.getWidget() instanceof Control))
			return;

		Control elementCtrl = (Control) element.getWidget();
		Control prevCtrl = null;
		for (MUIElement kid : parent.getChildren()) {
			if (kid == element) {
				if (prevCtrl != null)
					elementCtrl.moveBelow(prevCtrl);
				else
					elementCtrl.moveAbove(null);
				break;
			} else if (kid.getWidget() instanceof Control && kid.isVisible()) {
				prevCtrl = (Control) kid.getWidget();
			}
		}

		Object widget = parent.getWidget();
		if (widget instanceof Composite) {
			Composite composite = (Composite) widget;
			if (composite.getShell() == elementCtrl.getShell()) {
				Composite temp = elementCtrl.getParent();
				while (temp != composite) {
					if (temp == null) {
						return;
					}
					temp = temp.getParent();
				}

				composite.layout(true, true);
			}
		}
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
		context.set(IRendererFactory.class, curFactory);

		// Hook up the widget life-cycle subscriber
		if (eventBroker != null) {
			eventBroker.subscribe(UIEvents.UIElement.TOPIC_TOBERENDERED,
					toBeRenderedHandler);
			eventBroker.subscribe(UIEvents.UIElement.TOPIC_VISIBLE,
					visibilityHandler);
			eventBroker.subscribe(UIEvents.ElementContainer.TOPIC_CHILDREN,
					childrenHandler);
			eventBroker
					.subscribe(UIEvents.Window.TOPIC_WINDOWS, windowsHandler);
			eventBroker.subscribe(UIEvents.Perspective.TOPIC_WINDOWS,
					windowsHandler);
			eventBroker.subscribe(UIEvents.TrimmedWindow.TOPIC_TRIMBARS,
					trimHandler);

			cssThemeChangedHandler = new StylingPreferencesHandler(
					context.get(Display.class));
			eventBroker.subscribe(IThemeEngine.Events.THEME_CHANGED,
					cssThemeChangedHandler);
		}
	}

	@PreDestroy
	void contextDisposed() {
		if (eventBroker == null)
			return;
		eventBroker.unsubscribe(toBeRenderedHandler);
		eventBroker.unsubscribe(visibilityHandler);
		eventBroker.unsubscribe(childrenHandler);
		eventBroker.unsubscribe(trimHandler);
		eventBroker.unsubscribe(cssThemeChangedHandler);
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

	@Override
	public Object createGui(final MUIElement element,
			final Object parentWidget, final IEclipseContext parentContext) {
		final Object[] gui = { null };
		// wrap the handling in a SafeRunner so that exceptions do not prevent
		// the renderer from processing other elements
		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void handleException(Throwable e) {
				if (e instanceof Error) {
					// errors are deadly, we shouldn't ignore these
					throw (Error) e;
				}
				// log exceptions otherwise
				if (logger != null) {
					String message = "Exception occurred while rendering: {0}"; //$NON-NLS-1$
					logger.error(e, NLS.bind(message, element));
				}
			}

			@Override
			public void run() throws Exception {
				gui[0] = safeCreateGui(element, parentWidget, parentContext);
			}
		});
		return gui[0];
	}

	public Object safeCreateGui(MUIElement element, Object parentWidget,
			IEclipseContext parentContext) {
		if (!element.isToBeRendered())
			return null;

		// no creates while processing a remove
		if (removeRoot != null) {
			return null;
		}

		Object currentWidget = element.getWidget();
		if (currentWidget != null) {
			if (currentWidget instanceof Control) {
				Control control = (Control) currentWidget;
				// make sure the control is visible
				MUIElement elementParent = element.getParent();
				if (!(element instanceof MPlaceholder)
						|| !(elementParent instanceof MPartStack))
					control.setVisible(true);

				if (parentWidget instanceof Composite) {
					Composite currentParent = control.getParent();
					if (currentParent != parentWidget) {
						// check if the original parent was a tab folder
						if (currentParent instanceof CTabFolder) {
							CTabFolder folder = (CTabFolder) currentParent;
							// if we used to be the tab folder's top right
							// control, unset it
							if (folder.getTopRight() == control) {
								folder.setTopRight(null);
							}
						}

						// the parents are different so we should reparent it
						control.setParent((Composite) parentWidget);
					}
				}
			}

			// Reparent the context (or the kid's context)
			if (element instanceof MContext) {
				IEclipseContext ctxt = ((MContext) element).getContext();
				if (ctxt != null)
					ctxt.setParent(parentContext);
			} else {
				List<MContext> childContexts = modelService.findElements(
						element, null, MContext.class, null);
				for (MContext c : childContexts) {
					// Ensure that we only reset the context of our direct
					// children
					MUIElement kid = (MUIElement) c;
					MUIElement parent = kid.getParent();
					if (parent == null && kid.getCurSharedRef() != null)
						parent = kid.getCurSharedRef().getParent();
					if (!(element instanceof MPlaceholder) && parent != element)
						continue;

					if (c.getContext() != null
							&& c.getContext().getParent() != parentContext) {
						c.getContext().setParent(parentContext);
					}
				}
			}

			// Now that we have a widget let the parent (if any) know
			if (element.getParent() instanceof MUIElement) {
				MElementContainer<MUIElement> parentElement = element
						.getParent();
				AbstractPartRenderer parentRenderer = getRendererFor(parentElement);
				if (parentRenderer != null)
					parentRenderer.childRendered(parentElement, element);
			}
			return element.getWidget();
		}

		if (element instanceof MContext) {
			MContext ctxt = (MContext) element;
			// Assert.isTrue(ctxt.getContext() == null,
			// "Before rendering Context should be null");
			if (ctxt.getContext() == null) {
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
			}
		}

		// We check the widget again since it could be created by some UI event.
		// See Bug 417399
		if (element.getWidget() != null) {
			return safeCreateGui(element, parentWidget, parentContext);
		}

		// Create a control appropriate to the part
		Object newWidget = createWidget(element, parentWidget);

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
				if (lclContext != null) {
					lclContext.dispose();
					ctxt.setContext(null);
				}
			}
		}

		return newWidget;
	}

	private IEclipseContext getContext(MUIElement parent) {
		if (parent instanceof MContext) {
			return ((MContext) parent).getContext();
		}
		return modelService.getContainingContext(parent);
	}

	@Override
	public Object createGui(final MUIElement element) {
		final Object[] gui = { null };
		// wrap the handling in a SafeRunner so that exceptions do not prevent
		// the renderer from processing other elements
		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void handleException(Throwable e) {
				if (e instanceof Error) {
					// errors are deadly, we shouldn't ignore these
					throw (Error) e;
				}
				// log exceptions otherwise
				if (logger != null) {
					String message = "Exception occurred while rendering: {0}"; //$NON-NLS-1$
					logger.error(e, NLS.bind(message, element));
				}
			}

			@Override
			public void run() throws Exception {
				gui[0] = safeCreateGui(element);
			}
		});
		return gui[0];
	}

	private Object safeCreateGui(MUIElement element) {
		// Obtain the necessary parent widget
		Object parent = null;
		MUIElement parentME = element.getParent();
		if (parentME == null)
			parentME = (MUIElement) ((EObject) element).eContainer();
		if (parentME != null) {
			AbstractPartRenderer renderer = getRendererFor(parentME);
			if (renderer != null) {
				if (!element.isVisible()) {
					parent = getLimboShell();
				} else {
					parent = renderer.getUIContainer(element);
				}
			}
		}

		// Obtain the necessary parent context
		IEclipseContext parentContext = null;
		if (element.getCurSharedRef() != null) {
			MPlaceholder ph = element.getCurSharedRef();
			parentContext = getContext(ph.getParent());
		} else if (parentContext == null && element.getParent() != null) {
			parentContext = getContext(element.getParent());
		} else if (parentContext == null && element.getParent() == null) {
			parentContext = getContext((MUIElement) ((EObject) element)
					.eContainer());
		}

		return safeCreateGui(element, parent, parentContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.workbench.IPresentationEngine#focusGui(org.eclipse.
	 * e4.ui.model.application.ui.MUIElement)
	 */
	@Override
	public void focusGui(MUIElement element) {
		AbstractPartRenderer renderer = (AbstractPartRenderer) element
				.getRenderer();
		if (renderer == null || element.getWidget() == null)
			return;

		Object implementation = element instanceof MContribution ? ((MContribution) element)
				.getObject() : null;

		// If there is no class to call @Focus on then revert to the default
		if (implementation == null) {
			renderer.forceFocus(element);
			return;
		}

		try {
			IEclipseContext context = getContext(element);
			Object defaultValue = new Object();
			Object returnValue = ContextInjectionFactory.invoke(implementation,
					Focus.class, context, defaultValue);
			if (returnValue == defaultValue) {
				// No @Focus method, force the focus
				renderer.forceFocus(element);
			}
		} catch (InjectionException e) {
			log("Failed to grant focus to element", "Failed to grant focus to element ({0})", //$NON-NLS-1$ //$NON-NLS-2$
					element.getElementId(), e);
		} catch (RuntimeException e) {
			log("Failed to grant focus to element via DI", //$NON-NLS-1$
					"Failed to grant focus via DI to element ({0})", element.getElementId(), e); //$NON-NLS-1$
		}
	}

	private void log(String unidentifiedMessage, String identifiedMessage,
			String id, Exception e) {
		if (id == null || id.length() == 0) {
			logger.error(e, unidentifiedMessage);
		} else {
			logger.error(e, NLS.bind(identifiedMessage, id));
		}
	}

	private Shell getLimboShell() {
		if (limbo == null) {
			limbo = new Shell(Display.getCurrent(), SWT.NONE);
			limbo.setText("PartRenderingEngine's limbo"); //$NON-NLS-1$ // just for debugging, not shown anywhere

			// Place the limbo shell 'off screen'
			limbo.setLocation(0, 10000);

			limbo.setBackgroundMode(SWT.INHERIT_DEFAULT);
			limbo.setData(ShellActivationListener.DIALOG_IGNORE_KEY,
					Boolean.TRUE);
		}
		return limbo;
	}

	/**
	 * @param element
	 */
	@Override
	public void removeGui(final MUIElement element) {
		// wrap the handling in a SafeRunner so that exceptions do not prevent
		// the menu from being shown
		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void handleException(Throwable e) {
				if (e instanceof Error) {
					// errors are deadly, we shouldn't ignore these
					throw (Error) e;
				}
				// log exceptions otherwise
				if (logger != null) {
					String message = "Exception occurred while unrendering: {0}"; //$NON-NLS-1$
					logger.error(e, NLS.bind(message, element));
				}
			}

			@Override
			public void run() throws Exception {
				safeRemoveGui(element);
			}
		});
	}

	private void safeRemoveGui(MUIElement element) {
		if (removeRoot == null)
			removeRoot = element;

		// We call 'hideChild' *before* checking if the actual element
		// has been rendered in order to pick up cases of 'lazy loading'
		MUIElement parent = element.getParent();
		AbstractPartRenderer parentRenderer = parent != null ? getRendererFor(parent)
				: null;
		if (parentRenderer != null) {
			parentRenderer.hideChild(element.getParent(), element);
		}

		AbstractPartRenderer renderer = getRendererFor(element);

		// If the element hasn't been rendered then this is a NO-OP
		if (renderer != null) {

			if (element instanceof MElementContainer<?>) {
				MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) element;
				MUIElement selectedElement = container.getSelectedElement();
				List<MUIElement> children = container.getChildren();
				for (MUIElement child : children) {
					// remove stuff in the "back" first
					if (child != selectedElement) {
						removeGui(child);
					}
				}

				if (selectedElement != null
						&& children.contains(selectedElement)) {
					// now remove the selected element
					removeGui(selectedElement);
				}
			}

			if (element instanceof MPerspective) {
				MPerspective perspective = (MPerspective) element;
				for (MWindow subWindow : perspective.getWindows()) {
					removeGui(subWindow);
				}
			} else if (element instanceof MWindow) {
				MWindow window = (MWindow) element;
				for (MWindow subWindow : window.getWindows()) {
					removeGui(subWindow);
				}

				if (window instanceof MTrimmedWindow) {
					MTrimmedWindow trimmedWindow = (MTrimmedWindow) window;
					for (MUIElement trimBar : trimmedWindow.getTrimBars()) {
						removeGui(trimBar);
					}
				}
			}

			if (element instanceof MContribution) {
				MContribution contribution = (MContribution) element;
				Object client = contribution.getObject();
				IEclipseContext parentContext = renderer.getContext(element);
				if (parentContext != null && client != null) {
					try {
						ContextInjectionFactory.invoke(client,
								PersistState.class, parentContext, null);
					} catch (Exception e) {
						if (logger != null) {
							logger.error(e);
						}
					}
				}
			}

			renderer.disposeWidget(element);

			// unset the client object
			if (element instanceof MContribution) {
				MContribution contribution = (MContribution) element;
				Object client = contribution.getObject();
				IEclipseContext parentContext = renderer.getContext(element);
				if (parentContext != null && client != null) {
					try {
						ContextInjectionFactory.uninject(client, parentContext);
					} catch (Exception e) {
						if (logger != null) {
							logger.error(e);
						}
					}
				}
				contribution.setObject(null);
			}

			// dispose the context
			if (element instanceof MContext) {
				clearContext((MContext) element);
			}
		}

		if (removeRoot == element)
			removeRoot = null;
	}

	private void clearContext(MContext contextME) {
		MContext ctxt = (MContext) contextME;
		IEclipseContext lclContext = ctxt.getContext();
		if (lclContext != null) {
			IEclipseContext parentContext = lclContext.getParent();
			IEclipseContext child = parentContext != null ? parentContext
					.getActiveChild() : null;
			if (child == lclContext) {
				child.deactivate();
			}

			ctxt.setContext(null);
			lclContext.dispose();
		}
	}

	protected Object createWidget(MUIElement element, Object parent) {
		AbstractPartRenderer renderer = getRenderer(element, parent);
		if (renderer != null) {
			// Remember which renderer is responsible for this widget
			element.setRenderer(renderer);
			Object newWidget = renderer.createWidget(element, parent);
			if (newWidget != null) {
				renderer.bindWidget(element, newWidget);
				return newWidget;
			}
		}

		return null;
	}

	private AbstractPartRenderer getRenderer(MUIElement uiElement, Object parent) {
		// Is there a custom renderer defined ?
		String customURI = uiElement.getPersistedState().get(
				IPresentationEngine.CUSTOM_RENDERER_KEY);
		if (customURI != null) {
			if (customRendererMap.get(customURI) instanceof AbstractPartRenderer)
				return customRendererMap.get(customURI);

			IEclipseContext owningContext = modelService
					.getContainingContext(uiElement);
			IContributionFactory contributionFactory = (IContributionFactory) owningContext
					.get(IContributionFactory.class.getName());
			Object customRenderer = contributionFactory.create(customURI,
					owningContext);
			if (customRenderer instanceof AbstractPartRenderer) {
				customRendererMap.put(customURI,
						(AbstractPartRenderer) customRenderer);
				return (AbstractPartRenderer) customRenderer;
			}
		}

		// If not then use the default renderer
		return curFactory.getRenderer(uiElement, parent);
	}

	protected AbstractPartRenderer getRendererFor(MUIElement element) {
		return (AbstractPartRenderer) element.getRenderer();
	}

	@Override
	public Object run(final MApplicationElement uiRoot,
			final IEclipseContext runContext) {
		final Display display;
		if (runContext.get(Display.class) != null) {
			display = runContext.get(Display.class);
		} else {
			display = Display.getDefault();
			runContext.set(Display.class, display);
		}
		Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {

			@Override
			public void run() {
				initializeStyling(display, runContext);

				// Register an SWT resource handler
				runContext.set(IResourceUtilities.class.getName(),
						new ResourceUtility());

				// set up the keybinding manager
				KeyBindingDispatcher dispatcher = (KeyBindingDispatcher) ContextInjectionFactory
						.make(KeyBindingDispatcher.class, runContext);
				runContext.set(KeyBindingDispatcher.class.getName(), dispatcher);
				keyListener = dispatcher.getKeyDownFilter();
				display.addFilter(SWT.KeyDown, keyListener);
				display.addFilter(SWT.Traverse, keyListener);

				// Show the initial UI

				// Create a 'limbo' shell (used to host controls that shouldn't
				// be in the current layout)
				Shell limbo = getLimboShell();
				runContext.set("limbo", limbo);

				// HACK!! we should loop until the display gets disposed...
				// ...then we listen for the last 'main' window to get disposed
				// and dispose the Display
				testShell = null;
				theApp = null;
				boolean spinOnce = true;
				if (uiRoot instanceof MApplication) {
					ShellActivationListener shellDialogListener = new ShellActivationListener(
							(MApplication) uiRoot);
					display.addFilter(SWT.Activate, shellDialogListener);
					display.addFilter(SWT.Deactivate, shellDialogListener);
					spinOnce = false; // loop until the app closes
					theApp = (MApplication) uiRoot;
					// long startTime = System.currentTimeMillis();
					MWindow selected = theApp.getSelectedElement();
					if (selected == null) {
						for (MWindow window : theApp.getChildren()) {
							createGui(window);
						}
					} else {
						// render the selected one first
						createGui(selected);
						for (MWindow window : theApp.getChildren()) {
							if (selected != window) {
								createGui(window);
							}
						}
					}
					// long endTime = System.currentTimeMillis();
					// System.out.println("Render: " + (endTime - startTime));
					// tell the app context we are starting so the splash is
					// torn down
					IApplicationContext ac = appContext
							.get(IApplicationContext.class);
					if (ac != null) {
						ac.applicationRunning();
						if (eventBroker != null)
							eventBroker.post(
									UIEvents.UILifeCycle.APP_STARTUP_COMPLETE,
									theApp);
					}
				} else if (uiRoot instanceof MUIElement) {
					if (uiRoot instanceof MWindow) {
						testShell = (Shell) createGui((MUIElement) uiRoot);
					} else {
						// Special handling for partial models (for testing...)
						testShell = new Shell(display, SWT.SHELL_TRIM);
						createGui((MUIElement) uiRoot, testShell, null);
					}
				}

				// allow any early startup extensions to run
				Runnable earlyStartup = (Runnable) runContext
						.get(EARLY_STARTUP_HOOK);
				if (earlyStartup != null) {
					earlyStartup.run();
				}

				TestableObject testableObject = (TestableObject) runContext
						.get(TestableObject.class.getName());
				if (testableObject instanceof E4Testable) {
					((E4Testable) testableObject).init(display,
							(IWorkbench) runContext.get(IWorkbench.class
									.getName()));
				}

				IEventLoopAdvisor advisor = runContext.getActiveLeaf().get(
						IEventLoopAdvisor.class);
				if (advisor == null) {
					advisor = new IEventLoopAdvisor() {
						@Override
						public void eventLoopIdle(Display display) {
							display.sleep();
						}

						@Override
						public void eventLoopException(Throwable exception) {
							StatusReporter statusReporter = (StatusReporter) appContext
									.get(StatusReporter.class.getName());
							if (statusReporter != null) {
								statusReporter.show(StatusReporter.ERROR,
										"Internal Error", exception);
							} else {
								if (logger != null) {
									logger.error(exception);
								}
							}
						}
					};
				}
				// Spin the event loop until someone disposes the display
				while (((testShell != null && !testShell.isDisposed()) || (theApp != null && someAreVisible(theApp
						.getChildren()))) && !display.isDisposed()) {
					try {
						if (!display.readAndDispatch()) {
							runContext.processWaiting();
							if (spinOnce)
								return;
							advisor.eventLoopIdle(display);
						}
					} catch (ThreadDeath th) {
						throw th;
					} catch (Exception ex) {
						handle(ex, advisor);
					} catch (Error err) {
						handle(err, advisor);
					}
				}
				if (!spinOnce) {
					cleanUp();
				}
			}

			private void handle(Throwable ex, IEventLoopAdvisor advisor) {
				try {
					advisor.eventLoopException(ex);
				} catch (Throwable t) {
					if (t instanceof ThreadDeath) {
						throw (ThreadDeath) t;
					}

					// couldn't handle the exception, print to console
					t.printStackTrace();
				}
			}
		});

		return IApplication.EXIT_OK;
	}

	protected boolean someAreVisible(List<MWindow> windows) {
		// This method is called from the event dispatch loop, so the
		// following optimization is in order...

		// Ideally, we'd just do:
		// for (MWindow win : theApp.getChildren()) {
		// But this creates an iterator (which must be GC'd)
		// at every call. The code below creates no objects.
		final int limit = windows.size();
		for (int i = 0; i < limit; i++) {
			final MWindow win = windows.get(i);
			// Note: Removed isVisible test, as this should have
			// no impact on the whether the event loop
			// terminates - non-visible windows still exists
			// and can receive events.
			// Note: isToBeRendered() == true => win.getWidget() != null
			// but I'm not sure whether there is latency between setting
			// toBeRendered and the creation of the widget. So, keeping
			// both tests seems prudent.
			if (win.isToBeRendered() && win.getWidget() != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void stop() {
		// FIXME Without this call the test-suite fails
		cleanUp();
		if (theApp != null) {
			for (MWindow window : theApp.getChildren()) {
				if (window.getWidget() != null) {
					removeGui(window);
				}
			}
		} else if (testShell != null && !testShell.isDisposed()) {
			Object model = testShell.getData(AbstractPartRenderer.OWNING_ME);
			if (model instanceof MUIElement) {
				removeGui((MUIElement) model);
			} else {
				testShell.close();
			}
		}
	}

	/*
	 * There are situations where this is called more than once until we know
	 * why this is needed we should make this safe for multiple calls
	 */
	private void cleanUp() {
		if (keyListener != null) {
			Display display = Display.getDefault();
			if (!display.isDisposed()) {
				display.removeFilter(SWT.KeyDown, keyListener);
				display.removeFilter(SWT.Traverse, keyListener);
				keyListener = null;
			}
		}
	}

	public static void initializeStyling(Display display,
			IEclipseContext appContext) {
		String cssTheme = (String) appContext.get(E4Application.THEME_ID);
		String cssURI = (String) appContext.get(IWorkbench.CSS_URI_ARG);
		IThemeEngine themeEngineForEvent = null;
		if ("none".equals(cssTheme)) {
			appContext.set(IStylingEngine.SERVICE_NAME, new IStylingEngine() {
				@Override
				public void setClassname(Object widget, String classname) {
					WidgetElement.setCSSClass((Widget) widget, classname);
				}

				@Override
				public void setId(Object widget, String id) {
					WidgetElement.setID((Widget) widget, id);
				}

				@Override
				public void style(Object widget) {
				}

				@Override
				public CSSStyleDeclaration getStyle(Object widget) {
					return null;
				}

				@Override
				public void setClassnameAndId(Object widget, String classname,
						String id) {
					WidgetElement.setCSSClass((Widget) widget, classname);
					WidgetElement.setID((Widget) widget, id);
				}
			});
		} else if (cssTheme != null) {
			final IThemeEngine themeEngine = createThemeEngine(display,
					appContext);
			themeEngineForEvent = themeEngine;
			String cssResourcesURI = (String) appContext
					.get(IWorkbench.CSS_RESOURCE_URI_ARG);

			// Create the OSGi resource locator
			if (cssResourcesURI != null) {
				// TODO: Should this be set through an extension as well?
				themeEngine.registerResourceLocator(new OSGiResourceLocator(
						cssResourcesURI));
			}

			themeEngine.restore(cssTheme);

			appContext.set(IStylingEngine.SERVICE_NAME, new IStylingEngine() {
				@Override
				public void setClassname(Object widget, String classname) {
					WidgetElement.setCSSClass((Widget) widget, classname);
					themeEngine.applyStyles(widget, true);
				}

				@Override
				public void setId(Object widget, String id) {
					WidgetElement.setID((Widget) widget, id);
					themeEngine.applyStyles(widget, true);
				}

				@Override
				public void style(Object widget) {
					themeEngine.applyStyles(widget, true);
				}

				@Override
				public CSSStyleDeclaration getStyle(Object widget) {
					return themeEngine.getStyle(widget);
				}

				@Override
				public void setClassnameAndId(Object widget, String classname,
						String id) {
					WidgetElement.setCSSClass((Widget) widget, classname);
					WidgetElement.setID((Widget) widget, id);
					themeEngine.applyStyles(widget, true);
				}
			});
		} else if (cssURI != null) {
			String cssResourcesURI = (String) appContext
					.get(IWorkbench.CSS_RESOURCE_URI_ARG);
			final CSSSWTEngineImpl cssEngine = new CSSSWTEngineImpl(display,
					true);
			WidgetElement.setEngine(display, cssEngine);
			if (cssResourcesURI != null) {
				cssEngine.getResourcesLocatorManager().registerResourceLocator(
						new OSGiResourceLocator(cssResourcesURI.toString()));
			}
			// FIXME: is this needed?
			display.setData("org.eclipse.e4.ui.css.context", appContext); //$NON-NLS-1$
			appContext.set(IStylingEngine.SERVICE_NAME, new IStylingEngine() {
				@Override
				public void setClassname(Object widget, String classname) {
					WidgetElement.setCSSClass((Widget) widget, classname);
					cssEngine.applyStyles(widget, true);
				}

				@Override
				public void setId(Object widget, String id) {
					WidgetElement.setID((Widget) widget, id);
					cssEngine.applyStyles(widget, true);
				}

				@Override
				public void style(Object widget) {
					cssEngine.applyStyles(widget, true);
				}

				@Override
				public CSSStyleDeclaration getStyle(Object widget) {
					Element e = cssEngine.getCSSElementContext(widget)
							.getElement();
					if (e == null) {
						return null;
					}
					return cssEngine.getViewCSS().getComputedStyle(e, null);
				}

				@Override
				public void setClassnameAndId(Object widget, String classname,
						String id) {
					WidgetElement.setCSSClass((Widget) widget, classname);
					WidgetElement.setID((Widget) widget, id);
					cssEngine.applyStyles(widget, true);
				}
			});

			URL url;
			InputStream stream = null;
			try {
				url = FileLocator.resolve(new URL(cssURI));
				stream = url.openStream();
				cssEngine.parseStyleSheet(stream);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			Shell[] shells = display.getShells();
			for (Shell s : shells) {
				try {
					s.setRedraw(false);
					s.reskin(SWT.ALL);
					cssEngine.applyStyles(s, true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					s.setRedraw(true);
				}
			}
		}

		CSSRenderingUtils cssUtils = ContextInjectionFactory.make(
				CSSRenderingUtils.class, appContext);
		appContext.set(CSSRenderingUtils.class, cssUtils);

		IEventBroker broker = appContext.get(IEventBroker.class);
		if (broker != null) {
			Map<String, Object> data = null;
			if (themeEngineForEvent != null) {
				data = new HashMap<String, Object>();
				data.put(IThemeEngine.Events.THEME_ENGINE, themeEngineForEvent);
				data.put(IThemeEngine.Events.THEME,
						themeEngineForEvent.getActiveTheme());
				data.put(IThemeEngine.Events.DEVICE, display);
				data.put(IThemeEngine.Events.RESTORE, false);
			}
			broker.send(IThemeEngine.Events.THEME_CHANGED, data);
			broker.send(UIEvents.UILifeCycle.THEME_CHANGED, null);
		}
	}

	private static IThemeEngine createThemeEngine(Display display, IEclipseContext appContext) {
		// Store the app context
		IContributionFactory contribution = (IContributionFactory) appContext
				.get(IContributionFactory.class.getName());
		IEclipseContext cssContext = EclipseContextFactory.create();
		cssContext.set(IContributionFactory.class.getName(), contribution);
		display.setData("org.eclipse.e4.ui.css.context", cssContext); //$NON-NLS-1$

		IThemeManager mgr = appContext.get(IThemeManager.class);
		IThemeEngine themeEngine = mgr.getEngineForDisplay(display);

		appContext.set(IThemeEngine.class.getName(), themeEngine);
		return themeEngine;
	}

	public static class StylingPreferencesHandler implements EventHandler {
		private HashSet<IEclipsePreferences> prefs = null;

		public StylingPreferencesHandler(Display display) {
			if (display != null) {
				display.addListener(SWT.Dispose,
						createOnDisplayDisposedListener());
			}
		}

		protected Listener createOnDisplayDisposedListener() {
			return new Listener() {
					@Override
					public void handleEvent(org.eclipse.swt.widgets.Event event) {
						resetOverriddenPreferences();
					}
			};
		}

		@Override
		public void handleEvent(Event event) {
			resetOverriddenPreferences();
			overridePreferences(getThemeEngine(event));
		}

		protected void resetOverriddenPreferences() {
			for (IEclipsePreferences preferences : getPreferences()) {
				resetOverriddenPreferences(preferences);
			}
		}

		protected void resetOverriddenPreferences(
				IEclipsePreferences preferences) {
			for (String name : getOverriddenPropertyNames(preferences)) {
				preferences.remove(name);
			}
			removeOverriddenPropertyNames(preferences);
		}

		protected void removeOverriddenPropertyNames(
				IEclipsePreferences preferences) {
			EclipsePreferencesHelper.removeOverriddenPropertyNames(preferences);
		}

		protected List<String> getOverriddenPropertyNames(
				IEclipsePreferences preferences) {
			return EclipsePreferencesHelper
					.getOverriddenPropertyNames(preferences);
		}

		protected Set<IEclipsePreferences> getPreferences() {
			if (prefs == null) {
				prefs = new HashSet<IEclipsePreferences>();
				PlatformAdmin admin = WorkbenchSWTActivator.getDefault()
						.getPlatformAdmin();

				State state = admin.getState(false);
				BundleDescription[] bundles = state.getBundles();

				for (BundleDescription desc : bundles) {
					if (desc.getName() != null) {
						prefs.add(InstanceScope.INSTANCE.getNode(desc.getName()));
					}
				}
			}
			return prefs;
		}

		private void overridePreferences(IThemeEngine themeEngine) {
			if (themeEngine != null) {
				for (IEclipsePreferences preferences : getPreferences()) {
					themeEngine.applyStyles(preferences, false);
				}
			}
		}

		private IThemeEngine getThemeEngine(Event event) {
			return (IThemeEngine) event
					.getProperty(IThemeEngine.Events.THEME_ENGINE);
		}
	}
}
