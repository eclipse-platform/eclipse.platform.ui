/*
 * Created on Dec 19, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.wizards;

import org.eclipse.help.ui.internal.views.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author dejan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AltAssistedWizardDialog extends WizardDialog {
	private Button helpButton;
	private ContextHelpWindow contextHelpWindow;

	private class ContextHelpWindow extends ApplicationWindow {
		private ReusableHelpPart helpPart;

		private FormToolkit toolkit;

		public ContextHelpWindow(Shell parent) {
			super(parent);
			addToolBar(SWT.FLAT);
			addStatusLine();
			helpPart = new ReusableHelpPart(this);
			helpPart.init(getToolBarManager(), getStatusLineManager());
			parent.addControlListener(new ControlListener() {
				public void controlMoved(ControlEvent e) {
					syncHelpBounds();
				}
				public void controlResized(ControlEvent e) {
					syncHelpBounds();
				}
			});
		}
		
		public void setPartFocus() {
			if (helpPart!=null)
				helpPart.setFocus();
		}

		private void syncHelpBounds() {
			ContextHelpWindow helpWindow = getHelpWindow();
			if (helpWindow==null)
				return;
			Rectangle pbounds = AltAssistedWizardDialog.this.getShell().getBounds();
			helpWindow.getShell().setLocation(pbounds.x+pbounds.width, pbounds.y);
			helpWindow.getShell().setSize(helpWindow.getShell().getSize().x, pbounds.height);
		}

		protected Control createContents(Composite parent) {
			toolkit = new FormToolkit(parent.getDisplay());
			Composite container = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = layout.marginHeight = 0;
			layout.verticalSpacing = 0;
			container.setLayout(layout);

			GridData gd;
			Label separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
			gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gd.heightHint = 1;
			separator.setLayoutData(gd);			
			helpPart.createControl(container, toolkit);
			helpPart.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
			separator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
			gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
			gd.heightHint = 1;
			separator.setLayoutData(gd);				
			
			helpPart.showPage(IHelpViewConstants.CONTEXT_HELP_PAGE);
			updateForPage(getCurrentPage());
			return helpPart.getControl();
		}
		
		public void updateForPage(IWizardPage page) {
			if (page!=null)
				helpPart.update(page.getControl());
		}

		public boolean close() {
			if (super.close()) {
				if (toolkit!=null) {
					toolkit.dispose();
					toolkit = null;
				}
				if (helpPart != null) {
					helpPart.dispose();
					helpPart = null;
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * @param parentShell
	 * @param newWizard
	 */
	public AltAssistedWizardDialog(Shell parentShell, IWizard newWizard) {
		super(parentShell, newWizard);
	}

	protected void createButtonsForButtonBar(Composite parent) {
		helpButton = createButton(parent, IDialogConstants.HELP_ID,
				IDialogConstants.HELP_LABEL, false);
		super.createButtonsForButtonBar(parent);
	}

	protected void helpPressed() {
		ContextHelpWindow helpWindow = getHelpWindow();
		if (helpWindow!=null)
			helpWindow.getShell().setActive();
		else {
			Rectangle pbounds = getShell().getBounds();
			contextHelpWindow = new ContextHelpWindow(getShell());
			contextHelpWindow.create();
			Shell helpShell = contextHelpWindow.getShell();
			helpShell.setText("Help");
			helpShell.setLocation(pbounds.x+pbounds.width, pbounds.y);
			helpShell.setSize(200, pbounds.height);
			helpShell.open();
			helpShell.addControlListener(new ControlListener() {
				public void controlMoved(ControlEvent e) {
					syncParentShell();
				}
				public void controlResized(ControlEvent e) {
					syncParentShell();
				}
			});
			helpWindow = contextHelpWindow;
		}
		helpWindow.setPartFocus();		
	}
	private void syncParentShell() {
		ContextHelpWindow helpWindow = getHelpWindow();
		if (helpWindow==null) return;
		Shell parentShell = getShell();		
		Rectangle hbounds = helpWindow.getShell().getBounds();
		Rectangle pbounds = parentShell.getBounds();
		parentShell.setLocation(pbounds.x, hbounds.y);
		parentShell.setSize(hbounds.x-pbounds.x, hbounds.height);
	}
	
	private ContextHelpWindow getHelpWindow() {
		if (contextHelpWindow!=null) {
			if (contextHelpWindow.getShell()==null || contextHelpWindow.getShell().isDisposed())
				contextHelpWindow = null;
		}
		return contextHelpWindow;
	}
    protected void update() {
    	super.update();
    	ContextHelpWindow helpWindow =getHelpWindow();
    	if (helpWindow!=null)
    		helpWindow.updateForPage(getCurrentPage());
     }	
}