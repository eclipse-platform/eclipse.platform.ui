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
package org.eclipse.ui.forms.examples.internal.rcp;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
/**
 * @author dejan
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class NewStylePage extends FormPage {
	/**
	 * @param id
	 * @param title
	 */
	public NewStylePage(FormEditor editor) {
		super(editor, "newStyle", "New Style");
	}
	protected void createFormContent(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_HOVER);
		form.setText("New Style Form");
		FormColors colors = toolkit.getColors();
		colors.initializeSectionToolBarColors();
		Color gbg = colors.getColor(FormColors.TB_GBG);
		Color bg = colors.getBackground();
		form.getForm().setTextBackground(new Color[]{bg, gbg}, new int [] {100}, true);
		form.getForm().setSeparatorColor(colors.getColor(FormColors.TB_BORDER));
		form.getForm().setSeparatorVisible(true);
		ToolBarManager tbm = (ToolBarManager)form.getForm().getToolBarManager();
		tbm.getControl().setBackground(colors.getColor(FormColors.TB_GBG));
		
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		form.getBody().setLayout(layout);

		Button error = toolkit.createButton(form.getBody(), "Error", SWT.PUSH);
		error.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				form.getForm().setMessage("Test for an error message", IMessageProvider.ERROR);				
				
			}
		});
		Button warning = toolkit.createButton(form.getBody(), "Warning", SWT.PUSH);
		warning.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				form.getForm().setMessage("Test for a warning message", IMessageProvider.WARNING);
			}
		});		
		Button info = toolkit.createButton(form.getBody(), "Info", SWT.PUSH);
		info.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				form.getForm().setMessage("Test for an info message", IMessageProvider.INFORMATION);
			}
		});		
		Button cancel = toolkit.createButton(form.getBody(), "Cancel", SWT.PUSH);
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				form.getForm().setMessage(null);
			}
		});		
		
		final Button busy = toolkit.createButton(form.getBody(), "Start", SWT.PUSH);
		busy.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (form.getForm().isBusy()) {
					form.getForm().setBusy(false);
					busy.setText("Start");
				}
				else {
					form.getForm().setBusy(true);
					busy.setText("Stop");
				}
			}
		});		
		Action haction = new Action("hor", Action.AS_RADIO_BUTTON) {
			public void run() {
			}
		};
		haction.setChecked(true);
		haction.setToolTipText("Horizontal orientation");
		haction.setImageDescriptor(ExamplesPlugin.getDefault()
				.getImageRegistry()
				.getDescriptor(ExamplesPlugin.IMG_HORIZONTAL));
		Action vaction = new Action("ver", Action.AS_RADIO_BUTTON) {
			public void run() {
			}
		};
		vaction.setChecked(false);
		vaction.setToolTipText("Vertical orientation");
		vaction.setImageDescriptor(ExamplesPlugin.getDefault()
				.getImageRegistry().getDescriptor(ExamplesPlugin.IMG_VERTICAL));
		form.getToolBarManager().add(haction);
		form.getToolBarManager().add(vaction);
		form.getToolBarManager().update(true);
		form.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_DEF_VIEW));
	}
}