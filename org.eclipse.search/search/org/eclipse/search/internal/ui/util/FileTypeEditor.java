/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.search.internal.ui.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.dialogs.FileEditorMappingContentProvider;
import org.eclipse.ui.dialogs.FileEditorMappingLabelProvider;
import org.eclipse.ui.dialogs.ListSelectionDialog;

import org.eclipse.search.internal.ui.SearchMessages;

public class FileTypeEditor extends SelectionAdapter implements DisposeListener, SelectionListener {
	
	private Text fTextField;
	private Button fBrowseButton;
	private IEditorRegistry fResourceEditorRegistry;

	private static final String TYPE_DELIMITER= ","; //$NON-NLS-1$

	public FileTypeEditor(IEditorRegistry registry, Text textField, Button browseButton) {
		fResourceEditorRegistry= registry;
		fTextField= textField;
		fBrowseButton= browseButton;
		
		fTextField.addDisposeListener(this);
		fBrowseButton.addDisposeListener(this);
		fBrowseButton.addSelectionListener(this);
		
		if (fTextField.getText().equals("*")) //$NON-NLS-1$
			setFileTypes(getRegisteredFileExtensions());
	}
	
	public void widgetDisposed(DisposeEvent event) {
		Widget widget= event.widget;
		if (widget == fTextField) 
			fTextField= null;
		else if (widget	== fBrowseButton)
			fBrowseButton= null;
	}
	
	public void widgetSelected(SelectionEvent event) {
		if (event.widget == fBrowseButton)
			handleBrowseButton();
	}
		
	public void widgetDoubleSelected(SelectionEvent event) {
	}
		
	/**
	 *	Answer a collection of the currently-specified resource types
	 *
	 *	@return java.util.Vector
	 */
	public Set getFileTypes() {
		Set result= new HashSet();
		if (fTextField.getText().equals("*")) //$NON-NLS-1$
			return getRegisteredFileExtensions();
		else {
			StringTokenizer tokenizer= new StringTokenizer(fTextField.getText(), TYPE_DELIMITER);

			while (tokenizer.hasMoreTokens()) {
				String currentExtension= tokenizer.nextToken().trim();
				if (currentExtension.startsWith("*.")) //$NON-NLS-1$
					currentExtension= currentExtension.substring(2);
				if (!currentExtension.equals("")) //$NON-NLS-1$
					result.add(currentExtension);
			}
		}
		return result;
	}
	/**
	 *	Answer a collection of all registered extensions
	 *
	 *	@return java.util.Vector
	 */
	protected Set getRegisteredFileExtensions() {
		IFileEditorMapping editorMappings[]= getEditorMappings();
		int mappingsSize= editorMappings.length;
		Set result= new HashSet(mappingsSize);
		for (int i= 0; i < mappingsSize; i++) {
			IFileEditorMapping currentMapping= editorMappings[i];
			result.add(currentMapping.getExtension());
		};
		return result;
	}
	/**
	 *	Populate self's import types field based upon the passed types collection
	 *
	 *	@param types java.util.Vector
	 */
	public void setFileTypes(Set types) {
		StringBuffer result= new StringBuffer();
		Iterator typesIter= types.iterator();
		boolean first= true;
		while (typesIter.hasNext()) {
			if (!first) {
				result.append(TYPE_DELIMITER);
				result.append(" "); //$NON-NLS-1$
			} else
				first= false;
			result.append("*."); //$NON-NLS-1$
			result.append(typesIter.next());
		}
		fTextField.setText(result.toString());
	}

	protected IFileEditorMapping[] getEditorMappings() {
		IFileEditorMapping editorMappings[]= fResourceEditorRegistry.getFileEditorMappings();
		ArrayList noClassMappings= new ArrayList(editorMappings.length);
		for (int i= 0; i < editorMappings.length; i++) {
			IFileEditorMapping currentMapping= editorMappings[i];
			if (currentMapping.getName().equals("*") && !currentMapping.getExtension().equals("class")) // See 1G7A6PP //$NON-NLS-1$ //$NON-NLS-2$
				noClassMappings.add(currentMapping);
		}
		return (IFileEditorMapping[])noClassMappings.toArray(new IFileEditorMapping[noClassMappings.size()]);
	}

	protected void handleBrowseButton() {
		IFileEditorMapping editorMappings[]= getEditorMappings();

		int mappingsSize= editorMappings.length;
		Set selectedTypes= getFileTypes();
		List initialSelections= new ArrayList(selectedTypes.size());

		for (int i= 0; i < mappingsSize; i++) {
			IFileEditorMapping currentMapping= editorMappings[i];
			if (selectedTypes.contains(currentMapping.getExtension()))
				initialSelections.add(currentMapping);
		}

		ListSelectionDialog dialog= new ListSelectionDialog(
			fTextField.getShell(),
			editorMappings, 
			FileEditorMappingContentProvider.INSTANCE, 
			FileEditorMappingLabelProvider.INSTANCE, 
			SearchMessages.getString("ListSelectionDialog.message")); //$NON-NLS-1$

		dialog.setInitialSelections(initialSelections.toArray());
		dialog.setTitle(SearchMessages.getString("ListSelectionDialog.title")); //$NON-NLS-1$
		if (dialog.open() == dialog.OK) {
			Object[] dialogResult= dialog.getResult();
			int length= dialogResult.length;
			Set result= new HashSet(length);
			for (int i= 0; i < length; i++)
				result.add(((IFileEditorMapping)dialogResult[i]).getExtension());
			setFileTypes(result);
		}
	}
}