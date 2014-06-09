state =
    LOAD '/user/hduser/tsv/state.tsv'
	AS (code:int,abbr:chararray,name:chararray);

feature =
    LOAD '/user/hduser/tsv/feature.tsv'
	AS (name:chararray,type:chararray,county:chararray,state_name:chararray,latitude:double,longitude:double,elevation:int);

populated_place =
    LOAD '/user/hduser/tsv/populated_place.tsv'
	AS (name:chararray,county:chararray,state_code:int,county_code:int,latitude:double,longitude:double,elevation:int,population:int,federal_status:chararray,cell_name:chararray);


type_in_feature = 
	FOREACH feature
	GENERATE state_name, county, type;


ppl_stream_in_feature = 
	FILTER type_in_feature
	BY type == 'ppl'OR type=='stream';


ppl_in_feature = 
	FILTER type_in_feature
	BY type == 'ppl';
 

stream_in_feature = 
	FILTER type_in_feature
	BY type == 'stream';
	

group_ppl = 
	GROUP ppl_in_feature
	BY county PARALLEL 2;

	
group_stream = 
	GROUP stream_in_feature
	BY county;


num_ppl = 
	FOREACH group_ppl
	GENERATE COUNT(ppl_in_feature.type) AS no_ppl,
	group AS county;


num_stream = 
	FOREACH group_stream
	GENERATE COUNT(stream_in_feature.type) AS no_stream,
	group AS county;
	

state_county_ppl = 
	JOIN ppl_stream_in_feature BY county LEFT OUTER,
	     num_ppl BY county;


state_county_ppl_stream = 
	JOIN state_county_ppl BY ppl_stream_in_feature::county LEFT OUTER,
	     num_stream BY county;


state_county_noppl_nostream = 
	FOREACH state_county_ppl_stream
	GENERATE ppl_stream_in_feature::state_name AS state_name,
		 	 ppl_stream_in_feature::county AS county,
		     num_ppl::no_ppl AS no_ppl,
		     num_stream::no_stream AS no_stream;


unique_state_county_noppl_nostream = 
	DISTINCT state_county_noppl_nostream;


unique_state_county_noppl_nostream_order = 
	ORDER unique_state_county_noppl_nostream 
	BY state_name, county;

dump unique_state_county_noppl_nostream_order;