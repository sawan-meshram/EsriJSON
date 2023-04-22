/**
 * @author Sawan Meshram
 */
package com.jts.io.esrijson;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.Assert;

public class EsriJsonWriter {
	private double scale;
	
	private boolean isEncodeSR = true;

	public EsriJsonWriter() {
		this(8);
	}

	public EsriJsonWriter(int decimals) {
		this.scale = Math.pow(10, decimals);
	}
	
	public void setEncodeSR(boolean isEncodeSR) {
		this.isEncodeSR  = isEncodeSR;
	}
	
	public String write(Geometry geometry) {
		StringWriter writer = new StringWriter();
		try {
			write(geometry, writer);
		} catch (IOException ex) {
			Assert.shouldNeverReachHere();
		}
		return writer.toString();
	}
	
	public void write(Geometry geometry, Writer writer) throws IOException {
		Map<String, Object> map = create(geometry, isEncodeSR);
		JSONObject.writeJSONString(map, writer);
		writer.flush();
	}
	
	private Map<String, Object> create(Geometry geometry, boolean encodeSR) {

		Map<String, Object> result = new LinkedHashMap<String, Object>();

 		if (geometry instanceof Point) {
			Point point = (Point) geometry;
			
			result.put(EsriJsonConstants.NAME_X.getName(), new JSONAware() {
				public String toJSONString() {
					return formatOrdinate(point.getCoordinateSequence().getOrdinate(0, CoordinateSequence.X));
				}
			});
			
			result.put(EsriJsonConstants.NAME_Y.getName(), new JSONAware() {
				public String toJSONString() {
					return formatOrdinate(point.getCoordinateSequence().getOrdinate(0, CoordinateSequence.Y));
				}
			});
			
			if (point.getCoordinateSequence().getDimension() > 2 ) {
				double z = point.getCoordinateSequence().getOrdinate(0, CoordinateSequence.Z);
				if (!  Double.isNaN(z)) {
					result.put(EsriJsonConstants.NAME_Y.getName(), new JSONAware() {
						public String toJSONString() {
							return formatOrdinate(z);
						}
					});
				}
			}
			
		} else if (geometry instanceof LineString) {
			LineString lineString = (LineString) geometry;
	
			final String jsonString = getJsonString(lineString.getCoordinateSequence());
	
			ArrayList<JSONAware> paths = new ArrayList<JSONAware>();

			paths.add(new JSONAware() {
				public String toJSONString() {
					return jsonString;
				}
			});
			
			result.put(EsriJsonConstants.NAME_PATHS.getName(), paths);
		
		} else if (geometry instanceof Polygon) {
			Polygon polygon = (Polygon) geometry;
			
			result.put(EsriJsonConstants.NAME_RINGS.getName(), makeJsonAware(polygon));

		} else if (geometry instanceof MultiPoint) {
			MultiPoint multiPoint = (MultiPoint) geometry;
			result.put(EsriJsonConstants.NAME_POINTS.getName(), makeJsonAware(multiPoint));
		}  
		else if (geometry instanceof MultiLineString) {
			MultiLineString multiLineString = (MultiLineString) geometry;
	
			result.put(EsriJsonConstants.NAME_PATHS.getName(), makeJsonAware(multiLineString));

		} else if (geometry instanceof MultiPolygon) {
			MultiPolygon multiPolygon = (MultiPolygon) geometry;
	
			result.put(EsriJsonConstants.NAME_RINGS.getName(), makeJsonAware(multiPolygon));

		} 
		else {
			throw new IllegalArgumentException("EsriJson is not supported this geometry '" + geometry.getGeometryType()+"'.");
		}

		if (encodeSR) {
			result.put(EsriJsonConstants.NAME_SPATIAL_REFERENCE.getName(), createCRS(geometry.getSRID()));
		}

		return result;
	}
	
	private Map<String, Object> createCRS(int srid) {
		Map<String, Object> result = new LinkedHashMap<String, Object>();
		result.put(EsriJsonConstants.NAME_WKID.getName(), srid);
		return result;
	}
	
	private List<JSONAware> makeJsonAware(Polygon polygon) {
		ArrayList<JSONAware> result = new ArrayList<JSONAware>();
		
		final String jsonString = getJsonString(polygon.getExteriorRing().getCoordinateSequence());
		result.add(new JSONAware() {
			public String toJSONString() {
				return jsonString;
			}
		});
		
		for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
			final String jsonString1 = getJsonString(polygon.getInteriorRingN(i).getCoordinateSequence());
			result.add(new JSONAware() {
				public String toJSONString() {
					return jsonString1;
				}
			});
		}
		return result;
	}
	
	private List<JSONAware> makeJsonAware(MultiPolygon multiPolygon) {
		ArrayList<JSONAware> result = new ArrayList<JSONAware>();
		
		for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
			Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
			
			final String jsonString = getJsonString(polygon.getExteriorRing().getCoordinateSequence());
			result.add(new JSONAware() {
				public String toJSONString() {
					return jsonString;
				}
			});
			
			for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
				final String jsonString1 = getJsonString(polygon.getInteriorRingN(j).getCoordinateSequence());
				result.add(new JSONAware() {
					public String toJSONString() {
						return jsonString1;
					}
				});
			}
		}
		
		return result;
	}
	
	private List<Object> makeJsonAware(GeometryCollection geometryCollection) {
		ArrayList<Object> list = new ArrayList<Object>(geometryCollection.getNumGeometries());
		
		for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
			Geometry geometry = geometryCollection.getGeometryN(i);
	
			if (geometry instanceof Polygon) {
				Polygon polygon = (Polygon) geometry;
				list.add(makeJsonAware(polygon));
			} 
			else if (geometry instanceof LineString) {
				LineString lineString = (LineString) geometry;
				final String jsonString = getJsonString(lineString.getCoordinateSequence());
				
				list.add(new JSONAware() {
					public String toJSONString() {
						return jsonString;
					}
				});
			} 
			else if (geometry instanceof Point) {
				Point point = (Point) geometry;
				final String jsonString = getJsonString(point.getCoordinateSequence());
				list.add(new JSONAware() {
					public String toJSONString() {
						return jsonString;
					}
				});
			}
		}

		return list;
	}
	
	
	private String getJsonString(CoordinateSequence coordinateSequence) {
		StringBuilder result = new StringBuilder();

		if (coordinateSequence.size() > 1) {
			result.append("[");
		}
		for (int i = 0; i < coordinateSequence.size(); i++) {
			if (i > 0) {
				result.append(",");
			}
			result.append("[");
			result.append(formatOrdinate(coordinateSequence.getOrdinate(i, CoordinateSequence.X))); 
			result.append(",");
			result.append(formatOrdinate(coordinateSequence.getOrdinate(i, CoordinateSequence.Y)));
	
			if (coordinateSequence.getDimension() > 2 ) {
				double z = coordinateSequence.getOrdinate(i, CoordinateSequence.Z);
				if (!  Double.isNaN(z)) {
					result.append(",");
					result.append(formatOrdinate(z));
				}
			}
	
			result.append("]");

		}

		if (coordinateSequence.size() > 1) {
			result.append("]");
		}

		return result.toString();
	}
	

	private String formatOrdinate(double x) {
		String result = null;
		
		if (Math.abs(x) >= Math.pow(10, -3) && x < Math.pow(10, 7)) {
			x = Math.floor(x * scale + 0.5) / scale;
			long lx = (long) x;
			if (lx == x) {
				result = Long.toString(lx);
			} else {
				result = Double.toString(x);
			}
		} else {
			result = Double.toString(x);
		}
		
		return result;
	}
}
