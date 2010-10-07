/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - adjustment to EObject
 ******************************************************************************/
package org.eclipse.e4.emf.internal.xpath;

import java.util.Locale;

import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.eclipse.e4.emf.internal.xpath.helper.JXPathEObjectInfo;

/**
 * A Pointer that points to a JavaBean or a collection. It is either
 * the first element of a path or a pointer for a property value.
 * Typically there is a {@link EObjectPropertyPointer} between two EObjectPointers
 * in the chain.
 *
 */
public class EObjectPointer extends EStructuralFeatureOwnerPointer {
    private QName name;
    private Object bean;
    private JXPathEObjectInfo beanInfo;

    private static final long serialVersionUID = -8227317938284982440L;

    /**
     * Create a new BeanPointer.
     * @param name is the name given to the first node
     * @param bean pointed
     * @param beanInfo JXPathBeanInfo
     * @param locale Locale
     */
    public EObjectPointer(QName name, Object bean, JXPathEObjectInfo beanInfo,
            Locale locale) {
        super(null, locale);
        this.name = name;
        this.bean = bean;
        this.beanInfo = beanInfo;
    }

    /**
     * Create a new BeanPointer.
     * @param parent pointer
     * @param name is the name given to the first node
     * @param bean pointed
     * @param beanInfo JXPathBeanInfo
     */
    public EObjectPointer(NodePointer parent, QName name, Object bean,
    		JXPathEObjectInfo beanInfo) {
        super(parent);
        this.name = name;
        this.bean = bean;
        this.beanInfo = beanInfo;
    }

    public EStructuralFeaturePointer getPropertyPointer() {
        return new EObjectPropertyPointer(this, beanInfo);
    }

    public QName getName() {
        return name;
    }

    public Object getBaseValue() {
        return bean;
    }

    /**
     * {@inheritDoc}
     * @return false
     */
    public boolean isCollection() {
        return false;
    }

    /**
     * {@inheritDoc}
     * @return 1
     */
    public int getLength() {
        return 1;
    }

    public boolean isLeaf() {
        Object value = getNode();
        return value == null
            || JXPathIntrospector.getBeanInfo(value.getClass()).isAtomic();
    }

    public int hashCode() {
        return name == null ? 0 : name.hashCode();
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof EObjectPointer)) {
            return false;
        }

        EObjectPointer other = (EObjectPointer) object;
        if (parent != other.parent && (parent == null || !parent.equals(other.parent))) {
            return false;
        }

        if ((name == null && other.name != null)
                || (name != null && !name.equals(other.name))) {
            return false;
        }

        int iThis = (index == WHOLE_COLLECTION ? 0 : index);
        int iOther = (other.index == WHOLE_COLLECTION ? 0 : other.index);
        if (iThis != iOther) {
            return false;
        }

        if (bean instanceof Number
                || bean instanceof String
                || bean instanceof Boolean) {
            return bean.equals(other.bean);
        }
        return bean == other.bean;
    }

    /**
     * {@inheritDoc}
     * If the pointer has a parent, then parent's path.
     * If the bean is null, "null()".
     * If the bean is a primitive value, the value itself.
     * Otherwise - an empty string.
     */
    public String asPath() {
        if (parent != null) {
            return super.asPath();
        }
        if (bean == null) {
            return "null()";
        }
        if (bean instanceof Number) {
            String string = bean.toString();
            if (string.endsWith(".0")) {
                string = string.substring(0, string.length() - 2);
            }
            return string;
        }
        if (bean instanceof Boolean) {
            return ((Boolean) bean).booleanValue() ? "true()" : "false()";
        }
        if (bean instanceof String) {
            return "'" + bean + "'";
        }
        return "/";
    }
}
