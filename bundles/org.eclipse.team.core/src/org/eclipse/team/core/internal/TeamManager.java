package org.eclipse.team.core.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.IResourceStateChangeListener;
import org.eclipse.team.core.ITeamManager;
import org.eclipse.team.core.ITeamNature;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;

public class TeamManager implements ITeamManager {

	// Constants
	protected final static String ATT_NATUREID = "natureId";
	
	private static List natureIdsRegistry = new ArrayList(5);
	private static List listeners = new ArrayList(5);
	
	/**
	 * Start the team manager.
	 * 
	 * If this method throws an exception, it is taken as an indication that
	 * the manager initialization has failed; as a result, the client should consider
	 * team support disabled.
	 */
	public void startup() throws TeamException {
		initializeProviders();
	}
	
	protected boolean alreadyMapped(IProject project) {
		try {
			String[] natures = project.getDescription().getNatureIds();
			for (int i = 0; i < natures.length; i++) {
				if(natureIdsRegistry.contains(natures[i]))
					return true;			
			}
		} catch(CoreException e) {
			// fall through
		}
		return false;
	}
	
	protected String getFirstProviderNatureId(IProject project) {
		try {
			String[] natures = project.getDescription().getNatureIds();
			for (int i = 0; i < natures.length; i++) {
				if(natureIdsRegistry.contains(natures[i]))
					return natures[i];			
			}
		} catch(CoreException e) {
			// fall through
		}
		return null;			
	}
	
	/**
	 * @see ITeamManager#setProvider(IProject, String, IProgressMonitor)
	 */
	public void setProvider(IProject project, String natureId, Properties configuration, IProgressMonitor progress) throws TeamException {
		
		if(alreadyMapped(project)) {
			throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("manager.providerAlreadyMapped", 
												 project.getName(), natureId), null));
		}
		
		if(!natureIdsRegistry.contains(natureId)) {
			throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("manager.notTeamNature", 
												 natureId), null));
		}
		
		addNatureToProject(project, natureId, progress);
		
		if(configuration!=null) {
			setConfiguration(project, natureId, configuration, progress);
		} 
	}
	
	protected void setConfiguration(IProject project, String natureId, Properties properties, IProgressMonitor progress) throws TeamException {
		try {
			ITeamNature teamNature = (ITeamNature)project.getNature(natureId);
			teamNature.configureProvider(properties);			
		} catch(ClassCastException e) {
			throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("manager.teamNatureBadType", 
									 project.getName(), natureId), null));
		} catch(CoreException e) {
			throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("manager.cannotGetDescription", 
									 project.getName(), natureId), null));
		
		}	
	}
	
	/**
	 * @see ITeamManager#getProvider(IResource resource)
	 */
	public ITeamProvider getProvider(IResource resource) {
		IProject project = resource.getProject();
		String natureId = getFirstProviderNatureId(project);

		if(natureId==null) {
			return null;
		}

		try {
			ITeamNature teamNature = (ITeamNature)project.getNature(natureId);
			return teamNature.getProvider();
		} catch(ClassCastException e) {
			return null;
		} catch(CoreException e) {
			return null;
		} catch(TeamException e) {
			return null;
		}
	}
	
	/**
	 * Un-associate this project with its provider. If the project is not associated with
	 * a provider this method has no effect.
	 * 
	 * @param project to remote the associate to its provider.
	 */
	public void removeProvider(IProject project, IProgressMonitor progress) throws TeamException {
		String natureId = getFirstProviderNatureId(project);
		if(natureId==null) {
			return;
		} else {
			removeNatureFromProject(project, natureId, progress);
		}		
	}
	
	/**
	 * Utility for adding a nature to a project
	 */
	protected void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws TeamException {
		try {
			IProjectDescription description = proj.getDescription();
			String[] prevNatures= description.getNatureIds();
			String[] newNatures= new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length]= natureId;
			description.setNatureIds(newNatures);
			proj.setDescription(description, monitor);
		} catch(CoreException e) {
				throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("manager.errorSettingNature", 
														 proj.getName(), natureId), e));
		}
	}
	
	protected void removeNatureFromProject(IProject proj, String natureId, IProgressMonitor monitor) throws TeamException {
		try {
			IProjectDescription description = proj.getDescription();
			String[] prevNatures= description.getNatureIds();
			List newNatures = new ArrayList(Arrays.asList(prevNatures));
			newNatures.remove(natureId);
			description.setNatureIds((String[])newNatures.toArray(new String[newNatures.size()]));
			proj.setDescription(description, monitor);
		} catch(CoreException e) {
				throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("manager.errorRemovingNature", 
														 proj.getName(), natureId), e));
		}
	}
	
	/**
	 * Find and initialize all the registered providers
	 */
	private void initializeProviders() throws TeamException {

		IExtensionPoint extensionPoint = Platform.getPluginRegistry().getExtensionPoint(TeamPlugin.ID, TeamPlugin.PROVIDER_EXTENSION);
		if (extensionPoint == null) {
			throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("manager.providerExtensionNotFound"), null));
		}

		IExtension[] extensions = extensionPoint.getExtensions();
		if (extensions.length == 0)
			return;
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			IConfigurationElement[] configs = extension.getConfigurationElements();
			if (configs.length == 0) {
				// there is no configuration element
				// log as an error but continue to instantiate other executable extensions.
				TeamPlugin.log(IStatus.ERROR, Policy.bind("manager.providerNoConfigElems", extension.getUniqueIdentifier()), null);
				continue;
			}
			IConfigurationElement config = configs[0];
			if(config.getName().equalsIgnoreCase(TeamPlugin.PROVIDER_EXTENSION)){
				String natureId = config.getAttribute(ATT_NATUREID);
			
				if(natureId!=null) {
					natureIdsRegistry.add(natureId);
				} else {			
					// failed to instantiate executable extension.
					// log the error but continue to instantiate other executable extensions.
					TeamPlugin.log(IStatus.ERROR, Policy.bind("manager.cannotBadFormat", extension.getUniqueIdentifier()), null);
					continue;
				}
			}
		}	
	}
		
	/*
	 * @see ITeamManager#addResourceStateChangeListener(IResourceStateChangeListener)
	 */
	public void addResourceStateChangeListener(IResourceStateChangeListener listener) {
		listeners.add(listener);
	}

	/*
	 * @see ITeamManager#removeResourceStateChangeListener(IResourceStateChangeListener)
	 */
	public void removeResourceStateChangeListener(IResourceStateChangeListener listener) {
		listeners.remove(listener);
	}
	
	/*
	 * @see ITeamManager#broadcastResourceStateChanges(IResource[])
	 */
	public void broadcastResourceStateChanges(final IResource[] resources) {
		for(Iterator it=listeners.iterator(); it.hasNext();) {
			final IResourceStateChangeListener listener = (IResourceStateChangeListener)it.next();
			ISafeRunnable code = new ISafeRunnable() {
				public void run() throws Exception {
					listener.resourceStateChanged(resources);
				}
				public void handleException(Throwable e) {
					// don't log the exception....it is already being logged in Platform#run
				}
			};
			Platform.run(code);
		}
	}
}