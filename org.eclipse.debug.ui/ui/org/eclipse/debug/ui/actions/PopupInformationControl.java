/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * A popup that appears on top of a text viewer displaying 
 * information controlled by an 
 * <code>IPopupInformationControlAdapter</code>. 
 * <p>
 * Clients may instantiate this class. This class is not intended
 * to be subclassed.
 * </p>
 * <p>
 * This class is yet experimental.
 * </p>
 * @see org.eclipse.debug.ui.actions.IPopupInformationControlAdapter
 * @see org.eclipse.jface.text.information.IInformationPresenter
 * @see org.eclipse.jface.text.information.IInformationProvider
 * @since 3.0
 */
public class PopupInformationControl implements IInformationControl, IInformationControlExtension {
	/**
	 * Default border size
	 */
	private static final int BORDER= 5;
	
	/**
	 * The popup window
	 */
	private Shell shell;
	
	/**
	 * The maximum width of the popup
	 */
	private int maxWidth = -1;
	
	/**
	 * The maximum height of the popup
	 */
	private int maxHeight = -1;
	
	/**
	 * Configures the presentation of any information to be displayed.
	 */
	private IPopupInformationControlAdapter adapter;
	
	/**
	 * Creates a popup to display information provided by adapter
	 * Style is set to SWT.NONE
	 * 
	 * @param parent A shell which will be the parent of the new instance
	 * @param adapter Provides information  to display
	 */
	public PopupInformationControl(Shell parent, IPopupInformationControlAdapter adapter) {
		this(parent, SWT.NONE, adapter, null);
	}

	/**
	 * Creates a popup to display information provided by adapter
	 * @param parent A shell which will be the parent of the new instance
	 * @param shellStyle The style to apply to the new instance
	 * @param adapter Provides information  to display
	 */
	public PopupInformationControl(Shell parent, int shellStyle, IPopupInformationControlAdapter adapter) {
		this(parent, shellStyle, adapter, null);
	}
	
	/**
	 * Creates a popup to display information provided by adapter, Popup will have a button
	 * that execute <code>action</code> when clicked
	 * @param parent parent A shell which will be the parent of the new instance
	 * @param adapter Provides information  to display
	 * @param action Provide an action to execute on button click
	 */
	public PopupInformationControl(Shell parent, IPopupInformationControlAdapter adapter, IAction action) {
		this(parent, SWT.NONE, adapter, action);
	}
	/**
	 * Creates a popup to display information provided by adapter, Popup will have a button
	 * that execute <code>action</code> when clicked
	 * @param parent parent A shell which will be the parent of the new instance
	 * @param shellStyle The style to apply to the new instance
	 * @param adapter Provides information  to display
	 * @param action Provide an action to execute on button click 
	 */	
	public PopupInformationControl(Shell parent, int shellStyle, IPopupInformationControlAdapter adapter, IAction action) {
		this.adapter = adapter;
		
		shell= new Shell(parent, SWT.NO_FOCUS | SWT.ON_TOP | SWT.RESIZE | shellStyle);
		Display display = shell.getDisplay();
		shell.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		shell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		
		GridLayout layout= new GridLayout(1, false);
		int border= ((shellStyle & SWT.NO_TRIM) == 0) ? 0 : BORDER;
		layout.marginHeight= border;
		layout.marginWidth= border;
		shell.setLayout(layout);
		shell.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		GridData data= new GridData(GridData.FILL_BOTH);
		Composite composite = createInformationComposite();
		composite.setLayoutData(data);
		
		if (action != null) {
			Label separator= new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
			separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			Label label = new Label(shell, SWT.SHADOW_NONE | SWT.RIGHT);
			label.setText(action.getText());
			label.setToolTipText(action.getDescription());
			label.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			label.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			label.setEnabled(false);
			label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			
			final IAction finalAction = action;
			composite.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.character == SWT.CR)
						finalAction.run();
					dispose();
				}
			});
		}

	}

	/**
	 * Adds the given listener to the list of dispose listeners.
	 * @param listener The listener to be added
	 */
	public void addDisposeListener(DisposeListener listener) {
		shell.addDisposeListener(listener);
	}
	
	/**
	 * Adds the given listener to the list of focus listeners
	 * @param listener The listener to be added
	 */
	public void addFocusListener(FocusListener listener) {
		shell.addFocusListener(listener);
	}
	
		/**
		 * Computes and returns a proposal for the size of this information 
		 * control depending on the information to present. The method tries
		 * to honor known size constraints but might returns a size that 
		 * exceeds them. 
		 * @return The size suggested size for the control.
		 */
		public Point computeSizeHint() {
			Point computedSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);

			if (maxWidth > 0 && computedSize.x > maxWidth)
				computedSize.x = maxWidth;
			if (maxHeight > 0 && computedSize.y > maxHeight)
				computedSize.y = maxHeight;
			return computedSize;
		}

	/**
	 * Disposes this control
	 */
	public void dispose() {
		if (shell != null && !shell.isDisposed()) 
			shell.dispose();
		shell= null;
	}
	
	/**
	 * Removes the given listener from the list of dispose listeners
	 * @param listener The listener to be removed
	 */
	public void removeDisposeListener(DisposeListener listener) {
		shell.removeDisposeListener(listener);
	}

	/**
	 * Removes the focus listener from the list of focus listeners
	 * @param listener The listener to be removed
	 */
	public void removeFocusListener(FocusListener listener) {
		shell.removeFocusListener(listener);
	}

	/**
	 * Sets the background colour of the control
	 * @param background The new background colour.
	 */
	public void setBackgroundColor(Color background) {
		shell.setBackground(background);
	}

	/**
	 * Sets the keyboard focus to this information control
	 */
	public void setFocus(){
		shell.forceFocus();
	}
	
	/**
	 * Sets the foreground colour for this control
	 * @param foreground The new foreground colour
	 */
	public void setForegroundColor(Color foreground) {
		shell.setForeground(foreground);
	}

	/**
	 * Sets the location of this control
	 * @param location The location the control will be placed
	 */
	public void setLocation(Point location) {
		shell.setLocation(location);		
	}
	
	/**
	 * Sets the size of the control
	 * @param width The width of the control
	 * @param height The height of the control
	 */
	public void setSize(int width, int height) {
		shell.setSize(width, height);
	}
	
	/**
	 * Sets constraints on size of the control.
	 * @param maxWidth The maximum width of the control
	 * @param maxHeight The maximum height of the control
	 */
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		this.maxWidth = 300;
		this.maxHeight = 175;
	}

	/**
	 * Sets the control's visibility
	 * @param visible A value of true set the control visible, false sets it to be invisible
	 */
	public void setVisible(boolean visible) {
		shell.setVisible(visible);
	}

	/**
	 * Returns true if the receiver has the user-interface focus, and false otherwise. 
	 * @return true if the receiver has the user-interface focus, and false otherwise. 
	 */
	public boolean isFocusControl() { 
		return shell.isFocusControl() || adapter.isFocusControl();
	}
	
	/**
	 * Returns result of IPopupInformationControlAdapter.hasContents().
	 * @return result of IPopupInformationControlAdapter.hasContents()
	 * @see IPopupInformationControlAdapter
	 */
	public boolean hasContents() {
		return adapter.hasContents();
	}
	
	/**
	 * Sets information in IPopupInformationControlAdapter
	 * @param information the information or a description of the information to be presented
	 */
	public void setInformation(String information) {
		adapter.setInformation(information);
	}
	
	/**
	 * Retrieves from IPopupInformationControlAdapter, the Composite used to display
	 * information to the user
	 * @return The graphical presentation of the adapter's information
	 */
	public Composite createInformationComposite() {
		return adapter.createInformationComposite(shell);
	}

}
