/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.intro;

import java.util.ArrayList;

import org.eclipse.ui.intro.IIntroDescriptor;

/**
 * <em>EXPERIMENTAL</em>
 * 
 * @since 3.0
 */
public class IntroRegistry implements IIntroRegistry {

	private ArrayList intros = new ArrayList(10);
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.intro.IIntroRegistry#add(org.eclipse.ui.intro.IIntroDescriptor)
	 */
	public void add(IIntroDescriptor descriptor) {
		intros.add(descriptor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.intro.IIntroRegistry#getIntroCount()
	 */
	public int getIntroCount() {
		return intros.size();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.intro.IIntroRegistry#getIntros()
	 */
	public IIntroDescriptor[] getIntros() {
		return (IIntroDescriptor[]) intros.toArray(new IIntroDescriptor[intros.size()]);
	}
}
