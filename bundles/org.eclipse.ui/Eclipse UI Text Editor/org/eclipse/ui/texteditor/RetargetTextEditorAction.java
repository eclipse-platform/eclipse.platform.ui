package org.eclipse.ui.texteditor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;


/**
 * Action used by an editor action bar contributor to establish placeholders in
 * menus or action bars which can be retargeted to dynamically changing actions,
 * for example, those which come from the active editor. This action assumes that
 * the "wrapped" action sends out property change events in response to state
 * changes. It uses these change notification to adapt its enabling state and
 * its visual presentation.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public final class RetargetTextEditorAction extends ResourceAction {
	
	private IAction fAction;
	private String fDefaultText;
	
	private IPropertyChangeListener fListener= new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			update(event);
		}
	};
	
	/**
	 * Creates a new action. The action configures its initial visual 
	 * representation from the given resource bundle. If this action's
	 * wrapped action is set to <code>null</code> it also uses the 
	 * information in the resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or 
	 *   <code>null</code> if none
	 * @see ResourceAction#ResourceAction
	 */
	public RetargetTextEditorAction(ResourceBundle bundle, String prefix) {
		super(bundle, prefix);
		fDefaultText= getText();
	}
	
	/**
	 * Updates to the changes of the underlying action.
	 *
	 * @param event the change event describing the state change
	 */
	private void update(PropertyChangeEvent event) {
		if (ENABLED.equals(event.getProperty())) {
			Boolean bool= (Boolean) event.getNewValue();
			setEnabled(bool.booleanValue());
		} else if (TEXT.equals(event.getProperty()))
			setText((String) event.getNewValue());
		else if (TOOL_TIP_TEXT.equals(event.getProperty()))
			setToolTipText((String) event.getNewValue());
	}
	
	/**
	 * Sets the underlying action.
	 *
	 * @param action the underlying action
	 */
	public void setAction(IAction action) {
		
		if (fAction != null) {
			fAction.removePropertyChangeListener(fListener);
			fAction= null;
		}
		
		fAction= action;
		
		if (fAction == null) {
			
			setEnabled(false);
			setText(fDefaultText);
			setToolTipText(""); //$NON-NLS-1$
		
		} else {
						
			setEnabled(fAction.isEnabled());
			setText(fAction.getText());
			setToolTipText(fAction.getToolTipText());
			fAction.addPropertyChangeListener(fListener);
		}
	}

	/*
	 * @see Action#run()
	 */
	public void run() {
		if (fAction != null)
			fAction.run();
	}
	
	/*
	 * @see IAction#getActionDefinitionId()
	 */
	public String getActionDefinitionId() {
		if(fAction != null)
			return fAction.getActionDefinitionId();
		return null;
	}
}