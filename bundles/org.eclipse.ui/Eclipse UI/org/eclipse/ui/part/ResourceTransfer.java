package org.eclipse.ui.part;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import java.io.*;

/**
 * The <code>ResourceTransfer</code> class is used to transfer an
 * array of <code>IResources</code>s from one part to another in a 
 * in a drag and drop operation.
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
 * @see StructuredViewer
 * @see DropTarget
 * @see DragSource
 */
public class ResourceTransfer extends ByteArrayTransfer {

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
 */
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
	 * Projects are ignored.
	 */

	//count how many non-project resources there are
	int resourceCount = 0;
	for (int i = 0; i < resources.length; i++) {
		if (resources[i].getType() != IResource.PROJECT) {
			resourceCount++;
		}
	}
	try {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dataOut = new DataOutputStream(out);

		//write the number of resources
		dataOut.writeInt(resourceCount);

		//write each non-project resource
		for (int i = 0; i < resources.length; i++) {
			if (resources[i].getType() != IResource.PROJECT) {
				writeResource(dataOut, resources[i]);
			}
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
/* (non-Javadoc)
 * Method declared on Transfer.
 */
protected Object nativeToJava(TransferData transferData) {
	/**
	 * The resource serialization format is:
	 *  (int) number of resources
	 * Then, the following for each resource:
	 *  (int) resource type
	 *  (String) path of resource
	 * Projects are ignored.
	 */

	byte[] bytes = (byte[]) super.nativeToJava(transferData);
	if (bytes == null)
		return null;
	DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
	try {
		int count = in.readInt();
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
		case IResource.FOLDER :
			return workspace.getRoot().getFolder(new Path(path));
		case IResource.FILE :
			return workspace.getRoot().getFile(new Path(path));
	}
	Assert.isTrue(false, "Unknown resource type in ResourceTransfer.readResource");//$NON-NLS-1$
	return null;
}
/**
 * Writes the given resource to the given stream.
 *
 * @param dataOut the output stream
 * @param resource the resource
 * @exception IOException if there is a problem writing to the stream
 */
private void writeResource(DataOutputStream dataOut, IResource resource) throws IOException {
	dataOut.writeInt(resource.getType());
	dataOut.writeUTF(resource.getFullPath().toString());
}
}
