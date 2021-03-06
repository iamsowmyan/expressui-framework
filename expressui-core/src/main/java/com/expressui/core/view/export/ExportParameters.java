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

package com.expressui.core.view.export;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * Parameters for exporting to excel. See <a href="https://vaadin.com/directory#addon/tableexport">TableExport Add-On</a>.
 * Defaults can be customized in application.properties.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class ExportParameters {
    private String exportFilename = "export.xls";
    private String workbookName = "Export";
    private String sheetName = "Export";
    private String dateFormat = "mm/dd/yyyy";
    private String doubleFormat = "#0.00";
    private boolean displayRowHeaders = false;
    private boolean displayTotals;

    /**
     * Gets the filename to be export to.
     *
     * @return export filename
     */
    public String getExportFilename() {
        return exportFilename;
    }

    /**
     * Sets the filename to be export to.
     *
     * @param exportFilename export filename
     */
    public void setExportFilename(String exportFilename) {
        this.exportFilename = exportFilename;
    }

    /**
     * Gets workbook name to contain exported data.
     *
     * @return workbook name
     */
    public String getWorkbookName() {
        return workbookName;
    }

    /**
     * Sets workbook name to contain exported data.
     *
     * @param workbookName workbook name
     */
    public void setWorkbookName(String workbookName) {
        this.workbookName = workbookName;
    }

    /**
     * Gets sheet name.
     *
     * @return sheet name
     */
    public String getSheetName() {
        return sheetName;
    }

    /**
     * Sets sheet name.
     *
     * @param sheetName sheet name
     */
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    /**
     * Gets date format.
     *
     * @return date format
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Sets date format.
     *
     * @param dateFormat date format
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Gets double format.
     *
     * @return double format
     */
    public String getDoubleFormat() {
        return doubleFormat;
    }

    /**
     * Sets double format.
     *
     * @param doubleFormat double format
     */
    public void setDoubleFormat(String doubleFormat) {
        this.doubleFormat = doubleFormat;
    }

    /**
     * Asks whether or not to include row headers. Default is false.
     *
     * @return true to include row headers
     */
    public boolean isDisplayRowHeaders() {
        return displayRowHeaders;
    }

    /**
     * Sets whether or not to include row headers.
     *
     * @param displayRowHeaders true to include row headers
     */
    public void setDisplayRowHeaders(boolean displayRowHeaders) {
        this.displayRowHeaders = displayRowHeaders;
    }

    /**
     * Asks whether or not to include totals. Default is false.
     *
     * @return true to include totals
     */
    public boolean isDisplayTotals() {
        return displayTotals;
    }

    /**
     * Sets whether or not to include totals.
     *
     * @param displayTotals true to include totals
     */
    public void setDisplayTotals(boolean displayTotals) {
        this.displayTotals = displayTotals;
    }
}
