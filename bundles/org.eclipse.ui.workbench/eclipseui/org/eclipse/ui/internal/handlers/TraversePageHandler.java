/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.handlers;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
/**
 * This handler is an adaptation of the widget method handler that implements
 * page traversal via {@link SWT#TRAVERSE_PAGE_NEXT} and
 * {@link SWT#TRAVERSE_PAGE_PREVIOUS} events.
 *
 * @since 3.5
 */
public class TraversePageHandler extends WidgetMethodHandler {
	/**
	 * The parameters for traverse(int).
	 */
	private static final Class<?>[] METHOD_PARAMETERS = { int.class };
	@Override
	public final Object execute(final ExecutionEvent event) {
		Control focusControl = Display.getCurrent().getFocusControl();
		if (focusControl != null) {
			boolean forward = "next".equals(methodName); //$NON-NLS-1$
			int traversalDirection = translateToTraversalDirection(forward);
			Control control = focusControl;
			do {
				if (control instanceof CTabFolder folder && isFinalItemInCTabFolder(folder, forward)
						&& !hasHiddenItem(folder)) {
					loopToFirstOrLastItem(folder, forward);
					traversalDirection = translateToTraversalDirection(!forward); // we are in the second-to-last item in the given
					// direction. Now, use the Traverse-event to move back by one
				}
				if (control.traverse(traversalDirection))
					return null;
				if (control instanceof Shell)
					return null;
				control = control.getParent();
			} while (control != null);
		}
		return null;
	}

	private boolean hasHiddenItem(CTabFolder folder) {
		return Arrays.stream(folder.getItems()).anyMatch(i -> !i.isShowing());
	}

	private int translateToTraversalDirection(boolean forward) {
		return forward ? SWT.TRAVERSE_PAGE_NEXT : SWT.TRAVERSE_PAGE_PREVIOUS;
	}
	
	/**
	 * Sets the current selection to the first or last item the given direction.
	 *
	 * @param folder  the CTabFolder which we want to inspect
	 * @param forward whether we want to traverse forwards of backwards
	 */
	private void loopToFirstOrLastItem(CTabFolder folder, boolean forward) {
		if (forward) {
			folder.showItem(folder.getItem(0));
			folder.setSelection(1);
		} else {
			int itemCount = folder.getItemCount();
			folder.setSelection(itemCount - 2);
		}
	}

	/**
	 * {@return Returns whether the folder has currently selected the final item in
	 * the given direction.}
	 *
	 * @param folder  the CTabFolder which we want to inspect
	 * @param forward whether we want to traverse forwards of backwards
	 */
	private boolean isFinalItemInCTabFolder(CTabFolder folder, boolean forward) {
		CTabItem currentFolder = folder.getSelection();
		CTabItem lastFolder = null;
		if (forward) {
			int itemCount = folder.getItemCount();
			lastFolder = folder.getItem(itemCount - 1);
		} else {
			lastFolder = folder.getItem(0);
		}
		return currentFolder.equals(lastFolder);
	}

	/**
	 * Looks up the traverse(int) method on the given focus control.
	 *
	 * @return The method on the focus control; <code>null</code> if none.
	 */
	@Override
	protected Method getMethodToExecute() {
		final Control focusControl = Display.getCurrent().getFocusControl();
		if (focusControl != null) {
			try {
				return focusControl.getClass().getMethod("traverse", //$NON-NLS-1$
						METHOD_PARAMETERS);
			} catch (NoSuchMethodException e) {
				// Do nothing.
			}
		}
		return null;
	}
}