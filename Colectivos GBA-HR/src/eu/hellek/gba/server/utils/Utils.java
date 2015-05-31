package eu.hellek.gba.server.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.beoui.geocell.GeocellUtils;
import com.beoui.geocell.model.BoundingBox;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Objectify;

import eu.hellek.gba.model.Line;
import eu.hellek.gba.model.Point;
import eu.hellek.gba.server.dao.Dao;
import eu.hellek.gba.shared.LineProxy;

public class Utils {
	
	public static final int geoCellResolution = 8;
	public static final int distanceMultiplikator = 100000;
	/*public static final float searchDistanceWE = 0.00580F; // about 500m each
	public static final float searchDistanceNS = 0.00478F;*/
	public static final float searchDistanceWE = 0.00870F; // about 750m each
	public static final float searchDistanceNS = 0.00717F;
	
	private static Set<String> allowedUsers = new HashSet<String>();
	
	static {
		allowedUsers.add("todo");
	}
	
	public static GeoPt vectorBetween(Point a, Point b) {
		GeoPt res = new GeoPt(a.getLatlon().getLatitude()-b.getLatlon().getLatitude(),a.getLatlon().getLongitude()-b.getLatlon().getLongitude());
		return res;
	}
	
	private static GeoPt vectorBetween(GeoPt a, Point b) {
		GeoPt res = new GeoPt(a.getLatitude()-b.getLatlon().getLatitude(),a.getLongitude()-b.getLatlon().getLongitude());
		return res;
	}
	
	public static double distanceApprox(double a, double b) {
		return Math.sqrt(Math.pow(a, 2)+ Math.pow(b, 2));
	}

	public static double distanceApprox(Point a, Point b) {
		GeoPt pt = vectorBetween(a, b);
		return distanceApprox(pt.getLatitude(), pt.getLongitude());
	}
	
	private static double distanceApprox(GeoPt a, Point b) {
		GeoPt pt = vectorBetween(a, b);
		return distanceApprox(pt.getLatitude(), pt.getLongitude());
	}
	
	public static double distanceInMeters(Point a, Point b) {
		com.beoui.geocell.model.Point p1 = new com.beoui.geocell.model.Point(a.getLatlon().getLatitude(),a.getLatlon().getLongitude());
		com.beoui.geocell.model.Point p2 = new com.beoui.geocell.model.Point(b.getLatlon().getLatitude(),b.getLatlon().getLongitude());
		return GeocellUtils.distance(p1, p2);
	}
	
	public static User getUser() {
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        return user;
	}
	
	public static String computeGeoCell(com.beoui.geocell.model.Point p) {
		return MyGeocellUtils.compute(p, Utils.geoCellResolution);
	}
	
	public static String computeGeoCell(Point p) {
		return Utils.computeGeoCell(new com.beoui.geocell.model.Point(p.getLatlon().getLatitude(), p.getLatlon().getLongitude()));
	}
	
	public static void eMailError(Throwable e, String where) {
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        String msgBody = "Error in " + where + ": " + e + "\n\n";
        msgBody += e.getCause() + "\n\n";
        StackTraceElement[] trace = e.getStackTrace();
        for(StackTraceElement ele : trace) {
        	msgBody += ele.toString() + "\n";
        }
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("todo", "App Engine"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress("todo", "name"));
            msg.setSubject("Error in " + where);
            msg.setText(msgBody);
            Transport.send(msg);
        } catch(Exception exMail) {
        	exMail.printStackTrace();
        }
	}
	
	public static void eMailGeneric(String text, String subject) {
		Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        String msgBody = text;
        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("todo", "App Engine"));
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress("todo", "todo"));
            msg.setSubject(subject);
            msg.setText(msgBody);
            Transport.send(msg);
        } catch(Exception exMail) {
        	exMail.printStackTrace();
        }
	}
	
	public static int distanceBetweenGeoCells(String cell1, String cell2) {
		return distanceBetweenGeoCells(cell1, cell2, false);
	}
	
	public static int distanceBetweenGeoCells(String cell1, String cell2, boolean overWeighDiagonale) {
		BoundingBox bb1 = MyGeocellUtils.computeBox(cell1);
		BoundingBox bb2 = MyGeocellUtils.computeBox(cell2);
//		com.beoui.geocell.model.Point p1 = bb1.getNorthEast();
//		com.beoui.geocell.model.Point p2 = bb2.getNorthEast();
		com.beoui.geocell.model.Point p1 = bb1.getSouthWest();
		com.beoui.geocell.model.Point p2 = bb2.getSouthWest();
//		System.out.println(p1.getLat() + ", " + p1.getLon());
//		System.out.println(p2.getLat() + ", " + p2.getLon());
		int dist1;
		int dist2;
		if(p1.getLat() >= p2.getLat() && p1.getLon() >= p2.getLon()) {
			// p1 is north-east of p2 or at same height/length
			com.beoui.geocell.model.Point p3 = new com.beoui.geocell.model.Point(p1.getLat(), p2.getLon());
			String cell3 = computeGeoCell(p3);
			dist1 = (MyGeocellUtils.interpolationCount(cell1, cell3)-1)*5;
			dist2 = (MyGeocellUtils.interpolationCount(cell3, cell2)-1)*6;
//			System.err.println(dist1 + " " + dist2);
		} else if(p1.getLat() < p2.getLat() && p1.getLon() < p2.getLon()) {
			// p1 is south-west of p2
			com.beoui.geocell.model.Point p3 = new com.beoui.geocell.model.Point(p1.getLat(), p2.getLon());
			String cell3 = computeGeoCell(p3);
			dist1 = (MyGeocellUtils.interpolationCount(cell3, cell1)-1)*5;
			dist2 = (MyGeocellUtils.interpolationCount(cell2, cell3)-1)*6;
//			System.err.println(dist1 + " " + dist2);
		} else if(p1.getLat() >= p2.getLat() && p1.getLon() < p2.getLon()) {
			// p1 is north-west of p2
			com.beoui.geocell.model.Point p3 = new com.beoui.geocell.model.Point(p1.getLat(), p2.getLon());
			String cell3 = computeGeoCell(p3);
			dist1 = (MyGeocellUtils.interpolationCount(cell3, cell1)-1)*5;
			dist2 = (MyGeocellUtils.interpolationCount(cell3, cell2)-1)*6;
//			System.err.println(dist1 + " " + dist2);
		} else if(p1.getLat() < p2.getLat() && p1.getLon() >= p2.getLon()) {
			// p1 is south-east of p2
			com.beoui.geocell.model.Point p3 = new com.beoui.geocell.model.Point(p1.getLat(), p2.getLon());
			String cell3 = computeGeoCell(p3);
			dist1 = (MyGeocellUtils.interpolationCount(cell1, cell3)-1)*5;
			dist2 = (MyGeocellUtils.interpolationCount(cell2, cell3)-1)*6;
//			System.err.println(dist1 + " " + dist2);
		} else {
			System.err.println("Error in distanceBetweenGeoCells!!! Cells: " + cell1 + " and " + cell2);
			return -1;
		}
		if(overWeighDiagonale) {
			return (int)pythagorasOverWeighDiagonale(dist1, dist2);
		} else {
			return (int)pythagoras(dist1, dist2);
		}
	}
	
	public static Point closestPoint(GeoPt thePoint, Collection<Point> points) {
		GeoPt p = thePoint;
		double dMin = 99999999.9;
		Point pMin = null;
		for(Point pCur : points) {
			double distCur = Utils.distanceApprox(p, pCur);
			if(pCur.isIgnore()) { // absolutely avoid points that are set as ignore. Related to the dirty hack that searches any point, if none were found that are not "ignore"-flagged. Theoretically there shouldn't be any case where this is necessary, but right now there is (rarely).
				distCur += 10000;
			}
			if(distCur < dMin) {
				dMin = distCur;
				pMin = pCur;
			}
		}
		return pMin;
	}
	
	public static double pythagoras(int a, int b) {
		return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
	}
	
	public static double pythagorasOverWeighDiagonale(int a, int b) {
		double res = pythagoras(a, b);
		if(a != 0 && b != 0) {
			res = res * 1.35;
		}
		return res;
	}
	
	/*
	 * Get the connection between two points of the same line. The index of the second point must be higher
	 * than the index of the first point
	 */
	public static LineProxy getConnection(Point p1, Point p2, Objectify ofy) {
		Line line = Dao.getInstance().getLineByKey(p1.getOwner(), ofy);
		List<Float> floats = new LinkedList<Float>();
		floats.add(p1.getLatlon().getLatitude());
		floats.add(p1.getLatlon().getLongitude());
		double trackdist = 0.0;
		Point lastpoint = p1;
		Collection<Point> points = Dao.getInstance().getPointsToDisplayForLine(line, ofy);
		for(Point ppt : points) {	
			if(ppt.getIndex() > p1.getIndex()) {
				if(ppt.getIndex() < p2.getIndex()) {
					floats.add(ppt.getLatlon().getLatitude());
					floats.add(ppt.getLatlon().getLongitude());
					trackdist += Utils.distanceInMeters(lastpoint, ppt);
					lastpoint = ppt;
				} else {
					break;
				}
			}
		}
		floats.add(p2.getLatlon().getLatitude());
		floats.add(p2.getLatlon().getLongitude());
		trackdist += Utils.distanceInMeters(lastpoint, p2);
		
		int trackdist_int = (int)Math.round(trackdist);

		List<Float> coords = new ArrayList<Float>();
		List<String> stations = null;
		if(line.getType() >= 10) {
			stations = new ArrayList<String>();
			for(Point p : points) {
				coords.add(p.getLatlon().getLatitude());
				coords.add(p.getLatlon().getLongitude());
				stations.add(p.getStreet());
			}
		} else {
			for(Point p : points) {
				coords.add(p.getLatlon().getLatitude());
				coords.add(p.getLatlon().getLongitude());
			}
		}
		int type;
		if(line.getType() == 11) {
			type = 2;
		} else if(line.getType() == 21) {
			type = 3;
		} else if(line.getType() == 13) {
			type = 4;
		} else if(line.getType() == 15) {
			type = 5;
		} else {
			type = 1;
		}
		LineProxy lp = new LineProxy(line.getId().toString(), line.getLinenum(), line.getRamal(), type, floats, trackdist_int, coords, stations);
		lp.setStartStreet(p1.getStreet());
		lp.setDestStreet(p2.getStreet());
		return lp;
	}
	
	public static LineProxy walk(Point p1, Point p2) {
		int walkingDistance = (int)Math.round(Utils.distanceInMeters(p1, p2));
		List<Float> walkingPoints = new LinkedList<Float>();
		walkingPoints.add(p1.getLatlon().getLatitude());
		walkingPoints.add(p1.getLatlon().getLongitude());
		walkingPoints.add(p2.getLatlon().getLatitude());
		walkingPoints.add(p2.getLatlon().getLongitude());
		return new LineProxy(null, "caminar", walkingDistance + "m", 0, walkingPoints, walkingDistance, walkingPoints);
	}
	
	public static boolean isUserInSpecialACL() {
		User user = Utils.getUser();
		if(user == null) {
			return false;
		} else if(user.getEmail() == null) {
			return false;
		} else {
			String eMail = user.getEmail();
			if(allowedUsers.contains(eMail)) {
				return true;
			} else {
				return false;
			}
		}
	}
}
