/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import org.eclipse.core.internal.watson.IPathRequestor;
import org.eclipse.core.runtime.QualifiedName;

public class SyncInfoWriter {
	protected Synchronizer synchronizer;
	protected Workspace workspace;

	// version number
	public static final int SYNCINFO_SAVE_VERSION = 3;
	public static final int SYNCINFO_SNAP_VERSION = 3;

	// for sync info
	public static final byte INDEX = 1;
	public static final byte QNAME = 2;

	public SyncInfoWriter(Workspace workspace, Synchronizer synchronizer) {
		super();
		this.workspace = workspace;
		this.synchronizer = synchronizer;
	}

	public void savePartners(DataOutputStream output) throws IOException {
		Set registry = synchronizer.getRegistry();
		output.writeInt(registry.size());
		for (Iterator i = registry.iterator(); i.hasNext();) {
			QualifiedName qname = (QualifiedName) i.next();
			output.writeUTF(qname.getQualifier());
			output.writeUTF(qname.getLocalName());
		}
	}

	/**
	 * SAVE_FILE -> VERSION_ID RESOURCE+
	 * VERSION_ID -> int
	 * RESOURCE -> RESOURCE_PATH SIZE SYNCINFO*
	 * RESOURCE_PATH -> String
	 * SIZE -> int
	 * SYNCINFO -> TYPE BYTES
	 * TYPE -> INDEX | QNAME
	 * INDEX -> byte int
	 * QNAME -> byte String
	 * BYTES -> byte[]
	 */
	public void saveSyncInfo(ResourceInfo info, IPathRequestor requestor, DataOutputStream output, List writtenPartners) throws IOException {
		Map table = info.getSyncInfo(false);
		if (table == null)
			return;
		// if this is the first sync info that we have written, then
		// write the version id for the file.
		if (output.size() == 0)
			output.writeInt(SYNCINFO_SAVE_VERSION);
		output.writeUTF(requestor.requestPath().toString());
		output.writeInt(table.size());
		for (Iterator i = table.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			QualifiedName name = (QualifiedName) entry.getKey();
			// if we have already written the partner name once, then write an integer
			// constant to represent it instead to remove duplication
			int index = writtenPartners.indexOf(name);
			if (index == -1) {
				// FIXME: what to do about null qualifier?
				output.writeByte(QNAME);
				output.writeUTF(name.getQualifier());
				output.writeUTF(name.getLocalName());
				writtenPartners.add(name);
			} else {
				output.writeByte(INDEX);
				output.writeInt(index);
			}
			byte[] bytes = (byte[]) entry.getValue();
			output.writeInt(bytes.length);
			output.write(bytes);
		}
	}

	/**
	 * SNAP_FILE -> [VERSION_ID RESOURCE]*
	 * VERSION_ID -> int
	 * RESOURCE -> RESOURCE_PATH SIZE SYNCINFO*
	 * RESOURCE_PATH -> String
	 * SIZE -> int
	 * SYNCINFO -> QNAME BYTES
	 * QNAME -> String String
	 * BYTES -> byte[]
	 */
	public void snapSyncInfo(ResourceInfo info, IPathRequestor requestor, DataOutputStream output) throws IOException {
		if (!info.isSet(ICoreConstants.M_SYNCINFO_SNAP_DIRTY))
			return;
		Map table = info.getSyncInfo(false);
		if (table == null)
			return;
		// write the version id for the snapshot.
		output.writeInt(SYNCINFO_SNAP_VERSION);
		output.writeUTF(requestor.requestPath().toString());
		output.writeInt(table.size());
		for (Iterator i = table.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			QualifiedName name = (QualifiedName) entry.getKey();
			output.writeUTF(name.getQualifier());
			output.writeUTF(name.getLocalName());
			byte[] bytes = (byte[]) entry.getValue();
			output.writeInt(bytes.length);
			output.write(bytes);
		}
		info.clear(ICoreConstants.M_SYNCINFO_SNAP_DIRTY);
	}
}
