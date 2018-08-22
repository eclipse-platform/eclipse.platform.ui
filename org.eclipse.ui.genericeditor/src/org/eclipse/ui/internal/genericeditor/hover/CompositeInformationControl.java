/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 * - Lucas Bullen (Red Hat Inc.) - [Bug 527071] Empty string as content breaks hover
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor.hover;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IInformationControlExtension5;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.genericeditor.GenericEditorPlugin;

public class CompositeInformationControl extends AbstractInformationControl implements IInformationControlExtension2 {

	final LinkedHashMap<ITextHover, IInformationControlCreator> creators;
	LinkedHashMap<ITextHover, IInformationControl> controls;

	public CompositeInformationControl(Shell parentShell, LinkedHashMap<ITextHover, IInformationControlCreator> creators) {
		super(parentShell, true); // TODO check best constructor
		Assert.isLegal(creators.size() > 1, "Do not compose a unique hover"); //$NON-NLS-1$
		this.creators = creators;
		create();
	}

	@Override
	public boolean hasContents() {
		for (IInformationControl control : controls.values()) {
			if (control instanceof IInformationControlExtension) {
				if (((IInformationControlExtension)control).hasContents()) {
					return true;
				}
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public void setInput(Object input) {
		@SuppressWarnings("unchecked")
		Map<ITextHover, Object> inputs = (Map<ITextHover, Object>)input;
		for (Entry<ITextHover, Object> entry : inputs.entrySet()) {
			IInformationControl informationControl = controls.get(entry.getKey());
			if (informationControl != null) {
				if (informationControl instanceof IInformationControlExtension2) {
					((IInformationControlExtension2)informationControl).setInput(entry.getValue());
					continue;
				}
				String information = entry.getValue().toString();
				if(!information.isEmpty()){
					informationControl.setInformation(information);
				}
			}
		}
	}

	@Override
	public void createContent(Composite parent) {
		this.controls = new LinkedHashMap<>(); // TODO maybe use canReuse or canReplace
		GridLayout layout = new GridLayout(1, false);
		parent.setLayout(layout);
		boolean firstControl = true;
		for (Entry<ITextHover, IInformationControlCreator> hoverControlCreator : this.creators.entrySet()) {
			IInformationControl informationControl = hoverControlCreator.getValue().createInformationControl(parent.getShell());
			if (informationControl instanceof AbstractInformationControl) {
				List<Control> children = Arrays.asList(((AbstractInformationControl)informationControl).getShell().getChildren());
				children.remove(parent);
				if (children.size() == 0 ) {
					continue;
				}
				for (Control control : children) {
					control.setParent(parent);
					control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				}
				if (!firstControl) {
					((GridData)children.get(0).getLayoutData()).verticalIndent = 15;
				}
				controls.put(hoverControlCreator.getKey(), informationControl);
				firstControl = false;
			} else {
				GenericEditorPlugin.getDefault().getLog().log(new Status(IStatus.WARNING, GenericEditorPlugin.BUNDLE_ID,
						"Only text hovers producing an AbstractInformationControl can be aggregated; got a " + informationControl.getClass().getSimpleName())); //$NON-NLS-1$
				informationControl.dispose();
			}
		}
	}

	@Override
	public void dispose() {
		controls.values().forEach(IInformationControl::dispose);
		controls.clear();
		super.dispose();
	}

	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		if (controls.isEmpty()) {
			return null;
		} else if (controls.size() == 1) {
			IInformationControl control = controls.values().iterator().next();
			if (control instanceof IInformationControlExtension5) {
				return ((IInformationControlExtension5)control).getInformationPresenterControlCreator();
			}
		} else {
			LinkedHashMap<ITextHover, IInformationControlCreator> presenterCreators = new LinkedHashMap<>();
			boolean allNull = true;
			for (Entry<ITextHover, IInformationControl> hover : this.controls.entrySet()) {
				IInformationControlCreator creator = null;
				if (hover.getValue() instanceof IInformationControlExtension5)
				creator = ((IInformationControlExtension5)hover.getValue()).getInformationPresenterControlCreator();
				if (creator == null) {
					creator = this.creators.get(hover.getKey());
				} else {
					allNull = false;
				}
				if (creator != null) {
					presenterCreators.put(hover.getKey(), creator);
				}
			}
			if (allNull) {
				return null;
			}
			return new CompositeInformationControlCreator(presenterCreators);
		}
		return null;
	}

	@Override
	public Point computeSizeHint() {
		return getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
	}

}
