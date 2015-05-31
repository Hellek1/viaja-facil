package eu.hellek.gba.model;

import java.io.Serializable;

import javax.persistence.Id;

import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Cached;
import com.googlecode.objectify.annotation.Parent;
import com.googlecode.objectify.annotation.Unindexed;

import eu.hellek.gba.server.utils.Utils;

@Cached
public class Point implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private Long id;
	
	@Unindexed
	private GeoPt latlon;
	
	// The streetname until the next point
	@Unindexed
	private String street;
	
	// if true, ignore this section in route searching
	private boolean ignore;
	
	// This Point is not necessary to display the route. It is just an intermediate point for the search function
	private boolean forSearchOnly;

	// der index in der Punktliste der Linie
	private int index;
	
	// der index des naechsten Punktes der nicht nur der Suche dient
	@Unindexed
	private int nextMainPointIndex;
	/*
	@Geocells
	@OneToMany(fetch = FetchType.LAZY)
	private List<String> geoCells;
	*/
	private String defaultGeoCell; // cell with default resoultion
	
	@Parent
	private Key<Line> owner;
	
	/*@Longitude
	private float lon;
	
	@Latitude
	private float lat;*/
	
	public Point() { }
	
	public Point(String street, float lat, float lon, Key<Line> owner) {
		this(street, lat, lon, false, owner);
	}
	
	public Point(String street, float lat, float lon, boolean ignore, Key<Line> owner) {
		this(street, lat, lon, false, false, owner);
	}
	
	public Point(String street, float lat, float lon, boolean ignore, boolean forSearchOnly, Key<Line> owner) {
		this.street = street;
		this.latlon = new GeoPt(lat, lon);
		this.ignore = ignore;
		this.forSearchOnly = forSearchOnly;
		this.nextMainPointIndex = -1;
//		this.lat = lat;
//		this.lon = lon;
		if(forSearchOnly && ignore) {
			System.err.println("Point(...): forSearchOnly and ignore are set at the same time. This should not happen since it makes no sense to create a point for search only, yet ignore it in search then");
		}
		defaultGeoCell = generateGeoCell();
		this.owner = owner;
	}
	
	private String generateGeoCell() {
		 return Utils.computeGeoCell(this);
	}
	
	public boolean isIgnore() {
		return ignore;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public GeoPt getLatlon() {
		return latlon;
	}

	public void setLatlon(GeoPt latlon) {
		this.latlon = latlon;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}
	
	public boolean isForSearchOnly() {
		return forSearchOnly;
	}

	public void setForSearchOnly(boolean forSearchOnly) {
		this.forSearchOnly = forSearchOnly;
	}

	public int getNextMainPointIndex() {
		return nextMainPointIndex;
	}

	public void setNextMainPointIndex(int nextMainPointIndex) {
		this.nextMainPointIndex = nextMainPointIndex;
	}

	public Key<Line> getOwner() {
		return owner;
	}

	public void setOwner(Key<Line> owner) {
		this.owner = owner;
	}

	public String getDefaultGeoCell() {
		return defaultGeoCell;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
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
		Point other = (Point) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}
	
}
