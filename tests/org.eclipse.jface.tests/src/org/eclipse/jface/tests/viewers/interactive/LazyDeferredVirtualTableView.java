/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433608
 *******************************************************************************/
package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.UIJob;

/**
 * The LazyVirtualTableView is the VirtualTableView with lazy content.
 */
public class LazyDeferredVirtualTableView extends VirtualTableView {

	/**
	 * Create a new instance of the receiver.
	 */
	public LazyDeferredVirtualTableView() {
		super();
	}

	@Override
	protected IContentProvider getContentProvider() {
		return new ILazyContentProvider() {

			int rangeStart = -1;

			int rangeEnd = -1;

			UIJob updateJob = UIJob.create("Update", (ICoreRunnable) m -> {
				if (viewer.getControl().isDisposed()) {
					throw new OperationCanceledException();
				}
				int rangeLength = rangeEnd - rangeStart;
				for (int i = 0; i <= rangeLength; i++) {
					int index = i + rangeStart;
					viewer.replace("Element " + index, index);
				}
			});

			@Override
			public void updateElement(int index) {

				int begin = Math.max(0, index - 50);
				int end = Math.min(begin + 50, 9999);

				// Initial case
				if (rangeStart == -1 || rangeEnd == -1) {
					rangeStart = begin;
					rangeEnd = end;
					updateJob.schedule(1000);
					return;
				}

				// Are we in the range already being worked on?
				if (index >= rangeStart && index <= rangeEnd) {
					return;
				}

				// Are we outside of the old range?
				if (begin > rangeEnd || end < rangeStart) {
					viewer.getTable().clear(rangeStart, rangeEnd);
					rangeStart = begin;
					rangeEnd = end;
					updateJob.schedule(1000);
					return;
				}

				// Shift if it is before
				if (begin < rangeStart) {
					rangeStart = begin;
					int oldEnd = rangeEnd;
					rangeEnd = end;
					viewer.getTable().clear(end + 1, oldEnd);

					updateJob.schedule(1000);
					return;
				}

				// Shift if it is after
				if (end > rangeEnd) {
					rangeEnd = end;
					int oldStart = rangeStart;
					rangeStart = begin;
					viewer.getTable().clear(oldStart, rangeStart - 1);
					updateJob.schedule(1000);
					return;
				}
			}

			@Override
			public void dispose() {
				// Do Nothing
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// Do nothing.
			}
		};
	}

	@Override
	protected void resetInput() {
		viewer.setItemCount(itemCount);
		super.resetInput();
	}
}
