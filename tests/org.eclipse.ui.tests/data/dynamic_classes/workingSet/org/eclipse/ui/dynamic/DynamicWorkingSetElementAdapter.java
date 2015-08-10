/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.dynamic;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetElementAdapter;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;

public class DynamicWorkingSetElementAdapter implements
		IWorkingSetElementAdapter {

	@Override
	public IAdaptable[] adaptElements(IWorkingSet ws, IAdaptable[] elements) {
		return new IAdaptable[] {new IIntroPart() {

			@Override
			public void addPropertyListener(IPropertyListener listener) {
			}

			@Override
			public void createPartControl(Composite parent) {

			}

			@Override
			public void dispose() {

			}

			@Override
			public IIntroSite getIntroSite() {
				return null;
			}

			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Image getTitleImage() {

				return null;
			}

			@Override
			public void init(IIntroSite site, IMemento memento)
					throws PartInitException {


			}

			@Override
			public void removePropertyListener(IPropertyListener listener) {


			}

			@Override
			public void saveState(IMemento memento) {


			}

			@Override
			public void setFocus() {


			}

			@Override
			public void standbyStateChanged(boolean standby) {


			}

			@Override
			public Object getAdapter(Class adapter) {
				return null;
			}}};
	}

	@Override
	public void dispose() {

	}

}
