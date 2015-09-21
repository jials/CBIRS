import argparse
from cPickle import load
from learn import extractSift, computeHistograms, writeHistogramsToFile

HISTOGRAMS_FILE = 'visual_words_for_test_data'
CODEBOOK_FILE = 'codebook_b.file'
IMAGE_INDEX_FILE = 'image_indexe_test'

def parse_arguments():
    parser = argparse.ArgumentParser(description='generate visual words histogram for test image')
    parser.add_argument('-c', help='path to the codebook file', required=False, default=CODEBOOK_FILE)
    parser.add_argument('input_image', help='path to input image', nargs='+')
    args = parser.parse_args()
    return args


print "---------------------"
print "## extract Sift features"
all_files = []
all_features = {}

args = parse_arguments()
codebook_file = args.c
fnames = args.input_image
all_features = extractSift(fnames)

print "---------------------"
print "## loading codebook from " + codebook_file
with open(codebook_file, 'rb') as f:
    codebook = load(f)

print "---------------------"
print "## computing visual word histograms"
all_word_histgrams = {}
for imagefname in all_features:
    word_histgram = computeHistograms(codebook, all_features[imagefname])
    all_word_histgrams[imagefname] = word_histgram

print "---------------------"
print "## write the histograms to file"
nclusters = codebook.shape[0]
writeHistogramsToFile(nclusters,
                      fnames,
                      all_word_histgrams,
                      IMAGE_INDEX_FILE,
                      HISTOGRAMS_FILE)

print "---------------------"
print "finished"

