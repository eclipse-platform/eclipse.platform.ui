package org.eclipse.update.ui.forms.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.custom.ScrolledComposite;

/**
 * This form class understands form sections.
 * It assumes that they are part of the form
 * and it offers life cycle handling of sections
 * once they are registered.
 */

public abstract class AbstractSectionForm extends AbstractForm {
protected Vector sections=null;

public void registerSection(FormSection section) {
	if (sections == null)
		sections = new Vector();
	if (!sections.contains(section))
	   sections.add(section);
}

public void unregisterSection(FormSection section) {
	if (sections!=null && sections.contains(section))
	   sections.remove(section);
}
	
public void initialize(Object model) {
	if (sections != null) {
		for (Iterator iter = sections.iterator(); iter.hasNext();) {
			FormSection section = (FormSection) iter.next();
			section.initialize(model);
		}
	}
}
	
public void setFocus() {
	if (sections != null && sections.size()>0) {
		FormSection firstSection = (FormSection)sections.firstElement();
		firstSection.setFocus();
	}
}

public void update() {
	if (sections != null) {
		for (Iterator iter = sections.iterator(); iter.hasNext();) {
			FormSection section = (FormSection) iter.next();
			section.update();
		}
	}
}	
	
public void commitChanges(boolean onSave) {
	if (sections != null) {
		for (Iterator iter = sections.iterator(); iter.hasNext();) {
			FormSection section = (FormSection) iter.next();
			if (section.isDirty()) section.commitChanges(onSave);
		}
	}
}

public boolean doGlobalAction(String actionId) {
	Control focusControl = getFocusControl();
	if (focusControl==null) return false;

	if (canPerformDirectly(actionId, focusControl)) return true;
	Composite parent = focusControl.getParent();
	FormSection targetSection=null;
	while (parent!=null) {
		Object data = parent.getData();
		if (data!=null && data instanceof FormSection) {
			targetSection = (FormSection)data;
			break;
		}
		parent = parent.getParent();
	}
	if (targetSection!=null) {
		return targetSection.doGlobalAction(actionId);
	}
	return false;
}

protected Control getFocusControl() {
	Control control = getControl();
	if (control==null || control.isDisposed()) return null;
	Display display = control.getDisplay();
	Control focusControl = display.getFocusControl();
	if (focusControl==null || focusControl.isDisposed()) return null;
	return focusControl;
}

public boolean canPaste(Clipboard clipboard) {
	Control focusControl = getFocusControl();
	if (focusControl==null) return false;
	Composite parent = focusControl.getParent();
	FormSection targetSection=null;
	while (parent!=null) {
		Object data = parent.getData();
		if (data!=null && data instanceof FormSection) {
			targetSection = (FormSection)data;
			break;
		}
		parent = parent.getParent();
	}
	if (targetSection!=null) {
		return targetSection.canPaste(clipboard);
	}
	return false;
}

public void dispose() {
	if (sections != null) {
		for (Iterator iter = sections.iterator(); iter.hasNext();) {
			FormSection section = (FormSection) iter.next();
			section.dispose();
		}
	}
	super.dispose();
}

public static void ensureVisible(ScrolledComposite scomp, Control control) {
	// pending on the implementation of feature #10511
}

}

