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

package com.expressui.sample.view.dashboard;

import com.expressui.core.view.page.DashboardPage;
import com.expressui.domain.geocode.MapService;
import com.expressui.sample.dao.OpportunityDao;
import com.expressui.sample.entity.Contact;
import com.expressui.sample.entity.derived.TotalSalesStage;
import com.expressui.sample.entity.derived.TotalYearSales;
import com.vaadin.terminal.Sizeable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.vaadin.vaadinvisualizations.ColumnChart;
import org.vaadin.vaadinvisualizations.PieChart;
import org.vaadin.vol.OpenLayersMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

@Component
@Scope(SCOPE_SESSION)
public class SampleDashboardPage extends DashboardPage {

    public static final int PANEL_WIDTH = 680;
    public static final int PANEL_HEIGHT = 375;

    @Resource
    private OpportunityDao opportunityDao;

    @Resource
    private RecentContactResults recentContactResults;

    @Resource
    private MapService mapService;

    @PostConstruct
    @Override
    public void postConstruct() {
        super.postConstruct();

        addComponent(createOpportunityChartByYear(), "Sales - Won & Lost", 1, 1);

        configureRecentContactResults();
        addComponent(recentContactResults, "Recent Contacts", 1, 2);

        addComponent(createOpportunitySalesStageChart(), "Sales Stage Breakdown", 2, 1);

        // Map is shown at coordinates 2, 2 when user selects a contact, see contactSelectionChanged
    }

    private ColumnChart createOpportunityChartByYear() {
        ColumnChart columnChart = new ColumnChart();
        columnChart.setOption("width", PANEL_WIDTH);
        columnChart.setOption("height", PANEL_HEIGHT);
        columnChart.setWidth(PANEL_WIDTH, Sizeable.UNITS_PIXELS);
        columnChart.setHeight(PANEL_HEIGHT, Sizeable.UNITS_PIXELS);

        columnChart.setOption("is3D", true);
        columnChart.setOption("isStacked", false);
        columnChart.addXAxisLabel("Year");
        columnChart.addColumn("Closed Won");
        columnChart.addColumn("Closed Lost");

        List<TotalYearSales> totalYearSalesList = opportunityDao.getSalesByYear();
        List<TotalYearSales> totalYearSalesLostList = opportunityDao.getSalesLostByYear();
        for (TotalYearSales totalYearSales : totalYearSalesList) {
            double salesLost = 0;
            for (TotalYearSales yearSalesLost : totalYearSalesLostList) {
                if (yearSalesLost.getYear() == totalYearSales.getYear()) {
                    salesLost = yearSalesLost.getTotalSales().doubleValue();
                }
            }

            columnChart.add(String.valueOf(totalYearSales.getYear()),
                    new double[]{totalYearSales.getTotalSales().doubleValue(), salesLost});
        }

        return columnChart;
    }

    public PieChart createOpportunitySalesStageChart() {
        PieChart pieChart = new PieChart();
        pieChart.setOption("width", PANEL_WIDTH);
        pieChart.setOption("height", PANEL_HEIGHT);
        pieChart.setWidth(PANEL_WIDTH, Sizeable.UNITS_PIXELS);
        pieChart.setHeight(PANEL_HEIGHT, Sizeable.UNITS_PIXELS);

        pieChart.setOption("title", "Opportunity Sales Stages");
        pieChart.setOption("is3D", true);

        List<TotalSalesStage> totalSalesStages = opportunityDao.getSalesStageCounts();
        for (TotalSalesStage totalSalesStage : totalSalesStages) {
            pieChart.add(totalSalesStage.getSalesStage().getDisplayName(), totalSalesStage.getCount());
        }

        return pieChart;
    }

    private void configureRecentContactResults() {
        recentContactResults.setWidth(PANEL_WIDTH, Sizeable.UNITS_PIXELS);
        recentContactResults.getResultsTable().setWidth(620, Sizeable.UNITS_PIXELS);
        recentContactResults.setHeight(PANEL_HEIGHT, Sizeable.UNITS_PIXELS);

        recentContactResults.addSelectionChangedListener(this, "contactSelectionChanged");
        recentContactResults.setPageSizeVisible(false); // restrict page size, since dashboard cells are fixed size
    }

    @Override
    public void onDisplay() {
        super.onDisplay();

        recentContactResults.search();
        if (recentContactResults.getResultsTable().getContainerDataSource().size() > 0) {
            Object firstItem = recentContactResults.getResultsTable().getContainerDataSource().getIdByIndex(0);
            recentContactResults.getResultsTable().select(firstItem);
        }
    }

    public void contactSelectionChanged() {
        Collection<Contact> contacts = (Collection<Contact>) recentContactResults.getSelectedValue();

        if (contacts.size() == 1) {
            Contact contact = contacts.iterator().next();
            String formattedAddress = contact.getMailingAddress().getFormatted();
            addContactLocationMap(contact.getName(), formattedAddress);
        } else {
            removeContactLocationMap(); // remove map if user selects more than one contact
        }
    }

    private void addContactLocationMap(String contactName, String formattedAddress) {
        OpenLayersMap map = mapService.createMap(formattedAddress, contactName, 16);
        if (map != null) {
            map.setWidth(PANEL_WIDTH, Sizeable.UNITS_PIXELS);
            map.setHeight(PANEL_HEIGHT, Sizeable.UNITS_PIXELS);
            addComponent(map, "Contact Location", 2, 2);
        }
    }

    private void removeContactLocationMap() {
        removeComponent(2, 2);
    }
}