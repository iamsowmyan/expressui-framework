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

package com.expressui.sample.dao;

import com.expressui.core.dao.EntityDao;
import com.expressui.domain.ecbfx.EcbfxService;
import com.expressui.sample.entity.Currency;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.persistence.Query;
import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class CurrencyDao extends EntityDao<Currency, String> {

    @Resource
    private EcbfxService ecbfxService;

    @Override
    public List<Currency> findAll() {

        List<Currency> currencies;

        try {
            currencies = findAllWithFxRates();
        } catch (Exception e) {
            currencies = findAllWithoutFxRates();
        }

        return currencies;
    }

    private List<Currency> findAllWithoutFxRates() {
        Query query = getEntityManager().createQuery("SELECT c FROM Currency c ORDER BY c.displayName");

        setReadOnly(query);

        return query.getResultList();
    }

    private List<Currency> findAllWithFxRates() {
        Query query = getEntityManager().createQuery("SELECT c FROM Currency c " +
                " WHERE c.id in :currenciesWithFxRates ORDER BY c.displayName");

        query.setParameter("currenciesWithFxRates", ecbfxService.getFXRates().keySet());
        setReadOnly(query);

        return query.getResultList();
    }
}
