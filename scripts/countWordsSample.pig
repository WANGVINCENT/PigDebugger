text =  LOAD '/user/hduser/wikisample.sql';
words = FOREACH text GENERATE FLATTEN(TOKENIZE((chararray)$0)) AS word; 
word_tuples = GROUP words BY word PARALLEL 8;

results = FOREACH word_tuples GENERATE group AS word, COUNT(words) AS occurence;
--ordered_results = ORDER results BY occurence DESC PARALLEL 10;
DUMP results;