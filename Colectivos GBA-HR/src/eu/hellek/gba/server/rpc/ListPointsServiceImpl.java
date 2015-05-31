package eu.hellek.gba.server.rpc;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import eu.hellek.gba.client.ListPointsService;
import eu.hellek.gba.model.Point;
import eu.hellek.gba.server.dao.Dao;
import eu.hellek.gba.server.utils.Utils;
import eu.hellek.gba.shared.SearchResultProxy;

@SuppressWarnings("serial")
public class ListPointsServiceImpl extends RemoteServiceServlet implements ListPointsService {

//	private static final int maxRequestsDirect = 30;
	public static final int maxRequestsDirect = 100;
	public static final int maxRequestsIndirect = maxRequestsDirect;
//	private static final int maxRequestsIndirect = maxRequestsDirect/2;
	
	@Override
	public SearchResultProxy getDirectConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte) throws IllegalArgumentException {
		final String functionName = "getDirectConnections()";
//		System.err.println("Session cookie: " + getThreadLocalRequest().getSession().getId());
		double distanceBetweenPoints = Utils.distanceInMeters(new Point("", lat1, lon1, null), new Point("", lat2, lon2, null));
		if(distanceBetweenPoints > 1000) {
			int requests = 0;
			if(getThreadLocalRequest() != null && getThreadLocalRequest().getSession() != null && getThreadLocalRequest().getSession().getId() != null) {
				requests = Dao.getInstance().addAndCheckSearchForIp(getThreadLocalRequest().getSession().getId(), 0);
			} else {
				requests = Dao.getInstance().addAndCheckSearchForIp(getThreadLocalRequest().getRemoteAddr(), 0);
			}
			if(requests < maxRequestsDirect) {
				return new ExtractedFunctions().getDirectConnections(lat1, lon1, lat2,lon2,ignoreTrains, ignoreSubte, this.getThreadLocalRequest().getRemoteAddr());
			} else {
				Logger.getLogger("ListPointsServiceImpl").log(Level.WARNING, functionName + ": too many requests for ip " + getThreadLocalRequest().getRemoteAddr() + ": " + requests);
				return new SearchResultProxy(SearchResultProxy.tooManyReqests, null, null);
			}
		} else {
			Logger.getLogger("ListPointsServiceImpl").log(Level.INFO, functionName + ": distance too little " + distanceBetweenPoints);
			return new SearchResultProxy(SearchResultProxy.distanceTooLittle, null, null);
		}
	}

	@Override
	public SearchResultProxy getIndirectConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte, List<String> mlkSet1String, List<String> mlkSet2String) throws IllegalArgumentException {
		final String functionName = "getIndirectConnections()";
		double distanceBetweenPoints = Utils.distanceInMeters(new Point("", lat1, lon1, null), new Point("", lat2, lon2, null));
		if(distanceBetweenPoints > 1000) {
			int requests = 0;
			if(getThreadLocalRequest() != null && getThreadLocalRequest().getSession() != null && getThreadLocalRequest().getSession().getId() != null) {
				requests = Dao.getInstance().addAndCheckSearchForIp(getThreadLocalRequest().getSession().getId(), 1);
			} else {
				requests = Dao.getInstance().addAndCheckSearchForIp(getThreadLocalRequest().getRemoteAddr(), 1);
			}
			if(requests < maxRequestsIndirect) {
				return new ExtractedFunctions().getIndirectConnections(lat1, lon1, lat2, lon2, ignoreTrains, ignoreSubte, mlkSet1String, mlkSet2String);
			} else {
				Logger.getLogger("ListPointsServiceImpl").log(Level.WARNING, functionName + ": too many requests for ip " + getThreadLocalRequest().getRemoteAddr() + ": " + requests);
				return new SearchResultProxy(SearchResultProxy.tooManyReqests, null, null);
			}
		} else {
			Logger.getLogger("ListPointsServiceImpl").log(Level.INFO, functionName + ": distance too little " + distanceBetweenPoints);
			return new SearchResultProxy(SearchResultProxy.distanceTooLittle, null, null);
		}
	}
	
}
