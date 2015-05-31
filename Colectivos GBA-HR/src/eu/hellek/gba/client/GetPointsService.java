package eu.hellek.gba.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import eu.hellek.gba.shared.LoginInfo;
import eu.hellek.gba.shared.UserFavouritePositionProxy;

@RemoteServiceRelativePath("getPoints")
public interface GetPointsService extends RemoteService {
	String getUserMail() throws IllegalArgumentException;
	LoginInfo login(String requestUri);
	UserFavouritePositionProxy addFavourite(UserFavouritePositionProxy fpp);
	String removeFavourite(String id);
}
