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

import javax.inject.Inject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.workbench.modeling.EPartService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.INavigationHistory;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.MultiPartInitException;
import org.eclipse.ui.PartInitException;

/**
 * @since 3.5
 *
 */
public class WorkbenchPage implements IWorkbenchPage {

	WorkbenchWindow workbenchWindow;
	private IAdaptable input;

	@Inject
	private EPartService partService;

	/**
	 * @param workbenchWindow
	 * @param input
	 */
	public WorkbenchPage(WorkbenchWindow workbenchWindow, IAdaptable input) {
		this.workbenchWindow = workbenchWindow;
		this.input = input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#activate(org.eclipse.ui.IWorkbenchPart)
	 */
	public void activate(IWorkbenchPart part) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#bringToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void bringToTop(IWorkbenchPart part) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#close()
	 */
	public boolean close() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#closeAllEditors(boolean)
	 */
	public boolean closeAllEditors(boolean save) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#closeEditors(org.eclipse.ui.IEditorReference[], boolean)
	 */
	public boolean closeEditors(IEditorReference[] editorRefs, boolean save) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#closeEditor(org.eclipse.ui.IEditorPart, boolean)
	 */
	public boolean closeEditor(IEditorPart editor, boolean save) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#findView(java.lang.String)
	 */
	public IViewPart findView(String viewId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#findViewReference(java.lang.String)
	 */
	public IViewReference findViewReference(String viewId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#findViewReference(java.lang.String, java.lang.String)
	 */
	public IViewReference findViewReference(String viewId, String secondaryId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getActiveEditor()
	 */
	public IEditorPart getActiveEditor() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#findEditor(org.eclipse.ui.IEditorInput)
	 */
	public IEditorPart findEditor(IEditorInput input) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#findEditors(org.eclipse.ui.IEditorInput, java.lang.String, int)
	 */
	public IEditorReference[] findEditors(IEditorInput input, String editorId, int matchFlags) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getEditors()
	 */
	public IEditorPart[] getEditors() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getEditorReferences()
	 */
	public IEditorReference[] getEditorReferences() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getDirtyEditors()
	 */
	public IEditorPart[] getDirtyEditors() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getPerspective()
	 */
	public IPerspectiveDescriptor getPerspective() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getViewReferences()
	 */
	public IViewReference[] getViewReferences() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getViews()
	 */
	public IViewPart[] getViews() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#hideActionSet(java.lang.String)
	 */
	public void hideActionSet(String actionSetID) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#hideView(org.eclipse.ui.IViewPart)
	 */
	public void hideView(IViewPart view) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#hideView(org.eclipse.ui.IViewReference)
	 */
	public void hideView(IViewReference view) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#isPartVisible(org.eclipse.ui.IWorkbenchPart)
	 */
	public boolean isPartVisible(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#isEditorAreaVisible()
	 */
	public boolean isEditorAreaVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#reuseEditor(org.eclipse.ui.IReusableEditor, org.eclipse.ui.IEditorInput)
	 */
	public void reuseEditor(IReusableEditor editor, IEditorInput input) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(org.eclipse.ui.IEditorInput, java.lang.String)
	 */
	public IEditorPart openEditor(IEditorInput input, String editorId) throws PartInitException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(org.eclipse.ui.IEditorInput, java.lang.String, boolean)
	 */
	public IEditorPart openEditor(IEditorInput input, String editorId, boolean activate)
			throws PartInitException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#openEditor(org.eclipse.ui.IEditorInput, java.lang.String, boolean, int)
	 */
	public IEditorPart openEditor(IEditorInput input, String editorId, boolean activate,
			int matchFlags) throws PartInitException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#resetPerspective()
	 */
	public void resetPerspective() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#saveAllEditors(boolean)
	 */
	public boolean saveAllEditors(boolean confirm) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#saveEditor(org.eclipse.ui.IEditorPart, boolean)
	 */
	public boolean saveEditor(IEditorPart editor, boolean confirm) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#savePerspective()
	 */
	public void savePerspective() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#savePerspectiveAs(org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void savePerspectiveAs(IPerspectiveDescriptor perspective) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#setEditorAreaVisible(boolean)
	 */
	public void setEditorAreaVisible(boolean showEditorArea) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#setPerspective(org.eclipse.ui.IPerspectiveDescriptor)
	 */
	public void setPerspective(IPerspectiveDescriptor perspective) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#showActionSet(java.lang.String)
	 */
	public void showActionSet(String actionSetID) {
		// TODO Auto-generated method stub

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
		MPart part = partService.findPart(viewId);
		if (part == null) {
			part = partService.showPart(viewId);

			CompatibilityView compatibilityView = (CompatibilityView) part.getObject();
			compatibilityView.create();
			return compatibilityView.getView();
		}

		CompatibilityView compatibilityView = (CompatibilityView) part.getObject();
		compatibilityView.delegateSetFocus();
		return compatibilityView.getView();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#isEditorPinned(org.eclipse.ui.IEditorPart)
	 */
	public boolean isEditorPinned(IEditorPart editor) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getEditorReuseThreshold()
	 */
	public int getEditorReuseThreshold() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#setEditorReuseThreshold(int)
	 */
	public void setEditorReuseThreshold(int openEditors) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getNavigationHistory()
	 */
	public INavigationHistory getNavigationHistory() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getViewStack(org.eclipse.ui.IViewPart)
	 */
	public IViewPart[] getViewStack(IViewPart part) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getNewWizardShortcuts()
	 */
	public String[] getNewWizardShortcuts() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getPerspectiveShortcuts()
	 */
	public String[] getPerspectiveShortcuts() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getShowViewShortcuts()
	 */
	public String[] getShowViewShortcuts() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getOpenPerspectives()
	 */
	public IPerspectiveDescriptor[] getOpenPerspectives() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getSortedPerspectives()
	 */
	public IPerspectiveDescriptor[] getSortedPerspectives() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#closePerspective(org.eclipse.ui.IPerspectiveDescriptor, boolean, boolean)
	 */
	public void closePerspective(IPerspectiveDescriptor desc, boolean saveParts, boolean closePage) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#closeAllPerspectives(boolean, boolean)
	 */
	public void closeAllPerspectives(boolean saveEditors, boolean closePage) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getExtensionTracker()
	 */
	public IExtensionTracker getExtensionTracker() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getWorkingSets()
	 */
	public IWorkingSet[] getWorkingSets() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#setWorkingSets(org.eclipse.ui.IWorkingSet[])
	 */
	public void setWorkingSets(IWorkingSet[] sets) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getAggregateWorkingSet()
	 */
	public IWorkingSet getAggregateWorkingSet() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#isPageZoomed()
	 */
	public boolean isPageZoomed() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#zoomOut()
	 */
	public void zoomOut() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#toggleZoom(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void toggleZoom(IWorkbenchPartReference ref) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getPartState(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public int getPartState(IWorkbenchPartReference ref) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#setPartState(org.eclipse.ui.IWorkbenchPartReference, int)
	 */
	public void setPartState(IWorkbenchPartReference ref, int state) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#getReference(org.eclipse.ui.IWorkbenchPart)
	 */
	public IWorkbenchPartReference getReference(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#showEditor(org.eclipse.ui.IEditorReference)
	 */
	public void showEditor(IEditorReference ref) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#hideEditor(org.eclipse.ui.IEditorReference)
	 */
	public void hideEditor(IEditorReference ref) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPage#openEditors(org.eclipse.ui.IEditorInput[], java.lang.String[], int)
	 */
	public IEditorReference[] openEditors(IEditorInput[] inputs, String[] editorIDs, int matchFlags)
			throws MultiPartInitException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartService#addPartListener(org.eclipse.ui.IPartListener)
	 */
	public void addPartListener(IPartListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartService#addPartListener(org.eclipse.ui.IPartListener2)
	 */
	public void addPartListener(IPartListener2 listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartService#getActivePart()
	 */
	public IWorkbenchPart getActivePart() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartService#getActivePartReference()
	 */
	public IWorkbenchPartReference getActivePartReference() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartService#removePartListener(org.eclipse.ui.IPartListener)
	 */
	public void removePartListener(IPartListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartService#removePartListener(org.eclipse.ui.IPartListener2)
	 */
	public void removePartListener(IPartListener2 listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#addSelectionListener(org.eclipse.ui.ISelectionListener)
	 */
	public void addSelectionListener(ISelectionListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#addSelectionListener(java.lang.String, org.eclipse.ui.ISelectionListener)
	 */
	public void addSelectionListener(String partId, ISelectionListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#addPostSelectionListener(org.eclipse.ui.ISelectionListener)
	 */
	public void addPostSelectionListener(ISelectionListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#addPostSelectionListener(java.lang.String, org.eclipse.ui.ISelectionListener)
	 */
	public void addPostSelectionListener(String partId, ISelectionListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#getSelection()
	 */
	public ISelection getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#getSelection(java.lang.String)
	 */
	public ISelection getSelection(String partId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#removeSelectionListener(org.eclipse.ui.ISelectionListener)
	 */
	public void removeSelectionListener(ISelectionListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#removeSelectionListener(java.lang.String, org.eclipse.ui.ISelectionListener)
	 */
	public void removeSelectionListener(String partId, ISelectionListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#removePostSelectionListener(org.eclipse.ui.ISelectionListener)
	 */
	public void removePostSelectionListener(ISelectionListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionService#removePostSelectionListener(java.lang.String, org.eclipse.ui.ISelectionListener)
	 */
	public void removePostSelectionListener(String partId, ISelectionListener listener) {
		// TODO Auto-generated method stub

	}

}
