/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 205678
 *******************************************************************************/
package org.eclipse.ui.part;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * The <code>ResourceTransfer</code> class is used to transfer an
 * array of <code>IResource</code>s from one part to another in a
 * drag and drop operation or a cut, copy, paste action.
 * <p>
 * In every drag and drop operation there is a <code>DragSource</code> and
 * a <code>DropTarget</code>.  When a drag occurs a <code>Transfer</code> is
 * used to marshal the drag data from the source into a byte array.  If a drop
 * occurs another <code>Transfer</code> is used to marshal the byte array into
 * drop data for the target.
 * </p>
 * <p>
 * When a <code>CutAction</code> or a <code>CopyAction</code> is performed,
 * this transfer is used to place references to the selected resources
 * on the <code>Clipboard</code>.  When a <code>PasteAction</code> is performed, the
 * references on the clipboard are used to move or copy the resources
 * to the selected destination.
 * </p>
 * <p>
 * This class can be used for a <code>Viewer<code> or an SWT component directly.
 * A singleton is provided which may be serially reused (see <code>getInstance</code>).
 * It is not intended to be subclassed.
 * </p>
 * <p>
 * The amount of resources which can be transferred is limited to <code>MAX_RESOURCES_TO_TRANSFER</code> elements.
 * </p>
 * @see org.eclipse.jface.viewers.StructuredViewer
 * @see org.eclipse.swt.dnd.DropTarget
 * @see org.eclipse.swt.dnd.DragSource
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ResourceTransfer extends ByteArrayTransfer {

	/**
	 * See bug 205678: sometimes we can misinterpret native data received from
	 * clipboard. No one seriously would copy/paste or drag/drop more then
	 * 100.000 resources: only creating an *empty* array of 100.000.000
	 * resources will cause OOME on 512 MB heap size (default for shipped
	 * Eclipse packages), same with copy/paste of a *full* array of 10.000.000
	 * elements.
	 */
	private final static int MAX_RESOURCES_TO_TRANSFER = 1000 * 1000;

	/**
	 * Singleton instance.
	 */
    private static final ResourceTransfer instance = new ResourceTransfer();

    // Create a unique ID to make sure that different Eclipse
    // applications use different "types" of <code>ResourceTransfer</code>
    private static final String TYPE_NAME = "resource-transfer-format:" + System.currentTimeMillis() + ":" + instance.hashCode();//$NON-NLS-2$//$NON-NLS-1$

    private static final int TYPEID = registerType(TYPE_NAME);

    private IWorkspace workspace = ResourcesPlugin.getWorkspace();

    /**
     * Creates a new transfer object.
     */
    private ResourceTransfer() {
    }

    /**
     * Returns the singleton instance.
     *
     * @return the singleton instance
     */
    public static ResourceTransfer getInstance() {
        return instance;
    }

    @Override
	protected int[] getTypeIds() {
        return new int[] { TYPEID };
    }

    @Override
	protected String[] getTypeNames() {
        return new String[] { TYPE_NAME };
    }

    @Override
	protected void javaToNative(Object data, TransferData transferData) {
        if (!(data instanceof IResource[])) {
            return;
        }

        IResource[] resources = (IResource[]) data;
        /**
         * The resource serialization format is:
         *  (int) number of resources
         * Then, the following for each resource:
         *  (int) resource type
         *  (String) path of resource
         */

        int resourceCount = resources.length;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(out);

            //write the number of resources
            dataOut.writeInt(resourceCount);

            //write each resource
            for (int i = 0; i < resources.length; i++) {
                writeResource(dataOut, resources[i]);
            }

            //cleanup
            dataOut.close();
            out.close();
            byte[] bytes = out.toByteArray();
            super.javaToNative(bytes, transferData);
        } catch (IOException e) {
            //it's best to send nothing if there were problems
        }
    }

    @Override
	protected Object nativeToJava(TransferData transferData) {
        /**
         * The resource serialization format is:
         *  (int) number of resources
         * Then, the following for each resource:
         *  (int) resource type
         *  (String) path of resource
         */

        byte[] bytes = (byte[]) super.nativeToJava(transferData);
        if (bytes == null) {
			return null;
		}
        DataInputStream in = new DataInputStream(
                new ByteArrayInputStream(bytes));
        try {
            int count = in.readInt();
			if (count > MAX_RESOURCES_TO_TRANSFER) {
				String message = "Transfer aborted, too many resources: " + count + "."; //$NON-NLS-1$ //$NON-NLS-2$
				if (Util.isLinux()) {
					message += "\nIf you are running in x11vnc environment please consider to switch to vncserver " + //$NON-NLS-1$
							"+ vncviewer or to run x11vnc without clipboard support " + //$NON-NLS-1$
							"(use '-noclipboard' and '-nosetclipboard' arguments)."; //$NON-NLS-1$
				}
				IDEWorkbenchPlugin.log(message, new IllegalArgumentException(
						"Maximum limit of resources to transfer is: " + MAX_RESOURCES_TO_TRANSFER)); //$NON-NLS-1$
				return null;
			}
            IResource[] results = new IResource[count];
            for (int i = 0; i < count; i++) {
                results[i] = readResource(in);
            }
            return results;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Reads a resource from the given stream.
     *
     * @param dataIn the input stream
     * @return the resource
     * @exception IOException if there is a problem reading from the stream
     */
    private IResource readResource(DataInputStream dataIn) throws IOException {
        int type = dataIn.readInt();
        String path = dataIn.readUTF();
        switch (type) {
        case IResource.FOLDER:
            return workspace.getRoot().getFolder(new Path(path));
        case IResource.FILE:
            return workspace.getRoot().getFile(new Path(path));
        case IResource.PROJECT:
            return workspace.getRoot().getProject(path);
        }
        throw new IllegalArgumentException(
                "Unknown resource type in ResourceTransfer.readResource"); //$NON-NLS-1$
    }

    /**
     * Writes the given resource to the given stream.
     *
     * @param dataOut the output stream
     * @param resource the resource
     * @exception IOException if there is a problem writing to the stream
     */
    private void writeResource(DataOutputStream dataOut, IResource resource)
            throws IOException {
        dataOut.writeInt(resource.getType());
        dataOut.writeUTF(resource.getFullPath().toString());
    }
}
