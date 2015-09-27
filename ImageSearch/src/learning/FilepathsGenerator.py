import sys
import os
import getopt

def build(training_dir, output_file):
	filepaths = []

	for root, dirs, files in os.walk(training_dir):
		for file in files:
			if file.endswith(".jpg"):
				filepaths.append(os.path.abspath(os.path.join(root, file)))

	with open(output_file, "w+") as f:
		for filepath in filepaths:
			f.write(filepath + "\n")

	print "Successfully generated filepaths of img files in this directory!"


def usage():
	print "usage: " + sys.argv[0] + " -t training-dir -c output.txt"


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
	usage()
	sys.exit(2)

build(training_i, output)