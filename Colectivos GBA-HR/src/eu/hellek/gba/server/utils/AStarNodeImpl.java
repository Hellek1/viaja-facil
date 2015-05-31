package eu.hellek.gba.server.utils;

import com.googlecode.objectify.Key;

import eu.hellek.gba.model.Line;

public class AStarNodeImpl implements AStarNode {
	
	private int g;
	
	private int h;
	
	private AStarNode predecessor;
	
	private String geoCell;
	
	private Key<Line> owningLine;
	
	private int index;
	
	private boolean ignore;
	
	private boolean twoway;
	
	public AStarNodeImpl(String geoCell, Key<Line> k, int index, boolean ignore, boolean twoway) {
		this.geoCell = geoCell;
		this.owningLine = k;
		this.index = index;
		this.ignore = ignore;
		this.twoway = twoway;
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

	public void setGeoCell(String geoCell) {
		this.geoCell = geoCell;
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
		AStarNodeImpl other = (AStarNodeImpl) obj;
		if (geoCell == null) {
			if (other.geoCell != null)
				return false;
		} else if (!geoCell.equals(other.geoCell))
			return false;
		if (owningLine == null) {
			if (other.owningLine != null)
				return false;
		} else if (!owningLine.equals(other.owningLine))
			return false;
		return true;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return "AStarNodeImpl\t" + geoCell + " " + owningLine + " " + index + "\t" + g + "\t" + h;
	}

	public boolean isIgnore() {
		return ignore;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	@Override
	public String getPointGeoCell() {
		return this.getGeoCell();
	}

	public boolean isTwoway() {
		return twoway;
	}

}
