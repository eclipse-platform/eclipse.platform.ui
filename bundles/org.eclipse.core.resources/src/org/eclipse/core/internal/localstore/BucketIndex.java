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
	 * A entry in the bucket index. Each entry has one path and a collection
	 * of states, which by their turn contain a (UUID, timestamp) pair.  
	 */
	public static final class Entry {

		final static Comparator COMPARATOR = new Comparator() {
			public int compare(Object o1, Object o2) {
				byte[] state1 = (byte[]) o1;
				byte[] state2 = (byte[]) o2;
				return Entry.compareStates(state1, state2);
			}
		};

		private final static byte[][] EMPTY_DATA = new byte[0][];
		/**
		 * This entry has not been modified in any way so far.
		 * 
		 * @see #state
		 */
		private final static int STATE_CLEAR = 0;
		/**
		 * This entry has been requested for deletion.
		 * 
		 * @see #state
		 */
		private final static int STATE_DELETED = 0x02;
		/**
		 * This entry has been modified.
		 * 
		 * @see #state
		 */
		private final static int STATE_DIRTY = 0x01;
		// the length of a UUID in bytes
		private final static int UUID_LENGTH = UniversalUniqueIdentifier.BYTES_SIZE;
		// the length of a long in bytes
		private final static int LONG_LENGTH = 8;
		// the length of each component of the data array
		public final static int DATA_LENGTH = UUID_LENGTH + LONG_LENGTH;

		/**
		 * The history states. The first array dimension is the number of states. The
		 * second dimension is an encoding of the {UUID,timestamp} pair for that entry.
		 */
		private byte[][] data;
		/**
		 * Logical path of the object we are storing history for. This does not
		 * correspond to a file system path.
		 */
		private IPath path;

		/**
		 * State for this entry. Possible values are STATE_CLEAR, STATE_DIRTY and STATE_DELETED.
		 * 
		 * @see #STATE_CLEAR
		 * @see #STATE_DELETED
		 * @see #STATE_DIRTY
		 */
		private byte state = STATE_CLEAR;

		/**
		 * Comparison logic for states in byte[] form.
		 * 
		 * @see Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		static int compareStates(byte[] state1, byte[] state2) {
			long timestamp1 = getTimestamp(state1);
			long timestamp2 = getTimestamp(state2);
			if (timestamp1 == timestamp2)
				return -UniversalUniqueIdentifier.compareTime(state1, state2);
			return timestamp1 < timestamp2 ? +1 : -1;
		}

		/**
		 * Returns the byte array representation of a (UUID, timestamp) pair. 
		 */
		static byte[] getDataAsByteArray(byte[] uuid, long timestamp) {
			byte[] item = new byte[DATA_LENGTH];
			System.arraycopy(uuid, 0, item, 0, uuid.length);
			for (int j = 0; j < LONG_LENGTH; j++) {
				item[UUID_LENGTH + j] = (byte) (0xFF & timestamp);
				timestamp >>>= 8;
			}
			return item;
		}

		private static long getTimestamp(byte[] item) {
			long timestamp = 0;
			for (int j = 0; j < LONG_LENGTH; j++)
				timestamp += (item[UUID_LENGTH + j] & 0xFFL) << j * 8;
			return timestamp;
		}

		private static UniversalUniqueIdentifier getUUID(byte[] item) {
			return new UniversalUniqueIdentifier(item);
		}

		/**
		 * Merges two entries (are always sorted). Duplicates are discarded.
		 */
		static byte[][] merge(byte[][] base, byte[][] additions) {
			int additionPointer = 0;
			int basePointer = 0;
			int added = 0;
			byte[][] result = new byte[base.length + additions.length][];
			while (basePointer < base.length && additionPointer < additions.length) {
				int comparison = compareStates(base[basePointer], additions[additionPointer]);
				if (comparison == 0) {
					result[added++] = base[basePointer++];
					// duplicate, ignore
					additionPointer++;
				} else if (comparison < 0)
					result[added++] = base[basePointer++];
				else
					result[added++] = additions[additionPointer++];
			}
			// copy the remaining items from either additions or base arrays
			byte[][] remaining = basePointer == base.length ? additions : base;
			int remainingPointer = basePointer == base.length ? additionPointer : basePointer;
			int remainingCount = remaining.length - remainingPointer;
			System.arraycopy(remaining, remainingPointer, result, added, remainingCount);
			added += remainingCount;
			if (added == base.length + additions.length)
				// no collisions
				return result;
			// there were collisions, need to compact
			byte[][] finalResult = new byte[added][];
			System.arraycopy(result, 0, finalResult, 0, finalResult.length);
			return finalResult;
		}

		public Entry(IPath path, byte[][] data) {
			Assert.isNotNull(path);
			Assert.isNotNull(data);
			this.path = path;
			this.data = data;
		}

		/**
		 * Compacts the given array removing any null slots. If non-null slots
		 * are found, the entry is marked for removal. 
		 */
		void compact() {
			if (!isDirty())
				return;
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
				delete();
				return;
			}
			byte[][] result = new byte[occurrences][];
			System.arraycopy(data, 0, result, 0, occurrences);
			data = result;
		}

		public void delete() {
			state = STATE_DELETED;
		}

		public void deleteOccurrence(int i) {
			Assert.isTrue(state != STATE_DELETED);
			state = STATE_DIRTY;
			data[i] = null;
		}

		byte[][] getData() {
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

		public boolean isDeleted() {
			return state == STATE_DELETED;
		}

		public boolean isDirty() {
			return state == STATE_DIRTY;
		}

		public boolean isEmpty() {
			return data.length == 0;
		}
	}

	public abstract static interface Visitor {
		// should continue the traversal
		public final static int CONTINUE = 0;
		// should stop looking at states for files in this container (or any of its children)	
		public final static int RETURN = 2;
		// should stop the traversal	
		public final static int STOP = 1;

		/** 
		 * @return either STOP, CONTINUE or RETURN
		 */
		public int visit(Entry entry);
	}

	private static final String BUCKET = "bucket.index"; //$NON-NLS-1$

	/** Version number for the current implementation file's format.
	 * <p>
	 * Version 2: same as version 1, but states for an entry are already sorted
	 * </p>
	 * <p>
	 * Version 1:
	 * <pre>
	 * FILE ::= VERSION_ID ENTRY+
	 * ENTRY ::= STATE_COUNT STATE+
	 * STATE_COUNT ::= int
	 * STATE ::= UUID LAST_MODIFIED
	 * UUID	 ::= byte[16]
	 * LAST_MODIFIED ::= byte[8]
	 * </pre>
	 * </p>
	 */
	public final static byte VERSION = 1;

	/**
	 * Map of the history entries in this bucket. Maps (String -> byte[][]),
	 * where the key is the path of the object we are storing history for, and
	 * the value is the history entry data (UUID,timestamp) pairs.
	 */
	private final Map entries;
	/**
	 * The file system location of this bucket index file.
	 */
	private File location;
	/**
	 * Whether the in-memory bucket is dirty and needs saving
	 */
	private boolean needSaving = false;

	/**
	 * The root directory of the bucket indexes on disk.
	 */
	private File root;

	public BucketIndex(File root) {
		this.root = root;
		this.entries = new HashMap();
	}

	/**
	 * Applies the given visitor to this bucket index. 
	 * @param visitor
	 * @param filter
	 * @param exactMatch
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
				int outcome = visitor.visit(fileEntry);
				// compact the entry in case any changes have happened
				fileEntry.compact();
				if (fileEntry.isDeleted()) {
					needSaving = true;
					i.remove();
				} else if (fileEntry.isDirty()) {
					needSaving = true;
					entry.setValue(fileEntry.getData());
				}
				switch (outcome) {
					case Visitor.RETURN :
						// skip any other buckets under this
						return Visitor.RETURN;
					case Visitor.STOP :
						// stop looking
						return Visitor.STOP;
				}
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
		// look for the right spot where to insert the new guy
		int insertPosition;
		for (insertPosition = 0; insertPosition < existing.length; insertPosition++) {
			int result = Entry.compareStates(existing[insertPosition], item);
			if (result == 0)
				// already there - nothing else to be done
				return;
			if (result > 0)
				break;
		}
		byte[][] newValue = new byte[existing.length + 1][];
		if (insertPosition > 0)
			System.arraycopy(existing, 0, newValue, 0, insertPosition);
		newValue[insertPosition] = item;
		if (insertPosition < existing.length)
			System.arraycopy(existing, insertPosition, newValue, insertPosition + 1, existing.length - insertPosition);
		entries.put(pathAsString, newValue);
		needSaving = true;
	}

	public void addBlobs(Entry fileEntry) {
		IPath path = fileEntry.getPath();
		byte[][] additions = fileEntry.getData();
		String pathAsString = path.toString();
		byte[][] existing = (byte[][]) entries.get(pathAsString);
		if (existing == null) {
			entries.put(pathAsString, additions);
			needSaving = true;
			return;
		}
		entries.put(pathAsString, Entry.merge(existing, additions));
		needSaving = true;
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
				// TODO: remove this backward compatibility before M4
				boolean shouldSort = false;
				int version = source.readByte();
				// version 1 had the same format but states where not ordered
				if (version == 1) {
					shouldSort = true;
					// so it is converted to the new version
					needSaving = true;
				} else if (version != VERSION) {
					// unknown version
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
					if (shouldSort)
						Arrays.sort(uuids, Entry.COMPARATOR);
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
