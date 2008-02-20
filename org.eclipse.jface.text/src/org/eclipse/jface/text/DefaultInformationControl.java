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
package org.eclipse.jface.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.util.Geometry;


/**
 * Default implementation of {@link org.eclipse.jface.text.IInformationControl}.
 * <p>
 * Displays textual information in a {@link org.eclipse.swt.custom.StyledText}
 * widget. Before displaying, the information set to this information control is
 * processed by an <code>IInformationPresenter</code>.
 *
 * @since 2.0
 */
public class DefaultInformationControl extends AbstractInformationControl {

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

	/** The control's text widget */
	private StyledText fText;
	/** The information presenter */
	private IInformationPresenter fPresenter;
	/** A cached text presentation */
	private TextPresentation fPresentation= new TextPresentation();
	
	/**
	 * Style to use for the text control.
	 * @since 3.4
     */
	private final int fTextStyle;

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
		super(parentShell, shellStyle, statusFieldText);
		fTextStyle= style;
		fPresenter= presenter;
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
	 * @see org.eclipse.jface.text.AbstractInformationControl#createContent(org.eclipse.swt.widgets.Composite)
	 */
	protected void createContent(Composite parent) {
		fText= new StyledText(parent, SWT.MULTI | SWT.READ_ONLY | fTextStyle);
		GridData gd= new GridData(GridData.BEGINNING | GridData.FILL_BOTH);
		gd.horizontalIndent= INNER_BORDER;
		gd.verticalIndent= INNER_BORDER;
		fText.setLayoutData(gd);
	}
	
	/*
	 * @see IInformationControl#setInformation(String)
	 */
	public void setInformation(String content) {
		if (fPresenter == null) {
			fText.setText(content);
		} else {
			fPresentation.clear();
			
			int maxWidth= -1;
			int maxHight= -1;
			Point constraints= getSizeConstraints();
			if (constraints != null) {
				maxWidth= constraints.x;
				maxHight= constraints.y;
			}
			
			if (fPresenter instanceof IInformationPresenterExtension)
				content= ((IInformationPresenterExtension) fPresenter).updatePresentation(getShell(), content, fPresentation, maxWidth, maxHight);
			else
				content= fPresenter.updatePresentation(getShell().getDisplay(), content, fPresentation, maxWidth, maxHight);
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
		if (visible) {
			if (fText.getWordWrap()) {
				Point currentSize= getShell().getSize();
				getShell().pack(true);
				Point newSize= getShell().getSize();
				if (newSize.x > currentSize.x || newSize.y > currentSize.y)
					setSize(currentSize.x, currentSize.y); // restore previous size
			}
		}
		
		super.setVisible(visible);
	}

	/*
	 * @see IInformationControl#computeSizeHint()
	 */
	public Point computeSizeHint() {
		// see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=117602
		int widthHint= SWT.DEFAULT;
		Point constraints= getSizeConstraints();
		if (constraints != null && fText.getWordWrap())
			widthHint= constraints.x;
		
		return getShell().computeSize(widthHint, SWT.DEFAULT, true);
	}

	/*
	 * @see org.eclipse.jface.text.AbstractInformationControl#computeTrim(org.eclipse.swt.graphics.Rectangle)
	 */
	protected Rectangle computeTrim(Rectangle trim) {
		return Geometry.add(trim, fText.computeTrim(0, 0, 0, 0));
	}

	/*
	 * @see IInformationControl#setForegroundColor(Color)
	 */
	public void setForegroundColor(Color foreground) {
		super.setForegroundColor(foreground);
		fText.setForeground(foreground);
	}

	/*
	 * @see IInformationControl#setBackgroundColor(Color)
	 */
	public void setBackgroundColor(Color background) {
		super.setBackgroundColor(background);
		fText.setBackground(background);
	}

	/*
	 * @see IInformationControl#setFocus()
	 */
	public void setFocus() {
		super.setFocus();
		fText.setFocus();
	}

}
