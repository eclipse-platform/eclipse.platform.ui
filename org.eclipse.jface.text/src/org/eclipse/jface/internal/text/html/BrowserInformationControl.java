/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.internal.text.html;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.ToolBar;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.jface.text.IDelayedInputChangeProvider;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.IInformationControlExtension3;
import org.eclipse.jface.text.IInformationControlExtension4;
import org.eclipse.jface.text.IInformationControlExtension5;
import org.eclipse.jface.text.IInputChangedListener;
import org.eclipse.jface.text.TextPresentation;


/**
 * Displays HTML information in a {@link org.eclipse.swt.browser.Browser} widget.
 * <p>
 * This {@link IInformationControlExtension2} expects {@link #setInput(Object)} to be
 * called with an argument of type {@link BrowserInformationControlInput}.
 * </p>
 * <p>
 * Moved into this package from <code>org.eclipse.jface.internal.text.revisions</code>.</p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.</p>
 * <p>
 * Current problems:
 * <ul>
 * 	<li>the size computation is too small</li>
 * 	<li>focusLost event is not sent - see https://bugs.eclipse.org/bugs/show_bug.cgi?id=84532</li>
 * </ul>
 * </p>
 * 
 * @since 3.2
 */
public class BrowserInformationControl implements IInformationControl, IInformationControlExtension, IInformationControlExtension2, IInformationControlExtension3, IInformationControlExtension4, IInformationControlExtension5, IDelayedInputChangeProvider, DisposeListener {


	/**
	 * Tells whether the SWT Browser widget and hence this information
	 * control is available.
	 *
	 * @param parent the parent component used for checking or <code>null</code> if none
	 * @return <code>true</code> if this control is available
	 */
	public static boolean isAvailable(Composite parent) {
		if (!fgAvailabilityChecked) {
			try {
				Browser browser= new Browser(parent, SWT.NONE);
				browser.dispose();
				fgIsAvailable= true;
				
				Slider sliderV= new Slider(parent, SWT.VERTICAL);
				Slider sliderH= new Slider(parent, SWT.HORIZONTAL);
				int width= sliderV.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
				int height= sliderH.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
				fgScrollBarSize= new Point(width, height);
				sliderV.dispose();
				sliderH.dispose();
			} catch (SWTError er) {
				fgIsAvailable= false;
			} finally {
				fgAvailabilityChecked= true;
			}
		}

		return fgIsAvailable;
	}


	/** Border thickness in pixels. */
	private static final int BORDER= 1;
	
	/**
	 * Minimal size constraints.
	 * @since 3.2
	 */
	private static final int MIN_WIDTH= 80;
	private static final int MIN_HEIGHT= 50;

	
	/**
	 * Availability checking cache.
	 */
	private static boolean fgIsAvailable= false;
	private static boolean fgAvailabilityChecked= false;

	/**
	 * Cached scroll bar width and height
	 * @since 3.4
	 */
	private static Point fgScrollBarSize;
	
	/** The control's shell */
	private Shell fShell;
	/** The control's browser widget */
	private Browser fBrowser;
	/** Tells whether the browser has content */
	private boolean fBrowserHasContent;
	/** The control width constraint */
	private int fMaxWidth= SWT.DEFAULT;
	/** The control height constraint */
	private int fMaxHeight= SWT.DEFAULT;
	
	private Label fSeparator;
	private Font fStatusTextFont;
	private Label fStatusTextField;
	private String fStatusFieldText;
	
	private ToolBarManager fToolBarManager;
	private Label fTBSeparator;
	private ToolBar fToolBar;
	
	private final int fBorderWidth;
	private boolean fHideScrollBars;
	private Listener fShellListener;
	private ListenerList fFocusListeners= new ListenerList(ListenerList.IDENTITY);
	private TextLayout fTextLayout;
	private TextStyle fBoldStyle;

	private BrowserInformationControlInput fInput;

	/**
	 * <code>true</code> iff the browser has completed loading of the last
	 * input set via {@link #setInformation(String)}.
	 * @since 3.4
	 */
	private boolean fCompleted= false;

	/**
	 * The listener to be notified when a delayed location changing event happened.
	 * @since 3.4
	 */
	private IInputChangedListener fDelayedInputChangeListener;

	/**
	 * The listeners to be notified when the input changed.
	 * @since 3.4
	 */
	private ListenerList/*<IInputChangedListener>*/ fInputChangeListeners= new ListenerList(ListenerList.IDENTITY);

	/**
	 * The symbolic name of the font used for size computations, or <code>null</code> to use dialog font.
	 * @since 3.4
	 */
	private final String fSymbolicFontName;
	
	/**
	 * Creates a browser information control with the given shell as parent. The given
	 * information presenter is used to process the information to be displayed. The given
	 * styles are applied to the created browser widget.
	 *
	 * @param parent the parent shell
	 * @param shellStyle the additional styles for the shell
	 * @param style the additional styles for the browser widget
	 */
	public BrowserInformationControl(Shell parent, int shellStyle, int style) {
		this(parent, shellStyle, style, null, null, null);
	}

	/**
	 * Creates a browser information control with the given shell as parent. The given
	 * information presenter is used to process the information to be displayed. The given
	 * styles are applied to the created browser widget.
	 *
	 * @param parent the parent shell
	 * @param shellStyle the additional styles for the shell
	 * @param style the additional styles for the browser widget
	 * @param statusFieldText the text to be used in the optional status field
	 *                         or <code>null</code> if the status field should be hidden
	 */
	public BrowserInformationControl(Shell parent, int shellStyle, int style, String statusFieldText) {
		this(parent, shellStyle, style, null, null, statusFieldText);
	}
	
	/**
	 * Creates a browser information control with the given shell as parent. The given
	 * information presenter is used to process the information to be displayed. The given
	 * styles are applied to the created browser widget.
	 * 
	 * @param parent the parent shell
	 * @param shellStyle the additional styles for the shell
	 * @param style the additional styles for the browser widget
	 * @param symbolicFontName the symbolic name of the font used for size computations
	 * @param statusFieldText the text to be used in the optional status field
	 *            or <code>null</code> if the status field should be hidden
	 * @since 3.4
	 */
	public BrowserInformationControl(Shell parent, int shellStyle, int style, String symbolicFontName, String statusFieldText) {
		this(parent, shellStyle, style, symbolicFontName, null, statusFieldText);
	}
	
	/**
	 * Creates a browser information control with the given shell as parent. The given
	 * information presenter is used to process the information to be displayed. The given
	 * styles are applied to the created browser widget.
	 * 
	 * @param parent the parent shell
	 * @param shellStyle the additional styles for the shell
	 * @param style the additional styles for the browser widget
	 * @param symbolicFontName the symbolic name of the font used for size computations
	 * @param toolBarManager the tool bar manager or <code>null</code> to hide the tool bar
	 * @since 3.4
	 */
	public BrowserInformationControl(Shell parent, int shellStyle, int style, String symbolicFontName, ToolBarManager toolBarManager) {
		this(parent, shellStyle, style, symbolicFontName, toolBarManager, null);
	}
	
	/**
	 * Creates a browser information control with the given shell as parent. The given
	 * information presenter is used to process the information to be displayed. The given
	 * styles are applied to the created browser widget.
	 * <p>
	 * At most one of <code>toolBarManager</code> or <code>statusFieldText</code> can be non-null.
	 * </p>
	 * 
	 * @param parent the parent shell
	 * @param shellStyle the additional styles for the shell
	 * @param style the additional styles for the browser widget
	 * @param symbolicFontName the symbolic name of the font used for size computations
	 * @param toolBarManager the tool bar manager or <code>null</code> to hide the tool bar
	 * @param statusFieldText the text to be used in the optional status field
	 *            or <code>null</code> if the status field should be hidden
	 * @since 3.4
	 */
	private BrowserInformationControl(Shell parent, int shellStyle, int style, String symbolicFontName, ToolBarManager toolBarManager, String statusFieldText) {
		Assert.isLegal(toolBarManager == null || statusFieldText == null);
		fSymbolicFontName= symbolicFontName;
		fToolBarManager= toolBarManager;
		fStatusFieldText= statusFieldText;
		
		fShell= new Shell(parent, SWT.ON_TOP | shellStyle);
		Display display= fShell.getDisplay();
		fShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));

		Composite composite= fShell;
		GridLayout layout= new GridLayout(1, false);
		fBorderWidth= ((shellStyle & SWT.NO_TRIM) == 0) ? 0 : BORDER;
		layout.marginHeight= fBorderWidth;
		layout.marginWidth= fBorderWidth;
		composite.setLayout(layout);
		
		if (statusFieldText != null || toolBarManager != null) {
			composite= new Composite(composite, SWT.NONE);
			layout= new GridLayout(1, false);
			layout.marginHeight= 0;
			layout.marginWidth= 0;
			layout.verticalSpacing= 1;
			layout.horizontalSpacing= 1;
			composite.setLayout(layout);
			
			GridData  gd= new GridData(GridData.FILL_BOTH);
			composite.setLayoutData(gd);
		}
		if (statusFieldText != null) {
			composite.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			composite.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		}

		createBrowser(composite, style);
		
		if (toolBarManager != null)
			createToolBar(composite, toolBarManager);
		if (statusFieldText != null)
			createStatusField(composite, statusFieldText);

		addDisposeListener(this);
		createTextLayout();
	}

	/**
	 * Creates the browser control.
	 * 
	 * @param composite the parent composite
	 * @param style the additional styles for the browser widget
	 */
	private void createBrowser(Composite composite, int style) {
		fBrowser= new Browser(composite, SWT.NONE);
		fHideScrollBars= (style & SWT.V_SCROLL) == 0 && (style & SWT.H_SCROLL) == 0;
		
		GridData gd= new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
		fBrowser.setLayoutData(gd);
		
		Display display= fShell.getDisplay();
		fBrowser.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		fBrowser.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		fBrowser.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e)  {
				if (e.character == 0x1B) // ESC
					fShell.dispose(); // XXX: Just hide? Would avoid constant recreations.
			}

			public void keyReleased(KeyEvent e) {}
		});
		/*
		 * XXX revisit when the Browser support is better
		 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=107629 . Choosing a link to a
		 * non-available target will show an error dialog behind the ON_TOP shell that seemingly
		 * blocks the workbench. Disable links completely for now.
		 */
		fBrowser.addLocationListener(new LocationAdapter() {
			/*
			 * @see org.eclipse.swt.browser.LocationAdapter#changing(org.eclipse.swt.browser.LocationEvent)
			 */
			public void changing(LocationEvent event) {
				String location= event.location;
				/*
				 * Using the Browser.setText API triggers a location change to "about:blank".
				 * XXX: remove this code once https://bugs.eclipse.org/bugs/show_bug.cgi?id=130314 is fixed
				 */
				if (!"about:blank".equals(location)) //$NON-NLS-1$
					event.doit= false;
			}
		});

        fBrowser.addProgressListener(new ProgressAdapter() {
            public void completed(ProgressEvent event) {
            	fCompleted= true;
            }
        });
        
		// Replace browser's built-in context menu with none
		fBrowser.setMenu(new Menu(fShell, SWT.NONE));
	}

	/**
	 * Creates the tool bar.
	 * 
	 * @param composite parent
	 * @param toolBarManager manager
	 */
	private void createToolBar(Composite composite, ToolBarManager toolBarManager) {
		fTBSeparator= new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
		fTBSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		final Composite bars= new Composite(composite, SWT.NONE);
		bars.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		GridLayout layout= new GridLayout(2, false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		bars.setLayout(layout);
		
		fToolBar= toolBarManager.createControl(bars);
		GridData gd= new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
		fToolBar.setLayoutData(gd);
		
		addMoveSupport(bars);
	}

	/**
	 * Adds support to move the shell by dragging the given control.
	 * 
	 * @param control the control to make movable
	 * @since 3.4
	 */
	private void addMoveSupport(final Control control) {
		MouseAdapter moveSupport= new MouseAdapter() {
			private MouseMoveListener fMoveListener;

			public void mouseDown(MouseEvent e) {
				Point shellLoc= fShell.getLocation();
				final int shellX= shellLoc.x;
				final int shellY= shellLoc.y;
				Point mouseLoc= control.toDisplay(e.x, e.y);
				final int mouseX= mouseLoc.x;
				final int mouseY= mouseLoc.y;
				fMoveListener= new MouseMoveListener() {
					public void mouseMove(MouseEvent e2) {
						Point mouseLoc2= control.toDisplay(e2.x, e2.y);
						int dx= mouseLoc2.x - mouseX;
						int dy= mouseLoc2.y - mouseY;
						fShell.setLocation(shellX + dx, shellY + dy);
					}
				};
				control.addMouseMoveListener(fMoveListener);
			}
			
			public void mouseUp(MouseEvent e) {
				control.removeMouseMoveListener(fMoveListener);
				fMoveListener= null;
			}
		};
		control.addMouseListener(moveSupport);
	}

	/**
	 * Creates the status field.
	 * 
	 * @param composite the parent composite
	 * @param statusFieldText the text to show in the status field
	 */
	private void createStatusField(Composite composite, String statusFieldText) {
		fSeparator= new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
		fSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Status field label
		fStatusTextField= new Label(composite, SWT.RIGHT);
		fStatusTextField.setText(statusFieldText);
		Font font= fStatusTextField.getFont();
		FontData[] fontDatas= font.getFontData();
		for (int i= 0; i < fontDatas.length; i++)
			fontDatas[i].setHeight(fontDatas[i].getHeight() * 9 / 10);
		fStatusTextFont= new Font(fStatusTextField.getDisplay(), fontDatas);
		fStatusTextField.setFont(fStatusTextFont);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		fStatusTextField.setLayoutData(gd);

		Display display= fShell.getDisplay();
		fStatusTextField.setForeground(display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
		fStatusTextField.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
	}

	/**
	 * {@inheritDoc}
	 * @deprecated use {@link #setInput(Object)}
	 */
	public void setInformation(final String content) {
		setInput(new BrowserInformationControlInput(null) {
			public String getHtml() {
				return content;
			}
		});
	}
	
	/**
	 * {@inheritDoc} This control can handle {@link String} and
	 * {@link BrowserInformationControlInput}.
	 */
	public void setInput(Object input) {
		Assert.isLegal(input == null || input instanceof String || input instanceof BrowserInformationControlInput);

		if (input instanceof String) {
			setInformation((String)input);
			return;
		}

		fInput= (BrowserInformationControlInput)input;
		
		String content= null;
		if (fInput != null)
			content= fInput.getHtml();
		
		fBrowserHasContent= content != null && content.length() > 0;

		if (!fBrowserHasContent)
			content= "<html><body ></html>"; //$NON-NLS-1$

		int shellStyle= fShell.getStyle();
		boolean RTL= (shellStyle & SWT.RIGHT_TO_LEFT) != 0;

		// The default "overflow:auto" would not result in a predictable width for the client area
		// and the re-wrapping would cause visual noise
		String[] styles= null;
		if (RTL && !fHideScrollBars)
			styles= new String[] { "direction:rtl;", "overflow:scroll;", "word-wrap:break-word;" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		else if (RTL && fHideScrollBars)
			styles= new String[] { "direction:rtl;", "overflow:hidden;", "word-wrap:break-word;" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		else if (fHideScrollBars)
			//XXX: In IE, "word-wrap: break-word;" causes bogus wrapping even in non-broken words :-(see e.g. Javadoc of String).
			// Re-check whether we really still need this now that the Javadoc Hover header already sets this style.
			styles= new String[] { "overflow:hidden;"/*, "word-wrap: break-word;"*/ }; //$NON-NLS-1$
		else
			styles= new String[] { "overflow:scroll;" }; //$NON-NLS-1$
		
		StringBuffer buffer= new StringBuffer(content);
		HTMLPrinter.insertStyles(buffer, styles);
		content= buffer.toString();
		
		/*
		 * XXX: Should add some JavaScript here that shows something like
		 * "(continued...)" or "..." at the end of the visible area when the page overflowed
		 * with "overflow:hidden;".
		 */
		
		fCompleted= false;
		fBrowser.setText(content);
		
		Object[] listeners= fInputChangeListeners.getListeners();
		for (int i= 0; i < listeners.length; i++)
			((IInputChangedListener)listeners[i]).inputChanged(fInput);
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.IInformationControlExtension4#setStatusText(java.lang.String)
	 * @since 3.2
	 */
	public void setStatusText(String statusFieldText) {
		fStatusFieldText= statusFieldText;
	}

	/*
	 * @see IInformationControl#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (fShell.isVisible() == visible)
			return;
		
		if (visible) {
			if (fStatusTextField != null) {
				boolean state= fStatusFieldText != null;
				if (state)
					fStatusTextField.setText(fStatusFieldText);
				fStatusTextField.setVisible(state);
				fSeparator.setVisible(state);
			}
		}

		if (!visible) {
			fShell.setVisible(false);
			setInput(null);
			return;
		}
		
		/*
		 * The Browser widget flickers when made visible while it is not completely loaded.
		 * The fix is to delay the call to setVisible until either loading is completed
		 * (see ProgressListener in constructor), or a timeout has been reached.
		 */
		final Display display= fShell.getDisplay();
        
        // Make sure the display wakes from sleep after timeout:
        display.timerExec(100, new Runnable() {
            public void run() {
                fCompleted= true;
            }
        });
        
		while (!fCompleted) {
			// Drive the event loop to process the events required to load the browser widget's contents:
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		if (fShell == null || fShell.isDisposed())
			return;
		
		/*
		 * Avoids flickering when replacing hovers, especially on Vista in ON_CLICK mode.
		 * Causes flickering on GTK. Carbon does not care.
		 */
		if ("win32".equals(SWT.getPlatform())) //$NON-NLS-1$
			fShell.moveAbove(null);
		
        fShell.setVisible(true);
	}

	/**
	 * Creates and initializes the text layout used
	 * to compute the size hint.
	 * 
	 * @since 3.2
	 */
	private void createTextLayout() {
		fTextLayout= new TextLayout(fBrowser.getDisplay());
		
		// Initialize fonts
		Font font= fSymbolicFontName == null ? JFaceResources.getDialogFont() : JFaceResources.getFont(fSymbolicFontName);
		fTextLayout.setFont(font);
		fTextLayout.setWidth(-1);
		FontData[] fontData= font.getFontData();
		for (int i= 0; i < fontData.length; i++)
			fontData[i].setStyle(SWT.BOLD);
		font= new Font(fShell.getDisplay(), fontData);
		fBoldStyle= new TextStyle(font, null, null);
		
		// Compute and set tab width
		fTextLayout.setText("    "); //$NON-NLS-1$
		int tabWidth = fTextLayout.getBounds().width;
		fTextLayout.setTabs(new int[] {tabWidth});

		fTextLayout.setText(""); //$NON-NLS-1$
	}

	/*
	 * @see IInformationControl#dispose()
	 */
	public void dispose() {
		if (fTextLayout != null)
			fTextLayout.dispose();
		fTextLayout= null;
		if (fBoldStyle != null)
			fBoldStyle.font.dispose();
		fBoldStyle= null;
		if (fToolBarManager != null)
			fToolBarManager.dispose();
		if (fShell != null && !fShell.isDisposed())
			fShell.dispose();
		else
			widgetDisposed(null);
	}

	/*
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	public void widgetDisposed(DisposeEvent event) {
		if (fStatusTextFont != null && !fStatusTextFont.isDisposed())
			fStatusTextFont.dispose();

		fShell= null;
		fBrowser= null;
		fStatusTextFont= null;
	}

	/*
	 * @see IInformationControl#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		fShell.setSize(width, height);
	}

	/*
	 * @see IInformationControl#setLocation(Point)
	 */
	public void setLocation(Point location) {
		fShell.setLocation(location);
	}

	/*
	 * @see IInformationControl#setSizeConstraints(int, int)
	 */
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		fMaxWidth= maxWidth;
		fMaxHeight= maxHeight;
	}

	/*
	 * @see IInformationControl#computeSizeHint()
	 */
	public Point computeSizeHint() {
		Rectangle trim= computeTrim();
		int height= trim.height;
		
		//FIXME: The HTML2TextReader does not render <p> like a browser.
		// Instead of inserting an empty line, it just adds a single line break.
		// Furthermore, the indentation of <dl><dd> elements is too small (e.g with a long @see line)
		TextPresentation presentation= new TextPresentation();
		HTML2TextReader reader= new HTML2TextReader(new StringReader(fInput.getHtml()), presentation);
		String text;
		try {
			text= reader.getString();
		} catch (IOException e) {
			text= ""; //$NON-NLS-1$
		}

		fTextLayout.setText(text);
		fTextLayout.setWidth(fMaxWidth - trim.width);
		Iterator iter= presentation.getAllStyleRangeIterator();
		while (iter.hasNext()) {
			StyleRange sr= (StyleRange)iter.next();
			if (sr.fontStyle == SWT.BOLD)
				fTextLayout.setStyle(fBoldStyle, sr.start, sr.start + sr.length - 1);
		}
		
		Rectangle bounds= fTextLayout.getBounds(); // does not return minimum width, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=217446
		int lineCount= fTextLayout.getLineCount();
		int textWidth= 0;
		for (int i= 0; i < lineCount; i++) {
			Rectangle rect= fTextLayout.getLineBounds(i);
			textWidth= Math.max(textWidth, rect.x + rect.width);
		}
		bounds.width= textWidth;
		fTextLayout.setText(""); //$NON-NLS-1$
		
		int minWidth= bounds.width;
		height= height + bounds.height;
		
		// Add some air to accommodate for different browser renderings
		minWidth+= 15;
		height+= 15;

		// Consider width of text field and toolbar (height is already in trim)
		if (fStatusFieldText != null && fSeparator != null) {
			Point statusTextSize= fStatusTextField.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			minWidth= Math.max(minWidth, statusTextSize.x);
		}
		if (fToolBar != null && fTBSeparator != null) {
			Point toolBarSize= fToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			minWidth= Math.max(minWidth, toolBarSize.x);
		}
		
		// Apply max size constraints
		if (fMaxWidth != SWT.DEFAULT)
			minWidth= Math.min(fMaxWidth, minWidth + trim.width);
		if (fMaxHeight != SWT.DEFAULT)
			height= Math.min(fMaxHeight, height);

		// Ensure minimal size
		int width= Math.max(MIN_WIDTH, minWidth);
		height= Math.max(MIN_HEIGHT, height);
		
		return new Point(width, height);
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#computeTrim()
	 */
	public Rectangle computeTrim() {
		Rectangle trim= fShell.computeTrim(0, 0, 0, 0);
		addInternalTrim(trim);
		return trim;
	}

	/**
	 * Adds the internal trimmings to the given trim of the shell.
	 * 
	 * @param trim the shell's trim, will be updated
	 * @since 3.4
	 */
	private void addInternalTrim(Rectangle trim) {
		trim.x-= fBorderWidth;
		trim.y-= fBorderWidth;
		trim.width+= 2 * fBorderWidth;
		trim.height+= 2 * fBorderWidth;
		
		if (! fHideScrollBars) {
			boolean RTL= (fShell.getStyle() & SWT.RIGHT_TO_LEFT) != 0;
			if (RTL) {
				trim.x-= fgScrollBarSize.x;
			}
			trim.width+= fgScrollBarSize.x;
			trim.height+= fgScrollBarSize.y;
		}
		
		if (fStatusTextField != null) {
			trim.height+= 2; // from the layout's verticalSpacing
			trim.height+= fSeparator.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			trim.height+= fStatusTextField.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		}
		
		if (fToolBar != null) {
			trim.height+= 2; // from the layout's verticalSpacing
			trim.height+= fTBSeparator.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			trim.height+= fToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		}
	}
	
	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#getBounds()
	 */
	public Rectangle getBounds() {
//		return fShell == null || fShell.isDisposed() ? null : fShell.getBounds();
		return fShell.getBounds();
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#restoresLocation()
	 */
	public boolean restoresLocation() {
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#restoresSize()
	 */
	public boolean restoresSize() {
		return false;
	}

	/*
	 * @see IInformationControl#addDisposeListener(DisposeListener)
	 */
	public void addDisposeListener(DisposeListener listener) {
		fShell.addDisposeListener(listener);
	}

	/*
	 * @see IInformationControl#removeDisposeListener(DisposeListener)
	 */
	public void removeDisposeListener(DisposeListener listener) {
		fShell.removeDisposeListener(listener);
	}
	
	/**
	 * Adds the listener to the collection of listeners who will be
	 * notified when the current location has changed or is about to change.
	 * 
	 * @param listener the location listener
	 * @since 3.4
	 */
	public void addLocationListener(LocationListener listener) {
		fBrowser.addLocationListener(listener);
	}

	/*
	 * @see IInformationControl#setForegroundColor(Color)
	 */
	public void setForegroundColor(Color foreground) {
		fBrowser.setForeground(foreground);
	}

	/*
	 * @see IInformationControl#setBackgroundColor(Color)
	 */
	public void setBackgroundColor(Color background) {
		fBrowser.setBackground(background);
	}

	/*
	 * @see IInformationControl#isFocusControl()
	 */
	public boolean isFocusControl() {
		return fShell.getDisplay().getActiveShell() == fShell;
	}

	/*
	 * @see IInformationControl#setFocus()
	 */
	public void setFocus() {
		fShell.forceFocus();
		fBrowser.setFocus();
	}

	/*
	 * @see IInformationControl#addFocusListener(FocusListener)
	 */
	public void addFocusListener(final FocusListener listener) {
		if (fFocusListeners.isEmpty()) {
			fShellListener=  new Listener() {
				public void handleEvent(Event event) {
					Object[] listeners= fFocusListeners.getListeners();
					for (int i = 0; i < listeners.length; i++) {
						FocusListener focusListener= (FocusListener)listeners[i];
						if (event.type == SWT.Activate) {
							focusListener.focusGained(new FocusEvent(event));
						} else {
							focusListener.focusLost(new FocusEvent(event));
						}
					}
				}
			};
			fBrowser.getShell().addListener(SWT.Deactivate, fShellListener);
			fBrowser.getShell().addListener(SWT.Activate, fShellListener);
		}
		fFocusListeners.add(listener);
	}

	/*
	 * @see IInformationControl#removeFocusListener(FocusListener)
	 */
	public void removeFocusListener(FocusListener listener) {
		fFocusListeners.remove(listener);
		if (fFocusListeners.isEmpty()) {
			fBrowser.getShell().removeListener(SWT.Activate, fShellListener);
			fBrowser.getShell().removeListener(SWT.Deactivate, fShellListener);
			fShellListener= null;
		}
	}

	/*
	 * @see IInformationControlExtension#hasContents()
	 */
	public boolean hasContents() {
		return fBrowserHasContent;
	}
	
	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension5#containsControl(org.eclipse.swt.widgets.Control)
	 * @since 3.4
	 */
	public boolean containsControl(Control control) {
		do {
			if (control == fShell)
				return true;
			if (control instanceof Shell)
				return false;
			control= control.getParent();
		} while (control != null);
		return false;
	}
	
	/**
	 * Adds a listener for input changes to this input change provider.
	 * Has no effect if an identical listener is already registered.
	 * 
	 * @param inputChangeListener the listener to add
	 * @since 3.4
	 */
	public void addInputChangeListener(IInputChangedListener inputChangeListener) {
		Assert.isNotNull(inputChangeListener);
		fInputChangeListeners.add(inputChangeListener);
	}
	
	/**
	 * Removes the given input change listener from this input change provider.
	 * Has no effect if an identical listener is not registered.
	 * 
	 * @param inputChangeListener the listener to remove
	 * @since 3.4
	 */
	public void removeInputChangeListener(IInputChangedListener inputChangeListener) {
		fInputChangeListeners.remove(inputChangeListener);
	}
	
	/*
	 * @see org.eclipse.jface.text.IDelayedInputChangeProvider#setDelayedInputChangeListener(org.eclipse.jface.text.IInputChangedListener)
	 * @since 3.4
	 */
	public void setDelayedInputChangeListener(IInputChangedListener inputChangeListener) {
		fDelayedInputChangeListener= inputChangeListener;
	}
	
	/**
	 * Tells whether a delayed input change listener is registered.
	 * 
	 * @return <code>true</code> iff a delayed input change
	 *         listener is currently registered
	 * @since 3.4
	 */
	public boolean hasDelayedInputChangeListener() {
		return fDelayedInputChangeListener != null;
	}
	
	/**
	 * Notifies listeners of a delayed input change.
	 * 
	 * @param newInput the new input, or <code>null</code> to request cancellation
	 * @since 3.4
	 */
	public void notifyDelayedInputChange(Object newInput) {
		if (fDelayedInputChangeListener != null)
			fDelayedInputChangeListener.inputChanged(newInput);
	}
	
	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension5#isVisible()
	 * @since 3.4
	 */
	public boolean isVisible() {
		return fShell != null && ! fShell.isDisposed() && fShell.isVisible();
	}
	
	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension5#allowMoveIntoControl()
	 * @since 3.4
	 */
	public boolean allowMoveIntoControl() {
		return true;
	}
	
	/*
	 * @see java.lang.Object#toString()
	 * @since 3.4
	 */
	public String toString() {
		String style= (fShell.getStyle() & SWT.RESIZE) == 0 ? "fixed" : "resizeable"; //$NON-NLS-1$ //$NON-NLS-2$
		return super.toString() + " -  style: " + style; //$NON-NLS-1$
	}

	/**
	 * @return the current browser input or <code>null</code>
	 */
	public BrowserInformationControlInput getInput() {
		return fInput;
	}
	
	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension5#computeSizeConstraints(int, int)
	 */
	public Point computeSizeConstraints(int widthInChars, int heightInChars) {
		if (fSymbolicFontName == null)
			return null;
		
		GC gc= new GC(fBrowser);
		Font font= fSymbolicFontName == null ? JFaceResources.getDialogFont() : JFaceResources.getFont(fSymbolicFontName);
		gc.setFont(font);
		int width= gc.getFontMetrics().getAverageCharWidth();
		int height = gc.getFontMetrics().getHeight();
		gc.dispose();

		return new Point (widthInChars * width, heightInChars * height);
	}

}
