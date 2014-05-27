state =
    LOAD '/user/hduser/tsv/state.tsv'
	AS (code:int,abbr:chararray,name:chararray);

feature =
    LOAD '/user/hduser/tsv/feature.tsv'
AS (name:chararray,type:chararray,county:chararray,state_name:chararray,latitude:double,longitude:double,elevation:int);

populated_place =
    LOAD '/user/hduser/tsv/populated_place.tsv'
AS (name:chararray,county:chararray,state_code:int,county_code:int,latitude:double,longitude:double,elevation:int,population:int,federal_status:chararray,cell_name:chararray);

-- Project the columns in 'populated_place' for later use 
population_elevation_by_name = 
	FOREACH populated_place
	GENERATE population, elevation, state_code;
	
--Get the population, elevation by state_code
group_by_state = 
	GROUP population_elevation_by_name
	BY state_code;

--Get the sum of population, sum of elevation, number of elevation for each state
population_elevation_by_state = 
	FOREACH group_by_state 
	GENERATE SUM(population_elevation_by_name.population) AS population_state,
		     ROUND(AVG(population_elevation_by_name.elevation)) AS average_elevation,
		     group AS state_code;

--Find those state_name in 'state' that match the state_code 	
state_and_population_and_averageelevation_by_state = 
	JOIN population_elevation_by_state BY state_code,
	     state BY code;
		 
--Get the state_name, population, average elevation
state_and_population_and_averageelevation = 
	FOREACH state_and_population_and_averageelevation_by_state
	GENERATE state::name AS state_name,
		 	 population_elevation_by_state::population_state AS population, 
		     population_elevation_by_state::average_elevation AS elevation;

--Sort by state_name
state_population_averageelevation_in_order = 
	ORDER state_and_population_and_averageelevation
	BY state_name;

dump state_population_averageelevation_in_order;
