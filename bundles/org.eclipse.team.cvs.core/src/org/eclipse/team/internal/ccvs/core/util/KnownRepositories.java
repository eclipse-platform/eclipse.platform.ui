/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSListener;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;

/**
 * This class keeps track of the CVS repository locations that are known to
 * the CVS plugin.
 */
public class KnownRepositories {

	private List repositoryListeners = new ArrayList();
	private Map repositories = new HashMap();

	private static KnownRepositories instance;
	
	public static synchronized KnownRepositories getInstance() {
		if (instance == null) {
			instance = new KnownRepositories();
		}
		return instance;
	}
	
	/*
	 * Private class used to safely notify listeners of resouce sync info changes. 
	 * Subclass override the notify(IResourceStateChangeListener) method to
	 * fire specific events inside an ISafeRunnable.
	 */
	private abstract class Notification implements ISafeRunnable {
		private ICVSListener listener;
		public void handleException(Throwable exception) {
			// don't log the exception....it is already being logged in Platform#run
		}
		public void run(ICVSListener listener) {
			this.listener = listener;
			Platform.run(this);
		}
		public void run() throws Exception {
			notify(listener);
		}
		/**
		 * Subsclasses overide this method to send an event safely to a lsistener
		 * @param listener
		 */
		protected abstract void notify(ICVSListener listener);
	}
	
	/**
	 * Register to receive notification of repository creation and disposal
	 */
	public void addRepositoryListener(ICVSListener listener) {
		synchronized(repositoryListeners) {
			repositoryListeners.add(listener);
		}
	}
	
	/**
	 * De-register a listener
	 */
	public void removeRepositoryListener(ICVSListener listener) {
		synchronized(repositoryListeners) {
			repositoryListeners.remove(listener);
		}
	}
	
	private ICVSListener[] getListeners() {
		synchronized(repositoryListeners) {
			return (ICVSListener[]) repositoryListeners.toArray(new ICVSListener[repositoryListeners.size()]);
		}
	}
	
	private void fireNotification(Notification notification) {
		// Get a snapshot of the listeners so the list doesn't change while we're firing
		ICVSListener[] listeners = getListeners();
		// Notify each listener in a safe manner (i.e. so their exceptions don't kill us)
		for (int i = 0; i < listeners.length; i++) {
			ICVSListener listener = listeners[i];
			notification.run(listener);
		}
	}
	
	/*
	 * Add the repository location to the cached locations and notify listeners
	 */
	private void addToRepositoriesCache(final ICVSRepositoryLocation repository) {
		repositories.put(repository.getLocation(), repository);
		fireNotification(new Notification() {
			public void notify(ICVSListener listener) {
				listener.repositoryAdded(repository);
			}
		});
	}
	
	/*
	 * Remove the repository location from the cached locations and notify listeners
	 */
	private void removeFromRepositoriesCache(final ICVSRepositoryLocation repository) {
		if (repositories.remove(repository.getLocation()) != null) {
			fireNotification(new Notification() {
				public void notify(ICVSListener listener) {
					listener.repositoryRemoved(repository);
				}
			});
		}
	}
	
	/**
	 * Create a repository instance from the given properties.
	 * The supported properties are:
	 * 
	 *   connection The connection method to be used
	 *   user The username for the connection
	 *   password The password used for the connection (optional)
	 *   host The host where the repository resides
	 *   port The port to connect to (optional)
	 *   root The server directory where the repository is located
	 * 
	 * The created instance is not known by the provider and it's user information is not cached.
	 * The purpose of the created location is to allow connection validation before adding the
	 * location to the provider.
	 * 
	 * This method will throw a CVSException if the location for the given configuration already
	 * exists.
	 */
	public ICVSRepositoryLocation createRepository(Properties configuration) throws CVSException {
		// Create a new repository location
		CVSRepositoryLocation location = CVSRepositoryLocation.fromProperties(configuration);
		
		// Check the cache for an equivalent instance and if there is one, throw an exception
		CVSRepositoryLocation existingLocation = (CVSRepositoryLocation)repositories.get(location.getLocation());
		if (existingLocation != null) {
			throw new CVSException(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSProvider.alreadyExists"))); //$NON-NLS-1$
		}

		return location;
	}

	/**
	 * Add the repository to the receiver's list of known repositories. Doing this will enable
	 * password caching accross platform invokations.
	 */
	public void addRepository(ICVSRepositoryLocation repository) throws CVSException {
		// Check the cache for an equivalent instance and if there is one, just update the cache
		CVSRepositoryLocation existingLocation = (CVSRepositoryLocation)repositories.get(repository.getLocation());
		if (existingLocation != null) {
			((CVSRepositoryLocation)repository).updateCache();
		} else {
			// Cache the password and register the repository location
			addToRepositoriesCache(repository);
			((CVSRepositoryLocation)repository).updateCache();
		}
	}
	
	/**
	 * Dispose of the repository location
	 * 
	 * Removes any cached information about the repository such as a remembered password.
	 */
	public void disposeRepository(ICVSRepositoryLocation repository) throws CVSException {
		((CVSRepositoryLocation)repository).dispose();
		removeFromRepositoriesCache(repository);
	}

	/**
	 * Answer whether the provided repository location is known by the provider or not.
	 * The location string corresponds to the Strin returned by ICVSRepositoryLocation#getLocation()
	 */
	public boolean isKnownRepository(String location) {
		return repositories.get(location) != null;
	}
	
	/** 
	 * Return a list of the know repository locations
	 */
	public ICVSRepositoryLocation[] getKnownRepositories() {
		return (ICVSRepositoryLocation[])repositories.values().toArray(new ICVSRepositoryLocation[repositories.size()]);
	}
	
	/**
	 * Get the repository instance which matches the given String. The format of the String is
	 * the same as that returned by ICVSRepositoryLocation#getLocation().
	 * The format is:
	 * 
	 *   connection:user[:password]@host[#port]:root
	 * 
	 * where [] indicates optional and the identier meanings are:
	 * 
	 * 	 connection The connection method to be used
	 *   user The username for the connection
	 *   password The password used for the connection (optional)
	 *   host The host where the repository resides
	 *   port The port to connect to (optional)
	 *   root The server directory where the repository is located
	 * 
	 * It is expected that the instance requested by using this method exists.
	 * If the repository location does not exist, it will be automatically created
	 * and cached with the provider.
	 * 
	 * WARNING: Providing the password as part of the String will result in the password being part
	 * of the location permanently. This means that it cannot be modified by the authenticator. 
	 */
	public ICVSRepositoryLocation getRepository(String location) throws CVSException {
		ICVSRepositoryLocation repository = (ICVSRepositoryLocation)repositories.get(location);
		if (repository == null) {
			repository = CVSRepositoryLocation.fromString(location);
			addToRepositoriesCache(repository);
		}
		return repository;
	}
}
