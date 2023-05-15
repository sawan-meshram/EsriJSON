/**
 * @author Sawan Meshram
 */
package com.jts.io.esrijson;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

public class EsriGeometryUtil {
	/**
	 * This function is used to check or convert {@code Polygon} to {@code MultiPolygon} object.
	 * @param poly is input {@code Polygon} geometry object.
	 * @return {@code Geometry} object.
	 */
	public static Geometry convertPolygonOrMultiPolygon(Polygon poly) {
		if(poly.getNumInteriorRing() == 0)
			return poly;
		
		GeometryFactory factory = new GeometryFactory();
		
		Polygon outerPoly = factory.createPolygon(poly.getExteriorRing());
		
		List<Polygon> polyList = new ArrayList<>();
		List<LinearRing> holes = new ArrayList<>();
		
		for(int i = 0; i < poly.getNumInteriorRing(); i++) {
			Polygon innerPoly = factory.createPolygon(poly.getInteriorRingN(i));
			if(outerPoly.contains(innerPoly)) {
				holes.add(poly.getInteriorRingN(i));
			}
			else {
				polyList.add(innerPoly);
			}
		}
		
		if(holes.size() > 0) {
			Polygon newPoly = factory.createPolygon(poly.getExteriorRing(), holes.toArray(new LinearRing[holes.size()]));
			polyList.add(newPoly);
		}
		if(polyList.size() > 1) {
			return factory.createMultiPolygon(polyList.toArray(new Polygon[polyList.size()]));
		}
		return polyList.get(0);	
	}
}
