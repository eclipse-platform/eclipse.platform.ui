/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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

package org.eclipse.ui.internal.ide.dialogs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEInternalWorkbenchImages;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.part.EditorPart;
import org.xml.sax.SAXException;

/**
 * A "fake" editor to show a welcome page
 * The contents of this page are supplied in the product configuration
 *
 * PRIVATE
 *		This class is internal to the workbench and must not be called outside the workbench
 */
public class WelcomeEditor extends EditorPart {

	private static final int HORZ_SCROLL_INCREMENT = 20;

	private static final int VERT_SCROLL_INCREMENT = 20;

	// width at which wrapping will stop and a horizontal scroll bar will be
	// introduced
	private static final int WRAP_MIN_WIDTH = 150;

	private Composite editorComposite;

	private WelcomeParser parser;

	private ArrayList<StyleRange> hyperlinkRanges = new ArrayList<>();

	private ArrayList<StyledText> texts = new ArrayList<>();

	private ScrolledComposite scrolledComposite;

	private IPropertyChangeListener colorListener;

	private boolean mouseDown = false;

	private boolean dragEvent = false;

	private StyledText firstText, lastText;

	private StyledText lastNavigatedText, currentText;

	private boolean nextTabAbortTraversal, previousTabAbortTraversal = false;

	private WelcomeEditorCopyAction copyAction;

	/**
	 * Create a new instance of the welcome editor
	 */
	public WelcomeEditor() {
		super();
		setPartName(IDEWorkbenchMessages.WelcomeEditor_title);
		copyAction = new WelcomeEditorCopyAction(this);
		copyAction.setEnabled(false);
	}

	/**
	 * Update the welcome page to start at the
	 * beginning of the text.
	 */
	private void focusOn(StyledText newText, int caretOffset) {
		if (newText == null) {
			return;
		}
		newText.setFocus();
		newText.setCaretOffset(caretOffset);
		scrolledComposite.setOrigin(0, newText.getLocation().y);
	}

	/**
	 * Finds the next text
	 */
	private StyledText nextText(StyledText text) {
		if (text == null) {
			return texts.get(0);
		}
		int index = texts.indexOf(text);

		//If we are not at the end....
		if (index < texts.size() - 1) {
			return texts.get(index + 1);
		}
		return texts.get(0);
	}

	/**
	 * Finds the previous text
	 */
	private StyledText previousText(StyledText text) {
		if (text == null) {
			return texts.get(0);
		}
		int index = texts.indexOf(text);

		//If we are at the beginning....
		if (index == 0) {
			return texts.get(texts.size() - 1);
		}
		return texts.get(index - 1);
	}

	/**
	 * Returns the current text.
	 */
	protected StyledText getCurrentText() {
		return currentText;
	}

	/**
	 * Returns the copy action.
	 */
	protected WelcomeEditorCopyAction getCopyAction() {
		return copyAction;
	}

	/**
	 * Finds the next link after the current selection.
	 */
	private StyleRange findNextLink(StyledText text) {
		if (text == null) {
			return null;
		}

		WelcomeItem item = (WelcomeItem) text.getData();
		int currentSelectionEnd = text.getSelection().y;

		for (StyleRange range : text.getStyleRanges()) {
			if (range.start >= currentSelectionEnd) {
				if (item.isLinkAt(range.start)) {
					return range;
				}
			}
		}
		return null;
	}

	/**
	 * Finds the previous link before the current selection.
	 */
	private StyleRange findPreviousLink(StyledText text) {
		if (text == null) {
			return null;
		}

		WelcomeItem item = (WelcomeItem) text.getData();
		StyleRange[] ranges = text.getStyleRanges();
		int currentSelectionStart = text.getSelection().x;

		for (int i = ranges.length - 1; i > -1; i--) {
			if ((ranges[i].start + ranges[i].length) < currentSelectionStart) {
				if (item.isLinkAt(ranges[i].start + ranges[i].length - 1)) {
					return ranges[i];
				}
			}
		}
		return null;
	}

	/**
	 * Finds the current link of the current selection.
	 */
	protected StyleRange getCurrentLink(StyledText text) {
		int currentSelectionEnd = text.getSelection().y;
		int currentSelectionStart = text.getSelection().x;

		for (StyleRange range : text.getStyleRanges()) {
			if ((currentSelectionStart >= range.start)
					&& (currentSelectionEnd <= (range.start + range.length))) {
				return range;
			}
		}
		return null;
	}

	/**
	 * Adds listeners to the given styled text
	 */
	private void addListeners(StyledText styledText) {
		Cursor handCursor = styledText.getDisplay().getSystemCursor(SWT.CURSOR_HAND);
		Cursor busyCursor = styledText.getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
		styledText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				if (e.button != 1) {
					return;
				}
				mouseDown = true;
			}

			@Override
			public void mouseUp(MouseEvent e) {
				mouseDown = false;
				StyledText text = (StyledText) e.widget;
				WelcomeItem item = (WelcomeItem) e.widget.getData();
				int offset = text.getCaretOffset();
				if (dragEvent) {
					dragEvent = false;
					if (item.isLinkAt(offset)) {
						text.setCursor(handCursor);
					}
				} else if (item.isLinkAt(offset)) {
					text.setCursor(busyCursor);
					if (e.button == 1) {
						item.triggerLinkAt(offset);
						StyleRange selectionRange = getCurrentLink(text);
						text.setSelectionRange(selectionRange.start,
								selectionRange.length);
						text.setCursor(null);
					}
				}
			}
		});

		styledText.addMouseMoveListener(e -> {
			// Do not change cursor on drag events
			if (mouseDown) {
				if (!dragEvent) {
					StyledText text1 = (StyledText) e.widget;
					text1.setCursor(null);
				}
				dragEvent = true;
				return;
			}
			StyledText text2 = (StyledText) e.widget;
			WelcomeItem item = (WelcomeItem) e.widget.getData();
			int offset = text2.getOffsetAtPoint(new Point(e.x, e.y));
			if (offset == -1) {
				text2.setCursor(null);
			} else if (item.isLinkAt(offset)) {
				text2.setCursor(handCursor);
			} else {
				text2.setCursor(null);
			}
		});

		styledText.addTraverseListener(e -> {
			StyledText text = (StyledText) e.widget;

			switch (e.detail) {
			case SWT.TRAVERSE_ESCAPE:
				e.doit = true;
				break;
			case SWT.TRAVERSE_TAB_NEXT:
				// Handle Ctrl-Tab
				if ((e.stateMask & SWT.CTRL) != 0) {
					if (e.widget == lastText) {
						return;
					}
					e.doit = false;
					nextTabAbortTraversal = true;
					lastText.traverse(SWT.TRAVERSE_TAB_NEXT);
					return;
				}
				if (nextTabAbortTraversal) {
					nextTabAbortTraversal = false;
					return;
				}
				// Find the next link in current widget, if applicable
				// Stop at top of widget
				StyleRange nextLink = findNextLink(text);
				if (nextLink == null) {
					// go to the next widget, focus at beginning
					StyledText nextText = nextText(text);
					nextText.setSelection(0);
					focusOn(nextText, 0);
				} else {
					// focusOn: allow none tab traversals to align
					focusOn(text, text.getSelection().x);
					text.setSelectionRange(nextLink.start, nextLink.length);
				}
				e.detail = SWT.TRAVERSE_NONE;
				e.doit = true;
				break;
			case SWT.TRAVERSE_TAB_PREVIOUS:
				// Handle Ctrl-Shift-Tab
				if ((e.stateMask & SWT.CTRL) != 0) {
					if (e.widget == firstText) {
						return;
					}
					e.doit = false;
					previousTabAbortTraversal = true;
					firstText.traverse(SWT.TRAVERSE_TAB_PREVIOUS);
					return;
				}
				if (previousTabAbortTraversal) {
					previousTabAbortTraversal = false;
					return;
				}
				// Find the previous link in current widget, if applicable
				// Stop at top of widget also
				StyleRange previousLink = findPreviousLink(text);
				if (previousLink == null) {
					if (text.getSelection().x == 0) {
						// go to the previous widget, focus at end
						StyledText previousText = previousText(text);
						previousText.setSelection(previousText
								.getCharCount());
						previousLink = findPreviousLink(previousText);
						if (previousLink == null) {
							focusOn(previousText, 0);
						} else {
							focusOn(previousText, previousText
									.getSelection().x);
							previousText
									.setSelectionRange(previousLink.start,
											previousLink.length);
						}
					} else {
						// stay at top of this widget
						focusOn(text, 0);
					}
				} else {
					// focusOn: allow none tab traversals to align
					focusOn(text, text.getSelection().x);
					text.setSelectionRange(previousLink.start,
							previousLink.length);
				}
				e.detail = SWT.TRAVERSE_NONE;
				e.doit = true;
				break;
			default:
				break;
			}
		});

		styledText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				//Ignore a key release
			}

			@Override
			public void keyPressed(KeyEvent event) {
				StyledText text = (StyledText) event.widget;
				if (event.character == ' ' || event.character == SWT.CR) {
					if (text != null) {
						WelcomeItem item = (WelcomeItem) text.getData();

						//Be sure we are in the selection
						int offset = text.getSelection().x + 1;

						if (item.isLinkAt(offset)) {
							text.setCursor(busyCursor);
							item.triggerLinkAt(offset);
							StyleRange selectionRange = getCurrentLink(text);
							text.setSelectionRange(selectionRange.start,
									selectionRange.length);
							text.setCursor(null);
						}
					}
					return;
				}

				// When page down is pressed, move the cursor to the next item in the
				// welcome page.   Note that this operation wraps (pages to the top item
				// when the last item is reached).
				if (event.keyCode == SWT.PAGE_DOWN) {
					focusOn(nextText(text), 0);
					return;
				}

				// When page up is pressed, move the cursor to the previous item in the
				// welcome page.  Note that this operation wraps (pages to the bottom item
				// when the first item is reached).
				if (event.keyCode == SWT.PAGE_UP) {
					focusOn(previousText(text), 0);
					return;
				}
			}
		});

		styledText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				// Remember current text widget
				lastNavigatedText = (StyledText) e.widget;
			}

			@Override
			public void focusGained(FocusEvent e) {
				currentText = (StyledText) e.widget;

				// Remove highlighted selection if text widget has changed
				if ((currentText != lastNavigatedText)
						&& (lastNavigatedText != null)) {
					lastNavigatedText.setSelection(lastNavigatedText
							.getSelection().x);
				}

				// enable/disable copy action
				copyAction.setEnabled(currentText.isTextSelected());
			}
		});

		styledText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// enable/disable copy action
				StyledText text = (StyledText) e.widget;
				copyAction.setEnabled(text.isTextSelected());
			}
		});
	}

	/**
	 * Creates the wizard's title area.
	 *
	 * @param parent the SWT parent for the title area composite
	 * @return the created info area composite
	 */
	private Composite createInfoArea(Composite parent) {
		// Create the title area which will contain
		// a title, message, and image.
		this.scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL
				| SWT.H_SCROLL);
		this.scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		final Composite infoArea = new Composite(this.scrolledComposite,
				SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 10;
		layout.verticalSpacing = 5;
		layout.numColumns = 2;
		infoArea.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		infoArea.setLayoutData(data);
		boolean wrapped = parser.isFormatWrapped();
		int HINDENT = 20;

		// Get the background color for the title area
		Display display = parent.getDisplay();
		Color background = JFaceColors.getBannerBackground(display);
		Color foreground = JFaceColors.getBannerForeground(display);
		infoArea.setBackground(background);

		int textStyle = SWT.MULTI | SWT.READ_ONLY;
		if (wrapped) {
			textStyle = textStyle | SWT.WRAP;
		}
		StyledText sampleStyledText = null;
		// Create the intro item
		WelcomeItem item = getIntroItem();
		if (item != null) {
			StyledText styledText = new StyledText(infoArea, textStyle);
			this.texts.add(styledText);
			sampleStyledText = styledText;
			styledText.setCursor(null);
			JFaceColors.setColors(styledText, foreground, background);
			styledText.setText(getIntroItem().getText());
			setBoldRanges(styledText, item.getBoldRanges());
			setLinkRanges(styledText, item.getActionRanges());
			setLinkRanges(styledText, item.getHelpRanges());
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			gd.horizontalIndent = HINDENT;
			gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
			styledText.setLayoutData(gd);
			styledText.setData(item);
			addListeners(styledText);

			Label spacer = new Label(infoArea, SWT.NONE);
			spacer.setBackground(background);
			gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
			gd.horizontalSpan = 2;
			spacer.setLayoutData(gd);
		}
		firstText = sampleStyledText;

		// Create the welcome items
		Label imageLabel = null;
		for (WelcomeItem welcomeItem : getItems()) {
			Label label = new Label(infoArea, SWT.NONE);
			label.setBackground(background);
			label.setImage(PlatformUI.getWorkbench().getSharedImages()
					.getImage(IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_ITEM));
			GridData gd = new GridData();
			gd.horizontalIndent = HINDENT;
			gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
			label.setLayoutData(gd);
			if (imageLabel == null) {
				imageLabel = label;
			}

			StyledText styledText = new StyledText(infoArea, textStyle);
			this.texts.add(styledText);
			sampleStyledText = styledText;
			styledText.setCursor(null);
			JFaceColors.setColors(styledText, foreground, background);
			styledText.setText(welcomeItem.getText());
			setBoldRanges(styledText, welcomeItem.getBoldRanges());
			setLinkRanges(styledText, welcomeItem.getActionRanges());
			setLinkRanges(styledText, welcomeItem.getHelpRanges());
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
			gd.verticalSpan = 2;
			styledText.setLayoutData(gd);
			styledText.setData(welcomeItem);
			addListeners(styledText);

			Label spacer = new Label(infoArea, SWT.NONE);
			spacer.setBackground(background);
			gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
			gd.horizontalSpan = 2;
			spacer.setLayoutData(gd);

			// create context menu
			MenuManager menuMgr = new MenuManager("#PopUp"); //$NON-NLS-1$
			menuMgr.add(copyAction);
			styledText.setMenu(menuMgr.createContextMenu(styledText));
		}

		lastText = sampleStyledText;
		this.scrolledComposite.setContent(infoArea);
		Point p = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		this.scrolledComposite.setMinHeight(p.y);
		if (wrapped) {
			// introduce a horizontal scroll bar after a minimum width is reached
			this.scrolledComposite.setMinWidth(WRAP_MIN_WIDTH);
		} else {
			this.scrolledComposite.setMinWidth(p.x);
		}
		this.scrolledComposite.setExpandHorizontal(true);
		this.scrolledComposite.setExpandVertical(true);

		// When the welcome editor is resized, we need to set the width hint for
		// wrapped StyledText widgets so that the wrapped height will be recalculated.
		if (wrapped && (imageLabel != null)) {
			// figure out how wide the StyledText widgets should be, do this by first
			// calculating the width of the area not used by styled text widgets
			Rectangle bounds = imageLabel.getBounds();
			final int adjust = HINDENT + bounds.width + layout.verticalSpacing
					+ (layout.marginWidth * 2);
			final int adjustFirst = HINDENT + (layout.marginWidth * 2);
			infoArea.addListener(SWT.Resize, event -> {
				int w = scrolledComposite.getClientArea().width;
				// if the horizontal scroll bar exists, we want to wrap to the
				// minimum wrap width
				if (w < WRAP_MIN_WIDTH) {
					w = WRAP_MIN_WIDTH;
				}
				for (int i = 0; i < texts.size(); i++) {
					int extent;
					if (i == 0) {
						extent = w - adjustFirst;
					} else {
						extent = w - adjust;
					}
					StyledText text = texts.get(i);
					Point p1 = text.computeSize(extent, SWT.DEFAULT, false);
					((GridData) text.getLayoutData()).widthHint = p1.x;
				}
				// reset the scrolled composite height since the height of the
				// styled text widgets have changed
				Point p2 = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT,
						true);
				scrolledComposite.setMinHeight(p2.y);
			});
		}

		// Adjust the scrollbar increments
		if (sampleStyledText == null) {
			this.scrolledComposite.getHorizontalBar().setIncrement(
					HORZ_SCROLL_INCREMENT);
			this.scrolledComposite.getVerticalBar().setIncrement(
					VERT_SCROLL_INCREMENT);
		} else {
			GC gc = new GC(sampleStyledText);
			int width = (int) gc.getFontMetrics().getAverageCharacterWidth();
			gc.dispose();
			this.scrolledComposite.getHorizontalBar().setIncrement(width);
			this.scrolledComposite.getVerticalBar().setIncrement(
					sampleStyledText.getLineHeight());
		}
		return infoArea;
	}

	/**
	 * Creates the SWT controls for this workbench part.
	 * <p>
	 * Clients should not call this method (the workbench calls this method at
	 * appropriate times).
	 * </p>
	 * <p>
	 * For implementors this is a multi-step process:
	 * </p>
	 * <ol>
	 *   <li>Create one or more controls within the parent.</li>
	 *   <li>Set the parent layout as needed.</li>
	 *   <li>Register any global actions with the <code>IActionService</code>.</li>
	 *   <li>Register any popup menus with the <code>IActionService</code>.</li>
	 *   <li>Register a selection provider with the <code>ISelectionService</code>
	 *     (optional). </li>
	 * </ol>
	 *
	 * @param parent the parent control
	 */
	@Override
	public void createPartControl(Composite parent) {
		// read our contents
		readFile();
		if (parser == null) {
			return;
		}

		editorComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		editorComposite.setLayout(layout);

		createTitleArea(editorComposite);

		Label titleBarSeparator = new Label(editorComposite, SWT.HORIZONTAL
				| SWT.SEPARATOR);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		titleBarSeparator.setLayoutData(gd);

		createInfoArea(editorComposite);

		getSite().getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp(
				editorComposite, IIDEHelpContextIds.WELCOME_EDITOR);

		this.colorListener = event -> {
			if (event.getProperty()
					.equals(JFacePreferences.HYPERLINK_COLOR)) {
				Color fg = JFaceColors.getHyperlinkText(editorComposite
						.getDisplay());
				for (StyleRange range : hyperlinkRanges) {
					range.foreground = fg;
				}
			}
		};

		JFacePreferences.getPreferenceStore().addPropertyChangeListener(
				this.colorListener);

	}

	/**
	 * Creates the wizard's title area.
	 *
	 * @param parent the SWT parent for the title area composite
	 * @return the created title area composite
	 */
	private Composite createTitleArea(Composite parent) {
		// Get the background color for the title area
		Display display = parent.getDisplay();
		Color background = JFaceColors.getBannerBackground(display);
		Color foreground = JFaceColors.getBannerForeground(display);

		// Create the title area which will contain
		// a title, message, and image.
		Composite titleArea = new Composite(parent, SWT.NONE | SWT.NO_FOCUS);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.numColumns = 2;
		titleArea.setLayout(layout);
		titleArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		titleArea.setBackground(background);

		// Message label
		final CLabel messageLabel = new CLabel(titleArea, SWT.LEFT) {
			@Override
			protected String shortenText(GC gc, String text, int width) {
				if (gc.textExtent(text, SWT.DRAW_MNEMONIC).x <= width) {
					return text;
				}
				final String ellipsis = "..."; //$NON-NLS-1$
				int ellipseWidth = gc.textExtent(ellipsis, SWT.DRAW_MNEMONIC).x;
				int length = text.length();
				int end = length - 1;
				while (end > 0) {
					text = text.substring(0, end);
					int l1 = gc.textExtent(text, SWT.DRAW_MNEMONIC).x;
					if (l1 + ellipseWidth <= width) {
						return text + ellipsis;
					}
					end--;
				}
				return text + ellipsis;
			}
		};
		JFaceColors.setColors(messageLabel, foreground, background);
		messageLabel.setText(getBannerTitle());
		messageLabel.setFont(JFaceResources.getHeaderFont());

		final IPropertyChangeListener fontListener = event -> {
			if (JFaceResources.HEADER_FONT.equals(event.getProperty())) {
				messageLabel.setFont(JFaceResources.getHeaderFont());
			}
		};

		messageLabel.addDisposeListener(event -> JFaceResources.getFontRegistry().removeListener(fontListener));

		JFaceResources.getFontRegistry().addListener(fontListener);

		GridData gd = new GridData(GridData.FILL_BOTH);
		messageLabel.setLayoutData(gd);

		// Title image
		Label titleImage = new Label(titleArea, SWT.LEFT);
		titleImage.setBackground(background);
		titleImage.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(IDEInternalWorkbenchImages.IMG_OBJS_WELCOME_BANNER));
		gd = new GridData();
		gd.horizontalAlignment = GridData.END;
		titleImage.setLayoutData(gd);

		return titleArea;
	}

	/**
	 * The <code>WorkbenchPart</code> implementation of this
	 * <code>IWorkbenchPart</code> method disposes the title image
	 * loaded by <code>setInitializationData</code>. Subclasses may extend.
	 */
	@Override
	public void dispose() {
		super.dispose();
		if (this.colorListener != null) {
			JFacePreferences.getPreferenceStore().removePropertyChangeListener(
					this.colorListener);
		}
	}

	/**
	 * Saves the contents of this editor.
	 * <p>
	 * Subclasses must override this method to implement the open-save-close lifecycle
	 * for an editor.  For greater details, see <code>IEditorPart</code>
	 * </p>
	 *
	 * @see IEditorPart
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		// do nothing
	}

	/**
	 * Saves the contents of this editor to another object.
	 * <p>
	 * Subclasses must override this method to implement the open-save-close lifecycle
	 * for an editor.  For greater details, see <code>IEditorPart</code>
	 * </p>
	 *
	 * @see IEditorPart
	 */
	@Override
	public void doSaveAs() {
		// do nothing
	}

	/**
	 * Returns the title obtained from the parser
	 */
	private String getBannerTitle() {
		if (parser.getTitle() == null) {
			return ""; //$NON-NLS-1$
		}
		return parser.getTitle();
	}

	/**
	 * Returns the intro item or <code>null</code>
	 */
	private WelcomeItem getIntroItem() {
		return parser.getIntroItem();
	}

	/**
	 * Returns the welcome items
	 */
	private WelcomeItem[] getItems() {
		return parser.getItems();
	}

	/**
	 * Sets the cursor and selection state for this editor to the passage defined
	 * by the given marker.
	 * <p>
	 * Subclasses may override.  For greater details, see <code>IEditorPart</code>
	 * </p>
	 *
	 * @see IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		// do nothing
	}

	/**
	 * Initializes the editor part with a site and input.
	 * <p>
	 * Subclasses of <code>EditorPart</code> must implement this method.  Within
	 * the implementation subclasses should verify that the input type is acceptable
	 * and then save the site and input.  Here is sample code:
	 * </p>
	 * <pre>
	 *		if (!(input instanceof IFileEditorInput))
	 *			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
	 *		setSite(site);
	 *		setInput(editorInput);
	 * </pre>
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (!(input instanceof WelcomeEditorInput)) {
			throw new PartInitException(
					"Invalid Input: Must be WelcomeEditorInput"); //$NON-NLS-1$
		}
		setSite(site);
		setInput(input);
	}

	/**
	 * Returns whether the contents of this editor have changed since the last save
	 * operation.
	 * <p>
	 * Subclasses must override this method to implement the open-save-close lifecycle
	 * for an editor.  For greater details, see <code>IEditorPart</code>
	 * </p>
	 *
	 * @see IEditorPart
	 */
	@Override
	public boolean isDirty() {
		return false;
	}

	/**
	 * Returns whether the "save as" operation is supported by this editor.
	 * <p>
	 * Subclasses must override this method to implement the open-save-close lifecycle
	 * for an editor.  For greater details, see <code>IEditorPart</code>
	 * </p>
	 *
	 * @see IEditorPart
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	/**
	 * Read the contents of the welcome page
	 *
	 * @param is the <code>InputStream</code> to parse
	 * @throws IOException if there is a problem parsing the stream.
	 */
	public void read(InputStream is) throws IOException {
		try {
			parser = new WelcomeParser();
		} catch (ParserConfigurationException | SAXException e) {
			throw (IOException) (new IOException().initCause(e));
		}
		parser.parse(is);
	}

	/**
	 * Reads the welcome file
	 */
	public void readFile() {
		URL url = ((WelcomeEditorInput) getEditorInput()).getAboutInfo()
				.getWelcomePageURL();

		if (url == null) {
			// should not happen
			return;
		}

		InputStream is = null;
		try {
			is = url.openStream();
			read(is);
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR,
					IDEWorkbenchPlugin.IDE_WORKBENCH, 1, IDEWorkbenchMessages.WelcomeEditor_accessException, e);
			IDEWorkbenchPlugin.log(IDEWorkbenchMessages.WelcomeEditor_readFileError, status);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Sets the styled text's bold ranges
	 */
	private void setBoldRanges(StyledText styledText, int[][] boldRanges) {
		for (int[] boldRange : boldRanges) {
			StyleRange r = new StyleRange(boldRange[0], boldRange[1], null, null, SWT.BOLD);
			styledText.setStyleRange(r);
		}
	}

	/**
	 * Asks this part to take focus within the workbench.
	 * <p>
	 * Clients should not call this method (the workbench calls this method at
	 * appropriate times).
	 * </p>
	 */
	@Override
	public void setFocus() {
		if ((editorComposite != null) && (lastNavigatedText == null)
				&& (currentText == null)) {
			editorComposite.setFocus();
		}
	}

	/**
	 * Sets the styled text's link (blue) ranges
	 */
	private void setLinkRanges(StyledText styledText, int[][] linkRanges) {
		//Color fg = styledText.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		Color fg = JFaceColors.getHyperlinkText(styledText.getShell()
				.getDisplay());
		for (int[] linkRange : linkRanges) {
			StyleRange r = new StyleRange(linkRange[0], linkRange[1],
					fg, null);
			styledText.setStyleRange(r);
			hyperlinkRanges.add(r);
		}
	}

}
