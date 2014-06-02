-- Load the state, county, mcd, place and zip
state = 
    LOAD '/Users/nanwangn/Documents/DataBase/usgs/state.tsv' 
    AS (code:int,abbr:chararray,name:chararray);

feature = 
    LOAD '/Users/nanwangn/Documents/DataBase/usgs/feature.tsv' 
    AS (name:chararray,type:chararray,county:chararray,state_name:chararray,latitude:double,longitude:double,elevation:int);

populated_place = 
    LOAD '/Users/nanwangn/Documents/DataBase/usgs/populated_place.tsv' 
    AS (name:chararray,county:chararray,state_code:int,county_code:int,latitude:double,longitude:double,elevation:int,population:int,federal_status:chararray,cell_name:chararray);


