package org.eclipse.ui.forms.examples.views;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
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
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		text.setLayoutData(td);
		Button button = toolkit.createButton(form.getBody(),
				"An example of a checkbox in a form", SWT.CHECK);
		td = new TableWrapData();
		td.colspan = 2;
		button.setLayoutData(td);
		ExpandableComposite ec = toolkit.createExpandableComposite(form.getBody(), ExpandableComposite.TREE_NODE|ExpandableComposite.CLIENT_INDENT);
		ec.setText("Expandable Composite title");
		String ctext = "We will now create a somewhat long text so that "+
		"we can use it as content for the expandable composite. "+
		"Expandable composite is used to hide or show the text using the "+
		"toggle control";
		Label client = toolkit.createLabel(ec, ctext, SWT.WRAP);
		ec.setClient(client);
		td = new TableWrapData();
		td.colspan = 2;
		ec.setLayoutData(td);
		ec.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		Section section = toolkit.createSection(form.getBody(), Section.DESCRIPTION|Section.TWISTIE|Section.EXPANDED);
		td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 2;
		section.setLayoutData(td);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		section.setText("Section title");
		toolkit.createCompositeSeparator(section);
		section.setDescription("This is the description that goes below the title");
		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(new GridLayout());
		button = toolkit.createButton(sectionClient, "Radio 1", SWT.RADIO);
		button = toolkit.createButton(sectionClient, "Radio 2", SWT.RADIO);
		section.setClient(sectionClient);

		StringBuffer buf = new StringBuffer();
		buf.append("<form>");
		buf.append("<p>");
		buf.append("Here is some plain text for the text to render; ");
		buf.append("this text is at <a href=\"http://www.eclipse.org\" nowrap=\"true\">http://www.eclipse.org</a> web site.");
		buf.append("</p>");
		buf.append("<p>This line will contain some <b>bold</b> text. ");
		buf.append("We can also add <img href=\"image\"/> an image. ");
		buf.append("</p>");
		buf.append("<li>A default (bulleted) list item.</li>");
		buf.append("<li style=\"text\" value=\"1.\">A list item with text.</li>");
		buf.append("<li style=\"text\" value=\"2.\">Another list item with text</li>");
		buf.append("<li style=\"image\" value=\"image\">List item with an image bullet</li>");
		buf.append("<li style=\"text\" bindent=\"20\" indent=\"40\" value=\"3.\">A list item with text.</li>");
		buf.append("<li style=\"text\" bindent=\"20\" indent=\"40\" value=\"4.\">A list item with text.</li>");
		buf.append("</form>");
		RichText rtext = toolkit.createRichText(form.getBody(), true);
		td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 2;
		rtext.setLayoutData(td);
		rtext.setImage("image", ExamplesPlugin.getDefault().getImageRegistry().get(ExamplesPlugin.IMG_SAMPLE));
		rtext.setText(buf.toString(), true, false);
		rtext.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				System.out.println("Link active: "+e.getHref());
			}
		});
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