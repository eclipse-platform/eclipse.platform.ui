/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.localstore;

import java.io.*;
import java.util.*;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.internal.resources.ResourceStatus;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.*;

public class BucketIndex {

	/**
	 * A entry in the bucket index. Each entry has one file path and a collection
	 * of states, which by their turn contain a (UUID, timestamp) pair.  
	 */
	public static final class Entry {
		private final static byte[][] EMPTY_DATA = new byte[0][];
		// the length of a long in bytes
		private final static int LONG_LENGTH = 8;
		// the length of a UUID in bytes
		private final static int UUID_LENGTH = UniversalUniqueIdentifier.BYTES_SIZE;
		// the length of each component of the data array
		public final static int DATA_LENGTH = UUID_LENGTH + LONG_LENGTH;		
		byte[][] data;
		IPath path;

		/**
		 * Returns the byte array representation of a (UUID, timestamp) pair. 
		 */
		public static byte[] getDataAsByteArray(byte[] uuid, long timestamp) {
			byte[] item = new byte[DATA_LENGTH];
			System.arraycopy(uuid, 0, item, 0, uuid.length);
			for (int j = 0; j < LONG_LENGTH; j++) {
				item[UUID_LENGTH + j] = (byte) (0xFF & timestamp);
				timestamp >>>= 8;
			}
			return item;
		}

		public static long getTimestamp(byte[] item) {
			long timestamp = 0;
			for (int j = 0; j < LONG_LENGTH; j++)
				timestamp += (item[UUID_LENGTH + j] & 0xFFL) << j * 8;
			return timestamp;
		}

		public static UniversalUniqueIdentifier getUUID(byte[] item) {
			return new UniversalUniqueIdentifier(item);
		}

		public static void sortStates(byte[][] data) {
			Arrays.sort(data, new Comparator() {
				// sort in inverse order
				public int compare(Object o1, Object o2) {
					byte[] state1 = (byte[]) o1;
					byte[] state2 = (byte[]) o2;
					long timestamp1 = getTimestamp(state1);
					long timestamp2 = getTimestamp(state2);
					if (timestamp1 == timestamp2)
						return -UniversalUniqueIdentifier.compareTime(state1, state2);
					return timestamp1 < timestamp2 ? +1 : -1;
				}
			});
		}

		public Entry(IPath path, byte[][] data) {
			Assert.isNotNull(path);
			Assert.isNotNull(data);
			this.path = path;
			this.data = data;
		}

		/**
		 * Compacts the given array removing any null slots.
		 */
		void compact() {
			int occurrences = 0;
			for (int i = 0; i < data.length; i++)
				if (data[i] != null)
					data[occurrences++] = data[i];
			if (occurrences == data.length)
				// no items deleted
				return;
			if (occurrences == 0) {
				// no items remaining
				data = EMPTY_DATA;
				return;
			}
			byte[][] result = new byte[occurrences][];
			System.arraycopy(data, 0, result, 0, occurrences);
			data = result;
		}

		public void deleteOccurrence(int i) {
			data[i] = null;
		}

		public byte[][] getData() {
			return getData(false);
		}

		public byte[][] getData(boolean clone) {
			if (!clone || isEmpty())
				return data;
			// don't need to clone the contained arrays because they immutable
			byte[][] newData = new byte[data.length][];
			System.arraycopy(data, 0, newData, 0, data.length);
			return newData;
		}

		public int getOccurrences() {
			return data.length;
		}

		public IPath getPath() {
			return path;
		}

		public long getTimestamp(int i) {
			return getTimestamp(data[i]);
		}

		public UniversalUniqueIdentifier getUUID(int i) {
			return getUUID(data[i]);
		}

		public boolean isEmpty() {
			return data.length == 0;
		}

		public void sortStates() {
			sortStates(this.data);
		}
	}

	public abstract static interface Visitor {
		// should continue the traversal
		public final static int CONTINUE = 0;
		// should delete this entry (can be combined with the other constants except for UPDATE)
		public final static int DELETE = 0x100;
		// should stop looking at states for files in this container (or any of its children)	
		public final static int RETURN = 2;
		// should stop the traversal	
		public final static int STOP = 1;
		// should update this entry (can be combined with the other constants except for DELETE)		
		public final static int UPDATE = 0x200;

		/** 
		 * @return either STOP, CONTINUE or RETURN and optionally DELETE or UPDATE
		 */
		public int visit(Entry entry);
	}

	private static final String BUCKET = "bucket.index"; //$NON-NLS-1$

	public final static byte VERSION = 1;

	//	private static final int UUID_LENGTH = new UniversalUniqueIdentifier().toString().length();

	//	private static RecyclableBufferedInputStream bufferedInputStream = new RecyclableBufferedInputStream();

	//	private static RecyclableBufferedOutputStream bufferedOutputStream = new RecyclableBufferedOutputStream();
	private Map entries;
	private File location;
	private boolean needSaving = false;

	private File root;

	private static int indexOf(byte[][] array, byte[] item, int length) {
		// look for existing occurrences
		for (int i = 0; i < array.length; i++) {
			boolean same = true;
			for (int j = 0; j < length; j++)
				if (item[j] != array[i][j]) {
					same = false;
					break;
				}
			if (same)
				return i;
		}
		return -1;
	}

	public BucketIndex(File root) {
		this.root = root;
		this.entries = new HashMap();
	}

	/**
	 * 
	 * @param visitor
	 * @param filter
	 * @return one of STOP, RETURN or CONTINUE constants
	 * @throws CoreException
	 */
	public int accept(Visitor visitor, IPath filter, boolean exactMatch) throws CoreException {
		if (entries.isEmpty())
			return Visitor.CONTINUE;
		try {
			for (Iterator i = entries.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				IPath path = new Path((String) entry.getKey());
				// check whether the filter applies
				if (!filter.isPrefixOf(path) || (exactMatch && !filter.equals(path)))
					continue;
				// calls the visitor passing all uuids for the entry
				final Entry fileEntry = new Entry(path, (byte[][]) entry.getValue());
				fileEntry.sortStates();
				int outcome = visitor.visit(fileEntry);
				if ((outcome & Visitor.UPDATE) != 0) {
					needSaving = true;
					fileEntry.compact();
					if (fileEntry.isEmpty())
						i.remove();
					else
						entry.setValue(fileEntry.getData());
				} else if ((outcome & Visitor.DELETE) != 0) {
					needSaving = true;
					i.remove();
				}
				if ((outcome & Visitor.RETURN) != 0)
					// skip any other buckets under this
					return Visitor.RETURN;
				if ((outcome & Visitor.STOP) != 0)
					// stop looking
					return Visitor.STOP;
			}
			return Visitor.CONTINUE;
		} finally {
			save();
		}
	}

	public void addBlob(IPath path, UniversalUniqueIdentifier uuid, long lastModified) {
		byte[] item = Entry.getDataAsByteArray(uuid.toBytes(), lastModified);
		String pathAsString = path.toString();
		byte[][] existing = (byte[][]) entries.get(pathAsString);
		if (existing == null) {
			entries.put(pathAsString, new byte[][] {item});
			needSaving = true;
			return;
		}
		// look for existing occurrences
		if (contains(existing, item))
			// already there - nothing else to be done
			return;
		byte[][] newValue = new byte[existing.length + 1][];
		System.arraycopy(existing, 0, newValue, 0, existing.length);
		newValue[newValue.length - 1] = item;
		entries.put(pathAsString, newValue);
		needSaving = true;
	}

	public void addBlobs(Entry fileEntry) {
		IPath path = fileEntry.getPath();
		byte[][] data = fileEntry.getData();
		String pathAsString = path.toString();
		byte[][] existing = (byte[][]) entries.get(pathAsString);
		if (existing == null) {
			entries.put(pathAsString, data);
			needSaving = true;
			return;
		}
		// add after looking for existing occurrences
		List newUUIDs = new ArrayList(existing.length + data.length);
		for (int i = 0; i < data.length; i++)
			if (!contains(existing, data[i]))
				newUUIDs.add(data[i]);
		if (newUUIDs.isEmpty())
			// none added
			return;
		byte[][] newValue = new byte[existing.length + newUUIDs.size()][];
		newUUIDs.toArray(newValue);
		System.arraycopy(existing, 0, newValue, newUUIDs.size(), existing.length);
		entries.put(pathAsString, newValue);
		needSaving = true;
	}

	private boolean contains(byte[][] array, byte[] item) {
		return indexOf(array, item, UniversalUniqueIdentifier.BYTES_SIZE) >= 0;
	}

	/**
	 * Tries to delete as many empty levels as possible.
	 */
	private void delete(File toDelete) {
		// don't try to delete beyond the root for bucket indexes
		if (toDelete.equals(root))
			return;
		if (toDelete.delete())
			// if deletion went fine, try deleting the parent dir			
			delete(toDelete.getParentFile());
	}

	public Entry getEntry(IPath path) {
		String pathAsString = path.toString();
		byte[][] existing = (byte[][]) entries.get(pathAsString);
		if (existing == null)
			return null;
		return new Entry(path, existing);
	}

	public int getEntryCount() {
		return entries.size();
	}

	File getLocation() {
		return location == null ? null : location.getParentFile();
	}

	public void load(File baseLocation) throws CoreException {
		load(baseLocation, false);
	}

	public void load(File baseLocation, boolean force) throws CoreException {
		try {
			// avoid reloading
			if (!force && this.location != null && baseLocation.equals(this.location.getParentFile()))
				return;
			// previously loaded bucket may not have been saved... save before loading new one
			save();
			this.location = new File(baseLocation, BUCKET);
			this.entries.clear();
			if (!this.location.isFile())
				return;
			DataInputStream source = new DataInputStream(new BufferedInputStream(new FileInputStream(location), 8192));
			try {
				int version = source.readByte();
				if (version != VERSION) {
					String message = Policy.bind("resources.readMetaWrongVersion", location.getAbsolutePath(), Integer.toString(version)); //$NON-NLS-1$
					ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_READ_METADATA, message);
					throw new ResourceException(status);
				}
				int entryCount = source.readInt();
				for (int i = 0; i < entryCount; i++) {
					String key = source.readUTF();
					int length = source.readUnsignedShort();
					byte[][] uuids = new byte[length][Entry.DATA_LENGTH];
					for (int j = 0; j < uuids.length; j++)
						source.read(uuids[j]);
					this.entries.put(key, uuids);
				}
			} finally {
				source.close();
			}
		} catch (IOException ioe) {
			String message = Policy.bind("resources.readMeta", location.getAbsolutePath()); //$NON-NLS-1$
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_READ_METADATA, null, message, ioe);
			throw new ResourceException(status);
		}
	}

	public void save() throws CoreException {
		if (!needSaving)
			return;
		try {
			if (entries.isEmpty()) {
				needSaving = false;
				delete(location);
				return;
			}
			// ensure the parent location exists 
			location.getParentFile().mkdirs();
			DataOutputStream destination = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(location), 8192));
			try {
				destination.write(VERSION);
				destination.writeInt(entries.size());
				for (Iterator i = entries.entrySet().iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry) i.next();
					destination.writeUTF((String) entry.getKey());
					byte[][] uuids = (byte[][]) entry.getValue();
					destination.writeShort(uuids.length);
					for (int j = 0; j < uuids.length; j++)
						destination.write(uuids[j]);
				}
			} finally {
				destination.close();
			}
			needSaving = false;
		} catch (IOException ioe) {
			String message = Policy.bind("resources.writeMeta", location.getAbsolutePath()); //$NON-NLS-1$
			ResourceStatus status = new ResourceStatus(IResourceStatus.FAILED_WRITE_METADATA, null, message, ioe);
			throw new ResourceException(status);
		}
	}
}
