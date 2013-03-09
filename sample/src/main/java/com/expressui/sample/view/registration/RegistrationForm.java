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

package com.expressui.sample.view.registration;

import com.expressui.core.dao.security.RoleDao;
import com.expressui.core.dao.security.UserDao;
import com.expressui.core.dao.security.UserRoleDao;
import com.expressui.core.entity.security.Role;
import com.expressui.core.entity.security.User;
import com.expressui.core.entity.security.UserRole;
import com.expressui.core.view.form.FormFieldSet;
import com.expressui.sample.entity.Profile;
import com.expressui.sample.validator.UniqueLoginNameValidator;
import com.expressui.sample.view.LoginPage;
import com.expressui.sample.view.myprofile.MyProfileForm;
import com.vaadin.ui.Window;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings("serial")
public class RegistrationForm extends MyProfileForm<Profile> {

    @Resource
    private UserDao userDao;

    @Resource
    private UserRoleDao userRoleDao;

    @Resource
    private RoleDao roleDao;

    @Resource
    private UniqueLoginNameValidator uniqueLoginNameValidator;

    @Override
    public void postConstruct() {
        super.postConstruct();

        getSaveAndStayOpenButton().setCaption("Register");
    }

    @Override
    public void postCreate(Profile profile) {
        super.postCreate(profile);

        profile.setUser(new User());
    }

    @Override
    public void init(FormFieldSet formFields) {
        super.init(formFields);

        formFields.setOriginalReadOnly(id(p.getUser().getLoginName()), false);
        formFields.addValidator(id(p.getUser().getLoginName()), uniqueLoginNameValidator);
        // tricky: need to allow committing of non-unique loginName to entity so that entity-level validations are
        // applied against same data as Vaadin Validator
        formFields.getFormField(id(p.getUser().getLoginName())).getField().setInvalidCommitted(true);

        formFields.setOriginallyRequired(id(p.getUser().getLoginPassword()), true);
        formFields.setOriginallyRequired(id(p.getUser().getRepeatLoginPassword()), true);
    }

    @Override
    public void preSave(Profile profile) {
        Role anyUserRole = roleDao.findByName("ROLE_USER");
        Role guestRole = roleDao.findByName("ROLE_GUEST");

        User user = profile.getUser();
        userDao.save(user);
        UserRole userRole = new UserRole(user, anyUserRole);
        userRoleDao.save(userRole);

        userRole = new UserRole(user, guestRole);
        userRoleDao.save(userRole);
    }

    @Override
    public void postSave(Profile profile) {
        getMainApplication().displayPage(LoginPage.class);
    }

    @Override
    public void showSaveSuccessfulMessage() {
        String message = uiMessageSource.getMessage("registrationForm.registrationSuccessful");
        Window.Notification notification = new Window.Notification(message, null,
                Window.Notification.TYPE_HUMANIZED_MESSAGE, true);
        notification.setDelayMsec(2000);
        notification.setPosition(Window.Notification.POSITION_CENTERED_BOTTOM);
        getMainApplication().showNotification(notification);
    }

    @Override
    public String getTypeCaption() {
        return getDomainMessage();
    }
}
