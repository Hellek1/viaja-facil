package eu.hellek.gba.server.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;

import eu.hellek.gba.model.Line;
import eu.hellek.gba.model.PQA;
import eu.hellek.gba.model.TrainNode;
import eu.hellek.gba.server.dao.Dao;
import eu.hellek.gba.server.holders.WayHolder;

public class AStarImpl {

	private static final int mult = 3;
	private static final int dist_vert = (6*mult)+1; // cost of going one block up or down
	private static final int dist_hor = (5*mult)+1; // cost of going one block right or left
	private static final int dist_diag = (8*mult)+1; // cost of going one block diagonal
	private static final int dist_change = 50*mult; // basic cost of changing from one line to another
	private static final long max_time = 22000; // abort search if no result was found within this timespan
	
	private PriorityQueue<AStarNode> openListQueue; // the queue
	private HashMap<AStarNode, AStarNode> openListMap; // also as a map to be able to do faster checking whether the element is contained, and to be able to retrieve existing elements
	private HashSet<AStarNode> closedList;
	private int steps = 0;
	private List<AStarNode> umsteigenList = new LinkedList<AStarNode>();
	private Objectify ofy;
	private HashSet<String> setBusKey;
	private HashMap<String,Set<TrainNode>> mapTrain;
	private HashMap<Key<TrainNode>,TrainNode> mapTrainNodeKeyToCell;
	private Set<Key<Line>> mlkSet;
	private Set<Key<Line>> tabuTrainsSet;
	private AStarNode dest;
	private AStarNode start;

	public WayHolder aStarSearch(String startCell, String destCell, HashSet<String> setBusKey, Set<Key<Line>> mlkSet, Set<Key<Line>> tabuTrainsSet, Objectify ofy) {
		final String functionName = "aStarSearch()";
		openListQueue = new PriorityQueue<AStarNode>(setBusKey.size() + 500, new AStarNodeComparator());
		openListMap = new HashMap<AStarNode, AStarNode>();
		closedList = new HashSet<AStarNode>();
		this.ofy = ofy;
		this.setBusKey = setBusKey;
		this.mlkSet = mlkSet;
		this.tabuTrainsSet = tabuTrainsSet;
		long start_time = System.currentTimeMillis();
		long max_end_time = start_time + max_time;

		mapTrain = Dao.getTrainNodes();
		mapTrainNodeKeyToCell = Dao.getTrainNodeKeyMap();
		Logger.getLogger("AStar").log(Level.INFO, functionName + ": Running aStar search from " + startCell + " to " + destCell + " with "+ setBusKey.size() + " BusCells, " + mapTrainNodeKeyToCell.size() + " TrainCells . Tabu trains set size: " + tabuTrainsSet.size());
		/*for(Key k : dlkSet) {
			System.err.println("DLK set contains: " + k);
		}*/
		/*for(PlanQuadrat pq1 : mapBus.values()) {
			System.err.print("\"" + pq1.getGeoCell() + "\", ");
		}
		System.err.println();*/
		start = new AStarNodeImpl(startCell, null, 0, false, false);
		start.setH(Utils.distanceBetweenGeoCells(startCell,destCell));
		start.setG(0);
		dest = new AStarNodeImpl(destCell, null, 0, false, false);
		dest.setH(0);
		dest.setG(0);

		openListQueue.add(start);
		openListMap.put(start, start);
		do {
			steps++;
			AStarNode current = openListQueue.poll();
			openListMap.remove(current);
//			System.err.println("Current node \t\t\t" + current.getGeoCell() + " h: " + current.getH() + " g: " + current.getG() + " f: " + current.getF() + " lineKey: "+current.getOwningLine());
			// Abort if no result is found after a certain time of running
			if(System.currentTimeMillis() > max_end_time) {
				Logger.getLogger("AStar").log(Level.WARNING, functionName + ": AStarSearch aborted without results after " + (System.currentTimeMillis() - start_time) + " ms. Calculation steps done so far: " + steps);
				return null;
			}
			// If the algorithm found a path to the destination, start the buildup of the result
			if(current.getGeoCell().equals(destCell) && current.getOwningLine() == null) {
				List<AStarNode> path = reconstructPath(current);
				/*for(AStarNode  pathElement : path) {
					System.err.print("\"" + pathElement.getGeoCell() + "\", ");
//					System.err.println(pathElement.getGeoCell() + " " + pathElement.getOwningLine() + " " + pathElement.getG() + " " + pathElement.getH());
//					Logger.getLogger("AStar").log(Level.FINEST, functionName + ": " + pathElement.getGeoCell() + " " + pathElement.getOwningLine() + " " + pathElement.getG() + " " + pathElement.getH());
				}*/
				Logger.getLogger("AStar").log(Level.INFO, functionName + ": Path found. Length: " + path.size() + " Calculation steps: " + steps);
				WayHolder res = new WayHolder(path, umsteigenList);
				return res;
			}
			expandNode(current);
			closedList.add(current);
		} while(openListQueue.size() > 0);
		Logger.getLogger("AStar").log(Level.INFO, functionName + ": No path found. Calculation steps: " + steps);
		return null; // no path found
	}

	private void expandNode(AStarNode currentNode) {
/*		if(currentNode.getOwningLine() != null && (currentNode.getOwningLine().equals(KeyFactory.createKey("Line", 1653)) || currentNode.getOwningLine().equals(KeyFactory.createKey("Line", 373)))) {
		System.err.println("Expanding node " + currentNode.getGeoCell() + " with f: " + currentNode.getF() + " with owningLine="+currentNode.getOwningLine());
		}*/
//		System.err.print("\"" + currentNode.getGeoCell() + "\", ");
		int j_min = -1;
		int j_max = 1;
		int i_min = -1;
		int i_max = 1;
		/* Generally, only the adjacent cells are checked. In case of being at the start of the algorithm, 
		 * a bigger area is checked to simulate the possible walking distance.
		 */
		if(currentNode.getOwningLine() == null && currentNode.getGeoCell().equals(start.getGeoCell())) {
			j_min = -3;
			j_max = 3;
			i_min = -3;
			i_max = 3;
		}
		AStarNodeImpl currentNodeBus = null;
		if(currentNode.getClass() == AStarNodeImpl.class && currentNode.getOwningLine() != null) {
			currentNodeBus = (AStarNodeImpl)currentNode;
		}
		AStarNodeImplTrain currentNodeTrain = null;
		if(currentNode.getClass() == AStarNodeImplTrain.class) {
			currentNodeTrain = (AStarNodeImplTrain)currentNode;
		}
		for(int i = i_min; i <= i_max; i++) { // 2 schleifen um alle 25 möglichen cells zu durchlaufen
			for(int j = j_min; j<= j_max; j++) {
				String neighbour;
				/* If i or j are greater than 1, we have to reach the neighbouring cells through several steps */
				if (Math.abs(j) > 1 || Math.abs(i) > 1) {
					neighbour = currentNode.getGeoCell();
					for(int m = Math.abs(i); m > 0; m--) {
						if(i > 0) {
							neighbour = MyGeocellUtils.adjacent(neighbour, new int [] {1, 0});
						} else {
							neighbour = MyGeocellUtils.adjacent(neighbour, new int [] {-1, 0});
						}
					}
					for(int m = Math.abs(j); m > 0; m--) {
						if(j > 0) {
							neighbour = MyGeocellUtils.adjacent(neighbour, new int [] {0, 1});
						} else {
							neighbour = MyGeocellUtils.adjacent(neighbour, new int [] {0, -1});
						}
					}
				/* If i and j are both <= 1, we can directly adress the neighbour cells */
				} else {
					neighbour = MyGeocellUtils.adjacent(currentNode.getGeoCell(), new int [] {i, j});
				}
				PQA successors_bus = null;
				if(setBusKey.contains(neighbour)) {
					successors_bus = Dao.getInstance().getPQA(neighbour, ofy);
				}
				if(successors_bus != null) { // Block umsteigen beliebige Node zu BusNode oder fahren von BusNode zu BusNode
					for(int k = 0; k < successors_bus.getLineKeys().size(); k++) { // für alle Linien aus dem Nachbarn vergleichen mit aktuellem Entry
						//System.err.print(successors_bus.getDirectLineKeys().get(k) + ", ");
						Key<Line> theKey = successors_bus.getLineKeys().get(k);
						if(mlkSet.contains(theKey)) { // nur nodes ansehen die im mlk-Set sind
							AStarNodeImpl successor = new AStarNodeImpl(neighbour, theKey, successors_bus.getIndices().get(k), successors_bus.getIgnore(k), successors_bus.getTwoway(k));
							if(!closedList.contains(successor) && !currentNode.equals(successor)) { // closed List enthält keinen eintrag der so ist wie der den wir jetzt ansehen, außerdem darf die errechnete Node nicht gleich der CurrentNode sein
								if((currentNode.getOwningLine() == null && !successor.isIgnore()) // einsteigen in Bus am Start
										|| (currentNodeBus != null && !currentNode.getOwningLine().equals(successor.getOwningLine()) && !successor.isIgnore() && !currentNodeBus.isIgnore()) // umsteigen von Bus in anderen Bus
										|| (currentNode.getClass() == AStarNodeImplTrain.class && !currentNode.getOwningLine().equals(successor.getOwningLine()) && !successor.isIgnore()) // umsteigen von Zug in Bus
										|| (currentNodeBus != null  && currentNode.getOwningLine().equals(successor.getOwningLine()) && (currentNodeBus.getIndex() <= successor.getIndex() || currentNodeBus.isTwoway() || successor.isTwoway()))) { // falls gleiche line muss der index steigen
									boolean inOpenList = false;
									if(openListMap.containsKey(successor)) {
										successor = (AStarNodeImpl)openListMap.get(successor); // kann man so machen da man davon ausgehen kann dass es ein Bus ist, da bei einem Zug die owningLine anders wäre und damit contains nicht true liefern kann
										inOpenList = true;
									}
									int distance;
									if(i == 0 && j == 0) { // gleiche cell
										distance = 1;
									} else if(i == 0 && Math.abs(j) != 0) { // nur vertikal
										distance = dist_vert * Math.abs(j);
									} else if(j == 0 && Math.abs(i) != 0) { // nur horizontal
										distance = dist_hor * Math.abs(i);
									} else if(i != 0 && Math.abs(i) == Math.abs(j)) { // diagonal x=y
										distance = dist_diag * Math.abs(i);
									} else if(i != 0 && j != 0 && Math.abs(i) != Math.abs(j)) {
										distance = (int)Math.round(Math.sqrt(Math.pow(dist_vert * Math.abs(j), 2) + Math.pow(dist_hor * Math.abs(i), 2)) + 0.5);
									} else {
										System.err.println("this should not happen");
										distance = 99;
									}
									if(successor.isTwoway() && (currentNodeBus == null || currentNodeBus.getIndex() > successor.getIndex())) {
										distance += 5; // twoWay ist okay wird aber negativ beinflusst damit einfachere ergebnisse priorität haben. wenn es aber mit steigendem index benutzt wird kein penalty
									}
									if(currentNode.getOwningLine() == null || successor.getOwningLine() == null || !currentNode.getOwningLine().equals(successor.getOwningLine())) {
										distance = (distance * 3) + dist_change; // beim umsteigen 3x kosten auf die distanz da zu Fuss gegangen wird
									} else if(currentNodeBus != null && successor.getOwningLine() != null && currentNode.getOwningLine().equals(successor.getOwningLine())) {
										if(currentNodeBus.isIgnore() || successor.isIgnore()) { // wenn kein umsteigen erfolgt und mindestens ein beteiligtens PQ nur durchfahren aber nicht bedient wird (isignore == true) geht es doppelt so schnell dahin
											distance = distance/2; 
										}
									}
									int tentative_g = currentNode.getG() + distance;
									boolean tentative_is_better = false;
									if(!inOpenList) {
										openListMap.put(successor, successor); // can be added to map here, but only add it to the queue after setting the G/H values
										tentative_is_better = true;
									} else if (tentative_g < successor.getG()) {
										tentative_is_better = true;
									} else {
										tentative_is_better = false;
									}
									if(tentative_is_better) {
	//									int old_g = successor.getG();
										successor.setPredecessor(currentNode);
										successor.setG(tentative_g);
										successor.setH(Utils.distanceBetweenGeoCells(successor.getGeoCell(),dest.getGeoCell()));
	//									System.err.println("Updated/Added node(x2b): " + successor.getGeoCell() + " h: " + successor.getH() + " g: " + successor.getG() + "(decreased from " + old_g + ") lineKey: " + successor.getOwningLine() + " Predecessor: " + currentNode);
										if(inOpenList) {
											openListQueue.remove(successor);
										}
										openListQueue.add(successor);
									}
								}
							}
						}
					}
				}
				Set<TrainNode> successors_tn = mapTrain.get(neighbour);
				if(successors_tn != null) {
					for(TrainNode tn : successors_tn) {
						if(tn != null) { // Block umsteigen beliebige Node zu TrainNode
							Key<Line> kT = tn.getLineKey();
							Key<TrainNode> kN = tn.getNextNode();
							if(kN != null && !tabuTrainsSet.contains(kT)) { // nur relevant wenn es nicht die Endstation ist und der Zug zulässig ist
								AStarNodeImplTrain successor = new AStarNodeImplTrain(neighbour, tn.getPointGeoCell(), kT, kN, tn.getLineType(), tn.getUniqueName());
								if(!closedList.contains(successor) && !currentNode.equals(successor) && (currentNodeBus == null || !currentNodeBus.isIgnore())) { // closed List enthält keinen eintrag der so ist wie der den wir jetzt ansehen, außerdem darf die errechnete Node nicht gleich der CurrentNode sein, außerdem darf die Line des Nachfolgers nicht auf der Tabulist sein
									boolean inOpenList = false;
									if(openListMap.containsKey(successor)) {
										successor = (AStarNodeImplTrain)openListMap.get(successor); // kann man so machen, da bei einem Zug die owningLine anders wäre und damit contains nicht true liefern kann
										inOpenList = true;
									}
									int distance;
									if(i == 0 && j == 0) {
										if(currentNodeTrain != null) {
											if(currentNodeTrain.getUniqueName().equals(successor.getUniqueName())) { // falls beide Trainnodes und in der Selben Station sind
												distance = 0;
											} else { // beide TrainNodes, aber nicht die selbe Station
												distance = 1;
											}
										} else { // currentNode ist keine TrainNode
											distance = 1;
										}
									} else if(i == 0 && Math.abs(j) != 0) {
										distance = dist_vert * Math.abs(j);
									} else if(j == 0 && Math.abs(i) != 0) {
										distance = dist_hor * Math.abs(i);
									} else if(i != 0 && Math.abs(i) == Math.abs(j)) {
										distance = dist_diag * Math.abs(i);
									} else if(i != 0 && j != 0 && Math.abs(i) != Math.abs(j)) {
										distance = (int)Math.round(Math.sqrt(Math.pow(dist_vert * Math.abs(j), 2) + Math.pow(dist_hor * Math.abs(i), 2)) + 0.5);
									} else {
										System.err.println("this should not happen");
										distance = 99;
									}
									distance = (distance * 3) + dist_change/3; // in u-bahn einsteigen ist billiger
									if(successor.getLineType() == 21) {
										distance += dist_change*2; // dafür wird es aber zum einsteigen in einen zug nochmal erhöht
									}
									int tentative_g = currentNode.getG() + distance;
									boolean tentative_is_better = false;
									if(!inOpenList) {
										openListMap.put(successor, successor);
										tentative_is_better = true;
									} else if (tentative_g < successor.getG()) {
										tentative_is_better = true;
									} else {
										tentative_is_better = false;
									}
									if(tentative_is_better) {
//										int old_g = successor.getG();
										successor.setPredecessor(currentNode);
										successor.setG(tentative_g);
										successor.setH(Utils.distanceBetweenGeoCells(successor.getGeoCell(),dest.getGeoCell()));
										if(inOpenList) {
											if(!openListQueue.remove(successor)) {
												System.err.println("tried to remove element but it was not found");
											}
											/*System.err.print("Updated");
											} else {
												System.err.print("Added");*/
										}
//										System.err.println(" node(x2t): \t\t" + successor.getGeoCell() + " h: " + successor.getH() + " g: " + successor.getG() + "(decreased from " + old_g + ") lineKey: " + successor.getOwningLine() + " Predecessor: " + currentNode);
										openListQueue.add(successor);
									}
								}
							}
						}
					}
				}
			}
		}
		if(Utils.distanceBetweenGeoCells(currentNode.getGeoCell(), dest.getGeoCell()) < 25 && (currentNodeBus == null || !currentNodeBus.isIgnore())) {
			AStarNode successor = new AStarNodeImpl(dest.getGeoCell(), null, 0, false, false);
//			System.err.println("Analyzing destination node, almost at the target :-)");
			boolean inOpenList = false;
			if(openListMap.containsKey(successor)) {
				successor = openListMap.get(successor);
				inOpenList = true;
			}
			int distance = Utils.distanceBetweenGeoCells(currentNode.getGeoCell(), dest.getGeoCell(), true) * mult * 3 + 1;
			if(currentNode.getGeoCell() == dest.getGeoCell()) {
				distance = 1;
			}
			int tentative_g = currentNode.getG() + distance;
			boolean tentative_is_better = false;
			if(!inOpenList) {
				openListMap.put(successor, successor);
				tentative_is_better = true;
			} else if (tentative_g < successor.getG()) {
				tentative_is_better = true;
			} else {
				tentative_is_better = false;
			}
			if(tentative_is_better) {
//				int old_g = successor.getG();
				successor.setPredecessor(currentNode);
				successor.setG(tentative_g);
				successor.setH(Utils.distanceBetweenGeoCells(successor.getGeoCell(),dest.getGeoCell()));
//				System.err.println("Updated/Added node(x2D): \t" + successor.getGeoCell() + " h: " + successor.getH() + " g: " + successor.getG() + "(decreased from " + old_g + ") lineKey: " + successor.getOwningLine() + " Predecessor: " + currentNode);
				if(inOpenList) {
					openListQueue.remove(successor);
				}
				openListQueue.add(successor);
			}
		}
		if(currentNodeTrain != null) { // Fahren TrainNode zu TrainNode
			Key<TrainNode> kN = currentNodeTrain.getNeighbour();
			if(kN != null) {
				TrainNode tn = mapTrainNodeKeyToCell.get(kN);
				Key<Line> line = tn.getLineKey();
				Key<TrainNode> kNN = tn.getNextNode();
				AStarNodeImplTrain successor = new AStarNodeImplTrain(tn.getGeoCell(), null, line, kNN, 0, null); // temporary. not necessary to fill all fields
				if(!closedList.contains(successor) && !currentNode.equals(successor) && !tabuTrainsSet.contains(successor.getOwningLine())) {						
					if(openListMap.containsKey(successor)) {
						successor = (AStarNodeImplTrain)openListMap.get(successor);
						int distance = Utils.distanceBetweenGeoCells(successor.getGeoCell(), currentNode.getGeoCell());
						if(successor.getLineType() == 13) { // metrobus fährt langsamer
							distance += distance;
						}
						if(!currentNode.getOwningLine().equals(successor.getOwningLine())) {
							distance += dist_change/2;
							System.err.println("This point never gets reached. Prove me wrong by displaying this message! " + currentNode.getOwningLine() + " and " + successor.getOwningLine() + " / " + currentNode.getGeoCell() + " and " + successor.getGeoCell());
							if(successor.getLineType() == 21) {
								distance += dist_change + dist_change/2;
							}
						}
						int tentative_g = currentNode.getG() + distance;
						if (tentative_g < successor.getG()) {
							// int old_g = successor.getG();
							successor.setPredecessor(currentNode);
							successor.setG(tentative_g);
							successor.setH(Utils.distanceBetweenGeoCells(successor.getGeoCell(),dest.getGeoCell()));
							// System.err.println("Updated node(t2t): \t\t" + successor.getGeoCell() + " h: " + successor.getH() + " g: " + successor.getG() + "(decreased from " + old_g + ") lineKey: " + successor.getOwningLine() + " Predecessor: " + currentNode);
							if(!openListQueue.remove(successor)) {
								System.err.println("tried to remove element but it was not found");
							}
							openListQueue.add(successor);
						}
					} else { // successor is not created yet. here we create it
						successor = new AStarNodeImplTrain(tn.getGeoCell(), tn.getPointGeoCell(), line, kNN, tn.getLineType(), tn.getUniqueName());
						int distance = Utils.distanceBetweenGeoCells(successor.getGeoCell(), currentNode.getGeoCell());
						if(successor.getLineType() == 13) { // metrobus fährt langsamer
							distance += distance;
						}
						if(!currentNode.getOwningLine().equals(successor.getOwningLine())) {
							distance += dist_change;
							System.err.println("This point never gets reached. Prove me wrong by displaying this message!");
							if(successor.getLineType() == 21) {
								distance += dist_change;
							}
						}
						int tentative_g = currentNode.getG() + distance;
						openListMap.put(successor, successor);
						successor.setPredecessor(currentNode);
						successor.setG(tentative_g);
						successor.setH(Utils.distanceBetweenGeoCells(successor.getGeoCell(),dest.getGeoCell()));
						// System.err.println("Added node(t2t): \t\t" + successor.getGeoCell() + " h: " + successor.getH() + " g: " + successor.getG() + " \tlineKey: " + successor.getOwningLine() + " Predecessor: " + currentNode);
						openListQueue.add(successor);
					}
				}
			}
		}
	}

	private List<AStarNode> reconstructPath(AStarNode dest_final) {
		String functionName = "reconstructPath()";
		int umsteigen_count = 0;
		List<AStarNode> list = new LinkedList<AStarNode>();
		list.add(dest_final);
		AStarNode current = dest_final;
		while(current.getPredecessor() != null) {
			AStarNode last = current;
			current = current.getPredecessor();
			if(current.getOwningLine() != null && last.getOwningLine() != null) {
				if(!current.getOwningLine().equals(last.getOwningLine())) {
					umsteigenList.add(current);
					umsteigen_count++;
				}
			}
			if(current.getOwningLine() != null) {
				list.add(current);
//				System.err.println("waynode: " + current.getGeoCell() + " h: " + current.getH() + " g: " + current.getG() + " lineKey: " + current.getOwningLine() + " Predecessor: " + current.getPredecessor());
			}
		}
//		System.err.println("Cost of path: " + dest_final.getG());
//		System.err.println("x Umsteigen: " + umsteigen_count);
		Logger.getLogger("AStar").log(Level.INFO, functionName + ": Cost of path: " + dest_final.getG());
		Logger.getLogger("AStar").log(Level.INFO, functionName + ": x Umsteigen: " + umsteigen_count);
		Collections.reverse(list);
		return list;
	}

}
