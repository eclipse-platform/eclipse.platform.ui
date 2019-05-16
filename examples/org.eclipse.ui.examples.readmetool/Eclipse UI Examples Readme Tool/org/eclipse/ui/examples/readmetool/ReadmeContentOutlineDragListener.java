/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.examples.readmetool;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.ui.part.PluginTransfer;
import org.eclipse.ui.part.PluginTransferData;

/**
 * A drag listener for the readme editor's content outline page. Allows dragging
 * of content segments into views that support the <code>TextTransfer</code> or
 * <code>PluginTransfer</code> transfer types.
 */
public class ReadmeContentOutlineDragListener extends DragSourceAdapter {
	private ReadmeContentOutlinePage page;

	/**
	 * Creates a new drag listener for the given page.
	 */
	public ReadmeContentOutlineDragListener(ReadmeContentOutlinePage page) {
		this.page = page;
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		if (PluginTransfer.getInstance().isSupportedType(event.dataType)) {
			byte[] segmentData = getSegmentText().getBytes();
			event.data = new PluginTransferData(ReadmeDropActionDelegate.ID, segmentData);
			return;
		}
		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = getSegmentText();
			return;
		}
	}

	/**
	 * Returns the text of the currently selected readme segment.
	 */
	private String getSegmentText() {
		StringBuilder result = new StringBuilder();
		ISelection selection = page.getSelection();
		if (selection instanceof org.eclipse.jface.viewers.IStructuredSelection) {
			Object[] selected = ((IStructuredSelection) selection).toArray();
			result.append("\n"); //$NON-NLS-1$
			for (Object a : selected) {
				if (a instanceof MarkElement) {
					result.append(((MarkElement) a).getLabel(a));
					result.append("\n"); //$NON-NLS-1$
				}
			}
		}
		return result.toString();
	}
}
