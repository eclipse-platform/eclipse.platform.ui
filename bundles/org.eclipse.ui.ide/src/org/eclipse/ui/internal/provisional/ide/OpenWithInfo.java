package org.eclipse.ui.internal.provisional.ide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.jface.util.Assert;

/**
 * Indicates the editors that can be used to open a model element, and which
 * editor is the default.
 * 
 * @since 3.2
 */
public class OpenWithInfo {

	private OpenWithEntry[] entries;

	private OpenWithEntry preferredEntry, externalEntry, inPlaceEntry, defaultEntry;

	public OpenWithInfo() {
		this(new OpenWithEntry[0], null, null, null, null);
	}

	public OpenWithInfo(OpenWithEntry[] entries, OpenWithEntry preferredEntry, OpenWithEntry externalEntry, OpenWithEntry inPlaceEntry, OpenWithEntry defaultEntry) {
		Assert.isNotNull(entries);
		this.entries = entries;
		this.preferredEntry = preferredEntry;
		this.externalEntry = externalEntry;
		this.inPlaceEntry = inPlaceEntry;
		this.defaultEntry = defaultEntry;
	}

	public OpenWithEntry getDefaultEntry() {
		return defaultEntry;
	}

	public OpenWithEntry[] getEntries() {
		return entries;
	}

	public OpenWithEntry getInPlaceEntry() {
		return inPlaceEntry;
	}

	public OpenWithEntry getPreferredEntry() {
		return preferredEntry;
	}

	public OpenWithEntry getExternalEntry() {
		return externalEntry;
	}

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
		ArrayList mergedEntries = new ArrayList(thisEntries.length + otherEntries.length);
		mergedEntries.addAll(Arrays.asList(thisEntries));
		for (int i = 0; i < otherEntries.length; i++) {
			OpenWithEntry entry = otherEntries[i];
			if (entry.getEditorDescriptor() != null && !seenDescriptors.contains(entry.getEditorDescriptor())) {
				seenDescriptors.add(entry.getEditorDescriptor());
				mergedEntries.add(entry);
			}
		}
		HashSet allEntries = new HashSet(mergedEntries.size() + 3);
		allEntries.addAll(mergedEntries);
		
		OpenWithEntry externalEntry = getExternalEntry() != null ? getExternalEntry() : other.getExternalEntry();
		if (externalEntry != null) {
			allEntries.add(externalEntry);
		}
		OpenWithEntry inPlaceEntry = getInPlaceEntry() != null ? getInPlaceEntry() : other.getInPlaceEntry();
		if (inPlaceEntry != null) {
			allEntries.add(inPlaceEntry);
		}
		OpenWithEntry defaultEntry = getDefaultEntry() != null ? getDefaultEntry() : other.getDefaultEntry();
		if (defaultEntry != null) {
			allEntries.add(defaultEntry);
		}
		
		OpenWithEntry thisPreferredEntry = getPreferredEntry() != null && allEntries.contains(getPreferredEntry()) ? getPreferredEntry() : null;
		OpenWithEntry otherPreferredEntry = other.getPreferredEntry() != null && allEntries.contains(other.getPreferredEntry()) ? other.getPreferredEntry() : null;
		// if both have preferred entries that remain in the list, then claim no preferred entry 
		OpenWithEntry preferredEntry = thisPreferredEntry != null ? (otherPreferredEntry != null ? null : thisPreferredEntry) : otherPreferredEntry;
		
		OpenWithEntry[] mergedEntryArray = (OpenWithEntry[]) mergedEntries.toArray(new OpenWithEntry[mergedEntries.size()]);
		return new OpenWithInfo(mergedEntryArray, preferredEntry, externalEntry, inPlaceEntry, defaultEntry);
	}

}
