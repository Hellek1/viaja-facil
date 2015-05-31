package eu.hellek.gba.shared;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/*
 * represents a line entity that is part of a connection. might be walking, a subway connection, a bus connection, etc.
 */
public class LineProxy  implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// to calculate the time it takes, the length is multiplied with one of those values
	private static final float [] multiplicators = { 0.015F, 0.0024F, 0.0015F, 0.001F, 0.0020F, 0.0019F }; // 4, 25, 40 bzw 60 km/h

	private String key; // internal database id, might be that it is never used anyway
	
	private String linenum;
	
	private String ramal;
	
	private int type; // 0 = walk, 1 = bus, 2 = subte, 3 = train, 4 = metrobus, 5 = premetro
	
	private List<Float> relevantPoints; // points of the line that lie within the part relevant for the search query
	
	private List<Float> allPoints; // all points of the line, including those that are beyond the part that the connection actually uses
	
	private List<String> alternativeLines; // other lines that travel between this line's starting and end points as well, in text representation only
	
	private List<String> stations; // stations of this line, if available. index corresponds to allPoints/2. i.e. second station (index 1) = indices 2 and 3 in all points.
	
	private int distance; // sum of distances in relevantPoints
	
	private String startStreet; // street name at first element in relevantPoints
	
	private String destStreet; // street name at last element in relevantPoints
	
	public LineProxy() { }
	
	public LineProxy(String key, String linenum, String ramal, int type, List<Float> relevantPoints, int distance, List<Float> allPoints, List<String> stations) {
		this.key = key;
		this.linenum = linenum;
		this.ramal = ramal;
		this.relevantPoints = relevantPoints;
		this.distance = distance;
		this.allPoints = allPoints;
		this.type = type;
		this.stations = stations;
		this.alternativeLines = new LinkedList<String>();
	}
	
	public LineProxy(String key, String linenum, String ramal, int type, List<Float> relevantPoints, int distance, List<Float> allPoints) {
		this.key = key;
		this.linenum = linenum;
		this.ramal = ramal;
		this.relevantPoints = relevantPoints;
		this.distance = distance;
		this.allPoints = allPoints;
		this.type = type;
		this.stations = null;
		this.alternativeLines = new LinkedList<String>();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLinenum() {
		return linenum;
	}

	public void setLinenum(String linenum) {
		this.linenum = linenum;
	}

	public String getRamal() {
		return ramal;
	}

	public void setRamal(String ramal) {
		this.ramal = ramal;
	}

	public String toString() {
		return linenum + " " + ramal;
	}

	public int getDistance() {
		return distance;
	}
	
	/*
	 * time it takes in minutes
	 */
	public int getTime() {
		int trackTime = Math.round(distance*multiplicators[type]);
		int waitTime = 0;
		if(type == 1) {
			waitTime = 8;
		} else if (type == 2) {
			waitTime = 4;
		} else if (type == 3) {
			waitTime = 15;
		} else if (type == 4) {
			waitTime = 5;
		}
		return trackTime + waitTime;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public List<Float> getRelevantPoints() {
		return relevantPoints;
	}

	public void setRelevantPoints(List<Float> relevantPoints) {
		this.relevantPoints = relevantPoints;
	}

	public List<Float> getAllPoints() {
		return allPoints;
	}

	public void setAllPoints(List<Float> allPoints) {
		this.allPoints = allPoints;
	}
	
	public void addAlternativeLine(String s) {
		this.alternativeLines.add(s);
	}

	public List<String> getAlternativeLines() {
		return alternativeLines;
	}

	public String getStartStreet() {
		return startStreet;
	}

	public void setStartStreet(String startStreet) {
		this.startStreet = startStreet;
	}

	public String getDestStreet() {
		return destStreet;
	}

	public void setDestStreet(String destStreet) {
		this.destStreet = destStreet;
	}
	
	public int getType() {
		return type;
	}

	public String getTypeAsString() {
		if(type == 0) {
			return "caminar";
		} else if(type == 1) {
			return "colectivo";
		} else if(type == 2) {
			return "subte";
		} else if(type == 3) {
//			return "tren";
			return "";
		} else if(type == 4 || type == 5) { // metrobus und premetro haben dies schon im eigennamen
			return "";
		} else {
			return "error: invalid type";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((alternativeLines == null) ? 0 : alternativeLines.hashCode());
		result = prime * result
				+ ((destStreet == null) ? 0 : destStreet.hashCode());
		result = prime * result + distance;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((linenum == null) ? 0 : linenum.hashCode());
		result = prime * result + ((ramal == null) ? 0 : ramal.hashCode());
		result = prime * result
				+ ((startStreet == null) ? 0 : startStreet.hashCode());
		result = prime * result + type;
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
		LineProxy other = (LineProxy) obj;
		if (alternativeLines == null) {
			if (other.alternativeLines != null)
				return false;
		} else if (!alternativeLines.equals(other.alternativeLines))
			return false;
		if (destStreet == null) {
			if (other.destStreet != null)
				return false;
		} else if (!destStreet.equals(other.destStreet))
			return false;
		if (distance != other.distance)
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (linenum == null) {
			if (other.linenum != null)
				return false;
		} else if (!linenum.equals(other.linenum))
			return false;
		if (ramal == null) {
			if (other.ramal != null)
				return false;
		} else if (!ramal.equals(other.ramal))
			return false;
		if (startStreet == null) {
			if (other.startStreet != null)
				return false;
		} else if (!startStreet.equals(other.startStreet))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	public List<String> getStations() {
		return stations;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setAlternativeLines(List<String> alternativeLines) {
		this.alternativeLines = alternativeLines;
	}

	public void setStations(List<String> stations) {
		this.stations = stations;
	}
	
}
