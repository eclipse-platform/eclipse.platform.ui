/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.navigator;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.internal.ViewsPlugin;

/**
 * A LocalSelectionTransfer may be used for drag and drop operations
 * within the same instance of Eclipse.
 * The selection is made available directly for use in the DropTargetListener.
 * dropAccept method. The DropTargetEvent passed to dropAccept does not contain
 * the drop data. The selection may be used for validation purposes so that the
 * drop can be aborted if appropriate.
 *
 * This class is not intended to be subclassed.
 * 
 * @since 2.1
 */
public class LocalSelectionTransfer extends ByteArrayTransfer {

	// First attempt to create a UUID for the type name to make sure that
	// different Eclipse applications use different "types" of
	// <code>LocalSelectionTransfer</code>
	private static final String TYPE_NAME= "local-selection-transfer-format" + (new Long(System.currentTimeMillis())).toString(); //$NON-NLS-1$;
	private static final int TYPEID= registerType(TYPE_NAME);
	
	private static final LocalSelectionTransfer INSTANCE= new LocalSelectionTransfer();
	
	private ISelection selection;
	
	/**
	 * Only the singleton instance of this class may be used. 
	 */
	private LocalSelectionTransfer() {
	}
	/**
	 * Returns the singleton.
	 */
	public static LocalSelectionTransfer getInstance() {
		return INSTANCE;
	}
	/**
	 * Returns the local transfer data.
	 * 
	 * @return the local transfer data
	 */
	public ISelection getSelection() {
		return selection;
	}
	/**
	 * Tests whether native drop data matches this transfer type.
	 * 
	 * @param result result of converting the native drop data to Java
	 * @return true if the native drop data does not match this transfer type.
	 * 	false otherwise.
	 */
	private boolean isInvalidNativeType(Object result) {
		return !(result instanceof byte[]) || !TYPE_NAME.equals(new String((byte[])result));
	}
	/**
	 * Returns the type id used to identify this transfer.
	 * 
	 * @return the type id used to identify this transfer.
	 */
	protected int[] getTypeIds() {
		return new int[] {TYPEID};
	}
	/**
	 * Returns the type name used to identify this transfer.
	 * 
	 * @return the type name used to identify this transfer.
	 */
	protected String[] getTypeNames(){
		return new String[] {TYPE_NAME};
	}	
	/**
	 * Overrides org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(Object,
	 * TransferData).
	 * Only encode the transfer type name since the selection is read and
	 * written in the same process.
	 * 
	 * @see org.eclipse.swt.dnd.ByteArrayTransfer#javaToNative(java.lang.Object, org.eclipse.swt.dnd.TransferData)
	 */
	public void javaToNative(Object object, TransferData transferData) {
		byte[] check= TYPE_NAME.getBytes();
		super.javaToNative(check, transferData);
	}
	/**
	 * Overrides org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(TransferData).
	 * Test if the native drop data matches this transfer type.
	 * 
	 * @see org.eclipse.swt.dnd.ByteArrayTransfer#nativeToJava(TransferData)
	 */
	public Object nativeToJava(TransferData transferData) {
		Object result= super.nativeToJava(transferData);
		if (isInvalidNativeType(result)) {
			ILog log = ViewsPlugin.getDefault().getLog();
			log.log(
				new Status(
					IStatus.ERROR, 
					ViewsPlugin.PLUGIN_ID, 
					IStatus.ERROR, 
					ResourceNavigatorMessages.getString("LocalSelectionTransfer.errorMessage"), null));	//$NON-NLS-1$
		}
		return selection;
	}
	/**
	 * Sets the transfer data for local use.
	 * 
	 * @param s the transfer data
	 */	
	public void setSelection(ISelection s) {
		selection = s;
	}
}
