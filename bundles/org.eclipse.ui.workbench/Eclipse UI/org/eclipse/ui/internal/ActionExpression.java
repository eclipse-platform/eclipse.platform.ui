/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

/**
 * An ActionExpression is used to evaluate the enablement / visibility
 * criteria for an action.  
 */
public class ActionExpression {
    /**
     * Constant definition for OR. 
     * @see org.eclipse.ui\schema\commonExpression.mxsd
     */
    public static final String EXP_TYPE_OR = "or"; //$NON-NLS-1$

    /**
     * Constant definition for AND.
     * @see org.eclipse.ui\schema\commonExpression.mxsd
     */
    public static final String EXP_TYPE_AND = "and"; //$NON-NLS-1$

    /**
     * Constant definition for NOT.
     * @see org.eclipse.ui\schema\commonExpression.mxsd
     */
    public static final String EXP_TYPE_NOT = "not"; //$NON-NLS-1$

    /**
     * Constant definition for objectState.
     * @see org.eclipse.ui\schema\commonExpression.mxsd
     */
    public static final String EXP_TYPE_OBJECT_STATE = "objectState"; //$NON-NLS-1$

    /**
     * Constant definition for objectClass.
     * @see org.eclipse.ui\schema\commonExpression.mxsd
     */
    public static final String EXP_TYPE_OBJECT_CLASS = "objectClass"; //$NON-NLS-1$

    /**
     * Constant definition for pluginState.
     * @see org.eclipse.ui\schema\commonExpression.mxsd
     */
    public static final String EXP_TYPE_PLUG_IN_STATE = "pluginState"; //$NON-NLS-1$

    /**
     * Constant definition for systemProperty.
     * @see org.eclipse.ui\schema\commonExpression.mxsd
     */
    public static final String EXP_TYPE_SYSTEM_PROPERTY = "systemProperty"; //$NON-NLS-1$	

    private static final String ATT_NAME = "name"; //$NON-NLS-1$

    private static final String ATT_VALUE = "value"; //$NON-NLS-1$

    private static final String ATT_ID = "id"; //$NON-NLS-1$

    private static final String PLUGIN_INSTALLED = "installed"; //$NON-NLS-1$

    private static final String PLUGIN_ACTIVATED = "activated"; //$NON-NLS-1$

    private SingleExpression root;

    /**
     * Creates an action expression for the given configuration element.
     * @param element. The element to build the expression from.
     */
    public ActionExpression(IConfigurationElement element) {
        try {
            root = new SingleExpression(element);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            root = null;
        }
    }

    /**
     *  Create an instance of the receiver with the given expression type
     * and value. Currently the only supported expression type is
     * <code>EXP_TYPE_OBJECT_CLASS</code>.
     * @param expressionType The expression constant we are creating 
     *  an instance of.
     * @param expressionValue The name of the class we are creating
     * an expression for.
     */
    public ActionExpression(String expressionType, String expressionValue) {
        if (expressionType.equals(EXP_TYPE_OBJECT_CLASS)) {
            root = new SingleExpression(new ObjectClassExpression(
                    expressionValue));
        }
    }

    /**
     * Returns whether the expression is valid for the given object.
     * 
     * @param object the object to validate against (can be <code>null</code>)
     * @return boolean whether the expression is valid for the object.
     */
    public boolean isEnabledFor(Object object) {
        if (root == null)
            return false;
        return root.isEnabledFor(object);
    }

    /**
     * Returns whether or not the receiver is potentially valid for the object
     * via just the extension type. Currently the only supported expression type
     * is <code>EXP_TYPE_OBJECT_CLASS</code>.
     * 
     * @param object the object to validate against (can be <code>null</code>)
     * @param expressionType the expression type to consider
     * @return boolean whether the expression is potentially valid for the
     * object.
     */
    public boolean isEnabledForExpression(Object object, String expressionType) {
        if (root == null)
            return false;
        return root.isEnabledForExpression(object, expressionType);
    }

    /**
     * Returns whether the expression is valid for all elements of the given
     * selection.
     * 
     * @param selection the structured selection to use
     * @return boolean whether the expression is valid for the selection.
     */
    public boolean isEnabledFor(IStructuredSelection selection) {
        if (root == null)
            return false;

        if (selection == null || selection.isEmpty())
            return root.isEnabledFor(null);

        Iterator elements = selection.iterator();
        while (elements.hasNext()) {
            if (!isEnabledFor(elements.next()))
                return false;
        }
        return true;
    }
    
    /**
     * Return the values of the expression type that the receiver
     * is enabled for. If the receiver is not enabled for the
     * expressionType then return <code>null</code>.
     * 
     * @param expressionType the expression type to consider
     * @return Collection if there are values for this expression
     * or <code>null</code> if this is not possible in the 
     * receiver or any of it's children
     */
    public Collection valuesForExpression(String expressionType){
    	return root.valuesForExpression(expressionType);
    }

    /**
     * Create an expression from the attributes and sub-elements of the
     * configuration element.
     * @param element The IConfigurationElement with a tag 
     * defined in the public constants.
     * @return AbstractExpression based on the definition
     * @throws IllegalStateException if the expression tag is not defined in the schema.
     * @see org.eclipse.ui\schema\commonExpression.mxsd
     */
    private static AbstractExpression createExpression(
            IConfigurationElement element) throws IllegalStateException {
        String tag = element.getName();
        if (tag.equals(EXP_TYPE_OR))
            return new OrExpression(element);
        if (tag.equals(EXP_TYPE_AND))
            return new AndExpression(element);
        if (tag.equals(EXP_TYPE_NOT))
            return new NotExpression(element);
        if (tag.equals(EXP_TYPE_OBJECT_STATE))
            return new ObjectStateExpression(element);
        if (tag.equals(EXP_TYPE_OBJECT_CLASS))
            return new ObjectClassExpression(element);
        if (tag.equals(EXP_TYPE_PLUG_IN_STATE))
            return new PluginStateExpression(element);
        if (tag.equals(EXP_TYPE_SYSTEM_PROPERTY))
            return new SystemPropertyExpression(element);

        throw new IllegalStateException(
                "Action expression unrecognized element: " + tag); //$NON-NLS-1$
    }

    private static abstract class AbstractExpression {
        /**
         * Returns whether the expression is valid for the given object.
         * 
         * @param object the object to validate against (can be <code>null</code>)
         * @return boolean whether the expression is valid for the object.
         */
        public abstract boolean isEnabledFor(Object object);

        /**
         * Returns whether or not the receiver is potentially valid for the object
         * via just the extension type. Currently the only supported expression type
         * is <code>EXP_TYPE_OBJECT_CLASS</code>.
         * 
         * @param object the object to validate against (can be <code>null</code>)
         * @param expressionType the expression type to consider
         * @return boolean whether the expression is potentially valid for the
         * object.
         */
        public boolean isEnabledForExpression(Object object,
                String expressionType) {
            return false;
        }

        /**
         * Extract the object class test from the expression. This allows clients
         * (e.g. the decorator manager) to handle object class testing in a
         * more optimized way. This method removes the objectClass test from the 
         * expression and returns the object class. The expression is not changed and a 
         * <code>null</code> is returned if no object class is found.
         * @return the object class or <code>null</code> if none was found.
         */
        public String extractObjectClass() {
            return null;
        }
        
        /**
         * Return the value of the expression type that the receiver
         * is enabled for. If the receiver is not enabled for the
         * expressionType then return <code>null</code>.
         * 
         * @param expressionType the expression type to consider
         * @return  Collection of String if there are values for this expression
         * or <code>null</code> if this is not possible in the 
         * receiver or any of it's children
         */
        public Collection valuesForExpression(String expressionType){
        	return null;
        }
    }

    private static abstract class CompositeExpression extends
            AbstractExpression {
        protected ArrayList list;

        /**
         * Creates and populates the expression from the attributes and sub-
         * elements of the configuration element.
         * @param element The composite element we will create the
         * expression from.
         * @throws IllegalStateException if the expression tag is not defined in the schema.
         * @see org.eclipse.ui\schema\commonExpression.mxsd
         */
        public CompositeExpression(IConfigurationElement element)
                throws IllegalStateException {
            super();

            IConfigurationElement[] children = element.getChildren();
            if (children.length == 0)
                throw new IllegalStateException(
                        "Composite expression cannot be empty"); //$NON-NLS-1$

            list = new ArrayList(children.length);
            for (int i = 0; i < children.length; i++) {
                String tag = children[i].getName();
                AbstractExpression expr = createExpression(children[i]);
                if (EXP_TYPE_OBJECT_CLASS.equals(tag))
                    list.add(0, expr);
                else
                    list.add(expr);
            }
        }

        /* (non-Javadoc)
         * Method declared on AbstractExpression.
         */
        public boolean isEnabledForExpression(Object object,
                String expressionType) {
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                AbstractExpression next = (AbstractExpression) iterator.next();
                if (next.isEnabledForExpression(object, expressionType))
                    return true;
            }
            return false;
        }
        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.ActionExpression.AbstractExpression#extractObjectClass()
         */
        public String extractObjectClass() {
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                AbstractExpression next = (AbstractExpression) iterator.next();
                String objectClass = next.extractObjectClass();
                if (objectClass != null)
                    return objectClass;
            }
            return null;
        }
        
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.ActionExpression.AbstractExpression#valuesForExpression(java.lang.String)
         */
        public Collection valuesForExpression(String expressionType) {
        	 Iterator iterator = list.iterator();
        	 Collection allValues = null;
             while (iterator.hasNext()) {
                 AbstractExpression next = (AbstractExpression) iterator.next();
                 Collection values =
                	  next.valuesForExpression(expressionType);
                 if(values != null){
                	 if(allValues == null)
                		 allValues = values;
                	 else
                		 allValues.addAll(values);
                 }
                	
             }
             return allValues;
        }
    }

    private static class SingleExpression extends AbstractExpression {
        private AbstractExpression child;

        /**
         * Creates and populates the expression from the attributes and sub-
         * elements of the configuration element.
         * @param element The element to create the expression from.
         * @throws IllegalStateException if the expression tag is not defined in the schema.
         * @see org.eclipse.ui\schema\commonExpression.mxsd
         */
        public SingleExpression(IConfigurationElement element)
                throws IllegalStateException {
            super();

            IConfigurationElement[] children = element.getChildren();
            if (children.length != 1)
                throw new IllegalStateException(
                        "Single expression does not contain only 1 expression"); //$NON-NLS-1$
            child = createExpression(children[0]);
        }

        /**
         * Create a single expression from the abstract definition.
         * @param expression The expression that will be the child of the 
         * new single expression.
         * @throws IllegalStateException if the expression tag is not defined in the schema.
         * @see org.eclipse.ui\schema\commonExpression.mxsd
         */
        public SingleExpression(AbstractExpression expression)
                throws IllegalStateException {
            super();

            if (expression != null)
                child = expression;
            else
                throw new IllegalStateException(
                        "Single expression must contain 1 expression"); //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * Method declared on AbstractExpression.
         */
        public boolean isEnabledFor(Object object) {
            return child.isEnabledFor(object);
        }

        /* (non-Javadoc)
         * Method declared on AbstractExpression.
         */
        public boolean isEnabledForExpression(Object object,
                String expressionType) {
            return child.isEnabledForExpression(object, expressionType);
        }

        /**
         * Extract the object class test from the expression. This allows clients
         * (e.g. the decorator manager) to handle object class testing in a
         * more optimized way. This method removes the objectClass test from the 
         * expression and returns the object class. The expression is not changed and a 
         * <code>null</code> is returned if no object class is found.
         * @return the object class or <code>null</code> if none was found.
         */
        public String extractObjectClass() {
            return child.extractObjectClass();
        }
        
     
        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.ActionExpression.AbstractExpression#valuesForExpression(java.lang.String)
         */
        public Collection valuesForExpression(String expressionType) {
        	return child.valuesForExpression(expressionType);
        }

    }

    private static class NotExpression extends SingleExpression {

        /**
         * Creates and populates the expression from the attributes and sub-
         * elements of the configuration element.
         * @param element The element that will be used to create
         * the definition for the receiver.
         * @throws IllegalStateException if the expression tag is not defined in the schema.
         * @see org.eclipse.ui\schema\commonExpression.mxsd
         */
        public NotExpression(IConfigurationElement element)
                throws IllegalStateException {
            super(element);
        }

        /* (non-Javadoc)
         * Method declared on AbstractExpression.
         */
        public boolean isEnabledFor(Object object) {
            return !super.isEnabledFor(object);
        }
    }

    private static class OrExpression extends CompositeExpression {

        /**
         * Creates and populates the expression from the attributes and sub-
         * elements of the configuration element.
         * @param element. The element that will be used to determine
         * the expressions for Or.
         * @throws IllegalStateException if the expression tag is not defined in the schema.
         * @see org.eclipse.ui\schema\commonExpression.mxsd
         */
        public OrExpression(IConfigurationElement element)
                throws IllegalStateException {
            super(element);
        }

        /* (non-Javadoc)
         * Method declared on AbstractExpression.
         */
        public boolean isEnabledFor(Object object) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                AbstractExpression expr = (AbstractExpression) iter.next();
                if (expr.isEnabledFor(object))
                    return true;
            }
            return false;
        }
    }

    private static class AndExpression extends CompositeExpression {

        /**
         * Creates and populates the expression from the attributes and sub-
         * elements of the configuration element.
         * @param element. The element that will be used to determine
         * the expressions for And.
         * @throws IllegalStateException if the expression tag is not defined in the schema.
         * @see org.eclipse.ui\schema\commonExpression.mxsd
         */
        public AndExpression(IConfigurationElement element)
                throws IllegalStateException {
            super(element);
        }

        /* (non-Javadoc)
         * Method declared on AbstractExpression.
         */
        public boolean isEnabledFor(Object object) {
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                AbstractExpression expr = (AbstractExpression) iter.next();
                if (!expr.isEnabledFor(object))
                    return false;
            }
            return true;
        }
    }

    private static class ObjectStateExpression extends AbstractExpression {
        private String name;

        private String value;

        /**
         * Creates and populates the expression from the attributes and sub-
         * elements of the configuration element.
         * @param element. The element that will be used to determine
         * the expressions for objectState.
         * @throws IllegalStateException if the expression tag is not defined in the schema.
         * @see org.eclipse.ui\schema\commonExpression.mxsd
         */
        public ObjectStateExpression(IConfigurationElement element)
                throws IllegalStateException {
            super();

            name = element.getAttribute(ATT_NAME);
            value = element.getAttribute(ATT_VALUE);
            if (name == null || value == null)
                throw new IllegalStateException(
                        "Object state expression missing attribute"); //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * Method declared on AbstractExpression.
         */
        public boolean isEnabledFor(Object object) {
            if (object == null) {
                return false;
            }

            // Try out the object first.
            if (preciselyMatches(object)) {
                return true;
            }

            // Try out the underlying resource.
            Class resourceClass = LegacyResourceSupport.getResourceClass();
            if (resourceClass == null) {
                return false;
            }

            if (resourceClass.isInstance(object)) {
                return false;
            }

            Object res = null;
            if (object instanceof IAdaptable) {
                res = ((IAdaptable) object).getAdapter(resourceClass);
            }
            if (res == null)
                return false;

            return preciselyMatches(res);

        }

        private boolean preciselyMatches(Object object) {
            // Get the action filter.
            IActionFilter filter = getActionFilter(object);
            if (filter == null)
                return false;

            // Run the action filter.
            return filter.testAttribute(object, name, value);
        }

        private IActionFilter getActionFilter(Object object) {
            IActionFilter filter = null;
            if (object instanceof IActionFilter)
                filter = (IActionFilter) object;
            else if (object instanceof IAdaptable)
                filter = (IActionFilter) ((IAdaptable) object)
                        .getAdapter(IActionFilter.class);
            return filter;
        }
        
       
        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.ActionExpression.AbstractExpression#valuesForExpression(java.lang.String)
         */
        public Collection valuesForExpression(String expressionType) {
        	if(expressionType.equals(name)){
        		Collection returnValue =  new HashSet();
        		returnValue.add(value);
        		return returnValue;
        	}
        	return null;
        }
       
    }

    private static class ObjectClassExpression extends AbstractExpression {
        private String className;
        private boolean extracted;

        /**
         * Creates and populates the expression from the attributes and sub-
         * elements of the configuration element.
         * @param element. The element that will be used to determine
         * the expressions for objectClass.
         * @throws IllegalStateException if the expression tag is not defined in the schema.
         * @see org.eclipse.ui\schema\commonExpression.mxsd
         */
        public ObjectClassExpression(IConfigurationElement element)
                throws IllegalStateException {
            super();

            className = element.getAttribute(ATT_NAME);
            if (className == null)
                throw new IllegalStateException(
                        "Object class expression missing name attribute"); //$NON-NLS-1$
        }

        /**
         * Create an ObjectClass expression based on the className.
         * Added for backwards compatibility.
         * @param className
         */
        public ObjectClassExpression(String className) {
            super();

            if (className != null)
                this.className = className;
            else
                throw new IllegalStateException(
                        "Object class expression must have class name"); //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * Method declared on AbstractExpression.
         */
        public boolean isEnabledFor(Object object) {
            if (object == null)
                return false;
            if (extracted)
                return true;

            Class clazz = object.getClass();
            while (clazz != null) {
                // test the class itself
                if (clazz.getName().equals(className))
                    return true;

                // test all the interfaces the class implements
                Class[] interfaces = clazz.getInterfaces();
                for (int i = 0; i < interfaces.length; i++) {
                    if (checkInterfaceHierarchy(interfaces[i]))
                        return true;
                }

                // get the superclass
                clazz = clazz.getSuperclass();
            }

            return false;
        }

        /**
         * Check the interfaces the whole way up. If one of them
         * matches className return <code>true</code>.
         * @param interfaceToCheck The interface whose name
         *  we are testing against.
         * @return <code>true</code> if one of the interfaces
         *  in the hierarchy matches className, <code>false</code>
         *  otherwise.
         */
        private boolean checkInterfaceHierarchy(Class interfaceToCheck) {
            if (interfaceToCheck.getName().equals(className))
                return true;
            Class[] superInterfaces = interfaceToCheck.getInterfaces();
            for (int i = 0; i < superInterfaces.length; i++) {
                if (checkInterfaceHierarchy(superInterfaces[i]))
                    return true;
            }
            return false;
        }

        /* (non-Javadoc)
         * Method declared on AbstractExpression.
         */
        public boolean isEnabledForExpression(Object object,
                String expressionType) {
            if (expressionType.equals(EXP_TYPE_OBJECT_CLASS))
                return isEnabledFor(object);
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.ui.internal.ActionExpression.AbstractExpression#extractObjectClass()
         */
        public String extractObjectClass() {
            extracted = true;
            return className;
        }
    }

    private static class PluginStateExpression extends AbstractExpression {
        private String id;

        private String value;

        /**
         * Creates and populates the expression from the attributes and sub-
         * elements of the configuration element.
         * @param element. The element that will be used to determine
         * the expressions for pluginState.
         * @throws IllegalStateException if the expression tag is not defined in the schema.
         * @see org.eclipse.ui\schema\commonExpression.mxsd
         */
        public PluginStateExpression(IConfigurationElement element)
                throws IllegalStateException {
            super();

            id = element.getAttribute(ATT_ID);
            value = element.getAttribute(ATT_VALUE);
            if (id == null || value == null)
                throw new IllegalStateException(
                        "Plugin state expression missing attribute"); //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * Method declared on AbstractExpression.
         */
        public boolean isEnabledFor(Object object) {
            Bundle bundle = Platform.getBundle(id);
            if (!BundleUtility.isReady(bundle))
                return false;
            if (value.equals(PLUGIN_INSTALLED))
                return true;
            if (value.equals(PLUGIN_ACTIVATED))
                return BundleUtility.isActivated(bundle);
            return false;
        }
    }

    private static class SystemPropertyExpression extends AbstractExpression {
        private String name;

        private String value;

        /**
         * Creates and populates the expression from the attributes and sub-
         * elements of the configuration element.
         * @param element. The element that will be used to determine
         * the expressions for systemProperty.
         * @throws IllegalStateException if the expression tag is not defined in the schema.
         * @see org.eclipse.ui\schema\commonExpression.mxsd
         */
        public SystemPropertyExpression(IConfigurationElement element)
                throws IllegalStateException {
            super();

            name = element.getAttribute(ATT_NAME);
            value = element.getAttribute(ATT_VALUE);
            if (name == null || value == null)
                throw new IllegalStateException(
                        "System property expression missing attribute"); //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * Method declared on AbstractExpression.
         */
        public boolean isEnabledFor(Object object) {
            String str = System.getProperty(name);
            if (str == null)
                return false;
            return value.equals(str);
        }
    }

    /**
     * Extract the object class test from the expression. This allows clients
     * (e.g. the decorator manager) to handle object class testing in a
     * more optimized way. This method removes the objectClass test from the 
     * expression and returns the object class. The expression is not changed and a 
     * <code>null</code> is returned if no object class is found.
     * @return the object class or <code>null</code> if none was found.
     */
    public String extractObjectClass() {
        return root.extractObjectClass();
    }
}
