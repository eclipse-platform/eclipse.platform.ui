/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.editors.text;

import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.*;

/**
 * Action group for encoding actions.
 * @since 2.0
 */
public class EncodingActionGroup extends ActionGroup {

	/**
	 * Action for setting the encoding of the editor to the value this action has 
	 * been initialized with.
	 */
	static class PredefinedEncodingAction extends Action implements IUpdate {

		/** The target encoding of this action. */
		private EncodingDefinition fEncoding;
		/** Indicates whether the target encoding is the default encoding. */
		private boolean fIsDefault;
		/** Get the editor this will apply to*/
		private ITextEditor fTextEditor;

		/**
		 * Creates a new action for the given specification.
		 * 
		 * @param encoding EncodingDefinition
		 * @param editor the target editor
		 */
		public PredefinedEncodingAction(
			EncodingDefinition encoding,
			ITextEditor editor) {
			super();
			fEncoding = encoding;
			setText(encoding.getLabel());
			setToolTipText(encoding.getToolTip());
			setDescription(encoding.getDescription());
			fTextEditor = editor;
		}

		/**
			* Returns the action's text editor.
			*
			* @return the action's text editor
			*/
		protected ITextEditor getTextEditor() {
			return fTextEditor;
		}

		/**
		 * Returns the encoding support of the action's editor.
		 * 
		 * @return the encoding support of the action's editor
		 */
		private IEncodingSupport getEncodingSupport() {
			ITextEditor editor = getTextEditor();
			if (editor != null)
				return (IEncodingSupport) editor.getAdapter(
					IEncodingSupport.class);
			return null;
		}

		/*
		 * @see IAction#run()
		 */
		public void run() {
			IEncodingSupport s = getEncodingSupport();
			if (s != null)
				s.setEncoding(fIsDefault ? null : fEncoding.getValue());
		}

		/**
		 * Returns the encoding currently used in the given editor.
		 * 
		 * @param editor the editor
		 * @return the encoding currently used in the given editor
		 */
		private String getEncoding(ITextEditor editor) {
			IEncodingSupport s = getEncodingSupport();
			if (s != null)
				return s.getEncoding();
			return null;
		}

		/**
		 * Returns the default encoding for the given editor.
		 * 
		 * @param editor the editor
		 * @return the default encoding for the given editor
		 */
		private String getDefaultEncoding(ITextEditor editor) {
			IEncodingSupport s = getEncodingSupport();
			if (s != null)
				return s.getDefaultEncoding();
			return null;
		}

		/**
		* Sets the action's help context id.
		* 
		* @param contextId the help context id
		*/
		public final void setHelpContextId(String contextId) {
			WorkbenchHelp.setHelp(this, contextId);
		}

		/*
		 * @see IUpdate#update()
		 */
		public void update() {

			if (fEncoding == null) {
				setEnabled(false);
				return;
			}

			ITextEditor editor = getTextEditor();
			if (editor == null) {
				setEnabled(false);
				return;
			}

			// update label
			String encodingValue = getDefaultEncoding(editor);
			if (encodingValue != null) {
				fIsDefault = fEncoding.getValue().equals(encodingValue);
				String label = fEncoding.getLabel();
				if(fIsDefault)
					label = TextEditorMessages.format("Editor.ConvertEncoding.default",new String[] {label});
				setText(label);
			}

			// update enable state
			if (editor.isDirty()) {
				setEnabled(false);
			} else {
				String current = getEncoding(editor);
				if (fIsDefault)
					setEnabled(current != null);
				else
					setEnabled(!fEncoding.getValue().equals(current));
			}
		}
	};

	/**
	 * Sets the encoding of an  editor to the value that has interactively been defined.
	 */
	static class CustomEncodingAction extends TextEditorAction {

		/*
		 * @see org.eclipse.ui.texteditor.TextEditorAction#TextEditorAction(ResourceBundle, String, ITextEditor)
		 */
		protected CustomEncodingAction(
			ResourceBundle bundle,
			String prefix,
			ITextEditor editor) {
			super(bundle, prefix, editor);
		}

		/*
		 * @see IUpdate#update()
		 */
		public void update() {
			ITextEditor editor = getTextEditor();
			setEnabled(editor != null && !editor.isDirty());
		}

		/*
		 * @see IAction#run()
		 */
		public void run() {

			ITextEditor editor = getTextEditor();
			if (editor == null)
				return;

			String title = TextEditorMessages.getString("Editor.ConvertEncoding.Custom.dialog.title"); //$NON-NLS-1$
			String message = TextEditorMessages.getString("Editor.ConvertEncoding.Custom.dialog.message"); //$NON-NLS-1$
			IInputValidator inputValidator = new IInputValidator() {
				public String isValid(String newText) {
					return (newText == null || newText.length() == 0) ? " " : null; //$NON-NLS-1$
				}
			};

			InputDialog d = new InputDialog(editor.getSite().getShell(), title, message, "", inputValidator); //$NON-NLS-1$
			if (d.open() == d.OK) {
				IEncodingSupport s =
					(IEncodingSupport) editor.getAdapter(
						IEncodingSupport.class);
				if (s != null){
					String encodingValue = d.getValue();
					s.setEncoding(encodingValue);
					EncodingDefinitionManager.addEncoding(encodingValue);
				}
			}
		}
	};

	/** Suffix added to the default encoding action */
	private static final String DEFAULT_SUFFIX = " " + TextEditorMessages.getString("Editor.ConvertEncoding.default_suffix"); //$NON-NLS-1$ //$NON-NLS-2$

	/** List of encoding actions of this group */
	private List fRetargetActions = new ArrayList();
	private RetargetTextEditorAction customAction;

	/**
	 * Creates a new encoding action group for an action bar contributor.
	 */
	public EncodingActionGroup() {

		ResourceBundle b = TextEditorMessages.getResourceBundle();

		Collection encodings = EncodingDefinitionManager.getLabelSortedEncodings();
		Iterator iterator = encodings.iterator();
		while (iterator.hasNext()) {
			fRetargetActions.add(new EncodingDefinitionAction((EncodingDefinition) iterator.next())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		customAction = new RetargetTextEditorAction(b, "Editor.ConvertEncoding.Custom.", IEncodingActionsConstants.CUSTOM); //$NON-NLS-1$
	}

	/*
	 * @see ActionGroup#fillActionBars(IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		IMenuManager menuManager = actionBars.getMenuManager();
		IMenuManager editMenu =
			menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
		if (editMenu != null) {
			MenuManager subMenu = new MenuManager(TextEditorMessages.getString("Editor.ConvertEncoding.submenu.label")); //$NON-NLS-1$

			Iterator e = fRetargetActions.iterator();
			while (e.hasNext())
				subMenu.add((IAction) e.next());
			subMenu.add(customAction);

			editMenu.add(subMenu);
		}
	}

	/**
	 * Retargets this action group to the given editor.
	 * 
	 * @param editor the target editor
	 */
	public void retarget(ITextEditor editor) {
		Iterator e = fRetargetActions.iterator();
		while (e.hasNext()) {
			EncodingDefinitionAction a = (EncodingDefinitionAction) e.next();
			a.setAction(editor == null ? null : editor.getAction(a.getId()));
		}
		customAction.setAction(editor == null ? null : editor.getAction(customAction.getId()));
	}

	//------------------------------------------------------------------------------------------

	/** Text editor this group is associated with */
	private ITextEditor fTextEditor;

	/**
	 * Creates a new encoding action group for the given editor
	 * 
	 * @param editor the editor
	 */
	public EncodingActionGroup(ITextEditor editor) {

		fTextEditor = editor;
		ResourceBundle b = TextEditorMessages.getResourceBundle();


		Collection encodings = EncodingDefinitionManager.getLabelSortedEncodings();
		Iterator iterator = encodings.iterator();

		while (iterator.hasNext()) {
			PredefinedEncodingAction action;
			EncodingDefinition encoding = (EncodingDefinition) iterator.next();
			action = new PredefinedEncodingAction(encoding, editor); //$NON-NLS-1$ //$NON-NLS-2$
			action.setHelpContextId(encoding.getHelpContextId());
			action.setActionDefinitionId(encoding.getId());
			editor.setAction(encoding.getId(), action);
		}

		CustomEncodingAction custom = new CustomEncodingAction(b, "Editor.ConvertEncoding." + IEncodingActionsConstants.CUSTOM + ".", editor); //$NON-NLS-1$ //$NON-NLS-2$
		custom.setHelpContextId(IEncodingActionsHelpContextIds.CUSTOM);
		custom.setActionDefinitionId(IEncodingActionsDefinitionIds.CUSTOM);
		editor.setAction(IEncodingActionsConstants.CUSTOM, custom);
	}

	/**
	 * Updates all actions of this action group.
	 */
	public void update() {

		IAction a = fTextEditor.getAction(IEncodingActionsConstants.SYSTEM);
		if (a instanceof IUpdate)
			 ((IUpdate) a).update();

		Iterator encodings = EncodingDefinitionManager.getEncodings().iterator();
		while(encodings.hasNext()) {
			EncodingDefinition definition = (EncodingDefinition) encodings.next();
			a = fTextEditor.getAction(definition.getId());
			if (a instanceof IUpdate)
				 ((IUpdate) a).update();
		}

		a = fTextEditor.getAction(IEncodingActionsConstants.CUSTOM);
		if (a instanceof IUpdate)
			 ((IUpdate) a).update();
	}

	/*
	 * @see ActionGroup#dispose()
	 */
	public void dispose() {
		if (fTextEditor != null) {
			fTextEditor.setAction(IEncodingActionsConstants.SYSTEM, null);
			Iterator encodings = EncodingDefinitionManager.getEncodings().iterator();
			while(encodings.hasNext()) {
				EncodingDefinition definition = (EncodingDefinition) encodings.next();
				fTextEditor.setAction(definition.getId(), null);
			}
			fTextEditor.setAction(IEncodingActionsConstants.CUSTOM, null);

			fTextEditor = null;
		}
	}
}
