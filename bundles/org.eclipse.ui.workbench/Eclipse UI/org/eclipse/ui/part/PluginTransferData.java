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
package org.eclipse.ui.part;


/**
 * Record for transferring data during a drag and drop operation between
 * different plug-ins. This object contains an extension identifier and a block
 * of bytes. When the drop occurs, the data is interpreted by an action defined
 * in the specified extension.
 * <p>
 * The workbench will automatically create instances of this class as required.
 * It is not intended to be instantiated or subclassed by clients.
 * </p>
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
