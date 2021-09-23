package org.eclipse.e4.ui.workbench.persistence.tests.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

public class TestViewPart extends ViewPart {

	public static final String ID = "org.eclipse.ui.tests.harness.TestView";
	
	private static final String MEMENTO_TEXT_KEY = "MyText";

	private IMemento memento;

	private Text myText;

	public TestViewPart() {
	}

	@Override
	public void createPartControl(Composite parent) {
		myText = new Text(parent, SWT.NONE);
		if (this.memento != null) {
			myText.setText(this.memento.getString(MEMENTO_TEXT_KEY));
		} else {
			myText.setText("INITIAL TEXT!");
		}
	}

	@Override
	public void setFocus() {
		// Do nothing
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		this.memento = memento;
		super.init(site, memento);
	}

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putString(MEMENTO_TEXT_KEY, myText.getText());
	}


}
