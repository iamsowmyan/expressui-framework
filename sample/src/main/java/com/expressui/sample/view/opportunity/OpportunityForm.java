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

package com.expressui.sample.view.opportunity;

import com.expressui.core.entity.security.User;
import com.expressui.core.view.field.SelectField;
import com.expressui.core.view.form.EntityForm;
import com.expressui.core.view.form.FormFieldSet;
import com.expressui.core.view.form.FormTab;
import com.expressui.core.view.security.select.UserSelect;
import com.expressui.sample.entity.Account;
import com.expressui.sample.entity.Opportunity;
import com.expressui.sample.view.select.AccountSelect;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"serial"})
public class OpportunityForm extends EntityForm<Opportunity> {

    @Resource
    private AccountSelect accountSelect;

    @Resource
    private UserSelect userSelect;

    @Override
    public void init(FormFieldSet formFields) {

        FormTab overview = formFields.createTab(getDomainMessage("overview"));
        overview.setCoordinates(id(p.getName()), 1, 1);
        overview.setCoordinates(id(p.getOpportunityType()), 1, 2);

        overview.setCoordinates(id(p.getAccount().getName()), 2, 1);
        overview.setCoordinates(id(p.getLeadSource()), 2, 2);

        overview.setCoordinates(id(p.getSalesStage()), 3, 1);
        overview.setCoordinates(id(p.getAssignedTo().getLoginName()), 3, 2);

        overview.setCoordinates(id(p.getProbability()), 4, 1);
        overview.setCoordinates(id(p.getCurrency()), 4, 2);

        overview.setCoordinates(id(p.getAmount()), 5, 1);
        overview.setCoordinates(id(p.getValueWeightedInUSD()), 5, 2);

        overview.setCoordinates(id(p.getExpectedCloseDate()), 6, 1);
        overview.setCoordinates(id(p.getActualCloseDate()), 6, 2);

        FormTab description = formFields.createTab(getDomainMessage("description"));
        description.setCoordinates(id(p.getDescription()), 1, 1);

        SelectField<Opportunity, User> assignedToField =
                new SelectField<Opportunity, User>(this, id(p.getAssignedTo()), userSelect);
        formFields.setField(id(p.getAssignedTo().getLoginName()), assignedToField);

        SelectField<Opportunity, Account> accountField =
                new SelectField<Opportunity, Account>(this, id(p.getAmount()), accountSelect);
        formFields.setField(id(p.getAccount().getName()), accountField);
    }

    @Override
    public void onDisplay() {
        super.onDisplay();
        getMainApplication().showTrayMessage(
                "<h3>Feature Tips:</h3>" +
                        "<ul>" +
                        "<li>Change Sales Stage or Currency to see how other fields are automatically recalculated" +
                        "<li>Input invalid data and then mouse-over input to see error message" +
                        "</ul>"
        );
    }
}
