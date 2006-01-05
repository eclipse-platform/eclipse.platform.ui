/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.PopupDialog;


/**
 * Default implementation of {@link org.eclipse.jface.text.IInformationControl}.
 * <p>
 * Displays textual information in a {@link org.eclipse.swt.custom.StyledText}
 * widget. Before displaying, the information set to this information control is
 * processed by an <code>IInformationPresenter</code>.
 *
 * @since 2.0
 */
public class DefaultInformationControl implements IInformationControl, IInformationControlExtension, IInformationControlExtension3, DisposeListener {

	/**
	 * An information presenter determines the style presentation
	 * of information displayed in the default information control.
	 * The interface can be implemented by clients.
	 */
	public interface IInformationPresenter {

		/**
		 * Updates the given presentation of the given information and
		 * thereby may manipulate the information to be displayed. The manipulation
		 * could be the extraction of textual encoded style information etc. Returns the
		 * manipulated information.
		 *
		 * @param display the display of the information control
		 * @param hoverInfo the information to be presented
		 * @param presentation the presentation to be updated
		 * @param maxWidth the maximal width in pixels
		 * @param maxHeight the maximal height in pixels
		 *
		 * @return the manipulated information
		 * @deprecated As of 3.2, replaced by {@link DefaultInformationControl.IInformationPresenterExtension#updatePresentation(Drawable, String, TextPresentation, int, int)}
		 * 				see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38528 for details.
		 */
		String updatePresentation(Display display, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight);
	}
	
	
	/**
	 * An information presenter determines the style presentation
	 * of information displayed in the default information control.
	 * The interface can be implemented by clients.
	 * 
	 * @since 3.2
	 */
	public interface IInformationPresenterExtension {
		
		/**
		 * Updates the given presentation of the given information and
		 * thereby may manipulate the information to be displayed. The manipulation
		 * could be the extraction of textual encoded style information etc. Returns the
		 * manipulated information.
		 * <p>
		 * Replaces {@link DefaultInformationControl.IInformationPresenter#updatePresentation(Display, String, TextPresentation, int, int)}
		 * <em>Make sure that you do not pass in a <code>Display</code></em> until
		 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=38528 is fixed.
		 * </p>
		 *
		 * @param drawable the drawable of the information control
		 * @param hoverInfo the information to be presented
		 * @param presentation the presentation to be updated
		 * @param maxWidth the maximal width in pixels
		 * @param maxHeight the maximal height in pixels
		 *
		 * @return the manipulated information
		 */
		String updatePresentation(Drawable drawable, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight);
	}
	

	/**
	 * Inner border thickness in pixels.
	 * @since 3.1
	 */
	private static final int INNER_BORDER= 1;

	/**
	 * The control's popup dialog.
	 * @since 3.2
	 */
	private PopupDialog fPopupDialog;
	/** The control's text widget */
	private StyledText fText;
	/** The information presenter */
	private IInformationPresenter fPresenter;
	/** A cached text presentation */
	private TextPresentation fPresentation= new TextPresentation();
	/** The control width constraint */
	private int fMaxWidth= -1;
	/** The control height constraint */
	private int fMaxHeight= -1;
	

	/**
	 * Creates a default information control with the given shell as parent. The given
	 * information presenter is used to process the information to be displayed. The given
	 * styles are applied to the created styled text widget.
	 *
	 * @param parent the parent shell
	 * @param shellStyle the additional styles for the shell
	 * @param style the additional styles for the styled text widget
	 * @param presenter the presenter to be used
	 */
	public DefaultInformationControl(Shell parent, int shellStyle, int style, IInformationPresenter presenter) {
		this(parent, shellStyle, style, presenter, null);
	}

	/**
	 * Creates a default information control with the given shell as parent. The given
	 * information presenter is used to process the information to be displayed. The given
	 * styles are applied to the created styled text widget.
	 *
	 * @param parentShell the parent shell
	 * @param shellStyle the additional styles for the shell
	 * @param style the additional styles for the styled text widget
	 * @param presenter the presenter to be used
	 * @param statusFieldText the text to be used in the optional status field
	 *                         or <code>null</code> if the status field should be hidden
	 * @since 3.0
	 */
	public DefaultInformationControl(Shell parentShell, int shellStyle, final int style, IInformationPresenter presenter, String statusFieldText) {
		boolean takeFocus= false;
		if ((shellStyle & SWT.RESIZE) != 0)
			takeFocus= true;
		else
			shellStyle= shellStyle | PopupDialog.HOVER_SHELLSTYLE;
		
		fPopupDialog= new PopupDialog(parentShell, shellStyle, takeFocus, false, false, false, null, statusFieldText) {
			protected Control createDialogArea(Composite parent) {
				// Text field
				fText= new StyledText(parent, SWT.MULTI | SWT.READ_ONLY | style);
				GridData gd= new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
				gd.horizontalIndent= INNER_BORDER;
				gd.verticalIndent= INNER_BORDER;
				fText.setLayoutData(gd);
				fText.addKeyListener(new KeyListener() {

					public void keyPressed(KeyEvent e)  {
						if (e.character == 0x1B) // ESC
							close();
					}

					public void keyReleased(KeyEvent e) {}
				});
				return fText;
			}
		};

		fPresenter= presenter;

		// Force create early so that listeners can be added at all times with API.
		fPopupDialog.create();
	}

	/**
	 * Creates a default information control with the given shell as parent. The given
	 * information presenter is used to process the information to be displayed. The given
	 * styles are applied to the created styled text widget.
	 *
	 * @param parent the parent shell
	 * @param style the additional styles for the styled text widget
	 * @param presenter the presenter to be used
	 */
	public DefaultInformationControl(Shell parent,int style, IInformationPresenter presenter) {
		this(parent, SWT.TOOL | SWT.NO_TRIM, style, presenter);
	}

	/**
	 * Creates a default information control with the given shell as parent. The given
	 * information presenter is used to process the information to be displayed. The given
	 * styles are applied to the created styled text widget.
	 *
	 * @param parent the parent shell
	 * @param style the additional styles for the styled text widget
	 * @param presenter the presenter to be used
	 * @param statusFieldText the text to be used in the optional status field
	 *                         or <code>null</code> if the status field should be hidden
	 * @since 3.0
	 */
	public DefaultInformationControl(Shell parent,int style, IInformationPresenter presenter, String statusFieldText) {
		this(parent, SWT.TOOL | SWT.NO_TRIM, style, presenter, statusFieldText);
	}

	/**
	 * Creates a default information control with the given shell as parent.
	 * No information presenter is used to process the information
	 * to be displayed. No additional styles are applied to the styled text widget.
	 *
	 * @param parent the parent shell
	 */
	public DefaultInformationControl(Shell parent) {
		this(parent, SWT.NONE, null);
	}

	/**
	 * Creates a default information control with the given shell as parent. The given
	 * information presenter is used to process the information to be displayed.
	 * No additional styles are applied to the styled text widget.
	 *
	 * @param parent the parent shell
	 * @param presenter the presenter to be used
	 */
	public DefaultInformationControl(Shell parent, IInformationPresenter presenter) {
		this(parent, SWT.NONE, presenter);
	}

	/*
	 * @see IInformationControl#setInformation(String)
	 */
	public void setInformation(String content) {
		if (fPresenter == null) {
			fText.setText(content);
		} else {
			fPresentation.clear();
			if (fPresenter instanceof IInformationPresenterExtension)
				content= ((IInformationPresenterExtension)fPresenter).updatePresentation(fPopupDialog.getShell(), content, fPresentation, fMaxWidth, fMaxHeight);
			else
				content= fPresenter.updatePresentation(fPopupDialog.getShell().getDisplay(), content, fPresentation, fMaxWidth, fMaxHeight);
			if (content != null) {
				fText.setText(content);
				TextPresentation.applyTextPresentation(fPresentation, fText);
			} else {
				fText.setText(""); //$NON-NLS-1$
			}
		}
	}

	/*
	 * @see IInformationControl#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (visible)
			fPopupDialog.open();
		else
			fPopupDialog.getShell().setVisible(false);
	}

	/*
	 * @see IInformationControl#dispose()
	 */
	public void dispose() {
		fPopupDialog.close();
		fPopupDialog= null;
	}

	/*
	 * @see IInformationControl#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		fPopupDialog.getShell().setSize(width, height);
	}

	/*
	 * @see IInformationControl#setLocation(Point)
	 */
	public void setLocation(Point location) {
		Rectangle trim= computeTrim();
		Point textLocation= fText.getLocation();
		location.x += trim.x - textLocation.x;
		location.y += trim.y - textLocation.y;
		fPopupDialog.getShell().setLocation(location);
	}

	/*
	 * @see IInformationControl#setSizeConstraints(int, int)
	 */
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		fMaxWidth= maxWidth;
		fMaxHeight= maxHeight;
		
		// see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=117602
		Object layoutData= fText.getLayoutData();
		if (layoutData instanceof GridData) {
			if (fMaxWidth > -1)
				((GridData)layoutData).widthHint= fMaxWidth;
			else
				((GridData)layoutData).widthHint= SWT.DEFAULT;
		}
	}

	/*
	 * @see IInformationControl#computeSizeHint()
	 */
	public Point computeSizeHint() {
		return fPopupDialog.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#computeTrim()
	 * @since 3.0
	 */
	public Rectangle computeTrim() {
		return fPopupDialog.getShell().computeTrim(0, 0, 0, 0);
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#getBounds()
	 * @since 3.0
	 */
	public Rectangle getBounds() {
		return fPopupDialog.getShell().getBounds();
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#restoresLocation()
	 * @since 3.0
	 */
	public boolean restoresLocation() {
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#restoresSize()
	 * @since 3.0
	 */
	public boolean restoresSize() {
		return false;
	}

	/*
	 * @see IInformationControl#addDisposeListener(DisposeListener)
	 */
	public void addDisposeListener(DisposeListener listener) {
		fPopupDialog.getShell().addDisposeListener(listener);
	}

	/*
	 * @see IInformationControl#removeDisposeListener(DisposeListener)
	 */
	public void removeDisposeListener(DisposeListener listener) {
		fPopupDialog.getShell().removeDisposeListener(listener);
	}

	/*
	 * @see IInformationControl#setForegroundColor(Color)
	 */
	public void setForegroundColor(Color foreground) {
		fText.setForeground(foreground);
	}

	/*
	 * @see IInformationControl#setBackgroundColor(Color)
	 */
	public void setBackgroundColor(Color background) {
		fText.setBackground(background);
	}

	/*
	 * @see IInformationControl#isFocusControl()
	 */
	public boolean isFocusControl() {
		return fText.isFocusControl();
	}

	/*
	 * @see IInformationControl#setFocus()
	 */
	public void setFocus() {
		fPopupDialog.getShell().forceFocus();
		fText.setFocus();
	}

	/*
	 * @see IInformationControl#addFocusListener(FocusListener)
	 */
	public void addFocusListener(FocusListener listener) {
		fText.addFocusListener(listener);
	}

	/*
	 * @see IInformationControl#removeFocusListener(FocusListener)
	 */
	public void removeFocusListener(FocusListener listener) {
		fText.removeFocusListener(listener);
	}

	/*
	 * @see IInformationControlExtension#hasContents()
	 */
	public boolean hasContents() {
		return fText.getCharCount() > 0;
	}

	/**
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 * @since 3.0
	 * @deprecated As of 3.2, no longer used and called
	 */
	public void widgetDisposed(DisposeEvent event) {
	}
}

