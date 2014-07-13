/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.common;

import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * Adds auto complete functionality to a combo viewer. It is assumed that the
 * viewer is created with SWT.SIMPLE and SWT.READONLY flags. The proposals will
 * be dynamically generated from the viewers current contents.
 *
 * @author Steven Spungin
 *
 */
public class ComboViewerAutoComplete {

	private ContentProposalAdapter adapter;

	public ComboViewerAutoComplete(final ComboViewer dropDown) {
		// use all items in the wrapped combo as proposals to filter instead of
		// static String[]
		SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(new String[0]) {
			@Override
			public IContentProposal[] getProposals(String contents, int position) {
				String[] proposals = dropDown.getCombo().getItems();
				setProposals(proposals);
				return super.getProposals(contents, position);
			}
		};
		// set the viewer instead of the wrapped combo
		ComboContentAdapter contentAdapter = new ComboContentAdapter() {
			@Override
			public void setControlContents(Control control, String text1, int cursorPosition) {
				super.setControlContents(control, text1, cursorPosition);
				int index = Arrays.asList(dropDown.getCombo().getItems()).indexOf(text1);
				Object object = dropDown.getElementAt(index);
				dropDown.setSelection(new StructuredSelection(object));
			}
		};
		proposalProvider.setFiltering(true);
		adapter = new ContentProposalAdapter(dropDown.getCombo(), contentAdapter, proposalProvider, null, null);
		adapter.setPropagateKeys(true);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

		// Focus the entire field after tab or click
		dropDown.getCombo().addFocusListener(new FocusListener() {

			private Object valueWhenFocused;

			@Override
			public void focusLost(FocusEvent e) {
				// Do not allow a null value (e.g. the user clicks with
				// autocomplete text not set
				Object object = (((IStructuredSelection) dropDown.getSelection())).getFirstElement();
				if (object == null) {
					dropDown.setSelection(new StructuredSelection(valueWhenFocused));
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				valueWhenFocused = (((IStructuredSelection) dropDown.getSelection())).getFirstElement();

				// If the user clicks the text field (versus tab), the
				// text will be unselected after we set the selection, so we
				// need to delay the call
				// THIS IS A WORKAROUND
				final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
				executor.schedule(new Runnable() {
					@Override
					public void run() {
						dropDown.getCombo().getDisplay().syncExec(new Runnable() {

							@Override
							public void run() {
								dropDown.getCombo().setSelection(new Point(0, dropDown.getCombo().getText().length()));
							}
						});
					}
				}, 200, TimeUnit.MILLISECONDS);
			}
		});
	}

	public boolean isProposalPopupOpen() {
		return adapter.isProposalPopupOpen();
	}

}
