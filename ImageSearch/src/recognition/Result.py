import sys
import os
import getopt
import collections


def build(dictionary_dir, scores_file):
	INITIAL_INDEX = 1
	FINAL_INDEX = 1001
	conceptId = []
	conceptId_to_name = collections.defaultdict(list)
	count = 1

	with open(dictionary_dir, "r") as f:
		for line in f:
			line = line.strip()
			if line:
				names = line.split(' ')
				if len(names) > 0:
					for name in names:
						conceptId_to_name[count].append(name)
					# for i in range(0, len(nameScores), 2):
						# conceptId_to_nameScore[count].append((nameScores[i], nameScores[i+1]))
			count += 1

        #print conceptId_to_name
    
	with open(scores_file, "r") as f:
		indices = range(INITIAL_INDEX, FINAL_INDEX)
		for line in f:
				line = line.strip().rstrip()
				scores = line.split(" ")
				scores = [float(i) for i in scores]
				output = [(i,j) for (i,j) in zip(indices, scores) if j > 0]
				output = sorted(output, key=lambda x: -x[1])

	result = []
	for conceptId, score in output:
		if len(result) < 21:
			result.extend(conceptId_to_name[conceptId])
		else:
			break
	
	if len(result) < 21:
		for e in result:
			print e
	else:
		for i in range(0, 21):
			print result[i]


def usage():
	print "usage: " + sys.argv[0] + " -d dictionary.txt -s scores.txt"


dictionary_i = scores_f = None
try:
	opts, args = getopt.getopt(sys.argv[1:], 'd:s:')
except getopt.GetoptError, err:
	usage()
	sys.exit(2)
for o,a in opts:
	if o == '-d':
		dictionary_i = a
	elif o == '-s':
		scores_f = a
	else:
		assert False, "unhandled option"
if dictionary_i is None or scores_f is None:
	usage()
	sys.exit(2)

build(dictionary_i, scores_f)
