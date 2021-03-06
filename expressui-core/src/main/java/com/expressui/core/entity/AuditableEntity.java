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

package com.expressui.core.entity;

import com.expressui.core.security.SecurityService;
import com.expressui.core.util.SpringApplicationContext;

import javax.persistence.*;
import java.util.Date;

/**
 * Base class for entities wishing to be audited. This means that creation and modification timestamps
 * are saved to the database as well as the login name of the user responsible for the creation or modification.
 * This class also versions entities in order to handle concurrent optimistic writes gracefully.
 * Any instances of this class are automatically autowired by Spring, allowing injection
 * of bean resources into entities.
 */
@MappedSuperclass
@EntityListeners({AuditableEntity.WritableEntityListener.class})
public abstract class AuditableEntity implements IdentifiableEntity {

    @Version
    private Integer version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date lastModified;

    @Column(nullable = false)
    private String modifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date created;

    @Column(nullable = false)
    private String createdBy;

    protected AuditableEntity() {
        SpringApplicationContext.autowire(this);
    }

    /**
     * Gets the version number, which is incremented every time this entity' is updated.
     *
     * @return version number starting at 0
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Gets the last time changes were saved to the database.
     *
     * @return timestamp
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Gets the login name of the user who made the last modifications.
     *
     * @return login name of the user entity
     */
    public String getModifiedBy() {
        return modifiedBy;
    }

    /**
     * Gets the timestamp this entity was created in the database.
     *
     * @return timestamp
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Gets the login name of the user who created this entity.
     *
     * @return login name of the user entity
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Listener that sets auditing-tracking timestamps and username info.
     */
    public static class WritableEntityListener {

        public WritableEntityListener() {
        }

        /**
         * Called before persist and sets created, lastModified, createBy and modifiedBy
         * properties.
         *
         * @param auditableEntity entity to set timestamps and username info on
         */
        @PrePersist
        public void onPrePersist(AuditableEntity auditableEntity) {
            auditableEntity.created = new Date();
            auditableEntity.lastModified = auditableEntity.created;

            auditableEntity.createdBy = SecurityService.getCurrentLoginName();
            auditableEntity.modifiedBy = auditableEntity.createdBy;
        }

        /**
         * Called before update and sets lastModified and modifiedBy
         * properties.
         *
         * @param auditableEntity entity to set timestamps and username info on
         */
        @PreUpdate
        public void onPreUpdate(AuditableEntity auditableEntity) {
            auditableEntity.lastModified = new Date();
            auditableEntity.modifiedBy = SecurityService.getCurrentLoginName();
        }
    }
}
