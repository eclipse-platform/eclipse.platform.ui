/*******************************************************************************
 * Copyright (c) 2019 ArSysOp and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workspace.markers;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.FrameworkUtil;

/**
 * Translates attribute values from a marker instance to UI-friendly form.
 *
 * As the class is intended to directly serve UI, it cannot afford fail-fast
 * strategy. On the other hand, retrieving an attribute from an arbitrary marker
 * implementation can be anyhow sophisticated and the interface itself is
 * designed to fail easily.
 *
 * Thus, {@linkplain #message(IMarker)} does not fail in the case of
 * {@linkplain IMarker#getAttribute} invocation misbehavior, but instead return
 * not-filled {@linkplain Optional} and delegates error processing to the
 * configured {@code reporter}.
 *
 * @see IMarker
 */
public final class Translation {

	private final Consumer<CoreException> reporter;

	/**
	 * Use {@link ILog} of enclosing bundle as {@link CoreException} consumer
	 */
	public Translation() {
		this(e -> Platform.getLog(FrameworkUtil.getBundle(Translation.class)).log(e.getStatus()));
	}

	/**
	 *
	 * @param reporter for a {@link CoreException} if any of them will be thrown
	 */
	public Translation(Consumer<CoreException> reporter) {
		Objects.requireNonNull(reporter);
		this.reporter = reporter;
	}

	/**
	 * Retrieves the value of the {@link IMarker#MESSAGE} attribute of the marker.
	 *
	 * @param marker
	 * @return the optional result
	 */
	public Optional<String> message(IMarker marker) {
		try {
			Object attribute = marker.getAttribute(IMarker.MESSAGE);
			if (attribute instanceof String) {
				String value = (String) attribute;
				return Optional.of(value);
			}
		} catch (CoreException e) {
			reporter.accept(e);
		}
		return Optional.empty();
	}

}
