state =
    LOAD '/user/hduser/tsv/state.tsv'
	AS (code:int,abbr:chararray,name:chararray);

feature =
    LOAD '/user/hduser/tsv/feature.tsv'
	AS (name:chararray,type:chararray,county:chararray,state_name:chararray,latitude:double,longitude:double,elevation:int);

populated_place =
    LOAD '/user/hduser/tsv/populated_place.tsv'
	AS (name:chararray,county:chararray,state_code:int,county_code:int,latitude:double,longitude:double,elevation:int,population:int,federal_status:chararray,cell_name:chararray);



population_by_name = 
	FOREACH populated_place 
	GENERATE population, name, state_code;


population_not_null = 
	FILTER population_by_name
	BY population IS NOT NULL;

state_population_by_name = 
	JOIN population_not_null BY state_code,
	     state BY code;

statename_population_by_name =
	FOREACH state_population_by_name
	GENERATE state::name AS state_name,
		     population_not_null::name AS name,
		     population_not_null::population AS population; 


populated_place_by_state = 
	GROUP statename_population_by_name
	BY state_name;


population_in_order_5 = 
	FOREACH populated_place_by_state {
	sorted = ORDER statename_population_by_name BY population DESC;
	limit_sort = LIMIT sorted 5;
	GENERATE group AS state_name, limit_sort;
}


flatten_population_in_order_5 = 
	FOREACH population_in_order_5 
	GENERATE state_name, flatten(limit_sort);


result_in_state_order = 
	FOREACH flatten_population_in_order_5
	GENERATE state_name,
		     limit_sort::name AS name,
		     limit_sort::population AS population;

DUMP result_in_state_order;



