package eu.hellek.gba.server.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.beoui.geocell.model.BoundingBox;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

import eu.hellek.gba.model.Line;
import eu.hellek.gba.model.PlanQuadrat;
import eu.hellek.gba.model.Point;
import eu.hellek.gba.model.UserFavouritePosition;
import eu.hellek.gba.server.dao.Dao;
import eu.hellek.gba.server.holders.ConnectingLineHolder;
import eu.hellek.gba.server.utils.MyCostFunction;
import eu.hellek.gba.server.utils.MyGeocellUtils;
import eu.hellek.gba.server.utils.Utils;
import eu.hellek.gba.shared.ConnectionProxy;
import eu.hellek.gba.shared.ConnectionProxyComparator;
import eu.hellek.gba.shared.LineProxy;
import eu.hellek.gba.shared.LoginInfo;
import eu.hellek.gba.shared.SearchResultProxy;
import eu.hellek.gba.shared.UserFavouritePositionProxy;

public class ExtractedFunctions {

	public SearchResultProxy getDirectConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte, String ipaddr) {
		final String functionName = "ExctractedFunctions:getDirectConnections()";
		Objectify ofy = Dao.getInstance().getObjectify();
		HashSet<Key<Line>> tabuTrainsSet = new HashSet<Key<Line>>();
		ConnectionProxy connTren = null;
		if(ignoreTrains) {
			tabuTrainsSet.addAll(Dao.getTrainKeys());
		}
		if(ignoreSubte) {
			tabuTrainsSet.addAll(Dao.getSubteKeys());
		} else { // only run this search if the user allows subtes
			connTren = Dao.getInstance().indirectSearch(new GeoPt(lat1, lon1), new GeoPt(lat2, lon2), tabuTrainsSet, new HashSet<Key<Line>>(), ofy);
			double distanceBetweenPoints = Utils.distanceInMeters(new Point("", lat1, lon1, null), new Point("", lat2, lon2, null));
			if(connTren != null && connTren.getDistance() > 3 * distanceBetweenPoints) {
				Logger.getLogger("ListPointsServiceImpl").log(Level.INFO, functionName + ": dropped a bad result with length " + connTren.getDistance() + " which would have used " + connTren.getLines().size() + " lines.");
				connTren = null; // very bad result, let's delete it
			}
		}
		float distNS = Utils.searchDistanceNS;
		float distWE = Utils.searchDistanceWE; // about 750m in each direction, creating a 1500x1500 square
		Collection<PlanQuadrat> pqs1 = Dao.getInstance().getPQsInBB(lat1+distNS, lon1+distWE, lat1-distNS, lon1-distWE, ofy);
		Collection<PlanQuadrat> pqs2 = Dao.getInstance().getPQsInBB(lat2+distNS, lon2+distWE, lat2-distNS, lon2-distWE, ofy);
		BoundingBox bbCells1Inner = new BoundingBox(lat1+(distNS/2), lon1+(distWE/2), lat1-(distNS/2), lon1-(distWE/2)); // 750/2 in each direction creating a 750x750 square
		BoundingBox bbCells2Inner = new BoundingBox(lat2+(distNS/2), lon2+(distWE/2), lat2-(distNS/2), lon2-(distWE/2));
		HashSet<String> cellsInner = new HashSet<String>(MyGeocellUtils.bestBboxSearchCells(bbCells1Inner, new MyCostFunction()));
		cellsInner.addAll(MyGeocellUtils.bestBboxSearchCells(bbCells2Inner, new MyCostFunction()));
		
		Map<Key<Line>,Integer> set1 = new HashMap<Key<Line>,Integer>();
		Map<Key<Line>,Integer> set2 = new HashMap<Key<Line>,Integer>();
		Map<String,PlanQuadrat> pqs_map = new HashMap<String,PlanQuadrat>();
		List<ConnectingLineHolder> connectingLines = new ArrayList<ConnectingLineHolder>();
		HashSet<Key<Line>> mlkSet1 = new HashSet<Key<Line>>();
		HashSet<Key<Line>> mlkSet2 = new HashSet<Key<Line>>();
		HashSet<Key<Line>> mlkSet1Inner = new HashSet<Key<Line>>();
		HashSet<Key<Line>> mlkSet2Inner = new HashSet<Key<Line>>();
		HashSet<Key<Line>> tabuBusesSet = new HashSet<Key<Line>>();
		for(PlanQuadrat pq : pqs1) {
			for(int i = 0; i < pq.getMainLineKeys().size(); i++) {
				Key<Line> k = pq.getMainLineKeys().get(i);
				if(!pq.getIgnore().get(i)) {
					if(!set1.containsKey(k) || pq.getIndicesMLK().get(i) > set1.get(k)) {
						set1.put(k, pq.getIndicesMLK().get(i));
						mlkSet1.add(k);
					}
					if(cellsInner.contains(pq.getGeoCell())) {
						mlkSet1Inner.add(k);
//						System.err.println("innerset1 - added " + k);
					}
				}
			}
			pqs_map.put(pq.getGeoCell(), pq);
			// System.err.print("\"" + pq.getGeoCell() + "\", ");
		}
		for(PlanQuadrat pq : pqs2) {
			for(int i = 0; i < pq.getMainLineKeys().size(); i++) {
				Key<Line> k = pq.getMainLineKeys().get(i);
				if(!pq.getIgnore().get(i)) {
					if(!set2.containsKey(k) || pq.getIndicesMLK().get(i) > set2.get(k)) {
						set2.put(k, pq.getIndicesMLK().get(i));
						mlkSet2.add(k);
					}
					if(cellsInner.contains(pq.getGeoCell())) {
						mlkSet2Inner.add(k);
//						System.err.println("innerset2 - added " + k);
					}
				}
			}
			pqs_map.put(pq.getGeoCell(), pq);
			// System.err.print("\"" + pq.getGeoCell() + "\", ");
		}
		// System.err.println();
		/*HashSet<Key<Line>> mlkSetInner = new HashSet<Key<Line>>(mlkSet1Inner);
		mlkSetInner.addAll(mlkSet2Inner);*/
		for(Key<Line> k : set1.keySet()) {
			if(set2.containsKey(k) ) {
				if(mlkSet1Inner.contains(k) && mlkSet2Inner.contains(k)) {
					tabuBusesSet.add(k);
//					System.err.println("Added to tabu-set because it is close: " + k);
				}
				if(set1.get(k) <= set2.get(k)) {
					connectingLines.add(new ConnectingLineHolder(k, set1.get(k), set2.get(k)));
					// Logger.getLogger("ListPointsServiceImpl").log(Level.INFO, functionName + ": Matching line: " + KeyFactory.keyToString(k1));
				} else {
					tabuBusesSet.add(k);
//					System.err.println("Added to tabu-set because it is in reverse: " + k);
				}
			}
		}
		Collections.sort(connectingLines);
		if(connectingLines.size() > 10) {
			connectingLines.subList(10, connectingLines.size()).clear();
		}
		Iterator<ConnectingLineHolder> il = connectingLines.iterator();
		List<ConnectionProxy> conns = new LinkedList<ConnectionProxy>();
		while(il.hasNext()) {
			ConnectingLineHolder temp = il.next();
			// System.err.println(Dao.getInstance().getLineByKey(temp, em));
			Point p1 = new Point("", lat1, lon1, null);
			Point p2 = new Point("", lat2, lon2, null);
			/* List<Point> points1 = Dao.getInstance().getPointsInBB(lat1+distNS, lon1+distWE, lat1-distNS, lon1-distWE, temp);
        List<Point> points2 = Dao.getInstance().getPointsInBB(lat2+distNS, lon2+distWE, lat2-distNS, lon2-distWE, temp);
		Point p1_min = closestPoint(p1, points1);
		Point p2_min = closestPoint(p2, points2);*/
			Collection<Point> points1 = Dao.getInstance().getSearchPointsForLine(temp.getLineKey(), temp.getIndex1(), 50, ofy);
			Collection<Point> points2 = Dao.getInstance().getSearchPointsForLine(temp.getLineKey(), temp.getIndex2(), 50, ofy);
			Point p1_min = closestPoint(p1, points1);
			Point p2_min = closestPoint(p2, points2);
			List<LineProxy> lps = new LinkedList<LineProxy>();
			lps.add(Utils.walk(p1, p1_min));
			LineProxy lp = Utils.getConnection(p1_min, p2_min, ofy);
			List<Key<Line>> alternativesK = new LinkedList<Key<Line>>();
			PlanQuadrat pq1 = pqs_map.get(Utils.computeGeoCell(p1_min));
			if(pq1 == null) {
				pq1 = Dao.getInstance().getPlanQuadrat(Utils.computeGeoCell(p1_min), ofy);
				pqs_map.put(pq1.getGeoCell(), pq1);
			}
			PlanQuadrat pq2 = pqs_map.get(Utils.computeGeoCell(p2_min));
			if(pq2 == null) {
				pq2 = Dao.getInstance().getPlanQuadrat(Utils.computeGeoCell(p2_min), ofy);
				pqs_map.put(pq2.getGeoCell(), pq2);
			}
			int counter = 0;
			for(Key<Line> k1 : pq1.getDirectLineKeys()) {
				if(!k1.equals(temp) // nicht der bus der eh schon genommen wird
						&& !pq1.getMainLineKeys().contains(k1) // keine Main Lines, weil die sowieso als eigene Route gefunden und angezeigt werden
						&& pq2.getDirectLineKeys().contains(k1) // in anfangs und end PQ gleichermaﬂen enthalten
						&& pq1.getIndices().get(pq1.getDirectLineKeys().indexOf(k1)) // aufsteigender Index
						<= pq2.getIndices().get(pq2.getDirectLineKeys().indexOf(k1))
						&& counter < 10) {
					alternativesK.add(k1);
					counter++;
					// Logger.getLogger("ListPointsServiceImpl").log(Level.INFO, functionName + ": Matching line: " + KeyFactory.keyToString(k1));
				}
			}
			for(Key<Line> k : alternativesK) {
				Line l = Dao.getInstance().getLineByKey(k, ofy);
				lp.addAlternativeLine(l.getLinenum() + " " + l.getRamal());
			}
			lps.add(lp);
			lps.add(Utils.walk(p2_min, p2));
			ConnectionProxy cp = new ConnectionProxy(lps);
			conns.add(cp);
		}
		for(Key<Line> k : tabuBusesSet) {
			mlkSet1.remove(k);
			mlkSet2.remove(k);
			mlkSet1Inner.remove(k);
			mlkSet2Inner.remove(k);
		}
		/*for(Key k : tabuBusesSet) {
		System.out.println("Tabu set contains: " + k);
	}*/
		if(connTren != null) {
			conns.add(connTren);
		}
		Collections.sort(conns, new ConnectionProxyComparator());
		List<String> mlkSet1String = new LinkedList<String>();
		List<String> mlkSet2String = new LinkedList<String>();
		if(mlkSet1Inner.size() >= 79) {
			for(Key<Line> k : mlkSet1Inner) {
				mlkSet1String.add("" + k.getId());
			}
		} else {
			for(Key<Line> k : mlkSet1) {
				mlkSet1String.add("" + k.getId());
			}
		}
		if(mlkSet2Inner.size() >= 79) {
			for(Key<Line> k : mlkSet2Inner) {
				mlkSet2String.add("" + k.getId());
			}
		} else {
			for(Key<Line> k : mlkSet2) {
				mlkSet2String.add("" + k.getId());
			}
		}
		if(conns.size() > 0) {
			return new SearchResultProxy(conns, mlkSet1String, mlkSet2String);
		} else {
			return new SearchResultProxy(SearchResultProxy.noResults, mlkSet1String, mlkSet2String);
		}
	}
	
	public SearchResultProxy getIndirectConnections(float lat1, float lon1, float lat2, float lon2, boolean ignoreTrains, boolean ignoreSubte, List<String> mlkSet1String, List<String> mlkSet2String) {
		if(mlkSet2String == null || mlkSet2String.size() == 0) {
			Logger.getLogger("ListPointsServiceImpl").log(Level.INFO, "getIndirectConnections(): mlkSet2 was empty, no search was done");
			return new SearchResultProxy(SearchResultProxy.noResults, null, null);
		} else {
			Objectify ofy = Dao.getInstance().getObjectify();
			HashSet<Key<Line>> tabuTrainsSet = new HashSet<Key<Line>>();
			if(ignoreTrains) {
				tabuTrainsSet.addAll(Dao.getTrainKeys());
			}
			if(ignoreSubte) {
				tabuTrainsSet.addAll(Dao.getSubteKeys());
			}
			List<ConnectionProxy> connsIndirect = new LinkedList<ConnectionProxy>();
			HashSet<Key<Line>> mlkSet = new HashSet<Key<Line>>();;
			for(String s : mlkSet1String) {
				mlkSet.add(new Key<Line>(Line.class, Long.parseLong(s)));
			}
			for(String s : mlkSet2String) {
				mlkSet.add(new Key<Line>(Line.class, Long.parseLong(s)));
			}
			ConnectionProxy result = Dao.getInstance().indirectSearch(new GeoPt(lat1, lon1), new GeoPt(lat2, lon2), tabuTrainsSet, mlkSet, ofy);
			if(result != null) {
				connsIndirect.add(result);
			}
			if(connsIndirect.size() > 0) {
				return new SearchResultProxy(connsIndirect, null, null);
			} else {
				return new SearchResultProxy(SearchResultProxy.noResults, null, null);
			}
		}
	}
	
	/*
	 * can be used when all points are only belonging to the same line anyway
	 */
	private static Point closestPoint(Point thePoint, Collection<Point> points) {
		Point p = thePoint; // may be passed as transient point without line, key, etc.
		double dMin = 99999999.9;
		Point pMin = null;
		for(Point pCur : points) {
			double distCur = Utils.distanceApprox(pCur, p);
			if(distCur < dMin) {
				dMin = distCur;
				pMin = pCur;
			}
		}
		return pMin;
	}
	
	public LoginInfo login(String requestUri) {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		LoginInfo loginInfo = new LoginInfo();

		if (user != null) {
			Objectify ofy = Dao.getInstance().getObjectify();
			loginInfo.setLoggedIn(true);
			loginInfo.setEmailAddress(user.getEmail());
			loginInfo.setNickname(user.getNickname());
			loginInfo.setLogoutUrl(userService.createLogoutURL(requestUri));
			Collection<UserFavouritePosition> favs = Dao.getInstance().getUserFavouritePositions(user, ofy);
			if(favs != null && favs.size() > 0) {
				List<UserFavouritePositionProxy> favourites = new LinkedList<UserFavouritePositionProxy>();
				for(UserFavouritePosition fav : favs) {
					favourites.add(new UserFavouritePositionProxy(fav.getName(), fav.getPos().getLatitude(), fav.getPos().getLongitude(), fav.getId().toString()));
				}
				loginInfo.setFavourites(favourites);
			}
		} else {
			loginInfo.setLoggedIn(false);
			loginInfo.setLoginUrl(userService.createLoginURL(requestUri));
		}
		return loginInfo;
	}
	
	public UserFavouritePositionProxy addFavourite(UserFavouritePositionProxy fpp) {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user != null) {
			Objectify ofy = Dao.getInstance().getObjectify();
			UserFavouritePosition toSave = new UserFavouritePosition(user, new GeoPt((float)fpp.getLat(), (float)fpp.getLon()), fpp.getName());
			Dao.getInstance().addUserFavouritePosition(toSave, ofy);
			return new UserFavouritePositionProxy(fpp.getName(), fpp.getLat(), fpp.getLon(), toSave.getId().toString());
		} else {
			return null;
		}
	}
	
	public String removeFavourite(String id) {
		UserService userService = UserServiceFactory.getUserService();
		User user = userService.getCurrentUser();
		if (user != null) {
			Objectify ofy = Dao.getInstance().getObjectify();
			Key<UserFavouritePosition> key = new Key<UserFavouritePosition>(UserFavouritePosition.class, Long.parseLong(id));
			Dao.getInstance().deleteUserFavouritePosition(key, user, ofy);
			return "success";
		} else {
			return "failed";
		}
	}
	
}
