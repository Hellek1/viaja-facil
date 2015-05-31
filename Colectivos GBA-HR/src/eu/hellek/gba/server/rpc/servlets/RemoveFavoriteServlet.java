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

public class RemoveFavoriteServlet extends HttpServlet {

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
			String res = new ExtractedFunctions().removeFavourite(fppin.getKey());
			resp.getWriter().write(res);
		} catch (Exception e) {
			Logger.getLogger("RemoveFavoriteServlet").log(Level.SEVERE, "Error: " + e);
			e.printStackTrace();
		}
	}

}
