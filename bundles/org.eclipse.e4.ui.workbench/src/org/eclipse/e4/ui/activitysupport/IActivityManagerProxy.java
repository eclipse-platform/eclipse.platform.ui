/*******************************************************************************
 * Copyright (c) 2024 Advantest Europe GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 				Raghunandana Murthappa
 *******************************************************************************/
package org.eclipse.e4.ui.activitysupport;

/**
 * A bridge between org.eclipse.ui.workbench and
 * org.eclipse.e4.ui.workbench.renderers.swt.
 *
 * Service for this interface is bound to Platform.class bundle at
 * Workbench.class. We cannot depend on org.eclipse.ui.workbench from
 * org.eclipse.e4.ui.workbench.renderers.swt
 *
 * @since 1.16
 */
public interface IActivityManagerProxy {
	/**
	 * Checks whether the given element is enabled or not in the workbench activity
	 * support.
	 *
	 * @param identifierId A qualified id if the contribution. Which has format of
	 *                     bundle-id/element. Ex:
	 *                     org.eclipse.pde.spy.core/org.eclipse.pde.spy.core.SpyProcessor
	 * @return {@code true} if the given identifierId is enabled within workbench
	 *         activity support.
	 */
	public boolean isIdentifierEnabled(String identifierId);
}
