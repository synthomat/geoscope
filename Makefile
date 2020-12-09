.PHONY: image all clean


uberjar:
	lein uberjar

all: uberjar

clean:
	rm -rf target/*

image: uberjar
	docker build -t geoscope:latest -f docker/Dockerfile .