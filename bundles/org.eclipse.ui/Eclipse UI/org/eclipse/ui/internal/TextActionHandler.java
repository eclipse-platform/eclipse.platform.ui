package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

/**
 * Handles the redirection of the global Cut, Copy, Paste, and
 * Select All actions to either the current inline text control
 * or the part's supplied action handler.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p><p>
 * Example usage:
 * <pre>
 * textActionHandler = new TextActionHandler(this.getViewSite().getActionBars());
 * textActionHandler.addText((Text)textCellEditor1.getControl());
 * textActionHandler.addText((Text)textCellEditor2.getControl());
 * textActionHandler.setSelectAllAction(selectAllAction);
 * </pre>
 * </p>
 */
public class TextActionHandler {
	private CutActionHandler textCutAction = new CutActionHandler();
	private CopyActionHandler textCopyAction = new CopyActionHandler();
	private PasteActionHandler textPasteAction = new PasteActionHandler();
	private SelectAllActionHandler textSelectAllAction = new SelectAllActionHandler();
	
	private IAction cutAction;
	private IAction copyAction;
	private IAction pasteAction;
	private IAction selectAllAction;
	
	private IPropertyChangeListener cutActionListener = new PropertyChangeListener(textCutAction);
	private IPropertyChangeListener copyActionListener = new PropertyChangeListener(textCopyAction);
	private IPropertyChangeListener pasteActionListener = new PropertyChangeListener(textPasteAction);
	private IPropertyChangeListener selectAllActionListener = new PropertyChangeListener(textSelectAllAction);
	
	private Listener textControlListener = new TextControlListener();
	private Text activeTextControl;

	
	private class TextControlListener implements Listener {
		public void handleEvent(Event event) {
			switch (event.type) {
				case SWT.Activate:
					activeTextControl = (Text) event.widget;
					updateActionsEnableState();
					break;
				case SWT.Deactivate:
					activeTextControl = null;
					updateActionsEnableState();
					break;
				case SWT.Selection:
				case SWT.DefaultSelection:
					updateActionsEnableState();
					break;
				default:
					break;
			}
		}
	}
	
	private class PropertyChangeListener implements IPropertyChangeListener {
		private IAction actionHandler;
		protected PropertyChangeListener(IAction actionHandler) {
			super();
			this.actionHandler = actionHandler;
		}
		public void propertyChange(PropertyChangeEvent event) {
			if (activeTextControl != null)
				return;
			if (event.getProperty().equals(IAction.ENABLED)) {
				Boolean bool = (Boolean) event.getNewValue();
				actionHandler.setEnabled(bool.booleanValue());
			}
		}
	};
	
	private class CutActionHandler extends Action {
		protected CutActionHandler() {
			super(WorkbenchMessages.getString("Cut")); //$NON-NLS-1$
			setId("TextCellEditorCutActionHandler");//$NON-NLS-1$
			setEnabled(false);
		}
		public void run(Event event) {
			if (activeTextControl != null) {
				activeTextControl.cut();
				return;
			}
			if (cutAction != null) {
				cutAction.run(event);
				return;
			}
		}
		public void updateEnabledState() {
			if (activeTextControl != null) {
				setEnabled(activeTextControl.getSelectionCount() > 0);
				return;
			}
			if (cutAction != null) {
				setEnabled(cutAction.isEnabled());
				return;
			}
		}
	}
	
	private class CopyActionHandler extends Action {
		protected CopyActionHandler() {
			super(WorkbenchMessages.getString("Copy")); //$NON-NLS-1$
			setId("TextCellEditorCopyActionHandler");//$NON-NLS-1$
			setEnabled(false);
		}
		public void run(Event event) {
			if (activeTextControl != null) {
				activeTextControl.copy();
				return;
			}
			if (copyAction != null) {
				copyAction.run(event);
				return;
			}
		}
		public void updateEnabledState() {
			if (activeTextControl != null) {
				setEnabled(activeTextControl.getSelectionCount() > 0);
				return;
			}
			if (copyAction != null) {
				setEnabled(copyAction.isEnabled());
				return;
			}
		}
	}
	
	private class PasteActionHandler extends Action {
		protected PasteActionHandler() {
			super(WorkbenchMessages.getString("Paste")); //$NON-NLS-1$
			setId("TextCellEditorPasteActionHandler");//$NON-NLS-1$
			setEnabled(false);
		}
		public void run(Event event) {
			if (activeTextControl != null) {
				activeTextControl.paste();
				return;
			}
			if (pasteAction != null) {
				pasteAction.run(event);
				return;
			}
		}
		public void updateEnabledState() {
			if (activeTextControl != null) {
				setEnabled(true);
				return;
			}
			if (pasteAction != null) {
				setEnabled(pasteAction.isEnabled());
				return;
			}
		}
	}
	
	private class SelectAllActionHandler extends Action {
		protected SelectAllActionHandler() {
			super(WorkbenchMessages.getString("TextAction.selectAll")); //$NON-NLS-1$
			setId("TextCellEditorSelectAllActionHandler");//$NON-NLS-1$
			setEnabled(false);
		}
		public void run(Event event) {
			if (activeTextControl != null) {
				activeTextControl.selectAll();
				return;
			}
			if (selectAllAction != null) {
				selectAllAction.run(event);
				return;
			}
		}
		public void updateEnabledState() {
			if (activeTextControl != null) {
				setEnabled(true);
				return;
			}
			if (selectAllAction != null) {
				setEnabled(selectAllAction.isEnabled());
				return;
			}
		}
	}
/**
 * Creates a <code>Text</code> control action handler
 * for the global Cut, Copy, Paste, and Select All of
 * the action bar.
 *
 * @param actionBar the action bar to register global
 *    action handlers for Cut, Copy, Paste, and Select All
 */
public TextActionHandler(IActionBars actionBar) {
	super();
	actionBar.setGlobalActionHandler(IWorkbenchActionConstants.CUT, textCutAction);
	actionBar.setGlobalActionHandler(IWorkbenchActionConstants.COPY, textCopyAction);
	actionBar.setGlobalActionHandler(IWorkbenchActionConstants.PASTE, textPasteAction);
	actionBar.setGlobalActionHandler(IWorkbenchActionConstants.SELECT_ALL, textSelectAllAction);
}
/**
 * Add a <code>Text</code> control to the handler
 * so that the Cut, Copy, Paste, and Select All actions
 * are redirected to it when active.
 *
 * @param textControl the inline <code>Text</code> control
 */
public void addText(Text textControl) {
	if (textControl == null)
		return;

	textControl.addListener(SWT.Activate, textControlListener);
	textControl.addListener(SWT.Deactivate, textControlListener);
	textControl.addListener(SWT.Selection, textControlListener);
	textControl.addListener(SWT.DefaultSelection, textControlListener);
}
/**
 * Dispose of this action handler
 */
public void dispose() {
	setCutAction(null);
	setCopyAction(null);
	setPasteAction(null);
	setSelectAllAction(null);
}
/**
 * Removes a <code>Text</code> control from the handler
 * so that the Cut, Copy, Paste, and Select All actions
 * are no longer redirected to it when active.
 *
 * @param textControl the inline <code>Text</code> control
 */
public void removeText(Text textControl) {
	if (textControl == null)
		return;

	textControl.removeListener(SWT.Activate, textControlListener);
	textControl.removeListener(SWT.Deactivate, textControlListener);
	textControl.removeListener(SWT.Selection, textControlListener);
	textControl.removeListener(SWT.DefaultSelection, textControlListener);
}
/**
 * Set the default <code>IAction</code> handler for the Copy
 * action. This <code>IAction</code> is run only if no active
 * inline text control.
 *
 * @param action the <code>IAction</code> to run for the
 *    Copy action, or <code>null</null> if not interested.
 */
public void setCopyAction(IAction action) {
	if (copyAction == action)
		return;

	if (copyAction != null)
		copyAction.removePropertyChangeListener(copyActionListener);
		
	copyAction = action;

	if (copyAction != null)
		copyAction.addPropertyChangeListener(copyActionListener);

	textCopyAction.updateEnabledState();
}
/**
 * Set the default <code>IAction</code> handler for the Cut
 * action. This <code>IAction</code> is run only if no active
 * inline text control.
 *
 * @param action the <code>IAction</code> to run for the
 *    Cut action, or <code>null</null> if not interested.
 */
public void setCutAction(IAction action) {
	if (cutAction == action)
		return;

	if (cutAction != null)
		cutAction.removePropertyChangeListener(cutActionListener);
		
	cutAction = action;

	if (cutAction != null)
		cutAction.addPropertyChangeListener(cutActionListener);

	textCutAction.updateEnabledState();
}
/**
 * Set the default <code>IAction</code> handler for the Paste
 * action. This <code>IAction</code> is run only if no active
 * inline text control.
 *
 * @param action the <code>IAction</code> to run for the
 *    Paste action, or <code>null</null> if not interested.
 */
public void setPasteAction(IAction action) {
	if (pasteAction == action)
		return;

	if (pasteAction != null)
		pasteAction.removePropertyChangeListener(pasteActionListener);
		
	pasteAction = action;

	if (pasteAction != null)
		pasteAction.addPropertyChangeListener(pasteActionListener);

	textPasteAction.updateEnabledState();
}
/**
 * Set the default <code>IAction</code> handler for the Select All
 * action. This <code>IAction</code> is run only if no active
 * inline text control.
 *
 * @param action the <code>IAction</code> to run for the
 *    Select All action, or <code>null</null> if not interested.
 */
public void setSelectAllAction(IAction action) {
	if (selectAllAction == action)
		return;

	if (selectAllAction != null)
		selectAllAction.removePropertyChangeListener(selectAllActionListener);
		
	selectAllAction = action;

	if (selectAllAction != null)
		selectAllAction.addPropertyChangeListener(selectAllActionListener);

	textSelectAllAction.updateEnabledState();
}
/**
 * Update the enable state of the Cut, Copy,
 * Paste, and Select All action handlers
 */
private void updateActionsEnableState() {
	textCutAction.updateEnabledState();
	textCopyAction.updateEnabledState();
	textPasteAction.updateEnabledState();
	textSelectAllAction.updateEnabledState();
}
}
