package org.eclipse.ui.internal.actions;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * This is the contribution item that is used to add a help serch combo box to
 * the cool bar.
 * 
 * @since 3.1
 */

public class HelpSearchComboContributionItem extends ControlContribution {
	private static final String ID = "org.eclipse.ui.searchCombo"; //$NON-NLS-1$
	
	private IWorkbenchWindow window;

	private Combo combo;

	private int MAX_ITEM_COUNT = 10;
	
	/**
	 * Creates the contribution item.
	 * 
	 * @param window
	 */
	public HelpSearchComboContributionItem(IWorkbenchWindow window) {
		this(window, ID);
	}

	/**
	 * Creates the contribution item.
	 * 
	 * @param window
	 * @param id
	 */
	public HelpSearchComboContributionItem(IWorkbenchWindow window, String id) {
		super(id);
		this.window = window;
	}

	protected Control createControl(Composite parent) {
		combo = new Combo(parent, SWT.NULL);
		combo.setToolTipText(WorkbenchMessages.getString("WorkbenchWindow.searchCombo.toolTip")); //$NON-NLS-1$
		String[] items = WorkbenchPlugin.getDefault().getDialogSettings()
				.getArray(ID);
		if (items != null)
			combo.setItems(items);
		combo.setText(WorkbenchMessages.getString("WorkbenchWindow.searchCombo.text")); //$NON-NLS-1$
		combo.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.character == SWT.CR) {
					doSearch(combo.getText(), true);
				}
			}
		});
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = combo.getSelectionIndex();
				if (index!= -1)
					doSearch(combo.getItem(index), false);
			}
		});
		return combo;
	}

	protected int computeWidth(Control control) {
		return control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
	}

	private void doSearch(String phrase, boolean updateList) {
		if (phrase.length()==0) {
			window.getShell().getDisplay().beep();
			return;
		}
		if (updateList) {
			boolean exists = false;
			for (int i = 0; i < combo.getItemCount(); i++) {
				String item = combo.getItem(i);
				if (item.equalsIgnoreCase(phrase)) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				combo.add(phrase, 0);
				if (combo.getItemCount() > MAX_ITEM_COUNT)
					combo.remove(combo.getItemCount() - 1);
				WorkbenchPlugin.getDefault().getDialogSettings().put(ID,
						combo.getItems());
			}
		}
		PlatformUI.getWorkbench().getHelpSystem().search(phrase);
	}
}
