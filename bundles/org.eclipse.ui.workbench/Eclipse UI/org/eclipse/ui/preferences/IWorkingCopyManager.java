package org.eclipse.ui.preferences;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * IWorkingCopyManager is the interface for the working copy
 * support for references to shared preference nodes.
 * @since 3.1
 *
 */
public interface IWorkingCopyManager {
	/**
	 * Return a working copy instance based on the given preference node. If a
	 * working copy already exists then return it, otherwise create one and keep
	 * track of it for other clients who are looking for it.
	 * 
	 * @param original
	 *            the original node
	 * @return the working copy node
	 */
	public IEclipsePreferences getWorkingCopy(IEclipsePreferences original);

	/**
	 * Apply the changes for <em>all</em> working copies, to their original
	 * preference nodes. Alternatively, if a client wishes to apply the changes
	 * for a single working copy they can call <code>#flush</code> on that
	 * working copy node.
	 * 
	 * @throws BackingStoreException
	 *             if there were problems accessing the backing store
	 */
	public void applyChanges() throws BackingStoreException;


}
