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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 *
 */
public class FooTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationListener {

	protected Button fButt = null;
	protected Set<String> fOptions = null;

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite comp = SWTFactory.createComposite(parent, parent.getFont(), 1, 1, GridData.FILL_BOTH);
		fButt = SWTFactory.createCheckButton(comp, Messages.FooTab_0, null, false, 1);
		fButt.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}

		});
		setControl(comp);
		getLaunchManager().addLaunchConfigurationListener(this);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	@Override
	public String getName() {
		return Messages.FooTab_1;
	}

	/**
	 * @return the set of modes this tab works with
	 */
	protected Set<String> getModes() {
		Set<String> set = new HashSet<String>();
		set.add(Messages.FooTab_2);
		return set;
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#dispose()
	 */
	@Override
	public void dispose() {
		getLaunchManager().removeLaunchConfigurationListener(this);
		super.dispose();
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			Set<String> modes = configuration.getModes();
			if (modes != null) {
				modes.add(getLaunchConfigurationDialog().getMode());
				fButt.setSelection(getModes().containsAll(modes));
			} else {
				fButt.setSelection(false);
			}
		} catch (CoreException ce) {
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (fButt.getSelection()) {
			configuration.addModes(getModes());
		} else {
			configuration.removeModes(getModes());
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	@Override
	public String getId() {
		return Messages.FooTab_3;
	}

	/**
	 * @see org.eclipse.debug.core.ILaunchConfigurationListener#launchConfigurationChanged(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public void launchConfigurationChanged(ILaunchConfiguration configuration) {
		try {
			Set<String> modes = configuration.getModes();
			modes.add(getLaunchConfigurationDialog().getMode());
			if (!fButt.isDisposed()) {
				boolean sel = fButt.getSelection();
				if (!sel & modes.containsAll(getModes())) {
					fButt.setSelection(true);
				} else if (sel & !modes.containsAll(getModes())) {
					fButt.setSelection(false);
				}
			}
		} catch (CoreException ce) {
			DebugUIPlugin.log(ce);
		}
	}

	/**
	 * @param configuration
	 */
	@Override
	public void launchConfigurationAdded(ILaunchConfiguration configuration) {
	}

	/**
	 * @param configuration
	 */
	@Override
	public void launchConfigurationRemoved(ILaunchConfiguration configuration) {
	}
}
