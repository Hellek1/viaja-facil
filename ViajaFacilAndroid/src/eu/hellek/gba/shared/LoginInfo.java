package eu.hellek.gba.shared;

import java.io.Serializable;
import java.util.List;

/*
 * transmits login information and favorites.
 */
public class LoginInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean loggedIn = false;
	private String loginUrl; // not relevant
	private String logoutUrl; // not relevant
	private String emailAddress; // since the user logs in using his e-mail anyways, this is merely a confirmation of the login
	private String nickname;
	private List<UserFavouritePositionProxy> favourites; // favorites

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getLogoutUrl() {
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public List<UserFavouritePositionProxy> getFavourites() {
		return favourites;
	}

	public void setFavourites(List<UserFavouritePositionProxy> favourites) {
		this.favourites = favourites;
	}
	
}
