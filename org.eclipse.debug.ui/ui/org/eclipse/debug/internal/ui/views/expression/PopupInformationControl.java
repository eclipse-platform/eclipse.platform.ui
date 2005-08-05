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

package org.eclipse.debug.internal.ui.views.expression;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.AbstractHandler;
import org.eclipse.ui.commands.ExecutionException;
import org.eclipse.ui.commands.HandlerSubmission;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IHandler;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.commands.IWorkbenchCommandSupport;
import org.eclipse.ui.commands.Priority;
import org.eclipse.ui.contexts.IWorkbenchContextSupport;

/**
 * An information popup window. The window contains a control provided
 * by subclasses. A label is displayed at the bottom of the
 * window describing an action used to dismiss the window, which is invoked
 * with a key binding.
 * <p>
 * Clients are intended to subclass this class.
 * </p>
 * @see org.eclipse.debug.ui.actions.IPopupInformationControlAdapter
 * @see org.eclipse.jface.text.information.IInformationPresenter
 * @see org.eclipse.jface.text.information.IInformationProvider
 * @since 3.0
 */
public abstract class PopupInformationControl implements IInformationControl, IInformationControlExtension {
	
	private static final String HEIGHT_STRING = "_DEBUGPOPUP_HEIGHT"; //$NON-NLS-1$
	private static final String WIDTH_STRING = "_DEBUGPOPUP_WIDTH"; //$NON-NLS-1$
	
	/**
	 * The popup window
	 */
	protected Shell shell;
	
	/**
	 * The maximum width of the popup
	 */
	private int maxWidth = 300;
	
	/**
	 * The maximum height of the popup
	 */
	private int maxHeight = 300;
		
	/**
	 * ActionHandler for closeAction
	 */
	private HandlerSubmission submission;
	
	/**
	 * Handler used to close this popup, or <code>null</code> if none
	 */
	private IHandler closeHandler = null;
	
	/**
	 * Command id that provides a key sequence used to invoke this
	 * popup's close handler, or <code>null</code> if none
	 */
	private String commandId = null;
	
	/**
	 * Popup control
	 */
	private Control control = null;
			
	/**
	 * Creates a popup information control. When the specified command
	 * is invoked, the handler is invoked and the popup is closed.
	 * 
	 * @param parent the shell to parent the popup
	 * @param labelText label to display at the bottom of the popup window.
	 *  The label will be augmented with the key-sequence asscoaited with the
	 *  given commandId. 
	 * @param commandId command identifier used to bind a key sequence to
	 *  close the popup invoking <code>performCommand()</code>
	 */
	public PopupInformationControl(Shell parent, String labelText, String commandId) {
		this.closeHandler = new CloseHandler();
		this.commandId = commandId;
		
		shell= new Shell(parent, SWT.RESIZE);
		Display display = shell.getDisplay();
		shell.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		shell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		
		GridLayout layout= new GridLayout(1, false);
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		shell.setLayout(layout);
		shell.setLayoutData(new GridData(GridData.FILL_BOTH));
		control = createControl(shell);
		register();
		
		ICommandManager commandManager= PlatformUI.getWorkbench().getCommandSupport().getCommandManager();
		ICommand command = null;
		if (commandId != null) {
			command = commandManager.getCommand(commandId);
		}
		
		Label separator= new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(shell, SWT.SHADOW_NONE | SWT.RIGHT);
		label.setText(labelText);
		label.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		label.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		label.setEnabled(false);
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
		if (command != null) {
			List keyBindings = command.getKeySequenceBindings();
			if (keyBindings != null && keyBindings.size() > 0) {
				IKeySequenceBinding keySequenceBinding = (IKeySequenceBinding)keyBindings.get(0);
				label.setText(MessageFormat.format(DebugUIMessages.PopupInformationControl_1, new String[] {keySequenceBinding.getKeySequence().format(), labelText})); 
				label.getParent().layout();
			}			
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#addDisposeListener(DisposeListener)
	 */
	public void addDisposeListener(DisposeListener listener) {
		shell.addDisposeListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#addFocusListener(FocusListener)
	 */
	public void addFocusListener(FocusListener listener) {
		shell.addFocusListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#computeSizeHint()
	 */
	public Point computeSizeHint() {
		Point persistedSize = getInitialSize();
		if (persistedSize != null) {
			return persistedSize;
		}
		
		Point computedSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		if (maxWidth > 0 && computedSize.x > maxWidth)
			computedSize.x = maxWidth;
		if (maxHeight > 0 && computedSize.y > maxHeight)
			computedSize.y = maxHeight;
		return computedSize;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#dispose()
	 */
	public void dispose() {		
		deregister();
		persistSize();
		shell= null;
	}
	
	/**
	 * Deregisters this popup's default close action and turns off the
	 * debug popup scope.
	 */
	private void deregister() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchContextSupport contextSupport = workbench.getContextSupport();
		IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();
		
		commandSupport.removeHandlerSubmission(submission);
		contextSupport.unregisterShell(shell);
	}
	
	/**
	 * Registers this popup's default close action and turns on the
	 * debug popup scope.
	 */
	private void register() {
		if (closeHandler != null) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			
			IWorkbenchContextSupport contextSupport = workbench.getContextSupport();
			IWorkbenchCommandSupport commandSupport = workbench.getCommandSupport();
			
			submission = new HandlerSubmission(null, shell, null, commandId, closeHandler, Priority.MEDIUM);
			commandSupport.addHandlerSubmission(submission);
			
			contextSupport.registerShell(shell, IWorkbenchContextSupport.TYPE_DIALOG);
		}
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#removeDisposeListener(DisposeListener)
	 */
	public void removeDisposeListener(DisposeListener listener) {
		shell.removeDisposeListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#removeFocusListener(FocusListener)
	 */
	public void removeFocusListener(FocusListener listener) {
		shell.removeFocusListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setBackgroundColor(Color)
	 */
	public void setBackgroundColor(Color background) {
		shell.setBackground(background);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setForegroundColor(Color)
	 */
	public void setForegroundColor(Color foreground) {
		shell.setForeground(foreground);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setLocation(Point)
	 */
	public void setLocation(Point location) {
	    Rectangle displayBounds = control.getDisplay().getClientArea();
	    
	    location.x = location.x < 0 ? displayBounds.x + 25 : location.x;
	    location.y = location.y < 0 ? displayBounds.y + 25 : location.y;
	    
	    Point shellSize = shell.getSize();
	    boolean shellSizeChanged = false;
	    if (shellSize.x + location.x > displayBounds.width) {
	        shellSize.x = displayBounds.width - location.x;
	        shellSizeChanged = true;
	    }
	    if (shellSize.y + location.y > displayBounds.height) {
	        shellSize.y = displayBounds.height - location.y;
	        shellSizeChanged = true;
	    }
	    if (shellSizeChanged) {
	        shell.setSize(shellSize);
	    }

		shell.setLocation(location);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		shell.setSize(width, height);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setSizeContraints(int, int)
	 */
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		this.maxWidth = maxWidth;
		this.maxHeight = maxHeight;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		shell.setVisible(visible);
		if (!visible) {
			deregister();
			shell.dispose();
		}
	}
			
	/**
	 * Creates and returns the control for this popup.
	 *  
	 * @param parent parent control
	 * @return control
	 */
	protected abstract Control createControl(Composite parent);
	
	/**
	 * Attempts to retrieve the size of the popup when it was last disposed.
	 * @return The size the initial size of the popup if available, otherwise null 
	 */
	protected Point getInitialSize() {
		Point point = null;
		try {		
			IDialogSettings settings = getDialogSettings();
			if (settings != null) {
				String key = getClass().getName();
				
				int height = settings.getInt(key+HEIGHT_STRING); 
				int width = settings.getInt(key+WIDTH_STRING); 
				
				point = new Point(width, height);
			}
		} catch (NumberFormatException e) {
		}
		
		return point;
	}
	
	/**
	 * Returns a dialog settings in which to persist/restore popup control size.
	 * 
	 * @return dialog settings
	 */
	protected IDialogSettings getDialogSettings() {
		return DebugUIPlugin.getDefault().getDialogSettings();
	}
	
	/**
	 * Attempts to store the current size of the popup in the adapter's IDialogSettings.
	 * Uses the adapters fully qualified class name to create unique keys.
	 */
	protected void persistSize() {
		if (shell == null) {
			return;
		}
		
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			String key = getClass().getName();
			Point size = shell.getSize();
			settings.put(key+WIDTH_STRING, size.x); 
			settings.put(key+HEIGHT_STRING, size.y); 
		}
	}	

	/**
	 * Handler to closes this popup
	 */	
	private class CloseHandler extends AbstractHandler {
		/* (non-Javadoc)
		 * @see org.eclipse.ui.commands.IHandler#execute(java.lang.Object)
		 */
		public Object execute(Map parameter) throws ExecutionException {
			performCommand();
			if (shell != null) {
				shell.dispose();
			}
			return null;
		}
		
	}
	
	/**
	 * Called when this popup is closed via its command.
	 * Subclasses must override.
	 */
	protected abstract void performCommand(); 
	
	/**
	 * Returns this popup's shell.
	 * 
	 * @return shell
	 */
	protected Shell getShell() {
		return shell;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#isFocusControl()
	 */
	public boolean isFocusControl() {
		return control.isFocusControl();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.IInformationControl#setFocus()
	 */
	public void setFocus() {
		control.setFocus();
	}
}
