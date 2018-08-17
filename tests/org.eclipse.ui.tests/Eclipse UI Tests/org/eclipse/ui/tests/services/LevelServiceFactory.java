/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.tests.services;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

/**
 * @since 3.4
 *
 */
public class LevelServiceFactory extends AbstractServiceFactory {

	static int instancesCreated = 0;

	private static class LS implements ILevelService {
		private int level;

		public LS(int l) {
			level = l;
		}

		@Override
		public int getLevel() {
			return level;
		}

	}

	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator,
			IServiceLocator locator) {
		int level = 1;
		Object parent = parentLocator.getService(ILevelService.class);
		if (parent != null) {
			ILevelService l = (ILevelService) parent;
			level = l.getLevel() + 1;
		}
		instancesCreated++;
		return new LS(level);
	}

}
