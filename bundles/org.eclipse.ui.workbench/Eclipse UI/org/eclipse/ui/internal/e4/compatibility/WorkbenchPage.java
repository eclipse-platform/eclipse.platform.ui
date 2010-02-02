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

package org.eclipse.ui.internal.e4.compatibility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MEditor;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartDescriptor;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MPerspective;
import org.eclipse.e4.ui.model.application.MSaveablePart;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.workbench.modeling.EPartService;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
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

/**
 * @since 3.5
 *
 */
public class WorkbenchPage implements IWorkbenchPage {

	private WorkbenchWindow workbenchWindow;
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

	private List<IViewReference> viewReferences = new ArrayList<IViewReference>();
	private List<IEditorReference> editorReferences = new ArrayList<IEditorReference>();

	private ListenerList partListenerList = new ListenerList();
	private ListenerList partListener2List = new ListenerList();
	private ListenerList propertyChangeListeners = new ListenerList();

	private E4PartListener e4PartListener = new E4PartListener();

	/**
	 * @param workbenchWindow
	 * @param input
	 */
	public WorkbenchPage(WorkbenchWindow workbenchWindow, IAdaptable input) {
		this.workbenchWindow = workbenchWindow;
		this.input = input;
	}

	@PostConstruct
	void postConstruct() {
		partService.addPartListener(e4PartListener);
		window.getContext().set(IPartService.class.getName(), this);
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

	/**
	 * 
	 */
	private void processEventLoop() {
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

	private MPart findPart(IWorkbenchPart part) {
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
		return (IEditorPart) getActivePart();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#findEditor(org.eclipse.ui.IEditorInput)
	 */
	public IEditorPart findEditor(IEditorInput input) {
		for (IEditorReference editorRef : editorReferences) {
			IEditorPart editor = editorRef.getEditor(false);
			if (editor.getEditorInput().equals(input)) {
				return editor;
			}
		}
		return null;
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
				try {
					if (input.equals(editorRef.getEditorInput())) {
						editorRefs.add(editorRef);
					}
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
			if (editor.isDirty()) {
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
				compatibilityPart.delegateDispose();
				return true;
			}
		}

		for (Iterator<IEditorReference> it = editorReferences.iterator(); it.hasNext();) {
			IEditorReference reference = it.next();
			if (workbenchPart == reference.getPart(false)) {
				it.remove();
				partService.hidePart(part);
				compatibilityPart.delegateDispose();
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

		IEditorRegistry registry = workbenchWindow.getWorkbench().getEditorRegistry();
		EditorDescriptor descriptor = (EditorDescriptor) registry.findEditor(editorId);

		MPartDescriptor partDescriptor = getEditorDescriptor();
		MEditor editor = MApplicationFactory.eINSTANCE.createEditor();
		editor.setURI(partDescriptor.getURI());
		editor.setId(editorId);

		MUIElement element = findPrimaryDataStack(window);
		if (element instanceof MPartStack) {
			((MPartStack) element).getChildren().add(editor);
		} else {
			window.getChildren().add(editor);
		}

		CompatibilityEditor compatibilityEditor = (CompatibilityEditor) editor.getObject();
		compatibilityEditor.set(input, descriptor);

		editorReferences.add(new EditorReference(this, editor, input));

		if (activate) {
			partService.activate(editor);
		}

		return compatibilityEditor.getEditor();
	}

	private MPartStack findPrimaryDataStack(MElementContainer<?> container) {
		for (Object child : container.getChildren()) {
			if (((MApplicationElement) child).getId().equals("org.eclipse.e4.primaryDataStack")) { //$NON-NLS-1$
				return (MPartStack) child;
			} else if (child instanceof MElementContainer<?>) {
				MPartStack stack = findPrimaryDataStack((MElementContainer<?>) child);
				if (stack != null) {
					return stack;
				}
			}
		}
		return null;
	}

	private MPartDescriptor getEditorDescriptor() {
		for (MPartDescriptor descriptor : application.getDescriptors()) {
			if (descriptor
					.getURI()
					.equals(
							"platform:/plugin/org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor")) { //$NON-NLS-1$
				return descriptor;
			}
		}

		MPartDescriptor descriptor = MApplicationFactory.eINSTANCE.createPartDescriptor();
		descriptor
				.setURI("platform:/plugin/org.eclipse.ui.workbench/org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor"); //$NON-NLS-1$
		application.getDescriptors().add(descriptor);
		return descriptor;
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
								partService.savePart((MSaveablePart) part, confirm);
							}
						} else {
							partService.savePart((MSaveablePart) part, confirm);
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
		
		sortedPerspectives.remove(perspective);
		sortedPerspectives.add(0, perspective);

		if (!openedPerspectives.contains(perspective)) {
			openedPerspectives.add(perspective);
		}

		// TODO Auto-generated method stub
		MPerspective modelPerspective = MApplicationFactory.eINSTANCE.createPerspective();
		IPerspectiveFactory factory = ((PerspectiveDescriptor) perspective).createFactory();
		factory.createInitialLayout(new ModeledPageLayout(application, modelPerspective,
				perspective));
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
		}

		MPart part = partService.findPart(viewId);
		if (part == null) {
			switch (mode) {
			case VIEW_ACTIVATE:
				part = partService.showPart(viewId, EPartService.PartState.ACTIVATE);
				break;
			case VIEW_VISIBLE:
				part = partService.showPart(viewId, EPartService.PartState.VISIBLE);
				break;
			case VIEW_CREATE:
				part = partService.showPart(viewId, EPartService.PartState.CREATE);
				break;
			}

			CompatibilityView compatibilityView = (CompatibilityView) part.getObject();

			viewReferences.add(new ViewReference(this, part, compatibilityView.getDescriptor()));

			return compatibilityView.getView();
		}

		switch (mode) {
		case VIEW_ACTIVATE:
			part = partService.showPart(viewId, EPartService.PartState.ACTIVATE);
			CompatibilityView compatibilityView = (CompatibilityView) part.getObject();
			compatibilityView.delegateSetFocus();
			return compatibilityView.getView();
		case VIEW_VISIBLE:
			part = partService.showPart(viewId, EPartService.PartState.VISIBLE);
			compatibilityView = (CompatibilityView) part.getObject();
			return compatibilityView.getView();
		case VIEW_CREATE:
			part = partService.showPart(viewId, EPartService.PartState.CREATE);
			compatibilityView = (CompatibilityView) part.getObject();
			return compatibilityView.getView();
		default:
			throw new IllegalArgumentException(WorkbenchMessages.WorkbenchPage_IllegalViewMode);
		}
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
		// FIXME compat setEditorReuseThreshold, this is an empty implementation
		// in 3.x, see IPageLayout's setEditorReuseThreshold
		E4Util.unsupported("setEditorReuseThreshold"); //$NON-NLS-1$

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
				List<IViewPart> stack = new ArrayList<IViewPart>();

				for (Object child : parent.getChildren()) {
					MPart siblingPart = (MPart) child;
					Object siblingObject = siblingPart.getObject();
					if (siblingObject instanceof CompatibilityView) {
						IViewPart view = ((CompatibilityView) siblingObject).getView();
						stack.add(view);
					}
				}

				return stack.toArray(new IViewPart[stack.size()]);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getNewWizardShortcuts()
	 */
	public String[] getNewWizardShortcuts() {
		// FIXME compat getNewWizardShortcuts
		E4Util.unsupported("getNewWizardShortcuts"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getPerspectiveShortcuts()
	 */
	public String[] getPerspectiveShortcuts() {
		// FIXME compat getPerspectiveShortcuts
		E4Util.unsupported("getPerspectiveShortcuts"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getShowViewShortcuts()
	 */
	public String[] getShowViewShortcuts() {
		// FIXME compat getShowViewShortcuts
		E4Util.unsupported("getShowViewShortcuts"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getOpenPerspectives()
	 */
	public IPerspectiveDescriptor[] getOpenPerspectives() {
		return sortedPerspectives.toArray(new IPerspectiveDescriptor[sortedPerspectives.size()]);
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
		// FIXME compat addSelectionListener
		E4Util.unsupported("addSelectionListener"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#addSelectionListener(java.lang.String, org.eclipse.ui.ISelectionListener)
	 */
	public void addSelectionListener(String partId, ISelectionListener listener) {
		// FIXME compat addSelectionListener
		E4Util.unsupported("addSelectionListener(partId)"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#addPostSelectionListener(org.eclipse.ui.ISelectionListener)
	 */
	public void addPostSelectionListener(ISelectionListener listener) {
		// FIXME compat addPostSelectionListener
		E4Util.unsupported("addPostSelectionListener"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#addPostSelectionListener(java.lang.String, org.eclipse.ui.ISelectionListener)
	 */
	public void addPostSelectionListener(String partId, ISelectionListener listener) {
		// FIXME compat addPostSelectionListener
		E4Util.unsupported("addPostSelectionListener(partId)"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#getSelection()
	 */
	public ISelection getSelection() {
		// FIXME compat addPostSelectionListener
		E4Util.unsupported("getSelection"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#getSelection(java.lang.String)
	 */
	public ISelection getSelection(String partId) {
		// FIXME compat getSelection
		E4Util.unsupported("getSelection(partId)"); //$NON-NLS-1$
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#removeSelectionListener(org.eclipse.ui.ISelectionListener)
	 */
	public void removeSelectionListener(ISelectionListener listener) {
		// FIXME compat getSelection
		E4Util.unsupported("removeSelectionListener"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#removeSelectionListener(java.lang.String, org.eclipse.ui.ISelectionListener)
	 */
	public void removeSelectionListener(String partId, ISelectionListener listener) {
		// FIXME compat getSelection
		E4Util.unsupported("removeSelectionListener(partId)"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#removePostSelectionListener(org.eclipse.ui.ISelectionListener)
	 */
	public void removePostSelectionListener(ISelectionListener listener) {
		// FIXME compat getSelection
		E4Util.unsupported("removePostSelectionListener"); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#removePostSelectionListener(java.lang.String, org.eclipse.ui.ISelectionListener)
	 */
	public void removePostSelectionListener(String partId, ISelectionListener listener) {
		// FIXME compat getSelection
		E4Util.unsupported("removePostSelectionListener(partId)"); //$NON-NLS-1$

	}

	class E4PartListener implements org.eclipse.e4.workbench.modeling.IPartListener {

		public void partActivated(MPart part) {
			firePartActivated(part);
		}

		public void partBroughtToTop(MPart part) {
			firePartBroughtToTop(part);
		}

	}

}
