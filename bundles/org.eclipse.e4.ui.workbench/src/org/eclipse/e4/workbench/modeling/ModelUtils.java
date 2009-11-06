package org.eclipse.e4.workbench.modeling;

import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.emf.common.util.EList;

public class ModelUtils {
	public static MApplicationElement findById(MApplicationElement toTest, String id) {
		if (id == null || id.length() == 0)
			return null;

		if (id.equals(toTest.getId())) {
			return toTest;
		}

		if (toTest instanceof MElementContainer<?>) {
			MElementContainer<MUIElement> container = (MElementContainer<MUIElement>) toTest;
			EList<MUIElement> children = container.getChildren();
			for (MUIElement child : children) {
				MApplicationElement foundElement = findById(child, id);
				if (foundElement != null)
					return foundElement;
			}
		}

		return null;
	}
}
