package org.eclipse.ui.forms.examples.views;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.part.ViewPart;
public class FormView extends ViewPart {
	private FormToolkit toolkit;
	private ScrolledForm form;
	/**
	 * The constructor.
	 */
	public FormView() {
	}
	/**
	 * This is a callback that will allow us to create the viewer and
	 * initialize it.
	 */
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		form.setText("Hello, Eclipse Forms");
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		Hyperlink link = toolkit.createHyperlink(form.getBody(), "Click here.",
				SWT.WRAP);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				System.out.println("Link activated!");
			}
		});
		link.setText("This is an example of a form that is much longer and will need to wrap.");
		layout.numColumns = 2;
		TableWrapData td = new TableWrapData();
		td.colspan = 2;
		link.setLayoutData(td);
		Label label = toolkit.createLabel(form.getBody(), "Text field label:");
		Text text = toolkit.createText(form.getBody(), "");
		td = new TableWrapData();
		td.align = TableWrapData.FILL_GRAB;
		text.setLayoutData(td);
		Button button = toolkit.createButton(form.getBody(),
				"An example of a checkbox in a form", SWT.CHECK);
		td = new TableWrapData();
		td.colspan = 2;
		button.setLayoutData(td);
	}
	/**
	 * Passing the focus request to the form.
	 */
	public void setFocus() {
		form.setFocus();
	}
	/**
	 * Disposes the toolkit
	 */
	public void dispose() {
		toolkit.dispose();
		super.dispose();
	}
}