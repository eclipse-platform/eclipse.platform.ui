/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.*;
import java.util.Map;
import org.eclipse.core.internal.properties.PropertyStore;
import org.eclipse.core.internal.utils.ObjectMap;
import org.eclipse.core.internal.watson.IElementTreeData;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.QualifiedName;

public class ResourceInfo implements IElementTreeData, ICoreConstants {

	/** Set of flags which reflect various states of the info (dirty, transient, ...). */
	protected int flags = 0;

	/** The generation count for encoding changes. */
	private int charsetGenerationCount = 0;

	/** Unique content identifier */
	protected int contentId = 0;

	/** Unique modification stamp */
	// thread safety: (Concurrency004)
	protected volatile long modificationStamp = IResource.NULL_STAMP;

	/** Unique node identifier */
	// thread safety: (Concurrency004)
	protected volatile long nodeId = 0;

	/** Local sync info */
	// thread safety: (Concurrency004)
	protected volatile long localInfo = I_NULL_SYNC_INFO;

	/** The generation count for sync info changes. */
	protected int syncInfoGenerationCount = 0;

	/** 
	 * The table of sync infos. 
	 * <p>
	 * This field is declared as the implementing class rather than the
	 * interface so we ensure that we get it right since we are making certain
	 * assumptions about the object type w.r.t. casting.
	 */
	protected ObjectMap syncInfo = null;

	/** 
	 * The properties which are maintained for the lifecycle of the workspace.
	 * <p>
	 * This field is declared as the implementing class rather than the
	 * interface so we ensure that we get it right since we are making certain
	 * assumptions about the object type w.r.t. casting.
	 */
	protected ObjectMap sessionProperties = null;

	/** The generation count for marker changes. */
	protected int markerGenerationCount = 0;

	/** The collection of markers for this resource. */
	protected MarkerSet markers = null;

	/** 
	 * Clears all of the bits indicated by the mask.
	 */
	public void clear(int mask) {
		flags &= ~mask;
	}

	public synchronized void clearSessionProperties() {
		sessionProperties = null;
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null; // never gets here.
		}
	}

	/** 
	 * Returns the integer value stored in the indicated part of this info's flags.
	 */
	protected static int getBits(int flags, int mask, int start) {
		return (flags & mask) >> start;
	}

	public int getCharsetGenerationCount() {
		return charsetGenerationCount;
	}

	public int getContentId() {
		return contentId;
	}

	/** 
	 * Returns the set of flags for this info.
	 */
	public int getFlags() {
		return flags;
	}

	/** 
	 * Gets the local-relative sync information.
	 */
	public long getLocalSyncInfo() {
		return localInfo;
	}

	/** 
	 * Returns the marker generation count.
	 * The count is incremented whenever markers on the resource change.
	 */
	public int getMarkerGenerationCount() {
		return markerGenerationCount;
	}

	/** 
	 * Returns a copy of the collection of makers on this resource.
	 * <code>null</code> is returned if there are none.
	 */
	public MarkerSet getMarkers() {
		return getMarkers(true);
	}

	/** 
	 * Returns the collection of makers on this resource.
	 * <code>null</code> is returned if there are none.
	 */
	public MarkerSet getMarkers(boolean makeCopy) {
		if (markers == null)
			return null;
		return makeCopy ? (MarkerSet) markers.clone() : markers;
	}

	public long getModificationStamp() {
		return modificationStamp;
	}

	public long getNodeId() {
		return nodeId;
	}

	/**
	 * Returns the property store associated with this info.  The return value may be null.
	 */
	public PropertyStore getPropertyStore() {
		return null;
	}

	/** 
	 * Returns the value of the identified session property
	 */
	public Object getSessionProperty(QualifiedName name) {
		// thread safety: (Concurrency001)
		Map temp = sessionProperties;
		if (temp == null)
			return null;
		return temp.get(name);
	}

	public synchronized byte[] getSyncInfo(QualifiedName id, boolean makeCopy) {
		// thread safety: (Concurrency001)
		byte[] b;
		if (syncInfo == null)
			return null;
		b = (byte[]) syncInfo.get(id);
		return b == null ? null : (makeCopy ? (byte[]) b.clone() : b);
	}

	/**
	 * The parameter to this method is the implementing class rather than the
	 * interface so we ensure that we get it right since we are making certain
	 * assumptions about the object type w.r.t. casting.
	 */
	public synchronized ObjectMap getSyncInfo(boolean makeCopy) {
		if (syncInfo == null)
			return null;
		return makeCopy ? (ObjectMap) syncInfo.clone() : syncInfo;
	}

	/** 
	 * Returns the sync information generation count.
	 * The count is incremented whenever sync info on the resource changes.
	 */
	public int getSyncInfoGenerationCount() {
		return syncInfoGenerationCount;
	}

	/** 
	 * Returns the type setting for this info.  Valid values are 
	 * FILE, FOLDER, PROJECT, 
	 */
	public int getType() {
		return getType(flags);
	}

	/** 
	 * Returns the type setting for this info.  Valid values are 
	 * FILE, FOLDER, PROJECT, 
	 */
	public static int getType(int flags) {
		return getBits(flags, M_TYPE, M_TYPE_START);
	}

	/** 
	 * Increments the charset generation count.
	 * The count is incremented whenever the encoding on the resource changes.
	 */
	public void incrementCharsetGenerationCount() {
		++charsetGenerationCount;
	}

	/** 
	 * Mark this resource info as having changed content
	 */
	public void incrementContentId() {
		contentId += 1;
	}

	/** 
	 * Increments the marker generation count.
	 * The count is incremented whenever markers on the resource change.
	 */
	public void incrementMarkerGenerationCount() {
		++markerGenerationCount;
	}

	/** 
	 * Increments the sync information generation count.
	 * The count is incremented whenever sync info on the resource changes.
	 */
	public void incrementSyncInfoGenerationCount() {
		++syncInfoGenerationCount;
	}

	/** 
	 * Returns true if all of the bits indicated by the mask are set.
	 */
	public boolean isSet(int mask) {
		return isSet(flags, mask);
	}

	/** 
	 * Returns true if all of the bits indicated by the mask are set.
	 */
	public static boolean isSet(int flags, int mask) {
		return (flags & mask) != 0;
	}

	public void readFrom(int flags, DataInput input) throws IOException {
		// The flags for this info are read by the visitor (flattener). 
		// See Workspace.readElement().  This allows the reader to look ahead 
		// and see what type of info is being loaded.
		this.flags = flags;
		localInfo = input.readLong();
		nodeId = input.readLong();
		contentId = input.readInt();
		modificationStamp = input.readLong();
	}

	/** 
	 * Sets all of the bits indicated by the mask.
	 */
	public void set(int mask) {
		flags |= mask;
	}

	/** 
	 * Sets the value of the indicated bits to be the given value.
	 */
	protected void setBits(int mask, int start, int value) {
		int baseMask = mask >> start;
		int newValue = (value & baseMask) << start;
		// thread safety: (guarantee atomicity)
		int temp = flags;
		temp &= ~mask;
		temp |= newValue;
		flags = temp;
	}

	/** 
	 * Sets the flags for this info.
	 */
	protected void setFlags(int value) {
		flags = value;
	}

	/** 
	 * Sets the local-relative sync information.
	 */
	public void setLocalSyncInfo(long info) {
		localInfo = info;
	}

	/** 
	 * Sets the collection of makers for this resource.
	 * <code>null</code> is passed in if there are no markers.
	 */
	public void setMarkers(MarkerSet value) {
		markers = value;
	}

	/** 
	 *
	 */
	public void setModificationStamp(long stamp) {
		modificationStamp = stamp;
	}

	/** 
	 *
	 */
	public void setNodeId(long id) {
		nodeId = id;
	}

	/**
	 * Sets the property store associated with this info.  The value may be null.
	 */
	public void setPropertyStore(PropertyStore value) {
		// needs to be implemented on subclasses
	}

	/** 
	 * Sets the identified session property to the given value.  If
	 * the value is null, the property is removed.
	 */
	public synchronized void setSessionProperty(QualifiedName name, Object value) {
		// thread safety: (Concurrency001)
		if (value == null) {
			if (sessionProperties == null)
				return;
			ObjectMap temp = (ObjectMap) sessionProperties.clone();
			temp.remove(name);
			if (temp.isEmpty())
				sessionProperties = null;
			else
				sessionProperties = temp;
		} else {
			ObjectMap temp = sessionProperties;
			if (temp == null)
				temp = new ObjectMap(5);
			else
				temp = (ObjectMap) sessionProperties.clone();
			temp.put(name, value);
			sessionProperties = temp;
		}
	}

	/**
	 * The parameter to this method is the implementing class rather than the
	 * interface so we ensure that we get it right since we are making certain
	 * assumptions about the object type w.r.t. casting.
	 */
	protected void setSyncInfo(ObjectMap syncInfo) {
		this.syncInfo = syncInfo;
	}

	public synchronized void setSyncInfo(QualifiedName id, byte[] value) {
		if (value == null) {
			//delete sync info
			if (syncInfo == null)
				return;
			syncInfo.remove(id);
			if (syncInfo.isEmpty())
				syncInfo = null;
		} else {
			//add sync info
			if (syncInfo == null)
				syncInfo = new ObjectMap(5);
			syncInfo.put(id, value.clone());
		}
	}

	/** 
	 * Sets the type for this info to the given value.  Valid values are 
	 * FILE, FOLDER, PROJECT
	 */
	public void setType(int value) {
		setBits(M_TYPE, M_TYPE_START, value);
	}

	public void writeTo(DataOutput output) throws IOException {
		// The flags for this info are written by the visitor (flattener). 
		// See SaveManager.writeElement().  This allows the reader to look ahead 
		// and see what type of info is being loaded.
		output.writeLong(localInfo);
		output.writeLong(nodeId);
		output.writeInt(contentId);
		output.writeLong(modificationStamp);
	}
}