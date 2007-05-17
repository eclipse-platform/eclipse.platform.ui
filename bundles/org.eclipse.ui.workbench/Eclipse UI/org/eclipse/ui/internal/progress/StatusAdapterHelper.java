/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.progress;

import java.util.HashMap;

import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;

/**
 * StatusAdapterHelper is a class for caching {@link StatusAdapter} instances to make sure
 * they are not created twice within the progress service.
 * @since 3.3
 */
public class StatusAdapterHelper {
	private static StatusAdapterHelper instance;

	private HashMap map;

	private StatusAdapterHelper() {
	}

	/**
	 * Return the singleton.
	 * @return StatusAdapterHelper
	 */
	public static StatusAdapterHelper getInstance() {
		if (instance == null) {
			instance = new StatusAdapterHelper();
		}
		return instance;
	}

	/**
	 * Set the {@link StatusAdapter} for the {@link JobInfo}
	 * @param info
	 * @param statusAdapter
	 */
	public void putStatusAdapter(JobInfo info, StatusAdapter statusAdapter) {
		if (map == null) {
			map = new HashMap();
		}
		map.put(info, statusAdapter);
	}

	/**
	 * Return the adapter for this info.
	 * @param info
	 * @return
	 */
	public StatusAdapter getStatusAdapter(JobInfo info) {
		if (map == null) {
			return null;
		}
		StatusAdapter statusAdapter = (StatusAdapter) map.remove(info);
		statusAdapter.setProperty(
				IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY,
				Boolean.FALSE);
		return statusAdapter;
	}

	public void clear() {
		if (map != null) {
			map.clear();
		}
	}
}
