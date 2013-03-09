/*
 * Copyright (c) 2012 Brown Bag Consulting.
 * This file is part of the ExpressUI project.
 * Author: Juan Osuna
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License Version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * Brown Bag Consulting, Brown Bag Consulting DISCLAIMS THE WARRANTY OF
 * NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the ExpressUI software without
 * disclosing the source code of your own applications. These activities
 * include: offering paid services to customers as an ASP, providing
 * services from a web application, shipping ExpressUI with a closed
 * source product.
 *
 * For more information, please contact Brown Bag Consulting at this
 * address: juan@brownbagconsulting.com.
 */

package com.expressui.core.util;

import com.expressui.core.util.assertion.Assert;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * Provides static reflection and contextual information about a bean property.
 * Represents type information about a bean property within a static tree structure.
 */
public class BeanPropertyType {
    private static Map<String, BeanPropertyType> cache = new HashMap<String, BeanPropertyType>();

    /**
     * Gets the BeanPropertyType instance from the given class and dot-delimited propertyPath.
     * Uses caching to speed-up repeated calls for the same property.
     *
     * @param clazz        root type
     * @param propertyPath dot-delimited property path starting from the root type
     * @return BeanPropertyType representing property within the type tree
     */
    public static BeanPropertyType getBeanPropertyType(Class clazz, String propertyPath) {
        String key = clazz.getName() + "." + propertyPath;
        if (!cache.containsKey(key)) {
            cache.put(key, getBeanPropertyTypeImpl(clazz, propertyPath));
        }

        return cache.get(key);
    }

    private static BeanPropertyType getBeanPropertyTypeImpl(Class clazz, String propertyPath) {
        String[] properties = StringUtils.split(propertyPath, "\\.");
        Class containingType;
        Class currentPropertyType = clazz;
        BeanPropertyType beanPropertyType = null;
        for (String property : properties) {
            Class propertyType = BeanUtils.findPropertyType(property, new Class[]{currentPropertyType});
            Assert.PROGRAMMING.isTrue(propertyType != null && !propertyType.equals(Object.class),
                    "Invalid property path: " + clazz + "." + propertyPath);

            Class propertyPathType;
            Class collectionValueType = null;
            if (Collection.class.isAssignableFrom(propertyType)) {
                collectionValueType = ReflectionUtil.getCollectionValueType(currentPropertyType, property);
                propertyPathType = collectionValueType;
            } else {
                propertyPathType = propertyType;
            }

            containingType = currentPropertyType;
            currentPropertyType = propertyPathType;

            beanPropertyType = new BeanPropertyType(beanPropertyType, property, propertyType, containingType,
                    collectionValueType);
        }

        return beanPropertyType;
    }

    private BeanPropertyType parent;
    private String id;
    private Class type;
    private Class containerType;
    private Class collectionValueType;
    private BusinessType businessType;
    private Set<Annotation> annotations = new HashSet<Annotation>();

    private BeanPropertyType(BeanPropertyType parent, String id, Class type, Class containerType, Class collectionValueType) {
        this.parent = parent;
        this.id = id;
        this.type = type;
        this.containerType = containerType;
        this.collectionValueType = collectionValueType;

        initAnnotations();
        businessType = createBusinessType();
    }

    private void initAnnotations() {
        initPropertyAnnotations();
        Class currentType = containerType;
        while (currentType != null) {
            boolean foundProperty = initFieldAnnotations(currentType);
            if (foundProperty) break;
            currentType = currentType.getSuperclass();
        }
    }

    private void initPropertyAnnotations() {
        PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(containerType, id);
        if (descriptor != null) {
            Method method = descriptor.getReadMethod();
            if (method != null) {
                Annotation[] readMethodAnnotations = method.getAnnotations();
                Collections.addAll(annotations, readMethodAnnotations);
            }
        }
    }

    private boolean initFieldAnnotations(Class containerType) {
        boolean foundProperty = false;

        Field field;
        try {
            field = containerType.getDeclaredField(id);
            foundProperty = true;
            Annotation[] fieldAnnotations = field.getAnnotations();
            Collections.addAll(annotations, fieldAnnotations);
        } catch (NoSuchFieldException e) {
            // no need to get annotations if field doesn't exist
        }

        return foundProperty;
    }

    /**
     * Asks if a property has a given annotation.
     *
     * @param annotationClass annotation to check
     * @return true if bean property has the annotation
     */
    public boolean hasAnnotation(Class annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotationClass.isAssignableFrom(annotation.getClass())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the instance of given annotation type from bean property.
     *
     * @param annotationClass annotation to find on the property
     * @param <T>             annotation type
     * @return annotation instance
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        for (Annotation annotation : annotations) {
            if (annotationClass.isAssignableFrom(annotation.getClass())) {
                return (T) annotation;
            }
        }

        return null;
    }

    /**
     * Gets minimum length from validation annotations.
     *
     * @return minimum length for the property
     */
    public Integer getMinimumLength() {
        return MathUtil.minDisallowNull(getMinimumLengthImpl(), getMaximumLengthImpl());
    }

    /**
     * Gets maximum length from validation annotations.
     *
     * @return maximum length for the property
     */
    public Integer getMaximumLength() {
        return MathUtil.maxDisallowNull(getMinimumLengthImpl(), getMaximumLengthImpl());
    }

    private Integer getMinimumLengthImpl() {
        Integer min = null;

        for (Annotation annotation : annotations) {
            if (Size.class.isAssignableFrom(annotation.getClass())) {
                Size size = (Size) annotation;
                min = MathUtil.minIgnoreNull(min, size.min());
            } else if (Min.class.isAssignableFrom(annotation.getClass())) {
                Min m = (Min) annotation;
                min = MathUtil.minIgnoreNull(min, String.valueOf(m.value()).length());
            } else if (Digits.class.isAssignableFrom(annotation.getClass())) {
                Digits digits = (Digits) annotation;
                min = MathUtil.minIgnoreNull(min, digits.integer() + digits.fraction() + 1);
            } else if (DecimalMin.class.isAssignableFrom(annotation.getClass())) {
                DecimalMin m = (DecimalMin) annotation;
                min = MathUtil.minIgnoreNull(min, String.valueOf(m.value()).length());
            } else if (DecimalMax.class.isAssignableFrom(annotation.getClass())) {
                DecimalMax m = (DecimalMax) annotation;
                min = MathUtil.minIgnoreNull(min, String.valueOf(m.value()).length());
            }
        }

        return min;
    }

    private Integer getMaximumLengthImpl() {
        Integer max = null;

        for (Annotation annotation : annotations) {
            if (Size.class.isAssignableFrom(annotation.getClass())) {
                Size size = (Size) annotation;
                max = MathUtil.maxIgnoreNull(max, size.max());
            } else if (Max.class.isAssignableFrom(annotation.getClass())) {
                Max m = (Max) annotation;
                max = MathUtil.maxIgnoreNull(max, String.valueOf(m.value()).length());
            } else if (Digits.class.isAssignableFrom(annotation.getClass())) {
                Digits digits = (Digits) annotation;
                max = MathUtil.maxIgnoreNull(max, digits.integer() + digits.fraction() + 1);
            } else if (DecimalMin.class.isAssignableFrom(annotation.getClass())) {
                DecimalMin m = (DecimalMin) annotation;
                max = MathUtil.maxIgnoreNull(max, String.valueOf(m.value()).length());
            } else if (DecimalMax.class.isAssignableFrom(annotation.getClass())) {
                DecimalMax m = (DecimalMax) annotation;
                max = MathUtil.maxIgnoreNull(max, String.valueOf(m.value()).length());
            }
        }

        return max;
    }

    /**
     * Gets the parent property of this nested property within the type tree.
     *
     * @return parent property
     */
    public BeanPropertyType getParent() {
        return parent;
    }

    /**
     * Gets the list of ancestors of this property type, starting with the root and ending with this property type.
     *
     * @return list of property type ancestors
     */
    public List<BeanPropertyType> getAncestors() {
        List<BeanPropertyType> ancestors = new ArrayList<BeanPropertyType>();
        BeanPropertyType currentBeanPropertyType = this;
        while (currentBeanPropertyType != null) {
            ancestors.add(currentBeanPropertyType);
            currentBeanPropertyType = currentBeanPropertyType.getParent();
        }

        Collections.reverse(ancestors);

        return ancestors;
    }

    /**
     * Gets the bean property name with its full path.
     *
     * @return bean property name
     */
    public String getId() {
        return id;
    }

    /**
     * Gets only the leaf part of the bean property, that is after the last period.
     *
     * @return leaf part of the bean property
     */
    public String getLeafId() {
        return StringUtil.extractAfterPeriod(id);
    }

    /**
     * Gets the class type, declared for this property.
     *
     * @return declared class type
     */
    public Class getType() {
        return type;
    }

    /**
     * Gets type from a more abstract business standpoint.
     *
     * @return business type
     */
    public BusinessType getBusinessType() {
        return businessType;
    }

    private BusinessType createBusinessType() {
        if (getType() == Date.class) {
            Temporal temporal = getAnnotation(Temporal.class);
            if (temporal != null && temporal.value().equals(TemporalType.DATE)) {
                return BusinessType.DATE;
            } else {
                return BusinessType.DATE_TIME;
            }
        }
        if (ReflectionUtil.isNumberType(getType())) {
            return BusinessType.NUMBER;
        }
        if (String.class.isAssignableFrom(getType())) {
            return BusinessType.TEXT;
        }

        if (BigDecimal.class.isAssignableFrom(getType())) {
            return BusinessType.MONEY;
        }

        return null;
    }

    /**
     * Gets the class that contains this property.
     *
     * @return class of property container
     */
    public Class getContainerType() {
        return containerType;
    }

    /**
     * If this property is a collection, get the generically declared type of its members.
     *
     * @return type of collection values, as specified by declared generic parameter
     */
    public Class getCollectionValueType() {
        return collectionValueType;
    }

    /**
     * Asks if this property is a collection.
     *
     * @return true if property is a collection type
     */
    public boolean isCollectionType() {
        return Collection.class.isAssignableFrom(type);
    }

    /**
     * Asks if this property is validatable recursively because it has a @Valid annotation.
     *
     * @return true if this property is validatable recursively because it has a @Valid annotation
     */
    public boolean isValidatable() {
        BeanPropertyType beanPropertyType = parent;
        while (beanPropertyType != null) {
            Class containingType = beanPropertyType.getContainerType();
            String id = beanPropertyType.getId();

            PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(containingType, id);
            Method readMethod = descriptor.getReadMethod();
            Valid validAnnotation = null;
            if (readMethod != null) {
                validAnnotation = readMethod.getAnnotation(Valid.class);
            }
            if (validAnnotation == null) {
                Field field = ReflectionUtil.getField(containingType, id);
                Assert.PROGRAMMING.notNull(field, "Cannot find field: " + containingType.getName() + "." + id);

                validAnnotation = field.getAnnotation(Valid.class);
            }

            if (validAnnotation == null) {
                return false;
            } else {
                beanPropertyType = beanPropertyType.getParent();
            }
        }

        return true;
    }

    /**
     * Gets the root type of the type tree where this nested property resides.
     *
     * @return root of the type tree
     */
    public BeanPropertyType getRoot() {
        if (parent == null) {
            return this;
        } else {
            return getParent().getRoot();
        }
    }

    /**
     * Simplified type from a business/end-user perspective
     */
    public static enum BusinessType {
        /**
         * Any kind of integer or decimal number.
         */
        NUMBER,
        /**
         * Date without time.
         */
        DATE,
        /**
         * Date and time.
         */
        DATE_TIME,
        /**
         * BigDecimal that commonly used for money.
         */
        MONEY,
        /**
         * Straight text.
         */
        TEXT
    }
}