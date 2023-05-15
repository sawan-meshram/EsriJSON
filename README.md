# EsriJSON

This project is used to read or write the Esri JSON geometry to JTS Geometry object.

The Esri JSON formats of the geometry generally returned by ArcGIS REST API along with spatial reference objects. The supported geometry types are as follows:

* Point
* Multipoint
* Polyline
* Polygon
* Envelope *`(currently not supported by project)`*

## Reading EsriJSON

### Point
The input EsriJSON
```
{
  "x": -103.20762634277341,
  "y": 35.04658137962862,
  "spatialReference": {
    "wkid": 4326
  }
}
```
After reading, result in JTS Point Geometry as follows
```
POINT (-103.20762634277341 35.04658137962862)
```
### Multipoint
The input EsriJSON

```
{
  "points": [
    [-97.06138, 32.837],
    [-97.06133, 32.836],
    [-97.06124, 32.834],
    [-97.06127, 32.832]
  ],
  "spatialReference": {
    "wkid": 4326
  }
}
```
After reading, result in JTS MultiPoint Geometry as follows
```
MULTIPOINT ((-97.06138 32.837), (-97.06133 32.836), (-97.06124 32.834), (-97.06127 32.832))
```
### Polyline
Currently, the library is able to distinguish the EsriJSON contains either LineString or MultiLineString JTS geometries.

1] The input EsriJSON contains single paths
```
{
  "paths": [
    [
      [-97.06138, 32.837],
      [-97.06133, 32.836],
      [-97.06124, 32.834],
      [-97.06127, 32.832]
    ]
  ],
  "spatialReference": {
    "wkid": 4326
  }
}
```
After reading, result in JTS LineString Geometry as follows
```
LINESTRING (-97.06138 32.837, -97.06133 32.836, -97.06124 32.834, -97.06127 32.832)
```
2] The input EsriJSON contains more than single paths
```
{
  "paths": [
    [
      [-97.06138, 32.837],
      [-97.06133, 32.836],
      [-97.06124, 32.834],
      [-97.06127, 32.832]
    ],
    [
      [-97.06326, 32.759],
      [-97.06298, 32.755]
    ]
  ],
  "spatialReference": {
    "wkid": 4326
  }
}
```
After reading, result in JTS MultiLineString Geometry as follows
```
MULTILINESTRING ((-97.06138 32.837, -97.06133 32.836, -97.06124 32.834, -97.06127 32.832), (-97.06326 32.759, -97.06298 32.755))
```
### Polygon
1] The input EsriJSON contains single polygon
```
{
  "rings": [
    [
      [-97.13141441345215, 32.77385236175715],
      [-97.12450504302979, 32.77569261707643],
      [-97.11291790008544, 32.7747905358906],
      [-97.11502075195312, 32.76916134273928],
      [-97.13141441345215, 32.77385236175715]
    ]
  ],
  "spatialReference": {
    "wkid": 4326
  }
}
```
After reading, result in JTS Polyogn Geometry as follows
```
POLYGON ((-97.13141441345215 32.77385236175715, -97.12450504302979 32.77569261707643, -97.11291790008544 32.7747905358906, -97.11502075195312 32.76916134273928, -97.13141441345215 32.77385236175715))
```
2] The input EsriJSON contains single polygon with hole
```
{
  "rings": [
    [
      [-97.13141441345215, 32.77385236175715],
      [-97.12450504302979, 32.77569261707643],
      [-97.11291790008544, 32.7747905358906],
      [-97.11502075195312, 32.76916134273928],
      [-97.13141441345215, 32.77385236175715]
    ],
    [
      [-97.12358236312866, 32.77457403504532],
      [-97.12469816207887, 32.773509564895136],
      [-97.1170163154602, 32.77228270214884],
      [-97.11450576782227, 32.7731487246666],
      [-97.12358236312866, 32.77457403504532]
    ]
  ],
  "spatialReference": {
    "wkid": 4326
  }
}
```
After reading, result in JTS Polyogn Geometry as follows
```
POLYGON ((-97.13141441345215 32.77385236175715, -97.12450504302979 32.77569261707643, -97.11291790008544 32.7747905358906, -97.11502075195312 32.76916134273928, -97.13141441345215 32.77385236175715), (-97.12358236312866 32.77457403504532, -97.12469816207887 32.773509564895136, -97.1170163154602 32.77228270214884, -97.11450576782227 32.7731487246666, -97.12358236312866 32.77457403504532))
```
3] The input EsriJSON contains single polygon with hole and another polygon.

*(**Note:-** If JSON contains more the one Polygon,the EsriReader only return JTS Polygon geometry. If user want to check if it is MultiPolygon, then user has to check and convert this Polygon to MultiPolygon.)*
```
{
  "rings": [
    [
      [-97.13141441345215, 32.77385236175715],
      [-97.12450504302979, 32.77569261707643],
      [-97.11291790008544, 32.7747905358906],
      [-97.11502075195312, 32.76916134273928],
      [-97.13141441345215, 32.77385236175715]
    ],
    [
      [-97.12358236312866, 32.77457403504532],
      [-97.12469816207887, 32.773509564895136],
      [-97.1170163154602, 32.77228270214884],
      [-97.11450576782227, 32.7731487246666],
      [-97.12358236312866, 32.77457403504532]
    ],
    [
      [-97.11066484451294, 32.77343739696647],
      [-97.10875511169435, 32.774213199132774],
      [-97.10819721221924, 32.77201206838387],
      [-97.10909843444824, 32.76964849852645],
      [-97.10989236831665, 32.770460418913416],
      [-97.11066484451294, 32.77343739696647]
    ]
  ],
  "spatialReference": {
    "wkid": 4326
  }
}
```
After reading, result in JTS Polyogn Geometry as follows
```
POLYGON ((-97.13141441345215 32.77385236175715, -97.12450504302979 32.77569261707643, -97.11291790008544 32.7747905358906, -97.11502075195312 32.76916134273928, -97.13141441345215 32.77385236175715), (-97.12358236312866 32.77457403504532, -97.12469816207887 32.773509564895136, -97.1170163154602 32.77228270214884, -97.11450576782227 32.7731487246666, -97.12358236312866 32.77457403504532), (-97.11066484451294 32.77343739696647, -97.10875511169435 32.774213199132774, -97.10819721221924 32.77201206838387, -97.10909843444824 32.76964849852645, -97.10989236831665 32.770460418913416, -97.11066484451294 32.77343739696647))
```

## Example code for reading and writing the EsriJSON

### EsriJsonReader
```
EsriJsonReader reader = new EsriJsonReader();
String point = "{\"x\":-103.20762634277341,\"y\":35.04658137962862,\"spatialReference\":{\"wkid\":4326}}";
Geometry g = reader.read(point);
System.out.println(g);
```

### EsriJsonWriter
```
String point = "POINT(-103.56811523437501 35.37113502280101)";
Geometry g = GeometryUtil.toGeometry(point);
g.setSRID(4326);
EsriJsonWriter writer = new EsriJsonWriter();
String json = writer.write(g);
System.out.println(json);
```
### Updates
* If JSON contains more the one Polygon,the EsriReader only return JTS Polygon geometry. If user want to check if it is MultiPolygon, then user has to check and convert this Polygon to MultiPolygon.
```
Geometry g = reader.read(multipolygonWithHolesAndOtherPoly); //multipolygonWithHolesAndOtherPoly is input EsriJSON
System.out.println(g);
Polygon p1 = (Polygon)g;
Geometry g1 = EsriGeometryUtil.convertPolygonOrMultiPolygon(p1);
System.out.println(g1);
```

## Notes
Currently, future objectives to develop
* EsriJsonWriter is not supported to write GeometryCollection JTS object.
* EsriJsonWriter is not supported to write Envelope JTS object.
* EsriJsonReader is not supported to read Envelope EsriJSON.
* EsriJsonReader can not distinguish EsriJSON contains either single Polygon or MultiPolygon, it's only able to give as a Polygon.

