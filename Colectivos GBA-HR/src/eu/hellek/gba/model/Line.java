package eu.hellek.gba.model;

import java.io.Serializable;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

@Cached
public class Line implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id private Long id;
	
	private String linenum;
	
	private String ramal;
	
	private int type; // 1 = bus(, 11 = subte, 21 = zug, 13 = metrobus, 15 = premetro)
	
	public Line() { }
	
	public Line(String linenum, String ramal, int type) {
		this.linenum = linenum;
		this.ramal = ramal;
		this.type = type;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
	
	@Override
	public String toString() {
		return linenum + " " + ramal;
	}
	
	@Override
	public boolean equals(Object o) {
		if(Line.class.isInstance(o)) {
			Line l = (Line)o;
			return this.id.equals(l.getId());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
	    return this.id.hashCode();
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
