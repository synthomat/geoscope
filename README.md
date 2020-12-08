# geoscope

GeoScope is a receiver app for GPS tracking data that is procuded by the [Overland App](https://github.com/aaronpk/Overland-iOS).

**Project Status: EXPERIMENTAL**  
The project in it's current development state can be considered _experimental_; It actually can only receive the data but doesn't do much more right now.

## Prerequisites

**Common requirements** (production)
* JRE 8+
* [PostGIS][] (Postgres + Geo extension)

**Development requirements** (additional to production)
* JDK 8+
* [Leiningen][] 2.0.0+
* GNU Make

[leiningen]: https://github.com/technomancy/leiningen
[postgis]: https://postgis.net/

## Build
**Uberjar** (self-contained `jar` file)

    $ make build  # or just `make`

**Docker Image**

    $ make image

## Running

To start a web server for the application, run:

    $ DATABASE_URL=postgresql://localhost/database lein ring server

---

The whole stack (Application + Database)

**IMPORTANT**  
If you intend to use this in production, you NEED to change the `DATABASE_PASSWORD`! 

    $ docker-compose up 

## License

Copyright © 2020 synthomat
