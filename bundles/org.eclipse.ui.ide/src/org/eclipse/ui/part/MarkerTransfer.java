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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import java.io.*;

/**
 * A <code>MarkerTransfer</code> is used to transfer an array of 
 * <code>IMarker</code>s from one part to another in a drag and drop 
 * operation.
 * <p>
 * In every drag and drop operation there is a <code>DragSource</code> and 
 * a <code>DropTarget</code>.  When a drag occurs a <code>Transfer</code> is 
 * used to marshall the drag data from the source into a byte array.  If a drop 
 * occurs another <code>Transfer</code> is used to marshall the byte array into
 * drop data for the target.
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
public class MarkerTransfer extends ByteArrayTransfer {
	
	/**
	 * Singleton instance.
	 */
	private static final MarkerTransfer instance = new MarkerTransfer();

	// Create a unique ID to make sure that different Eclipse
	// applications use different "types" of <code>MarkerTransfer</code>
	private static final String TYPE_NAME = "marker-transfer-format" + System.currentTimeMillis() + ":" + instance.hashCode();//$NON-NLS-2$//$NON-NLS-1$
	private static final int TYPEID = registerType(TYPE_NAME);

	private IWorkspace workspace;
	
/**
 * Creates a new transfer object.
 */
private MarkerTransfer() {
}
/**
 * Locates and returns the marker associated with the given attributes.
 *
 * @param pathString the resource path
 * @param id the id of the marker to get (as per {@link IResource#getMarker
 *    IResource.getMarker})
 * @return the specified marker
 */
private IMarker findMarker(String pathString, long id) {
	IPath path = new Path(pathString);
	IResource resource = workspace.getRoot().findMember(path);
	if (resource != null) {
		return resource.getMarker(id);
	}
	return null;		
}
/**
 * Returns the singleton instance.
 *
 * @return the singleton instance
 */
public static MarkerTransfer getInstance() {
	return instance;
}
/* (non-Javadoc)
 * Method declared on Transfer.
 */
protected int[] getTypeIds() {
	return new int[] {TYPEID};
}
/* (non-Javadoc)
 * Returns the type names.
 *
 * @return the list of type names
 */
protected String[] getTypeNames() {
	return new String[] {TYPE_NAME};
}
/* (non-Javadoc)
 * Method declared on Transfer.
 * On a successful conversion, the transferData.result field will be set to
 * OLE.S_OK. If this transfer agent is unable to perform the conversion, the
 * transferData.result field will be set to the failure value of OLE.DV_E_TYMED.
 */
protected void javaToNative(Object object, TransferData transferData) {
	/**
	 * Transfer data is an array of markers.  Serialized version is:
	 * (int) number of markers
	 * (Marker) marker 1
	 * (Marker) marker 2
	 * ... repeat last four for each subsequent marker
	 * see writeMarker for the (Marker) format.
	 */
	Object[] markers = (Object[]) object;
	lazyInit(markers);

	ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	DataOutputStream out = new DataOutputStream(byteOut);

	byte[] bytes = null;

	try {
		/* write number of markers */
		out.writeInt(markers.length);

		/* write markers */
		for (int i = 0; i < markers.length; i++) {
			writeMarker((IMarker)markers[i], out);
		}
		out.close();
		bytes = byteOut.toByteArray();
	} catch (IOException e) {
		//when in doubt send nothing
	}

	if (bytes != null) {
	    super.javaToNative(bytes, transferData);
	}
}
/**
 * Initializes the transfer mechanism if necessary.
 */
private void lazyInit(Object[] markers) {
	if (workspace == null) {
		if (markers != null && markers.length > 0) {
			this.workspace = ((IMarker)markers[0]).getResource().getWorkspace();
		}
	}
}
/* (non-Javadoc)
 * Method declared on Transfer.
 */
protected Object nativeToJava(TransferData transferData) {
	byte[] bytes = (byte[]) super.nativeToJava(transferData);
	DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

	try {
		/* read number of markers */
		int n = in.readInt();

		/* read markers */
		IMarker[] markers = new IMarker[n];
		for (int i = 0; i < n; i++) {
			IMarker marker = readMarker(in);
			if (marker == null) {
				return null;
			}
			markers[i] = marker;
		}
		return markers;
	} catch (IOException e) {
		return null;
	}
}
/**
 * Reads and returns a single marker from the given stream.
 *
 * @param dataIn the input stream
 * @return the marker
 * @exception IOException if there is a problem reading from the stream
 */
private IMarker readMarker(DataInputStream dataIn) throws IOException {
	/**
	 * Marker serialization format is as follows:
	 * (String) path of resource for marker
	 * (int) marker ID
	 */
	String path = dataIn.readUTF();
	long id = dataIn.readLong();
	return findMarker(path, id);
}
/**
 * Writes the given marker to the given stream.
 *
 * @param marker the marker
 * @param dataOut the output stream
 * @exception IOException if there is a problem writing to the stream
 */
private void writeMarker(IMarker marker, DataOutputStream dataOut) throws IOException {
	/**
	 * Marker serialization format is as follows:
	 * (String) path of resource for marker
	 * (int) marker ID
	 */

	dataOut.writeUTF(marker.getResource().getFullPath().toString());
	dataOut.writeLong(marker.getId());
}
}
