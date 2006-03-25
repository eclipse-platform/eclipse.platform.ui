package org.eclipse.jface.internal.text.revisions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlCreatorExtension;

/**
 * Abstract class for a reusable information control creators.
 * <p>
 * XXX copy of org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator.
 * </p>
 * 
 * @since 3.2
 */
abstract class AbstractReusableInformationControlCreator implements IInformationControlCreator, IInformationControlCreatorExtension, DisposeListener {

	private Map fInformationControls= new HashMap();

	/**
	 * Creates the control.
	 * 
	 * @param parent the parent shell
	 * @return the created information control
	 */
	protected abstract IInformationControl doCreateInformationControl(Shell parent);

	/*
	 * @see org.eclipse.jface.text.IInformationControlCreator#createInformationControl(org.eclipse.swt.widgets.Shell)
	 */
	public IInformationControl createInformationControl(Shell parent) {
		IInformationControl control= (IInformationControl)fInformationControls.get(parent);
		if (control == null) {
			control= doCreateInformationControl(parent);
			control.addDisposeListener(this);
			fInformationControls.put(parent, control);
		}			
		return control;
	}
	
	/*
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	public void widgetDisposed(DisposeEvent e) {
		Composite parent= null;
		if (e.widget instanceof Shell)
			parent= ((Shell)e.widget).getParent();
		if (parent instanceof Shell)
			fInformationControls.remove(parent);
	}


	/*
	 * @see org.eclipse.jface.text.IInformationControlCreatorExtension#canReuse(org.eclipse.jface.text.IInformationControl)
	 */
	public boolean canReuse(IInformationControl control) {
		return fInformationControls.containsValue(control);
	}

	/*
	 * @see org.eclipse.jface.text.IInformationControlCreatorExtension#canReplace(org.eclipse.jface.text.IInformationControlCreator)
	 */
	public boolean canReplace(IInformationControlCreator creator) {
		return creator.getClass() == getClass();
	}
}
