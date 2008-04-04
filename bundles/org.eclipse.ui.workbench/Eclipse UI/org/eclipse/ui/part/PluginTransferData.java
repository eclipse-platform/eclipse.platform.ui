/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.part;

/**
 * Record for transferring data during a drag and drop operation between
 * different plug-ins. This object contains an extension identifier and a block
 * of bytes. When the drop occurs, the data is interpreted by an action defined
 * in the specified extension.
 * <p>
 * Clients using PluginTransfer should create an instance to contain the
 * drop data.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PluginTransferData {
    String extensionName;

    byte[] transferData;

    /**
     * Creates a new record for the given extension id and data.
     *
     * @param extensionId the extension id
     * @param data the data to transfer
     */
    public PluginTransferData(String extensionId, byte[] data) {
        this.extensionName = extensionId;
        this.transferData = data;
    }

    /**
     * Returns the data being transferred.
     *
     * @return the data
     */
    public byte[] getData() {
        return transferData;
    }

    /**
     * Returns the id of the extension that will provide the drop action.
     *
     * @return the extension id
     */
    public String getExtensionId() {
        return extensionName;
    }
}
