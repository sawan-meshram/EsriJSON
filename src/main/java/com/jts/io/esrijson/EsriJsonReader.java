/**
 * @author Sawan Meshram
 */
package com.jts.io.esrijson;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.parser.JSONParser;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.locationtech.jts.io.ParseException;

public class EsriJsonReader {
	private GeometryFactory gf;

	public EsriJsonReader() {
		super();
	}

	public EsriJsonReader(GeometryFactory geometryFactory) {
		this.gf = geometryFactory;
	}

	public Geometry read(String json) throws ParseException {
		Geometry result = read(new StringReader(json));
		return result;
	}

	public Geometry read(Reader reader) throws ParseException {

		Geometry result = null;

		JSONParser parser = new JSONParser();
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> geometryMap = (Map<String, Object>) parser.parse(reader);
			GeometryFactory geometryFactory = null;
			if (this.gf == null) {
				geometryFactory = this.getGeometryFactory(geometryMap);
			} else {
				geometryFactory = this.gf;
			}

			result = create(geometryMap, geometryFactory);

		} catch (org.json.simple.parser.ParseException e) {
			throw new ParseException(e);
		} catch (IOException e) {
			throw new ParseException(e);
		}

		return result;
	}
		
	private Geometry create(Map<String, Object> geometryMap, GeometryFactory geometryFactory) throws ParseException {

		EsriGeometryType type = getEsriGeometryType(geometryMap);
		if (type == null) {
			throw new ParseException("Could not parse Geometry from Json string. No defined 'esri geometry type'.");
		}
		
		Geometry result = null;
		switch(type) {
			case esriGeometryPoint:
				checkSupportedKeysInJson(geometryMap, type);
				result = createPoint(geometryMap, geometryFactory);
				break;
			case esriGeometryMultipoint:
				checkSupportedKeysInJson(geometryMap, type);
				result = createMultiPoint(geometryMap, geometryFactory);
				break;
	
			case esriGeometryPolyline:
				checkSupportedKeysInJson(geometryMap, type);
				result = createMultiLineString(geometryMap, geometryFactory);
				break;
			case esriGeometryPolygon:
				checkSupportedKeysInJson(geometryMap, type);
				result = createPolygon(geometryMap, geometryFactory);
				break;
	//		case esriGeometryEnvelope:
	//			break;
			default:
				throw new ParseException("Could not parse Geometry from EsriJson string.  Unsupported 'type':" + type);
		}
		
		return result;
	}
	
	private Geometry createPoint(Map<String, Object> geometryMap, GeometryFactory geometryFactory) throws ParseException {
		Geometry result = null;
		try {

			List<Number> coordinateList = new ArrayList<>();
			
			Object x = geometryMap.get(EsriJsonConstants.NAME_X.getName());
			Object y = geometryMap.get(EsriJsonConstants.NAME_Y.getName());
			Object z = geometryMap.get(EsriJsonConstants.NAME_Z.getName());
			
			if(x != null) coordinateList.add((Number) x);
			if(y != null) coordinateList.add((Number) y);
			if(z != null) coordinateList.add((Number) z);
	
			CoordinateSequence coordinate = this.createCoordinate(coordinateList);
	
			result = geometryFactory.createPoint(coordinate);

		} catch (RuntimeException e) {
			throw new ParseException("Could not parse Point from EsriJson string.");
		}
		return result;
	}

	private Geometry createMultiPoint(Map<String, Object> geometryMap, GeometryFactory geometryFactory) throws ParseException {

		Geometry result = null;

		try {

		@SuppressWarnings("unchecked")
		List<List<Number>> coordinatesList = (List<List<Number>>) geometryMap .get(EsriJsonConstants.NAME_POINTS.getName());

		CoordinateSequence coordinates = this.createCoordinateSequence(coordinatesList);

		result = geometryFactory.createMultiPoint(coordinates);

		} catch (RuntimeException e) {
			throw new ParseException("Could not parse MultiPoint from EsriJson string.");
		}
		return result;
	}

	private Geometry createMultiLineString(Map<String, Object> geometryMap, GeometryFactory geometryFactory) throws ParseException {
		Geometry result = null;
		try {

			@SuppressWarnings("unchecked")
			List<List<List<Number>>> linesList = (List<List<List<Number>>>) geometryMap.get(EsriJsonConstants.NAME_PATHS.getName());
	
			LineString[] lineStrings = new LineString[linesList.size()];
	
			int i = 0;
			for (List<List<Number>> coordinates : linesList) {
				lineStrings[i] = geometryFactory.createLineString(createCoordinateSequence(coordinates));
//				lineStrings[i] = createLineString(coordinates, geometryFactory);
				++i;
			}
			if(lineStrings.length > 1)
				result = geometryFactory.createMultiLineString(lineStrings);
			else {
				//Create LineString, if there is only one Polyline
				result = lineStrings[0];
			}
			
		} catch (RuntimeException e) {
			throw new ParseException("Could not parse MultiLineString from EsriJson string.");
		}
		return result;
	}

/*	private LineString createLineString(List<List<Number>> coordinatesList, GeometryFactory geometryFactory) throws ParseException {

		LineString result = null;

		try {
			result = geometryFactory.createLineString(createCoordinateSequence(coordinatesList));
		} catch (RuntimeException e) {
			throw new ParseException("Could not parse LineString from EsriJson string.");
		}
		return result;
	}
*/	
	private Geometry createPolygon(Map<String, Object> geometryMap, GeometryFactory geometryFactory) throws ParseException {
		Geometry result = null;
		try {

			@SuppressWarnings("unchecked")
			List<List<List<Number>>> ringsList = (List<List<List<Number>>>) geometryMap.get(EsriJsonConstants.NAME_RINGS.getName());
	
			List<CoordinateSequence> rings = new ArrayList<CoordinateSequence>();
	
			for (List<List<Number>> coordinates : ringsList) {
				rings.add(createCoordinateSequence(coordinates));
			}
	
			if (rings.isEmpty()) {
				throw new IllegalArgumentException("Polygon specified with no rings.");
			}
	
			LinearRing outer = geometryFactory.createLinearRing(rings.get(0));
			LinearRing[] inner = null;
			if (rings.size() > 1) {
				inner = new LinearRing[rings.size() - 1];
				for (int i = 1; i < rings.size(); i++) {
					inner[i - 1] = geometryFactory.createLinearRing(rings.get(i));
				}
			}
	
			result = geometryFactory.createPolygon(outer, inner);

		} catch (RuntimeException e) {
			throw new ParseException("Could not parse Polygon from EsriJson string.");
		}
		return result;
	}
	
	private CoordinateSequence createCoordinate(List<Number> ordinates) {
		CoordinateSequence result = new CoordinateArraySequence(1);
		
		if (ordinates.size() > 0) {
			result.setOrdinate(0, 0, ordinates.get(0).doubleValue());
		}
		if (ordinates.size() > 1) {
			result.setOrdinate(0, 1, ordinates.get(1).doubleValue());
		}
		if (ordinates.size() > 2) {
			result.setOrdinate(0, 2, ordinates.get(2).doubleValue());
		}
		return result;
	}

	private CoordinateSequence createCoordinateSequence(List<List<Number>> coordinates) {
		CoordinateSequence result = null;

		result = new CoordinateArraySequence(coordinates.size());

		for (int i = 0; i < coordinates.size(); ++i) {
			List<Number> ordinates = coordinates.get(i);
	
			if (ordinates.size() > 0) {
				result.setOrdinate(i, 0, ordinates.get(0).doubleValue());
			}
			if (ordinates.size() > 1) {
				result.setOrdinate(i, 1, ordinates.get(1).doubleValue());
			}
			if (ordinates.size() > 2) {
				result.setOrdinate(i, 2, ordinates.get(2).doubleValue());
			}
		}

		return result;
	}
	
	private GeometryFactory getGeometryFactory(Map<String, Object> geometryMap)  throws ParseException {

		GeometryFactory result = null;
		@SuppressWarnings("unchecked")
		Map<String, Object> srMap = (Map<String, Object>) geometryMap.get(EsriJsonConstants.NAME_SPATIAL_REFERENCE.getName());
		Integer srid = null;

		if (srMap != null) {
			try {
				Object wkid = srMap.get(EsriJsonConstants.NAME_LATEST_WKID.getName()); 
				if(wkid == null) {
					wkid = srMap.get(EsriJsonConstants.NAME_WKID.getName());
				}
				srid = Integer.valueOf(wkid.toString());
			} catch (RuntimeException e) {
				throw new ParseException("Could not parse SRID from Esrijson 'spatialReference' object.");
			}
		}
		
		if (srid == null) {
			// The default CRS is a geographic coordinate reference system, using the WGS84 datum, and with longitude and latitude units of decimal degrees. SRID 4326
			srid = Integer.valueOf(4326);
		}

		result = new GeometryFactory(new PrecisionModel(), srid.intValue());
		return result;
	}
	
	private enum EsriGeometryType{
		esriGeometryPoint ("x", "y", "z", "spatialReference"),
		esriGeometryMultipoint ("points", "spatialReference"),
		esriGeometryPolyline ("paths", "spatialReference"),
		esriGeometryPolygon ("rings", "spatialReference"),
		esriGeometryEnvelope ("xmin", "ymin", "xmax", "ymax", "spatialReference");
		
		private final String[] contains;
		EsriGeometryType(String... contains){
			this.contains = contains;
		}
		public String[] getContainsKey() {
			return contains;
		}
	}
	
	private void checkSupportedKeysInJson(Map<String, Object> geometryMap, EsriGeometryType esriGeometryType) throws ParseException {
		String msg = null;
		String supportKeys = null;
		switch(esriGeometryType) {
			case esriGeometryPoint:
				msg = validateEsriGeometryJson(geometryMap, EsriGeometryType.esriGeometryPoint);
				break;
			case esriGeometryMultipoint:
				msg = validateEsriGeometryJson(geometryMap, EsriGeometryType.esriGeometryMultipoint);
				break;
			case esriGeometryPolyline:
				msg = validateEsriGeometryJson(geometryMap, EsriGeometryType.esriGeometryPolyline);
				break;
			case esriGeometryPolygon:
				msg = validateEsriGeometryJson(geometryMap, EsriGeometryType.esriGeometryPolygon);
				break;
			default:
				break;
		}
		
		if (msg != null) {
			throw new ParseException("Could not parse Geometry from Json string. Can't allow '"+msg+"' property and supported keys are '"+supportKeys+"'.");
		}
	}
	
	private String validateEsriGeometryJson(Map<String, Object> geometryMap, EsriGeometryType esriGeometryType) {
		Set<String> keySupportInJson = new HashSet<String>(Arrays.asList(esriGeometryType.getContainsKey()));
		Set<String> names =  geometryMap.keySet();
		
		StringBuilder msg = null;
		String delimiter = ", ";
		
		for(String name : names) {
			if(!keySupportInJson.contains(name)) {
				
				if(msg == null) msg = new StringBuilder();
				else msg.append(delimiter);
				
				msg.append(name);
			}
		}
		return (msg != null) ? msg.toString() : null;
	}
	
	private boolean isFound(String supportKey, String jsonKey) {
		if(supportKey.equals(jsonKey)) return true;
		return false;
	}
	
	private boolean isEsriGeometryType(EsriGeometryType type, Set<String> jsonKeys) {
		int count = 0;
		for(String jsonKey : jsonKeys) {
			for(String name : type.contains) {
				if(name.equals(EsriJsonConstants.NAME_SPATIAL_REFERENCE.getName()))continue;
				if(isFound(name, jsonKey))
					count++;
			}
		}
		if(count > 0) return true;
		return false;
	}
	
	private EsriGeometryType getEsriGeometryType(Map<String, Object> geometryMap) {
		Set<String> jsonKeys =  geometryMap.keySet();
		for(EsriGeometryType type : EsriGeometryType.values()) {
			if (isEsriGeometryType(type, jsonKeys)) {
				return type;
			}	
		}
		return null;
	}
}
