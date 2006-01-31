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
package org.eclipse.jface.fieldassist;

import java.util.ArrayList;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.util.Assert;
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
 * ContentProposalAdapter can be used to attach content proposal behavior to a
 * control. This behavior includes obtaining proposals, opening a popup dialog,
 * managing the content of the control relative to the selections in the popup,
 * and optionally opening up a secondary popup to further describe proposals.
 * 
 * <p>
 * This API is considered experimental. It is still evolving during 3.2 and is
 * subject to change. It is being released to obtain feedback from early
 * adopters.
 * 
 * @since 3.2
 */
public class ContentProposalAdapter {

	/*
	 * The lightweight popup used to show content proposals for a text field. If
	 * additional information exists for a proposal, then selecting that
	 * proposal will result in the information being displayed in a secondary
	 * popup.
	 */
	class ContentProposalPopup extends PopupDialog {

		/*
		 * The listener we will install on the target control.
		 */
		private final class TargetControlListener implements Listener {
			// Key events from the control
			public void handleEvent(Event e) {
				if (!isValid())
					return;

				// If we registered for any event that's not KeyDown or
				// Traverse,
				// it means we want to close the popup.
				if (!(e.type == SWT.KeyDown || e.type == SWT.Traverse)) {
					close();
					return;
				}

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
								&& e.keyCode != SWT.MOD2
								&& e.keyCode != SWT.MOD3
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
					if (filter) {
						filterText = filterText.substring(0, filterText
								.length() - 1);
						recomputeProposals(filterText);
						return;
					}

				default:
					// If the key is a defined unicode character, and not one of
					// the
					// special cases processed above, update the filter text and
					// filter the proposals.
					if (filter && Character.isDefined(key)) {
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
				super(parent, PopupDialog.HOVER_SHELLSTYLE, false, false,
						false, false, null, null);
			}

			/*
			 * Create a text control for showing the info about a proposal.
			 */
			protected Control createDialogArea(Composite parent) {
				text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP
						| SWT.NO_FOCUS);

				// Use the compact margins employed by PopupDialog.
				GridData gd = new GridData(GridData.BEGINNING
						| GridData.FILL_BOTH);
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
								parentBounds.y
										+ PopupDialog.POPUP_VERTICALSPACING,
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
		 * The listener installed on the target control.
		 */
		private Listener targetControlListener;

		/*
		 * The table used to show the list of proposals.
		 */
		private Table proposalTable;

		/*
		 * The proposals to be shown (cached to avoid repeated requests).
		 */
		private IContentProposal[] proposals;

		/*
		 * Secondary popup used to show detailed information about the selected
		 * proposal..
		 */
		private InfoPopupDialog infoPopup;

		/*
		 * Filter text - tracked while popup is open, only if we are told to
		 * filter
		 */
		private String filterText = EMPTY;

		/*
		 * Boolean that indicates whether we are filtering.
		 */
		private boolean filter = false;

		/**
		 * Constructs a new instance of this popup, specifying the control for
		 * which this popup is showing content, and how the proposals should be
		 * obtained and displayed.
		 * 
		 * @param adapter
		 *            the content proposal adapter that opened this dialog.
		 * @param control
		 *            the control whose content is being proposed.
		 * @param infoText
		 *            Text to be shown in a lower info area, or
		 *            <code>null</code> if there is no info area.
		 * @param filter
		 *            a boolean indicating whether proposals should be filtered
		 *            as keys are typed in the popup.
		 */
		ContentProposalPopup(String infoText, boolean filter) {
			super(control.getShell(), PopupDialog.INFOPOPUP_SHELLSTYLE, false,
					false, false, false, null, infoText);
			this.proposals = getProposals(filterText);
			this.filter = filter;
		}

		/*
		 * Creates the content area for the proposal popup. This creates a table
		 * and places it inside the composite. The table will contain a list of
		 * all the proposals.
		 * 
		 * @param parent The parent composite to contain the dialog area; must
		 * not be <code>null</code>.
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
						IContentProposal proposal = (IContentProposal) item
								.getData();
						showProposalDescription(proposal.getDescription());
					}
				}

				// Default selection was made. Accept the current proposal.
				public void widgetDefaultSelected(SelectionEvent e) {
					acceptCurrentProposal();
				}
			});

			// Compute a height and width for the table.
			GridData data = new GridData(GridData.FILL_BOTH);
			GC gc = new GC(proposalTable);
			gc.setFont(proposalTable.getFont());
			FontMetrics fontMetrics = gc.getFontMetrics();
			gc.dispose();
			data.widthHint = control.getBounds().width;
			data.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics,
					POPUP_CHARHEIGHT);
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
			Point location = control.getDisplay().map(control.getParent(),
					null, control.getLocation());
			getShell().setLocation(location.x + 3,
					location.y + control.getSize().y + 3);
		}

		/*
		 * Handle the set data event. Set the item data of the requested item to
		 * the corresponding proposal in the proposal cache.
		 */
		private void handleSetData(Event event) {
			TableItem item = (TableItem) event.item;
			int index = proposalTable.indexOf(item);

			if (0 <= index && index < proposals.length) {
				IContentProposal current = proposals[index];
				item.setText(getString(current));
				item.setImage(getImage(current));
				item.setData(current);
			} else {
				// this should not happen, but does on win32
			}
		}

		/*
		 * Caches the specified proposals and repopulates the table if it has
		 * been created.
		 */
		private void setProposals(IContentProposal[] newProposals) {
			if (newProposals == null || newProposals.length == 0) {
				newProposals = getEmptyProposalArray();
			}
			this.proposals = newProposals;

			// If there is a table
			if (isValid()) {
				final int newSize = newProposals.length;
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
						IContentProposal proposal = newProposals[i];
						item.setText(getString(proposal));
						item.setImage(getImage(proposal));
						item.setData(proposal);
					}
					proposalTable.setRedraw(true);
				}
				// Default to the first selection if there is content.
				if (newProposals.length > 0) {
					selectProposal(0);
				} else {
					// No selection, close the secondary popup if it was open
					if (infoPopup != null)
						infoPopup.close();

				}
			}
		}

		/*
		 * Get the string for the specified proposal. Always return a String of
		 * some kind.
		 */
		private String getString(IContentProposal proposal) {
			if (proposal == null)
				return EMPTY;
			if (labelProvider == null)
				return proposal.getLabel() == null ? proposal.getContent()
						: proposal.getLabel();
			return labelProvider.getText(proposal);
		}

		/*
		 * Get the image for the specified proposal. If there is no image
		 * available, return null.
		 */
		private Image getImage(IContentProposal proposal) {
			if (proposal == null || labelProvider == null)
				return null;
			return labelProvider.getImage(proposal);
		}

		/*
		 * Return an empty array. Used so that something always shows in the
		 * proposal popup, even if no proposal provider was specified.
		 */
		private IContentProposal[] getEmptyProposalArray() {
			return new IContentProposal[0];
		}

		/*
		 * Add a listener to the target control. We monitor key and traverse
		 * events in the target control while we are open, in order to handle
		 * traversal and navigation requests.
		 */
		private void addControlListener() {
			targetControlListener = new TargetControlListener();
			control.addListener(SWT.KeyDown, targetControlListener);
			control.addListener(SWT.Traverse, targetControlListener);
			control.addListener(SWT.Move, targetControlListener);
			control.addListener(SWT.Resize, targetControlListener);
		}

		/*
		 * Remove any listeners installed on the control.
		 */
		private void removeControlListener() {
			control.removeListener(SWT.KeyDown, targetControlListener);
			control.removeListener(SWT.Traverse, targetControlListener);
			control.removeListener(SWT.Move, targetControlListener);
			control.removeListener(SWT.Resize, targetControlListener);
		}

		/*
		 * Answer true if the popup is valid, which means the table has been
		 * created and not disposed.
		 */
		private boolean isValid() {
			return proposalTable != null && !proposalTable.isDisposed();
		}

		/*
		 * Return the current selected proposal.
		 */
		private IContentProposal getSelectedProposal() {
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
			Assert
					.isTrue(index >= 0,
							"Proposal index should never be negative"); //$NON-NLS-1$
			if (!isValid() || proposals == null || index >= proposals.length)
				return;
			proposalTable.setSelection(index);
			proposalTable.showSelection();

			showProposalDescription(proposals[index].getDescription());
		}

		/**
		 * Opens this ContentProposalPopup. This method is extended in order to
		 * add the control listener when the popup is opened and to invoke the
		 * secondary popup if applicable.
		 * 
		 * @return the return code
		 * 
		 * @see org.eclipse.jface.window.Window#open()
		 */
		public int open() {
			addControlListener();
			int value = super.open();
			showProposalDescription(getSelectedProposal().getDescription());
			return value;
		}

		/**
		 * Closes this popup. This method is extended to remove the control
		 * listener.
		 * 
		 * @return <code>true</code> if the window is (or was already) closed,
		 *         and <code>false</code> if it is still open
		 */
		public boolean close() {
			removeControlListener();
			return super.close();
		}

		/*
		 * Get the proposals from the proposal provider. The provider may or may
		 * not filter the proposals based on the specified filter text.
		 */
		private IContentProposal[] getProposals(String filterString) {
			if (proposalProvider == null)
				return null;
			int position = getControlContentAdapter().getCursorPosition(
					getControl());
			String contents = getControlContentAdapter().getControlContents(
					getControl());
			IContentProposal[] proposals = proposalProvider.getProposals(
					contents, position);
			if (filter) {
				return filterProposals(proposals, filterString);
			}
			return proposals;
		}

		/*
		 * Show the proposal description in a secondary popup.
		 */
		private void showProposalDescription(String description) {
			// If we have not created an info popup yet, do so now.
			if (infoPopup == null && description != null) {
				// Create a thread that will sleep for the specified delay
				// before creating the popup. We do not use Jobs since this
				// code must be able to run independently of the Eclipse
				// runtime.
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
								IContentProposal p = getSelectedProposal();
								if (p != null) {
									if (infoPopup == null) {
										infoPopup = new InfoPopupDialog(
												getShell());
										infoPopup.open();
									}
									infoPopup.setContents(p.getDescription());
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
			proposalAccepted(getSelectedProposal());
			close();
		}

		/*
		 * Request the proposals from the proposal provider, and recompute any
		 * caches. Repopulate the popup if it is open.
		 */
		private void recomputeProposals(String filterText) {
			setProposals(getProposals(filterText));
		}

		/*
		 * Filter the provided list of content proposals according to the filter
		 * text.
		 */
		private IContentProposal[] filterProposals(
				IContentProposal[] proposals, String filterString) {
			if (filterString.length() == 0)
				return proposals;

			// Check each string for a match. Use the string displayed to the
			// user,
			// not the proposal content.
			ArrayList list = new ArrayList();
			for (int i = 0; i < proposals.length; i++) {
				String string = getString(proposals[i]);
				if (string.length() >= filterString.length()
						&& string.substring(0, filterString.length())
								.equalsIgnoreCase(filterString))
					list.add(proposals[i]);

			}
			return (IContentProposal[]) list.toArray(new IContentProposal[list
					.size()]);
		}
	}

	/**
	 * Flag that controls the printing of debug info.
	 */
	public static final boolean DEBUG = false;

	/**
	 * Indicates that a chosen proposal should be inserted into the field.
	 */
	public static final int PROPOSAL_INSERT = 1;

	/**
	 * Indicates that a chosen proposal should replace the entire contents of
	 * the field.
	 */
	public static final int PROPOSAL_REPLACE = 2;

	/**
	 * Indicates that the contents of the control should not be modified when a
	 * proposal is chosen. This is typically used when a client needs more
	 * specialized behavior when a proposal is chosen. In this case, clients
	 * typically register an IContentProposalListener so that they are notified
	 * when a proposal is chosen.
	 */
	public static final int PROPOSAL_IGNORE = 3;

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
	 * The character height hint for the popup.
	 */
	private static final int POPUP_CHARHEIGHT = 10;

	/*
	 * Empty string.
	 */
	private static final String EMPTY = ""; //$NON-NLS-1$

	/*
	 * The object that provides content proposals.
	 */
	private IContentProposalProvider proposalProvider;

	/*
	 * A label provider used to display proposals in the popup, and to extract
	 * Strings from non-String proposals.
	 */
	private ILabelProvider labelProvider;

	/*
	 * The control for which content proposals are provided.
	 */
	private Control control;

	/*
	 * The adapter used to extract the String contents from an arbitrary
	 * control.
	 */
	private IControlContentAdapter controlContentAdapter;

	/*
	 * The popup used to show proposals.
	 */
	private ContentProposalPopup popup;

	/*
	 * The keystroke that signifies content proposals should be shown.
	 */
	private KeyStroke triggerKeyStroke;

	/*
	 * The String containing characters that auto-activate the popup.
	 */
	private String autoActivateString;

	/*
	 * A flag indicating how an accepted proposal should affect the control. One
	 * of PROPOSAL_IGNORE, PROPOSAL_INSERT, or PROPOSAL_REPLACE.
	 */
	private int acceptance;

	/*
	 * A boolean that indicates whether keys events received while the proposal
	 * popup is open should also be propagated to the control.
	 */
	private boolean propagateKeys;

	/*
	 * A boolean that indicates whether proposals should be filtered as keys are
	 * typed when the popup is open.
	 */
	private boolean filterProposals;

	/*
	 * The listener we install on the control.
	 */
	private Listener controlListener;

	/*
	 * The list of listeners who wish to be notified when something significant
	 * happens with the proposals.
	 */
	private ListenerList proposalListeners = new ListenerList();

	/*
	 * Flag that indicates whether the adapter is enabled. In some cases,
	 * adapters may be installed but depend upon outside state.
	 */
	private boolean isEnabled = true;

	/*
	 * The delay in milliseconds used when autoactivating the popup.
	 */
	private int autoActivationDelay = 0;

	/**
	 * Construct a content proposal adapter that can assist the user with
	 * choosing content for the field.
	 * 
	 * @param control
	 *            the control for which the adapter is providing content assist.
	 *            May not be <code>null</code>.
	 * @param controlContentAdapter
	 *            the <code>IControlContentAdapter</code> used to obtain and
	 *            update the control's contents as proposals are accepted. May
	 *            not be <code>null</code>.
	 * @param proposalProvider
	 *            the <code>IContentProposalProvider</code> used to obtain
	 *            content proposals for this control, or <code>null</code> if
	 *            no content proposal is available.
	 * @param labelProvider
	 *            the label provider which provides text and image information
	 *            for content proposals. A <code>null</code> value indicates
	 *            that no label provider is needed, and all values will be
	 *            obtained from the proposals themselves. The lifecycle of the
	 *            specified label provider is not managed by this adapter.
	 *            Clients must dispose the label provider when it is no longer
	 *            needed.
	 * @param keyStroke
	 *            the keystroke that will invoke the content proposal popup. If
	 *            this value is <code>null</code>, then proposals will be
	 *            activated automatically when any of the auto activation
	 *            characters are typed.
	 * @param autoActivationCharacters
	 *            An array of characters that trigger auto-activation of content
	 *            proposal. If specified, these characters will trigger
	 *            auto-activation of the proposal popup, regardless of whether
	 *            an explicit invocation keyStroke was specified. If this
	 *            parameter is <code>null</code>, then only a specified
	 *            keyStroke will invoke content proposal. If this parameter is
	 *            <code>null</code> and the keyStroke parameter is
	 *            <code>null</code>, then all alphanumeric characters will
	 *            auto-activate content proposal.
	 * @param propagateKeys
	 *            a boolean that indicates whether key events (including
	 *            auto-activation characters) should be propagated to the
	 *            adapted control when the proposal popup is open.
	 * @param filterProposals
	 *            a boolean that indicates whether the proposal popup should
	 *            filter its contents based on keys typed when it is open
	 * @param acceptance
	 *            a constant indicating how an accepted proposal should affect
	 *            the control's content. Should be one of
	 *            <code>PROPOSAL_INSERT</code>, <code>PROPOSAL_REPLACE</code>,
	 *            or <code>PROPOSAL_IGNORE</code>
	 * 
	 */
	public ContentProposalAdapter(Control control,
			IControlContentAdapter controlContentAdapter,
			IContentProposalProvider proposalProvider,
			ILabelProvider labelProvider, KeyStroke keyStroke,
			char[] autoActivationCharacters, boolean propagateKeys,
			boolean filterProposals, int acceptance) {
		super();
		// We always assume the control and content adapter are valid.
		Assert.isNotNull(control);
		Assert.isNotNull(controlContentAdapter);
		this.control = control;
		this.controlContentAdapter = controlContentAdapter;

		// The rest of these may be null
		this.proposalProvider = proposalProvider;
		this.labelProvider = labelProvider;
		this.triggerKeyStroke = keyStroke;
		if (autoActivationCharacters != null)
			this.autoActivateString = new String(autoActivationCharacters);
		this.propagateKeys = propagateKeys;
		this.filterProposals = filterProposals;
		this.acceptance = acceptance;
		addControlListener(control);
	}

	/**
	 * Get the control on which the content proposal adapter is installed.
	 * 
	 * @return the control on which the proposal adapter is installed.
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * Get the label provider that is used to show proposals.
	 * 
	 * @return the {@link ILabelProvider} used to show proposals, or
	 *         <code>null</code> if one has not been installed.
	 */
	public ILabelProvider getLabelProvider() {
		return labelProvider;
	}

	/**
	 * Return a boolean indicating whether the receiver is enabled.
	 * 
	 * @return <code>true</code> if the adapter is enabled, and
	 *         <code>false</code> if it is not.
	 */
	public boolean isEnabled() {
		return isEnabled;
	}

	/**
	 * Set the label provider that is used to show proposals. The lifecycle of
	 * the specified label provider is not managed by this adapter. Clients must
	 * dispose the label provider when it is no longer needed.
	 * 
	 * @param labelProvider
	 *            the (@link ILabelProvider} used to show proposals.
	 */
	public void setLabelProvider(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	/**
	 * Return the proposal provider that provides content proposals given the
	 * current content of the field. A value of <code>null</code> indicates
	 * that there are no content proposals available for the field.
	 * 
	 * @return the {@link IContentProposalProvider} used to show proposals. May
	 *         be <code>null</code>.
	 */
	public IContentProposalProvider getContentProposalProvider() {
		return proposalProvider;
	}

	/**
	 * Set the content proposal provider that is used to show proposals.
	 * 
	 * @param proposalProvider
	 *            the {@link IContentProposalProvider} used to show proposals
	 */
	public void setContentProposalProvider(
			IContentProposalProvider proposalProvider) {
		this.proposalProvider = proposalProvider;
	}

	/**
	 * Return the array of characters on which the popup is autoactivated.
	 * 
	 * @return An array of characters that trigger auto-activation of content
	 *         proposal. If specified, these characters will trigger
	 *         auto-activation of the proposal popup, regardless of whether an
	 *         explicit invocation keyStroke was specified. If this parameter is
	 *         <code>null</code>, then only a specified keyStroke will invoke
	 *         content proposal. If this value is <code>null</code> and the
	 *         keyStroke value is <code>null</code>, then all alphanumeric
	 *         characters will auto-activate content proposal.
	 */
	public char[] getAutoActivationCharacters() {
		if (autoActivateString == null)
			return null;
		return autoActivateString.toCharArray();
	}

	/**
	 * Set the array of characters that will trigger autoactivation of the
	 * popup.
	 * 
	 * @param autoActivationCharacters
	 *            An array of characters that trigger auto-activation of content
	 *            proposal. If specified, these characters will trigger
	 *            auto-activation of the proposal popup, regardless of whether
	 *            an explicit invocation keyStroke was specified. If this
	 *            parameter is <code>null</code>, then only a specified
	 *            keyStroke will invoke content proposal. If this parameter is
	 *            <code>null</code> and the keyStroke value is
	 *            <code>null</code>, then all alphanumeric characters will
	 *            auto-activate content proposal.
	 * 
	 */
	public void setAutoActivationCharacters(char[] autoActivationCharacters) {
		this.autoActivateString = new String(autoActivationCharacters);
	}

	/**
	 * Set the delay, in milliseconds, used before any autoactivation is
	 * triggered.
	 * 
	 * @return the time in milliseconds that will pass before a popup is
	 *         automatically opened
	 */
	public int getAutoActivationDelay() {
		return autoActivationDelay;

	}

	/**
	 * Set the delay, in milliseconds, used before autoactivation is triggered.
	 * 
	 * @param delay
	 *            the time in milliseconds that will pass before a popup is
	 *            automatically opened
	 */
	public void setAutoActivationDelay(int delay) {
		autoActivationDelay = delay;

	}

	/**
	 * Return the content adapter that can get or retrieve the text contents
	 * from the adapter's control. This method is used when a client, such as a
	 * content proposal listener, needs to update the control's contents
	 * manually.
	 * 
	 * @return the {@link IControlContentAdapter} which can update the control
	 *         text.
	 */
	public IControlContentAdapter getControlContentAdapter() {
		return controlContentAdapter;
	}

	/**
	 * Set the boolean flag that determines whether the adapter is enabled.
	 * 
	 * @param enabled
	 *            <code>true</code> if the adapter is enabled and responding
	 *            to user input, <code>false</code> if it is ignoring user
	 *            input.
	 * 
	 */
	public void setEnabled(boolean enabled) {
		// If we are disabling it while it's proposing content, close the
		// content proposal popup.
		if (isEnabled && !enabled) {
			if (popup != null)
				popup.close();
		}
		isEnabled = enabled;
	}

	/**
	 * Add the specified listener to the list of content proposal listeners that
	 * are notified when content proposals are chosen.
	 * </p>
	 * 
	 * @param listener
	 *            the IContentProposalListener to be added as a listener. Must
	 *            not be <code>null</code>. If an attempt is made to register
	 *            an instance which is already registered with this instance,
	 *            this method has no effect.
	 * 
	 * @see org.eclipse.jface.fieldassist.IContentProposalListener
	 */
	public void addContentProposalListener(IContentProposalListener listener) {
		proposalListeners.add(listener);
	}

	/*
	 * Add our listener to the control. Debug information to be left in until
	 * this support is stable on all platforms.
	 */
	private void addControlListener(Control control) {
		if (DEBUG)
			System.out
					.println("ContentProposalListener#installControlListener()"); //$NON-NLS-1$

		if (controlListener != null)
			return;
		controlListener = new Listener() {
			public void handleEvent(Event e) {
				if (!isEnabled)
					return;

				switch (e.type) {
				case SWT.KeyDown:
					if (DEBUG)
						dump("keyDown OK", e); //$NON-NLS-1$
					if (triggerKeyStroke != null) {
						// Either there are no modifiers for the trigger and we
						// check the character field...
						if ((triggerKeyStroke.getModifierKeys() == KeyStroke.NO_KEY && triggerKeyStroke
								.getNaturalKey() == e.character)
								||
								// ...or there are modifiers, in which case the
								// keycode and state must match
								(triggerKeyStroke.getNaturalKey() == e.keyCode && ((triggerKeyStroke
										.getModifierKeys() & e.stateMask) == triggerKeyStroke
										.getModifierKeys()))) {
							// We never propagate an explicit keystroke
							// invocation
							e.doit = false;
							/*
							 * Open the popup in an async so that this keystroke
							 * finishes processing before the popup registers
							 * its own listeners.
							 */
							if (popup == null) {
								getControl().getDisplay().asyncExec(
										new Runnable() {
											public void run() {
												openProposalPopup();
											}
										});
							}
							return;
						}
					}
					/*
					 * The triggering keystroke was not invoked. Check for
					 * autoactivation characters.
					 */
					if (e.character != 0) {
						boolean autoActivated = false;
						// Auto-activation characters were specified. Check
						// them.
						if (autoActivateString != null) {
							if (autoActivateString.indexOf(e.character) >= 0) {
								autoActivated = true;
							}
							// Auto-activation characters were not specified. If
							// there was no key stroke specified, assume
							// activation for alphanumeric characters.
						} else if (triggerKeyStroke == null
								&& Character.isLetterOrDigit(e.character)) {
							autoActivated = true;
						}
						/*
						 * When autoactivating, we check the autoactivation
						 * delay.
						 */
						if (autoActivated) {
							e.doit = propagateKeys;
							if (popup == null) {
								if (autoActivationDelay > 0) {
									Runnable runnable = new Runnable() {
										public void run() {
											try {
												Thread
														.sleep(autoActivationDelay);
											} catch (InterruptedException e) {
											}
											if (!isValid())
												return;
											getControl().getDisplay().syncExec(
													new Runnable() {
														public void run() {
															openProposalPopup();
														}
													});
										}
									};
									Thread t = new Thread(runnable);
									t.start();
								} else {
									openProposalPopup();
								}
							}
						}
					}

					// If the popup is not open, we always propagate keys.
					// If the popup is open, consult the flag.
					e.doit = popup == null || propagateKeys;
					break;

				default:
					break;
				}
			}

			/**
			 * Dump the given events to "standard" output.
			 * 
			 * @param who
			 *            who is dumping the event
			 * @param e
			 *            the event
			 */
			private void dump(String who, Event e) {
				StringBuffer sb = new StringBuffer(
						"--- [ContentProposalAdapter]\n"); //$NON-NLS-1$
				sb.append(who);
				sb.append(" - e: keyCode=" + e.keyCode + hex(e.keyCode)); //$NON-NLS-1$
				sb.append("; character=" + e.character + hex(e.character)); //$NON-NLS-1$
				sb.append("; stateMask=" + e.stateMask + hex(e.stateMask)); //$NON-NLS-1$
				sb.append("; doit=" + e.doit); //$NON-NLS-1$
				sb.append("; detail=" + e.detail + hex(e.detail)); //$NON-NLS-1$
				sb.append("; widget=" + e.widget); //$NON-NLS-1$
				System.out.println(sb);
			}

			private String hex(int i) {
				return "[0x" + Integer.toHexString(i) + ']'; //$NON-NLS-1$
			}
		};
		control.addListener(SWT.KeyDown, controlListener);

		if (DEBUG)
			System.out
					.println("ContentProposalAdapter#installControlListener() - installed"); //$NON-NLS-1$
	}

	/**
	 * Open the proposal popup and display the proposals provided by the
	 * proposal provider. This method returns immediately. That is, it does not
	 * wait for a proposal to be selected.
	 */
	protected void openProposalPopup() {
		if (popup == null) {
			popup = new ContentProposalPopup(null, filterProposals);
			popup.open();
			popup.getShell().addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					popup = null;
				}
			});
		}
	}

	/*
	 * A content proposal has been accepted. Update the control contents
	 * accordingly and notify any listeners.
	 * 
	 * @param proposal the accepted proposal
	 */
	private void proposalAccepted(IContentProposal proposal) {
		switch (acceptance) {
		case (PROPOSAL_REPLACE):
			setControlContent(proposal.getContent(), proposal
					.getCursorPosition());
			break;
		case (PROPOSAL_INSERT):
			insertControlContent(proposal.getContent(), proposal
					.getCursorPosition());
			break;
		default:
			// do nothing. Typically a listener is installed to handle this in
			// a custom way.
			break;
		}

		// In all cases, notify listeners of an accepted proposal.
		final Object[] listenerArray = proposalListeners.getListeners();
		for (int i = 0; i < listenerArray.length; i++)
			((IContentProposalListener) listenerArray[i])
					.proposalAccepted(proposal);
	}

	/*
	 * Set the text content of the control to the specified text, setting the
	 * cursorPosition at the desired location within the new contents.
	 */
	private void setControlContent(String text, int cursorPosition) {
		if (isValid())
			controlContentAdapter.setControlContents(control, text,
					cursorPosition);
	}

	/*
	 * Insert the specified text into the control content, setting the
	 * cursorPosition at hte desired location within the new contents.
	 */
	private void insertControlContent(String text, int cursorPosition) {
		if (isValid())
			controlContentAdapter.insertControlContents(control, text,
					cursorPosition);
	}

	/*
	 * Check that the control and content adapter are valid.
	 */
	private boolean isValid() {
		return control != null && !control.isDisposed()
				&& controlContentAdapter != null;
	}
}
