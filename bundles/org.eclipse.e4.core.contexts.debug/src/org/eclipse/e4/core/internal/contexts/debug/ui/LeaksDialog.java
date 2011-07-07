/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.core.internal.contexts.debug.ui;

import java.util.Map;
import java.util.Set;
import org.eclipse.e4.core.internal.contexts.Computation;
import org.eclipse.e4.core.internal.contexts.EclipseContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

public class LeaksDialog extends ElementTreeSelectionDialog {

	private static class ListenerContentProvider implements ITreeContentProvider {

		private Map<EclipseContext, Set<Computation>> snapshotDiff;

		public ListenerContentProvider() {
			// placeholder
		}

		public void dispose() {
			if (snapshotDiff == null)
				return;
			snapshotDiff.clear();
			snapshotDiff = null;
		}

		@SuppressWarnings("unchecked")
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			snapshotDiff = (Map<EclipseContext, Set<Computation>>) newInput;
		}

		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			return ((Map<EclipseContext, Set<Computation>>) inputElement).keySet().toArray();
		}

		public Object[] getChildren(Object parentElement) {
			if (snapshotDiff == null)
				return null;
			if (parentElement instanceof EclipseContext) {
				return snapshotDiff.get(parentElement).toArray();
			}
			return null;
		}

		public Object getParent(Object element) {
			if (snapshotDiff == null)
				return null;
			if (element instanceof Computation) {
				for (EclipseContext context : snapshotDiff.keySet()) {
					Set<Computation> computations = snapshotDiff.get(context);
					if (computations.contains(element))
						return context;
				}
			}
			return null;
		}

		public boolean hasChildren(Object element) {
			if (snapshotDiff == null)
				return false;
			if (element instanceof EclipseContext) {
				if (!snapshotDiff.containsKey(element))
					return false;
				if (snapshotDiff.get(element).isEmpty())
					return false;
				return true;
			}
			return false;
		}
	}

	public LeaksDialog(Shell parent) {
		super(parent, new LabelProvider(), new ListenerContentProvider());
		setComparator(new ViewerComparator());
		setTitle(ContextMessages.diffDialogTitle);
		setMessage(ContextMessages.diffDialogMessage);
	}

	// Only need OK button
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
}
