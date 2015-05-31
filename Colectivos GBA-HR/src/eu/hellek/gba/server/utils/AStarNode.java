package eu.hellek.gba.server.utils;

import com.googlecode.objectify.Key;

import eu.hellek.gba.model.Line;

public interface AStarNode {

	public int getH();
	public void setH(int h);
	public int getG();
	public void setG(int g);
	public int getF(); // return g+h;
	
	public AStarNode getPredecessor();
	public void setPredecessor(AStarNode predecessor);
	
	public String getGeoCell();
	public String getPointGeoCell();
	
	public Key<Line> getOwningLine();
	
	public boolean equals(Object obj);
	public int hashCode();
	
	public String toString();
	
}
