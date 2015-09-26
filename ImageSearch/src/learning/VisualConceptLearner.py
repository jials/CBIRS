import sys
import os
import getopt
import collections


def build(training_dir, output_file):
	INITIAL_INDEX = 1
	FINAL_INDEX = 1001
	filepaths = []
	conceptId = []
	conceptId_to_nameScore = collections.defaultdict(list)

	# find all the txt files in /train/data directory
	for root, dirs, files in os.walk(training_dir):
		for file in files:
			if file.endswith(".txt"):
				filepaths.append(os.path.join(root, file))
	
	for filepath in filepaths:
		indices = range(INITIAL_INDEX, FINAL_INDEX)	
		with open(filepath, "r") as f:
			for line in f:
				line = line.strip()
				scores = line.split(" ")
				scores = [float(i) for i in scores]
				output = [(i,j) for (i,j) in zip(indices, scores) if j > 0]
				output = sorted(output, key=lambda x: -x[1])
				# output = [i for (i,j) in output]
				# print output
				for index, score in output:
					conceptId_to_nameScore[index].append((filepath, score))
					if index not in conceptId:
						conceptId.append(index)

	with open(output_file, "w+") as f:
		for index in indices:
			if index not in conceptId:
				f.write("\n")
			else:
				for index, score in conceptId_to_nameScore[index]:
					f.write(str(index) + " ")
					# f.write(str(index) + " " + str(score) + " ")
				f.write("\n")


def usage():
	print "usage: " + sys.argv[0] + " -t training-directory-with-txts -c output-file.txt"


training_i = output = None
try:
	opts, args = getopt.getopt(sys.argv[1:], 't:c:')
except getopt.GetoptError, err:
	usage()
	sys.exit(2)
for o,a in opts:
	if o == '-t':
		training_i = a
	elif o == '-c':
		output = a
	else:
		assert False, "unhandled option"
if training_i is None or output is None:
	print training_i, output
	usage()
	sys.exit(2)

build(training_i, output)