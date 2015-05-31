package eu.hellek.gba.model;

import java.io.Serializable;

import javax.persistence.Id;

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Unindexed;

@Cached
public class UserFavouritePosition implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Id private Long id;
	
	private User user;
	
	@Unindexed
	private GeoPt pos;
	
	@Unindexed
	private String name;
	
	public UserFavouritePosition() { }
	
	public UserFavouritePosition(User user, GeoPt pos, String name) {
		this.user = user;
		this.pos = pos;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public GeoPt getPos() {
		return pos;
	}

	public String getName() {
		return name;
	}
	
}
