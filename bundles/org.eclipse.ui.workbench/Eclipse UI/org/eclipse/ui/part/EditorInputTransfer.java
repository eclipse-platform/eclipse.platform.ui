/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.part;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * The <code>EditorInputTransfer</code> class is used to transfer an
 * <code>IEditorInput</code> and corresponding editorId from one part to another
 * in a drag and drop operation.  Only opening of internal editors is supported.
 * <p> 
 * In every drag and drop operation there is a <code>DragSource</code> and a
 * <code>DropTarget</code>.  When a drag occurs a <code>Transfer</code> is used
 * to marshall the drag data from the source into a byte array.  If a drop
 * occurs another <code>Transfer</code> is used to marshall the byte array into
 * drop data for the target.
 * </p>
 * <p>
 * This class can be used for a <code>Viewer<code> or an SWT component directly.
 * A singleton is provided which may be serially reused (see <code>getInstance</code>).  
 * The <code>setEditorId</code> and <code>setInput</code> should be used during
 * a <code>DragSource</code>.<code>dragSetData</code> implementation to
 * populate the transfer with the appropriate editorId and IEditorInput.
 * This class is not intended to be subclassed.
 * </p>
 *
 * @see StructuredViewer
 * @see DropTarget
 * @see DragSource
 * @see IEditorInput
 */
public class EditorInputTransfer extends ByteArrayTransfer {

	/**
	 * Singleton instance.
	 */
	private static final EditorInputTransfer instance =
		new EditorInputTransfer();

	// Create a unique ID to make sure that different Eclipse
	// applications use different "types" of <code>EditorInputTransfer</code>
	private static final String TYPE_NAME = "editor-input-transfer-format:" + System.currentTimeMillis() + ":" + instance.hashCode(); //$NON-NLS-2$//$NON-NLS-1$

	private static final int TYPEID = registerType(TYPE_NAME);

	private String editorId;
	private IEditorInput input;
	/**
	 * Creates a new transfer object.
	 */
	private EditorInputTransfer() {
	}
	/**
	 * Returns the singleton instance.
	 *
	 * @return the singleton instance
	 */
	public static EditorInputTransfer getInstance() {
		return instance;
	}
	/* (non-Javadoc)
	 * Method declared on Transfer.
	 */
	protected int[] getTypeIds() {
		return new int[] { TYPEID };
	}
	/* (non-Javadoc)
	 * Returns the type names.
	 *
	 * @return the list of type names
	 */
	protected String[] getTypeNames() {
		return new String[] { TYPE_NAME };
	}
	public void javaToNative(Object object, TransferData transferData) {
		// No encoding needed since this is a hardcoded string read and written in the same process.
		// See nativeToJava below
		byte[] check = TYPE_NAME.getBytes();
		super.javaToNative(check, transferData);
	}

	public Object nativeToJava(TransferData transferData) {
		Object result = super.nativeToJava(transferData);
		if (isInvalidNativeType(result)) {
			//something went wrong, log error message
			WorkbenchPlugin.log(WorkbenchMessages.getString("EditorInputTransfer.errorMessage"));
		}
		return new Object [] {editorId, input};
	}

	private boolean isInvalidNativeType(Object result) {
		// No encoding needed since this is a hardcoded string read and written in the same process.
		// See javaToNative above
		return !(result instanceof byte[])
			|| !TYPE_NAME.equals(new String((byte[]) result));
	}
	/**
	 * Set the String editorId for the id of the editor to be opened.  This
	 * should be called during <code>DragSource</code>.<code>dragSetData</code>
	 * to set the id of the editor.  Only opening of internal editors is
	 * supported.
	 * @param editorId The editorId to set
	 */
	public void setEditorId(String editorId) {
		this.editorId = editorId;
	}

	/**
	 * Set the IEditorInput for the editor to be opened.  This should be called
	 * during <code>DragSource</code>.<code>dragSetData</code> to set the input
	 * for the editor
	 * @param input The input to set
	 */
	public void setInput(IEditorInput input) {
		this.input = input;
	}

}