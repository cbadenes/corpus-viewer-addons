mvn -DargLine="-Xmx4096m" -Dtest=BM25ByDTFEval test
mvn -DargLine="-Xmx4096m" -Dtest=BM25ByHashEval test
mvn -DargLine="-Xmx4096m" -Dtest=BooleanByDTFEval test
mvn -DargLine="-Xmx4096m" -Dtest=BooleanByHashEval test
mvn -DargLine="-Xmx4096m" -Dtest=MLTByDTFEval test
mvn -DargLine="-Xmx4096m" -Dtest=MLTByHashEval test
mvn -DargLine="-Xmx4096m" -Dtest=BruteForceEval test
