/**
 * @author Sawan Meshram
 */
package com.jts.io.esrijson;

public enum EsriJsonConstants {
	NAME_SPATIAL_REFERENCE("spatialReference"),
	NAME_WKID("wkid"),
	NAME_LATEST_WKID("latestWkid"),
	NAME_X("x"),
	NAME_Y("y"),
	NAME_Z("Z"),
	NAME_POINTS("points"),
	NAME_PATHS("paths"),
	NAME_RINGS("rings"),
	NAME_XMIN("xmin"),
	NAME_YMIN("ymin"),
	NAME_XMAX("xmax"),
	NAME_YMAX("ymax");	
	
	private final String name;
	EsriJsonConstants(String name){
		this.name = name;
	}
	public String getName() {
		return name;
	}
}
