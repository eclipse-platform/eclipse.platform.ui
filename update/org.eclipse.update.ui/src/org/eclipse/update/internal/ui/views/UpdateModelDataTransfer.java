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
package org.eclipse.update.internal.ui.views;


import java.io.*;

import org.eclipse.swt.dnd.*;

/**
 * @version 	1.0
 * @author
 */
public class UpdateModelDataTransfer extends ByteArrayTransfer {
	/**
	 * Singleton instance.
	 */
	private static final UpdateModelDataTransfer instance =
		new UpdateModelDataTransfer();
	// Create a unique ID to make sure that different Eclipse
	// applications use different "types" of <code>UpdateModelDataTransfer</code>
	
	public static final String TYPE_PREFIX = "update-model-transfer-format";
	private static final String TYPE_NAME =
		TYPE_PREFIX + ":"
			+ System.currentTimeMillis()
			+ ":"
			+ instance.hashCode();

	private static final int TYPEID = registerType(TYPE_NAME);
	
	public static UpdateModelDataTransfer getInstance() {
		return instance;
	}

	/**
	 * Constructor for ModelDataTransfer.
	 */
	public UpdateModelDataTransfer() {
		super();
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
		if (!(data instanceof Object[])) {
			return;
		}
		Object[] objects = (Object[]) data;
		int count = objects.length;
		
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream objectOut = new ObjectOutputStream(out);

			//write the number of resources
			objectOut.writeInt(count);

			//write each object
			for (int i = 0; i < objects.length; i++) {
				objectOut.writeObject(objects[i]);
			}

			//cleanup
			objectOut.close();
			out.close();
			byte[] bytes = out.toByteArray();
			super.javaToNative(bytes, transferData);
		} catch (IOException e) {
			//it's best to send nothing if there were problems
			System.out.println(e);
		}
			
	}
	/* (non-Javadoc)
	 * Method declared on Transfer.
	 */
	protected Object nativeToJava(TransferData transferData) {
		byte[] bytes = (byte[]) super.nativeToJava(transferData);
		if (bytes == null)
			return null;
		try {
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));

			int count = in.readInt();
			Object[] objects = new Object[count];
			for (int i = 0; i < count; i++) {
				objects[i] = in.readObject();
			}
			in.close();
			return objects;
		} catch (ClassNotFoundException e) {
			System.out.println(e);
			return null;
		} catch (IOException e) {
			System.out.println(e);
			return null;
		}
	}
}
