/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Markus Schorn - initial API and implementation 
 *******************************************************************************/

package org.eclipse.search2.internal.ui.text2;

import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.search.core.text.AbstractTextFileScanner;

import org.eclipse.search2.internal.ui.SearchMessages;

/**
 * @author markus.schorn@windriver.com
 */
public class RetrieverFilterTab implements IRetrieverKeys {
	private RetrieverPage fView;

	private boolean fEnable;
	private boolean fEnableLocation;
	private boolean fEnableString;

	private Button fUseLocationFilter;
	private Button fCommentFilter;
	private Button fStringFilter;
	private Button fIncludeFilter;
	private Button fPrepFilter;
	private Button fFunctionFilter;
	private Button fOtherFilter;

	private Button fUseStringFilter;
	private Combo fFilterString;
	private Button fHideMatching;
	private Button fRegularExpressionFilter;

	private Control[][] fEnableDisable;


	RetrieverFilterTab(RetrieverPage view) {
		fView= view;
	}

	public void createControl(Composite parent) {
		int indent= 15;
		GridData gd;
		GridLayout gl;

		Composite outer= new Composite(parent, SWT.NONE);
		outer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		outer.setLayout(gl= new GridLayout(1, false));
		gl.marginHeight= gl.marginWidth= 0;
		gl.verticalSpacing= 2;

		fUseLocationFilter= new Button(outer, SWT.CHECK);
		fUseLocationFilter.setText(SearchMessages.RetrieverFilterTab_LocationFilter_text);
		fUseLocationFilter.setLayoutData(new GridData());

		// location group
		Composite locationGroup= new Composite(outer, SWT.NONE);
		locationGroup.setLayoutData(gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		gd.horizontalIndent= indent;

		locationGroup.setLayout(gl= new GridLayout(3, false));
		gl.marginWidth= 0;
		gl.marginHeight= 0;
		gl.verticalSpacing= 0;

		// line 3, 4
		fCommentFilter= new Button(locationGroup, SWT.CHECK);
		fCommentFilter.setText(SearchMessages.RetrieverFilterTab_Comment_text);

		fIncludeFilter= new Button(locationGroup, SWT.CHECK);
		fIncludeFilter.setText(SearchMessages.RetrieverFilterTab_Import_text);

		if (TextFileScannerRegistry.getInstance().hasPreprocessorSupport()) {
			fPrepFilter= new Button(locationGroup, SWT.CHECK);
			fPrepFilter.setText(SearchMessages.RetrieverFilterTab_Preprocessor_text);
		} else {
			new Label(locationGroup, SWT.NONE);
		}

		fStringFilter= new Button(locationGroup, SWT.CHECK);
		fStringFilter.setText(SearchMessages.RetrieverFilterTab_String_text);

		if (TextFileScannerRegistry.getInstance().hasFunctionSupport()) {
			fFunctionFilter= new Button(locationGroup, SWT.CHECK);
			fFunctionFilter.setText(SearchMessages.RetrieverFilterTab_FunctionBody_text);
		}

		fOtherFilter= new Button(locationGroup, SWT.CHECK);
		fOtherFilter.setText(SearchMessages.RetrieverFilterTab_OtherLocation_text);

		// a bit of space before next line
		Label separator2= new Label(outer, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		new Label(outer, SWT.NONE).setLayoutData(gd= new GridData());
		gd.heightHint= 5;

		// text filter
		Composite indepAlign= new Composite(outer, SWT.NONE);
		indepAlign.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		indepAlign.setLayout(gl= new GridLayout(2, false));
		gl.marginHeight= gl.marginWidth= 0;

		fUseStringFilter= new Button(indepAlign, SWT.CHECK);
		fUseStringFilter.setLayoutData(new GridData());
		fUseStringFilter.setText(SearchMessages.RetrieverFilterTab_TextFilter_text);

		fFilterString= new Combo(indepAlign, SWT.NONE);
		fFilterString.setVisibleItemCount(VISIBLE_ITEMS_IN_COMBO);
		fFilterString.setLayoutData(gd= new GridData(GridData.FILL_HORIZONTAL));
		gd.widthHint= 20;

		indepAlign= new Composite(outer, SWT.NONE);
		indepAlign.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		indepAlign.setLayout(gl= new GridLayout(2, false));
		gl.marginHeight= gl.marginWidth= 0;

		fRegularExpressionFilter= new Button(indepAlign, SWT.CHECK);
		fRegularExpressionFilter.setText(SearchMessages.RetrieverFindTab_regularExpression);

		fHideMatching= new Button(indepAlign, SWT.CHECK);
		fHideMatching.setText(SearchMessages.RetrieverFilterTab_HideMatching_text);

		fEnableDisable= new Control[][] { {fCommentFilter, fStringFilter, fIncludeFilter, fPrepFilter, fFunctionFilter, fOtherFilter}, {fFilterString, fHideMatching, fRegularExpressionFilter}};
	}

	void createListeners() {
		SelectionListener s= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onEnableFilter();
			}
		};
		fUseLocationFilter.addSelectionListener(s);
		fUseStringFilter.addSelectionListener(s);
		fFilterString.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				if ((e.character == SWT.LF || e.character == SWT.CR)) {
					onFilter();
					selectStringFilter();
				}
			}
		});
		fFilterString.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				onFilter();
			}
		});

		SelectionListener onFilter= new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				onFilter();
			}
		};
		fHideMatching.addSelectionListener(onFilter);
		fRegularExpressionFilter.addSelectionListener(onFilter);
		fStringFilter.addSelectionListener(onFilter);
		fCommentFilter.addSelectionListener(onFilter);
		fIncludeFilter.addSelectionListener(onFilter);
		if (fPrepFilter != null) {
			fPrepFilter.addSelectionListener(onFilter);
		}
		if (fFunctionFilter != null) {
			fFunctionFilter.addSelectionListener(onFilter);
		}
		fOtherFilter.addSelectionListener(onFilter);
	}

	protected void onEnableFilter() {
		boolean el= fUseLocationFilter.getSelection();
		boolean es= fUseStringFilter.getSelection();
		fEnable= el || es;
		if (fEnable) {
			fEnableLocation= el;
			fEnableString= es;
		}
		// reroute through the enable filter on the view
		fView.onEnableToolbarFilter(fEnable);
	}

	protected void onFilter() {
		storeValues();
		fView.onFilterTabChanged();
	}

	public RetrieverFilter getFilter() {
		if (fEnable) {
			return new RetrieverFilter(fView, getAcceptedLocations(), getFilterText(), fRegularExpressionFilter.getSelection(), fHideMatching.getSelection(), hashCode());
		}
		return new RetrieverFilter();
	}

	private int getAcceptedLocations() {
		int acceptLocations= ALL_LOCATIONS;
		if (fUseLocationFilter.getSelection()) {
			acceptLocations= 0;
			if (fStringFilter.getSelection()) {
				acceptLocations|= (1 << AbstractTextFileScanner.LOCATION_STRING_LITERAL);
			}
			if (fCommentFilter.getSelection()) {
				acceptLocations|= (1 << AbstractTextFileScanner.LOCATION_COMMENT);
			}
			if (fIncludeFilter.getSelection()) {
				acceptLocations|= (1 << AbstractTextFileScanner.LOCATION_IMPORT_OR_INCLUDE_STATEMENT);
			}
			if (fPrepFilter == null || fPrepFilter.getSelection()) {
				acceptLocations|= (1 << AbstractTextFileScanner.LOCATION_PREPROCESSOR_DIRECTIVE);
			}
			if (fFunctionFilter == null || fFunctionFilter.getSelection()) {
				acceptLocations|= (1 << AbstractTextFileScanner.LOCATION_FUNCTION);
			}
			if (fOtherFilter.getSelection()) {
				acceptLocations|= (1 << AbstractTextFileScanner.LOCATION_OTHER);
			}
		}
		return acceptLocations;
	}

	private String getFilterText() {
		return fUseStringFilter.getSelection() ? fFilterString.getText() : null;
	}


	void storeValues() {
		fView.storeValue(fEnableString, KEY_ENABLE_TEXT_FILTER);
		fView.storeValue(fEnableLocation, KEY_ENABLE_LOCATION_FILTER);
		fView.storeComboContent(fFilterString, KEY_FILTER_PATTERNS, MAX_FILTER_PATTERNS);
		fView.storeButton(fHideMatching, KEY_INVERT_FILTER);
		fView.storeButton(fRegularExpressionFilter, KEY_REGULAR_EXPRESSION_FILTER);
		fView.storeButton(fCommentFilter, KEY_COMMENT_FILTER);
		fView.storeButton(fStringFilter, KEY_STRING_FILTER);
		fView.storeButton(fIncludeFilter, KEY_INCLUDE_FILTER);
		fView.storeButton(fPrepFilter, KEY_PREP_FILTER);
		fView.storeButton(fFunctionFilter, KEY_FUNCTION_FILTER);
		fView.storeButton(fOtherFilter, KEY_ELSEWHERE_FILTER);
	}

	/**
	 * called when the view is created
	 */
	void restoreValues(boolean filterEnabled) {
		fEnable= filterEnabled;
		fEnableLocation= fView.restoreValue(KEY_ENABLE_LOCATION_FILTER, true);
		fEnableString= fView.restoreValue(KEY_ENABLE_TEXT_FILTER, false);
		fUseLocationFilter.setSelection(fEnable && fEnableLocation);
		fUseStringFilter.setSelection(fEnable && fEnableString);
		fView.restoreCombo(fFilterString, KEY_FILTER_PATTERNS, ""); //$NON-NLS-1$
		fView.restoreButton(fHideMatching, KEY_INVERT_FILTER, false);
		fView.restoreButton(fRegularExpressionFilter, KEY_REGULAR_EXPRESSION_FILTER, false);
		fView.restoreButton(fCommentFilter, KEY_COMMENT_FILTER, true);
		fView.restoreButton(fStringFilter, KEY_STRING_FILTER, true);
		fView.restoreButton(fIncludeFilter, KEY_INCLUDE_FILTER, true);
		fView.restoreButton(fPrepFilter, KEY_PREP_FILTER, true);
		fView.restoreButton(fFunctionFilter, KEY_FUNCTION_FILTER, true);
		fView.restoreButton(fOtherFilter, KEY_ELSEWHERE_FILTER, true);
		updateEnablement();
	}

	private void updateEnablement() {
		enableAll(fEnableDisable[0], fEnable && fEnableLocation);
		enableAll(fEnableDisable[1], fEnable && fEnableString);
	}

	private void enableAll(Control[] controls, boolean enable) {
		for (int i= 0; i < controls.length; i++) {
			Control control= controls[i];
			if (control != null) {
				control.setEnabled(enable);
			}
		}
	}

	public void onEnableToolbarFilter(boolean enable) {
		fEnable= enable;
		fUseLocationFilter.setSelection(fEnable && fEnableLocation);
		fUseStringFilter.setSelection(fEnable && fEnableString);
		updateEnablement();
		selectStringFilter();
	}

	public void onSelected() {
		selectStringFilter();
	}

	private void selectStringFilter() {
		if (fFilterString.isEnabled()) {
			fFilterString.setFocus();
			fFilterString.setSelection(new Point(0, fFilterString.getText().length()));
		}
	}

	public void getProperties(Properties props) {
		if (fEnable) {
			props.setProperty(KEY_ENABLE_FILTER, String.valueOf(true));
			getProperty(props, KEY_COMMENT_FILTER, fCommentFilter);
			getProperty(props, KEY_ELSEWHERE_FILTER, fOtherFilter);
			getProperty(props, KEY_FILTER_PATTERNS, fFilterString);
			getProperty(props, KEY_FUNCTION_FILTER, fFunctionFilter);
			getProperty(props, KEY_INCLUDE_FILTER, fIncludeFilter);
			getProperty(props, KEY_INVERT_FILTER, fHideMatching);
			getProperty(props, KEY_PREP_FILTER, fPrepFilter);
			getProperty(props, KEY_REGULAR_EXPRESSION_FILTER, fRegularExpressionFilter);
			getProperty(props, KEY_STRING_FILTER, fStringFilter);
		}
	}

	private void getProperty(Properties props, String key, Button button) {
		if (button != null) {
			props.setProperty(key, String.valueOf(button.getSelection()));
		}
	}

	private void getProperty(Properties props, String key, Combo text) {
		props.setProperty(key, text.getText());
	}

	public void setProperties(Properties props) {
		if (Boolean.valueOf(props.getProperty(KEY_ENABLE_FILTER)).booleanValue()) {
			fEnable= true;
			setProperty(props, KEY_COMMENT_FILTER, fCommentFilter);
			setProperty(props, KEY_INCLUDE_FILTER, fIncludeFilter);
			setProperty(props, KEY_PREP_FILTER, fPrepFilter);
			setProperty(props, KEY_STRING_FILTER, fStringFilter);
			setProperty(props, KEY_FUNCTION_FILTER, fFunctionFilter);
			setProperty(props, KEY_ELSEWHERE_FILTER, fOtherFilter);
			setProperty(props, KEY_FILTER_PATTERNS, fFilterString);
			setProperty(props, KEY_INVERT_FILTER, fHideMatching);
			setProperty(props, KEY_REGULAR_EXPRESSION_FILTER, fRegularExpressionFilter);
		} else {
			fEnable= false;
		}

		updateEnablement();
	}

	private void setProperty(Properties props, String key, Combo combo) {
		String property= props.getProperty(key);
		if (property != null) {
			combo.setText(property);
		}
	}

	private void setProperty(Properties props, String key, Button button) {
		if (button != null) {
			String property= props.getProperty(key);
			if (property != null) {
				button.setSelection(Boolean.valueOf(property).booleanValue());
			}
		}
	}

	public boolean restoreFromResult(RetrieverFilter filter) {
		// this came right back to me, so ignore it.
		if (filter.getOriginatorsHash() == hashCode()) {
			return fEnable;
		}
		int loc= filter.getAcceptedLocations();
		Pattern pattern= filter.getPattern();
		boolean el= (loc != ALL_LOCATIONS);
		boolean es= (pattern != null);

		fEnable= el || es;
		if (fEnable) {
			fEnableLocation= el;
			fEnableString= es;
		} else {
			el= es= false;
		}

		fUseLocationFilter.setSelection(el);
		fUseStringFilter.setSelection(es);

		if (el) {
			setButton(fCommentFilter, loc, AbstractTextFileScanner.LOCATION_COMMENT);
			setButton(fStringFilter, loc, AbstractTextFileScanner.LOCATION_STRING_LITERAL);
			setButton(fIncludeFilter, loc, AbstractTextFileScanner.LOCATION_IMPORT_OR_INCLUDE_STATEMENT);
			setButton(fPrepFilter, loc, AbstractTextFileScanner.LOCATION_PREPROCESSOR_DIRECTIVE);
			setButton(fFunctionFilter, loc, AbstractTextFileScanner.LOCATION_FUNCTION);
			setButton(fOtherFilter, loc, AbstractTextFileScanner.LOCATION_OTHER);
		}
		if (es) {
			fEnableString= true;
			fUseStringFilter.setSelection(true);
			fFilterString.setText(filter.getFilterString());
			fRegularExpressionFilter.setSelection(filter.getIsRegex());
			fHideMatching.setSelection(filter.getHideMatching());
		}
		updateEnablement();
		return fEnable;
	}

	private void setButton(Button button, int allowedLoc, int loc) {
		if (button != null) {
			button.setSelection((allowedLoc & (1 << loc)) != 0);
		}
	}
}
