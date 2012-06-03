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

package com.expressui.core.view.form;

import com.expressui.core.entity.WritableEntity;
import com.expressui.core.entity.security.User;
import com.expressui.core.util.MethodDelegate;
import com.expressui.core.validation.AssertTrueForProperties;
import com.expressui.core.validation.Validation;
import com.expressui.core.view.field.FormField;
import com.expressui.core.view.field.SelectField;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.util.BeanItem;
import com.vaadin.terminal.ErrorMessage;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.UserError;
import com.vaadin.ui.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.metadata.ConstraintDescriptor;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * A form bound to a JPA entity, providing save, refresh and cancel actions. Underneath the form
 * are tabs representing to-many relationships, allowing related entities to be added and removed.
 *
 * @param <T> type of entity
 */
public abstract class EntityForm<T extends WritableEntity> extends TypedForm<T> {

    @Resource
    private Validation validation;

    private boolean isViewMode;

    private TabSheet toManyRelationshipTabs;

    private Button cancelButton;
    private Button refreshButton;
    private Button saveAndCloseButton;
    private Button saveAndStayOpenButton;
    private boolean isValidationEnabled = true;

    private com.vaadin.terminal.Resource saveAndCloseButtonIconBackup;
    private com.vaadin.terminal.Resource saveAndStayOpenButtonIconBackup;

    private Set<MethodDelegate> closeListeners = new LinkedHashSet<MethodDelegate>();
    private Set<MethodDelegate> cancelListeners = new LinkedHashSet<MethodDelegate>();
    private Set<MethodDelegate> saveListeners = new LinkedHashSet<MethodDelegate>();

    /**
     * Get all to-many relationships, displayed as tabs below the form.
     *
     * @return list of to-many relationships
     */
    public List<ToManyRelationship> getToManyRelationships() {
        return new ArrayList<ToManyRelationship>();
    }

    /**
     * Get all viewable to-many relationships, based on security permissions.
     *
     * @return all viewable to-many relationships
     */
    public List<ToManyRelationship> getViewableToManyRelationships() {
        List<ToManyRelationship> viewableToManyRelationships = new ArrayList<ToManyRelationship>();
        List<ToManyRelationship> toManyRelationships = getToManyRelationships();

        for (ToManyRelationship toManyRelationship : toManyRelationships) {
            User user = securityService.getCurrentUser();

            if (user.isViewAllowed(toManyRelationship.getResults().getType().getName())
                    && !toManyRelationship.getResults().getResultsFieldSet().getViewablePropertyIds().isEmpty()) {
                viewableToManyRelationships.add(toManyRelationship);
            }
        }

        return viewableToManyRelationships;
    }

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        List<ToManyRelationship> toManyRelationships = getViewableToManyRelationships();
        if (toManyRelationships.size() > 0) {
            toManyRelationshipTabs = new TabSheet();
            setDebugId(toManyRelationshipTabs, "toManyRelationshipTabs");
            toManyRelationshipTabs.setSizeUndefined();
            for (ToManyRelationship toManyRelationship : toManyRelationships) {
                toManyRelationshipTabs.addTab(toManyRelationship);
                labelRegistry.putFieldLabel(getType().getName(), toManyRelationship.getResults().getChildPropertyId(),
                        "Relationship", toManyRelationship.getTypeCaption());
            }

            HorizontalLayout toManyRelationshipLayout = new HorizontalLayout();
            setDebugId(toManyRelationshipLayout, "toManyRelationshipLayout");
            toManyRelationshipLayout.setSizeUndefined();
            Label label = new Label("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;", Label.CONTENT_XHTML);
            toManyRelationshipLayout.addComponent(label);
            toManyRelationshipLayout.addComponent(toManyRelationshipTabs);
            addComponent(toManyRelationshipLayout);
        }

        addCodePopupButtonIfEnabled(Alignment.MIDDLE_RIGHT, EntityForm.class);
    }

    @Override
    public void postWire() {
        super.postWire();

        List<ToManyRelationship> toManyRelationships = getViewableToManyRelationships();
        for (ToManyRelationship toManyRelationship : toManyRelationships) {
            toManyRelationship.postWire();
        }

        Collection<FormField> formFields = getFormFieldSet().getFormFields();
        for (FormField formField : formFields) {
            Field field = formField.getField();
            if (field instanceof SelectField) {
                ((SelectField) field).getEntitySelect().postWire();
            }
        }
    }

    @Override
    public void onDisplay() {
        List<ToManyRelationship> toManyRelationships = getToManyRelationships();
        for (ToManyRelationship toManyRelationship : toManyRelationships) {
            toManyRelationship.onDisplay();
        }

        setFormVisible(true);
    }

    /**
     * Animate the component if and only if there are viewable to-many tabs, i.e. allow component's
     * visibility to be toggled
     *
     * @param component component to show/hide
     * @return the newly created layout that contains the toggle button and animated component
     */
    @Override
    protected Component animate(Component component) {
        if (getViewableToManyRelationships().size() > 0) {
            return super.animate(component);
        } else {
            return component;
        }
    }

    /**
     * Create the footer buttons: cancel, refresh, save
     *
     * @param footerLayout horizontal layout containing buttons
     */
    @Override
    protected void createFooterButtons(HorizontalLayout footerLayout) {
        footerLayout.setSpacing(true);
        footerLayout.setMargin(true);

        cancelButton = new Button(uiMessageSource.getMessage("entityForm.cancel"), this, "cancel");
        cancelButton.setDescription(uiMessageSource.getMessage("entityForm.cancel.description"));
        cancelButton.setIcon(new ThemeResource("../expressui/icons/16/cancel.png"));
        cancelButton.addStyleName("small default");
        footerLayout.addComponent(cancelButton);

        refreshButton = new Button(uiMessageSource.getMessage("entityForm.refresh"), this, "refresh");
        refreshButton.setDescription(uiMessageSource.getMessage("entityForm.refresh.description"));
        refreshButton.setIcon(new ThemeResource("../expressui/icons/16/refresh.png"));
        refreshButton.addStyleName("small default");
        footerLayout.addComponent(refreshButton);

        saveAndStayOpenButton = new Button(uiMessageSource.getMessage("entityForm.saveAndStayOpen"), this, "saveAndStayOpen");
        saveAndStayOpenButton.setDescription(uiMessageSource.getMessage("entityForm.save.description"));
        saveAndStayOpenButton.setIcon(new ThemeResource("../expressui/icons/16/save.png"));
        saveAndStayOpenButton.addStyleName("small default");
        footerLayout.addComponent(saveAndStayOpenButton);

        saveAndCloseButton = new Button(uiMessageSource.getMessage("entityForm.saveAndClose"), this, "saveAndClose");
        saveAndCloseButton.setDescription(uiMessageSource.getMessage("entityForm.save.description"));
        saveAndCloseButton.setIcon(new ThemeResource("../expressui/icons/16/save.png"));
        saveAndCloseButton.addStyleName("small default");
        footerLayout.addComponent(saveAndCloseButton);

        backupSaveButtonIcons();
    }

    /**
     * Ask if this form is in read/view-only mode.
     *
     * @return true if in view-only mode
     */
    public boolean isViewMode() {
        return isViewMode;
    }

    /**
     * Set whether or not this form is in read/view-only mode. Note that this action does not immediately change
     * fields to read-only or restore them to writable. It just sets the mode for the next time
     * an entity is loaded or applyViewMode is called.
     *
     * @param viewMode true if in view-only mode
     */
    public void setViewMode(boolean viewMode) {
        isViewMode = viewMode;
        List<ToManyRelationship> toManyRelationships = getToManyRelationships();
        for (ToManyRelationship toManyRelationship : toManyRelationships) {
            toManyRelationship.getResults().setViewMode(viewMode);
        }
    }

    /**
     * Apply view mode to form.
     */
    public void applyViewMode() {
        if (isViewMode()) {
            setReadOnly(true);
        } else {
            applySecurityIsEditable();
        }
    }

    /**
     * Set entire form to read-only or writable, including fields, to-many relationships and action buttons
     *
     * @param isReadOnly true to set to read-only, otherwise make writable
     */
    @Override
    public void setReadOnly(boolean isReadOnly) {
        super.setReadOnly(isReadOnly);
        getFormFieldSet().setReadOnly(isReadOnly);

        saveAndCloseButton.setVisible(!isReadOnly);
        saveAndStayOpenButton.setVisible(!isReadOnly);
        refreshButton.setVisible(!isReadOnly);

        List<ToManyRelationship> toManyRelationships = getToManyRelationships();
        for (ToManyRelationship toManyRelationship : toManyRelationships) {
            toManyRelationship.getResults().setReadOnly(isReadOnly);
        }
    }

    /**
     * Restore the read-only settings of the form fields to how they were originally configured.
     */
    public void restoreIsReadOnly() {
        getFormFieldSet().restoreIsReadOnly();

        saveAndCloseButton.setVisible(true);
        saveAndStayOpenButton.setVisible(true);
        refreshButton.setVisible(true);

        List<ToManyRelationship> toManyRelationships = getToManyRelationships();
        for (ToManyRelationship toManyRelationship : toManyRelationships) {
            toManyRelationship.getResults().setReadOnly(false);
        }
    }

    /**
     * Apply security permission logic to fields for controlling if each field is editable
     */
    public void applySecurityIsEditable() {
        saveAndCloseButton.setVisible(true);
        saveAndStayOpenButton.setVisible(true);
        refreshButton.setVisible(true);
        getFormFieldSet().applySecurityIsEditable();

        List<ToManyRelationship> toManyRelationships = getToManyRelationships();
        for (ToManyRelationship toManyRelationship : toManyRelationships) {
            toManyRelationship.getResults().applySecurityIsEditable();
        }
    }

    /**
     * Ask if the current entity bound to this form is persistent, i.e. has a primary key assigned
     *
     * @return true if entity has primary key
     */
    public boolean isEntityPersistent() {
        return genericDao.isPersistent(getEntity());
    }

    /**
     * Ask if validation is currently enabled for this form. Sometimes it is useful to temporarily turn off
     * validation, e.g. when setting a new data-source
     *
     * @return true if validation is currently enabled
     */
    public boolean isValidationEnabled() {
        return isValidationEnabled;
    }

    private void setItemDataSource(Item newDataSource, Collection<?> propertyIds) {
        isValidationEnabled = false;
        getForm().setItemDataSource(newDataSource, propertyIds);
        isValidationEnabled = true;
    }

    /**
     * Load and bind a new entity to the form. Automatically selects the first tab (if tabs exist), whenever
     * a new entity is loaded.
     *
     * @param entity entity to load
     */
    public void load(T entity) {
        load(entity, true);
    }

    /**
     * Load and bind a new entity to the form.
     *
     * @param entity         entity to load
     * @param selectFirstTab true to select the first tab, once the entity is loaded
     */
    public void load(T entity, boolean selectFirstTab) {
        T loadedEntity = (T) genericDao.find(getType(), entity.getId());
        postLoad(loadedEntity);
        BeanItem beanItem = createBeanItem(loadedEntity);
        setItemDataSource(beanItem, getFormFieldSet().getPropertyIds());
        getFormFieldSet().autoAdjustWidths();

        validate(true);

        loadToManyRelationships();
        resetTabs(selectFirstTab);

        refreshButton.setCaption(uiMessageSource.getMessage("entityForm.refresh"));
    }

    @Override
    protected BeanItem createBeanItem(Object entity) {
        isValidationEnabled = false;
        BeanItem beanItem = super.createBeanItem(entity);
        isValidationEnabled = true;

        return beanItem;
    }

    private void loadToManyRelationships() {
        List<ToManyRelationship> toManyRelationships = getViewableToManyRelationships();
        if (toManyRelationships.size() > 0) {
            for (ToManyRelationship toManyRelationship : toManyRelationships) {
                Object parent = getEntity();
                toManyRelationship.getResults().getEntityQuery().clear();
                toManyRelationship.getResults().getEntityQuery().setParent(parent);
                toManyRelationship.getResults().search();
                toManyRelationship.getResults().selectionChanged();

            }
            toManyRelationshipTabs.setVisible(true);
            setFormAnimatorToggleButtonVisible(true);
        }
    }

    /**
     * Clear the form of data and all errors.
     */
    public void clear() {
        clearAllErrors(true);
        setItemDataSource(null, getFormFieldSet().getPropertyIds());
    }

    /**
     * Create an empty form with an empty entity. Makes to-many relationship tabs invisible. User must
     * save an entity and reload to add to-many relationships.
     */
    public void create() {
        createImpl();

        if (toManyRelationshipTabs != null) {
            toManyRelationshipTabs.setVisible(false);
            setFormAnimatorToggleButtonVisible(false);
        }
    }

    private void createImpl() {
        T newEntity = createEntity();
        postCreate(newEntity);
        BeanItem beanItem = createBeanItem(newEntity);
        setItemDataSource(beanItem, getFormFieldSet().getPropertyIds());

        validate(true);

        resetTabs();

        refreshButton.setCaption(uiMessageSource.getMessage("entityForm.clear"));
    }

    private T createEntity() {
        if (getEntityDao() == null) {
            return (T) genericDao.create(getType());
        } else {
            return getEntityDao().create();
        }
    }

    private void resetTabs() {
        resetTabs(true);
    }

    private void resetTabs(boolean selectFirstTab) {

        if (selectFirstTab) {
            selectFirstToManyTab();
        }

        if (!hasTabs()) return;

        Set<String> viewableTabNames = getFormFieldSet().getViewableTabNames();
        Set<String> tabNames = getFormFieldSet().getTabNames();
        for (String tabName : tabNames) {
            TabSheet.Tab tab = getTabByName(tabName);

            Set<FormField> fields = getFormFieldSet().getFormFields(tabName);

            if (getFormFieldSet().isTabOptional(tabName)) {
                boolean isTabEmpty = true;
                for (FormField field : fields) {
                    if (field.getField().getValue() != null) {
                        isTabEmpty = false;
                        break;
                    }
                }

                setIsRequiredEnable(tabName, !isTabEmpty);
                tab.setClosable(!isViewMode());
                tab.setVisible(!isTabEmpty && viewableTabNames.contains(tabName));
            } else {
                tab.setVisible(viewableTabNames.contains(tabName));
            }
        }

        resetContextMenu();

        if (selectFirstTab || !getTabByName(getCurrentlySelectedTabName()).isVisible()) {
            selectFirstTab();
        }

        syncTabAndSaveButtonErrors();
    }

    private void selectFirstToManyTab() {
        if (toManyRelationshipTabs != null) {
            toManyRelationshipTabs.setSelectedTab(toManyRelationshipTabs.getTab(0).getComponent());
        }
    }

    @Override
    protected void resetContextMenu() {
        if (getFormFieldSet().hasOptionalTabs()) {
            Set<String> tabNames = getFormFieldSet().getViewableTabNames();
            for (String tabName : tabNames) {
                TabSheet.Tab tab = getTabByName(tabName);

                String caption = uiMessageSource.getMessage("typedForm.add") + " " + tabName;
                if (menu.containsItem(caption)) {
                    menu.getContextMenuItem(caption).setVisible(!tab.isVisible() && !isViewMode());
                }
                caption = uiMessageSource.getMessage("typedForm.remove") + " " + tabName;
                if (menu.containsItem(caption)) {
                    menu.getContextMenuItem(caption).setVisible(tab.isVisible() && !isViewMode());
                }
            }
        }
    }

    /**
     * Add cancel listener.
     *
     * @param target     target object
     * @param methodName name of method to invoke
     */
    public void addCancelListener(Object target, String methodName) {
        cancelListeners.add(new MethodDelegate(target, methodName));
    }

    /**
     * Add close listener. Listener is invoked when form is closed.
     *
     * @param target     target object
     * @param methodName name of method to invoke
     */
    public void addCloseListener(Object target, String methodName) {
        closeListeners.add(new MethodDelegate(target, methodName));
    }

    /**
     * Add save listener. Listener is invoked when form is saved.
     *
     * @param target     target object
     * @param methodName name of method to invoke
     */
    public void addSaveListener(Object target, String methodName) {
        saveListeners.add(new MethodDelegate(target, methodName));
    }

    public void removeListeners(Object target) {
        Set<MethodDelegate> listenersToRemove;

        listenersToRemove = new HashSet<MethodDelegate>();
        for (MethodDelegate closeListener : closeListeners) {
            if (closeListener.getTarget().equals(target)) {
                listenersToRemove.add(closeListener);
            }
        }
        for (MethodDelegate listener : listenersToRemove) {
            closeListeners.remove(listener);
        }

        listenersToRemove = new HashSet<MethodDelegate>();
        for (MethodDelegate cancelListener : cancelListeners) {
            if (cancelListener.getTarget().equals(target)) {
                listenersToRemove.add(cancelListener);
            }
        }
        for (MethodDelegate listener : listenersToRemove) {
            cancelListeners.remove(listener);
        }

        listenersToRemove = new HashSet<MethodDelegate>();
        for (MethodDelegate saveListener : saveListeners) {
            if (saveListener.getTarget().equals(target)) {
                listenersToRemove.add(saveListener);
            }
        }
        for (MethodDelegate listener : listenersToRemove) {
            saveListeners.remove(listener);
        }
    }

    /**
     * Cancel and close the form, discarding any changes.
     */
    public void cancel() {
        clearAllErrors(true);
        getForm().discard();
        BeanItem beanItem = (BeanItem) getForm().getItemDataSource();
        if (beanItem == null) {
            clear();
        } else {
            T entity = (T) beanItem.getBean();
            if (entity.getId() == null) {
                create();
            } else {
                load(entity);
            }
        }

        for (MethodDelegate cancelListener : cancelListeners) {
            cancelListener.execute();
        }
    }

    /**
     * Save changes to the entity, either persisting a transient entity or updating existing one, then close form.
     */
    public void saveAndClose() {
        save(true);
    }

    /**
     * Save changes to the entity, either persisting a transient entity or updating existing one, while
     * keeping form open.
     */
    public void saveAndStayOpen() {
        save(false);
    }

    /**
     * Save changes to the entity, either persisting a transient entity or updating existing one.
     *
     * @param executeCloseListeners whether or not to execute close listeners
     */
    public void save(boolean executeCloseListeners) {
        boolean isValid = validate(false);
        if (getForm().isValid() && isValid) {
            getForm().commit();

            preSave(getEntity());

            T entity = getEntity();
            if (entity.getId() != null) {
                T mergedEntity;
                if (getEntityDao() == null) {
                    mergedEntity = genericDao.merge(entity);
                } else {
                    mergedEntity = getEntityDao().merge(entity);
                }

                postSave(mergedEntity);
                load(mergedEntity, false);
            } else {
                if (getEntityDao() == null) {
                    genericDao.persist(entity);
                } else {
                    getEntityDao().persist(entity);
                }
                postSave(entity);
                load(entity, false);

                if (!executeCloseListeners) {
                    loadToManyRelationships();
                }
            }

            showSaveSuccessfulMessage();

            for (MethodDelegate saveListener : saveListeners) {
                saveListener.execute();
            }

            if (executeCloseListeners) {
                for (MethodDelegate closeListener : closeListeners) {
                    closeListener.execute();
                }
            }
        } else {
            showSaveUnsuccessfulMessage();
        }
    }

    /**
     * Show notification message that save was successful.
     */
    public void showSaveSuccessfulMessage() {
        Window.Notification notification = new Window.Notification("\"" + getTypeCaption()
                + "\" " + uiMessageSource.getMessage("entityForm.saved"),
                Window.Notification.TYPE_HUMANIZED_MESSAGE);
        notification.setDelayMsec(Window.Notification.DELAY_NONE);
        notification.setPosition(Window.Notification.POSITION_CENTERED);
        getMainApplication().showNotification(notification);
    }

    /**
     * Show notification message that save was unsuccessful.
     */
    public void showSaveUnsuccessfulMessage() {
        Window.Notification notification = new Window.Notification("\"" + getTypeCaption()
                + "\" " + uiMessageSource.getMessage("entityForm.notSaved"),
                Window.Notification.TYPE_ERROR_MESSAGE);
        notification.setDelayMsec(Window.Notification.DELAY_NONE);
        notification.setPosition(Window.Notification.POSITION_CENTERED);
        getMainApplication().showNotification(notification);
    }

    /**
     * Reload entity from the database, discarding any changes and clearing any errors.
     */
    public void refresh() {
        clearAllErrors(true);
        BeanItem beanItem = (BeanItem) getForm().getItemDataSource();
        if (beanItem == null) {
            createImpl();
        } else {
            T entity = (T) beanItem.getBean();
            if (entity.getId() == null) {
                createImpl();
            } else {
                getForm().discard();
                load(entity, false);
            }
        }
    }

    @Override
    void executeContextAction(String name) {
        super.executeContextAction(name);
        validate(false);
    }

    /**
     * Validate this form by validating the bound JPA entity, annotated with JSR 303 annotations.
     *
     * @param clearConversionErrors true if any existing type-conversion errors should be cleared
     * @return true if no validation errors were found
     */
    public boolean validate(boolean clearConversionErrors) {
        WritableEntity entity = getEntity();

        clearAllErrors(clearConversionErrors);

        Set<ConstraintViolation<WritableEntity>> constraintViolations = validation.validate(entity);
        for (ConstraintViolation constraintViolation : constraintViolations) {
            String propertyPath = constraintViolation.getPropertyPath().toString();

            ConstraintDescriptor descriptor = constraintViolation.getConstraintDescriptor();
            Annotation annotation = descriptor.getAnnotation();

            if (propertyPath.isEmpty()) {
                Validator.InvalidValueException error = new Validator.InvalidValueException(constraintViolation.getMessage());
                getForm().setComponentError(error);
            } else {
                FormField field;
                if (annotation instanceof AssertTrueForProperties) {
                    if (propertyPath.lastIndexOf(".") > 0) {
                        propertyPath = propertyPath.substring(0, propertyPath.lastIndexOf(".") + 1);
                    } else {
                        propertyPath = "";
                    }
                    AssertTrueForProperties assertTrueForProperties = (AssertTrueForProperties) annotation;
                    propertyPath += assertTrueForProperties.errorProperty();
                }
                if (getFormFieldSet().containsPropertyId(propertyPath)) {
                    field = getFormFieldSet().getFormField(propertyPath);
                    if (!field.hasIsRequiredError()) {
                        Validator.InvalidValueException error = new Validator.InvalidValueException(constraintViolation.getMessage());
                        field.addError(error);
                    }
                }
            }
        }

        syncTabAndSaveButtonErrors();

        return constraintViolations.isEmpty();
    }

    private void clearAllErrors(boolean clearConversionErrors) {
        getFormFieldSet().clearErrors(clearConversionErrors);
        getForm().setComponentError(null);
        saveAndCloseButton.setComponentError(null);
        saveAndStayOpenButton.setComponentError(null);

        Set<String> tabNames = getFormFieldSet().getViewableTabNames();
        for (String tabName : tabNames) {
            setTabError(tabName, null);
        }
    }

    private void setTabError(String tabName, ErrorMessage error) {
        TabSheet.Tab tab = getTabByName(tabName);
        if (tab != null) {
            tab.setComponentError(error);
        }
    }

    /**
     * Reset validation error indicators on tabs and save buttons, according to whether
     * any validation errors exist in any fields.
     */
    public void syncTabAndSaveButtonErrors() {
        Set<String> tabNames = getFormFieldSet().getViewableTabNames();
        boolean formHasErrors = false;
        for (String tabName : tabNames) {
            if (getFormFieldSet().hasError(tabName)) {
                setTabError(tabName, new UserError("Tab contains invalid values"));
                formHasErrors = true;
            } else {
                setTabError(tabName, null);
            }
        }

        if (getForm().getComponentError() != null) {
            formHasErrors = true;
        }

        if (formHasErrors) {
            saveAndCloseButton.setIcon(null);
            saveAndStayOpenButton.setIcon(null);
            saveAndCloseButton.setComponentError(new UserError("Form contains invalid values"));
            saveAndStayOpenButton.setComponentError(new UserError("Form contains invalid values"));
        } else {
            saveAndCloseButton.setComponentError(null);
            saveAndStayOpenButton.setComponentError(null);
            restoreSaveButtonIcons();
        }
    }

    private void backupSaveButtonIcons() {
        saveAndCloseButtonIconBackup = saveAndCloseButton.getIcon();
        saveAndStayOpenButtonIconBackup = saveAndStayOpenButton.getIcon();
    }

    private void restoreSaveButtonIcons() {
        saveAndCloseButton.setIcon(saveAndCloseButtonIconBackup);
        saveAndStayOpenButton.setIcon(saveAndStayOpenButtonIconBackup);
    }

    /**
     * Set whether or not to enable and hide cancel button.
     *
     * @param isEnabled true if cancel button will be enabled
     */
    public void enableCancelButton(Boolean isEnabled) {
        cancelButton.setEnabled(isEnabled);
        cancelButton.setVisible(isEnabled);
    }

    /**
     * Set whether or not to enable and hide save-and-close button.
     *
     * @param isEnabled true if save-and-close button will be enabled
     */
    public void enableSaveAndCloseButton(Boolean isEnabled) {
        saveAndCloseButton.setEnabled(isEnabled);
        saveAndCloseButton.setVisible(isEnabled);
    }

    /**
     * Lifecycle method called after new entity is created for this form
     *
     * @param entity newly created entity
     */
    public void postCreate(T entity) {
    }

    /**
     * Lifecycle method called after an entity is loaded from database
     *
     * @param entity loaded entity
     */
    public void postLoad(T entity) {
    }

    /**
     * Lifecycle method called before entity is saved to database.
     *
     * @param entity entity to be saved
     */
    public void preSave(T entity) {
    }

    /**
     * Lifecycle method called  after entity is saved to databased.
     *
     * @param entity saved entity
     */
    public void postSave(T entity) {
    }

    /**
     * Get cancel button.
     *
     * @return cancel button
     */
    public Button getCancelButton() {
        return cancelButton;
    }

    /**
     * Get refresh button.
     *
     * @return refresh button
     */
    public Button getRefreshButton() {
        return refreshButton;
    }

    /**
     * Get save-and-close button.
     *
     * @return save-and-close button
     */
    public Button getSaveAndCloseButton() {
        return saveAndCloseButton;
    }

    /**
     * Get save-and-stay-open button.
     *
     * @return save-and-stay-open button
     */
    public Button getSaveAndStayOpenButton() {
        return saveAndStayOpenButton;
    }
}