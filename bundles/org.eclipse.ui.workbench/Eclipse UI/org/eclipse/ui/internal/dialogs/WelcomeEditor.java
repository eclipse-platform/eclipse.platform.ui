package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.part.EditorPart;

/**
 * A "fake" editor to show a welcome page
 * The contents of this page are supplied in the product configuration
 *
 * @private
 *		This class is internal to the workbench and must not be called outside the workbench
 */
public class WelcomeEditor extends EditorPart {
	
	private final static int HORZ_SCROLL_INCREMENT = 20;
	private final static int VERT_SCROLL_INCREMENT = 20;
	// width at which wrapping will stop and a horizontal scroll bar will be 
	// introduced
	private final static int WRAP_MIN_WIDTH = 150;  
	
	private Composite editorComposite;

	private Cursor handCursor;
	private Cursor busyCursor;
	
	private IWorkbench workbench;
	private WelcomeParser parser;
	private Image image;
	
	private ArrayList hyperlinkRanges = new ArrayList();
	private ArrayList texts = new ArrayList();
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
	setTitle(WorkbenchMessages.getString("WelcomeEditor.title")); //$NON-NLS-1$
	copyAction = new WelcomeEditorCopyAction(this);
	copyAction.setEnabled(false);	
}

/**
 * Update the welcome page to start at the
 * beginning of the text.
 */
private void focusOn(StyledText newText, int caretOffset){
	if(newText == null)
		return;
	newText.setFocus();
	newText.setCaretOffset(caretOffset);
	scrolledComposite.setOrigin(0,newText.getLocation().y);
}

/**
 * Finds the next text 
 */		
private StyledText nextText(StyledText text){
	int index = 0;
	if(text == null)
		return (StyledText) texts.get(0);
	else
		index = texts.indexOf(text);
		
	//If we are not at the end....
	if(index < texts.size() - 1)
		return (StyledText) texts.get(index + 1);
	else
		return (StyledText)  texts.get(0);						
}

/**
 * Finds the previous text 
 */		
private StyledText previousText(StyledText text){
	int index = 0;
	if(text == null)
		return (StyledText) texts.get(0);
	else
		index = texts.indexOf(text);
		
	//If we are at the beginning....
	if(index == 0)
		return (StyledText) texts.get(texts.size() - 1);
	else
		return (StyledText)  texts.get(index - 1);						
}

/**
 * Returns the current text. */
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
	if(text == null)
		return null;
		
	WelcomeItem item = (WelcomeItem)text.getData();		
	StyleRange[] ranges = text.getStyleRanges();
	int currentSelectionEnd = text.getSelection().y;

	for (int i = 0; i < ranges.length; i++) {
		if(ranges[i].start >= currentSelectionEnd)
			if (item.isLinkAt(ranges[i].start))
				return ranges[i];
	}
	return null;
}

/**
 * Finds the previous link before the current selection.
 */
private StyleRange findPreviousLink(StyledText text) {
	if(text == null)
		return null;
		
	WelcomeItem item = (WelcomeItem)text.getData();
	StyleRange[] ranges = text.getStyleRanges();
	int currentSelectionStart = text.getSelection().x;

	for (int i = ranges.length - 1; i > -1; i--) {
		if((ranges[i].start + ranges[i].length) < currentSelectionStart)
			if (item.isLinkAt(ranges[i].start + ranges[i].length - 1))
				return ranges[i];
	}
	return null;
}

/**
 * Finds the current link of the current selection.
 */
protected StyleRange getCurrentLink(StyledText text){
	StyleRange[] ranges = text.getStyleRanges();
	int currentSelectionEnd = text.getSelection().y;
	int currentSelectionStart = text.getSelection().x;
	
	for (int i = 0; i < ranges.length; i++) {
		if((currentSelectionStart >= ranges[i].start) && 
			(currentSelectionEnd <= (ranges[i].start + ranges[i].length))) {
			return ranges[i];
		}
	}
	return null;
}
/**
 * Adds listeners to the given styled text
 */
private void addListeners(StyledText styledText) {
	styledText.addMouseListener(new MouseAdapter() {
		public void mouseDown(MouseEvent e) {
			if (e.button != 1) {
				return;
			}
			mouseDown = true;
		}
		public void mouseUp(MouseEvent e) {
			mouseDown = false;
			StyledText text = (StyledText)e.widget;
			WelcomeItem item = (WelcomeItem)e.widget.getData();
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
					text.setSelectionRange(selectionRange.start, selectionRange.length);
					text.setCursor(null);
				}
			}
		}
	});
	
	styledText.addMouseMoveListener(new MouseMoveListener() {
		public void mouseMove(MouseEvent e) {
			// Do not change cursor on drag events
			if (mouseDown) {
				if (!dragEvent) {
					StyledText text = (StyledText)e.widget;
					text.setCursor(null);
				}
				dragEvent = true;
				return;
			}
			StyledText text = (StyledText)e.widget;
			WelcomeItem item = (WelcomeItem)e.widget.getData();
			int offset = -1;
			try {
				offset = text.getOffsetAtLocation(new Point(e.x, e.y));
			} catch (IllegalArgumentException ex) {
				// location is not over a character
			}
			if (offset == -1)
				text.setCursor(null);
			else if (item.isLinkAt(offset)) 
				text.setCursor(handCursor);
			else 
				text.setCursor(null);
		}
	});
	
	styledText.addTraverseListener(new TraverseListener() {
		public void keyTraversed(TraverseEvent e) {
			StyledText text = (StyledText)e.widget;
			
			switch (e.detail) {
			case SWT.TRAVERSE_ESCAPE:
				e.doit = true;
				break;
			case SWT.TRAVERSE_TAB_NEXT:
				// Handle Ctrl-Tab
				if ((e.stateMask & SWT.CTRL) != 0) {
					if (e.widget == lastText)
						return;
					else {
						e.doit = false;
						nextTabAbortTraversal = true;
						lastText.traverse(SWT.TRAVERSE_TAB_NEXT);
						return;
					}
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
					focusOn(nextText,0);
				}
				else {
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
					if (e.widget == firstText)
						return;
					else {
						e.doit = false;
						previousTabAbortTraversal = true;
						firstText.traverse(SWT.TRAVERSE_TAB_PREVIOUS);
						return;
					}
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
						previousText.setSelection(previousText.getCharCount());
						previousLink = findPreviousLink(previousText);
						if (previousLink == null)	
							focusOn(previousText,0);
						else {
							focusOn(previousText, previousText.getSelection().x);
							previousText.setSelectionRange(previousLink.start, previousLink.length);
						}
					}
					else {
						// stay at top of this widget
						focusOn(text, 0);
					}
				}
				else {
					// focusOn: allow none tab traversals to align
					focusOn(text, text.getSelection().x);
					text.setSelectionRange(previousLink.start, previousLink.length);
				}
				e.detail = SWT.TRAVERSE_NONE;
				e.doit = true;
				break;
			default:
				break;
			}
		}
	});
	
	styledText.addKeyListener(new KeyListener() {
		public void keyReleased(KeyEvent e){
			//Ignore a key release
		}
		public void keyPressed (KeyEvent event){
			StyledText text = (StyledText)event.widget;
			if(event.character == ' ' || event.character == SWT.CR){
				if(text != null) {
					WelcomeItem item = (WelcomeItem)text.getData();
	
					//Be sure we are in the selection
					int offset = text.getSelection().x + 1;
				
					if (item.isLinkAt(offset)) {	
						text.setCursor(busyCursor);
						item.triggerLinkAt(offset);
						StyleRange selectionRange = getCurrentLink(text);
						text.setSelectionRange(selectionRange.start, selectionRange.length);
						text.setCursor(null);
					}
				}
				return;
			}	
			
			// When page down is pressed, move the cursor to the next item in the 
			// welcome page.   Note that this operation wraps (pages to the top item
			// when the last item is reached).
			if(event.keyCode == SWT.PAGE_DOWN){
				focusOn(nextText(text),0);
				return;
			}
			
			// When page up is pressed, move the cursor to the previous item in the 
			// welcome page.  Note that this operation wraps (pages to the bottom item
			// when the first item is reached).
			if(event.keyCode == SWT.PAGE_UP){
				focusOn(previousText(text),0);
				return;
			}	
		}
	});
	
	styledText.addFocusListener(new FocusAdapter() {
		public void focusLost(FocusEvent e) {
			// Remember current text widget
			lastNavigatedText = (StyledText)e.widget;
		}
		public void focusGained(FocusEvent e) {
			currentText = (StyledText)e.widget;
			
			// Remove highlighted selection if text widget has changed
			if ((currentText != lastNavigatedText) && (lastNavigatedText != null))
				lastNavigatedText.setSelection(lastNavigatedText.getSelection().x);
				
			// enable/disable copy action
			copyAction.setEnabled(currentText.getSelectionCount()>0);
		}
	});
	
	styledText.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			// enable/disable copy action			
			StyledText text = (StyledText)e.widget;
			copyAction.setEnabled(text.getSelectionCount()>0);
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
	this.scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
	this.scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
	final Composite infoArea = new Composite(this.scrolledComposite, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.marginHeight = 10;
	layout.verticalSpacing = 5;
	layout.numColumns = 2;
	infoArea.setLayout(layout);
	GridData data = new GridData(GridData.FILL_BOTH);
	infoArea.setLayoutData(data);
	boolean wrapped = parser.isFormatWrapped();

	if (wrapped) {
		scrolledComposite.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				// in the wrapped case the height of the StyledText widgets will change as
				// the width of the info area changes, so recompute the height
				Point p = infoArea.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				scrolledComposite.setMinHeight(p.y);
			}
		});
	}

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
		JFaceColors.setColors(styledText,foreground,background);
		styledText.setText(getIntroItem().getText());
		setBoldRanges(styledText, item.getBoldRanges());
		setLinkRanges(styledText, item.getActionRanges());
		setLinkRanges(styledText, item.getHelpRanges());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalIndent = 20;
		styledText.setLayoutData(gd);
		styledText.setData(item);
		addListeners(styledText);
	
		Label spacer = new Label(infoArea, SWT.NONE);
		spacer.setBackground(background);
		gd = new GridData(); 
		gd.horizontalSpan = 2;
		spacer.setLayoutData(gd);
	}
	firstText = sampleStyledText;

	// Create the welcome items
	WelcomeItem[] items = getItems();
	for (int i = 0; i < items.length; i++) {
		Label image = new Label(infoArea, SWT.NONE);
		image.setBackground(background);
		image.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_OBJS_WELCOME_ITEM));
		GridData gd = new GridData(); 
		gd.horizontalIndent = 20;
		gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		image.setLayoutData(gd);

		StyledText styledText = new StyledText(infoArea, textStyle);
		this.texts.add(styledText);
		sampleStyledText = styledText;
		styledText.setCursor(null);
		JFaceColors.setColors(styledText,foreground,background);
		styledText.setText(items[i].getText());
		setBoldRanges(styledText, items[i].getBoldRanges());
		setLinkRanges(styledText, items[i].getActionRanges());
		setLinkRanges(styledText, items[i].getHelpRanges());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;		
		gd.verticalSpan = 2;
		styledText.setLayoutData(gd);
		styledText.setData(items[i]);
		addListeners(styledText);
			
		Label spacer = new Label(infoArea, SWT.NONE);
		spacer.setBackground(background);
		gd = new GridData(GridData.FILL_HORIZONTAL); 
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

	// Adjust the scrollbar increments
	if (sampleStyledText == null) {
		this.scrolledComposite.getHorizontalBar().setIncrement(HORZ_SCROLL_INCREMENT);
		this.scrolledComposite.getVerticalBar().setIncrement(VERT_SCROLL_INCREMENT);
	} else {
		GC gc = new GC(sampleStyledText);
		int width = gc.getFontMetrics().getAverageCharWidth();
		gc.dispose();
		this.scrolledComposite.getHorizontalBar().setIncrement(width);
		this.scrolledComposite.getVerticalBar().setIncrement(sampleStyledText.getLineHeight());
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
 * <ol>
 *   <li>Create one or more controls within the parent.</li>
 *   <li>Set the parent layout as needed.</li>
 *   <li>Register any global actions with the <code>IActionService</code>.</li>
 *   <li>Register any popup menus with the <code>IActionService</code>.</li>
 *   <li>Register a selection provider with the <code>ISelectionService</code>
 *     (optional). </li>
 * </ol>
 * </p>
 *
 * @param parent the parent control
 */
public void createPartControl(Composite parent) {
	// read our contents
	readFile();
	if (parser == null)
		return;

	handCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
	busyCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
	
	editorComposite = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.marginHeight = 0;
	layout.marginWidth = 0;
	layout.verticalSpacing = 0;
	layout.horizontalSpacing = 0;
	editorComposite.setLayout(layout);

	createTitleArea(editorComposite);

	Label titleBarSeparator = new Label(editorComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	titleBarSeparator.setLayoutData(gd);

	createInfoArea(editorComposite);

	WorkbenchHelp.setHelp(editorComposite, IHelpContextIds.WELCOME_EDITOR);
	
	this.colorListener = 
		new IPropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent event){
				if(event.getProperty().equals(JFacePreferences.HYPERLINK_COLOR)){
					Color fg = JFaceColors.getHyperlinkText(editorComposite.getDisplay());
					Iterator links = hyperlinkRanges.iterator();
					while(links.hasNext()){
						StyleRange range = (StyleRange) links.next();
						range.foreground = fg;
					}
				}
			}
		};
		
	JFacePreferences.getPreferenceStore().addPropertyChangeListener(this.colorListener);
		
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
	final CLabel messageLabel = new CLabel(titleArea, SWT.LEFT);
	JFaceColors.setColors(messageLabel,foreground,background);
	messageLabel.setText(getBannerTitle());
	messageLabel.setFont(JFaceResources.getHeaderFont());
	
	final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if(JFaceResources.HEADER_FONT.equals(event.getProperty())) {
				messageLabel.setFont(JFaceResources.getHeaderFont());
			}
		}
	};
	
	messageLabel.addDisposeListener(new DisposeListener() {
		public void widgetDisposed(DisposeEvent event) {
			JFaceResources.getFontRegistry().removeListener(fontListener);
		}
	});
	
	JFaceResources.getFontRegistry().addListener(fontListener);
	
	
	GridData gd = new GridData(GridData.FILL_BOTH);
	messageLabel.setLayoutData(gd);

	// Title image
	Label titleImage = new Label(titleArea, SWT.LEFT);
	titleImage.setBackground(background);
	titleImage.setImage(WorkbenchImages.getImage(IWorkbenchGraphicConstants.IMG_OBJS_WELCOME_BANNER));
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
public void dispose() {
	super.dispose();
	if (busyCursor != null)
		busyCursor.dispose();
	if (handCursor != null)
		handCursor.dispose();
	if (this.colorListener != null) {
		JFacePreferences.getPreferenceStore().
			removePropertyChangeListener(this.colorListener);
	}
}
/* (non-Javadoc)
 * Saves the contents of this editor.
 * <p>
 * Subclasses must override this method to implement the open-save-close lifecycle
 * for an editor.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public void doSave(IProgressMonitor monitor) {
	// do nothing
}
/* (non-Javadoc)
 * Saves the contents of this editor to another object.
 * <p>
 * Subclasses must override this method to implement the open-save-close lifecycle
 * for an editor.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public void doSaveAs() {
	// do nothing	
}
/**
 * Returns the title obtained from the parser
 */
private String getBannerTitle() {
	if (parser.getTitle() == null)
		return ""; //$NON-NLS-1$
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
/* (non-Javadoc)
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
/* (non-Javadoc)
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
public void init(IEditorSite site, IEditorInput input) throws PartInitException {
	if (!(input instanceof WelcomeEditorInput))
		throw new PartInitException("Invalid Input: Must be IFileEditorInput"); //$NON-NLS-1$
	setSite(site);
	setInput(input);
}

/* (non-Javadoc)
 * Returns whether the contents of this editor have changed since the last save
 * operation.
 * <p>
 * Subclasses must override this method to implement the open-save-close lifecycle
 * for an editor.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public boolean isDirty() {
	return false;
}
/* (non-Javadoc)
 * Returns whether the "save as" operation is supported by this editor.
 * <p>
 * Subclasses must override this method to implement the open-save-close lifecycle
 * for an editor.  For greater details, see <code>IEditorPart</code>
 * </p>
 *
 * @see IEditorPart
 */
public boolean isSaveAsAllowed() {
	return false;
}
/**
 * Read the contents of the welcome page
 */
public void read(InputStream is) {
	parser = new WelcomeParser();
	parser.parse(is);
}
/**
 * Reads the welcome file
 */
public void readFile() {
	URL url = ((WelcomeEditorInput)getEditorInput()).getAboutInfo().getWelcomePageURL();

	if (url == null)
		// should not happen 
		return;
		
	InputStream is = null;
	try {
		is = url.openStream();
		read(is);
	}
	catch (IOException e) {
		IStatus status = new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, 1, WorkbenchMessages.getString("WelcomeEditor.accessException"), e); //$NON-NLS-1$
		WorkbenchPlugin.log(WorkbenchMessages.getString("WelcomeEditor.readFileError"), status); //$NON-NLS-1$
	}
	finally {
		try { 
			if (is != null)
				is.close(); 
		} catch (IOException e) {}
	}
}
/**
 * Sets the styled text's bold ranges
 */
private void setBoldRanges(StyledText styledText, int[][] boldRanges) {
	for (int i = 0; i < boldRanges.length; i++) {
		StyleRange r = new StyleRange(boldRanges[i][0], boldRanges[i][1], null, null, SWT.BOLD);
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
public void setFocus() {
	if ((editorComposite != null) && (lastNavigatedText == null) && (currentText == null))
		editorComposite.setFocus();
}
/**
 * Sets the styled text's link (blue) ranges
 */
private void setLinkRanges(StyledText styledText, int[][] linkRanges) {
	//Color fg = styledText.getDisplay().getSystemColor(SWT.COLOR_BLUE);
	Color fg = JFaceColors.getHyperlinkText(styledText.getShell().getDisplay());
	for (int i = 0; i < linkRanges.length; i++) {
		StyleRange r = new StyleRange(linkRanges[i][0], linkRanges[i][1], fg, null);
		styledText.setStyleRange(r);
		hyperlinkRanges.add(r);
	}
}

}
