/*
 * Created on Dec 11, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.wizards;

import org.eclipse.help.*;
import org.eclipse.help.internal.context.IStyledContext;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.*;


/**
 * @author dejan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ContextHelpPage implements IHelpContentPage {
	public static final String ID = "context-help";
	private static final String HELP_KEY = "org.eclipse.ui.help"; //$NON-NLS-1$	
	private ContentSectionPart sectionPart;
	private FormText text;
	private String defaultText;
	
	public ContextHelpPage() {
		defaultText="Click anywhere in the workbench to see a description of the selected part.";				
	}

	public void init(ContentSectionPart sectionPart, IMemento memento) {
        this.sectionPart = sectionPart;
	}
	
	public void saveState(IMemento memento) {
	}

	public void addToActionBars(IActionBars bars) {
	}
	public String getId() {
		return ID;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.views.IHelpViewPage#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	public void createControl(Composite parent, FormToolkit toolkit) {
		text = toolkit.createFormText(parent, true);
		text.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));
		text.setImage(ExamplesPlugin.IMG_HELP_TOPIC, ExamplesPlugin
				.getDefault().getImage(ExamplesPlugin.IMG_HELP_TOPIC));
		text.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				sectionPart.openLink(e.getHref());
			}
		});
		text.setText(defaultText, false, false);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.views.IHelpViewPage#dispose()
	 */
	public void dispose() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.views.IHelpViewPage#getControl()
	 */
	public Control getControl() {
		return text;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.examples.views.IHelpViewPage#setFocus()
	 */
	public void setFocus() {
		if (text!=null)
			text.setFocus();
	}
	
	public void handleActivation(Control page) {
		if (text.isDisposed())
			return;
		// title.setText("What is" + " \"" + part.getSite().getRegisteredName()
		// + "\"?"); //$NON-NLS-1$ //$NON-NLS-2$
		String helpText = createContextHelp(page);
		text.setText(helpText != null ? helpText : defaultText, helpText != null, //$NON-NLS-1$
						false);
		// form.getBody().layout();
		sectionPart.reflow();
	}

	private String createContextHelp(Control page) {
		String text = null;
		if (page != null) {
			if (page != null /* && page.isVisible() */&& !page.isDisposed()) {
				IContext helpContext = findHelpContext(page);
				if (helpContext != null) {
					text = formatHelpContext(helpContext);
				}
			}
		}
		return text;
	}
	
	private IContext findHelpContext(Control c) {
		String contextId = null;
		Control node = c;
		do {
			contextId = (String) node.getData(HELP_KEY);
			if (contextId != null)
				break;
			node = node.getParent();
		} while (node != null);
		if (contextId != null) {
			return HelpSystem.getContext(contextId);
		}
		return null;
	} 	

	private String formatHelpContext(IContext context) {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<form>"); //$NON-NLS-1$
		sbuf.append("<p>"); //$NON-NLS-1$
		sbuf.append(decodeContextBoldTags(context));
		sbuf.append("</p>"); //$NON-NLS-1$
		IHelpResource[] links = context.getRelatedTopics();
		if (links != null && links.length > 0) {
			sbuf.append("<p><span color=\"");
			sbuf.append(FormColors.TITLE);
			sbuf.append("\">See also:</span></p>");
			for (int i = 0; i < links.length; i++) {
				IHelpResource link = links[i];
				sbuf.append("<li style=\"text\" indent=\"2\">"); //$NON-NLS-1$
				sbuf.append("<img href=\""); //$NON-NLS-1$
				sbuf.append(ExamplesPlugin.IMG_HELP_TOPIC);
				sbuf.append("\"/> "); //$NON-NLS-1$
				sbuf.append("<a href=\""); //$NON-NLS-1$
				sbuf.append(link.getHref());
				sbuf.append("\">"); //$NON-NLS-1$
				sbuf.append(link.getLabel());
				sbuf.append("</a>"); //$NON-NLS-1$
				sbuf.append("</li>"); //$NON-NLS-1$
			}
		}
		sbuf.append("</form>"); //$NON-NLS-1$
		return sbuf.toString();
	}

	/**
	 * Make sure to support the Help system bold tag. Help systen returns a
	 * regular string for getText(). Use internal apis for now to get bold.
	 * 
	 * @param context
	 * @return
	 */
	private String decodeContextBoldTags(IContext context) {
		String styledText;
		if (context instanceof IStyledContext) {
			styledText = ((IStyledContext) context).getStyledText();
		} else {
			styledText = context.getText();
		}
		String decodedString = styledText.replaceAll("<@#\\$b>", "<b>"); //$NON-NLS-1$ //$NON-NLS-2$
		decodedString = decodedString.replaceAll("</@#\\$b>", "</b>"); //$NON-NLS-1$ //$NON-NLS-2$
		return decodedString;
	}	
}
