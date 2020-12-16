var map = L.map('map')

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png?{foo}', {foo: 'bar', attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'})
    .addTo(map);

map.setView([51.505, -0.09], 13);

function makePairs(data) {
    return data.reduce((result, value, index, array) => {
        if (index % 2 === 0) {
            result.push(array.slice(index, index + 2));
        }

        return result
    }, []);
}
var lay = null;

function getData(ts) {
    fetch('/api/data?range='+ts)
        .then(x => x.json())
        .then(data => {
            var geo = data.data;
            var latlngs = makePairs(geo)
            if (lay) {map.removeLayer(lay)}
            lay = L.polyline(latlngs, {color: 'red'});
            lay.addTo(map);

            // zoom the map to the polyline
            map.fitBounds(lay.getBounds());
        })
}

fetch('/api/dates')
    .then(x => x.json())
    .then(data => {
        var days = data.days;
        var sel = document.getElementById('day')
        sel.onchange = function() {
            var interval = sel.value
            if (sel.selectedIndex - 1 !== undefined) {
                interval += ':'+sel.options[sel.selectedIndex-1].value
            }
            getData(interval)
        }

        days.forEach(d => {
            var option = document.createElement("option");
            option.text = new Date(d*1000).toDateString();
            option.value = d;
            sel.add(option)
        })

    })