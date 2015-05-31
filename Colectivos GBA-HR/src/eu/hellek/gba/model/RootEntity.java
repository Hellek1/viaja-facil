package eu.hellek.gba.model;

import java.io.Serializable;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Cached;

@Cached
public class RootEntity implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	private Long id;
	
	private int type; // 1 = bus, 2 = tren
	
	public RootEntity() { }
	
	public RootEntity(int type) {
		this.type = type;
	}

	public Long getId() {
		return id;
	}

	public int getType() {
		return type;
	}
	
}
