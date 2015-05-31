package eu.hellek.gba.server.rpc;

import java.util.Date;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import eu.hellek.gba.client.GetPointsService;
import eu.hellek.gba.server.utils.Utils;
import eu.hellek.gba.shared.LoginInfo;
import eu.hellek.gba.shared.UserFavouritePositionProxy;

@SuppressWarnings("serial")
public class GetPointsServiceImpl extends RemoteServiceServlet implements GetPointsService {
	
	@Override
	public String getUserMail() {
		return Utils.getUser().getEmail();
	}

	@Override
	public LoginInfo login(String requestUri) {
		//getThreadLocalRequest().getSession().setAttribute("date", new Date());
		return new ExtractedFunctions().login(requestUri);
	}
	
	@Override
	public UserFavouritePositionProxy addFavourite(UserFavouritePositionProxy fpp) {
		return new ExtractedFunctions().addFavourite(fpp);
	}
	
	@Override
	public String removeFavourite(String id) {
		return new ExtractedFunctions().removeFavourite(id);
	}
	
}
