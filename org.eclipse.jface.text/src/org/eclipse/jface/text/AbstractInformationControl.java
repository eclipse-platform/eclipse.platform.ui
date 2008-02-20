/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

import org.eclipse.core.runtime.ListenerList;

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Geometry;


/**
 * The abstract information control can show any content inside a popup window.
 * Additionally it can present either a status line containing a status text or
 * a toolbar containing toolbar buttons.
 * <p>
 * Clients must implement {@link #createContent(Composite)} and
 * {@link IInformationControl#setInformation(String)}
 * </p>
 * <p>
 * FIXME: Work in progress. Will be modified in order to serve as super class
 * for the {@link BrowserInformationControl} as well, see: .
 * </p>
 * 
 * @since 3.4
 */
public abstract class AbstractInformationControl implements IInformationControl, IInformationControlExtension, IInformationControlExtension3, IInformationControlExtension5 {

	
	/** The control's popup dialog. */
	private PopupDialog fPopupDialog;
	/** Composite containing the content created by subclasses. */
	private Composite fContentComposite;
	/** Composite containing the status line content or <code>null</code> if none. */
	private Composite fStatusComposite;
	/** Separator between content and status line or <code>null</code> if none. */
	private Label fSeparator;
	/** Label in the status line or <code>null</code> if none. */
	private Label fStatusLabel;
	/** Status line toolbar or <code>null</code> if none. */
	private ToolBar fToolBar;
	/** Listener for shell activation and deactivation. */
	private Listener fShellListener;
	/** All focus listeners registered to this information control. */
	private ListenerList fFocusListeners= new ListenerList(ListenerList.IDENTITY);
	/** Size constrains, x is the maxWidth and y is the maxHeight, if any. */
	private Point fSizeConstaints;
	/** The toolbar manager used by the toolbar or <code>null</code> if none. */
	private final ToolBarManager fToolBarManager;
	
	
	/**
	 * Creates an abstract information control with the given shell as parent.
	 * The given shell style is used for popup shell. The control will show a
	 * status line with the given status field text.
	 * 
	 * @param parentShell the parent of the popup shell
	 * @param shellStyle style of the popup shell
	 * @param statusFieldText text to show in the status line
	 */
	public AbstractInformationControl(Shell parentShell, int shellStyle, String statusFieldText) {
		this(parentShell, shellStyle, statusFieldText, null);
	}

	/**
	 * Creates an abstract information control with the given shell as parent.
	 * The given shell style is used for popup shell. The control will show tool
	 * bar managed by the given tool bar manager.
	 * 
	 * @param parentShell the parent of the popup shell
	 * @param shellStyle style of the popup shell
	 * @param toolBarManager the manager of the popup tool bar
	 */
	public AbstractInformationControl(Shell parentShell, int shellStyle, ToolBarManager toolBarManager) {
		this(parentShell, shellStyle, null, toolBarManager);
	}
	
	/**
	 * Creates an abstract information control with the given shell as parent.
	 * The given shell style is used for popup shell. The control will show tool
	 * bar managed by the given tool bar manager.
	 * 
	 * @param parentShell the parent of the popup shell
	 * @param shellStyle style of the popup shell
	 * @param statusFieldText text to show in the status line, if any
	 * @param toolBarManager the manager of the popup tool bar, if any
	 */
	private AbstractInformationControl(Shell parentShell, int shellStyle, final String statusFieldText, final ToolBarManager toolBarManager) {
		fToolBarManager= toolBarManager;
		fPopupDialog= new PopupDialog(parentShell, shellStyle | SWT.NO_FOCUS | SWT.ON_TOP, false, false, false, false, null, null) {

			/*
			 * @see org.eclipse.jface.dialogs.PopupDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
			 */
			protected Control createDialogArea(Composite parent) {
				Composite composite= new Composite(parent, SWT.NONE);

				composite.setLayoutData(new GridData(GridData.BEGINNING | GridData.FILL_BOTH));

				GridLayout layout= new GridLayout();
				layout.marginHeight= 0;
				layout.marginWidth= 0;
				layout.verticalSpacing= 0;
				composite.setLayout(layout);

				fContentComposite= new Composite(composite, SWT.NONE);
				fContentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
				GridLayout contentLayout= new GridLayout(1, false);
				contentLayout.marginHeight= 0;
				contentLayout.marginWidth= 0;
				fContentComposite.setLayout(contentLayout);
				createContent(fContentComposite);

				if (toolBarManager != null || statusFieldText != null) {
					fStatusComposite= new Composite(composite, SWT.NONE);
					GridData gridData= new GridData(SWT.FILL, SWT.BOTTOM, true, false);
					fStatusComposite.setLayoutData(gridData);
					GridLayout gridLayout= new GridLayout(1, false);
					gridLayout.marginHeight= 0;
					gridLayout.marginWidth= 0;
					gridLayout.verticalSpacing= 0;
					fStatusComposite.setLayout(gridLayout);

					createStatusLine(fStatusComposite, statusFieldText, toolBarManager);
				}

				return composite;
			}

			/*
			 * @see org.eclipse.jface.dialogs.PopupDialog#getBackgroundColorExclusions()
			 */
			protected List getBackgroundColorExclusions() {
				List result= super.getBackgroundColorExclusions();
				
				if (fToolBar != null) {
					result.add(fStatusComposite);
					result.add(fSeparator);
					result.add(fToolBar);
				}
				
				return result;
			}

			/*
			 * @see org.eclipse.jface.dialogs.PopupDialog#getForegroundColorExclusions()
			 */
			protected List getForegroundColorExclusions() {
				List result= super.getForegroundColorExclusions();
				
				if (fStatusLabel != null) {
					result.add(fStatusLabel);
				}
				
				return result;
			}
		};

		// Force create early so that listeners can be added at all times with API.
		fPopupDialog.create();
	}
	
	/**
	 * The shell of the popup window.
	 * 
	 * @return the shell used for the popup window
	 */
	protected Shell getShell() {
		return fPopupDialog.getShell();
	}
	
	/**
	 * The toolbar manager used to manage the toolbar, or <code>null</code> if
	 * no toolbar is shown.
	 * 
	 * @return the tool bar manager or <code>null</code>
	 */
	protected final ToolBarManager getToolBarManager() {
		return fToolBarManager;
	}

	/**
	 * Creates the content of the popup window.
	 * 
	 * @param parent the container of the content
	 */
	protected abstract void createContent(Composite parent);

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
		fPopupDialog.getShell().setLocation(location);
	}

	/*
	 * @see IInformationControl#setSizeConstraints(int, int)
	 */
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		fSizeConstaints= new Point(maxWidth, maxHeight);
	}
	
	/**
	 * Returns the size constraints.
	 * 
	 * @return the size constraints or <code>null</code> if not set
	 * @see #setSizeConstraints(int, int)
	 */
	protected final Point getSizeConstraints() {
		return fSizeConstaints != null ? Geometry.copy(fSizeConstaints) : null;
	}

	/*
	 * @see IInformationControl#computeSizeHint()
	 */
	public Point computeSizeHint() {
		Point constrains= getSizeConstraints();
		if (constrains == null)
			return fPopupDialog.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		
		return fPopupDialog.getShell().computeSize(constrains.x, constrains.y, true);
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#computeTrim()
	 */
	public Rectangle computeTrim() {
		Shell shell= fPopupDialog.getShell();
		Rectangle trim= shell.computeTrim(0, 0, 0, 0);

		if (fToolBar != null) {
			trim.height+= fSeparator.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			trim.height+= fToolBar.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		} else if (fStatusLabel != null) {
			trim.height+= fSeparator.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
			trim.height+= fStatusLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
		}

		// Popup dialog adds a 1 pixel border when SWT.NO_TRIM is set:
		Layout layout= shell.getLayout();
		if (layout instanceof GridLayout) {
			GridLayout gridLayout= (GridLayout) layout;
			int left= gridLayout.marginLeft + gridLayout.marginWidth;
			int top= gridLayout.marginTop + gridLayout.marginHeight;
			trim.x-= left;
			trim.y-= top;
			trim.width+= left + gridLayout.marginRight + gridLayout.marginWidth;
			trim.height+= top + gridLayout.marginBottom + gridLayout.marginHeight;
		}
		
		return computeTrim(trim);
	}

	/**
	 * Compute the trim based on the given trim.
	 * <p>
	 * Subclasses can either adapt the given trim according to there needs or
	 * overwrite {@link #computeTrim()} directly.
	 * </p>
	 * 
	 * @param trim the proposed trim size
	 * @return the trim of the popup dialogs
	 * 
	 * @see org.eclipse.jface.text.IInformationControlExtension3#computeTrim()
	 */
	protected Rectangle computeTrim(Rectangle trim) {
		return trim;
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension3#getBounds()
	 */
	public Rectangle getBounds() {
		return fPopupDialog.getShell().getBounds();
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
		fContentComposite.setForeground(foreground);
	}

	/*
	 * @see IInformationControl#setBackgroundColor(Color)
	 */
	public void setBackgroundColor(Color background) {
		fContentComposite.setBackground(background);
	}

	/*
	 * @see IInformationControl#isFocusControl()
	 */
	public boolean isFocusControl() {
		Shell shell= fPopupDialog.getShell();
		return shell.getDisplay().getActiveShell() == shell;
	}

	/*
	 * @see IInformationControl#setFocus()
	 */
	public void setFocus() {
		fPopupDialog.getShell().forceFocus();
	}

	/*
	 * @see IInformationControl#addFocusListener(FocusListener)
	 */
	public void addFocusListener(final FocusListener listener) {
		if (fFocusListeners.isEmpty()) {
			fShellListener= new Listener() {

				public void handleEvent(Event event) {
					Object[] listeners= fFocusListeners.getListeners();
					for (int i= 0; i < listeners.length; i++) {
						FocusListener focusListener= (FocusListener)listeners[i];
						if (event.type == SWT.Activate) {
							focusListener.focusGained(new FocusEvent(event));
						} else {
							focusListener.focusLost(new FocusEvent(event));
						}
					}
				}
			};
			getShell().addListener(SWT.Deactivate, fShellListener);
			getShell().addListener(SWT.Activate, fShellListener);
		}
		fFocusListeners.add(listener);
	}

	/*
	 * @see IInformationControl#removeFocusListener(FocusListener)
	 */
	public void removeFocusListener(FocusListener listener) {
		fFocusListeners.remove(listener);
		if (fFocusListeners.isEmpty()) {
			getShell().removeListener(SWT.Activate, fShellListener);
			getShell().removeListener(SWT.Deactivate, fShellListener);
			fShellListener= null;
		}
	}

	/*
	 * @see IInformationControlExtension#hasContents()
	 */
	public boolean hasContents() {
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension5#containsControl(org.eclipse.swt.widgets.Control)
	 */
	public boolean containsControl(Control control) {
		do {
			Shell popupShell= fPopupDialog.getShell();
			if (control == popupShell)
				return true;
			if (control instanceof Shell)
				return false;
			control= control.getParent();
		} while (control != null);
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension5#isVisible()
	 */
	public boolean isVisible() {
		Shell popupShell= fPopupDialog.getShell();
		return popupShell != null && !popupShell.isDisposed() && popupShell.isVisible();
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension5#allowMoveIntoControl()
	 */
	public boolean allowMoveIntoControl() {
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlExtension5#computeSizeConstraints(int, int)
	 */
	public Point computeSizeConstraints(int widthInChars, int heightInChars) {
		GC gc= new GC(fContentComposite);
		gc.setFont(JFaceResources.getDialogFont());
		int width= gc.getFontMetrics().getAverageCharWidth();
		int height= gc.getFontMetrics().getHeight();
		gc.dispose();

		return new Point(widthInChars * width, heightInChars * height);
	}

	private void createStatusLine(Composite parent, String statusFieldText, ToolBarManager toolBarManager) {
		fSeparator= new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
		fSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (toolBarManager != null) {
			fToolBar= toolBarManager.createControl(parent);
			fToolBar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			
			addMoveSupport(parent);
		} else {
			fStatusLabel= new Label(parent, SWT.RIGHT);
			fStatusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			fStatusLabel.setText(statusFieldText);

			FontData[] fontDatas= fStatusLabel.getFont().getFontData();
			for (int i= 0; i < fontDatas.length; i++) {
				fontDatas[i].setHeight(fontDatas[i].getHeight() * 9 / 10);
			}
			fStatusLabel.setFont(new Font(fStatusLabel.getDisplay(), fontDatas));
			fStatusLabel.setForeground(fStatusLabel.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
		}
	}

	/**
	 * Adds support to move the shell by dragging the given control.
	 * 
	 * @param control the control that can be used to move the shell
	 */
	private void addMoveSupport(final Control control) {
		MouseAdapter moveSupport= new MouseAdapter() {
			private MouseMoveListener fMoveListener;

			public void mouseDown(MouseEvent e) {
				Point shellLoc= fPopupDialog.getShell().getLocation();
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
						fPopupDialog.getShell().setLocation(shellX + dx, shellY + dy);
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
	
}
