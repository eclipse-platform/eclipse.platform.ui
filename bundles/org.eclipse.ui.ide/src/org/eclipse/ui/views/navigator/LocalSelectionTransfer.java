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
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.TransferData;

/**
 * A LocalSelectionTransfer may be used for drag and drop operations within the
 * same instance of Eclipse. The selection is made available directly for use in
 * the DropTargetListener. dropAccept method. The DropTargetEvent passed to
 * dropAccept does not contain the drop data. The selection may be used for
 * validation purposes so that the drop can be aborted if appropriate. This
 * class is not intended to be subclassed.
 *
 * @since 2.1
 * @noextend This class is not intended to be subclassed by clients.
 * @deprecated as of 3.5, use
 *             {@link org.eclipse.jface.util.LocalSelectionTransfer} instead
 */
@Deprecated(forRemoval = true)
public class LocalSelectionTransfer extends org.eclipse.jface.util.LocalSelectionTransfer {

	private static final LocalSelectionTransfer INSTANCE = new LocalSelectionTransfer();

	/**
	 * The get/set methods delegate to JFace's LocalSelectionTransfer to allow data
	 * to be exchanged freely whether the client uses this LocalSelectionTransfer or
	 * JFace's LocalSelectionTransfer. Protected methods such as getTypeIds() are
	 * handled via inheritance, not delegation due to visibility constraints.
	 */
	private org.eclipse.jface.util.LocalSelectionTransfer jfaceTransfer = org.eclipse.jface.util.LocalSelectionTransfer
			.getTransfer();

	/**
	 * Only the singleton instance of this class may be used.
	 */
	private LocalSelectionTransfer() {
	}

	/**
	 * Returns the singleton.
	 *
	 * @return the singleton
	 */
	public static LocalSelectionTransfer getInstance() {
		return INSTANCE;
	}

	@Override
	public ISelection getSelection() {
		return jfaceTransfer.getSelection();
	}

	@Override
	public long getSelectionSetTime() {
		return jfaceTransfer.getSelectionSetTime();
	}

	@Override
	public void setSelection(ISelection s) {
		jfaceTransfer.setSelection(s);
	}

	@Override
	public void setSelectionSetTime(long time) {
		jfaceTransfer.setSelectionSetTime(time);
	}

	@Override
	public void javaToNative(Object object, TransferData transferData) {
		jfaceTransfer.javaToNative(object, transferData);
	}

	@Override
	public Object nativeToJava(TransferData transferData) {
		return jfaceTransfer.nativeToJava(transferData);
	}
}
