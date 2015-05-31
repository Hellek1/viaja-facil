package eu.hellek.gba.server.utils;

import java.util.Comparator;

public class AStarNodeComparator implements Comparator<AStarNode> {

	@Override
	public int compare(AStarNode o1, AStarNode o2) {
		return o1.getF() - o2.getF();
	}

}
