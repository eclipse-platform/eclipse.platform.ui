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
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor.hover;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlCreatorExtension;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.swt.widgets.Shell;

public class CompositeInformationControlCreator
		implements IInformationControlCreator, IInformationControlCreatorExtension {

	private final LinkedHashMap<ITextHover, IInformationControlCreator> creators;

	public CompositeInformationControlCreator(List<ITextHover> hovers) {
		this.creators = new LinkedHashMap<>();
		for (ITextHover hover : hovers) {
			creators.put(hover, getInformationControlCreator(hover));
		}
	}

	public CompositeInformationControlCreator(LinkedHashMap<ITextHover, IInformationControlCreator> creators) {
		this.creators = creators;
	}

	private static IInformationControlCreator getInformationControlCreator(ITextHover hover) {
		IInformationControlCreator controlCreator = null;
		if (hover instanceof ITextHoverExtension) {
			controlCreator = ((ITextHoverExtension)hover).getHoverControlCreator();
		}
		if (controlCreator == null) {
			controlCreator = new AbstractReusableInformationControlCreator() {
				@Override
				protected IInformationControl doCreateInformationControl(Shell parent) {
					return new DefaultInformationControl(parent, true);
				}
			};
		}
		return controlCreator;
	}


	@Override
	public boolean canReuse(IInformationControl control) {
		if (control.getClass() != CompositeInformationControl.class) {
			return false;
		}
		CompositeInformationControl other = (CompositeInformationControl)control;
		if (!other.creators.equals(this.creators)) {
			return false;
		}
		Iterator<Entry<ITextHover, IInformationControlCreator>> thisIterator = this.creators.entrySet().iterator();
		Iterator<Entry<ITextHover, IInformationControl>> otherIterator = other.controls.entrySet().iterator();
		do {
			Entry<ITextHover, IInformationControlCreator> thisEntry = thisIterator.next();
			Entry<ITextHover, IInformationControl> otherEntry = otherIterator.next();
			if (!thisEntry.getKey().equals(otherEntry.getKey())) {
				return false;
			}
			if (!(thisEntry.getValue() instanceof IInformationControlCreatorExtension)) {
				return false;
			}
			if (!((IInformationControlCreatorExtension)thisEntry.getValue()).canReuse(otherEntry.getValue())) {
				return false;
			}
		} while (thisIterator.hasNext());
		return true;
	}

	@Override
	public boolean canReplace(IInformationControlCreator creator) {
		if (creator.getClass() != this.getClass()) {
			return false;
		}
		CompositeInformationControlCreator other = (CompositeInformationControlCreator)creator;
		if (other.creators.size() != this.creators.size()) {
			return false;
		}
		Iterator<Entry<ITextHover, IInformationControlCreator>> thisIterator = this.creators.entrySet().iterator();
		Iterator<Entry<ITextHover, IInformationControlCreator>> otherIterator = other.creators.entrySet().iterator();
		do {
			Entry<ITextHover, IInformationControlCreator> thisEntry = thisIterator.next();
			Entry<ITextHover, IInformationControlCreator> otherEntry = otherIterator.next();
			if (!thisEntry.getKey().equals(otherEntry.getKey())) {
				return false;
			}
			if (!(thisEntry.getValue() instanceof IInformationControlCreatorExtension)) {
				return false;
			}
			if (!((IInformationControlCreatorExtension)thisEntry.getValue()).canReplace(otherEntry.getValue())) {
				return false;
			}
		} while (thisIterator.hasNext());
		return true;
	}
	

	@Override
	public IInformationControl createInformationControl(Shell parent) {
		return new CompositeInformationControl(parent, this.creators);
	}

}
