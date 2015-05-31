package eu.hellek.gba.server.rpc.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.protobuf.CodedInputStream;

import eu.hellek.gba.proto.LoginInfoProtos.LoginInfo.UserFavouritePositionProxy;
import eu.hellek.gba.server.rpc.ExtractedFunctions;

public class AddFavoriteServlet extends HttpServlet {

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
		InputStream is = req.getInputStream();	
		try {
			UserFavouritePositionProxy fppin = UserFavouritePositionProxy.parseFrom(CodedInputStream.newInstance(is));
			eu.hellek.gba.shared.UserFavouritePositionProxy fpp = new eu.hellek.gba.shared.UserFavouritePositionProxy(fppin.getName(), fppin.getLat(), fppin.getLon(), null);
			eu.hellek.gba.shared.UserFavouritePositionProxy fav = new ExtractedFunctions().addFavourite(fpp);
			UserFavouritePositionProxy toSend = UserFavouritePositionProxy.newBuilder().setKey(fav.getKey()).setName(fav.getName()).setLat(fav.getLat()).setLon(fav.getLon()).build();
			toSend.writeTo(resp.getOutputStream());
		} catch (Exception e) {
			Logger.getLogger("AddFavoriteServlet").log(Level.SEVERE, "Error: " + e);
			e.printStackTrace();
		}
	}

}
