/*
 * Copyright (c) 2011 Brown Bag Consulting.
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

package com.expressui.core.view.tomanyrelationship;

import com.expressui.core.view.entityselect.EntitySelect;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * Results containing entities in a to-many aggregation relationship.
 *
 * @param <T> type of the entities in the results
 */
public abstract class ToManyAggregationRelationshipResults<T> extends ToManyRelationshipResults<T> {

    /**
     * Get the EntitySelect component used to select an entity for adding to the relationship
     *
     * @return EntitySelect component
     */
    public abstract EntitySelect<T> getEntitySelect();

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        getEntitySelect().getResults().getResultsTable().setMultiSelect(true);
    }

    @Override
    public void postWire() {
        super.postWire();
        getEntitySelect().postWire();
        getEntitySelect().getResults().setSelectButtonListener(this, "itemsSelected");
    }

    @Override
    public void add() {
        getEntitySelect().open();
    }

    /**
     * Invoked when user sellects entities to be added to the relationship.
     */
    public void itemsSelected() {
        getEntitySelect().close();
        Collection<T> selectedValues = getEntitySelect().getResults().getSelectedValues();
        setReferencesToParentAndPersist((T[]) selectedValues.toArray());
    }
}
