/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Patrick Chuong (Texas Instruments) - Checkbox support for Flexible Hierachy view (Bug 286310)
 *******************************************************************************/
package org.eclipse.debug.examples.ui.midi.adapters;

import org.eclipse.debug.examples.ui.pda.views.CheckboxView;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Column presentation factory for a sequencer.
 * 
 * @since 1.0
 */
public class SequencerColumnFactory implements IColumnPresentationFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory#createColumnPresentation(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
	 */
	@Override
	public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
		if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(context.getId()) || CheckboxView.ID.equals(context.getId())) {
			return new SequencerColumnPresentation();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory#getColumnPresentationId(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
	 */
	@Override
	public String getColumnPresentationId(IPresentationContext context, Object element) {
		if (IDebugUIConstants.ID_VARIABLE_VIEW.equals(context.getId()) || CheckboxView.ID.equals(context.getId())) {
			return SequencerColumnPresentation.ID;
		}
		return null;
	}

}
