#!/bin/bash

curl -XPOST localhost:8080/estimate-consumption -d "$(cat samples/request4.json)"