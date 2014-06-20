
REGISTER /home/hduser/PigDebugger/myudfs.jar;
A = LOAD '/home/hduser/student.tsv' AS (name: chararray, age: int, gpa: float);
B = FOREACH A GENERATE myudfs.UPPER(name);
DUMP B;
