text =  LOAD '/user/hduser/tsv/text2.tsv';
words = FOREACH text GENERATE FLATTEN(TOKENIZE((chararray)$0)) AS word; 
word_tuples = GROUP words BY word PARALLEL 6;
results = FOREACH word_tuples GENERATE COUNT(words), group;
DUMP results;

