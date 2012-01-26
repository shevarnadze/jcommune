/**
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jtalks.jcommune.model.entity;

import org.joda.time.DateTime;
import org.jtalks.common.model.entity.User;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores information about the forum user.
 * Used as {@code UserDetails} in spring security for user authentication, authorization.
 *
 * @author Pavel Vervenko
 * @author Kirill Afonin
 * @author Alexandre Teterin
 * @author Andrey Kluev
 */
public class JCUser extends User {

    private String signature;
    private int postCount;
    private Language language = Language.ENGLISH;
    private int pageSize = DEFAULT_PAGE_SIZE;
    private String location;
    private DateTime registrationDate;

    public static final int MIN_NAME_SIZE = 4;
    public static final int MAX_NAME_SIZE = 20;
    public static final int MAX_LAST_NAME_SIZE = 255;
    public static final int MIN_PASS_SIZE = 4;
    public static final int MAX_PASS_SIZE = 20;
    public static final int MAX_LOCATION_SIZE = 30;

    public static final int DEFAULT_PAGE_SIZE = 50;

    private static final long serialVersionUID = 19981017L;
    private Set<UserContact> contacts = new HashSet<UserContact>();

    /**
     * Only for hibernate usage.
     */
    protected JCUser() {
    }

    /**
     * Create instance with required fields.
     *
     * @param username username
     * @param email    email
     * @param password password
     */
    public JCUser(String username, String email, String password) {
        // passing salt as null until we're not using encrypted passwords
        super(username, email, password, null);
    }

    /**
     * Updates login time to current time
     */
    public void updateLastLoginTime() {
        this.setLastLogin(new DateTime());
    }

    /**
     *
     * @param contact
     */
    public void addContact(UserContact contact){
        contact.setOwner(this);
        this.getContacts().add(contact);
    }

    /**
     *
     * @param contact
     */
    public void removeContact(UserContact contact){
        this.getContacts().remove(contact);
    }

    /**
     * @return user signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @param signature user signature
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * @return count post this user
     */
    public int getPostCount() {
        return this.postCount;

    }

    /**
     * @param postCount count posts this user to set
     */
    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    /**
     * @return user language
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * @param language of user
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * @return user page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize user page size
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @return user location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location user location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return user registration date
     */
    public DateTime getRegistrationDate() {
        return registrationDate;
    }

    /**
     * @param registrationDate user registration date
     */
    public void setRegistrationDate(DateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * @return set contacts of user
     */
    protected Set<UserContact> getContacts() {
        return contacts;
    }

    /**
     * @param contacts contacts of user
     */
    protected void setContacts(Set<UserContact> contacts) {
        this.contacts = contacts;
    }
}