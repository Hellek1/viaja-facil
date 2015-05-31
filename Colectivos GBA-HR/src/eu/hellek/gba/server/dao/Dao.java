package eu.hellek.gba.server.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

import com.beoui.geocell.model.BoundingBox;
import com.beoui.geocell.model.Tuple;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;
import com.googlecode.objectify.util.DAOBase;

import eu.hellek.gba.model.Line;
import eu.hellek.gba.model.PQA;
import eu.hellek.gba.model.PlanQuadrat;
import eu.hellek.gba.model.Point;
import eu.hellek.gba.model.RootEntity;
import eu.hellek.gba.model.TrainNode;
import eu.hellek.gba.model.UserFavouritePosition;
import eu.hellek.gba.server.holders.WayHolder;
import eu.hellek.gba.server.utils.AStarImpl;
import eu.hellek.gba.server.utils.AStarNode;
import eu.hellek.gba.server.utils.AStarNodeImpl;
import eu.hellek.gba.server.utils.MyCostFunction;
import eu.hellek.gba.server.utils.MyGeocellUtils;
import eu.hellek.gba.server.utils.Utils;
import eu.hellek.gba.shared.ConnectionProxy;
import eu.hellek.gba.shared.LineProxy;

/**
 * 
 * @author David
 */
public final class Dao extends DAOBase {
	
	static {
        ObjectifyService.register(Line.class);
        ObjectifyService.register(Point.class);
        ObjectifyService.register(PlanQuadrat.class);
        ObjectifyService.register(PQA.class);
        ObjectifyService.register(TrainNode.class);
        ObjectifyService.register(UserFavouritePosition.class);
        ObjectifyService.register(RootEntity.class);
    }
	
	private static Dao instance;
	private static String location = "Dao";
	private static HashMap<String,Set<TrainNode>> trainNodes; 
	private static HashMap<Key<TrainNode>,TrainNode> mapTrainNodeKeyToNode;
	private static List<Key<Line>> trainKeys;
	private static List<Key<Line>> subteKeys;
	private static Key<RootEntity> rootEntityBus;
	private static Key<RootEntity> rootEntityTrain;
	private static Key<RootEntity> rootEntityPQA;
	private static Map<String, HashMap<String, PQA>> mapBusCache;
	
	private Dao() {	}
	
	public static synchronized Dao getInstance() {
		if(instance == null) {
			instance = new Dao();
			mapBusCache = Collections.synchronizedMap(new MapBusCached<String, HashMap<String, PQA>>(500));
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized HashMap<String,Set<TrainNode>> getTrainNodes() {
		String functionName = "getTrainNodes()";
		if(trainNodes == null || trainNodes.size() == 0) {
			trainNodes = new HashMap<String,Set<TrainNode>>();
			Objectify ofy = ObjectifyService.begin();
			Query<TrainNode> q = ofy.query(TrainNode.class);
			List<Key<TrainNode>> keys;
			try {
	        	Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
	        	keys = (List<Key<TrainNode>>)cache.get(q.toString());
	        	if(keys == null) {
					keys = q.listKeys();
					cache.put(q.toString(), keys);
				}
	        } catch (CacheException e) {
	        	keys = q.listKeys();
	        	Logger.getLogger(location).log(Level.SEVERE, functionName + ": Cache error: " + e);
	        	e.printStackTrace();
	        }
	        Map<Key<TrainNode>, TrainNode> res = ofy.get(keys);
			Collection<TrainNode> tns = res.values();
			Logger.getLogger(location).log(Level.INFO, functionName + ": Got " + res.size() + " TrainNodes. keys.size(): " + keys.size());			
//			String m = "";
			for(TrainNode tn : tns) {
				if(!trainNodes.containsKey(tn.getGeoCell())) {
					trainNodes.put(tn.getGeoCell(), new HashSet<TrainNode>());
				}
				trainNodes.get(tn.getGeoCell()).add(tn);
/*				if(tn.getLineKey().equals(new Key<Line>(Line.class, 155))) {
//				if(tn.getLineType() == 11) {
					System.err.print("\"" + tn.getGeoCell() + "\", ");
				}*/
			}
//			Utils.eMailGeneric(m, "DaoTemp");
		}
		return trainNodes;
	}
	
	public static synchronized HashMap<Key<TrainNode>,TrainNode> getTrainNodeKeyMap() {
		String functionName = "getTrainNodeKeyMap()";
		Key<RootEntity> re = Dao.getRootEntityTrain();
		if(mapTrainNodeKeyToNode == null || mapTrainNodeKeyToNode.size() == 0) {
			HashMap<String,Set<TrainNode>> mapTrain = Dao.getTrainNodes();
			mapTrainNodeKeyToNode = new HashMap<Key<TrainNode>,TrainNode>();
			for(Set<TrainNode> tnl : mapTrain.values()) {
				for(TrainNode tn : tnl) {
					mapTrainNodeKeyToNode.put(new Key<TrainNode>(re, TrainNode.class, tn.getId()), tn);
				}
			}
			Logger.getLogger(location).log(Level.INFO, functionName + ": served new mapTrainNodeKeyToCell. #" + mapTrainNodeKeyToNode.size());
		}
		return mapTrainNodeKeyToNode;
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized List<Key<Line>> getSubteKeys() {
		String functionName = "getSubteKeys()";
		if(subteKeys == null || subteKeys.size() == 0) {
			Objectify ofy = ObjectifyService.begin();
			Query<Line> q = ofy.query(Line.class).filter("type", 11);
			List<Key<Line>> keys;
			try {
	        	Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
	        	keys = (List<Key<Line>>)cache.get(q.toString());
	        	if(keys == null) {
					keys = q.listKeys();
					cache.put(q.toString(), keys);
				}
	        } catch (CacheException e) {
	        	keys = q.listKeys();
	        	Logger.getLogger(location).log(Level.SEVERE, functionName + ": Cache error: " + e);
	        	e.printStackTrace();
	        }
			subteKeys = keys;
			Logger.getLogger(location).log(Level.INFO, functionName + ": served new subteKeys. #" + subteKeys.size());
		}
		return subteKeys;
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized List<Key<Line>> getTrainKeys() {
		String functionName = "getTrainKeys()";
		if(trainKeys == null || trainKeys.size() == 0) {
			Objectify ofy = ObjectifyService.begin();
			Query<Line> q = ofy.query(Line.class).filter("type", 21);
			List<Key<Line>> keys;
			try {
	        	Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
	        	keys = (List<Key<Line>>)cache.get(q.toString());
	        	if(keys == null) {
					keys = q.listKeys();
					cache.put(q.toString(), keys);
				}
	        } catch (CacheException e) {
	        	keys = q.listKeys();
	        	Logger.getLogger(location).log(Level.SEVERE, functionName + ": Cache error: " + e);
	        	e.printStackTrace();
	        }
			trainKeys = keys;
			Logger.getLogger(location).log(Level.INFO, functionName + ": served new trainKeys. #" + trainKeys.size());
		}
		return trainKeys;
	}
	
	public static synchronized void resetTrainNodes() {
    	Objectify ofy = ObjectifyService.begin();
    	Query<TrainNode> q = ofy.query(TrainNode.class);
    	Dao.getInstance().removeFromCache(q.toString());
		trainNodes = null;
		mapTrainNodeKeyToNode = null;
		trainKeys = null;
		subteKeys = null;
		mapBusCache = Collections.synchronizedMap(new MapBusCached<String, HashMap<String, PQA>>(500));
		System.err.println("resetTrainNodes was executed");
	}
	
	public synchronized static Key<RootEntity> getRootEntityBus() {
		/*if(rootEntityBus == null) {
			Objectify ofy = ObjectifyService.begin();
			Query<RootEntity> q = ofy.query(RootEntity.class).filter("type", 1);
			Key<RootEntity> result = q.getKey();
			if(result == null) {
				rootEntityBus = ofy.put(new RootEntity(1));
			} else {
				rootEntityBus = result;
			}
		}*/
		if(rootEntityBus == null) {
			rootEntityBus = new Key<RootEntity>(RootEntity.class, 4);
		}
		return rootEntityBus;
	}
	
	public synchronized static Key<RootEntity> getRootEntityTrain() {
		/*if(rootEntityTrain == null) {
			Objectify ofy = ObjectifyService.begin();
			Query<RootEntity> q = ofy.query(RootEntity.class).filter("type", 2);
			Key<RootEntity> result = q.getKey();
			if(result == null) {
				rootEntityTrain = ofy.put(new RootEntity(2));
			} else {
				rootEntityTrain = result;
			}
		}*/
		if(rootEntityTrain == null) {
			rootEntityTrain = new Key<RootEntity>(RootEntity.class, 1);
		}
		return rootEntityTrain;
	}
	
	public synchronized static Key<RootEntity> getRootEntityPQA() {
		if(rootEntityPQA == null) {
			rootEntityPQA = new Key<RootEntity>(RootEntity.class, 6);
		}
		return rootEntityPQA;
	}
	
	private void removeFromCache(Object key) {
		try {
			Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
        	cache.remove(key);
        } catch (CacheException e) {
        	Logger.getLogger(location).log(Level.SEVERE, "removeFromCache(): Cache error: " + e);
        	e.printStackTrace();
        }
	}
	
	public ConnectionProxy indirectSearch(GeoPt start, GeoPt dest, Set<Key<Line>> tabuTrainsSet, Set<Key<Line>> mlkSet, Objectify ofy) {
		final String functionName = "newIndirectSearch()";
		final int plusMinus = 8;
		final int maxAlternatives = 8;
		HashSet<Key<PQA>> PQsForAStar = new HashSet<Key<PQA>>();
		Logger.getLogger(location).log(Level.INFO, functionName + ": " + "fetching PQAs for " + mlkSet.size() + " lines.");
		try {
			Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
			for(Key<Line> k : mlkSet) {
				Query<PQA> q = ofy.query(PQA.class).filter("lineKeys", k);
				@SuppressWarnings("unchecked")
				List<Key<PQA>> rl = (List<Key<PQA>>)cache.get(q.toString());
				if(rl == null) {
					rl = q.listKeys();
					cache.put(q.toString(), rl);
				}
				PQsForAStar.addAll(rl);
			}
        } catch (CacheException e) {
        	Logger.getLogger(location).log(Level.SEVERE, functionName + ": Cache error: " + e);
        	e.printStackTrace();
        }
		Logger.getLogger(location).log(Level.FINER, functionName + ": " + "Got " + PQsForAStar.size() + " PQs.");
		/*for(PlanQuadrat pq : PQsForAStar) {
    			System.err.print("\"" + pq.getGeoCell() + "\",");
    		}
    		System.err.println();*/
		HashSet<String> setBusKey = new HashSet<String>();
		for(Key<PQA> pq : PQsForAStar) {
			setBusKey.add(pq.getName());
		}
//		HashMap<String, PlanQuadrat> mapBus = new HashMap<String, PlanQuadrat>();
		WayHolder wh = new AStarImpl().aStarSearch(Utils.computeGeoCell(new com.beoui.geocell.model.Point(start.getLatitude(), start.getLongitude())), Utils.computeGeoCell(new com.beoui.geocell.model.Point(dest.getLatitude(), dest.getLongitude())), setBusKey, mlkSet, tabuTrainsSet, ofy); // erste Cell jeweils aus Liste Start und Liste Dest Cells. das gehört noch überarbeitet
		/*
    		for(AStarNode asn : wh.getWay()) {
    			System.err.print("\"" + asn.getGeoCell() + "\", ");
    		}
    		System.err.println();
    		for(AStarNode asn : wh.getWay()) {
    			System.err.print("\"" + asn.getOwningLine() + "\", ");
    		}
    		System.err.println();*/
		if(wh != null && wh.getWay().size() > 1) {
			List<AStarNode> way = wh.getWay();
			List<AStarNode> umsteigen = wh.getCombinationPoints();
			int index = 0;
			if(way.get(0).getOwningLine() == null) {
				index = 1;
			}
			Collection<Point> pointsStart;
			if(way.get(index).getClass() == AStarNodeImpl.class) {
				pointsStart = getSearchPointsForLine(way.get(index).getOwningLine(), way.get(index).getPointGeoCell(), plusMinus, ofy);
			} else {
				Line tempLine = getLineByKey(way.get(index).getOwningLine(), ofy);
				if(tempLine.getType() == 11 || tempLine.getType() == 13 || tempLine.getType() == 15) {
					pointsStart = getSearchPointsForLine(way.get(index).getOwningLine(), way.get(index).getPointGeoCell(), 1, ofy);
				} else {
					pointsStart = getSearchPointsForLine(way.get(index).getOwningLine(), way.get(index).getPointGeoCell(), 0, ofy);
				}
			}
//			System.err.println("pointsStart.size: " + pointsStart.size());
			Point startPoint = Utils.closestPoint(start, pointsStart);
			List<LineProxy> lineProxies = new LinkedList<LineProxy>();
			LineProxy walk = Utils.walk(new Point(null, start.getLatitude(), start.getLongitude(), null), startPoint); 
			lineProxies.add(walk);

			Point lastPoint = startPoint;
			AStarNode lastNode = way.get(index);
			for(int i = index; i < way.size(); i++) {
				AStarNode an = way.get(i);
				if(umsteigen.contains(an)) {
					Collection<Point> pointsLine1;
					if(an.getClass() == AStarNodeImpl.class) {
						pointsLine1 = getSearchPointsForLine(lastPoint.getOwner(), an.getPointGeoCell(), plusMinus, ofy);
					} else {
						Line tempLine = getLineByKey(an.getOwningLine(), ofy);
						if(tempLine.getType() == 11 || tempLine.getType() == 13 || tempLine.getType() == 15) {
							pointsLine1 = getSearchPointsForLine(an.getOwningLine(), an.getPointGeoCell(), 1, ofy);
						} else {
							pointsLine1 = getSearchPointsForLine(an.getOwningLine(), an.getPointGeoCell(), 0, ofy);
						}
					}
					AStarNode next = way.get(i+1);
					Collection<Point> pointsLine2;
					if(next.getClass() == AStarNodeImpl.class) {
//						System.err.println("Line: " + next.getOwningLine() + " / " + Dao.getInstance().getLineByKey(next.getOwningLine(), ofy) + " / " + next.getPointGeoCell());
						pointsLine2 = getSearchPointsForLine(next.getOwningLine(), next.getPointGeoCell(), plusMinus, ofy);
					} else {
						Line tempLine = getLineByKey(next.getOwningLine(), ofy);
						if(tempLine.getType() == 11 || tempLine.getType() == 13 || tempLine.getType() == 15) {
							pointsLine2 = getSearchPointsForLine(next.getOwningLine(), next.getPointGeoCell(), 1, ofy);
						} else {
							pointsLine2 = getSearchPointsForLine(next.getOwningLine(), next.getPointGeoCell(), 0, ofy);
						}
					}
					Logger.getLogger(location).log(Level.FINE, functionName + ": Umsteigen von " + an.getOwningLine() + " zu " + next.getOwningLine() + " in " + an.getPointGeoCell() + " und " + next.getPointGeoCell() + ". Results: " + pointsLine1.size() + " und " + pointsLine2.size());

					Iterator<Point> j1 = pointsLine1.iterator();
					double min_distance = 999999999.9;
					Tuple<Point,Point> tuple_min = null;
					while(j1.hasNext()) {
						Point outerPoint = j1.next();
						Iterator<Point> j2 = pointsLine2.iterator();
						while(j2.hasNext()) { // für jeden Punkt (innerhalb der errechneten cells) von Line inner distanz zu jedem Punkt von outer berechnen, das kürzeste Paar speichern
							Point innerPoint = j2.next();
							double distance = Utils.distanceApprox(innerPoint, outerPoint);
							if(innerPoint.isIgnore() || outerPoint.isIgnore()) { // absolutely avoid points that are set as ignore. Related to the dirty hack that searches any point, if none were found that are not "ignore"-flagged. Theoretically there shouldn't be any case where this is necessary, but right now there is (rarely).
								distance += 10000;
							}
							if(distance < min_distance) {
								min_distance = distance;
								tuple_min = new Tuple<Point,Point>(outerPoint,innerPoint);
							}
						}
					}
					LineProxy c = Utils.getConnection(lastPoint, tuple_min.getFirst(), ofy);
					lineProxies.add(c);
					Line lastPointOwner1 = ofy.get(lastPoint.getOwner());
					if(lastPointOwner1.getType() == 1) { // wenn bus, alternativen suchen
						List<Key<Line>> alternativesK = new LinkedList<Key<Line>>();
						PlanQuadrat pq1 = Dao.getInstance().getPlanQuadrat(lastPoint.getDefaultGeoCell(), ofy);
						PlanQuadrat pq2 = Dao.getInstance().getPlanQuadrat(tuple_min.getFirst().getDefaultGeoCell(), ofy);
						int counter = 0;
						for(Key<Line> k1 : pq1.getDirectLineKeys()) {
							if(k1.getId() != lastPoint.getOwner().getId() // nicht der bus der eh schon genommen wird
									&& pq2.getDirectLineKeys().contains(k1) // in anfangs und end PQ gleichermaßen enthalten
									&& pq1.getIndices().get(pq1.getDirectLineKeys().indexOf(k1)) 
									<= pq2.getIndices().get(pq2.getDirectLineKeys().indexOf(k1))
									&& counter < maxAlternatives) {
								alternativesK.add(k1);
								counter++;
								// Logger.getLogger("ListPointsServiceImpl").log(Level.INFO, functionName + ": Matching line: " + KeyFactory.keyToString(k1));
							}
						}
						for(Key<Line> k : alternativesK) {
							Line l = getLineByKey(k, ofy);
							c.addAlternativeLine(l.getLinenum() + " " + l.getRamal());
						}
					} else if (lastPointOwner1.getType() == 21) { // auch für züge alternativen suchen
						List<Key<Line>> alternativesK = new LinkedList<Key<Line>>();
						Set<TrainNode> tns1 = Dao.getTrainNodes().get(lastNode.getGeoCell());
						Set<TrainNode> tns2 = Dao.getTrainNodes().get(an.getGeoCell());
						for(TrainNode tn1 : tns1) {
							if(!tn1.getLineKey().equals(lastPoint.getOwner())) { // nicht der Zug der eh schon genommen wird
								for(TrainNode tn2 : tns2) {
									if(tn1.getLineKey().equals(tn2.getLineKey()) // gehören zum gleichen zug
											&& tn1.getIndex() < tn2.getIndex()) { // und der index steig
										alternativesK.add(tn1.getLineKey());
									}
								}
							}
						}
						for(Key<Line> k : alternativesK) {
							Line l = getLineByKey(k, ofy);
							if(!l.getRamal().equals(c.getRamal())) {
								c.addAlternativeLine("Ramal a " + l.getRamal());
							}
						}
					}
					walk = Utils.walk(tuple_min.getFirst(), tuple_min.getSecond()); 
					lineProxies.add(walk);					
					lastPoint = tuple_min.getSecond();
					lastNode = next;
				}
			}
			index = way.size()-1;
			if(way.get(index).getOwningLine() == null) {
				index = way.size()-2;
			}
			Collection<Point> pointsDest;
			if(way.get(index).getClass() == AStarNodeImpl.class) {
				pointsDest = getSearchPointsForLine(way.get(index).getOwningLine(), way.get(index).getPointGeoCell(), plusMinus, ofy);
			} else {
				Line tempLine = getLineByKey(way.get(index).getOwningLine(), ofy);
				if(tempLine.getType() == 11 || tempLine.getType() == 13 || tempLine.getType() == 15) {
					pointsDest = getSearchPointsForLine(way.get(index).getOwningLine(), way.get(index).getPointGeoCell(), 1, ofy);
				} else {
					pointsDest = getSearchPointsForLine(way.get(index).getOwningLine(), way.get(index).getPointGeoCell(), 0, ofy);
				}
			}
			Point destPoint = Utils.closestPoint(dest, pointsDest);
			LineProxy c = Utils.getConnection(lastPoint, destPoint, ofy);
			lineProxies.add(c);
			Line lastPointOwner2 = ofy.get(lastPoint.getOwner());
			if(lastPointOwner2.getType() == 1) { // wenn bus, alternativen suchen
				List<Key<Line>> alternativesK = new LinkedList<Key<Line>>();
				PlanQuadrat pq1 = Dao.getInstance().getPlanQuadrat(lastPoint.getDefaultGeoCell(), ofy);
				PlanQuadrat pq2 = Dao.getInstance().getPlanQuadrat(destPoint.getDefaultGeoCell(), ofy);
				int counter = 0;
				for(Key<Line> k1 : pq1.getDirectLineKeys()) {
					if(k1.getId() != lastPoint.getOwner().getId() // nicht der bus der eh schon genommen wird
							&& pq2.getDirectLineKeys().contains(k1) // in anfangs und end PQ gleichermaßen enthalten
							&& pq1.getIndices().get(pq1.getDirectLineKeys().indexOf(k1)) 
							<= pq2.getIndices().get(pq2.getDirectLineKeys().indexOf(k1))
							&& counter < maxAlternatives) {
						alternativesK.add(k1);
						counter++;
						// Logger.getLogger("ListPointsServiceImpl").log(Level.INFO, functionName + ": Matching line: " + KeyFactory.keyToString(k1));
					}
				}
				for(Key<Line> k : alternativesK) {
					Line l = getLineByKey(k, ofy);
					c.addAlternativeLine(l.getLinenum() + " " + l.getRamal());
				}
			} else if (lastPointOwner2.getType() == 21) { // auch für züge alternativen suchen
//				System.err.println(lastPoint.getOwner() + " " + lastPointOwner2 + " " + lastPoint.getDefaultGeoCell());
				List<Key<Line>> alternativesK = new LinkedList<Key<Line>>();
				Set<TrainNode> tns1 = Dao.getTrainNodes().get(lastNode.getGeoCell());
				Set<TrainNode> tns2 = Dao.getTrainNodes().get(way.get(index).getGeoCell());
				for(TrainNode tn1 : tns1) {
					if(!tn1.getLineKey().equals(lastPoint.getOwner())) { // nicht der Zug der eh schon genommen wird
						for(TrainNode tn2 : tns2) {
							if(tn1.getLineKey().equals(tn2.getLineKey()) // gehören zum gleichen zug
									&& tn1.getIndex() < tn2.getIndex()) { // und der index steig
								alternativesK.add(tn1.getLineKey());
							}
						}
					}
				}
				for(Key<Line> k : alternativesK) {
					Line l = getLineByKey(k, ofy);
					if(!l.getRamal().equals(c.getRamal())) {
						c.addAlternativeLine("Ramal " + l.getRamal());
					}
				}
			}

			walk = Utils.walk(destPoint, new Point(null, dest.getLatitude(), dest.getLongitude(), null)); 
			lineProxies.add(walk);

			ConnectionProxy cp = new ConnectionProxy(lineProxies);
			return cp;
		} else {
			Logger.getLogger(location).log(Level.INFO, functionName + ": no indirect connection found.");
			return null;
		}
	}
	
	public Line getLineByKey(Key<Line> line, Objectify ofy) {
		return ofy.find(line);
	}
	
	public PlanQuadrat getPlanQuadrat(String cell, Objectify ofy) {
		return ofy.find(new Key<PlanQuadrat>(Dao.getRootEntityBus(), PlanQuadrat.class, cell));
	}
	
	/*private PQA getPQA(String cell, Key<RootEntity> re, Objectify ofy) {
		return ofy.find(new Key<PQA>(re, PQA.class, cell));
	}*/
	
	public PQA getPQA(String geoCell, Objectify ofy) {
		String shortened = geoCell.substring(0, geoCell.length() - 2);
		if(mapBusCache.containsKey(shortened)) {
			// System.err.println("getting cached pqa");
			return mapBusCache.get(shortened).get(geoCell);
		} else {
			try {
				Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
				@SuppressWarnings("unchecked")
				HashMap<String, PQA> list = (HashMap<String, PQA>)cache.get("PQAsCache"+shortened);
				if(list == null) {
					list = generatePQAsCombination(shortened, ofy);
					cache.put("PQAsCache"+shortened, list);
//					System.out.print("was not in cache ... ");
				}
//				System.out.println("got a result " + list.size());
				mapBusCache.put(shortened, list);
				return list.get(geoCell);
			} catch (CacheException e) {
				Logger.getLogger(location).log(Level.INFO, "getPQA(): CacheException: " + e);
				HashMap<String, PQA> list = generatePQAsCombination(shortened, ofy);
				mapBusCache.put(shortened, list);
				return list.get(geoCell);
			}
		}
	}
	
	/*
	 * returns a Map of up to 32 PQAs. The key of each entry is it's geoCell. They all share the same prefix, only the last two characters differ
	 */
	private HashMap<String, PQA> generatePQAsCombination(String shortened, Objectify ofy) {
		Key<RootEntity> re = Dao.getRootEntityPQA();
		List<Key<PQA>> pqas_to_fetch = new ArrayList<Key<PQA>>(32);
		for(int i = 0; i <= 9; i++) {
			pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + i + "l"));
			pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + i + "r"));
		}
		pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + "al"));
		pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + "ar"));
		pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + "bl"));
		pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + "br"));
		pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + "cl"));
		pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + "cr"));
		pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + "dl"));
		pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + "dr"));
		pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + "el"));
		pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + "er"));
		pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + "fl"));
		pqas_to_fetch.add(new Key<PQA>(re, PQA.class, shortened + "fr"));
		Map<Key<PQA>, PQA> pqas = ofy.get(pqas_to_fetch);
//		System.err.println("Got " + pqas.size());
		HashMap<String, PQA> pqas2 = new HashMap<String, PQA>(pqas.size());
		for(PQA pqa : pqas.values()) {
			pqas2.put(pqa.getId(), pqa);
		}
		return pqas2;
	}
	
	/*
	 * returns the Points of a line which are from the original import, without the intermediate points that are created for searches
	 * caches the keys of the query result so that the entities can be read through a batch get (which checks memcache first). this caching should be evaluated and removed if it does not work out
	 */
	@SuppressWarnings("unchecked")
	public Collection<Point> getPointsToDisplayForLine(Line l, Objectify ofy) {
		String functionName = "getPointsForLine()";
        List<Key<Point>> keys;
        Query<Point> q = ofy.query(Point.class).ancestor(l).filter("forSearchOnly", false).order("index");
        try {
        	Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
        	keys = (List<Key<Point>>)cache.get(q.toString());
			if(keys == null) {
				keys = q.listKeys();
				cache.put(q.toString(), keys);
			}
        } catch (CacheException e) {
        	keys = q.listKeys();
        	Logger.getLogger(location).log(Level.SEVERE, functionName + ": Cache error: " + e);
        	e.printStackTrace();
        }
        Map<Key<Point>, Point> points = ofy.get(keys);
		return points.values();
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Point> getSearchPointsForLine(Key<Line> l, String geoCell, int plusMinusIndex, Objectify ofy) {
        String functionName = "getSearchPointsForLine(String geoCell)";
        Query<Point> q = ofy.query(Point.class).ancestor(l).filter("ignore", false).filter("defaultGeoCell", geoCell).limit(1);
        Key<Point> kMiddle;
        try {
        	Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
        	kMiddle = (Key<Point>)cache.get(q.toString());
        	if(kMiddle == null) {
        		kMiddle = q.getKey();
				cache.put(q.toString(), kMiddle);
			}
        } catch (CacheException e) {
        	kMiddle = q.getKey();
        	Logger.getLogger(location).log(Level.SEVERE, functionName + ": Cache error: " + e);
        	e.printStackTrace();
        }
        if(kMiddle == null) { // dirty hack, but still better than failing in case that no point was found
        	q = ofy.query(Point.class).ancestor(l).filter("defaultGeoCell", geoCell).limit(1);
            kMiddle = q.getKey();
            System.err.println("had to resort to dirty hack and retrieve a point that is set to ignore for line " + l + " in cell " + geoCell + ". Result: " + kMiddle);
        }
        try {
	        Point middle = ofy.get(kMiddle);
	        Collection<Point> points;
	        if(plusMinusIndex > 0) {
	        	points = getSearchPointsForLine(l, middle.getIndex(), plusMinusIndex, ofy);
	        } else {
	        	points = new LinkedList<Point>();
	        	points.add(middle);
	        }
	        return points;
        } catch (NullPointerException e) {
        	q = ofy.query(Point.class).ancestor(l).filter("ignore", false);
        	System.err.println("because of " + e + " had to resort to even dirtier hack and retrieve all potential points for line " + l + ", even those not in cell " + geoCell);
        	return q.list();
        }
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Point> getSearchPointsForLine(Key<Line> l, int middleIndex, int plusMinusIndex, Objectify ofy) {
        String functionName = "getSearchPointsForLine(int plusMinusIndex)";
        List<Key<Point>> keys;
        plusMinusIndex++; // query is exclusive, therefore we expand by one to return the expected number of results
        Query<Point> q = ofy.query(Point.class).ancestor(l).filter("ignore", false).filter("index <", middleIndex + plusMinusIndex).filter("index >", middleIndex - plusMinusIndex);
        try {
        	Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
        	keys = (List<Key<Point>>)cache.get(q.toString());
        	if(keys == null) {
				keys = q.listKeys();
				cache.put(q.toString(), keys);
			}
        } catch (CacheException e) {
        	keys = q.listKeys();
        	Logger.getLogger(location).log(Level.SEVERE, functionName + ": Cache error: " + e);
        	e.printStackTrace();
        }
        Map<Key<Point>, Point> points = ofy.get(keys);
        return points.values();
	}
	
	public Collection<PlanQuadrat> getPQsInBB(float latN, float lonE, float latS, float lonW, Objectify ofy) {
		BoundingBox bb = new BoundingBox(latN, lonE, latS, lonW);
        List<String> cells = MyGeocellUtils.bestBboxSearchCells(bb, new MyCostFunction());
        /*List<PlanQuadrat> pqs = new LinkedList<PlanQuadrat>();
        for(String cell : cells) {
        	PlanQuadrat pq = ofy.find(PlanQuadrat.class, cell);
        	if(pq != null) {
        		pqs.add(pq);
        	}
        }*/
        Key<RootEntity> re = Dao.getRootEntityBus();
/*        List<PlanQuadrat> pqs = new LinkedList<PlanQuadrat>();
        for(String cell : cells) {
        	PlanQuadrat pq = Dao.getInstance().getPlanQuadrat(cell, re, ofy);
        	if(pq != null) {
        		pqs.add(pq);
        	}
        }*/
        Set<Key<PlanQuadrat>> keys = new HashSet<Key<PlanQuadrat>>();
        for(String cell : cells) {
        	keys.add(new Key<PlanQuadrat>(re, PlanQuadrat.class, cell));
        }
        Collection<PlanQuadrat> pqs = ofy.get(keys).values();
		return pqs;
	}
	
	public int addAndCheckSearchForIp(String ip, int mode) {
		String functionName = "addAndCheckSearchForIp()";
		Cache cache;
		Map<String, Integer> props = new HashMap<String, Integer>();
        props.put(GCacheFactory.EXPIRATION_DELTA, 600); // 10 minutes
        if(Utils.isUserInSpecialACL()) {
//        	System.err.println("No limit for user: " + Utils.getUser().getEmail() + "(" + Utils.getUser().getNickname() + ")");
        	Logger.getLogger(location).log(Level.INFO, functionName + ": No limit for user: " + Utils.getUser().getEmail() + "(" + Utils.getUser().getNickname() + ")");
        	return 1;
        }
        try {
        	String key = "requestCounter:" + mode + ":" + ip;
        	CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(props);
            Integer counter = new Integer(1);
            Integer o = (Integer)cache.get(key);
            if(o != null) {
            	counter = counter + o;
            }
            cache.put(key, counter);
            return counter;
        } catch (CacheException e) {
        	Logger.getLogger(location).log(Level.SEVERE, functionName + ": caching error: " + e);
        	return -1;
        }
	}
	
	@SuppressWarnings("unchecked")
	public Collection<UserFavouritePosition> getUserFavouritePositions(User user, Objectify ofy) {
		String functionName = "getUserFavouritePositions()";
		Query<UserFavouritePosition> q = ofy.query(UserFavouritePosition.class).filter("user", user);
		List<Key<UserFavouritePosition>> keys;
		try {
        	Cache cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
        	keys = (List<Key<UserFavouritePosition>>)cache.get(q.toString());
        	if(keys == null) {
				keys = q.listKeys();
				cache.put(q.toString(), keys);
			}
        } catch (CacheException e) {
        	Logger.getLogger(location).log(Level.SEVERE, functionName + ": caching error: " + e);
        	keys = q.listKeys();
        }
        return ofy.get(keys).values();
	}
	
	public void addUserFavouritePosition(UserFavouritePosition fav, Objectify ofy) {
		String functionName = "addUserFavouritePosition()";
		if(fav.getUser() == null) {
			Logger.getLogger(location).log(Level.SEVERE, functionName + ": got a fav without user");
		} else {
			Query<UserFavouritePosition> q = ofy.query(UserFavouritePosition.class).filter("user", fav.getUser());
			removeFromCache(q.toString());
			ofy.put(fav);
		}
	}
	
	public void deleteUserFavouritePosition(Key<UserFavouritePosition> fav, User currentUser, Objectify ofy) {
		String functionName = "deleteUserFavouritePosition()";
		UserFavouritePosition toDelete = ofy.find(fav);
		if(toDelete != null && toDelete.getUser().equals(currentUser)) {
			Query<UserFavouritePosition> q = ofy.query(UserFavouritePosition.class).filter("user", toDelete.getUser());
			removeFromCache(q.toString());
			ofy.delete(fav);
		} else if (toDelete == null) {
			Logger.getLogger(location).log(Level.SEVERE, functionName + ": got a request to delete a userfavouriteposition but none were found with that key: " + fav);
		} else {
			Logger.getLogger(location).log(Level.SEVERE, functionName + ": got a request to delete a userfavouriteposition but it did not match the current user: " + currentUser);
		}
	}
	
	public Objectify getObjectify() {
		return ObjectifyService.begin();
	}

}
