var root = document.body

var Selector = {
  view: () => {
    return [
      m("span", "from:"),
      m("input", {type: "date"}),
      m("span", "to:"),
      m("input", {type: "date"})
    ]
  }
}

var Hello = {
  view: () => {
    return m("main", [
      m("h1", {class: "title"}, "GeoScope"),
      m(Selector),
      m("div", {id: "map", style: "width: 100%; height: 600px"}),
    ])
  }
}

m.mount(root, Hello)

var styles = {
  'route': new ol.style.Style({
    stroke: new ol.style.Stroke({ width: 3, color: "red" })
  }),
  'geoMarker': new ol.style.Style({
    image: new ol.style.Circle({
      radius: 6,
      fill: new ol.style.Fill({ color: 'black' }),
      stroke: new ol.style.Stroke({ color: 'white', width: 2 })
    })
  })
};

var vectorLayer = new ol.layer.Vector({
  source: new ol.source.Vector(),
  style: (f) => styles[f.get('type')]
});

var map = new ol.Map({
  target: 'map',
  layers: [
    new ol.layer.Tile({ source: new ol.source.OSM() }),
    vectorLayer
  ],
  view: new ol.View({
    center: ol.proj.fromLonLat([11.565771, 48.187237]),
    zoom: 16
  })
})


function makePairs(data) {
  return data.reduce((result, value, index, array) => {
    if (index % 2 === 0) {
      result.push(array.slice(index, index + 2));
    }

    return result
  }, []);
}



var fetchData = () => new Promise((resolve, reject) => {
  fetch("/data" + location.search)
    .then(resp => resp.json())
    .then(json => {
      var data = json.data

      var points = makePairs(data)

      resolve(points);
    })
})

fetchData().then(points => {
  var route = new ol.geom.LineString(points).transform('EPSG:4326', 'EPSG:3857');

  var routeCoords = route.getCoordinates();

  var routeFeature = new ol.Feature({
    type: 'route',
    geometry: route
  });

  var geoMarker = new ol.Feature({
    type: 'geoMarker',
    geometry: new ol.geom.Point(routeCoords[routeCoords.length-1])
  });

  vectorLayer.getSource().addFeature(routeFeature)
  vectorLayer.getSource().addFeature(geoMarker)

  var view = new ol.View({
    center: ol.proj.fromLonLat(points[points.length -1]),
    zoom: 14
  })

  map.setView(view)
})
