package org.eclipse.ui.tests.api;

import org.eclipse.ui.*;
import org.eclipse.ui.junit.util.CallHistory;

public class MockEditorActionBarContributor
	implements IEditorActionBarContributor 
{
	protected CallHistory callHistory;
	protected IEditorPart target;

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
	}

	/**
	 * @see IEditorActionBarContributor#setActiveEditor(IEditorPart)
	 */
	public void setActiveEditor(IEditorPart targetEditor) {
		callHistory.add("setActiveEditor");
		target = targetEditor;
	}
	
	public IEditorPart getActiveEditor() {
		return target;
	}

}

