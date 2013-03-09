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

package com.expressui.core.view;

import com.expressui.core.MainApplication;
import com.expressui.core.dao.GenericDao;
import com.expressui.core.entity.security.User;
import com.expressui.core.security.SecurityService;
import com.expressui.core.util.ApplicationProperties;
import com.expressui.core.util.BeanInvocationInterceptor;
import com.expressui.core.util.StringUtil;
import com.expressui.core.util.assertion.Assert;
import com.expressui.core.view.field.LabelRegistry;
import com.expressui.core.view.field.format.DefaultFormats;
import com.expressui.core.view.util.CodePopup;
import com.expressui.core.view.util.MessageSource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * Root of the view component hierarchy.
 */
public abstract class RootComponent extends CustomComponent implements ViewBean {

    /**
     * Application properties defined in application.properties
     */
    @Resource
    public ApplicationProperties applicationProperties;

    /**
     * Service for authenticating and getting the current user.
     */
    @Resource
    public SecurityService securityService;

    /**
     * Registry that keeps track of all display labels used in the application. Security system
     * uses this registry to link permissions to user-friendly names representing entity and UI components.
     */
    @Resource
    public LabelRegistry labelRegistry;

    /**
     * Defines some default formats, for dates, times, numbers, currency, etc.
     */
    @Resource
    public DefaultFormats defaultFormats;

    /**
     * Generic DAO for database actions.
     */
    @Resource
    public GenericDao genericDao;

    /**
     * Useful for displaying source code with sample applications, not for production use.
     */
    @Resource
    public CodePopup codePopup;

    /**
     * Provides messages (display labels) associated with domain-level entities.
     */
    @Resource
    public MessageSource domainMessageSource;

    /**
     * Provides messages (display labels) associated with UI elements.
     */
    @Resource
    public MessageSource uiMessageSource;

    /**
     * Provides validation error messages.
     */
    @Resource
    public MessageSource validationMessageSource;

    protected RootComponent() {
    }

    @PostConstruct
    @Override
    public void postConstruct() {
        autoAddStyleNames();

        AbstractOrderedLayout rootLayout = createOrderedLayout();
        setCompositionRoot(rootLayout);
        setWidthSizeFull();
    }

    @Override
    public void postWire() {
    }

    /**
     * Gets code for looking up messages associated with this component, using fully qualified class name.
     *
     * @return code
     */
    public String getCode() {
        return getClass().getName();
    }

    /**
     * Gets code for looking up messages associated with this component, using fully qualified class name plus code arg.
     *
     * @param code code to prepend class name
     * @return code fully qualified code prepended by class name
     */
    public String getCode(String code) {
        return getClass().getName() + "." + code;
    }

    /**
     * Gets domain message associated with this component, where the code consists only of the class name.
     *
     * @return internationalized domain message
     */
    public String getDomainMessage() {
        return domainMessageSource.getMessage(getCode());
    }

    /**
     * Gets domain message associated with this component and given code.
     *
     * @param code code to prepend class name
     * @return internationalized domain message
     */
    public String getDomainMessage(String code) {
        return domainMessageSource.getMessage(getCode(code));
    }

    /**
     * Gets domain message associated with this component, where the code consists only of the class name.
     *
     * @param args used to interpolate the message
     * @return internationalized domain message
     */
    public String getDomainMessage(Object... args) {
        return domainMessageSource.getMessage(getCode(), args);
    }

    /**
     * Gets domain message associated with this component and given code.
     *
     * @param code code to prepend class name
     * @param args used to interpolate the message
     * @return internationalized domain message
     */
    public String getDomainMessage(String code, Object... args) {
        return domainMessageSource.getMessage(getCode(code), args);
    }

    /**
     * Gets the main application, representing user's session.
     *
     * @return main application
     */
    public MainApplication getMainApplication() {
        return MainApplication.getInstance();
    }

    /**
     * Gets the user entity for the currently logged in user.
     *
     * @return user entity with roles and permissions
     */
    public User getCurrentUser() {
        return securityService.getCurrentUser();
    }

    public static <T> T newBeanRoot(Class<T> type) {
        return BeanInvocationInterceptor.newBeanRoot(type);
    }

    /**
     * Automatically adds style names (CSS classes) to all visual components. This is automatically called on
     * postConstruct. CSS class names are derived from Java class names, that is simple class name without package
     * qualifier. For any given component, CSS classes are generated for each Java class in the inheritance hierarchy
     * up to RootComponent.
     */
    protected void autoAddStyleNames() {
        List<String> styles = StringUtil.generateStyleNamesFromClassHierarchy("e", RootComponent.class, this);
        for (String style : styles) {
            addStyleName(style);
        }
    }

    /**
     * Sets the debug id for this component, useful for finding the generated HTML with a tool like Firebug.
     *
     * @param subComponent nested component
     * @param suffix       friendly suffix
     */
    public void setDebugId(AbstractComponent subComponent, String suffix) {
        String id = StringUtil.generateDebugId("e", this, subComponent, suffix);
        subComponent.setDebugId(id);
    }

    @Override
    public void addComponent(Component c) {
        Assert.PROGRAMMING.notNull(getCompositionRoot(), "Composition root must be set before this method can be called");
        ((ComponentContainer) getCompositionRoot()).addComponent(c);
    }

    /**
     * Set alignment on a child component.
     *
     * @param childComponent child component
     * @param alignment      alignment
     */
    public void setComponentAlignment(Component childComponent, Alignment alignment) {
        Assert.PROGRAMMING.notNull(getCompositionRoot(), "Composition root must be set before this method can be called");
        Assert.PROGRAMMING.isTrue(getCompositionRoot() instanceof AbstractOrderedLayout,
                "RootComponent.setComponentAlignment may only be called if using HorizontalLayout or VerticalLayout");
        ((AbstractOrderedLayout) getCompositionRoot()).setComponentAlignment(childComponent, alignment);
    }

    /**
     * Removes all child components.
     */
    public void removeAllComponents() {
        Assert.PROGRAMMING.notNull(getCompositionRoot(), "Composition root must be set before this method can be called");
        ((AbstractComponentContainer) getCompositionRoot()).removeAllComponents();
    }

    /**
     * Creates ordered layout (vertical or horizontal) for this component. Default is vertical. Override to change this.
     *
     * @return ordered layout
     */
    protected AbstractOrderedLayout createOrderedLayout() {
        return createVerticalLayout();
    }

    /**
     * Sets composition root of this component to a VerticalLayout with margins and spacing.
     */
    public VerticalLayout createVerticalLayout() {
        VerticalLayout rootLayout = new VerticalLayout();
        setDebugId(rootLayout, "rootLayout");
        rootLayout.setMargin(true);
        rootLayout.setSpacing(false);

        return rootLayout;
    }

    /**
     * Sets composition root of this component to a HorizontalLayout with margins and spacing.
     */
    public HorizontalLayout createHorizontalLayout() {
        HorizontalLayout rootLayout = new HorizontalLayout();
        setDebugId(rootLayout, "rootLayout");
        rootLayout.setMargin(true);
        rootLayout.setSpacing(false);

        return rootLayout;
    }

    /**
     * Sets width of this component to 100%.
     */
    public void setWidthSizeFull() {
        Assert.PROGRAMMING.notNull(getCompositionRoot(), "Composition root must be set before this method can be called");
        setWidth(100, Sizeable.UNITS_PERCENTAGE);
        getCompositionRoot().setWidth(100, Sizeable.UNITS_PERCENTAGE);
    }

    /**
     * Sets height of this component to 100%.
     */
    public void setHeightSizeFull() {
        Assert.PROGRAMMING.notNull(getCompositionRoot(), "Composition root must be set before this method can be called");
        setHeight(100, Sizeable.UNITS_PERCENTAGE);
        getCompositionRoot().setHeight(100, Sizeable.UNITS_PERCENTAGE);
    }

    /**
     * Sets both width and height of this component to 100%.
     */
    public void setSizeFull() {
        Assert.PROGRAMMING.notNull(getCompositionRoot(), "Composition root must be set before this method can be called");
        super.setSizeFull();
        getCompositionRoot().setSizeFull();
    }

    /**
     * Sets width size to undefined.
     */
    public void setWidthUndefined() {
        Assert.PROGRAMMING.notNull(getCompositionRoot(), "Composition root must be set before this method can be called");
        setWidth(-1, UNITS_PIXELS);
        getCompositionRoot().setWidth(-1, UNITS_PIXELS);
    }

    /**
     * Sets height size to undefined.
     */
    public void setHeightUndefined() {
        Assert.PROGRAMMING.notNull(getCompositionRoot(), "Composition root must be set before this method can be called");
        setHeight(-1, UNITS_PIXELS);
        getCompositionRoot().setHeight(-1, UNITS_PIXELS);
    }

    /**
     * Sets size to undefined.
     */
    public void setSizeUndefined() {
        Assert.PROGRAMMING.notNull(getCompositionRoot(), "Composition root must be set before this method can be called");
        super.setSizeUndefined();
        getCompositionRoot().setSizeUndefined();
    }

    /**
     * Adds code popup button next to this component to the right.
     *
     * @param classes classes for displaying related source code and Javadoc. If
     *                class is within com.expressui.core or com.expressui.domain,
     *                then Javadoc is displayed, otherwise source code.
     */
    protected void addCodePopupButtonIfEnabled(Class... classes) {
        addCodePopupButtonIfEnabled(Alignment.MIDDLE_LEFT, classes);
    }

    /**
     * Adds code popup button next to this component to the right.
     *
     * @param alignment alignment for button
     * @param classes   classes for displaying related source code and Javadoc
     */
    protected void addCodePopupButtonIfEnabled(Alignment alignment, Class... classes) {
        if (isCodePopupEnabled()) {
            Component firstComponent = getCompositionRoot();
            AbstractOrderedLayout codePopupButtonLayout = new HorizontalLayout();
            codePopupButtonLayout.setMargin(false);
            setDebugId(codePopupButtonLayout, "codePopupButtonLayout");
            setCompositionRoot(codePopupButtonLayout);
            codePopupButtonLayout.addComponent(firstComponent);
            Button codePopupButton = codePopup.createPopupCodeButton(autoAddCodeClasses(classes));
            codePopupButtonLayout.addComponent(codePopupButton);
            codePopupButtonLayout.setComponentAlignment(codePopupButton, alignment);
        }
    }

    protected Class[] autoAddCodeClasses(Class... classes) {
        Class[] allClasses = new Class[classes.length + 1];
        allClasses[0] = getClass();
        System.arraycopy(classes, 0, allClasses, 1, classes.length);

        return allClasses;
    }

    /**
     * Asks if code popups should be displayed. Only useful for demo applications.
     *
     * @return true if code popups should be displayed
     */
    public boolean isCodePopupEnabled() {
        return applicationProperties.isCodePopupEnabled();
    }
}
