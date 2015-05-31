package eu.hellek.gba.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import eu.hellek.gba.shared.LoginInfo;
import eu.hellek.gba.shared.UserFavouritePositionProxy;

public interface GetPointsServiceAsync {
	void getUserMail(AsyncCallback<String> callback) throws IllegalArgumentException;
	void login(String requestUri, AsyncCallback<LoginInfo> async);
	void addFavourite(UserFavouritePositionProxy fpp, AsyncCallback<UserFavouritePositionProxy> async);
	void removeFavourite(String id, AsyncCallback<String> async);
}
