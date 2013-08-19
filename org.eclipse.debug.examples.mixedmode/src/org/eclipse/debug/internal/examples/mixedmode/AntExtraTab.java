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

public class AntExtraTab extends DoNothingMainTab {

	/**
	 * @see org.eclipse.debug.internal.examples.mixedmode.DoNothingMainTab#getName()
	 */
	@Override
	public String getName() {
		return Messages.AntExtraTab_0;
	}

	/**
	 * @see org.eclipse.debug.internal.examples.mixedmode.DoNothingMainTab#getId()
	 */
	@Override
	public String getId() {
		return "org.eclipse.debug.examples.mixedmode.ant.tab"; //$NON-NLS-1$
	}

	/**
	 * @return the set of modes this tab works with
	 */
	@Override
	public Set<String> getModes() {
		if (fOptions == null) {
			fOptions = new HashSet<String>();
			fOptions.add("ant"); //$NON-NLS-1$
			fOptions.add("test"); //$NON-NLS-1$
		}
		return fOptions;
	}

}
