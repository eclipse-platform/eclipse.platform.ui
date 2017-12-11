/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christian Janz  - <christian.janz@gmail.com> Fix for Bug 385592
 *     Marc-Andre Laperle (Ericsson) - Fix for Bug 413590
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 431340, 431348, 426535, 433234, 431868, 472654
 *     Cornel Izbasa <cizbasa@info.uvt.ro> - Bug 442214
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 411639, 372799, 466230
 *     Dirk Fauth <dirk.fauth@googlemail.com> - Bug 473063
 *     Stefan Prieschl <stefan.prieschl@gmail.com> - Bug 374132
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.internal.workbench.ModelServiceImpl;
import org.eclipse.e4.ui.internal.workbench.PartServiceImpl;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MGenericStack;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.internal.provisional.action.ICoolBarManager2;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationHistory;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableEditor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.ISaveablesSource;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.MultiPartInitException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.Saveable;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.EditorSelectionDialog;
import org.eclipse.ui.internal.dialogs.cpd.CustomizePerspectiveDialog;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityPart;
import org.eclipse.ui.internal.e4.compatibility.CompatibilityView;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.e4.compatibility.SelectionService;
import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.internal.misc.ExternalEditor;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.misc.UIListenerLogging;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.eclipse.ui.internal.registry.UIExtensionTracker;
import org.eclipse.ui.internal.registry.ViewDescriptor;
import org.eclipse.ui.internal.tweaklets.TabBehaviour;
import org.eclipse.ui.internal.tweaklets.Tweaklets;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.IStickyViewDescriptor;
import org.eclipse.ui.views.IViewDescriptor;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * A collection of views and editors in a workbench.
 */
public class WorkbenchPage implements IWorkbenchPage {

	private static final String ATT_AGGREGATE_WORKING_SET_ID = "aggregateWorkingSetId"; //$NON-NLS-1$

	private static final int WINDOW_SCOPE = EModelService.OUTSIDE_PERSPECTIVE
			| EModelService.IN_ANY_PERSPECTIVE | EModelService.IN_SHARED_AREA;

	class E4PartListener implements org.eclipse.e4.ui.workbench.modeling.IPartListener {

		@Override
		public void partActivated(MPart part) {
			// update the workbench window's current selection with the active
			// part's selection
			IWorkbenchPart workbenchPart = getWorkbenchPart(part);
			selectionService.updateSelection(workbenchPart);

			updateActivations(part);
			firePartActivated(part);
			selectionService.notifyListeners(workbenchPart);
		}

		@Override
		public void partBroughtToTop(MPart part) {
			updateBroughtToTop(part);
			firePartBroughtToTop(part);
		}

		@Override
		public void partDeactivated(MPart part) {
			firePartDeactivated(part);

			Object client = part.getObject();
			if (client instanceof CompatibilityPart) {
				IWorkbenchPart workbenchPart = getWrappedPart((CompatibilityPart) client);
				if (workbenchPart == null) {
					return;
				}
				IWorkbenchPartSite site = workbenchPart.getSite();
				// if it's an editor, we only want to disable the actions
				((PartSite) site).deactivateActionBars(site instanceof ViewSite);
			}

			WorkbenchWindow wwindow = (WorkbenchWindow) getWorkbenchWindow();
			if (!wwindow.isClosing()) {
				wwindow.getStatusLineManager().update(false);
			}
		}

		@Override
		public void partHidden(MPart part) {
			firePartHidden(part);
		}

		@Override
		public void partVisible(MPart part) {
			firePartVisible(part);
		}
	}

	ArrayList<MPart> activationList = new ArrayList<>();

	/**
	 * Cached perspective stack for this workbench page.
	 */
	private MPerspectiveStack _perspectiveStack;

	/** Ids of parts used as Show In targets, maintained in MRU order */
	private List<String> mruShowInPartIds = new ArrayList<>();

	/**
	 * Deactivate the last editor's action bars if another type of editor has //
	 * * been activated.
	 *
	 * @param part
	 *            the part that is being activated
	 */
	private void deactivateLastEditor(MPart part) {
		Object client = part.getObject();
		// we only care if the currently activated part is an editor
		if (client instanceof CompatibilityEditor) {
			IWorkbenchPart activePart = getWrappedPart((CompatibilityEditor) client);
			if (activePart == null) {
				return;
			}
			String activeId = activePart.getSite().getId();

			// find another editor that was last activated
			for (MPart previouslyActive : activationList) {
				if (previouslyActive != part) {
					Object object = previouslyActive.getObject();
					if (object instanceof CompatibilityEditor) {
						IWorkbenchPart workbenchPart = getWrappedPart((CompatibilityEditor) object);
						if (workbenchPart == null) {
							continue;
						}
						EditorSite site = (EditorSite) workbenchPart.getSite();
						String lastId = site.getId();

						// if not the same, hide the other editor's action bars
						if (lastId != null && !lastId.equals(activeId)) {
							site.deactivateActionBars(true);
						}
						break;
					}
				}
			}
		}
	}

	private void updateActivations(MPart part) {
		if (activationList.size() > 1) {
			deactivateLastEditor(part);
		}

		activationList.remove(part);
		activationList.add(0, part);
		updateActivePartSources(part);
		updateActiveEditorSources(part);

		Object client = part.getObject();
		if (client instanceof CompatibilityPart) {
			IWorkbenchPart workbenchPart = getWrappedPart((CompatibilityPart) client);
			if (workbenchPart != null) {
				PartSite site = (PartSite) workbenchPart.getSite();
				site.activateActionBars(true);

				IActionBars actionBars = site.getActionBars();
				if (actionBars instanceof EditorActionBars) {
					((EditorActionBars) actionBars).partChanged(workbenchPart);
				}
			}
		}

		((WorkbenchWindow) getWorkbenchWindow()).getStatusLineManager().update(false);

		IWorkbenchPart workbenchPart = getWorkbenchPart(part);
		actionSwitcher.updateActivePart(workbenchPart);
	}

	private void updateActivePartSources(MPart part) {
		IWorkbenchPart workbenchPart = getWorkbenchPart(part);
		IContextService cs = legacyWindow.getService(IContextService.class);
		try {
			cs.deferUpdates(true);
			if (workbenchPart == null) {
				window.getContext().set(ISources.ACTIVE_PART_NAME, null);
				window.getContext().set(ISources.ACTIVE_PART_ID_NAME, null);
				window.getContext().set(ISources.ACTIVE_SITE_NAME, null);
			} else {
				window.getContext().set(ISources.ACTIVE_PART_NAME, workbenchPart);
				window.getContext().set(ISources.ACTIVE_PART_ID_NAME, workbenchPart.getSite().getId());
				window.getContext().set(ISources.ACTIVE_SITE_NAME, workbenchPart.getSite());
			}
		} finally {
			cs.deferUpdates(false);
		}
	}

	private void updateActionSets(Perspective oldPersp, Perspective newPersp) {
		// Update action sets

		IContextService service = legacyWindow.getService(IContextService.class);
		try {
			service.deferUpdates(true);
			if (newPersp != null) {
				List<IActionSetDescriptor> newAlwaysOn = newPersp.getAlwaysOnActionSets();
				for (int i = 0; i < newAlwaysOn.size(); i++) {
					IActionSetDescriptor descriptor = newAlwaysOn.get(i);

					actionSets.showAction(descriptor);
				}

				List<IActionSetDescriptor> newAlwaysOff = newPersp.getAlwaysOffActionSets();
				for (int i = 0; i < newAlwaysOff.size(); i++) {
					IActionSetDescriptor descriptor = newAlwaysOff.get(i);

					actionSets.maskAction(descriptor);
				}
			}

			if (oldPersp != null) {
				List<IActionSetDescriptor> oldAlwaysOn = oldPersp.getAlwaysOnActionSets();
				for (int i = 0; i < oldAlwaysOn.size(); i++) {
					IActionSetDescriptor descriptor = oldAlwaysOn.get(i);

					actionSets.hideAction(descriptor);
				}

				List<IActionSetDescriptor> oldAlwaysOff = oldPersp.getAlwaysOffActionSets();
				for (int i = 0; i < oldAlwaysOff.size(); i++) {
					IActionSetDescriptor descriptor = oldAlwaysOff.get(i);

					actionSets.unmaskAction(descriptor);
				}
			}
		} finally {
			service.deferUpdates(false);
		}
	}

	private IWorkbenchPart getWorkbenchPart(MPart part) {
		if (part != null) {
			Object clientObject = part.getObject();
			if (clientObject instanceof CompatibilityPart) {
				return ((CompatibilityPart) clientObject).getPart();
			} else if (clientObject != null) {
				if (part.getTransientData().get(E4PartWrapper.E4_WRAPPER_KEY) instanceof E4PartWrapper) {
					return (IWorkbenchPart) part.getTransientData().get(
							E4PartWrapper.E4_WRAPPER_KEY);
				}

				ViewReference viewReference = getViewReference(part);
				if (viewReference != null) {
					E4PartWrapper legacyPart = E4PartWrapper.getE4PartWrapper(part);
					try {
						viewReference.initialize(legacyPart);
					} catch (PartInitException e) {
						WorkbenchPlugin.log(e);
					}
					part.getTransientData().put(E4PartWrapper.E4_WRAPPER_KEY, legacyPart);
					return legacyPart;
				}
			}
		}
		return null;
	}

	private void updateActiveEditorSources(MPart part) {
		IEditorPart editor = getEditor(part);
		window.getContext().set(ISources.ACTIVE_EDITOR_ID_NAME,
				editor == null ? null : editor.getSite().getId());
		window.getContext().set(ISources.ACTIVE_EDITOR_NAME, editor);
		window.getContext().set(ISources.ACTIVE_EDITOR_INPUT_NAME,
				editor == null ? null : editor.getEditorInput());

		if (editor != null) {
			navigationHistory.markEditor(editor);
		}
		actionSwitcher.updateTopEditor(editor);
	}

	public void updateShowInSources(MPart part) {

		IWorkbenchPart workbenchPart = getWorkbenchPart(part);
		ShowInContext context = getContext(workbenchPart);
		if (context != null) {
			window.getContext().set(ISources.SHOW_IN_INPUT, context.getInput());
			window.getContext().set(ISources.SHOW_IN_SELECTION, context.getSelection());
		}
	}

	private IShowInSource getShowInSource(IWorkbenchPart sourcePart) {
		return Adapters.adapt(sourcePart, IShowInSource.class);
	}

	private ShowInContext getContext(IWorkbenchPart sourcePart) {
		IShowInSource source = getShowInSource(sourcePart);
		if (source != null) {
			ShowInContext context = source.getShowInContext();
			if (context != null) {
				return context;
			}
		} else if (sourcePart instanceof IEditorPart) {
			Object input = ((IEditorPart) sourcePart).getEditorInput();
			ISelectionProvider sp = sourcePart.getSite().getSelectionProvider();
			ISelection sel = sp == null ? null : sp.getSelection();
			return new ShowInContext(input, sel);
		}
		return null;
	}

	private IEditorPart getEditor(MPart part) {
		if (part != null) {
			Object clientObject = part.getObject();
			if (clientObject instanceof CompatibilityEditor) {
				return ((CompatibilityEditor) clientObject).getEditor();
			}
		}
		return getActiveEditor();
	}

	private void updateBroughtToTop(MPart part) {
		updateActiveEditorSources(part);
		IWorkbenchPart workbenchPart = getWorkbenchPart(part);
		if (workbenchPart instanceof IEditorPart) {
			navigationHistory.markEditor((IEditorPart) workbenchPart);
		}

		MElementContainer<?> parent = part.getParent();
		if (parent == null) {
			MPlaceholder placeholder = part.getCurSharedRef();
			if (placeholder == null) {
				return;
			}

			parent = placeholder.getParent();
		}

		if (parent instanceof MPartStack) {
			int newIndex = lastIndexOfContainer(parent);
			// New index can be -1 if there is no last index
			if (newIndex >= 0 && part == activationList.get(newIndex)) {
				return;
			}
			activationList.remove(part);
			if (newIndex >= 0 && newIndex < activationList.size() - 1) {
				activationList.add(newIndex, part);
			} else {
				activationList.add(part);
			}
		}
	}

	private int lastIndexOfContainer(MElementContainer<?> parent) {
		for (int i = 0; i < activationList.size(); i++) {
			MPart mPart = activationList.get(i);
			MElementContainer<MUIElement> container = mPart.getParent();
			if (container == parent) {
				return i;
			} else if (container == null) {
				MPlaceholder placeholder = mPart.getCurSharedRef();
				if (placeholder != null && placeholder.getParent() == parent) {
					return i;
				}
			}
		}
		return -1;
	}

	private List<ViewReference> viewReferences = new ArrayList<>();
	private List<EditorReference> editorReferences = new ArrayList<>();

	private List<IPerspectiveDescriptor> sortedPerspectives = new ArrayList<>();

	private ListenerList<IPartListener> partListenerList = new ListenerList<>();
	private ListenerList<IPartListener2> partListener2List = new ListenerList<>();

	/**
	 * A listener that forwards page change events to our part listeners.
	 */
	private IPageChangedListener pageChangedListener = new IPageChangedListener() {
		@Override
		public void pageChanged(final PageChangedEvent event) {
			for (final IPartListener2 listener : partListener2List) {
				if (listener instanceof IPageChangedListener) {
					SafeRunner.run(new SafeRunnable() {
						@Override
						public void run() throws Exception {
							((IPageChangedListener) listener).pageChanged(event);
						}
					});
				}
			}
		}
	};

	private E4PartListener e4PartListener = new E4PartListener();

	protected WorkbenchWindow legacyWindow;

    private IAdaptable input;

    private IWorkingSet workingSet;

    private AggregateWorkingSet aggregateWorkingSet;

    private Composite composite;

	private ListenerList<IPropertyChangeListener> propertyChangeListeners = new ListenerList<>();

    private IActionBars actionBars;

    private ActionSetManager actionSets;

    private NavigationHistory navigationHistory = new NavigationHistory(this);


    /**
     * If we're in the process of activating a part, this points to the new part.
     * Otherwise, this is null.
     */
    private IWorkbenchPartReference partBeingActivated = null;



    private IPropertyChangeListener workingSetPropertyChangeListener = new IPropertyChangeListener() {
        /*
         * Remove the working set from the page if the working set is deleted.
         */
        @Override
		public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
            if (IWorkingSetManager.CHANGE_WORKING_SET_REMOVE.equals(property)) {
            		if(event.getOldValue().equals(workingSet)) {
						setWorkingSet(null);
					}

            		// room for optimization here
				List<IWorkingSet> newList = new ArrayList<>(Arrays.asList(workingSets));
				if (newList.remove(event.getOldValue())) {
					setWorkingSets(newList.toArray(new IWorkingSet[newList.size()]));
				}
            }
        }
    };

	private ActionSwitcher actionSwitcher = new ActionSwitcher();

	private IExtensionTracker tracker;

    // Deferral count... delays disposing parts and sending certain events if nonzero
    private int deferCount = 0;


	private IWorkingSet[] workingSets = new IWorkingSet[0];
	private String aggregateWorkingSetId;

	// determines if a prompt is shown when opening large files
	private long maxFileSize = 0;
	private boolean checkDocumentSize;

	/**
	 * Manages editor contributions and action set part associations.
	 */
	private class ActionSwitcher {
		private IWorkbenchPart activePart;

		private IEditorPart topEditor;

		private List<IActionSetDescriptor> oldActionSets = new ArrayList<>();

		/**
		 * Updates the contributions given the new part as the active part.
		 *
		 * @param newPart
		 *            the new active part, may be <code>null</code>
		 */
		public void updateActivePart(IWorkbenchPart newPart) {
			if (activePart == newPart) {
				return;
			}

			boolean isNewPartAnEditor = newPart instanceof IEditorPart;
			if (isNewPartAnEditor) {
				String oldId = null;
				if (topEditor != null) {
					oldId = topEditor.getSite().getId();
				}
				String newId = newPart.getSite().getId();

				// if the active part is an editor and the new editor
				// is the same kind of editor, then we don't have to do
				// anything
				if (activePart == topEditor && newId.equals(oldId)) {
					activePart = newPart;
					topEditor = (IEditorPart) newPart;
					return;
				}

				// remove the contributions of the old editor
				// if it is a different kind of editor
				if (oldId != null && !oldId.equals(newId)) {
					deactivateContributions(topEditor, true);
				}

				// if a view was the active part, disable its contributions
				if (activePart != null && activePart != topEditor) {
					deactivateContributions(activePart, true);
				}

				// show (and enable) the contributions of the new editor
				// if it is a different kind of editor or if the
				// old active part was a view
				if (!newId.equals(oldId) || activePart != topEditor) {
					activateContributions(newPart, true);
				}

			} else if (newPart == null) {
				if (activePart != null) {
					// remove all contributions
					deactivateContributions(activePart, true);
				}
			} else {
				// new part is a view

				// if old active part is a view, remove all contributions,
				// but if old part is an editor only disable
				if (activePart != null) {
					deactivateContributions(activePart, activePart instanceof IViewPart);
				}

				activateContributions(newPart, true);
			}

			List<IActionSetDescriptor> newActionSets = null;
			if (isNewPartAnEditor || (activePart == topEditor && newPart == null)) {
				newActionSets = calculateActionSets(newPart, null);
			} else {
				newActionSets = calculateActionSets(newPart, topEditor);
			}

			if (!updateActionSets(newActionSets)) {
				updateActionBars();
			}

			if (isNewPartAnEditor) {
				topEditor = (IEditorPart) newPart;
			} else if (activePart == topEditor && newPart == null) {
				// since we removed all the contributions, we clear the top
				// editor
				topEditor = null;
			}

			activePart = newPart;
		}

		/**
		 * Updates the contributions given the new part as the topEditor.
		 *
		 * @param newEditor
		 *            the new top editor, may be <code>null</code>
		 */
		public void updateTopEditor(IEditorPart newEditor) {
			if (topEditor == newEditor) {
				return;
			}

			if (activePart == topEditor) {
				updateActivePart(newEditor);
				return;
			}

			String oldId = null;
			if (topEditor != null) {
				oldId = topEditor.getSite().getId();
			}
			String newId = null;
			if (newEditor != null) {
				newId = newEditor.getSite().getId();
			}
			if (oldId == null ? newId == null : oldId.equals(newId)) {
				// we don't have to change anything
				topEditor = newEditor;
				return;
			}

			// Remove the contributions of the old editor
			if (topEditor != null) {
				deactivateContributions(topEditor, true);
			}

			// Show (disabled) the contributions of the new editor
			if (newEditor != null) {
				activateContributions(newEditor, false);
			}

			List<IActionSetDescriptor> newActionSets = calculateActionSets(activePart, newEditor);
			if (!updateActionSets(newActionSets)) {
				updateActionBars();
			}

			topEditor = newEditor;
		}

		/**
		 * Activates the contributions of the given part. If <code>enable</code>
		 * is <code>true</code> the contributions are visible and enabled,
		 * otherwise they are disabled.
		 *
		 * @param part
		 *            the part whose contributions are to be activated
		 * @param enable
		 *            <code>true</code> the contributions are to be enabled, not
		 *            just visible.
		 */
		private void activateContributions(IWorkbenchPart part, boolean enable) {
			PartSite site = (PartSite) part.getSite();
			site.activateActionBars(enable);
		}

		/**
		 * Deactivates the contributions of the given part. If
		 * <code>remove</code> is <code>true</code> the contributions are
		 * removed, otherwise they are disabled.
		 *
		 * @param part
		 *            the part whose contributions are to be deactivated
		 * @param remove
		 *            <code>true</code> the contributions are to be removed, not
		 *            just disabled.
		 */
		private void deactivateContributions(IWorkbenchPart part, boolean remove) {
			PartSite site = (PartSite) part.getSite();
			site.deactivateActionBars(remove);
		}

		/**
		 * Calculates the action sets to show for the given part and editor
		 *
		 * @param part
		 *            the active part, may be <code>null</code>
		 * @param editor
		 *            the current editor, may be <code>null</code>, may be the
		 *            active part
		 * @return the new action sets
		 */
		private List<IActionSetDescriptor> calculateActionSets(IWorkbenchPart part,
				IEditorPart editor) {
			List<IActionSetDescriptor> newActionSets = new ArrayList<>();
			if (part != null) {
				IActionSetDescriptor[] partActionSets = WorkbenchPlugin.getDefault()
						.getActionSetRegistry().getActionSetsFor(part.getSite().getId());
				for (IActionSetDescriptor partActionSetDescriptor : partActionSets) {
					newActionSets.add(partActionSetDescriptor);
				}
			}
			if (editor != null && editor != part) {
				IActionSetDescriptor[] editorActionSets = WorkbenchPlugin.getDefault()
						.getActionSetRegistry().getActionSetsFor(editor.getSite().getId());
				for (IActionSetDescriptor editorActionSetDescriptor : editorActionSets) {
					newActionSets.add(editorActionSetDescriptor);
				}
			}
			return newActionSets;
		}

		/**
		 * Updates the actions we are showing for the active part and current
		 * editor.
		 *
		 * @param newActionSets
		 *            the action sets to show
		 * @return <code>true</code> if the action sets changed
		 */
		private boolean updateActionSets(List<IActionSetDescriptor> newActionSets) {
			if (oldActionSets.equals(newActionSets)) {
				return false;
			}

			IContextService service = legacyWindow
					.getService(IContextService.class);
			try {
				service.deferUpdates(true);

				// show the new
				for (int i = 0; i < newActionSets.size(); i++) {
					actionSets.showAction(newActionSets.get(i));
				}

				// hide the old
				for (int i = 0; i < oldActionSets.size(); i++) {
					actionSets.hideAction(oldActionSets.get(i));
				}

				oldActionSets = newActionSets;

			} finally {
				service.deferUpdates(false);
			}
			Perspective persp = getActivePerspective();
			if (persp == null) {
				return false;
			}

			legacyWindow.updateActionSets(); // this calls updateActionBars
			legacyWindow.firePerspectiveChanged(WorkbenchPage.this, getPerspective(),
					CHANGE_ACTION_SET_SHOW);
			return true;
		}

	}

	private EPartService partService;

	private SelectionService selectionService;

	private MApplication application;

	private MWindow window;

	private EModelService modelService;

	private IEventBroker broker;

	/**
	 * An event handler that listens for an MArea's widget being set so that we
	 * can install DND support into its control.
	 */
	private EventHandler widgetHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
			Object newValue = event.getProperty(UIEvents.EventTags.NEW_VALUE);

			if (element instanceof MArea) {
				// If it's an MArea in this window install the DND handling
				if (modelService.findElements(window, null, MArea.class, null).contains(element)) {
					if (newValue instanceof Control) {
						installAreaDropSupport((Control) newValue);
					}
				}
			} else if (element instanceof MPart && newValue == null) {
				// If it's a 'e4' part then remove the reference for it
				MPart changedPart = (MPart) element;
				Object impl = changedPart.getObject();
				if (impl != null && !(impl instanceof CompatibilityPart)) {
					EditorReference eRef = getEditorReference(changedPart);
					if (eRef != null)
						editorReferences.remove(eRef);
					ViewReference vRef = getViewReference(changedPart);
					if (vRef != null)
						viewReferences.remove(vRef);
				}
			}
		}
	};

	@Inject
	@Optional
	private void handleMinimizedStacks(
			@UIEventTopic(UIEvents.ApplicationElement.TOPIC_TAGS) Event event) {
		Object changedObj = event.getProperty(EventTags.ELEMENT);

		if (!(changedObj instanceof MToolControl))
			return;

		final MToolControl minimizedStack = (MToolControl) changedObj;

		// Note: The non-API type TrimStack is not imported to avoid
		// https://bugs.eclipse.org/435521
		if (!(minimizedStack.getObject() instanceof org.eclipse.e4.ui.workbench.addons.minmax.TrimStack))
			return;

		org.eclipse.e4.ui.workbench.addons.minmax.TrimStack ts = (org.eclipse.e4.ui.workbench.addons.minmax.TrimStack) minimizedStack
				.getObject();
		if (!(ts.getMinimizedElement() instanceof MPartStack))
			return;

		MPartStack stack = (MPartStack) ts.getMinimizedElement();
		MUIElement stackSel = stack.getSelectedElement();
		MPart thePart = null;
		if (stackSel instanceof MPart) {
			thePart = (MPart) stackSel;
		} else if (stackSel instanceof MPlaceholder) {
			MPlaceholder ph = (MPlaceholder) stackSel;
			if (ph.getRef() instanceof MPart) {
				thePart = (MPart) ph.getRef();
			}
		}

		if (thePart == null)
			return;

		if (UIEvents.isADD(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.NEW_VALUE,
					org.eclipse.e4.ui.workbench.addons.minmax.TrimStack.MINIMIZED_AND_SHOWING)) {
				firePartVisible(thePart);
			}
		} else if (UIEvents.isREMOVE(event)) {
			if (UIEvents.contains(event, UIEvents.EventTags.OLD_VALUE,
					org.eclipse.e4.ui.workbench.addons.minmax.TrimStack.MINIMIZED_AND_SHOWING)) {
				firePartHidden(thePart);
			}
		}
	}

	/**
	 * Boolean field to determine whether DND support has been added to the
	 * shared area yet.
	 *
	 * @see #installAreaDropSupport(Control)
	 */
	private boolean dndSupportInstalled = false;

    /**
     * Constructs a page. <code>restoreState(IMemento)</code> should be
     * called to restore this page from data stored in a persistance file.
     *
     * @param w
     *            the parent window
     * @param input
     *            the page input
     * @throws WorkbenchException
     */
    public WorkbenchPage(WorkbenchWindow w, IAdaptable input)
            throws WorkbenchException {
        super();
        init(w, null, input, false);
    }

    /**
     * Allow access to the UI model that this page is managing
     * @return the MWindow element for this page
     */
    public MWindow getWindowModel() {
    	return window;

    }
    /**
     * Activates a part. The part will be brought to the front and given focus.
     *
     * @param part
     *            the part to activate
     */
    @Override
	public void activate(IWorkbenchPart part) {
		if (part == null || !certifyPart(part) || legacyWindow.isClosing()) {
			return;
		}
		MPart mpart = findPart(part);
		if (mpart != null) {
			partService.activate(mpart);
			actionSwitcher.updateActivePart(part);
		}
	}

    /**
	 * Adds an IPartListener to the part service.
	 */
    @Override
	public void addPartListener(IPartListener l) {
		partListenerList.add(l);
    }

	/**
	 * Adds an IPartListener to the part service.
	 */
    @Override
	public void addPartListener(IPartListener2 l) {
		partListener2List.add(l);
    }

    /**
     * Implements IWorkbenchPage
     *
     * @see org.eclipse.ui.IWorkbenchPage#addPropertyChangeListener(IPropertyChangeListener)
     * @since 2.0
     * @deprecated individual views should store a working set if needed and
     *             register a property change listener directly with the
     *             working set manager to receive notification when the view
     *             working set is removed.
     */
    @Deprecated
	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
        propertyChangeListeners.add(listener);
    }

    @Override
	public void addSelectionListener(ISelectionListener listener) {
		selectionService.addSelectionListener(listener);
    }

    @Override
	public void addSelectionListener(String partId, ISelectionListener listener) {
		selectionService.addSelectionListener(partId, listener);
    }

    @Override
	public void addPostSelectionListener(ISelectionListener listener) {
		selectionService.addPostSelectionListener(listener);
    }

    @Override
	public void addPostSelectionListener(String partId,
            ISelectionListener listener) {
		selectionService.addPostSelectionListener(partId, listener);
    }

    /**
     * Moves a part forward in the Z order of a perspective so it is visible.
     * If the part is in the same stack as the active part, the new part is
     * activated.
     *
     * @param part
     *            the part to bring to move forward
     */
    @Override
	public void bringToTop(IWorkbenchPart part) {
        // Sanity check.
		MPart mpart = findPart(part);
		if (mpart != null) {
			partService.bringToTop(mpart);
		}
    }

	public MPart findPart(IWorkbenchPart part) {
		if (part == null) {
			return null;
		}

		for (IViewReference reference : viewReferences) {
			if (part == reference.getPart(false)) {
				return ((WorkbenchPartReference) reference).getModel();
			}
		}

		for (IEditorReference reference : editorReferences) {
			if (part == reference.getPart(false)) {
				return ((WorkbenchPartReference) reference).getModel();
			}
		}
		return null;
	}

	public EditorReference createEditorReferenceForPart(final MPart part, IEditorInput input,
			String editorId,
			IMemento memento) {
		IEditorRegistry registry = legacyWindow.getWorkbench().getEditorRegistry();
		EditorDescriptor descriptor = (EditorDescriptor) registry.findEditor(editorId);
		final EditorReference ref = new EditorReference(window.getContext(), this, part, input,
				descriptor, memento);
		addEditorReference(ref);
		ref.subscribe();
		return ref;
	}

	private List<EditorReference> getOrderedEditorReferences() {

		List<EditorReference> editorRefs = new ArrayList<>();
		List<MPart> visibleEditors = modelService.findElements(window,
				CompatibilityEditor.MODEL_ELEMENT_ID, MPart.class, null);
		for (MPart editor : visibleEditors) {
			if (editor.isToBeRendered()) {
				EditorReference ref = getEditorReference(editor);
				if (ref != null && !editorRefs.contains(ref)) {
					editorRefs.add(ref);
				}
			}
		}

		return editorRefs;
	}

	List<EditorReference> getSortedEditorReferences() {
		return getSortedEditorReferences(false);
	}

	private List<EditorReference> getSortedEditorReferences(boolean allPerspectives) {
		List<EditorReference> sortedReferences = new ArrayList<>();
		for (MPart part : activationList) {
			for (EditorReference ref : editorReferences) {
				if (ref.getModel() == part) {
					sortedReferences.add(ref);
					break;
				}
			}
		}

		for (EditorReference ref : editorReferences) {
			if (!sortedReferences.contains(ref)) {
				sortedReferences.add(ref);
			}
		}

		MPerspective currentPerspective = getCurrentPerspective();
		if (currentPerspective != null) {
			int scope = allPerspectives ? WINDOW_SCOPE : EModelService.PRESENTATION;
			List<MPart> placeholders = modelService.findElements(window,
					CompatibilityEditor.MODEL_ELEMENT_ID, MPart.class, null, scope);
			List<EditorReference> visibleReferences = new ArrayList<>();
			for (EditorReference reference : sortedReferences) {
				for (MPart placeholder : placeholders) {
					if (reference.getModel() == placeholder && placeholder.isToBeRendered()) {
						// only rendered placeholders are valid references
						visibleReferences.add(reference);
					}
				}
			}

			return visibleReferences;
		}

		return sortedReferences;
	}

	public List<EditorReference> getInternalEditorReferences() {
		return editorReferences;
	}

	public EditorReference getEditorReference(MPart part) {
		for (EditorReference ref : editorReferences) {
			if (ref.getModel() == part) {
				return ref;
			}
		}
		return null;
	}

	public ViewReference getViewReference(MPart part) {
		for (ViewReference ref : viewReferences) {
			if (ref.getModel() == part) {
				return ref;
			}
		}
		return null;
	}

	private boolean contains(ViewReference reference) {
		for (ViewReference viewReference : viewReferences) {
			if (reference.getModel().getElementId().equals(viewReference.getModel().getElementId())) {
				return true;
			}
		}
		return false;
	}

	public void addViewReference(ViewReference reference) {
		if (!contains(reference)) {
			viewReferences.add(reference);
		}
	}

	public void addEditorReference(EditorReference editorReference) {
		WorkbenchPage curPage = (WorkbenchPage) editorReference.getPage();

		// Ensure that the page is up-to-date
		if (curPage != this) {
			curPage.editorReferences.remove(editorReference);
			editorReference.setPage(this);
		}

		// Avoid dups
		if (!editorReferences.contains(editorReference)) {
			editorReferences.add(editorReference);
		}
	}

	MPartDescriptor findDescriptor(String id) {
		return modelService.getPartDescriptor(id);
	}

	/**
	 * Searches the workbench window for a part with the given view id and
	 * secondary id (if desired) given the specified search rules.
	 *
	 * @param viewId
	 *            the id of the view
	 * @param secondaryId
	 *            the secondary id of the view, or <code>null</code> if the view
	 *            to search for should be one without a secondary id defined
	 * @param searchFlags
	 *            the desired search locations
	 * @return the part with the specified view id and secondary id, or
	 *         <code>null</code> if it could not be found in this page's parent
	 *         workbench window
	 * @see EModelService#findElements(MUIElement, String, Class, List, int)
	 */
	private MPart findPart(String viewId, int searchFlags) {
		List<MPart> parts = modelService.findElements(getWindowModel(), viewId, MPart.class, null,
				searchFlags);
		if (parts.size() > 0)
			return parts.get(0);

		return null;
	}

	PartState convert(int mode) {
		switch (mode) {
		case VIEW_ACTIVATE:
			return PartState.ACTIVATE;
		case VIEW_VISIBLE:
			return PartState.VISIBLE;
		case VIEW_CREATE:
			return PartState.CREATE;
		}
		throw new IllegalArgumentException(WorkbenchMessages.WorkbenchPage_IllegalViewMode);
	}

	/**
	 * Shows a view.
	 *
	 * Assumes that a busy cursor is active.
	 */
	protected IViewPart busyShowView(String viewId, int mode) throws PartInitException {
		switch (mode) {
		case VIEW_ACTIVATE:
		case VIEW_VISIBLE:
		case VIEW_CREATE:
			break;
		default:
			throw new IllegalArgumentException(WorkbenchMessages.WorkbenchPage_IllegalViewMode);
		}

		MPart part = findPart(viewId, EModelService.ANYWHERE);
		if (part == null) {
			MPlaceholder ph = partService.createSharedPart(viewId, false);
			if (ph == null) {
				throw new PartInitException(NLS.bind(WorkbenchMessages.ViewFactory_couldNotCreate,
						viewId));
			}

			part = (MPart) ph.getRef();
			part.setCurSharedRef(ph);
		}

		part = showPart(mode, part);

		ViewReference ref = getViewReference(part);

		return (IViewPart) ref.getPart(true);
	}
	private MPart showPart(int mode, MPart part) {
		switch (mode) {
		case VIEW_ACTIVATE:
			partService.showPart(part, PartState.ACTIVATE);
			if (part.getObject() instanceof CompatibilityView) {
				CompatibilityView compatibilityView = (CompatibilityView) part.getObject();
				actionSwitcher.updateActivePart(getWrappedPart(compatibilityView));
			}
			break;
		case VIEW_VISIBLE:
			MPart activePart = partService.getActivePart();
			if (activePart == null) {
				partService.showPart(part, PartState.ACTIVATE);
				if (part.getObject() instanceof CompatibilityView) {
					CompatibilityView compatibilityView = (CompatibilityView) part.getObject();
					actionSwitcher.updateActivePart(getWrappedPart(compatibilityView));
				}
			} else {
				part = ((PartServiceImpl) partService).addPart(part);
				MPlaceholder activePlaceholder = activePart.getCurSharedRef();
				MUIElement activePartParent = activePlaceholder == null ? activePart
						.getParent() : activePlaceholder.getParent();
				partService.showPart(part, PartState.CREATE);
				if (part.getCurSharedRef() == null || part.getCurSharedRef().getParent() != activePartParent) {
					partService.bringToTop(part);
				}
			}
			break;
		case VIEW_CREATE:
			partService.showPart(part, PartState.CREATE);

			// Report the visibility of the created part
			MStackElement sElement = part;
			if (part.getCurSharedRef() != null)
				sElement = part.getCurSharedRef();
			MUIElement parentElement = sElement.getParent();
			if (parentElement instanceof MPartStack) {
				MPartStack partStack = (MPartStack) parentElement;
				if (partStack.getSelectedElement() == sElement
						&& !partStack.getTags().contains(IPresentationEngine.MINIMIZED)) {
					firePartVisible(part);
				} else {
					firePartHidden(part);
				}
			} else {
				firePartVisible(part); // Stand-alone part
			}
			break;
		}
		return part;
	}

    /**
     * Returns whether a part exists in the current page.
     */
    private boolean certifyPart(IWorkbenchPart part) {
        //Workaround for bug 22325
        if (part != null && !(part.getSite() instanceof PartSite)) {
			return false;
		}
		return true;
    }

    /**
	 * Closes this page.
     */
    @Override
	public boolean close() {
        final boolean[] ret = new boolean[1];
        BusyIndicator.showWhile(null, new Runnable() {
            @Override
			public void run() {
				ret[0] = close(true, true);
            }
        });
        return ret[0];
    }

    public boolean closeAllSavedEditors() {
        // get the Saved editors
        IEditorReference editors[] = getEditorReferences();
        IEditorReference savedEditors[] = new IEditorReference[editors.length];
        int j = 0;
		for (IEditorReference editor : editors) {
            if (!editor.isDirty()) {
                savedEditors[j++] = editor;
            }
        }
        //there are no unsaved editors
        if (j == 0) {
			return true;
		}
        IEditorReference[] newSaved = new IEditorReference[j];
        System.arraycopy(savedEditors, 0, newSaved, 0, j);
        return closeEditors(newSaved, false);
    }

    /**
     * See IWorkbenchPage
     */
    @Override
	public boolean closeAllEditors(boolean save) {
        return closeEditors(getEditorReferences(), save);
    }

	/**
	 * See IWorkbenchPage
	 */
	@Override
	public boolean closeEditors(IEditorReference[] refArray, boolean save) {
		if (refArray.length == 0) {
			return true;
        }

		// Check if we're being asked to close any parts that are already closed
		// or cannot
		// be closed at this time
		ArrayList<IEditorReference> editorRefs = new ArrayList<>();
		for (IEditorReference reference : refArray) {
			// If we're in the middle of creating this part, this is a
			// programming error. Abort the entire
			// close operation. This usually occurs if someone tries to open a
			// dialog in a method that
			// isn't allowed to do so, and a *syncExec tries to close the part.
			// If this shows up in a log
			// file with a dialog's event loop on the stack, then the code that
			// opened the dialog is usually
			// at fault.
			if (reference == partBeingActivated) {
				WorkbenchPlugin.log(new RuntimeException(
						"WARNING: Blocked recursive attempt to close part " //$NON-NLS-1$
								+ partBeingActivated.getId()
								+ " while still in the middle of activating it")); //$NON-NLS-1$
				return false;
			}

			if (reference instanceof WorkbenchPartReference) {
				WorkbenchPartReference ref = (WorkbenchPartReference) reference;

				// If we're being asked to close a part that is disposed (ie:
				// already closed),
				// skip it and proceed with closing the remaining parts.
				if (ref.isDisposed()) {
					continue;
                }
            }

			editorRefs.add(reference);
        }

		// if active navigation position belongs to an editor being closed,
		// update it
		// (The navigation position for an editor N was updated as an editor N +
		// 1
		// was activated. As a result, all but the last editor have up-to-date
		// navigation positions.)
		for (IEditorReference ref : editorRefs) {
			IEditorPart oldPart = ref.getEditor(false);
			if (oldPart == null)
				continue;
			if (navigationHistory.updateActive(oldPart))
				break; // updated - skip the rest
        }

		// notify the model manager before the close
		List<IWorkbenchPart> partsToClose = new ArrayList<>();
		for (IEditorReference ref : editorRefs) {
			IEditorPart refPart = ref.getEditor(false);
			if (refPart != null) {
				partsToClose.add(refPart);
            }
        }

		boolean confirm = true;
		SaveablesList modelManager = null;
		Object postCloseInfo = null;
		if (partsToClose.size() > 0) {
			modelManager = (SaveablesList) getWorkbenchWindow().getService(
					ISaveablesLifecycleListener.class);
			// this may prompt for saving and return null if the user canceled:
			postCloseInfo = modelManager.preCloseParts(partsToClose, save, getWorkbenchWindow());
			if (postCloseInfo == null) {
				return false;
			}
			confirm = false;
        }

		// Fire pre-removal changes
		for (IEditorReference ref : editorRefs) {
			// Notify interested listeners before the close
			legacyWindow.firePerspectiveChanged(this, getPerspective(), ref, CHANGE_EDITOR_CLOSE);

		}

        deferUpdates(true);
		try {
			if (modelManager != null) {
				modelManager.postClose(postCloseInfo);
			}

			// Close all editors.
			for (Iterator<IEditorReference> it = editorRefs.iterator(); it.hasNext();) {
				IEditorReference ref = it.next();
				// hide editors that haven't been instantiated first
				if (ref.getPart(false) == null) {
					if (!(hidePart(((EditorReference) ref).getModel(), false, confirm, false, false))) {
						return false;
					}
					// hidden successfully, remove it from the list
					it.remove();
				}
			}

			MPart activePart = findPart(getActiveEditor());
			boolean closeActivePart = false;
			// now hide all instantiated editors
			for (IEditorReference editorRef : editorRefs) {
				MPart model = ((EditorReference) editorRef).getModel();
				if (activePart == model) {
					closeActivePart = true;
				} else if (!(hidePart(model, false, confirm, false, false))) {
					// saving should've been handled earlier above
					return false;
				}
			}

			// close the active part last to minimize activation churn
			if (closeActivePart) {
				if (!(hidePart(activePart, false, confirm, false))) {
					return false;
				}
			}
		} finally {
			deferUpdates(false);
        }

		// Notify interested listeners after the close
		legacyWindow.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_CLOSE);

        // Return true on success.
		return true;
    }

	public boolean closeEditor(IEditorReference editor) {
		if (getInternalEditorReferences().contains(editor)) {
			MPart part = ((EditorReference) editor).getModel();
			hidePart(part, false, false, false, false);

			MElementContainer<MUIElement> parent = part.getParent();
			if (parent != null) {
				parent.getChildren().remove(part);
			}
			return true;
		}
		return false;
	}

	private boolean hidePart(MPart part, boolean save, boolean confirm, boolean force) {
		return hidePart(part, save, confirm, force, true);
	}

	private boolean hidePart(MPart part, boolean save, boolean confirm, boolean force, boolean local) {
		if (!partService.getParts().contains(part)) {
			if (local) {
				return false;
			}
			part.setToBeRendered(false);
			return true;
		}

		Object clientObject = part.getObject();
		if (!(clientObject instanceof CompatibilityPart)) {
			// either not a 3.x part or it's an e4 part, should still hide it
			if (save) {
				// save as necessary
				if (partService.savePart(part, confirm)) {
					partService.hidePart(part, force);
					return true;
				}
				return false;
			}
			partService.hidePart(part, force);
			return true;
		}

		IWorkbenchPart workbenchPart = getWrappedPart((CompatibilityPart) clientObject);
		if (save && workbenchPart != null) {
			ISaveablePart saveablePart = SaveableHelper.getSaveable(workbenchPart);
			if (saveablePart != null) {
				if (saveablePart.isSaveOnCloseNeeded()) {
					if (!saveSaveable(saveablePart, workbenchPart, confirm, true)) {
						return false;
					}
				}
			}
		}

		for (IViewReference viewRef : viewReferences) {
			if (workbenchPart == viewRef.getPart(false)) {
				partService.hidePart(part, force);
				return true;
			}
		}

		for (IEditorReference viewRef : editorReferences) {
			if (workbenchPart == viewRef.getPart(false)) {
				partService.hidePart(part, force);
				return true;
			}
		}
		return false;
	}

    /**
     * Enables or disables listener notifications. This is used to delay listener notifications until the
     * end of a public method.
     *
     * @param shouldDefer
     */
    private void deferUpdates(boolean shouldDefer) {
        if (shouldDefer) {
            if (deferCount == 0) {
                startDeferring();
            }
            deferCount++;
        } else {
            deferCount--;
            if (deferCount == 0) {
                handleDeferredEvents();
            }
        }
    }

    private void startDeferring() {
		// TODO compat: do we defer events
    }

    private void handleDeferredEvents() {
		// TODO compat: do we handler defered events
    }

    public boolean closeEditor(IEditorReference editorRef, boolean save) {
        return closeEditors(new IEditorReference[] {editorRef}, save);
    }

    /**
     * See IWorkbenchPage#closeEditor
     */
    @Override
	public boolean closeEditor(IEditorPart editor, boolean save) {
        IWorkbenchPartReference ref = getReference(editor);
        if (ref instanceof IEditorReference) {
        	return closeEditors(new IEditorReference[] {(IEditorReference) ref}, save);
        }
        return false;
    }

	/**
	 * Closes the specified perspective.
	 *
	 * @param desc
	 *            the perspective to close
	 * @param perspectiveId
	 *            the id of the perspective being closed
	 * @param saveParts
	 *            <code>true</code> if dirty parts should be prompted for its
	 *            contents to be saved, <code>false</code> otherwise
	 */
	private void closePerspective(IPerspectiveDescriptor desc, String perspectiveId,
			boolean saveParts) {
		MPerspective persp = (MPerspective) modelService.find(perspectiveId, window);
		// check to ensure this perspective actually exists in this window
		if (persp != null) {
			if (saveParts) {
				List<IWorkbenchPart> partsToSave = new ArrayList<>();
				// retrieve all parts under the specified perspective
				List<MPart> parts = modelService.findElements(persp, null, MPart.class, null);
				if (!parts.isEmpty()) {
					// filter out any parts that are visible in any other
					// perspectives
					for (MPerspective perspective : getPerspectiveStack().getChildren()) {
						if (perspective != persp) {
							parts.removeAll(modelService.findElements(perspective, null,
									MPart.class, null));
						}
					}

					if (!parts.isEmpty()) {
						for (Iterator<MPart> it = parts.iterator(); it.hasNext();) {
							MPart part = it.next();
							if (part.isDirty()) {
								Object object = part.getObject();
								if (object instanceof CompatibilityPart) {
									IWorkbenchPart workbenchPart = getWrappedPart((CompatibilityPart) object);
									if (workbenchPart == null) {
										continue;
									}
									ISaveablePart saveablePart = SaveableHelper.getSaveable(workbenchPart);
									if (saveablePart != null) {
										if (!saveablePart.isSaveOnCloseNeeded()) {
											part.setDirty(false);
											it.remove();
										} else {
											partsToSave.add(workbenchPart);
										}
									}
								}
							} else {
								it.remove();
							}
						}

						if (!partsToSave.isEmpty()) {
							if (!saveAll(partsToSave, true, true, false, legacyWindow, legacyWindow)) {
								// user cancel
								return;
							}
						}
					}
				}
			}

			// Remove from caches
			sortedPerspectives.remove(desc);
			// check if we're closing the currently active perspective
			if (getPerspectiveStack().getSelectedElement() == persp
					&& !sortedPerspectives.isEmpty()) {
				// get the perspective that was last active and set it
				IPerspectiveDescriptor lastActive = sortedPerspectives.get(sortedPerspectives
						.size() - 1);
				if (lastActive != null) {
					setPerspective(lastActive);
				}
			}
			modelService.removePerspectiveModel(persp, window);
			modelToPerspectiveMapping.remove(persp);

			legacyWindow.firePerspectiveClosed(this, desc);
		}
	}

	@Override
	public void closePerspective(IPerspectiveDescriptor desc, boolean saveParts, boolean closePage) {
		closePerspective(desc, desc.getId(), saveParts, closePage);
	}

	public void closePerspective(IPerspectiveDescriptor desc, String perspectiveId,
			boolean saveParts, boolean closePage) {
		MPerspective persp = (MPerspective) modelService.find(perspectiveId, window);
		// check to ensure this perspective actually exists in this window
		if (persp != null) {
			persp.getTags().add("PerspClosing"); //$NON-NLS-1$
			try {
				MPerspectiveStack perspectiveStack = modelService.findElements(window, null,
						MPerspectiveStack.class, null).get(0);
				if (perspectiveStack.getChildren().size() == 1) {
					closeAllPerspectives(saveParts, closePage);
				} else {
					closePerspective(desc, perspectiveId, saveParts);
				}
			} finally {
				persp.getTags().remove("PerspClosing"); //$NON-NLS-1$
			}
		}
	}

	@Override
	public void closeAllPerspectives(boolean saveEditors, boolean closePage) {
		boolean okToProceed = closeAllEditors(true);
		if (okToProceed) {
			List<MPerspective> kids = new ArrayList<>(_perspectiveStack.getChildren());
			MPerspective curPersp = _perspectiveStack.getSelectedElement();
			for (MPerspective persp : kids) {
				if (persp != curPersp) {
					closePerspective(getPerspectiveDesc(persp.getElementId()),
							persp.getElementId(), false);
				}
			}
			if (curPersp != null) {
				closePerspective(getPerspectiveDesc(curPersp.getElementId()),
						curPersp.getElementId(),
						false);
			}
			if (closePage) {
				close();
			}
		}
	}

	private boolean close(boolean save, boolean unsetPage) {
		if (save && !saveAllEditors(true, true, true)) {
			return false;
		}

		Collection<MPart> partsToHide = partService.getParts();
		// workaround for bug 455281
		List<MPart> partsOutsidePersp = modelService.findElements(window, null, MPart.class, null,
				EModelService.OUTSIDE_PERSPECTIVE);
		partsToHide.removeAll(partsOutsidePersp);

		for (MPart part : partsToHide) {
			// no save, no confirm, force
			hidePart(part, false, true, true);
		}

		MPerspectiveStack perspectiveStack = modelService.findElements(window, null,
				MPerspectiveStack.class, null).get(0);
		MPerspective current = perspectiveStack.getSelectedElement();
		for (Object perspective : perspectiveStack.getChildren().toArray()) {
			if (perspective != current) {
				modelService.removePerspectiveModel((MPerspective) perspective, window);
			}
		}

		if (current != null) {
			modelService.removePerspectiveModel(current, window);
		}

		viewReferences.clear();
		editorReferences.clear();
		sortedPerspectives.clear();
		modelToPerspectiveMapping.clear();

		if (unsetPage) {
			legacyWindow.setActivePage(null);
			partService.removePartListener(e4PartListener);
			broker.unsubscribe(selectionHandler);
			broker.unsubscribe(widgetHandler);
			broker.unsubscribe(referenceRemovalEventHandler);
			broker.unsubscribe(firingHandler);
			broker.unsubscribe(childrenHandler);
			partEvents.clear();

			partListenerList.clear();
			partListener2List.clear();
			propertyChangeListeners.clear();

			selectionService.dispose();

			ContextInjectionFactory.uninject(this, window.getContext());
		}
		return true;
	}

	/**
	 * Forces all perspectives on the page to zoom out.
	 */
	public void unzoomAllPerspectives() {
		// TODO compat: we have no min/max behaviour
    }



    /**
	 * Cleanup.
	 */
	public void dispose() {

// // Always unzoom
		// if (isZoomed()) {
		// zoomOut();
		// }
		//
		// // makeActiveEditor(null);
		// // makeActive(null);
		//
		// // Close and dispose the editors.
		// closeAllEditors(false);
		//
		// // Need to make sure model data is cleaned up when the page is
		// // disposed. Collect all the views on the page and notify the
		// // saveable list of a pre/post close. This will free model data.
		// IWorkbenchPartReference[] partsToClose = getOpenParts();
		// List dirtyParts = new ArrayList(partsToClose.length);
		// for (int i = 0; i < partsToClose.length; i++) {
		// IWorkbenchPart part = partsToClose[i].getPart(false);
		// if (part != null && part instanceof IViewPart) {
		// dirtyParts.add(part);
		// }
		// }
		// SaveablesList saveablesList = (SaveablesList)
		// getWorkbenchWindow().getWorkbench().getService(ISaveablesLifecycleListener.class);
		// Object postCloseInfo = saveablesList.preCloseParts(dirtyParts,
		// false,getWorkbenchWindow());
		// saveablesList.postClose(postCloseInfo);
		//
		// // Get rid of perspectives. This will close the views.
		// Iterator itr = perspList.iterator();
		// while (itr.hasNext()) {
		// Perspective perspective = (Perspective) itr.next();
		// legacyWindow.firePerspectiveClosed(this, perspective.getDesc());
		// perspective.dispose();
		// }
		// perspList = new PerspectiveList();
		//
		// // Capture views.
		// IViewReference refs[] = viewFactory.getViews();
		//
		// if (refs.length > 0) {
		// // Dispose views.
		// for (int i = 0; i < refs.length; i++) {
		// final WorkbenchPartReference ref = (WorkbenchPartReference) refs[i];
		// //partList.removePart(ref);
		// //firePartClosed(refs[i]);
		// SafeRunner.run(new SafeRunnable() {
		// public void run() {
		// // WorkbenchPlugin.log(new Status(IStatus.WARNING,
		// WorkbenchPlugin.PI_WORKBENCH,
		////                                Status.OK, "WorkbenchPage leaked a refcount for view " + ref.getId(), null));  //$NON-NLS-1$//$NON-NLS-2$
		//
		// ref.dispose();
		// }
		//
		// public void handleException(Throwable e) {
		// }
		// });
		// }
		// }
		//
		// activationList = new ActivationList();
		//
		// // Get rid of editor presentation.
		// editorPresentation.dispose();
		//
		// // Get rid of composite.
		// composite.dispose();
		//
		// navigationHistory.dispose();
		//
		// stickyViewMan.clear();
		//
		// if (tracker != null) {
		// tracker.close();
		// }
		//
		// // if we're destroying a window in a non-shutdown situation then we
		// should
		// // clean up the working set we made.
		// if (!legacyWindow.getWorkbench().isClosing()) {
		// if (aggregateWorkingSet != null) {
		// PlatformUI.getWorkbench().getWorkingSetManager().removeWorkingSet(aggregateWorkingSet);
		// }
		// }
    }



    /**
     * @return NavigationHistory
     */
    @Override
	public INavigationHistory getNavigationHistory() {
        return navigationHistory;
    }

    public boolean editActionSets() {
		Perspective persp = getActivePerspective();
		if (persp == null) {
			return false;
		}

		// Create list dialog.
		CustomizePerspectiveDialog dlg = legacyWindow.createCustomizePerspectiveDialog(persp,
				window.getContext());
		// Open.
		boolean ret = (dlg.open() == Window.OK);
		if (ret) {
			legacyWindow.updateActionSets();
			legacyWindow.firePerspectiveChanged(this, getPerspective(), CHANGE_RESET);
			legacyWindow.firePerspectiveChanged(this, getPerspective(), CHANGE_RESET_COMPLETE);
		}
		return ret;
    }


    /**
     * See IWorkbenchPage@findView.
     */
    @Override
	public IViewPart findView(String id) {
        IViewReference ref = findViewReference(id);
        if (ref == null) {
			return null;
		}
        return ref.getView(true);
    }

    @Override
	public IViewReference findViewReference(String viewId) {
		for (IViewReference reference : getViewReferences()) {
			ViewReference ref = (ViewReference) reference;
			if (viewId.equals(ref.getModel().getElementId())) {
				return reference;
			}
		}
		return null;
    }

    @Override
	public IViewReference findViewReference(String viewId, String secondaryId) {
		String compoundId = viewId;
		if (secondaryId != null && secondaryId.length() > 0)
			compoundId += ":" + secondaryId; //$NON-NLS-1$
		return findViewReference(compoundId);
    }

	public void createViewReferenceForPart(final MPart part, String viewId) {
		// If the id contains a ':' use the part before it as the descriptor id
		int colonIndex = viewId.indexOf(':');
		String descId = colonIndex == -1 ? viewId : viewId.substring(0, colonIndex);

		IViewDescriptor desc = getWorkbenchWindow().getWorkbench().getViewRegistry().find(descId);
		final ViewReference ref = new ViewReference(window.getContext(), this, part,
				(ViewDescriptor) desc);
		if (contains(ref)) {
			return;
		}

		IEclipseContext partContext = part.getContext();
		if (partContext == null) {
			ref.subscribe();
		} else {
			partContext.set(ViewReference.class.getName(), ref);
		}
		addViewReference(ref);
	}

    /**
     * Notify property change listeners about a property change.
     *
     * @param changeId
     *            the change id
     * @param oldValue
     *            old property value
     * @param newValue
     *            new property value
     */
    private void firePropertyChange(String changeId, Object oldValue,
            Object newValue) {

        UIListenerLogging.logPagePropertyChanged(this, changeId, oldValue, newValue);

        PropertyChangeEvent event = new PropertyChangeEvent(this, changeId,
                oldValue, newValue);

		for (IPropertyChangeListener listener : propertyChangeListeners) {
			listener.propertyChange(event);
        }
    }

    /*
     * Returns the action bars.
     */
    public IActionBars getActionBars() {
        if (actionBars == null) {
			actionBars = new WWinActionBars(legacyWindow);
		}
        return actionBars;
    }

	/**
	 * Returns an array of the visible action sets.
	 *
	 * @return an array of the currently visible action sets
	 */
	public IActionSetDescriptor[] getActionSets() {
		Collection<?> collection = actionSets.getVisibleItems();
		return collection.toArray(new IActionSetDescriptor[collection.size()]);
	}

    /**
     * @see IWorkbenchPage
     */
    @Override
	public IEditorPart getActiveEditor() {
		IWorkbenchPart activePart = getActivePart();
		if (activePart instanceof IEditorPart) {
			// if the currently active part is an editor, return it
			return (IEditorPart) activePart;
		}

		if (!activationList.isEmpty()) {
			IEditorPart editor = findActiveEditor();
			if (editor != null) {
				return editor;
			}
		}

		MUIElement area = findSharedArea();
		if (area instanceof MPlaceholder) {
			area = ((MPlaceholder) area).getRef();
		}
		if (area != null && area.isVisible() && area.isToBeRendered()) {
			// we have a shared area, try iterating over its editors first
			List<MPart> editors = modelService.findElements(area,
					CompatibilityEditor.MODEL_ELEMENT_ID, MPart.class, null);
			for (MPart model : editors) {
				Object object = model.getObject();
				if (object instanceof CompatibilityEditor) {
					CompatibilityEditor editor = (CompatibilityEditor) object;
					// see bug 308492
					if (!editor.isBeingDisposed() && isInArea(area, model)) {
						return ((CompatibilityEditor) object).getEditor();
					}
				}
			}
		}

		MPerspective perspective = getPerspectiveStack().getSelectedElement();
		if (perspective == null) {
			return null;
		}

		List<MPart> parts = modelService.findElements(perspective,
				CompatibilityEditor.MODEL_ELEMENT_ID, MPart.class, null);
		for (MPart part : parts) {
			Object object = part.getObject();
			if (object instanceof CompatibilityEditor) {
				CompatibilityEditor editor = (CompatibilityEditor) object;
				// see bug 308492
				if (!editor.isBeingDisposed()) {
					if (isValid(perspective, part) || isValid(window, part)) {
						return ((CompatibilityEditor) object).getEditor();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Searches and returns an editor from the activation list that is being
	 * displayed in the current presentation. If an editor is in the
	 * presentation but is behind another part it will not be returned.
	 *
	 * @return an editor that is being shown in the current presentation and was
	 *         previously activated, editors that are behind another part in a
	 *         stack will not be returned
	 */
	private IEditorPart findActiveEditor() {
		List<MPart> candidates = new ArrayList<>(activationList);
		MUIElement area = findSharedArea();
		if (area instanceof MPlaceholder) {
			area = ((MPlaceholder) area).getRef();
		}
		if (area != null && area.isVisible() && area.isToBeRendered()) {
			// we have a shared area, try iterating over its editors first
			List<MPart> editors = modelService
					.findElements(area, CompatibilityEditor.MODEL_ELEMENT_ID, MPart.class, null);
			for (Iterator<MPart> it = candidates.iterator(); it.hasNext();) {
				MPart model = it.next();
				if (!editors.contains(model)) {
					continue;
				}

				Object object = model.getObject();
				if (object instanceof CompatibilityEditor) {
					CompatibilityEditor editor = (CompatibilityEditor) object;
					// see bug 308492
					if (!editor.isBeingDisposed() && isInArea(area, model)) {
						return ((CompatibilityEditor) object).getEditor();
					}
				}
				it.remove();
			}
		}

		MPerspective perspective = getPerspectiveStack().getSelectedElement();
		for (MPart model : activationList) {
			Object object = model.getObject();
			if (object instanceof CompatibilityEditor) {
				CompatibilityEditor editor = (CompatibilityEditor) object;
				// see bug 308492
				if (!editor.isBeingDisposed()) {
					if (isValid(perspective, model) || isValid(window, model)) {
						return ((CompatibilityEditor) object).getEditor();
					}
				}
			}
		}
		return null;
    }

	private boolean isInArea(MUIElement area, MUIElement element) {
		if (!element.isToBeRendered() || !element.isVisible()) {
			return false;
		}

		if (element == area) {
			return true;
		}

		MElementContainer<?> parent = element.getParent();
		if (parent == null || parent instanceof MPerspective || parent instanceof MWindow) {
			return false;
		} else if (parent instanceof MGenericStack) {
			return parent.getSelectedElement() == element ? isValid(area, parent) : false;
		}

		return isValid(area, parent);
	}

	private boolean isValid(MUIElement ancestor, MUIElement element) {
		if (!element.isToBeRendered() || !element.isVisible()) {
			return false;
		}

		if (element == ancestor) {
			return true;
		}

		MElementContainer<?> parent = element.getParent();
		if (parent == null) {
			// might be a detached window
			if (element instanceof MWindow) {
				parent = (MElementContainer<?>) ((EObject) element).eContainer();
			}

			if (parent == null) {
				return false;
			}
		}

		if (parent instanceof MGenericStack) {
			return parent.getSelectedElement() == element ? isValid(ancestor, parent) : false;
		}

		return isValid(ancestor, parent);
	}

    @Override
	public IWorkbenchPart getActivePart() {
		MPart part = partService.getActivePart();
		return getWorkbenchPart(part);
	}

	@Override
	public IWorkbenchPartReference getActivePartReference() {
		IWorkbenchPart part = getActivePart();
		return part == null ? null : getReference(part);
	}

	public Composite getClientComposite() {
		return composite;
	}

	@Override
	public IEditorPart[] getDirtyEditors() {
		List<IEditorPart> dirtyEditors = new ArrayList<>();
		for (IEditorReference editorRef : editorReferences) {
			IEditorPart editor = editorRef.getEditor(false);
			if (editor != null && editor.isDirty()) {
				dirtyEditors.add(editor);
			}
		}
		return dirtyEditors.toArray(new IEditorPart[dirtyEditors.size()]);
	}

	@Override
	public IEditorPart findEditor(IEditorInput input) {
		IEditorReference[] references = findEditors(input, null, MATCH_INPUT);
		return references.length == 0 ? null : references[0].getEditor(true);
	}

	@Override
	public IEditorReference[] findEditors(IEditorInput input, String editorId, int matchFlags) {
		List<EditorReference> filteredReferences = getSortedEditorReferences();

		switch (matchFlags) {
		case MATCH_INPUT:
			List<IEditorReference> editorRefs = new ArrayList<>();
			for (EditorReference editorRef : filteredReferences) {
				checkEditor(input, editorRefs, editorRef);
			}
			return editorRefs.toArray(new IEditorReference[editorRefs.size()]);
		case MATCH_ID:
			editorRefs = new ArrayList<>();
			for (IEditorReference editorRef : filteredReferences) {
				if (editorId.equals(editorRef.getId())) {
					editorRefs.add(editorRef);
				}
			}
			return editorRefs.toArray(new IEditorReference[editorRefs.size()]);
		default:
			if ((matchFlags & IWorkbenchPage.MATCH_ID) != 0
					&& (matchFlags & IWorkbenchPage.MATCH_INPUT) != 0) {
				editorRefs = new ArrayList<>();
				for (EditorReference editorRef : filteredReferences) {
					if (editorRef.getId().equals(editorId)) {
						checkEditor(input, editorRefs, editorRef);
					}
				}
				return editorRefs.toArray(new IEditorReference[editorRefs.size()]);
			}
			return new IEditorReference[0];
		}
	}

	private void checkEditor(IEditorInput input, List<IEditorReference> editorRefs,
			EditorReference editorRef) {
		EditorDescriptor descriptor = editorRef.getDescriptor();
		if (descriptor != null) {
			IEditorMatchingStrategy strategy = descriptor.getEditorMatchingStrategy();
			if (strategy != null && strategy.matches(editorRef, input)) {
				editorRefs.add(editorRef);
				return;
			}
		}

		IEditorPart editor = editorRef.getEditor(false);
		if (editor == null) {
			try {
				String name = input.getName();
				IPersistableElement persistable = input.getPersistable();
				if (name == null || persistable == null) {
					return;
				}

				String id = persistable.getFactoryId();
				if (id != null && id.equals(editorRef.getFactoryId())
						&& name.equals(editorRef.getName())
						&& input.equals(editorRef.getEditorInput())) {
					editorRefs.add(editorRef);
				}
			} catch (PartInitException e) {
				WorkbenchPlugin.log(e);
			}
		} else if (editor.getEditorInput().equals(input)) {
			editorRefs.add(editorRef);
		}
	}

	@Override
	public IEditorPart[] getEditors() {
		final IEditorReference[] editorReferences = getEditorReferences();
		int length = editorReferences.length;
		IEditorPart[] editors = new IEditorPart[length];
		for (int i = 0; i < length; i++) {
			editors[i] = editorReferences[i].getEditor(true);
		}
		return editors;
	}

	@Override
	public IEditorReference[] getEditorReferences() {
		List<EditorReference> references = getOrderedEditorReferences();
		return references.toArray(new IEditorReference[references.size()]);
	}

	public IEditorReference[] getSortedEditors() {
		IWorkbenchPartReference[] parts = getSortedParts(true, false, false);
		IEditorReference[] editors = new IEditorReference[parts.length];
		System.arraycopy(parts, 0, editors, 0, parts.length);
		return editors;
	}

	public IWorkbenchPartReference[] getSortedParts() {
		return getSortedParts(true, true, false);
	}

	/**
	 * Returns a sorted array of references to editors and/or views from this
	 * page.
	 *
	 * @param editors
	 *            include editors
	 * @param views
	 *            include views
	 * @param allPerspectives
	 *            if {@code false}, does not include parts from inactive
	 *            perspectives
	 * @return a sorted array of references to editors and/or views
	 */
	private IWorkbenchPartReference[] getSortedParts(boolean editors, boolean views,
			boolean allPerspectives) {
		if (!editors && !views) {
			return new IWorkbenchPartReference[0];
		}

		List<IWorkbenchPartReference> sortedReferences = new ArrayList<>();
		IViewReference[] viewReferences = getViewReferences(allPerspectives);
		List<EditorReference> editorReferences = getSortedEditorReferences(allPerspectives);

		activationLoop: for (MPart part : activationList) {
			if (views) {
				for (IViewReference ref : viewReferences) {
					if (((ViewReference) ref).getModel() == part) {
						sortedReferences.add(ref);
						continue activationLoop;
					}
				}
			}

			if (editors) {
				for (EditorReference ref : editorReferences) {
					if (ref.getModel() == part) {
						sortedReferences.add(ref);
						break;
					}
				}
			}
		}

		if (views) {
			for (IViewReference ref : viewReferences) {
				if (!sortedReferences.contains(ref)) {
					sortedReferences.add(ref);
				}
			}
		}

		if (editors) {
			for (EditorReference ref : editorReferences) {
				if (!sortedReferences.contains(ref)) {
					sortedReferences.add(ref);
				}
			}
		}

		return sortedReferences.toArray(new IWorkbenchPartReference[sortedReferences.size()]);
	}

    /**
     * @see IWorkbenchPage
     */
    @Override
	public IAdaptable getInput() {
        return input;
    }

    /**
     * Returns the page label. This is a combination of the page input and
     * active perspective.
     */
    @Override
	public String getLabel() {
        String label = WorkbenchMessages.WorkbenchPage_UnknownLabel;
        IWorkbenchAdapter adapter = Adapters.adapt(input, IWorkbenchAdapter.class);
        if (adapter != null) {
			label = adapter.getLabel(input);
		}
		// Perspective persp = getActivePerspective();
		// if (persp != null) {
		// label = NLS.bind(WorkbenchMessages.WorkbenchPage_PerspectiveFormat,
		// label, persp.getDesc().getLabel());
		// } else if (deferredActivePersp != null) {
		// label =
		// NLS.bind(WorkbenchMessages.WorkbenchPage_PerspectiveFormat,label,
		// deferredActivePersp.getLabel());
		// }
        return label;
    }

    /**
     * Returns the perspective.
     */
    @Override
	public IPerspectiveDescriptor getPerspective() {
		MPerspectiveStack ps = getPerspectiveStack();
		MPerspective curPersp = ps.getSelectedElement();
		if (curPersp == null)
			return null;
		return getPerspectiveDesc(curPersp.getElementId());
	}

	public IPerspectiveDescriptor getPerspectiveDesc(String id) {
		IPerspectiveRegistry perspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();
		// registry may be null on shutdown
		if (perspectiveRegistry == null) {
			return null;
		}
		IPerspectiveDescriptor desc = perspectiveRegistry
				.findPerspectiveWithId(id);
		return desc;
	}

    @Override
	public ISelection getSelection() {
		return selectionService.getSelection();
    }

    @Override
	public ISelection getSelection(String partId) {
		return selectionService.getSelection(partId);
    }

	/**
	 * Returns the ids of the parts to list in the Show In... prompter. This is
	 * a List of Strings.
	 *
	 * @return the ids of the parts that should be available in the 'Show In...'
	 *         prompt
	 */
	public ArrayList<?> getShowInPartIds() {
		MPerspective perspective = getPerspectiveStack().getSelectedElement();
		return new ArrayList<>(ModeledPageLayout.getIds(perspective,
				ModeledPageLayout.SHOW_IN_PART_TAG));
	}

	/**
	 * The user successfully performed a Show In... action on the specified
	 * part. Update the list of Show In items accordingly.
	 *
	 * @param partId
	 *            the id of the part that the action was performed on
	 */
	public void performedShowIn(String partId) {
		mruShowInPartIds.remove(partId);
		mruShowInPartIds.add(0, partId);
	}

	/**
	 * Sorts the given collection of show in target part ids in MRU order.
	 *
	 * @param partIds
	 *            the collection of part ids to rearrange
	 */
	public void sortShowInPartIds(ArrayList<?> partIds) {
		Collections.sort(partIds, new Comparator<Object>() {
			@Override
			public int compare(Object ob1, Object ob2) {
				int index1 = mruShowInPartIds.indexOf(ob1);
				int index2 = mruShowInPartIds.indexOf(ob2);
				if (index1 != -1 && index2 == -1)
					return -1;
				if (index1 == -1 && index2 != -1)
					return 1;
				return index1 - index2;
			}
		});
	}

    /**
     * See IWorkbenchPage.
     */
    @Override
	public IViewReference[] getViewReferences() {
		return getViewReferences(false);
	}

	private IViewReference[] getViewReferences(boolean allPerspectives) {
		MPerspective perspective = getCurrentPerspective();
		if (perspective != null) {
			int scope = allPerspectives ? WINDOW_SCOPE : EModelService.PRESENTATION;
			Set<MUIElement> parts = new HashSet<>();
			List<MPlaceholder> placeholders = modelService.findElements(window, null, MPlaceholder.class, null, scope);
			parts.addAll(placeholders);
			parts.addAll(modelService.findElements(window, null, MPart.class, null, scope));
			List<IViewReference> visibleReferences = new ArrayList<>();
			for (ViewReference reference : viewReferences) {
				MPart model = reference.getModel();
				// The part may be linked in either directly or via a
				// placeholder. In the latter case we can look directly
				// at the part's curSharedRef since we're only considering
				// parts visible in the current perspective
				if (parts.contains(model) && model.isToBeRendered()
						&& (model.getCurSharedRef() == null || model.getCurSharedRef().isToBeRendered())) {
					// only rendered placeholders are valid view references
					visibleReferences.add(reference);
				}
			}
			return visibleReferences.toArray(new IViewReference[visibleReferences.size()]);
		}
		return new IViewReference[0];
	}

	/**
	 * See IWorkbenchPage.
	 */
    @Override
	public IViewPart[] getViews() {
		IViewReference[] viewReferences = getViewReferences();
		int length = viewReferences.length;
		IViewPart[] views = new IViewPart[length];
		for (int i = 0; i < length; i++) {
			views[i] = viewReferences[i].getView(true);
		}
		return views;
	}



    /**
	 * See IWorkbenchPage.
	 */
    @Override
	public IWorkbenchWindow getWorkbenchWindow() {
		return legacyWindow;
    }

    /**
     * Implements IWorkbenchPage
     *
     * @see org.eclipse.ui.IWorkbenchPage#getWorkingSet()
     * @since 2.0
     * @deprecated individual views should store a working set if needed
     */
    @Deprecated
	@Override
	public IWorkingSet getWorkingSet() {
        return workingSet;
    }

    /**
     * @see IWorkbenchPage
     */
    @Override
	public void hideActionSet(String actionSetID) {
		MPerspective mpersp = getCurrentPerspective();
		if (mpersp == null)
			return;

		Perspective persp = getActivePerspective();
		if (persp != null) {
			ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();

			IActionSetDescriptor desc = reg.findActionSet(actionSetID);
			if (desc != null) {
				persp.removeActionSet(desc);
			}
			legacyWindow.updateActionSets();
			legacyWindow.firePerspectiveChanged(this, getPerspective(), CHANGE_ACTION_SET_HIDE);
		}
		String tag = ModeledPageLayout.ACTION_SET_TAG + actionSetID;
		addHiddenItems(tag);
    }

	@Override
	public void hideView(IViewReference view) {
    	if (view != null) {
    		for (IViewReference reference : getViewReferences()) {
    			if (reference == view) {
					hidePart(((ViewReference) view).getModel(), true, true, false);
					break;
    			}
    		}
		}
	}

	@Override
	public void hideView(IViewPart view) {
		if (view != null) {
			MPart part = findPart(view);
			if (part != null) {
				hidePart(part, true, true, false);
			}
		}
	}

    /**
     * Initialize the page.
     *
     * @param w
     *            the parent window
     * @param layoutID
     *            may be <code>null</code> if restoring from file
     * @param input
     *            the page input
     * @param openExtras
     *            whether to process the perspective extras preference
     */
	private void init(WorkbenchWindow w, String layoutID, IAdaptable input, boolean openExtras) {
		// Save args.
		this.legacyWindow = w;
        this.input = input;
        actionSets = new ActionSetManager(w);
		initActionSetListener();
		initMaxFileSize();
	}

	private void initMaxFileSize() {
		IPreferenceStore preferenceStore = PrefUtil.getInternalPreferenceStore();
		maxFileSize = preferenceStore.getLong(IPreferenceConstants.LARGE_DOC_SIZE_FOR_EDITORS);
		checkDocumentSize = maxFileSize != 0;
	}

	@PostConstruct
	public void setup(MApplication application, EModelService modelService, IEventBroker broker,
			MWindow window, EPartService partService) {
		this.application = application;
		this.modelService = modelService;
		this.broker = broker;
		this.window = window;
		this.partService = partService;
		selectionService = ContextInjectionFactory.make(SelectionService.class, window.getContext());

		partService.addPartListener(e4PartListener);

		// create editor references for all editors
		List<MPart> editors = modelService.findElements(window,
				CompatibilityEditor.MODEL_ELEMENT_ID, MPart.class, null,
				EModelService.IN_ANY_PERSPECTIVE | EModelService.OUTSIDE_PERSPECTIVE
						| EModelService.IN_SHARED_AREA);
		for (MPart editor : editors) {
			createEditorReferenceForPart(editor, null, editor.getElementId(), null);
		}

		// create view references for rendered view placeholders
		List<MPlaceholder> placeholders = modelService.findElements(window, null,
				MPlaceholder.class, null, EModelService.IN_ANY_PERSPECTIVE
						| EModelService.OUTSIDE_PERSPECTIVE);
		for (MPlaceholder placeholder : placeholders) {
			if (placeholder.isToBeRendered()) {
				MUIElement ref = placeholder.getRef();
				if (ref instanceof MPart) {
					MPart part = (MPart) ref;
					String uri = part.getContributionURI();
					if (uri.equals(CompatibilityPart.COMPATIBILITY_VIEW_URI)) {
						createViewReferenceForPart(part, part.getElementId());
					}
				}
			}
		}

		broker.subscribe(UIEvents.ElementContainer.TOPIC_SELECTEDELEMENT, selectionHandler);
		broker.subscribe(UIEvents.UIElement.TOPIC_WIDGET, widgetHandler);
		broker.subscribe(UIEvents.UIElement.TOPIC_TOBERENDERED, referenceRemovalEventHandler);
		broker.subscribe(UIEvents.Contribution.TOPIC_OBJECT, firingHandler);
		broker.subscribe(UIEvents.ElementContainer.TOPIC_CHILDREN, childrenHandler);

		// Bug 479126 PERSPECTIVE_BAR_EXTRAS setting not taken into account
		createPerspectiveBarExtras();

		MPerspectiveStack perspectiveStack = getPerspectiveStack();
		if (perspectiveStack != null) {
			extendPerspectives(perspectiveStack);
		}

		IPerspectiveRegistry registry = getWorkbenchWindow().getWorkbench()
				.getPerspectiveRegistry();
		for (MPerspective perspective : perspectiveStack.getChildren()) {
			IPerspectiveDescriptor desc = registry
					.findPerspectiveWithId(perspective.getElementId());
			if (desc != null) {
				sortedPerspectives.add(desc);
			}
		}

		MPerspective selectedPerspective = perspectiveStack.getSelectedElement();
		if (selectedPerspective != null) {
			IPerspectiveDescriptor desc = registry.findPerspectiveWithId(selectedPerspective
					.getElementId());
			if (desc != null) {
				sortedPerspectives.remove(desc);
				sortedPerspectives.add(desc);
			}
		}
		restoreWorkingSets();
		restoreShowInMruPartIdsList();
		configureExistingWindows();
    }

	/*
	 * Perform any configuration required for an existing MWindow. The
	 * association of an MWindow to the WorkbenchWindow/WorkbenchPage can occur
	 * at different times (see Bug 454056 for details).
	 */
	private void configureExistingWindows() {
		List<MArea> elements = modelService.findElements(window, null, MArea.class, null);
		for (MArea area : elements) {
			Object widget = area.getWidget();
			if (widget instanceof Control) {
				installAreaDropSupport((Control) widget);
			}
		}
	}

	public void restoreWorkingSets() {
		String workingSetName = getWindowModel().getPersistedState().get(
				IWorkbenchConstants.TAG_WORKING_SET);
		if (workingSetName != null) {
			AbstractWorkingSetManager workingSetManager = (AbstractWorkingSetManager) getWorkbenchWindow()
					.getWorkbench().getWorkingSetManager();
			setWorkingSet(workingSetManager.getWorkingSet(workingSetName));
		}

		String workingSetMemString = getWindowModel().getPersistedState().get(
				IWorkbenchConstants.TAG_WORKING_SETS);
		if (workingSetMemString != null) {
			IMemento workingSetMem;
			try {
				workingSetMem = XMLMemento.createReadRoot(new StringReader(workingSetMemString));
				IMemento[] workingSetChildren = workingSetMem
						.getChildren(IWorkbenchConstants.TAG_WORKING_SET);
				List<IWorkingSet> workingSetList = new ArrayList<>(workingSetChildren.length);
				for (IMemento memento : workingSetChildren) {
					IWorkingSet set = getWorkbenchWindow().getWorkbench().getWorkingSetManager()
							.getWorkingSet(memento.getID());
					if (set != null) {
						workingSetList.add(set);
					}
				}

				workingSets = workingSetList.toArray(new IWorkingSet[workingSetList.size()]);
			} catch (WorkbenchException e) {
				StatusManager.getManager().handle(
						new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR,
								WorkbenchMessages.WorkbenchPage_problemRestoringTitle, e));
			}
		}

		aggregateWorkingSetId = getWindowModel().getPersistedState().get(
				ATT_AGGREGATE_WORKING_SET_ID);
	}

	private void restoreShowInMruPartIdsList() {
		String mruList = getWindowModel().getPersistedState().get(IWorkbenchConstants.TAG_SHOW_IN_TIME);
		if (mruList != null) {
			try {
				IMemento memento = XMLMemento.createReadRoot(new StringReader(mruList));
				IMemento[] mementoChildren = memento.getChildren();
				for (IMemento child : mementoChildren) {
					mruShowInPartIds.add(child.getID());
				}
			} catch (WorkbenchException e) {
				StatusManager.getManager().handle(
						new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR,
								WorkbenchMessages.WorkbenchPage_problemRestoringTitle, e));
			}
		}
	}

	@PreDestroy
	public void saveWorkingSets() {
		// Save working set if set
		if (workingSet != null) {
			getWindowModel().getPersistedState().put(IWorkbenchConstants.TAG_WORKING_SET,
					workingSet.getName());
		} else {
			getWindowModel().getPersistedState().remove(IWorkbenchConstants.TAG_WORKING_SET);
		}

		List<String> workingSetNames = new ArrayList<>(workingSets.length);
		for (IWorkingSet workingSet : workingSets) {
			workingSetNames.add(workingSet.getName());
		}
		saveMemento(IWorkbenchConstants.TAG_WORKING_SETS, IWorkbenchConstants.TAG_WORKING_SET, workingSetNames);

		getWindowModel().getPersistedState().put(ATT_AGGREGATE_WORKING_SET_ID,
				aggregateWorkingSetId);
	}

	@PreDestroy
	public void saveShowInMruPartIdsList() {
		saveMemento(IWorkbenchConstants.TAG_SHOW_IN_TIME, IWorkbenchConstants.TAG_ID, mruShowInPartIds);
	}

	private void saveMemento(String rootType, String childType, Collection<String> ids) {
		XMLMemento memento = XMLMemento.createWriteRoot(rootType);
		for (String id : ids) {
			memento.createChild(childType, id);
		}
		StringWriter writer = new StringWriter();
		try {
			memento.save(writer);
			getWindowModel().getPersistedState().put(rootType, writer.getBuffer().toString());
		} catch (IOException e) {
			// Simply don't store the settings
			StatusManager.getManager().handle(
					new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.ERROR, WorkbenchMessages.SavingProblem, e));
		}
	}

	/**
	 * Extends the perspectives within the given stack with action set
	 * contributions from the <code>perspectiveExtensions</code> extension
	 * point.
	 *
	 * @param perspectiveStack
	 *            the stack that contain the perspectives to be extended
	 */
	private void extendPerspectives(MPerspectiveStack perspectiveStack) {
		for (MPerspective perspective : perspectiveStack.getChildren()) {
			String id = perspective.getElementId();
			IPerspectiveDescriptor desc = getWorkbenchWindow().getWorkbench()
					.getPerspectiveRegistry().findPerspectiveWithId(id);
			if (desc != null) {
				MPerspective temporary = modelService.createModelElement(MPerspective.class);
				ModeledPageLayout modelLayout = new ModeledPageLayout(window, modelService,
						partService, temporary, desc, this, true);

				PerspectiveExtensionReader reader = new PerspectiveExtensionReader();
				reader.setIncludeOnlyTags(new String[] { IWorkbenchRegistryConstants.TAG_ACTION_SET });
				reader.extendLayout(null, id, modelLayout);

				addActionSet(perspective, temporary);
			}
		}
	}

	ArrayList<String> getPerspectiveExtensionActionSets(String id) {
		IPerspectiveDescriptor desc = getWorkbenchWindow().getWorkbench().getPerspectiveRegistry()
				.findPerspectiveWithId(id);
		if (desc != null) {
			MPerspective temporary = modelService.createModelElement(MPerspective.class);
			ModeledPageLayout modelLayout = new ModeledPageLayout(window, modelService,
					partService, temporary, desc, this, true);

			PerspectiveExtensionReader reader = new PerspectiveExtensionReader();
			reader.setIncludeOnlyTags(new String[] { IWorkbenchRegistryConstants.TAG_ACTION_SET });
			reader.extendLayout(null, id, modelLayout);
			return new ArrayList<>(ModeledPageLayout.getIds(temporary, ModeledPageLayout.ACTION_SET_TAG));
		}
		return null;
	}

	/**
	 * Copies action set extensions from the temporary perspective to the other
	 * one.
	 *
	 * @param perspective
	 *            the perspective to copy action set contributions to
	 * @param temporary
	 *            the perspective to copy action set contributions from
	 */
	private void addActionSet(MPerspective perspective, MPerspective temporary) {
		List<String> tags = perspective.getTags();
		List<String> extendedTags = temporary.getTags();
		for (String extendedTag : extendedTags) {
			if (!tags.contains(extendedTag)) {
				tags.add(extendedTag);
			}
		}
	}

	/**
	 * Installs drop support into the shared area so that editors can be opened
	 * by dragging and dropping files into it.
	 *
	 * @param control
	 *            the control to attach the drop support to
	 */
	private void installAreaDropSupport(Control control) {
		if (!dndSupportInstalled) {
			WorkbenchWindowConfigurer configurer = legacyWindow.getWindowConfigurer();
			DropTargetListener dropTargetListener = configurer.getDropTargetListener();
			if (dropTargetListener != null) {
				DropTarget dropTarget = new DropTarget(control, DND.DROP_DEFAULT | DND.DROP_COPY
						| DND.DROP_LINK);
				dropTarget.setTransfer(configurer.getTransfers());
				dropTarget.addDropListener(dropTargetListener);
			}
			dndSupportInstalled = true;
		}
	}

	private List<MPartStack> getPartStacks(MPerspective perspective) {
		if (perspective == null) {
			return Collections.emptyList();
		}
		return modelService.findElements(perspective, null, MPartStack.class, null);
	}

	private EventHandler selectionHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			Object changedElement = event.getProperty(UIEvents.EventTags.ELEMENT);

			if (!(changedElement instanceof MPerspectiveStack)) {
				return;
			}

			List<MPerspectiveStack> theStack = modelService.findElements(window, null,
					MPerspectiveStack.class, null);
			if (theStack.isEmpty()) {
				return;
			} else if (!theStack.isEmpty() && changedElement != theStack.get(0)) {
				return;
			}

			MPerspective oldPersp = (MPerspective) event.getProperty(UIEvents.EventTags.OLD_VALUE);
			MPerspective newPersp = (MPerspective) event.getProperty(UIEvents.EventTags.NEW_VALUE);
			// updatePerspectiveActionSets(oldPersp, newPersp);

			// ((CoolBarToTrimManager)
			// legacyWindow.getCoolBarManager2()).updateAll(true);
			// legacyWindow.menuManager.updateAll(true);

			List<MPart> hiddenParts = new ArrayList<>();
			List<MPart> visibleParts = new ArrayList<>();

			List<MPartStack> oldStacks = getPartStacks(oldPersp);
			List<MPartStack> newStacks = getPartStacks(newPersp);

			for (MPartStack oldStack : oldStacks) {
				MStackElement element = oldStack.getSelectedElement();
				if (element instanceof MPlaceholder) {
					hiddenParts.add((MPart) ((MPlaceholder) element).getRef());
				} else if (element instanceof MPart) {
					hiddenParts.add((MPart) element);
				}
			}

			for (MPartStack newStack : newStacks) {
				MStackElement element = newStack.getSelectedElement();
				if (element instanceof MPlaceholder) {
					visibleParts.add((MPart) ((MPlaceholder) element).getRef());
				} else if (element instanceof MPart) {
					visibleParts.add((MPart) element);
				}
			}

			List<MPart> ignoredParts = new ArrayList<>();
			for (MPart hiddenPart : hiddenParts) {
				if (visibleParts.contains(hiddenPart)) {
					ignoredParts.add(hiddenPart);
				}
			}

			hiddenParts.removeAll(ignoredParts);
			visibleParts.removeAll(ignoredParts);

			for (MPart hiddenPart : hiddenParts) {
				firePartHidden(hiddenPart);
			}

			for (MPart visiblePart : visibleParts) {
				firePartVisible(visiblePart);
			}

			updateActionSets(getPerspective(oldPersp), getPerspective(newPersp));

			// might've been set to null if we were closing the perspective
			if (newPersp != null) {
				IPerspectiveDescriptor perspective = getPerspectiveDesc(newPersp
						.getElementId());
				legacyWindow.firePerspectiveActivated(WorkbenchPage.this, perspective);

				sortedPerspectives.remove(perspective);
				sortedPerspectives.add(perspective);
			}
			legacyWindow.updateActionSets();
		}
	};

	/**
     * See IWorkbenchPage.
     */
    @Override
	public boolean isPartVisible(IWorkbenchPart part) {
		MPart mpart = findPart(part);
		return mpart == null ? false : partService.isPartVisible(mpart);
    }

	public MUIElement findSharedArea() {
		MPerspective perspective = getPerspectiveStack().getSelectedElement();
		return perspective == null ? null : modelService.find(IPageLayout.ID_EDITOR_AREA,
				perspective);
	}

    /**
     * See IWorkbenchPage.
     */
    @Override
	public boolean isEditorAreaVisible() {
		MUIElement find = findSharedArea();
		return find == null ? false : find.isVisible() && find.isToBeRendered();
    }

    @Override
	public boolean isPageZoomed() {
		List<String> maxTag = new ArrayList<>();
		maxTag.add(IPresentationEngine.MAXIMIZED);
		List<Object> maxElements = modelService.findElements(window, null, null, maxTag);
		return maxElements.size() > 0;
    }


	// /**
	// * This method is called when the page is activated.
	// */
	// protected void onActivate() {
	// composite.setVisible(true);
	// Perspective persp = getActivePerspective();
	//
	// if (persp != null) {
	// persp.onActivate();
	// updateVisibility(null, persp);
	// }
	// }
	//
	// /**
	// * This method is called when the page is deactivated.
	// */
	// protected void onDeactivate() {
	// makeActiveEditor(null);
	// makeActive(null);
	// if (getActivePerspective() != null) {
	// getActivePerspective().onDeactivate();
	// }
	// composite.setVisible(false);
	// }

    /**
     * See IWorkbenchPage.
     */
    @Override
	public void reuseEditor(IReusableEditor editor, IEditorInput input) {

        // Rather than calling editor.setInput on the editor directly, we do it through the part reference.
        // This case lets us detect badly behaved editors that are not firing a PROP_INPUT event in response
        // to the input change... but if all editors obeyed their API contract, the "else" branch would be
        // sufficient.

		// TODO compat: should we be talking to the editor reference here
		editor.setInput(input);
        navigationHistory.markEditor(editor);
    }

    /**
     * See IWorkbenchPage.
     */
    @Override
	public IEditorPart openEditor(IEditorInput input, String editorID)
            throws PartInitException {
        return openEditor(input, editorID, true, MATCH_INPUT);
    }

    /**
     * See IWorkbenchPage.
     */
    @Override
	public IEditorPart openEditor(IEditorInput input, String editorID,
			boolean activate) throws PartInitException {
		return openEditor(input, editorID, activate, MATCH_INPUT);
    }

    /**
     * See IWorkbenchPage.
     */
    @Override
	public IEditorPart openEditor(final IEditorInput input,
            final String editorID, final boolean activate, final int matchFlags)
            throws PartInitException {
    	return openEditor(input, editorID, activate, matchFlags, null, true);
    }

	/**
	 * This is not public API but for use internally. editorState can be
	 * <code>null</code>.
	 *
	 * @param input
	 *            the input to open the editor with
	 * @param editorID
	 *            the id of the editor to open
	 * @param activate
	 *            <tt>true</tt> if the editor should be activated,
	 *            <tt>false</tt> otherwise
	 * @param matchFlags
	 *            a bit mask consisting of zero or more of the MATCH_* constants
	 *            OR-ed together
	 * @param editorState
	 *            the previously saved state of the editor as a memento, this
	 *            may be <tt>null</tt>
	 * @param notify
	 *            <tt>true</tt> if the perspective should fire off events about
	 *            the editors being opened, <tt>false</tt> otherwise
	 * @return the opened editor
	 * @exception PartInitException
	 *                if the editor could not be created or initialized
	 */
	public IEditorPart openEditor(final IEditorInput input, final String editorID,
			final boolean activate, final int matchFlags, final IMemento editorState,
			final boolean notify) throws PartInitException {
        if (input == null || editorID == null) {
            throw new IllegalArgumentException();
        }

        final IEditorPart result[] = new IEditorPart[1];
        final PartInitException ex[] = new PartInitException[1];
		BusyIndicator.showWhile(legacyWindow.getWorkbench().getDisplay(),
                new Runnable() {
                    @Override
					public void run() {
                        try {
					result[0] = busyOpenEditor(input, editorID, activate, matchFlags, editorState,
							notify);
                        } catch (PartInitException e) {
                            ex[0] = e;
                        }
                    }
                });
        if (ex[0] != null) {
			throw ex[0];
		}
        return result[0];
    }



    /**
     * @see #openEditor(IEditorInput, String, boolean, int)
	 */
	private IEditorPart busyOpenEditor(IEditorInput input, String editorId, boolean activate,
			int matchFlags, IMemento editorState, boolean notify) throws PartInitException {

		if (input == null || editorId == null) {
			throw new IllegalArgumentException();
		}

		// Special handling for external editors (they have no tabs...)
		if ("org.eclipse.ui.systemExternalEditor".equals(editorId)) { //$NON-NLS-1$
			IPathEditorInput fileInput = getPathEditorInput(input);
			if (fileInput == null) {
				throw new PartInitException(WorkbenchMessages.EditorManager_systemEditorError);
			}

			String fullPath = fileInput.getPath().toOSString();
			Program.launch(fullPath);
			return null;
		}

		IEditorDescriptor desc = getWorkbenchWindow().getWorkbench()
				.getEditorRegistry().findEditor(editorId);
		if (desc != null && !desc.isOpenExternal() && isLargeDocument(input)) {
			desc = getAlternateEditor();
			if (desc == null) {
				// the user pressed cancel in the editor selection dialog
				return null;
			}
		}
		if (desc == null) {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_unknownEditorIDMessage, editorId));
		}

		setEditorAreaVisible(true);

		IEditorReference[] editorReferences = findEditors(input, editorId, matchFlags);
		if (editorReferences.length != 0) {
			IEditorPart editor = editorReferences[0].getEditor(true);
			if (editor instanceof IShowEditorInput) {
				((IShowEditorInput) editor).showEditorInput(input);
			}

			partService.showPart(((EditorReference) editorReferences[0]).getModel(),
					PartState.VISIBLE);

			if (activate) {
				activate(editor);
			}

			recordEditor(input, desc);
			return editor;
		} else if (desc.isInternal()) {
			// look for an editor to reuse
			EditorReference reusableEditorRef = (EditorReference) ((TabBehaviour) Tweaklets
					.get(TabBehaviour.KEY)).findReusableEditor(this);
			if (reusableEditorRef != null) {
				IEditorPart reusableEditor = reusableEditorRef.getEditor(false);
				if (editorId.equals(reusableEditorRef.getId())
						&& reusableEditor instanceof IReusableEditor) {
					// reusable editors that share the same id are okay
					recordEditor(input, desc);
					reuseEditor((IReusableEditor) reusableEditor, input);

					MPart editor = reusableEditorRef.getModel();
					partService.showPart(editor, PartState.VISIBLE);
					if (activate) {
						partService.activate(editor);
					} else {
						updateActiveEditorSources(editor);
					}
					return reusableEditor;
				}
				// should have saved already if necessary, close this editor, a
				// new one will be opened
				closeEditor(reusableEditorRef, false);
			}
		} else if (desc.isOpenExternal()) {
			openExternalEditor((EditorDescriptor) desc, input);
			// no editor parts for external editors, return null
			return null;
		}

		MPart editor = partService.createPart(CompatibilityEditor.MODEL_ELEMENT_ID);
		editor.getTags().add(editorId);
		EditorReference ref = createEditorReferenceForPart(editor, input, editorId, editorState);
		partService.showPart(editor, PartState.VISIBLE);

		CompatibilityEditor compatibilityEditor = (CompatibilityEditor) editor.getObject();
		if (compatibilityEditor == null) {
			return null;
		}

		if (activate) {
			partService.activate(editor);
		} else {
			updateActiveEditorSources(editor);
		}

		if (notify) {
			legacyWindow.firePerspectiveChanged(this, getPerspective(), ref, CHANGE_EDITOR_OPEN);
			legacyWindow.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_OPEN);
		}

		recordEditor(input, desc);
		return compatibilityEditor.getEditor();
    }

	private void recordEditor(IEditorInput input, IEditorDescriptor descriptor) {
		EditorHistory history = ((Workbench) legacyWindow.getWorkbench()).getEditorHistory();
		history.add(input, descriptor);
	}

	private static IEditorDescriptor getAlternateEditor() {
		Shell shell = ProgressManagerUtil.getDefaultParent();
		EditorSelectionDialog dialog = new EditorSelectionDialog(shell) {
			@Override
			protected IDialogSettings getDialogSettings() {
				IDialogSettings result = new DialogSettings("EditorSelectionDialog"); //$NON-NLS-1$
				result.put(EditorSelectionDialog.STORE_ID_INTERNAL_EXTERNAL, true);
				return result;
			}
		};
		dialog.setMessage(WorkbenchMessages.EditorManager_largeDocumentWarning);

		if (dialog.open() == Window.OK)
			return dialog.getSelectedEditor();
		return null;
	}

	boolean isLargeDocument(IEditorInput editorInput) {

		if (!checkDocumentSize)
			return false;

		if (!(editorInput instanceof IPathEditorInput))
			return false; // we know nothing about it

		try {
			IPath path = ((IPathEditorInput) editorInput).getPath();
			File file = new File(path.toOSString());
			return file.length() > maxFileSize;
		} catch (Exception e) {
			// ignore exceptions
			return false;
		}
	}

    /**
     * See IWorkbenchPage.
     */
    @Override
	public boolean isEditorPinned(IEditorPart editor) {
    	WorkbenchPartReference ref = (WorkbenchPartReference)getReference(editor);
        return ref != null && ref.isPinned();
    }



    /**
     * Removes an IPartListener from the part service.
     */
    @Override
	public void removePartListener(IPartListener l) {
		partListenerList.remove(l);
    }

    /**
     * Removes an IPartListener from the part service.
     */
    @Override
	public void removePartListener(IPartListener2 l) {
		partListener2List.remove(l);
    }

    /**
     * Implements IWorkbenchPage
     *
     * @see org.eclipse.ui.IWorkbenchPage#removePropertyChangeListener(IPropertyChangeListener)
     * @since 2.0
     * @deprecated individual views should store a working set if needed and
     *             register a property change listener directly with the
     *             working set manager to receive notification when the view
     *             working set is removed.
     */
    @Deprecated
	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
        propertyChangeListeners.remove(listener);
    }

    @Override
	public void removeSelectionListener(ISelectionListener listener) {
		selectionService.removeSelectionListener(listener);
    }

    @Override
	public void removeSelectionListener(String partId,
            ISelectionListener listener) {
		selectionService.removeSelectionListener(partId, listener);
    }

    @Override
	public void removePostSelectionListener(ISelectionListener listener) {
		selectionService.removePostSelectionListener(listener);
    }

    @Override
	public void removePostSelectionListener(String partId,
            ISelectionListener listener) {
		selectionService.removePostSelectionListener(partId, listener);
    }



    /**
     * Resets the layout for the perspective. The active part in the old layout
     * is activated in the new layout for consistent user context.
     */
    @Override
	public void resetPerspective() {
		MPerspectiveStack perspStack = getPerspectiveStack();
		MPerspective persp = perspStack.getSelectedElement();
		if (persp == null)
			return;

		// HACK!! the 'perspective' field doesn't match reality...
		IPerspectiveDescriptor desc = PlatformUI.getWorkbench().getPerspectiveRegistry()
				.findPerspectiveWithId(persp.getElementId());
		if (desc == null)
			return;

		// send out reset notification
		legacyWindow.firePerspectiveChanged(this, desc, CHANGE_RESET);

		// collect all the parts under the current perspective
		List<MPart> perspectiveParts = modelService.findElements(persp, null, MPart.class, null);
		// find the shared area
		MUIElement area = findSharedArea();
		if (area != null) {
			// remove all editors in the shared area from the list of parts
			perspectiveParts.removeAll(modelService.findElements(area,
					CompatibilityEditor.MODEL_ELEMENT_ID, MPart.class, null));
		}

		List<MPart> dirtyParts = new ArrayList<>();
		List<IWorkbenchPart> partsToSave = new ArrayList<>();
		// iterate over the list of parts to find dirty parts
		for (MPart currentPart : perspectiveParts) {
			if (currentPart.isDirty()) {
				Object object = currentPart.getObject();
				if (object == null) {
					continue;
				} else if (object instanceof CompatibilityPart) {
					IWorkbenchPart workbenchPart = getWrappedPart((CompatibilityPart) object);
					if (workbenchPart == null) {
						continue;
					}
					ISaveablePart saveable = SaveableHelper.getSaveable(workbenchPart);
					if (saveable == null || !saveable.isSaveOnCloseNeeded()) {
						continue;
					}
					partsToSave.add(workbenchPart);
				}

				dirtyParts.add(currentPart);
			}
		}

		SaveablesList saveablesList = null;
		Object postCloseInfo = null;
		if (partsToSave.size() > 0) {
			saveablesList = (SaveablesList) getWorkbenchWindow().getService(
					ISaveablesLifecycleListener.class);
			postCloseInfo = saveablesList.preCloseParts(partsToSave, true,
					this.getWorkbenchWindow());
			if (postCloseInfo == null) {
				// cancel
				// We're not going through with the reset, so it is
				// complete.
				legacyWindow.firePerspectiveChanged(this, desc, CHANGE_RESET_COMPLETE);
				return;
			}
		}

		modelService.resetPerspectiveModel(persp, window);

		if (saveablesList != null) {
			saveablesList.postClose(postCloseInfo);
		}

		boolean revert = false;
		if (desc instanceof PerspectiveDescriptor) {
			PerspectiveDescriptor perspectiveDescriptor = (PerspectiveDescriptor) desc;
			revert = perspectiveDescriptor.isPredefined()
					&& !perspectiveDescriptor.hasCustomDefinition();
		}

		MPerspective dummyPerspective = null;
		if (!revert) {
			dummyPerspective = (MPerspective) modelService.cloneSnippet(application, desc.getId(),
					window);
			if (dummyPerspective != null) {
				handleNullRefPlaceHolders(dummyPerspective, window);
			}
		}

		if (dummyPerspective == null) {
			// instantiate a dummy perspective perspective
			dummyPerspective = modelService.createModelElement(MPerspective.class);
			dummyPerspective.setElementId(persp.getElementId());

			IPerspectiveFactory factory = ((PerspectiveDescriptor) desc).createFactory();
			ModeledPageLayout modelLayout = new ModeledPageLayout(window, modelService,
					partService, dummyPerspective, desc, this, true);
			factory.createInitialLayout(modelLayout);

			PerspectiveTagger.tagPerspective(dummyPerspective, modelService);
			PerspectiveExtensionReader reader = new PerspectiveExtensionReader();
			reader.extendLayout(getExtensionTracker(), desc.getId(), modelLayout);
		}

		String hiddenItems = dummyPerspective.getPersistedState().get(ModeledPageLayout.HIDDEN_ITEMS_KEY);
		persp.getPersistedState().put(ModeledPageLayout.HIDDEN_ITEMS_KEY, hiddenItems);

		legacyWindow.getMenuManager().updateAll(true);
		// ((ICoolBarManager2) ((WorkbenchWindow)
		// getWorkbenchWindow()).getCoolBarManager2())
		// .resetItemOrder();

		// Hide placeholders for parts that exist in the 'global' areas
		modelService.hideLocalPlaceholders(window, dummyPerspective);

		int dCount = dummyPerspective.getChildren().size();
		while (dummyPerspective.getChildren().size() > 0) {
			MPartSashContainerElement dChild = dummyPerspective.getChildren().remove(0);
			persp.getChildren().add(dChild);
		}

		while (persp.getChildren().size() > dCount) {
			MUIElement child = persp.getChildren().get(0);
			child.setToBeRendered(false);
			persp.getChildren().remove(0);
		}

		List<MWindow> existingDetachedWindows = new ArrayList<>();
		existingDetachedWindows.addAll(persp.getWindows());

		// Move any detached windows from template to perspective
		while (dummyPerspective.getWindows().size() > 0) {
			MWindow detachedWindow = dummyPerspective.getWindows().remove(0);
			persp.getWindows().add(detachedWindow);
		}

		// Remove original windows.  Can't remove them first or the MParts will be disposed
		for (MWindow detachedWindow : existingDetachedWindows) {
			detachedWindow.setToBeRendered(false);
			persp.getWindows().remove(detachedWindow);
		}

		// deactivate and activate other action sets as
		Perspective oldPersp = getPerspective(persp);
		Perspective dummyPersp = getPerspective(dummyPerspective);
		updateActionSets(oldPersp, dummyPersp);
		oldPersp.getAlwaysOnActionSets().clear();
		oldPersp.getAlwaysOnActionSets().addAll(dummyPersp.getAlwaysOnActionSets());
		oldPersp.getAlwaysOffActionSets().clear();
		oldPersp.getAlwaysOffActionSets().addAll(dummyPersp.getAlwaysOffActionSets());

		modelToPerspectiveMapping.remove(dummyPerspective);

		// partly fixing toolbar refresh issue, see bug 383569 comment 10
		legacyWindow.updateActionSets();

		// migrate the tags
		List<String> tags = persp.getTags();
		tags.clear();
		tags.addAll(dummyPerspective.getTags());

		// remove HIDDEN_EXPLICITLY tag from trim elements
		List<MTrimElement> trimElements = modelService.findElements(window, null,
				MTrimElement.class, null);
		for (MTrimElement mTrimElement : trimElements) {
			mTrimElement.getTags().remove(IPresentationEngine.HIDDEN_EXPLICITLY);
		}

		partService.requestActivation();

		// reset complete
		legacyWindow.firePerspectiveChanged(this, desc, CHANGE_RESET_COMPLETE);
		UIEvents.publishEvent(UIEvents.UILifeCycle.PERSPECTIVE_RESET, persp);
	}

	private void initActionSetListener() {
		// actionSets.addListener(new IPropertyListener() {
		// public void propertyChanged(Object source, int propId) {
		// if (source instanceof IActionSetDescriptor) {
		// final IActionSetDescriptor desc = (IActionSetDescriptor) source;
		// final String actionSetId = ModeledPageLayout.ACTION_SET_TAG +
		// desc.getId();
		// final MPerspective currentPerspective = getCurrentPerspective();
		// if (currentPerspective != null) {
		// final List<String> tags = currentPerspective.getTags();
		// if (propId == ActionSetManager.PROP_VISIBLE) {
		// if (!tags.contains(actionSetId)) {
		// tags.add(actionSetId);
		// }
		// } else if (propId == ActionSetManager.PROP_HIDDEN) {
		// tags.remove(actionSetId);
		// }
		// }
		// }
		// }
		// });
	}

    /**
     * See IWorkbenchPage
     */
    @Override
	public boolean saveAllEditors(boolean confirm) {
        return saveAllEditors(confirm, false, false);
    }

	/**
	 * @return {@link ISaveablePart} objects derived from {@link IWorkbenchPart}
	 *         's on this page
	 */
	public ISaveablePart[] getDirtyParts() {
		List<ISaveablePart> result = new ArrayList<>(3);
		IWorkbenchPartReference[] allParts = getSortedParts(true, true, true);
		for (IWorkbenchPartReference reference : allParts) {
			IWorkbenchPart part = reference.getPart(false);
			ISaveablePart saveable = SaveableHelper.getSaveable(part);
			if (saveable != null && !result.contains(saveable)) {
				if (saveable.isDirty()) {
					result.add(saveable);
				}
			}
		}
		return result.toArray(new ISaveablePart[result.size()]);
	}

	/**
	 * @return workbench parts which are dirty (implement or adapt to
	 *         {@link ISaveablePart}). Only parts matching different saveables
	 *         are returned.
	 */
	public IWorkbenchPart[] getDirtyWorkbenchParts() {
		List<IWorkbenchPart> result = new ArrayList<>(3);
		Map<ISaveablePart, IWorkbenchPart> saveables = new LinkedHashMap<>(3);
		IWorkbenchPartReference[] allParts = getSortedParts(true, true, true);
		for (IWorkbenchPartReference reference : allParts) {
			IWorkbenchPart part = reference.getPart(false);
			ISaveablePart saveable = SaveableHelper.getSaveable(part);
			if (saveable == null || !saveable.isDirty()) {
				continue;
			}
			IWorkbenchPart previousPart = saveables.get(saveable);
			if (previousPart != null) {
				// We have already a part claiming to handle this saveable.
				// See bug 470076 where a property view might return
				// saveable which is in turn just editor part
				if (previousPart == saveable) {
					// if the previous part matches saveable, we have a
					// perfect match already
					continue;
				}
				// if parts provide adapters to same saveable but
				// saveable itself is not a part, we can try to keep
				// editors and skip views
				if (part != saveable && previousPart instanceof IEditorPart) {
					continue;
				}
				// last part wins, since we don't want to return multiple parts
				// representing same saveables
				result.remove(previousPart);
			}
			result.add(part);
			saveables.put(saveable, part);
		}
		return result.toArray(new IWorkbenchPart[result.size()]);
	}

	public boolean saveAllEditors(boolean confirm, boolean closing, boolean addNonPartSources) {
		IWorkbenchPart[] parts = getDirtyWorkbenchParts();
		if (parts.length == 0) {
			return true;
		}
		// saveAll below expects a mutable list
		List<IWorkbenchPart> dirtyParts = new ArrayList<>(parts.length);
		for (IWorkbenchPart part : parts) {
			dirtyParts.add(part);
		}

		// If confirmation is required ..
		return saveAll(dirtyParts, confirm, closing, addNonPartSources, legacyWindow, legacyWindow);
	}

	public static boolean saveAll(List<IWorkbenchPart> dirtyParts, final boolean confirm, final boolean closing,
			boolean addNonPartSources, final IRunnableContext runnableContext,
			final IWorkbenchWindow workbenchWindow) {
		// clone the input list
		dirtyParts = new ArrayList<>(dirtyParts);

		if (closing) {
			// if the parts are going to be closed, then we only save those that
			// need to be saved when closed, see bug 272070
			removeSaveOnCloseNotNeededParts(dirtyParts);
		}

		SaveablesList saveablesList = (SaveablesList) PlatformUI.getWorkbench().getService(
				ISaveablesLifecycleListener.class);
		if (confirm) {
			return processSaveable2(dirtyParts) ? false : saveablesList.preCloseParts(dirtyParts, true, true,
					workbenchWindow, workbenchWindow) != null;
		}
		List<Saveable> modelsToSave = convertToSaveables(dirtyParts, closing, addNonPartSources);
		return modelsToSave.isEmpty() ? true : !saveablesList.saveModels(modelsToSave, workbenchWindow,
				runnableContext, closing);

	}

	/**
	 * Removes from the provided list parts that don't need to be saved on
	 * close.
	 *
	 * @param parts
	 *            the list of the parts (ISaveablePart)
	 */
	private static void removeSaveOnCloseNotNeededParts(List<IWorkbenchPart> parts) {
		for (Iterator<IWorkbenchPart> it = parts.iterator(); it.hasNext();) {
			IWorkbenchPart part = it.next();
			ISaveablePart saveable = SaveableHelper.getSaveable(part);
			if (saveable == null || !saveable.isSaveOnCloseNeeded()) {
				it.remove();
			}
		}
	}

	/**
	 * Processes all parts that implement ISaveablePart2 and removes them from
	 * the list.
	 *
	 * @param dirtyParts
	 *            the list of the parts
	 * @return true if cancelled
	 */
	private static boolean processSaveable2(List<IWorkbenchPart> dirtyParts) {
		boolean saveable2Processed = false;
		// Process all parts that implement ISaveablePart2.
		// These parts are removed from the list after saving
		// them. We then need to restore the workbench to
		// its previous state, for now this is just last
		// active perspective.
		// Note that the given parts may come from multiple
		// windows, pages and perspectives.
		ListIterator<IWorkbenchPart> listIterator = dirtyParts.listIterator();

		WorkbenchPage currentPage = null;
		Perspective currentPageOriginalPerspective = null;
		while (listIterator.hasNext()) {
			IWorkbenchPart part = listIterator.next();
			ISaveablePart2 saveable2 = SaveableHelper.getSaveable2(part);
			if (saveable2 != null) {
				WorkbenchPage page = (WorkbenchPage) part.getSite().getPage();
				if (!Util.equals(currentPage, page)) {
					if (currentPage != null && currentPageOriginalPerspective != null) {
						if (!currentPageOriginalPerspective.equals(currentPage
								.getActivePerspective())) {
							currentPage
									.setPerspective(currentPageOriginalPerspective.getDesc());
						}
					}
					currentPage = page;
					currentPageOriginalPerspective = page.getActivePerspective();
				}
				page.bringToTop(part);
				// try to save the part
				int choice = SaveableHelper.savePart(saveable2, page.getWorkbenchWindow(), true);
				if (choice == ISaveablePart2.CANCEL) {
					// If the user cancels, don't restore the previous
					// workbench state, as that will
					// be an unexpected switch from the current state.
					return true;
				} else if (choice != ISaveablePart2.DEFAULT) {
					saveable2Processed = true;
					listIterator.remove();
				}
			}
		}

		// try to restore the workbench to its previous state
		if (currentPage != null && currentPageOriginalPerspective != null) {
			if (!currentPageOriginalPerspective.equals(currentPage.getActivePerspective())) {
				currentPage.setPerspective(currentPageOriginalPerspective.getDesc());
			}
		}

		// if processing a ISaveablePart2 caused other parts to be
		// saved, remove them from the list presented to the user.
		if (saveable2Processed) {
			removeNonDirtyParts(dirtyParts);
		}

		return false;
	}

	private static void removeNonDirtyParts(List<IWorkbenchPart> parts) {
		ListIterator<IWorkbenchPart> listIterator;
		listIterator = parts.listIterator();
		while (listIterator.hasNext()) {
			ISaveablePart part = SaveableHelper.getSaveable(listIterator.next());
			if (part == null || !part.isDirty()) {
				listIterator.remove();
			}
		}
	}

	/**
	 * For each part (view or editor) in the given list, attempts to convert it
	 * to one or more saveable models. Duplicate models are removed. If closing
	 * is true, then models that will remain open in parts other than the given
	 * parts are removed.
	 *
	 * @param parts
	 *            the parts (list of IViewPart or IEditorPart)
	 * @param closing
	 *            whether the parts are being closed
	 * @param addNonPartSources
	 *            whether non-part sources should be added (true for the Save
	 *            All action, see bug 139004)
	 * @return the dirty models
	 */
	private static List<Saveable> convertToSaveables(List<IWorkbenchPart> parts, boolean closing,
			boolean addNonPartSources) {
		ArrayList<Saveable> result = new ArrayList<>();
		HashSet<Saveable> seen = new HashSet<>();
		for (IWorkbenchPart part : parts) {
			for (Saveable saveable : getSaveables(part)) {
				if (saveable.isDirty() && !seen.contains(saveable)) {
					seen.add(saveable);
					if (!closing
							|| closingLastPartShowingModel(saveable, parts, part.getSite()
									.getPage())) {
						result.add(saveable);
					}
				}
			}
		}
		if (addNonPartSources) {
			SaveablesList saveablesList = (SaveablesList) PlatformUI.getWorkbench().getService(
					ISaveablesLifecycleListener.class);
			ISaveablesSource[] nonPartSources = saveablesList.getNonPartSources();
			for (ISaveablesSource nonPartSource : nonPartSources) {
				for (Saveable saveable : nonPartSource.getSaveables()) {
					if (saveable.isDirty() && !seen.contains(saveable)) {
						seen.add(saveable);
						result.add(saveable);
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns the saveable models provided by the given part. If the part does
	 * not provide any models, a default model is returned representing the
	 * part.
	 *
	 * @param part
	 *            the workbench part
	 * @return the saveable models
	 */
	private static Saveable[] getSaveables(IWorkbenchPart part) {
		if (part instanceof ISaveablesSource) {
			ISaveablesSource source = (ISaveablesSource) part;
			return source.getSaveables();
		}
		return new Saveable[] { new DefaultSaveable(part) };
	}

	/**
	 * Returns true if, in the given page, no more parts will reference the
	 * given model if the given parts are closed.
	 *
	 * @param model
	 *            the model
	 * @param closingParts
	 *            the parts being closed (list of IViewPart or IEditorPart)
	 * @param page
	 *            the page
	 * @return <code>true</code> if no more parts in the page will reference the
	 *         given model, <code>false</code> otherwise
	 */
	private static boolean closingLastPartShowingModel(Saveable model, List<IWorkbenchPart> closingParts,
			IWorkbenchPage page) {
		HashSet<IWorkbenchPart> closingPartsWithSameModel = new HashSet<>();
		for (IWorkbenchPart part : closingParts) {
			Saveable[] models = getSaveables(part);
			if (Arrays.asList(models).contains(model)) {
				closingPartsWithSameModel.add(part);
			}
		}
		IWorkbenchPartReference[] pagePartRefs = ((WorkbenchPage) page).getSortedParts();
		HashSet<IWorkbenchPart> pagePartsWithSameModels = new HashSet<>();
		for (IWorkbenchPartReference partRef : pagePartRefs) {
			IWorkbenchPart part = partRef.getPart(false);
			if (part != null) {
				Saveable[] models = getSaveables(part);
				if (Arrays.asList(models).contains(model)) {
					pagePartsWithSameModels.add(part);
				}
			}
		}
		for (IWorkbenchPart part : closingPartsWithSameModel) {
			pagePartsWithSameModels.remove(part);
		}
		return pagePartsWithSameModels.isEmpty();
	}

	/**
	 * Saves the contents of the provided saveable and returns whether the
	 * operation succeeded or not.
	 *
	 * @param saveable
	 *            the saveable part to save
	 * @param part
	 * @param confirm
	 *            whether the user should be prompted for confirmation of the
	 *            save request
	 * @param closing
	 *            whether the part will be closed after the save operation has
	 *            completed, this may determine whether whether the save
	 *            operation will actually be invoked or not
	 * @return <code>true</code> if the saveable's contents has been persisted,
	 *         <code>false</code> otherwise
	 * @see ISaveablePart#isSaveOnCloseNeeded()
	 */
	public boolean saveSaveable(ISaveablePart saveable, IWorkbenchPart part, boolean confirm,
			boolean closing) {
		if (closing && !saveable.isSaveOnCloseNeeded()) {
			return true;
		}
		return SaveableHelper.savePart(saveable, part, legacyWindow, confirm);
	}

    /**
     * Saves an editors in the workbench. If <code>confirm</code> is <code>true</code>
     * the user is prompted to confirm the command.
     *
     * @param confirm
     *            if user confirmation should be sought
     * @return <code>true</code> if the command succeeded, or <code>false</code>
     *         if the user cancels the command
     */
    @Override
	public boolean saveEditor(IEditorPart editor, boolean confirm) {
		return saveSaveable(editor, editor, confirm, false);
    }

	@Override
	public void savePerspective() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void savePerspectiveAs(IPerspectiveDescriptor perspective) {
		MPerspective visiblePerspective = getPerspectiveStack().getSelectedElement();
		// get the original perspective
		String originalPerspectiveId = visiblePerspective.getElementId();
		IPerspectiveDescriptor originalPerspective = getWorkbenchWindow().getWorkbench()
				.getPerspectiveRegistry().findPerspectiveWithId(originalPerspectiveId);
		// remove it from our collection of previously opened perspectives
		sortedPerspectives.remove(originalPerspective);
		// append the saved perspective
		sortedPerspectives.add(perspective);

		visiblePerspective.setLabel(perspective.getLabel());
		visiblePerspective.setTooltip(perspective.getLabel());
		visiblePerspective.setElementId(perspective.getId());

		MPerspective copy = (MPerspective) EcoreUtil.copy((EObject) visiblePerspective);

		List<MPlaceholder> elementsToHide = modelService.findElements(copy, null, MPlaceholder.class, null);
		for (MPlaceholder elementToHide : elementsToHide) {
			if (elementToHide.isToBeRendered()
					&& elementToHide.getRef().getTags().contains(IPresentationEngine.NO_RESTORE)) {
				elementToHide.setToBeRendered(false);
				updateSelectionAndParentVisibility(elementToHide);
			}
		}
		// remove placeholder refs and save as snippet
		modelService.cloneElement(copy, application);
		if (perspective instanceof PerspectiveDescriptor) {
			((PerspectiveDescriptor) perspective).setHasCustomDefinition(true);
		}

		UIEvents.publishEvent(UIEvents.UILifeCycle.PERSPECTIVE_SAVED, visiblePerspective);
	}

	private void updateSelectionAndParentVisibility(MUIElement element) {
		MElementContainer<MUIElement> parent = element.getParent();
		if (parent.getSelectedElement() == element) {
			parent.setSelectedElement(null);
		}
		int renderableChildren = modelService.countRenderableChildren(parent);
		if (renderableChildren == 0 && !modelService.isLastEditorStack(parent)) {
			parent.setToBeRendered(false);
			updateSelectionAndParentVisibility(parent);
		}
	}

	@Override
	public void setEditorAreaVisible(boolean showEditorArea) {
		MUIElement find = findSharedArea();
		if (find != null) {
			if (showEditorArea) {
				// make sure it's been rendered if it hasn't been
				find.setToBeRendered(true);
			}

			// If the EA is minimized, restore it...
			if (showEditorArea) {
				find.getTags().remove(IPresentationEngine.MINIMIZED);
			}

			find.setVisible(showEditorArea);
		}
	}

	private HashMap<MPerspective, Perspective> modelToPerspectiveMapping = new HashMap<>();

	private Perspective getPerspective(MPerspective mperspective) {
		if (mperspective == null) {
			return null;
		}
		if (!modelToPerspectiveMapping.containsKey(mperspective)) {
			boolean fixedPerspective = false;
			PerspectiveDescriptor perspectiveDesc = (PerspectiveDescriptor) getPerspectiveDesc(mperspective.getElementId());
			if (perspectiveDesc == null) {
				fixedPerspective = true;
				perspectiveDesc = fixOrphanPerspective(mperspective);
			}
			Perspective p = new Perspective(perspectiveDesc, mperspective, this);
			modelToPerspectiveMapping.put(mperspective, p);
			p.initActionSets();
			if (fixedPerspective) {
				UIEvents.publishEvent(UIEvents.UILifeCycle.PERSPECTIVE_SAVED, mperspective);
			}
		}
		return modelToPerspectiveMapping.get(mperspective);
	}

	/**
	 * An 'orphan' perspective is one that was originally created through a
	 * contribution but whose contributing bundle is no longer available. In
	 * order to allow it to behave correctly within the environment (for Close,
	 * Reset...) we turn it into a 'custom' perspective on its first activation.
	 *
	 * @return
	 */
	private PerspectiveDescriptor fixOrphanPerspective(MPerspective mperspective) {
		PerspectiveRegistry reg = (PerspectiveRegistry) PlatformUI.getWorkbench().getPerspectiveRegistry();
		String perspId = mperspective.getElementId();
		String label = mperspective.getLabel();
		String msg = "Perspective with name '" + label + "' and id '" + perspId + "' has been made into a local copy"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		IStatus status = StatusUtil.newStatus(IStatus.WARNING, msg, null);
		StatusManager.getManager().handle(status, StatusManager.LOG);

		String newDescId = NLS.bind(WorkbenchMessages.Perspective_localCopyLabel, label);
		while (reg.findPerspectiveWithId(newDescId) != null) {
			newDescId = NLS.bind(WorkbenchMessages.Perspective_localCopyLabel, newDescId);
		}
		PerspectiveDescriptor pd = new PerspectiveDescriptor(perspId, label, null);
		PerspectiveDescriptor newDesc = reg.createPerspective(newDescId, pd);
		if (mperspective.getIconURI() != null) {
			try {
				ImageDescriptor img = ImageDescriptor.createFromURL(new URI(mperspective.getIconURI()).toURL());
				newDesc.setImageDescriptor(img);
			} catch (MalformedURLException | URISyntaxException e) {
				WorkbenchPlugin.log(MessageFormat.format("Error on applying configured perspective icon: {0}", //$NON-NLS-1$
						mperspective.getIconURI(), e));
			}
		}

		mperspective.setElementId(newDesc.getId());
		mperspective.setLabel(newDesc.getLabel());
		sortedPerspectives.add(newDesc);
		modelService.cloneElement(mperspective, application);
		newDesc.setHasCustomDefinition(true);
		return newDesc;
	}

	@Override
	public void setPerspective(IPerspectiveDescriptor perspective) {
		if (perspective == null) {
			return;
		}

		IPerspectiveDescriptor lastPerspective = getPerspective();
		if (lastPerspective != null && lastPerspective.getId().equals(perspective.getId())) {
			// no change
			MPerspectiveStack perspectives = getPerspectiveStack();
			for (MPerspective mperspective : perspectives.getChildren()) {
				if (mperspective.getElementId().equals(perspective.getId())) {
					handleNullRefPlaceHolders(mperspective, window);
				}
			}
			return;
		}

		MPerspectiveStack perspectives = getPerspectiveStack();
		for (MPerspective mperspective : perspectives.getChildren()) {
			if (mperspective.getElementId().equals(perspective.getId())) {
				if (lastPerspective != null) {
					legacyWindow.firePerspectiveDeactivated(this, lastPerspective);
				}

				// this perspective already exists, switch to this one
				perspectives.setSelectedElement(mperspective);
				mperspective.getContext().activate();
				handleNullRefPlaceHolders(mperspective, window);
				return;
			}
		}

		MPerspective modelPerspective = (MPerspective) modelService.cloneSnippet(application,
				perspective.getId(), window);

		if (modelPerspective == null) {
			// couldn't find the perspective, create a new one
			modelPerspective = createPerspective(perspective);
		}

		handleNullRefPlaceHolders(modelPerspective, window);

		modelPerspective.setLabel(perspective.getLabel());

		ImageDescriptor imageDescriptor = perspective.getImageDescriptor();
		if (imageDescriptor != null) {
			String imageURL = MenuHelper.getImageUrl(imageDescriptor);
			modelPerspective.setIconURI(imageURL);
		}

		if (lastPerspective != null) {
			legacyWindow.firePerspectiveDeactivated(this, lastPerspective);
		}

		// Hide placeholders for parts that exist in the 'global' areas
		modelService.hideLocalPlaceholders(window, modelPerspective);

		// add it to the stack
		perspectives.getChildren().add(modelPerspective);
		// activate it
		perspectives.setSelectedElement(modelPerspective);

		modelPerspective.getContext().activate();
		modelPerspective.getContext().set(ISelectionService.class, selectionService);

		legacyWindow.firePerspectiveOpened(this, perspective);
		UIEvents.publishEvent(UIEvents.UILifeCycle.PERSPECTIVE_OPENED, modelPerspective);
	}

	private void handleNullRefPlaceHolders(MUIElement element, MWindow refWin) {
		List<MPlaceholder> nullRefList = ((ModelServiceImpl) modelService).getNullRefPlaceHolders(element, refWin);

		List<MPart> partList = modelService.findElements(element, null, MPart.class, null);
		for (MPart part : partList) {
			if (CompatibilityPart.COMPATIBILITY_VIEW_URI.equals(part.getContributionURI())
					&& part.getIconURI() == null) {
				part.getTransientData().put(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY,
						ImageDescriptor.getMissingImageDescriptor().createImage());
			}
		}

		if (nullRefList != null && nullRefList.size() > 0) {
			for (MPlaceholder ph : nullRefList) {
				if (ph.isToBeRendered()) {
					replacePlaceholder(ph);
				}
			}
		}
	}

	private void replacePlaceholder(MPlaceholder ph) {
		MPart part = modelService.createModelElement(MPart.class);
		part.setElementId(ph.getElementId());
		part.getTransientData().put(IPresentationEngine.OVERRIDE_ICON_IMAGE_KEY,
				ImageDescriptor.getMissingImageDescriptor().createImage());
		String label = (String) ph.getTransientData().get(IWorkbenchConstants.TAG_LABEL);
		if (label != null) {
			part.setLabel(label);
		} else {
			part.setLabel(getLabel(ph.getElementId()));
		}
		part.setContributionURI(CompatibilityPart.COMPATIBILITY_VIEW_URI);
		part.setCloseable(true);
		MElementContainer<MUIElement> curParent = ph.getParent();
		int curIndex = curParent.getChildren().indexOf(ph);
		curParent.getChildren().remove(curIndex);
		curParent.getChildren().add(curIndex, part);
		if (curParent.getSelectedElement() == ph) {
			curParent.setSelectedElement(part);
		}
	}

	private String getLabel(String str) {
		int index = str.lastIndexOf('.');
		if (index == -1)
			return str;
		return str.substring(index + 1);
	}

	/**
	 * @param perspective
	 * @return never null
	 */
	private MPerspective createPerspective(IPerspectiveDescriptor perspective) {
		MPerspective modelPerspective = modelService.createModelElement(MPerspective.class);

		// tag it with the same id
		modelPerspective.setElementId(perspective.getId());

		// instantiate the perspective
		IPerspectiveFactory factory = ((PerspectiveDescriptor) perspective).createFactory();
		ModeledPageLayout modelLayout = new ModeledPageLayout(window, modelService,
				partService, modelPerspective, perspective, this, true);
		factory.createInitialLayout(modelLayout);
		PerspectiveTagger.tagPerspective(modelPerspective, modelService);
		PerspectiveExtensionReader reader = new PerspectiveExtensionReader();
		reader.extendLayout(getExtensionTracker(), perspective.getId(), modelLayout);
		return modelPerspective;
	}

	void perspectiveActionSetChanged(Perspective perspective, IActionSetDescriptor descriptor,
			int changeType) {
		if (perspective == getActivePerspective()) {
			actionSets.change(descriptor, changeType);
		}
	}


	/**
	 * Retrieves the perspective stack of the window that's containing this
	 * workbench page.
	 *
	 * @return the stack of perspectives of this page's containing window
	 */
	private MPerspectiveStack getPerspectiveStack() {
		if (_perspectiveStack != null) {
			return _perspectiveStack;
		}
		List<MPerspectiveStack> theStack = modelService.findElements(window, null,
				MPerspectiveStack.class, null);
		if (theStack.size() > 0) {
			_perspectiveStack = theStack.get(0);
			return _perspectiveStack;
		}

		for (MWindowElement child : window.getChildren()) {
			if (child instanceof MPerspectiveStack) {
				_perspectiveStack = (MPerspectiveStack) child;
				return _perspectiveStack;
			}
		}

		MPartSashContainer stickySash = modelService.createModelElement(MPartSashContainer.class);
		stickySash.setHorizontal(true);

		MPerspectiveStack perspectiveStack = modelService
				.createModelElement(MPerspectiveStack.class);
		perspectiveStack.setElementId(IWorkbenchConstants.PERSPECTIVE_STACK_ID);
		perspectiveStack.setContainerData("7500"); //$NON-NLS-1$

		MPartStack stickyFolder = modelService.createModelElement(MPartStack.class);
		stickyFolder.setContainerData("2500"); //$NON-NLS-1$
		stickyFolder.setElementId("stickyFolderRight"); //$NON-NLS-1$
		stickyFolder.setToBeRendered(false);

		IStickyViewDescriptor[] stickyViews = getWorkbenchWindow().getWorkbench().getViewRegistry()
				.getStickyViews();
		for (IStickyViewDescriptor stickyView : stickyViews) {
			if (stickyView.getLocation() == IPageLayout.RIGHT) {
				MStackElement viewModel = ModeledPageLayout.createViewModel(application,
						stickyView.getId(), false, this, partService, true);
				stickyFolder.getChildren().add(viewModel);
			}
		}

		stickySash.getChildren().add(perspectiveStack);
		stickySash.getChildren().add(stickyFolder);
		stickySash.setSelectedElement(perspectiveStack);

		window.getChildren().add(stickySash);
		window.setSelectedElement(stickySash);
		_perspectiveStack = perspectiveStack;
		return perspectiveStack;
	}

    /**
     * Sets the active working set for the workbench page. Notifies property
     * change listener about the change.
     *
     * @param newWorkingSet
     *            the active working set for the page. May be null.
     * @since 2.0
     * @deprecated individual views should store a working set if needed
     */
    @Deprecated
	public void setWorkingSet(IWorkingSet newWorkingSet) {
        IWorkingSet oldWorkingSet = workingSet;

        workingSet = newWorkingSet;
        if (oldWorkingSet != newWorkingSet) {
            firePropertyChange(CHANGE_WORKING_SET_REPLACE, oldWorkingSet,
                    newWorkingSet);
        }
        if (newWorkingSet != null) {
            WorkbenchPlugin.getDefault().getWorkingSetManager()
                    .addPropertyChangeListener(workingSetPropertyChangeListener);
        } else {
            WorkbenchPlugin.getDefault().getWorkingSetManager()
                    .removePropertyChangeListener(workingSetPropertyChangeListener);
        }
    }

    /**
     * @see IWorkbenchPage
     */
    @Override
	public void showActionSet(String actionSetID) {
    	 Perspective persp = getActivePerspective();
         if (persp != null) {
             ActionSetRegistry reg = WorkbenchPlugin.getDefault()
                  .getActionSetRegistry();

             IActionSetDescriptor desc = reg.findActionSet(actionSetID);
             if (desc != null) {
				List<IActionSetDescriptor> offActionSets = persp.getAlwaysOffActionSets();
				for (IActionSetDescriptor off : offActionSets) {
					if (off.getId().equals(desc.getId())) {
						return;
					}
				}
                 persp.addActionSet(desc);
                 legacyWindow.updateActionSets();
                 legacyWindow.firePerspectiveChanged(this, getPerspective(),
                         CHANGE_ACTION_SET_SHOW);
             }
         }
    }

    /**
     * See IWorkbenchPage.
     */
    @Override
	public IViewPart showView(String viewID) throws PartInitException {
        return showView(viewID, null, VIEW_ACTIVATE);
    }

	@Override
	public IViewPart showView(final String viewID, final String secondaryID, final int mode)
			throws PartInitException {

		if (secondaryID != null) {
			if (secondaryID.length() == 0 || secondaryID.indexOf(':') != -1) {
				throw new IllegalArgumentException(
						WorkbenchMessages.WorkbenchPage_IllegalSecondaryId);
			}
		}
		if (!certifyMode(mode)) {
			throw new IllegalArgumentException(WorkbenchMessages.WorkbenchPage_IllegalViewMode);
		}

		// Run op in busy cursor.
		final String compoundId = secondaryID != null ? viewID + ':' + secondaryID : viewID;
		final Object[] result = new Object[1];
		BusyIndicator.showWhile(null, new Runnable() {
			@Override
			public void run() {
				try {
					result[0] = busyShowView(compoundId, mode);
				} catch (PartInitException e) {
					result[0] = e;
				}
			}
		});
		if (result[0] instanceof IViewPart) {
			return (IViewPart) result[0];
		} else if (result[0] instanceof PartInitException) {
			throw (PartInitException) result[0];
		} else {
			throw new PartInitException(WorkbenchMessages.WorkbenchPage_AbnormalWorkbenchCondition);
		}
	}

    /**
     * @param mode the mode to test
     * @return whether the mode is recognized
     * @since 3.0
     */
    private boolean certifyMode(int mode) {
        switch (mode) {
        case VIEW_ACTIVATE:
        case VIEW_VISIBLE:
        case VIEW_CREATE:
            return true;
        default:
            return false;
        }
    }

	public MUIElement getActiveElement(IWorkbenchPartReference ref) {
		MUIElement element = null;

		MPerspective curPersp = modelService.getActivePerspective(window);
		if (curPersp == null)
			return null;

		MPlaceholder eaPH = (MPlaceholder) modelService.find(IPageLayout.ID_EDITOR_AREA, curPersp);
		MPart model = ((WorkbenchPartReference) ref).getModel();
		MPlaceholder placeholder = model.getCurSharedRef();

		switch (modelService.getElementLocation(placeholder == null ? model : placeholder)) {
		case EModelService.IN_ACTIVE_PERSPECTIVE:
		case EModelService.OUTSIDE_PERSPECTIVE:
			MUIElement parent = placeholder == null ? model.getParent() : placeholder.getParent();
			if (parent instanceof MPartStack) {
				element = parent;
			}
			break;
		case EModelService.IN_SHARED_AREA:
			element = eaPH;
			break;
		}
		return element;
	}

	@Override
	public void setPartState(IWorkbenchPartReference ref, int iState) {
		MUIElement element = getActiveElement(ref);
		String state = null;

		if (iState == STATE_MINIMIZED) {
			state = IPresentationEngine.MINIMIZED;
		} else if (iState == STATE_MAXIMIZED) {
			state = IPresentationEngine.MAXIMIZED;
		}
		setPartState(element, state);
	}

    @Override
	public int getPartState(IWorkbenchPartReference ref) {
		int state = STATE_RESTORED;
		MUIElement element = getActiveElement(ref);

		if (element != null) {
			if (element.getTags().contains(IPresentationEngine.MINIMIZED)) {
				state = STATE_MINIMIZED;
			} else if (element.getTags().contains(IPresentationEngine.MAXIMIZED)) {
				state = STATE_MAXIMIZED;
			}
		}
		return state;
	}

	// if the state is null, then we'll just restore the view
	private void setPartState(MUIElement element, String state) {
		if (element != null) {
			element.getTags().remove(IPresentationEngine.MINIMIZED_BY_ZOOM);
			if (IPresentationEngine.MINIMIZED.equals(state)) {
				element.getTags().remove(IPresentationEngine.MAXIMIZED);
				element.getTags().add(IPresentationEngine.MINIMIZED);
			} else if (IPresentationEngine.MAXIMIZED.equals(state)) {
				element.getTags().remove(IPresentationEngine.MINIMIZED);
				element.getTags().add(IPresentationEngine.MAXIMIZED);
			} else {
				element.getTags().remove(IPresentationEngine.MINIMIZED);
				element.getTags().remove(IPresentationEngine.MAXIMIZED);
			}
		}
	}

    /**
     * updateActionBars method comment.
     */
    public void updateActionBars() {
		legacyWindow.updateActionBars();
    }


    @Override
	public void zoomOut() {
		// TODO compat: what does the zoom do?
    }

	@Override
	public void toggleZoom(IWorkbenchPartReference ref) {
		MUIElement element = getActiveElement(ref);
		if (element != null) {
			String state = null;
			if (!element.getTags().contains(IPresentationEngine.MAXIMIZED)) {
				state = IPresentationEngine.MAXIMIZED;
			}
			this.setPartState(element, state);
		}
	}


	@Override
	public IPerspectiveDescriptor[] getOpenPerspectives() {
		MPerspectiveStack perspectiveStack = modelService.findElements(window, null,
				MPerspectiveStack.class, null).get(0);
		IPerspectiveRegistry registry = PlatformUI.getWorkbench().getPerspectiveRegistry();

		ArrayList<IPerspectiveDescriptor> tmp = new ArrayList<>(
				perspectiveStack.getChildren().size());
		for (MPerspective persp : perspectiveStack.getChildren()) {
			String perspectiveId = persp.getElementId();
			IPerspectiveDescriptor desc = registry.findPerspectiveWithId(perspectiveId);
			if (desc != null) {
				tmp.add(desc);
			}
		}
		IPerspectiveDescriptor[] descs = new IPerspectiveDescriptor[tmp.size()];
		tmp.toArray(descs);

		return descs;
	}

	@Override
	public IPerspectiveDescriptor[] getSortedPerspectives() {
		return sortedPerspectives.toArray(new IPerspectiveDescriptor[sortedPerspectives.size()]);
	}



    /**
     * Returns the reference to the given part, or <code>null</code> if it has no reference
     * (i.e. it is not a top-level part in this workbench page).
     *
     * @param part the part
     * @return the part's reference or <code>null</code> if the given part does not belong
     * to this workbench page
     */
	@Override
	public IWorkbenchPartReference getReference(IWorkbenchPart part) {
		if (part != null) {
			IWorkbenchPartSite site = part.getSite();
			if (site instanceof PartSite) {
				return ((PartSite) site).getPartReference();
			}
		}
		return null;
	}

	public MPerspective getCurrentPerspective() {
		MPerspectiveStack stack = getPerspectiveStack();
		return stack == null ? null : stack.getSelectedElement();
	}

	Perspective getActivePerspective() {
		return getPerspective(getCurrentPerspective());
	}

	@Override
	public IViewPart[] getViewStack(IViewPart part) {
		String compoundId = PagePartSelectionTracker.getPartId(part);
		MPart mpart = partService.findPart(compoundId);
		if (mpart != null) {
			MElementContainer<?> parent = mpart.getParent();
			if (parent == null) {
				// this is a shared part, check for placeholders
				MPlaceholder placeholder = mpart.getCurSharedRef();
				if (placeholder != null) {
					parent = placeholder.getParent();
				}
			}

			if (parent instanceof MPartStack) {
				MStackElement selectedElement = ((MPartStack) parent).getSelectedElement();
				final MUIElement topPart = selectedElement instanceof MPlaceholder ? ((MPlaceholder) selectedElement)
						.getRef() : null;

				List<CompatibilityView> stack = new ArrayList<>();
				for (Object child : parent.getChildren()) {
					MPart siblingPart = child instanceof MPart ? (MPart) child
							: (MPart) ((MPlaceholder) child).getRef();
					// Bug 398433 - guard against NPE
					Object siblingObject = siblingPart != null ? siblingPart.getObject() : null;
					if (siblingObject instanceof CompatibilityView) {
						stack.add((CompatibilityView) siblingObject);
					}
				}

				// sort the list by activation order (most recently activated
				// first)
				Collections.sort(stack, new Comparator<CompatibilityView>() {
					@Override
					public int compare(CompatibilityView o1, CompatibilityView o2) {
						MPart model1 = o1.getModel();
						MPart model2 = o2.getModel();

						/*
						 * WORKAROUND: Since we only have the activation list
						 * and not a bingToTop list, we can't set/know the order
						 * for inactive stacks. This workaround makes sure that
						 * the topmost part is at least at the first position.
						 */
						if (model1 == topPart)
							return Integer.MIN_VALUE;
						if (model2 == topPart)
							return Integer.MAX_VALUE;

						int pos1 = activationList.indexOf(model1);
						int pos2 = activationList.indexOf(model2);
						if (pos1 == -1)
							pos1 = Integer.MAX_VALUE;
						if (pos2 == -1)
							pos2 = Integer.MAX_VALUE;
						return pos1 - pos2;
					}
				});

				IViewPart[] result = new IViewPart[stack.size()];
				for (int i = 0; i < result.length; i++) {
					result[i] = stack.get(i).getView();
				}
				return result;
			}

			// not in a stack, standalone
			return new IViewPart[] { part };
		}
		return null;
	}


	@Override
	public IExtensionTracker getExtensionTracker() {
		if (tracker == null) {
			tracker = new UIExtensionTracker(getWorkbenchWindow().getWorkbench().getDisplay());
		}
		return tracker;
	}

	private final static String[] EMPTY_STRING_ARRAY = new String[0];

	private String[] getArrayForTag(String tagPrefix) {
		List<String> id = getCollectionForTag(tagPrefix);
		if (id == null)
			return EMPTY_STRING_ARRAY;
		return id.toArray(new String[id.size()]);
	}

	private List<String> getCollectionForTag(String tagPrefix) {
		MPerspective perspective = getPerspectiveStack().getSelectedElement();
		if (perspective == null) {
			return Collections.emptyList();
		}
		return ModeledPageLayout.getIds(perspective, tagPrefix);
	}

	@Override
	public String[] getNewWizardShortcuts() {
		return getArrayForTag(ModeledPageLayout.NEW_WIZARD_TAG);
	}

	@Override
	public String[] getPerspectiveShortcuts() {
		return getArrayForTag(ModeledPageLayout.PERSP_SHORTCUT_TAG);
	}

	@Override
	public String[] getShowViewShortcuts() {
		return getArrayForTag(ModeledPageLayout.SHOW_VIEW_TAG);
	}



    public boolean isPartVisible(IWorkbenchPartReference reference) {
        IWorkbenchPart part = reference.getPart(false);
        // Can't be visible if it isn't created yet
        if (part == null) {
            return false;
        }

        return isPartVisible(part);
    }

	@Override
	public IWorkingSet[] getWorkingSets() {
		return workingSets;
	}

	@Override
	public void setWorkingSets(IWorkingSet[] newWorkingSets) {
		if (newWorkingSets != null) {
			WorkbenchPlugin
					.getDefault()
					.getWorkingSetManager()
					.addPropertyChangeListener(workingSetPropertyChangeListener);
		} else {
			WorkbenchPlugin.getDefault().getWorkingSetManager()
					.removePropertyChangeListener(
							workingSetPropertyChangeListener);
		}

		if (newWorkingSets == null) {
			newWorkingSets = new IWorkingSet[0];
		}

		IWorkingSet[] oldWorkingSets = workingSets;

		// filter out any duplicates if necessary
		if (newWorkingSets.length > 1) {
			Set<IWorkingSet> setOfSets = new HashSet<>();
			for (IWorkingSet workingSet : newWorkingSets) {
				if (workingSet == null) {
					throw new IllegalArgumentException();
				}
				setOfSets.add(workingSet);
			}
			newWorkingSets = setOfSets.toArray(new IWorkingSet[setOfSets.size()]);
		}

		workingSets = newWorkingSets;
		if (!Arrays.equals(oldWorkingSets, newWorkingSets)) {
			firePropertyChange(CHANGE_WORKING_SETS_REPLACE, oldWorkingSets,
					newWorkingSets);
			if (aggregateWorkingSet != null) {
				aggregateWorkingSet.setComponents(workingSets);
			}
		}
	}

	@Override
	public IWorkingSet getAggregateWorkingSet() {
		if (aggregateWorkingSet == null) {
			IWorkingSetManager workingSetManager = PlatformUI.getWorkbench()
					.getWorkingSetManager();

			if (aggregateWorkingSetId == null) {
				aggregateWorkingSetId = generateAggregateWorkingSetId();
			} else {
				aggregateWorkingSet = (AggregateWorkingSet) workingSetManager.getWorkingSet(aggregateWorkingSetId);
			}
			if (aggregateWorkingSet == null) {
				aggregateWorkingSet = (AggregateWorkingSet) workingSetManager
						.createAggregateWorkingSet(aggregateWorkingSetId,
								WorkbenchMessages.WorkbenchPage_workingSet_default_label,
								getWorkingSets());
				workingSetManager.addWorkingSet(aggregateWorkingSet);
			}
		}
		return aggregateWorkingSet;
	}

	private String generateAggregateWorkingSetId() {
		return "Aggregate for window " + System.currentTimeMillis(); //$NON-NLS-1$
	}

	@Override
	public void showEditor(IEditorReference ref) {
		IWorkbenchPart wPart = ref.getPart(false);
		MPart part = ((EditorReference)ref).getModel();
		part.setVisible(true);

		//Workaround to get content visible. Otherwise the content sometimes is not rendered.
		MElementContainer<MUIElement> partStack = part.getParent();
		partStack.setSelectedElement(null);
		partStack.setSelectedElement(part);
		wPart.setFocus();
	}

	@Override
	public void hideEditor(IEditorReference ref) {
		MPart part = ((EditorReference)ref).getModel();
		part.setVisible(false);
	}

	private String getEditorImageURI(EditorReference reference) {
		String iconURI = null;

		EditorDescriptor descriptor = reference.getDescriptor();
		if (descriptor != null) {
			IConfigurationElement element = descriptor.getConfigurationElement();
			if (element != null) {
				iconURI = MenuHelper.getIconURI(element, IWorkbenchRegistryConstants.ATT_ICON);
			}
		}
		return iconURI;
	}

	@Override
	public IMemento[] getEditorState(IEditorReference[] editorRefs, boolean includeInputState) {
		IMemento[] m = new IMemento[editorRefs.length];
		for (int i = 0; i < editorRefs.length; i++) {
			m[i] = ((EditorReference) editorRefs[i]).getEditorState();
			if (!includeInputState && m[i] != null) {
				m[i] = m[i].getChild(IWorkbenchConstants.TAG_EDITOR_STATE);
			}
		}
		return m;
	}

	@Override
	public IEditorReference[] openEditors(IEditorInput[] inputs, String[] editorIDs, int matchFlags) throws MultiPartInitException {
		return openEditors(inputs, editorIDs, null, matchFlags, 0);
	}

	@Override
	public IEditorReference[] openEditors(IEditorInput[] inputs, String[] editorIDs,
			IMemento[] mementos, int matchFlags, int activationIndex)
			throws MultiPartInitException {
		// If we are only working with mementos create a placeholder array of
		// nulls
		if (inputs == null) {
			Assert.isTrue(mementos != null);
			inputs = new IEditorInput[mementos.length];
		}

		// If we are only working with mementos create a placeholder array of
		// nulls
		if (editorIDs == null) {
			Assert.isTrue(mementos != null);
			editorIDs = new String[mementos.length];
		}

		Assert.isTrue(inputs.length == editorIDs.length);
		Assert.isTrue(inputs.length > 0);
		Assert.isTrue(mementos == null || mementos.length == inputs.length);

		PartInitException[] exceptions = new PartInitException[inputs.length];
		IEditorReference[] references = new IEditorReference[inputs.length];
		boolean hasFailures = false;

		IEditorRegistry reg = getWorkbenchWindow().getWorkbench().getEditorRegistry();
		MPart editorToActivate = null;
		for (int i = 0; i < inputs.length; i++) {
			String curEditorID = editorIDs[i];
			IEditorInput curInput = inputs[i];
			IMemento curMemento = mementos == null ? null : mementos[i];

			// If we don't have an editorID get it from the memento
			if (curEditorID == null && curMemento != null) {
				curEditorID = curMemento.getString(IWorkbenchConstants.TAG_ID);
			}

			// If we don't have an input create on from the memento
			if (curInput == null && curMemento != null) {
				try {
					curInput = EditorReference.createInput(curMemento);
				} catch (PartInitException e) {
					curInput = null;
					exceptions[i] = e;
					hasFailures = true;
					continue;
				}
			}

			// Adjust the memento so that it's always 'comlpete (i.e. including
			// both input and editor state)
			if (curMemento != null && !curMemento.getID().equals(IWorkbenchConstants.TAG_EDITOR)) {
				XMLMemento outerMem = XMLMemento.createWriteRoot(IWorkbenchConstants.TAG_EDITOR);
				outerMem.putString(IWorkbenchConstants.TAG_ID, curEditorID);
				outerMem.copyChild(curMemento);

				XMLMemento inputMem = (XMLMemento) outerMem
						.createChild(IWorkbenchConstants.TAG_INPUT);
				inputMem.putString(IWorkbenchConstants.TAG_FACTORY_ID, curInput.getPersistable()
						.getFactoryId());
				inputMem.putString(IWorkbenchConstants.TAG_PATH, curInput.getName());
			}

			// OK, by this point we should have the EditorInput, the editor ID
			// and the memento (if any)
			if (reg.findEditor(curEditorID) == null) {
				references[i] = null;
				exceptions[i] = new PartInitException(NLS.bind(
						WorkbenchMessages.EditorManager_unknownEditorIDMessage, curEditorID));
				hasFailures = true;
			} else if (curInput == null) {
				references[i] = null;
				exceptions[i] = new PartInitException(NLS.bind(
						WorkbenchMessages.EditorManager_no_persisted_state, curEditorID));
				hasFailures = true;
			} else {
				// Is there an existing editor ?
				IEditorReference[] existingEditors = findEditors(curInput, curEditorID,
						matchFlags);
				if (existingEditors.length == 0) {
					MPart editor = partService.createPart(CompatibilityEditor.MODEL_ELEMENT_ID);
					references[i] = createEditorReferenceForPart(editor, curInput, curEditorID,
							null);

					if (i == activationIndex)
						editorToActivate = editor;

					// Set the information in the supplied IMemento into the
					// editor's model
					if (curMemento instanceof XMLMemento) {
						XMLMemento memento = (XMLMemento) curMemento;
						StringWriter writer = new StringWriter();
						try {
							memento.save(writer);
							editor.getPersistedState().put(WorkbenchPartReference.MEMENTO_KEY,
									writer.toString());
						} catch (IOException e) {
							WorkbenchPlugin.log(e);
						}
					}

					editor.setLabel(references[i].getTitle());
					editor.setTooltip(references[i].getTitleToolTip());
					editor.setIconURI(getEditorImageURI((EditorReference) references[i]));
					((PartServiceImpl) partService).addPart(editor);
				} else {
					// Use the existing editor, update the state if it has *not*
					// been rendered
					EditorReference ee = (EditorReference) existingEditors[0];
					if (i == activationIndex)
						editorToActivate = ee.getModel();

					if (ee.getModel().getWidget() == null) {
						// Set the information in the supplied IMemento into the
						// editor's model
						if (curMemento instanceof XMLMemento) {
							XMLMemento momento = (XMLMemento) curMemento;
							StringWriter writer = new StringWriter();
							try {
								momento.save(writer);
								ee.getModel().getPersistedState()
										.put(WorkbenchPartReference.MEMENTO_KEY, writer.toString());
							} catch (IOException e) {
								WorkbenchPlugin.log(e);
							}
						}
					} else {
						// editor already rendered, try to update its state
						if (curMemento != null
								&& ee.getModel().getObject() instanceof CompatibilityEditor) {
							CompatibilityEditor ce = (CompatibilityEditor) ee.getModel()
									.getObject();
							if (ce.getEditor() instanceof IPersistableEditor) {
								IPersistableEditor pe = (IPersistableEditor) ce.getEditor();

								// Extract the 'editorState' from the memento
								IMemento editorMem = curMemento
										.getChild(IWorkbenchConstants.TAG_EDITOR_STATE);
								if (editorMem == null) {
									// Must be an externally defined memento,
									// take the second child
									IMemento[] kids = curMemento.getChildren();
									if (kids.length == 2)
										editorMem = kids[1];
								}
								if (editorMem != null)
									pe.restoreState(editorMem);
							}
						}
					}
				}
			}
		}

		if (editorToActivate != null) {
			partService.activate(editorToActivate);
		}

		boolean hasSuccesses = false;
		for (IEditorReference reference : references) {
			if (reference != null) {
				hasSuccesses = true;
				legacyWindow.firePerspectiveChanged(this, getPerspective(), reference,
						CHANGE_EDITOR_OPEN);
			}
		}

		// only fire this event if an editor was opened
		if (hasSuccesses) {
			legacyWindow.firePerspectiveChanged(this, getPerspective(), CHANGE_EDITOR_OPEN);
		}

		if (hasFailures) {
			throw new MultiPartInitException(references, exceptions);
		}

		return references;
	}

	void updatePerspectiveActionSets() {
		updateActionSets(null, getActivePerspective());
	}

	void fireInitialPartVisibilityEvents() {
		MPerspective selectedElement = getPerspectiveStack().getSelectedElement();
		// technically shouldn't be null here
		if (selectedElement != null) {
			Collection<MPart> parts = modelService.findElements(selectedElement, null, MPart.class,
					null);
			List<MPart> visibleParts = new ArrayList<>(parts.size());
			for (MPart part : parts) {
				if (isVisible(selectedElement, part)) {
					visibleParts.add(part);
				}
			}

			for (MPart part : visibleParts) {
				firePartVisible(part);
			}
		}
	}

	private boolean isVisible(MPerspective perspective, MUIElement element) {
		if (element == perspective) {
			return true;
		} else if (element.isVisible() && element.isToBeRendered()) {
			MElementContainer<?> parent = element.getParent();
			if (parent instanceof MPartStack) {
				if (parent.getSelectedElement() == element) {
					return isVisible(perspective, parent);
				}
			} else if (parent == null) {
				MPlaceholder placeholder = element.getCurSharedRef();
				return placeholder == null ? false : isVisible(perspective, placeholder);
			} else {
				return isVisible(perspective, parent);
			}
		}
		return false;
	}

	private void firePartActivated(MPart part) {

		Object client = part.getObject();
		if (client instanceof CompatibilityPart) {
			final IWorkbenchPart workbenchPart = getWrappedPart((CompatibilityPart) client);
			if (workbenchPart == null) {
				return;
			}
			final IWorkbenchPartReference partReference = getReference(workbenchPart);
			if (partReference == null) {
				WorkbenchPlugin.log(new RuntimeException("Reference is null in firePartActivated: " + part)); //$NON-NLS-1$
				return;
			}

			for (final IPartListener listener : partListenerList) {
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						listener.partActivated(workbenchPart);
					}
				});
			}

			for (final IPartListener2 listener : partListener2List) {
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						listener.partActivated(partReference);
					}
				});
			}
		}
		else if (client != null) {
			if (part.getTransientData().get(E4PartWrapper.E4_WRAPPER_KEY) instanceof E4PartWrapper) {
				IWorkbenchPart workbenchPart = (IWorkbenchPart) part.getTransientData()
						.get(E4PartWrapper.E4_WRAPPER_KEY);
				final IWorkbenchPartReference partReference = getReference(workbenchPart);

				if (partReference != null) {
					for (final IPartListener listener : partListenerList) {
						SafeRunner.run(new SafeRunnable() {
							@Override
							public void run() throws Exception {
								listener.partActivated(workbenchPart);
							}
						});
					}

					for (final IPartListener2 listener : partListener2List) {
						SafeRunner.run(new SafeRunnable() {
							@Override
							public void run() throws Exception {
								listener.partActivated(partReference);
							}
						});
					}
				}
			}
		}
	}

	private void firePartDeactivated(MPart part) {
		Object client = part.getObject();
		if (client instanceof CompatibilityPart) {
			final IWorkbenchPart workbenchPart = getWrappedPart((CompatibilityPart) client);
			if (workbenchPart == null) {
				return;
			}
			final IWorkbenchPartReference partReference = getReference(workbenchPart);

			for (final IPartListener listener : partListenerList) {
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						listener.partDeactivated(workbenchPart);
					}
				});
			}

			for (final IPartListener2 listener : partListener2List) {
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						listener.partDeactivated(partReference);
					}
				});
			}
		} else if (client != null) {
			if (part.getTransientData().get(E4PartWrapper.E4_WRAPPER_KEY) instanceof E4PartWrapper) {
				IWorkbenchPart workbenchPart = (IWorkbenchPart) part.getTransientData()
						.get(E4PartWrapper.E4_WRAPPER_KEY);
				final IWorkbenchPartReference partReference = getReference(workbenchPart);

				if (partReference != null) {
					for (final IPartListener listener : partListenerList) {
						SafeRunner.run(new SafeRunnable() {
							@Override
							public void run() throws Exception {
								listener.partDeactivated(workbenchPart);
							}
						});
					}

					for (final IPartListener2 listener : partListener2List) {
						SafeRunner.run(new SafeRunnable() {
							@Override
							public void run() throws Exception {
								listener.partDeactivated(partReference);
							}
						});
					}
				}
			}
		}
	}

	/**
	 * @param comPart
	 *            e4 wrapper around {@link IWorkbenchPart}
	 * @return can return null, in case {@link CompatibilityPart} was already
	 *         disposed
	 */
	private IWorkbenchPart getWrappedPart(CompatibilityPart comPart) {
		IWorkbenchPart part = comPart.getPart();
		if (part == null) {
			WorkbenchPlugin.log(new RuntimeException("Trying to access already disposed part: " //$NON-NLS-1$
					+ comPart));
		}
		return part;
	}

	public void firePartOpened(CompatibilityPart compatibilityPart) {
		final IWorkbenchPart part = getWrappedPart(compatibilityPart);
		final IWorkbenchPartReference partReference = compatibilityPart.getReference();

		if (part != null) {
			SaveablesList saveablesList = (SaveablesList) getWorkbenchWindow()
					.getService(ISaveablesLifecycleListener.class);
			saveablesList.postOpen(part);
			for (final IPartListener listener : partListenerList) {
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						listener.partOpened(part);
					}
				});
			}
		}

		for (final IPartListener2 listener : partListener2List) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partOpened(partReference);
				}
			});
		}

		if (part instanceof IPageChangeProvider) {
			((IPageChangeProvider) part).addPageChangedListener(pageChangedListener);
		}

		if (compatibilityPart instanceof CompatibilityView) {
			legacyWindow.firePerspectiveChanged(this, getPerspective(), partReference,
					CHANGE_VIEW_SHOW);
			legacyWindow.firePerspectiveChanged(this, getPerspective(), CHANGE_VIEW_SHOW);
		}
	}

	public void firePartClosed(CompatibilityPart compatibilityPart) {
		final IWorkbenchPart part = getWrappedPart(compatibilityPart);
		final WorkbenchPartReference partReference = compatibilityPart.getReference();
		MPart model = partReference.getModel();

		if (part != null) {
			SaveablesList modelManager = (SaveablesList) getWorkbenchWindow()
					.getService(ISaveablesLifecycleListener.class);
			Object postCloseInfo = modelManager.preCloseParts(Collections.singletonList(part), false,
					getWorkbenchWindow());
			if (postCloseInfo != null) {
				modelManager.postClose(postCloseInfo);
			}

			for (final IPartListener listener : partListenerList) {
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						listener.partClosed(part);
					}
				});
			}
		}

		for (final IPartListener2 listener : partListener2List) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partClosed(partReference);
				}
			});
		}

		if (part instanceof IViewPart) {
			viewReferences.remove(partReference);
		} else if (part != null) {
			editorReferences.remove(partReference);
		} else {
			// Whatever it was, try to cleanup the dirt
			viewReferences.remove(partReference);
			editorReferences.remove(partReference);
		}

		for (int i = 0; i < activationList.size(); i++) {
			if (model == activationList.get(i)) {
				activationList.remove(i);
				break;
			}
		}

		MPart activePart = partService.getActivePart();
		if (activePart == null) {
			// unset active part/editor sources if no active part found
			updateActivePartSources(null);
			updateActiveEditorSources(null);
		} else if (part instanceof IEditorPart) {
			// an editor got closed, update information about active editor
			IEditorPart activeEditor = getActiveEditor();
			if (activeEditor == null) {
				updateActiveEditorSources(activePart);
			} else {
				updateActiveEditorSources(findPart(activeEditor));
			}
		}

		if (part instanceof IPageChangeProvider) {
			((IPageChangeProvider) part).removePageChangedListener(pageChangedListener);
		}

		if (compatibilityPart instanceof CompatibilityView) {
			legacyWindow.firePerspectiveChanged(this, getPerspective(), partReference,
					CHANGE_VIEW_HIDE);
			legacyWindow.firePerspectiveChanged(this, getPerspective(), CHANGE_VIEW_HIDE);
		}
	}

	private void firePartBroughtToTop(MPart part) {
		Object client = part.getObject();
		if (client instanceof CompatibilityPart) {
			final IWorkbenchPart workbenchPart = getWrappedPart((CompatibilityPart) client);
			if (workbenchPart == null) {
				return;
			}
			final IWorkbenchPartReference partReference = getReference(workbenchPart);

			for (final IPartListener listener : partListenerList) {
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						listener.partBroughtToTop(workbenchPart);
					}
				});
			}

			for (final IPartListener2 listener : partListener2List) {
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						listener.partBroughtToTop(partReference);
					}
				});
			}
		} else {
			Integer val = partEvents.get(part);
			if (val == null) {
				partEvents.put(part, Integer.valueOf(FIRE_PART_BROUGHTTOTOP));
			} else {
				partEvents.put(part, Integer.valueOf(val.intValue() | FIRE_PART_BROUGHTTOTOP));
			}
		}
	}

	private WeakHashMap<MPart, Integer> partEvents = new WeakHashMap<>();
	private static final int FIRE_PART_VISIBLE = 0x1;
	private static final int FIRE_PART_BROUGHTTOTOP = 0x2;

	private EventHandler firingHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
			Object value = event.getProperty(UIEvents.EventTags.NEW_VALUE);
			if (value instanceof CompatibilityPart && element instanceof MPart) {
				Integer events = partEvents.remove(element);
				if (events != null) {
					int e = events.intValue();
					if ((e & FIRE_PART_VISIBLE) == FIRE_PART_VISIBLE) {
						firePartVisible((MPart) element);
					}
					if ((e & FIRE_PART_BROUGHTTOTOP) == FIRE_PART_BROUGHTTOTOP) {
						firePartBroughtToTop((MPart) element);
					}
				}
			}
		}
	};

	private EventHandler childrenHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			Object changedObj = event.getProperty(UIEvents.EventTags.ELEMENT);

			// ...in this window ?
			MUIElement changedElement = (MUIElement) changedObj;
			if (modelService.getTopLevelWindowFor(changedElement) != window)
				return;

			if (UIEvents.isADD(event)) {
				for (Object o : UIEvents.asIterable(event, UIEvents.EventTags.NEW_VALUE)) {
					if (!(o instanceof MUIElement))
						continue;

					// We have to iterate through the new elements to see if any
					// contain (or are) MParts (e.g. we may have dragged a split
					// editor which contains two editors, both with EditorRefs)
					MUIElement element = (MUIElement) o;
					List<MPart> addedParts = modelService.findElements(element, null, MPart.class,
							null);
					for (MPart part : addedParts) {
						IWorkbenchPartReference ref = (IWorkbenchPartReference) part
								.getTransientData().get(
								IWorkbenchPartReference.class.getName());

						// For now we only check for editors changing pages
						if (ref instanceof EditorReference && getEditorReference(part) == null) {
							addEditorReference((EditorReference) ref);
						}
					}
				}
			}
		}
	};

	// FIXME: convert me to e4 events!
	private void firePartVisible(MPart part) {
		Object client = part.getObject();
		if (client instanceof CompatibilityPart) {
			IWorkbenchPart workbenchPart = getWrappedPart((CompatibilityPart) client);
			if (workbenchPart == null) {
				return;
			}
			final IWorkbenchPartReference partReference = getReference(workbenchPart);

			for (final IPartListener2 listener : partListener2List) {
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						listener.partVisible(partReference);
					}
				});
			}
		} else {
			Integer val = partEvents.get(part);
			if (val == null) {
				partEvents.put(part, Integer.valueOf(FIRE_PART_VISIBLE));
			} else {
				partEvents.put(part, Integer.valueOf(val.intValue() | FIRE_PART_VISIBLE));
			}
		}
	}

	// FIXME: convert me to e4 events!
	public void firePartHidden(MPart part) {
		Object client = part.getObject();
		if (client instanceof CompatibilityPart) {
			IWorkbenchPart workbenchPart = getWrappedPart((CompatibilityPart) client);
			if (workbenchPart == null) {
				return;
			}
			final IWorkbenchPartReference partReference = getReference(workbenchPart);

			for (final IPartListener2 listener : partListener2List) {
				SafeRunner.run(new SafeRunnable() {
					@Override
					public void run() throws Exception {
						listener.partHidden(partReference);
					}
				});
			}
		}
	}

	public void firePartInputChanged(final IWorkbenchPartReference partReference) {
		for (final IPartListener2 listener : partListener2List) {
			SafeRunner.run(new SafeRunnable() {
				@Override
				public void run() throws Exception {
					listener.partInputChanged(partReference);
				}
			});
		}
	}

	@Override
	public int getEditorReuseThreshold() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		return store.getInt(IPreferenceConstants.REUSE_EDITORS);
	}

	@Override
	public void setEditorReuseThreshold(int openEditors) {
		// this is an empty implementation in 3.x, see IPageLayout's
		// setEditorReuseThreshold
	}

	/**
	 * Opens an editor represented by the descriptor with the given input.
	 *
	 * @param fileEditorInput
	 *            the input that the editor should open
	 * @param editorDescriptor
	 *            the descriptor of the editor to open
	 * @param activate
	 *            <tt>true</tt> if the editor should be activated,
	 *            <tt>false</tt> otherwise
	 * @param editorState
	 *            the previously saved state of the editor as a memento, this
	 *            may be <tt>null</tt>
	 * @return the opened editor
	 * @exception PartInitException
	 *                if the editor could not be created or initialized
	 */
	public IEditorPart openEditorFromDescriptor(IEditorInput fileEditorInput,
			IEditorDescriptor editorDescriptor, final boolean activate, final IMemento editorState)
			throws PartInitException {
		if (editorDescriptor.isOpenExternal()) {
			openExternalEditor((EditorDescriptor) editorDescriptor, fileEditorInput);
			return null;
		}
		return openEditor(fileEditorInput, editorDescriptor.getId(), activate, MATCH_INPUT,
				editorState, true);
	}

	/**
	 * Open a specific external editor on an file based on the descriptor.
	 */
	private IEditorReference openExternalEditor(final EditorDescriptor desc, IEditorInput input)
			throws PartInitException {
		final CoreException ex[] = new CoreException[1];

		final IPathEditorInput pathInput = getPathEditorInput(input);
		if (pathInput != null && pathInput.getPath() != null) {
			BusyIndicator.showWhile(legacyWindow.getWorkbench().getDisplay(), new Runnable() {
				@Override
				public void run() {
					try {
						if (desc.getLauncher() != null) {
							// open using launcher
							Object launcher = WorkbenchPlugin.createExtension(desc
									.getConfigurationElement(),
									IWorkbenchRegistryConstants.ATT_LAUNCHER);
							((IEditorLauncher) launcher).open(pathInput.getPath());
						} else {
							// open using command
							ExternalEditor oEditor = new ExternalEditor(pathInput.getPath(), desc);
							oEditor.open();
						}
					} catch (CoreException e) {
						ex[0] = e;
					}
				}
			});
		} else {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_errorOpeningExternalEditor, desc.getFileName(),
					desc.getId()));
		}

		if (ex[0] != null) {
			throw new PartInitException(NLS.bind(
					WorkbenchMessages.EditorManager_errorOpeningExternalEditor, desc.getFileName(),
					desc.getId()), ex[0]);
		}

		recordEditor(input, desc);
		// we do not have an editor part for external editors
		return null;
	}

	private IPathEditorInput getPathEditorInput(IEditorInput input) {
		if (input instanceof IPathEditorInput)
			return (IPathEditorInput) input;
		return Adapters.adapt(input, IPathEditorInput.class);
	}

	/**
	 * Unzooms the shared area if there are no more rendered parts contained
	 * within it.
	 *
	 * @see #unzoomSharedArea(MUIElement)
	 */
	private void unzoomSharedArea() {
		MPerspective curPersp = getPerspectiveStack().getSelectedElement();
		if (curPersp == null)
			return;

		MPlaceholder eaPH = (MPlaceholder) modelService.find(IPageLayout.ID_EDITOR_AREA, curPersp);
		for (MPart part : modelService.findElements(eaPH, null, MPart.class, null)) {
			if (part.isToBeRendered()) {
				MPlaceholder placeholder = part.getCurSharedRef();
				if (placeholder == null || placeholder.isToBeRendered()) {
					return;
				}
			}
		}

		setPartState(eaPH, null);
	}

	/**
	 * Unzooms the shared area if the specified element is in the shared area.
	 *
	 * @param element
	 *            the element to check if it is in the shared area
	 * @see #unzoomSharedArea()
	 */
	private void unzoomSharedArea(MUIElement element) {
		if (modelService.getElementLocation(element) == EModelService.IN_SHARED_AREA) {
			unzoomSharedArea();
		}
	}

	/**
	 * An event handler for listening to parts and placeholders being
	 * unrendered.
	 */
	private EventHandler referenceRemovalEventHandler = new EventHandler() {
		@Override
		public void handleEvent(Event event) {
			if (Boolean.TRUE.equals(event.getProperty(UIEvents.EventTags.NEW_VALUE))) {
				return;
			}

			Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
			if (element instanceof MPlaceholder) {
				MUIElement ref = ((MPlaceholder) element).getRef();
				// a placeholder has been unrendered, check to see if the shared
				// area needs to be unzoomed
				unzoomSharedArea(ref);

				if (ref instanceof MPart) {
					// find all placeholders for this part
					List<MPlaceholder> placeholders = modelService.findElements(window,
							ref.getElementId(), MPlaceholder.class, null,
							EModelService.IN_ANY_PERSPECTIVE | EModelService.IN_SHARED_AREA
									| EModelService.OUTSIDE_PERSPECTIVE);
					for (MPlaceholder placeholder : placeholders) {
						if (placeholder.getRef() == ref && placeholder.isToBeRendered()) {
							// if there's a rendered placeholder, return
							return;
						}
					}

					// no rendered placeholders around, unsubscribe
					ViewReference reference = getViewReference((MPart) ref);
					if (reference != null) {
						reference.unsubscribe();
					}
				}
			} else if (element instanceof MPart) {
				MPart part = (MPart) element;
				// a part has been unrendered, check to see if the shared
				// area needs to be unzoomed
				unzoomSharedArea(part);

				if (CompatibilityEditor.MODEL_ELEMENT_ID.equals(part.getElementId())) {
					EditorReference reference = getEditorReference(part);
					if (reference != null) {
						reference.unsubscribe();
					}
				}
			}
		}
	};

	public String getHiddenItems() {
		MPerspective perspective = getCurrentPerspective();
		if (perspective == null)
			return ""; //$NON-NLS-1$

		String result = perspective.getPersistedState().get(ModeledPageLayout.HIDDEN_ITEMS_KEY);
		if (result == null)
			return ""; //$NON-NLS-1$

		return result;
	}

	public void addHiddenItems(MPerspective perspective, String id) {
		String hiddenIDs = perspective.getPersistedState().get(ModeledPageLayout.HIDDEN_ITEMS_KEY);
		if (hiddenIDs == null)
			hiddenIDs = ""; //$NON-NLS-1$

		String persistedID = id + ","; //$NON-NLS-1$
		if (!hiddenIDs.contains(persistedID)) {
			hiddenIDs = hiddenIDs + persistedID;
			perspective.getPersistedState().put(ModeledPageLayout.HIDDEN_ITEMS_KEY, hiddenIDs);
		}
	}

	public void addHiddenItems(String id) {
		MPerspective perspective = getCurrentPerspective();
		if (perspective == null)
			return;
		addHiddenItems(perspective, id);
	}

	public void removeHiddenItems(MPerspective perspective, String id) {
		String persistedID = id + ","; //$NON-NLS-1$

		String hiddenIDs = perspective.getPersistedState().get(ModeledPageLayout.HIDDEN_ITEMS_KEY);
		if (hiddenIDs == null)
			return;

		String newValue = hiddenIDs.replaceFirst(persistedID, ""); //$NON-NLS-1$
		if (hiddenIDs.length() != newValue.length()) {
			if (newValue.length() == 0)
				perspective.getPersistedState().remove(ModeledPageLayout.HIDDEN_ITEMS_KEY);
			else
				perspective.getPersistedState().put(ModeledPageLayout.HIDDEN_ITEMS_KEY,
						newValue);
		}
	}

	public void removeHiddenItems(String id) {
		MPerspective perspective = getCurrentPerspective();
		if (perspective == null)
			return;
		removeHiddenItems(perspective, id);
	}

	public void setNewShortcuts(List<String> wizards, String tagPrefix) {
		MPerspective persp = getCurrentPerspective();
		if (persp == null)
			return;

		List<String> existingNewWizards = new ArrayList<>();
		for (String tag : persp.getTags()) {
			if (tag.contains(tagPrefix))
				existingNewWizards.add(tag);
		}

		List<String> newWizards = new ArrayList<>(wizards.size());
		for (String wizardName : wizards) {
			newWizards.add(tagPrefix + wizardName);
		}

		persp.getTags().removeAll(existingNewWizards);
		persp.getTags().addAll(newWizards);
	}

	/**
	 *
	 */
	public void resetToolBarLayout() {
		ICoolBarManager2 mgr = (ICoolBarManager2) legacyWindow.getCoolBarManager2();
		mgr.resetItemOrder();
	}

	/**
	 * Call {@link #firePartDeactivated(MPart)} if the passed part is the
	 * currently active part according to the part service. This method should
	 * only be called in the case of workbench shutdown, where E4 does not fire
	 * deactivate listeners on the active part.
	 *
	 * @param part
	 */
	public void firePartDeactivatedIfActive(MPart part) {
		if (partService.getActivePart() == part) {
			// At shutdown, e4 doesn't fire part deactivated on the active
			// part.
			firePartDeactivated(part);
		}
	}

	/**
	 * Add ToolItems for perspectives specified in "PERSPECTIVE_BAR_EXTRAS"
	 */
	private void createPerspectiveBarExtras() {
		String persps = PrefUtil.getAPIPreferenceStore()
				.getString(IWorkbenchPreferenceConstants.PERSPECTIVE_BAR_EXTRAS);
		// e3 allowed spaces and commas as separator
		String[] parts = persps.split("[, ]"); //$NON-NLS-1$
		Set<String> perspSet = new LinkedHashSet<>();
		for (String part : parts) {
			part = part.trim();
			if (!part.isEmpty())
				perspSet.add(part);
		}

		for (String perspId : perspSet) {
			MPerspective persp = (MPerspective) modelService.find(perspId, window);
			if (persp != null)
				continue; // already in stack, i.e. has already been added above
			IPerspectiveDescriptor desc = getDescriptorFor(perspId);
			if (desc == null)
				continue; // this perspective does not exist
			persp = createPerspective(desc);
			persp.setLabel(desc.getLabel());
			getPerspectiveStack().getChildren().add(persp);
			// "add" fires Event, causes creation of ToolItem on perspective bar
		}
	}

	private IPerspectiveDescriptor getDescriptorFor(String id) {
		IPerspectiveRegistry perspectiveRegistry = getWorkbenchWindow().getWorkbench().getPerspectiveRegistry();
		if (perspectiveRegistry instanceof PerspectiveRegistry) {
			return ((PerspectiveRegistry) perspectiveRegistry).findPerspectiveWithId(id, false);
		}

		return perspectiveRegistry.findPerspectiveWithId(id);
	}

}
