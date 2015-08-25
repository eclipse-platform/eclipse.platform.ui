/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.preference;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * A concrete implementation of a node in a preference dialog tree. This class
 * also supports lazy creation of the node's preference page.
 */
public class PreferenceNode implements IPreferenceNode {
    /**
     * Preference page, or <code>null</code> if not yet loaded.
     */
    private IPreferencePage page;

    /**
     * The list of subnodes (immediate children) of this node (element type:
     * <code>IPreferenceNode</code>).
     */
    private List<IPreferenceNode> subNodes;

    /**
     * Name of a class that implements <code>IPreferencePage</code>, or
     * <code>null</code> if none.
     */
    private String classname;

    /**
     * The id of this node.
     */
    private String id;

    /**
     * Text label for this node. Note that this field is only used prior to the
     * creation of the preference page.
     */
    private String label;

    /**
     * Image descriptor for this node, or <code>null</code> if none.
     */
    private ImageDescriptor imageDescriptor;

    /**
     * Cached image, or <code>null</code> if none.
     */
    private Image image;

    /**
     * Creates a new preference node with the given id. The new node has no
     * subnodes.
     *
     * @param id
     *            the node id
     */
    public PreferenceNode(String id) {
        Assert.isNotNull(id);
        this.id = id;
    }

    /**
     * Creates a preference node with the given id, label, and image, and
     * lazily-loaded preference page. The preference node assumes (sole)
     * responsibility for disposing of the image; this will happen when the node
     * is disposed.
     *
     * @param id
     *            the node id
     * @param label
     *            the label used to display the node in the preference dialog's
     *            tree
     * @param image
     *            the image displayed left of the label in the preference
     *            dialog's tree, or <code>null</code> if none
     * @param className
     *            the class name of the preference page; this class must
     *            implement <code>IPreferencePage</code>
     */
    public PreferenceNode(String id, String label, ImageDescriptor image,
            String className) {
        this(id);
        this.imageDescriptor = image;
        Assert.isNotNull(label);
        this.label = label;
        this.classname = className;
    }

    /**
     * Creates a preference node with the given id and preference page. The
     * title of the preference page is used for the node label. The node will
     * not have an image.
     *
     * @param id
     *            the node id
     * @param preferencePage
     *            the preference page
     */
    public PreferenceNode(String id, IPreferencePage preferencePage) {
        this(id);
        Assert.isNotNull(preferencePage);
        page = preferencePage;
    }

    @Override
	public void add(IPreferenceNode node) {
        if (subNodes == null) {
			subNodes = new ArrayList<>();
		}
        subNodes.add(node);
    }

    /**
     * Creates a new instance of the given class <code>className</code>.
     *
     * @param className
     * @return new Object or <code>null</code> in case of failures.
     */
    private Object createObject(String className) {
        Assert.isNotNull(className);
        try {
            Class<?> cl = Class.forName(className);
            if (cl != null) {
				return cl.newInstance();
			}
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (NoSuchMethodError e) {
            return null;
        }
        return null;
    }

    @Override
	public void createPage() {
        page = (IPreferencePage) createObject(classname);
        if (getLabelImage() != null) {
			page.setImageDescriptor(imageDescriptor);
		}
        page.setTitle(label);
    }

    @Override
	public void disposeResources() {
        if (image != null) {
            image.dispose();
            image = null;
        }
        if (page != null) {
            page.dispose();
            page = null;
        }
    }

    @Override
	public IPreferenceNode findSubNode(String id) {
        Assert.isNotNull(id);
        Assert.isTrue(id.length() > 0);
        if (subNodes == null) {
			return null;
		}
        int size = subNodes.size();
        for (int i = 0; i < size; i++) {
            IPreferenceNode node = subNodes.get(i);
            if (id.equals(node.getId())) {
				return node;
			}
        }
        return null;
    }

    @Override
	public String getId() {
        return this.id;
    }

    /**
     * Returns the image descriptor for this node.
     *
     * @return the image descriptor
     */
    protected ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    @Override
	public Image getLabelImage() {
        if (image == null && imageDescriptor != null) {
            image = imageDescriptor.createImage();
        }
        return image;
    }

    @Override
	public String getLabelText() {
        if (page != null) {
			return page.getTitle();
		}
        return label;
    }

    @Override
	public IPreferencePage getPage() {
        return page;
    }

    @Override
	public IPreferenceNode[] getSubNodes() {
        if (subNodes == null) {
			return new IPreferenceNode[0];
		}
        return subNodes
                .toArray(new IPreferenceNode[subNodes.size()]);
    }

    @Override
	public IPreferenceNode remove(String id) {
        IPreferenceNode node = findSubNode(id);
        if (node != null) {
			remove(node);
		}
        return node;
    }

    @Override
	public boolean remove(IPreferenceNode node) {
        if (subNodes == null) {
			return false;
		}
        return subNodes.remove(node);
    }

    /**
     * Set the current page to be newPage.
     *
     * @param newPage
     */
    public void setPage(IPreferencePage newPage) {
        page = newPage;
    }
}
