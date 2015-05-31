package eu.hellek.gba.server.utils;

import com.googlecode.objectify.Key;

import eu.hellek.gba.model.Line;
import eu.hellek.gba.model.TrainNode;

public class AStarNodeImplTrain implements AStarNode {
	
	private int g;
	
	private int h;
	
	private AStarNode predecessor;
	
	private String geoCell;
	
	private String pointGeoCell;
	
	private Key<Line> owningLine;
	
	private int lineType;
	
	private Key<TrainNode> neighbour;
	
	private String uniqueName;
	
	public AStarNodeImplTrain(String geoCell, String pointGeoCell, Key<Line> k, Key<TrainNode> neighbour, int type, String uniqueName) {
		this.geoCell = geoCell;
		this.owningLine = k;
		this.neighbour = neighbour;
		this.lineType = type;
		this.pointGeoCell = pointGeoCell;
		this.uniqueName = uniqueName;
	}

	@Override
	public int getH() {
		return h;
	}

	@Override
	public void setH(int h) {
		this.h = h;
	}

	@Override
	public int getG() {
		return g;
	}

	@Override
	public void setG(int g) {
		this.g = g;
	}

	@Override
	public int getF() {
		return g+h;
	}

	@Override
	public AStarNode getPredecessor() {
		return predecessor;
	}

	@Override
	public void setPredecessor(AStarNode predecessor) {
		this.predecessor = predecessor;
	}

	public String getGeoCell() {
		return geoCell;
	}

	public String getPointGeoCell() {
		return pointGeoCell;
	}

	public Key<Line> getOwningLine() {
		return owningLine;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((geoCell == null) ? 0 : geoCell.hashCode());
		result = prime * result
				+ ((neighbour == null) ? 0 : neighbour.hashCode());
		result = prime * result
				+ ((owningLine == null) ? 0 : owningLine.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AStarNodeImplTrain other = (AStarNodeImplTrain) obj;
		if (geoCell == null) {
			if (other.geoCell != null)
				return false;
		} else if (!geoCell.equals(other.geoCell))
			return false;
		if (neighbour == null) {
			if (other.neighbour != null)
				return false;
		} else if (!neighbour.equals(other.neighbour))
			return false;
		if (owningLine == null) {
			if (other.owningLine != null)
				return false;
		} else if (!owningLine.equals(other.owningLine))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AStarNodeImplTrain\t" + geoCell + " " + owningLine + "\t" + g + "\t" + h;
	}

	public Key<TrainNode> getNeighbour() {
		return neighbour;
	}

	public void setNeighbour(Key<TrainNode> neighbour) {
		this.neighbour = neighbour;
	}

	public int getLineType() {
		return lineType;
	}

	public String getUniqueName() {
		return uniqueName;
	}

}
