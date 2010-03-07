/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

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
 * @noextend This class is not intended to be subclassed by clients.
 */
public class TextActionHandler {
    private DeleteActionHandler textDeleteAction = new DeleteActionHandler();

    private CutActionHandler textCutAction = new CutActionHandler();

    private CopyActionHandler textCopyAction = new CopyActionHandler();

    private PasteActionHandler textPasteAction = new PasteActionHandler();

    private SelectAllActionHandler textSelectAllAction = new SelectAllActionHandler();

    private IAction deleteAction;

    private IAction cutAction;

    private IAction copyAction;

    private IAction pasteAction;

    private IAction selectAllAction;

    private IPropertyChangeListener deleteActionListener = new PropertyChangeListener(
            textDeleteAction);

    private IPropertyChangeListener cutActionListener = new PropertyChangeListener(
            textCutAction);

    private IPropertyChangeListener copyActionListener = new PropertyChangeListener(
            textCopyAction);

    private IPropertyChangeListener pasteActionListener = new PropertyChangeListener(
            textPasteAction);

    private IPropertyChangeListener selectAllActionListener = new PropertyChangeListener(
            textSelectAllAction);

    private Listener textControlListener = new TextControlListener();

    private Text activeTextControl;

	private IActionBars actionBars;

    private MouseAdapter mouseAdapter = new MouseAdapter() {
        public void mouseUp(MouseEvent e) {
            updateActionsEnableState();
        }
    };

    private KeyAdapter keyAdapter = new KeyAdapter() {
        public void keyReleased(KeyEvent e) {
            updateActionsEnableState();
        }
    };

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
            if (activeTextControl != null) {
				return;
			}
            if (event.getProperty().equals(IAction.ENABLED)) {
                Boolean bool = (Boolean) event.getNewValue();
                actionHandler.setEnabled(bool.booleanValue());
            }
        }
    }

    private class DeleteActionHandler extends Action {
        protected DeleteActionHandler() {
            super(IDEWorkbenchMessages.Delete);
            setId("TextDeleteActionHandler");//$NON-NLS-1$
            setEnabled(false);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
					IIDEHelpContextIds.TEXT_DELETE_ACTION);
        }

        public void runWithEvent(Event event) {
			if (activeTextControl != null && !activeTextControl.isDisposed()) {
				Point selection = activeTextControl.getSelection();
				if (selection.y == selection.x
						&& selection.x < activeTextControl.getCharCount()) {
					activeTextControl
							.setSelection(selection.x, selection.x + 1);
				}
				activeTextControl.insert(""); //$NON-NLS-1$

				updateActionsEnableState();
				return;
			}
            if (deleteAction != null) {
                deleteAction.runWithEvent(event);
                return;
            }
        }

        /**
         * Update state.
         */
        public void updateEnabledState() {
            if (activeTextControl != null && !activeTextControl.isDisposed()) {
                setEnabled(activeTextControl.getEditable()
                		&& (activeTextControl.getSelectionCount() > 0
                        || activeTextControl.getCaretPosition() < activeTextControl
                                .getCharCount()));
                return;
            }
            if (deleteAction != null) {
                setEnabled(deleteAction.isEnabled());
                return;
            }
            setEnabled(false);
        }
    }

    private class CutActionHandler extends Action {
        protected CutActionHandler() {
            super(IDEWorkbenchMessages.Cut);
            setId("TextCutActionHandler");//$NON-NLS-1$
            setEnabled(false);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
					IIDEHelpContextIds.TEXT_CUT_ACTION);
        }

        public void runWithEvent(Event event) {
            if (activeTextControl != null && !activeTextControl.isDisposed()) {
                activeTextControl.cut();
                updateActionsEnableState();
                return;
            }
            if (cutAction != null) {
                cutAction.runWithEvent(event);
                return;
            }
        }

        /**
         * Update state.
         */
        public void updateEnabledState() {
            if (activeTextControl != null && !activeTextControl.isDisposed()) {
                setEnabled(activeTextControl.getEditable() && activeTextControl.getSelectionCount() > 0);
                return;
            }
            if (cutAction != null) {
                setEnabled(cutAction.isEnabled());
                return;
            }
            setEnabled(false);
        }
    }

    private class CopyActionHandler extends Action {
        protected CopyActionHandler() {
            super(IDEWorkbenchMessages.Copy);
            setId("TextCopyActionHandler");//$NON-NLS-1$
            setEnabled(false);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
					IIDEHelpContextIds.TEXT_COPY_ACTION);
        }

        public void runWithEvent(Event event) {
            if (activeTextControl != null && !activeTextControl.isDisposed()) {
                activeTextControl.copy();
                updateActionsEnableState();
                return;
            }
            if (copyAction != null) {
                copyAction.runWithEvent(event);
                return;
            }
        }

        /**
         * Update the state.
         */
        public void updateEnabledState() {
            if (activeTextControl != null && !activeTextControl.isDisposed()) {
                setEnabled(activeTextControl.getSelectionCount() > 0);
                return;
            }
            if (copyAction != null) {
                setEnabled(copyAction.isEnabled());
                return;
            }
            setEnabled(false);
        }
    }

    private class PasteActionHandler extends Action {
        protected PasteActionHandler() {
            super(IDEWorkbenchMessages.Paste);
            setId("TextPasteActionHandler");//$NON-NLS-1$
            setEnabled(false);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
					IIDEHelpContextIds.TEXT_PASTE_ACTION);
        }

        public void runWithEvent(Event event) {
            if (activeTextControl != null && !activeTextControl.isDisposed()) {
                activeTextControl.paste();
                updateActionsEnableState();
                return;
            }
            if (pasteAction != null) {
                pasteAction.runWithEvent(event);
                return;
            }
        }

        /**
         * Update the state
         */
        public void updateEnabledState() {
            if (activeTextControl != null && !activeTextControl.isDisposed()) {
        		boolean canPaste = false;
            	if (activeTextControl.getEditable()) {
            		Clipboard clipboard = new Clipboard(activeTextControl.getDisplay());
            		TransferData[] td = clipboard.getAvailableTypes();
            		for (int i = 0; i < td.length; ++i) {
            			if (TextTransfer.getInstance().isSupportedType(td[i])) {
            				canPaste = true;
            				break;
            			}
            		}
            		
            		clipboard.dispose();
            	}
            	
                setEnabled(canPaste);
                return;
            }
            if (pasteAction != null) {
                setEnabled(pasteAction.isEnabled());
                return;
            }
            setEnabled(false);
        }
    }

    private class SelectAllActionHandler extends Action {
        protected SelectAllActionHandler() {
            super(IDEWorkbenchMessages.TextAction_selectAll);
            setId("TextSelectAllActionHandler");//$NON-NLS-1$
            setEnabled(false);
            PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
					IIDEHelpContextIds.TEXT_SELECT_ALL_ACTION);
        }

        public void runWithEvent(Event event) {
            if (activeTextControl != null && !activeTextControl.isDisposed()) {
                activeTextControl.selectAll();
                updateActionsEnableState();
                return;
            }
            if (selectAllAction != null) {
                selectAllAction.runWithEvent(event);
                return;
            }
        }

        /**
         * Update the state.
         */
        public void updateEnabledState() {
            if (activeTextControl != null && !activeTextControl.isDisposed()) {
                setEnabled(activeTextControl.getCharCount() > 0);
                return;
            }
            if (selectAllAction != null) {
                setEnabled(selectAllAction.isEnabled());
                return;
            }
            setEnabled(false);
        }
    }

    /**
     * Creates a <code>Text</code> control action handler
     * for the global Cut, Copy, Paste, Delete, and Select All 
     * of the action bar.
     *
     * @param actionBar the action bar to register global
     *    action handlers for Cut, Copy, Paste, Delete, 
     * 	  and Select All
     */
    public TextActionHandler(IActionBars actionBar) {
        super();
        actionBars = actionBar;
        updateActionBars();
    }

    /**
     * Updates the actions bars.
     * 
	 * @since 3.6 
	 */
	public void updateActionBars() {
		actionBars.setGlobalActionHandler(ActionFactory.CUT.getId(),
                textCutAction);
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(),
                textCopyAction);
		actionBars.setGlobalActionHandler(ActionFactory.PASTE.getId(),
                textPasteAction);
		actionBars.setGlobalActionHandler(ActionFactory.SELECT_ALL.getId(),
                textSelectAllAction);
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(),
                textDeleteAction);
	}

    
    /**
     * Add a <code>Text</code> control to the handler
     * so that the Cut, Copy, Paste, Delete, and Select All 
     * actions are redirected to it when active.
     *
     * @param textControl the inline <code>Text</code> control
     */
    public void addText(Text textControl) {
        if (textControl == null) {
			return;
		}

        textControl.addListener(SWT.Activate, textControlListener);
        textControl.addListener(SWT.Deactivate, textControlListener);

        // We really want a selection listener but it is not supported so we
        // use a key listener and a mouse listener to know when selection changes
        // may have occured
        textControl.addKeyListener(keyAdapter);
        textControl.addMouseListener(mouseAdapter);

        if (textControl.isFocusControl()) {
        	activeTextControl = textControl;
        	updateActionsEnableState();
        }
    }

    /**
     * Dispose of this action handler
     */
    public void dispose() {
        setCutAction(null);
        setCopyAction(null);
        setPasteAction(null);
        setSelectAllAction(null);
        setDeleteAction(null);
    }

    /**
     * Removes a <code>Text</code> control from the handler
     * so that the Cut, Copy, Paste, Delete, and Select All 
     * actions are no longer redirected to it when active.
     *
     * @param textControl the inline <code>Text</code> control
     */
    public void removeText(Text textControl) {
        if (textControl == null) {
			return;
		}

        textControl.removeListener(SWT.Activate, textControlListener);
        textControl.removeListener(SWT.Deactivate, textControlListener);

        textControl.removeMouseListener(mouseAdapter);
        textControl.removeKeyListener(keyAdapter);

        activeTextControl = null;
        updateActionsEnableState();
    }

    /**
     * Set the default <code>IAction</code> handler for the Copy
     * action. This <code>IAction</code> is run only if no active
     * inline text control.
     *
     * @param action the <code>IAction</code> to run for the
     *    Copy action, or <code>null</code> if not interested.
     */
    public void setCopyAction(IAction action) {
        if (copyAction == action) {
			return;
		}

        if (copyAction != null) {
			copyAction.removePropertyChangeListener(copyActionListener);
		}

        copyAction = action;

        if (copyAction != null) {
			copyAction.addPropertyChangeListener(copyActionListener);
		}

        textCopyAction.updateEnabledState();
    }

    /**
     * Set the default <code>IAction</code> handler for the Cut
     * action. This <code>IAction</code> is run only if no active
     * inline text control.
     *
     * @param action the <code>IAction</code> to run for the
     *    Cut action, or <code>null</code> if not interested.
     */
    public void setCutAction(IAction action) {
        if (cutAction == action) {
			return;
		}

        if (cutAction != null) {
			cutAction.removePropertyChangeListener(cutActionListener);
		}

        cutAction = action;

        if (cutAction != null) {
			cutAction.addPropertyChangeListener(cutActionListener);
		}

        textCutAction.updateEnabledState();
    }

    /**
     * Set the default <code>IAction</code> handler for the Paste
     * action. This <code>IAction</code> is run only if no active
     * inline text control.
     *
     * @param action the <code>IAction</code> to run for the
     *    Paste action, or <code>null</code> if not interested.
     */
    public void setPasteAction(IAction action) {
        if (pasteAction == action) {
			return;
		}

        if (pasteAction != null) {
			pasteAction.removePropertyChangeListener(pasteActionListener);
		}

        pasteAction = action;

        if (pasteAction != null) {
			pasteAction.addPropertyChangeListener(pasteActionListener);
		}

        textPasteAction.updateEnabledState();
    }

    /**
     * Set the default <code>IAction</code> handler for the Select All
     * action. This <code>IAction</code> is run only if no active
     * inline text control.
     *
     * @param action the <code>IAction</code> to run for the
     *    Select All action, or <code>null</code> if not interested.
     */
    public void setSelectAllAction(IAction action) {
        if (selectAllAction == action) {
			return;
		}

        if (selectAllAction != null) {
			selectAllAction
                    .removePropertyChangeListener(selectAllActionListener);
		}

        selectAllAction = action;

        if (selectAllAction != null) {
			selectAllAction.addPropertyChangeListener(selectAllActionListener);
		}

        textSelectAllAction.updateEnabledState();
    }

    /**
     * Set the default <code>IAction</code> handler for the Delete
     * action. This <code>IAction</code> is run only if no active
     * inline text control.
     *
     * @param action the <code>IAction</code> to run for the
     *    Delete action, or <code>null</code> if not interested.
     */
    public void setDeleteAction(IAction action) {
        if (deleteAction == action) {
			return;
		}

        if (deleteAction != null) {
			deleteAction.removePropertyChangeListener(deleteActionListener);
		}

        deleteAction = action;

        if (deleteAction != null) {
			deleteAction.addPropertyChangeListener(deleteActionListener);
		}

        textDeleteAction.updateEnabledState();
    }

    /**
     * Update the enable state of the Cut, Copy,
     * Paste, Delete, and Select All action handlers
     */
    private void updateActionsEnableState() {
        textCutAction.updateEnabledState();
        textCopyAction.updateEnabledState();
        textPasteAction.updateEnabledState();
        textSelectAllAction.updateEnabledState();
        textDeleteAction.updateEnabledState();
    }
}
