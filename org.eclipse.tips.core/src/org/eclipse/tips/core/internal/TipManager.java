/****************************************************************************
 * Copyright (c) 2017, 2018 Remain Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wim Jongman <wim.jongman@remainsoftware.com> - initial API and implementation
 *****************************************************************************/
package org.eclipse.tips.core.internal;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.tips.core.ITipManager;
import org.eclipse.tips.core.TipProvider;

/**
 * An abstract implementation of ITipManager with additional control API. While
 * the rest of the framework must work with ITipManager, this class provides API
 * to open the dialog and do low level housekeeping that is of no concern to
 * external participants (Tip and TipProvider).
 *
 */
public abstract class TipManager implements ITipManager {

	private Map<String, TipProvider> fProviders = new HashMap<>();
	private Map<Integer, List<String>> fProviderPrio = new TreeMap<>();
	private boolean fOpen;
	private boolean fServeReadTips = false;
	private boolean fIsDiposed;
	private PropertyChangeSupport fChangeSupport = new PropertyChangeSupport(this);

	/**
	 * Instantiates a new TipManager.
	 */
	public TipManager() {
	}

	/**
	 * Gets the provider with the specified ID.
	 *
	 * @param providerID the id of the provider to fetch
	 * @return the provider with the specified ID or null if no such provider
	 *         exists.
	 * @see TipProvider#getID()
	 */
	public TipProvider getProvider(String providerID) {
		checkDisposed();
		return fProviders.get(providerID);
	}

	/**
	 * Binds the passed provider to this manager. Implementations should override,
	 * call super, and then asynchronously call the
	 * {@link TipProvider#loadNewTips(org.eclipse.core.runtime.IProgressMonitor)}
	 * method.
	 * 
	 * This manager then starts listening to the a {@link TipProvider#PROP_READY}
	 * property change event and resends it through its own change support.
	 *
	 * @param provider the {@link TipProvider} to register.
	 *
	 * @return this
	 */
	@Override
	public ITipManager register(TipProvider provider) {
		checkDisposed();
		String message = MessageFormat.format(Messages.TipManager_0, provider.getID() ,provider.getDescription());
		log(LogUtil.info(message));
		provider.setManager(this);
		addToMaps(provider, Integer.valueOf(getPriority(provider)));
		provider.getChangeSupport().addPropertyChangeListener(event -> {
			if (event.getPropertyName().equals(TipProvider.PROP_READY)) {
				PropertyChangeEvent newEvent = new PropertyChangeEvent(this, event.getPropertyName(), null, provider);
				newEvent.setPropagationId(event.getPropagationId());
				getChangeSupport().firePropertyChange(newEvent);
			}
		});
		return this;
	}

	public PropertyChangeSupport getChangeSupport() {
		return fChangeSupport;
	}

	private void checkDisposed() {
		if (isDisposed()) {
			throw new RuntimeException(Messages.TipManager_2);
		}

	}

	/**
	 * Calculates the priority that this provider has in the Tips framework. The
	 * {@link TipProvider#getExpression()} was purposed to aid in the calculation of
	 * the priority.
	 *
	 * @param provider the provider
	 * @return the priority, lower is higher, never negative.
	 */
	public abstract int getPriority(TipProvider provider);

	private synchronized void addToMaps(TipProvider pProvider, Integer pPriorityHint) {
		removeFromMaps(pProvider);
		addToProviderMaps(pProvider, pPriorityHint);
		addToPriorityMap(pProvider, pPriorityHint);
	}

	private void addToPriorityMap(TipProvider provider, Integer priorityHint) {
		if (!fProviderPrio.get(priorityHint).contains(provider.getID())) {
			if (!fProviderPrio.get(priorityHint).contains(provider.getID())) {
				fProviderPrio.get(priorityHint).add(provider.getID());
			}
		}
	}

	private void addToProviderMaps(TipProvider provider, Integer priorityHint) {
		fProviders.put(provider.getID(), provider);
		if (fProviderPrio.get(priorityHint) == null) {
			fProviderPrio.put(priorityHint, new ArrayList<>());
		}
	}

	private void removeFromMaps(TipProvider provider) {
		if (fProviders.containsKey(provider.getID())) {
			for (Map.Entry<Integer, List<String>> entry : fProviderPrio.entrySet()) {
				entry.getValue().remove(provider.getID());
			}
			fProviders.remove(provider.getID());
		}
	}

	/**
	 * The returned list contains providers ready to serve tips and is guaranteed to
	 * be in a prioritised order according the implementation of this manager.
	 *
	 * @return the prioritised list of ready providers with tips in an immutable
	 *         list.
	 */
	public List<TipProvider> getProviders() {
		checkDisposed();
		if (fProviders == null) {
			return Collections.emptyList();
		}
		ArrayList<TipProvider> result = new ArrayList<>();
		for (Map.Entry<Integer, List<String>> entry : fProviderPrio.entrySet()) {
			for (String id : entry.getValue()) {
				if (fProviders.get(id).isReady()) {
					result.add(fProviders.get(id));
				}
			}
		}
		return Collections.unmodifiableList(result);
	}

	/**
	 * Determines if the Tips framework must run at startup. The default
	 * implementation returns true, subclasses should probably override this.
	 *
	 * @return true if the Tips framework should run at startup.
	 * @see TipManager#setRunAtStartup(boolean)
	 */
	public boolean isRunAtStartup() {
		checkDisposed();
		return true;
	}

	/**
	 * Determines if the Tips framework must run at startup.
	 *
	 * @param shouldRun true if the tips should be displayed at startup, false
	 *                  otherwise.
	 *
	 * @return this
	 *
	 * @see #isRunAtStartup()
	 */
	public abstract TipManager setRunAtStartup(boolean shouldRun);

	/**
	 * The default implementation disposes of this manager and all the TipProviders
	 * when the dialog is disposed. Subclasses may override but must call super.
	 */
	public void dispose() {
		checkDisposed();
		try {
			for (TipProvider provider : fProviders.values()) {
				try {
					provider.dispose();
				} catch (Exception e) {
					log(LogUtil.error(e));
				}
			}
		} finally {
			fProviders.clear();
			fProviderPrio.clear();
			fIsDiposed = true;
		}
	}

	/**
	 * @return true if this manager is currently open.
	 */
	@Override
	public boolean isOpen() {
		// checkDisposed();
		if (isDisposed())
			return false;
		return fOpen;
	}

	protected void setOpen(boolean open) {
		fOpen = open;
	}

	/**
	 * Indicates whether read tips must be served or not. Subclasses could override,
	 * to save the state somewhere, but must call super.
	 *
	 * @param serveRead true of read tips may be served by the {@link TipProvider}s
	 * @return this
	 * @see TipManager#mustServeReadTips()
	 */
	public TipManager setServeReadTips(boolean serveRead) {
		checkDisposed();
		fServeReadTips = serveRead;
		return this;
	}

	/**
	 * Indicates whether already read tips must be served or not.
	 *
	 * @return true or false
	 * @see #setServeReadTips(boolean)
	 */
	@Override
	public boolean mustServeReadTips() {
		checkDisposed();
		return fServeReadTips;
	}

	public boolean isDisposed() {
		return fIsDiposed;
	}

	@Override
	public boolean hasContent() {
		for (TipProvider provider : getProviders()) {
			if (provider.isReady() && !provider.getTips().isEmpty()) {
				return true;
			}
		}
		return false;
	}
}