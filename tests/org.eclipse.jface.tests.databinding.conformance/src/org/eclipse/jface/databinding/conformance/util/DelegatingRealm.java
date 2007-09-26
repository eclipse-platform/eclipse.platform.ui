package org.eclipse.jface.databinding.conformance.util;

import org.eclipse.core.databinding.observable.Realm;

/**
 * Realm that will delegate to another for all operations except calls to
 * {@link #isCurrent()}. The current status can be set by the consumer to
 * enable testing of realm checks.
 * 
 * @since 3.2
 */
public class DelegatingRealm extends CurrentRealm {
	private Realm realm;

	public DelegatingRealm(Realm realm) {
		this.realm = realm;
	}

	protected void syncExec(Runnable runnable) {
		realm.exec(runnable);
	}

	public void asyncExec(Runnable runnable) {
		realm.asyncExec(runnable);
	}
}
