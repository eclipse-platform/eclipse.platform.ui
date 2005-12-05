/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.navigator.internal.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.internal.CommonNavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part of a work in
 * progress. There is a guarantee neither that this API will work nor that it will remain the same.
 * Please do not use this API without consulting with the Platform/UI team.
 * </p>
 * 
 * @since 3.2
 *  
 */
public class NavigatorSelectionSerializer implements ISerializer {

	private String viewerId;

	public class NavigatorSerializedSelection {

		public final String viewerId;

		public final Map selectionMap;

		public NavigatorSerializedSelection(String viewerId, Map selectionMap) {
			this.viewerId = viewerId;
			this.selectionMap = selectionMap;
		}
	}

	public NavigatorSelectionSerializer() {
	}

	/**
	 *  
	 */
	public NavigatorSelectionSerializer(String aViewerId) {
		this.viewerId = aViewerId;
	}

	public byte[] toByteArray(Object object) {
		if (viewerId == null)
			return null;
		if (!(object instanceof IStructuredSelection))
			return new byte[0];

		IStructuredSelection selection = (IStructuredSelection) object;
		//	  TODO MDE Serialize muliple elements -- getFirstElement() is just to
		// test the idea
		byte[] result = null;

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

		try {
			dataOutputStream.writeUTF(viewerId);

			byte[] data = null;
			Object element = null;
			SerializerDescriptor[] serializerDescriptors = null;
			/* write the number of objects to be serialized */
			dataOutputStream.writeInt(selection.size());
			for (Iterator selectionIterator = selection.iterator(); selectionIterator.hasNext();) {
				element = selectionIterator.next();
				serializerDescriptors = CommonDropHandlerService.getInstance(viewerId).getSerializersEnabledFor(element);

				/* write the number of different representations to be stored */
				ByteArrayOutputStream localRepresentationOutputStream = null;
				int differentRepresentations = 0;
				DataOutputStream serializedDataOutputStream = new DataOutputStream((localRepresentationOutputStream = new ByteArrayOutputStream()));
				for (int i = 0; i < serializerDescriptors.length; i++) {
					ISerializer serializer = serializerDescriptors[i].getSerializer();
					data = serializer.toByteArray(element);
					if (data != null) {
						/*
						 * write the id of the serializer that marshalled the data
						 */
						serializedDataOutputStream.writeUTF(serializerDescriptors[i].id);
						/* write the amount of data */
						serializedDataOutputStream.writeInt(data.length);
						/* write the data */
						serializedDataOutputStream.write(data);
						++differentRepresentations;
					}
				}

				dataOutputStream.writeInt(differentRepresentations);
				dataOutputStream.write(localRepresentationOutputStream.toByteArray());
			}
			result = byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			NavigatorPlugin.log(CommonNavigatorMessages.NavigatorSelectionSerializer_1 + e.toString());  
			result = null;
		}
		return result;
	}

 
	public Object fromByteArray(byte[] data) {
		Map results = new HashMap();

		DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(data));
		try {
			int localDataLen = 0;
			byte[] localData = null;
			String serializerId = null;
			Object dataObject = null;

			String localViewerId = dataInputStream.readUTF();
			int numberOfObjects = dataInputStream.readInt();
			for (int i = 0; i < numberOfObjects; i++) {
				int numberOfRepresentations = dataInputStream.readInt();
				for (int j = 0; j < numberOfRepresentations; j++) {
					serializerId = dataInputStream.readUTF();
					SerializerDescriptor serializerDescriptor = CommonDropHandlerService.getInstance(localViewerId).getSerializerById(serializerId);
					ISerializer serializer = serializerDescriptor.getSerializer();
					localDataLen = dataInputStream.readInt();
					localData = new byte[localDataLen];
					dataInputStream.read(localData, 0, localData.length);
					dataObject = serializer.fromByteArray(localData);
					getDataList(results, serializerId).add(dataObject);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new NavigatorSerializedSelection(viewerId, results);
	}

	public List getDataList(Map serializerMap, String id) {
		List result = (List) serializerMap.get(id);
		if (result == null)
			serializerMap.put(id, (result = new ArrayList()));
		return result;
	}

}
