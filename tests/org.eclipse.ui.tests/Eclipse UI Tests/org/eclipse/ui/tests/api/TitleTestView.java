/*
 * Created on May 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.tests.api;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.layout.CellLayout;
import org.eclipse.ui.internal.layout.Row;
import org.eclipse.ui.part.ViewPart;

/**
 */
public class TitleTestView extends ViewPart {

	Composite composite;
	Text title;
	Text name;
	Text contentDescription;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		CellLayout layout = new CellLayout(2)
			.setColumn(0, Row.fixed())
			.setColumn(1, Row.growing());
		composite.setLayout(layout);
		
		Label firstLabel = new Label(composite, SWT.NONE);
		firstLabel.setText("Title");
		title = new Text(composite, SWT.BORDER);
		title.setText(getTitle());
		
		title.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setTitle(title.getText());
			}
		});
		
		Label secondLabel = new Label(composite, SWT.NONE);
		secondLabel.setText("Name");
		name = new Text(composite, SWT.BORDER);
		name.setText(getPartName());
		name.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPartName(name.getText());
			}
		});
		
		Label thirdLabel = new Label(composite, SWT.NONE);
		thirdLabel.setText("Content");
		contentDescription = new Text(composite, SWT.BORDER);
		contentDescription.setText(getContentDescription());
		contentDescription.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setContentDescription(contentDescription.getText());
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {

	}

}
