/*******************************************************************************
 * Copyright (c) 2022 EclipseSource GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     EclipseSource GmbH - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.internal.compatibiliy.migration;

import java.util.List;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.ui.internal.Workbench;

@SuppressWarnings("restriction")
public final class WorkbenchUtil {
	
	private WorkbenchUtil() {}

	/**
	 * Retrieve the IEclipseContext from the Workbench.
	 *
	 * @return The IEclipseContext
	 */
	private static IEclipseContext getEclipseContext() {
		return Workbench.getInstance().getContext();
	}

	/**
	 * Retrieve the EModelService from the EclipseContext.
	 *
	 * @return The EModelService
	 */
	public static EModelService getEModelService() {
		return getEclipseContext().get(EModelService.class);
	}
	
	/**
	 * Retrieve the first MWindow from the MApplication from the EclipseContext.
	 *
	 * @return The MWindow
	 */
	public static MWindow getCurrentMainWindow() {
		MApplication mApplication = getEclipseContext().get(MApplication.class);
		List<MWindow> children = mApplication.getChildren();
		MWindow mWindow = children.get(0);
		return mWindow;
	}
}
