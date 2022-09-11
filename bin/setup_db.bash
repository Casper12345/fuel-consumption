#!/bin/bash

psql postgres -h localhost -c "create database fuel_consumption" -c "create user fuel_consumer"
psql fuel_consumption -h localhost -c "create table fuel_consumption(imo int, draught numeric, speed numeric, beaufort numeric, consumption numeric)"
psql fuel_consumption -h localhost -c "grant select,insert on table fuel_consumption to fuel_consumer"

java -cp "jars/fuel-consumption-csv-parser-assembly-0.1.0-SNAPSHOT.jar" com.fuel.parser.init.Main