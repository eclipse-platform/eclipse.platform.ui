package org.eclipse.team.internal.ccvs.ui.wizards;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * Helper class to compute change sets for keyword substitution modes.
 */
class KSubstChangeSet {
	public static final int ADDED_FILES = 1;
	public static final int CHANGED_FILES = 2;
	public static final int UNCHANGED_FILES = 4;

	private final IResource[] resources;
	private final int depth;
	private Set /* of IFile */ files = null;
	private Map /* from IFile to KSubstOption */ addedFiles = new HashMap();
	private Map /* from IFile to KSubstOption */ changedFiles = new HashMap();
	private Map /* from IFile to KSubstOption */ unchangedFiles = new HashMap();
	private boolean computed = false;
	private KSubstOption ksubst = null;

	/**
	 * Creates a new empty change set for the specified resources.
	 * 
	 * @param resources the resources to consider
	 * @param depth the recursion depth
	 */
	public KSubstChangeSet(IResource[] resources, int depth) {
		this.resources = resources;
		this.depth = depth;
	}

	/**
	 * Computes the lists of files that must have their keyword substitution
	 * mode changed, classified into three categories: added (but not yet committed),
	 * changed, unchanged.
	 *
	 * @param ksubst the desired keyword substitution mode, if null chooses for each file:
	 *         <code>KSubstOption.fromPattern(fileName).isBinary() ? KSUBST_BINARY : KSUBST_TEXT</code>
	 */
	public void computeAffectedFiles(KSubstOption ksubst) throws TeamException {
		// already computed this?
		if (computed && this.ksubst == ksubst) return;
		this.ksubst = ksubst;
		this.computed = true;
		// clear stored data
		addedFiles.clear();
		changedFiles.clear();
		unchangedFiles.clear();
		if (files == null) files = getAllFiles(resources, depth);
		// iterate over all files and determine the appropriate classifications
		for (Iterator it = files.iterator(); it.hasNext();) {
			IFile file = (IFile) it.next();
			ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(file);
			if (cvsFile.isManaged()) {
				// check if the keyword substitution mode needs to be changed
				ResourceSyncInfo info = cvsFile.getSyncInfo();
				KSubstOption fromKSubst = KSubstOption.fromMode(info.getKeywordMode());
				KSubstOption toKSubst = ksubst;
				if (ksubst == null) {
					toKSubst = KSubstOption.fromPattern(file.getName());
				}
				if (! toKSubst.equals(fromKSubst)) {
					// classify the change
					if (info.isAdded()) {
						addedFiles.put(file, toKSubst);
					} else if (info.isDeleted()) {
						// ignore deletions
					} else if (cvsFile.isModified()) {
						changedFiles.put(file, toKSubst);
					} else {
						unchangedFiles.put(file, toKSubst);
					}
				}
			}
		}
	}
	
	/**
	 * Returns an unmodifiable map describing the changes to be performed.
	 *
	 * @return a Map from IFile to KSubstOption
	 */
	public Map getChangeSet(int type) {
		Map map;
		switch (type) {
			case ADDED_FILES:
				map = addedFiles;
				break;
			case CHANGED_FILES:
				map = changedFiles;
				break;
			case UNCHANGED_FILES:
				map = unchangedFiles;
				break;
			default:
				map = new HashMap();
				if ((type & ADDED_FILES) != 0) map.putAll(addedFiles);
				if ((type & CHANGED_FILES) != 0) map.putAll(changedFiles);
				if ((type & UNCHANGED_FILES) != 0) map.putAll(unchangedFiles);
		}
		return Collections.unmodifiableMap(map);
	}
	
	/**
	 * Returns an unmodifiable collection of all files that will be changed.
	 * 
	 * @return a Collection of IFile
	 */
	public Collection getFileSet(int type) {
		Collection collection;
		switch (type) {
			case ADDED_FILES:
				collection = addedFiles.keySet();
				break;
			case CHANGED_FILES:
				collection = changedFiles.keySet();
				break;
			case UNCHANGED_FILES:
				collection = unchangedFiles.keySet();
				break;
			default:
				collection = new ArrayList();
				if ((type & ADDED_FILES) != 0) collection.addAll(addedFiles.keySet());
				if ((type & CHANGED_FILES) != 0) collection.addAll(changedFiles.keySet());
				if ((type & UNCHANGED_FILES) != 0) collection.addAll(unchangedFiles.keySet());
		}
		return Collections.unmodifiableCollection(collection);
	}

	/*
	 * Returns a set of all files encountered during the traversal.
	 */
	private static Set getAllFiles(IResource[] resources, int depth) throws TeamException {
		final Set /* of IFile */ files = new HashSet();
		for (int i = 0; i < resources.length; i++) {
			final IResource currentResource = resources[i];
			try {
				currentResource.accept(new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						if (resource.getType() == IResource.FILE) {
							files.add(resource);
						}
						// always return true and let the depth determine if children are visited
						return true;
					}
				}, depth, false);
			} catch (CoreException e) {
				throw new CVSException(new Status(IStatus.ERROR, CVSProviderPlugin.ID,
					TeamException.UNABLE, Policy.bind("CVSTeamProvider.visitError", //$NON-NLS-1$
					new Object[] { currentResource.getFullPath() }), e));
			}
		}
		return files;
	}
}
