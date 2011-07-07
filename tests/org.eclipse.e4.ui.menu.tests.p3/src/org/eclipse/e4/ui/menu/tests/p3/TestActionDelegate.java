package org.eclipse.e4.ui.menu.tests.p3;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.actions.ActionDelegate;


public class TestActionDelegate extends ActionDelegate {
	@Override
	public void runWithEvent(IAction action, Event event) {
		System.out.println("runWithEvent");
	}
}
