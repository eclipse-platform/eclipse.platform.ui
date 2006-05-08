/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablesLifecycleListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.MultiEditor;

public abstract class PartList {
	private IWorkbenchPartReference activePartReference;

	private IEditorReference activeEditorReference;

	// private List parts = new ArrayList();

	private IPropertyListener listener = new IPropertyListener() {
		public void propertyChanged(Object source, int propId) {
			WorkbenchPartReference ref = (WorkbenchPartReference) source;

			switch (propId) {
			case WorkbenchPartReference.INTERNAL_PROPERTY_OPENED:
				partOpened(ref);
				break;
			case WorkbenchPartReference.INTERNAL_PROPERTY_CLOSED:
				partClosed(ref);
				break;
			case WorkbenchPartReference.INTERNAL_PROPERTY_VISIBLE: {
				if (ref.getVisible()) {
					partVisible(ref);
				} else {
					partHidden(ref);
				}
				break;
			}
			case IWorkbenchPartConstants.PROP_INPUT: {
				partInputChanged(ref);
				break;
			}
			}
		}
	};

	public IWorkbenchPartReference getActivePartReference() {
		return activePartReference;
	}

	public IEditorReference getActiveEditorReference() {
		return activeEditorReference;
	}

	public IEditorPart getActiveEditor() {
		return activeEditorReference == null ? null : activeEditorReference
				.getEditor(false);
	}

	public IWorkbenchPart getActivePart() {
		return activePartReference == null ? null : activePartReference
				.getPart(false);
	}

	public void addPart(WorkbenchPartReference ref) {
		Assert.isNotNull(ref);

		ref.addInternalPropertyListener(listener);

		// parts.add(ref);
		firePartAdded(ref);

		// If this part is already open, fire the "part opened" event
		// immediately
		if (ref.getPart(false) != null) {
			partOpened(ref);
		}

		// If this part is already visible, fire the visibility event
		// immediately
		if (ref.getVisible()) {
			partVisible(ref);
		}
	}

	/**
	 * Sets the active part.
	 * 
	 * @param ref
	 */
	public void setActivePart(IWorkbenchPartReference ref) {
		if (ref == activePartReference) {
			return;
		}

		IWorkbenchPartReference oldPart = activePartReference;

		// A part can't be activated until it is added
		// Assert.isTrue(ref == null || parts.contains(ref));

		if (ref != null) {
			IWorkbenchPart part = ref.getPart(true);
			Assert.isNotNull(part);
			if (part instanceof MultiEditor) {
				IWorkbenchPartSite site = ((MultiEditor) part)
						.getActiveEditor().getSite();
				if (site instanceof PartSite) {
					ref = ((PartSite) site).getPane().getPartReference();
				}
			}
		}

		activePartReference = ref;

		fireActivePartChanged(oldPart, ref);
	}

	public void setActiveEditor(IEditorReference ref) {
		if (ref == activeEditorReference) {
			return;
		}

		// A part can't be activated until it is added
		// Assert.isTrue(ref == null || parts.contains(ref));

		if (ref != null) {
			IWorkbenchPart part = ref.getPart(true);
			Assert.isNotNull(part);
			if (part instanceof MultiEditor) {
				IWorkbenchPartSite site = ((MultiEditor) part)
						.getActiveEditor().getSite();
				if (site instanceof PartSite) {
					ref = (IEditorReference) ((PartSite) site).getPane()
							.getPartReference();
				}
			}
		}

		activeEditorReference = ref;

		fireActiveEditorChanged(ref);
	}

	/**
	 * In order to remove a part, it must first be deactivated.
	 */
	public void removePart(WorkbenchPartReference ref) {
		Assert.isNotNull(ref);
		// It is an error to remove a part that isn't in the list
		// Assert.isTrue(parts.contains(ref));
		// We're not allowed to remove the active part. We must deactivate it
		// first.
		Assert.isTrue(ref != activePartReference);
		// We're not allowed to remove the active editor. We must deactivate it
		// first.
		Assert.isTrue(ref != activeEditorReference);

		if (ref.getVisible()) {
			ref.setVisible(false);
		}

		// If this part is currently open, fire the "part closed" event before
		// removal
		if (ref.getPart(false) != null) {
			partClosed(ref);
		}

		ref.removeInternalPropertyListener(listener);

		firePartRemoved(ref);
	}

	private void partInputChanged(WorkbenchPartReference ref) {
		firePartInputChanged(ref);
	}

	private void partHidden(WorkbenchPartReference ref) {
		// Part should not be null
		Assert.isNotNull(ref);
		// This event should only be fired if the part is actually visible
		Assert.isTrue(!ref.getVisible());
		// We shouldn't be receiving events from parts until they are in the
		// list
		// Assert.isTrue(parts.contains(ref));

		firePartHidden(ref);
	}

	private void partOpened(WorkbenchPartReference ref) {
		Assert.isNotNull(ref);

		IWorkbenchPart actualPart = ref.getPart(false);
		// We're firing the event that says "the part was just created"... so
		// there better be a part there.
		Assert.isNotNull(actualPart);
		// Must be called after the part is inserted into the part list
		// Assert.isTrue(parts.contains(ref));
		// The active part must be opened before it is activated, so we should
		// never get an
		// open event for a part that is already active. (This either indicates
		// that a redundant
		// open event was fired or that a closed part was somehow activated)
		Assert.isTrue(activePartReference != ref);
		// The active editor must be opened before it is activated, so we should
		// never get an
		// open event for an editor that is already active. (This either
		// indicates that a redundant
		// open event was fired or that a closed editor was somehow activated)
		Assert.isTrue(activeEditorReference != ref);

		SaveablesList modelManager = (SaveablesList) actualPart
				.getSite().getService(ISaveablesLifecycleListener.class);
		modelManager.postOpen(actualPart);

		// Fire the "part opened" event
		firePartOpened(ref);
	}

	/**
	 * Called when a concrete part is about to be destroyed. This is called
	 * BEFORE disposal happens, so the part should still be accessable from the
	 * part reference.
	 * 
	 * @param ref
	 */
	private void partClosed(WorkbenchPartReference ref) {
		Assert.isNotNull(ref);

		IWorkbenchPart actualPart = ref.getPart(false);
		// Called before the part is disposed, so the part should still be
		// there.
		Assert.isNotNull(actualPart);
		// Must be called before the part is actually removed from the part list
		// Assert.isTrue(parts.contains(ref));
		// Not allowed to close the active part. The part must be deactivated
		// before it may
		// be closed.
		Assert.isTrue(activePartReference != ref);
		// Not allowed to close the active editor. The editor must be
		// deactivated before it may
		// be closed.
		Assert.isTrue(activeEditorReference != ref);

		firePartClosed(ref);
	}

	private void partVisible(WorkbenchPartReference ref) {
		// Part should not be null
		Assert.isNotNull(ref);
		// This event should only be fired if the part is actually visible
		Assert.isTrue(ref.getVisible());
		// We shouldn't be receiving events from parts until they are in the
		// list
		// Assert.isTrue(parts.contains(ref));
		// Part must be open before it can be made visible
		Assert.isNotNull(ref.getPart(false));

		firePartVisible(ref);
	}

	/**
	 * Fire the event indicating that a part reference was just realized. That
	 * is, the concrete IWorkbenchPart has been attached to the part reference.
	 * 
	 * @param part
	 *            the reference that was create
	 */
	protected abstract void firePartOpened(IWorkbenchPartReference part);

	/**
	 * Fire the event indicating that a part reference was just realized. That
	 * is, the concrete IWorkbenchPart has been attached to the part reference.
	 * 
	 * @param part
	 *            the reference that was create
	 */
	protected abstract void firePartClosed(IWorkbenchPartReference part);

	/**
	 * Indicates that a new part reference was added to the list.
	 * 
	 * @param part
	 */
	protected abstract void firePartAdded(IWorkbenchPartReference part);

	/**
	 * Indicates that a part reference was removed from the list
	 * 
	 * @param part
	 */
	protected abstract void firePartRemoved(IWorkbenchPartReference part);

	/**
	 * Indicates that the active editor changed
	 * 
	 * @param part
	 *            active part reference or null if none
	 */
	protected abstract void fireActiveEditorChanged(IWorkbenchPartReference ref);

	/**
	 * Indicates that the active part has changed
	 * 
	 * @param part
	 *            active part reference or null if none
	 */
	protected abstract void fireActivePartChanged(
			IWorkbenchPartReference oldPart, IWorkbenchPartReference newPart);

	/**
	 * Indicates that the part has been made visible
	 * 
	 * @param ref
	 */
	protected abstract void firePartVisible(IWorkbenchPartReference ref);

	/**
	 * Indicates that the part has been hidden
	 * 
	 * @param ref
	 */
	protected abstract void firePartHidden(IWorkbenchPartReference ref);

	/**
	 * Indicates that the part input has changed
	 * 
	 * @param ref
	 */
	protected abstract void firePartInputChanged(IWorkbenchPartReference ref);

	protected abstract void firePartBroughtToTop(IWorkbenchPartReference ref);
}
