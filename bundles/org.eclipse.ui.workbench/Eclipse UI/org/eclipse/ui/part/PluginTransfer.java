/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

/**
 * This class can be used to transfer an instance of <code>PluginTransferData</code>
 * between two parts in a workbench in a drag and drop operation.
 * <p>
 * In every drag and drop operation there is a <code>DragSource</code> and 
 * a <code>DropTarget</code>.  When a drag occurs a <code>Transfer</code> is 
 * used to marshall the drag data from the source into a byte array.  If a drop 
 * occurs another <code>Transfer</code> is used to marshall the byte array into
 * drop data for the target.
 * </p><p>
 * A <code>PluginTransferData</code> contains the id of a drop action extension.  
 * If a drop occurs the extension is invoked to perform a drop action.  As a benefit, 
 * the destination viewer doesn't need to have any knowledge of the items being 
 * dropped into it.  
 * </p><p>
 * This class can be used for a <code>Viewer<code> or an SWT component directly.
 * A singleton is provided which may be serially reused (see <code>getInstance</code>).  
 * It is not intended to be subclassed.
 * </p>
 *
 * @see org.eclipse.jface.viewers.StructuredViewer
 * @see org.eclipse.swt.dnd.DropTarget
 * @see org.eclipse.swt.dnd.DragSource
 */
public class PluginTransfer extends ByteArrayTransfer {

    private static final String TYPE_NAME = "pluggable-transfer-format";//$NON-NLS-1$

    private static final int TYPEID = registerType(TYPE_NAME);

    /**
     * Singleton instance.
     */
    private static PluginTransfer instance = new PluginTransfer();

    /**
     * Creates a new transfer object.
     */
    private PluginTransfer() {
        super();
    }

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance
     */
    public static PluginTransfer getInstance() {
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

    /* (non-Javadoc)
     * Method declared on Transfer.
     */
    protected void javaToNative(Object data, TransferData transferData) {
        PluginTransferData realData = (PluginTransferData) data;
        if (data == null) {
			return;
		}
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(out);
            dataOut.writeUTF(realData.getExtensionId());
            dataOut.writeInt(realData.getData().length);
            dataOut.write(realData.getData());
            dataOut.close();
            super.javaToNative(out.toByteArray(), transferData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * Method declared on Transfer.
     */
    protected Object nativeToJava(TransferData transferData) {
        try {
            byte[] bytes = (byte[]) super.nativeToJava(transferData);
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            DataInputStream dataIn = new DataInputStream(in);
            String extensionName = dataIn.readUTF();
            int len = dataIn.readInt();
            byte[] pluginData = new byte[len];
            dataIn.readFully(pluginData);
            return new PluginTransferData(extensionName, pluginData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //can't get here
        return null;
    }
}
