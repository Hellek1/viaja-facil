package eu.hellek.gba.server.rpc.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.hellek.gba.proto.LoginInfoProtos.LoginInfo;
import eu.hellek.gba.proto.LoginInfoProtos.LoginInfo.Builder;
import eu.hellek.gba.proto.LoginInfoProtos.LoginInfo.UserFavouritePositionProxy;
import eu.hellek.gba.server.rpc.ExtractedFunctions;

public class LoginServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		service(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		service(req, resp);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//PrintWriter out = resp.getWriter();
		//req.getSession().setAttribute("date", new Date());
		eu.hellek.gba.shared.LoginInfo res = new ExtractedFunctions().login("");
		List<UserFavouritePositionProxy> favs = new ArrayList<UserFavouritePositionProxy>();
		if(res.getFavourites() != null) {
			for(eu.hellek.gba.shared.UserFavouritePositionProxy ufpp : res.getFavourites()) {
				UserFavouritePositionProxy neu = UserFavouritePositionProxy.newBuilder().setKey(ufpp.getKey()).setLat(ufpp.getLat()).setLon(ufpp.getLon()).setName(ufpp.getName()).build();
				favs.add(neu);
			}
		}
		Builder builder = LoginInfo.newBuilder();
		if(res.getEmailAddress() != null) {
			builder.setEmailAddress(res.getEmailAddress());
		}
		if(res.getNickname() != null) {
			builder.setNickname(res.getNickname());
		}
		if(res.getLoginUrl() != null) {
			builder.setLoginUrl(res.getLoginUrl());
		}
		if(res.getLogoutUrl() != null) {
			builder.setLogoutUrl(res.getLogoutUrl());
		}
		builder.addAllFavourites(favs).setLoggedIn(res.isLoggedIn());
		LoginInfo toSend = builder.build();
		//resp.setContentType("application/x-protobuf");
		//resp.setContentLength(toSend.getSerializedSize());
		toSend.writeTo(resp.getOutputStream());
		//resp.flushBuffer();
		//resp.getWriter().close();
	}

}
