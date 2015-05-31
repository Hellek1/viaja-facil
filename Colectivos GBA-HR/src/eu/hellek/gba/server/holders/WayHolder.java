package eu.hellek.gba.server.holders;

import java.util.List;

import eu.hellek.gba.server.utils.AStarNode;

public class WayHolder implements Comparable<WayHolder> {
	
	private List<AStarNode> way;
	private List<AStarNode> combinationPoints; // Cells in denen umgestiegen wird

	public WayHolder(List<AStarNode> way, List<AStarNode> combinationPoints) {
		this.way = way;
		this.combinationPoints = combinationPoints;
	}

	public List<AStarNode> getWay() {
		return way;
	}

	public void setWay(List<AStarNode> way) {
		this.way = way;
	}

	public List<AStarNode> getCombinationPoints() {
		return combinationPoints;
	}

	public void setCombinationPoints(List<AStarNode> combinationPoints) {
		this.combinationPoints = combinationPoints;
	}

	@Override
	public int compareTo(WayHolder o) {
		return this.way.size() - o.way.size();
	}
	
}
