/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.ui.midi.detailpanes;

import org.eclipse.debug.examples.core.midi.launcher.TempoControl;
import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * A slider to control tempo.
 * 
 * @since 1.0
 */
public class TempoSliderDetailPane implements IDetailPane {
	
	private Slider fSlider;
	private TempoControl fControl;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		fSlider = new Slider(parent, SWT.HORIZONTAL);
		fSlider.setMinimum(20);
		fSlider.setMaximum(500);
		fSlider.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				int selection = fSlider.getSelection();
				if (fControl != null) {
					fControl.setValue(Integer.toString(selection));
				}
			}
		});
		return fSlider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#display(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void display(IStructuredSelection selection) {
		fControl = null;
		if (selection == null || selection.isEmpty()) {
			fSlider.setEnabled(false);
		} else {
			fSlider.setEnabled(true);
			fControl = (TempoControl) selection.getFirstElement();
			int bpm = (int)fControl.getSequencer().getTempoInBPM();
			fSlider.setSelection(bpm);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#getDescription()
	 */
	public String getDescription() {
		return "Tempo (beats per minute)"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#getID()
	 */
	public String getID() {
		return ControlDetailPaneFactory.ID_TEMPO_SLIDER;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#getName()
	 */
	public String getName() {
		return "Tempo Slider (BPM)"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#init(org.eclipse.ui.IWorkbenchPartSite)
	 */
	public void init(IWorkbenchPartSite partSite) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#setFocus()
	 */
	public boolean setFocus() {
		fSlider.setFocus();
		return true;
	}

}
