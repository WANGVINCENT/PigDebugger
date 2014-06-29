
REGISTER '/home/hduser/PigDebugger/UDFs/udfs.jar';
feature =
    LOAD '/user/hduser/tsv/feature.tsv'
	AS (name:chararray,type:chararray,county:chararray,state_name:chararray,latitude:double,longitude:double,elevation:int);
B = FOREACH feature GENERATE myudfs.UPPER(name);
DUMP B;
