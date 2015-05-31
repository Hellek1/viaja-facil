package eu.hellek.gba.server.servlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;

import com.google.appengine.api.utils.SystemProperty;

import eu.hellek.gba.server.dao.Dao;

public class WarmUp extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public void init() {
		if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
			Logger.getLogger("WarmUp").log(Level.INFO, "load on startup init() called.");
			Dao.getTrainNodeKeyMap();
		}
	}

}
