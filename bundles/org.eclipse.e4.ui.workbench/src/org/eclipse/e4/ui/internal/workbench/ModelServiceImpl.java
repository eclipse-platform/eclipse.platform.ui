/*******************************************************************************
 * Copyright (c) 2010, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 434611, 472654
 *     Manumitting Technologies Inc - Bug 380609
 *     Stefan Nöbauer - Bug 547997
 *     Christoph Läubrich - Bug 538153
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.model.ModelXPathEvaluator;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.MBindingContext;
import org.eclipse.e4.ui.model.application.commands.MBindingTable;
import org.eclipse.e4.ui.model.application.commands.MCommand;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.commands.MKeyBinding;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MAdvancedFactory;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.model.internal.ModelUtils;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPlaceholderResolver;
import org.eclipse.e4.ui.workbench.modeling.ElementMatcher;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class ModelServiceImpl implements EModelService {

	static String HOSTED_ELEMENT = "HostedElement"; //$NON-NLS-1$

	private IEclipseContext appContext;

	/** Factory which is able to create {@link MApplicationElement}s in a generic way. */
	private GenericMApplicationElementFactoryImpl mApplicationElementFactory;

	private ServiceRegistration<?> handlerRegistration;

	/**
	 * This is a singleton service. One instance is used throughout the running
	 * application
	 *
	 * @param appContext The applicationContext to get the eventBroker from
	 *
	 * @throws NullPointerException if the given appContext is <code>null</code>
	 */
	@Inject
	public ModelServiceImpl(IEclipseContext appContext) {
		Objects.requireNonNull(appContext, "No application context given!"); //$NON-NLS-1$

		this.appContext = appContext;

		Bundle bundle = FrameworkUtil.getBundle(getClass());
		if (bundle != null) {
			BundleContext bundleContext = bundle.getBundleContext();
			if (bundleContext != null) {
				// register the event handler via whiteboard pattern
				Dictionary<String, Object> properties = new Hashtable<>();
				properties.put(EventConstants.EVENT_TOPIC, new String[] { UIEvents.UIElement.TOPIC_WIDGET });
				handlerRegistration = bundleContext.registerService(EventHandler.class.getName(),
						ContextInjectionFactory.make(HostedElementEventHandler.class, appContext), properties);
			}
		} else {
			// if we are not running in an OSGi environment, we try to use the IEventBroker
			IEventBroker eventBroker = appContext.get(IEventBroker.class);
			if (eventBroker == null) {
				throw new IllegalStateException(
						"Could not get an IEventBroker instance. Please check your configuration that a providing bundle is present and active."); //$NON-NLS-1$
			}
			// subscribe headless as we ensure that the handler is called in the UI thread
			// internally
			eventBroker.subscribe(UIEvents.UIElement.TOPIC_WIDGET, null,
					ContextInjectionFactory.make(HostedElementEventHandler.class, appContext), true);
		}

		mApplicationElementFactory = new GenericMApplicationElementFactoryImpl(
				appContext.get(IExtensionRegistry.class));
	}

	@PreDestroy
	void dispose() {
		if (handlerRegistration != null) {
			handlerRegistration.unregister();
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public final <T extends MApplicationElement> T createModelElement(Class<T> elementType) {
		if (elementType == null) {
			throw new NullPointerException("Argument cannot be null."); //$NON-NLS-1$
		}

		T back = (T) mApplicationElementFactory.createEObject(elementType);
		if (back != null) {
			return back;
		}

		throw new IllegalArgumentException(
				"Unsupported model object type: " + elementType.getCanonicalName()); //$NON-NLS-1$
	}

	@Override
	public void deleteModelElement(MUIElement element) {
		Objects.nonNull(element);
		element.setToBeRendered(false);
		if (element.getParent() != null) {
			element.getParent().getChildren().remove(element);
		}

	}

	private <T> void findElementsRecursive(MApplicationElement searchRoot, Class<T> clazz,
			Selector matcher, LinkedHashSet<T> elements, int searchFlags) {
		Assert.isLegal(searchRoot != null);
		if (searchFlags == 0) {
			return;
		}

		// are *we* a match ?
		boolean classMatch = clazz == null ? true : clazz.isInstance(searchRoot);
		if (classMatch && matcher.select(searchRoot)) {
			@SuppressWarnings("unchecked")
			T element = (T) searchRoot;
			elements.add(element);
		}
		if (searchRoot instanceof MApplication && (searchFlags == ANYWHERE)) {
			MApplication app = (MApplication) searchRoot;

			List<MApplicationElement> children = new ArrayList<>();
			if (clazz != null) {
				if (clazz.equals(MHandler.class)) {
					children.addAll(app.getHandlers());
				} else if (clazz.equals(MCommand.class)) {
					children.addAll(app.getCommands());
				} else if (clazz.equals(MBindingContext.class)) {
					children.addAll(app.getBindingContexts());
				} else if (clazz.equals(MBindingTable.class) || clazz.equals(MKeyBinding.class)) {
					children.addAll(app.getBindingTables());
				} else if (clazz.equals(MAddon.class)) {
					children.addAll(app.getAddons());
				}
				// } else { only look for these if specifically asked.
				// children.addAll(app.getHandlers());
				// children.addAll(app.getCommands());
				// children.addAll(app.getBindingContexts());
				// children.addAll(app.getBindingTables());
			}

			for (MApplicationElement child : children) {
				findElementsRecursive(child, clazz, matcher, elements, searchFlags);
			}
		}

		if (searchRoot instanceof MBindingContext && (searchFlags == ANYWHERE)) {
			MBindingContext bindingContext = (MBindingContext) searchRoot;
			for (MBindingContext child : bindingContext.getChildren()) {
				findElementsRecursive(child, clazz, matcher, elements, searchFlags);
			}
		}

		if (searchRoot instanceof MBindingTable) {
			MBindingTable bindingTable = (MBindingTable) searchRoot;
			for (MKeyBinding child : bindingTable.getBindings()) {
				findElementsRecursive(child, clazz, matcher, elements, searchFlags);
			}
		}

		// Check regular containers
		if (searchRoot instanceof MElementContainer<?>) {
			/*
			 * Bug 455281: If given a window with a primary perspective stack,
			 * and we're not told to look outside of the perspectives (i.e.,
			 * searchFlags is missing OUTSIDE_PERSPECTIVE), then just search the
			 * primary perspective stack instead. This ignores special areas
			 * like the compat layer's stack holding the Help, CheatSheets, and
			 * Intro.
			 */
			MElementContainer<?> searchContainer = (MElementContainer<?>) searchRoot;
			MPerspectiveStack primaryStack = null;
			if (searchRoot instanceof MWindow ) {
				if((searchFlags & IN_SHARED_ELEMENTS) != 0) {
					List<MUIElement> sharedElements = ((MWindow) searchRoot).getSharedElements();
					for (MUIElement muiElement : sharedElements) {
						findElementsRecursive(muiElement, clazz, matcher, elements, searchFlags);
					}
				}

				if( (searchFlags & OUTSIDE_PERSPECTIVE) == 0
					&& (primaryStack = getPrimaryPerspectiveStack((MWindow) searchRoot)) != null) {
				searchContainer = primaryStack;
				}
			}
			if (searchContainer instanceof MPerspectiveStack) {
				if ((searchFlags & IN_ANY_PERSPECTIVE) != 0) {
					// Search *all* the perspectives
					MElementContainer<? extends MUIElement> container = searchContainer;
					List<? extends MUIElement> children = container.getChildren();
					for (MUIElement child : children) {
						findElementsRecursive(child, clazz, matcher, elements, searchFlags);
					}
				} else if ((searchFlags & IN_ACTIVE_PERSPECTIVE) != 0) {
					// Only search the currently active perspective, if any
					MPerspective active = ((MPerspectiveStack) searchContainer).getSelectedElement();
					if (active != null) {
						findElementsRecursive(active, clazz, matcher, elements, searchFlags);
					}
				} else if ((searchFlags & IN_SHARED_AREA) != 0) {
					// Only recurse through the shared areas
					List<MArea> areas = findElements(searchContainer, null, MArea.class);
					for (MArea area : areas) {
						findElementsRecursive(area, clazz, matcher, elements, searchFlags);
					}
				}
			} else {
				@SuppressWarnings("unchecked")
				MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) searchRoot;
				List<MUIElement> children = container.getChildren();
				for (MUIElement child : children) {
					findElementsRecursive(child, clazz, matcher, elements, searchFlags);
				}
			}
		}

		// Search Trim
		if (searchRoot instanceof MTrimmedWindow && (searchFlags & IN_TRIM) != 0) {
			MTrimmedWindow tw = (MTrimmedWindow) searchRoot;
			List<MTrimBar> bars = tw.getTrimBars();
			for (MTrimBar bar : bars) {
				findElementsRecursive(bar, clazz, matcher, elements, searchFlags);
			}
		}

		// Search Detached Windows
		if (searchRoot instanceof MWindow) {
			MWindow window = (MWindow) searchRoot;
			for (MWindow dw : window.getWindows()) {
				findElementsRecursive(dw, clazz, matcher, elements, searchFlags);
			}

			MMenu menu = window.getMainMenu();
			if (menu != null && (searchFlags & IN_MAIN_MENU) != 0) {
				findElementsRecursive(menu, clazz, matcher, elements, searchFlags);
			}
			// Check for Handlers
			if (searchFlags == ANYWHERE && MHandler.class.equals(clazz)) {
				for (MHandler child : window.getHandlers()) {
					findElementsRecursive(child, clazz, matcher, elements, searchFlags);
				}
			}
		}

		if (searchRoot instanceof MPerspective) {
			MPerspective persp = (MPerspective) searchRoot;
			for (MWindow dw : persp.getWindows()) {
				findElementsRecursive(dw, clazz, matcher, elements, searchFlags);
			}
		}
		// Search shared elements
		if (searchRoot instanceof MPlaceholder) {
			MPlaceholder ph = (MPlaceholder) searchRoot;

			// Don't search in shared areas unless the flag is set
			if (ph.getRef() != null
					&& (!(ph.getRef() instanceof MArea) || (searchFlags & IN_SHARED_AREA) != 0)) {
				findElementsRecursive(ph.getRef(), clazz, matcher, elements, searchFlags);
			}
		}

		if (searchRoot instanceof MPart && (searchFlags & IN_PART) != 0) {
			MPart part = (MPart) searchRoot;

			for (MMenu menu : part.getMenus()) {
				findElementsRecursive(menu, clazz, matcher, elements, searchFlags);
			}

			MToolBar toolBar = part.getToolbar();
			if (toolBar != null) {
				findElementsRecursive(toolBar, clazz, matcher, elements, searchFlags);
			}
			if (MHandler.class.equals(clazz)) {
				for (MHandler child : part.getHandlers()) {
					findElementsRecursive(child, clazz, matcher, elements, searchFlags);
				}
			}
		}
	}

	/**
	 * If this window has a primary perspective stack, return it. Otherwise
	 * return null. A primary stack is a single MPerspectiveStack found either
	 * as the window's immediate children or the child under a single
	 * MPartSashContainer.
	 *
	 * @param window
	 *            the window
	 * @return the stack or {@code null}
	 */
	private MPerspectiveStack getPrimaryPerspectiveStack(MWindow window) {
		List<MWindowElement> winKids = window.getChildren();
		if (winKids.isEmpty()) {
			return null;
		}
		// Check if we have a MPerspectiveStack in window's children
		if (instanceCount(winKids, MPerspectiveStack.class) == 1) {
			return firstInstance(winKids, MPerspectiveStack.class);
		}
		// Traditional shape of IWorkbenchWindow:
		// MWindow{ MPartSashContainer{ MPerspectiveStack, MPartStack (intro
		// + cheatsheets + help)}}
		if (winKids.size() == 1 && winKids.get(0) instanceof MPartSashContainer) {
			MPartSashContainer topLevelPSC = (MPartSashContainer) winKids.get(0);
			if (instanceCount(topLevelPSC.getChildren(), MPerspectiveStack.class) == 1) {
				return firstInstance(topLevelPSC.getChildren(), MPerspectiveStack.class);
			}
		}
		return null;
	}

	/**
	 * Return the first element that is an instance of {@code clazz}
	 *
	 * @return the first element that is an instanceof {@code clazz} or null
	 */
	private <T> T firstInstance(Collection<? super T> elements, Class<T> clazz) {
		for (Object o : elements) {
			if (clazz.isInstance(o)) {
				return clazz.cast(o);
			}
		}
		return null;
	}

	/**
	 * Return the number of elements that are an instance of {@code clazz}.
	 *
	 * @param elements
	 *            the elements
	 * @param clazz
	 *            the class
	 * @return the number of elements that are an instance of {@code clazz}
	 */
	private int instanceCount(Collection<?> elements, Class<?> clazz) {
		int count = 0;
		for (Object o : elements) {
			if (clazz.isInstance(o)) {
				count++;
			}
		}
		return count;
	}

	@Override
	public <T> List<T> findElements(MUIElement searchRoot, String id, Class<T> clazz, List<String> tagsToMatch) {
		ElementMatcher matcher = new ElementMatcher(id, clazz, tagsToMatch);
		return findElements(searchRoot, clazz, ANYWHERE, matcher);
	}

	@Override
	public <T> List<T> findElements(MUIElement searchRoot, String id, Class<T> clazz) {
		return findElements(searchRoot, id, clazz, null);
	}

	@Override
	public <T> List<T> findElements(MUIElement searchRoot, String id, Class<T> clazz,
			List<String> tagsToMatch, int searchFlags) {
		ElementMatcher matcher = new ElementMatcher(id, clazz, tagsToMatch);
		return findElements(searchRoot, clazz, searchFlags, matcher);
	}

	@Override
	public <T> List<T> findElements(MApplicationElement searchRoot, Class<T> clazz,
			int searchFlags, Selector matcher) {
		LinkedHashSet<T> elements = new LinkedHashSet<>();
		findElementsRecursive(searchRoot, clazz, matcher, elements, searchFlags);
		return new ArrayList<>(elements);
	}

	@Override
	public <T> Stream<T> findMatchingElements(MApplicationElement searchRoot, String xPath, Class<T> clazz) {
		return ModelXPathEvaluator.findMatchingElements(searchRoot, xPath, clazz);
	}

	private <T> Iterable<T> findPerspectiveElements(MUIElement searchRoot, String id,
			Class<T> clazz,
			List<String> tagsToMatch) {
		LinkedHashSet<T> elements = new LinkedHashSet<>();
		ElementMatcher matcher = new ElementMatcher(id, clazz, tagsToMatch);
		findElementsRecursive(searchRoot, clazz, matcher, elements, PRESENTATION);
		return elements;
	}

	@Override
	public MUIElement find(String id, MUIElement searchRoot) {
		if (id == null || id.isEmpty()) {
			return null;
		}

		List<MUIElement> elements = findElements(searchRoot, id, MUIElement.class);
		if (!elements.isEmpty()) {
			return elements.get(0);
		}
		return null;
	}

	@Override
	public int countRenderableChildren(MUIElement element) {
		if (!(element instanceof MElementContainer<?>)) {
			return 0;
		}

		@SuppressWarnings("unchecked")
		MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) element;
		int count = 0;
		List<MUIElement> kids = container.getChildren();
		for (MUIElement kid : kids) {
			if (kid.isToBeRendered()) {
				count++;
			}
		}

		if (element instanceof MPerspective) {
			MPerspective perspective = (MPerspective) element;
			for (MWindow window : perspective.getWindows()) {
				if (window.isToBeRendered()) {
					count++;
				}
			}
		}
		return count;
	}

	@Override
	public IEclipseContext getContainingContext(MUIElement element) {
		return ModelUtils.getContainingContext(element);
	}

	@Override
	public MUIElement cloneElement(MUIElement element, MSnippetContainer snippetContainer) {
		EObject eObj = (EObject) element;
		MUIElement clone = (MUIElement) EcoreUtil.copy(eObj);

		// null out all the references
		List<MPlaceholder> phList = findElements(clone, null, MPlaceholder.class);
		for (MPlaceholder ph : phList) {
			// Skip placeholders in the sharedArea
			int location = getElementLocation(ph);
			if ((location & IN_SHARED_AREA) != 0) {
				continue;
			}

			ph.setRef(null);
		}

		if (snippetContainer != null) {
			MUIElement snippet = findSnippet(snippetContainer, element.getElementId());
			if (snippet != null) {
				snippetContainer.getSnippets().remove(snippet);
			}
			snippetContainer.getSnippets().add(clone);
		}

		// Cache the original element in the clone's transientData
		clone.getTransientData().put(CLONED_FROM_KEY, element);

		return clone;
	}

	@Override
	public MUIElement cloneSnippet(MSnippetContainer snippetContainer, String snippetId,
			MWindow refWin) {
		if (snippetContainer == null || snippetId == null || snippetId.isEmpty()) {
			return null;
		}

		MApplicationElement elementToClone = null;
		for (MApplicationElement snippet : snippetContainer.getSnippets()) {
			if (snippetId.equals(snippet.getElementId())) {
				elementToClone = snippet;
				break;
			}
		}
		if (elementToClone == null) {
			return null;
		}

		EObject eObj = (EObject) elementToClone;
		MUIElement element = (MUIElement) EcoreUtil.copy(eObj);

		MUIElement appElement = refWin == null ? null : refWin.getParent();
		if (appElement instanceof MApplication) {
			getNullRefPlaceHolders(element, refWin, true);
		}

		return element;
	}

	private List<MPlaceholder> getNullRefPlaceHolders(MUIElement element, MWindow refWin, boolean resolveAlways) {
		// use appContext as MApplication.getContext() is null during the processing of
		// the model processor classes
		EPlaceholderResolver resolver = appContext.get(EPlaceholderResolver.class);
		// Re-resolve any placeholder references
		List<MPlaceholder> phList = findElements(element, null, MPlaceholder.class);
		List<MPlaceholder> nullRefList = new ArrayList<>();
		for (MPlaceholder ph : phList) {
			if (resolveAlways) {
				resolver.resolvePlaceholderRef(ph, refWin);
			} else if ((!resolveAlways) && (ph.getRef() == null)) {
				resolver.resolvePlaceholderRef(ph, refWin);
				MUIElement partElement = ph.getRef();
				if (partElement instanceof MPart) {
					MPart part = (MPart) partElement;
					if (part.getIconURI() == null) {
						MPartDescriptor desc = getPartDescriptor(part.getElementId());
						if (desc != null) {
							part.setIconURI(desc.getIconURI());
						}
					}
				}
			}
			if (ph.getRef() == null) {
				nullRefList.add(ph);
			}
		}
		return nullRefList;
	}

	/**
	 * @return list of null referencing place holders
	 */
	public List<MPlaceholder> getNullRefPlaceHolders(MUIElement element, MWindow refWin) {
		return getNullRefPlaceHolders(element, refWin, false);
	}

	@Override
	public MUIElement findSnippet(MSnippetContainer snippetContainer, String id) {
		if (snippetContainer == null || id == null || id.isEmpty()) {
			return null;
		}

		List<MUIElement> snippets = snippetContainer.getSnippets();
		for (MUIElement snippet : snippets) {
			if (id.equals(snippet.getElementId())) {
				return snippet;
			}
		}

		return null;
	}

	@Override
	public void bringToTop(MUIElement element) {
		if (element instanceof MApplication) {
			return;
		}

		MWindow window = getTopLevelWindowFor(element);
		if (window == element) {
			if (!element.isToBeRendered()) {
				element.setToBeRendered(true);
			}

			window.getParent().setSelectedElement(window);
		} else {
			showElementInWindow(window, element);
		}
		UIEvents.publishEvent(UIEvents.UILifeCycle.BRINGTOTOP, element);
	}

	private void showElementInWindow(MWindow window, MUIElement element) {
		MUIElement parent = element.getParent();
		if (parent == null) {
			MPlaceholder ph = findPlaceholderFor(window, element);
			if (ph != null) {
				element = ph;
				parent = element.getParent();
			}
		}

		if (parent == null && element instanceof MWindow) {
			// no parent but is a window, could be a detached window then
			parent = (MUIElement) ((EObject) element).eContainer();
			if (parent != null) {
				// Force the element to be rendered
				if (!element.isToBeRendered()) {
					element.setToBeRendered(true);
				}

				if (window != parent) {
					showElementInWindow(window, parent);
				}
			}
		} else if (parent != null) {
			// Force the element to be rendered
			if (!element.isToBeRendered()) {
				element.setToBeRendered(true);
			}

			@SuppressWarnings("unchecked")
			MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) parent;
			container.setSelectedElement(element);
			if (window != parent) {
				showElementInWindow(window, parent);
			}
		}
	}

	@Override
	public MPlaceholder findPlaceholderFor(MWindow window, MUIElement element) {
		Iterable<MPlaceholder> phList = findPerspectiveElements(window, null, MPlaceholder.class, null);
		List<MPlaceholder> elementRefs = new ArrayList<>();
		for (MPlaceholder ph : phList) {
			if (ph.getRef() == element) {
				elementRefs.add(ph);
			}
		}

		if (elementRefs.isEmpty()) {
			return null;
		}

		if (elementRefs.size() == 1) {
			return elementRefs.get(0);
		}

		// If there is more than one placeholder then return the one in the shared area
		for (MPlaceholder refPh : elementRefs) {
			int loc = getElementLocation(refPh);
			if ((loc & IN_SHARED_AREA) != 0) {
				return refPh;
			}
		}

		// Just return the first one
		return elementRefs.get(0);
	}

	@Override
	public <T extends MUIElement> void move(T element, MElementContainer<? super T> newParent) {
		move(element, newParent, -1, false);
	}

	@Override
	public <T extends MUIElement> void move(T element, MElementContainer<? super T> newParent,
			boolean leavePlaceholder) {
		move(element, newParent, -1, leavePlaceholder);
	}

	@Override
	public <T extends MUIElement> void move(T element, MElementContainer<? super T> newParent, int index) {
		move(element, newParent, index, false);
	}

	@Override
	public <T extends MUIElement> void move(T element, MElementContainer<? super T> newParent, int index,
			boolean leavePlaceholder) {
		// Cache where we were
		MElementContainer<MUIElement> curParent = element.getParent();
		int curIndex = curParent.getChildren().indexOf(element);

		// Move the model element
		if (newParent == curParent) {
			int target = index != -1 ? index : newParent.getChildren().size() - 1;
			ECollections.move(newParent.getChildren(), target, element);
		} else if (index == -1) {
			newParent.getChildren().add(element);
		} else {
			newParent.getChildren().add(index, element);
		}

		if (leavePlaceholder) {
			MPlaceholder ph = MAdvancedFactory.INSTANCE.createPlaceholder();
			ph.setRef(element);
			curParent.getChildren().add(curIndex, ph);
		}
	}

	private void combine(MPartSashContainerElement toInsert, MPartSashContainerElement relTo,
			MPartSashContainer newSash, boolean newFirst, float ratio) {
		MElementContainer<MUIElement> curParent = relTo.getParent();
		if (curParent == null) {
			// if relTo is a shared element, use its current placeholder
			MWindow win = getTopLevelWindowFor(relTo);
			relTo = findPlaceholderFor(win, relTo);
			curParent = relTo.getParent();
		}
		Assert.isLegal(relTo != null && curParent != null);
		int index = curParent.getChildren().indexOf(relTo);
		curParent.getChildren().remove(relTo);
		if (newFirst) {
			newSash.getChildren().add(toInsert);
			newSash.getChildren().add(relTo);
		} else {
			newSash.getChildren().add(relTo);
			newSash.getChildren().add(toInsert);
		}

		// Set up the container data before adding the new sash to the model
		// To raise the granularity assume 100% == 10,000
		int adjustedPct = (int) (ratio * 10000);
		toInsert.setContainerData(Integer.toString(adjustedPct));
		relTo.setContainerData(Integer.toString(10000 - adjustedPct));

		// add the new sash at the same location
		curParent.getChildren().add(index, newSash);
	}

	@Override
	public void insert(MPartSashContainerElement toInsert, MPartSashContainerElement relTo,
			int where, float ratio) {
		assert (toInsert != null && relTo != null);
		if (ratio >= 1) {
			warn("EModelService#insert() expects the ratio to be between (0,100)"); //$NON-NLS-1$
			ratio = ratio / 100; // reduce it
		}
		assert(ratio > 0 && ratio < 1);

		// determine insertion order
		boolean insertBefore = where == ABOVE || where == LEFT_OF;
		boolean horizontal = where == LEFT_OF || where == RIGHT_OF;

		MPartSashContainer newSash = BasicFactoryImpl.eINSTANCE.createPartSashContainer();
		newSash.setHorizontal(horizontal);

		// Maintain the existing weight in the new sash
		newSash.setContainerData(relTo.getContainerData());

		combine(toInsert, relTo, newSash, insertBefore, ratio);
	}

	@Override
	public void detach(MPartSashContainerElement element, int x, int y, int width, int height) {
		// If we're showing through a placehoilder then detach it...
		if (element.getCurSharedRef() != null) {
			element = element.getCurSharedRef();
		}

		// Determine the correct parent for the new window
		MWindow window = getTopLevelWindowFor(element);
		MPerspective thePersp = getPerspectiveFor(element);

		MTrimmedWindow newWindow = MBasicFactory.INSTANCE.createTrimmedWindow();

		newWindow.setX(x);
		newWindow.setY(y);
		newWindow.setWidth(width);
		newWindow.setHeight(height);

		element.getParent().getChildren().remove(element);
		MWindowElement uiRoot = wrapElementForWindow(element);
		newWindow.getChildren().add(uiRoot);

		if (thePersp != null) {
			thePersp.getWindows().add(newWindow);
		} else if (window != null) {
			window.getWindows().add(newWindow);
		}
	}

	/**
	 * Wraps an element in a PartStack if it's a MPart or an MPlaceholder that references an MPart
	 *
	 * @param element
	 *            The element to be wrapped
	 * @return The wrapper for the given element
	 */
	private MWindowElement wrapElementForWindow(MPartSashContainerElement element) {
		if (element instanceof MPlaceholder) {
			MUIElement ref = ((MPlaceholder) element).getRef();
			if (ref instanceof MPart) {
				MPartStack newPS = MBasicFactory.INSTANCE.createPartStack();
				newPS.getChildren().add((MPlaceholder) element);
				return newPS;
			}
		} else if (element instanceof MPart) {
			MPartStack newPS = MBasicFactory.INSTANCE.createPartStack();
			newPS.getChildren().add((MPart) element);
			return newPS;
		} else if (element instanceof MWindowElement) {
			return (MWindowElement) element;
		}
		return null;
	}

	@Override
	public MTrimBar getTrim(MTrimmedWindow window, SideValue sv) {
		List<MTrimBar> bars = window.getTrimBars();
		for (MTrimBar bar : bars) {
			if (bar.getSide() == sv) {
				return bar;
			}
		}

		// Didn't find a trim bar for the side, make one
		MTrimBar newBar = BasicFactoryImpl.eINSTANCE.createTrimBar();

		// Assign default ids to the trim bars to match legacy eclipse
		if (sv == SideValue.TOP) {
			newBar.setElementId("org.eclipse.ui.main.menu"); //$NON-NLS-1$
		} else if (sv == SideValue.BOTTOM) {
			newBar.setElementId("org.eclipse.ui.trim.status"); //$NON-NLS-1$
		} else if (sv == SideValue.LEFT) {
			newBar.setElementId("org.eclipse.ui.trim.vertical1"); //$NON-NLS-1$
		} else if (sv == SideValue.RIGHT) {
			newBar.setElementId("org.eclipse.ui.trim.vertical2"); //$NON-NLS-1$
		}

		newBar.setSide(sv);
		window.getTrimBars().add(newBar);
		return newBar;
	}

	@Override
	public MWindow getTopLevelWindowFor(MUIElement element) {
		EObject eObj = (EObject) element;
		while (eObj != null && !(eObj.eContainer() instanceof MApplication)) {
			eObj = eObj.eContainer();
		}

		if (eObj instanceof MWindow) {
			return (MWindow) eObj;
		}

		return null; // Ooops!
	}

	@Override
	public MPerspective getPerspectiveFor(MUIElement element) {

		while (true) {
			// if we have a placeholder, reassign ourselves
			MPlaceholder placeholder = element.getCurSharedRef();
			if (placeholder != null) {
				element = placeholder;
			}
			EObject container = ((EObject) element).eContainer();
			if (container == null || container instanceof MApplication) {
				// climbed to the top and found nothing, return null
				return null;
			} else if (container instanceof MPerspectiveStack) {
				// parent is a perspective stack, we ourselves should be a perspective
				return (MPerspective) element;
			}

			// climb up
			element = (MUIElement) container;
		}
	}

	@Override
	public void resetPerspectiveModel(MPerspective persp, MWindow window) {
		resetPerspectiveModel(persp, window, true);
	}

	private void resetPerspectiveModel(MPerspective persp, MWindow window,
			boolean removeSharedPlaceholders) {
		if (persp == null) {
			return;
		}

		if (removeSharedPlaceholders) {
			// Remove any views (Placeholders) from the shared area
			EPartService ps = window.getContext().get(EPartService.class);
			List<MArea> areas = findElements(window, null, MArea.class, null);
			if (areas.size() == 1) {
				MArea area = areas.get(0);

				// Strip out the placeholders in visible stacks
				List<MPlaceholder> phList = findElements(area, null, MPlaceholder.class, null);
				for (MPlaceholder ph : phList) {
					ps.hidePart((MPart) ph.getRef());
					ph.getParent().getChildren().remove(ph);
				}

				// Prevent shared stacks ids from clashing with the ones in the perspective
				List<MPartStack> stacks = findElements(area, null, MPartStack.class, null);
				for (MPartStack stack : stacks) {
					String generatedId = "PartStack@" + Integer.toHexString(stack.hashCode()); //$NON-NLS-1$
					stack.setElementId(generatedId);
				}

				// Also remove any min/max tags on the area (or its placeholder)
				MUIElement areaPresentation = area;
				if (area.getCurSharedRef() != null) {
					areaPresentation = area.getCurSharedRef();
				}

				areaPresentation.getTags().remove(IPresentationEngine.MAXIMIZED);
				areaPresentation.getTags().remove(IPresentationEngine.MINIMIZED);
				areaPresentation.getTags().remove(IPresentationEngine.MINIMIZED_BY_ZOOM);
			}
		}

		// Remove any minimized stacks for this perspective
		String perspectiveSuffix = "(" + persp.getElementId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		List<MTrimBar> bars = findElements(window, null, MTrimBar.class, null);
		List<MToolControl> toRemove = new ArrayList<>();
		for (MTrimBar bar : bars) {
			for (MTrimElement barKid : bar.getChildren()) {
				if (!(barKid instanceof MToolControl)) {
					continue;
				}
				String id = barKid.getElementId();
				if (id != null && id.contains(perspectiveSuffix)) {
					toRemove.add((MToolControl) barKid);
				}
			}
		}

		for (MToolControl toolControl : toRemove) {
			// Close any open fast view
			toolControl.setToBeRendered(false);
			toolControl.getParent().getChildren().remove(toolControl);
		}
	}

	@Override
	public void removePerspectiveModel(MPerspective persp, MWindow window) {
		// pick a new perspective to become active (if any)
		MUIElement psElement = persp.getParent();
		if (psElement == null) {
			return;
		}
		MPerspectiveStack ps = (MPerspectiveStack) psElement;
		boolean foundNewSelection = false;
		if (ps.getSelectedElement() == persp) {
			for (MPerspective p : ps.getChildren()) {
				if (p != persp && p.isToBeRendered()) {
					ps.setSelectedElement(p);
					foundNewSelection = true;
					break;
				}
			}

			if (!foundNewSelection) {
				ps.setSelectedElement(null);
			}
		}

		// Remove transient elements (minimized stacks, detached windows)
		resetPerspectiveModel(persp, window, false);

		// unrender the perspective and remove it
		persp.setToBeRendered(false);
		ps.getChildren().remove(persp);
	}

	@Override
	public MPerspective getActivePerspective(MWindow window) {
		List<MPerspectiveStack> pStacks = findElements(window, null, MPerspectiveStack.class);
		if (pStacks.size() == 1) {
			return pStacks.get(0).getSelectedElement();
		}

		return null;
	}

	@Override
	public int toBeRenderedCount(MElementContainer<?> container) {
		int count = 0;
		for (MUIElement child : container.getChildren()) {
			if (child.isToBeRendered()) {
				count++;
			}
		}
		return count;
	}

	@Override
	public MUIElement getContainer(MUIElement element) {
		if (element == null) {
			return null;
		}

		return (MUIElement) ((EObject) element).eContainer();
	}

	@Override
	public int getElementLocation(MUIElement element) {
		if (element == null) {
			return NOT_IN_UI;
		}

		// If the element is shared then use its current placeholder
		if (element.getCurSharedRef() != null) {
			element = element.getCurSharedRef();
		}

		int location = NOT_IN_UI;
		MUIElement curElement = element;
		while (curElement != null) {
			Object container = ((EObject) curElement).eContainer();
			if (!(container instanceof MUIElement))
				return NOT_IN_UI;

			if (container instanceof MApplication) {
				if (location != NOT_IN_UI)
					return location;
				return OUTSIDE_PERSPECTIVE;
			} else if (container instanceof MPerspective) {
				MPerspective perspective = (MPerspective) container;
				MUIElement perspParent = perspective.getParent();
				if (perspParent == null) {
					location = NOT_IN_UI;
				} else if (perspective.getParent().getSelectedElement() == perspective) {
					location |= IN_ACTIVE_PERSPECTIVE;
				} else {
					location |= IN_ANY_PERSPECTIVE;
				}
			} else if (container instanceof MTrimBar) {
				location = IN_TRIM;
			} else if (container instanceof MArea) {
				location = IN_SHARED_AREA;
			}

			curElement = (MUIElement) container;
		}

		return NOT_IN_UI;
	}

	@Override
	public MPartDescriptor getPartDescriptor(String id) {
		MApplication application = appContext.get(MApplication.class);

		// If the id contains a ':' use the part before it as the descriptor id
		int colonIndex = id == null ? -1 : id.indexOf(':');
		String descId = colonIndex == -1 ? id : id.substring(0, colonIndex);

		for (MPartDescriptor descriptor : application.getDescriptors()) {
			if (descriptor.getElementId().equals(descId)) {
				return descriptor;
			}
		}
		return null;
	}

	@Override
	public MPart createPart(MPartDescriptor descriptor) {
		if (descriptor == null) {
			return null;
		}
		MPart part = createModelElement(MPart.class);
		part.setElementId(descriptor.getElementId());
		part.getMenus().addAll(EcoreUtil.copyAll(descriptor.getMenus()));
		if (descriptor.getToolbar() != null) {
			part.setToolbar((MToolBar) EcoreUtil.copy((EObject) descriptor.getToolbar()));
		}
		part.setContributorURI(descriptor.getContributorURI());
		part.setCloseable(descriptor.isCloseable());
		part.setContributionURI(descriptor.getContributionURI());
		part.setLabel(descriptor.getLabel());
		part.setIconURI(descriptor.getIconURI());
		part.setTooltip(descriptor.getTooltip());
		part.getHandlers().addAll(EcoreUtil.copyAll(descriptor.getHandlers()));
		part.getTags().addAll(descriptor.getTags());
		part.getVariables().addAll(descriptor.getVariables());
		part.getProperties().putAll(descriptor.getProperties());
		part.getPersistedState().putAll(descriptor.getPersistedState());
		part.getBindingContexts().addAll(descriptor.getBindingContexts());
		if (descriptor.getTrimBars() != null) {
			part.getTrimBars().addAll(EcoreUtil.copyAll(descriptor.getTrimBars()));
		}
		return part;
	}

	@Override
	public void hideLocalPlaceholders(MWindow window, MPerspective perspective) {
		List<MPlaceholder> globals = findElements(window, null, MPlaceholder.class, null,
				OUTSIDE_PERSPECTIVE | IN_SHARED_AREA);

		// Iterate across the perspective(s) removing any 'local' placeholders
		List<MPerspective> persps = new ArrayList<>();
		if (perspective != null) {
			persps.add(perspective);
		} else {
			persps = findElements(window, null, MPerspective.class);
		}

		for (MPerspective persp : persps) {
			List<MPlaceholder> locals = findElements(persp, null, MPlaceholder.class, null,
					IN_ANY_PERSPECTIVE);
			for (MPlaceholder local : locals) {
				for (MPlaceholder global : globals) {
					if (global.getRef() == local.getRef()) {
						local.setToBeRendered(false);
						MElementContainer<MUIElement> localParent = local.getParent();
						setStackVisibility(localParent);
					}
				}
			}
		}
	}

	private void setStackVisibility(MElementContainer<MUIElement> parent) {
		if (parent == null) {
			return;
		}
		for (MUIElement child : parent.getChildren()) {
			if (child.isToBeRendered() && child.isVisible()) {
				parent.setToBeRendered(true);
				return;
			}
		}
		parent.setToBeRendered(false);
		// continue modifying the visibility as the parent's parent may also
		// need to be hidden from the user
		setStackVisibility(parent.getParent());
	}

	@Override
	public boolean isLastEditorStack(MUIElement stack) {
		if (!(stack instanceof MPartStack)) {
			return false;
		}

		// is it in the shared area?
		MUIElement parent = stack.getParent();
		while (parent != null && !(parent instanceof MArea)) {
			parent = parent.getParent();
		}
		if (parent == null) {
			return false;
		}

		// OK, it's in the area, is it the last TBR one ?
		MArea area = (MArea) parent;
		List<MPartStack> stacks = findElements(area, null, MPartStack.class, null);
		int count = 0;
		for (MPartStack aStack : stacks) {
			if (aStack.isToBeRendered()) {
				count++;
			}
		}
		return count < 2 && stack.isToBeRendered();
	}

	@Override
	public void hostElement(MUIElement element, MWindow hostWindow, Object uiContainer,
			IEclipseContext hostContext) {
		// This is subtle; unless the element is hooked into the model it won't fire events
		hostWindow.getSharedElements().add(element);
		element.getTags().add(HOSTED_ELEMENT);

		IPresentationEngine renderer = hostWindow.getContext().get(IPresentationEngine.class);
		renderer.createGui(element, uiContainer, hostContext);
	}

	@Override
	public boolean isHostedElement(MUIElement element, MWindow hostWindow) {
		MUIElement curElement = element;
		while (curElement != null && !curElement.getTags().contains(HOSTED_ELEMENT)) {
			if (curElement.getCurSharedRef() != null) {
				curElement = curElement.getCurSharedRef();
			} else {
				curElement = curElement.getParent();
			}
		}

		if (curElement == null) {
			return false;
		}

		return hostWindow.getSharedElements().contains(curElement);
	}

	private void warn(String message) {
		Logger logger = appContext.get(Logger.class);
		if (logger != null) {
			logger.warn(message);
		}
	}

}
