package org.eclipse.e4.internal.core.services.bundle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ILookupStrategy;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

public class BundleContextStrategy implements ILookupStrategy, IDisposable {

	private static final String PREFERENCE_PREFIX = "preference-";
	private Bundle bundle;
	private IEclipsePreferences node;
	private IEclipsePreferences defaultNode;
	Job syncJob;

	class PrefData {
		// the preference id
		String name;

		// the contexts using this service (IEclipseContext -> null)
		final Map users = new WeakHashMap();

		PrefData(String name) {
			this.name = name;
		}

		public void addContext(IEclipseContext originatingContext) {
			users.put(originatingContext, null);
		}
	}

	/**
	 * Map of String (preference key) -> PrefData
	 */
	private Map preferences = Collections.synchronizedMap(new HashMap());
	private IPreferenceChangeListener listener = new IPreferenceChangeListener() {
		public void preferenceChange(PreferenceChangeEvent event) {
			handlePreferenceChange(event);
		}
	};

	public BundleContextStrategy(Bundle bundle) {
		this.bundle = bundle;
		this.node = new InstanceScope().getNode(bundle.getSymbolicName());
		this.defaultNode = new DefaultScope().getNode(bundle.getSymbolicName());
	}

	void handlePreferenceChange(PreferenceChangeEvent event) {
		String key = event.getKey();
		String newValue = (String) event.getNewValue();
		PrefData prefData = (PrefData) preferences.get(key);
		if (prefData != null) {
			for (Iterator it = prefData.users.keySet().iterator(); it.hasNext();)
				((IEclipseContext) it.next()).set(PREFERENCE_PREFIX + key, newValue);
		}
		synchronized (this) {
			if (syncJob == null) {
				syncJob = new Job("sync preferences") {
					@Override
					protected IStatus run(IProgressMonitor monitor) {
						System.out.println("saving preferences for " + bundle.getSymbolicName());
						try {
							node.flush();
						} catch (BackingStoreException e) {
							e.printStackTrace();
						}
						return Status.OK_STATUS;
					}
				};
			}
			syncJob.schedule(1000);
		}
	}

	public Object lookup(String name, IEclipseContext context) {
		if (IEclipsePreferences.class.getName().equals(name)) {
			return node;
		}
		if (BundleContext.class.getName().equals(name)) {
			return bundle.getBundleContext();
		}
		if (!name.startsWith(PREFERENCE_PREFIX)) {
			return null;
		}
		String preferenceKey = name.substring(PREFERENCE_PREFIX.length());
		String value = node.get(preferenceKey, null);
		if (value == null) {
			value = defaultNode.get(preferenceKey, null);
			if (value == null) {
				return null;
			}
		}

		PrefData prefData = (PrefData) preferences.get(preferenceKey);
		if (prefData == null) {
			startListening();
			prefData = new PrefData(preferenceKey);
			preferences.put(preferenceKey, prefData);
		}
		prefData.addContext(context);
		return value;
	}

	private void startListening() {
		node.addPreferenceChangeListener(listener);
	}

	private void stopListening() {
		node.removePreferenceChangeListener(listener);
	}

	public void dispose() {
		stopListening();
		try {
			node.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		synchronized (preferences) {
			for (Iterator it = preferences.values().iterator(); it.hasNext();) {
				PrefData prefData = (PrefData) it.next();
			}
			preferences.clear();
		}
	}

	public boolean containsKey(String name, IEclipseContext context) {
		if (IEclipsePreferences.class.getName().equals(name)) {
			return true;
		}
		if (BundleContext.class.getName().equals(name)) {
			return true;
		}
		if (!name.startsWith(PREFERENCE_PREFIX)) {
			return false;
		}
		String preferenceKey = name.substring(PREFERENCE_PREFIX.length());
		String value = node.get(preferenceKey, null);
		if (value == null) {
			if (defaultNode.get(preferenceKey, null) == null) {
				return false;
			}
		}

		return true;
	}

}