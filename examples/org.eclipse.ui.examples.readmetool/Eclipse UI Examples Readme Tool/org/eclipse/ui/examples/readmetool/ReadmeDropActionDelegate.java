package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.part.IDropActionDelegate;
import java.io.ByteArrayInputStream;

/**
 * Adapter for handling the dropping of readme segments into
 * another plugin.  In this case, we expect the segments
 * to be dropped onto <code>IFile</code> object, or an adapter 
 * that supports <code>IFile</code>.
 */
public class ReadmeDropActionDelegate implements IDropActionDelegate {
	public static final String ID = "org_eclipse_ui_examples_readmetool_drop_actions";
/** (non-Javadoc)
 * Method declared on IDropActionDelegate
 */
public boolean run(Object source, Object target) {
	if (source instanceof byte[] && target instanceof IFile) {
		IFile file = (IFile)target;
		try {
			file.appendContents(new ByteArrayInputStream((byte[])source), false, true, null);
		} catch (CoreException e) {
			System.out.println("Exception in readme drop adapter: " + e.getStatus().getMessage());
			return false;
		}
		return true;
	}
	return false;
}
}
