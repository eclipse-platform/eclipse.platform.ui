package org.eclipse.ant.internal.ui;/* * (c) Copyright IBM Corp. 2000, 2001. * All Rights Reserved. */import java.util.Vector;import org.apache.tools.ant.Target;import org.eclipse.jface.viewers.LabelProvider;

public class TargetsListLabelProvider extends LabelProvider {
	
	private Vector selectedTargets = null;

	/**
	 * Takes an object and returns a string which will stand for this object.
	 * 
	 * @param model the object that has to be displayed
	 * @return the string representing the object
	 */	
	public String getText(Object model) {
		Target targetToDisplay = (Target) model;
		// Could it be null ?
		if (targetToDisplay != null) {
			StringBuffer result = new StringBuffer(targetToDisplay.getName());
			if (selectedTargets != null) {
				int targetIndex = selectedTargets.indexOf(model);
				if (targetIndex >= 0) {
					result.append(" [");
					result.append(targetIndex + 1);
					result.append("]");
				}
			}
			
			return result.toString();
		}

		else
			return "";
	}
	
//	public Image getImage(Object model) {
//		return null;
//	}

	public void setSelectedTargets(Vector value) {
		selectedTargets = value;
	}
}
