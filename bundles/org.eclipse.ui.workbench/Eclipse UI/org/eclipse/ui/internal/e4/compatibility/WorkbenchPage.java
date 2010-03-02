/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.compatibility;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MPerspective;
import org.eclipse.e4.ui.model.application.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.events.IEventBroker;
import org.eclipse.e4.workbench.modeling.EModelService;
import org.eclipse.e4.workbench.modeling.EPartService;
import org.eclipse.e4.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INavigationHistory;
import org.eclipse.ui.INavigationLocation;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.MultiPartInitException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.EditorDescriptor;
import org.eclipse.ui.views.IViewDescriptor;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * @since 3.5
 *
 */
public class WorkbenchPage implements IWorkbenchPage {
	
	static final String SECONDARY_ID_HEADER = "3x-secondary:"; //$NON-NLS-1$

	private WorkbenchWindow workbenchWindow;
	private ModeledPageLayout modelLayout;
	private IAdaptable input;
	private IPerspectiveDescriptor perspective;
	private List<IPerspectiveDescriptor> openedPerspectives = new ArrayList<IPerspectiveDescriptor>();
	private List<IPerspectiveDescriptor> sortedPerspectives = new ArrayList<IPerspectiveDescriptor>();

	@Inject
	private EPartService partService;

	@Inject
	private MApplication application;

	@Inject
	private MWindow window;

	@Inject
	private EModelService modelService;

	private List<IViewReference> viewReferences = new ArrayList<IViewReference>();
	private List<IEditorReference> editorReferences = new ArrayList<IEditorReference>();

	private ListenerList partListenerList = new ListenerList();
	private ListenerList partListener2List = new ListenerList();
	private ListenerList propertyChangeListeners = new ListenerList();

	private E4PartListener e4PartListener = new E4PartListener();

	private EventHandler selectedHandler = new EventHandler() {
		public void handleEvent(Event event) {
			Object selected = event.getProperty(UIEvents.EventTags.NEW_VALUE);
			Object oldSelected = event.getProperty(UIEvents.EventTags.OLD_VALUE);

			if (oldSelected instanceof MPart) {
				MPart oldSelectedPart = (MPart) oldSelected;
				if (oldSelectedPart.isToBeRendered()) {
					firePartHidden(oldSelectedPart);
				}
			}

			if (selected instanceof MPart) {
				MPart selectedPart = (MPart) selected;
				if (selectedPart.isToBeRendered()) {
					firePartBroughtToTop(selectedPart);
					firePartVisible(selectedPart);
				}
			}
		}
	};

	/**
	 * @param workbenchWindow
	 * @param input
	 */
	public WorkbenchPage(WorkbenchWindow workbenchWindow, IAdaptable input) {
		this.workbenchWindow = workbenchWindow;
		this.input = input;
	}

	@PostConstruct
	void postConstruct() throws InvocationTargetException, InstantiationException {
		partService.addPartListener(e4PartListener);
		window.getContext().set(IPartService.class.getName(), this);

		IEventBroker eventBroker = (IEventBroker) window.getContext().get(
				IEventBroker.class.getName());
		eventBroker.subscribe(UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
				UIEvents.ElementContainer.SELECTEDELEMENT), selectedHandler);

		Collection<MPart> parts = partService.getParts();
		for (MPart part : parts) {
			String uri = part.getURI();
			if (uri.equals(CompatibilityPart.COMPATIBILITY_VIEW_URI)) {
				createViewReferenceForPart(part, part.getId());
			} else if (uri.equals(CompatibilityPart.COMPATIBILITY_EDITOR_URI)) {
				// TODO compat: we need that editor input back, or we have squat
				createEditorReferenceForPart(part, null, part.getId());
			}
		}
	}

	List<IEditorReference> getInternalEditorReferences() {
		return editorReferences;
	}

	public ViewReference getViewReference(MPart part) {
		for (IViewReference ref : viewReferences) {
			if (((ViewReference) ref).getModel() == part) {
				return (ViewReference) ref;
			}
		}
		return null;
	}

	public void addViewReference(IViewReference viewReference) {
		viewReferences.add(viewReference);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#activate(org.eclipse.ui.IWorkbenchPart)
	 */
	public void activate(IWorkbenchPart part) {
		MPart mpart = findPart(part);
		if (mpart != null) {
			partService.activate(mpart);
			part.setFocus();
			processEventLoop();
		}
	}

	void processEventLoop() {
		Display display = getWorkbenchWindow().getShell().getDisplay();
		if (display.isDisposed()) {
			return;
		}
		while (display.readAndDispatch())
			;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.add(listener);
	}

	MPart findPart(IWorkbenchPart part) {
		for (Iterator<IViewReference> it = viewReferences.iterator(); it.hasNext();) {
			IViewReference reference = it.next();
			if (part == reference.getPart(false)) {
				return ((WorkbenchPartReference) reference).getModel();
			}
		}

		for (Iterator<IEditorReference> it = editorReferences.iterator(); it.hasNext();) {
			IEditorReference reference = it.next();
			if (part == reference.getPart(false)) {
				return ((WorkbenchPartReference) reference).getModel();
			}
		}
		return null;
	}

	private void firePartActivated(MPart part) {
		Object client = part.getObject();
		if (client instanceof CompatibilityPart) {
			IWorkbenchPart workbenchPart = ((CompatibilityPart) client).getPart();
			IWorkbenchPartReference partReference = getReference(workbenchPart);

			for (Object listener : partListenerList.getListeners()) {
				((IPartListener) listener).partActivated(workbenchPart);
			}

			for (Object listener : partListener2List.getListeners()) {
				((IPartListener2) listener).partActivated(partReference);
			}
		}
	}

	private void firePartBroughtToTop(MPart part) {
		Object client = part.getObject();
		if (client instanceof CompatibilityPart) {
			IWorkbenchPart workbenchPart = ((CompatibilityPart) client).getPart();
			IWorkbenchPartReference partReference = getReference(workbenchPart);

			for (Object listener : partListenerList.getListeners()) {
				((IPartListener) listener).partBroughtToTop(workbenchPart);
			}

			for (Object listener : partListener2List.getListeners()) {
				((IPartListener2) listener).partBroughtToTop(partReference);
			}
		}
	}

	// FIXME: convert me to e4 events!
	void firePartClosed(CompatibilityPart compatibilityPart) {
		IWorkbenchPart part = compatibilityPart.getPart();
		IWorkbenchPartReference partReference = compatibilityPart.getReference();

		for (Object listener : partListenerList.getListeners()) {
			((IPartListener) listener).partClosed(part);
		}

		for (Object listener : partListener2List.getListeners()) {
			((IPartListener2) listener).partClosed(partReference);
		}
	}

	// FIXME: convert me to e4 events!
	private void firePartVisible(MPart part) {
		Object client = part.getObject();
		if (client instanceof CompatibilityPart) {
			IWorkbenchPart workbenchPart = ((CompatibilityPart) client).getPart();
			IWorkbenchPartReference partReference = getReference(workbenchPart);

			for (Object listener : partListener2List.getListeners()) {
				((IPartListener2) listener).partVisible(partReference);
			}
		}
	}

	// FIXME: convert me to e4 events!
	private void firePartHidden(MPart part) {
		Object client = part.getObject();
		if (client instanceof CompatibilityPart) {
			IWorkbenchPart workbenchPart = ((CompatibilityPart) client).getPart();
			IWorkbenchPartReference partReference = getReference(workbenchPart);

			for (Object listener : partListener2List.getListeners()) {
				((IPartListener2) listener).partHidden(partReference);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#bringToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void bringToTop(IWorkbenchPart part) {
		MPart mpart = findPart(part);
		if (mpart != null) {
			partService.bringToTop(mpart);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#close()
	 */
	public boolean close() {
		if (!closeAllEditors(true)) {
			return false;
		}

		for (IViewPart view : getViews()) {
			hideView(view);
		}

		partService.removePartListener(e4PartListener);
		workbenchWindow.setActivePage(null);
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#closeAllEditors(boolean)
	 */
	public boolean closeAllEditors(boolean save) {
		for (IEditorPart editor : getEditors()) {
			if (!closeEditor(editor, save)) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#closeEditors(org.eclipse.ui.IEditorReference[], boolean)
	 */
	public boolean closeEditors(IEditorReference[] editorRefs, boolean save) {
		for (IEditorReference editorRef : editorRefs) {
			MPart model = ((EditorReference) editorRef).getModel();
			if (!(hidePart(model, save))) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#closeEditor(org.eclipse.ui.IEditorPart, boolean)
	 */
	public boolean closeEditor(IEditorPart editor, boolean save) {
		MPart part = null;
		for (Iterator<IEditorReference> it = editorReferences.iterator(); it.hasNext();) {
			IEditorReference reference = it.next();
			if (editor == reference.getPart(false)) {
				part = ((EditorReference) reference).getModel();
				break;
			}
		}

		if (part != null) {
			return hidePart(part, save);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#findView(java.lang.String)
	 */
	public IViewPart findView(String viewId) {
		MPart part = partService.findPart(viewId);
		if (part != null) {
			Object object = part.getObject();
			if (object instanceof CompatibilityView) {
				return ((CompatibilityView) object).getView();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#findViewReference(java.lang.String)
	 */
	public IViewReference findViewReference(String viewId) {
		return findViewReference(viewId, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#findViewReference(java.lang.String, java.lang.String)
	 */
	public IViewReference findViewReference(String viewId, String secondaryId) {
		for (IViewReference reference : viewReferences) {
			if (viewId.equals(reference.getId())) {
				String refSecondaryId = reference.getSecondaryId();
				if (refSecondaryId == null) {
					if (secondaryId == null) {
						return reference;
					}
				} else if (refSecondaryId.equals(secondaryId)) {
					return reference;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getActiveEditor()
	 */
	public IEditorPart getActiveEditor() {
		IWorkbenchPart part = getActivePart();
		return (IEditorPart) (part instanceof IEditorPart ? part : null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#findEditor(org.eclipse.ui.IEditorInput)
	 */
	public IEditorPart findEditor(IEditorInput input) {
		IEditorReference[] references = findEditors(input, null, MATCH_INPUT);
		return references.length == 0 ? null : references[0].getEditor(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#findEditors(org.eclipse.ui.IEditorInput, java.lang.String, int)
	 */
	public IEditorReference[] findEditors(IEditorInput input, String editorId, int matchFlags) {
		switch (matchFlags) {
		case MATCH_NONE:
			return new IEditorReference[0];
		case MATCH_INPUT:
			List<IEditorReference> editorRefs = new ArrayList<IEditorReference>();
			for (IEditorReference editorRef : editorReferences) {
				IEditorPart editor = editorRef.getEditor(false);
				if (editor == null) {
					try {
						if (input.equals(editorRef.getEditorInput())) {
							editorRefs.add(editorRef);
						}
					} catch (PartInitException e) {
						WorkbenchPlugin.log(e);
					}
				} else if (editor.getEditorInput().equals(input)) {
					editorRefs.add(editorRef);
				}
			}
			return editorRefs.toArray(new IEditorReference[editorRefs.size()]);
		case MATCH_ID:
			editorRefs = new ArrayList<IEditorReference>();
			for (IEditorReference editorRef : editorReferences) {
				if (editorId.equals(editorRef.getId())) {
					editorRefs.add(editorRef);
				}
			}
			return editorRefs.toArray(new IEditorReference[editorRefs.size()]);
		default:
			// TODO Auto-generated catch block
			return new IEditorReference[0];
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getEditors()
	 */
	public IEditorPart[] getEditors() {
		int length = editorReferences.size();
		IEditorPart[] editors = new IEditorPart[length];
		for (int i = 0; i < length; i++) {
			editors[i] = editorReferences.get(i).getEditor(true);
		}
		return editors;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getEditorReferences()
	 */
	public IEditorReference[] getEditorReferences() {
		return editorReferences.toArray(new IEditorReference[editorReferences.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getDirtyEditors()
	 */
	public IEditorPart[] getDirtyEditors() {
		List<IEditorPart> dirtyEditors = new ArrayList<IEditorPart>();
		for (IEditorReference editorRef : editorReferences) {
			IEditorPart editor = editorRef.getEditor(false);
			if (editor != null && editor.isDirty()) {
				dirtyEditors.add(editor);
			}
		}
		return dirtyEditors.toArray(new IEditorPart[dirtyEditors.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getInput()
	 */
	public IAdaptable getInput() {
		return input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getLabel()
	 */
	public String getLabel() {
		// IWorkbenchPage#testGetLabel checks for non-null
		String label = window.getLabel();
		return label == null ? "" : label; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getPerspective()
	 */
	public IPerspectiveDescriptor getPerspective() {
		return perspective;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getViewReferences()
	 */
	public IViewReference[] getViewReferences() {
		return viewReferences.toArray(new IViewReference[viewReferences.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getViews()
	 */
	public IViewPart[] getViews() {
		int length = viewReferences.size();
		IViewPart[] views = new IViewPart[length];
		for (int i = 0; i < length; i++) {
			views[i] = viewReferences.get(i).getView(true);
		}
		return views;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getWorkbenchWindow()
	 */
	public IWorkbenchWindow getWorkbenchWindow() {
		return workbenchWindow;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getWorkingSet()
	 */
	public IWorkingSet getWorkingSet() {
		// FIXME compat getWorkingSet
		E4Util.unsupported("getWorkingSet"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#hideActionSet(java.lang.String)
	 */
	public void hideActionSet(String actionSetID) {
		// FIXME compat hideActionSet
		E4Util.unsupported("hideActionSet"); //$NON-NLS-1$

	}

	private boolean hidePart(MPart part, boolean save) {
		if (!partService.getParts().contains(part)) {
			return false;
		}

		Object clientObject = part.getObject();
		if (!(clientObject instanceof CompatibilityPart)) {
			return false;
		}

		CompatibilityPart compatibilityPart = (CompatibilityPart) clientObject;
		IWorkbenchPart workbenchPart = compatibilityPart.getPart();
		if (save) {
			if (workbenchPart instanceof ISaveablePart) {
				ISaveablePart saveablePart = (ISaveablePart) workbenchPart;
				if (!saveSaveable(saveablePart, true, true)) {
					return false;
				}
			}
		}

		for (Iterator<IViewReference> it = viewReferences.iterator(); it.hasNext();) {
			IViewReference reference = it.next();
			if (workbenchPart == reference.getPart(false)) {
				it.remove();
				partService.hidePart(part);
				return true;
			}
		}

		for (Iterator<IEditorReference> it = editorReferences.iterator(); it.hasNext();) {
			IEditorReference reference = it.next();
			if (workbenchPart == reference.getPart(false)) {
				it.remove();
				partService.hidePart(part);
				return true;
			}
		}
		return false;
	}

	private void hidePart(String id) {
		MPart part = partService.findPart(id);
		if (part != null) {
			hidePart(part, true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPage#hideView(org.eclipse.ui.IViewPart)
	 */
	public void hideView(IViewPart view) {
		if (view != null) {
			hidePart(view.getSite().getId());
		}
	}

	@Inject
	IPresentationEngine engine;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#hideView(org.eclipse.ui.IViewReference)
	 */
	public void hideView(IViewReference view) {
		if (view != null) {
			hidePart(view.getId());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#isPartVisible(org.eclipse.ui.IWorkbenchPart)
	 */
	public boolean isPartVisible(IWorkbenchPart part) {
		MPart mpart = findPart(part);
		return mpart == null ? false : partService.isPartVisible(mpart);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#isEditorAreaVisible()
	 */
	public boolean isEditorAreaVisible() {
		// FIXME compat isEditorAreaVisible
		E4Util.unsupported("isEditorAreaVisible"); //$NON-NLS-1$
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#reuseEditor(org.eclipse.ui.IReusableEditor, org.eclipse.ui.IEditorInput)
	 */
	public void reuseEditor(IReusableEditor editor, IEditorInput input) {
		editor.setInput(input);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(org.eclipse.ui.IEditorInput, java.lang.String)
	 */
	public IEditorPart openEditor(IEditorInput input, String editorId) throws PartInitException {
		return openEditor(input, editorId, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(org.eclipse.ui.IEditorInput, java.lang.String, boolean)
	 */
	public IEditorPart openEditor(IEditorInput input, String editorId, boolean activate)
			throws PartInitException {
		return openEditor(input, editorId, activate, MATCH_INPUT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(org.eclipse.ui.IEditorInput, java.lang.String, boolean, int)
	 */
	public IEditorPart openEditor(IEditorInput input, String editorId, boolean activate,
			int matchFlags) throws PartInitException {
		try {
			return internalOpenEditor(input, editorId, activate, matchFlags);
		} finally {
			processEventLoop(); // FIXME: remove when bug 299529 is fixed
		}
	}

	private IEditorPart internalOpenEditor(IEditorInput input, String editorId, boolean activate,
			int matchFlags) throws PartInitException {
		if (matchFlags == MATCH_INPUT) {
			IEditorPart editor = findEditor(input);
			if (editor != null) {
				if (editor instanceof IShowEditorInput) {
					((IShowEditorInput) editor).showEditorInput(input);
				}

				if (activate) {
					activate(editor);
				}
				return editor;
			}
		}



		MPart editor = partService.createPart("org.eclipse.e4.ui.compatibility.editor"); //$NON-NLS-1$
		createEditorReferenceForPart(editor, input, editorId);
		partService.showPart(editor, PartState.VISIBLE);

		CompatibilityEditor compatibilityEditor = (CompatibilityEditor) editor.getObject();

		if (activate) {
			partService.activate(editor);
			compatibilityEditor.delegateSetFocus();
		}


		return compatibilityEditor.getEditor();
	}

	private void createEditorReferenceForPart(final MPart part, IEditorInput input, String editorId) {
		IEditorRegistry registry = workbenchWindow.getWorkbench().getEditorRegistry();
		EditorDescriptor descriptor = (EditorDescriptor) registry.findEditor(editorId);
		final EditorReference ref = new EditorReference(window.getContext(), this, part, input,
				descriptor);
		editorReferences.add(ref);
		final IEventBroker broker = (IEventBroker) application.getContext().get(
				IEventBroker.class.getName());
		broker.subscribe(UIEvents.buildTopic(UIEvents.Context.TOPIC, UIEvents.Context.CONTEXT),
				new EventHandler() {
					public void handleEvent(Event event) {
						Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
						if (element == part) {
							if (part.getContext() != null) {
								broker.unsubscribe(this);
								part.getContext().set(EditorReference.class.getName(), ref);
							}
						}
					}
				});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		propertyChangeListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#resetPerspective()
	 */
	public void resetPerspective() {
		// FIXME compat resetPerspective
		E4Util.unsupported("resetPerspective"); //$NON-NLS-1$

	}

	private boolean saveAllEditors(boolean confirm, boolean closing) {
		for (IEditorPart editor : getEditors()) {
			if (!saveSaveable(editor, confirm, closing)) {
				return false;
			}
		}

		for (IViewPart view : getViews()) {
			if (view instanceof ISaveablePart) {
				if (!saveSaveable((ISaveablePart) view, confirm, closing)) {
					return false;
				}
			}
		}

		return partService.saveAll(confirm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPage#saveAllEditors(boolean)
	 */
	public boolean saveAllEditors(boolean confirm) {
		return saveAllEditors(confirm, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#saveEditor(org.eclipse.ui.IEditorPart, boolean)
	 */
	private boolean saveSaveable(ISaveablePart saveable, boolean confirm, boolean closing) {
		Collection<MPart> parts = partService.getParts();
		for (MPart part : parts) {
			Object client = part.getObject();
			if (client instanceof CompatibilityPart) {
				if (((CompatibilityPart) client).getPart() == saveable) {
					if (saveable.isDirty()) {
						if (closing) {
							if (saveable.isSaveOnCloseNeeded()) {
								return partService.savePart(part, confirm);
							}
						} else {
							return partService.savePart(part, confirm);
						}
					}
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPage#saveEditor(org.eclipse.ui.IEditorPart,
	 * boolean)
	 */
	public boolean saveEditor(IEditorPart editor, boolean confirm) {
		return saveSaveable(editor, confirm, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#savePerspective()
	 */
	public void savePerspective() {
		// FIXME compat savePerspective
		E4Util.unsupported("savePerspective"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#savePerspectiveAs(org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void savePerspectiveAs(IPerspectiveDescriptor perspective) {
		// FIXME compat savePerspectiveAs
		E4Util.unsupported("savePerspectiveAs"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#setEditorAreaVisible(boolean)
	 */
	public void setEditorAreaVisible(boolean showEditorArea) {
		// FIXME compat setEditorAreaVisible
		E4Util.unsupported("setEditorAreaVisible"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#setPerspective(org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void setPerspective(IPerspectiveDescriptor perspective) {
		if (perspective == null)
			return;
		this.perspective = perspective;
		
		if (sortedPerspectives.contains(perspective)) {
			sortedPerspectives.remove(perspective);
		}
		sortedPerspectives.add(perspective);

		if (!openedPerspectives.contains(perspective)) {
			openedPerspectives.add(perspective);
		}

		MPerspectiveStack perspectives = getPerspectiveStack();
		for (MPerspective mperspective : perspectives.getChildren()) {
			if (mperspective.getId().equals(perspective.getId())) {
				// this perspective already exists, switch to this one
				perspectives.setSelectedElement(mperspective);
				return;
			}
		}

		// couldn't find the perspective, create a new one
		MPerspective modelPerspective = MApplicationFactory.eINSTANCE.createPerspective();
		// tag it with the same id
		modelPerspective.setId(perspective.getId());

		// instantiate the perspective
		IPerspectiveFactory factory = ((PerspectiveDescriptor) perspective).createFactory();
		modelLayout = new ModeledPageLayout(application, modelService, window,
				modelPerspective, perspective, this);
		factory.createInitialLayout(modelLayout);

		// add it to the stack
		perspectives.getChildren().add(modelPerspective);
		// activate it
		perspectives.setSelectedElement(modelPerspective);

		// FIXME: we need to fire events
	}

	/**
	 * Retrieves the perspective stack of the window that's containing this
	 * workbench page.
	 * 
	 * @return the stack of perspectives of this page's containing window
	 */
	private MPerspectiveStack getPerspectiveStack() {
		for (MPSCElement child : window.getChildren()) {
			if (child instanceof MPerspectiveStack) {
				return (MPerspectiveStack) child;
			}
		}

		MPerspectiveStack perspectiveStack = MApplicationFactory.eINSTANCE.createPerspectiveStack();
		window.getChildren().add(perspectiveStack);
		window.setSelectedElement(perspectiveStack);
		return perspectiveStack;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#showActionSet(java.lang.String)
	 */
	public void showActionSet(String actionSetID) {
		// FIXME compat showActionSet
		E4Util.unsupported("showActionSet"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#showView(java.lang.String)
	 */
	public IViewPart showView(String viewId) throws PartInitException {
		return showView(viewId, null, VIEW_ACTIVATE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#showView(java.lang.String, java.lang.String, int)
	 */
	public IViewPart showView(String viewId, String secondaryId, int mode) throws PartInitException {
		try {
			return internalShowView(viewId, secondaryId, mode);
		} finally {
			processEventLoop(); // FIXME: remove when bug 299529 is fixed
		}
	}

	private MPart findPart(String viewId, String secondaryId) {
		if (secondaryId == null) {
			return partService.findPart(viewId);
		}

		Collection<MPart> parts = partService.getParts();
		for (MPart part : parts) {
			if (part.getId().equals(viewId) && part.getTags().contains(secondaryId)) {
				return part;
			}
		}
		return null;
	}

	private IViewPart internalShowView(String viewId, String secondaryId, int mode)
			throws PartInitException {
		switch (mode) {
		case VIEW_ACTIVATE:
		case VIEW_VISIBLE:
		case VIEW_CREATE:
			break;
		default:
			throw new IllegalArgumentException(WorkbenchMessages.WorkbenchPage_IllegalViewMode);
		}

		if (secondaryId != null) {
			if (secondaryId.length() == 0 || secondaryId.indexOf(':') != -1) {
				throw new IllegalArgumentException(
						WorkbenchMessages.WorkbenchPage_IllegalSecondaryId);
			}

			secondaryId = SECONDARY_ID_HEADER + secondaryId;

			MPartDescriptor descriptor = findDescriptor(viewId);
			if (!descriptor.isAllowMultiple()) {
				throw new PartInitException(NLS.bind(WorkbenchMessages.ViewFactory_noMultiple,
						viewId));
			}
		}

		MPart part = findPart(viewId, secondaryId);
		if (part == null) {
			part = partService.createPart(viewId);
			if (part == null) {
				throw new PartInitException(NLS.bind(WorkbenchMessages.ViewFactory_couldNotCreate,
						viewId));
			}

			partService.showPart(part, convert(mode));

			if (secondaryId != null) {
				part.getTags().add(secondaryId);
			}

			CompatibilityView compatibilityView = (CompatibilityView) part.getObject();


			return compatibilityView.getView();
		}

		part = partService.showPart(part, convert(mode));
		if (secondaryId != null) {
			part.getTags().add(secondaryId);
		}

		CompatibilityView compatibilityView = (CompatibilityView) part.getObject();

		if (mode == VIEW_ACTIVATE) {
			compatibilityView.delegateSetFocus();
		}

		return compatibilityView.getView();
	}

	void createViewReferenceForPart(final MPart part, String viewId) {
		IViewDescriptor desc = getWorkbenchWindow().getWorkbench().getViewRegistry().find(viewId);
		final ViewReference ref = new ViewReference(window.getContext(), this, part,
				(ViewDescriptor) desc);
		IEclipseContext partContext = part.getContext();
		if (partContext == null) {
			final IEventBroker broker = (IEventBroker) application.getContext().get(
					IEventBroker.class.getName());
			broker.subscribe(UIEvents.buildTopic(UIEvents.Context.TOPIC, UIEvents.Context.CONTEXT),
					new EventHandler() {
						public void handleEvent(Event event) {
							Object element = event.getProperty(UIEvents.EventTags.ELEMENT);
							if (element == part) {
								if (part.getContext() != null) {
									broker.unsubscribe(this);
									part.getContext().set(ViewReference.class.getName(), ref);
								}
							}
						}
					});
		} else {
			partContext.set(ViewReference.class.getName(), ref);
		}
		viewReferences.add(ref);
	}

	private MPartDescriptor findDescriptor(String id) {
		for (MPartDescriptor descriptor : application.getDescriptors()) {
			if (descriptor.getId().equals(id)) {
				return descriptor;
			}
		}
		return null;
	}

	private PartState convert(int mode) {
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#isEditorPinned(org.eclipse.ui.IEditorPart)
	 */
	public boolean isEditorPinned(IEditorPart editor) {
		IEditorReference reference = (IEditorReference) getReference(editor);
		return reference == null ? false : reference.isPinned();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getEditorReuseThreshold()
	 */
	public int getEditorReuseThreshold() {
		IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
		return store.getInt(IPreferenceConstants.REUSE_EDITORS);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#setEditorReuseThreshold(int)
	 */
	public void setEditorReuseThreshold(int openEditors) {
		// this is an empty implementation in 3.x, see IPageLayout's
		// setEditorReuseThreshold
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getNavigationHistory()
	 */
	public INavigationHistory getNavigationHistory() {
		// FIXME compat getNavigationHistory
		E4Util.unsupported("getNavigationHistory"); //$NON-NLS-1$
		return new INavigationHistory() {

			public void markLocation(IEditorPart part) {
			}

			public INavigationLocation[] getLocations() {
				return new INavigationLocation[] { getCurrentLocation() };
			}

			public INavigationLocation getCurrentLocation() {
				return new INavigationLocation() {

					public void update() {
					}

					public void setInput(Object input) {
					}

					public void saveState(IMemento memento) {
					}

					public void restoreState(IMemento memento) {
					}

					public void restoreLocation() {
					}

					public void releaseState() {
					}

					public boolean mergeInto(INavigationLocation currentLocation) {
						return false;
					}

					public String getText() {
						return "nowhere"; //$NON-NLS-1$
					}

					public Object getInput() {
						return null;
					}

					public void dispose() {
					}
				};
			}
		};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getViewStack(org.eclipse.ui.IViewPart)
	 */
	public IViewPart[] getViewStack(IViewPart part) {
		MPart mpart = partService.findPart(part.getSite().getId());
		if (mpart != null) {
			MElementContainer<?> parent = mpart.getParent();
			if (parent instanceof MPartStack) {
				List<CompatibilityView> stack = new ArrayList<CompatibilityView>();

				for (Object child : parent.getChildren()) {
					MPart siblingPart = (MPart) child;
					Object siblingObject = siblingPart.getObject();
					if (siblingObject instanceof CompatibilityView) {
						stack.add((CompatibilityView) siblingObject);
					}
				}

				// sort the list by activation order (most recently activated
				// first)
				Collections.sort(stack, new Comparator<CompatibilityView>() {
					public int compare(CompatibilityView o1, CompatibilityView o2) {
						int pos1 = (-1) * activationList.indexOf(o1.getModel());
						int pos2 = (-1) * activationList.indexOf(o2.getModel());
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getNewWizardShortcuts()
	 */
	public String[] getNewWizardShortcuts() {
		ArrayList shortcuts = modelLayout.getNewWizardShortcuts();
		return (String[]) shortcuts.toArray(new String[shortcuts.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getPerspectiveShortcuts()
	 */
	public String[] getPerspectiveShortcuts() {
		ArrayList shortcuts = modelLayout.getPerspectiveShortcuts();
		return (String[]) shortcuts.toArray(new String[shortcuts.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getShowViewShortcuts()
	 */
	public String[] getShowViewShortcuts() {
		ArrayList shortcuts = modelLayout.getShowViewShortcuts();
		return (String[]) shortcuts.toArray(new String[shortcuts.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getOpenPerspectives()
	 */
	public IPerspectiveDescriptor[] getOpenPerspectives() {
		return openedPerspectives.toArray(new IPerspectiveDescriptor[openedPerspectives.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getSortedPerspectives()
	 */
	public IPerspectiveDescriptor[] getSortedPerspectives() {
		return sortedPerspectives.toArray(new IPerspectiveDescriptor[sortedPerspectives.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#closePerspective(org.eclipse.ui.IPerspectiveDescriptor, boolean, boolean)
	 */
	public void closePerspective(IPerspectiveDescriptor desc, boolean saveParts, boolean closePage) {
		if (openedPerspectives.size() == 1) {
			closeAllPerspectives(saveParts, closePage);
		} else {
			// TODO Auto-generated method stub
			sortedPerspectives.remove(desc);
			openedPerspectives.remove(desc);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#closeAllPerspectives(boolean, boolean)
	 */
	public void closeAllPerspectives(boolean saveEditors, boolean closePage) {
		// TODO Auto-generated method stub
		if (saveEditors) {
			saveAllEditors(true, true);
		}

		sortedPerspectives.clear();
		openedPerspectives.clear();

		for (MPart part : partService.getParts()) {
			hidePart(part, false);
		}

		if (closePage) {
			workbenchWindow.setActivePage(null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getExtensionTracker()
	 */
	public IExtensionTracker getExtensionTracker() {
		// FIXME compat we'll probably need this at some point, or use the
		// window version
		E4Util.unsupported("getExtensionTracker"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getWorkingSets()
	 */
	public IWorkingSet[] getWorkingSets() {
		// FIXME compat most of working sets API has been added back to
		// WorkbenchPlugin
		E4Util.unsupported("getWorkingSets"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#setWorkingSets(org.eclipse.ui.IWorkingSet[])
	 */
	public void setWorkingSets(IWorkingSet[] sets) {
		// FIXME compat setWorkingSets
		E4Util.unsupported("setWorkingSets"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getAggregateWorkingSet()
	 */
	public IWorkingSet getAggregateWorkingSet() {
		// FIXME compat getAggregateWorkingSet
		E4Util.unsupported("getAggregateWorkingSet"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#isPageZoomed()
	 */
	public boolean isPageZoomed() {
		// FIXME compat: the page is not zoomed :-)
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#zoomOut()
	 */
	public void zoomOut() {
		// FIXME compat zoomOut
		E4Util.unsupported("zoomOut"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#toggleZoom(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void toggleZoom(IWorkbenchPartReference ref) {
		// FIXME compat toggleZoom
		E4Util.unsupported("toggleZoom"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getPartState(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public int getPartState(IWorkbenchPartReference ref) {
		// FIXME compat getPartState
		return STATE_RESTORED;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#setPartState(org.eclipse.ui.IWorkbenchPartReference, int)
	 */
	public void setPartState(IWorkbenchPartReference ref, int state) {
		// FIXME compat setPartState
		E4Util.unsupported("setPartState"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getReference(org.eclipse.ui.IWorkbenchPart)
	 */
	public IWorkbenchPartReference getReference(IWorkbenchPart part) {
		for (IEditorReference editorRef : editorReferences) {
			if (editorRef.getPart(false) == part) {
				return editorRef;
			}
		}

		for (IViewReference viewRef : viewReferences) {
			if (viewRef.getPart(false) == part) {
				return viewRef;
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#showEditor(org.eclipse.ui.IEditorReference)
	 */
	public void showEditor(IEditorReference ref) {
		// FIXME compat showEditor
		E4Util.unsupported("showEditor"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#hideEditor(org.eclipse.ui.IEditorReference)
	 */
	public void hideEditor(IEditorReference ref) {
		// FIXME compat hideEditor
		E4Util.unsupported("hideEditor"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#openEditors(org.eclipse.ui.IEditorInput[], java.lang.String[], int)
	 */
	public IEditorReference[] openEditors(IEditorInput[] inputs, String[] editorIDs, int matchFlags)
			throws MultiPartInitException {
		Assert.isTrue(inputs.length == editorIDs.length);

		PartInitException[] exceptions = new PartInitException[inputs.length];
		IEditorReference[] references = new IEditorReference[inputs.length];
		boolean hasFailures = false;

		for (int i = 0; i < inputs.length; i++) {
			try {
				IEditorPart editor = openEditor(inputs[i], editorIDs[i], i == 0, matchFlags);
				references[i] = (IEditorReference) getReference(editor);
			} catch (PartInitException e) {
				if (!hasFailures) {
					hasFailures = true;
					exceptions[i] = e;
				}
			}
		}

		if (hasFailures) {
			throw new MultiPartInitException(references, exceptions);
		}

		return references;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartService#addPartListener(org.eclipse.ui.IPartListener)
	 */
	public void addPartListener(IPartListener listener) {
		partListenerList.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartService#addPartListener(org.eclipse.ui.IPartListener2)
	 */
	public void addPartListener(IPartListener2 listener) {
		partListener2List.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartService#getActivePart()
	 */
	public IWorkbenchPart getActivePart() {
		processEventLoop(); // FIXME: remove when bug 299529 is fixed
		MPart part = partService.getActivePart();
		if (part != null) {
			Object object = part.getObject();
			if (object instanceof CompatibilityPart) {
				return ((CompatibilityPart) object).getPart();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartService#getActivePartReference()
	 */
	public IWorkbenchPartReference getActivePartReference() {
		IWorkbenchPart part = getActivePart();
		if (part != null) {
			for (IWorkbenchPartReference reference : viewReferences) {
				if (reference.getPart(false) == part) {
					return reference;
				}
			}

			for (IWorkbenchPartReference reference : editorReferences) {
				if (reference.getPart(false) == part) {
					return reference;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartService#removePartListener(org.eclipse.ui.IPartListener)
	 */
	public void removePartListener(IPartListener listener) {
		partListenerList.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartService#removePartListener(org.eclipse.ui.IPartListener2)
	 */
	public void removePartListener(IPartListener2 listener) {
		partListener2List.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#addSelectionListener(org.eclipse.ui.ISelectionListener)
	 */
	public void addSelectionListener(ISelectionListener listener) {
		workbenchWindow.getSelectionService().addSelectionListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#addSelectionListener(java.lang.String, org.eclipse.ui.ISelectionListener)
	 */
	public void addSelectionListener(String partId, ISelectionListener listener) {
		workbenchWindow.getSelectionService().addSelectionListener(partId, listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#addPostSelectionListener(org.eclipse.ui.ISelectionListener)
	 */
	public void addPostSelectionListener(ISelectionListener listener) {
		workbenchWindow.getSelectionService().addPostSelectionListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#addPostSelectionListener(java.lang.String, org.eclipse.ui.ISelectionListener)
	 */
	public void addPostSelectionListener(String partId, ISelectionListener listener) {
		workbenchWindow.getSelectionService().addPostSelectionListener(partId, listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#getSelection()
	 */
	public ISelection getSelection() {
		return workbenchWindow.getSelectionService().getSelection();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#getSelection(java.lang.String)
	 */
	public ISelection getSelection(String partId) {
		return workbenchWindow.getSelectionService().getSelection(partId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#removeSelectionListener(org.eclipse.ui.ISelectionListener)
	 */
	public void removeSelectionListener(ISelectionListener listener) {
		workbenchWindow.getSelectionService().removeSelectionListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#removeSelectionListener(java.lang.String, org.eclipse.ui.ISelectionListener)
	 */
	public void removeSelectionListener(String partId, ISelectionListener listener) {
		workbenchWindow.getSelectionService().removeSelectionListener(partId, listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#removePostSelectionListener(org.eclipse.ui.ISelectionListener)
	 */
	public void removePostSelectionListener(ISelectionListener listener) {
		workbenchWindow.getSelectionService().removePostSelectionListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#removePostSelectionListener(java.lang.String, org.eclipse.ui.ISelectionListener)
	 */
	public void removePostSelectionListener(String partId, ISelectionListener listener) {
		workbenchWindow.getSelectionService().removePostSelectionListener(partId, listener);
	}

	ArrayList<MPart> activationList = new ArrayList<MPart>();

	private void updateActivations(MPart part) {
		activationList.remove(part);
		activationList.add(part);
	}

	private void updateBroughtToTop(MPart part) {
		MElementContainer<?> parent = part.getParent();
		if (parent instanceof MPartStack) {
			int newIndex = lastIndexOfContainer(parent);
			// New index can be -1 if there is no last index
			if (newIndex >= 0 && part == activationList.get(newIndex)) {
				return;
			}
			activationList.remove(part);
			if (newIndex >= 0 && newIndex < activationList.size() - 1) {
				activationList.add(newIndex + 1, part);
			} else {
				activationList.add(part);
			}
		}
	}

	private int lastIndexOfContainer(MElementContainer<?> parent) {
		for (int i = activationList.size() - 1; i >= 0; i--) {
			MPart mPart = activationList.get(i);
			if (mPart.getParent() == parent) {
				return i;
			}
		}
		return -1;
	}

	class E4PartListener implements org.eclipse.e4.workbench.modeling.IPartListener {

		public void partActivated(MPart part) {
			updateActivations(part);
			firePartActivated(part);
		}

		public void partBroughtToTop(MPart part) {
			updateBroughtToTop(part);
			firePartBroughtToTop(part);
		}

	}

}
