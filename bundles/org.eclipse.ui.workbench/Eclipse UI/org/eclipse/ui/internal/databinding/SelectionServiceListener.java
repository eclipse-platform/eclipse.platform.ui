package org.eclipse.ui.internal.databinding;

import org.eclipse.core.databinding.observable.IDiff;
import org.eclipse.core.databinding.property.IProperty;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.NativePropertyListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;

class SelectionServiceListener<S extends ISelectionService, D extends IDiff>
		extends NativePropertyListener<S, D>
		implements ISelectionListener {
	private final String partId;
	private final boolean post;

	SelectionServiceListener(IProperty property, ISimplePropertyListener<S, D> wrapped, String partID,
			boolean post) {
		super(property, wrapped);
		this.partId = partID;
		this.post = post;
	}

	@Override
	protected void doAddTo(S source) {
		ISelectionService selectionService = source;
		if (post) {
			if (partId != null) {
				selectionService.addPostSelectionListener(partId, this);
			} else {
				selectionService.addPostSelectionListener(this);
			}
		} else if (partId != null) {
			selectionService.addSelectionListener(partId, this);
		} else {
			selectionService.addSelectionListener(this);
		}
	}

	@Override
	protected void doRemoveFrom(S source) {
		ISelectionService selectionService = source;
		if (post) {
			if (partId != null) {
				selectionService.removePostSelectionListener(partId, this);
			} else {
				selectionService.removePostSelectionListener(this);
			}
		} else if (partId != null) {
			selectionService.removeSelectionListener(partId, this);
		} else {
			selectionService.removeSelectionListener(this);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		fireChange((S) part, null);
	}
}
