from annoy import AnnoyIndex
import random
import pandas as pd
import numpy as np
import hashlib
import time

f = 350
t = AnnoyIndex(f)  # Length of item vector that will be indexed



#for i in range(1000):
#    v = [random.gauss(0, 1) for z in range(f)]
#    t.add_item(i, v)


chunksize = 10*10
#nrows=2
start_index_time = time.time()
for chunk in pd.read_csv('doctopics.csv.gz', chunksize=chunksize, compression='gzip', header=None, error_bad_lines=False):
    for index, row in chunk.iterrows():
        if index%1000==0:
            print(index)
        t.add_item(index, row.tail(f).values)


t.build(10) # 10 trees
t.save('test.ann')
elapsed_index_time = time.time() - start_index_time
print("index time",elapsed_index_time)
# ...

u = AnnoyIndex(f)
u.load('test.ann') # super fast, will just mmap the file
start_query_time = time.time()
print(u.get_nns_by_item(0, 10)) # will find the 1000 nearest neighbors
elapsed_query_time = time.time() - start_query_time
print("query time",elapsed_query_time)