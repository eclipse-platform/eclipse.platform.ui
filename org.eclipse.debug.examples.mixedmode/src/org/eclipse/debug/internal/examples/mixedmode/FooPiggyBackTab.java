/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.examples.mixedmode;

import java.util.HashSet;
import java.util.Set;

public class FooPiggyBackTab extends FooTab {

	/**
	 * @see org.eclipse.debug.internal.examples.mixedmode.FooTab#getName()
	 */
	@Override
	public String getName() {
		return Messages.FooPiggyBackTab_0;
	}

	/**
	 * @see org.eclipse.debug.internal.examples.mixedmode.FooTab#getId()
	 */
	@Override
	public String getId() {
		return Messages.FooPiggyBackTab_1;
	}

	/**
	 * @return the set of modes this tab works with
	 */
	@Override
	public Set<String> getModes() {
		if (fOptions == null) {
			fOptions = new HashSet<String>();
			fOptions.add(Messages.FooPiggyBackTab_2);
			fOptions.add(Messages.FooPiggyBackTab_3);
		}
		return fOptions;
	}

}
