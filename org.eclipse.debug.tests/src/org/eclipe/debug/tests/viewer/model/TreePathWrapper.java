package org.eclipe.debug.tests.viewer.model;

import junit.framework.Assert;

import org.eclipse.jface.viewers.TreePath;

/**
 * Utility for comparing TreePath objects in unit tests.  This wrapper prints the tree 
 * paths in exception showing contexts of the paths.
 * 
 * @since 3.7
 */
public class TreePathWrapper {
    private final TreePath fPath;

    public TreePathWrapper(TreePath path) {
        fPath = path;
    }
    
    public int hashCode() {
        return fPath.hashCode();
    }
    
    public boolean equals(Object obj) {
        return obj instanceof TreePathWrapper &&
               fPath.equals( ((TreePathWrapper)obj).fPath ); 
    }
    
    public String toString() {
        if (fPath.getSegmentCount() == 0) {
            return "TreePath:EMPTY";
        }
        
        StringBuffer buf = new StringBuffer("TreePath:[");
        
        for (int i = 0; i < fPath.getSegmentCount(); i++) {
            if (i != 0) {
                buf.append(", ");                    
            }
            buf.append(fPath.getSegment(i));
        }
        buf.append(']');
        return buf.toString();
    }
    
    /**
     * Asserts that the two given tree paths are the same.  In case of failure, the 
     * generated exception will contain a printout of the tree paths' contents.
     */
    public static void assertEqual(TreePath expected, TreePath actual) {
        Assert.assertEquals(
            expected != null ? new TreePathWrapper(expected) : null,  
            actual != null ? new TreePathWrapper(actual) : null);
    }        
}