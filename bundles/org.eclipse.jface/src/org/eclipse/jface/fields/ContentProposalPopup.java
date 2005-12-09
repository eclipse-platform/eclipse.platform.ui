/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.fields;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * A lightweight popup used to show content proposals for a text field. If
 * additional information exists for a proposal, then selecting that proposal
 * will result in the information being displayed in a secondary popup.
 * 
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters. For now, there is limited API to control how the popup is sized or
 * placed relative to the control, or whether it is resizable. The intention is
 * that standard techniques will be implemented to "do the right thing"
 * depending on the associated control's size/location and the content size, and
 * API added if we discover there are additional choices to be made.
 * 
 * @since 3.2
 */
public class ContentProposalPopup extends PopupDialog {

	/*
	 * Set to <code>true</code> to use a Table with SWT.VIRTUAL. This is a
	 * workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=98585#c40
	 * The corresponding SWT bug is
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=90321
	 */
	private static final boolean USE_VIRTUAL = !"motif".equals(SWT.getPlatform()); //$NON-NLS-1$

	/*
	 * The delay before showing a secondary popup.
	 */
	private static final int POPUP_DELAY = 500;

	/*
	 * The character width hint for the popup and secondary popup. Used as a
	 * minimum width.
	 */
	private static final int POPUP_CHARWIDTH = 20;

	/*
	 * Empty string.
	 */
	private static final String EMPTY = ""; //$NON-NLS-1$

	/*
	 * The listener we will install on the target control.
	 */
	private final class TargetControlListener implements Listener {
		// Key events from the control
		public void handleEvent(Event e) {
			if (!isValid())
				return;

			if (!(e.type == SWT.KeyDown || e.type == SWT.Traverse))
				return;

			// Traverse events will be blocked when the popup is open, but
			// we will interpret their characters as navigation within the
			// popup.
			if (e.type == SWT.Traverse) {
				e.detail = SWT.TRAVERSE_NONE;
				e.doit = true;
			}

			char key = e.character;

			// No character. Check for navigation keys.

			if (key == 0) {
				int newSelection = proposalTable.getSelectionIndex();
				int visibleRows = (proposalTable.getSize().y / proposalTable
						.getItemHeight()) - 1;
				switch (e.keyCode) {
				case SWT.ARROW_UP:
					newSelection -= 1;
					if (newSelection < 0)
						newSelection = proposalTable.getItemCount() - 1;
					break;

				case SWT.ARROW_DOWN:
					newSelection += 1;
					if (newSelection > proposalTable.getItemCount() - 1)
						newSelection = 0;
					break;

				case SWT.PAGE_DOWN:
					newSelection += visibleRows;
					if (newSelection >= proposalTable.getItemCount())
						newSelection = proposalTable.getItemCount() - 1;
					break;

				case SWT.PAGE_UP:
					newSelection -= visibleRows;
					if (newSelection < 0)
						newSelection = 0;
					break;

				case SWT.HOME:
					newSelection = 0;
					break;

				case SWT.END:
					newSelection = proposalTable.getItemCount() - 1;
					break;

				// Any unknown keycodes will cause the popup to close.
				// Modifier keys are explicitly checked and ignored because
				// they are not complete yet (no character).
				default:
					if (e.keyCode != SWT.CAPS_LOCK && e.keyCode != SWT.MOD1
							&& e.keyCode != SWT.MOD2 && e.keyCode != SWT.MOD3
							&& e.keyCode != SWT.MOD4)
						close();
					return;
				}

				// If any of these navigation events caused a new selection,
				// then handle that now and return.
				if (newSelection >= 0)
					selectProposal(newSelection);
				return;
			}

			// key != 0
			// Check for special keys involved in cancelling, accepting, or
			// filtering the proposals.
			switch (key) {
			case SWT.ESC: // Esc
				e.doit = false;
				close();
				break;

			case SWT.LF:
			case SWT.CR:
				e.doit = false;
				Object p = getSelectedProposal();
				if (p != null)
					acceptCurrentProposal();
				close();
				break;

			case SWT.TAB:
				e.doit = false;
				getShell().setFocus();
				return;

			case SWT.BS:
				// Backspace should back out of any stored filter text
				if (proposalProvider instanceof IFilteringContentProposalProvider) {
					filterText = filterText.substring(0,
							filterText.length() - 1);
					recomputeProposals(filterText);
					return;
				}

			default:
				// If the key is a defined unicode character, and not one of the
				// special cases processed above, update the filter text and
				// filter the proposals.
				if (proposalProvider instanceof IFilteringContentProposalProvider
						&& Character.isDefined(key)) {
					filterText = filterText + String.valueOf(key);
					recomputeProposals(filterText);
				}
				break;
			}
		}
	}

	/*
	 * Internal class used to implement the secondary popup.
	 */
	private class InfoPopupDialog extends PopupDialog {

		/*
		 * The text control that displays the text.
		 */
		private Text text;

		/*
		 * The String shown in the popup.
		 */
		private String contents = EMPTY;

		/*
		 * Construct an info-popup with the specified parent.
		 */
		InfoPopupDialog(Shell parent) {
			super(parent, PopupDialog.HOVER_SHELLSTYLE, false, false, false,
					false, null, null);
		}

		/*
		 * Create a text control for showing the info about a proposal.
		 */
		protected Control createDialogArea(Composite parent) {
			text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP
					| SWT.NO_FOCUS);

			// Use the compact margins employed by PopupDialog.
			GridData gd = new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
			gd.horizontalIndent = PopupDialog.POPUP_HORIZONTALSPACING;
			gd.verticalIndent = PopupDialog.POPUP_VERTICALSPACING;
			text.setLayoutData(gd);
			text.setText(contents);

			// since SWT.NO_FOCUS is only a hint...
			text.addFocusListener(new FocusAdapter() {
				public void focusGained(FocusEvent event) {
					ContentProposalPopup.this.close();
				}
			});
			return text;
		}

		/*
		 * Adjust the bounds so that we appear adjacent to our parent shell
		 */
		protected void adjustBounds() {
			Rectangle parentBounds = getParentShell().getBounds();
			getShell().setBounds(
					new Rectangle(parentBounds.x + parentBounds.width
							+ PopupDialog.POPUP_HORIZONTALSPACING,
							parentBounds.y + PopupDialog.POPUP_VERTICALSPACING,
							parentBounds.width, parentBounds.height));

		}

		/*
		 * Set the text contents of the popup.
		 */
		void setContents(String newContents) {
			if (newContents == null)
				newContents = EMPTY;
			this.contents = newContents;
			if (text != null && !text.isDisposed()) {
				text.setText(contents);
			}
		}
	}

	/*
	 * The adapter on the target control.
	 */
	private ContentProposalAdapter adapter;

	/*
	 * The listener installed on the target control.
	 */
	private Listener controlListener;

	/*
	 * The control on which we are proposing content.
	 */
	private Control control;

	/*
	 * The table used to show the list of proposals.
	 */
	private Table proposalTable;

	/*
	 * The provider of proposals and proposal descriptions
	 */
	private IContentProposalProvider proposalProvider;

	/*
	 * The proposals to be shown (cached to avoid repeated requests).
	 */
	private Object[] proposals;

	/*
	 * A label provider used to show the proposals in the table.
	 */
	private ILabelProvider labelProvider;

	/*
	 * Secondary popup used to show detailed information about the selected
	 * proposal..
	 */
	private InfoPopupDialog infoPopup;

	/*
	 * Filter text - tracked while popup is open, only if the
	 * IContentProposalProvider implements filtering.
	 */
	private String filterText = EMPTY;

	/**
	 * Constructs a new instance of this popup, specifying the control for which
	 * this popup is showing content, and how the proposals should be obtained
	 * and displayed.
	 * 
	 * @param adapter
	 *            the content proposal adapter that opened this dialog.
	 * @param control
	 *            the control whose content is being proposed.
	 * @param proposalProvider
	 *            the provider of content proposals
	 * @param infoText
	 *            Text to be shown in a lower info area, or <code>null</code>
	 *            if there is no info area.
	 * @param labelProvider
	 *            the {@link ILabelProvider} used to obtain visual content for
	 *            the proposals. Must never be <code>null</code>.
	 */
	public ContentProposalPopup(ContentProposalAdapter adapter,
			Control control, IContentProposalProvider proposalProvider,
			String infoText, ILabelProvider labelProvider) {
		super(control.getShell(), PopupDialog.INFOPOPUPRESIZE_SHELLSTYLE,
				false, false, false, false, null, infoText);
		this.adapter = adapter;
		this.control = control;
		this.labelProvider = labelProvider;
		this.proposalProvider = proposalProvider;
		this.proposals = getProposals(filterText);
	}

	/*
	 * Creates the content area for the proposal popup. This creates a table and
	 * places it inside the composite. The table will contain a list of all the
	 * proposals.
	 * 
	 * @param parent The parent composite to contain the dialog area; must not
	 * be <code>null</code>.
	 */
	protected final Control createDialogArea(final Composite parent) {
		// Use virtual where appropriate (see flag definition).
		if (USE_VIRTUAL) {
			proposalTable = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL
					| SWT.VIRTUAL);

			Listener listener = new Listener() {
				public void handleEvent(Event event) {
					handleSetData(event);
				}
			};
			proposalTable.addListener(SWT.SetData, listener);
		} else {
			proposalTable = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		}

		// compute the proposals to force population of the table.
		recomputeProposals(filterText);

		proposalTable.setHeaderVisible(false);
		proposalTable.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				// If a proposal has been selected, show it in the popup.
				// Otherwise close the popup.
				if (e.item == null) {
					if (infoPopup != null)
						infoPopup.close();
				} else {
					TableItem item = (TableItem) e.item;
					String description = getProposalDescription(item.getData());
					showProposalDescription(description);
				}
			}

			// Default selection was made. Accept the current proposal.
			public void widgetDefaultSelected(SelectionEvent e) {
				acceptCurrentProposal();
			}
		});

		// Compute a minimum width for the table.
		GridData data = new GridData(GridData.FILL_BOTH);
		GC gc = new GC(proposalTable);
		gc.setFont(proposalTable.getFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		data.widthHint = Dialog.convertWidthInCharsToPixels(fontMetrics,
				POPUP_CHARWIDTH);
		proposalTable.setLayoutData(data);

		return proposalTable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.PopupDialog.adjustBounds()
	 */
	protected void adjustBounds() {
		// Get our control's location in display coordinates.
		Point location = control.getDisplay().map(control.getParent(), null,
				control.getLocation());
		getShell().setLocation(location.x + 3,
				location.y + control.getSize().y + 3);
	}

	/*
	 * Handle the set data event. Set the item data of the requested item to the
	 * corresponding proposal in the proposal cache.
	 */
	private void handleSetData(Event event) {
		TableItem item = (TableItem) event.item;
		int index = proposalTable.indexOf(item);

		if (0 <= index && index < proposals.length) {
			Object current = proposals[index];
			item.setText(getString(current));
			item.setImage(getImage(current));
			item.setData(current);
		} else {
			// this should not happen, but does on win32
		}
	}

	/*
	 * Caches the specified proposals and repopulates the table if it has been
	 * created.
	 */
	private void setProposals(Object[] proposals) {
		if (proposals == null || proposals.length == 0) {
			proposals = getEmptyProposalArray();
		}
		this.proposals = proposals;

		// If there is a table
		if (isValid()) {
			final int newSize = proposals.length;
			if (USE_VIRTUAL) {
				// Set and clear the virtual table. Data will be
				// provided in the SWT.SetData event handler.
				proposalTable.setItemCount(newSize);
				proposalTable.clearAll();
			} else {
				// Populate the table manually
				proposalTable.setRedraw(false);
				proposalTable.setItemCount(newSize);
				TableItem[] items = proposalTable.getItems();
				for (int i = 0; i < items.length; i++) {
					TableItem item = items[i];
					Object proposal = proposals[i];
					item.setText(getString(proposal));
					item.setImage(getImage(proposal));
					item.setData(proposal);
				}
				proposalTable.setRedraw(true);
			}
			// Default to the first selection if there is content.
			if (proposals.length > 0) {
				selectProposal(0);
			} else {
				// No selection, close the secondary popup if it was open
				if (infoPopup != null)
					infoPopup.close();

			}
		}
	}

	/*
	 * Get the string for the specified proposal. Always return a String of some
	 * kind.
	 */
	private String getString(Object proposal) {
		if (proposal == null)
			return EMPTY;
		if (labelProvider == null)
			return proposal.toString();
		return labelProvider.getText(proposal);
	}

	/*
	 * Get the image for the specified proposal. If there is no image available,
	 * return null.
	 */
	private Image getImage(Object proposal) {
		if (proposal == null || labelProvider == null)
			return null;
		return labelProvider.getImage(proposal);
	}

	/*
	 * Return an empty array. Used so that something always shows in the
	 * proposal popup, even if no proposal provider was specified.
	 */
	private Object[] getEmptyProposalArray() {
		return new Object[0];
	}

	/*
	 * Add a listener to the target control. We monitor key and traverse events
	 * in the target control while we are open, in order to handle traversal and
	 * navigation requests.
	 */
	private void addControlListener() {
		controlListener = new TargetControlListener();
		control.addListener(SWT.KeyDown, controlListener);
		control.addListener(SWT.Traverse, controlListener);
	}

	/*
	 * Remove any listeners installed on the control.
	 */
	private void removeControlListener() {
		control.removeListener(SWT.KeyDown, controlListener);
		control.removeListener(SWT.Traverse, controlListener);
	}

	/*
	 * Answer true if the popup is valid, which means the table has been created
	 * and not disposed.
	 */
	private boolean isValid() {
		return proposalTable != null && !proposalTable.isDisposed();
	}

	/*
	 * Return the current selected proposal.
	 */
	private Object getSelectedProposal() {
		if (isValid()) {
			int i = proposalTable.getSelectionIndex();
			if (proposals == null || i < 0 || i >= proposals.length)
				return null;
			return proposals[i];
		}
		return null;
	}

	/*
	 * Select the proposal at the given index.
	 */
	private void selectProposal(int index) {
		Assert.isTrue(index >= 0, "Proposal index should never be negative"); //$NON-NLS-1$
		if (!isValid() || proposals == null || index >= proposals.length)
			return;
		proposalTable.setSelection(index);
		proposalTable.showSelection();

		String description = getProposalDescription(proposals[index]);
		showProposalDescription(description);
	}

	/**
	 * Opens this ContentProposalPopup. This method is extended in order to add
	 * the control listener when the popup is opened and to invoke the secondary
	 * popup if applicable.
	 * 
	 * @return the return code
	 * 
	 * @see org.eclipse.jface.window.Window#open()
	 */
	public int open() {
		addControlListener();
		int value = super.open();
		showProposalDescription(getProposalDescription(getSelectedProposal()));
		return value;
	}

	/**
	 * Closes this popup. This method is extended to remove the control
	 * listener.
	 * 
	 * @return <code>true</code> if the window is (or was already) closed, and
	 *         <code>false</code> if it is still open
	 */
	public boolean close() {
		removeControlListener();
		return super.close();
	}

	/*
	 * Get the proposals from the proposal provider. The provider may or may not
	 * filter the proposals based on the specified filter text.
	 */
	private Object[] getProposals(String filter) {
		if (proposalProvider == null)
			return null;
		if (proposalProvider instanceof IFilteringContentProposalProvider)
			return ((IFilteringContentProposalProvider) proposalProvider)
					.getProposals(filter);
		return proposalProvider.getProposals();
	}

	/*
	 * Get the description for the specified proposal.
	 */
	private String getProposalDescription(Object proposal) {
		if (proposalProvider == null)
			return null;
		return proposalProvider.getProposalDescription(proposal);
	}

	/*
	 * Show the proposal description in a secondary popup.
	 */
	private void showProposalDescription(String description) {
		// If we have not created an info popup yet, do so now.
		if (infoPopup == null && description != null) {
			// Create a thread that will sleep for the specified delay
			// before creating the popup. We do not use Jobs since this
			// code must be able to run independently of the Eclipse runtime.
			Runnable runnable = new Runnable() {
				public void run() {
					try {
						Thread.sleep(POPUP_DELAY);
					} catch (InterruptedException e) {
					}
					if (!isValid())
						return;
					getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							// The selection may have changed by the time we
							// open, so double check it.
							Object p = getSelectedProposal();
							if (p != null) {
								infoPopup = new InfoPopupDialog(getShell());
								infoPopup
										.setContents(getProposalDescription(p));
								infoPopup.open();
								infoPopup.getShell().addDisposeListener(
										new DisposeListener() {
											public void widgetDisposed(
													DisposeEvent event) {
												infoPopup = null;
											}
										});
							}
						}
					});
				}
			};
			Thread t = new Thread(runnable);
			t.start();
		} else if (infoPopup != null) {
			// We already have a popup, so just reset the contents.
			infoPopup.setContents(description);
		}
	}

	/*
	 * Accept the current proposal.
	 */
	private void acceptCurrentProposal() {
		adapter.proposalAccepted(getString(getSelectedProposal()));
		close();
	}

	/*
	 * Request the proposals from the proposal provider, and recompute any
	 * caches. Repopulate the popup if it is open.
	 */
	private void recomputeProposals(String filterText) {
		setProposals(getProposals(filterText));
	}
}
