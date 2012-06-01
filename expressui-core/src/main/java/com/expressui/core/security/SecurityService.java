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

package com.expressui.core.security;


import com.expressui.core.dao.security.UserDao;
import com.expressui.core.entity.security.Role;
import com.expressui.core.entity.security.User;
import com.expressui.core.entity.security.UserRole;
import com.expressui.core.security.exception.*;
import com.expressui.core.util.assertion.Assert;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static org.springframework.web.context.WebApplicationContext.SCOPE_SESSION;

/**
 * Service for logging in/out and getting the current user. The current user entity
 * provides access to roles and permissions.
 */
@Service
@Scope(SCOPE_SESSION)
public class SecurityService {

    /**
     * Default user name when no user is logged in, e.g. in unit tests or system process that doesn't require log in
     */
    public static final String DEFAULT_USER = "system";

    /**
     * Default role for system user
     */
    public static final String DEFAULT_ROLE = "system";

    private static ThreadLocal<String> currentLoginName = new ThreadLocal<String>();

    @Resource
    private UserDao userDao;

    private User currentUser;

    /**
     * Get the login name of the currently logged-in user. If no one is logged in, returns DEFAULT_USER.
     *
     * @return login name
     */
    public static String getCurrentLoginName() {
        if (currentLoginName.get() == null) {
            return DEFAULT_USER;
        } else {
            return currentLoginName.get();
        }
    }

    public static void setCurrentLoginName(String loginName) {
        currentLoginName.set(loginName);
    }

    public static void removeCurrentLoginName() {
        currentLoginName.remove();
    }

    /**
     * Get the user entity for the currently logged in user. Caches user entity.
     *
     * @return user entity with roles and permissions
     */
    public User getCurrentUser() {
        return currentUser;
    }

    private User findUser(String loginName) {
        Assert.PROGRAMMING.notNull(loginName, "loginName must not be null");

        User user = userDao.findByNaturalId("loginName", loginName);
        if (user != null) {
            user.hasRole("ROLE_BOGUS"); // force initialization of user and it's roles
        }

        return user;
    }

    /**
     * Sets current user, useful in unit-test environment, where user can be set programmatically.
     *
     * @param user user to set
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Logout by clearing user and current login name
     */
    public void logout() {
        currentUser = null;
        removeCurrentLoginName();
    }

    /**
     * Login as default system user, useful for testing or scenarios where no authentication is required. System user
     * has full rights without restrictions.
     */
    public void loginAsDefaultSystemUser() {
        User user = new User(DEFAULT_USER, DEFAULT_USER);
        Role role = new Role(DEFAULT_ROLE);
        UserRole userRole = new UserRole(user, role);
        user.getUserRoles().add(userRole);
        setCurrentUser(user);
    }

    /**
     * Login and cache current user in the session.
     *
     * @param loginName     user name
     * @param loginPassword password in plaintext
     */
    public void login(String loginName, String loginPassword) throws AuthenticationException {
        logout();

        if (loginName == null) loginName = "";
        if (loginPassword == null) loginPassword = "";
        loginName = loginName.trim();

        User user = findUser(loginName);
        if (user == null) {
            throw new LoginNameNotFoundException();
        }

        BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
        if (!passwordEncryptor.checkPassword(loginPassword, user.getLoginPasswordEncrypted())) {
            throw new IncorrectCredentialsException();
        }

        assertLoginAllowed(user);

        setCurrentUser(user);
        setCurrentLoginName(loginName);
    }

    /**
     * Assert that given user is allowed to log in.
     *
     * @param user user to check
     * @throws AuthenticationException if user is not allowed
     */
    public static void assertLoginAllowed(User user) throws AuthenticationException {
        if (user.isAccountExpired()) {
            throw new AccountExpiredException();
        } else if (user.isAccountLocked()) {
            throw new AccountLockedException();
        } else if (user.isCredentialsExpired()) {
            throw new CredentialsExpiredException();
        } else if (!user.isEnabled()) {
            throw new AccountDisabledException();
        }
    }
}
