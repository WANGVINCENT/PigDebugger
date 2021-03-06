populated_place = 
	LOAD '/user/hduser/tsv/populated_place.tsv' AS (name:chararray, county:chararray, state_code:int, county_code:int, latitude:double, longitude:double, elevation:int, population:int, federal_status:chararray, cell_name:chararray);

states = 
	LOAD '/user/hduser/tsv/state.tsv' AS (code:int,abbr:chararray,name:chararray);

states_infos = 
	FOREACH states GENERATE code, name;

populated_place_infos = 
	FOREACH populated_place GENERATE population, 
									 state_code AS code; 

group_populated_place = 
	GROUP populated_place_infos BY code;

population_per_state = 
	FOREACH group_populated_place GENERATE flatten(group) AS code, 
										   SUM(populated_place_infos.population) AS population;

join_states_population_per_state = 
	JOIN states_infos BY code, population_per_state BY code; 

population_name = 
	FOREACH join_states_population_per_state GENERATE states_infos::name AS name,
						                              population_per_state::population AS population; 
                          
population_per_state_order = ORDER population_name BY population DESC;

ten_in_population_per_state_order = LIMIT population_per_state_order 10;

DUMP ten_in_population_per_state_order;