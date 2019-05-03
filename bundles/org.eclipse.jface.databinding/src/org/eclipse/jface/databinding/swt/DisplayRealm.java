/*******************************************************************************
 * Copyright (c) 2014, 2018 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation based on SWTObservable.getRealm
 *******************************************************************************/
package org.eclipse.jface.databinding.swt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.swt.widgets.Display;

/**
 * This class is used to get a {@link Realm} for a certain {@link Display}.
 *
 * @since 1.7
 *
 */
public class DisplayRealm extends Realm {

	private static List<DisplayRealm> realms = new ArrayList<DisplayRealm>();

	/**
	 * Returns the realm representing the UI thread for the given display.
	 *
	 * @param display
	 * @return the realm representing the UI thread for the given display
	 */
	public static Realm getRealm(final Display display) {
		synchronized (realms) {
			for (DisplayRealm element : realms) {
				if (element.display == display) {
					return element;
				}
			}
			DisplayRealm result = new DisplayRealm(display);
			realms.add(result);
			return result;
		}
	}

	private Display display;

	/**
	 * @param display
	 */
	private DisplayRealm(Display display) {
		this.display = display;
	}

	@Override
	public boolean isCurrent() {
		return Display.getCurrent() == display;
	}

	@Override
	public void asyncExec(final Runnable runnable) {
		Runnable safeRunnable = () -> safeRun(runnable);
		if (!display.isDisposed()) {
			display.asyncExec(safeRunnable);
		}
	}

	@Override
	public void timerExec(int milliseconds, final Runnable runnable) {
		if (!display.isDisposed()) {
			Runnable safeRunnable = () -> safeRun(runnable);
			display.timerExec(milliseconds, safeRunnable);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(display);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DisplayRealm other = (DisplayRealm) obj;
		return Objects.equals(display, other.display);
	}
}