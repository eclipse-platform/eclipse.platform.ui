/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.dialogs;

import java.net.URI;
import java.util.ArrayList;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * @since 3.4
 *
 */
public class RelativePathVariableGroup {

	private Button variableCheckbox = null;
	
	private Combo variableCombo = null;
	
	private Shell shell;
	
	private IModel content;
	
	private String label;
	
	public interface IModel {
		/**
		 * @return
		 */
		IResource getResource();

		/**
		 * @param object
		 */
		void setVariable(String string);

		/**
		 * @return
		 */
		String getVariable();
	}
	/**
	 * 
	 */
	public RelativePathVariableGroup(IModel content) {
		this.content = content;
	}

	public RelativePathVariableGroup(IModel content, String label) {
		this.content = content;
		this.label = label;
	}

	/**
	 * @param variableGroup 
	 * @return the control
	 */
	public Control createContents(Composite variableGroup) {
		shell = variableGroup.getShell();

		variableCheckbox = new Button(variableGroup, SWT.CHECK);
		variableCheckbox.setText(label != null? label:IDEWorkbenchMessages.ImportTypeDialog_importElementsAs);
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		variableCheckbox.setFont(variableGroup.getFont());
		variableCheckbox.setLayoutData(gridData);
		variableCheckbox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				selectRelativeCombo();
			}
			public void widgetSelected(SelectionEvent e) {
				selectRelativeCombo();
			}
			private void selectRelativeCombo() {
				if (variableCheckbox.getSelection()) {
					variableCombo.setEnabled(true);
					selectVariable(variableCombo.getItem(variableCombo.getSelectionIndex()));
					variableCheckbox.setToolTipText(IDEWorkbenchMessages.ImportTypeDialog_importElementsAsTooltipSet);
				}
				else {
					variableCombo.setEnabled(false);
					content.setVariable(null);
					variableCheckbox.setToolTipText(IDEWorkbenchMessages.ImportTypeDialog_importElementsAsTooltip);
				}
				setupVariableCheckboxToolTip();
			}
		});
		
		variableCombo = new Combo(variableGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING); // GridData.FILL_HORIZONTAL);
		variableCombo.setLayoutData(gridData);
		variableCombo.setFont(variableGroup.getFont());
		variableCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				if (variableCombo.getSelectionIndex() == (variableCombo.getItemCount() -1))
					editVariables();
				else
					selectVariable(variableCombo.getItem(variableCombo.getSelectionIndex()));
			}
			public void widgetSelected(SelectionEvent e) {
				if (variableCombo.getSelectionIndex() == (variableCombo.getItemCount() -1))
					editVariables();
				else
					selectVariable(variableCombo.getItem(variableCombo.getSelectionIndex()));
			}
		});
		setupVariableContent();
		selectVariable("PROJECT_LOC"); //$NON-NLS-1$
		return variableGroup;
	}

	/**
	 * 
	 */
	public void setupVariableContent() {
		IPathVariableManager pathVariableManager;
		if (content.getResource() != null)
			pathVariableManager = content.getResource().getPathVariableManager();
		else
			pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
		String[] variables = pathVariableManager.getPathVariableNames();
		
		ArrayList items = new ArrayList();
		for (int i = 0; i < variables.length; i++) {
			if (variables[i].equals("PARENT")) //$NON-NLS-1$
				continue;
			items.add(variables[i]);
		}
		items.add(IDEWorkbenchMessages.ImportTypeDialog_editVariables);
		variableCombo.setItems((String[]) items.toArray(new String[0]));
	}

	private void setupVariableCheckboxToolTip() {
		if (variableCheckbox != null) {
			if (variableCheckbox.getSelection())
				variableCheckbox.setToolTipText(IDEWorkbenchMessages.ImportTypeDialog_importElementsAsTooltipSet);
			else
				variableCheckbox.setToolTipText(IDEWorkbenchMessages.ImportTypeDialog_importElementsAsTooltip);
		}
	}

	private void editVariables() {
		String selectedItem = content.getVariable();
		PathVariableEditDialog dialog = new PathVariableEditDialog(shell);
		dialog.setResource(content.getResource());
		if (dialog.open() == IDialogConstants.OK_ID) {
			String[] variableNames = (String[]) dialog.getResult();
			if (variableNames != null && variableNames.length >= 1) {
				selectedItem = variableNames[0];
			}
		}
		setupVariableContent();
		if (selectedItem != null) {
			selectVariable(selectedItem);
		}
	}

	/**
	 * @param var
	 */
	public void selectVariable(String var) {
		String[] items = variableCombo.getItems();
		for (int i = 0; i < items.length; i++) {
			if (var.equals(items[i])) {
				variableCombo.select(i);
				content.setVariable(items[i]);
				return;
			}
		}
		variableCombo.select(0);
		content.setVariable(items[0]);
	}

	/**
	 * @param b
	 */
	public void setEnabled(boolean b) {
		variableCheckbox.setEnabled(b);
		variableCombo.setEnabled(variableCheckbox.getSelection() && variableCheckbox.isEnabled());
		setupVariableCheckboxToolTip();
	}

	/**
	 * @param b
	 */
	public void setSelection(boolean b) {
		variableCheckbox.setSelection(b);
		setupVariableCheckboxToolTip();
		variableCombo.setEnabled(variableCheckbox.getSelection() && variableCheckbox.isEnabled());
	}

	/**
	 * Find the most appropriate path variable for a set of paths.
	 * The first thing is to find a common root for all the paths.
	 * So for the following paths:
	 * 		c:\foo\path\bar\dir1\file1.txt
	 * 		c:\foo\path\bar\dir2\file2.txt
	 * The following root will be selected:
	 * 		c:\foo\path\bar\
	 * Then, given all the path variable locations, the variable
	 * who's distance (in segments) from the common root in the smallest
	 * will be chosen.
	 * A priority is given as to variables enclosing the root, as others
	 * only being enclosed by the root.
	 *
	 * So if there's two variables, being 
	 * 		FOO - c:\foo\
	 * 		DIR1 - c:\foo\path\bar\dir1
	 * And the common root is:
	 * 		c:\foo\path\bar
	 * FOO will be selected over DIR1, even through the distance between 
	 * the common root and DIR1 is (1), and the distance between the 
	 * common root and FOO is (2).  This is because selecting DIR1 would
	 * cause the location to be relative to its parent.

	 * @param paths
	 * 		The list of items that were dragged
	 * @param target
	 * 		The target container onto which the items were dropped
	 * @return the most appropriate path variable given the context
	 */
	public static String getPreferredVariable(IPath[] paths,
			IContainer target) {
		IPath commonRoot = null;
		for (int i = 0; i < paths.length; i++) {
			if (paths[i] != null) {
				if (commonRoot == null)
					commonRoot = paths[i];
				else  {
					int count = commonRoot.matchingFirstSegments(paths[i]);
					int remainingSegments = commonRoot.segmentCount() - count;
					if (remainingSegments <= 0)
						return null;
					commonRoot = commonRoot.removeLastSegments(remainingSegments);
				}
			}
		}
		
		String mostAppropriate = null;
		String mostAppropriateToParent = null;
		int mostAppropriateCount = Integer.MAX_VALUE;
		int mostAppropriateCountToParent = Integer.MAX_VALUE;
		IPathVariableManager pathVariableManager = target.getPathVariableManager();
		String [] variables = pathVariableManager.getPathVariableNames();
		
		for (int i = 0; i < variables.length; i++) {
			if (isPreferred(variables[i])) {
				URI rawValue = pathVariableManager.getURIValue(variables[i]);
				URI value = pathVariableManager.resolveURI(rawValue);
				if (value != null) {
					IPath path = URIUtil.toPath(value);
					if (path != null) {
						int difference = path.matchingFirstSegments(commonRoot);
						if (difference > 0) {
							if (difference < mostAppropriateCount) {
								mostAppropriateCount = difference;
								mostAppropriate = variables[i];
							}
						}
						else {
							// calculate if commonRoot could be relative to the parent of path
							difference = commonRoot.matchingFirstSegments(path);
							if (difference > 0) {
								if (difference < mostAppropriateCountToParent) {
									mostAppropriateCountToParent = difference;
									mostAppropriateToParent = variables[i];
								}
							}
						}
					}
				}
			}
		}
		
		if (mostAppropriate == null) {
			if (mostAppropriateToParent == null)
				return "PROJECT_LOC"; //$NON-NLS-1$
			return mostAppropriateToParent;
		}
		return mostAppropriate;
	}
	
	private static boolean isPreferred(String variableName) {
		return !(variableName.equals("WORKSPACE_LOC") || //$NON-NLS-1$
				variableName.equals("PARENT_LOC") || //$NON-NLS-1$
				variableName.equals("PARENT")); //$NON-NLS-1$
	}

	/**
	 * Return the most appropriate path variable given the context
	 * @param sources
	 * 		The list of resources that were dragged
	 * @param target
	 * 		The target container onto which the resources were dropped
	 * @return the most appropriate path variable given the context
	 */
	public static String getPreferredVariable(IResource[] sources,
			IContainer target) {
		IPath[] paths = new IPath[sources.length];
		for (int i = 0; i < sources.length; i++) {
			paths[i] = sources[i].getLocation();
		}
		return getPreferredVariable(paths, target);
	}

	/**
	 * Return the most appropriate path variable given the context
	 * @param names
	 * 		The list of files that were dragged
	 * @param target
	 * 		The target container onto which the files were dropped
	 * @return the most appropriate path variable given the context
	 */
	public static String getPreferredVariable(String[] names,
			IContainer target) {
		IPath[] paths = new IPath[names.length];
		for (int i = 0; i < names.length; i++) {
			paths[i] = Path.fromOSString(names[i]);
		}
		return getPreferredVariable(paths, target);
	}

	/**
	 * @return
	 */
	public boolean getSelection() {
		return variableCheckbox.getSelection();
	}
}
