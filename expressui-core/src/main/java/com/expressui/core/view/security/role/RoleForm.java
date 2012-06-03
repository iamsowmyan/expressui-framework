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

package com.expressui.core.view.security.role;

import com.expressui.core.entity.security.Role;
import com.expressui.core.view.field.FormField;
import com.expressui.core.view.form.EntityForm;
import com.expressui.core.view.form.FormFieldSet;
import com.expressui.core.view.security.role.related.RelatedPermissions;
import com.expressui.core.view.security.role.related.RelatedUsers;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.TextArea;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * Form for viewing or editing roles.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"rawtypes", "serial"})
public class RoleForm extends EntityForm<Role> {

    @Resource
    private RelatedUsers relatedUsers;

    @Resource
    private RelatedPermissions relatedPermissions;

    @Override
    public void init(FormFieldSet formFields) {
        formFields.setCoordinates("name", 1, 1);
        formFields.setCoordinates("allowOrDenyByDefault", 1, 2);

        formFields.setCoordinates("description", 2, 1, 2, 2);
        formFields.setField("description", new TextArea());
        formFields.setWidth("description", 40, Sizeable.UNITS_EM);
        formFields.setHeight("description", 5, Sizeable.UNITS_EM);
        formFields.setAutoAdjustWidthMode("description", FormField.AutoAdjustWidthMode.NONE);
    }

    @Override
    public String getTypeCaption() {
        return "Role Form";
    }

    @Override
    public List<ToManyRelationship> getToManyRelationships() {
        List<ToManyRelationship> toManyRelationships = new ArrayList<ToManyRelationship>();
        toManyRelationships.add(relatedUsers);
        toManyRelationships.add(relatedPermissions);

        return toManyRelationships;
    }
}