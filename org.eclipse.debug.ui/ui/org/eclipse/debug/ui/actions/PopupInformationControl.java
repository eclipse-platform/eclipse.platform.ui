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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlExtension;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.contexts.EnabledSubmission;

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
	protected static final String HEIGHT_STRING = "_DEBUGPOPUP_HEIGHT"; //$NON-NLS-1$
	protected static final String WIDTH_STRING = "_DEBUGPOPUP_WIDTH"; //$NON-NLS-1$
	
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
	 * Debug hover scope 
	 */
	private List hoverScope = Collections.singletonList(new EnabledSubmission((Shell)null, (IWorkbenchSite)null, "org.eclipse.debug.ui.debugging.popups")); //$NON-NLS-1$
	
	/**
	 * Default action used to close popup
	 */
	private IAction closeAction = null;
	
	/**
	 * The part this popup is parented by
	 */
	private IWorkbenchPart parentPart = null;

	/**
	 * Creates a popup to display information provided by adapter
	 * Style is set to SWT.NONE
	 * 
	 * @param parent the shell to parent the popup
	 * @param adapter Provides information  to display
	 */
	public PopupInformationControl(Shell parent, IPopupInformationControlAdapter adapter) {
		this(parent, SWT.NONE, adapter, null);
	}
	
	/**
	 * Creates a popup to display information provided by adapter
	 * @param parent the shell to parent the popup
	 * @param shellStyle The style to apply to the new instance
	 * @param adapter Provides information  to display
	 */
	public PopupInformationControl(Shell parent, int shellStyle, IPopupInformationControlAdapter adapter) {
		this(parent, shellStyle, adapter, null);
	}
	
	/**
	 * Creates a popup to display information provided by adapter, Popup will have a button
	 * that execute <code>action</code> when clicked
	 * @param parent the shell to parent the popup
	 * @param adapter Provides information  to display
	 * @param action Provide an action to execute. The action MUST have a KeyBinding associated with it.
	 */
	public PopupInformationControl(Shell parent, IPopupInformationControlAdapter adapter, IAction action) {
		this(parent, SWT.NONE, adapter, action);
	}
	/**
	 * Creates a popup to display information provided by adapter, Popup will have a button
	 * that execute <code>action</code> when clicked
	 * @param part the shell to parent the popup
	 * @param shellStyle The style to apply to the new instance
	 * @param adapter Provides information  to display
	 * @param action Provide an action to execute. The action MUST have a KeyBinding associated with it.
	 */	
	public PopupInformationControl(Shell parent, int shellStyle, IPopupInformationControlAdapter adapter, final	 IAction action) {
		this.adapter = adapter;
		parentPart = DebugUIPlugin.getActiveWorkbenchWindow().getActivePage().getActivePart();
		if (action != null) {
			closeAction = new WrappedAction(action);
		}
		
		shell= new Shell(parent, SWT.NO_FOCUS | SWT.ON_TOP | SWT.RESIZE | shellStyle);
		Display display = shell.getDisplay();
		shell.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		shell.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});		
		
		GridLayout layout= new GridLayout(1, false);
		int border= ((shellStyle & SWT.NO_TRIM) == 0) ? 0 : BORDER;
		layout.marginHeight= border;
		layout.marginWidth= border;
		shell.setLayout(layout);
		shell.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		GridData data= new GridData(GridData.FILL_BOTH);
		Composite composite = createInformationComposite();
		composite.setLayoutData(data);
		register();
		ICommandManager commandManager= PlatformUI.getWorkbench().getCommandSupport().getCommandManager();
		ICommand command = commandManager.getCommand(closeAction.getActionDefinitionId());
		if (action != null) {
			Label separator= new Label(shell, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.LINE_DOT);
			separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			Label label = new Label(shell, SWT.SHADOW_NONE | SWT.RIGHT);
			label.setText(closeAction.getText());
			label.setToolTipText(action.getDescription());
			label.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			label.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
			label.setEnabled(false);
			label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
			List keyBindings = command.getKeySequenceBindings();
			if (keyBindings != null && keyBindings.size() > 0) {
				IKeySequenceBinding lastBinding = (IKeySequenceBinding)keyBindings.get(keyBindings.size()-1);
				label.setText(MessageFormat.format(DebugUIMessages.getString("PopupInformationControl.1"), new String[] {lastBinding.getKeySequence().format(), closeAction.getText()})); //$NON-NLS-1$
				label.getParent().layout();
			}							
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
	 * control based upon data persisted in its IPopupInformantionControl first.
	 * If no data is found, it attempts to calculate a suggestion based 
	 * on the information to present. The method tries
	 * to honor known size constraints but might returns a size that 
	 * exceeds them. 
	 * @return The size suggested size for the control.
	 */
	public Point computeSizeHint() {
		Point persistedSize = getInitialSize();
		if (persistedSize != null) {
			return persistedSize;
		} else {
			Point computedSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			
			if (maxWidth > 0 && computedSize.x > maxWidth)
				computedSize.x = maxWidth;
			if (maxHeight > 0 && computedSize.y > maxHeight)
				computedSize.y = maxHeight;
			return computedSize;
		}
	}
	
	/**
	 * Disposes this control
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
		workbench.getContextSupport().removeEnabledSubmissions(hoverScope);
		workbench.getCommandSupport().deregisterFromKeyBindings(shell);
		if (closeAction != null) {
			parentPart.getSite().getKeyBindingService().unregisterAction(closeAction);
		}
	}
	
	/**
	 * Registers this popup's default close action and turns on the
	 * debug popup scope.
	 */
	private void register() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getContextSupport().addEnabledSubmissions(hoverScope);
		workbench.getCommandSupport().registerForKeyBindings(shell, false);
		if (closeAction != null) {
			parentPart.getSite().getKeyBindingService().registerAction(closeAction);
		}
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
		if (!visible) {
			deregister();
		}
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
	
	/**
	 * Attempts to retrieve the size of the popup when it was last disposed.
	 * @return The size the initial size of the popup if available, otherwise null 
	 */
	protected Point getInitialSize() {
		Point point = null;
		try {		
			IDialogSettings settings = adapter.getDialogSettings();
			if (settings != null) {
				String key = adapter.getClass().getName();
				
				int height = settings.getInt(key+HEIGHT_STRING); 
				int width = settings.getInt(key+WIDTH_STRING); 
				
				point = new Point(width, height);
			}
		} catch (NumberFormatException e) {
		}
		
		return point;
	}
	
	/**
	 * Attempts to store the current size of the popup in the adapter's IDialogSettings.
	 * Uses the adapters fully qualified class name to create unique keys.
	 */
	protected void persistSize() {
		if (shell == null) {
			return;
		}
		
		IDialogSettings settings = adapter.getDialogSettings();
		if (settings != null) {
			String key = adapter.getClass().getName();
			Point size = shell.getSize();
			settings.put(key+WIDTH_STRING, size.x); 
			settings.put(key+HEIGHT_STRING, size.y); 
		}
	}
	
	/**
	 * Wraps the supplied action in order to call dispose after any time the action has been completed. 
	 */
	private class WrappedAction implements IAction {
		IAction realAction;

		
		WrappedAction(IAction action) {
			realAction = action;			
		}
		
		public void addPropertyChangeListener(IPropertyChangeListener listener) {
			realAction.addPropertyChangeListener(listener);
		}
		public int getAccelerator() {
			return realAction.getAccelerator();
		}
		public String getActionDefinitionId() {
			return realAction.getActionDefinitionId();
		}
		public String getDescription() {
			return realAction.getDescription();
		}
		public ImageDescriptor getDisabledImageDescriptor() {
			return realAction.getDisabledImageDescriptor();
		}
		public HelpListener getHelpListener() {
			return realAction.getHelpListener();
		}
		public ImageDescriptor getHoverImageDescriptor() {
			return realAction.getHoverImageDescriptor();
		}
		public String getId() {
			return realAction.getId();
		}
		public ImageDescriptor getImageDescriptor() {
			return realAction.getImageDescriptor();
		}
		public IMenuCreator getMenuCreator() {
			return realAction.getMenuCreator();
		}
		public int getStyle() {
			return realAction.getStyle();
		}
		public String getText() {
			return realAction.getText();
		}
		public String getToolTipText() {
			return realAction.getToolTipText();
		}
		public boolean isChecked() {
			return realAction.isChecked();
		}
		public boolean isEnabled() {
			return realAction.isEnabled();	
		}
		public void removePropertyChangeListener(IPropertyChangeListener listener) {
			realAction.removePropertyChangeListener(listener);
		}
		public void run() {
			try {
				realAction.run();
			} finally {
				shell.dispose();
			}
		}
		public void runWithEvent(Event event) {
			try {
				realAction.runWithEvent(event);
			} finally {
				shell.dispose();
			}
		}
		public void setActionDefinitionId(String id) {
			realAction.setActionDefinitionId(id);
		}
		public void setChecked(boolean checked) {
			realAction.setChecked(checked);
		}
		public void setDescription(String text) {
			realAction.setDescription(text);
		}
		public void setDisabledImageDescriptor(ImageDescriptor newImage) {
			realAction.setDisabledImageDescriptor(newImage);
		}
		public void setEnabled(boolean enabled) {
			realAction.setEnabled(enabled);
		}
		public void setHelpListener(HelpListener listener) {
			realAction.setHelpListener(listener);
		}
		public void setHoverImageDescriptor(ImageDescriptor newImage) {
			realAction.setHoverImageDescriptor(newImage);
		}
		public void setId(String id) {
			realAction.setId(id);
		}
		public void setImageDescriptor(ImageDescriptor newImage) {
			realAction.setImageDescriptor(newImage);
		}
		public void setMenuCreator(IMenuCreator creator) {
			realAction.setMenuCreator(creator);
		}
		public void setText(String text) {
			realAction.setText(text);
		}
		public void setToolTipText(String text) {
			realAction.setToolTipText(text);
		}
		public void setAccelerator(int keycode) {
			realAction.setAccelerator(keycode);
		}
	}
	
}
