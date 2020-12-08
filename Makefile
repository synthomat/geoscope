.PHONY: image all clean


uberjar:
	lein ring uberjar

all: uberjar

clean:
	rm -rf target/*

image: uberjar
	docker build -t geoscope:latest .