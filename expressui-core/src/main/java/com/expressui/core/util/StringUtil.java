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

package com.expressui.core.util;

import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class for managing Strings
 */
public class StringUtil {

    public static final FontMetrics FONT_METRICS;

    static {
        JTextField jTextField = new JTextField("");
        Font font = new Font("Helvetica", Font.PLAIN, 12);
        FONT_METRICS = jTextField.getFontMetrics(font);
    }

    /**
     * Ask if the given String is equal to any of the other args
     *
     * @param s    string to query for
     * @param args strings to query in
     * @return true if string is found
     */
    public static boolean isEqual(String s, String... args) {
        for (String arg : args) {
            if (s.equals(arg)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the approximate column width in EM units of the given string
     *
     * @param s string to measure
     * @return width in EM units
     */
    public static int approximateColumnWidth(String s) {
        return (int) Math.ceil(FONT_METRICS.stringWidth(s) * 0.1) + 1;
    }

    /**
     * Get the approximate column width in pixels of the given string
     *
     * @param s string to measure
     * @return width in pixels
     */
    public static int approximateColumnPixelWidth(String s) {
        return FONT_METRICS.stringWidth(s) + 30;
    }

    /**
     * Extract part of string after last period.
     *
     * @param str string to extract from
     * @return last part of string, after period, just returns str if no period found
     */
    public static String extractAfterPeriod(String str) {
        int periodIndex = str.indexOf(".");
        if (periodIndex < 0) {
            return str;
        } else {
            return str.substring(periodIndex + 1);
        }
    }

    /**
     * Converts camel case to human-readable form, i.e. spaced-delimited words with each first
     * letter capitalized
     *
     * @param camelCase string in camel case
     * @return
     */
    public static String humanizeCamelCase(String camelCase) {
        String[] camelCaseParts = StringUtils.splitByCharacterTypeCamelCase(camelCase);
        String joined = StringUtils.join(camelCaseParts, " ");
        return capitaliseFirstLetter(joined);
    }

    /**
     * Capitalize first letter of given string
     *
     * @param str string to capitalize first letter of
     * @return new String with first letter capitalized
     */
    public static String capitaliseFirstLetter(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Ask if the given arg is empty, i.e. null or empty string
     *
     * @param s any object
     * @return true if arg is null or empty string
     */
    public static boolean isEmpty(Object s) {
        if (s != null && s instanceof String) {
            return ((String) s).isEmpty();
        } else {
            return s == null;
        }
    }
}
