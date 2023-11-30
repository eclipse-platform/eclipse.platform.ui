/*******************************************************************************
 * Copyright (c) 2005, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christoph Läubrich - refactor for E4
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench;

import java.text.MessageFormat;
import java.util.concurrent.Executor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;

/**
 * @since 3.1
 */
public class UIExtensionTracker extends ExtensionTracker {
	private Executor executor;
	private ILog log;

	public UIExtensionTracker(Executor executor, ILog log) {
		this.executor = executor;
		this.log = log;
	}

	@Override
	protected void applyRemove(final IExtensionChangeHandler handler, final IExtension removedExtension,
			final Object[] objects) {
		executor.execute(() -> {
			try {
				handler.removeExtension(removedExtension, objects);
			} catch (Exception e) {
				log(getClass(), "doRemove", e); //$NON-NLS-1$
			}
		});
	}

	@Override
	protected void applyAdd(final IExtensionChangeHandler handler, final IExtension addedExtension) {
		executor.execute(() -> {
			try {
				handler.addExtension(UIExtensionTracker.this, addedExtension);
			} catch (Exception e) {
				log(getClass(), "doAdd", e); //$NON-NLS-1$
			}
		});
	}

	private void log(Class<?> clazz, String methodName, Throwable t) {
		String msg = MessageFormat.format("Exception in {0}.{1}: {2}", //$NON-NLS-1$
				clazz.getName(), methodName, t);
		log.error(msg, t);
	}
}
