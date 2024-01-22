/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Nikolay Botev - bug 240651
 *******************************************************************************/

package org.eclipse.ui.part;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.e4.compatibility.E4Util;

/**
 * A AbstractMultiEditor is a composite of editors.
 *
 * This class is intended to be subclassed.
 *
 * @since 3.5
 */
public abstract class AbstractMultiEditor extends EditorPart {

	private int activeEditorIndex;

	private IEditorPart innerEditors[];

	private IPartListener2 propagationListener;

	/**
	 * Constructs an editor to contain other editors.
	 */
	public AbstractMultiEditor() {
		super();
	}

	/**
	 * Handles a property change notification from a nested editor. The default
	 * implementation simply forwards the change to listeners on this multi editor
	 * by calling <code>firePropertyChange</code> with the same property id. For
	 * example, if the dirty state of a nested editor changes (property id
	 * <code>ISaveablePart.PROP_DIRTY</code>), this method handles it by firing a
	 * property change event for <code>ISaveablePart.PROP_DIRTY</code> to property
	 * listeners on this multi editor.
	 * <p>
	 * Subclasses may extend or reimplement this method.
	 * </p>
	 *
	 * @param propId the id of the property that changed
	 * @since 3.6
	 */
	protected void handlePropertyChange(int propId) {
		firePropertyChange(propId);
	}

	/*
	 * @see IEditorPart#doSave(IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		for (IEditorPart e : innerEditors) {
			e.doSave(monitor);
		}
	}

	/*
	 * @see IEditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		// no-op
	}

	/*
	 * @see IEditorPart#init(IEditorSite, IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		init(site, (MultiEditorInput) input);
	}

	/**
	 * @param site  the editor site
	 * @param input the editor input
	 * @exception PartInitException if this editor was not initialized successfully
	 *
	 * @see IEditorPart#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, MultiEditorInput input) throws PartInitException {
		setInput(input);
		setSite(site);
		setPartName(input.getName());
		setTitleToolTip(input.getToolTipText());
		setupEvents();
	}

	/*
	 * @see IEditorPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		for (IEditorPart e : innerEditors) {
			if (e.isDirty()) {
				return true;
			}
		}
		return false;
	}

	/*
	 * @see IEditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/*
	 * @see IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		innerEditors[activeEditorIndex].setFocus();
	}

	/**
	 * Returns the active inner editor.
	 *
	 * @return the active editor
	 */
	public final IEditorPart getActiveEditor() {
		return innerEditors[activeEditorIndex];
	}

	/**
	 * Returns an array with all inner editors.
	 *
	 * @return the inner editors
	 */
	public final IEditorPart[] getInnerEditors() {
		return innerEditors;
	}

	/**
	 * Set the inner editors.
	 *
	 * Should not be called by clients.
	 *
	 * @param children the inner editors of this multi editor
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public final void setChildren(IEditorPart[] children) {
		innerEditors = children;
		activeEditorIndex = 0;

		for (IEditorPart child : children) {
			child.addPropertyListener((source, propId) -> handlePropertyChange(propId));
		}

		innerEditorsCreated();
	}

	/**
	 * Called as soon as the inner editors have been created and are available.
	 */
	protected abstract void innerEditorsCreated();

	/**
	 * Activates the given nested editor.
	 *
	 * @param part the nested editor
	 * @since 3.0
	 */
	public void activateEditor(IEditorPart part) {
		activeEditorIndex = getIndex(part);
		// IEditorPart e = getActiveEditor();
		// EditorSite innerSite = (EditorSite) e.getEditorSite();
		// ((WorkbenchPage) innerSite.getPage()).requestActivation(e);
		E4Util.unsupported("We need to request an activation of this part"); //$NON-NLS-1$
	}

	/**
	 * Returns the index of the given nested editor.
	 *
	 * @return the index of the nested editor
	 * @since 3.0
	 */
	protected int getIndex(IEditorPart editor) {
		for (int i = 0; i < innerEditors.length; i++) {
			if (innerEditors[i] == editor) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Set up the AbstractMultiEditor to propagate events like partClosed().
	 *
	 * @since 3.2
	 */
	private void setupEvents() {
		propagationListener = new IPartListener2() {
			@Override
			public void partActivated(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partBroughtToTop(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partClosed(IWorkbenchPartReference partRef) {
				IWorkbenchPart part = partRef.getPart(false);
				if (part == AbstractMultiEditor.this && innerEditors != null) {
					// propagate the events
					E4Util.unsupported("propogate the events needed"); //$NON-NLS-1$
				}
			}

			@Override
			public void partDeactivated(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partOpened(IWorkbenchPartReference partRef) {
				IWorkbenchPart part = partRef.getPart(false);
				if (part == AbstractMultiEditor.this && innerEditors != null) {
					// PartService partService = ((WorkbenchPage) getSite()
					// .getPage()).getPartService();
					// for (int i = 0; i < innerEditors.length; i++) {
					// IEditorPart editor = innerEditors[i];
					// IWorkbenchPartReference innerRef = ((PartSite) editor
					// .getSite()).getPartReference();
					// partService.firePartOpened(innerRef);
					// }
					// propagate the events
					E4Util.unsupported("propogate the events needed"); //$NON-NLS-1$
				}
			}

			@Override
			public void partHidden(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partVisible(IWorkbenchPartReference partRef) {
			}

			@Override
			public void partInputChanged(IWorkbenchPartReference partRef) {
			}
		};
		getSite().getPage().addPartListener(propagationListener);
	}

	/**
	 * Release the added listener.
	 *
	 * @since 3.2
	 */
	@Override
	public void dispose() {
		getSite().getPage().removePartListener(propagationListener);
		super.dispose();
	}

	/**
	 * This method is called after createPartControl has been executed and should
	 * return the container for the given inner editor.
	 *
	 * @param innerEditorReference a reference to the inner editor that is being
	 *                             created.
	 * @return the container in which the inner editor's pane and part controls are
	 *         to be created.
	 */
	public abstract Composite getInnerEditorContainer(IEditorReference innerEditorReference);

}
