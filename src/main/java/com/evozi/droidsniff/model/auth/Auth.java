/*    	Auth.java is a wrapper for a requires cookie list of one Authentication
    	Copyright (C) 2011 Andreas Koch <koch.trier@gmail.com>
    	
    	This software was supported by the University of Trier 

	    This program is free software; you can redistribute it and/or modify
	    it under the terms of the GNU General Public License as published by
	    the Free Software Foundation; either version 3 of the License, or
	    (at your option) any later version.
	
	    This program is distributed in the hope that it will be useful,
	    but WITHOUT ANY WARRANTY; without even the implied warranty of
	    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	    GNU General Public License for more details.
	
	    You should have received a copy of the GNU General Public License along
	    with this program; if not, write to the Free Software Foundation, Inc.,
	    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA. */

package com.evozi.droidsniff.model.auth;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.evozi.droidsniff.model.Session;
import lombok.*;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@EqualsAndHashCode
public final class Auth implements Serializable {
    private static final long serialVersionUID = 0x8008;
    private static Set<Auth> saved = new HashSet<Auth>();

    @NonNull List<Session> sessions;
	@NonNull String url;
	String mobileUrl;
    String name;
    @NonNull String ip;
    @NonNull String authName;

    public String getName() {
        return (name == null || name.equals("")) ? url : name + " [" + url + "]";
    }
	
	public boolean isGeneric() {
		return authName.equalsIgnoreCase("generic");
	}

    public boolean isSaved() {
        return saved.contains(this);
    }

    public void setSaved(boolean save) {
        if (save) {
            saved.add(this);
        } else {
            saved.remove(this);
        }
    }

    public int getId() {
        return this.hashCode();
    }
}
