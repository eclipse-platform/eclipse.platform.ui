/*
 * Created on Jun 9, 2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code Template
 */
package org.eclipse.update.internal.ui.parts;

import org.eclipse.jface.viewers.*;
import org.eclipse.update.internal.ui.model.*;

/**
 * @author Wassim Melhem
 */
public class MyComputerContentProvider
	extends DefaultContentProvider
	implements ITreeContentProvider {

		public Object[] getChildren(Object parent) {
			if (parent instanceof MyComputer) {
				return ((MyComputer) parent).getChildren(parent);
			}
			if (parent instanceof MyComputerDirectory) {
				return ((MyComputerDirectory) parent).getChildren(parent);
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof MyComputerDirectory) {
				Object[] children = ((MyComputerDirectory) parent).getChildren(parent);
				for (int i = 0; i < children.length; i++) {
					if (!(children[i] instanceof MyComputerFile))
						return true;
				}
			}
			return false;
		}

		public Object[] getElements(Object element) {
			return getChildren(element);
		}

}
