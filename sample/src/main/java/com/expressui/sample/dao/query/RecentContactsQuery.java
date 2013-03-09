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

package com.expressui.sample.dao.query;

import com.expressui.core.dao.query.StructuredEntityQuery;
import com.expressui.sample.entity.Contact;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class RecentContactsQuery extends StructuredEntityQuery<Contact> {

    @Override
    public Path buildOrderBy(Root<Contact> contact) {
        if (getOrderByPropertyId().equals(id(p.getMailingAddress().getCountry()))) {
            return orderByPath(contact, p.getMailingAddress().getCountry());
        } else if (getOrderByPropertyId().equals(id(p.getMailingAddress().getStreet()))) {
            return orderByPath(contact, p.getMailingAddress().getStreet());
        } else if (getOrderByPropertyId().equals(id(p.getMailingAddress().getCity()))) {
            return orderByPath(contact, p.getMailingAddress().getCity());
        } else if (getOrderByPropertyId().equals(id(p.getMailingAddress().getState().getCode()))) {
            return orderByPath(contact, p.getMailingAddress().getState().getCode());
        } else {
            return null;
        }
    }

    @Override
    public void addFetchJoins(Root<Contact> contact) {
        fetch(contact, JoinType.LEFT, p.getMailingAddress().getState().getCountry());
    }

    @Override
    public String toString() {
        return "RecentContactsQuery{}";
    }
}
