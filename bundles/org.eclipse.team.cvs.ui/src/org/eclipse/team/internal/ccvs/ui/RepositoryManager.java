package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSListener;
import org.eclipse.team.ccvs.core.ICVSProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.ccvs.core.ILogEntry;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.NullCopyHandler;
import org.eclipse.team.internal.ccvs.core.client.ResponseHandler;
import org.eclipse.team.internal.ccvs.ui.model.BranchTag;

/**
 * This class is repsible for maintaining the UI's list of known repositories,
 * and a list of known tags within each of those repositories.
 * 
 * It also provides a number of useful methods for assisting in repository operations.
 */
public class RepositoryManager {
	private static final String STATE_FILE = ".repositoryManagerState";
	
	// Map ICVSRepositoryLocation -> List of Tags
	Hashtable branchTags = new Hashtable();
	// Map ICVSRepositoryLocation -> Hashtable of (Project name -> Set of CVSTags)
	Hashtable versionTags = new Hashtable();
	
	List listeners = new ArrayList();

	// The previously remembered comment
	private static String previousComment = "";
	
	/**
	 * Answer an array of all known remote roots.
	 */
	public ICVSRepositoryLocation[] getKnownRoots() {
		return getCVSProvider().getKnownRepositories();
	}
	
	private ICVSProvider getCVSProvider() {
		return CVSProviderPlugin.getProvider();
	}
	
	/**
	 * Get the list of known branch tags for a given remote root.
	 */
	public BranchTag[] getKnownBranchTags(ICVSRepositoryLocation root) {
		Set set = (Set)branchTags.get(root);
		if (set == null) return new BranchTag[0];
		return (BranchTag[])set.toArray(new BranchTag[0]);
	}
	/**
	 * Get the list of known version tags for a given project.
	 * 
	 * This list includes:
	 * -All manually defined or auto-defined version tags
	 * -All tags for the .vcm_meta file, if one exists
	 * 
	 * A server hit is incurred on each call to ensure up-to-date results.
	 */
	public CVSTag[] getKnownVersionTags(ICVSRemoteResource resource, IProgressMonitor monitor) throws TeamException {
		// Find tags in .vcm_meta file, optimization for Eclipse users
		Set result = new HashSet();
		ICVSRemoteFile[] vcmMeta = getVCMMeta(resource);
		for (int j = 0; j < vcmMeta.length; j++) {
			ICVSRemoteFile iCVSRemoteFile = vcmMeta[j];
			CVSTag[] tags = getTags(iCVSRemoteFile, new NullProgressMonitor());
			for (int i = 0; i < tags.length; i++) {
				if (tags[i].getType() == CVSTag.VERSION) {
					result.add(tags[i]);
				}
			}
		}
		
		Hashtable table = (Hashtable)versionTags.get(resource.getRepository());
		if (table == null) {
			return (CVSTag[])result.toArray(new CVSTag[result.size()]);
		}
		Set set = (Set)table.get(resource.getName());
		if (set == null) {
			return (CVSTag[])result.toArray(new CVSTag[result.size()]);
		}
		result.addAll(set);
		return (CVSTag[])result.toArray(new CVSTag[0]);
	}
	private ICVSRemoteFile[] getVCMMeta(ICVSRemoteResource resource) throws TeamException {
		// There should be a better way of doing this.
		List files = new ArrayList();
		IRemoteResource[] resources = resource.members(new NullProgressMonitor());
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] instanceof ICVSRemoteFile && 
					(resources[i].getName().equals(".vcm_meta") || resources[i].getName().equals(".project"))) {
				files.add(resources[i]);
			}
		}
		return (ICVSRemoteFile[]) files.toArray(new ICVSRemoteFile[files.size()]);
	}
	/**
	 * Add the given branch tags to the list of known tags for the given
	 * remote root.
	 */
	public void addBranchTags(ICVSRepositoryLocation root, BranchTag[] tags) {
		Set set = (Set)branchTags.get(root);
		if (set == null) {
			set = new HashSet();
			branchTags.put(root, set);
		}
		for (int i = 0; i < tags.length; i++) {
			set.add(tags[i]);
		}
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.branchTagsAdded(tags, root);
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
		BranchTag[] branchTags = getKnownBranchTags(root);
		Hashtable vTags = (Hashtable)this.versionTags.get(root);
		this.branchTags.remove(root);
		this.versionTags.remove(root);
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
	 * Add the given version tags to the list of known tags for the given
	 * remote project.
	 */
	public void addVersionTags(ICVSRemoteResource resource, CVSTag[] tags) {
		String name = resource.getName();
		Hashtable table = (Hashtable)versionTags.get(resource.getRepository());
		if (table == null) {
			table = new Hashtable();
			versionTags.put(resource.getRepository(), table);
		}
		Set set = (Set)table.get(name);
		if (set == null) {
			set = new HashSet();
			table.put(name, set);
		}
		for (int i = 0; i < tags.length; i++) {
			set.add(tags[i]);
		}
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.versionTagsAdded(tags, resource.getRepository());
		}
	}
	/**
	 * Remove the given branch tag from the list of known tags for the
	 * given remote root.
	 */
	public void removeBranchTag(ICVSRepositoryLocation root, BranchTag[] tags) {
		Set set = (Set)branchTags.get(root);
		if (set == null) return;
		for (int i = 0; i < tags.length; i++) {
			set.remove(tags[i]);
		}
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.branchTagsRemoved(tags, root);
		}
	}
	/**
	 * Remove the given tags from the list of known tags for the
	 * given remote root.
	 */
	public void removeVersionTags(ICVSRemoteResource resource, CVSTag[] tags) {
		Hashtable table = (Hashtable)versionTags.get(resource.getRepository());
		if (table == null) return;
		Set set = (Set)table.get(resource.getName());
		if (set == null) return;
		for (int i = 0; i < tags.length; i++) {
			set.remove(tags[i]);
		}
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.versionTagsRemoved(tags, resource.getRepository());
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
				throw new TeamException(new Status(Status.ERROR, CVSUIPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.ioException"), e));
			}
		}
	}
	
	private void saveState() throws TeamException {
		IPath pluginStateLocation = CVSUIPlugin.getPlugin().getStateLocation();
		File tempFile = pluginStateLocation.append(STATE_FILE + ".tmp").toFile();
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
				throw new TeamException(new Status(Status.ERROR, CVSUIPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.rename", tempFile.getAbsolutePath()), null));
			}
		} catch (IOException e) {
			throw new TeamException(new Status(Status.ERROR, CVSUIPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.save",stateFile.getAbsolutePath()), e));
		}
	}
	private void writeState(DataOutputStream dos) throws IOException {
		// Write the repositories
		Collection repos = Arrays.asList(getKnownRoots());
		dos.writeInt(repos.size());
		Iterator it = repos.iterator();
		while (it.hasNext()) {
			ICVSRepositoryLocation root = (ICVSRepositoryLocation)it.next();
			dos.writeUTF(root.getLocation());
			BranchTag[] branchTags = getKnownBranchTags(root);
			dos.writeInt(branchTags.length);
			for (int i = 0; i < branchTags.length; i++) {
				dos.writeUTF(branchTags[i].getTag().getName());
				dos.writeInt(branchTags[i].getTag().getType());
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
		}
	}
	private void readState(DataInputStream dis) throws IOException, TeamException {
		int repoSize = dis.readInt();
		for (int i = 0; i < repoSize; i++) {
			ICVSRepositoryLocation root = CVSProviderPlugin.getProvider().getRepository(dis.readUTF());
			int tagsSize = dis.readInt();
			BranchTag[] branchTags = new BranchTag[tagsSize];
			for (int j = 0; j < tagsSize; j++) {
				String tagName = dis.readUTF();
				int tagType = dis.readInt();
				branchTags[j] = new BranchTag(new CVSTag(tagName, tagType), root);
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
		monitor.beginTask("", keySet.size() * 1000);
		monitor.setTaskName(Policy.bind("RepositoryManager.adding"));
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
		monitor.beginTask("", keySet.size() * 1000);
		monitor.setTaskName(Policy.bind("RepositoryManager.deleting"));
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
		monitor.beginTask("", keySet.size() * 1000);
		monitor.setTaskName(Policy.bind("RepositoryManager.updating"));
		Iterator iterator = keySet.iterator();
		while (iterator.hasNext()) {
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
			CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
			List list = (List)table.get(provider);
			IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
			ResponseHandler handler = null;
			if (!createBackups) {
				handler = new NullCopyHandler();
			}
			provider.update(providerResources, options, null, handler, subMonitor);
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
		monitor.beginTask("", keySet.size() * 1000);
		monitor.setTaskName(Policy.bind("RepositoryManager.committing"));
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
	public CVSTag[] getTags(ICVSRemoteFile file, IProgressMonitor monitor) throws TeamException {
		ICVSRepositoryLocation root = file.getRepository();
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

	/**
	 * Auto-define version and branch tags for the given files.
	 */	
	public void autoDefineTags(ICVSRemoteFile[] files, IProgressMonitor monitor) throws TeamException {
		for (int i = 0; i < files.length; i++) {
			ICVSRemoteFile file = files[i];
			ICVSRepositoryLocation root = file.getRepository();
			CVSTag[] tags = getTags(file, monitor);

			// Break tags up into version tags and branch tags.
			List branchTags = new ArrayList();
			List versionTags = new ArrayList();
			for (int j = 0; j < tags.length; j++) {
				CVSTag tag = tags[j];
				if (tag.getType() == CVSTag.BRANCH) {
					branchTags.add(new BranchTag(tag, root));
				} else {
					versionTags.add(tag);
				}
			}
			if (branchTags.size() > 0) {
				addBranchTags(root, (BranchTag[])branchTags.toArray(new BranchTag[0]));
			}
			if (versionTags.size() > 0) {
				// Current behaviour for version tags is to match the behaviour in VCM 1.0, 
				// which is to attach them to the top-most folder in CVS. This may change in the future
				// to allow a more flexible scheme of attaching 'project' semantics to arbitrary
				// cvs folders. Get the top-most folder now to optimize.
				ICVSRemoteResource current = file.getRemoteParent();
				ICVSRemoteResource next = current.getRemoteParent();
				while (next != null && next.getRemoteParent() != null) {
					current = next;
					next = current.getRemoteParent();
				}
				addVersionTags(current, (CVSTag[])versionTags.toArray(new CVSTag[0]));
			}
		}
	}
}
