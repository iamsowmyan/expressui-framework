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

package com.expressui.sample.entity;


import com.expressui.core.entity.WritableEntity;
import com.expressui.core.entity.security.User;
import com.expressui.sample.util.validator.ValidPhone;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table
public class Profile extends WritableEntity {

    public static final String DEFAULT_PHONE_COUNTRY = "US";

    private String firstName;

    private String lastName;

    private String title;

    private String company;

    private String email;

    private boolean doNotEmail;

    @Embedded
    private Phone mainPhone;

    @Enumerated(EnumType.STRING)
    private PhoneType mainPhoneType = PhoneType.BUSINESS;

    private boolean doNotCall;

    @Index(name = "IDX_PROFILE_USER")
    @ForeignKey(name = "FK_PROFILE_USER")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    private User user;

    @Index(name = "IDX_PROFILE_MAILING_ADDRESS")
    @ForeignKey(name = "FK_PROFILE_MAILING_ADDRESS")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Address address = new Address(AddressType.MAILING);

    public Profile() {
    }

    public Profile(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @NotBlank
    @NotNull
    @Size(min = 1, max = 64)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @NotBlank
    @NotNull
    @Size(min = 1, max = 64)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getName() {
        if (getFirstName() == null && getLastName() != null) {
            return getFirstName();
        } else if (getFirstName() != null && getLastName() == null) {
            return getLastName();
        } else if (getFirstName() == null && getLastName() == null) {
            return null;
        } else {
            return getLastName() + ", " + getFirstName();
        }
    }

    @Size(min = 1, max = 64)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @NotBlank
    @Size(min = 1, max = 64)
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    @NotBlank
    @Email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isDoNotEmail() {
        return doNotEmail;
    }

    public void setDoNotEmail(boolean doNotEmail) {
        this.doNotEmail = doNotEmail;
    }

    @ValidPhone
    public Phone getMainPhone() {
        return mainPhone;
    }

    public void setMainPhone(Phone mainPhone) {
        this.mainPhone = mainPhone;
    }

    @NotNull
    public PhoneType getMainPhoneType() {
        return mainPhoneType;
    }

    public void setMainPhoneType(PhoneType mainPhoneType) {
        this.mainPhoneType = mainPhoneType;
    }

    public boolean isDoNotCall() {
        return doNotCall;
    }

    public void setDoNotCall(boolean doNotCall) {
        this.doNotCall = doNotCall;
    }

    @NotNull
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Valid
    @NotNull
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        if (address != null) {
            address.setAddressType(AddressType.MAILING);
        }
        this.address = address;
    }
}