/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.tree;

import org.eclipse.swt.graphics.Image;

import org.eclipse.ui.externaltools.internal.ant.antview.core.ResourceMgr;

public class ProjectErrorNode extends ProjectNode {
    public ProjectErrorNode(String filename, String error) {
        super(filename, error);
        addChild(new ErrorNode(error));
    }
        
	public Image getImage() {		          		   
		return ResourceMgr.getImage(IMAGE_PROJECT_ERROR);
	}
}
