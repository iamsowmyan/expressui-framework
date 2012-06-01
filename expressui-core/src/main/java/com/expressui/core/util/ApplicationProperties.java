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

package com.expressui.core.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Access to core Spring-loaded properties.
 */
@Configuration
public class ApplicationProperties {

    @Value("${http.proxyHost:}")
    private String httpProxyHost;

    @Value("${http.proxyPort:}")
    private Integer httpProxyPort;

    @Value("${http.proxyUsername:}")
    private String httpProxyUsername;

    @Value("${http.proxyPassword:}")
    private String httpProxyPassword;

    @Value("${restartApplicationUrl}")
    private String restartApplicationUrl;

    @Value("${codePopupEnabled:false}")
    private boolean codePopupEnabled = false;

    @Value("${baseCodeUrl:}")
    private String baseCodeUrl;

    @Value("${baseDocUrl:}")
    private String baseDocUrl;

    @Value("${sessionTimeout:30}")
    private Integer sessionTimeout = 30;

    @Value("${sessionTimeoutWarning:5}")
    private Integer sessionTimeoutWarning = 5;

    /**
     * Get the HTTP proxy hostname
     *
     * @return HTTP proxy hostname
     */
    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    /**
     * Get the HTTP proxy port
     *
     * @return HTTP proxy port
     */
    public Integer getHttpProxyPort() {
        return httpProxyPort;
    }

    /**
     * Get the HTTP proxy username
     *
     * @return HTTP username
     */
    public String getHttpProxyUsername() {
        return httpProxyUsername;
    }

    /**
     * Get the HTTP proxy password
     *
     * @return HTTP password
     */
    public String getHttpProxyPassword() {
        return httpProxyPassword;
    }

    /**
     * Get URL for restarting the app and creating a new session, e.g. upon logout or when there is an error.
     *
     * @return URL for restarting the app
     */
    public String getRestartApplicationUrl() {
        return restartApplicationUrl;
    }

    /**
     * Ask if code popups should be displayed. Only useful for demo applications.
     *
     * @return true if code popups should be displayed
     */
    public boolean isCodePopupEnabled() {
        return codePopupEnabled;
    }

    /**
     * Get base URL for fetching code to be displayed in popups. Only useful for demo applications.
     *
     * @return base URL
     */
    public String getBaseCodeUrl() {
        return baseCodeUrl;
    }

    /**
     * Get the URL for fetching source code for the given class. Only useful for demo applications.
     *
     * @param clazz class to fetch source code for
     * @return URL
     */
    public String getCodeUrl(Class clazz) {
        String url = baseCodeUrl;
        url = url.replace("\\", "/");
        if (!baseCodeUrl.endsWith("/")) {
            url += "/";
        }

        String outerClassName;
        int dollarIndex = clazz.getName().indexOf("$");
        if (dollarIndex >= 0) {
            outerClassName = clazz.getName().substring(0, dollarIndex);
        } else {
            outerClassName = clazz.getName();
        }

        return url + outerClassName.replace(".", "/") + ".java";
    }

    /**
     * Get base URL for fetching Javadoc to be displayed in popups. Only useful for demo applications.
     *
     * @return base URL
     */
    public String getBaseDocUrl() {
        return baseDocUrl;
    }

    /**
     * Get the URL for fetching Javadoc for the given class. Only useful for demo applications.
     *
     * @param clazz class to fetch Javadoc for
     * @return URL
     */
    public String getDocUrl(Class clazz) {
        String url = baseDocUrl;
        url = url.replace("\\", "/");
        if (!baseDocUrl.endsWith("/")) {
            url += "/";
        }
        return url + clazz.getName().replace(".", "/").replace("$", ".") + ".html";
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public Integer getSessionTimeoutWarning() {
        return sessionTimeoutWarning;
    }

    /**
     * Lifecycle method called after bean is constructed. Sets http.proxyHost and http.proxyPort system property
     */
    @PostConstruct
    public void postConstruct() {
        if (!StringUtil.isEmpty(System.getProperty("http.proxyHost")) && httpProxyHost != null) {
            System.setProperty("http.proxyHost", httpProxyHost);
        }
        if (!StringUtil.isEmpty(System.getProperty("http.proxyPort")) && httpProxyPort != null) {
            System.setProperty("http.proxyPort", httpProxyPort.toString());
        }
    }
}
