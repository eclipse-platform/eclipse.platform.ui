/*******************************************************************************
 * Copyright (c) 2007, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.text;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;


/**
 * Abstract class for a reusable information control creators.
 *
 * @since 3.3
 */
public abstract class AbstractReusableInformationControlCreator implements IInformationControlCreator, IInformationControlCreatorExtension, DisposeListener {

	private Map<Shell, IInformationControl> fInformationControls= new HashMap<>();

	/**
	 * Creates the control.
	 *
	 * @param parent the parent shell
	 * @return the created information control
	 */
	protected abstract IInformationControl doCreateInformationControl(Shell parent);

	@Override
	public IInformationControl createInformationControl(Shell parent) {
		IInformationControl control= fInformationControls.get(parent);
		if (control == null) {
			control= doCreateInformationControl(parent);
			control.addDisposeListener(this);
			fInformationControls.put(parent, control);
		}
		return control;
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		Composite parent= null;
		if (e.widget instanceof Shell)
			parent= ((Shell)e.widget).getParent();
		if (parent instanceof Shell)
			fInformationControls.remove(parent);
	}


	@Override
	public boolean canReuse(IInformationControl control) {
		return fInformationControls.containsValue(control);
	}

	@Override
	public boolean canReplace(IInformationControlCreator creator) {
		return creator.getClass() == getClass();
	}
}
