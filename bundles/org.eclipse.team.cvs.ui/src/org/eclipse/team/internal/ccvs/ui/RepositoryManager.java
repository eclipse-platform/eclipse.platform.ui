package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.ITeamManager;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.ui.model.Tag;

/**
 * This class is repsible for maintaining the UI's list of known repositories,
 * and a list of known tags within each of those repositories.
 */
public class RepositoryManager {
	private static final String STATE_FILE = ".repositoryManagerState";
	
	Hashtable repositories = new Hashtable();
	// Map ICVSRepositoryLocation -> List of Tags
	Hashtable branchTags = new Hashtable();
	// Map ICVSRepositoryLocation -> List of Tags
	Hashtable versionTags = new Hashtable();
	
	List listeners = new ArrayList();
	
	/**
	 * Answer an array of all known remote roots.
	 */
	public ICVSRepositoryLocation[] getKnownRoots() {
		return (ICVSRepositoryLocation[])repositories.values().toArray(new ICVSRepositoryLocation[0]);
	}
	/**
	 * Answer the root corresponding with the given properties.
	 * If the root is in the list of known roots, it is returned.
	 * If it is not in the list of known roots, it is created and
	 * added.
	 */
	public ICVSRepositoryLocation getRoot(Properties properties) {
		StringBuffer keyBuffer = new StringBuffer();
		keyBuffer.append(":");
		keyBuffer.append(properties.getProperty("connection"));
		keyBuffer.append(":");
		keyBuffer.append(properties.getProperty("user"));
		keyBuffer.append("@");
		keyBuffer.append(properties.getProperty("host"));
		String port = properties.getProperty("port");
		if (port != null) {
			keyBuffer.append("#");
			keyBuffer.append(port);
		}
		keyBuffer.append(":");
		keyBuffer.append(properties.getProperty("root"));
		String key = keyBuffer.toString();
		
		ICVSRepositoryLocation result = (ICVSRepositoryLocation)repositories.get(key);
		if (result != null) {
			return result;
		}
		try {
			result = CVSProviderPlugin.getProvider().createRepository(properties);
			addRoot(result);
		} catch (TeamException e) {
			CVSUIPlugin.log(e.getStatus());
			return null;
		}
		return result;
	}
	/**
	 * Get the list of known branch tags for a given remote root.
	 */
	public Tag[] getKnownBranchTags(ICVSRepositoryLocation root) {
		Set set = (Set)branchTags.get(root);
		if (set == null) return new Tag[0];
		return (Tag[])set.toArray(new Tag[0]);
	}
	/**
	 * Get the list of known version tags for a given remote root.
	 */
	public Tag[] getKnownVersionTags(ICVSRepositoryLocation root) {
		Set set = (Set)versionTags.get(root);
		if (set == null) return new Tag[0];
		return (Tag[])set.toArray(new Tag[0]);
	}
	/**
	 * Add the given branch tag to the list of known tags for the given
	 * remote root.
	 */
	public void addBranchTag(ICVSRepositoryLocation root, Tag tag) {
		Set set = (Set)branchTags.get(root);
		if (set == null) {
			set = new HashSet();
			branchTags.put(root, set);
		}
		set.add(tag);
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.tagAdded(tag, root);
		}
	}
	/**
	 * Add the given repository location to the list of known repository
	 * locations. Listeners are notified.
	 */
	public void addRoot(ICVSRepositoryLocation root) {
		if (repositories.get(root.getLocation()) != null) return;
		repositories.put(root.getLocation(), root);
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.repositoryAdded(root);
		}
	}
	/**
	 * Add the given version tag to the list of known tags for the given
	 * remote root.
	 */
	public void addVersionTag(ICVSRepositoryLocation root, Tag tag) {
		Set set = (Set)versionTags.get(root);
		if (set == null) {
			set = new HashSet();
			versionTags.put(root, set);
		}
		set.add(tag);
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.tagAdded(tag, root);
		}
	}
	/**
	 * Remove the given branch tag from the list of known tags for the
	 * given remote root.
	 */
	public void removeBranchTag(ICVSRepositoryLocation root, Tag tag) {
		Set set = (Set)branchTags.get(root);
		if (set == null) return;
		set.remove(tag);
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.tagRemoved(tag, root);
		}
	}
	/**
	 * Remove the given tag from the list of known tags for the
	 * given remote root.
	 */
	public void removeVersionTag(ICVSRepositoryLocation root, Tag tag) {
		Set set = (Set)versionTags.get(root);
		if (set == null) return;
		set.remove(tag);
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			listener.tagRemoved(tag, root);
		}
	}
	/**
	 * Remove the given root from the list of known remote roots.
	 * Also removed the tags defined for this root.
	 */
	public void removeRoot(ICVSRepositoryLocation root) {
		Tag[] branchTags = getKnownBranchTags(root);
		Tag[] versionTags = getKnownVersionTags(root);
		Object o = repositories.remove(root.getLocation());
		if (o == null) return;
		this.branchTags.remove(root);
		this.versionTags.remove(root);
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			IRepositoryListener listener = (IRepositoryListener)it.next();
			for (int i = 0; i < branchTags.length; i++) {
				listener.tagRemoved(branchTags[i], root);
			}
			for (int i = 0; i < versionTags.length; i++) {
				listener.tagRemoved(versionTags[i], root);
			}
			listener.repositoryRemoved(root);
		}
	}
	
	public void startup() throws TeamException {
		loadState();
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
				readState(dis);
				dis.close();
			} catch (IOException e) {
				throw new TeamException(new Status(Status.ERROR, CVSUIPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.ioException"), e));
			}
		} else {
			// If the file did not exist, then prime the list of repositories with
			// the providers with which the projects in the workspace are shared.
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			ITeamManager manager = TeamPlugin.getManager();
			for (int i = 0; i < projects.length; i++) {
				ITeamProvider provider = manager.getProvider(projects[i]);
				if (provider instanceof CVSTeamProvider) {
					CVSTeamProvider cvsProvider = (CVSTeamProvider)provider;
					ICVSRepositoryLocation result = cvsProvider.getRemoteResource(projects[i]).getRepository();
					repositories.put(result.getLocation(), result);
					Iterator it = listeners.iterator();
					while (it.hasNext()) {
						IRepositoryListener listener = (IRepositoryListener)it.next();
						listener.repositoryAdded(result);
					}
				}
			}
		}
	}
	
	private void saveState() throws TeamException {
		IPath pluginStateLocation = CVSUIPlugin.getPlugin().getStateLocation();
		File tempFile = pluginStateLocation.append(STATE_FILE + ".tmp").toFile();
		File stateFile = pluginStateLocation.append(STATE_FILE).toFile();
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
			writeState(dos);
			dos.close();
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
		Collection repos = repositories.values();
		dos.writeInt(repos.size());
		Iterator it = repos.iterator();
		while (it.hasNext()) {
			ICVSRepositoryLocation root = (ICVSRepositoryLocation)it.next();
			dos.writeUTF(root.getMethod().getName());
			dos.writeUTF(root.getUsername());
			dos.writeUTF(root.getHost());
			dos.writeUTF("" + root.getPort());
			dos.writeUTF(root.getRootDirectory());
			Tag[] branchTags = getKnownBranchTags(root);
			dos.writeInt(branchTags.length);
			for (int i = 0; i < branchTags.length; i++) {
				dos.writeUTF(branchTags[i].getTag());
			}
			Tag[] versionTags = getKnownVersionTags(root);
			dos.writeInt(versionTags.length);
			for (int i = 0; i < versionTags.length; i++) {
				dos.writeUTF(versionTags[i].getTag());
			}
		}
	}
	private void readState(DataInputStream dis) throws IOException {
		int repoSize = dis.readInt();
		for (int i = 0; i < repoSize; i++) {
			Properties properties = new Properties();
			properties.setProperty("connection", dis.readUTF());
			properties.setProperty("user", dis.readUTF());
			properties.setProperty("host", dis.readUTF());
			String port = dis.readUTF();
			if (!port.equals("" + ICVSRepositoryLocation.USE_DEFAULT_PORT)) {
				properties.setProperty("port", port);
			}
			properties.setProperty("root", dis.readUTF());
			ICVSRepositoryLocation root = getRoot(properties);
			int tagsSize = dis.readInt();
			for (int j = 0; j < tagsSize; j++) {
				String tag = dis.readUTF();
				addBranchTag(root, new Tag(tag, true, root));
			}
			tagsSize = dis.readInt();
			for (int j = 0; j < tagsSize; j++) {
				String tag = dis.readUTF();
				addVersionTag(root, new Tag(tag, false, root));
			}
			
		}
	}
	
	public void addRepositoryListener(IRepositoryListener listener) {
		listeners.add(listener);
	}
	
	public void remoteRepositoryListener(IRepositoryListener listener) {
		listeners.remove(listener);
	}
}
