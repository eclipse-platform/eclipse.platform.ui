package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProvider;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSListener;
import org.eclipse.team.internal.ccvs.core.ICVSProvider;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.client.Command;

/**
 * This class is repsible for maintaining the UI's list of known repositories,
 * and a list of known tags within each of those repositories.
 * 
 * It also provides a number of useful methods for assisting in repository operations.
 */
public class RepositoryManager {
	private static final String STATE_FILE = ".repositoryManagerState"; //$NON-NLS-1$
	private static final int STATE_FILE_VERSION_1 = -1;
	
	// Map ICVSRepositoryLocation -> List of CVSTag
	Hashtable branchTags = new Hashtable();
	// Map ICVSRepositoryLocation -> Hashtable of (Project name -> Set of CVSTag)
	Hashtable versionTags = new Hashtable();
	// Map ICVSRepositoryLocation -> Hashtable of (Project name -> Set of file paths that are project relative)
	Hashtable autoRefreshFiles = new Hashtable();
	
	List listeners = new ArrayList();

	// The previously remembered comment
	private static String previousComment = ""; //$NON-NLS-1$
	
	public static boolean notifyRepoView = true;
	
	/**
	 * Answer an array of all known remote roots.
	 */
	public ICVSRepositoryLocation[] getKnownRoots() {
		return getCVSProvider().getKnownRepositories();
	}
	
	/**
	 * Get the list of known branch tags for a given remote root.
	 */
	public CVSTag[] getKnownBranchTags(ICVSFolder project) {
		ICVSRepositoryLocation location = getRepositoryLocationFor(project);
		return getKnownBranchTags(location);
	}

	public CVSTag[] getKnownBranchTags(ICVSRepositoryLocation location) {
		Set set = (Set)branchTags.get(location);
		if (set == null) return new CVSTag[0];
		return (CVSTag[])set.toArray(new CVSTag[0]);
	}

	/**
	 * Get the list of known version tags for a given project.
	 */
	public CVSTag[] getKnownVersionTags(ICVSFolder project) {
		ICVSRepositoryLocation location = getRepositoryLocationFor(project);
		Set result = new HashSet();
		Hashtable table = (Hashtable)versionTags.get(location);
		if (table == null) {
			return (CVSTag[])result.toArray(new CVSTag[result.size()]);
		}
		Set set = (Set)table.get(project.getName());
		if (set == null) {
			return (CVSTag[])result.toArray(new CVSTag[result.size()]);
		}
		result.addAll(set);
		return (CVSTag[])result.toArray(new CVSTag[0]);
	}
	
	public Map getKnownProjectsAndVersions(ICVSRepositoryLocation location) {
		return (Hashtable)versionTags.get(location);
	}
		
	/*
	 * Fetches tags from .project and .vcm_meta if they exist. Then fetches tags from the user defined auto-refresh file
	 * list. The fetched tags are cached in the CVS ui plugin's tag cache.
	 */
	public void refreshDefinedTags(ICVSFolder project, boolean notify) throws TeamException {
		try {
			ICVSRepositoryLocation location = CVSProvider.getInstance().getRepository(project.getFolderSyncInfo().getRoot());
			List tags = new ArrayList();
			List filesToRefresh = new ArrayList(Arrays.asList(getAutoRefreshFiles(project)));
			filesToRefresh.add(".project"); //$NON-NLS-1$
			filesToRefresh.add(".vcm_meta"); //$NON-NLS-1$
			for (Iterator it = filesToRefresh.iterator(); it.hasNext();) {
				String relativePath = (String)it.next();
				ICVSFile file = null;
				if(project instanceof ICVSRemoteFolder) {
					// There should be a better way of doing this.
					ICVSRemoteFolder parentFolder = location.getRemoteFolder(new Path(project.getName()).append(relativePath).removeLastSegments(1).toString(), CVSTag.DEFAULT);
					ICVSResource[] resources = parentFolder.fetchChildren(null);
					for (int i = 0; i < resources.length; i++) {
						if (resources[i] instanceof ICVSRemoteFile && resources[i].getName().equals(new Path(relativePath).lastSegment())) {
							file = (ICVSFile)resources[i];
						}
					}
				} else {
					file = project.getFile(relativePath);
				}
				if(file!=null) {
					tags.addAll(Arrays.asList(fetchDefinedTagsFor(file, project, location)));
				}
			}
			// add all tags in one pass so that the listeners only get one notification for
			// versions and another for branches
			List branches = new ArrayList();
			List versions = new ArrayList();
			for (Iterator it = tags.iterator(); it.hasNext();) {
				CVSTag element = (CVSTag) it.next();
				if(element.getType()==CVSTag.BRANCH) {
					branches.add(element);
				} else {
					versions.add(element);
				}
			}
			
			// XXX Back HACK for optimizing refresing of repo view
			notifyRepoView = false;
			addBranchTags(project, (CVSTag[]) branches.toArray(new CVSTag[branches.size()]));
			if(notify) {
				notifyRepoView = true;
			}
			addVersionTags(project, (CVSTag[]) versions.toArray(new CVSTag[versions.size()]));
			notifyRepoView = true;
		} catch(CVSException e) {
			throw new TeamException(e.getStatus());
		}
	}
	
	/**
	 * Accept branch tags for any CVS resource. However, for the time being,
	 * the given branch tags are added to the list of known tags for the resource's
	 * remote root.
	 */
	public void addBranchTags(ICVSResource resource, CVSTag[] tags) {
		ICVSRepositoryLocation location = getRepositoryLocationFor(resource);
		addBranchTags(location, tags);
	}
	
	public void addBranchTags(ICVSRepositoryLocation location, CVSTag[] tags) {
		Set set = (Set)branchTags.get(location);
		if (set == null) {
			set = new HashSet();
			branchTags.put(location, set);
		}
		for (int i = 0; i < tags.length; i++) {
			set.add(tags[i]);
		}
		Iterator it = listeners.iterator();
		while (it.hasNext() && notifyRepoView) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.branchTagsAdded(tags, location);
		}
	}
	
	/**
	 * A repository root has been added. Notify any listeners.
	 */
	public void rootAdded(ICVSRepositoryLocation root) {
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.repositoryAdded(root);
		}
	}
	
	/**
	 * A repository root has been removed.
	 * Remove the tags defined for this root and notify any listeners
	 */
	public void rootRemoved(ICVSRepositoryLocation root) {
		CVSTag[] branchTags = getKnownBranchTags(root);
		Hashtable vTags = (Hashtable)this.versionTags.get(root);
		this.branchTags.remove(root);
		this.versionTags.remove(root);
		this.autoRefreshFiles.remove(root);
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.branchTagsRemoved(branchTags, root);
			if (vTags != null) {
				Iterator keyIt = vTags.keySet().iterator();
				while (keyIt.hasNext()) {
					String projectName = (String)keyIt.next();
					Set tagSet = (Set)vTags.get(projectName);
					CVSTag[] versionTags = (CVSTag[])tagSet.toArray(new CVSTag[0]);
					listener.versionTagsRemoved(versionTags, root);
				}
			}
			listener.repositoryRemoved(root);
		}
	}
	
	/**
	 * Accept version tags for any CVS resource. However, for the time being,
	 * the given version tags are added to the list of known tags for the 
	 * remote ancestor of the resource that is a direct child of the remote root
	 */
	public void addVersionTags(ICVSResource resource, CVSTag[] tags) {
		try {
			
			// Make sure there is a version tag table for the location
			ICVSRepositoryLocation location = getRepositoryLocationFor(resource);
			Hashtable table = (Hashtable)versionTags.get(location);
			if (table == null) {
				table = new Hashtable();
				versionTags.put(location, table);
			}
			
			// Get the name to cache the version tags with
			ICVSFolder parent;
			if (resource.isFolder()) {
				parent = (ICVSFolder)resource;
			} else {
				parent = resource.getParent();
			}
			if ( ! parent.isCVSFolder()) return;
			String name = new Path(parent.getFolderSyncInfo().getRepository()).segment(0);
			
			// Make sure there is a table for the ancestor that holds the tags
			Set set = (Set)table.get(name);
			if (set == null) {
				set = new HashSet();
				table.put(name, set);
			}
			
			// Store the tag with the appropriate ancestor
			for (int i = 0; i < tags.length; i++) {
				set.add(tags[i]);
			}
			
			// Notify any listeners
			Iterator it = listeners.iterator();
			while (it.hasNext() && notifyRepoView) {
				IRepositoryListener listener = (IRepositoryListener)it.next();
				listener.versionTagsAdded(tags, location);
			}
		} catch (CVSException e) {
			CVSUIPlugin.log(e.getStatus());
		}
	}
	
	public void addAutoRefreshFiles(ICVSFolder project, String[] relativeFilePaths) {
		ICVSRepositoryLocation location = getRepositoryLocationFor(project);
		Hashtable table = (Hashtable)autoRefreshFiles.get(location);
		if (table == null) {
			table = new Hashtable();
			autoRefreshFiles.put(location, table);
		}
		Set set = (Set)table.get(project.getName());
		if (set == null) {
			set = new HashSet();
			table.put(project.getName(), set);
		}
		for (int i = 0; i < relativeFilePaths.length; i++) {
			set.add(relativeFilePaths[i]);
		}
	}
	
	public void removeAutoRefreshFiles(ICVSFolder project, String[] relativeFilePaths) {
		ICVSRepositoryLocation location = getRepositoryLocationFor(project);
		Hashtable table = (Hashtable)autoRefreshFiles.get(location);
		if (table == null) return;
		Set set = (Set)table.get(project.getName());
		if (set == null) return;
		for (int i = 0; i < relativeFilePaths.length; i++) {
			set.remove(relativeFilePaths[i]);
		}
	}
	
	public String[] getAutoRefreshFiles(ICVSFolder project) {
		ICVSRepositoryLocation location = getRepositoryLocationFor(project);
		Set result = new HashSet();
		Hashtable table = (Hashtable)autoRefreshFiles.get(location);
		if (table == null) {
			return (String[])result.toArray(new String[result.size()]);
		}
		Set set = (Set)table.get(project.getName());
		if (set == null) {
			return (String[])result.toArray(new String[result.size()]);
		}
		result.addAll(set);
		return (String[])result.toArray(new String[0]);
	}
	
	/**
	 * Remove the given branch tag from the list of known tags for the
	 * given remote root.
	 */
	public void removeBranchTag(ICVSFolder project, CVSTag[] tags) {
		ICVSRepositoryLocation location = getRepositoryLocationFor(project);
		removeBranchTag(location, tags);
	}
	
	public void removeBranchTag(ICVSRepositoryLocation location, CVSTag[] tags) {
		Set set = (Set)branchTags.get(location);
		if (set == null) return;
		for (int i = 0; i < tags.length; i++) {
			set.remove(tags[i]);
		}
		Iterator it = listeners.iterator();
		while (it.hasNext() && notifyRepoView) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.branchTagsRemoved(tags, location);
		}
	}
	
	/**
	 * Remove the given tags from the list of known tags for the
	 * given remote root.
	 */
	public void removeVersionTags(ICVSFolder project, CVSTag[] tags) {
		ICVSRepositoryLocation location = getRepositoryLocationFor(project);
		Hashtable table = (Hashtable)versionTags.get(location);
		if (table == null) return;
		Set set = (Set)table.get(project.getName());
		if (set == null) return;
		for (int i = 0; i < tags.length; i++) {
			set.remove(tags[i]);
		}
		Iterator it = listeners.iterator();
		while (it.hasNext() && notifyRepoView) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.versionTagsRemoved(tags, location);
		}
	}
	
	public void startup() throws TeamException {
		loadState();
		CVSProviderPlugin.getProvider().addRepositoryListener(new ICVSListener() {
			public void repositoryAdded(ICVSRepositoryLocation root) {
				rootAdded(root);
			}
			public void repositoryRemoved(ICVSRepositoryLocation root) {
				rootRemoved(root);
			}
		});
	}
	
	public void shutdown() throws TeamException {
		saveState();
	}
	
	private void loadState() throws TeamException {
		IPath pluginStateLocation = CVSUIPlugin.getPlugin().getStateLocation().append(STATE_FILE);
		File file = pluginStateLocation.toFile();
		if (file.exists()) {
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(file));
				try {
					readState(dis);
				} finally {
					dis.close();
				}
			} catch (IOException e) {
				CVSUIPlugin.log(new Status(Status.ERROR, CVSUIPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.ioException"), e)); //$NON-NLS-1$
			} catch (TeamException e) {
				CVSUIPlugin.log(e.getStatus());
			}
		}
	}
	
	private void saveState() throws TeamException {
		IPath pluginStateLocation = CVSUIPlugin.getPlugin().getStateLocation();
		File tempFile = pluginStateLocation.append(STATE_FILE + ".tmp").toFile(); //$NON-NLS-1$
		File stateFile = pluginStateLocation.append(STATE_FILE).toFile();
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
			try {
				writeState(dos);
			} finally {
				dos.close();
			}
			if (stateFile.exists()) {
				stateFile.delete();
			}
			boolean renamed = tempFile.renameTo(stateFile);
			if (!renamed) {
				throw new TeamException(new Status(Status.ERROR, CVSUIPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.rename", tempFile.getAbsolutePath()), null)); //$NON-NLS-1$
			}
		} catch (IOException e) {
			throw new TeamException(new Status(Status.ERROR, CVSUIPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.save",stateFile.getAbsolutePath()), e)); //$NON-NLS-1$
		}
	}
	private void writeState(DataOutputStream dos) throws IOException {
		// Write the repositories
		Collection repos = Arrays.asList(getKnownRoots());
		// Write out version number for file.
		// We write it as an int so we can read either the repoSize or the version in the readState
		// XXX We should come up with a more long term solution.
		dos.writeInt(STATE_FILE_VERSION_1);
		dos.writeInt(repos.size());
		Iterator it = repos.iterator();
		while (it.hasNext()) {
			ICVSRepositoryLocation root = (ICVSRepositoryLocation)it.next();
			dos.writeUTF(root.getLocation());
			CVSTag[] branchTags = getKnownBranchTags(root);
			dos.writeInt(branchTags.length);
			for (int i = 0; i < branchTags.length; i++) {
				dos.writeUTF(branchTags[i].getName());
				dos.writeInt(branchTags[i].getType());
			}
			// write number of projects for which there are tags in this root
			Hashtable table = (Hashtable)versionTags.get(root);
			if (table == null) {
				dos.writeInt(0);
			} else {
				dos.writeInt(table.size());
				// for each project, write the name of the project, number of tags, and each tag.
				Iterator projIt = table.keySet().iterator();
				while (projIt.hasNext()) {
					String name = (String)projIt.next();
					dos.writeUTF(name);
					Set tagSet = (Set)table.get(name);
					dos.writeInt(tagSet.size());
					Iterator tagIt = tagSet.iterator();
					while (tagIt.hasNext()) {
						CVSTag tag = (CVSTag)tagIt.next();
						dos.writeUTF(tag.getName());
					}
				}
			}
			// write number of projects for which there were customized auto refresh files
			table = (Hashtable)autoRefreshFiles.get(root);
			if (table == null) {
				dos.writeInt(0);
			} else {
				dos.writeInt(table.size());
				// for each project, write the name of the project, number of filename then each file name
				Iterator projIt = table.keySet().iterator();
				while (projIt.hasNext()) {
					String name = (String)projIt.next();
					dos.writeUTF(name);
					Set tagSet = (Set)table.get(name);
					dos.writeInt(tagSet.size());
					Iterator filenameIt = tagSet.iterator();
					while (filenameIt.hasNext()) {
						String filename = (String)filenameIt.next();
						dos.writeUTF(filename);
					}
				}
			}
		}
	}
	private void readState(DataInputStream dis) throws IOException, TeamException {
		int repoSize = dis.readInt();
		boolean version1 = false;
		if (repoSize == STATE_FILE_VERSION_1) {
			version1 = true;
			repoSize = dis.readInt();
		}
		for (int i = 0; i < repoSize; i++) {
			ICVSRepositoryLocation root = CVSProviderPlugin.getProvider().getRepository(dis.readUTF());
			
			// read branch tags associated with this root
			int tagsSize = dis.readInt();
			CVSTag[] branchTags = new CVSTag[tagsSize];
			for (int j = 0; j < tagsSize; j++) {
				String tagName = dis.readUTF();
				int tagType = dis.readInt();
				branchTags[j] = new CVSTag(tagName, tagType);
			}
			addBranchTags(root, branchTags);
			
			// read the number of projects for this root that have version tags
			int projSize = dis.readInt();
			if (projSize > 0) {
				Hashtable projTable = new Hashtable();
				versionTags.put(root, projTable);
				for (int j = 0; j < projSize; j++) {
					String name = dis.readUTF();
					Set tagSet = new HashSet();
					projTable.put(name, tagSet);
					int numTags = dis.readInt();
					for (int k = 0; k < numTags; k++) {
						tagSet.add(new CVSTag(dis.readUTF(), CVSTag.VERSION));
					}
					Iterator it = listeners.iterator();
					while (it.hasNext()) {
						IRepositoryListener listener = (IRepositoryListener)it.next();
						listener.versionTagsAdded((CVSTag[])tagSet.toArray(new CVSTag[0]), root);
					}
				}
			}
			// read the auto refresh filenames for this project
			if (version1) {
				try {
					projSize = dis.readInt();
					if (projSize > 0) {
						Hashtable autoRefreshTable = new Hashtable();
						autoRefreshFiles.put(root, autoRefreshTable);
						for (int j = 0; j < projSize; j++) {
							String name = dis.readUTF();
							Set filenames = new HashSet();
							autoRefreshTable.put(name, filenames);
							int numFilenames = dis.readInt();
							for (int k = 0; k < numFilenames; k++) {
								filenames.add(dis.readUTF());
							}
						}
					}
				} catch (EOFException e) {
					// auto refresh files are not persisted, continue and save them next time.
				}
			}
		}
	}
	
	public void addRepositoryListener(IRepositoryListener listener) {
		listeners.add(listener);
	}
	
	public void removeRepositoryListener(IRepositoryListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Add the given resources to their associated providers.
	 * This schedules the resources for addition; they still need to be committed.
	 */
	public void add(IResource[] resources, IProgressMonitor monitor) throws TeamException {
		Hashtable table = getProviderMapping(resources);
		Set keySet = table.keySet();
		monitor.beginTask("", keySet.size() * 1000); //$NON-NLS-1$
		monitor.setTaskName(Policy.bind("RepositoryManager.adding")); //$NON-NLS-1$
		Iterator iterator = keySet.iterator();
		while (iterator.hasNext()) {
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
			CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
			provider.setComment(previousComment);
			List list = (List)table.get(provider);
			IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
			provider.add(providerResources, IResource.DEPTH_ZERO, subMonitor);
		}		
	}
	
	/**
	 * Delete the given resources from their associated providers.
	 * This schedules the resources for deletion; they still need to be committed.
	 */
	public void delete(IResource[] resources, IProgressMonitor monitor) throws TeamException {
		Hashtable table = getProviderMapping(resources);
		Set keySet = table.keySet();
		monitor.beginTask("", keySet.size() * 1000); //$NON-NLS-1$
		monitor.setTaskName(Policy.bind("RepositoryManager.deleting")); //$NON-NLS-1$
		Iterator iterator = keySet.iterator();
		while (iterator.hasNext()) {
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
			CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
			provider.setComment(previousComment);
			List list = (List)table.get(provider);
			IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
			provider.delete(providerResources, subMonitor);
		}		
	}
	
	public void update(IResource[] resources, Command.LocalOption[] options, boolean createBackups, IProgressMonitor monitor) throws TeamException {
		Hashtable table = getProviderMapping(resources);
		Set keySet = table.keySet();
		monitor.beginTask("", keySet.size() * 1000); //$NON-NLS-1$
		monitor.setTaskName(Policy.bind("RepositoryManager.updating")); //$NON-NLS-1$
		Iterator iterator = keySet.iterator();
		while (iterator.hasNext()) {
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
			CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
			List list = (List)table.get(provider);
			IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
			provider.update(providerResources, options, null, createBackups, subMonitor);
		}		
	}
	
	/**
	 * Mark the files as merged.
	 */
	public void merged(IRemoteSyncElement[] elements) throws TeamException {
		Hashtable table = getProviderMapping(elements);
		Set keySet = table.keySet();
		Iterator iterator = keySet.iterator();
		while (iterator.hasNext()) {
			CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
			provider.setComment(previousComment);
			List list = (List)table.get(provider);
			IRemoteSyncElement[] providerElements = (IRemoteSyncElement[])list.toArray(new IRemoteSyncElement[list.size()]);
			provider.merged(providerElements);
		}		
	}
	
	/**
	 * Return the entered comment or null.
	 * Persist the entered release comment for the next caller.
	 */
	public String promptForComment(final Shell shell) {
		final int[] result = new int[1];
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				ReleaseCommentDialog dialog = new ReleaseCommentDialog(shell);
				dialog.setComment(previousComment);
				result[0] = dialog.open();
				if (result[0] != ReleaseCommentDialog.OK) return;
				previousComment = dialog.getComment();
			}
		});
		if (result[0] != ReleaseCommentDialog.OK) return null;
		return previousComment;
	}
	
	/**
	 * Commit the given resources to their associated providers.
	 * 
	 * @param resources  the resources to commit
	 * @param monitor  the progress monitor
	 */
	public void commit(IResource[] resources, String comment, IProgressMonitor monitor) throws TeamException {
		Hashtable table = getProviderMapping(resources);
		Set keySet = table.keySet();
		monitor.beginTask("", keySet.size() * 1000); //$NON-NLS-1$
		monitor.setTaskName(Policy.bind("RepositoryManager.committing")); //$NON-NLS-1$
		Iterator iterator = keySet.iterator();
		while (iterator.hasNext()) {
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
			CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
			provider.setComment(comment);
			List list = (List)table.get(provider);
			IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
			provider.checkin(providerResources, IResource.DEPTH_INFINITE, subMonitor);
		}
	}
	
	/**
	 * Helper method. Return a hashtable mapping provider to a list of resources
	 * shared with that provider.
	 */
	private Hashtable getProviderMapping(IResource[] resources) {
		Hashtable result = new Hashtable();
		for (int i = 0; i < resources.length; i++) {
			RepositoryProvider provider = RepositoryProvider.getProvider(resources[i].getProject(), CVSProviderPlugin.getTypeId());
			List list = (List)result.get(provider);
			if (list == null) {
				list = new ArrayList();
				result.put(provider, list);
			}
			list.add(resources[i]);
		}
		return result;
	}
	/**
	 * Helper method. Return a hashtable mapping provider to a list of IRemoteSyncElements
	 * shared with that provider.
	 */
	private Hashtable getProviderMapping(IRemoteSyncElement[] elements) {
		Hashtable result = new Hashtable();
		for (int i = 0; i < elements.length; i++) {
			RepositoryProvider provider = RepositoryProvider.getProvider(elements[i].getLocal().getProject(), CVSProviderPlugin.getTypeId());
			List list = (List)result.get(provider);
			if (list == null) {
				list = new ArrayList();
				result.put(provider, list);
			}
			list.add(elements[i]);
		}
		return result;
	}

	/**
	 * Returns Branch and Version tags for the given files
	 */	
	public CVSTag[] getTags(ICVSFile file, IProgressMonitor monitor) throws TeamException {
		Set tagSet = new HashSet();
		ILogEntry[] entries = file.getLogEntries(monitor);
		for (int j = 0; j < entries.length; j++) {
			CVSTag[] tags = entries[j].getTags();
			for (int k = 0; k < tags.length; k++) {
				tagSet.add(tags[k]);
			}
		}
		return (CVSTag[])tagSet.toArray(new CVSTag[0]);
	}
	
	public ICVSRepositoryLocation getRepositoryLocationFor(ICVSResource resource) {
		try {
			ICVSFolder folder;
			if (resource.isFolder()) {
				folder = (ICVSFolder)resource;
			} else {
				folder = resource.getParent();
			}
			if(folder.isCVSFolder()) {
				ICVSRepositoryLocation location = CVSProvider.getInstance().getRepository(folder.getFolderSyncInfo().getRoot());
				return location;
			}
			return null;
		} catch(CVSException e) {
			CVSUIPlugin.log(e.getStatus());
			return null;
		}
	}
	
	/*
	 * Fetches and caches the tags found on the provided remote file.
	 */
	private CVSTag[] fetchDefinedTagsFor(ICVSFile file, ICVSFolder project, ICVSRepositoryLocation location) throws TeamException {
		if(file != null && file.exists()) {
			return getTags(file, null);
		}
		return new CVSTag[0];
	}
	
	private ICVSProvider getCVSProvider() {
		return CVSProviderPlugin.getProvider();
	}
}
