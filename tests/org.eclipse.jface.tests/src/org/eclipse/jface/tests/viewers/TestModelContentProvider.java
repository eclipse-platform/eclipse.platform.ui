/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class TestModelContentProvider implements ITestModelListener, ITreeContentProvider {
	Viewer fViewer;

	@Override
	public void dispose() {
	}

	protected void doInsert(TestModelChange change) {
		if (fViewer instanceof ListViewer viewer) {
			if (change.getParent() != null && change.getParent().equals(fViewer.getInput())) {
				viewer.add((Object[]) change.getChildren());
			}
		} else if (fViewer instanceof TableViewer viewer) {
			if (change.getParent() != null && change.getParent().equals(fViewer.getInput())) {
				viewer.add(change.getChildren());
			}
		} else if (fViewer instanceof AbstractTreeViewer viewer) {
			viewer.add(change.getParent(), (Object[]) change.getChildren());
		} else if (fViewer instanceof ComboViewer viewer) {
			viewer.add((Object[]) change.getChildren());
		} else {
			Assert.isTrue(false, "Unknown kind of viewer");
		}
	}

	protected void doNonStructureChange(TestModelChange change) {
		if (fViewer instanceof StructuredViewer viewer) {
			viewer.update(change.getParent(), new String[] { IBasicPropertyConstants.P_TEXT });
		} else {
			Assert.isTrue(false, "Unknown kind of viewer");
		}
	}

	protected void doRemove(TestModelChange change) {
		if (fViewer instanceof ListViewer viewer) {
			viewer.remove((Object[]) change.getChildren());
		} else if (fViewer instanceof TableViewer viewer) {
			viewer.remove(change.getChildren());
		} else if (fViewer instanceof AbstractTreeViewer viewer) {
			viewer.remove((Object[]) change.getChildren());
		} else if (fViewer instanceof ComboViewer viewer) {
			viewer.remove((Object[]) change.getChildren());
		} else {
			Assert.isTrue(false, "Unknown kind of viewer");
		}
	}

	protected void doStructureChange(TestModelChange change) {
		if (fViewer instanceof StructuredViewer viewer) {
			viewer.refresh(change.getParent());
		} else {
			Assert.isTrue(false, "Unknown kind of viewer");
		}
	}

	@Override
	public Object[] getChildren(Object element) {
		TestElement testElement = (TestElement) element;
		int count = testElement.getChildCount();
		TestElement[] children = new TestElement[count];
		for (int i = 0; i < count; ++i) {
			children[i] = testElement.getChildAt(i);
		}
		return children;
	}

	@Override
	public Object[] getElements(Object element) {
		return getChildren(element);
	}

	@Override
	public Object getParent(Object element) {
		return ((TestElement) element).getContainer();
	}

	@Override
	public boolean hasChildren(Object element) {
		return ((TestElement) element).getChildCount() > 0;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fViewer = viewer;
		TestElement oldElement = (TestElement) oldInput;
		if (oldElement != null) {
			oldElement.getModel().removeListener(this);
		}
		TestElement newElement = (TestElement) newInput;
		if (newElement != null) {
			newElement.getModel().addListener(this);
		}
	}

	public boolean isDeleted(Object element) {
		return ((TestElement) element).isDeleted();
	}

	@Override
	public void testModelChanged(TestModelChange change) {
		switch (change.getKind()) {
		case TestModelChange.INSERT:
			doInsert(change);
			break;
		case TestModelChange.REMOVE:
			doRemove(change);
			break;
		case TestModelChange.STRUCTURE_CHANGE:
			doStructureChange(change);
			break;
		case TestModelChange.NON_STRUCTURE_CHANGE:
			doNonStructureChange(change);
			break;
		default:
			throw new IllegalArgumentException("Unknown kind of change");
		}

		StructuredSelection selection = new StructuredSelection(change.getChildren());
		if ((change.getModifiers() & TestModelChange.SELECT) != 0) {
			fViewer.setSelection(selection);
		}
		if ((change.getModifiers() & TestModelChange.REVEAL) != 0) {
			Object element = selection.getFirstElement();
			if (element != null) {
				((StructuredViewer) fViewer).reveal(element);
			}
		}
	}
}
