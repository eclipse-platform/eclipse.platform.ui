package org.eclipse.update.internal.ui.search;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.internal.ui.model.ISiteAdapter;
import org.eclipse.update.ui.forms.internal.FormWidgetFactory;

public class ExpressionSearchCategory extends SearchCategory {
	private Text expressionText;
	private Button caseCheck;
	private Button nameCheck;
	private Button providerCheck;
	private Button descriptionCheck;
	private boolean caseSensitive = false;
	private boolean searchName = true;
	private boolean searchProvider = false;
	private boolean searchDesc = false;
	private String expression="";
	
	public ExpressionSearchCategory() {
	}
	
	public void createControl(Composite parent, FormWidgetFactory factory) {		
		Composite container = factory.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 2;
		layout.numColumns = 2;
		container.setLayout(layout);
		factory.createLabel(container, "Expression:");
		expressionText = factory.createText(container, "");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		expressionText.setLayoutData(gd);
		caseCheck = factory.createButton(container, "Case sensitive", SWT.CHECK);
		fillHorizontal(caseCheck, 0);
		Label label = factory.createLabel(container, "Look for expression in:");
		fillHorizontal(label, 0);
		nameCheck = factory.createButton(container, "Feature name", SWT.CHECK);
		fillHorizontal(nameCheck, 10);
		providerCheck = factory.createButton(container, "Feature provider", SWT.CHECK);
		fillHorizontal(providerCheck, 10);
		descriptionCheck = factory.createButton(container, "Feature description", SWT.CHECK);
		fillHorizontal(descriptionCheck, 10);
		factory.paintBordersFor(container);
		initializeWidgets();
		setControl(container);
	}
	private void fillHorizontal(Control control, int indent) {
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.horizontalIndent = indent;
		control.setLayoutData(gd);
	}

	/**
	 * @see ISearchCategory#matches(IFeature)
	 */
	public ISearchQuery [] getQueries() {
		storeSettingsFromWidgets();
		ISearchQuery query = new ISearchQuery() {
			public ISiteAdapter getSearchSite() {
				return null;
			}
			public boolean matches(IFeature feature) {
				return internalMatches(feature);
			}
		};
		return new ISearchQuery [] { query };
	}
	
	private void storeSettingsFromWidgets() {
		caseSensitive = caseCheck.getSelection();
		searchName = nameCheck.getSelection();
		searchProvider = providerCheck.getSelection();
		searchDesc = descriptionCheck.getSelection();
		expression = expressionText.getText().trim();
	}
	private void initializeWidgets() {
		caseCheck.setSelection(caseSensitive);
		nameCheck.setSelection(searchName);
		providerCheck.setSelection(searchProvider);
		descriptionCheck.setSelection(searchDesc);
		expressionText.setText(expression);
	}
	
	public String getCurrentSearch() {
		return "Expression: "+expressionText.getText();
	}
	private boolean internalMatches(IFeature feature) {
		if (searchName) {
			if (matches(feature.getLabel())) return true;
		}
		if (searchProvider) {
			if (matches(feature.getProvider())) return true;
		}
		if (searchDesc) {
			String annotation = null;
			if (feature.getDescription()!=null)
				annotation = feature.getDescription().getAnnotation();
			if (annotation!=null) {
				if (matches(annotation)) return true;
			}
		}
		return false;
	}
	private boolean matches(String text) {
		return text.indexOf(expression)!= -1;
	}
	public void load(Map map) {
		caseSensitive = getBoolean("case", map);
		searchName = getBoolean("name", map);
		searchProvider = getBoolean("provider", map);
		searchDesc = getBoolean("desc", map);
		expression = getString("expression", map);
		if (caseCheck!=null)
			initializeWidgets();
	}

	public void store(Map map) {
		storeSettingsFromWidgets();
		map.put("case", caseSensitive?"true":"false");
		map.put("name", searchName?"true":"false");
		map.put("provider", searchProvider?"true":"false");
		map.put("desc", searchDesc?"true":"false");
		map.put("expression", expression);
	}
}