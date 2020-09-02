var styles = {
    'route': new ol.style.Style({
        stroke: new ol.style.Stroke({
            width: 3,
            color: "red"
        })
    }),
    'geoMarker': new ol.style.Style({
        image: new ol.style.Circle({
            radius: 7,
            snapToPixel: false,
            fill: new ol.style.Fill({
                color: 'black'
            }),
            stroke: new ol.style.Stroke({
                color: 'white',
                width: 2
            })
        })
    })
};


var vectorLayer = new ol.layer.Vector({
    source: new ol.source.Vector(),
    style: function(feature) {
        return styles[feature.get('type')];
    }
});

var map = new ol.Map({
    target: 'map',
    layers: [
        new ol.layer.Tile({
            source: new ol.source.OSM()
        }),
        vectorLayer
    ],
    view: new ol.View({
        center: ol.proj.fromLonLat([
                                           11.565771,
                                           48.187237
                                       ]),
        zoom: 16
    })
});

fetch("/data")
  .then(resp => resp.json())
  .then(json => {
    var data = json.data
    var points = data.map(d => d.point)
    var route = new ol.geom.LineString(points)
        .transform('EPSG:4326', 'EPSG:3857');

    var routeCoords = route.getCoordinates();

    var routeFeature = new ol.Feature({
        type: 'route',
        geometry: route
    });

    var geoMarker = new ol.Feature({
        type: 'geoMarker',
        geometry: new ol.geom.Point(routeCoords[0])
    });

    vectorLayer.getSource().addFeature(routeFeature)
    vectorLayer.getSource().addFeature(geoMarker)
    map.setView(new ol.View({
                        center: ol.proj.fromLonLat(points[0]),
                        zoom: 14
                    }))
  })