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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

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
 * This class can be used for a <code>Viewer</code> or an SWT component directly.
 * A singleton is provided which may be serially reused (see <code>getInstance</code>).  
 * For an implementor of <code>IEditorInput</code> to be supported by
 * <code>EditorInputTransfer</code>, it must provide a proper implementation of
 * <code>IEditorInput</code>.<code>getPersistable</code>.  For further details,
 * consult the <code>org.eclipse.ui.elementFactories</code> extension point.
 * </p>
 * <p> 
 * The data for a transfer is represented by the <code>EditorInputData</code>
 * class, and a convenience method <code>createEditorInputData</code> is
 * provided.  A <code>DragSource</code>.<code>dragSetData</code> implementation
 * should set the data to an array of <code>EditorInputData</code>.  In this
 * way, the dragging of multiple editor inputs is supported.
 * </p>
 * <p>
 * Below is an example of how to set the data for dragging a single editor
 * input using a <code>EditorInputTransfer</code>.
 * </p>
 * <p>
 * <pre>
 * public void dragSetData(DragSourceEvent event) {
 * 		if (EditorInputTransfer.getInstance().isSupportedType(event.dataType)) {
 * 
 * 			EditorInputTransfer.EditorInputData data = 
 * 				EditorInputTransfer.
 * 				createEditorInputData(EDITOR_ID, getEditorInput());
 * 			event.data = new EditorInputTransfer.EditorInputData [] {data};
 * 		}
 * }
 * </pre>
 * </p>
 *
 * @see org.eclipse.jface.viewers.StructuredViewer
 * @see org.eclipse.swt.dnd.DropTarget
 * @see org.eclipse.swt.dnd.DragSource
 * @see org.eclipse.ui.IEditorInput
 * @see org.eclipse.ui.IPersistableElement
 * @see org.eclipse.ui.IElementFactory
 */
public class EditorInputTransfer extends ByteArrayTransfer {

    /**
     * Singleton instance.
     */
    private static final EditorInputTransfer instance = new EditorInputTransfer();

    // Create a unique ID to make sure that different Eclipse
    // applications use different "types" of <code>EditorInputTransfer</code>
    private static final String TYPE_NAME = "editor-input-transfer-format:" + System.currentTimeMillis() + ":" + instance.hashCode(); //$NON-NLS-2$//$NON-NLS-1$

    private static final int TYPEID = registerType(TYPE_NAME);

    public static class EditorInputData {

        public String editorId;

        public IEditorInput input;

        private EditorInputData(String editorId, IEditorInput input) {
            this.editorId = editorId;
            this.input = input;
        }
    }

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

    /* (non-Javadoc)
     * Method declared on Transfer.
     */
    public void javaToNative(Object data, TransferData transferData) {

        if (!(data instanceof EditorInputData[])) {
            return;
        }

        EditorInputData[] editorInputs = (EditorInputData[]) data;
        /**
         * The editor input serialization format is:
         * (int)	number of editor inputs
         * Then, the following for each editor input:
         * (String)	editorId
         * (String)	factoryId 
         * (String)	data used to recreate the IEditorInput
         */

        int editorInputCount = editorInputs.length;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DataOutputStream dataOut = new DataOutputStream(out);

            //write the number of resources
            dataOut.writeInt(editorInputCount);

            //write each resource
            for (int i = 0; i < editorInputs.length; i++) {
                writeEditorInput(dataOut, editorInputs[i]);
            }

            //cleanup
            dataOut.close();
            out.close();
            byte[] bytes = out.toByteArray();
            super.javaToNative(bytes, transferData);
        } catch (IOException e) {
        }
    }

    /* (non-Javadoc)
     * Method declared on Transfer.
     */
    public Object nativeToJava(TransferData transferData) {

        byte[] bytes = (byte[]) super.nativeToJava(transferData);
        if (bytes == null) {
			return null;
		}
        DataInputStream in = new DataInputStream(
                new ByteArrayInputStream(bytes));
        try {
            int count = in.readInt();
            EditorInputData[] results = new EditorInputData[count];
            for (int i = 0; i < count; i++) {
                results[i] = readEditorInput(in);
            }
            return results;
        } catch (IOException e) {
            return null;
        } catch (WorkbenchException e) {
            return null;
        }

    }

    /**
     * Method readEditorInput.
     * @param in
     * @return EditorInputData
     */
    private EditorInputData readEditorInput(DataInputStream dataIn)
            throws IOException, WorkbenchException {

        String editorId = dataIn.readUTF();
        String factoryId = dataIn.readUTF();
        String xmlString = dataIn.readUTF();

        if (xmlString == null || xmlString.length() == 0) {
			return null;
		}

        StringReader reader = new StringReader(xmlString);

        // Restore the editor input
        XMLMemento memento = XMLMemento.createReadRoot(reader);

        IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(
                factoryId);

        if (factory != null) {
            IAdaptable adaptable = factory.createElement(memento);
            if (adaptable != null && (adaptable instanceof IEditorInput)) {
                return new EditorInputData(editorId, (IEditorInput) adaptable);
            }
        }

        return null;
    }

    /**
     * Method writeEditorInput.
     * @param dataOut
     * @param editorInputData
     */
    private void writeEditorInput(DataOutputStream dataOut,
            EditorInputData editorInputData) throws IOException {
        //write the id of the editor
        dataOut.writeUTF(editorInputData.editorId);

        //write the information needed to recreate the editor input
        if (editorInputData.input != null) {
            // Capture the editor information
            XMLMemento memento = XMLMemento.createWriteRoot("IEditorInput");//$NON-NLS-1$

            IPersistableElement element = editorInputData.input
                    .getPersistable();
            if (element != null) {
                //get the IEditorInput to save its state
                element.saveState(memento);

                //convert memento to String
                StringWriter writer = new StringWriter();
                memento.save(writer);
                writer.close();

                //write the factor ID and state information
                dataOut.writeUTF(element.getFactoryId());
                dataOut.writeUTF(writer.toString());
            }
        }
    }

    public static EditorInputData createEditorInputData(String editorId,
            IEditorInput input) {
        return new EditorInputData(editorId, input);
    }

}
