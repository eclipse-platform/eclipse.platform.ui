package org.eclipse.ui.tests.api;

import org.eclipse.ui.*;
import org.eclipse.ui.tests.util.CallHistory;

public class MockEditorActionBarContributor
	implements IEditorActionBarContributor 
{
	protected CallHistory callHistory;
	protected IActionBars bars;
	protected IEditorPart target;
	protected int ACTION_COUNT = 5;
	protected MockAction [] actions;

	/**
	 * Constructor for MockEditorActionBarContributor
	 */
	public MockEditorActionBarContributor() {
		super();
		callHistory = new CallHistory(this);
	}

	public CallHistory getCallHistory() {
		return callHistory;
	}
	
	/**
	 * @see IEditorActionBarContributor#init(IActionBars)
	 */
	public void init(IActionBars bars) {
		callHistory.add("init");
		this.bars = bars;
		actions = new MockAction[ACTION_COUNT];
		for (int nX = 0; nX < ACTION_COUNT; nX ++) {
			actions[nX] = new MockAction(Integer.toString(nX));
			if (nX % 2 > 0)
				actions[nX].setEnabled(false);
			bars.getToolBarManager().add(actions[nX]);
		}
		bars.updateActionBars();
	}

	/**
	 * @see IEditorActionBarContributor#setActiveEditor(IEditorPart)
	 */
	public void setActiveEditor(IEditorPart targetEditor) {
		callHistory.add("setActiveEditor");
		target = targetEditor;
	}
	
	/**
	 * Returns the active editor.
	 */
	public IEditorPart getActiveEditor() {
		return target;
	}
	
	/**
	 * Returns the action bars.
	 */
	public IActionBars getActionBars() {
		return bars;
	}
	
	/**
	 * Returns the actions.
	 */
	public MockAction [] getActions() {
		return actions;
	}
	
	/**
	 * Set the enablement for all actions.
	 */
	public void enableActions(boolean b) {
		for (int nX = 0; nX < ACTION_COUNT; nX ++) {
			actions[nX].setEnabled(b);
		}
	}

}

