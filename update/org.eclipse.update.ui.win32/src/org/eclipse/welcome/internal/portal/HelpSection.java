/*
 * Created on Jun 20, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.eclipse.welcome.internal.portal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.ui.forms.internal.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HelpSection implements IPortalSectionForm {
	private Text phraseText;

	/* (non-Javadoc)
	 * @see org.eclipse.welcome.internal.portal.IPortalSection#init(org.eclipse.update.ui.forms.internal.IFormPage)
	 */
	public void init(IFormPage page) {
	}
	
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.welcome.internal.portal.IPortalSection#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent, FormWidgetFactory factory) {
		Composite container = factory.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginWidth = 0;
		container.setLayout(layout);
		factory.createLabel(container, "Phrase:");
		phraseText = factory.createText(container, "");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		phraseText.setLayoutData(gd);
		final Button button = factory.createButton(container, "Go", SWT.PUSH);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doSearch(phraseText.getText());
			}
		});
		button.setEnabled(false);
		phraseText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text=phraseText.getText();
				button.setEnabled(text.length()>0);
			}
		});
		phraseText.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.character=='\r') {
					if (button.isEnabled())
						doSearch(phraseText.getText());
				}
			}
		});
		factory.paintBordersFor(container);
		return container;
	}
	private void doSearch(String phrase) {
		String query = "tab=search&searchWord=\""+phrase+"\"";
		WorkbenchHelp.getHelpSupport().displayHelpResource(query);
	} 
}
