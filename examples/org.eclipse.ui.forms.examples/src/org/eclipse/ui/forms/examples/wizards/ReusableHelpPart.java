/*
 * Created on Oct 21, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ui.forms.examples.wizards;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.help.internal.search.SearchHit;
import org.eclipse.help.ui.internal.search.*;
import org.eclipse.jface.operation.*;
import org.eclipse.search.ui.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * @author dejan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ReusableHelpPart {
	private ScrolledForm form;
	private Text phraseText;
	private ContentSectionPart contentSectionPart;
	private FormText searchResults;
	private String defaultText="";
	private IRunnableContext runnableContext;
	private SorterByScore resultSorter;
	private FormToolkit toolkit;
	private IMemento memento;

	public ReusableHelpPart(IRunnableContext runnableContext) {
		this.runnableContext = runnableContext;
		resultSorter = new SorterByScore();
	}
	
	public void init(IMemento memento) {
		this.memento = memento;
	}
	public void saveState(IMemento memento) {
		contentSectionPart.saveState(memento);
	}
	
	public void reflow() {
		form.reflow(true);
	}
	
	public FormToolkit getToolkit() {
		return toolkit;
	}

	public void createControl(Composite parent, FormToolkit toolkit) {
		// parent form
		this.toolkit = toolkit;
		form = toolkit.createScrolledForm(parent);
		TableWrapLayout layout = new TableWrapLayout();
		layout.leftMargin = layout.rightMargin = 0;
		form.getBody().setLayout(layout);
		form.setText("");
		contentSectionPart = new ContentSectionPart();
		contentSectionPart.init(this, null);
		contentSectionPart.createPartControl(form.getBody(), toolkit);
		Section section = contentSectionPart.getSection();
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB,
				TableWrapData.FILL));
		toolkit.createLabel(form.getBody(), null);
		section = toolkit.createSection(form.getBody(), Section.TITLE_BAR
				| Section.TWISTIE | Section.EXPANDED);
		section.setText("Search");
		Composite helpContainer = toolkit.createComposite(section);
		section.setClient(helpContainer);
		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		GridLayout glayout = new GridLayout();
		glayout.numColumns = 3;
		glayout.marginWidth = glayout.marginHeight = 1;
		helpContainer.setLayout(glayout);
		helpContainer.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		toolkit.paintBordersFor(helpContainer);
		Label label = toolkit.createLabel(helpContainer, "Search");
		label.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		phraseText = toolkit.createText(helpContainer, ""); //$NON-NLS-1$
		phraseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final Button button = toolkit.createButton(helpContainer,
				"Go", SWT.PUSH); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				doSearch(phraseText.getText());
			}
		});
		button.setEnabled(false);
		phraseText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String text = phraseText.getText();
				button.setEnabled(text.length() > 0);
			}
		});
		phraseText.addKeyListener(new KeyAdapter() {

			public void keyReleased(KeyEvent e) {
				if (e.character == '\r') {
					if (button.isEnabled())
						doSearch(phraseText.getText());
				}
			}
		});
		toolkit.paintBordersFor(form.getBody());

		searchResults = toolkit.createFormText(form.getBody(), true);
		// searchResults.setBackground(helpContainer.getDisplay().getSystemColor(SWT.COLOR_CYAN));
		searchResults.setColor(FormColors.TITLE, toolkit.getColors().getColor(
				FormColors.TITLE));
		searchResults.setImage(ExamplesPlugin.IMG_HELP_TOPIC, ExamplesPlugin
				.getDefault().getImage(ExamplesPlugin.IMG_HELP_TOPIC));
		searchResults.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				Object href = e.getHref();
				if (href.toString().equals("_more"))
					doExternalSearch(phraseText.getText());
				else
					openLink(e.getHref());
			}
		});
		searchResults.setText("", false, false);
	}
	
    public boolean isMonitoringContextHelp() {
    	return contentSectionPart!=null && contentSectionPart.isMonitoringContextHelp();
    }

	public Control getControl() {
		return form;
	}

	public ScrolledForm getForm() {
		return form;
	}

	private void doExternalSearch(String phrase) {
		try {
			String ephrase = URLEncoder.encode(phrase, "UTF-8"); //$NON-NLS-1$
			String query = "tab=search&searchWord=" + ephrase; //$NON-NLS-1$
			WorkbenchHelp.displayHelpResource(query);
		} catch (UnsupportedEncodingException e) {
			System.out.println(e);
		}
	}

	private void doSearch(String phrase) {
		SearchQueryData data = new SearchQueryData();
		data.setMaxHits(5);
		data.setSearchWord(phrase);
		final HelpSearchQuery query = new HelpSearchQuery(data);
		final ISearchResult result = query.getSearchResult();
		final StringBuffer resultBuffer = new StringBuffer();
		result.addListener(new ISearchResultListener() {
			public void searchResultChanged(SearchResultEvent e) {
				final HelpSearchResult hresult = (HelpSearchResult) result;
				getControl().getDisplay().asyncExec(new Runnable() {
					public void run() {
						updateResults(resultBuffer, hresult);
					}
				});
			}
		});
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				query.run(monitor);
			}
		};
		try {
			runnableContext.run(true, true, op);
		} catch (InvocationTargetException e) {
		} catch (InterruptedException e) {
		}
	}

	private void updateResults(StringBuffer buff, HelpSearchResult hresult) {
		buff.delete(0, buff.length());
		if (hresult.getMatchCount() > 0) {
			buff.append("<form>");
			buff.append("<p><span color=\"");
			buff.append(FormColors.TITLE);
			buff.append("\">Search results:</span></p>");
			Object[] elements = hresult.getElements();
			resultSorter.sort(null, elements);

			for (int i = 0; i < elements.length; i++) {
				SearchHit hit = (SearchHit) elements[i];
				buff.append("<li indent=\"21\" style=\"image\" value=\"");
				buff.append(ExamplesPlugin.IMG_HELP_TOPIC);
				buff.append("\">");
				buff.append("<a href=\"");
				buff.append(hit.getHref());
				buff.append("\">");
				buff.append(hit.getLabel());
				buff.append("</a>");
				buff.append("</li>");
			}
			if (elements.length > 0) {
				buff.append("<p><a href=\"_more\">More results...</a></p>");
			}
			buff.append("</form>");
			searchResults.setText(buff.toString(), true, false);
		} else
			searchResults.setText("", false, false);
		form.reflow(true);
	}
	
	public void openLink(Object href) {
		String url = (String) href;
		if (url != null)
			WorkbenchHelp.displayHelpResource(url);
	}

	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.intro.impl.parts.IStandbyContentPart#setFocus()
	 */
	public void setFocus() {
		phraseText.setFocus();
	}

	public void update(Control control) {
		if (form != null)
			contentSectionPart.handleActivation(control);
	}
	public void setDefaultText(String text) {
		this.defaultText = text;
	}
}