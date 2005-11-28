package org.eclipse.ui.internal.provisional.ide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.jface.util.Assert;

/**
 * Provides the entries to show in the Open With menu, and indicates which entry
 * is the default.
 * 
 * @since 3.2
 */
public class OpenWithInfo {

	private OpenWithEntry[] entries;

	private OpenWithEntry preferredEntry, externalEntry, inPlaceEntry,
			defaultEntry;

	/**
	 * Creates an empty <code>OpenWithInfo</code>.
	 */
	public OpenWithInfo() {
		this(new OpenWithEntry[0], null, null, null, null);
	}

	/**
	 * Creates a new <code>OpenWithInfo</code> with the given entries.
	 * 
	 * @param entries
	 *            the regular entries
	 * @param preferredEntry
	 *            the preferred entry, or <code>null</code> if none
	 * @param externalEntry
	 *            the entry for the external system editor, or <code>null</code>
	 *            if none
	 * @param inPlaceEntry
	 *            the entry for the in-place system editor, or <code>null</code>
	 *            if none
	 * @param defaultEntry
	 *            the entry for Default (i.e. revert to factory defaults), or
	 *            <code>null</code> if none
	 */
	public OpenWithInfo(OpenWithEntry[] entries, OpenWithEntry preferredEntry,
			OpenWithEntry externalEntry, OpenWithEntry inPlaceEntry,
			OpenWithEntry defaultEntry) {
		Assert.isNotNull(entries);
		this.entries = entries;
		this.preferredEntry = preferredEntry;
		this.externalEntry = externalEntry;
		this.inPlaceEntry = inPlaceEntry;
		this.defaultEntry = defaultEntry;
	}

	/**
	 * Returns the entry for Default (i.e. revert to factory defaults) entry, or
	 * <code>null</code> if none.
	 * 
	 * @return the Default entry, or <code>null</code>
	 */
	public OpenWithEntry getDefaultEntry() {
		return defaultEntry;
	}

	/**
	 * Returns the regular entries.
	 * 
	 * @return the regular entries
	 */
	public OpenWithEntry[] getEntries() {
		return entries;
	}

	/**
	 * Returns the entry for the in-place system editor, or <code>null</code>
	 * if none.
	 * 
	 * @return the in-place entry, or <code>null</code>
	 */
	public OpenWithEntry getInPlaceEntry() {
		return inPlaceEntry;
	}

	/**
	 * Returns the preferred entry, or <code>null</code> if none. This entry
	 * is typically highlighted with bold or a radio button in the Open With
	 * menu.
	 * 
	 * @return the preferred entry, or <code>null</code>
	 */
	public OpenWithEntry getPreferredEntry() {
		return preferredEntry;
	}

	/**
	 * Returns the entry for the in-place external editor, or <code>null</code>
	 * if none.
	 * 
	 * @return the external entry, or <code>null</code>
	 */
	public OpenWithEntry getExternalEntry() {
		return externalEntry;
	}

	/**
	 * Returns a new <code>OpenWithInfo</code> which is the result of merging
	 * this <code>OpenWithInfo</code> with the given other
	 * <code>OpenWithInfo</code>.
	 * 
	 * @return the merged info
	 */
	public OpenWithInfo mergeWith(OpenWithInfo other) {
		HashSet seenDescriptors = new HashSet();
		OpenWithEntry[] thisEntries = getEntries();
		for (int i = 0; i < thisEntries.length; i++) {
			OpenWithEntry entry = thisEntries[i];
			if (entry.getEditorDescriptor() != null) {
				seenDescriptors.add(entry.getEditorDescriptor());
			}
		}
		OpenWithEntry[] otherEntries = other.getEntries();
		ArrayList mergedEntries = new ArrayList(thisEntries.length
				+ otherEntries.length);
		mergedEntries.addAll(Arrays.asList(thisEntries));
		for (int i = 0; i < otherEntries.length; i++) {
			OpenWithEntry entry = otherEntries[i];
			if (entry.getEditorDescriptor() != null
					&& !seenDescriptors.contains(entry.getEditorDescriptor())) {
				seenDescriptors.add(entry.getEditorDescriptor());
				mergedEntries.add(entry);
			}
		}
		HashSet allEntries = new HashSet(mergedEntries.size() + 3);
		allEntries.addAll(mergedEntries);

		OpenWithEntry externalEntry = getExternalEntry() != null ? getExternalEntry()
				: other.getExternalEntry();
		if (externalEntry != null) {
			allEntries.add(externalEntry);
		}
		OpenWithEntry inPlaceEntry = getInPlaceEntry() != null ? getInPlaceEntry()
				: other.getInPlaceEntry();
		if (inPlaceEntry != null) {
			allEntries.add(inPlaceEntry);
		}
		OpenWithEntry defaultEntry = getDefaultEntry() != null ? getDefaultEntry()
				: other.getDefaultEntry();
		if (defaultEntry != null) {
			allEntries.add(defaultEntry);
		}

		OpenWithEntry thisPreferredEntry = getPreferredEntry() != null
				&& allEntries.contains(getPreferredEntry()) ? getPreferredEntry()
				: null;
		OpenWithEntry otherPreferredEntry = other.getPreferredEntry() != null
				&& allEntries.contains(other.getPreferredEntry()) ? other
				.getPreferredEntry() : null;
		// if both have preferred entries that remain in the list, then claim no
		// preferred entry
		OpenWithEntry preferredEntry = thisPreferredEntry != null ? (otherPreferredEntry != null ? null
				: thisPreferredEntry)
				: otherPreferredEntry;

		OpenWithEntry[] mergedEntryArray = (OpenWithEntry[]) mergedEntries
				.toArray(new OpenWithEntry[mergedEntries.size()]);
		return new OpenWithInfo(mergedEntryArray, preferredEntry,
				externalEntry, inPlaceEntry, defaultEntry);
	}

}
