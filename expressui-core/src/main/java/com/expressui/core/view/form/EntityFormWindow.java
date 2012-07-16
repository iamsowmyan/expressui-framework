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

import com.expressui.core.MainApplication;
import com.expressui.core.util.MethodDelegate;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Popup window for displaying entity form.
 */
public class EntityFormWindow extends Window {

    private ResultsConnectedEntityForm resultsConnectedEntityForm;
    private EntityForm entityForm;

    private Set<MethodDelegate> closeListeners = new LinkedHashSet<MethodDelegate>();

    /**
     * Constructs window to display entity form that is not connected to results, in other words, where the
     * user cannot step through results with previous/next buttons.
     *
     * @param entityForm form to display inside window
     */
    protected EntityFormWindow(EntityForm entityForm) {
        super(entityForm.getEntityCaption());

        initialize();
        this.entityForm = entityForm;
        addComponent(entityForm);
        entityForm.addCancelListener(this, "close");
        entityForm.addCloseListener(this, "close");
        entityForm.addSaveListener(this, "refreshCaption");
        entityForm.onDisplay();
    }

    /**
     * Constructs window to display a results-connected entity form, in other words, where the
     * user can step through results with previous/next buttons.
     *
     * @param resultsConnectedEntityForm results-connected form
     */
    protected EntityFormWindow(ResultsConnectedEntityForm resultsConnectedEntityForm) {
        super(resultsConnectedEntityForm.getEntityForm().getEntityCaption());

        initialize();
        this.resultsConnectedEntityForm = resultsConnectedEntityForm;
        this.entityForm = resultsConnectedEntityForm.getEntityForm();
        addComponent(resultsConnectedEntityForm);
        resultsConnectedEntityForm.getEntityForm().addCancelListener(this, "close");
        resultsConnectedEntityForm.getEntityForm().addCloseListener(this, "close");
        resultsConnectedEntityForm.addWalkListener(this, "refreshCaption");
        entityForm.addSaveListener(this, "refreshCaption");
        entityForm.onDisplay();
    }

    /**
     * Refreshes caption, as user steps through results.
     */
    void refreshCaption() {
        setCaption(entityForm.getEntityCaption());
    }

    private void initialize() {
        addStyleName("e-entity-form-window");
        addStyleName("opaque");
        VerticalLayout layout = (VerticalLayout) getContent();
        layout.setMargin(true);
        layout.setSpacing(true);
        layout.setSizeUndefined();
        setSizeUndefined();
        setModal(true);
        setClosable(true);
        setScrollable(true);
        addListener(CloseEvent.class, this, "onClose");

        MainApplication.getInstance().getMainWindow().addWindow(this);
    }

    /**
     * Opens window to display entity form that is connected to results.
     *
     * @param resultsConnectedEntityForm results-connected form
     * @return window
     */
    public static EntityFormWindow open(ResultsConnectedEntityForm resultsConnectedEntityForm) {
        return new EntityFormWindow(resultsConnectedEntityForm);
    }

    /**
     * Opens window to display entity form that is not connected to results.
     *
     * @param entityForm entityForm form to display inside window
     * @return window
     */
    public static EntityFormWindow open(EntityForm entityForm) {
        return new EntityFormWindow(entityForm);
    }

    /**
     * Listener method called that in turn notifies all close listeners.
     *
     * @param closeEvent ignored
     */
    public void onClose(CloseEvent closeEvent) {
        for (MethodDelegate closeListener : closeListeners) {
            closeListener.execute();
        }

        if (resultsConnectedEntityForm != null) {
            resultsConnectedEntityForm.removeListeners(this);
        }

        entityForm.removeListeners(this);
    }

    /**
     * Adds a listener to get invoked when user closes the window.
     *
     * @param target     object to invoke
     * @param methodName name of method to invoke
     */
    public void addCloseListener(Object target, String methodName) {
        closeListeners.add(new MethodDelegate(target, methodName));
    }
}
