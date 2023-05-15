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
    [
      -97.06138,
      32.837
    ],
    [
      -97.06133,
      32.836
    ],
    [
      -97.06124,
      32.834
    ],
    [
      -97.06127,
      32.832
    ]
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

