package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Iterator;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValueModification;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Action for changing the value of primitives and <code>String</code> variables.
 */
public class ChangeVariableValueAction extends SelectionProviderAction {

	// Fields for inline editing 
	protected Composite fComposite;
	protected Tree fTree;
	protected Label fEditorLabel;
	protected Text fEditorText;
	protected TreeEditor fTreeEditor;
	protected IVariable fVariable;
	protected boolean fKeyReleased= false;
	
	public ChangeVariableValueAction(Viewer viewer) {
		super(viewer, ActionMessages.getString("ChangeVariableValue.title")); //$NON-NLS-1$
		setDescription(ActionMessages.getString("ChangeVariableValue.toolTipText")); //$NON-NLS-1$
		fTree= ((TreeViewer)viewer).getTree();
		fTreeEditor= new TreeEditor(fTree);
		WorkbenchHelp.setHelp(
			this,
			IDebugHelpContextIds.CHANGE_VALUE_ACTION);
	}
	
	/**
	 * Edit the variable value with an inline text editor.  
	 */
	protected void doActionPerformed(final IVariable variable) {
		final Shell activeShell= DebugUIPlugin.getActiveWorkbenchWindow().getShell();
		
		// If a previous edit is still in progress, finish it
		if (fEditorText != null) {
			saveChangesAndCleanup(fVariable, activeShell);
		}
		fVariable = variable;
		
		// Use a Composite containing a Label and a Text.  This allows us to edit just
		// the value, while still showing the variable name.
		fComposite = new Composite(fTree, SWT.NONE);
		fComposite.setBackground(fTree.getBackground());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		fComposite.setLayout(layout);
		
		fEditorLabel = new Label(fComposite, SWT.LEFT);
		fEditorLabel.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		
		// Fix for bug 1766.  Border behavior on Windows & Linux for text
		// fields is different.  On Linux, you always get a border, on Windows,
		// you don't.  Specifying a border on Linux results in the characters
		// getting pushed down so that only there very tops are visible.  Thus,
		// we have to specify different style constants for the different platforms.
		int textStyles = SWT.SINGLE | SWT.LEFT;
		if (SWT.getPlatform().equals("win32")) {  //$NON-NLS-1$
			textStyles |= SWT.BORDER;
		}
		fEditorText = new Text(fComposite, textStyles);
		fEditorText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		String valueString= ""; //$NON-NLS-1$
		try {
			valueString= fVariable.getValue().getValueString();
		} catch (DebugException de) {
			DebugUIPlugin.errorDialog(activeShell,ActionMessages.getString("ChangeVariableValue.errorDialogTitle"),ActionMessages.getString("ChangeVariableValue.errorDialogMessage"), de.getStatus());	 //$NON-NLS-1$ //$NON-NLS-2$
			DebugUIPlugin.logError(de);
		}
		TreeItem[] selectedItems = fTree.getSelection();
		fTreeEditor.horizontalAlignment = SWT.LEFT;
		fTreeEditor.grabHorizontal = true;
		fTreeEditor.setEditor(fComposite, selectedItems[0]);

		// There is no API on the model presentation to get just the variable name, 
		// so we have to make do with just calling IVariable.getName()
		String varName = ""; //$NON-NLS-1$
		try {
			varName = fVariable.getName();
		} catch (DebugException de) {
			DebugUIPlugin.logError(de);
		}
		fEditorLabel.setText(varName + "="); //$NON-NLS-1$

		fEditorText.setText(valueString);
		fEditorText.selectAll();
		
		fComposite.layout(true);
		fComposite.setVisible(true);
		fEditorText.setFocus();
	
		// CR means commit the changes, ESC means abort changing the value
		fEditorText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				if (event.character == SWT.CR) {
					if (fKeyReleased) {
						saveChangesAndCleanup(fVariable, activeShell);
					} else {
						cleanup();
						return;
					}
				}
				if (event.character == SWT.ESC) {
					cleanup();
					return;
				}
				fKeyReleased= true;
			}
		});
	
		// If the focus is lost, then act as if user hit CR and commit changes
		fEditorText.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent fe) {
				if (fKeyReleased) {
					saveChangesAndCleanup(fVariable, activeShell);
				} else {
					cleanup();
				}
			}
		});				
	}
	
	/** 
	 * If the new value validates, save it, and dispose the text widget, 
	 * otherwise sound the system bell and leave the user in the editor.
	 */
	protected void saveChangesAndCleanup(IVariable variable, Shell shell) {
		String newValue= fEditorText.getText();
		try {
			if (!variable.verifyValue(newValue)) {
				shell.getDisplay().beep();
				return;
			}
			variable.setValue(newValue);
		} catch (DebugException de) {
			DebugUIPlugin.errorDialog(shell, ActionMessages.getString("ChangeVariableValue.errorDialogTitle"),ActionMessages.getString("ChangeVariableValue.errorDialogMessage"), de.getStatus());	//$NON-NLS-2$ //$NON-NLS-1$
			DebugUIPlugin.logError(de);
		}
		cleanup();		
	}

	/**
	 * Tidy up the widgets that were used
	 */
	private void cleanup() {
		fKeyReleased= false;
		if (fEditorText != null) {
			fEditorText.dispose();
			fEditorText = null;
			fVariable = null;
			fTreeEditor.setEditor(null, null);
			fComposite.setVisible(false);
		}
	}
		
	/**
	 * Updates the enabled state of this action based
	 * on the selection
	 */
	protected void update(IStructuredSelection sel) {
		Iterator iter= sel.iterator();
		if (iter.hasNext()) {
			Object object= iter.next();
			if (object instanceof IValueModification) {
				IValueModification varMod= (IValueModification)object;
				if (!varMod.supportsValueModification()) {
					setEnabled(false);
					return;
				}
				setEnabled(!iter.hasNext());
				return;
			}
		}
		setEnabled(false);
	}

	/**
	 * @see Action
	 */
	public void run() {
		Iterator iterator= getStructuredSelection().iterator();
		doActionPerformed((IVariable)iterator.next());
	}
	
	/**
	 * @see SelectionProviderAction
	 */
	public void selectionChanged(IStructuredSelection sel) {
		update(sel);
	}
}

